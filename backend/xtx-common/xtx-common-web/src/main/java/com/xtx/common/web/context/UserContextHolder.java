package com.xtx.common.web.context;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户上下文持有者。
 * 基于 ThreadLocal 存储每个请求线程的用户信息，包括用户 ID、账号、客户端类型和追踪 ID。
 * 请求结束后必须在拦截器或过滤器中调用 {@link #clear()} 释放资源，防止内存泄漏。
 */
public class UserContextHolder {

    private static final ThreadLocal<Map<String, Object>> threadLocal =
            ThreadLocal.withInitial(HashMap::new);

    /** 用户 ID 键 */
    private static final String KEY_USER_ID = "userId";

    /** 用户账号键 */
    private static final String KEY_ACCOUNT = "account";

    /** 客户端类型键 */
    private static final String KEY_CLIENT_TYPE = "clientType";

    /** 链路追踪 ID 键 */
    private static final String KEY_TRACE_ID = "traceId";

    /**
     * 设置当前线程的用户 ID。
     *
     * @param userId 用户 ID
     */
    public static void setUserId(Long userId) {
        threadLocal.get().put(KEY_USER_ID, userId);
    }

    /**
     * 获取当前线程的用户 ID。
     *
     * @return 用户 ID，可能为 null
     */
    public static Long getUserId() {
        return (Long) threadLocal.get().get(KEY_USER_ID);
    }

    /**
     * 设置当前线程的用户账号。
     *
     * @param account 用户账号
     */
    public static void setAccount(String account) {
        threadLocal.get().put(KEY_ACCOUNT, account);
    }

    /**
     * 获取当前线程的用户账号。
     *
     * @return 用户账号，可能为 null
     */
    public static String getAccount() {
        return (String) threadLocal.get().get(KEY_ACCOUNT);
    }

    /**
     * 设置当前线程的客户端类型。
     *
     * @param clientType 客户端类型（APP / H5 / PC）
     */
    public static void setClientType(String clientType) {
        threadLocal.get().put(KEY_CLIENT_TYPE, clientType);
    }

    /**
     * 获取当前线程的客户端类型。
     *
     * @return 客户端类型，可能为 null
     */
    public static String getClientType() {
        return (String) threadLocal.get().get(KEY_CLIENT_TYPE);
    }

    /**
     * 设置当前线程的链路追踪 ID。
     *
     * @param traceId 链路追踪 ID
     */
    public static void setTraceId(String traceId) {
        threadLocal.get().put(KEY_TRACE_ID, traceId);
    }

    /**
     * 获取当前线程的链路追踪 ID。
     *
     * @return 链路追踪 ID，可能为 null
     */
    public static String getTraceId() {
        return (String) threadLocal.get().get(KEY_TRACE_ID);
    }

    /**
     * 清除当前线程的上下文数据。
     * 务必在请求完成后调用，避免 ThreadLocal 内存泄漏。
     */
    public static void clear() {
        threadLocal.remove();
    }
}
