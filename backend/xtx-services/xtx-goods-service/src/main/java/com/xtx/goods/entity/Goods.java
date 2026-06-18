package com.xtx.goods.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.xtx.common.mybatisflex.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品实体类
 * 对应数据库表 goods，存储商品基本信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("goods")
public class Goods extends BaseEntity {

    /** 主键ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 商品名称 */
    private String name;

    /** 商品 SPU 编码 */
    private String spuCode;

    /** 商品描述/副标题 */
    private String description;

    /** 商品标签 */
    private String tag;

    /** 商品价格（元，字符串格式如 49.00） */
    private BigDecimal price;

    /** 商品原价（元） */
    private BigDecimal oldPrice;

    /** 折扣比例 */
    private Integer discount;

    /** 商品主图 URL */
    private String picture;

    /** 品牌ID */
    private Long brandId;

    /** 品牌名称（冗余） */
    private String brandName;

    /** 品牌 Logo（冗余） */
    private String brandLogo;

    /** 三级分类ID */
    private Long categoryId;

    /** 一级分类ID */
    private Long topCategoryId;

    /** 二级分类ID */
    private Long parentCategoryId;

    /** 库存数量 */
    private Integer inventory;

    /** 销量 */
    private Integer salesCount;

    /** 评论数 */
    private Integer commentCount;

    /** 收藏数 */
    private Integer collectCount;

    /** 是否预售：0-否，1-是 */
    private Integer isPreSale;

    /** 是否收藏：0-否，1-是 */
    private Integer isCollect;

    /** 状态：0-下架，1-上架 */
    private Integer status;

    /** 排序权重 */
    private Integer sort;
}
