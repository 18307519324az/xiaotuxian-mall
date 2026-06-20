package com.xtx.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 异步提交订单请求 DTO。
 * 与 SubmitOrderDTO 字段结构相同，独立定义以解耦两条下单链路。
 */
@Data
public class AsyncSubmitOrderDTO {

    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    @NotEmpty(message = "商品列表不能为空")
    @Valid
    private List<SubmitOrderDTO.OrderItemDTO> goods;

    /** 支付渠道：1-微信 2-支付宝 3-模拟支付 */
    private Integer payChannel;

    /** 买家留言 */
    private String buyerMessage;

    private String couponId;

    private String giftCardCode;

    /** 配送时间类型：1-不限 2-工作日 3-周末/节假日 */
    private Integer deliveryTimeType;

    /** 下单防重复 token（由 GET /member/order/token 获取） */
    private String token;
}
