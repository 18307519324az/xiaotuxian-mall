package com.xtx.home.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.xtx.common.mybatisflex.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 首页品牌实体类
 * 对应数据库表 home_brand，存储推荐品牌信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("home_brand")
public class HomeBrand extends BaseEntity {

    /** 主键ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 品牌名称 */
    private String name;

    /** 品牌图片地址 */
    private String picture;

    /** 品牌 Logo 地址 */
    private String logo;

    /** 排序权重 */
    private Integer sort;

    /** 状态：0-下架，1-上架 */
    private Integer status;
}
