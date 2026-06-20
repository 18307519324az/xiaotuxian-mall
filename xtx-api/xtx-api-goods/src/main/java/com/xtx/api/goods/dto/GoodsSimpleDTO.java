package com.xtx.api.goods.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品简单信息 DTO
 * 用于列表展示或远程调用时传递商品概要信息
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoodsSimpleDTO {

    /** 商品 SPU ID */
    private Long id;

    /** 商品名称 */
    private String name;

    /** 商品主图 */
    private String picture;

    /** 商品价格 */
    private BigDecimal price;

    /** 商品描述 */
    private String desc;
}
