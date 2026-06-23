package org.simior.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.simior.model.entity.BlogArticleTag;

import java.util.List;
import java.util.Map;

/**
 * 文章标签关联Mapper接口
 */
@Mapper
public interface ArticleTagMapper extends BaseMapper<BlogArticleTag> {

    /**
     * 按标签统计文章数量（GROUP BY 替代 N+1 查询）
     * <p>
     * 同时过滤已删除的文章和已删除的标签，确保统计结果仅包含有效数据。
     *
     * @return 每行包含 tag_id 和 cnt 两个字段
     */
    @Select("SELECT art_tag.tag_id, COUNT(*) AS cnt FROM blog_article_tag art_tag " +
            "JOIN blog_article a ON art_tag.article_id = a.id " +
            "JOIN blog_tag t ON art_tag.tag_id = t.id " +
            "WHERE a.deleted = 0 AND t.deleted = 0 " +
            "GROUP BY art_tag.tag_id")
    List<Map<String, Object>> countArticlesGroupByTag();
}
