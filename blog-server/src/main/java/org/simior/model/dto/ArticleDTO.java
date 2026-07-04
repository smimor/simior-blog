package org.simior.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 文章发布/编辑请求参数
 * <p>
 * 用于创建新文章或编辑已有文章。内容字段（content）不强制非空，
 * 以支持保存草稿场景。标题最大 200 字符，内容最大 100000 字符。
 */
@Data
public class ArticleDTO {

    /**
     * 文章ID（编辑时必传，创建时为 null）
     */
    private Long id;

    /**
     * 文章标题
     */
    @NotBlank(message = "文章标题不能为空")
    @Size(max = 200, message = "文章标题不能超过200个字符")
    private String title;

    /**
     * 文章摘要（可选，前端可自动截取）
     */
    @Size(max = 500, message = "文章摘要不能超过500个字符")
    private String summary;

    /**
     * 封面图 URL
     */
    private String coverImage;

    /**
     * 文章内容（Markdown 格式，草稿时允许为空）
     */
    @Size(max = 100000, message = "文章内容不能超过100000个字符")
    private String content;

    /**
     * HTML 内容（由后端渲染 Markdown 生成）
     */
    @Size(max = 200000, message = "HTML内容不能超过200000个字符")
    private String htmlContent;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 标签ID列表
     */
    private List<Long> tagIds;

    /**
     * 是否置顶 0-否 1-是
     */
    private Integer isTop;

    /**
     * 是否草稿 0-否 1-是
     */
    private Integer isDraft;
}
