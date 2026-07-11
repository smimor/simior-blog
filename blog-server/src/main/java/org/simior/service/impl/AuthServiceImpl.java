package org.simior.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.simior.common.exception.BusinessException;
import org.simior.common.utils.CaptchaUtil;
import org.simior.common.utils.RedisLockUtil;
import org.simior.mapper.UserMapper;
import org.simior.model.dto.LoginDTO;
import org.simior.model.dto.RegisterDTO;
import org.simior.model.entity.SysUser;
import org.simior.model.vo.LoginVO;
import org.simior.model.vo.UserInfoVO;
import org.simior.service.AuthService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String DUMMY_HASH = BCrypt.hashpw("dummy-placeholder");
    private static final String CAPTCHA_KEY_PREFIX = "captcha:";
    private static final String LOGIN_FAIL_KEY_PREFIX = "login:fail:";
    private static final String REGISTER_LOCK_PREFIX = "lock:register:";
    private static final long CAPTCHA_EXPIRE_MINUTES = 5;
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOGIN_LOCK_MINUTES = 15;
    private final UserMapper userMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisLockUtil redisLockUtil;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 0. 校验验证码
        validateCaptcha(loginDTO.getCaptchaId(), loginDTO.getCaptchaCode());

        // 0.5 检查登录失败限流
        checkLoginRateLimit(loginDTO.getUsername());

        // 1. 查询用户
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, loginDTO.getUsername());
        SysUser user = userMapper.selectOne(queryWrapper);

        // 2. 校验用户
        if (user == null) {
            BCrypt.checkpw(loginDTO.getPassword(), DUMMY_HASH); // 消耗与真实校验相同的时间
            recordLoginFailure(loginDTO.getUsername());
            throw new BusinessException("用户名或密码错误");
        }

        // 3. 校验密码（使用BCrypt加密）
        if (!BCrypt.checkpw(loginDTO.getPassword(), user.getPassword())) {
            recordLoginFailure(loginDTO.getUsername());
            throw new BusinessException("用户名或密码错误");
        }

        // 4. 校验用户状态（防止 getStatus() 为 null 时拆箱 NPE）
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        // 5. 登录成功，生成 Token
        StpUtil.login(user.getId());

        // 6. 清除登录失败计数
        clearLoginFailure(loginDTO.getUsername());

        // 7. 返回登录信息
        return buildLoginVO(user);
    }

    @Override
    public Map<String, String> getCaptcha() {
        // 一次性生成验证码文本和图片（确保一致）
        Map<String, String> captcha = CaptchaUtil.generate();
        String code = captcha.get("code");
        String captchaImage = captcha.get("image");

        // 生成唯一 ID
        String captchaId = UUID.randomUUID().toString().replace("-", "");

        // 存入 Redis，5 分钟过期
        String redisKey = CAPTCHA_KEY_PREFIX + captchaId;
        stringRedisTemplate.opsForValue().set(redisKey, code, CAPTCHA_EXPIRE_MINUTES, TimeUnit.MINUTES);

        Map<String, String> result = new HashMap<>();
        result.put("captchaId", captchaId);
        result.put("captchaImage", captchaImage);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO register(RegisterDTO registerDTO) {
        // 1. 校验两次密码是否一致
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new BusinessException("两次密码输入不一致");
        }

        String lockKey = REGISTER_LOCK_PREFIX + registerDTO.getUsername().toLowerCase();
        String lockToken = redisLockUtil.tryLock(lockKey);
        try {
            // 2. 检查用户名是否已存在
            LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysUser::getUsername, registerDTO.getUsername());
            Long count = userMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException("用户名已存在");
            }

            // 3. 检查邮箱是否已存在（如果提供了邮箱）
            if (registerDTO.getEmail() != null && !registerDTO.getEmail().isEmpty()) {
                queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(SysUser::getEmail, registerDTO.getEmail());
                count = userMapper.selectCount(queryWrapper);
                if (count > 0) {
                    throw new BusinessException("邮箱已被注册");
                }
            }

            // 4. 检查手机号是否已存在（如果提供了手机号）
            if (registerDTO.getPhone() != null && !registerDTO.getPhone().isEmpty()) {
                queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(SysUser::getPhone, registerDTO.getPhone());
                count = userMapper.selectCount(queryWrapper);
                if (count > 0) {
                    throw new BusinessException("手机号已被注册");
                }
            }

            // 5. 创建用户
            SysUser user = new SysUser();
            user.setUsername(registerDTO.getUsername());
            user.setPassword(BCrypt.hashpw(registerDTO.getPassword())); // BCrypt加密
            user.setNickname(registerDTO.getNickname() != null ? registerDTO.getNickname() : registerDTO.getUsername());
            user.setEmail(registerDTO.getEmail());
            user.setPhone(registerDTO.getPhone());
            user.setStatus(1); // 默认启用

            // 6. 保存用户
            int result = userMapper.insert(user);
            if (result <= 0) {
                throw new BusinessException("注册失败");
            }

            // 7. 自动登录
            StpUtil.login(user.getId());

            // 8. 返回登录信息
            return buildLoginVO(user);
        } finally {
            redisLockUtil.unlock(lockKey, lockToken);
        }
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    @Override
    public UserInfoVO getCurrentUser() {
        // 获取当前登录用户ID
        Long userId = StpUtil.getLoginIdAsLong();

        // 查询用户信息
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 检查账号状态
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException("账号已被禁用");
        }

        // 转换为 VO
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(user, userInfoVO);

        // 填充权限数据：角色列表 + 按钮权限列表
        List<String> roles = StpUtil.getRoleList();
        List<String> permissions = StpUtil.getPermissionList();
        userInfoVO.setRoles(roles);
        // 当前项目未配置细粒度权限，将角色标识作为按钮权限提供给前端
        userInfoVO.setButtons(permissions.isEmpty() ? roles : permissions);

        return userInfoVO;
    }

    @Override
    public LoginVO refreshToken() {
        Long userId = StpUtil.getLoginIdAsLong();

        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 续签到配置的完整有效期，而非当前剩余时间
        long fullTimeout = cn.dev33.satoken.SaManager.getConfig().getTimeout();
        StpUtil.renewTimeout(fullTimeout);

        return buildLoginVO(user);
    }

    /**
     * 构建登录返回对象
     */
    private LoginVO buildLoginVO(SysUser user) {
        LoginVO loginVO = new LoginVO();
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setNickname(user.getNickname());
        loginVO.setToken(StpUtil.getTokenValue());
        loginVO.setTokenName(StpUtil.getTokenName());
        loginVO.setTokenTimeout(StpUtil.getTokenTimeout());
        return loginVO;
    }

    /**
     * 校验验证码（一次性使用，验证后立即删除）
     */
    private void validateCaptcha(String captchaId, String captchaCode) {
        if (captchaId == null || captchaId.isBlank() || captchaCode == null || captchaCode.isBlank()) {
            throw new BusinessException("验证码不能为空");
        }

        String redisKey = CAPTCHA_KEY_PREFIX + captchaId;
        String cachedCode = stringRedisTemplate.opsForValue().get(redisKey);

        // 立即删除验证码（一次性使用，防止重放）
        stringRedisTemplate.delete(redisKey);

        if (cachedCode == null) {
            throw new BusinessException("验证码已过期，请重新获取");
        }

        if (!cachedCode.equalsIgnoreCase(captchaCode.trim())) {
            throw new BusinessException("验证码错误");
        }
    }

    /**
     * 检查登录失败限流：同一用户名 5 分钟内失败超过 5 次，锁定 15 分钟
     */
    private void checkLoginRateLimit(String username) {
        String failKey = LOGIN_FAIL_KEY_PREFIX + username.toLowerCase();
        String failCount = stringRedisTemplate.opsForValue().get(failKey);
        if (failCount != null) {
            long count;
            try {
                count = Long.parseLong(failCount);
            } catch (NumberFormatException e) {
                // Redis 值异常，清除后放行
                stringRedisTemplate.delete(failKey);
                return;
            }
            if (count >= MAX_LOGIN_ATTEMPTS) {
                Long ttl = stringRedisTemplate.getExpire(failKey);
                long remainingMinutes = (ttl != null && ttl > 0) ? (ttl / 60) + 1 : LOGIN_LOCK_MINUTES;
                throw new BusinessException("登录尝试过多，请 " + remainingMinutes + " 分钟后再试");
            }
        }
    }

    /**
     * 记录登录失败次数
     */
    private void recordLoginFailure(String username) {
        String failKey = LOGIN_FAIL_KEY_PREFIX + username.toLowerCase();
        Long count = stringRedisTemplate.opsForValue().increment(failKey);
        if (count != null && count == 1) {
            // 首次失败，设置过期时间
            stringRedisTemplate.expire(failKey, LOGIN_LOCK_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
        }
    }

    /**
     * 清除登录失败计数（登录成功时调用）
     */
    private void clearLoginFailure(String username) {
        String failKey = LOGIN_FAIL_KEY_PREFIX + username.toLowerCase();
        stringRedisTemplate.delete(failKey);
    }
}
