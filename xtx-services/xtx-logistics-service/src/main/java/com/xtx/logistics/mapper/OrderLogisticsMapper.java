package com.xtx.logistics.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xtx.logistics.entity.OrderLogistics;
import org.apache.ibatis.annotations.Mapper;


/**
 * 订单物流 Mapper 接口
 */
@Mapper
public interface OrderLogisticsMapper extends BaseMapper<OrderLogistics> {

    /**
     * 根据订单ID查询物流信息
     *
     * @param orderId 订单ID
     * @return 物流信息
     */
    default OrderLogistics selectByOrderId(Long orderId) {
        return selectOneByQuery(
                QueryWrapper.create().eq("order_id", orderId)
        );
    }
}
