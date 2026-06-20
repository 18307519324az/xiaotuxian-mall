package com.xtx.payment.service;

import com.xtx.api.order.OrderClient;
import com.xtx.api.order.dto.OrderStatusUpdateDTO;
import com.xtx.common.core.exception.BizException;
import com.xtx.common.core.result.ApiResponse;
import com.xtx.common.core.result.ResultCode;
import com.xtx.payment.entity.PayOrder;
import com.xtx.payment.mapper.PayOrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaymentAppServiceTest {

    @Mock PayOrderMapper payOrderMapper;
    @Mock OrderClient orderClient;

    @Captor ArgumentCaptor<OrderStatusUpdateDTO> orderStatusCaptor;

    PaymentAppService paymentAppService;

    @BeforeEach
    void setUp() {
        paymentAppService = new PaymentAppService(payOrderMapper, orderClient);
    }

    @Test
    void mockPayByOrderNoShouldUpdateOrderStatus() {
        PayOrder payOrder = new PayOrder();
        payOrder.setId(1L);
        payOrder.setOrderId(100L);
        payOrder.setOrderNo("ORDER001");
        payOrder.setUserId(1L);
        payOrder.setPayStatus(PaymentAppService.PAY_STATUS_PENDING);
        when(payOrderMapper.selectOneByQuery(any())).thenReturn(payOrder);
        when(orderClient.updateOrderStatus(any())).thenReturn(ApiResponse.success());

        paymentAppService.mockPayByOrderNo("ORDER001", 1L);

        verify(payOrderMapper).update(any(PayOrder.class));
        verify(orderClient).updateOrderStatus(orderStatusCaptor.capture());
        assertEquals(100L, orderStatusCaptor.getValue().getOrderId());
        assertEquals(2, orderStatusCaptor.getValue().getTargetState());
    }

    @Test
    void mockPayByOrderNoShouldFailWhenOrderMissing() {
        when(payOrderMapper.selectOneByQuery(any())).thenReturn(null);

        BizException ex = assertThrows(BizException.class,
                () -> paymentAppService.mockPayByOrderNo("ORDER001", 1L));
        assertEquals(ResultCode.NOT_FOUND.getCode(), ex.getCode());
    }
}
