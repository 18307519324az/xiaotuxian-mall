package com.xtx.api.inventory.dto;

import lombok.Data;

import java.util.List;

@Data
public class StockPreDeductRequestDTO {

    private String requestId;

    private String orderNo;

    private Long userId;

    private List<Item> items;

    @Data
    public static class Item {
        private Long skuId;
        private Integer count;
    }
}
