package com.xtx.api.order.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * 订单状态更新 DTO
 * 用于远程调用时传递订单状态变更信息
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderStatusUpdateDTO {

    /** 订单 ID */
    private Long orderId;

    /** 目标订单状态 */
    private Integer targetState;

    /** 操作人 */
    private String operator;

    /** 操作备注 */
    private String remark;
}
