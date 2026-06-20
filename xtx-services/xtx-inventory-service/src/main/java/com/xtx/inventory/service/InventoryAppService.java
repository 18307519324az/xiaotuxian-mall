package com.xtx.inventory.service;

import com.xtx.api.inventory.dto.StockPreDeductRequestDTO;
import com.xtx.api.inventory.dto.StockPreDeductResultDTO;
import com.xtx.api.inventory.dto.StockRollbackRequestDTO;
import com.xtx.api.inventory.dto.StockReserveRequestDTO;
import com.xtx.api.inventory.dto.StockReserveResultDTO;
import com.xtx.api.inventory.dto.StockWarmupRequestDTO;
import com.xtx.api.inventory.dto.StockWarmupResultDTO;

import java.util.Map;

/**
 * 库存应用服务接口
 * 提供库存查询、预占、释放和确认扣减等库存管理功能
 */
public interface InventoryAppService {

    /**
     * 获取 SKU 库存信息
     *
     * @param skuId SKU ID
     * @return 库存信息（可用库存、锁定库存等）
     */
    Map<String, Object> getStockInfo(Long skuId);

    StockWarmupResultDTO warmupStocks(StockWarmupRequestDTO request);

    Long getRedisStock(Long skuId);

    StockPreDeductResultDTO preDeductByRedis(StockPreDeductRequestDTO request);

    void rollbackRedisStock(StockRollbackRequestDTO request);

    /**
     * 预占库存（下单时调用）
     * 使用乐观锁保证并发安全，支持幂等（防止重复预占）
     *
     * @param request 预占请求
     * @return 预占结果
     */
    StockReserveResultDTO reserveStocks(StockReserveRequestDTO request);

    /**
     * 释放预占库存（订单取消/超时未支付时调用）
     *
     * @param orderNo 订单编号
     */
    void releaseStocks(String orderNo);

    /**
     * 确认扣减库存（支付完成后调用）
     * 将预占库存转为已售库存
     *
     * @param orderNo 订单编号
     */
    void confirmDeduction(String orderNo);
}
