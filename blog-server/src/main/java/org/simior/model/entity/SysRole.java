package org.simior.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色实体类
 *
 * <p>对应数据库表 {@code sys_role}，用于 RBAC（基于角色的访问控制）模型中的角色定义。
 * {@code roleKey} 字段作为角色的唯一标识符，供 Sa-Token 框架在接口鉴权时进行权限匹配；
 * {@code roleSort} 控制角色列表的展示顺序，{@code status} 支持角色的快速启用/禁用。</p>
 *
 * <p>使用 {@code @TableLogic} 实现逻辑删除，确保角色配置数据在误删后可恢复。</p>
 */
@Data
@TableName("sys_role")
public class SysRole implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色标识
     */
    private String roleKey;

    /**
     * 显示顺序
     */
    private Integer roleSort;

    /**
     * 状态 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 逻辑删除 0-未删除 1-已删除
     */
    @TableLogic
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
