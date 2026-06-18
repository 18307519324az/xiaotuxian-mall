package com.xtx.home.mapper;

import com.mybatisflex.core.BaseMapper;
import com.xtx.home.entity.HomeBrand;
import org.apache.ibatis.annotations.Mapper;

/**
 * 首页品牌 Mapper
 * 提供推荐品牌数据的基础 CRUD 操作
 */
@Mapper
public interface HomeBrandMapper extends BaseMapper<HomeBrand> {
}
