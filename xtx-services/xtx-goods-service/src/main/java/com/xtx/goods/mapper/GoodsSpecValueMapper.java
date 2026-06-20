package com.xtx.goods.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xtx.goods.entity.GoodsSpecValue;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * 商品规格值 Mapper
 * 提供规格维度可选值的查询
 */
@Mapper
public interface GoodsSpecValueMapper extends BaseMapper<GoodsSpecValue> {

    /**
     * 根据规格ID查询规格值列表
     *
     * @param specId 规格ID
     * @return 规格值列表
     */
    default List<GoodsSpecValue> selectBySpecId(Long specId) {
        return selectListByQuery(
                QueryWrapper.create()
                        .eq("spec_id", specId)
                        .orderBy("sort", false)
        );
    }
}
