package com.xtx.inventory.service.impl;

import com.xtx.api.inventory.dto.StockPreDeductRequestDTO;
import com.xtx.api.inventory.dto.StockPreDeductResultDTO;
import com.xtx.api.inventory.dto.StockRollbackRequestDTO;
import com.xtx.api.inventory.dto.StockReserveRequestDTO;
import com.xtx.api.inventory.dto.StockReserveResultDTO;
import com.xtx.api.inventory.dto.StockWarmupRequestDTO;
import com.xtx.api.inventory.dto.StockWarmupResultDTO;
import com.xtx.common.core.exception.BizException;
import com.xtx.common.core.result.ResultCode;
import com.xtx.inventory.entity.StockChangeLog;
import com.xtx.inventory.entity.StockReservation;
import com.xtx.inventory.entity.StockSku;
import com.xtx.inventory.mapper.StockChangeLogMapper;
import com.xtx.inventory.mapper.StockReservationMapper;
import com.xtx.inventory.mapper.StockSkuMapper;
import com.xtx.inventory.service.InventoryAppService;
import com.xtx.inventory.service.RedisStockCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 库存应用服务实现类
 * 实现库存查询、乐观锁预占、释放与确认扣减等核心库存逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryAppServiceImpl implements InventoryAppService {

    private final StockSkuMapper stockSkuMapper;
    private final StockReservationMapper stockReservationMapper;
    private final StockChangeLogMapper stockChangeLogMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisStockCommandService redisStockCommandService;

    /**
     * 库存缓存 Key 前缀
     */
    private static final String CACHE_KEY_STOCK = "stock:sku:";

    @Override
    public Map<String, Object> getStockInfo(Long skuId) {
        // 优先从缓存获取
        String cacheKey = CACHE_KEY_STOCK + skuId;
        Map<String, Object> cached = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        StockSku stockSku = stockSkuMapper.selectBySkuId(skuId);
        if (stockSku == null) {
            // 无库存记录时返回默认值
            Map<String, Object> defaultStock = new HashMap<>();
            defaultStock.put("skuId", skuId);
            defaultStock.put("totalStock", 0);
            defaultStock.put("availableStock", 0);
            defaultStock.put("lockedStock", 0);
            defaultStock.put("soldStock", 0);
            return defaultStock;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("skuId", stockSku.getSkuId());
        result.put("totalStock", stockSku.getTotalStock());
        result.put("availableStock", stockSku.getAvailableStock());
        result.put("lockedStock", stockSku.getLockedStock());
        result.put("soldStock", stockSku.getSoldStock());

        // 写入缓存，30 秒过期（库存变化频繁，缓存时间短）
        redisTemplate.opsForValue().set(cacheKey, result, 30, TimeUnit.SECONDS);

        return result;
    }

    @Override
    public StockWarmupResultDTO warmupStocks(StockWarmupRequestDTO request) {
        return redisStockCommandService.warmupStocks(request);
    }

    @Override
    public Long getRedisStock(Long skuId) {
        return redisStockCommandService.getRedisStock(skuId);
    }

    @Override
    public StockPreDeductResultDTO preDeductByRedis(StockPreDeductRequestDTO request) {
        return redisStockCommandService.preDeductByRedis(request);
    }

    @Override
    public void rollbackRedisStock(StockRollbackRequestDTO request) {
        redisStockCommandService.rollbackRedisStock(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StockReserveResultDTO reserveStocks(StockReserveRequestDTO request) {
        StockReserveResultDTO result = new StockReserveResultDTO();
        List<Map<String, Object>> failedItems = new ArrayList<>();
        List<Map<String, Object>> successItems = new ArrayList<>();

        for (StockReserveRequestDTO.StockReserveItemDTO item : request.getItems()) {
            // 幂等校验：检查是否已存在预占记录
            StockReservation existing = stockReservationMapper.selectByOrderNoAndSkuId(
                    request.getOrderNo(), item.getSkuId());
            if (existing != null) {
                // 已预占过，返回成功
                Map<String, Object> successItem = new HashMap<>();
                successItem.put("skuId", item.getSkuId());
                successItem.put("count", existing.getCount());
                successItems.add(successItem);
                continue;
            }

            // 查询库存
            StockSku stockSku = stockSkuMapper.selectBySkuId(item.getSkuId());
            if (stockSku == null) {
                failedItems.add(buildFailedItem(item, "库存记录不存在"));
                continue;
            }

            // 校验可用库存是否足够
            if (stockSku.getAvailableStock() < item.getCount()) {
                failedItems.add(buildFailedItem(item, "库存不足"));
                continue;
            }

            // 乐观锁更新：扣减可用库存，增加锁定库存
            int updated = stockSkuMapper.updateByQuery(
                    new StockSku() {{
                        setAvailableStock(stockSku.getAvailableStock() - item.getCount());
                        setLockedStock(stockSku.getLockedStock() + item.getCount());
                        setVersion(stockSku.getVersion() + 1);
                        setUpdateTime(LocalDateTime.now());
                    }},
                    com.mybatisflex.core.query.QueryWrapper.create()
                            .eq("id", stockSku.getId())
                            .eq("version", stockSku.getVersion())
            );

            if (updated == 0) {
                // 乐观锁冲突，重试或返回失败
                failedItems.add(buildFailedItem(item, "库存更新冲突，请重试"));
                continue;
            }

            // 插入预占记录
            StockReservation reservation = new StockReservation();
            reservation.setOrderId(request.getOrderId());
            reservation.setOrderNo(request.getOrderNo());
            reservation.setSkuId(item.getSkuId());
            reservation.setCount(item.getCount());
            reservation.setStatus(1); // 已预占
            reservation.setCreateTime(LocalDateTime.now());
            stockReservationMapper.insert(reservation);

            // 记录变更日志
            saveChangeLog(item.getSkuId(), "RESERVE", -item.getCount(),
                    stockSku.getAvailableStock(), stockSku.getAvailableStock() - item.getCount(),
                    request.getOrderNo(), "system");

            Map<String, Object> successItem = new HashMap<>();
            successItem.put("skuId", item.getSkuId());
            successItem.put("count", item.getCount());
            successItems.add(successItem);

            // 清除缓存
            redisTemplate.delete(CACHE_KEY_STOCK + item.getSkuId());
        }

        result.setSuccessItems(successItems);
        result.setFailedItems(failedItems);
        result.setAllSuccess(failedItems.isEmpty());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseStocks(String orderNo) {
        List<StockReservation> reservations = stockReservationMapper.selectByOrderNo(orderNo);
        if (reservations.isEmpty()) {
            log.warn("未找到订单 {} 的预占记录，可能已释放", orderNo);
            return;
        }

        for (StockReservation reservation : reservations) {
            // 只处理已预占状态的记录
            if (reservation.getStatus() != 1) {
                continue;
            }

            StockSku stockSku = stockSkuMapper.selectBySkuId(reservation.getSkuId());
            if (stockSku == null) {
                log.warn("SKU {} 库存记录不存在，跳过释放", reservation.getSkuId());
                continue;
            }

            int count = reservation.getCount();

            // 乐观锁更新：增加可用库存，减少锁定库存
            int updated = stockSkuMapper.updateByQuery(
                    new StockSku() {{
                        setAvailableStock(stockSku.getAvailableStock() + count);
                        setLockedStock(stockSku.getLockedStock() - count);
                        setVersion(stockSku.getVersion() + 1);
                        setUpdateTime(LocalDateTime.now());
                    }},
                    com.mybatisflex.core.query.QueryWrapper.create()
                            .eq("id", stockSku.getId())
                            .eq("version", stockSku.getVersion())
            );

            if (updated == 0) {
                throw new BizException(ResultCode.CONFLICT.getCode(), "释放库存失败，乐观锁冲突");
            }

            // 更新预占记录状态为已释放
            reservation.setStatus(3);
            stockReservationMapper.update(reservation);

            // 记录变更日志
            saveChangeLog(reservation.getSkuId(), "RELEASE", count,
                    stockSku.getAvailableStock(), stockSku.getAvailableStock() + count,
                    orderNo, "system");

            // 清除缓存
            redisTemplate.delete(CACHE_KEY_STOCK + reservation.getSkuId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmDeduction(String orderNo) {
        List<StockReservation> reservations = stockReservationMapper.selectByOrderNo(orderNo);
        if (reservations.isEmpty()) {
            log.warn("未找到订单 {} 的预占记录，可能已扣减", orderNo);
            return;
        }

        for (StockReservation reservation : reservations) {
            // 只处理已预占状态的记录
            if (reservation.getStatus() != 1) {
                continue;
            }

            StockSku stockSku = stockSkuMapper.selectBySkuId(reservation.getSkuId());
            if (stockSku == null) {
                log.warn("SKU {} 库存记录不存在，跳过扣减", reservation.getSkuId());
                continue;
            }

            int count = reservation.getCount();

            // 乐观锁更新：减少锁定库存，增加已售库存
            int updated = stockSkuMapper.updateByQuery(
                    new StockSku() {{
                        setLockedStock(stockSku.getLockedStock() - count);
                        setSoldStock(stockSku.getSoldStock() + count);
                        setTotalStock(stockSku.getTotalStock());
                        setVersion(stockSku.getVersion() + 1);
                        setUpdateTime(LocalDateTime.now());
                    }},
                    com.mybatisflex.core.query.QueryWrapper.create()
                            .eq("id", stockSku.getId())
                            .eq("version", stockSku.getVersion())
            );

            if (updated == 0) {
                throw new BizException(ResultCode.CONFLICT.getCode(), "扣减库存失败，乐观锁冲突");
            }

            // 更新预占记录状态为已扣减
            reservation.setStatus(2);
            stockReservationMapper.update(reservation);

            // 记录变更日志
            saveChangeLog(reservation.getSkuId(), "DEDUCT", -count,
                    stockSku.getLockedStock(), stockSku.getLockedStock() - count,
                    orderNo, "system");

            // 清除缓存
            redisTemplate.delete(CACHE_KEY_STOCK + reservation.getSkuId());
        }
    }

    /**
     * 构建失败条目
     *
     * @param item  预占条目
     * @param reason 失败原因
     * @return 失败条目 Map
     */
    private Map<String, Object> buildFailedItem(StockReserveRequestDTO.StockReserveItemDTO item, String reason) {
        Map<String, Object> failed = new HashMap<>();
        failed.put("skuId", item.getSkuId());
        failed.put("requestedCount", item.getCount());
        failed.put("reason", reason);
        return failed;
    }

    /**
     * 记录库存变更日志
     *
     * @param skuId       SKU ID
     * @param changeType  变更类型
     * @param changeAmount 变更数量
     * @param beforeStock 变更前库存
     * @param afterStock  变更后库存
     * @param bizKey      业务标识
     * @param operator    操作人
     */
    private void saveChangeLog(Long skuId, String changeType, Integer changeAmount,
                               Integer beforeStock, Integer afterStock,
                               String bizKey, String operator) {
        StockChangeLog logRecord = new StockChangeLog();
        logRecord.setSkuId(skuId);
        logRecord.setChangeType(changeType);
        logRecord.setChangeAmount(changeAmount);
        logRecord.setBeforeStock(beforeStock);
        logRecord.setAfterStock(afterStock);
        logRecord.setBizKey(bizKey);
        logRecord.setOperator(operator);
        logRecord.setCreateTime(LocalDateTime.now());
        stockChangeLogMapper.insert(logRecord);
    }
}
