package com.xtx.api.inventory.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class StockWarmupResultDTO {

    private Integer successCount = 0;

    private Integer failedCount = 0;

    private List<Map<String, Object>> failedItems = new ArrayList<>();
}
