package com.xtx.category.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分类轮播图实体类
 * 对应数据库表 category_banner，存储分类页面的 Banner
 */
@Data
@Table("category_banner")
public class CategoryBanner {

    /** 主键ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 关联的分类ID */
    private Long categoryId;

    /** 图片地址 */
    private String imgUrl;

    /** 点击跳转链接 */
    private String linkUrl;

    /** 排序权重 */
    private Integer sort;

    /** 创建时间 */
    private LocalDateTime createTime;
}
