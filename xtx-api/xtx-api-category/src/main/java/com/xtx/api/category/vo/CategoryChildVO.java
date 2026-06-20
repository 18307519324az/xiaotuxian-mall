package com.xtx.api.category.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分类子节点 VO（二级/三级分类）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryChildVO {

    /** 分类 ID */
    private String id;

    /** 分类名称 */
    private String name;

    /** 分类图片 URL */
    private String picture;

    /** 分类图标 URL */
    private String icon;

    /** 父级分类 ID */
    private String parentId;

    /** 父级分类名称 */
    private String parentName;

    /** 子分类列表（三级分类时为空数组） */
    private List<CategoryChildVO> children;

    /** 商品卡片列表 */
    private List<GoodsCardVO> goods;

    /** 子分类列表（用于筛选页面） */
    private List<CategoryChildVO> categories;

    /** 品牌列表（用于筛选页面） */
    private List<CategoryFilterBrandVO> brands;

    /** 销售属性（用于筛选页面） */
    private List<Object> saleProperties;
}
