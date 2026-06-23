package org.simior.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 浏览历史实体类
 *
 * <p>对应数据库表 {@code blog_view_history}，用于记录文章的访问历史。
 * 对于已登录用户通过 {@code userId} 标识身份，对于匿名用户则 {@code userId} 为 {@code null}，
 * 退化为基于 {@code ipAddress} 的访问记录。该设计兼顾了登录用户的精确追踪和匿名用户的基本统计需求。</p>
 */
@Data
@TableName("blog_view_history")
public class BlogViewHistory implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 用户ID(未登录则为NULL)
     */
    private Long userId;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 浏览时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
