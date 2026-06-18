package com.xtx.goods.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xtx.goods.entity.GoodsSpec;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * 商品规格 Mapper
 * 提供商品规格维度（颜色、尺寸等）的查询
 */
@Mapper
public interface GoodsSpecMapper extends BaseMapper<GoodsSpec> {

    /**
     * 根据商品ID查询规格列表，按排序权重降序排列
     *
     * @param goodsId 商品ID
     * @return 规格列表
     */
    default List<GoodsSpec> selectByGoodsId(Long goodsId) {
        return selectListByQuery(
                QueryWrapper.create()
                        .eq("goods_id", goodsId)
                        .orderBy("sort", false)
        );
    }
}
