package com.xtx.goods.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 专题商品关联实体类
 * 对应数据库表 special_goods，存储专题与商品的关联关系
 */
@Data
@Table("special_goods")
public class SpecialGoods {

    /** 主键ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 专题ID，关联 topic.id */
    private String specialId;

    /** 商品ID，关联 goods.id */
    private Long goodsId;

    /** 排序权重 */
    private Integer sortOrder;

    /** 创建时间 */
    private LocalDateTime createTime;
}
