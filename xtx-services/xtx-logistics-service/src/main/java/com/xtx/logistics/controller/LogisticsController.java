package com.xtx.logistics.controller;

import com.xtx.common.web.annotation.XUserId;
import com.xtx.common.core.result.FrontResponse;
import com.xtx.logistics.service.LogisticsAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 物流控制器
 */
@RestController
@RequestMapping("/member/order")
@RequiredArgsConstructor
public class LogisticsController {

    private final LogisticsAppService logisticsAppService;

    /**
     * 获取订单物流信息
     *
     * @param orderId 订单ID
     * @param userId  用户ID
     * @return 物流信息及轨迹
     */
    @GetMapping("/{orderId}/logistics")
    public FrontResponse<Map<String, Object>> getLogistics(@PathVariable Long orderId, @XUserId Long userId) {
        Map<String, Object> result = logisticsAppService.getOrderLogistics(orderId);
        return FrontResponse.success(result);
    }
}
