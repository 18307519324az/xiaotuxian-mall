package com.xtx.order.mq;

import com.xtx.api.order.dto.AsyncOrderCreateMessageDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import org.springframework.amqp.core.MessageDeliveryMode;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderCreateMessageProducerTest {

    @Mock RabbitTemplate rabbitTemplate;
    @Captor ArgumentCaptor<MessagePostProcessor> mppCaptor;

    OrderCreateMessageProducer producer;

    @BeforeEach
    void setUp() {
        producer = new OrderCreateMessageProducer(rabbitTemplate);
    }

    private AsyncOrderCreateMessageDTO createMessage() {
        AsyncOrderCreateMessageDTO msg = new AsyncOrderCreateMessageDTO();
        msg.setRequestId("req-001");
        msg.setOrderNo("ORDER2026061900001");
        msg.setUserId(1L);
        return msg;
    }

    @Test
    void sendCreateOrderShouldUseCorrectExchangeAndRoutingKey() {
        AsyncOrderCreateMessageDTO msg = createMessage();
        producer.sendCreateOrderMessage(msg);

        verify(rabbitTemplate).convertAndSend(
                eq("xtx.order.exchange"),
                eq("xtx.order.create"),
                eq(msg),
                isA(MessagePostProcessor.class)
        );
    }

    @Test
    void sendCreateOrderShouldSetMessageId() {
        producer.sendCreateOrderMessage(createMessage());

        verify(rabbitTemplate).convertAndSend(
                anyString(), anyString(), any(Object.class),
                mppCaptor.capture());

        MessagePostProcessor mpp = mppCaptor.getValue();
        org.springframework.amqp.core.Message mockMessage = mock(org.springframework.amqp.core.Message.class);
        org.springframework.amqp.core.MessageProperties props = new org.springframework.amqp.core.MessageProperties();

        when(mockMessage.getMessageProperties()).thenReturn(props);
        mpp.postProcessMessage(mockMessage);

        assertEquals("req-001", props.getMessageId());
        assertEquals(MessageDeliveryMode.PERSISTENT, props.getDeliveryMode());
    }

    @Test
    void sendCreateOrderShouldThrowOnFailure() {
        doThrow(new RuntimeException("connection lost"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class), isA(MessagePostProcessor.class));

        assertThrows(RuntimeException.class, () -> producer.sendCreateOrderMessage(createMessage()));
    }

    @Test
    void sendRetryMessageShouldUseDeadExchangeAndRetryRoutingKey() {
        AsyncOrderCreateMessageDTO msg = createMessage();
        msg.setRetryCount(1);
        producer.sendRetryMessage(msg);

        verify(rabbitTemplate).convertAndSend(
                eq("xtx.order.dead.exchange"),
                eq("xtx.order.retry"),
                eq(msg),
                isA(MessagePostProcessor.class)
        );
    }

    @Test
    void sendDeadMessageShouldUseDeadExchangeAndDeadRoutingKey() {
        AsyncOrderCreateMessageDTO msg = createMessage();
        msg.setRetryCount(3);
        producer.sendDeadMessage(msg);

        verify(rabbitTemplate).convertAndSend(
                eq("xtx.order.dead.exchange"),
                eq("xtx.order.dead"),
                eq(msg),
                isA(MessagePostProcessor.class)
        );
    }
}
