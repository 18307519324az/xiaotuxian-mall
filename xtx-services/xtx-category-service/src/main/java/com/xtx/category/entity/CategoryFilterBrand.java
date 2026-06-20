package com.xtx.category.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分类筛选品牌实体类
 * 对应数据库表 category_filter_brand，存储二级分类下的品牌筛选项
 */
@Data
@Table("category_filter_brand")
public class CategoryFilterBrand {

    /** 主键ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 关联的分类ID（二级分类） */
    private Long categoryId;

    /** 品牌ID */
    private String brandId;

    /** 品牌名称 */
    private String brandName;

    /** 品牌 Logo URL */
    private String brandLogo;

    /** 品牌首字母 */
    private String brandLetter;

    /** 排序权重 */
    private Integer sort;

    /** 创建时间 */
    private LocalDateTime createTime;
}
