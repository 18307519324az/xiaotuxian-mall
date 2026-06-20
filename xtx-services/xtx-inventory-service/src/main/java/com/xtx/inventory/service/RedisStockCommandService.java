package com.xtx.inventory.service;

import com.xtx.api.inventory.dto.StockPreDeductRequestDTO;
import com.xtx.api.inventory.dto.StockPreDeductResultDTO;
import com.xtx.api.inventory.dto.StockRollbackRequestDTO;
import com.xtx.api.inventory.dto.StockWarmupRequestDTO;
import com.xtx.api.inventory.dto.StockWarmupResultDTO;
import com.xtx.inventory.entity.StockSku;
import com.xtx.inventory.mapper.StockSkuMapper;
import com.xtx.inventory.service.redis.InventoryRedisExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RedisStockCommandService {

    private static final long REQUEST_TTL_SECONDS = 1800L;

    private final StockSkuMapper stockSkuMapper;
    private final InventoryRedisExecutor inventoryRedisExecutor;

    public StockWarmupResultDTO warmupStocks(StockWarmupRequestDTO request) {
        StockWarmupResultDTO result = new StockWarmupResultDTO();
        if (request == null || request.getSkuIds() == null) {
            return result;
        }
        boolean forceRefresh = Boolean.TRUE.equals(request.getForceRefresh());
        for (Long skuId : request.getSkuIds()) {
            StockSku stockSku = stockSkuMapper.selectBySkuId(skuId);
            if (stockSku == null) {
                result.getFailedItems().add(failedItem(skuId, "库存记录不存在"));
                continue;
            }
            boolean success = inventoryRedisExecutor.warmupStock(skuId, stockSku.getAvailableStock(), forceRefresh);
            if (success) {
                result.setSuccessCount(result.getSuccessCount() + 1);
            } else {
                result.getFailedItems().add(failedItem(skuId, "Redis 库存已存在且未强制刷新"));
            }
        }
        result.setFailedCount(result.getFailedItems().size());
        return result;
    }

    public Long getRedisStock(Long skuId) {
        return inventoryRedisExecutor.getStock(skuId);
    }

    public StockPreDeductResultDTO preDeductByRedis(StockPreDeductRequestDTO request) {
        StockPreDeductResultDTO result = new StockPreDeductResultDTO();
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            result.getFailedItems().add(failedItem(null, "请求商品不能为空"));
            return result;
        }

        List<StockPreDeductRequestDTO.Item> deductedItems = new ArrayList<>();
        for (StockPreDeductRequestDTO.Item item : request.getItems()) {
            String itemRequestId = itemRequestId(request.getRequestId(), item.getSkuId());
            Long scriptResult = inventoryRedisExecutor.preDeduct(
                    item.getSkuId(),
                    itemRequestId,
                    item.getCount(),
                    REQUEST_TTL_SECONDS
            );
            if (Long.valueOf(1L).equals(scriptResult)) {
                deductedItems.add(item);
                result.getSuccessItems().add(successItem(item));
                continue;
            }
            if (Long.valueOf(2L).equals(scriptResult)) {
                result.getSuccessItems().add(successItem(item));
                continue;
            }

            rollbackSuccessItems(request.getRequestId(), deductedItems);
            result.getSuccessItems().clear();
            result.getFailedItems().add(failedItem(item.getSkuId(), mapFailureReason(scriptResult), item.getCount()));
            result.setAllSuccess(Boolean.FALSE);
            return result;
        }

        result.setAllSuccess(Boolean.TRUE);
        return result;
    }

    public void rollbackRedisStock(StockRollbackRequestDTO request) {
        if (request == null || request.getItems() == null) {
            return;
        }
        for (StockRollbackRequestDTO.Item item : request.getItems()) {
            inventoryRedisExecutor.rollback(
                    item.getSkuId(),
                    itemRequestId(request.getRequestId(), item.getSkuId()),
                    item.getCount()
            );
        }
    }

    private void rollbackSuccessItems(String requestId, List<StockPreDeductRequestDTO.Item> deductedItems) {
        for (StockPreDeductRequestDTO.Item deductedItem : deductedItems) {
            inventoryRedisExecutor.rollback(
                    deductedItem.getSkuId(),
                    itemRequestId(requestId, deductedItem.getSkuId()),
                    deductedItem.getCount()
            );
        }
    }

    private String itemRequestId(String requestId, Long skuId) {
        return requestId + ":" + skuId;
    }

    private String mapFailureReason(Long scriptResult) {
        if (Long.valueOf(-1L).equals(scriptResult)) {
            return "库存不存在";
        }
        if (Long.valueOf(0L).equals(scriptResult)) {
            return "库存不足";
        }
        return "Redis 预扣失败";
    }

    private Map<String, Object> successItem(StockPreDeductRequestDTO.Item item) {
        Map<String, Object> map = new HashMap<>();
        map.put("skuId", item.getSkuId());
        map.put("count", item.getCount());
        return map;
    }

    private Map<String, Object> failedItem(Long skuId, String reason) {
        return failedItem(skuId, reason, null);
    }

    private Map<String, Object> failedItem(Long skuId, String reason, Integer count) {
        Map<String, Object> map = new HashMap<>();
        map.put("skuId", skuId);
        map.put("reason", reason);
        if (count != null) {
            map.put("requestedCount", count);
        }
        return map;
    }
}
