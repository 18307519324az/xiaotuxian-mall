package com.xtx.order.service;

import com.xtx.api.inventory.StockClient;
import com.xtx.order.dto.SubmitOrderDTO;
import com.xtx.order.entity.OrderGoods;
import com.xtx.order.mapper.OrderGoodsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockReleaseOrchestrator {

    private final StockClient stockClient;
    private final OrderGoodsMapper orderGoodsMapper;
    private final OrderStockPreDeductService orderStockPreDeductService;
    private final OrderRequestTrackingService orderRequestTrackingService;

    public void releaseOrderStocks(Long orderId, String orderNo) {
        releaseDatabaseReservation(orderNo);
        rollbackRedisStock(orderNo, orderId);
    }

    public void releaseDatabaseReservation(String orderNo) {
        stockClient.releaseStocks(orderNo);
    }

    public void rollbackRedisStock(String requestId, String orderNo, List<SubmitOrderDTO.OrderItemDTO> items) {
        if (requestId == null || items == null || items.isEmpty()) {
            return;
        }
        orderStockPreDeductService.rollback(requestId, orderNo, items);
        orderRequestTrackingService.removeRequestId(orderNo);
    }

    public void rollbackRedisStock(String orderNo, Long orderId) {
        String requestId = orderRequestTrackingService.getRequestId(orderNo);
        if (requestId == null) {
            log.warn("未找到 requestId，跳过 Redis 库存回补, orderNo={}", orderNo);
            return;
        }
        List<SubmitOrderDTO.OrderItemDTO> items = orderGoodsMapper.selectByOrderId(orderId).stream()
                .map(this::toSubmitOrderItem)
                .collect(Collectors.toList());
        rollbackRedisStock(requestId, orderNo, items);
    }

    private SubmitOrderDTO.OrderItemDTO toSubmitOrderItem(OrderGoods goods) {
        SubmitOrderDTO.OrderItemDTO dto = new SubmitOrderDTO.OrderItemDTO();
        dto.setSkuId(goods.getSkuId());
        dto.setCount(goods.getCount());
        return dto;
    }
}
