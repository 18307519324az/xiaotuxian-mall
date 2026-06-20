package com.xtx.cart.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车实体类
 */
@Data
@Table("cart")
public class Cart {

    /** 主键ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** SKU ID（商品规格ID） */
    private Long skuId;

    /** 购买数量 */
    private Integer count;

    /** 是否选中（1-选中，0-未选中） */
    private Integer selected;

    /** 是否有效（1-有效，0-无效，下架或删除时置为无效） */
    private Integer isEffective;

    /** 是否删除（1-删除，0-正常） */
    private Integer deleted;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
