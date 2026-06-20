package com.xtx.order.service;

import com.xtx.api.order.dto.AsyncOrderCreateMessageDTO;
import com.xtx.common.core.result.ApiResponse;
import com.xtx.order.entity.OrderInfo;
import com.xtx.order.entity.StockCompensationTask;
import com.xtx.order.mapper.OrderInfoMapper;
import com.xtx.order.mapper.StockCompensationTaskMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StockCompensationServiceTest {

    @Mock StockCompensationTaskMapper stockCompensationTaskMapper;
    @Mock OrderInfoMapper orderInfoMapper;
    @Mock OrderStockPreDeductService orderStockPreDeductService;
    @Mock StockReleaseOrchestrator stockReleaseOrchestrator;

    @Captor ArgumentCaptor<StockCompensationTask> taskCaptor;

    StockCompensationService stockCompensationService;

    @BeforeEach
    void setUp() {
        stockCompensationService = new StockCompensationService(
                stockCompensationTaskMapper, orderInfoMapper, orderStockPreDeductService, stockReleaseOrchestrator);
    }

    @Test
    void createTasksShouldInsertPendingTask() {
        AsyncOrderCreateMessageDTO message = message();
        when(stockCompensationTaskMapper.selectByOrderNoAndSkuId("ORDER001", 1001L)).thenReturn(null);

        stockCompensationService.createTasks(message);

        verify(stockCompensationTaskMapper).insert(taskCaptor.capture());
        assertEquals("ORDER001", taskCaptor.getValue().getOrderNo());
        assertEquals("req-001", taskCaptor.getValue().getRequestId());
        assertEquals(1001L, taskCaptor.getValue().getSkuId());
        assertEquals(1, taskCaptor.getValue().getCount());
        assertEquals(0, taskCaptor.getValue().getStatus());
    }

    @Test
    void rollbackImmediatelyShouldRollbackWhenOrderMissing() {
        AsyncOrderCreateMessageDTO message = message();
        when(orderInfoMapper.selectByOrderNo("ORDER001")).thenReturn(null);

        stockCompensationService.rollbackRedisImmediatelyIfOrderMissing(message);

        verify(stockReleaseOrchestrator).rollbackRedisStock(eq("req-001"), eq("ORDER001"), any(List.class));
    }

    @Test
    void processTaskShouldReleaseDbAndMarkSuccess() {
        StockCompensationTask task = task();
        OrderInfo order = new OrderInfo();
        order.setOrderNo("ORDER001");
        when(orderInfoMapper.selectByOrderNo("ORDER001")).thenReturn(order);

        stockCompensationService.processTask(task);

        verify(stockReleaseOrchestrator).releaseDatabaseReservation("ORDER001");
        verify(stockReleaseOrchestrator).rollbackRedisStock(eq("req-001"), eq("ORDER001"), any(List.class));
        verify(stockCompensationTaskMapper).update(taskCaptor.capture());
        assertEquals(1, taskCaptor.getValue().getStatus());
    }

    @Test
    void processTaskFailureShouldIncreaseRetryCount() {
        StockCompensationTask task = task();
        doThrow(new RuntimeException("rollback failed"))
                .when(stockReleaseOrchestrator).rollbackRedisStock(eq("req-001"), eq("ORDER001"), any(List.class));

        stockCompensationService.processTask(task);

        verify(stockCompensationTaskMapper).update(taskCaptor.capture());
        assertEquals(2, taskCaptor.getValue().getStatus());
        assertEquals(1, taskCaptor.getValue().getRetryCount());
    }

    private AsyncOrderCreateMessageDTO message() {
        AsyncOrderCreateMessageDTO message = new AsyncOrderCreateMessageDTO();
        message.setOrderNo("ORDER001");
        message.setRequestId("req-001");
        AsyncOrderCreateMessageDTO.Item item = new AsyncOrderCreateMessageDTO.Item();
        item.setSkuId(1001L);
        item.setCount(1);
        message.setItems(List.of(item));
        return message;
    }

    private StockCompensationTask task() {
        StockCompensationTask task = new StockCompensationTask();
        task.setOrderNo("ORDER001");
        task.setRequestId("req-001");
        task.setSkuId(1001L);
        task.setCount(1);
        task.setStatus(0);
        task.setRetryCount(0);
        return task;
    }
}
