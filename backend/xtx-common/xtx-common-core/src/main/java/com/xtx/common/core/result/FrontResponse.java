package com.xtx.common.core.result;

import com.xtx.common.core.util.TraceIdUtil;

/**
 * 前端兼容响应结果封装。
 * 与前端约定的统一响应格式，字段命名贴近前端习惯。
 * v1.7 新增 code 和 traceId 字段，实现全链路追踪。
 *
 * @param <T> 响应数据类型
 */
public class FrontResponse<T> {

    /** 状态码 */
    private String code;

    /** 提示信息 */
    private String msg;

    /** 链路追踪 ID */
    private String traceId;

    /** 响应结果数据 */
    private T result;

    public FrontResponse() {}

    public FrontResponse(String code, String msg, String traceId, T result) {
        this.code = code;
        this.msg = msg;
        this.traceId = traceId;
        this.result = result;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public T getResult() { return result; }
    public void setResult(T result) { this.result = result; }

    /**
     * 操作成功，无返回数据。
     */
    public static <T> FrontResponse<T> success() {
        FrontResponse<T> resp = new FrontResponse<>();
        resp.setCode(String.valueOf(ResultCode.SUCCESS.getCode()));
        resp.setMsg("成功");
        resp.setTraceId(TraceIdUtil.get());
        return resp;
    }

    /**
     * 操作成功，带返回结果。
     *
     * @param result 返回结果数据
     */
    public static <T> FrontResponse<T> success(T result) {
        FrontResponse<T> resp = new FrontResponse<>();
        resp.setCode(String.valueOf(ResultCode.SUCCESS.getCode()));
        resp.setMsg("成功");
        resp.setTraceId(TraceIdUtil.get());
        resp.setResult(result);
        return resp;
    }

    /**
     * 操作失败，带错误提示信息。
     *
     * @param msg 错误提示信息
     */
    public static <T> FrontResponse<T> failure(String msg) {
        FrontResponse<T> resp = new FrontResponse<>();
        resp.setCode(String.valueOf(ResultCode.INTERNAL_ERROR.getCode()));
        resp.setMsg(msg);
        resp.setTraceId(TraceIdUtil.get());
        return resp;
    }

    /**
     * 操作失败，带状态码和错误提示信息。
     *
     * @param code 状态码
     * @param msg  错误提示信息
     */
    public static <T> FrontResponse<T> failure(int code, String msg) {
        FrontResponse<T> resp = new FrontResponse<>();
        resp.setCode(String.valueOf(code));
        resp.setMsg(msg);
        resp.setTraceId(TraceIdUtil.get());
        return resp;
    }

    /**
     * 操作失败，使用枚举中的状态码和提示信息。
     *
     * @param rc 状态码枚举
     */
    public static <T> FrontResponse<T> failure(ResultCode rc) {
        return failure(rc.getCode(), rc.getMessage());
    }
}
