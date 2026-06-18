package com.xtx.home.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.xtx.common.mybatisflex.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 首页专题实体类
 * 对应数据库表 home_special，存储精选专题信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("home_special")
public class HomeSpecial extends BaseEntity {

    /** 主键ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 专题标题 */
    private String title;

    /** 专题封面图 URL */
    private String cover;

    /** 专题摘要 */
    private String summary;

    /** 最低价格（元） */
    private BigDecimal lowestPrice;

    /** 排序权重 */
    private Integer sort;

    /** 状态：0-下架，1-上架 */
    private Integer status;
}
