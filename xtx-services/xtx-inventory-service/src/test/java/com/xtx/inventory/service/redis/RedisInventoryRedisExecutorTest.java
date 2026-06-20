package com.xtx.inventory.service.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisInventoryRedisExecutorTest {

    private RedisTemplate<String, Object> redisTemplate;
    private ValueOperations<String, Object> valueOperations;
    private RedisInventoryRedisExecutor executor;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(RedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        executor = new RedisInventoryRedisExecutor(redisTemplate);
    }

    @Test
    void warmupWithForceRefreshShouldWriteDirectly() {
        boolean success = executor.warmupStock(1001L, 12L, true);

        assertTrue(success);
        verify(valueOperations).set("xtx:stock:sku:1001", 12L);
    }

    @Test
    void warmupWithoutForceRefreshShouldUseSetIfAbsentWithTtl() {
        when(valueOperations.setIfAbsent("xtx:stock:sku:1002", 8L, 30, TimeUnit.MINUTES)).thenReturn(Boolean.TRUE);

        boolean success = executor.warmupStock(1002L, 8L, false);

        assertTrue(success);
        verify(valueOperations).setIfAbsent("xtx:stock:sku:1002", 8L, 30, TimeUnit.MINUTES);
    }

    @Test
    void preDeductShouldExecuteLuaWithExpectedKeysAndArgs() {
        when(redisTemplate.execute(any(DefaultRedisScript.class), eq(List.of("xtx:stock:sku:1003", "xtx:order:request:req-1003")), eq(2), eq("req-1003"), eq(1800L)))
                .thenReturn(1L);

        Long result = executor.preDeduct(1003L, "req-1003", 2, 1800L);

        assertEquals(1L, result);
        verify(redisTemplate).execute(
                any(DefaultRedisScript.class),
                eq(List.of("xtx:stock:sku:1003", "xtx:order:request:req-1003")),
                eq(2),
                eq("req-1003"),
                eq(1800L)
        );
    }

    @Test
    void rollbackShouldExecuteLuaWithExpectedKeysAndArgs() {
        when(redisTemplate.execute(any(DefaultRedisScript.class), eq(List.of("xtx:stock:sku:1004", "xtx:order:request:req-1004")), eq(3)))
                .thenReturn(1L);

        executor.rollback(1004L, "req-1004", 3);

        verify(redisTemplate).execute(
                any(DefaultRedisScript.class),
                eq(List.of("xtx:stock:sku:1004", "xtx:order:request:req-1004")),
                eq(3)
        );
    }

    @Test
    void getStockShouldReadCurrentValue() {
        when(valueOperations.get("xtx:stock:sku:1005")).thenReturn(15L);

        Long result = executor.getStock(1005L);

        assertEquals(15L, result);
        verify(valueOperations).get("xtx:stock:sku:1005");
    }
}
