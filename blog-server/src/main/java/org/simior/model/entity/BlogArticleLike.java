package org.simior.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文章点赞实体类
 *
 * <p>对应数据库表 {@code blog_article_like}，表示用户与文章之间的点赞关系，
 * 属于多对多关联的中间表。每条记录代表一位用户对一篇文章的一次点赞，
 * 通过 {@code userId} 和 {@code articleId} 的联合唯一约束防止重复点赞，
 * 同时与 {@code BlogArticle.likeCount} 冗余字段配合使用以提升查询性能。</p>
 */
@Data
@TableName("blog_article_like")
public class BlogArticleLike implements Serializable {
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
     * 用户ID
     */
    private Long userId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
