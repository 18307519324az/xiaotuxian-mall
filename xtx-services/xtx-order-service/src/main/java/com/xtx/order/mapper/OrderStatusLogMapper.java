package com.xtx.order.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xtx.order.entity.OrderStatusLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * 订单状态日志 Mapper 接口
 */
@Mapper
public interface OrderStatusLogMapper extends BaseMapper<OrderStatusLog> {

    /**
     * 根据订单ID查询状态日志，按创建时间升序排列
     *
     * @param orderId 订单ID
     * @return 状态日志列表
     */
    default List<OrderStatusLog> selectByOrderIdOrderByTime(Long orderId) {
        return selectListByQuery(
                QueryWrapper.create()
                        .eq("order_id", orderId)
                        .orderBy("create_time", true)
        );
    }
}
