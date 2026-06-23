package org.simior.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 标签DTO
 *
 * <p>用于 {@code POST /v1/tags} 和 {@code PUT /v1/tags/{id}} 接口。
 * 标签名称为必填项且不可为空，用于文章的分类标记与检索。</p>
 */
@Data
public class TagDTO {

    /**
     * 标签ID (编辑时传)
     */
    private Long id;

    /**
     * 标签名称
     */
    @NotBlank(message = "标签名称不能为空")
    private String tagName;

}
