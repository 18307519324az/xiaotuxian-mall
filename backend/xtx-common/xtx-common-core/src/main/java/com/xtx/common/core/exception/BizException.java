package com.xtx.common.core.exception;

import com.xtx.common.core.result.ResultCode;
import lombok.Getter;

/**
 * 业务异常。
 * 在业务逻辑层抛出，由全局异常处理器统一捕获并转换为标准响应返回。
 */
@Getter
public class BizException extends RuntimeException {

    /** 业务异常状态码 */
    private final int code;

    /** 业务异常提示信息 */
    private final String message;

    /**
     * 使用状态码枚举构造业务异常。
     *
     * @param rc 状态码枚举
     */
    public BizException(ResultCode rc) {
        super(rc.getMessage());
        this.code = rc.getCode();
        this.message = rc.getMessage();
    }

    /**
     * 使用自定义状态码和提示信息构造业务异常。
     *
     * @param code    状态码
     * @param message 提示信息
     */
    public BizException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
