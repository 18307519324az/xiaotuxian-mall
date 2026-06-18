-- ============================================
-- V1: 分类模块基础表结构
-- 仅建表，不含种子数据（见 V2）
-- ============================================

-- 分类表（支持三级分类：一级 → 二级 → 三级）
CREATE TABLE IF NOT EXISTS category (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父分类ID（0 表示一级分类）',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    icon_url VARCHAR(500) DEFAULT NULL COMMENT '分类图标 URL',
    picture_url VARCHAR(500) DEFAULT NULL COMMENT '分类图片 URL（Banner/大图）',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序权重（值越大越靠前）',
    level TINYINT NOT NULL COMMENT '层级：1-一级 2-二级 3-三级',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_level (level),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表（三级分类体系）';

-- 分类-商品关联表（多对多）
CREATE TABLE IF NOT EXISTS category_goods (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    category_id BIGINT NOT NULL COMMENT '分类ID（通常是三级分类）',
    goods_id VARCHAR(50) NOT NULL COMMENT '商品ID（对应 products.json 的 key）',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序权重',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_category_goods (category_id, goods_id),
    INDEX idx_category_id (category_id),
    INDEX idx_goods_id (goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类-商品关联表';

-- 分类 Banner 表
CREATE TABLE IF NOT EXISTS category_banner (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    category_id BIGINT NOT NULL COMMENT '关联的分类ID（一级分类）',
    img_url VARCHAR(500) NOT NULL COMMENT 'Banner 图片 URL',
    link_url VARCHAR(500) DEFAULT NULL COMMENT '点击跳转链接',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序权重',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类 Banner 表';

-- 分类筛选品牌表
CREATE TABLE IF NOT EXISTS category_filter_brand (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    category_id BIGINT NOT NULL COMMENT '关联的分类ID（二级分类）',
    brand_id VARCHAR(50) NOT NULL COMMENT '品牌ID',
    brand_name VARCHAR(100) NOT NULL COMMENT '品牌名称',
    brand_logo VARCHAR(500) DEFAULT NULL COMMENT '品牌 Logo URL',
    brand_letter VARCHAR(10) DEFAULT NULL COMMENT '品牌首字母',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序权重',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_category_brand (category_id, brand_id),
    INDEX idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类筛选品牌表';

-- 分类商品卡片表（从 Mock Master 导出，分类展示用）
-- 当 goods-service 上线后，此表可废弃，改为 Feign 调用 goods-service
CREATE TABLE IF NOT EXISTS category_goods_card (
    goods_id VARCHAR(50) NOT NULL COMMENT '商品ID',
    name VARCHAR(200) NOT NULL COMMENT '商品名称',
    description VARCHAR(500) DEFAULT NULL COMMENT '商品描述/副标题',
    price DECIMAL(10,2) NOT NULL COMMENT '商品价格',
    picture VARCHAR(500) NOT NULL DEFAULT '' COMMENT '商品主图 URL',
    tag VARCHAR(200) DEFAULT NULL COMMENT '商品标签',
    sales_count INT NOT NULL DEFAULT 0 COMMENT '销量',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类商品卡片（Master 导出数据）';
