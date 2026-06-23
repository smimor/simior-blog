package org.simior.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息通知实体类
 */
@Data
@TableName("blog_message")
public class BlogMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 接收用户ID
     */
    private Long userId;

    /**
     * 发送用户ID
     */
    private Long fromUserId;

    /**
     * 消息类型 1-评论 2-点赞 3-收藏 4-系统通知
     */
    private Integer type;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 相关文章ID
     */
    private Long articleId;

    /**
     * 相关评论ID
     */
    private Long commentId;

    /**
     * 是否已读 0-未读 1-已读
     */
    private Integer isRead;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
