package org.simior.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 分类实体类
 *
 * <p>对应数据库表 {@code blog_category}，用于管理文章的分类体系（taxonomy），
 * 每条记录包含分类名称、描述和排序权重。文章通过 {@code BlogArticle.categoryId} 外键与分类关联，
 * 属于一对多关系。通过 {@code sort} 字段控制分类在前端的展示顺序。</p>
 *
 * <p>使用 {@code @TableLogic} 实现逻辑删除，避免删除分类时影响已关联的历史文章数据。</p>
 */
@Data
@TableName("blog_category")
public class BlogCategory implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 分类ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 分类描述
     */
    private String categoryDesc;

    /**
     * 排序
     */
    private Integer sort;

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
