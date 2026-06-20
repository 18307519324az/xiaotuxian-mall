package com.xtx.order.service;

import com.xtx.common.core.constant.RedisKeyConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OrderRequestTrackingService {

    private static final long REQUEST_TTL_MINUTES = 30;

    private final RedisTemplate<String, Object> redisTemplate;

    public void saveRequestId(String orderNo, String requestId) {
        redisTemplate.opsForValue().set(buildKey(orderNo), requestId, REQUEST_TTL_MINUTES, TimeUnit.MINUTES);
    }

    public String getRequestId(String orderNo) {
        Object value = redisTemplate.opsForValue().get(buildKey(orderNo));
        return value != null ? value.toString() : null;
    }

    public void removeRequestId(String orderNo) {
        redisTemplate.delete(buildKey(orderNo));
    }

    private String buildKey(String orderNo) {
        return RedisKeyConstants.ORDER_REQUEST_KEY + orderNo;
    }
}
