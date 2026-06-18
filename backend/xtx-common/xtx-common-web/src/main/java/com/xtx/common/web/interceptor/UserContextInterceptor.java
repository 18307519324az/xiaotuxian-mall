package com.xtx.common.web.interceptor;

import cn.hutool.core.util.StrUtil;
import com.xtx.common.core.constant.CommonConstant;
import com.xtx.common.web.context.UserContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户上下文拦截器。
 * 从 HTTP 请求头中提取用户信息（用户 ID、账号、客户端类型、追踪 ID），
 * 并填充到 {@link UserContextHolder} 和 SLF4J MDC 中，实现全链路上下文传递。
 */
public class UserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        // 提取并设置用户 ID
        String userId = request.getHeader(CommonConstant.X_USER_ID);
        if (StrUtil.isNotBlank(userId)) {
            UserContextHolder.setUserId(Long.valueOf(userId));
            MDC.put(CommonConstant.X_USER_ID, userId);
        }

        // 提取并设置用户账号
        String account = request.getHeader(CommonConstant.X_USER_ACCOUNT);
        if (StrUtil.isNotBlank(account)) {
            UserContextHolder.setAccount(account);
            MDC.put(CommonConstant.X_USER_ACCOUNT, account);
        }

        // 提取并设置客户端类型
        String clientType = request.getHeader(CommonConstant.X_CLIENT_TYPE);
        if (StrUtil.isNotBlank(clientType)) {
            UserContextHolder.setClientType(clientType);
            MDC.put(CommonConstant.X_CLIENT_TYPE, clientType);
        }

        // 提取链路追踪 ID，为空则使用 TraceIdFilter 已设置的 MDC 值，再为空则自动生成
        String traceId = request.getHeader(CommonConstant.X_TRACE_ID);
        if (StrUtil.isBlank(traceId)) {
            traceId = MDC.get(CommonConstant.TRACE_ID);
            if (StrUtil.isBlank(traceId)) {
                traceId = com.xtx.common.core.util.TraceIdUtil.generateTraceId();
            }
        }
        UserContextHolder.setTraceId(traceId);
        MDC.put(CommonConstant.TRACE_ID, traceId);
        response.setHeader(CommonConstant.X_TRACE_ID, traceId);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 请求结束后清理上下文，防止内存泄漏
        UserContextHolder.clear();
        MDC.clear();
    }
}
