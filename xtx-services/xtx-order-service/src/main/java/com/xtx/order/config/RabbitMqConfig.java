package com.xtx.order.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtx.common.core.constant.MqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMqConfig {

    @Bean
    public MessageConverter rabbitMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter rabbitMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(rabbitMessageConverter);
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory,
            MessageConverter rabbitMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(rabbitMessageConverter);
        return factory;
    }

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(MqConstants.ORDER_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange orderDeadExchange() {
        return new DirectExchange(MqConstants.ORDER_DEAD_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderCreateQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", MqConstants.ORDER_DEAD_EXCHANGE);
        args.put("x-dead-letter-routing-key", MqConstants.ORDER_DEAD_ROUTING_KEY);
        return QueueBuilder.durable(MqConstants.ORDER_CREATE_QUEUE).withArguments(args).build();
    }

    @Bean
    public Binding orderCreateBinding() {
        return BindingBuilder.bind(orderCreateQueue())
                .to(orderExchange())
                .with(MqConstants.ORDER_CREATE_ROUTING_KEY);
    }

    @Bean
    public Queue orderDeadQueue() {
        return QueueBuilder.durable(MqConstants.ORDER_DEAD_QUEUE).build();
    }

    @Bean
    public Binding orderDeadBinding() {
        return BindingBuilder.bind(orderDeadQueue())
                .to(orderDeadExchange())
                .with(MqConstants.ORDER_DEAD_ROUTING_KEY);
    }

    @Bean
    public Queue orderRetryQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", 30000);
        args.put("x-dead-letter-exchange", MqConstants.ORDER_EXCHANGE);
        args.put("x-dead-letter-routing-key", MqConstants.ORDER_CREATE_ROUTING_KEY);
        return QueueBuilder.durable(MqConstants.ORDER_RETRY_QUEUE).withArguments(args).build();
    }

    @Bean
    public Binding orderRetryBinding() {
        return BindingBuilder.bind(orderRetryQueue())
                .to(orderDeadExchange())
                .with(MqConstants.ORDER_RETRY_ROUTING_KEY);
    }
}
