package com.xtx.payment.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付订单实体类
 */
@Data
@Table("pay_order")
public class PayOrder {

    /** 主键ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 支付单号 */
    private String payNo;

    /** 订单ID */
    private Long orderId;

    /** 订单编号 */
    private String orderNo;

    /** 用户ID */
    private Long userId;

    /** 支付渠道：1-微信 2-支付宝 3-模拟支付 */
    private Integer payChannel;

    /** 支付金额 */
    private BigDecimal payMoney;

    /** 支付状态：1-待支付 2-已支付 3-已退款 */
    private Integer payStatus;

    /** 第三方交易号 */
    private String thirdTradeNo;

    /** 过期时间 */
    private LocalDateTime expireTime;

    /** 支付时间 */
    private LocalDateTime payTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
