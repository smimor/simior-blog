package org.simior.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论实体类
 *
 * <p>对应数据库表 {@code blog_comment}，存储博客文章下的用户评论。采用两级回复模型：
 * {@code parentId} 指向所属一级评论（顶层评论），{@code replyUserId} 标识被回复的目标用户，
 * 从而在单表中实现「评论 + 回复」的嵌套结构，无需递归查询。</p>
 *
 * <p>支持审核状态（{@code auditStatus}）控制评论发布流程，
 * 使用 {@code @TableLogic} 实现逻辑删除以保留评论审计痕迹。</p>
 */
@Data
@TableName("blog_comment")
public class BlogComment implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 评论ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 评论用户ID
     */
    private Long userId;

    /**
     * 父评论ID(二级回复)
     */
    private Long parentId;

    /**
     * 回复用户ID
     */
    private Long replyUserId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 审核状态 0-待审核 1-审核通过 2-审核拒绝
     */
    private Integer auditStatus;

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
