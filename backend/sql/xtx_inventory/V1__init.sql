-- 小兔鲜儿库存数据库初始化脚本
CREATE DATABASE IF NOT EXISTS xtx_inventory DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE xtx_inventory;

-- SKU库存表
CREATE TABLE IF NOT EXISTS stock_sku (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    sku_id BIGINT NOT NULL COMMENT 'SKU ID',
    total_stock INT DEFAULT 0 COMMENT '总库存',
    available_stock INT DEFAULT 0 COMMENT '可用库存',
    locked_stock INT DEFAULT 0 COMMENT '锁定库存（已下单未支付）',
    sold_stock INT DEFAULT 0 COMMENT '已售数量',
    version INT DEFAULT 1 COMMENT '乐观锁版本号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sku_id (sku_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SKU库存表';

-- 库存预扣记录表
CREATE TABLE IF NOT EXISTS stock_reservation (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    order_id BIGINT COMMENT '订单ID',
    order_no VARCHAR(50) NOT NULL COMMENT '订单编号',
    sku_id BIGINT NOT NULL COMMENT 'SKU ID',
    count INT NOT NULL COMMENT '预占数量',
    status TINYINT DEFAULT 0 COMMENT '状态（1-已预占 2-已扣减 3-已释放）',
    expire_time DATETIME DEFAULT NULL COMMENT '过期时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_order_no (order_no),
    KEY idx_sku_id (sku_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存预扣记录表';

-- 库存变更日志表
CREATE TABLE IF NOT EXISTS stock_change_log (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    sku_id BIGINT NOT NULL COMMENT 'SKU ID',
    change_type VARCHAR(20) NOT NULL COMMENT '变更类型（RESERVE-预扣 CONFIRM-确认 RELEASE-释放 INCREASE-增加）',
    change_amount INT NOT NULL COMMENT '变更数量',
    before_stock INT DEFAULT 0 COMMENT '变更前库存',
    after_stock INT DEFAULT 0 COMMENT '变更后库存',
    biz_key VARCHAR(50) DEFAULT NULL COMMENT '业务唯一标识',
    order_no VARCHAR(50) DEFAULT NULL COMMENT '关联订单编号',
    operator VARCHAR(50) DEFAULT 'system' COMMENT '操作人',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_sku_id (sku_id),
    KEY idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存变更日志表';

-- 初始化所有SKU库存（total_stock=1000, available_stock=1000, locked_stock=0, sold_stock=0, version=1）
INSERT INTO stock_sku(sku_id, total_stock, available_stock, locked_stock, sold_stock, version) VALUES
-- 商品1 SKUs
(1, 1000, 1000, 0, 0, 1), (2, 1000, 1000, 0, 0, 1), (3, 1000, 1000, 0, 0, 1),
(4, 1000, 1000, 0, 0, 1), (5, 1000, 1000, 0, 0, 1), (6, 1000, 1000, 0, 0, 1),
-- 商品2 SKUs
(7, 1000, 1000, 0, 0, 1), (8, 1000, 1000, 0, 0, 1), (9, 1000, 1000, 0, 0, 1),
(10, 1000, 1000, 0, 0, 1),
-- 商品3 SKUs
(11, 1000, 1000, 0, 0, 1), (12, 1000, 1000, 0, 0, 1), (13, 1000, 1000, 0, 0, 1),
(14, 1000, 1000, 0, 0, 1),
-- 商品4 SKUs
(15, 1000, 1000, 0, 0, 1), (16, 1000, 1000, 0, 0, 1), (17, 1000, 1000, 0, 0, 1),
(18, 1000, 1000, 0, 0, 1),
-- 商品5 SKUs
(19, 1000, 1000, 0, 0, 1), (20, 1000, 1000, 0, 0, 1), (21, 1000, 1000, 0, 0, 1),
(22, 1000, 1000, 0, 0, 1), (23, 1000, 1000, 0, 0, 1), (24, 1000, 1000, 0, 0, 1),
(25, 1000, 1000, 0, 0, 1), (26, 1000, 1000, 0, 0, 1), (27, 1000, 1000, 0, 0, 1);
