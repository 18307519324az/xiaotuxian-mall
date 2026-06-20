package com.xtx.auth.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统短信验证码实体类
 * 对应数据库表 sys_sms_code，存储短信验证码发送记录
 */
@Data
@Table("sys_sms_code")
public class SysSmsCode {

    /** 主键ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 接收验证码的手机号 */
    private String mobile;

    /** 短信验证码内容 */
    private String code;

    /** 验证码类型：LOGIN-登录，REGISTER-注册，BIND-绑定手机 */
    private String type;

    /** 是否已使用：0-未使用，1-已使用 */
    private Integer used;

    /** 过期时间 */
    private LocalDateTime expireTime;

    /** 创建时间 */
    private LocalDateTime createTime;
}
