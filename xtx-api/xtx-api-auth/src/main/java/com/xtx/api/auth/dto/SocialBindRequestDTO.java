package com.xtx.api.auth.dto;

import lombok.Data;

/**
 * 第三方账号绑定请求 DTO
 * 将第三方登录账号与现有手机号进行绑定
 */
@Data
public class SocialBindRequestDTO {

    /** 第三方平台唯一标识 */
    private String unionId;

    /** 手机号 */
    private String mobile;

    /** 短信验证码 */
    private String code;

    /** 第三方平台类型（如：WX-微信, QQ等） */
    private String platform;

    /** 来源渠道（如：MINI_APP-小程序, H5等） */
    private Integer source;
}
