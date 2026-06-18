package com.xtx.order.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.xtx.order.entity.OrderInfo;
import org.apache.ibatis.annotations.Mapper;


/**
 * 订单信息 Mapper 接口
 */
@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    /**
     * 根据订单编号查询订单
     *
     * @param orderNo 订单编号
     * @return 订单信息
     */
    default OrderInfo selectByOrderNo(String orderNo) {
        return selectOneByQuery(
                QueryWrapper.create().eq("order_no", orderNo)
        );
    }

    /**
     * 分页查询用户订单列表
     *
     * @param userId     用户ID
     * @param page       分页对象
     * @param orderState 订单状态（可选）
     * @return 分页结果
     */
    default Page<OrderInfo> selectByUserIdPage(Long userId, Page<OrderInfo> page, Integer orderState) {
        QueryWrapper query = QueryWrapper.create()
                .eq("user_id", userId)
                .eq("is_deleted", 0);
        if (orderState != null) {
            query.eq("order_state", orderState);
        }
        query.orderBy("create_time", false);
        return paginate(page, query);
    }
}
