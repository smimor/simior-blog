package org.simior.model.vo;

import lombok.Data;

/**
 * 轮播图VO
 */
@Data
public class BannerVO {

    /**
     * 轮播图ID
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 链接URL
     */
    private String linkUrl;

    /**
     * 排序
     */
    private Integer sort;
}
