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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderAppService {

    public static final int ORDER_STATE_PENDING_PAY = 1;
    public static final int ORDER_STATE_PENDING_DELIVERY = 2;
    public static final int ORDER_STATE_PENDING_RECEIPT = 3;
    public static final int ORDER_STATE_PENDING_REVIEW = 4;
    public static final int ORDER_STATE_COMPLETED = 5;
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
    private final OrderBenefitService orderBenefitService;
    private final OrderStockPreDeductService orderStockPreDeductService;
    private final OrderTokenService orderTokenService;
    private final OrderRequestTrackingService orderRequestTrackingService;
    private final StockReleaseOrchestrator stockReleaseOrchestrator;
    @Value("${services.payment:http://localhost:8109}")
    private String paymentServiceBaseUrl;

    public Map<String, Object> getOrderPreview(Long userId) {
        Map<String, Object> result = new HashMap<>();
        List<CartMergeItemDTO> selectedItems = getSelectedCartItems(userId);
        if (CollUtil.isEmpty(selectedItems)) {
            result.put("skus", Collections.emptyList());
            result.put("summaries", buildEmptySummaries());
            return result;
        }

        List<Long> skuIds = selectedItems.stream()
                .map(CartMergeItemDTO::getSkuId)
                .collect(Collectors.toList());
        ApiResponse<List<SkuSnapshotDTO>> skuResponse = goodsClient.listSkuSnapshots(skuIds);
        List<SkuSnapshotDTO> skuList = skuResponse != null ? skuResponse.getData() : null;
        Map<Long, SkuSnapshotDTO> skuMap = new HashMap<>();
        if (skuList != null) {
            skuMap = skuList.stream()
                    .collect(Collectors.toMap(SkuSnapshotDTO::getSkuId, Function.identity(), (left, right) -> left));
        }

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

        List<Long> skuIds = orderGoods.stream().map(OrderGoods::getSkuId).collect(Collectors.toList());
        ApiResponse<List<SkuSnapshotDTO>> skuResponse = goodsClient.listSkuSnapshots(skuIds);
        List<SkuSnapshotDTO> currentSkuList = skuResponse != null ? skuResponse.getData() : null;
        Map<Long, SkuSnapshotDTO> currentSkuMap = new HashMap<>();
        if (currentSkuList != null) {
            currentSkuMap = currentSkuList.stream()
                    .collect(Collectors.toMap(SkuSnapshotDTO::getSkuId, Function.identity(), (left, right) -> left));
        }

        List<OrderPreviewItemDTO> previewItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalCount = 0;
        for (OrderGoods orderGood : orderGoods) {
            SkuSnapshotDTO sku = currentSkuMap.get(orderGood.getSkuId());
            if (sku == null) {
                continue;
            }
            OrderPreviewItemDTO previewItem = new OrderPreviewItemDTO();
            previewItem.setSkuId(orderGood.getSkuId());
            previewItem.setName(sku.getGoodsName());
            previewItem.setPicture(sku.getPicture());
            previewItem.setAttrsText(sku.getAttrsText());
            previewItem.setPrice(sku.getNowPrice() != null ? sku.getNowPrice() : sku.getPrice());
            previewItem.setCount(orderGood.getCount());
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

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> submitOrder(Long userId, String authHeader, SubmitOrderDTO dto) {
        String goodsKey = buildGoodsKey(dto);
        String idempotentKey = userId + ":" + goodsKey;
        String requestHash = DigestUtil.md5Hex(goodsKey + System.currentTimeMillis());
        LocalDateTime now = LocalDateTime.now();

        OrderIdempotent existingIdempotent =
                orderIdempotentMapper.selectLatestByUserIdAndKey(userId, idempotentKey, "submitOrder");
        if (isActiveSubmittingRecord(existingIdempotent, now)) {
            throw new BizException(
                    ResultCode.ORDER_DUPLICATE_SUBMIT.getCode(),
                    ResultCode.ORDER_DUPLICATE_SUBMIT.getMessage()
            );
        }

        OrderIdempotent idempotent = new OrderIdempotent();
        idempotent.setUserId(userId);
        idempotent.setIdempotentKey(idempotentKey);
        idempotent.setBizType("submitOrder");
        idempotent.setStatus(0);
        idempotent.setRequestHash(requestHash);
        idempotent.setExpireTime(now.plusMinutes(30));
        idempotent.setCreateTime(now);
        idempotent.setUpdateTime(now);
        orderIdempotentMapper.insert(idempotent);

        String requestId = null;
        String orderNo = null;
        try {
            ApiResponse<AddressSnapshotDTO> addressResponse = memberClient.getAddressSnapshot(userId, dto.getAddressId());
            AddressSnapshotDTO address = addressResponse != null ? addressResponse.getData() : null;
            if (address == null) {
                throw new BizException(ResultCode.BAD_REQUEST.getCode(), "收货地址不存在");
            }

            List<Long> skuIds = dto.getGoods().stream()
                    .map(SubmitOrderDTO.OrderItemDTO::getSkuId)
                    .collect(Collectors.toList());
            ApiResponse<List<SkuSnapshotDTO>> skuResponse = goodsClient.listSkuSnapshots(skuIds);
            List<SkuSnapshotDTO> skuList = skuResponse != null ? skuResponse.getData() : null;
            Map<Long, SkuSnapshotDTO> skuMap = new HashMap<>();
            if (skuList != null) {
                skuMap = skuList.stream()
                        .collect(Collectors.toMap(SkuSnapshotDTO::getSkuId, Function.identity(), (left, right) -> left));
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

            orderTokenService.validateAndConsumeToken(userId, dto.getToken());

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
            OrderBenefitService.BenefitSnapshot benefitSnapshot = orderBenefitService.resolveForSubmit(
                    authHeader, dto.getCouponId(), dto.getGiftCardCode(), totalMoney, postFee);
            BigDecimal payMoney = benefitSnapshot.getPayMoney();

            orderNo = generateOrderNo();
            requestId = orderStockPreDeductService.preDeduct(orderNo, userId, dto.getGoods());
            orderRequestTrackingService.saveRequestId(orderNo, requestId);
            log.debug("Redis 库存预扣成功, orderNo={}, requestId={}", orderNo, requestId);

            OrderInfo order = new OrderInfo();
            order.setOrderNo(orderNo);
            order.setUserId(userId);
            order.setPayMoney(payMoney);
            order.setTotalMoney(totalMoney);
            order.setPostFee(postFee);
            order.setTotalNum(totalNum);
            order.setOrderState(isZeroPayOrder(payMoney) ? ORDER_STATE_PENDING_DELIVERY : ORDER_STATE_PENDING_PAY);
            order.setDeliveryTimeType(dto.getDeliveryTimeType() != null ? dto.getDeliveryTimeType() : 1);
            order.setPayType(1);
            order.setPayChannel(dto.getPayChannel() != null ? dto.getPayChannel() : 1);
            order.setBuyerMessage(dto.getBuyerMessage());
            order.setCouponId(benefitSnapshot.getCouponId());
            order.setCouponName(benefitSnapshot.getCouponName());
            order.setCouponType(benefitSnapshot.getCouponType());
            order.setDiscountGoodsAmount(benefitSnapshot.getDiscountGoodsAmount());
            order.setDiscountFreightAmount(benefitSnapshot.getDiscountFreightAmount());
            order.setDiscountAmount(benefitSnapshot.getDiscountAmount());
            order.setGiftCardAmount(benefitSnapshot.getGiftCardAmount());
            order.setGiftCardCode(benefitSnapshot.getGiftCardCode());
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

            for (SubmitOrderDTO.OrderItemDTO item : dto.getGoods()) {
                SkuSnapshotDTO sku = skuMap.get(item.getSkuId());
                BigDecimal price = sku.getNowPrice() != null ? sku.getNowPrice() : sku.getPrice();
                BigDecimal totalPrice = price.multiply(BigDecimal.valueOf(item.getCount()));

                OrderGoods orderGoods = new OrderGoods();
                orderGoods.setOrderId(orderId);
                orderGoods.setSkuId(item.getSkuId());
                orderGoods.setGoodsId(0L);
                orderGoods.setGoodsName(sku.getGoodsName());
                orderGoods.setGoodsImage(sku.getPicture());
                orderGoods.setAttrsText(sku.getAttrsText());
                orderGoods.setPrice(price);
                orderGoods.setCount(item.getCount());
                orderGoods.setTotalPrice(totalPrice);
                orderGoods.setTotalPayPrice(totalPrice);
                orderGoods.setCreateTime(LocalDateTime.now());
                orderGoodsMapper.insert(orderGoods);
            }

            StockReserveRequestDTO reserveRequest = new StockReserveRequestDTO();
            reserveRequest.setOrderId(orderId);
            reserveRequest.setOrderNo(orderNo);
            reserveRequest.setItems(dto.getGoods().stream()
                    .map(item -> {
                        StockReserveRequestDTO.StockReserveItemDTO reserveItem = new StockReserveRequestDTO.StockReserveItemDTO();
                        reserveItem.setSkuId(item.getSkuId());
                        reserveItem.setCount(item.getCount());
                        return reserveItem;
                    })
                    .collect(Collectors.toList()));
            ApiResponse<StockReserveResultDTO> stockResponse = stockClient.reserveStocks(reserveRequest);
            StockReserveResultDTO reserveResult = stockResponse != null ? stockResponse.getData() : null;
            if (reserveResult == null || !reserveResult.isAllSuccess()) {
                throw new BizException(ResultCode.INTERNAL_ERROR.getCode(), "库存预留失败");
            }

            try {
                cartClient.cleanCartBySkuIds(skuIds, userId);
            } catch (Exception e) {
                log.warn("清理购物车异常, userId={}, skuIds={}", userId, skuIds, e);
            }

            if (isZeroPayOrder(payMoney)) {
                stockClient.confirmDeduction(orderNo);
                order.setPayTime(LocalDateTime.now());
                order.setUpdateTime(LocalDateTime.now());
                orderInfoMapper.update(order);
                orderRequestTrackingService.removeRequestId(orderNo);
                saveStatusLog(orderId, null, order.getOrderState(), "system", "涓嬪崟鎴愬姛");

                idempotent.setStatus(1);
                idempotent.setBizId(String.valueOf(orderId));
                idempotent.setResponseJson(orderNo);
                idempotent.setUpdateTime(LocalDateTime.now());
                orderIdempotentMapper.update(idempotent);

                Map<String, Object> result = new HashMap<>();
                result.put("id", orderId);
                result.put("orderId", orderId);
                result.put("orderNo", orderNo);
                result.put("payMoney", payMoney);
                return result;
            }

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

            saveStatusLog(orderId, null, ORDER_STATE_PENDING_PAY, "system", "下单成功");

            idempotent.setStatus(1);
            idempotent.setBizId(String.valueOf(orderId));
            idempotent.setResponseJson(orderNo);
            idempotent.setUpdateTime(LocalDateTime.now());
            orderIdempotentMapper.update(idempotent);

            Map<String, Object> result = new HashMap<>();
            result.put("id", orderId);
            result.put("orderId", orderId);
            result.put("orderNo", orderNo);
            result.put("payMoney", payMoney);
            return result;
        } catch (Exception e) {
            if (requestId != null) {
                try {
                    orderStockPreDeductService.rollback(requestId, orderNo, dto.getGoods());
                    if (orderNo != null) {
                        orderRequestTrackingService.removeRequestId(orderNo);
                    }
                } catch (Exception rollbackEx) {
                    log.error("Redis 库存回滚失败, requestId={}, orderNo={}", requestId, orderNo, rollbackEx);
                }
            }
            idempotent.setStatus(0);
            idempotent.setUpdateTime(LocalDateTime.now());
            orderIdempotentMapper.update(idempotent);
            throw e;
        }
    }

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
            item.put("couponId", order.getCouponId());
            item.put("couponName", order.getCouponName());
            item.put("couponType", order.getCouponType());
            item.put("discountGoodsAmount", order.getDiscountGoodsAmount());
            item.put("discountFreightAmount", order.getDiscountFreightAmount());
            item.put("discountAmount", order.getDiscountAmount());
            item.put("giftCardAmount", order.getGiftCardAmount());
            item.put("giftCardCode", order.getGiftCardCode());
            item.put("goods", orderGoodsMapper.selectByOrderId(order.getId()).stream().map(goods -> {
                Map<String, Object> goodsMap = new HashMap<>();
                goodsMap.put("skuId", goods.getSkuId());
                goodsMap.put("name", goods.getGoodsName());
                goodsMap.put("picture", goods.getGoodsImage());
                goodsMap.put("price", goods.getPrice());
                goodsMap.put("count", goods.getCount());
                goodsMap.put("attrsText", goods.getAttrsText());
                return goodsMap;
            }).collect(Collectors.toList()));
            records.add(item);
        }

        PageResult<Map<String, Object>> pageResult = new PageResult<>();
        pageResult.setItems(records);
        pageResult.setTotal((int) orderPage.getTotalRow());
        pageResult.setPage(page);
        pageResult.setPageSize(pageSize);
        return pageResult;
    }

    public Map<String, Object> getOrderDetail(Long userId, Long orderId) {
        OrderInfo order = orderInfoMapper.selectOneById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        }
        return buildOrderDetail(order);
    }

    public Map<String, Object> getOrderDetailByOrderNo(Long userId, String orderNo) {
        OrderInfo order = orderInfoMapper.selectByOrderNo(orderNo);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        }
        return buildOrderDetail(order);
    }

    private Map<String, Object> buildOrderDetail(OrderInfo order) {
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
        result.put("couponId", order.getCouponId());
        result.put("couponName", order.getCouponName());
        result.put("couponType", order.getCouponType());
        result.put("discountGoodsAmount", order.getDiscountGoodsAmount());
        result.put("discountFreightAmount", order.getDiscountFreightAmount());
        result.put("discountAmount", order.getDiscountAmount());
        result.put("giftCardAmount", order.getGiftCardAmount());
        result.put("giftCardCode", order.getGiftCardCode());
        result.put("receiverName", order.getReceiverName());
        result.put("receiverPhone", order.getReceiverPhone());
        result.put("createTime", order.getCreateTime());
        result.put("payTime", order.getPayTime());
        result.put("deliveryTime", order.getDeliveryTime());
        result.put("consignTime", order.getConsignTime());
        result.put("endTime", order.getEndTime());
        result.put("cancelReason", order.getCancelReason());
        result.put("goods", orderGoodsMapper.selectByOrderId(order.getId()).stream().map(goods -> {
            Map<String, Object> goodsMap = new HashMap<>();
            goodsMap.put("id", goods.getId());
            goodsMap.put("skuId", goods.getSkuId());
            goodsMap.put("goodsId", goods.getGoodsId());
            goodsMap.put("name", goods.getGoodsName());
            goodsMap.put("picture", goods.getGoodsImage());
            goodsMap.put("attrsText", goods.getAttrsText());
            goodsMap.put("price", goods.getPrice());
            goodsMap.put("count", goods.getCount());
            goodsMap.put("totalPrice", goods.getTotalPrice());
            return goodsMap;
        }).collect(Collectors.toList()));
        result.put("statusLogs", orderStatusLogMapper.selectByOrderIdOrderByTime(order.getId()));
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long userId, Long orderId, String reason) {
        OrderInfo order = orderInfoMapper.selectOneById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        }
        cancelOrderInternal(order, reason, String.valueOf(userId));
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelOrderByOrderNo(Long userId, String orderNo, String reason) {
        OrderInfo order = orderInfoMapper.selectByOrderNo(orderNo);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        }
        cancelOrderInternal(order, reason, String.valueOf(userId));
    }

    @Transactional(rollbackFor = Exception.class)
    public void payOrderByOrderNo(Long userId, String orderNo) {
        OrderInfo order = orderInfoMapper.selectByOrderNo(orderNo);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        }
        if (order.getOrderState() != ORDER_STATE_PENDING_PAY) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "仅待付款订单可以支付");
        }
        try {
            invokeMockPayByOrderNo(orderNo, userId);
        } catch (Exception e) {
            log.error("模拟支付失败, orderNo={}", orderNo, e);
            throw new BizException(ResultCode.INTERNAL_ERROR.getCode(), "支付失败");
        }
    }

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

        saveStatusLog(orderId, fromState, ORDER_STATE_PENDING_REVIEW, userId.toString(), "确认收货");
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteOrder(Long userId, Long orderId) {
        OrderInfo order = orderInfoMapper.selectOneById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        }
        if (order.getOrderState() != ORDER_STATE_COMPLETED && order.getOrderState() != ORDER_STATE_CANCELED) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "仅已完成或已取消的订单可以删除");
        }
        order.setIsDeleted(1);
        order.setUpdateTime(LocalDateTime.now());
        orderInfoMapper.update(order);
    }

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
                stockClient.confirmDeduction(order.getOrderNo());
                order.setPayTime(LocalDateTime.now());
                orderRequestTrackingService.removeRequestId(order.getOrderNo());
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
        saveStatusLog(dto.getOrderId(), fromState, dto.getTargetState(), dto.getOperator(), dto.getRemark());
    }

    public Map<String, Object> getOrderSnapshot(Long orderId) {
        OrderInfo order = orderInfoMapper.selectOneById(orderId);
        if (order == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("goods", orderGoodsMapper.selectByOrderId(orderId));
        return result;
    }

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
        StringBuilder builder = new StringBuilder();
        for (SubmitOrderDTO.OrderItemDTO item : dto.getGoods()) {
            builder.append(item.getSkuId()).append(":").append(item.getCount()).append(",");
        }
        return DigestUtil.md5Hex(builder.toString());
    }

    private String generateOrderNo() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = RandomUtil.randomNumbers(8);
        return "ORDER" + datePart + randomPart;
    }

    private void invokeMockPayByOrderNo(String orderNo, Long userId) throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of("orderNo", orderNo));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(paymentServiceBaseUrl + "/pay/mock"))
                .header("Content-Type", "application/json")
                .header("X-User-Id", String.valueOf(userId))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new BizException(ResultCode.INTERNAL_ERROR.getCode(), "鏀粯鏈嶅姟鍝嶅簲寮傚父");
        }
        Map<?, ?> responseMap = objectMapper.readValue(response.body(), Map.class);
        Object code = responseMap.get("code");
        if (!String.valueOf(ResultCode.SUCCESS.getCode()).equals(String.valueOf(code))) {
            throw new BizException(ResultCode.INTERNAL_ERROR.getCode(), "鏀粯鏈嶅姟鎵ц澶辫触");
        }
    }

    private boolean isActiveSubmittingRecord(OrderIdempotent idempotent, LocalDateTime now) {
        if (idempotent == null) {
            return false;
        }
        if (idempotent.getStatus() == null || idempotent.getStatus() != 0) {
            return false;
        }
        return idempotent.getExpireTime() == null || idempotent.getExpireTime().isAfter(now);
    }

    private boolean isZeroPayOrder(BigDecimal payMoney) {
        return payMoney != null && payMoney.compareTo(BigDecimal.ZERO) <= 0;
    }

    private void saveStatusLog(Long orderId, Integer fromState, Integer toState, String operator, String remark) {
        OrderStatusLog statusLog = new OrderStatusLog();
        statusLog.setOrderId(orderId);
        statusLog.setFromState(fromState);
        statusLog.setToState(toState);
        statusLog.setOperator(operator);
        statusLog.setRemark(remark);
        statusLog.setCreateTime(LocalDateTime.now());
        orderStatusLogMapper.insert(statusLog);
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

    private void cancelOrderInternal(OrderInfo order, String reason, String operator) {
        if (order.getOrderState() != ORDER_STATE_PENDING_PAY) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "仅待付款订单可以取消");
        }
        int fromState = order.getOrderState();
        order.setOrderState(ORDER_STATE_CANCELED);
        order.setCancelReason(reason);
        order.setUpdateTime(LocalDateTime.now());
        orderInfoMapper.update(order);
        stockReleaseOrchestrator.releaseOrderStocks(order.getId(), order.getOrderNo());
        saveStatusLog(order.getId(), fromState, ORDER_STATE_CANCELED, operator, reason);
    }
}
