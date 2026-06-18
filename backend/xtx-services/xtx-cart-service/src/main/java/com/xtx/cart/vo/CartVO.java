package com.xtx.cart.vo;

import lombok.Data;

/**
 * 购物车视图对象
 * 前端契约：id/skuId 为 String，price/nowPrice 为 String "49.00"
 */
@Data
public class CartVO {

    /** 购物车条目ID */
    private String id;

    /** SKU ID */
    private String skuId;

    /** 商品名称 */
    private String name;

    /** 规格文本（如：颜色:黑色 尺寸:M） */
    private String attrsText;

    /** 商品图片URL */
    private String picture;

    /** 原价 */
    private String price;

    /** 当前价格（可能为促销价） */
    private String nowPrice;

    /** 是否选中 */
    private Boolean selected;

    /** 库存数量 */
    private Integer stock;

    /** 购买数量 */
    private Integer count;

    /** 是否有效（true-有效，false-无效） */
    private Boolean isEffective;
}
