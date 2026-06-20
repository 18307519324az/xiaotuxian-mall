package com.xtx.inventory.service;

import com.xtx.api.inventory.dto.StockPreDeductRequestDTO;
import com.xtx.api.inventory.dto.StockPreDeductResultDTO;
import com.xtx.api.inventory.dto.StockRollbackRequestDTO;
import com.xtx.api.inventory.dto.StockWarmupRequestDTO;
import com.xtx.api.inventory.dto.StockWarmupResultDTO;
import com.xtx.inventory.entity.StockSku;
import com.xtx.inventory.mapper.StockSkuMapper;
import com.xtx.inventory.service.redis.InventoryRedisExecutor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RedisStockCommandServiceTest {

    @Test
    void warmupStocksShouldLoadAvailableStockToRedis() {
        StockSkuMapper mapper = mock(StockSkuMapper.class);
        StockSku stockSku = new StockSku();
        stockSku.setSkuId(1001L);
        stockSku.setAvailableStock(12);
        when(mapper.selectBySkuId(1001L)).thenReturn(stockSku);

        FakeInventoryRedisExecutor executor = new FakeInventoryRedisExecutor();
        RedisStockCommandService service = new RedisStockCommandService(mapper, executor);

        StockWarmupRequestDTO request = new StockWarmupRequestDTO();
        request.setSkuIds(List.of(1001L));
        request.setForceRefresh(Boolean.TRUE);

        StockWarmupResultDTO result = service.warmupStocks(request);

        assertEquals(1, result.getSuccessCount());
        assertEquals(0, result.getFailedCount());
        assertEquals(12L, executor.getStock(1001L));
    }

    @Test
    void preDeductShouldRollbackPreviousSuccessWhenLaterItemFails() {
        FakeInventoryRedisExecutor executor = new FakeInventoryRedisExecutor();
        executor.warmupStock(1L, 5, true);
        executor.warmupStock(2L, 1, true);
        RedisStockCommandService service = new RedisStockCommandService(mock(StockSkuMapper.class), executor);

        StockPreDeductRequestDTO request = new StockPreDeductRequestDTO();
        request.setRequestId("req-1");
        request.setItems(List.of(item(1L, 3), item(2L, 2)));

        StockPreDeductResultDTO result = service.preDeductByRedis(request);

        assertFalse(result.getAllSuccess());
        assertEquals(0, result.getSuccessItems().size());
        assertEquals("库存不足", result.getFailedItems().get(0).get("reason"));
        assertEquals(5L, executor.getStock(1L));
        assertEquals(1L, executor.getStock(2L));
    }

    @Test
    void duplicateRequestIdShouldBeTreatedAsSuccess() {
        FakeInventoryRedisExecutor executor = new FakeInventoryRedisExecutor();
        executor.warmupStock(8L, 10, true);
        RedisStockCommandService service = new RedisStockCommandService(mock(StockSkuMapper.class), executor);

        StockPreDeductRequestDTO first = new StockPreDeductRequestDTO();
        first.setRequestId("req-duplicate");
        first.setItems(List.of(item(8L, 4)));

        StockPreDeductResultDTO firstResult = service.preDeductByRedis(first);
        assertTrue(firstResult.getAllSuccess());
        assertEquals(6L, executor.getStock(8L));

        StockPreDeductResultDTO secondResult = service.preDeductByRedis(first);
        assertTrue(secondResult.getAllSuccess());
        assertEquals(1, secondResult.getSuccessItems().size());
        assertEquals(6L, executor.getStock(8L));
    }

    @Test
    void duplicateRequestItemShouldNotBeRolledBackWhenLaterItemFails() {
        FakeInventoryRedisExecutor executor = new FakeInventoryRedisExecutor();
        executor.warmupStock(11L, 5, true);
        executor.warmupStock(12L, 1, true);
        RedisStockCommandService service = new RedisStockCommandService(mock(StockSkuMapper.class), executor);

        StockPreDeductRequestDTO first = new StockPreDeductRequestDTO();
        first.setRequestId("req-mixed");
        first.setItems(List.of(item(11L, 2)));
        assertTrue(service.preDeductByRedis(first).getAllSuccess());
        assertEquals(3L, executor.getStock(11L));

        StockPreDeductRequestDTO second = new StockPreDeductRequestDTO();
        second.setRequestId("req-mixed");
        second.setItems(List.of(item(11L, 2), item(12L, 2)));

        StockPreDeductResultDTO secondResult = service.preDeductByRedis(second);

        assertFalse(secondResult.getAllSuccess());
        assertEquals("库存不足", secondResult.getFailedItems().get(0).get("reason"));
        assertEquals(3L, executor.getStock(11L));
        assertEquals(1L, executor.getStock(12L));
    }

    @Test
    void preDeductShouldNotOversellUnderConcurrency() throws Exception {
        FakeInventoryRedisExecutor executor = new FakeInventoryRedisExecutor();
        executor.warmupStock(9L, 10, true);
        RedisStockCommandService service = new RedisStockCommandService(mock(StockSkuMapper.class), executor);

        int threads = 20;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            final int idx = i;
            futures.add(pool.submit(() -> {
                startLatch.await();
                StockPreDeductRequestDTO request = new StockPreDeductRequestDTO();
                request.setRequestId("req-" + idx);
                request.setItems(List.of(item(9L, 1)));
                return service.preDeductByRedis(request).getAllSuccess();
            }));
        }

        startLatch.countDown();

        int successCount = 0;
        for (Future<Boolean> future : futures) {
            if (future.get()) {
                successCount++;
            }
        }
        pool.shutdownNow();

        assertEquals(10, successCount);
        assertEquals(0L, executor.getStock(9L));
    }

    @Test
    void preDeductThenRollbackReturnsStock() {
        FakeInventoryRedisExecutor executor = new FakeInventoryRedisExecutor();
        executor.warmupStock(3L, 10, true);
        RedisStockCommandService service = new RedisStockCommandService(mock(StockSkuMapper.class), executor);

        StockPreDeductRequestDTO deductReq = new StockPreDeductRequestDTO();
        deductReq.setRequestId("req-rollback-test");
        deductReq.setItems(List.of(item(3L, 4)));

        StockPreDeductResultDTO result = service.preDeductByRedis(deductReq);
        assertTrue(result.getAllSuccess());
        assertEquals(6L, executor.getStock(3L));

        StockRollbackRequestDTO rollbackReq = new StockRollbackRequestDTO();
        rollbackReq.setRequestId("req-rollback-test");
        StockRollbackRequestDTO.Item rollbackItem = new StockRollbackRequestDTO.Item();
        rollbackItem.setSkuId(3L);
        rollbackItem.setCount(4);
        rollbackReq.setItems(List.of(rollbackItem));
        service.rollbackRedisStock(rollbackReq);

        assertEquals(10L, executor.getStock(3L));
    }

    @Test
    void rollbackShouldBeIdempotent() {
        FakeInventoryRedisExecutor executor = new FakeInventoryRedisExecutor();
        executor.warmupStock(6L, 10, true);
        RedisStockCommandService service = new RedisStockCommandService(mock(StockSkuMapper.class), executor);

        StockPreDeductRequestDTO deductReq = new StockPreDeductRequestDTO();
        deductReq.setRequestId("req-idempotent-rollback");
        deductReq.setItems(List.of(item(6L, 3)));
        assertTrue(service.preDeductByRedis(deductReq).getAllSuccess());
        assertEquals(7L, executor.getStock(6L));

        StockRollbackRequestDTO rollbackReq = new StockRollbackRequestDTO();
        rollbackReq.setRequestId("req-idempotent-rollback");
        StockRollbackRequestDTO.Item rollbackItem = new StockRollbackRequestDTO.Item();
        rollbackItem.setSkuId(6L);
        rollbackItem.setCount(3);
        rollbackReq.setItems(List.of(rollbackItem));

        service.rollbackRedisStock(rollbackReq);
        service.rollbackRedisStock(rollbackReq);

        assertEquals(10L, executor.getStock(6L));
    }

    @Test
    void multiplePreDeductsReduceStockCorrectly() {
        FakeInventoryRedisExecutor executor = new FakeInventoryRedisExecutor();
        executor.warmupStock(4L, 10, true);
        RedisStockCommandService service = new RedisStockCommandService(mock(StockSkuMapper.class), executor);

        for (int i = 0; i < 3; i++) {
            StockPreDeductRequestDTO req = new StockPreDeductRequestDTO();
            req.setRequestId("req-multi-" + i);
            req.setItems(List.of(item(4L, 3)));
            StockPreDeductResultDTO result = service.preDeductByRedis(req);
            assertTrue(result.getAllSuccess(), "round " + i + " should succeed");
        }

        assertEquals(1L, executor.getStock(4L));
    }

    private static StockPreDeductRequestDTO.Item item(Long skuId, Integer count) {
        StockPreDeductRequestDTO.Item item = new StockPreDeductRequestDTO.Item();
        item.setSkuId(skuId);
        item.setCount(count);
        return item;
    }

    private static class FakeInventoryRedisExecutor implements InventoryRedisExecutor {

        private final ConcurrentHashMap<Long, Long> stock = new ConcurrentHashMap<>();
        private final Set<String> requests = ConcurrentHashMap.newKeySet();

        @Override
        public boolean warmupStock(Long skuId, long stock, boolean forceRefresh) {
            if (forceRefresh) {
                this.stock.put(skuId, stock);
                return true;
            }
            return this.stock.putIfAbsent(skuId, stock) == null;
        }

        @Override
        public Long getStock(Long skuId) {
            return stock.get(skuId);
        }

        @Override
        public Long preDeduct(Long skuId, String requestId, int deductCount, long requestTtlSeconds) {
            synchronized (this) {
                if (requests.contains(requestId)) {
                    return 2L;
                }
                Long current = stock.get(skuId);
                if (current == null) {
                    return -1L;
                }
                if (current < deductCount) {
                    return 0L;
                }
                stock.put(skuId, current - deductCount);
                requests.add(requestId);
                return 1L;
            }
        }

        @Override
        public void rollback(Long skuId, String requestId, int rollbackCount) {
            synchronized (this) {
                if (!requests.remove(requestId)) {
                    return;
                }
                stock.merge(skuId, (long) rollbackCount, Long::sum);
            }
        }
    }
}
