package com.xtx.inventory.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 库存变更日志实体类
 * 对应数据库表 stock_change_log，记录所有库存变动的操作明细
 */
@Data
@Table("stock_change_log")
public class StockChangeLog {

    /** 主键ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** SKU ID */
    private Long skuId;

    /** 变更类型：RESERVE-预占，RELEASE-释放，DEDUCT-扣减，INCREASE-入库 */
    private String changeType;

    /** 变更数量（正数为增加，负数为减少） */
    private Integer changeAmount;

    /** 变更前库存 */
    private Integer beforeStock;

    /** 变更后库存 */
    private Integer afterStock;

    /** 业务唯一标识（防止重复处理） */
    private String bizKey;

    /** 操作人 */
    private String operator;

    /** 创建时间 */
    private LocalDateTime createTime;
}
