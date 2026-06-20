package com.xtx.api.category.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 一级分类详情 VO
 * 对应 /category?id=X 响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTopVO {

    /** 分类 ID */
    private String id;

    /** 分类名称 */
    private String name;

    /** 分类图片 URL */
    private String picture;

    /** 父级分类 ID（一级分类时为 "0"） */
    private String parentId;

    /** 父级分类名称 */
    private String parentName;

    /** 子分类列表（二级分类 + 三级分类嵌套） */
    private List<CategoryChildVO> children;

    /** Banner 列表 */
    private List<CategoryBannerVO> banners;
}
