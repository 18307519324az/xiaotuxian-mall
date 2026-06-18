package com.xtx.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 提交订单请求DTO
 */
@Data
public class SubmitOrderDTO {

    /** 收货地址ID */
    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    /** 商品列表 */
    @NotEmpty(message = "商品列表不能为空")
    @Valid
    private List<OrderItemDTO> goods;

    /** 支付渠道：1-微信 2-支付宝 3-模拟支付 */
    private Integer payChannel;

    /** 买家留言 */
    private String buyerMessage;

    /** 配送时间类型：1-不限 2-工作日 3-周末/节假日 */
    private Integer deliveryTimeType;

    /**
     * 订单商品项DTO
     */
    @Data
    public static class OrderItemDTO {

        /** SKU ID */
        @NotNull(message = "SKU ID不能为空")
        private Long skuId;

        /** 购买数量 */
        @NotNull(message = "购买数量不能为空")
        private Integer count;
    }
}
