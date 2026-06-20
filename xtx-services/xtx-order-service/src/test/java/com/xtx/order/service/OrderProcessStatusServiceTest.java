package com.xtx.order.service;

import com.xtx.api.order.dto.OrderProcessStatusDTO;
import com.xtx.common.core.constant.RedisKeyConstants;
import com.xtx.order.entity.OrderInfo;
import com.xtx.order.mapper.OrderInfoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderProcessStatusServiceTest {

    @Mock RedisTemplate<String, Object> redisTemplate;
    @Mock OrderInfoMapper orderInfoMapper;
    @Mock ValueOperations<String, Object> valueOps;

    OrderProcessStatusService service;

    @BeforeEach
    void setUp() {
        service = new OrderProcessStatusService(redisTemplate, orderInfoMapper);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void markProcessingShouldSaveToRedis() {
        service.markProcessing("ORDER001", "req-001");

        verify(valueOps).set(
                eq(RedisKeyConstants.ORDER_PROCESS_KEY + "ORDER001"),
                any(OrderProcessStatusDTO.class),
                eq(30L),
                eq(TimeUnit.MINUTES));
    }

    @Test
    void markProcessingShouldSetProcessingStatus() {
        service.markProcessing("ORDER001", "req-001");

        verify(valueOps).set(
                eq(RedisKeyConstants.ORDER_PROCESS_KEY + "ORDER001"),
                argThat(dto -> {
                    OrderProcessStatusDTO status = (OrderProcessStatusDTO) dto;
                    return OrderProcessStatusDTO.STATUS_PROCESSING.equals(status.getStatus())
                            && "ORDER001".equals(status.getOrderNo())
                            && "req-001".equals(status.getRequestId());
                }),
                eq(30L),
                eq(TimeUnit.MINUTES));
    }

    @Test
    void markSuccessShouldSaveToRedis() {
        service.markSuccess("ORDER001", "req-001", 100L);

        verify(valueOps).set(
                eq(RedisKeyConstants.ORDER_PROCESS_KEY + "ORDER001"),
                argThat(dto -> {
                    OrderProcessStatusDTO status = (OrderProcessStatusDTO) dto;
                    return OrderProcessStatusDTO.STATUS_SUCCESS.equals(status.getStatus())
                            && status.getOrderId() == 100L;
                }),
                eq(30L),
                eq(TimeUnit.MINUTES));
    }

    @Test
    void markFailedShouldSaveToRedis() {
        service.markFailed("ORDER001", "req-001", "库存不足");

        verify(valueOps).set(
                eq(RedisKeyConstants.ORDER_PROCESS_KEY + "ORDER001"),
                argThat(dto -> {
                    OrderProcessStatusDTO status = (OrderProcessStatusDTO) dto;
                    return OrderProcessStatusDTO.STATUS_FAILED.equals(status.getStatus())
                            && "库存不足".equals(status.getMessage());
                }),
                eq(30L),
                eq(TimeUnit.MINUTES));
    }

    @Test
    void getStatusShouldReturnFromRedisFirst() {
        OrderProcessStatusDTO cached = new OrderProcessStatusDTO();
        cached.setOrderNo("ORDER001");
        cached.setStatus(OrderProcessStatusDTO.STATUS_PROCESSING);
        when(valueOps.get(RedisKeyConstants.ORDER_PROCESS_KEY + "ORDER001")).thenReturn(cached);

        OrderProcessStatusDTO result = service.getStatus("ORDER001");

        assertNotNull(result);
        assertEquals(OrderProcessStatusDTO.STATUS_PROCESSING, result.getStatus());
        verify(orderInfoMapper, never()).selectByOrderNo(anyString());
    }

    @Test
    void getStatusShouldFallbackToDbWhenRedisMiss() {
        when(valueOps.get(RedisKeyConstants.ORDER_PROCESS_KEY + "ORDER001")).thenReturn(null);

        OrderInfo order = new OrderInfo();
        order.setOrderNo("ORDER001");
        order.setId(100L);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        when(orderInfoMapper.selectByOrderNo("ORDER001")).thenReturn(order);

        OrderProcessStatusDTO result = service.getStatus("ORDER001");

        assertNotNull(result);
        assertEquals(OrderProcessStatusDTO.STATUS_SUCCESS, result.getStatus());
        assertEquals(Long.valueOf(100L), result.getOrderId());
    }

    @Test
    void getStatusShouldReturnNullWhenBothMiss() {
        when(valueOps.get(RedisKeyConstants.ORDER_PROCESS_KEY + "ORDER001")).thenReturn(null);
        when(orderInfoMapper.selectByOrderNo("ORDER001")).thenReturn(null);

        OrderProcessStatusDTO result = service.getStatus("ORDER001");

        assertNull(result);
    }
}
