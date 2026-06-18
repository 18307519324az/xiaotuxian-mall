package com.xtx.api.inventory;

import com.xtx.api.inventory.dto.StockInfoDTO;
import com.xtx.api.inventory.dto.StockReserveRequestDTO;
import com.xtx.api.inventory.dto.StockReserveResultDTO;
import com.xtx.common.core.result.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 库存服务 Feign 远程调用客户端
 * 提供库存预占、释放、确认扣减以及库存查询接口
 */
@FeignClient(name = "xtx-inventory-service", url = "${services.inventory:http://localhost:8106}", contextId = "stockClient", path = "/inner/stocks")
public interface StockClient {

    /**
     * 预占/锁定库存
     *
     * @param request 库存预占请求
     * @return 预占结果
     */
    @PostMapping("/reserve")
    ApiResponse<StockReserveResultDTO> reserveStocks(@RequestBody StockReserveRequestDTO request);

    /**
     * 释放预占的库存（取消订单时调用）
     *
     * @param orderNo 订单编号
     * @return 无返回值
     */
    @PostMapping("/release/{orderNo}")
    ApiResponse<Void> releaseStocks(@PathVariable("orderNo") String orderNo);

    /**
     * 确认扣减库存（支付完成时调用）
     *
     * @param orderNo 订单编号
     * @return 无返回值
     */
    @PostMapping("/confirm/{orderNo}")
    ApiResponse<Void> confirmDeduction(@PathVariable("orderNo") String orderNo);

    /**
     * 查询指定 SKU 的库存信息
     *
     * @param skuId SKU ID
     * @return 库存信息
     */
    @GetMapping("/sku/{skuId}")
    ApiResponse<StockInfoDTO> getStockInfo(@PathVariable("skuId") Long skuId);
}
