package com.xtx.home.mapper;

import com.mybatisflex.core.BaseMapper;
import com.xtx.home.entity.HomeSpecial;
import org.apache.ibatis.annotations.Mapper;

/**
 * 首页专题 Mapper
 * 提供精选专题数据的基础 CRUD 操作
 */
@Mapper
public interface HomeSpecialMapper extends BaseMapper<HomeSpecial> {
}
