package com.xtx.inventory.service.redis;

import com.xtx.common.core.constant.RedisKeyConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisInventoryRedisExecutor implements InventoryRedisExecutor {

    private final RedisTemplate<String, Object> redisTemplate;

    private final DefaultRedisScript<Long> stockPreDeductScript = buildScript("lua/stock_pre_deduct.lua");

    private final DefaultRedisScript<Long> stockRollbackScript = buildScript("lua/stock_rollback.lua");

    @Override
    public boolean warmupStock(Long skuId, long stock, boolean forceRefresh) {
        String stockKey = stockKey(skuId);
        if (forceRefresh) {
            redisTemplate.opsForValue().set(stockKey, stock);
            return true;
        }
        Boolean success = redisTemplate.opsForValue().setIfAbsent(stockKey, stock, 30, TimeUnit.MINUTES);
        return Boolean.TRUE.equals(success);
    }

    @Override
    public Long getStock(Long skuId) {
        Object value = redisTemplate.opsForValue().get(stockKey(skuId));
        if (value == null) {
            return null;
        }
        return Long.parseLong(String.valueOf(value));
    }

    @Override
    public Long preDeduct(Long skuId, String requestId, int deductCount, long requestTtlSeconds) {
        return redisTemplate.execute(
                stockPreDeductScript,
                List.of(stockKey(skuId), requestKey(requestId)),
                deductCount,
                requestId,
                requestTtlSeconds
        );
    }

    @Override
    public void rollback(Long skuId, String requestId, int rollbackCount) {
        redisTemplate.execute(
                stockRollbackScript,
                List.of(stockKey(skuId), requestKey(requestId)),
                rollbackCount
        );
    }

    private String stockKey(Long skuId) {
        return RedisKeyConstants.STOCK_SKU_KEY + skuId;
    }

    private String requestKey(String requestId) {
        return RedisKeyConstants.ORDER_REQUEST_KEY + requestId;
    }

    private DefaultRedisScript<Long> buildScript(String path) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource(path));
        script.setResultType(Long.class);
        return script;
    }
}
