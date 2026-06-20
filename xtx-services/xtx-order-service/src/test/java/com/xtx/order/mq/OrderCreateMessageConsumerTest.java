package com.xtx.order.mq;

import com.rabbitmq.client.Channel;
import com.xtx.api.order.dto.AsyncOrderCreateMessageDTO;
import com.xtx.order.service.OrderCreateTransactionService;
import com.xtx.order.service.OrderProcessStatusService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderCreateMessageConsumerTest {

    @Mock OrderCreateTransactionService orderCreateTransactionService;
    @Mock OrderProcessStatusService orderProcessStatusService;
    @Mock OrderCreateMessageProducer orderCreateMessageProducer;
    @Mock Channel channel;

    OrderCreateMessageConsumer consumer;

    @Captor ArgumentCaptor<AsyncOrderCreateMessageDTO> messageCaptor;

    @BeforeEach
    void setUp() {
        consumer = new OrderCreateMessageConsumer(
                orderCreateTransactionService, orderProcessStatusService, orderCreateMessageProducer);
    }

    private AsyncOrderCreateMessageDTO validMessage() {
        AsyncOrderCreateMessageDTO msg = new AsyncOrderCreateMessageDTO();
        msg.setRequestId("req-001");
        msg.setOrderNo("ORDER001");
        msg.setUserId(1L);
        msg.setAddressId(10L);
        msg.setTotalAmount(new BigDecimal("99.00"));
        msg.setRetryCount(0);
        AsyncOrderCreateMessageDTO.Item item = new AsyncOrderCreateMessageDTO.Item();
        item.setSkuId(1001L);
        item.setCount(1);
        item.setPrice(new BigDecimal("99.00"));
        msg.setItems(List.of(item));
        return msg;
    }

    private Message mockMessage(long deliveryTag) {
        Message raw = mock(Message.class);
        MessageProperties props = new MessageProperties();
        props.setDeliveryTag(deliveryTag);
        when(raw.getMessageProperties()).thenReturn(props);
        return raw;
    }

    @Test
    void validMessageShouldCreateOrderAndMarkSuccess() throws Exception {
        when(orderCreateTransactionService.createOrderAfterRedisPreDeduct(any())).thenReturn(100L);

        consumer.handleCreateOrder(validMessage(), channel, mockMessage(1L));

        verify(orderCreateTransactionService).createOrderAfterRedisPreDeduct(any());
        verify(orderProcessStatusService).markSuccess(eq("ORDER001"), eq("req-001"), eq(100L));
        verify(channel).basicAck(1L, false);
        verifyNoInteractions(orderCreateMessageProducer);
    }

    @Test
    void nullRequestIdShouldAck() throws Exception {
        AsyncOrderCreateMessageDTO msg = validMessage();
        msg.setRequestId(null);
        consumer.handleCreateOrder(msg, channel, mockMessage(2L));
        verify(channel).basicAck(2L, false);
        verifyNoInteractions(orderCreateTransactionService);
    }

    @Test
    void blankRequestIdShouldAck() throws Exception {
        AsyncOrderCreateMessageDTO msg = validMessage();
        msg.setRequestId("   ");
        consumer.handleCreateOrder(msg, channel, mockMessage(3L));
        verify(channel).basicAck(3L, false);
        verifyNoInteractions(orderCreateTransactionService);
    }

    @Test
    void nullUserIdShouldAck() throws Exception {
        AsyncOrderCreateMessageDTO msg = validMessage();
        msg.setUserId(null);
        consumer.handleCreateOrder(msg, channel, mockMessage(4L));
        verify(channel).basicAck(4L, false);
        verifyNoInteractions(orderCreateTransactionService);
    }

    @Test
    void nullItemsShouldAck() throws Exception {
        AsyncOrderCreateMessageDTO msg = validMessage();
        msg.setItems(null);
        consumer.handleCreateOrder(msg, channel, mockMessage(5L));
        verify(channel).basicAck(5L, false);
        verifyNoInteractions(orderCreateTransactionService);
    }

    @Test
    void emptyItemsShouldAck() throws Exception {
        AsyncOrderCreateMessageDTO msg = validMessage();
        msg.setItems(List.of());
        consumer.handleCreateOrder(msg, channel, mockMessage(6L));
        verify(channel).basicAck(6L, false);
        verifyNoInteractions(orderCreateTransactionService);
    }

    @Test
    void firstFailureShouldSendRetry() throws Exception {
        when(orderCreateTransactionService.createOrderAfterRedisPreDeduct(any()))
                .thenThrow(new RuntimeException("DB异常"));

        consumer.handleCreateOrder(validMessage(), channel, mockMessage(10L));

        verify(orderCreateMessageProducer).sendRetryMessage(messageCaptor.capture());
        verify(orderCreateMessageProducer, never()).sendDeadMessage(any(AsyncOrderCreateMessageDTO.class));
        verifyNoInteractions(orderProcessStatusService);
        Assertions.assertEquals(1, messageCaptor.getValue().getRetryCount());
        verify(channel).basicAck(10L, false);
    }

    @Test
    void maxRetryFailureShouldSendDead() throws Exception {
        AsyncOrderCreateMessageDTO msg = validMessage();
        msg.setRetryCount(3);
        when(orderCreateTransactionService.createOrderAfterRedisPreDeduct(any()))
                .thenThrow(new RuntimeException("DB异常"));

        consumer.handleCreateOrder(msg, channel, mockMessage(11L));

        verify(orderCreateMessageProducer).sendDeadMessage(messageCaptor.capture());
        verify(orderCreateMessageProducer, never()).sendRetryMessage(any(AsyncOrderCreateMessageDTO.class));
        verifyNoInteractions(orderProcessStatusService);
        Assertions.assertEquals(4, messageCaptor.getValue().getRetryCount());
        verify(channel).basicAck(11L, false);
    }

    @Test
    void nullMessageShouldAck() throws Exception {
        consumer.handleCreateOrder(null, channel, mockMessage(20L));
        verify(channel).basicAck(20L, false);
        verifyNoInteractions(orderCreateTransactionService);
    }
}
