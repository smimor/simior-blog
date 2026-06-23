package org.simior.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.simior.model.entity.BlogComment;

/**
 * 评论Mapper接口
 */
@Mapper
public interface CommentMapper extends BaseMapper<BlogComment> {

    /**
     * 原子递增评论点赞数
     */
    @Update("UPDATE blog_comment SET like_count = like_count + 1 WHERE id = #{id} AND deleted = 0")
    int incrementLikeCount(@Param("id") Long id);

    /**
     * 原子递减评论点赞数（不低于0）
     */
    @Update("UPDATE blog_comment SET like_count = GREATEST(0, like_count - 1) WHERE id = #{id} AND deleted = 0")
    int decrementLikeCount(@Param("id") Long id);
}
