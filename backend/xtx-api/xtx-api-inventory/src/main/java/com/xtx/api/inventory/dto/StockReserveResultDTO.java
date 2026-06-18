package com.xtx.api.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 库存预占结果 DTO
 * 返回库存预占操作的处理结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockReserveResultDTO {

    /** 是否全部预占成功 */
    private boolean allSuccess;

    /** 预占成功的 SKU 列表 */
    private List<Map<String, Object>> successItems = new ArrayList<>();

    /** 预占失败的 SKU 列表 */
    private List<Map<String, Object>> failedItems = new ArrayList<>();
}
