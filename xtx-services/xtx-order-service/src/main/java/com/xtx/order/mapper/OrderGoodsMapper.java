package com.xtx.order.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xtx.order.entity.OrderGoods;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * 订单商品快照 Mapper 接口
 */
@Mapper
public interface OrderGoodsMapper extends BaseMapper<OrderGoods> {

    /**
     * 根据订单ID查询商品快照列表
     *
     * @param orderId 订单ID
     * @return 商品快照列表
     */
    default List<OrderGoods> selectByOrderId(Long orderId) {
        return selectListByQuery(
                QueryWrapper.create().eq("order_id", orderId)
        );
    }
}
