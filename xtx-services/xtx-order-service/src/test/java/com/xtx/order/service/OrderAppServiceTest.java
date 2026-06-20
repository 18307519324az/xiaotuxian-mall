package com.xtx.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtx.api.cart.CartClient;
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
import com.xtx.common.core.result.ApiResponse;
import com.xtx.order.dto.SubmitOrderDTO;
import com.xtx.order.entity.OrderGoods;
import com.xtx.order.entity.OrderIdempotent;
import com.xtx.order.entity.OrderInfo;
import com.xtx.order.mapper.OrderGoodsMapper;
import com.xtx.order.mapper.OrderIdempotentMapper;
import com.xtx.order.mapper.OrderInfoMapper;
import com.xtx.order.mapper.OrderStatusLogMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderAppServiceTest {

    @Mock OrderInfoMapper orderInfoMapper;
    @Mock OrderGoodsMapper orderGoodsMapper;
    @Mock OrderStatusLogMapper orderStatusLogMapper;
    @Mock OrderIdempotentMapper orderIdempotentMapper;
    @Mock GoodsClient goodsClient;
    @Mock StockClient stockClient;
    @Mock CartClient cartClient;
    @Mock PaymentClient paymentClient;
    @Mock MemberClient memberClient;
    @Mock ObjectMapper objectMapper;
    @Mock OrderBenefitService orderBenefitService;
    @Mock OrderStockPreDeductService orderStockPreDeductService;
    @Mock OrderTokenService orderTokenService;
    @Mock OrderRequestTrackingService orderRequestTrackingService;
    @Mock StockReleaseOrchestrator stockReleaseOrchestrator;

    OrderAppService orderAppService;

    private static final Long USER_ID = 1L;
    private static final Long SKU_ID = 1001L;
    private static final int COUNT = 2;
    private static final BigDecimal PRICE = new BigDecimal("99.00");
    private static final Long ADDRESS_ID = 10L;

    @BeforeEach
    void setUp() throws Exception {
        orderAppService = new OrderAppService(
                orderInfoMapper, orderGoodsMapper, orderStatusLogMapper,
                orderIdempotentMapper, goodsClient, stockClient,
                cartClient, paymentClient, memberClient, objectMapper,
                orderBenefitService, orderStockPreDeductService, orderTokenService,
                orderRequestTrackingService, stockReleaseOrchestrator);

        when(orderIdempotentMapper.selectLatestByUserIdAndKey(anyLong(), anyString(), anyString())).thenReturn(null);
        doNothing().when(orderTokenService).validateAndConsumeToken(anyLong(), anyString());
        mockValidations();
        when(orderBenefitService.resolveForSubmit(any(), any(), any(), any(BigDecimal.class), any(BigDecimal.class)))
                .thenAnswer(invocation -> {
                    BigDecimal totalMoney = invocation.getArgument(3);
                    BigDecimal postFee = invocation.getArgument(4);
                    return OrderBenefitService.BenefitSnapshot.empty(totalMoney.add(postFee));
                });
    }

    private SubmitOrderDTO createValidRequest() {
        SubmitOrderDTO dto = new SubmitOrderDTO();
        dto.setAddressId(ADDRESS_ID);
        dto.setToken("test-token");
        SubmitOrderDTO.OrderItemDTO item = new SubmitOrderDTO.OrderItemDTO();
        item.setSkuId(SKU_ID);
        item.setCount(COUNT);
        dto.setGoods(List.of(item));
        return dto;
    }

    private void mockValidations() throws Exception {
        AddressSnapshotDTO address = new AddressSnapshotDTO();
        address.setReceiverName("测试");
        address.setReceiverPhone("13800138000");
        address.setFullAddress("测试地址");
        when(memberClient.getAddressSnapshot(eq(USER_ID), eq(ADDRESS_ID)))
                .thenReturn(ApiResponse.success(address));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

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

    private void mockOrderInsert(Long orderId) {
        doAnswer(invocation -> {
            OrderInfo order = invocation.getArgument(0);
            Field idField = OrderInfo.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(order, orderId);
            return 1;
        }).when(orderInfoMapper).insert(any(OrderInfo.class));
    }

    private void mockDbReserveSuccess() {
        StockReserveResultDTO reserveResult = new StockReserveResultDTO();
        reserveResult.setAllSuccess(true);
        when(stockClient.reserveStocks(any(StockReserveRequestDTO.class)))
                .thenReturn(ApiResponse.success(reserveResult));
    }

    private void mockPaySuccess() {
        PayOrderDTO payData = new PayOrderDTO();
        payData.setPayNo("PAY123");
        when(paymentClient.createPayOrder(any(CreatePayOrderRequestDTO.class)))
                .thenReturn(ApiResponse.success(payData));
    }

    @Test
    void submitOrderSuccess() throws Exception {
        when(orderStockPreDeductService.preDeduct(anyString(), anyLong(), anyList())).thenReturn("req-1");
        mockOrderInsert(100L);
        mockDbReserveSuccess();
        mockPaySuccess();

        Map<String, Object> result = orderAppService.submitOrder(USER_ID, null, createValidRequest());

        assertNotNull(result);
        assertEquals(100L, result.get("orderId"));
        assertNotNull(result.get("orderNo"));
        assertNotNull(result.get("payMoney"));

        verify(orderTokenService).validateAndConsumeToken(eq(USER_ID), eq("test-token"));
        verify(orderStockPreDeductService).preDeduct(anyString(), eq(USER_ID), anyList());
        verify(orderRequestTrackingService).saveRequestId(anyString(), eq("req-1"));
        verify(stockClient).reserveStocks(any(StockReserveRequestDTO.class));
        verify(cartClient).cleanCartBySkuIds(anyList(), eq(USER_ID));
    }

    @Test
    void completedIdempotentRecordShouldNotBlockRepurchase() throws Exception {
        OrderIdempotent existing = new OrderIdempotent();
        existing.setStatus(1);
        existing.setBizId("7");
        existing.setResponseJson("ORDER_OLD");
        existing.setExpireTime(LocalDateTime.now().plusMinutes(30));
        when(orderIdempotentMapper.selectLatestByUserIdAndKey(anyLong(), anyString(), anyString()))
                .thenReturn(existing);
        when(orderStockPreDeductService.preDeduct(anyString(), anyLong(), anyList())).thenReturn("req-repurchase");
        mockOrderInsert(100L);
        mockDbReserveSuccess();
        mockPaySuccess();

        Map<String, Object> result = orderAppService.submitOrder(USER_ID, null, createValidRequest());

        assertEquals(100L, result.get("orderId"));
        assertNotNull(result.get("orderNo"));
        verify(orderStockPreDeductService).preDeduct(anyString(), eq(USER_ID), anyList());
        verify(orderInfoMapper).insert(any(OrderInfo.class));
    }

    @Test
    void processingIdempotentRecordShouldBlockDuplicateSubmit() {
        OrderIdempotent existing = new OrderIdempotent();
        existing.setStatus(0);
        existing.setExpireTime(LocalDateTime.now().plusMinutes(30));
        when(orderIdempotentMapper.selectLatestByUserIdAndKey(anyLong(), anyString(), anyString()))
                .thenReturn(existing);

        BizException ex = assertThrows(BizException.class,
                () -> orderAppService.submitOrder(USER_ID, null, createValidRequest()));

        assertEquals(40020, ex.getCode());
        verify(orderIdempotentMapper, never()).insert(any(OrderIdempotent.class));
        verify(orderStockPreDeductService, never()).preDeduct(anyString(), anyLong(), anyList());
        verify(orderInfoMapper, never()).insert(any(OrderInfo.class));
    }

    @Test
    void tokenDuplicateShouldFail() {
        doThrow(new BizException(40020, "订单重复提交"))
                .when(orderTokenService).validateAndConsumeToken(anyLong(), anyString());

        BizException ex = assertThrows(BizException.class,
                () -> orderAppService.submitOrder(USER_ID, null, createValidRequest()));
        assertEquals(40020, ex.getCode());

        verify(orderStockPreDeductService, never()).preDeduct(anyString(), anyLong(), anyList());
        verify(orderInfoMapper, never()).insert(any());
    }

    @Test
    void redisNotEnoughShouldFail() {
        doThrow(new BizException(40000, "库存不足"))
                .when(orderStockPreDeductService).preDeduct(anyString(), anyLong(), anyList());

        BizException ex = assertThrows(BizException.class,
                () -> orderAppService.submitOrder(USER_ID, null, createValidRequest()));
        assertTrue(ex.getMessage().contains("库存不足"));

        verify(orderInfoMapper, never()).insert(any());
        verify(orderStockPreDeductService, never()).rollback(anyString(), anyString(), anyList());
    }

    @Test
    void dbReserveFailureShouldRollbackRedis() throws Exception {
        when(orderStockPreDeductService.preDeduct(anyString(), anyLong(), anyList())).thenReturn("req-123");
        mockOrderInsert(100L);

        StockReserveResultDTO failResult = new StockReserveResultDTO();
        failResult.setAllSuccess(false);
        when(stockClient.reserveStocks(any(StockReserveRequestDTO.class)))
                .thenReturn(ApiResponse.success(failResult));

        assertThrows(BizException.class,
                () -> orderAppService.submitOrder(USER_ID, null, createValidRequest()));

        verify(orderStockPreDeductService).rollback(eq("req-123"), anyString(), anyList());
        verify(orderRequestTrackingService).removeRequestId(anyString());
    }

    @Test
    void orderSaveFailureShouldRollbackRedis() throws Exception {
        when(orderStockPreDeductService.preDeduct(anyString(), anyLong(), anyList())).thenReturn("req-456");
        doThrow(new RuntimeException("DB error")).when(orderInfoMapper).insert(any(OrderInfo.class));

        assertThrows(RuntimeException.class,
                () -> orderAppService.submitOrder(USER_ID, null, createValidRequest()));

        verify(orderStockPreDeductService).rollback(eq("req-456"), anyString(), anyList());
        verify(orderRequestTrackingService).removeRequestId(anyString());
    }

    @Test
    void goodsSaveFailureShouldRollbackRedis() throws Exception {
        when(orderStockPreDeductService.preDeduct(anyString(), anyLong(), anyList())).thenReturn("req-789");
        mockOrderInsert(100L);
        doThrow(new RuntimeException("Goods DB error")).when(orderGoodsMapper).insert(any(OrderGoods.class));

        assertThrows(RuntimeException.class,
                () -> orderAppService.submitOrder(USER_ID, null, createValidRequest()));

        verify(orderStockPreDeductService).rollback(eq("req-789"), anyString(), anyList());
        verify(orderRequestTrackingService).removeRequestId(anyString());
    }

    @Test
    void redisPreDeductShouldSetRedisRequestId() throws Exception {
        when(orderStockPreDeductService.preDeduct(anyString(), anyLong(), anyList())).thenReturn("req-999");
        mockOrderInsert(100L);
        when(stockClient.reserveStocks(any(StockReserveRequestDTO.class)))
                .thenThrow(new BizException(40900, "库存预占冲突"));

        assertThrows(BizException.class,
                () -> orderAppService.submitOrder(USER_ID, null, createValidRequest()));

        verify(orderRequestTrackingService).saveRequestId(anyString(), eq("req-999"));
        verify(orderStockPreDeductService).rollback(eq("req-999"), anyString(), anyList());
    }

    @Test
    void submitOrderSuccessShouldNotCallRollback() throws Exception {
        when(orderStockPreDeductService.preDeduct(anyString(), anyLong(), anyList())).thenReturn("req-1");
        mockOrderInsert(100L);
        mockDbReserveSuccess();
        mockPaySuccess();

        orderAppService.submitOrder(USER_ID, null, createValidRequest());

        verify(orderStockPreDeductService, never()).rollback(anyString(), anyString(), anyList());
        verify(orderRequestTrackingService, never()).removeRequestId(anyString());
    }

    @Test
    void payServiceFailureShouldRollbackRedis() throws Exception {
        when(orderStockPreDeductService.preDeduct(anyString(), anyLong(), anyList())).thenReturn("req-rollback-pay");
        mockOrderInsert(100L);
        mockDbReserveSuccess();
        when(paymentClient.createPayOrder(any(CreatePayOrderRequestDTO.class)))
                .thenThrow(new RuntimeException("支付服务异常"));

        assertThrows(RuntimeException.class,
                () -> orderAppService.submitOrder(USER_ID, null, createValidRequest()));

        verify(orderStockPreDeductService).rollback(eq("req-rollback-pay"), anyString(), anyList());
        verify(orderRequestTrackingService).removeRequestId(anyString());
    }
}
