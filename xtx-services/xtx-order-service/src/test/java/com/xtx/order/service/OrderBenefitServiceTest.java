package com.xtx.order.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderBenefitServiceTest {

    private final OrderBenefitService service = new OrderBenefitService(null);

    @Test
    void applyBenefitsShouldApplyGoodsCouponThenGiftCard() {
        OrderBenefitService.BenefitSnapshot snapshot = service.applyBenefits(
                List.of(Map.of(
                        "id", "cp-1",
                        "status", "available",
                        "threshold", "100.00",
                        "amount", "20.00",
                        "couponType", "goods",
                        "name", "满减券"
                )),
                List.of(Map.of(
                        "code", "CARD100",
                        "status", "active",
                        "balance", "50.00"
                )),
                "cp-1",
                "card100",
                new BigDecimal("120.00"),
                new BigDecimal("10.00")
        );

        assertEquals(new BigDecimal("60.00"), snapshot.getPayMoney());
        assertEquals("cp-1", snapshot.getCouponId());
        assertEquals("满减券", snapshot.getCouponName());
        assertEquals("goods", snapshot.getCouponType());
        assertEquals(new BigDecimal("20.00"), snapshot.getDiscountGoodsAmount());
        assertEquals(new BigDecimal("0.00"), snapshot.getDiscountFreightAmount());
        assertEquals(new BigDecimal("20.00"), snapshot.getDiscountAmount());
        assertEquals("CARD100", snapshot.getGiftCardCode());
        assertEquals(new BigDecimal("50.00"), snapshot.getGiftCardAmount());
    }

    @Test
    void applyBenefitsShouldLimitFreightCouponToPostFee() {
        OrderBenefitService.BenefitSnapshot snapshot = service.applyBenefits(
                List.of(Map.of(
                        "id", "cp-2",
                        "status", "available",
                        "threshold", "1.00",
                        "amount", "20.00",
                        "couponType", "freight",
                        "name", "运费券"
                )),
                List.of(),
                "cp-2",
                null,
                new BigDecimal("80.00"),
                new BigDecimal("10.00")
        );

        assertEquals(new BigDecimal("80.00"), snapshot.getPayMoney());
        assertEquals("cp-2", snapshot.getCouponId());
        assertEquals("运费券", snapshot.getCouponName());
        assertEquals("freight", snapshot.getCouponType());
        assertEquals(new BigDecimal("0.00"), snapshot.getDiscountGoodsAmount());
        assertEquals(new BigDecimal("10.00"), snapshot.getDiscountFreightAmount());
        assertEquals(new BigDecimal("10.00"), snapshot.getDiscountAmount());
        assertEquals(new BigDecimal("0.00"), snapshot.getGiftCardAmount());
    }
}
