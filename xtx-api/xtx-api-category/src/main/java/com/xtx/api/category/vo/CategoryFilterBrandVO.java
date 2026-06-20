package com.xtx.api.category.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分类筛选品牌 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryFilterBrandVO {

    /** 品牌 ID */
    private String id;

    /** 品牌名称 */
    private String name;

    /** 品牌首字母 */
    private String letter;
}
