package com.xtx.auth.controller;

import com.xtx.api.auth.dto.LoginRequestDTO;
import com.xtx.api.auth.dto.SmsLoginRequestDTO;
import com.xtx.api.auth.dto.SocialBindRequestDTO;
import com.xtx.api.auth.dto.SocialComplementRequestDTO;
import com.xtx.api.auth.dto.UserProfileDTO;
import com.xtx.auth.service.LoginService;
import com.xtx.common.core.result.FrontResponse;
import com.xtx.common.web.annotation.FrontController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 登录认证控制器
 * 提供账号密码登录、短信登录、社交登录等认证接口
 */
@FrontController
@RestController
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    /**
     * 账号密码登录
     *
     * @param request 登录请求体（账号和密码）
     * @return 用户认证信息
     */
    @PostMapping("/login")
    public FrontResponse<UserProfileDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        UserProfileDTO result = loginService.login(request);
        return FrontResponse.success(result);
    }

    /**
     * 获取短信登录验证码
     *
     * @param mobile 手机号
     * @return 无数据，仅表示发送成功
     */
    @GetMapping("/login/code")
    public FrontResponse<Void> sendCode(@RequestParam String mobile) {
        loginService.sendSmsCode(mobile, "LOGIN");
        return FrontResponse.success();
    }

    /**
     * 短信验证码登录
     *
     * @param request 短信登录请求体（手机号和验证码）
     * @return 用户认证信息
     */
    @PostMapping("/login/code")
    public FrontResponse<UserProfileDTO> smsLogin(@Valid @RequestBody SmsLoginRequestDTO request) {
        UserProfileDTO result = loginService.smsLogin(request);
        return FrontResponse.success(result);
    }

    /**
     * 第三方社交账号登录
     *
     * @param unionId 第三方平台唯一标识
     * @param source  来源渠道（如：MINI_APP-小程序，H5 等）
     * @return 用户认证信息；若未绑定则抛出 SOCIAL_NOT_BOUND 异常
     */
    @PostMapping("/login/social")
    public FrontResponse<UserProfileDTO> socialLogin(@RequestParam String unionId, @RequestParam Integer source) {
        UserProfileDTO result = loginService.socialLogin(unionId, source);
        return FrontResponse.success(result);
    }

    /**
     * 获取社交登录绑定手机号的验证码
     *
     * @param mobile 手机号
     * @return 无数据，仅表示发送成功
     */
    @GetMapping("/login/social/code")
    public FrontResponse<Void> sendSocialBindCode(@RequestParam String mobile) {
        loginService.sendSmsCode(mobile, "BIND");
        return FrontResponse.success();
    }

    /**
     * 社交登录绑定手机号
     *
     * @param request 社交绑定请求体
     * @return 用户认证信息
     */
    @PostMapping("/login/social/bind")
    public FrontResponse<UserProfileDTO> socialBind(@Valid @RequestBody SocialBindRequestDTO request) {
        UserProfileDTO result = loginService.socialBind(request);
        return FrontResponse.success(result);
    }

    /**
     * 注册时校验账号是否已被注册
     *
     * @param account 待校验的账号
     * @return 校验结果 Map，包含 valid 字段
     */
    @GetMapping("/register/check")
    public FrontResponse<Map<String, Boolean>> checkAccount(@RequestParam String account) {
        Map<String, Boolean> result = loginService.checkAccount(account);
        return FrontResponse.success(result);
    }

    /**
     * 获取注册短信验证码
     *
     * @param mobile 手机号
     * @return 无数据，仅表示发送成功
     */
    @GetMapping("/register/code")
    public FrontResponse<Void> sendRegisterCode(@RequestParam String mobile) {
        loginService.sendSmsCode(mobile, "REGISTER");
        return FrontResponse.success();
    }

    /**
     * 社交登录信息补全
     * 首次社交登录时补充手机号、账号、昵称等信息
     *
     * @param unionId 第三方平台唯一标识
     * @param request 信息补全请求体
     * @return 用户认证信息
     */
    @PostMapping("/login/social/{unionId}/complement")
    public FrontResponse<UserProfileDTO> socialComplement(@PathVariable String unionId,
                                                          @Valid @RequestBody SocialComplementRequestDTO request) {
        UserProfileDTO result = loginService.socialComplement(unionId, request);
        return FrontResponse.success(result);
    }
}
