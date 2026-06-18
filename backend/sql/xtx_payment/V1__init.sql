-- 小兔鲜儿支付数据库初始化脚本
CREATE DATABASE IF NOT EXISTS xtx_payment DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE xtx_payment;

-- 支付订单表
CREATE TABLE IF NOT EXISTS pay_order (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    pay_no VARCHAR(50) NOT NULL COMMENT '支付单号',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    order_no VARCHAR(50) NOT NULL COMMENT '订单编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    pay_channel TINYINT DEFAULT 1 COMMENT '支付渠道（1-微信 2-支付宝 3-模拟支付）',
    pay_money DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    pay_status TINYINT DEFAULT 1 COMMENT '支付状态（1-待支付 2-已支付 3-已退款）',
    third_trade_no VARCHAR(100) DEFAULT NULL COMMENT '第三方交易号',
    expire_time DATETIME DEFAULT NULL COMMENT '过期时间',
    pay_time DATETIME DEFAULT NULL COMMENT '支付时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_pay_no (pay_no),
    KEY idx_order_id (order_id),
    KEY idx_order_no (order_no),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付订单表';

-- 支付回调日志表
CREATE TABLE IF NOT EXISTS pay_callback_log (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    pay_no VARCHAR(50) DEFAULT NULL COMMENT '支付单号',
    channel VARCHAR(50) DEFAULT NULL COMMENT '回调渠道',
    raw_callback_data TEXT DEFAULT NULL COMMENT '原始回调数据',
    processed TINYINT DEFAULT 0 COMMENT '处理状态（0-未处理 1-处理成功 2-处理失败）',
    error_msg VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_pay_no (pay_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付回调日志表';
