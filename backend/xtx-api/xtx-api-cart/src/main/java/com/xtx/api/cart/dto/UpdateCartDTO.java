package com.xtx.api.cart.dto;

import lombok.Data;

/**
 * 更新购物车请求 DTO
 * 用户修改购物车中某 SKU 的选中状态或数量
 */
@Data
public class UpdateCartDTO {

    /** 选中状态（可选，传 null 表示不修改） */
    private Boolean selected;

    /** 购买数量（可选，传 null 表示不修改） */
    private Integer count;
}
