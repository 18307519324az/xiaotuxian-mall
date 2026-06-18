package com.xtx.cms.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xtx.cms.entity.HomePanel;
import org.apache.ibatis.annotations.Mapper;


/**
 * 首页面板 Mapper
 * 提供首页商品板块（新品、人气等）的查询
 */
@Mapper
public interface HomePanelMapper extends BaseMapper<HomePanel> {

    /**
     * 根据面板类型查询已启用的面板
     *
     * @param type 面板类型：NEW-新品推荐，HOT-人气推荐
     * @return 已启用且匹配类型的面板列表，按排序权重降序排列
     */
    default java.util.List<HomePanel> selectEnabledByType(String type) {
        return selectListByQuery(
                QueryWrapper.create()
                        .eq("status", 1)
                        .eq("type", type)
                        .orderBy("sort", false)
        );
    }
}
