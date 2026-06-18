package com.xtx.api.cart.dto;

import lombok.Data;

/**
 * 购物车合并项 DTO
 * 未登录状态下加入购物车的商品，在登录后需要与服务端合并时使用
 */
@Data
public class CartMergeItemDTO {

    /** SKU ID */
    private Long skuId;

    /** 数量 */
    private Integer count;

    /** 是否选中 */
    private Boolean selected;
}
