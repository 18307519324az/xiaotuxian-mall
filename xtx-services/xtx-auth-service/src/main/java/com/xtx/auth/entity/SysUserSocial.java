package com.xtx.auth.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户第三方社交账号绑定实体类
 * 对应数据库表 sys_user_social，存储用户与微信等第三方平台的绑定关系
 */
@Data
@Table("sys_user_social")
public class SysUserSocial {

    /** 主键ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 系统用户ID，关联 sys_user.id */
    private Long userId;

    /** 第三方平台唯一标识（微信 openId/unionId） */
    private String unionId;

    /** 第三方平台名称：WECHAT、QQ、WEIBO 等 */
    private String platform;

    /** 来源渠道：小程序、公众号、H5 等 */
    private String source;

    /** 创建时间 */
    private LocalDateTime createTime;
}
