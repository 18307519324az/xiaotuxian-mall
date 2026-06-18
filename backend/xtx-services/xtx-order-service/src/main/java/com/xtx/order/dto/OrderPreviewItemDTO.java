package com.xtx.order.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单预览商品项DTO
 */
@Data
public class OrderPreviewItemDTO {

    /** SKU ID */
    private Long skuId;

    /** 商品名称 */
    private String name;

    /** 商品图片 */
    private String picture;

    /** 规格文本 */
    private String attrsText;

    /** 单价 */
    private BigDecimal price;

    /** 购买数量 */
    private Integer count;

    /** 小计 */
    private BigDecimal subtotal;

    /** 是否有效 */
    private Boolean isEffective;

    /** 库存 */
    private Integer stock;
}
