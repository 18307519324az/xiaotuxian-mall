package com.xtx.inventory.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xtx.inventory.entity.StockSku;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * SKU 库存 Mapper
 * 提供库存数据的查询与更新（含乐观锁）
 */
@Mapper
public interface StockSkuMapper extends BaseMapper<StockSku> {

    /**
     * 根据 SKU ID 查询库存
     *
     * @param skuId SKU ID
     * @return 库存信息
     */
    default StockSku selectBySkuId(Long skuId) {
        return selectOneByQuery(
                QueryWrapper.create()
                        .eq("sku_id", skuId)
                        .limit(1)
        );
    }

    /**
     * 根据 SKU ID 列表批量查询库存
     *
     * @param skuIds SKU ID 列表
     * @return 库存列表
     */
    default List<StockSku> selectBySkuIds(List<Long> skuIds) {
        return selectListByQuery(
                QueryWrapper.create()
                        .in("id", skuIds)
        );
    }
}
