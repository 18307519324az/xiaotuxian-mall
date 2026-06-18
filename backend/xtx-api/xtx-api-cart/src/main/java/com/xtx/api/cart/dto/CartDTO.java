package com.xtx.api.cart.dto;

import lombok.Data;

/**
 * 购物车数据 DTO
 * 用于远程调用时传递购物车项的基本信息
 */
@Data
public class CartDTO {

    /** 购物车项 ID */
    private Long id;

    /** SKU ID */
    private Long skuId;

    /** 购买数量 */
    private Integer count;

    /** 是否选中：0-未选中，1-已选中 */
    private Integer selected;
}
