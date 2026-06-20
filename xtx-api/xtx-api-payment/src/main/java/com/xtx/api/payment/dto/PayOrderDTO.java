package com.xtx.api.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 支付订单 DTO
 * 用于远程调用时传递支付订单信息
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayOrderDTO {

    /** 支付单 ID */
    private Long id;

    /** 支付单编号 */
    private String payNo;

    /** 业务订单编号 */
    private String orderNo;

    /** 支付金额 */
    private BigDecimal payMoney;

    /** 支付状态：0-待支付，1-支付成功，2-已退款 */
    private Integer payStatus;

    /** 支付时间 */
    private String payTime;

    /** 支付渠道：1-微信支付，2-支付宝 */
    private Integer payChannel;
}
