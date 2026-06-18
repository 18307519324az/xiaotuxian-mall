-- 小兔鲜儿购物车数据库初始化脚本
CREATE DATABASE IF NOT EXISTS xtx_cart DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE xtx_cart;

-- 购物车表
CREATE TABLE IF NOT EXISTS cart (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    sku_id BIGINT NOT NULL COMMENT 'SKU ID',
    count INT DEFAULT 1 COMMENT '购买数量',
    selected TINYINT DEFAULT 1 COMMENT '是否选中（1-选中 0-未选中）',
    is_effective TINYINT DEFAULT 1 COMMENT '是否有效（1-有效 0-无效）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_sku (user_id, sku_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';
