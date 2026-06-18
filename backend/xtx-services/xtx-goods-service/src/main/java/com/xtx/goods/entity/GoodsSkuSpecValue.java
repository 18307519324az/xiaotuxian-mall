package com.xtx.goods.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品 SKU 规格值关联实体类
 * 对应数据库表 goods_sku_spec_value，记录每个 SKU 所包含的规格值组合
 */
@Data
@Table("goods_sku_spec_value")
public class GoodsSkuSpecValue {

    /** 主键ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** SKU ID，关联 goods_sku.id */
    private Long skuId;

    /** 规格 ID，关联 goods_spec.id */
    private Long specId;

    /** 规格值 ID，关联 goods_spec_value.id */
    private Long specValueId;

    /** 规格值名称（冗余存储，便于查询） */
    private String specValueName;

    /** 创建时间 */
    private LocalDateTime createTime;
}
