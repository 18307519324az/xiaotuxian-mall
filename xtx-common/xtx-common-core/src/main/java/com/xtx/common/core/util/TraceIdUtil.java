package com.xtx.common.core.util;

import cn.hutool.core.util.IdUtil;
import com.xtx.common.core.constant.CommonConstant;
import org.slf4j.MDC;

/**
 * 链路追踪 ID 工具类。
 * 基于 Hutool 的 IdUtil 生成全局唯一的追踪 ID，并操作 SLF4J MDC 实现全链路日志串联。
 */
public class TraceIdUtil {

    /**
     * 生成全局唯一链路追踪 ID。
     * 使用 Hutool 的 fastSimpleUUID，性能更优。
     *
     * @return 追踪 ID 字符串
     */
    public static String generateTraceId() {
        return IdUtil.fastSimpleUUID();
    }

    /**
     * 将追踪 ID 放入 MDC。
     *
     * @param traceId 追踪 ID
     */
    public static void put(String traceId) {
        MDC.put(CommonConstant.TRACE_ID, traceId);
    }

    /**
     * 从 MDC 获取当前线程的追踪 ID。
     *
     * @return 追踪 ID，可能为 null
     */
    public static String get() {
        return MDC.get(CommonConstant.TRACE_ID);
    }

    /**
     * 清除 MDC 中的追踪 ID。
     */
    public static void clear() {
        MDC.remove(CommonConstant.TRACE_ID);
    }
}
