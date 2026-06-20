package com.xtx.common.web.handler;

import com.xtx.common.core.exception.BizException;
import com.xtx.common.core.result.ApiResponse;
import com.xtx.common.core.result.ResultCode;
import com.xtx.common.core.util.TraceIdUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器。
 * 统一捕获应用各层抛出的异常，转换为 {@link ApiResponse} 标准响应格式返回给客户端。
 * v1.7: 所有异常响应包含 traceId 链路追踪 ID。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常。
     */
    @ExceptionHandler(BizException.class)
    public ApiResponse<Void> handleBizException(BizException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        ApiResponse<Void> response = ApiResponse.failure(e.getCode(), e.getMessage());
        response.setTraceId(TraceIdUtil.get());
        return response;
    }

    /**
     * 处理请求参数校验失败异常（@Valid 或 @Validated）。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", msg);
        ApiResponse<Void> response = ApiResponse.failure(ResultCode.BAD_REQUEST.getCode(), msg);
        response.setTraceId(TraceIdUtil.get());
        return response;
    }

    /**
     * 处理单个参数校验失败异常（@RequestParam + @NotEmpty 等）。
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<Void> handleConstraintViolation(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数约束违规: {}", msg);
        ApiResponse<Void> response = ApiResponse.failure(ResultCode.BAD_REQUEST.getCode(), msg);
        response.setTraceId(TraceIdUtil.get());
        return response;
    }

    /**
     * 处理请求体不可读异常（JSON 格式错误等）。
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<Void> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体不可读: {}", e.getMessage());
        ApiResponse<Void> response = ApiResponse.failure(ResultCode.BAD_REQUEST.getCode(), "请求体格式错误");
        response.setTraceId(TraceIdUtil.get());
        return response;
    }

    /**
     * 处理缺少必填请求参数异常。
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ApiResponse<Void> handleMissingServletRequestParameter(
            MissingServletRequestParameterException e) {
        log.warn("缺少请求参数: {}", e.getParameterName());
        ApiResponse<Void> response = ApiResponse.failure(ResultCode.BAD_REQUEST.getCode(),
                "缺少必填参数: " + e.getParameterName());
        response.setTraceId(TraceIdUtil.get());
        return response;
    }

    /**
     * 处理 HTTP 状态异常（如 401 未授权、404 未找到）。
     * 保持原始 HTTP 状态码，让前端 Axios 拦截器能正确识别并处理（如跳转登录页）。
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatus(ResponseStatusException e) {
        String traceId = TraceIdUtil.get();
        if (traceId == null) {
            traceId = TraceIdUtil.generateTraceId();
        }
        log.warn("HTTP {}: {}", e.getStatusCode(), e.getReason());
        ApiResponse<Void> response = ApiResponse.failure(e.getStatusCode().value(), e.getReason());
        response.setTraceId(traceId);
        return ResponseEntity.status(e.getStatusCode()).body(response);
    }

    /**
     * 处理未捕获的系统级异常（兜底处理）。
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e, HttpServletRequest request) {
        String traceId = TraceIdUtil.get();
        if (traceId == null) {
            traceId = TraceIdUtil.generateTraceId();
        }
        log.error("系统异常 traceId={}, uri={}", traceId, request.getRequestURI(), e);
        ApiResponse<Void> response = ApiResponse.failure(ResultCode.INTERNAL_ERROR);
        response.setTraceId(traceId);
        response.setMessage("系统繁忙，请稍后重试");
        return response;
    }
}
