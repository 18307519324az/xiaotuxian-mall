package com.xtx.goods.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品规格（维度）实体类
 * 对应数据库表 goods_spec，定义商品的规格维度，如颜色、尺寸、内存等
 */
@Data
@Table("goods_spec")
public class GoodsSpec {

    /** 主键ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 商品ID */
    private Long goodsId;

    /** 规格名称，如"颜色"、"尺寸"、"内存容量" */
    private String name;

    /** 排序权重 */
    private Integer sort;

    /** 创建时间 */
    private LocalDateTime createTime;
}
