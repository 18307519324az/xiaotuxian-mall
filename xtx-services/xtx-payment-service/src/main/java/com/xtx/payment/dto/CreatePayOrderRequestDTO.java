package com.xtx.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建支付订单请求DTO
 */
@Data
public class CreatePayOrderRequestDTO {

    /** 订单ID */
    private Long orderId;

    /** 订单编号 */
    private String orderNo;

    /** 用户ID */
    private Long userId;

    /** 支付金额 */
    private BigDecimal payMoney;

    /** 支付渠道：1-微信 2-支付宝 3-模拟支付 */
    private Integer payChannel;
}
