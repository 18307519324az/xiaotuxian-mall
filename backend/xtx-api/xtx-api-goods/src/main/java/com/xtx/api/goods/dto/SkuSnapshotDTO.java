package com.xtx.api.goods.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;

/**
 * SKU 快照数据传输对象
 * 用于远程调用时传递 SKU 的瞬时状态信息
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SkuSnapshotDTO {

    /** SKU ID */
    private Long skuId;

    /** 商品 SPU ID */
    private Long goodsId;

    /** SKU 编码 */
    private String skuCode;

    /** 当前售价 */
    private BigDecimal price;

    /** 原价 */
    private BigDecimal oldPrice;

    /** SKU 图片地址 */
    private String picture;

    /** 规格属性描述，如 "红色; 128G" */
    private String attrsText;

    /** 是否有效：0-无效，1-有效 */
    private Integer isEffective;

    /** 状态：0-下架，1-上架 */
    private Integer status;

    /** 商品名称 */
    private String goodsName;

    /** 当前售价（与 price 相同，兼容前端字段名） */
    private BigDecimal nowPrice;

    /** 库存数量（快照时可能不精确，仅供展示） */
    private Integer stock;
}
