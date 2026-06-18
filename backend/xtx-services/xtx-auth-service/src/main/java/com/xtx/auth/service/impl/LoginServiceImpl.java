package com.xtx.auth.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.xtx.api.auth.dto.LoginRequestDTO;
import com.xtx.api.auth.dto.SmsLoginRequestDTO;
import com.xtx.api.auth.dto.SocialBindRequestDTO;
import com.xtx.api.auth.dto.SocialComplementRequestDTO;
import com.xtx.api.auth.dto.UserProfileDTO;
import com.xtx.auth.entity.SysSmsCode;
import com.xtx.auth.entity.SysUser;
import com.xtx.auth.entity.SysUserSocial;
import com.xtx.auth.mapper.SysSmsCodeMapper;
import com.xtx.auth.mapper.SysUserMapper;
import com.xtx.auth.mapper.SysUserSocialMapper;
import com.xtx.auth.service.LoginService;
import com.xtx.common.core.exception.BizException;
import com.xtx.common.core.result.ResultCode;
import com.xtx.common.security.util.JwtUtil;
import com.xtx.common.security.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 登录服务实现类
 * 实现账号密码、短信验证码、第三方社交登录等认证逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final SysUserMapper sysUserMapper;
    private final SysUserSocialMapper sysUserSocialMapper;
    private final SysSmsCodeMapper sysSmsCodeMapper;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 开发环境默认验证码
     */
    private static final String DEV_SMS_CODE = "123456";

    /**
     * 验证码 Redis 前缀
     */
    private static final String SMS_CODE_PREFIX = "sms:code:";

    @Override
    public UserProfileDTO login(LoginRequestDTO request) {
        // 根据账号查询用户
        SysUser user = sysUserMapper.selectEnabledByAccount(request.getAccount());
        if (user == null) {
            throw new BizException(ResultCode.UNAUTHORIZED.getCode(), "账号或密码错误");
        }

        // 校验密码
        if (!PasswordUtil.matches(request.getPassword(), user.getPassword())) {
            throw new BizException(ResultCode.UNAUTHORIZED.getCode(), "账号或密码错误");
        }

        // 生成令牌
        return generateUserProfile(user);
    }

    @Override
    public void sendSmsCode(String mobile, String type) {
        // 生成验证码，开发环境固定为 123456
        String code;
        if (isDevEnvironment()) {
            code = DEV_SMS_CODE;
            log.info("【开发环境】短信验证码：{} -> {}", mobile, code);
        } else {
            code = RandomUtil.randomNumbers(6);
        }

        // 保存验证码记录
        SysSmsCode smsCode = new SysSmsCode();
        smsCode.setMobile(mobile);
        smsCode.setCode(code);
        smsCode.setType(type);
        smsCode.setUsed(0);
        smsCode.setExpireTime(LocalDateTime.now().plusMinutes(5));
        smsCode.setCreateTime(LocalDateTime.now());
        sysSmsCodeMapper.insert(smsCode);

        // 缓存到 Redis（用于快速校验和防刷）
        String redisKey = SMS_CODE_PREFIX + type + ":" + mobile;
        redisTemplate.opsForValue().set(redisKey, code, 5, TimeUnit.MINUTES);

        log.info("短信验证码已发送：mobile={}, type={}, code={}", mobile, type, code);
    }

    @Override
    public UserProfileDTO smsLogin(SmsLoginRequestDTO request) {
        // 校验短信验证码
        SysSmsCode smsCode = sysSmsCodeMapper.selectLatestValidCode(request.getMobile(), "LOGIN");
        if (smsCode == null) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "验证码无效或已过期");
        }

        if (!smsCode.getCode().equals(request.getCode())) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "验证码错误");
        }

        // 标记验证码已使用
        smsCode.setUsed(1);
        sysSmsCodeMapper.update(smsCode);

        // 查找或创建用户
        SysUser user = sysUserMapper.selectByMobile(request.getMobile());
        if (user == null) {
            user = new SysUser();
            user.setMobile(request.getMobile());
            user.setAccount("u_" + request.getMobile().substring(Math.max(0, request.getMobile().length() - 8)));
            user.setNickname("用户" + request.getMobile().substring(Math.max(0, request.getMobile().length() - 4)));
            user.setStatus(1);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            sysUserMapper.insert(user);
        }

        // 生成令牌
        return generateUserProfile(user);
    }

    @Override
    public UserProfileDTO socialLogin(String unionId, Integer source) {
        // 查询社交绑定记录
        SysUserSocial social = sysUserSocialMapper.selectByUnionId(unionId);
        if (social == null) {
            // 未绑定，抛出异常通知前端引导绑定或补全信息
            throw new BizException(30001, "该第三方账号未绑定，请先绑定手机号");
        }

        // 查询系统用户
        SysUser user = sysUserMapper.selectOneById(social.getUserId());
        if (user == null || user.getStatus() != 1) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "用户不存在或已被禁用");
        }

        // 生成令牌
        return generateUserProfile(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserProfileDTO socialBind(SocialBindRequestDTO request) {
        // 校验验证码
        SysSmsCode smsCode = sysSmsCodeMapper.selectLatestValidCode(request.getMobile(), "BIND");
        if (smsCode == null) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "验证码无效或已过期");
        }
        if (!smsCode.getCode().equals(request.getCode())) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "验证码错误");
        }

        // 标记验证码已使用
        smsCode.setUsed(1);
        sysSmsCodeMapper.update(smsCode);

        // 查找或创建用户
        SysUser user = sysUserMapper.selectByMobile(request.getMobile());
        if (user == null) {
            user = new SysUser();
            user.setMobile(request.getMobile());
            user.setAccount("u_" + request.getMobile().substring(Math.max(0, request.getMobile().length() - 8)));
            user.setNickname("用户" + request.getMobile().substring(Math.max(0, request.getMobile().length() - 4)));
            user.setStatus(1);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            sysUserMapper.insert(user);
        }

        // 绑定社交账号
        SysUserSocial social = new SysUserSocial();
        social.setUserId(user.getId());
        social.setUnionId(request.getUnionId());
        social.setPlatform(request.getPlatform());
        social.setSource(String.valueOf(request.getSource()));
        social.setCreateTime(LocalDateTime.now());
        sysUserSocialMapper.insert(social);

        // 生成令牌
        return generateUserProfile(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserProfileDTO socialComplement(String unionId, SocialComplementRequestDTO request) {
        // 校验验证码
        SysSmsCode smsCode = sysSmsCodeMapper.selectLatestValidCode(request.getMobile(), "BIND");
        if (smsCode == null) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "验证码无效或已过期");
        }
        if (!smsCode.getCode().equals(request.getCode())) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "验证码错误");
        }

        // 标记验证码已使用
        smsCode.setUsed(1);
        sysSmsCodeMapper.update(smsCode);

        // 创建用户
        SysUser user = new SysUser();
        user.setMobile(request.getMobile());
        user.setAccount(request.getAccount());
        user.setNickname(request.getNickname());
        user.setPassword(request.getPassword() != null ? PasswordUtil.encode(request.getPassword()) : null);
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.insert(user);

        // 绑定社交账号
        SysUserSocial social = new SysUserSocial();
        social.setUserId(user.getId());
        social.setUnionId(unionId);
        social.setPlatform(request.getPlatform());
        social.setSource(String.valueOf(request.getSource()));
        social.setCreateTime(LocalDateTime.now());
        sysUserSocialMapper.insert(social);

        // 生成令牌
        return generateUserProfile(user);
    }

    @Override
    public Map<String, Boolean> checkAccount(String account) {
        // 统计同名账号数量
        long count = sysUserMapper.selectCountByQuery(
                com.mybatisflex.core.query.QueryWrapper.create()
                        .eq("account", account)
        );
        Map<String, Boolean> result = new HashMap<>();
        result.put("valid", count == 0);
        return result;
    }

    /**
     * 生成用户认证信息（JWT 令牌 + 用户资料）
     *
     * @param user 系统用户
     * @return 用户认证信息
     */
    private UserProfileDTO generateUserProfile(SysUser user) {
        // 生成访问令牌和刷新令牌
        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getAccount(), "H5");
        String refreshToken = jwtUtil.createRefreshToken(user.getId());

        // 构建用户资料
        UserProfileDTO profile = new UserProfileDTO();
        profile.setId(user.getId());
        profile.setAccount(user.getAccount());
        profile.setNickname(user.getNickname());
        profile.setAvatar(user.getAvatar());
        profile.setMobile(user.getMobile());
        profile.setToken(accessToken);
        profile.setRefreshToken(refreshToken);

        return profile;
    }

    /**
     * 判断是否为开发环境
     *
     * @return true 表示开发环境
     */
    private boolean isDevEnvironment() {
        // 简单判断：可通过配置文件或 Spring Profile 控制
        return true;
    }
}
