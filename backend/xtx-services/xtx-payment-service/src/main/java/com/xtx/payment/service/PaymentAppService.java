package com.xtx.payment.service;

import cn.hutool.core.util.RandomUtil;
import com.xtx.api.order.OrderClient;
import com.xtx.api.order.dto.OrderStatusUpdateDTO;
import com.xtx.common.core.exception.BizException;
import com.xtx.common.core.result.ApiResponse;
import com.xtx.common.core.result.ResultCode;
import com.xtx.payment.dto.CreatePayOrderRequestDTO;
import com.xtx.payment.dto.PayOrderDTO;
import com.xtx.payment.entity.PayOrder;
import com.xtx.payment.mapper.PayOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 支付应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentAppService {

    /** 支付状态：待支付 */
    public static final int PAY_STATUS_PENDING = 1;
    /** 支付状态：已支付 */
    public static final int PAY_STATUS_PAID = 2;
    /** 支付状态：已退款 */
    public static final int PAY_STATUS_REFUNDED = 3;

    private final PayOrderMapper payOrderMapper;
    private final OrderClient orderClient;

    /**
     * 创建支付订单
     */
    @Transactional(rollbackFor = Exception.class)
    public PayOrderDTO createPayOrder(CreatePayOrderRequestDTO request) {
        String payNo = generatePayNo();

        PayOrder payOrder = new PayOrder();
        payOrder.setPayNo(payNo);
        payOrder.setOrderId(request.getOrderId());
        payOrder.setOrderNo(request.getOrderNo());
        payOrder.setUserId(request.getUserId());
        payOrder.setPayChannel(request.getPayChannel() != null ? request.getPayChannel() : 1);
        payOrder.setPayMoney(request.getPayMoney());
        payOrder.setPayStatus(PAY_STATUS_PENDING);
        payOrder.setExpireTime(LocalDateTime.now().plusMinutes(30));
        payOrder.setCreateTime(LocalDateTime.now());
        payOrder.setUpdateTime(LocalDateTime.now());
        payOrderMapper.insert(payOrder);

        log.info("创建支付订单成功, payNo={}, orderNo={}", payNo, request.getOrderNo());
        return toDTO(payOrder);
    }

    /**
     * 根据订单编号查询支付订单
     */
    public PayOrderDTO getPayOrderByOrderNo(String orderNo) {
        PayOrder payOrder = payOrderMapper.selectByOrderNo(orderNo);
        if (payOrder == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "支付订单不存在");
        }
        return toDTO(payOrder);
    }

    /**
     * 模拟支付（开发测试用）
     */
    @Transactional(rollbackFor = Exception.class)
    public void mockPay(Long orderId, Long userId) {
        PayOrder payOrder = payOrderMapper.selectOneByQuery(
                com.mybatisflex.core.query.QueryWrapper.create()
                        .eq("order_id", orderId)
                        .eq("user_id", userId)
                        .orderBy("create_time", false)
        );

        if (payOrder == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "支付订单不存在");
        }
        if (payOrder.getPayStatus() != PAY_STATUS_PENDING) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "支付订单已处理");
        }

        payOrder.setPayStatus(PAY_STATUS_PAID);
        payOrder.setPayTime(LocalDateTime.now());
        payOrder.setThirdTradeNo("MOCK" + RandomUtil.randomNumbers(18));
        payOrder.setUpdateTime(LocalDateTime.now());
        payOrderMapper.update(payOrder);

        log.info("模拟支付成功, payNo={}, orderNo={}", payOrder.getPayNo(), payOrder.getOrderNo());

        try {
            OrderStatusUpdateDTO orderUpdate = new OrderStatusUpdateDTO();
            orderUpdate.setOrderId(orderId);
            orderUpdate.setTargetState(2);
            orderUpdate.setOperator("payment-service");
            orderUpdate.setRemark("模拟支付成功");
            ApiResponse<Void> orderResponse = orderClient.updateOrderStatus(orderUpdate);
            if (orderResponse != null && orderResponse.getCode() == ResultCode.SUCCESS.getCode()) {
                log.info("订单状态已更新为待发货, orderId={}", orderId);
            } else {
                log.error("订单状态更新失败, orderId={}", orderId);
            }
        } catch (Exception e) {
            log.error("调用订单服务更新状态失败", e);
            throw new BizException(ResultCode.INTERNAL_ERROR.getCode(), "订单状态更新失败");
        }

        log.info("支付完成，等待确认收货后扣减库存, orderNo={}", payOrder.getOrderNo());
    }

    private String generatePayNo() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = RandomUtil.randomNumbers(8);
        return "PAY" + datePart + randomPart;
    }

    private PayOrderDTO toDTO(PayOrder payOrder) {
        if (payOrder == null) {
            return null;
        }
        PayOrderDTO dto = new PayOrderDTO();
        dto.setId(payOrder.getId());
        dto.setPayNo(payOrder.getPayNo());
        dto.setOrderId(payOrder.getOrderId());
        dto.setOrderNo(payOrder.getOrderNo());
        dto.setUserId(payOrder.getUserId());
        dto.setPayChannel(payOrder.getPayChannel());
        dto.setPayMoney(payOrder.getPayMoney());
        dto.setPayStatus(payOrder.getPayStatus());
        dto.setThirdTradeNo(payOrder.getThirdTradeNo());
        dto.setExpireTime(payOrder.getExpireTime());
        dto.setPayTime(payOrder.getPayTime());
        dto.setCreateTime(payOrder.getCreateTime());
        return dto;
    }
}
