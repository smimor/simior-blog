package org.simior.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.simior.common.exception.BusinessException;
import org.simior.mapper.UserMapper;
import org.simior.model.dto.LoginDTO;
import org.simior.model.dto.RegisterDTO;
import org.simior.model.entity.SysUser;
import org.simior.model.vo.LoginVO;
import org.simior.model.vo.UserInfoVO;
import org.simior.service.AuthService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 认证服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final ConcurrentHashMap<String, Object> REGISTER_LOCKS = new ConcurrentHashMap<>();

    private final UserMapper userMapper;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 1. 查询用户
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, loginDTO.getUsername());
        SysUser user = userMapper.selectOne(queryWrapper);

        // 2. 校验用户
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 3. 校验密码（使用BCrypt加密）
        if (!BCrypt.checkpw(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 4. 校验用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        // 5. 登录成功，生成 Token
        StpUtil.login(user.getId());

        // 6. 返回登录信息
        return buildLoginVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO register(RegisterDTO registerDTO) {
        // 1. 校验两次密码是否一致
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new BusinessException("两次密码输入不一致");
        }

        String lockKey = "register:" + registerDTO.getUsername().toLowerCase();
        Object lock = REGISTER_LOCKS.computeIfAbsent(lockKey, k -> new Object());
        try {
            synchronized (lock) {
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
            }
        } finally {
            REGISTER_LOCKS.remove(lockKey);
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
        if (user.getStatus() == 0) {
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
}
