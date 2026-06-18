package com.xtx.api.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 添加购物车请求 DTO
 * 用户将指定 SKU 加入购物车时传递的参数
 */
@Data
public class AddCartDTO {

    /** SKU ID */
    @NotNull
    private Long skuId;

    /** 购买数量 */
    @NotNull
    @Min(1)
    private Integer count;
}
