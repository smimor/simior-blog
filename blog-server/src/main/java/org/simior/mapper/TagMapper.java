package org.simior.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.simior.model.entity.BlogTag;

import java.util.List;

/**
 * 标签Mapper接口
 */
@Mapper
public interface TagMapper extends BaseMapper<BlogTag> {

    /**
     * 根据文章id查询标签
     *
     * @param articleId 文章id
     * @return 标签列表
     */
    @Select("SELECT t.* FROM `blog_tag` t JOIN `blog_article_tag` art_tag ON t.id = art_tag.tag_id " +
            "WHERE art_tag.article_id = #{articleId} AND t.deleted = 0")
    List<BlogTag> selectTagsByArticleId(@Param("articleId") Long articleId);
}
