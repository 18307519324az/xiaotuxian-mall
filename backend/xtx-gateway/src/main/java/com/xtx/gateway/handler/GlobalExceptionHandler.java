package com.xtx.gateway.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtx.common.core.result.FrontResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 网关全局异常处理器
 * 统一处理网关层面发生的异常，返回标准 FrontResponse 格式
 */
@Slf4j
@Order(-1)
@Component
@RequiredArgsConstructor
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 默认错误响应
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "网关处理异常，请稍后重试";

        // 根据异常类型确定状态码和消息
        if (ex instanceof ResponseStatusException statusException) {
            httpStatus = HttpStatus.valueOf(statusException.getStatusCode().value());
            message = statusException.getReason();
        } else if (ex instanceof IllegalArgumentException) {
            httpStatus = HttpStatus.BAD_REQUEST;
            message = ex.getMessage();
        }

        log.error("网关异常处理, status: {}, message: {}", httpStatus, message, ex);

        response.setStatusCode(httpStatus);
        return writeErrorResponse(response, message);
    }

    /**
     * 写入 JSON 错误响应
     *
     * @param response ServerHttpResponse
     * @param message  错误消息
     * @return Mono<Void>
     */
    private Mono<Void> writeErrorResponse(ServerHttpResponse response, String message) {
        FrontResponse<?> frontResponse = FrontResponse.failure(message);
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsString(frontResponse).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            log.error("序列化异常响应失败", e);
            bytes = ("{\"msg\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
