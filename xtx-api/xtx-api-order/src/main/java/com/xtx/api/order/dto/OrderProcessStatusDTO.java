package com.xtx.api.order.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单处理状态 DTO。
 * 用于异步下单链路中查询订单处理进度。
 */
@Data
public class OrderProcessStatusDTO {

    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";

    private String orderNo;
    private String requestId;
    private String status;
    private String message;
    private Long orderId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
