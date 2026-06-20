package com.xtx.order.service;

import com.xtx.api.goods.GoodsClient;
import com.xtx.api.goods.dto.SkuSnapshotDTO;
import com.xtx.api.member.MemberClient;
import com.xtx.api.member.dto.AddressSnapshotDTO;
import com.xtx.api.order.dto.AsyncOrderCreateMessageDTO;
import com.xtx.common.core.exception.BizException;
import com.xtx.common.core.result.ApiResponse;
import com.xtx.order.dto.AsyncSubmitOrderDTO;
import com.xtx.order.dto.SubmitOrderDTO;
import com.xtx.order.mq.OrderCreateMessageProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AsyncOrderServiceTest {

    @Mock OrderTokenService orderTokenService;
    @Mock OrderStockPreDeductService orderStockPreDeductService;
    @Mock OrderCreateMessageProducer orderCreateMessageProducer;
    @Mock OrderProcessStatusService orderProcessStatusService;
    @Mock OrderRequestTrackingService orderRequestTrackingService;
    @Mock MemberClient memberClient;
    @Mock GoodsClient goodsClient;
    @Mock OrderBenefitService orderBenefitService;

    @Captor ArgumentCaptor<AsyncOrderCreateMessageDTO> messageCaptor;

    AsyncOrderService asyncOrderService;

    private static final Long USER_ID = 1L;
    private static final Long SKU_ID = 1001L;
    private static final int COUNT = 2;
    private static final BigDecimal PRICE = new BigDecimal("99.00");
    private static final Long ADDRESS_ID = 10L;

    @BeforeEach
    void setUp() {
        asyncOrderService = new AsyncOrderService(
                orderTokenService, orderStockPreDeductService,
                orderCreateMessageProducer, orderProcessStatusService,
                orderRequestTrackingService, memberClient, goodsClient, orderBenefitService);

        doNothing().when(orderTokenService).validateAndConsumeToken(anyLong(), anyString());
        mockValidations();
        when(orderStockPreDeductService.preDeduct(anyString(), anyLong(), anyList())).thenReturn("redis-req-1");
        doNothing().when(orderCreateMessageProducer).sendCreateOrderMessage(any(AsyncOrderCreateMessageDTO.class));
        when(orderBenefitService.resolveForSubmit(any(), any(), any(), any(BigDecimal.class), any(BigDecimal.class)))
                .thenAnswer(invocation -> {
                    BigDecimal totalMoney = invocation.getArgument(3);
                    BigDecimal postFee = invocation.getArgument(4);
                    return OrderBenefitService.BenefitSnapshot.empty(totalMoney.add(postFee));
                });
    }

    private AsyncSubmitOrderDTO createValidRequest() {
        AsyncSubmitOrderDTO dto = new AsyncSubmitOrderDTO();
        dto.setAddressId(ADDRESS_ID);
        dto.setToken("test-token");
        SubmitOrderDTO.OrderItemDTO item = new SubmitOrderDTO.OrderItemDTO();
        item.setSkuId(SKU_ID);
        item.setCount(COUNT);
        dto.setGoods(List.of(item));
        return dto;
    }

    private void mockValidations() {
        AddressSnapshotDTO address = new AddressSnapshotDTO();
        address.setReceiverName("测试");
        address.setReceiverPhone("13800138000");
        address.setFullAddress("测试地址");
        when(memberClient.getAddressSnapshot(eq(USER_ID), eq(ADDRESS_ID)))
                .thenReturn(ApiResponse.success(address));

        SkuSnapshotDTO sku = new SkuSnapshotDTO();
        sku.setSkuId(SKU_ID);
        sku.setGoodsName("测试商品");
        sku.setPicture("http://example.com/pic.jpg");
        sku.setAttrsText("测试规格");
        sku.setPrice(PRICE);
        sku.setNowPrice(PRICE);
        sku.setStatus(1);
        sku.setStock(100);
        when(goodsClient.listSkuSnapshots(anyList()))
                .thenReturn(ApiResponse.success(List.of(sku)));
    }

    @Test
    void submitAsyncSuccess() {
        Map<String, Object> result = asyncOrderService.submitAsync(USER_ID, null, createValidRequest());

        assertNotNull(result);
        assertEquals("PROCESSING", result.get("status"));
        assertNotNull(result.get("orderNo"));

        verify(orderTokenService).validateAndConsumeToken(eq(USER_ID), eq("test-token"));
        verify(orderStockPreDeductService).preDeduct(anyString(), eq(USER_ID), anyList());
        verify(orderRequestTrackingService).saveRequestId(anyString(), eq("redis-req-1"));
        verify(orderProcessStatusService).markProcessing(anyString(), eq("redis-req-1"));
        verify(orderCreateMessageProducer).sendCreateOrderMessage(messageCaptor.capture());

        AsyncOrderCreateMessageDTO sentMsg = messageCaptor.getValue();
        assertEquals("redis-req-1", sentMsg.getRequestId());
        assertEquals(USER_ID, sentMsg.getUserId());
        assertEquals(ADDRESS_ID, sentMsg.getAddressId());
        assertEquals(new BigDecimal("198.00"), sentMsg.getTotalAmount());
        assertEquals(BigDecimal.ZERO, sentMsg.getPostFee());
        assertEquals(new BigDecimal("198.00"), sentMsg.getPayMoney());
        assertEquals(Integer.valueOf(0), sentMsg.getRetryCount());
        assertEquals(1, sentMsg.getItems().size());
        assertEquals(SKU_ID, sentMsg.getItems().get(0).getSkuId());
        assertEquals(Integer.valueOf(COUNT), sentMsg.getItems().get(0).getCount());
    }

    @Test
    void tokenInvalidShouldFail() {
        doThrow(new BizException(40020, "订单重复提交"))
                .when(orderTokenService).validateAndConsumeToken(anyLong(), anyString());

        BizException ex = assertThrows(BizException.class,
                () -> asyncOrderService.submitAsync(USER_ID, null, createValidRequest()));
        assertEquals(40020, ex.getCode());

        verify(orderStockPreDeductService, never()).preDeduct(anyString(), anyLong(), anyList());
        verify(orderCreateMessageProducer, never()).sendCreateOrderMessage(any());
        verify(orderProcessStatusService, never()).markProcessing(anyString(), anyString());
    }

    @Test
    void addressInvalidShouldFail() {
        when(memberClient.getAddressSnapshot(eq(USER_ID), eq(ADDRESS_ID)))
                .thenReturn(ApiResponse.success(null));

        BizException ex = assertThrows(BizException.class,
                () -> asyncOrderService.submitAsync(USER_ID, null, createValidRequest()));
        assertTrue(ex.getMessage().contains("收货地址不存在"));

        verify(orderStockPreDeductService, never()).preDeduct(anyString(), anyLong(), anyList());
        verify(orderCreateMessageProducer, never()).sendCreateOrderMessage(any());
    }

    @Test
    void skuNotFoundShouldFail() {
        when(goodsClient.listSkuSnapshots(anyList()))
                .thenReturn(ApiResponse.success(List.of()));

        BizException ex = assertThrows(BizException.class,
                () -> asyncOrderService.submitAsync(USER_ID, null, createValidRequest()));
        assertTrue(ex.getMessage().contains("SKU不存在"));

        verify(orderStockPreDeductService, never()).preDeduct(anyString(), anyLong(), anyList());
        verify(orderCreateMessageProducer, never()).sendCreateOrderMessage(any());
    }

    @Test
    void skuOffShelfShouldFail() {
        SkuSnapshotDTO sku = new SkuSnapshotDTO();
        sku.setSkuId(SKU_ID);
        sku.setGoodsName("已下架商品");
        sku.setStatus(0);
        when(goodsClient.listSkuSnapshots(anyList()))
                .thenReturn(ApiResponse.success(List.of(sku)));

        BizException ex = assertThrows(BizException.class,
                () -> asyncOrderService.submitAsync(USER_ID, null, createValidRequest()));
        assertTrue(ex.getMessage().contains("商品已下架"));

        verify(orderStockPreDeductService, never()).preDeduct(anyString(), anyLong(), anyList());
        verify(orderCreateMessageProducer, never()).sendCreateOrderMessage(any());
    }

    @Test
    void preDeductInsufficientShouldFail() {
        doThrow(new BizException(40000, "库存不足"))
                .when(orderStockPreDeductService).preDeduct(anyString(), anyLong(), anyList());

        BizException ex = assertThrows(BizException.class,
                () -> asyncOrderService.submitAsync(USER_ID, null, createValidRequest()));
        assertTrue(ex.getMessage().contains("库存不足"));

        verify(orderCreateMessageProducer, never()).sendCreateOrderMessage(any());
        verify(orderProcessStatusService, never()).markProcessing(anyString(), anyString());
        verify(orderStockPreDeductService, never()).rollback(anyString(), anyString(), anyList());
    }

    @Test
    void mqPublishFailureShouldRollbackAndMarkFailed() {
        doThrow(new RuntimeException("MQ不可用"))
                .when(orderCreateMessageProducer).sendCreateOrderMessage(any(AsyncOrderCreateMessageDTO.class));

        BizException ex = assertThrows(BizException.class,
                () -> asyncOrderService.submitAsync(USER_ID, null, createValidRequest()));
        assertTrue(ex.getMessage().contains("异步下单失败"));

        verify(orderProcessStatusService).markProcessing(anyString(), eq("redis-req-1"));
        verify(orderStockPreDeductService).rollback(eq("redis-req-1"), anyString(), anyList());
        verify(orderRequestTrackingService).removeRequestId(anyString());
        verify(orderProcessStatusService).markFailed(anyString(), eq("redis-req-1"), anyString());
    }
}
