package com.xtx.api.inventory.dto;

import lombok.Data;

import java.util.List;

@Data
public class StockRollbackRequestDTO {

    private String requestId;

    private String orderNo;

    private List<Item> items;

    @Data
    public static class Item {
        private Long skuId;
        private Integer count;
    }
}
