package org.simior.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 轮播图实体类
 *
 * <p>对应数据库表 {@code blog_banner}，用于管理首页轮播图/横幅的展示内容，
 * 包含图片地址、跳转链接、排序权重和启用状态。通过 {@code sort} 字段控制轮播顺序，
 * {@code status} 字段实现快速上下线切换而无需删除数据。</p>
 *
 * <p>使用 {@code @TableLogic} 实现逻辑删除，确保轮播图配置数据可恢复。</p>
 */
@Data
@TableName("blog_banner")
public class BlogBanner implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 轮播图ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 链接URL
     */
    private String linkUrl;

    /**
     * 排序
     */
    private Integer sort;

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
