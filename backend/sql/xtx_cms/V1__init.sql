-- 小兔鲜儿内容管理数据库初始化脚本
CREATE DATABASE IF NOT EXISTS xtx_cms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE xtx_cms;

-- 首页轮播图表
CREATE TABLE IF NOT EXISTS home_banner (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    img_url VARCHAR(500) NOT NULL COMMENT '图片URL',
    href_url VARCHAR(500) DEFAULT NULL COMMENT '跳转链接',
    type VARCHAR(20) DEFAULT 'banner' COMMENT '类型（banner-轮播图）',
    sort_order INT DEFAULT 0 COMMENT '排序',
    title VARCHAR(100) DEFAULT NULL COMMENT '标题',
    status TINYINT DEFAULT 1 COMMENT '状态（0-禁用 1-启用）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页轮播图表';

-- 首页面板表（人气推荐/热门品牌等）
CREATE TABLE IF NOT EXISTS home_panel (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    title VARCHAR(100) NOT NULL COMMENT '标题',
    subtitle VARCHAR(200) DEFAULT NULL COMMENT '副标题',
    type VARCHAR(20) NOT NULL COMMENT '类型（NEW-新鲜好物 HOT-人气推荐 BRAND-热门品牌）',
    cover VARCHAR(500) DEFAULT NULL COMMENT '封面图',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态（0-禁用 1-启用）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页面板表';

-- 专题表
CREATE TABLE IF NOT EXISTS special (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    title VARCHAR(200) NOT NULL COMMENT '专题标题',
    subtitle VARCHAR(500) DEFAULT NULL COMMENT '专题副标题',
    cover VARCHAR(500) DEFAULT NULL COMMENT '专题封面',
    content TEXT DEFAULT NULL COMMENT '专题内容',
    status TINYINT DEFAULT 1 COMMENT '状态（0-禁用 1-启用）',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专题表';

-- 专题商品关联表
CREATE TABLE IF NOT EXISTS special_goods (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    special_id BIGINT NOT NULL COMMENT '专题ID',
    goods_id BIGINT NOT NULL COMMENT '商品ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_special_id (special_id),
    KEY idx_goods_id (goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专题商品关联表';

-- 初始化轮播图数据
INSERT INTO home_banner(img_url, href_url, type, sort_order, title, status) VALUES
('https://example.com/banner1.jpg', '/pages/goods/detail?id=1', 'banner', 1, '春季新品上市', 1),
('https://example.com/banner2.jpg', '/pages/special/detail?id=1', 'banner', 2, '限时特惠', 1);

-- 初始化面板数据
INSERT INTO home_panel(title, subtitle, type, cover, sort_order, status) VALUES
('新鲜好物', '新鲜好物 品质生活', 'NEW', 'https://example.com/panel_new.jpg', 1, 1),
('人气推荐', '人气推荐 爆款集结', 'HOT', 'https://example.com/panel_hot.jpg', 2, 1);

-- 初始化专题数据
INSERT INTO special(title, subtitle, cover, status, sort_order) VALUES
('春季焕新', '换季必备好物推荐', 'https://example.com/special1.jpg', 1, 1),
('居家好物', '提升幸福感的小物件', 'https://example.com/special2.jpg', 1, 2);
