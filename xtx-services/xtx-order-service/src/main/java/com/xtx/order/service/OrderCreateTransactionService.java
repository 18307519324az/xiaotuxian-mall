package com.xtx.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtx.api.goods.GoodsClient;
import com.xtx.api.goods.dto.SkuSnapshotDTO;
import com.xtx.api.inventory.StockClient;
import com.xtx.api.inventory.dto.StockReserveRequestDTO;
import com.xtx.api.inventory.dto.StockReserveResultDTO;
import com.xtx.api.member.MemberClient;
import com.xtx.api.member.dto.AddressSnapshotDTO;
import com.xtx.api.order.dto.AsyncOrderCreateMessageDTO;
import com.xtx.api.payment.PaymentClient;
import com.xtx.api.payment.dto.CreatePayOrderRequestDTO;
import com.xtx.api.payment.dto.PayOrderDTO;
import com.xtx.common.core.exception.BizException;
import com.xtx.common.core.result.ApiResponse;
import com.xtx.common.core.result.ResultCode;
import com.xtx.order.entity.OrderGoods;
import com.xtx.order.entity.OrderInfo;
import com.xtx.order.entity.OrderStatusLog;
import com.xtx.order.mapper.OrderGoodsMapper;
import com.xtx.order.mapper.OrderInfoMapper;
import com.xtx.order.mapper.OrderStatusLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCreateTransactionService {

    private final OrderInfoMapper orderInfoMapper;
    private final OrderGoodsMapper orderGoodsMapper;
    private final OrderStatusLogMapper orderStatusLogMapper;
    private final GoodsClient goodsClient;
    private final StockClient stockClient;
    private final MemberClient memberClient;
    private final PaymentClient paymentClient;
    private final ObjectMapper objectMapper;
    private final OrderRequestTrackingService orderRequestTrackingService;

    @Transactional(rollbackFor = Exception.class)
    public Long createOrderAfterRedisPreDeduct(AsyncOrderCreateMessageDTO message) {
        Long userId = message.getUserId();
        String orderNo = message.getOrderNo();

        AddressSnapshotDTO address = getAddress(userId, message.getAddressId());

        List<Long> skuIds = message.getItems().stream()
                .map(AsyncOrderCreateMessageDTO.Item::getSkuId)
                .collect(Collectors.toList());
        Map<Long, SkuSnapshotDTO> skuMap = getSkuMap(skuIds);
        validateSkus(message, skuMap);

        BigDecimal totalMoney = BigDecimal.ZERO;
        int totalNum = 0;
        for (AsyncOrderCreateMessageDTO.Item item : message.getItems()) {
            SkuSnapshotDTO sku = skuMap.get(item.getSkuId());
            BigDecimal price = sku.getNowPrice() != null ? sku.getNowPrice() : sku.getPrice();
            totalMoney = totalMoney.add(price.multiply(BigDecimal.valueOf(item.getCount())));
            totalNum += item.getCount();
        }
        BigDecimal postFee = message.getPostFee() != null
                ? message.getPostFee()
                : (totalMoney.compareTo(BigDecimal.valueOf(99)) >= 0
                ? BigDecimal.ZERO : BigDecimal.valueOf(10));
        BigDecimal payMoney = message.getPayMoney() != null
                ? message.getPayMoney()
                : totalMoney.add(postFee);

        reserveStocks(orderNo, message.getItems());

        OrderInfo order = buildOrder(orderNo, userId, payMoney, totalMoney, postFee, totalNum, address, message);
        orderInfoMapper.insert(order);
        Long orderId = order.getId();

        for (AsyncOrderCreateMessageDTO.Item item : message.getItems()) {
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

        if (payMoney.compareTo(BigDecimal.ZERO) > 0) {
            createPayOrder(orderId, orderNo, userId, payMoney);
        } else {
            stockClient.confirmDeduction(orderNo);
            order.setPayTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());
            orderInfoMapper.update(order);
            orderRequestTrackingService.removeRequestId(orderNo);
        }

        saveStatusLog(orderId, null, order.getOrderState(), "system", "异步下单创建成功");
        return orderId;
    }

    private AddressSnapshotDTO getAddress(Long userId, Long addressId) {
        ApiResponse<AddressSnapshotDTO> response = memberClient.getAddressSnapshot(userId, addressId);
        AddressSnapshotDTO address = response != null ? response.getData() : null;
        if (address == null) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "收货地址不存在");
        }
        return address;
    }

    private Map<Long, SkuSnapshotDTO> getSkuMap(List<Long> skuIds) {
        ApiResponse<List<SkuSnapshotDTO>> response = goodsClient.listSkuSnapshots(skuIds);
        List<SkuSnapshotDTO> skuList = response != null ? response.getData() : null;
        if (skuList == null) {
            throw new BizException(ResultCode.INTERNAL_ERROR.getCode(), "获取SKU信息失败");
        }
        return skuList.stream()
                .collect(Collectors.toMap(SkuSnapshotDTO::getSkuId, Function.identity(), (left, right) -> left));
    }

    private void validateSkus(AsyncOrderCreateMessageDTO message, Map<Long, SkuSnapshotDTO> skuMap) {
        for (AsyncOrderCreateMessageDTO.Item item : message.getItems()) {
            SkuSnapshotDTO sku = skuMap.get(item.getSkuId());
            if (sku == null) {
                throw new BizException(ResultCode.BAD_REQUEST.getCode(), "SKU不存在: " + item.getSkuId());
            }
            if (sku.getStatus() == null || sku.getStatus() != 1) {
                throw new BizException(ResultCode.BAD_REQUEST.getCode(), "商品已下架: " + sku.getGoodsName());
            }
        }
    }

    private void reserveStocks(String orderNo, List<AsyncOrderCreateMessageDTO.Item> items) {
        try {
            StockReserveRequestDTO reserveRequest = new StockReserveRequestDTO();
            reserveRequest.setOrderNo(orderNo);
            List<StockReserveRequestDTO.StockReserveItemDTO> reserveItems = items.stream()
                    .map(item -> {
                        StockReserveRequestDTO.StockReserveItemDTO reserveItem =
                                new StockReserveRequestDTO.StockReserveItemDTO();
                        reserveItem.setSkuId(item.getSkuId());
                        reserveItem.setCount(item.getCount());
                        return reserveItem;
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
    }

    private OrderInfo buildOrder(String orderNo, Long userId, BigDecimal payMoney,
                                 BigDecimal totalMoney, BigDecimal postFee, int totalNum,
                                 AddressSnapshotDTO address, AsyncOrderCreateMessageDTO message) {
        OrderInfo order = new OrderInfo();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setPayMoney(payMoney);
        order.setTotalMoney(totalMoney);
        order.setPostFee(postFee);
        order.setTotalNum(totalNum);
        order.setOrderState(payMoney.compareTo(BigDecimal.ZERO) > 0
                ? OrderAppService.ORDER_STATE_PENDING_PAY
                : OrderAppService.ORDER_STATE_PENDING_DELIVERY);
        order.setDeliveryTimeType(1);
        order.setPayType(1);
        order.setPayChannel(1);
        order.setCouponId(message.getCouponId());
        order.setCouponName(message.getCouponName());
        order.setCouponType(message.getCouponType());
        order.setDiscountGoodsAmount(message.getDiscountGoodsAmount());
        order.setDiscountFreightAmount(message.getDiscountFreightAmount());
        order.setDiscountAmount(message.getDiscountAmount());
        order.setGiftCardCode(message.getGiftCardCode());
        order.setGiftCardAmount(message.getGiftCardAmount());
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        try {
            order.setReceiverAddress(objectMapper.writeValueAsString(address));
        } catch (JsonProcessingException e) {
            throw new BizException(ResultCode.INTERNAL_ERROR.getCode(), "地址序列化失败");
        }
        if (payMoney.compareTo(BigDecimal.ZERO) <= 0) {
            order.setPayTime(LocalDateTime.now());
        }
        order.setIsDeleted(0);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        return order;
    }

    private void createPayOrder(Long orderId, String orderNo, Long userId, BigDecimal payMoney) {
        try {
            CreatePayOrderRequestDTO payRequest = new CreatePayOrderRequestDTO();
            payRequest.setOrderId(orderId);
            payRequest.setOrderNo(orderNo);
            payRequest.setUserId(userId);
            payRequest.setPayMoney(payMoney);
            payRequest.setPayChannel(1);

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
    }

    private void saveStatusLog(Long orderId, Integer fromState, Integer toState,
                               String operator, String remark) {
        OrderStatusLog statusLog = new OrderStatusLog();
        statusLog.setOrderId(orderId);
        statusLog.setFromState(fromState);
        statusLog.setToState(toState);
        statusLog.setOperator(operator);
        statusLog.setRemark(remark);
        statusLog.setCreateTime(LocalDateTime.now());
        orderStatusLogMapper.insert(statusLog);
    }
}
