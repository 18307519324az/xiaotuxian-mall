package com.xtx.api.category.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 首页头部分类树 VO
 * 对应 /home/category/head 响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryHeadVO {

    /** 分类 ID */
    private String id;

    /** 分类名称 */
    private String name;

    /** 分类图片 URL */
    private String picture;

    /** 分类图标 URL */
    private String icon;

    /** 子分类列表（含二级和三级） */
    private List<CategoryChildVO> children;

    /** 商品卡片列表（首页左侧悬浮弹层用） */
    private List<GoodsCardVO> goods;
}
