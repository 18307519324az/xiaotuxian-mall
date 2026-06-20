package com.xtx.goods.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xtx.goods.entity.GoodsSkuSpecValue;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * 商品 SKU 规格值关联 Mapper
 * 提供 SKU 与规格值关联关系的查询
 */
@Mapper
public interface GoodsSkuSpecValueMapper extends BaseMapper<GoodsSkuSpecValue> {

    /**
     * 根据 SKU ID 查询该 SKU 的所有规格值关联
     *
     * @param skuId SKU ID
     * @return 规格值关联列表
     */
    default List<GoodsSkuSpecValue> selectBySkuId(Long skuId) {
        return selectListByQuery(
                QueryWrapper.create()
                        .eq("sku_id", skuId)
        );
    }

    /**
     * 根据 SKU ID 列表批量查询规格值关联
     *
     * @param skuIds SKU ID 列表
     * @return 规格值关联列表
     */
    default List<GoodsSkuSpecValue> selectBySkuIds(List<Long> skuIds) {
        return selectListByQuery(
                QueryWrapper.create()
                        .in("sku_id", skuIds)
        );
    }
}
