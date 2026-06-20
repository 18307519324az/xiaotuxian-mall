package com.xtx.home.mapper;

import com.mybatisflex.core.BaseMapper;
import com.xtx.home.entity.HomeBanner;
import org.apache.ibatis.annotations.Mapper;

/**
 * 首页横幅 Mapper
 * 提供首页轮播图数据的基础 CRUD 操作
 */
@Mapper
public interface HomeBannerMapper extends BaseMapper<HomeBanner> {
}
