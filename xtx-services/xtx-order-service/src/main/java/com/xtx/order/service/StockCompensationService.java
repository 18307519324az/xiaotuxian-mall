package com.xtx.order.service;

import com.xtx.api.order.dto.AsyncOrderCreateMessageDTO;
import com.xtx.order.dto.SubmitOrderDTO;
import com.xtx.order.entity.OrderInfo;
import com.xtx.order.entity.StockCompensationTask;
import com.xtx.order.mapper.OrderInfoMapper;
import com.xtx.order.mapper.StockCompensationTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockCompensationService {

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_SUCCESS = 1;
    public static final int STATUS_FAILED = 2;

    private final StockCompensationTaskMapper stockCompensationTaskMapper;
    private final OrderInfoMapper orderInfoMapper;
    private final OrderStockPreDeductService orderStockPreDeductService;
    private final StockReleaseOrchestrator stockReleaseOrchestrator;

    @Transactional(rollbackFor = Exception.class)
    public void createTasks(AsyncOrderCreateMessageDTO message) {
        if (message == null || message.getItems() == null || message.getItems().isEmpty()) {
            return;
        }
        for (AsyncOrderCreateMessageDTO.Item item : message.getItems()) {
            StockCompensationTask existing = stockCompensationTaskMapper
                    .selectByOrderNoAndSkuId(message.getOrderNo(), item.getSkuId());
            if (existing != null) {
                continue;
            }
            StockCompensationTask task = new StockCompensationTask();
            task.setOrderNo(message.getOrderNo());
            task.setRequestId(message.getRequestId());
            task.setSkuId(item.getSkuId());
            task.setCount(item.getCount());
            task.setStatus(STATUS_PENDING);
            task.setRetryCount(0);
            task.setCreateTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());
            stockCompensationTaskMapper.insert(task);
        }
    }

    public void processRetryableTasks() {
        for (StockCompensationTask task : stockCompensationTaskMapper.selectRetryableTasks()) {
            if (!shouldRetry(task)) {
                continue;
            }
            processTask(task);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void processTask(StockCompensationTask task) {
        try {
            OrderInfo order = orderInfoMapper.selectByOrderNo(task.getOrderNo());
            if (order != null) {
                stockReleaseOrchestrator.releaseDatabaseReservation(task.getOrderNo());
            }
            rollbackRedis(task);
            task.setStatus(STATUS_SUCCESS);
            task.setFailReason(null);
            task.setUpdateTime(LocalDateTime.now());
            stockCompensationTaskMapper.update(task);
        } catch (Exception e) {
            task.setStatus(STATUS_FAILED);
            task.setRetryCount((task.getRetryCount() != null ? task.getRetryCount() : 0) + 1);
            task.setFailReason(e.getMessage());
            task.setUpdateTime(LocalDateTime.now());
            stockCompensationTaskMapper.update(task);
            log.error("库存补偿任务执行失败, orderNo={}, skuId={}, retryCount={}",
                    task.getOrderNo(), task.getSkuId(), task.getRetryCount(), e);
        }
    }

    public boolean orderExists(String orderNo) {
        return orderInfoMapper.selectByOrderNo(orderNo) != null;
    }

    public void rollbackRedisImmediatelyIfOrderMissing(AsyncOrderCreateMessageDTO message) {
        if (message == null || message.getOrderNo() == null || orderExists(message.getOrderNo())) {
            return;
        }
        rollbackRedis(message.getRequestId(), message.getOrderNo(), toSubmitOrderItems(message));
    }

    private void rollbackRedis(StockCompensationTask task) {
        rollbackRedis(task.getRequestId(), task.getOrderNo(), List.of(toSubmitOrderItem(task)));
    }

    private void rollbackRedis(String requestId, String orderNo, List<SubmitOrderDTO.OrderItemDTO> items) {
        stockReleaseOrchestrator.rollbackRedisStock(requestId, orderNo, items);
    }

    private List<SubmitOrderDTO.OrderItemDTO> toSubmitOrderItems(AsyncOrderCreateMessageDTO message) {
        return message.getItems().stream()
                .map(item -> {
                    SubmitOrderDTO.OrderItemDTO dto = new SubmitOrderDTO.OrderItemDTO();
                    dto.setSkuId(item.getSkuId());
                    dto.setCount(item.getCount());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private SubmitOrderDTO.OrderItemDTO toSubmitOrderItem(StockCompensationTask task) {
        SubmitOrderDTO.OrderItemDTO dto = new SubmitOrderDTO.OrderItemDTO();
        dto.setSkuId(task.getSkuId());
        dto.setCount(task.getCount());
        return dto;
    }

    private boolean shouldRetry(StockCompensationTask task) {
        if (task.getStatus() == null) {
            return false;
        }
        if (task.getStatus() == STATUS_PENDING) {
            return true;
        }
        return task.getStatus() == STATUS_FAILED && (task.getRetryCount() == null || task.getRetryCount() < 5);
    }
}
