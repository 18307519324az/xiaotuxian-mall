package com.xtx.order.service;

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
import com.xtx.order.entity.OrderGoods;
import com.xtx.order.entity.OrderInfo;
import com.xtx.order.entity.OrderStatusLog;
import com.xtx.order.mapper.OrderGoodsMapper;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderCreateTransactionServiceTest {

    @Mock OrderInfoMapper orderInfoMapper;
    @Mock OrderGoodsMapper orderGoodsMapper;
    @Mock OrderStatusLogMapper orderStatusLogMapper;
    @Mock GoodsClient goodsClient;
    @Mock StockClient stockClient;
    @Mock MemberClient memberClient;
    @Mock PaymentClient paymentClient;
    @Mock ObjectMapper objectMapper;
    @Mock OrderRequestTrackingService orderRequestTrackingService;

    OrderCreateTransactionService service;

    private static final Long USER_ID = 1L;
    private static final Long SKU_ID = 1001L;
    private static final int COUNT = 2;
    private static final BigDecimal PRICE = new BigDecimal("99.00");
    private static final Long ADDRESS_ID = 10L;

    @BeforeEach
    void setUp() throws Exception {
        service = new OrderCreateTransactionService(
                orderInfoMapper, orderGoodsMapper, orderStatusLogMapper,
                goodsClient, stockClient, memberClient, paymentClient, objectMapper, orderRequestTrackingService);

        // Mock address
        AddressSnapshotDTO address = new AddressSnapshotDTO();
        address.setReceiverName("测试");
        address.setReceiverPhone("13800138000");
        address.setFullAddress("测试地址");
        when(memberClient.getAddressSnapshot(eq(USER_ID), eq(ADDRESS_ID)))
                .thenReturn(ApiResponse.success(address));

        // Mock SKU snapshots
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

        // Mock objectMapper
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // Mock DB stock reserve success
        StockReserveResultDTO reserveResult = new StockReserveResultDTO();
        reserveResult.setAllSuccess(true);
        when(stockClient.reserveStocks(any(StockReserveRequestDTO.class)))
                .thenReturn(ApiResponse.success(reserveResult));

        // Mock order insert with ID callback
        doAnswer(invocation -> {
            OrderInfo order = invocation.getArgument(0);
            Field idField = OrderInfo.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(order, 100L);
            return 1;
        }).when(orderInfoMapper).insert(any(OrderInfo.class));

        // Mock pay order creation
        PayOrderDTO payData = new PayOrderDTO();
        payData.setPayNo("PAY123");
        when(paymentClient.createPayOrder(any(CreatePayOrderRequestDTO.class)))
                .thenReturn(ApiResponse.success(payData));
    }

    private AsyncOrderCreateMessageDTO validMessage() {
        AsyncOrderCreateMessageDTO msg = new AsyncOrderCreateMessageDTO();
        msg.setRequestId("req-001");
        msg.setOrderNo("ORDER001");
        msg.setUserId(USER_ID);
        msg.setAddressId(ADDRESS_ID);
        msg.setTotalAmount(new BigDecimal("198.00"));
        msg.setPostFee(BigDecimal.ZERO);
        msg.setPayMoney(new BigDecimal("198.00"));
        msg.setRetryCount(0);
        AsyncOrderCreateMessageDTO.Item item = new AsyncOrderCreateMessageDTO.Item();
        item.setSkuId(SKU_ID);
        item.setCount(COUNT);
        msg.setItems(List.of(item));
        return msg;
    }

    @Test
    void createOrderSuccess() {
        Long orderId = service.createOrderAfterRedisPreDeduct(validMessage());

        assertEquals(100L, orderId);
        verify(orderInfoMapper).insert(any(OrderInfo.class));
        verify(orderGoodsMapper, times(1)).insert(any(OrderGoods.class));
        verify(stockClient).reserveStocks(any(StockReserveRequestDTO.class));
        verify(paymentClient).createPayOrder(any(CreatePayOrderRequestDTO.class));
        verify(orderStatusLogMapper).insert(any(OrderStatusLog.class));
        verify(orderRequestTrackingService, never()).removeRequestId(anyString());
    }

    @Test
    void addressNotFoundShouldFail() {
        when(memberClient.getAddressSnapshot(eq(USER_ID), eq(ADDRESS_ID)))
                .thenReturn(ApiResponse.success(null));

        assertThrows(BizException.class,
                () -> service.createOrderAfterRedisPreDeduct(validMessage()));
        verify(orderInfoMapper, never()).insert(any());
    }

    @Test
    void skuNotFoundShouldFail() {
        when(goodsClient.listSkuSnapshots(anyList()))
                .thenReturn(ApiResponse.success(List.of()));

        assertThrows(BizException.class,
                () -> service.createOrderAfterRedisPreDeduct(validMessage()));
        verify(orderInfoMapper, never()).insert(any());
    }

    @Test
    void skuOffShelfShouldFail() {
        SkuSnapshotDTO sku = new SkuSnapshotDTO();
        sku.setSkuId(SKU_ID);
        sku.setGoodsName("已下架商品");
        sku.setStatus(0);
        when(goodsClient.listSkuSnapshots(anyList()))
                .thenReturn(ApiResponse.success(List.of(sku)));

        assertThrows(BizException.class,
                () -> service.createOrderAfterRedisPreDeduct(validMessage()));
        verify(orderInfoMapper, never()).insert(any());
    }

    @Test
    void stockReserveFailureShouldFail() {
        StockReserveResultDTO failResult = new StockReserveResultDTO();
        failResult.setAllSuccess(false);
        when(stockClient.reserveStocks(any(StockReserveRequestDTO.class)))
                .thenReturn(ApiResponse.success(failResult));

        assertThrows(BizException.class,
                () -> service.createOrderAfterRedisPreDeduct(validMessage()));
    }

    @Test
    void payOrderFailureShouldFail() {
        when(paymentClient.createPayOrder(any(CreatePayOrderRequestDTO.class)))
                .thenReturn(ApiResponse.success(null));

        assertThrows(BizException.class,
                () -> service.createOrderAfterRedisPreDeduct(validMessage()));
    }

    @Test
    void zeroPayOrderShouldSkipPaymentAndConfirmStock() {
        AsyncOrderCreateMessageDTO msg = validMessage();
        msg.setPayMoney(BigDecimal.ZERO);
        msg.setCouponId("cp-1");
        msg.setCouponName("满减券");
        msg.setCouponType("goods");
        msg.setDiscountGoodsAmount(new BigDecimal("198.00"));
        msg.setDiscountFreightAmount(BigDecimal.ZERO);
        msg.setDiscountAmount(new BigDecimal("198.00"));
        msg.setGiftCardAmount(BigDecimal.ZERO);

        Long orderId = service.createOrderAfterRedisPreDeduct(msg);

        assertEquals(100L, orderId);
        verify(paymentClient, never()).createPayOrder(any(CreatePayOrderRequestDTO.class));
        verify(stockClient).confirmDeduction("ORDER001");
        verify(orderRequestTrackingService).removeRequestId("ORDER001");
        verify(orderInfoMapper).update(argThat(order ->
                order.getOrderState().equals(OrderAppService.ORDER_STATE_PENDING_DELIVERY)
                        && order.getPayTime() != null
                        && "cp-1".equals(order.getCouponId())));
    }

    @Test
    void freeShippingOver99() {
        SkuSnapshotDTO sku = new SkuSnapshotDTO();
        sku.setSkuId(SKU_ID);
        sku.setGoodsName("测试商品");
        sku.setPrice(new BigDecimal("100.00"));
        sku.setNowPrice(new BigDecimal("100.00"));
        sku.setStatus(1);
        sku.setStock(100);
        when(goodsClient.listSkuSnapshots(anyList()))
                .thenReturn(ApiResponse.success(List.of(sku)));

        AsyncOrderCreateMessageDTO msg = validMessage();
        msg.getItems().get(0).setCount(1);
        service.createOrderAfterRedisPreDeduct(msg);

        verify(orderInfoMapper).insert(argThat(order ->
                order.getPostFee().compareTo(BigDecimal.ZERO) == 0));
    }

    @Test
    void multipleItemsCalculatesCorrectly() {
        AsyncOrderCreateMessageDTO msg = validMessage();
        AsyncOrderCreateMessageDTO.Item item2 = new AsyncOrderCreateMessageDTO.Item();
        item2.setSkuId(1002L);
        item2.setCount(3);

        SkuSnapshotDTO sku2 = new SkuSnapshotDTO();
        sku2.setSkuId(1002L);
        sku2.setGoodsName("商品2");
        sku2.setPrice(new BigDecimal("50.00"));
        sku2.setNowPrice(new BigDecimal("50.00"));
        sku2.setStatus(1);
        sku2.setStock(100);

        when(goodsClient.listSkuSnapshots(anyList()))
                .thenReturn(ApiResponse.success(List.of(
                        sku(), sku2)));

        msg.setItems(List.of(msg.getItems().get(0), item2));

        service.createOrderAfterRedisPreDeduct(msg);

        // totalMoney = 99*2 + 50*3 = 198 + 150 = 348
        verify(orderInfoMapper).insert(argThat(order ->
                order.getTotalMoney().compareTo(new BigDecimal("348")) == 0
                && order.getTotalNum() == 5));
    }

    private SkuSnapshotDTO sku() {
        SkuSnapshotDTO sku = new SkuSnapshotDTO();
        sku.setSkuId(SKU_ID);
        sku.setGoodsName("测试商品");
        sku.setPicture("http://example.com/pic.jpg");
        sku.setAttrsText("测试规格");
        sku.setPrice(PRICE);
        sku.setNowPrice(PRICE);
        sku.setStatus(1);
        sku.setStock(100);
        return sku;
    }
}
