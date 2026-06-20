package com.xtx.auth.service;

import com.xtx.api.auth.dto.LoginRequestDTO;
import com.xtx.api.auth.dto.SmsLoginRequestDTO;
import com.xtx.api.auth.dto.SocialBindRequestDTO;
import com.xtx.api.auth.dto.SocialComplementRequestDTO;
import com.xtx.api.auth.dto.UserProfileDTO;

import java.util.Map;

/**
 * 登录服务接口
 * 提供账号密码登录、短信验证码登录、第三方社交登录等功能
 */
public interface LoginService {

    /**
     * 账号密码登录
     *
     * @param request 登录请求参数
     * @return 用户认证信息（含访问令牌、刷新令牌和用户资料）
     */
    UserProfileDTO login(LoginRequestDTO request);

    /**
     * 发送短信验证码
     *
     * @param mobile 手机号
     * @param type   验证码类型：LOGIN-登录，REGISTER-注册，BIND-绑定
     */
    void sendSmsCode(String mobile, String type);

    /**
     * 短信验证码登录
     *
     * @param request 短信登录请求参数
     * @return 用户认证信息
     */
    UserProfileDTO smsLogin(SmsLoginRequestDTO request);

    /**
     * 第三方社交账号登录
     *
     * @param unionId 第三方平台唯一标识
     * @param source  来源渠道
     * @return 用户认证信息
     */
    UserProfileDTO socialLogin(String unionId, Integer source);

    /**
     * 绑定手机号并完成社交登录
     *
     * @param request 社交绑定请求参数
     * @return 用户认证信息
     */
    UserProfileDTO socialBind(SocialBindRequestDTO request);

    /**
     * 社交登录信息补全（首次登录需补充手机号等信息）
     *
     * @param unionId 第三方平台唯一标识
     * @param request 信息补全请求参数
     * @return 用户认证信息
     */
    UserProfileDTO socialComplement(String unionId, SocialComplementRequestDTO request);

    /**
     * 校验账号是否可用
     *
     * @param account 待校验的账号
     * @return Map，key 为 "valid"，value 为 true（可用）或 false（已存在）
     */
    Map<String, Boolean> checkAccount(String account);
}
