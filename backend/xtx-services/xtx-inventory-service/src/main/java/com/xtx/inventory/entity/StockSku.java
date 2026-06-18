package com.xtx.inventory.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * SKU 库存实体类
 * 对应数据库表 stock_sku，存储每个 SKU 的库存数量（含乐观锁）
 */
@Data
@Table("stock_sku")
public class StockSku {

    /** 主键ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** SKU ID（唯一） */
    private Long skuId;

    /** 总库存数量 */
    private Integer totalStock;

    /** 可用库存数量 */
    private Integer availableStock;

    /** 锁定库存数量（已预占未支付） */
    private Integer lockedStock;

    /** 已售库存数量 */
    private Integer soldStock;

    /** 乐观锁版本号 */
    private Integer version;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
