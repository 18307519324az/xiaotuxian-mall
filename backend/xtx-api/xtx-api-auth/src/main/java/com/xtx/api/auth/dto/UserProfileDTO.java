package com.xtx.api.auth.dto;

import lombok.Data;

/**
 * 用户信息 DTO
 * 登录成功后返回的用户基本信息与令牌
 */
@Data
public class UserProfileDTO {

    /** 用户 ID */
    private Long id;

    /** 登录账号 */
    private String account;

    /** 头像地址 */
    private String avatar;

    /** 手机号 */
    private String mobile;

    /** 昵称 */
    private String nickname;

    /** 认证令牌 */
    private String token;

    /** 刷新令牌 */
    private String refreshToken;
}
