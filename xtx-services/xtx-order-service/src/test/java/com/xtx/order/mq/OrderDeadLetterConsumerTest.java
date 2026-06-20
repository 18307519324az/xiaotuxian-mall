package com.xtx.order.mq;

import com.rabbitmq.client.Channel;
import com.xtx.api.order.dto.AsyncOrderCreateMessageDTO;
import com.xtx.order.service.OrderProcessStatusService;
import com.xtx.order.service.StockCompensationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderDeadLetterConsumerTest {

    @Mock OrderProcessStatusService orderProcessStatusService;
    @Mock StockCompensationService stockCompensationService;
    @Mock Channel channel;

    OrderDeadLetterConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new OrderDeadLetterConsumer(orderProcessStatusService, stockCompensationService);
    }

    private Message mockMessage(long deliveryTag) {
        Message raw = mock(Message.class);
        MessageProperties props = new MessageProperties();
        props.setDeliveryTag(deliveryTag);
        when(raw.getMessageProperties()).thenReturn(props);
        return raw;
    }

    @Test
    void deadMessageShouldCreateCompensationTaskAndMarkFailed() throws Exception {
        AsyncOrderCreateMessageDTO msg = new AsyncOrderCreateMessageDTO();
        msg.setRequestId("dead-001");
        msg.setOrderNo("ORDER001");
        msg.setRetryCount(5);
        AsyncOrderCreateMessageDTO.Item item = new AsyncOrderCreateMessageDTO.Item();
        item.setSkuId(1001L);
        item.setCount(1);
        msg.setItems(List.of(item));

        consumer.handleDeadLetter(msg, channel, mockMessage(1L));

        verify(stockCompensationService).rollbackRedisImmediatelyIfOrderMissing(msg);
        verify(stockCompensationService).createTasks(msg);
        verify(orderProcessStatusService).markFailed(eq("ORDER001"), eq("dead-001"), anyString());
        verify(channel).basicAck(1L, false);
    }

    @Test
    void nullMessageShouldAck() throws Exception {
        consumer.handleDeadLetter(null, channel, mockMessage(2L));
        verify(channel).basicAck(2L, false);
        verifyNoInteractions(orderProcessStatusService);
        verifyNoInteractions(stockCompensationService);
    }

    @Test
    void messageWithoutRequestIdShouldNotMarkFailed() throws Exception {
        AsyncOrderCreateMessageDTO msg = new AsyncOrderCreateMessageDTO();
        msg.setOrderNo("ORDER003");

        consumer.handleDeadLetter(msg, channel, mockMessage(3L));
        verify(channel).basicAck(3L, false);
        verifyNoInteractions(orderProcessStatusService);
        verifyNoInteractions(stockCompensationService);
    }

    @Test
    void exceptionDuringAckShouldStillExit() throws Exception {
        doThrow(new RuntimeException("ack failed")).doNothing().when(channel).basicAck(anyLong(), anyBoolean());

        AsyncOrderCreateMessageDTO msg = new AsyncOrderCreateMessageDTO();
        msg.setRequestId("dead-002");
        msg.setOrderNo("ORDER002");

        try {
            consumer.handleDeadLetter(msg, channel, mockMessage(4L));
        } catch (RuntimeException ignored) {
        }
    }
}
