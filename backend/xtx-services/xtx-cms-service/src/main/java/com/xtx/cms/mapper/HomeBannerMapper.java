package com.xtx.cms.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xtx.cms.entity.HomeBanner;
import org.apache.ibatis.annotations.Mapper;


/**
 * 首页轮播图 Mapper
 * 提供轮播图的查询与管理功能
 */
@Mapper
public interface HomeBannerMapper extends BaseMapper<HomeBanner> {

    /**
     * 查询所有已启用并按排序权重降序排列的轮播图
     *
     * @return 已启用的轮播图列表
     */
    default java.util.List<HomeBanner> selectEnabledSorted() {
        return selectListByQuery(
                QueryWrapper.create()
                        .eq("status", 1)
                        .orderBy("sort", false)
                        .orderBy("id", false)
        );
    }
}
