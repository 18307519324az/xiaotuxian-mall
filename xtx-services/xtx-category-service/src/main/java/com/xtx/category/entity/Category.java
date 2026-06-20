package com.xtx.category.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.xtx.common.mybatisflex.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品分类实体类
 * 对应数据库表 category，支持三级分类体系（一级 → 二级 → 三级）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("category")
public class Category extends BaseEntity {

    /** 主键ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 父级分类ID，一级分类该值为 0 */
    private Long parentId;

    /** 分类名称 */
    private String name;

    /** 分类图标 URL */
    private String iconUrl;

    /** 分类图片 URL（用于 Banner/大图展示） */
    private String pictureUrl;

    /** 排序权重（值越大越靠前） */
    private Integer sort;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    /** 分类层级：1-一级，2-二级，3-三级 */
    private Integer level;
}
