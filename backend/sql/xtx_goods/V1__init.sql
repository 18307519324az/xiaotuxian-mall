-- 小兔鲜儿商品数据库初始化脚本
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

-- ==================== 种子数据 ====================

-- 品牌数据
INSERT INTO brand(name, name_en, logo, sort_order, status) VALUES
('优享生活', 'YOX', 'https://example.com/brand/yox.png', 1, 1),
('品味时光', 'TWS', 'https://example.com/brand/tws.png', 2, 1),
('自然精选', 'NAT', 'https://example.com/brand/nat.png', 3, 1);

-- 商品数据（5个已上架商品）
INSERT INTO goods(name, description, category_id, brand_id, status, is_new, is_hot, sales_count, comment_count, fav_count) VALUES
('北欧风简约台灯', '护眼LED台灯 三档调光', 9, 1, 1, 1, 1, 120, 35, 80),
('日式陶瓷茶杯套装', '手工制作 简约风格', 10, 2, 1, 1, 0, 85, 20, 45),
('混合坚果礼盒', '每日坚果 5种坚果搭配', 15, 3, 1, 0, 1, 300, 60, 120),
('新疆阿克苏苹果', '冰糖心苹果 新鲜采摘', 17, 3, 1, 0, 1, 500, 90, 200),
('纯棉简约T恤', '柔软亲肤 透气舒适', 20, 1, 1, 1, 0, 200, 40, 65);

-- 商品图片数据
INSERT INTO goods_picture(goods_id, picture_url, sort_order) VALUES
(1, 'https://example.com/goods/1_1.jpg', 1), (1, 'https://example.com/goods/1_2.jpg', 2),
(2, 'https://example.com/goods/2_1.jpg', 1), (2, 'https://example.com/goods/2_2.jpg', 2),
(3, 'https://example.com/goods/3_1.jpg', 1), (3, 'https://example.com/goods/3_2.jpg', 2),
(4, 'https://example.com/goods/4_1.jpg', 1), (4, 'https://example.com/goods/4_2.jpg', 2),
(5, 'https://example.com/goods/5_1.jpg', 1), (5, 'https://example.com/goods/5_2.jpg', 2);

-- 商品详情数据
INSERT INTO goods_detail(goods_id, properties) VALUES
(1, '{"材质":"ABS+金属","功率":"6W","光源":"LED","调光方式":"三档触控"}'),
(2, '{"材质":"陶瓷","容量":"350ml","工艺":"手工拉胚","适用":"茶水/咖啡"}'),
(3, '{"净含量":"500g","保质期":"180天","包装方式":"罐装","产地":"中国"}'),
(4, '{"规格":"5kg/箱","产地":"新疆阿克苏","储存方式":"冷藏","保质期":"30天"}'),
(5, '{"面料":"100%纯棉","版型":"标准","领型":"圆领","袖长":"短袖"}');

-- 规格数据（每个商品2个规格：颜色、尺寸）
INSERT INTO goods_spec(goods_id, name, sort_order) VALUES
(1, '颜色', 1), (1, '尺寸', 2),
(2, '颜色', 1), (2, '尺寸', 2),
(3, '规格', 1), (3, '口味', 2),
(4, '规格', 1), (4, '包装', 2),
(5, '颜色', 1), (5, '尺寸', 2);

-- 规格值数据（每个规格2-3个值）
INSERT INTO goods_spec_value(spec_id, name, sort_order) VALUES
-- 商品1 规格值
(1, '白色', 1), (1, '黑色', 2), (1, '金色', 3),
(2, '小号', 1), (2, '大号', 2),
-- 商品2 规格值
(3, '白色', 1), (3, '蓝色', 2),
(4, '小号', 1), (4, '大号', 2),
-- 商品3 规格值
(5, '250g装', 1), (5, '500g装', 2),
(6, '原味', 1), (6, '混合味', 2),
-- 商品4 规格值
(7, '5kg装', 1), (7, '10kg装', 2),
(8, '礼盒装', 1), (8, '简装', 2),
-- 商品5 规格值
(9, '白色', 1), (9, '黑色', 2), (9, '灰色', 3),
(10, 'M', 1), (10, 'L', 2), (10, 'XL', 3);

