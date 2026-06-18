package com.xtx.api.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 库存预占请求 DTO
 * 下单时预占/锁定指定商品的库存数量
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockReserveRequestDTO {

    /** 订单ID */
    private Long orderId;

    /** 订单编号 */
    private String orderNo;

    /** 预占库存项列表 */
    private List<StockReserveItemDTO> items;

    /**
     * 库存预占项
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockReserveItemDTO {

        /** SKU ID */
        private Long skuId;

        /** 预占数量 */
        private Integer count;
    }
}
