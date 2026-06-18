-- ============================================
-- V1: 商品模块基础表结构
-- 注意：ID 使用 BIGINT AUTO_INCREMENT 以兼容 Mock 数字字符串 ID
-- ============================================

-- 商品表
CREATE TABLE IF NOT EXISTS goods (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(200) NOT NULL COMMENT '商品名称',
    spu_code VARCHAR(100) DEFAULT NULL COMMENT '商品 SPU 编码',
    description VARCHAR(500) DEFAULT NULL COMMENT '商品描述/副标题',
    tag VARCHAR(200) DEFAULT NULL COMMENT '商品标签',
    price DECIMAL(10,2) DEFAULT NULL COMMENT '商品价格（元）',
    old_price DECIMAL(10,2) DEFAULT NULL COMMENT '商品原价（元）',
    discount INT DEFAULT 1 COMMENT '折扣比例',
    picture VARCHAR(500) DEFAULT NULL COMMENT '商品主图 URL',
    brand_id BIGINT DEFAULT NULL COMMENT '品牌ID',
    brand_name VARCHAR(100) DEFAULT NULL COMMENT '品牌名称',
    brand_logo VARCHAR(500) DEFAULT NULL COMMENT '品牌 Logo URL',
    category_id BIGINT DEFAULT NULL COMMENT '三级分类ID',
    top_category_id BIGINT DEFAULT NULL COMMENT '一级分类ID',
    parent_category_id BIGINT DEFAULT NULL COMMENT '二级分类ID',
    inventory INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    sales_count INT NOT NULL DEFAULT 0 COMMENT '销量',
    comment_count INT NOT NULL DEFAULT 0 COMMENT '评论数',
    collect_count INT NOT NULL DEFAULT 0 COMMENT '收藏数',
    is_pre_sale TINYINT NOT NULL DEFAULT 0 COMMENT '是否预售：0-否 1-是',
    is_collect TINYINT NOT NULL DEFAULT 0 COMMENT '是否收藏：0-否 1-是',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序权重',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-下架 1-上架',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_category_id (category_id),
    INDEX idx_top_category_id (top_category_id),
    INDEX idx_status (status),
    INDEX idx_sales_count (sales_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 商品图片表
CREATE TABLE IF NOT EXISTS goods_picture (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    goods_id BIGINT NOT NULL COMMENT '商品ID',
    picture_url VARCHAR(500) NOT NULL COMMENT '图片 URL',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序权重',
    is_main TINYINT NOT NULL DEFAULT 0 COMMENT '是否主图：0-否 1-是',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_goods_id (goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品图片表';

-- 商品详情表
CREATE TABLE IF NOT EXISTS goods_detail (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    goods_id BIGINT NOT NULL COMMENT '商品ID',
    detail_images TEXT DEFAULT NULL COMMENT '详情图片 URL 列表（JSON 数组）',
    properties TEXT DEFAULT NULL COMMENT '详情属性列表（JSON 格式）',
    detail_html TEXT DEFAULT NULL COMMENT '详情HTML',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_goods_id (goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品详情表';

-- 商品规格维度表（如：颜色、尺寸）
CREATE TABLE IF NOT EXISTS goods_spec (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    goods_id BIGINT NOT NULL COMMENT '商品ID',
    name VARCHAR(50) NOT NULL COMMENT '规格名称（如：颜色、尺寸）',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序权重',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_goods_id (goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品规格维度表';

-- 商品规格值表（如：白色、红色）
CREATE TABLE IF NOT EXISTS goods_spec_value (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    spec_id BIGINT NOT NULL COMMENT '规格ID',
    name VARCHAR(50) NOT NULL COMMENT '规格值名称',
    picture VARCHAR(500) DEFAULT NULL COMMENT '规格值图片（颜色规格可能有图）',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序权重',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_spec_id (spec_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品规格值表';

-- 商品 SKU 表
CREATE TABLE IF NOT EXISTS goods_sku (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    goods_id BIGINT NOT NULL COMMENT '商品ID',
    sku_code VARCHAR(100) DEFAULT NULL COMMENT 'SKU 编码',
    price INT NOT NULL DEFAULT 0 COMMENT '售价（分，如 4900 表示 49.00 元）',
    old_price INT DEFAULT NULL COMMENT '原价（分）',
    picture VARCHAR(500) DEFAULT NULL COMMENT 'SKU 图片 URL',
    inventory INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    is_effective TINYINT NOT NULL DEFAULT 1 COMMENT '是否有效：0-无效 1-有效',
    attrs_text VARCHAR(500) DEFAULT NULL COMMENT '规格文本（如 "颜色:白色 尺寸:标准款"）',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_goods_id (goods_id),
    INDEX idx_sku_code (sku_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品 SKU 表';

-- 商品 SKU 规格值关联表
CREATE TABLE IF NOT EXISTS goods_sku_spec_value (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    sku_id BIGINT NOT NULL COMMENT 'SKU ID',
    spec_id BIGINT NOT NULL COMMENT '规格ID',
    spec_value_id BIGINT DEFAULT NULL COMMENT '规格值ID',
    spec_value_name VARCHAR(50) DEFAULT NULL COMMENT '规格值名称（冗余）',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_sku_id (sku_id),
    INDEX idx_spec_id (spec_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SKU 规格值关联表';
