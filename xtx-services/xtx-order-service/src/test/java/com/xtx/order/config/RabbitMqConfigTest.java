package com.xtx.order.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;

import static org.junit.jupiter.api.Assertions.*;

class RabbitMqConfigTest {

    private final RabbitMqConfig config = new RabbitMqConfig();

    @Test
    void orderExchangeShouldBeDirectAndDurable() {
        DirectExchange exchange = config.orderExchange();
        assertTrue(exchange.isDurable());
        assertFalse(exchange.isAutoDelete());
        assertEquals("xtx.order.exchange", exchange.getName());
    }

    @Test
    void orderDeadExchangeShouldBeDirectAndDurable() {
        DirectExchange exchange = config.orderDeadExchange();
        assertTrue(exchange.isDurable());
        assertEquals("xtx.order.dead.exchange", exchange.getName());
    }

    @Test
    void orderCreateQueueShouldBeDurableWithDeadLetter() {
        Queue queue = config.orderCreateQueue();
        assertTrue(queue.isDurable());
        assertEquals("xtx.order.create.queue", queue.getName());
        assertNotNull(queue.getArguments().get("x-dead-letter-exchange"));
        assertEquals("xtx.order.dead.exchange", queue.getArguments().get("x-dead-letter-exchange"));
    }

    @Test
    void orderDeadQueueShouldBeDurable() {
        Queue queue = config.orderDeadQueue();
        assertTrue(queue.isDurable());
        assertEquals("xtx.order.dead.queue", queue.getName());
    }

    @Test
    void orderRetryQueueShouldHaveTtlAndDeadLetter() {
        Queue queue = config.orderRetryQueue();
        assertTrue(queue.isDurable());
        assertEquals("xtx.order.retry.queue", queue.getName());
        assertEquals(30000, queue.getArguments().get("x-message-ttl"));
        assertEquals("xtx.order.exchange", queue.getArguments().get("x-dead-letter-exchange"));
        assertEquals("xtx.order.create", queue.getArguments().get("x-dead-letter-routing-key"));
    }

    @Test
    void bindingsShouldNotBeNull() {
        assertNotNull(config.orderCreateBinding());
        assertNotNull(config.orderDeadBinding());
        assertNotNull(config.orderRetryBinding());
    }

    @Test
    void bindingDestinationsShouldMatch() {
        Binding createBinding = config.orderCreateBinding();
        assertEquals("xtx.order.create.queue", createBinding.getDestination());
        assertEquals("xtx.order.create", createBinding.getRoutingKey());

        Binding deadBinding = config.orderDeadBinding();
        assertEquals("xtx.order.dead.queue", deadBinding.getDestination());
        assertEquals("xtx.order.dead", deadBinding.getRoutingKey());

        Binding retryBinding = config.orderRetryBinding();
        assertEquals("xtx.order.retry.queue", retryBinding.getDestination());
        assertEquals("xtx.order.retry", retryBinding.getRoutingKey());
    }
}
