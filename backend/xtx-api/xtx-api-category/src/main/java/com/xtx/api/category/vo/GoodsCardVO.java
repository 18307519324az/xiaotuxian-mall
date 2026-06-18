package com.xtx.api.category.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 商品卡片 VO
 * 用于分类树、首页推荐等场景中的商品概要展示
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoodsCardVO {

    /** 商品 ID */
    private String id;

    /** 商品名称 */
    private String name;

    /** 商品描述 */
    private String desc;

    /** 商品价格 */
    private BigDecimal price;

    /** 商品主图 URL */
    private String picture;

    /** 商品标签 */
    private String tag;

    /** 排序序号（仅分类商品列表使用） */
    private Integer orderNum;
}
