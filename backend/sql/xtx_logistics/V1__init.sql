-- 小兔鲜儿物流数据库初始化脚本
CREATE DATABASE IF NOT EXISTS xtx_logistics DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE xtx_logistics;

-- 订单物流信息表
CREATE TABLE IF NOT EXISTS order_logistics (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    logistics_no VARCHAR(100) NOT NULL COMMENT '物流单号',
    company_name VARCHAR(100) DEFAULT NULL COMMENT '物流公司名称',
    company_code VARCHAR(50) DEFAULT NULL COMMENT '物流公司编码',
    status TINYINT DEFAULT 1 COMMENT '物流状态（1-已揽收 2-运输中 3-派送中 4-已签收）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_id (order_id),
    KEY idx_logistics_no (logistics_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单物流信息表';

-- 物流轨迹表
CREATE TABLE IF NOT EXISTS order_logistics_trace (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    logistics_id BIGINT NOT NULL COMMENT '物流ID',
    accept_time VARCHAR(50) DEFAULT NULL COMMENT '发生时间',
    accept_station VARCHAR(200) DEFAULT NULL COMMENT '发生地点',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_logistics_id (logistics_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流轨迹表';
