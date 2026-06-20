package com.xtx.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtx.common.core.exception.BizException;
import com.xtx.common.core.result.ResultCode;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OrderBenefitService {

    private final ObjectMapper objectMapper;

    @Value("${services.mock:http://localhost:8099}")
    private String mockServiceBaseUrl;

    public BenefitSnapshot resolveForSubmit(String authHeader,
                                            String couponId,
                                            String giftCardCode,
                                            BigDecimal totalMoney,
                                            BigDecimal postFee) {
        BenefitSnapshot emptySnapshot = BenefitSnapshot.empty(totalMoney.add(postFee));
        if (isBlank(couponId) && isBlank(giftCardCode)) {
            return emptySnapshot;
        }
        if (isBlank(authHeader)) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "未获取到登录凭证，无法校验优惠信息");
        }

        List<Map<String, Object>> coupons = isBlank(couponId)
                ? Collections.emptyList()
                : fetchResultList("/member/coupon", authHeader, "items");
        List<Map<String, Object>> giftCards = isBlank(giftCardCode)
                ? Collections.emptyList()
                : fetchResultList("/member/gift-card", authHeader, "cards");
        return applyBenefits(coupons, giftCards, couponId, giftCardCode, totalMoney, postFee);
    }

    BenefitSnapshot applyBenefits(List<Map<String, Object>> coupons,
                                  List<Map<String, Object>> giftCards,
                                  String couponId,
                                  String giftCardCode,
                                  BigDecimal totalMoney,
                                  BigDecimal postFee) {
        BigDecimal goodsAmount = scale(totalMoney);
        BigDecimal freightAmount = scale(postFee);
        BenefitSnapshot snapshot = BenefitSnapshot.empty(goodsAmount.add(freightAmount));

        if (!isBlank(couponId)) {
            Map<String, Object> coupon = coupons.stream()
                    .filter(item -> Objects.equals(couponId, stringValue(item.get("id"))))
                    .filter(item -> Objects.equals("available", stringValue(item.get("status"))))
                    .findFirst()
                    .orElse(null);
            if (coupon != null) {
                BigDecimal threshold = decimalValue(coupon.get("threshold"));
                if (goodsAmount.compareTo(threshold) >= 0) {
                    BigDecimal amount = decimalValue(coupon.get("amount"));
                    String couponType = stringValue(coupon.get("couponType"));
                    snapshot.setCouponId(couponId);
                    snapshot.setCouponName(stringValue(coupon.get("name")));
                    snapshot.setCouponType(couponType);
                    if ("freight".equals(couponType)) {
                        BigDecimal discountFreightAmount = amount.min(freightAmount);
                        freightAmount = freightAmount.subtract(discountFreightAmount);
                        snapshot.setDiscountFreightAmount(scale(discountFreightAmount));
                    } else {
                        BigDecimal discountGoodsAmount = amount.min(goodsAmount);
                        goodsAmount = goodsAmount.subtract(discountGoodsAmount);
                        snapshot.setDiscountGoodsAmount(scale(discountGoodsAmount));
                    }
                    snapshot.setDiscountAmount(scale(
                            snapshot.getDiscountGoodsAmount().add(snapshot.getDiscountFreightAmount())));
                }
            }
        }

        BigDecimal payMoney = goodsAmount.add(freightAmount);
        if (!isBlank(giftCardCode)) {
            String normalizedCode = giftCardCode.trim().toUpperCase();
            Map<String, Object> giftCard = giftCards.stream()
                    .filter(item -> Objects.equals(normalizedCode, stringValue(item.get("code")).trim().toUpperCase()))
                    .filter(item -> Objects.equals("active", stringValue(item.get("status"))))
                    .findFirst()
                    .orElse(null);
            if (giftCard != null) {
                BigDecimal balance = decimalValue(giftCard.get("balance"));
                if (balance.compareTo(BigDecimal.ZERO) > 0 && payMoney.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal giftCardAmount = balance.min(payMoney);
                    payMoney = payMoney.subtract(giftCardAmount);
                    snapshot.setGiftCardCode(normalizedCode);
                    snapshot.setGiftCardAmount(scale(giftCardAmount));
                }
            }
        }

        snapshot.setPayMoney(scale(payMoney));
        return snapshot;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchResultList(String path, String authHeader, String arrayField) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(mockServiceBaseUrl + path))
                    .header("Authorization", authHeader)
                    .GET()
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BizException(ResultCode.INTERNAL_ERROR.getCode(), "权益服务查询失败");
            }
            Map<String, Object> body = objectMapper.readValue(response.body(), Map.class);
            Object result = body.get("result");
            if (!(result instanceof Map<?, ?> resultMap)) {
                return Collections.emptyList();
            }
            Object items = resultMap.get(arrayField);
            if (!(items instanceof List<?> list)) {
                return Collections.emptyList();
            }
            return (List<Map<String, Object>>) list;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException(ResultCode.INTERNAL_ERROR.getCode(), "权益服务查询失败: " + e.getMessage());
        }
    }

    private BigDecimal decimalValue(Object value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return scale(new BigDecimal(String.valueOf(value)));
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    @Data
    public static class BenefitSnapshot {
        private BigDecimal payMoney;
        private String couponId;
        private String couponName;
        private String couponType;
        private BigDecimal discountGoodsAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        private BigDecimal discountFreightAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        private BigDecimal discountAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        private String giftCardCode;
        private BigDecimal giftCardAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        public static BenefitSnapshot empty(BigDecimal payMoney) {
            BenefitSnapshot snapshot = new BenefitSnapshot();
            snapshot.setPayMoney(payMoney.setScale(2, RoundingMode.HALF_UP));
            return snapshot;
        }
    }
}
