package com.xtx.order.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单商品快照实体类
 */
@Data
@Table("order_goods")
public class OrderGoods {

    /** 主键ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 订单ID */
    private Long orderId;

    /** SKU ID */
    private Long skuId;

    /** 商品SPU ID */
    private Long goodsId;

    /** 商品名称 */
    private String goodsName;

    /** 商品图片 */
    private String goodsImage;

    /** 规格文本 */
    private String attrsText;

    /** 单价 */
    private BigDecimal price;

    /** 购买数量 */
    private Integer count;

    /** 小计金额 */
    private BigDecimal totalPrice;

    /** 小计实付金额 */
    private BigDecimal totalPayPrice;

    /** 创建时间 */
    private LocalDateTime createTime;
}
