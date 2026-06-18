package com.xtx.inventory.controller;

import com.xtx.api.inventory.dto.StockReserveRequestDTO;
import com.xtx.api.inventory.dto.StockReserveResultDTO;
import com.xtx.common.core.result.ApiResponse;
import com.xtx.inventory.service.InventoryAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 库存内部控制器（Feign 调用）
 * 提供订单服务等内部系统调用的库存预占、释放和扣减接口
 */
@RestController
@RequiredArgsConstructor
public class StockInnerController {

    private final InventoryAppService inventoryAppService;

    /**
     * 预占库存（下单时调用）
     *
     * @param request 预占请求，包含订单号和 SKU 列表
     * @return 预占结果
     */
    @PostMapping("/inner/stocks/reserve")
    public ApiResponse<StockReserveResultDTO> reserve(@RequestBody StockReserveRequestDTO request) {
        StockReserveResultDTO result = inventoryAppService.reserveStocks(request);
        return ApiResponse.success(result);
    }

    /**
     * 释放预占库存（订单取消或超时未支付时调用）
     *
     * @param orderNo 订单编号
     * @return 无数据，仅表示操作成功
     */
    @PostMapping("/inner/stocks/release/{orderNo}")
    public ApiResponse<Void> release(@PathVariable String orderNo) {
        inventoryAppService.releaseStocks(orderNo);
        return ApiResponse.success();
    }

    /**
     * 确认扣减库存（支付完成后调用）
     * 将预占库存转为已售库存
     *
     * @param orderNo 订单编号
     * @return 无数据，仅表示操作成功
     */
    @PostMapping("/inner/stocks/confirm/{orderNo}")
    public ApiResponse<Void> confirm(@PathVariable String orderNo) {
        inventoryAppService.confirmDeduction(orderNo);
        return ApiResponse.success();
    }
}
