package org.simior.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.simior.model.entity.SysRole;

/**
 * 角色Mapper接口
 */
@Mapper
public interface RoleMapper extends BaseMapper<SysRole> {

}
