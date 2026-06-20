package com.xtx.goods.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xtx.goods.entity.GoodsSku;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * 商品 SKU Mapper
 * 提供商品库存量单位的查询与操作
 */
@Mapper
public interface GoodsSkuMapper extends BaseMapper<GoodsSku> {

    /**
     * 根据商品ID查询 SKU 列表
     *
     * @param goodsId 商品ID
     * @return SKU 列表
     */
    default List<GoodsSku> selectByGoodsId(Long goodsId) {
        return selectListByQuery(
                QueryWrapper.create()
                        .eq("goods_id", goodsId)
                        .eq("status", 1)
        );
    }

    /**
     * 根据 SKU ID 列表批量查询
     *
     * @param skuIds SKU ID 列表
     * @return SKU 列表
     */
    default List<GoodsSku> selectBatchByIds(List<Long> skuIds) {
        return selectListByQuery(
                QueryWrapper.create()
                        .in("id", skuIds)
                        .eq("status", 1)
        );
    }
}
