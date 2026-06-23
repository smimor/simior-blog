package org.simior.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签VO
 */
@Data
public class TagVO {

    /**
     * 标签ID
     */
    private Long id;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 文章数量
     */
    private Long articleCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
