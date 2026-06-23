package org.simior.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.simior.model.entity.BlogViewHistory;

/**
 * 浏览记录Mapper接口
 */
@Mapper
public interface ViewHistoryMapper extends BaseMapper<BlogViewHistory> {

}
