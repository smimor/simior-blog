package org.simior.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * 文章实体类
 *
 * <p>对应数据库表 {@code blog_article}，是博客系统的核心内容实体，存储文章的 Markdown 与 HTML 双格式内容、
 * 摘要、封面图以及浏览/点赞/评论/收藏等统计数据。支持草稿（{@code isDraft}）和置顶（{@code isTop}）标记，
 * 并通过审核状态字段实现内容发布前的审签流程。</p>
 *
 * <p>使用 {@code @Version} 实现乐观锁，防止并发编辑导致的内容覆盖；
 * 使用 {@code @TableLogic} 实现逻辑删除，确保文章数据可恢复。</p>
 */
@Data
@TableName("blog_article")
public class BlogArticle implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文章ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 作者ID
     */
    private Long userId;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章摘要
     */
    private String summary;

    /**
     * 封面图
     */
    private String coverImage;

    /**
     * 文章内容(Markdown)
     */
    private String content;

    /**
     * 是否置顶 0-否 1-是
     */
    private Integer isTop;

    /**
     * 是否草稿 0-否 1-是
     */
    private Integer isDraft;

    /**
     * 审核状态 0-待审核 1-审核通过 2-审核拒绝
     */
    private Integer auditStatus;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 收藏数
     */
    private Integer collectCount;

    /**
     * 文章版本号
     */
    @Version
    private Integer version;

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
