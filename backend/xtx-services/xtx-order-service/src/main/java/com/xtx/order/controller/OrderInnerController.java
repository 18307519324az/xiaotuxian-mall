package com.xtx.order.controller;

import com.xtx.common.core.result.ApiResponse;
import com.xtx.order.dto.OrderStatusUpdateDTO;
import com.xtx.order.service.OrderAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 订单内部控制器（服务间调用）
 */
@RestController
@RequestMapping("/inner/orders")
@RequiredArgsConstructor
public class OrderInnerController {

    private final OrderAppService orderAppService;

    /**
     * 更新订单状态（由支付回调等服务调用）
     *
     * @param dto 状态更新参数
     * @return 操作结果
     */
    @PutMapping("/status")
    public ApiResponse<Void> updateOrderStatus(@RequestBody OrderStatusUpdateDTO dto) {
        orderAppService.updateOrderStatus(dto);
        return ApiResponse.success();
    }

    /**
     * 获取订单快照
     *
     * @param orderId 订单ID
     * @return 订单快照数据
     */
    @GetMapping("/{orderId}")
    public ApiResponse<Map<String, Object>> getOrderSnapshot(@PathVariable Long orderId) {
        Map<String, Object> snapshot = orderAppService.getOrderSnapshot(orderId);
        return ApiResponse.success(snapshot);
    }
}
