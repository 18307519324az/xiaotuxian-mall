package com.xtx.payment.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xtx.payment.entity.PayOrder;
import org.apache.ibatis.annotations.Mapper;


/**
 * 支付订单 Mapper 接口
 */
@Mapper
public interface PayOrderMapper extends BaseMapper<PayOrder> {

    /**
     * 根据订单编号查询支付订单
     *
     * @param orderNo 订单编号
     * @return 支付订单
     */
    default PayOrder selectByOrderNo(String orderNo) {
        return selectOneByQuery(
                QueryWrapper.create().eq("order_no", orderNo)
        );
    }

    /**
     * 根据支付单号查询支付订单
     *
     * @param payNo 支付单号
     * @return 支付订单
     */
    default PayOrder selectByPayNo(String payNo) {
        return selectOneByQuery(
                QueryWrapper.create().eq("pay_no", payNo)
        );
    }
}
