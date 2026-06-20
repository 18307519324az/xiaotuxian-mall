package com.xtx.category.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分类-商品关联实体类
 * 对应数据库表 category_goods，建立分类与商品的多对多关联
 */
@Data
@Table("category_goods")
public class CategoryGoods {

    /** 主键ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 分类ID（通常是三级分类） */
    private Long categoryId;

    /** 商品ID（对应 products.json 的 key） */
    private String goodsId;

    /** 排序权重 */
    private Integer sort;

    /** 创建时间 */
    private LocalDateTime createTime;
}
