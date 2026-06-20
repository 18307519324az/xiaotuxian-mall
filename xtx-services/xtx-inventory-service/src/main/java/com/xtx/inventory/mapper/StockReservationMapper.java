package com.xtx.inventory.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xtx.inventory.entity.StockReservation;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * 库存预占记录 Mapper
 * 提供库存预占记录的查询与操作
 */
@Mapper
public interface StockReservationMapper extends BaseMapper<StockReservation> {

    /**
     * 根据订单号查询所有预占记录
     *
     * @param orderNo 订单编号
     * @return 预占记录列表
     */
    default List<StockReservation> selectByOrderNo(String orderNo) {
        return selectListByQuery(
                QueryWrapper.create()
                        .eq("order_no", orderNo)
        );
    }

    /**
     * 根据订单号和 SKU ID 查询预占记录
     *
     * @param orderNo 订单编号
     * @param skuId   SKU ID
     * @return 预占记录
     */
    default StockReservation selectByOrderNoAndSkuId(String orderNo, Long skuId) {
        return selectOneByQuery(
                QueryWrapper.create()
                        .eq("order_no", orderNo)
                        .eq("sku_id", skuId)
                        .limit(1)
        );
    }
}
