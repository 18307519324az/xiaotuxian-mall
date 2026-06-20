package com.xtx.cms.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xtx.cms.entity.Special;
import org.apache.ibatis.annotations.Mapper;


/**
 * 专题活动 Mapper
 * 提供专题活动的查询与管理
 */
@Mapper
public interface SpecialMapper extends BaseMapper<Special> {

    /**
     * 查询已启用且最新的 4 条专题活动
     *
     * @return 最新的 4 条专题活动列表
     */
    default java.util.List<Special> selectEnabledLatest() {
        return selectListByQuery(
                QueryWrapper.create()
                        .eq("status", 1)
                        .orderBy("create_time", false)
                        .limit(4)
        );
    }
}
