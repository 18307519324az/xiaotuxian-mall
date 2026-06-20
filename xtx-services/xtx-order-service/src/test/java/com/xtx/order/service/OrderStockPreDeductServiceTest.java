package com.xtx.order.service;

import com.xtx.api.inventory.StockClient;
import com.xtx.api.inventory.dto.StockPreDeductRequestDTO;
import com.xtx.api.inventory.dto.StockPreDeductResultDTO;
import com.xtx.api.inventory.dto.StockWarmupRequestDTO;
import com.xtx.api.inventory.dto.StockWarmupResultDTO;
import com.xtx.common.core.exception.BizException;
import com.xtx.common.core.result.ApiResponse;
import com.xtx.order.dto.SubmitOrderDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderStockPreDeductServiceTest {

    @Mock StockClient stockClient;
    OrderStockPreDeductService service;

    @BeforeEach
    void setUp() {
        service = new OrderStockPreDeductService(stockClient);
    }

    private List<SubmitOrderDTO.OrderItemDTO> items(Long skuId, int count) {
        SubmitOrderDTO.OrderItemDTO item = new SubmitOrderDTO.OrderItemDTO();
        item.setSkuId(skuId);
        item.setCount(count);
        return List.of(item);
    }

    @Test
    void preDeductSuccess() {
        StockPreDeductResultDTO result = new StockPreDeductResultDTO();
        result.setAllSuccess(true);
        when(stockClient.preDeductByRedis(any(StockPreDeductRequestDTO.class)))
                .thenReturn(ApiResponse.success(result));

        String requestId = service.preDeduct("ORDER001", 1L, items(1001L, 2));

        assertNotNull(requestId);
        verify(stockClient).preDeductByRedis(any(StockPreDeductRequestDTO.class));
        verify(stockClient, never()).warmupStocks(any());
    }

    @Test
    void redisNotEnoughShouldThrow() {
        StockPreDeductResultDTO result = new StockPreDeductResultDTO();
        result.setAllSuccess(false);
        Map<String, Object> failed = new HashMap<>();
        failed.put("skuId", 1001L);
        failed.put("reason", "库存不足");
        result.setFailedItems(List.of(failed));
        when(stockClient.preDeductByRedis(any(StockPreDeductRequestDTO.class)))
                .thenReturn(ApiResponse.success(result));

        BizException ex = assertThrows(BizException.class,
                () -> service.preDeduct("ORDER001", 1L, items(1001L, 2)));
        assertTrue(ex.getMessage().contains("库存不足"));
        verify(stockClient, never()).warmupStocks(any());
    }

    @Test
    void missingKeyShouldWarmupAndRetry() {
        // First call: missing key
        StockPreDeductResultDTO firstResult = new StockPreDeductResultDTO();
        firstResult.setAllSuccess(false);
        Map<String, Object> failed = new HashMap<>();
        failed.put("skuId", 1001L);
        failed.put("reason", "库存不存在");
        firstResult.setFailedItems(List.of(failed));

        // Second call: success after warmup
        StockPreDeductResultDTO secondResult = new StockPreDeductResultDTO();
        secondResult.setAllSuccess(true);

        StockWarmupResultDTO warmupResult = new StockWarmupResultDTO();
        warmupResult.setSuccessCount(1);

        when(stockClient.preDeductByRedis(any(StockPreDeductRequestDTO.class)))
                .thenReturn(ApiResponse.success(firstResult))
                .thenReturn(ApiResponse.success(secondResult));
        when(stockClient.warmupStocks(any(StockWarmupRequestDTO.class)))
                .thenReturn(ApiResponse.success(warmupResult));

        String requestId = service.preDeduct("ORDER001", 1L, items(1001L, 2));

        assertNotNull(requestId);
        verify(stockClient, times(2)).preDeductByRedis(any(StockPreDeductRequestDTO.class));
        verify(stockClient).warmupStocks(any(StockWarmupRequestDTO.class));
    }

    @Test
    void missingKeyWarmupThenStillFailShouldThrow() {
        // First call: missing key
        StockPreDeductResultDTO firstResult = new StockPreDeductResultDTO();
        firstResult.setAllSuccess(false);
        Map<String, Object> failed1 = new HashMap<>();
        failed1.put("skuId", 1001L);
        failed1.put("reason", "库存不存在");
        firstResult.setFailedItems(List.of(failed1));

        // Second call: still insufficient
        StockPreDeductResultDTO secondResult = new StockPreDeductResultDTO();
        secondResult.setAllSuccess(false);
        Map<String, Object> failed2 = new HashMap<>();
        failed2.put("skuId", 1001L);
        failed2.put("reason", "库存不足");
        secondResult.setFailedItems(List.of(failed2));

        StockWarmupResultDTO warmupResult = new StockWarmupResultDTO();
        warmupResult.setSuccessCount(1);

        when(stockClient.preDeductByRedis(any(StockPreDeductRequestDTO.class)))
                .thenReturn(ApiResponse.success(firstResult))
                .thenReturn(ApiResponse.success(secondResult));
        when(stockClient.warmupStocks(any(StockWarmupRequestDTO.class)))
                .thenReturn(ApiResponse.success(warmupResult));

        assertThrows(BizException.class,
                () -> service.preDeduct("ORDER001", 1L, items(1001L, 2)));
        verify(stockClient, times(2)).preDeductByRedis(any(StockPreDeductRequestDTO.class));
        verify(stockClient).warmupStocks(any(StockWarmupRequestDTO.class));
    }

    @Test
    void rollbackSuccess() {
        when(stockClient.rollbackRedisStock(any())).thenReturn(ApiResponse.success());

        assertDoesNotThrow(() -> service.rollback("req-1", "ORDER001", items(1001L, 2)));
        verify(stockClient).rollbackRedisStock(any());
    }

    @Test
    void rollbackFailureShouldNotThrow() {
        when(stockClient.rollbackRedisStock(any())).thenThrow(new RuntimeException("网络异常"));

        assertDoesNotThrow(() -> service.rollback("req-1", "ORDER001", items(1001L, 2)));
    }
}
