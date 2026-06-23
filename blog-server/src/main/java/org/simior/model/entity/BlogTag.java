package org.simior.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 标签实体类
 *
 * <p>对应数据库表 {@code blog_tag}，用于管理文章的标签体系。标签通过中间表
 * {@code BlogArticleTag} 与文章建立多对多关联，支持灵活的内容分类和检索。
 * 与分类（{@code BlogCategory}）互补，提供更细粒度的内容组织方式。</p>
 *
 * <p>使用 {@code @TableLogic} 实现逻辑删除，避免删除标签时破坏已有的文章-标签关联数据。</p>
 */
@Data
@TableName("blog_tag")
public class BlogTag implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 标签ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 标签名称
     */
    private String tagName;

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
