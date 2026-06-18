package com.xtx.cms.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 专题关联商品实体类
 * 对应数据库表 special_goods，存储专题与商品的关联关系
 */
@Data
@Table("special_goods")
public class SpecialGoods {

    /** 主键ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 专题ID，关联 special.id */
    private Long specialId;

    /** 商品ID，关联 goods.id */
    private Long goodsId;

    /** 排序权重 */
    private Integer sort;

    /** 创建时间 */
    private LocalDateTime createTime;
}
