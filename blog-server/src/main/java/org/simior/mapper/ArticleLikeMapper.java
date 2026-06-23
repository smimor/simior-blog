package org.simior.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.simior.model.entity.BlogArticleLike;

/**
 * 文章点赞Mapper接口
 */
@Mapper
public interface ArticleLikeMapper extends BaseMapper<BlogArticleLike> {
}
