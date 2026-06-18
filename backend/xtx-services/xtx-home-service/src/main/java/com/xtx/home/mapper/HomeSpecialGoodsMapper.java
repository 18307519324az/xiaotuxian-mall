package com.xtx.home.mapper;

import com.mybatisflex.core.BaseMapper;
import com.xtx.home.entity.HomeSpecialGoods;
import org.apache.ibatis.annotations.Mapper;

/**
 * 专题商品关联 Mapper
 * 提供专题与商品关联数据的基础 CRUD 操作
 */
@Mapper
public interface HomeSpecialGoodsMapper extends BaseMapper<HomeSpecialGoods> {
}
