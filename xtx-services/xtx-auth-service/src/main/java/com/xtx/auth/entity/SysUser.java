package com.xtx.auth.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 系统用户实体类
 * 对应数据库表 sys_user，存储系统用户账号信息
 */
@Data
@Table("sys_user")
public class SysUser {

    /** 用户主键ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 登录账号 */
    private String account;

    /** 登录密码（BCrypt 加密） */
    private String password;

    /** 手机号 */
    private String mobile;

    /** 头像地址 */
    private String avatar;

    /** 用户昵称 */
    private String nickname;

    /** 电子邮箱 */
    private String email;

    /** 性别：0-未知，1-男，2-女 */
    private Integer gender;

    /** 出生日期 */
    private LocalDate birthday;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
