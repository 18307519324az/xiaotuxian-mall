package com.xtx.common.core.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 内部标准响应结果封装。
 * 用于服务间调用和全局异常处理时的统一响应格式。
 *
 * @param <T> 响应数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /** 状态码 */
    private int code;

    /** 提示信息 */
    private String message;

    /** 响应数据 */
    private T data;

    /** 链路追踪 ID */
    private String traceId;

    /**
     * 操作成功，无返回数据。
     */
    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    /**
     * 操作成功，带返回数据。
     *
     * @param data 返回数据
     */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(ResultCode.SUCCESS.getCode());
        response.setMessage(ResultCode.SUCCESS.getMessage());
        response.setData(data);
        return response;
    }

    /**
     * 操作失败，自定义状态码和提示信息。
     *
     * @param code    状态码
     * @param message 提示信息
     */
    public static <T> ApiResponse<T> failure(int code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setMessage(message);
        return response;
    }

    /**
     * 操作失败，使用枚举中的状态码和提示信息。
     *
     * @param rc 状态码枚举
     */
    public static <T> ApiResponse<T> failure(ResultCode rc) {
        return failure(rc.getCode(), rc.getMessage());
    }
}
