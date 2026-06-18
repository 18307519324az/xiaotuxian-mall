-- 小兔鲜儿分类数据库初始化脚本
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

-- 初始化一级分类（居家、美食、服饰）
INSERT INTO category(name, parent_id, level, icon, sort_order, status) VALUES
('居家', 0, 1, 'https://example.com/icon/home.png', 1, 1),
('美食', 0, 1, 'https://example.com/icon/food.png', 2, 1),
('服饰', 0, 1, 'https://example.com/icon/clothing.png', 3, 1);

-- 初始化二级分类（每个一级分类下2个）
INSERT INTO category(name, parent_id, level, sort_order, status) VALUES
('家居生活', 1, 2, 1, 1),
('家装软饰', 1, 2, 2, 1),
('休闲零食', 2, 2, 1, 1),
('生鲜水果', 2, 2, 2, 1),
('女装', 3, 2, 1, 1),
('男装', 3, 2, 2, 1);

-- 初始化三级分类（每个二级分类下2个）
INSERT INTO category(name, parent_id, level, sort_order, status) VALUES
('收纳用品', 4, 3, 1, 1),
('厨房用品', 4, 3, 2, 1),
('装饰摆件', 5, 3, 1, 1),
('墙饰挂画', 5, 3, 2, 1),
('坚果炒货', 6, 3, 1, 1),
('饼干糕点', 6, 3, 2, 1),
('时令水果', 7, 3, 1, 1),
('海鲜水产', 7, 3, 2, 1),
('连衣裙', 8, 3, 1, 1),
('T恤', 8, 3, 2, 1),
('衬衫', 9, 3, 1, 1),
('裤装', 9, 3, 2, 1);
