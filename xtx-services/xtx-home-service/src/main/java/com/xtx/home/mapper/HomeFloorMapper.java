package com.xtx.home.mapper;

import com.mybatisflex.core.BaseMapper;
import com.xtx.home.entity.HomeFloor;
import org.apache.ibatis.annotations.Mapper;

/**
 * 首页楼层 Mapper
 * 提供楼层运营配置数据的基础 CRUD 操作
 */
@Mapper
public interface HomeFloorMapper extends BaseMapper<HomeFloor> {
}
