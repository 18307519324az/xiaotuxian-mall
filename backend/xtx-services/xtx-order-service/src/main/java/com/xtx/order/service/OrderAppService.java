package com.xtx.order.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.paginate.Page;
import com.xtx.api.cart.CartClient;
import com.xtx.api.cart.dto.CartMergeItemDTO;
import com.xtx.api.goods.GoodsClient;
import com.xtx.api.goods.dto.SkuSnapshotDTO;
import com.xtx.api.inventory.StockClient;
import com.xtx.api.inventory.dto.StockReserveRequestDTO;
import com.xtx.api.inventory.dto.StockReserveResultDTO;
import com.xtx.api.member.MemberClient;
import com.xtx.api.member.dto.AddressSnapshotDTO;
import com.xtx.api.payment.PaymentClient;
import com.xtx.api.payment.dto.CreatePayOrderRequestDTO;
import com.xtx.api.payment.dto.PayOrderDTO;
import com.xtx.common.core.exception.BizException;
import com.xtx.common.core.model.PageResult;
import com.xtx.common.core.result.ApiResponse;
import com.xtx.common.core.result.ResultCode;
import com.xtx.order.dto.OrderPreviewItemDTO;
import com.xtx.order.dto.OrderStatusUpdateDTO;
import com.xtx.order.dto.SubmitOrderDTO;
import com.xtx.order.entity.OrderGoods;
import com.xtx.order.entity.OrderIdempotent;
import com.xtx.order.entity.OrderInfo;
import com.xtx.order.entity.OrderStatusLog;
import com.xtx.order.mapper.OrderGoodsMapper;
import com.xtx.order.mapper.OrderIdempotentMapper;
import com.xtx.order.mapper.OrderInfoMapper;
import com.xtx.order.mapper.OrderStatusLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 订单应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderAppService {

    /** 订单状态：待付款 */
    public static final int ORDER_STATE_PENDING_PAY = 1;
    /** 订单状态：待发货 */
    public static final int ORDER_STATE_PENDING_DELIVERY = 2;
    /** 订单状态：待收货 */
    public static final int ORDER_STATE_PENDING_RECEIPT = 3;
    /** 订单状态：待评价 */
    public static final int ORDER_STATE_PENDING_REVIEW = 4;
    /** 订单状态：已完成 */
    public static final int ORDER_STATE_COMPLETED = 5;
    /** 订单状态：已取消 */
    public static final int ORDER_STATE_CANCELED = 6;

    private final OrderInfoMapper orderInfoMapper;
    private final OrderGoodsMapper orderGoodsMapper;
    private final OrderStatusLogMapper orderStatusLogMapper;
    private final OrderIdempotentMapper orderIdempotentMapper;
    private final GoodsClient goodsClient;
    private final StockClient stockClient;
    private final CartClient cartClient;
    private final PaymentClient paymentClient;
    private final MemberClient memberClient;
    private final ObjectMapper objectMapper;

    /**
     * 获取订单预览信息
     */
    public Map<String, Object> getOrderPreview(Long userId) {
        Map<String, Object> result = new HashMap<>();

        // 获取购物车中已选中的商品
        List<CartMergeItemDTO> selectedItems = getSelectedCartItems(userId);
        if (CollUtil.isEmpty(selectedItems)) {
            result.put("skus", Collections.emptyList());
            result.put("summaries", buildEmptySummaries());
            return result;
        }

        // 获取SKU详细信息
        List<Long> skuIds = selectedItems.stream()
                .map(CartMergeItemDTO::getSkuId)
                .collect(Collectors.toList());
        ApiResponse<List<SkuSnapshotDTO>> skuResponse = goodsClient.listSkuSnapshots(skuIds);
        List<SkuSnapshotDTO> skuList = skuResponse != null ? skuResponse.getData() : null;
        Map<Long, SkuSnapshotDTO> skuMap = new HashMap<>();
        if (skuList != null) {
            skuMap = skuList.stream()
                    .collect(Collectors.toMap(SkuSnapshotDTO::getSkuId, Function.identity(), (k1, k2) -> k1));
        }

        // 组装预览商品列表
        List<OrderPreviewItemDTO> previewItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalCount = 0;

        for (CartMergeItemDTO item : selectedItems) {
            SkuSnapshotDTO sku = skuMap.get(item.getSkuId());
            if (sku == null) {
                continue;
            }

            OrderPreviewItemDTO previewItem = new OrderPreviewItemDTO();
            previewItem.setSkuId(item.getSkuId());
            previewItem.setName(sku.getGoodsName());
            previewItem.setPicture(sku.getPicture());
            previewItem.setAttrsText(sku.getAttrsText());
            previewItem.setPrice(sku.getNowPrice() != null ? sku.getNowPrice() : sku.getPrice());
            previewItem.setCount(item.getCount());
            previewItem.setSubtotal(previewItem.getPrice().multiply(BigDecimal.valueOf(previewItem.getCount())));
            previewItem.setIsEffective(sku.getStatus() != null && sku.getStatus() == 1);
            previewItem.setStock(sku.getStock() != null ? sku.getStock() : 0);
            previewItems.add(previewItem);

            totalAmount = totalAmount.add(previewItem.getSubtotal());
            totalCount += previewItem.getCount();
        }

        result.put("skus", previewItems);
        result.put("summaries", buildSummaries(totalAmount, totalCount));
        return result;
    }

    /**
     * 获取复购预览信息
     */
    public Map<String, Object> getRepurchasePreview(Long userId, Long orderId) {
        Map<String, Object> result = new HashMap<>();

        OrderInfo order = orderInfoMapper.selectOneById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        }

        List<OrderGoods> orderGoods = orderGoodsMapper.selectByOrderId(orderId);
        if (CollUtil.isEmpty(orderGoods)) {
            result.put("skus", Collections.emptyList());
            result.put("summaries", buildEmptySummaries());
            return result;
        }

        List<Long> skuIds = orderGoods.stream()
                .map(OrderGoods::getSkuId)
                .collect(Collectors.toList());
        ApiResponse<List<SkuSnapshotDTO>> skuResponse = goodsClient.listSkuSnapshots(skuIds);
        List<SkuSnapshotDTO> currentSkuList = skuResponse != null ? skuResponse.getData() : null;
        Map<Long, SkuSnapshotDTO> currentSkuMap = new HashMap<>();
        if (currentSkuList != null) {
            currentSkuMap = currentSkuList.stream()
                    .collect(Collectors.toMap(SkuSnapshotDTO::getSkuId, Function.identity(), (k1, k2) -> k1));
        }

        List<OrderPreviewItemDTO> previewItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalCount = 0;

        for (OrderGoods og : orderGoods) {
            SkuSnapshotDTO sku = currentSkuMap.get(og.getSkuId());
            if (sku == null) {
                continue;
            }

            OrderPreviewItemDTO previewItem = new OrderPreviewItemDTO();
            previewItem.setSkuId(og.getSkuId());
            previewItem.setName(sku.getGoodsName());
            previewItem.setPicture(sku.getPicture());
            previewItem.setAttrsText(sku.getAttrsText());
            previewItem.setPrice(sku.getNowPrice() != null ? sku.getNowPrice() : sku.getPrice());
            previewItem.setCount(og.getCount());
            previewItem.setSubtotal(previewItem.getPrice().multiply(BigDecimal.valueOf(previewItem.getCount())));
            previewItem.setIsEffective(sku.getStatus() != null && sku.getStatus() == 1);
            previewItem.setStock(sku.getStock() != null ? sku.getStock() : 0);
            previewItems.add(previewItem);

            totalAmount = totalAmount.add(previewItem.getSubtotal());
            totalCount += previewItem.getCount();
        }

        result.put("skus", previewItems);
        result.put("summaries", buildSummaries(totalAmount, totalCount));
        return result;
    }

    /**
     * 提交订单（核心方法）
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> submitOrder(Long userId, SubmitOrderDTO dto) {
        // Step 1: 构建商品列表摘要用于幂等性检查
        String goodsKey = buildGoodsKey(dto);
        String idempotentKey = userId + ":" + goodsKey;
        String requestHash = DigestUtil.md5Hex(goodsKey + System.currentTimeMillis());

        // 检查幂等性
        OrderIdempotent existingIdempotent = orderIdempotentMapper.selectByUserIdAndKey(userId, idempotentKey, "submitOrder");
        if (existingIdempotent != null && existingIdempotent.getStatus() == 1) {
            Map<String, Object> cachedResult = new HashMap<>();
            cachedResult.put("orderId", existingIdempotent.getBizId());
            cachedResult.put("orderNo", existingIdempotent.getResponseJson());
            cachedResult.put("idempotent", true);
            return cachedResult;
        }

        // 写入幂等记录
        OrderIdempotent idempotent = new OrderIdempotent();
        idempotent.setUserId(userId);
        idempotent.setIdempotentKey(idempotentKey);
        idempotent.setBizType("submitOrder");
        idempotent.setStatus(0);
        idempotent.setRequestHash(requestHash);
        idempotent.setExpireTime(LocalDateTime.now().plusMinutes(30));
        idempotent.setCreateTime(LocalDateTime.now());
        idempotent.setUpdateTime(LocalDateTime.now());
        orderIdempotentMapper.insert(idempotent);

        try {
            // Step 2: 验证收货地址
            ApiResponse<AddressSnapshotDTO> addressResponse = memberClient.getAddressSnapshot(userId, dto.getAddressId());
            AddressSnapshotDTO address = addressResponse != null ? addressResponse.getData() : null;
            if (address == null) {
                throw new BizException(ResultCode.BAD_REQUEST.getCode(), "收货地址不存在");
            }

            // Step 3: 验证SKU
            List<Long> skuIds = dto.getGoods().stream()
                    .map(SubmitOrderDTO.OrderItemDTO::getSkuId)
                    .collect(Collectors.toList());
            ApiResponse<List<SkuSnapshotDTO>> skuResponse = goodsClient.listSkuSnapshots(skuIds);
            List<SkuSnapshotDTO> skuList = skuResponse != null ? skuResponse.getData() : null;
            Map<Long, SkuSnapshotDTO> skuMap = new HashMap<>();
            if (skuList != null) {
                skuMap = skuList.stream()
                        .collect(Collectors.toMap(SkuSnapshotDTO::getSkuId, Function.identity(), (k1, k2) -> k1));
            }

            for (SubmitOrderDTO.OrderItemDTO item : dto.getGoods()) {
                SkuSnapshotDTO sku = skuMap.get(item.getSkuId());
                if (sku == null) {
                    throw new BizException(ResultCode.BAD_REQUEST.getCode(), "SKU不存在: " + item.getSkuId());
                }
                if (sku.getStatus() == null || sku.getStatus() != 1) {
                    throw new BizException(ResultCode.BAD_REQUEST.getCode(), "商品已下架: " + sku.getGoodsName());
                }
            }

            // Step 4: 服务端计算金额
            BigDecimal totalMoney = BigDecimal.ZERO;
            int totalNum = 0;
            for (SubmitOrderDTO.OrderItemDTO item : dto.getGoods()) {
                SkuSnapshotDTO sku = skuMap.get(item.getSkuId());
                BigDecimal price = sku.getNowPrice() != null ? sku.getNowPrice() : sku.getPrice();
                totalMoney = totalMoney.add(price.multiply(BigDecimal.valueOf(item.getCount())));
                totalNum += item.getCount();
            }
            BigDecimal postFee = totalMoney.compareTo(BigDecimal.valueOf(99)) >= 0
                    ? BigDecimal.ZERO : BigDecimal.valueOf(10);
            BigDecimal payMoney = totalMoney.add(postFee);

            // Step 5: 生成订单编号
            String orderNo = generateOrderNo();

            // Step 6: 保存订单信息
            OrderInfo order = new OrderInfo();
            order.setOrderNo(orderNo);
            order.setUserId(userId);
            order.setPayMoney(payMoney);
            order.setTotalMoney(totalMoney);
            order.setPostFee(postFee);
            order.setTotalNum(totalNum);
            order.setOrderState(ORDER_STATE_PENDING_PAY);
            order.setDeliveryTimeType(dto.getDeliveryTimeType() != null ? dto.getDeliveryTimeType() : 1);
            order.setPayType(1);
            order.setPayChannel(dto.getPayChannel() != null ? dto.getPayChannel() : 1);
            order.setBuyerMessage(dto.getBuyerMessage());
            order.setReceiverName(address.getReceiverName());
            order.setReceiverPhone(address.getReceiverPhone());
            try {
                order.setReceiverAddress(objectMapper.writeValueAsString(address));
            } catch (JsonProcessingException e) {
                throw new BizException(ResultCode.INTERNAL_ERROR.getCode(), "地址序列化失败");
            }
            order.setIsDeleted(0);
            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());
            orderInfoMapper.insert(order);
            Long orderId = order.getId();

            // Step 7: 保存订单商品快照
            for (SubmitOrderDTO.OrderItemDTO item : dto.getGoods()) {
                SkuSnapshotDTO sku = skuMap.get(item.getSkuId());
                BigDecimal price = sku.getNowPrice() != null ? sku.getNowPrice() : sku.getPrice();
                BigDecimal totalPrice = price.multiply(BigDecimal.valueOf(item.getCount()));

                OrderGoods og = new OrderGoods();
                og.setOrderId(orderId);
                og.setSkuId(item.getSkuId());
                og.setGoodsId(0L);
                og.setGoodsName(sku.getGoodsName());
                og.setGoodsImage(sku.getPicture());
                og.setAttrsText(sku.getAttrsText());
                og.setPrice(price);
                og.setCount(item.getCount());
                og.setTotalPrice(totalPrice);
                og.setTotalPayPrice(totalPrice);
                og.setCreateTime(LocalDateTime.now());
                orderGoodsMapper.insert(og);
            }

            // Step 8: 预留库存
            try {
                StockReserveRequestDTO reserveRequest = new StockReserveRequestDTO();
                reserveRequest.setOrderId(orderId);
                reserveRequest.setOrderNo(orderNo);
                List<StockReserveRequestDTO.StockReserveItemDTO> reserveItems = dto.getGoods().stream()
                        .map(item -> {
                            StockReserveRequestDTO.StockReserveItemDTO ri = new StockReserveRequestDTO.StockReserveItemDTO();
                            ri.setSkuId(item.getSkuId());
                            ri.setCount(item.getCount());
                            return ri;
                        })
                        .collect(Collectors.toList());
                reserveRequest.setItems(reserveItems);
                ApiResponse<StockReserveResultDTO> stockResponse = stockClient.reserveStocks(reserveRequest);
                StockReserveResultDTO reserveResult = stockResponse != null ? stockResponse.getData() : null;
                if (reserveResult == null || !reserveResult.isAllSuccess()) {
                    throw new BizException(ResultCode.INTERNAL_ERROR.getCode(), "库存预留失败");
                }
            } catch (BizException e) {
                throw e;
            } catch (Exception e) {
                log.error("库存预留失败, orderNo={}", orderNo, e);
                throw new BizException(ResultCode.INTERNAL_ERROR.getCode(), "库存预留失败: " + e.getMessage());
            }

            // Step 9: 清理购物车
            try {
                ApiResponse<Void> cartResponse = cartClient.cleanCartBySkuIds(skuIds, userId);
                if (cartResponse == null) {
                    log.warn("清理购物车失败, userId={}, skuIds={}", userId, skuIds);
                }
            } catch (Exception e) {
                log.warn("清理购物车异常, userId={}, skuIds={}", userId, skuIds, e);
            }

            // Step 10: 创建支付订单
            try {
                CreatePayOrderRequestDTO payRequest = new CreatePayOrderRequestDTO();
                payRequest.setOrderId(orderId);
                payRequest.setOrderNo(orderNo);
                payRequest.setUserId(userId);
                payRequest.setPayMoney(payMoney);
                payRequest.setPayChannel(dto.getPayChannel() != null ? dto.getPayChannel() : 1);
                ApiResponse<PayOrderDTO> payResponse = paymentClient.createPayOrder(payRequest);
                PayOrderDTO payData = payResponse != null ? payResponse.getData() : null;
                if (payData == null) {
                    throw new BizException(ResultCode.INTERNAL_ERROR.getCode(), "创建支付订单失败");
                }
            } catch (BizException e) {
                throw e;
            } catch (Exception e) {
                log.error("创建支付订单失败, orderNo={}", orderNo, e);
                throw new BizException(ResultCode.INTERNAL_ERROR.getCode(), "创建支付订单失败: " + e.getMessage());
            }

            // Step 11: 保存状态日志
            saveStatusLog(orderId, null, ORDER_STATE_PENDING_PAY, "system", "下单成功");

            // Step 12: 更新幂等记录
            idempotent.setStatus(1);
            idempotent.setBizId(String.valueOf(orderId));
            idempotent.setResponseJson(orderNo);
            idempotent.setUpdateTime(LocalDateTime.now());
            orderIdempotentMapper.update(idempotent);

            Map<String, Object> result = new HashMap<>();
            result.put("orderId", orderId);
            result.put("orderNo", orderNo);
            result.put("payMoney", payMoney);
            return result;
        } catch (Exception e) {
            idempotent.setStatus(0);
            idempotent.setUpdateTime(LocalDateTime.now());
            orderIdempotentMapper.update(idempotent);
            throw e;
        }
    }

    /**
     * 获取订单列表（分页）
     */
    public PageResult<Map<String, Object>> getOrderList(Long userId, Integer page, Integer pageSize, Integer orderState) {
        Page<OrderInfo> pageObj = new Page<>(page, pageSize);
        Page<OrderInfo> orderPage = orderInfoMapper.selectByUserIdPage(userId, pageObj, orderState);

        List<Map<String, Object>> records = new ArrayList<>();
        for (OrderInfo order : orderPage.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", order.getId());
            item.put("orderNo", order.getOrderNo());
            item.put("totalMoney", order.getTotalMoney());
            item.put("payMoney", order.getPayMoney());
            item.put("postFee", order.getPostFee());
            item.put("totalNum", order.getTotalNum());
            item.put("orderState", order.getOrderState());
            item.put("createTime", order.getCreateTime());

            List<OrderGoods> goodsList = orderGoodsMapper.selectByOrderId(order.getId());
            List<Map<String, Object>> goodsPreview = goodsList.stream().map(g -> {
                Map<String, Object> gmap = new HashMap<>();
                gmap.put("skuId", g.getSkuId());
                gmap.put("name", g.getGoodsName());
                gmap.put("picture", g.getGoodsImage());
                gmap.put("price", g.getPrice());
                gmap.put("count", g.getCount());
                gmap.put("attrsText", g.getAttrsText());
                return gmap;
            }).collect(Collectors.toList());
            item.put("goods", goodsPreview);

            records.add(item);
        }

        PageResult<Map<String, Object>> pageResult = new PageResult<>();
        pageResult.setItems(records);
        pageResult.setTotal((int) orderPage.getTotalRow());
        pageResult.setPage(page);
        pageResult.setPageSize(pageSize);
        return pageResult;
    }

    /**
     * 获取订单详情
     */
    public Map<String, Object> getOrderDetail(Long userId, Long orderId) {
        OrderInfo order = orderInfoMapper.selectOneById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", order.getId());
        result.put("orderNo", order.getOrderNo());
        result.put("totalMoney", order.getTotalMoney());
        result.put("payMoney", order.getPayMoney());
        result.put("postFee", order.getPostFee());
        result.put("totalNum", order.getTotalNum());
        result.put("orderState", order.getOrderState());
        result.put("deliveryTimeType", order.getDeliveryTimeType());
        result.put("payType", order.getPayType());
        result.put("payChannel", order.getPayChannel());
        result.put("buyerMessage", order.getBuyerMessage());
        result.put("receiverName", order.getReceiverName());
        result.put("receiverPhone", order.getReceiverPhone());
        result.put("createTime", order.getCreateTime());
        result.put("payTime", order.getPayTime());
        result.put("deliveryTime", order.getDeliveryTime());
        result.put("consignTime", order.getConsignTime());
        result.put("endTime", order.getEndTime());
        result.put("cancelReason", order.getCancelReason());

        List<OrderGoods> goodsList = orderGoodsMapper.selectByOrderId(orderId);
        List<Map<String, Object>> goodsMaps = goodsList.stream().map(g -> {
            Map<String, Object> gmap = new HashMap<>();
            gmap.put("id", g.getId());
            gmap.put("skuId", g.getSkuId());
            gmap.put("goodsId", g.getGoodsId());
            gmap.put("name", g.getGoodsName());
            gmap.put("picture", g.getGoodsImage());
            gmap.put("attrsText", g.getAttrsText());
            gmap.put("price", g.getPrice());
            gmap.put("count", g.getCount());
            gmap.put("totalPrice", g.getTotalPrice());
            return gmap;
        }).collect(Collectors.toList());
        result.put("goods", goodsMaps);

        List<OrderStatusLog> logs = orderStatusLogMapper.selectByOrderIdOrderByTime(orderId);
        result.put("statusLogs", logs);

        return result;
    }

    /**
     * 取消订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long userId, Long orderId, String reason) {
        OrderInfo order = orderInfoMapper.selectOneById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        }
        if (order.getOrderState() != ORDER_STATE_PENDING_PAY) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "仅待付款订单可以取消");
        }

        int fromState = order.getOrderState();
        order.setOrderState(ORDER_STATE_CANCELED);
        order.setCancelReason(reason);
        order.setUpdateTime(LocalDateTime.now());
        orderInfoMapper.update(order);

        List<OrderGoods> goodsList = orderGoodsMapper.selectByOrderId(orderId);
        try {
            stockClient.releaseStocks(order.getOrderNo());
        } catch (Exception e) {
            log.error("释放库存失败, orderNo={}", order.getOrderNo(), e);
        }

        saveStatusLog(orderId, fromState, ORDER_STATE_CANCELED, userId.toString(), reason);
    }

    /**
     * 确认收货
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceipt(Long userId, Long orderId) {
        OrderInfo order = orderInfoMapper.selectOneById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        }
        if (order.getOrderState() != ORDER_STATE_PENDING_RECEIPT) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "仅待收货订单可以确认收货");
        }

        int fromState = order.getOrderState();
        order.setOrderState(ORDER_STATE_PENDING_REVIEW);
        order.setConsignTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderInfoMapper.update(order);

        List<OrderGoods> goodsList = orderGoodsMapper.selectByOrderId(orderId);
        try {
            stockClient.confirmDeduction(order.getOrderNo());
        } catch (Exception e) {
            log.error("确认库存扣减失败, orderNo={}", order.getOrderNo(), e);
        }

        saveStatusLog(orderId, fromState, ORDER_STATE_PENDING_REVIEW, userId.toString(), "确认收货");
    }

    /**
     * 删除订单（软删除）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrder(Long userId, Long orderId) {
        OrderInfo order = orderInfoMapper.selectOneById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        }
        if (order.getOrderState() != ORDER_STATE_COMPLETED
                && order.getOrderState() != ORDER_STATE_CANCELED) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "仅已完成或已取消的订单可以删除");
        }

        order.setIsDeleted(1);
        order.setUpdateTime(LocalDateTime.now());
        orderInfoMapper.update(order);
    }

    /**
     * 内部方法：更新订单状态（由支付回调等服务调用）
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatus(OrderStatusUpdateDTO dto) {
        OrderInfo order = orderInfoMapper.selectOneById(dto.getOrderId());
        if (order == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        }

        int fromState = order.getOrderState();
        order.setOrderState(dto.getTargetState());
        order.setUpdateTime(LocalDateTime.now());

        switch (dto.getTargetState()) {
            case ORDER_STATE_PENDING_DELIVERY:
                order.setPayTime(LocalDateTime.now());
                break;
            case ORDER_STATE_PENDING_RECEIPT:
                order.setDeliveryTime(LocalDateTime.now());
                break;
            case ORDER_STATE_COMPLETED:
                order.setEndTime(LocalDateTime.now());
                break;
            default:
                break;
        }

        orderInfoMapper.update(order);
        saveStatusLog(dto.getOrderId(), fromState, dto.getTargetState(),
                dto.getOperator(), dto.getRemark());
    }

    /**
     * 内部方法：获取订单快照
     */
    public Map<String, Object> getOrderSnapshot(Long orderId) {
        OrderInfo order = orderInfoMapper.selectOneById(orderId);
        if (order == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        }
        List<OrderGoods> goodsList = orderGoodsMapper.selectByOrderId(orderId);

        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("goods", goodsList);
        return result;
    }

    // ========== 私有辅助方法 ==========

    private List<CartMergeItemDTO> getSelectedCartItems(Long userId) {
        try {
            ApiResponse<List<CartMergeItemDTO>> response = cartClient.getSelectedItems(userId);
            if (response != null && response.getData() != null) {
                return response.getData();
            }
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("获取购物车选中商品失败", e);
            return Collections.emptyList();
        }
    }

    private String buildGoodsKey(SubmitOrderDTO dto) {
        StringBuilder sb = new StringBuilder();
        for (SubmitOrderDTO.OrderItemDTO item : dto.getGoods()) {
            sb.append(item.getSkuId()).append(":").append(item.getCount()).append(",");
        }
        return DigestUtil.md5Hex(sb.toString());
    }

    private String generateOrderNo() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = RandomUtil.randomNumbers(8);
        return "ORDER" + datePart + randomPart;
    }

    private void saveStatusLog(Long orderId, Integer fromState, Integer toState,
                               String operator, String remark) {
        OrderStatusLog log = new OrderStatusLog();
        log.setOrderId(orderId);
        log.setFromState(fromState);
        log.setToState(toState);
        log.setOperator(operator);
        log.setRemark(remark);
        log.setCreateTime(LocalDateTime.now());
        orderStatusLogMapper.insert(log);
    }

    private Map<String, Object> buildSummaries(BigDecimal totalAmount, int totalCount) {
        Map<String, Object> summaries = new HashMap<>();
        BigDecimal postFee = totalAmount.compareTo(BigDecimal.valueOf(99)) >= 0
                ? BigDecimal.ZERO : BigDecimal.valueOf(10);
        summaries.put("totalMoney", totalAmount);
        summaries.put("postFee", postFee);
        summaries.put("payMoney", totalAmount.add(postFee));
        summaries.put("totalNum", totalCount);
        return summaries;
    }

    private Map<String, Object> buildEmptySummaries() {
        Map<String, Object> summaries = new HashMap<>();
        summaries.put("totalMoney", BigDecimal.ZERO);
        summaries.put("postFee", BigDecimal.ZERO);
        summaries.put("payMoney", BigDecimal.ZERO);
        summaries.put("totalNum", 0);
        return summaries;
    }
}
