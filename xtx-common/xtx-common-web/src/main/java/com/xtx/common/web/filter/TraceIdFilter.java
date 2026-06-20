package com.xtx.common.web.filter;

import com.xtx.common.core.constant.CommonConstant;
import com.xtx.common.core.util.TraceIdUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 链路追踪 ID 过滤器。
 * <p>
 * 从 HTTP 请求头 {@code X-Trace-Id} 中提取追踪 ID，为空则自动生成；
 * 将追踪 ID 放入 SLF4J MDC 供日志输出，并设置到响应头实现全链路传递。
 * 请求结束后清理 MDC 防止内存泄漏。
 * </p>
 * <p>
 * 执行顺序：最高优先级（在 {@code UserContextInterceptor} 之前执行），
 * 确保控制器和拦截器能通过 {@link TraceIdUtil#get()} 获取到追踪 ID。
 * </p>
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String traceId = request.getHeader(CommonConstant.X_TRACE_ID);
        if (traceId == null || traceId.isBlank()) {
            traceId = TraceIdUtil.generateTraceId();
        }

        TraceIdUtil.put(traceId);
        response.setHeader(CommonConstant.X_TRACE_ID, traceId);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
