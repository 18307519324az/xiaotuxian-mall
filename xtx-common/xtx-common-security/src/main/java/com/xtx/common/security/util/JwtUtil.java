package com.xtx.common.security.util;

import com.xtx.common.security.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

/**
 * JWT 令牌工具类。
 * 提供访问令牌和刷新令牌的创建、解析及校验功能。
 * 使用 HMAC-SHA 算法对令牌进行签名，密钥从 {@link JwtProperties} 中获取。
 */
@Component
public class JwtUtil {

    /** JWT 配置属性 */
    private final JwtProperties jwtProperties;

    /** 签名密钥 */
    private final SecretKey secretKey;

    /**
     * 构造 JwtUtil 实例并初始化签名密钥。
     *
     * @param jwtProperties JWT 配置属性
     */
    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        byte[] keyBytes = Base64.getDecoder().decode(jwtProperties.getSecret());
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 创建访问令牌（Access Token）。
     * 短有效期，用于接口鉴权。
     *
     * @param userId     用户 ID
     * @param account    用户账号
     * @param clientType 客户端类型
     * @return JWT 令牌字符串
     */
    public String createAccessToken(Long userId, String account, String clientType) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("account", account)
                .claim("clientType", clientType)
                .issuedAt(new Date(now))
                .expiration(new Date(now + jwtProperties.getAccessTokenExpireMinutes() * 60 * 1000))
                .issuer(jwtProperties.getIssuer())
                .signWith(secretKey)
                .compact();
    }

    /**
     * 创建刷新令牌（Refresh Token）。
     * 长有效期，用于刷新访问令牌。
     *
     * @param userId 用户 ID
     * @return JWT 令牌字符串
     */
    public String createRefreshToken(Long userId) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(new Date(now))
                .expiration(new Date(now + jwtProperties.getRefreshTokenExpireDays() * 24 * 60 * 60 * 1000))
                .issuer(jwtProperties.getIssuer())
                .signWith(secretKey)
                .compact();
    }

    /**
     * 解析 JWT 令牌，获取 Claims 载荷。
     *
     * @param token JWT 令牌字符串
     * @return Claims 载荷
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 JWT 令牌中提取用户 ID。
     *
     * @param token JWT 令牌字符串
     * @return 用户 ID
     */
    public Long getUserId(String token) {
        return Long.valueOf(parseToken(token).getSubject());
    }

    /**
     * 校验 JWT 令牌是否有效且未过期。
     *
     * @param token JWT 令牌字符串
     * @return true 有效，false 无效或已过期
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
