package com.xtx.home.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.xtx.common.mybatisflex.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 首页楼层实体类
 * 对应数据库表 home_floor，存储首页楼层运营配置
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("home_floor")
public class HomeFloor extends BaseEntity {

    /** 主键ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 一级分类ID */
    private Long categoryId;

    /** 一级分类名称（冗余） */
    private String categoryName;

    /** 楼层运营图片 URL */
    private String picture;

    /** 促销文案 */
    private String saleInfo;

    /** 排序权重 */
    private Integer sort;

    /** 状态：0-下架，1-上架 */
    private Integer status;
}
