package com.xtx.order.dto;

import lombok.Data;

/**
 * 订单状态更新DTO（内部服务调用）
 */
@Data
public class OrderStatusUpdateDTO {

    /** 订单ID */
    private Long orderId;

    /** 目标状态 */
    private Integer targetState;

    /** 操作人 */
    private String operator;

    /** 备注 */
    private String remark;
}
