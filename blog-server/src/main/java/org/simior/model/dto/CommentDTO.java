package org.simior.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 评论发布请求参数
 * <p>
 * 用于新建评论时的数据校验，包含文章关联、父评论（回复）和评论内容。
 */
@Data
public class CommentDTO {

    /**
     * 文章ID
     */
    @NotNull(message = "文章ID不能为空")
    private Long articleId;

    /**
     * 父评论ID（回复评论时传入，顶级评论为 null）
     */
    private Long parentId;

    /**
     * 回复用户ID（回复他人评论时传入）
     */
    private Long replyUserId;

    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 2000, message = "评论内容不能超过2000个字符")
    private String content;
}
