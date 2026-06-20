package com.xtx.common.core.constant;

/**
 * 系统通用常量。
 * 定义请求头常量名称，贯穿网关、微服务间调用及业务逻辑的全链路。
 */
public interface CommonConstant {

    /** HTTP 请求头：用户 ID */
    String X_USER_ID = "X-User-Id";

    /** HTTP 请求头：用户账号 */
    String X_USER_ACCOUNT = "X-User-Account";

    /** HTTP 请求头：客户端类型（APP / H5 / PC） */
    String X_CLIENT_TYPE = "X-Client-Type";

    /** HTTP 请求头：链路追踪 ID */
    String X_TRACE_ID = "X-Trace-Id";

    /** MDC 中存储链路追踪 ID 的键名 */
    String TRACE_ID = "traceId";
}
