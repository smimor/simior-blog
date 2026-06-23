package org.simior.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.simior.model.entity.BlogArticleCollect;

/**
 * 文章收藏Mapper接口
 */
@Mapper
public interface ArticleCollectMapper extends BaseMapper<BlogArticleCollect> {
}
