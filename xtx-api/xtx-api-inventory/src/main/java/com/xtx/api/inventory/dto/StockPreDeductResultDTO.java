package com.xtx.api.inventory.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class StockPreDeductResultDTO {

    private Boolean allSuccess = Boolean.FALSE;

    private List<Map<String, Object>> successItems = new ArrayList<>();

    private List<Map<String, Object>> failedItems = new ArrayList<>();
}
