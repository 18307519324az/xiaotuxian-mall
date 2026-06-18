-- ============================================
-- 02-init-xtx_goods.sql
-- 小兔鲜儿商品数据库初始化
-- v1.7: 合并 sql/xtx_goods/V1__init.sql + Flyway V4/V6 变更
-- 完整种子数据见 Flyway V2/V7/V8 迁移脚本
-- ============================================

CREATE DATABASE IF NOT EXISTS xtx_goods DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE xtx_goods;

-- 品牌表
CREATE TABLE IF NOT EXISTS brand (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '品牌名称',
    name_en VARCHAR(100) DEFAULT NULL COMMENT '品牌英文名',
    logo VARCHAR(500) DEFAULT NULL COMMENT '品牌Logo',
    picture VARCHAR(500) DEFAULT NULL COMMENT '品牌图片',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态（0-禁用 1-启用）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='品牌表';

-- 商品表
CREATE TABLE IF NOT EXISTS goods (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(200) NOT NULL COMMENT '商品名称',
    description VARCHAR(500) DEFAULT NULL COMMENT '商品描述',
    category_id BIGINT DEFAULT NULL COMMENT '分类ID',
    brand_id BIGINT DEFAULT NULL COMMENT '品牌ID',
    brand_picture VARCHAR(500) DEFAULT NULL COMMENT '品牌图片URL（V4 新增）',
    brand_code VARCHAR(64) DEFAULT NULL COMMENT '品牌编码，兼容字符串品牌ID（V4 新增）',
    status TINYINT DEFAULT 0 COMMENT '状态（0-下架 1-上架 2-待审核）',
    sort_order INT DEFAULT 0 COMMENT '排序',
    is_new TINYINT DEFAULT 0 COMMENT '是否新品（0-否 1-是）',
    is_hot TINYINT DEFAULT 0 COMMENT '是否热门（0-否 1-是）',
    sales_count INT DEFAULT 0 COMMENT '销量',
    comment_count INT DEFAULT 0 COMMENT '评价数',
    fav_count INT DEFAULT 0 COMMENT '收藏数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_category_id (category_id),
    KEY idx_brand_id (brand_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 商品图片表
CREATE TABLE IF NOT EXISTS goods_picture (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    goods_id BIGINT NOT NULL COMMENT '商品ID',
    picture_url VARCHAR(500) NOT NULL COMMENT '图片URL',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_goods_id (goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品图片表';

-- 商品详情表（属性等信息）
CREATE TABLE IF NOT EXISTS goods_detail (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    goods_id BIGINT NOT NULL COMMENT '商品ID',
    properties JSON DEFAULT NULL COMMENT '商品属性（JSON格式）',
    detail_images TEXT DEFAULT NULL COMMENT '详情图片列表（逗号分隔）',
    detail_html TEXT DEFAULT NULL COMMENT '详情HTML',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_goods_id (goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品详情表';

-- 商品规格表
CREATE TABLE IF NOT EXISTS goods_spec (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    goods_id BIGINT NOT NULL COMMENT '商品ID',
    name VARCHAR(50) NOT NULL COMMENT '规格名称（如：颜色、尺寸）',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_goods_id (goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品规格表';

-- 商品规格值表
CREATE TABLE IF NOT EXISTS goods_spec_value (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    spec_id BIGINT NOT NULL COMMENT '规格ID',
    name VARCHAR(100) NOT NULL COMMENT '规格值（如：黑色、M码）',
    picture VARCHAR(500) DEFAULT NULL COMMENT '规格值图片',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_spec_id (spec_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品规格值表';

-- 商品SKU表
CREATE TABLE IF NOT EXISTS goods_sku (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    goods_id BIGINT NOT NULL COMMENT '商品ID',
    sku_code VARCHAR(100) DEFAULT NULL COMMENT 'SKU编码',
    name VARCHAR(200) DEFAULT NULL COMMENT 'SKU名称',
    picture VARCHAR(500) DEFAULT NULL COMMENT 'SKU图片',
    price DECIMAL(10,2) NOT NULL COMMENT '原价',
    now_price DECIMAL(10,2) DEFAULT NULL COMMENT '当前售价',
    cost_price DECIMAL(10,2) DEFAULT NULL COMMENT '成本价',
    stock INT DEFAULT 0 COMMENT '库存',
    status TINYINT DEFAULT 1 COMMENT '状态（0-禁用 1-启用）',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_goods_id (goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品SKU表';

-- 商品SKU规格值关联表
CREATE TABLE IF NOT EXISTS goods_sku_spec_value (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    sku_id BIGINT NOT NULL COMMENT 'SKU ID',
    spec_id BIGINT NOT NULL COMMENT '规格ID',
    spec_value_id BIGINT NOT NULL COMMENT '规格值ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_sku_id (sku_id),
    KEY idx_spec_id (spec_id),
    KEY idx_spec_value_id (spec_value_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品SKU规格值关联表';

-- 专题表（V6 新增）
CREATE TABLE IF NOT EXISTS topic (
    id VARCHAR(64) NOT NULL PRIMARY KEY COMMENT '专题ID（兼容字符串ID）',
    title VARCHAR(200) NOT NULL COMMENT '专题标题',
    summary VARCHAR(500) DEFAULT '' COMMENT '专题摘要',
    cover VARCHAR(500) DEFAULT '' COMMENT '专题封面图URL',
    collect_num INT DEFAULT 0 COMMENT '收藏数',
    view_num INT DEFAULT 0 COMMENT '浏览数',
    reply_num INT DEFAULT 0 COMMENT '评论数',
    lowest_price DECIMAL(10,2) DEFAULT 0.00 COMMENT '最低价格',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    sort_order INT DEFAULT 0 COMMENT '排序权重',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专题活动表';

-- 专题商品关联表（V6 新增）
CREATE TABLE IF NOT EXISTS special_goods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    special_id VARCHAR(64) NOT NULL COMMENT '专题ID，关联 topic.id',
    goods_id BIGINT NOT NULL COMMENT '商品ID，关联 goods.id',
    sort_order INT DEFAULT 0 COMMENT '排序权重',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_special_id (special_id),
    INDEX idx_goods_id (goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专题商品关联表';
