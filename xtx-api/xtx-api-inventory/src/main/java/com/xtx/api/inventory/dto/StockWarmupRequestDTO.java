package com.xtx.api.inventory.dto;

import lombok.Data;

import java.util.List;

@Data
public class StockWarmupRequestDTO {

    private List<Long> skuIds;

    private Boolean forceRefresh;
}
