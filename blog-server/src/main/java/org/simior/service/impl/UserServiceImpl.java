package org.simior.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.simior.common.exception.BusinessException;
import org.simior.mapper.UserMapper;
import org.simior.model.dto.UserCreateDTO;
import org.simior.model.dto.UserUpdateDTO;
import org.simior.model.entity.SysUser;
import org.simior.model.vo.UserVO;
import org.simior.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, SysUser> implements UserService {

    @Override
    public Page<SysUser> pageUsers(Long pageNum, Long pageSize, String username) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();

        // 如果提供了用户名，进行模糊查询
        if (username != null && !username.trim().isEmpty()) {
            queryWrapper.like(SysUser::getUsername, username);
        }

        // 按创建时间倒序排列
        queryWrapper.orderByDesc(SysUser::getCreateTime);

        return this.page(page, queryWrapper);
    }

    @Override
    public SysUser getUserByUsername(String username) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, username);
        return this.getOne(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(UserCreateDTO dto) {
        // 检查用户名是否已存在
        SysUser existUser = getUserByUsername(dto.getUsername());
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(BCrypt.hashpw(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);

        save(user);
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long id, UserUpdateDTO dto) {
        SysUser user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 用户名变更时检查唯一性
        if (dto.getUsername() != null && !dto.getUsername().equals(user.getUsername())) {
            SysUser existUser = getUserByUsername(dto.getUsername());
            if (existUser != null) {
                throw new BusinessException("用户名已存在");
            }
            user.setUsername(dto.getUsername());
        }

        // 密码非空时更新并加密
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(BCrypt.hashpw(dto.getPassword()));
        }

        if (dto.getNickname() != null) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getStatus() != null) {
            user.setStatus(dto.getStatus());
        }

        updateById(user);
    }

    @Override
    public Page<UserVO> pageUserVOs(Long pageNum, Long pageSize, String username) {
        Page<SysUser> userPage = pageUsers(pageNum, pageSize, username);
        Page<UserVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setTotal(userPage.getTotal());
        voPage.setRecords(userPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public List<UserVO> listAllUserVOs() {
        List<SysUser> users = this.list();
        return users.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public UserVO getUserVOById(Long id) {
        SysUser user = getById(id);
        if (user == null) {
            return null;
        }
        return convertToVO(user);
    }

    @Override
    public UserVO getUserVOByUsername(String username) {
        SysUser user = getUserByUsername(username);
        if (user == null) {
            return null;
        }
        return convertToVO(user);
    }

    /**
     * 将 SysUser 转换为 UserVO
     */
    private UserVO convertToVO(SysUser user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
