package com.xtx.category.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 分类商品卡片实体类
 * 对应数据库表 category_goods_card，存储商品概要信息用于分类展示
 */
@Data
@Table("category_goods_card")
public class CategoryGoodsCard {

    /** 商品ID */
    @Id(keyType = KeyType.None)
    private String goodsId;

    /** 商品名称 */
    private String name;

    /** 商品描述/副标题 */
    private String description;

    /** 商品价格 */
    private BigDecimal price;

    /** 商品主图 URL */
    private String picture;

    /** 商品标签 */
    private String tag;

    /** 销量 */
    private Integer salesCount;

    /** 创建时间 */
    private LocalDateTime createTime;
}
