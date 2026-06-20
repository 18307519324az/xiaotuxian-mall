package com.xtx.order.mq;

import com.rabbitmq.client.Channel;
import com.xtx.api.order.dto.AsyncOrderCreateMessageDTO;
import com.xtx.common.core.constant.MqConstants;
import com.xtx.order.service.OrderProcessStatusService;
import com.xtx.order.service.StockCompensationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderDeadLetterConsumer {

    private final OrderProcessStatusService orderProcessStatusService;
    private final StockCompensationService stockCompensationService;

    @RabbitListener(queues = MqConstants.ORDER_DEAD_QUEUE)
    public void handleDeadLetter(AsyncOrderCreateMessageDTO message, Channel channel, Message raw) throws IOException {
        long deliveryTag = raw.getMessageProperties().getDeliveryTag();
        try {
            if (message != null && message.getRequestId() != null && message.getOrderNo() != null) {
                log.error("【MQ 死信】收到死信消息, requestId={}, orderNo={}, userId={}, retryCount={}",
                        message.getRequestId(), message.getOrderNo(), message.getUserId(), message.getRetryCount());
                stockCompensationService.rollbackRedisImmediatelyIfOrderMissing(message);
                stockCompensationService.createTasks(message);
                orderProcessStatusService.markFailed(message.getOrderNo(), message.getRequestId(), "超过最大重试次数");
            } else {
                log.error("【MQ 死信】收到无效死信消息, requestId={}, orderNo={}",
                        message != null ? message.getRequestId() : "null",
                        message != null ? message.getOrderNo() : "null");
            }
        } catch (Exception e) {
            log.error("死信消息处理异常, deliveryTag={}", deliveryTag, e);
        } finally {
            channel.basicAck(deliveryTag, false);
        }
    }
}
