package com.xtx.order.service;

import com.xtx.api.goods.GoodsClient;
import com.xtx.api.goods.dto.SkuSnapshotDTO;
import com.xtx.api.member.MemberClient;
import com.xtx.api.member.dto.AddressSnapshotDTO;
import com.xtx.api.order.dto.AsyncOrderCreateMessageDTO;
import com.xtx.common.core.exception.BizException;
import com.xtx.common.core.result.ApiResponse;
import com.xtx.common.core.result.ResultCode;
import com.xtx.order.dto.AsyncSubmitOrderDTO;
import com.xtx.order.dto.SubmitOrderDTO;
import com.xtx.order.mq.OrderCreateMessageProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncOrderService {

    private final OrderTokenService orderTokenService;
    private final OrderStockPreDeductService orderStockPreDeductService;
    private final OrderCreateMessageProducer orderCreateMessageProducer;
    private final OrderProcessStatusService orderProcessStatusService;
    private final OrderRequestTrackingService orderRequestTrackingService;
    private final MemberClient memberClient;
    private final GoodsClient goodsClient;
    private final OrderBenefitService orderBenefitService;

    public Map<String, Object> submitAsync(Long userId, String authHeader, AsyncSubmitOrderDTO dto) {
        orderTokenService.validateAndConsumeToken(userId, dto.getToken());

        getAddress(userId, dto.getAddressId());

        List<Long> skuIds = dto.getGoods().stream()
                .map(SubmitOrderDTO.OrderItemDTO::getSkuId)
                .collect(Collectors.toList());
        Map<Long, SkuSnapshotDTO> skuMap = getSkuMap(skuIds);
        validateSkus(dto.getGoods(), skuMap);
        java.math.BigDecimal totalMoney = dto.getGoods().stream()
                .map(item -> {
                    SkuSnapshotDTO sku = skuMap.get(item.getSkuId());
                    java.math.BigDecimal price = sku.getNowPrice() != null ? sku.getNowPrice() : sku.getPrice();
                    return price.multiply(java.math.BigDecimal.valueOf(item.getCount()));
                })
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        java.math.BigDecimal postFee = totalMoney.compareTo(java.math.BigDecimal.valueOf(99)) >= 0
                ? java.math.BigDecimal.ZERO : java.math.BigDecimal.valueOf(10);
        OrderBenefitService.BenefitSnapshot benefitSnapshot = orderBenefitService.resolveForSubmit(
                authHeader, dto.getCouponId(), dto.getGiftCardCode(), totalMoney, postFee);

        String orderNo = generateOrderNo();
        String requestId;
        try {
            requestId = orderStockPreDeductService.preDeduct(orderNo, userId, dto.getGoods());
            orderRequestTrackingService.saveRequestId(orderNo, requestId);
            log.debug("Redis 库存预扣成功, orderNo={}, requestId={}", orderNo, requestId);
        } catch (BizException e) {
            log.warn("Redis 库存预扣不足, orderNo={}, userId={}", orderNo, userId);
            throw e;
        }

        orderProcessStatusService.markProcessing(orderNo, requestId);

        try {
            AsyncOrderCreateMessageDTO message = buildMessage(
                    requestId, orderNo, userId, dto, totalMoney, postFee, benefitSnapshot);
            orderCreateMessageProducer.sendCreateOrderMessage(message);
            log.info("异步下单消息已发布, orderNo={}, requestId={}", orderNo, requestId);
        } catch (Exception e) {
            log.error("MQ 消息发布失败, orderNo={}, requestId={}", orderNo, requestId, e);
            try {
                orderStockPreDeductService.rollback(requestId, orderNo, dto.getGoods());
                orderRequestTrackingService.removeRequestId(orderNo);
            } catch (Exception rollbackEx) {
                log.error("Redis 回滚失败, orderNo={}, requestId={}", orderNo, requestId, rollbackEx);
            }
            orderProcessStatusService.markFailed(orderNo, requestId, "MQ消息发布失败");
            throw new BizException(ResultCode.INTERNAL_ERROR.getCode(), "异步下单失败，请稍后重试");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", orderNo);
        result.put("status", "PROCESSING");
        return result;
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

    private void validateSkus(List<SubmitOrderDTO.OrderItemDTO> items, Map<Long, SkuSnapshotDTO> skuMap) {
        for (SubmitOrderDTO.OrderItemDTO item : items) {
            SkuSnapshotDTO sku = skuMap.get(item.getSkuId());
            if (sku == null) {
                throw new BizException(ResultCode.BAD_REQUEST.getCode(), "SKU不存在: " + item.getSkuId());
            }
            if (sku.getStatus() == null || sku.getStatus() != 1) {
                throw new BizException(ResultCode.BAD_REQUEST.getCode(), "商品已下架: " + sku.getGoodsName());
            }
        }
    }

    private AsyncOrderCreateMessageDTO buildMessage(String requestId, String orderNo,
                                                    Long userId, AsyncSubmitOrderDTO dto,
                                                    java.math.BigDecimal totalMoney,
                                                    java.math.BigDecimal postFee,
                                                    OrderBenefitService.BenefitSnapshot benefitSnapshot) {
        AsyncOrderCreateMessageDTO message = new AsyncOrderCreateMessageDTO();
        message.setRequestId(requestId);
        message.setOrderNo(orderNo);
        message.setUserId(userId);
        message.setAddressId(dto.getAddressId());
        message.setTotalAmount(totalMoney);
        message.setPostFee(postFee);
        message.setPayMoney(benefitSnapshot.getPayMoney());
        message.setCouponId(benefitSnapshot.getCouponId());
        message.setCouponName(benefitSnapshot.getCouponName());
        message.setCouponType(benefitSnapshot.getCouponType());
        message.setDiscountGoodsAmount(benefitSnapshot.getDiscountGoodsAmount());
        message.setDiscountFreightAmount(benefitSnapshot.getDiscountFreightAmount());
        message.setDiscountAmount(benefitSnapshot.getDiscountAmount());
        message.setGiftCardCode(benefitSnapshot.getGiftCardCode());
        message.setGiftCardAmount(benefitSnapshot.getGiftCardAmount());
        message.setCreateTime(LocalDateTime.now());
        message.setRetryCount(0);
        message.setItems(dto.getGoods().stream()
                .map(item -> {
                    AsyncOrderCreateMessageDTO.Item messageItem = new AsyncOrderCreateMessageDTO.Item();
                    messageItem.setSkuId(item.getSkuId());
                    messageItem.setCount(item.getCount());
                    return messageItem;
                })
                .collect(Collectors.toList()));
        return message;
    }

    private String generateOrderNo() {
        String datePart = java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = cn.hutool.core.util.RandomUtil.randomNumbers(8);
        return "ORDER" + datePart + randomPart;
    }
}
