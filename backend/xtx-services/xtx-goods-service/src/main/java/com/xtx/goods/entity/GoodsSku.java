package com.xtx.goods.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.xtx.common.mybatisflex.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品 SKU 实体类
 * 对应数据库表 goods_sku，存储商品的库存量单位（具体售卖规格）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("goods_sku")
public class GoodsSku extends BaseEntity {

    /** 主键ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 商品ID */
    private Long goodsId;

    /** SKU 编码 */
    private String skuCode;

    /** 售价（分，如 4900 表示 49.00 元） */
    private Integer price;

    /** 原价（分） */
    private Integer oldPrice;

    /** SKU 图片地址 */
    private String picture;

    /** 库存数量 */
    private Integer inventory;

    /** 是否有效：0-无效，1-有效 */
    private Integer isEffective;

    /** 规格文本（如 "颜色:白色 尺寸:标准款"） */
    private String attrsText;

    /** 状态：0-禁用，1-启用 */
    private Integer status;
}
