package com.xtx.api.category.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分类 Banner VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryBannerVO {

    /** Banner ID */
    private String id;

    /** 图片 URL */
    private String imgUrl;

    /** 跳转链接 */
    private String linkUrl;
}
