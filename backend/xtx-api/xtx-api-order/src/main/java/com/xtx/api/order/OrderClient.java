package com.xtx.api.order;

import com.xtx.api.order.dto.OrderSnapshotDTO;
import com.xtx.api.order.dto.OrderStatusUpdateDTO;
import com.xtx.common.core.result.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 订单服务 Feign 远程调用客户端
 * 提供订单状态更新和订单快照查询接口
 */
@FeignClient(name = "xtx-order-service", url = "${services.order:http://localhost:8108}", contextId = "orderClient", path = "/inner/orders")
public interface OrderClient {

    /**
     * 更新订单状态
     *
     * @param request 订单状态更新请求
     * @return 无返回值
     */
    @PutMapping("/status")
    ApiResponse<Void> updateOrderStatus(@RequestBody OrderStatusUpdateDTO request);

    /**
     * 查询订单快照
     *
     * @param orderId 订单 ID
     * @return 订单快照信息
     */
    @GetMapping("/{orderId}")
    ApiResponse<OrderSnapshotDTO> getOrderSnapshot(@PathVariable("orderId") Long orderId);
}