-- SKU数据
INSERT INTO goods_sku(goods_id, sku_code, name, picture, price, now_price, status, sort_order) VALUES
-- 商品1 SKU (颜色x尺寸: 3x2=6个)
(1, 'SKU001001', '北欧台灯 白色 小号', 'https://example.com/goods/1_1.jpg', 129.00, 99.00, 1, 1),
(1, 'SKU001002', '北欧台灯 白色 大号', 'https://example.com/goods/1_1.jpg', 169.00, 139.00, 1, 2),
(1, 'SKU001003', '北欧台灯 黑色 小号', 'https://example.com/goods/1_2.jpg', 129.00, 99.00, 1, 3),
(1, 'SKU001004', '北欧台灯 黑色 大号', 'https://example.com/goods/1_2.jpg', 169.00, 139.00, 1, 4),
(1, 'SKU001005', '北欧台灯 金色 小号', 'https://example.com/goods/1_1.jpg', 139.00, 109.00, 1, 5),
(1, 'SKU001006', '北欧台灯 金色 大号', 'https://example.com/goods/1_1.jpg', 179.00, 149.00, 1, 6),
-- 商品2 SKU (颜色x尺寸: 2x2=4个)
(2, 'SKU002001', '陶瓷茶杯 白色 小号', 'https://example.com/goods/2_1.jpg', 89.00, 69.00, 1, 1),
(2, 'SKU002002', '陶瓷茶杯 白色 大号', 'https://example.com/goods/2_1.jpg', 129.00, 99.00, 1, 2),
(2, 'SKU002003', '陶瓷茶杯 蓝色 小号', 'https://example.com/goods/2_2.jpg', 89.00, 69.00, 1, 3),
(2, 'SKU002004', '陶瓷茶杯 蓝色 大号', 'https://example.com/goods/2_2.jpg', 129.00, 99.00, 1, 4),
-- 商品3 SKU (规格x口味: 2x2=4个)
(3, 'SKU003001', '混合坚果 250g 原味', 'https://example.com/goods/3_1.jpg', 49.00, 39.90, 1, 1),
(3, 'SKU003002', '混合坚果 250g 混合味', 'https://example.com/goods/3_1.jpg', 49.00, 39.90, 1, 2),
(3, 'SKU003003', '混合坚果 500g 原味', 'https://example.com/goods/3_2.jpg', 89.00, 69.90, 1, 3),
(3, 'SKU003004', '混合坚果 500g 混合味', 'https://example.com/goods/3_2.jpg', 89.00, 69.90, 1, 4),
-- 商品4 SKU (规格x包装: 2x2=4个)
(4, 'SKU004001', '阿克苏苹果 5kg 礼盒装', 'https://example.com/goods/4_1.jpg', 79.00, 59.90, 1, 1),
(4, 'SKU004002', '阿克苏苹果 5kg 简装', 'https://example.com/goods/4_1.jpg', 59.00, 49.90, 1, 2),
(4, 'SKU004003', '阿克苏苹果 10kg 礼盒装', 'https://example.com/goods/4_2.jpg', 139.00, 109.90, 1, 3),
(4, 'SKU004004', '阿克苏苹果 10kg 简装', 'https://example.com/goods/4_2.jpg', 109.00, 89.90, 1, 4),
-- 商品5 SKU (颜色x尺寸: 3x3=9个)
(5, 'SKU005001', '纯棉T恤 白色 M', 'https://example.com/goods/5_1.jpg', 79.00, 59.00, 1, 1),
(5, 'SKU005002', '纯棉T恤 白色 L', 'https://example.com/goods/5_1.jpg', 79.00, 59.00, 1, 2),
(5, 'SKU005003', '纯棉T恤 白色 XL', 'https://example.com/goods/5_1.jpg', 79.00, 59.00, 1, 3),
(5, 'SKU005004', '纯棉T恤 黑色 M', 'https://example.com/goods/5_2.jpg', 79.00, 59.00, 1, 4),
(5, 'SKU005005', '纯棉T恤 黑色 L', 'https://example.com/goods/5_2.jpg', 79.00, 59.00, 1, 5),
(5, 'SKU005006', '纯棉T恤 黑色 XL', 'https://example.com/goods/5_2.jpg', 79.00, 59.00, 1, 6),
(5, 'SKU005007', '纯棉T恤 灰色 M', 'https://example.com/goods/5_1.jpg', 79.00, 59.00, 1, 7),
(5, 'SKU005008', '纯棉T恤 灰色 L', 'https://example.com/goods/5_1.jpg', 79.00, 59.00, 1, 8),
(5, 'SKU005009', '纯棉T恤 灰色 XL', 'https://example.com/goods/5_1.jpg', 79.00, 59.00, 1, 9);

-- SKU规格值关联
INSERT INTO goods_sku_spec_value(sku_id, spec_id, spec_value_id)
-- 商品1 SKU规格关联
VALUES (1, 1, 1), (1, 2, 4), (2, 1, 1), (2, 2, 5), (3, 1, 2), (3, 2, 4),
       (4, 1, 2), (4, 2, 5), (5, 1, 3), (5, 2, 4), (6, 1, 3), (6, 2, 5),
-- 商品2 SKU规格关联
       (7, 3, 6), (7, 4, 8), (8, 3, 6), (8, 4, 9), (9, 3, 7), (9, 4, 8),
       (10, 3, 7), (10, 4, 9),
-- 商品3 SKU规格关联
       (11, 5, 10), (11, 6, 12), (12, 5, 10), (12, 6, 13), (13, 5, 11), (13, 6, 12),
       (14, 5, 11), (14, 6, 13),
-- 商品4 SKU规格关联
       (15, 7, 14), (15, 8, 16), (16, 7, 14), (16, 8, 17), (17, 7, 15), (17, 8, 16),
       (18, 7, 15), (18, 8, 17),
-- 商品5 SKU规格关联
       (19, 9, 18), (19, 10, 21), (20, 9, 18), (20, 10, 22), (21, 9, 18), (21, 10, 23),
       (22, 9, 19), (22, 10, 21), (23, 9, 19), (23, 10, 22), (24, 9, 19), (24, 10, 23),
       (25, 9, 20), (25, 10, 21), (26, 9, 20), (26, 10, 22), (27, 9, 20), (27, 10, 23);
