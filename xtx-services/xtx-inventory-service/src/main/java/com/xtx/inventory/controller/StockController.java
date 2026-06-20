package com.xtx.inventory.controller;

import com.xtx.api.inventory.dto.StockWarmupRequestDTO;
import com.xtx.api.inventory.dto.StockWarmupResultDTO;
import com.xtx.common.core.result.FrontResponse;
import com.xtx.common.web.annotation.FrontController;
import com.xtx.inventory.service.InventoryAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 库存前端控制器
 * 提供库存信息的查询接口
 */
@FrontController
@RestController
@RequiredArgsConstructor
public class StockController {

    private final InventoryAppService inventoryAppService;

    /**
     * 获取 SKU 库存信息
     *
     * @param skuId SKU ID
     * @return 库存信息（可用库存、锁定库存、已售库存等）
     */
    @GetMapping("/goods/stock/{skuId}")
    public FrontResponse<Map<String, Object>> getStockInfo(@PathVariable Long skuId) {
        Map<String, Object> stockInfo = inventoryAppService.getStockInfo(skuId);
        return FrontResponse.success(stockInfo);
    }

    @PostMapping("/inventory/stocks/warmup")
    public FrontResponse<StockWarmupResultDTO> warmupStocks(@RequestBody StockWarmupRequestDTO request) {
        return FrontResponse.success(inventoryAppService.warmupStocks(request));
    }

    @GetMapping("/inventory/stocks/redis/{skuId}")
    public FrontResponse<Long> getRedisStock(@PathVariable Long skuId) {
        return FrontResponse.success(inventoryAppService.getRedisStock(skuId));
    }
}
