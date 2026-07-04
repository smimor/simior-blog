package org.simior.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.simior.model.entity.BlogArticle;

import java.util.List;
import java.util.Map;

/**
 * 文章Mapper接口
 */
@Mapper
public interface ArticleMapper extends BaseMapper<BlogArticle> {

    /**
     * 原子递增浏览量（独立于乐观锁，不递增 version 避免冲突）
     */
    @Update("UPDATE blog_article SET view_count = view_count + 1 WHERE id = #{id} AND deleted = 0")
    int incrementViewCount(@Param("id") Long id);

    /**
     * 原子递增点赞数（独立于乐观锁，不递增 version 避免冲突）
     */
    @Update("UPDATE blog_article SET like_count = like_count + 1 WHERE id = #{id} AND deleted = 0")
    int incrementLikeCount(@Param("id") Long id);

    /**
     * 原子递减点赞数（不低于0，独立于乐观锁）
     */
    @Update("UPDATE blog_article SET like_count = GREATEST(0, like_count - 1) WHERE id = #{id} AND deleted = 0")
    int decrementLikeCount(@Param("id") Long id);

    /**
     * 原子递增收藏数（独立于乐观锁，不递增 version 避免冲突）
     */
    @Update("UPDATE blog_article SET collect_count = collect_count + 1 WHERE id = #{id} AND deleted = 0")
    int incrementCollectCount(@Param("id") Long id);

    /**
     * 原子递减收藏数（不低于0，独立于乐观锁）
     */
    @Update("UPDATE blog_article SET collect_count = GREATEST(0, collect_count - 1) WHERE id = #{id} AND deleted = 0")
    int decrementCollectCount(@Param("id") Long id);

    /**
     * 原子递增评论数（独立于乐观锁，不递增 version 避免冲突）
     */
    @Update("UPDATE blog_article SET comment_count = comment_count + 1 WHERE id = #{id} AND deleted = 0")
    int incrementCommentCount(@Param("id") Long id);

    /**
     * 原子递减评论数（不低于0，独立于乐观锁）
     */
    @Update("UPDATE blog_article SET comment_count = GREATEST(0, comment_count - 1) WHERE id = #{id} AND deleted = 0")
    int decrementCommentCount(@Param("id") Long id);

    /**
     * 按分类统计已发布文章数量（GROUP BY 替代 N+1 查询）
     * <p>
     * 仅统计未删除且已发布的文章，同时排除已删除分类下的文章。
     *
     * @return 每行包含 category_id 和 cnt 两个字段
     */
    @Select("SELECT category_id, COUNT(*) AS cnt FROM blog_article " +
            "WHERE deleted = 0 AND is_draft = 0 " +
            "AND category_id IN (SELECT id FROM blog_category WHERE deleted = 0) " +
            "GROUP BY category_id")
    List<Map<String, Object>> countArticlesGroupByCategory();
}
