package com.xtx.goods.mapper;

import com.mybatisflex.core.BaseMapper;
import com.xtx.goods.entity.Brand;
import org.apache.ibatis.annotations.Mapper;

/**
 * 品牌 Mapper
 * 提供品牌信息的基础 CRUD 操作
 */
@Mapper
public interface BrandMapper extends BaseMapper<Brand> {
}
