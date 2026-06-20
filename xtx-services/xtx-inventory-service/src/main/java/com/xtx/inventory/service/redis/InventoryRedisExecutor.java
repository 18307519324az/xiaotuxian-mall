package com.xtx.inventory.service.redis;

public interface InventoryRedisExecutor {

    boolean warmupStock(Long skuId, long stock, boolean forceRefresh);

    Long getStock(Long skuId);

    Long preDeduct(Long skuId, String requestId, int deductCount, long requestTtlSeconds);

    void rollback(Long skuId, String requestId, int rollbackCount);
}
