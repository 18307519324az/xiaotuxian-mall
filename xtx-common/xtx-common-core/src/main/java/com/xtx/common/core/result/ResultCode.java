package com.xtx.common.core.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一响应状态码枚举。
 * 定义了系统中所有标准响应码及其对应的中文描述信息。
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    /** 成功 */
    SUCCESS(20000, "success"),

    /** 请求参数错误 */
    BAD_REQUEST(40000, "请求参数错误"),

    /** 订单重复提交 */
    ORDER_DUPLICATE_SUBMIT(40020, "订单重复提交"),

    /** 订单 token 无效或已过期 */
    ORDER_TOKEN_INVALID(40021, "订单 token 无效或已过期"),

    /** 未授权，请先登录 */
    UNAUTHORIZED(40100, "未授权，请先登录"),

    /** 无权限访问 */
    FORBIDDEN(40300, "无权限访问"),

    /** 资源不存在 */
    NOT_FOUND(40400, "资源不存在"),

    /** 资源冲突 */
    CONFLICT(40900, "资源冲突"),

    /** 请求过于频繁 */
    TOO_MANY_REQUESTS(42900, "请求过于频繁"),

    /** 系统内部错误 */
    INTERNAL_ERROR(50000, "系统内部错误"),

    /** 服务不可用 */
    SERVICE_UNAVAILABLE(50300, "服务不可用");

    /** 状态码 */
    private final int code;

    /** 提示信息 */
    private final String message;
}
