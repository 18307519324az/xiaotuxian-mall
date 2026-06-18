-- ============================================
-- 01-init-xtx_category.sql
-- 小兔鲜儿分类数据库初始化
-- v1.7: 从 sql/xtx_category/V1__init.sql 整理
-- 完整种子数据见 Flyway V2__init_category_data.sql
-- ============================================

CREATE DATABASE IF NOT EXISTS xtx_category DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE xtx_category;

-- 分类表（支持三级分类）
CREATE TABLE IF NOT EXISTS category (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID（0-顶级分类）',
    level TINYINT NOT NULL COMMENT '层级（1-一级 2-二级 3-三级）',
    icon VARCHAR(500) DEFAULT NULL COMMENT '分类图标',
    picture VARCHAR(500) DEFAULT NULL COMMENT '分类图片',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态（0-禁用 1-启用）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id),
    KEY idx_level (level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 分类Banner表
CREATE TABLE IF NOT EXISTS category_banner (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    img_url VARCHAR(500) NOT NULL COMMENT 'Banner图片URL',
    href_url VARCHAR(500) DEFAULT NULL COMMENT '跳转链接',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类Banner表';
