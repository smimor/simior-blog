package org.simior.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 分类DTO
 *
 * <p>用于 {@code POST /v1/categories} 和 {@code PUT /v1/categories/{id}} 接口。
 * 分类名称为必填项且不可为空，描述和排序字段为可选，用于对分类进行补充说明和自定义排序。</p>
 */
@Data
public class CategoryDTO {

    /**
     * 分类ID (编辑时传)
     */
    private Long id;

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    private String categoryName;

    /**
     * 分类描述
     */
    private String categoryDesc;

    /**
     * 排序
     */
    private Integer sort;
}
