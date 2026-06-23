package org.simior.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * 文章标签关联实体类
 *
 * <p>对应数据库表 {@code blog_article_tag}，是文章（{@code BlogArticle}）与标签（{@code BlogTag}）
 * 之间多对多关系的中间表。每条记录通过 {@code articleId} 和 {@code tagId} 将一篇文章与一个标签关联，
 * 支持一篇文章拥有多个标签、一个标签下包含多篇文章的双向查询。</p>
 */
@Data
@TableName("blog_article_tag")
public class BlogArticleTag implements Serializable {
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
     * 标签ID
     */
    private Long tagId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}
