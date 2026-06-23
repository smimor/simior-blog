package org.simior.model.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户信息返回对象
 *
 * <p>用于 {@code GET /v1/auth/info} 接口，返回当前登录用户的完整信息。
 * 包含基础用户资料及权限数据（{@code buttons}、{@code roles}），
 * 供前端路由守卫和权限控制使用。</p>
 */
@Data
public class UserInfoVO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 个人简介
     */
    private String intro;

    /**
     * 状态 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 按钮权限列表（前端用于控制按钮级权限）
     */
    private List<String> buttons;

    /**
     * 角色标识列表（如 "admin"、"user"）
     */
    private List<String> roles;
}
