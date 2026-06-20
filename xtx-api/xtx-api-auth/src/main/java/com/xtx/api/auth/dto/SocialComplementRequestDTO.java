package com.xtx.api.auth.dto;

import lombok.Data;

/**
 * 第三方账号补全信息请求 DTO
 * 第三方首次登录时需要补全手机号和账号信息
 */
@Data
public class SocialComplementRequestDTO {

    /** 第三方平台唯一标识 */
    private String unionId;

    /** 手机号 */
    private String mobile;

    /** 短信验证码 */
    private String code;

    /** 登录账号 */
    private String account;

    /** 昵称 */
    private String nickname;

    /** 登录密码 */
    private String password;

    /** 第三方平台类型（如：WX-微信, QQ等） */
    private String platform;

    /** 来源渠道（如：MINI_APP-小程序, H5等） */
    private Integer source;
}
