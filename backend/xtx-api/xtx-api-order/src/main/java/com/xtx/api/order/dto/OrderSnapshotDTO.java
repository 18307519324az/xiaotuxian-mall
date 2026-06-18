package com.xtx.api.order.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单快照 DTO
 * 用于远程调用时获取订单的概要信息
 */
@Data
public class OrderSnapshotDTO {

    /** 订单 ID */
    private Long id;

    /** 订单编号 */
    private String orderNo;

    /** 订单状态 */
    private Integer orderState;

    /** 实付金额 */
    private BigDecimal payMoney;

    /** 订单总金额 */
    private BigDecimal totalMoney;

    /** 创建时间 */
    private LocalDateTime createTime;
}
