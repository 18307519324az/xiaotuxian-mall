package com.xtx.gateway.filter;

import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 请求链路追踪过滤器
 * 为每次请求生成或传递 X-Trace-Id，用于日志链路追踪
 */
@Slf4j
@Component
public class RequestTraceFilter implements GlobalFilter, Ordered {

    /** 链路追踪 ID 请求头名称 */
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 尝试从请求头中获取 TraceId
        String traceId = request.getHeaders().getFirst(TRACE_ID_HEADER);

        // 如果请求头中不存在，则生成新的 TraceId
        if (traceId == null || traceId.isEmpty()) {
            traceId = IdUtil.fastSimpleUUID();
            log.debug("生成新 TraceId: {}", traceId);
        } else {
            log.debug("复用请求 TraceId: {}", traceId);
        }

        // 将 TraceId 添加到下游请求头中
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(TRACE_ID_HEADER, traceId)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }
}
