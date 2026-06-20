package com.xtx.inventory.service;

import com.xtx.api.inventory.dto.StockPreDeductRequestDTO;
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
import static org.mockito.Mockito.mock;

class InventoryConcurrentDeductTest {

    @Test
    void concurrentDeductShouldNeverOversell() throws Exception {
        FakeInventoryRedisExecutor executor = new FakeInventoryRedisExecutor();
        executor.warmupStock(1027026L, 10, true);
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
                request.setRequestId("inventory-concurrent-" + idx);
                request.setItems(List.of(item(1027026L, 1)));
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
        assertEquals(0L, executor.getStock(1027026L));
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
