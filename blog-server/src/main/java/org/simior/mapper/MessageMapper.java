package org.simior.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.simior.model.entity.BlogMessage;

/**
 * 消息通知Mapper接口
 */
@Mapper
public interface MessageMapper extends BaseMapper<BlogMessage> {

}
