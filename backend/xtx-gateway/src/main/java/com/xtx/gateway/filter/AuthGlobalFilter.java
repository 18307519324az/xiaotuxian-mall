package com.xtx.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtx.common.core.result.FrontResponse;
import com.xtx.common.security.util.JwtUtil;
import com.xtx.gateway.config.GatewaySecurityProperties;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 全局认证过滤器
 * 校验请求的 JWT 令牌，对白名单路径放行，非白名单路径进行鉴权
 */
@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    /** 白名单路径列表，从配置文件读取 */
    private final List<String> whitelist;

    /** 路径匹配器，支持 Ant 风格模式匹配 */
    private final PathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 构造器注入
     *
     * @param jwtUtil       JWT 工具类
     * @param objectMapper  JSON 处理
     * @param securityProperties 网关安全配置
     */
    public AuthGlobalFilter(JwtUtil jwtUtil, ObjectMapper objectMapper,
                            GatewaySecurityProperties securityProperties) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
        this.whitelist = securityProperties.getWhitelist();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // CORS 预检请求直接放行（OPTIONS 请求不带 Authorization 头）
        if ("OPTIONS".equals(request.getMethod().name())) {
            log.debug("CORS 预检请求放行: {}", path);
            return chain.filter(exchange);
        }

        // 检查当前路径是否在白名单中
        for (String pattern : whitelist) {
            if (pathMatcher.match(pattern, path)) {
                log.debug("白名单路径放行: {}", path);
                return chain.filter(exchange);
            }
        }

        // 获取 Authorization 请求头
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("请求缺少有效的 Authorization 头, path: {}", path);
            return unauthorizedResponse(exchange, "登录已失效，请重新登录");
        }

        // 提取 Bearer Token
        String token = authHeader.substring(7);

        try {
            // 解析 JWT 令牌
            Claims claims = jwtUtil.parseToken(token);

            // 从 Claims 中提取用户信息（userId 存储在 subject 中）
            String userId = claims.getSubject();
            String account = claims.get("account", String.class);
            String clientType = claims.get("clientType", String.class);

            // 将用户信息添加到请求头，传递给下游服务
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Account", account)
                    .header("X-Client-Type", clientType)
                    .build();

            log.debug("请求鉴权通过, userId: {}, path: {}", userId, path);
            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            log.warn("JWT 令牌解析失败: {}", e.getMessage());
            return unauthorizedResponse(exchange, "登录已失效，请重新登录");
        }
    }

    /**
     * 返回 401 未授权的 JSON 响应
     *
     * @param exchange ServerWebExchange
     * @param message  错误提示信息
     * @return Mono<Void>
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 构建 FrontResponse 错误响应
        FrontResponse<?> frontResponse = FrontResponse.failure(message);
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsString(frontResponse).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            log.error("序列化响应失败", e);
            bytes = "{\"code\":401,\"message\":\"登录已失效，请重新登录\"}".getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
