package com.xtx.api.inventory.dto;

import lombok.Data;

/**
 * 库存信息 DTO
 * 用于查询指定 SKU 的实时库存数据
 */
@Data
public class StockInfoDTO {

    /** SKU ID */
    private Long skuId;

    /** 可用库存数量 */
    private Integer availableStock;

    /** 已锁定/预占库存数量 */
    private Integer lockedStock;
}
