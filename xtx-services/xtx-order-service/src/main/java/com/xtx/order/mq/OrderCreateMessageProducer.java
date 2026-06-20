package com.xtx.order.mq;

import com.xtx.api.order.dto.AsyncOrderCreateMessageDTO;
import com.xtx.common.core.constant.MqConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 订单创建消息 Producer。
 * 当前仅用于 MQ 空跑验证，未接入实际下单流程。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreateMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送订单创建消息。
     *
     * @param message 订单创建消息
     */
    public void sendCreateOrderMessage(AsyncOrderCreateMessageDTO message) {
        String requestId = message.getRequestId();
        log.info("发送订单创建消息, requestId={}, orderNo={}", requestId, message.getOrderNo());

        MessagePostProcessor mpp = msg -> {
            msg.getMessageProperties().setMessageId(requestId);
            msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return msg;
        };

        try {
            rabbitTemplate.convertAndSend(
                    MqConstants.ORDER_EXCHANGE,
                    MqConstants.ORDER_CREATE_ROUTING_KEY,
                    message,
                    mpp
            );
            log.info("订单创建消息发送成功, requestId={}", requestId);
        } catch (Exception e) {
            log.error("订单创建消息发送失败, requestId={}", requestId, e);
            throw new RuntimeException("订单创建消息发送失败: " + e.getMessage(), e);
        }
    }

    /**
     * 发送重试消息到重试队列。
     *
     * @param message 订单创建消息
     */
    public void sendRetryMessage(AsyncOrderCreateMessageDTO message) {
        log.info("发送订单创建重试消息, requestId={}, orderNo={}, retryCount={}",
                message.getRequestId(), message.getOrderNo(), message.getRetryCount());

        MessagePostProcessor mpp = msg -> {
            msg.getMessageProperties().setMessageId(message.getRequestId());
            msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return msg;
        };

        rabbitTemplate.convertAndSend(
                MqConstants.ORDER_DEAD_EXCHANGE,
                MqConstants.ORDER_RETRY_ROUTING_KEY,
                message,
                mpp
        );
    }

    /**
     * 发送死信消息到死信队列。
     *
     * @param message 订单创建消息
     */
    public void sendDeadMessage(AsyncOrderCreateMessageDTO message) {
        log.info("发送订单创建死信消息, requestId={}, orderNo={}, retryCount={}",
                message.getRequestId(), message.getOrderNo(), message.getRetryCount());

        MessagePostProcessor mpp = msg -> {
            msg.getMessageProperties().setMessageId(message.getRequestId());
            msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return msg;
        };

        rabbitTemplate.convertAndSend(
                MqConstants.ORDER_DEAD_EXCHANGE,
                MqConstants.ORDER_DEAD_ROUTING_KEY,
                message,
                mpp
        );
    }
}
