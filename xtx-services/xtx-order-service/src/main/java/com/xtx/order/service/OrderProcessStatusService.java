package com.xtx.order.service;

import com.xtx.api.order.dto.OrderProcessStatusDTO;
import com.xtx.common.core.constant.RedisKeyConstants;
import com.xtx.order.entity.OrderInfo;
import com.xtx.order.mapper.OrderInfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 订单处理状态服务。
 * 管理异步下单订单的处理进度状态，优先从 Redis 查询，兜底查订单表。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderProcessStatusService {

    private static final long PROCESS_TTL_MINUTES = 30;

    private final RedisTemplate<String, Object> redisTemplate;
    private final OrderInfoMapper orderInfoMapper;

    /**
     * 标记订单为处理中。
     */
    public void markProcessing(String orderNo, String requestId) {
        OrderProcessStatusDTO dto = new OrderProcessStatusDTO();
        dto.setOrderNo(orderNo);
        dto.setRequestId(requestId);
        dto.setStatus(OrderProcessStatusDTO.STATUS_PROCESSING);
        dto.setMessage("订单处理中");
        dto.setCreateTime(LocalDateTime.now());
        dto.setUpdateTime(LocalDateTime.now());
        saveToRedis(orderNo, dto);
        log.info("订单处理状态 -> PROCESSING, orderNo={}", orderNo);
    }

    /**
     * 标记订单为创建成功。
     */
    public void markSuccess(String orderNo, String requestId, Long orderId) {
        OrderProcessStatusDTO dto = new OrderProcessStatusDTO();
        dto.setOrderNo(orderNo);
        dto.setRequestId(requestId);
        dto.setStatus(OrderProcessStatusDTO.STATUS_SUCCESS);
        dto.setMessage("订单创建成功");
        dto.setOrderId(orderId);
        dto.setUpdateTime(LocalDateTime.now());
        saveToRedis(orderNo, dto);
        log.info("订单处理状态 -> SUCCESS, orderNo={}, orderId={}", orderNo, orderId);
    }

    /**
     * 标记订单为创建失败。
     */
    public void markFailed(String orderNo, String requestId, String reason) {
        OrderProcessStatusDTO dto = new OrderProcessStatusDTO();
        dto.setOrderNo(orderNo);
        dto.setRequestId(requestId);
        dto.setStatus(OrderProcessStatusDTO.STATUS_FAILED);
        dto.setMessage(reason != null ? reason : "订单创建失败");
        dto.setUpdateTime(LocalDateTime.now());
        saveToRedis(orderNo, dto);
        log.warn("订单处理状态 -> FAILED, orderNo={}, reason={}", orderNo, reason);
    }

    /**
     * 查询订单处理状态。
     * 优先查 Redis，如果不存在则查订单表兜底。
     */
    public OrderProcessStatusDTO getStatus(String orderNo) {
        // 优先查 Redis
        String key = buildKey(orderNo);
        OrderProcessStatusDTO cached = (OrderProcessStatusDTO) redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return cached;
        }

        // Redis 不存在则查订单表
        OrderInfo order = orderInfoMapper.selectByOrderNo(orderNo);
        if (order != null) {
            OrderProcessStatusDTO dto = new OrderProcessStatusDTO();
            dto.setOrderNo(order.getOrderNo());
            dto.setStatus(OrderProcessStatusDTO.STATUS_SUCCESS);
            dto.setMessage("订单创建成功");
            dto.setOrderId(order.getId());
            dto.setCreateTime(order.getCreateTime());
            dto.setUpdateTime(order.getUpdateTime());
            return dto;
        }

        // 都不存在时返回 null，由调用方处理
        return null;
    }

    private void saveToRedis(String orderNo, OrderProcessStatusDTO dto) {
        String key = buildKey(orderNo);
        redisTemplate.opsForValue().set(key, dto, PROCESS_TTL_MINUTES, TimeUnit.MINUTES);
    }

    private String buildKey(String orderNo) {
        return RedisKeyConstants.ORDER_PROCESS_KEY + orderNo;
    }
}
