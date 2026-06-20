package com.xtx.order.service;

import com.xtx.common.core.constant.RedisKeyConstants;
import com.xtx.common.core.exception.BizException;
import com.xtx.common.core.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * OrderTokenService — 下单防重复 Token 服务。
 * <p>
 * 基于 Redis 原子 delete 实现"一次消费"语义：同一 token 只能被消费一次，
 * 第二次 delete 返回 false 拒绝。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderTokenService {

    private final RedisTemplate<String, Object> redisTemplate;

    /** Token 过期时间（分钟） */
    private static final long TOKEN_TTL_MINUTES = 30;

    /**
     * 生成下单 token。
     *
     * @param userId 用户 ID
     * @return token 字符串
     */
    public String generateToken(Long userId) {
        String token = UUID.randomUUID().toString();
        String key = buildKey(userId, token);
        redisTemplate.opsForValue().set(key, "1", TOKEN_TTL_MINUTES, TimeUnit.MINUTES);
        log.debug("下单 token 已生成, userId={}, token={}", userId, token);
        return token;
    }

    /**
     * 校验并消费下单 token。
     * <p>
     * 使用 Redis delete 原子判断，delete 返回 true 表示 token 有效且被消费；
     * 返回 false 表示 token 不存在或已被使用。
     *
     * @param userId 用户 ID
     * @param token  token 字符串
     * @throws BizException token 为空、已使用或不存在时抛出
     */
    public void validateAndConsumeToken(Long userId, String token) {
        if (token == null || token.isBlank()) {
            throw new BizException(ResultCode.ORDER_TOKEN_INVALID.getCode(), "缺少下单 token");
        }

        String key = buildKey(userId, token);
        Boolean deleted = redisTemplate.delete(key);

        if (!Boolean.TRUE.equals(deleted)) {
            log.warn("下单 token 已被使用或不存在, userId={}, token={}", userId, token);
            throw new BizException(ResultCode.ORDER_DUPLICATE_SUBMIT.getCode(), "订单已提交或 token 已过期");
        }

        log.debug("下单 token 消费成功, userId={}, token={}", userId, token);
    }

    private String buildKey(Long userId, String token) {
        return RedisKeyConstants.ORDER_TOKEN_KEY + userId + ":" + token;
    }
}
