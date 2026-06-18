package com.xtx.common.security.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 配置属性。
 * 从配置文件读取以 "xtx.jwt" 为前缀的 JWT 相关配置项。
 */
@Data
@ConfigurationProperties(prefix = "xtx.jwt")
public class JwtProperties {

    /** JWT 签名密钥（Base64 编码，至少 256 位），由应用配置覆盖 */
    private String secret = "CHANGE-ME-LOCAL-DEV-ONLY";

    /** 访问令牌过期时间（分钟），默认 30 分钟 */
    private long accessTokenExpireMinutes = 30;

    /** 刷新令牌过期时间（天），默认 7 天 */
    private long refreshTokenExpireDays = 7;

    /** JWT 签发者 */
    private String issuer = "xtx-mall";
}
