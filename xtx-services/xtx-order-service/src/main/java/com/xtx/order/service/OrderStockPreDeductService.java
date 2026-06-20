package com.xtx.order.service;

import com.xtx.api.inventory.StockClient;
import com.xtx.api.inventory.dto.StockPreDeductRequestDTO;
import com.xtx.api.inventory.dto.StockPreDeductResultDTO;
import com.xtx.api.inventory.dto.StockRollbackRequestDTO;
import com.xtx.api.inventory.dto.StockWarmupRequestDTO;
import com.xtx.api.inventory.dto.StockWarmupResultDTO;
import com.xtx.common.core.exception.BizException;
import com.xtx.common.core.result.ApiResponse;
import com.xtx.common.core.result.ResultCode;
import com.xtx.order.dto.SubmitOrderDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * OrderStockPreDeductService — Redis Lua 库存预扣减适配器。
 * <p>
 * 在订单创建前同步调用 Redis Lua 预扣库存，作为快速失败前置检查。
 * 当 Redis key 不存在时自动 warmup 并重试一次。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderStockPreDeductService {

    private final StockClient stockClient;

    /**
     * Redis 库存预扣减（含自动 warmup 重试）。
     *
     * @param orderNo 订单号
     * @param userId  用户 ID
     * @param items   商品列表
     * @return requestId 用于后续 rollback
     * @throws BizException 库存不足或预扣失败时抛出
     */
    public String preDeduct(String orderNo, Long userId, List<SubmitOrderDTO.OrderItemDTO> items) {
        String requestId = UUID.randomUUID().toString();

        StockPreDeductResultDTO result = executePreDeduct(requestId, orderNo, userId, items);

        // 当 key 不存在时自动 warmup 并重试一次
        if (result != null && !result.getAllSuccess() && hasMissingKeys(result.getFailedItems())) {
            List<Long> missingSkuIds = extractMissingSkuIds(result.getFailedItems());
            log.info("Redis 库存 key 不存在，执行 warmup: skuIds={}", missingSkuIds);
            warmupMissingKeys(missingSkuIds);

            result = executePreDeduct(requestId, orderNo, userId, items);
        }

        if (result == null || !result.getAllSuccess()) {
            String reason = extractFailureReason(result);
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), reason);
        }

        return requestId;
    }

    /**
     * Redis 库存回滚（释放预扣减的库存）。
     *
     * @param requestId 预扣时生成的 requestId
     * @param orderNo   订单号（仅用于日志）
     * @param items     商品列表
     */
    public void rollback(String requestId, String orderNo, List<SubmitOrderDTO.OrderItemDTO> items) {
        StockRollbackRequestDTO request = new StockRollbackRequestDTO();
        request.setRequestId(requestId);
        List<StockRollbackRequestDTO.Item> rollbackItems = items.stream()
                .map(item -> {
                    StockRollbackRequestDTO.Item ri = new StockRollbackRequestDTO.Item();
                    ri.setSkuId(item.getSkuId());
                    ri.setCount(item.getCount());
                    return ri;
                })
                .collect(Collectors.toList());
        request.setItems(rollbackItems);

        try {
            stockClient.rollbackRedisStock(request);
            log.info("Redis 库存回滚成功, orderNo={}, requestId={}", orderNo, requestId);
        } catch (Exception e) {
            log.error("Redis 库存回滚失败, orderNo={}, requestId={}", orderNo, requestId, e);
        }
    }

    private StockPreDeductResultDTO executePreDeduct(String requestId, String orderNo, Long userId,
                                                     List<SubmitOrderDTO.OrderItemDTO> items) {
        StockPreDeductRequestDTO request = new StockPreDeductRequestDTO();
        request.setRequestId(requestId);
        request.setOrderNo(orderNo);
        request.setUserId(userId);
        List<StockPreDeductRequestDTO.Item> preDeductItems = items.stream()
                .map(item -> {
                    StockPreDeductRequestDTO.Item pi = new StockPreDeductRequestDTO.Item();
                    pi.setSkuId(item.getSkuId());
                    pi.setCount(item.getCount());
                    return pi;
                })
                .collect(Collectors.toList());
        request.setItems(preDeductItems);

        try {
            ApiResponse<StockPreDeductResultDTO> response = stockClient.preDeductByRedis(request);
            return response != null ? response.getData() : null;
        } catch (Exception e) {
            log.error("Redis 库存预扣调用失败, orderNo={}", orderNo, e);
            return null;
        }
    }

    private boolean hasMissingKeys(List<Map<String, Object>> failedItems) {
        return failedItems != null && failedItems.stream()
                .anyMatch(item -> "库存不存在".equals(item.get("reason")));
    }

    private List<Long> extractMissingSkuIds(List<Map<String, Object>> failedItems) {
        return failedItems.stream()
                .filter(item -> "库存不存在".equals(item.get("reason")))
                .map(item -> {
                    Object skuId = item.get("skuId");
                    if (skuId instanceof Number) {
                        return ((Number) skuId).longValue();
                    }
                    return null;
                })
                .filter(id -> id != null)
                .collect(Collectors.toList());
    }

    private void warmupMissingKeys(List<Long> skuIds) {
        StockWarmupRequestDTO warmupRequest = new StockWarmupRequestDTO();
        warmupRequest.setSkuIds(skuIds);
        warmupRequest.setForceRefresh(true);
        try {
            ApiResponse<StockWarmupResultDTO> response = stockClient.warmupStocks(warmupRequest);
            StockWarmupResultDTO warmupResult = response != null ? response.getData() : null;
            if (warmupResult != null && warmupResult.getFailedCount() > 0) {
                log.warn("部分 SKU warmup 失败: {}", warmupResult.getFailedItems());
            }
        } catch (Exception e) {
            log.error("库存 warmup 远程调用失败, skuIds={}", skuIds, e);
            throw new BizException(ResultCode.INTERNAL_ERROR.getCode(), "库存预热失败");
        }
    }

    private String extractFailureReason(StockPreDeductResultDTO result) {
        if (result != null && result.getFailedItems() != null && !result.getFailedItems().isEmpty()) {
            Map<String, Object> firstFailed = result.getFailedItems().get(0);
            Object reason = firstFailed.get("reason");
            return reason != null ? reason.toString() : "库存不足";
        }
        return "库存不足";
    }
}
