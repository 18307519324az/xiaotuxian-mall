package com.xtx.home.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.xtx.common.mybatisflex.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 专题商品关联实体类
 * 对应数据库表 home_special_goods，记录专题关联的商品
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("home_special_goods")
public class HomeSpecialGoods extends BaseEntity {

    /** 主键ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 专题ID */
    private Long specialId;

    /** 商品ID */
    private Long goodsId;

    /** 排序权重 */
    private Integer sort;

    /** 状态：0-下架，1-上架 */
    private Integer status;
}
