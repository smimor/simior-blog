package org.simior.config;

import cn.dev33.satoken.stp.StpInterface;
import lombok.RequiredArgsConstructor;
import org.simior.mapper.RoleMapper;
import org.simior.mapper.UserMapper;
import org.simior.model.entity.SysRole;
import org.simior.model.entity.SysUser;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Sa-Token 权限数据加载实现
 * <p>
 * 为 {@code @SaCheckRole} 等注解提供角色和权限数据源。
 * 通过 UserMapper 查询用户角色，通过 RoleMapper 查询角色标识。
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    /**
     * 获取用户权限列表（当前项目未使用细粒度权限，返回空列表）
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return Collections.emptyList();
    }

    /**
     * 获取用户角色列表
     * <p>
     * 根据用户ID查询 sys_user 表获取 roleId，
     * 再根据 roleId 查询 sys_role 表获取 roleKey。
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        SysUser user = userMapper.selectById(Long.valueOf(loginId.toString()));
        if (user == null || user.getRoleId() == null) {
            return Collections.emptyList();
        }

        SysRole role = roleMapper.selectById(user.getRoleId());
        if (role == null || role.getRoleKey() == null || role.getStatus() != 1) {
            return Collections.emptyList();
        }

        return List.of(role.getRoleKey());
    }
}
