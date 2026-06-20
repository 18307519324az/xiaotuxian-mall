package com.xtx.common.core.constant;

public final class MqConstants {

    private MqConstants() {}

    public static final String ORDER_EXCHANGE = "xtx.order.exchange";
    public static final String ORDER_CREATE_QUEUE = "xtx.order.create.queue";
    public static final String ORDER_CREATE_ROUTING_KEY = "xtx.order.create";

    public static final String ORDER_DEAD_EXCHANGE = "xtx.order.dead.exchange";
    public static final String ORDER_DEAD_QUEUE = "xtx.order.dead.queue";
    public static final String ORDER_DEAD_ROUTING_KEY = "xtx.order.dead";

    public static final String ORDER_RETRY_QUEUE = "xtx.order.retry.queue";
    public static final String ORDER_RETRY_ROUTING_KEY = "xtx.order.retry";
}
