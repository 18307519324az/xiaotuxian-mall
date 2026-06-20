package com.xtx.api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 短信验证码登录请求 DTO
 */
@Data
public class SmsLoginRequestDTO {

    /** 手机号 */
    @NotBlank(message = "手机号不能为空")
    private String mobile;

    /** 短信验证码 */
    @NotBlank(message = "验证码不能为空")
    private String code;
}
