package com.xtx.auth.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 刷新令牌实体类
 * 对应数据库表 auth_refresh_token，用于 JWT 令牌的刷新和续期
 */
@Data
@Table("auth_refresh_token")
public class AuthRefreshToken {

    /** 主键ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 令牌唯一标识（JTI） */
    private String tokenId;

    /** 刷新令牌值 */
    private String refreshToken;

    /** 过期时间 */
    private LocalDateTime expiresAt;

    /** 是否已吊销：0-未吊销，1-已吊销 */
    private Integer revoked;

    /** 创建时间 */
    private LocalDateTime createTime;
}
