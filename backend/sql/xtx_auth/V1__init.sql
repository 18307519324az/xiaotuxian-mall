-- 小兔鲜儿认证数据库初始化脚本
CREATE DATABASE IF NOT EXISTS xtx_auth DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE xtx_auth;

-- 系统用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    account VARCHAR(50) NOT NULL COMMENT '账号',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    mobile VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    avatar VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    gender TINYINT DEFAULT 0 COMMENT '性别（0-未知 1-男 2-女）',
    birthday DATE DEFAULT NULL COMMENT '生日',
    status TINYINT DEFAULT 1 COMMENT '状态（0-禁用 1-启用）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_account (account),
    KEY idx_mobile (mobile)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 用户第三方登录关联表
CREATE TABLE IF NOT EXISTS sys_user_social (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    social_type VARCHAR(20) NOT NULL COMMENT '第三方类型（WECHAT/QQ/WEIBO）',
    social_openid VARCHAR(100) NOT NULL COMMENT '第三方OpenID',
    social_unionid VARCHAR(100) DEFAULT NULL COMMENT '第三方UnionID',
    social_nickname VARCHAR(50) DEFAULT NULL COMMENT '第三方昵称',
    social_avatar VARCHAR(500) DEFAULT NULL COMMENT '第三方头像',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_social_openid (social_openid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户第三方登录关联表';

-- 短信验证码表
CREATE TABLE IF NOT EXISTS sys_sms_code (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    mobile VARCHAR(20) NOT NULL COMMENT '手机号',
    code VARCHAR(6) NOT NULL COMMENT '验证码',
    biz_type VARCHAR(50) DEFAULT 'LOGIN' COMMENT '业务类型（LOGIN-登录 REGISTER-注册 RESET_PWD-重置密码）',
    status TINYINT DEFAULT 0 COMMENT '状态（0-未使用 1-已使用 2-已过期）',
    expire_time DATETIME NOT NULL COMMENT '过期时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_mobile (mobile)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短信验证码表';

-- 刷新令牌表
CREATE TABLE IF NOT EXISTS auth_refresh_token (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    token VARCHAR(500) NOT NULL COMMENT '刷新令牌',
    expired TINYINT DEFAULT 0 COMMENT '是否过期（0-未过期 1-已过期）',
    expire_time DATETIME NOT NULL COMMENT '过期时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_token (token(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='刷新令牌表';

-- 初始化种子用户（密码为123456的BCrypt哈希）
INSERT INTO sys_user(account, password, mobile, nickname, status)
VALUES('xiaotuxian001', '$2a$10$N.zmJ9CFMRCIuFHZ1lB.gO4RACi1C3GFrD6GzQjKQGDMsNNBHWqCu', '13800000001', '小兔鲜用户', 1);
