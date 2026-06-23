package org.simior.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分类VO
 */
@Data
public class CategoryVO {

    /**
     * 分类ID
     */
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
     * 文章数量
     */
    private Long articleCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
