package com.xtx.goods.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xtx.goods.entity.SpecialGoods;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 专题商品关联 Mapper
 * 提供专题与商品关联关系的查询
 */
@Mapper
public interface SpecialGoodsMapper extends BaseMapper<SpecialGoods> {

    /**
     * 根据专题ID查询关联的商品ID列表，按 sort_order 升序排列
     *
     * @param specialId 专题ID
     * @return 关联记录列表
     */
    default List<SpecialGoods> selectBySpecialId(String specialId) {
        return selectListByQuery(
                QueryWrapper.create()
                        .eq("special_id", specialId)
                        .orderBy("sort_order", true)
        );
    }
}
