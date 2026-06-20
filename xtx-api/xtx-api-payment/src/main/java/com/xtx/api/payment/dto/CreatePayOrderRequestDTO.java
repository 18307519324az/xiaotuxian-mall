package com.xtx.api.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建支付订单请求 DTO
 * 发起支付时传递必要参数
 */
@Data
public class CreatePayOrderRequestDTO {

    /** 订单 ID */
    private Long orderId;

    /** 订单编号 */
    private String orderNo;

    /** 用户 ID */
    private Long userId;

    /** 支付金额 */
    private BigDecimal payMoney;

    /** 支付渠道：1-微信支付，2-支付宝 */
    private Integer payChannel;
}
