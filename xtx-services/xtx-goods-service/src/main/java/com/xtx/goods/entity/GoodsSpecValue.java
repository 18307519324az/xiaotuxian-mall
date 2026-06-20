package com.xtx.goods.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品规格值实体类
 * 对应数据库表 goods_spec_value，存储规格维度的可选值
 */
@Data
@Table("goods_spec_value")
public class GoodsSpecValue {

    /** 主键ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 规格ID，关联 goods_spec.id */
    private Long specId;

    /** 规格值名称，如"红色"、"128GB" */
    private String name;

    /** 规格值图片（颜色等视觉类规格使用） */
    private String picture;

    /** 排序权重 */
    private Integer sort;

    /** 创建时间 */
    private LocalDateTime createTime;
}
