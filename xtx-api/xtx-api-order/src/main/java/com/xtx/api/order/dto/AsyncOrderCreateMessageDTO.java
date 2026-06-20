package com.xtx.api.order.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 异步订单创建消息 DTO。
 * 用于 MQ 消息传递，不要直接暴露为前端 VO。
 */
@Data
public class AsyncOrderCreateMessageDTO {

    private String requestId;
    private String orderNo;
    private Long userId;
    private Long addressId;
    private List<Item> items;
    private BigDecimal totalAmount;
    private BigDecimal postFee;
    private BigDecimal payMoney;
    private String couponId;
    private String couponName;
    private String couponType;
    private BigDecimal discountGoodsAmount;
    private BigDecimal discountFreightAmount;
    private BigDecimal discountAmount;
    private String giftCardCode;
    private BigDecimal giftCardAmount;
    private LocalDateTime createTime;
    private Integer retryCount;

    @Data
    public static class Item {
        private Long skuId;
        private Long goodsId;
        private Integer count;
        private BigDecimal price;
        private String name;
    }
}
