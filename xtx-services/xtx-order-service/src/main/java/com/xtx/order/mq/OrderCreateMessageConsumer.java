package com.xtx.order.mq;

import com.rabbitmq.client.Channel;
import com.xtx.api.order.dto.AsyncOrderCreateMessageDTO;
import com.xtx.common.core.constant.MqConstants;
import com.xtx.order.service.OrderCreateTransactionService;
import com.xtx.order.service.OrderProcessStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreateMessageConsumer {

    static final int MAX_RETRY_COUNT = 3;

    private final OrderCreateTransactionService orderCreateTransactionService;
    private final OrderProcessStatusService orderProcessStatusService;
    private final OrderCreateMessageProducer orderCreateMessageProducer;

    @RabbitListener(queues = MqConstants.ORDER_CREATE_QUEUE)
    public void handleCreateOrder(AsyncOrderCreateMessageDTO message, Channel channel, Message raw) throws IOException {
        long deliveryTag = raw.getMessageProperties().getDeliveryTag();
        try {
            if (message == null || message.getRequestId() == null || message.getRequestId().isBlank()) {
                log.warn("订单创建消息 requestId 为空，直接 ack");
                channel.basicAck(deliveryTag, false);
                return;
            }
            if (message.getUserId() == null) {
                log.warn("订单创建消息 userId 为空，requestId={}，直接 ack", message.getRequestId());
                channel.basicAck(deliveryTag, false);
                return;
            }
            if (message.getItems() == null || message.getItems().isEmpty()) {
                log.warn("订单创建消息 items 为空，requestId={}，直接 ack", message.getRequestId());
                channel.basicAck(deliveryTag, false);
                return;
            }

            log.info("开始异步创建订单，requestId={}, orderNo={}, userId={}, items={}",
                    message.getRequestId(), message.getOrderNo(), message.getUserId(), message.getItems().size());
            Long orderId = orderCreateTransactionService.createOrderAfterRedisPreDeduct(message);

            orderProcessStatusService.markSuccess(message.getOrderNo(), message.getRequestId(), orderId);
            log.info("异步订单创建成功，orderNo={}, orderId={}, requestId={}",
                    message.getOrderNo(), orderId, message.getRequestId());

            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("异步订单创建失败，requestId={}, orderNo={}",
                    message != null ? message.getRequestId() : "null",
                    message != null ? message.getOrderNo() : "null", e);
            handleFailure(message, channel, deliveryTag);
        }
    }

    private void handleFailure(AsyncOrderCreateMessageDTO message, Channel channel, long deliveryTag) throws IOException {
        if (message == null || message.getRequestId() == null || message.getOrderNo() == null) {
            channel.basicAck(deliveryTag, false);
            return;
        }

        int retryCount = message.getRetryCount() != null ? message.getRetryCount() : 0;
        message.setRetryCount(retryCount + 1);

        try {
            if (retryCount < MAX_RETRY_COUNT) {
                orderCreateMessageProducer.sendRetryMessage(message);
                log.warn("异步订单创建失败，已投递重试队列，requestId={}, orderNo={}, retryCount={}",
                        message.getRequestId(), message.getOrderNo(), message.getRetryCount());
            } else {
                orderCreateMessageProducer.sendDeadMessage(message);
                log.error("异步订单创建失败，已投递死信队列，requestId={}, orderNo={}, retryCount={}",
                        message.getRequestId(), message.getOrderNo(), message.getRetryCount());
            }
        } catch (Exception sendEx) {
            log.error("异步订单失败后投递重试/死信队列失败，requestId={}, orderNo={}",
                    message.getRequestId(), message.getOrderNo(), sendEx);
            orderProcessStatusService.markFailed(message.getOrderNo(), message.getRequestId(), "MQ消息处理失败");
        }

        channel.basicAck(deliveryTag, false);
    }
}
