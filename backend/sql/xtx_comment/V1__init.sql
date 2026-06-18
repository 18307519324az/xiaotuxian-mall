-- 小兔鲜儿评价数据库初始化脚本
CREATE DATABASE IF NOT EXISTS xtx_comment DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE xtx_comment;

-- 商品评价表
CREATE TABLE IF NOT EXISTS goods_comment (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    goods_id BIGINT NOT NULL COMMENT '商品SPU ID',
    order_id BIGINT DEFAULT NULL COMMENT '订单ID',
    sku_id BIGINT DEFAULT NULL COMMENT 'SKU ID',
    user_id BIGINT DEFAULT NULL COMMENT '用户ID',
    content TEXT DEFAULT NULL COMMENT '评价内容',
    score TINYINT DEFAULT 5 COMMENT '评分（1-5星）',
    tags VARCHAR(500) DEFAULT NULL COMMENT '标签（JSON数组字符串，如"品质好,发货快"）',
    has_picture TINYINT DEFAULT 0 COMMENT '是否有图（0-无 1-有）',
    is_audit TINYINT DEFAULT 1 COMMENT '审核状态（0-待审核 1-审核通过 2-审核不通过）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_goods_id (goods_id),
    KEY idx_order_id (order_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评价表';

-- 评价图片表
CREATE TABLE IF NOT EXISTS goods_comment_picture (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    comment_id BIGINT NOT NULL COMMENT '评价ID',
    picture_url VARCHAR(500) NOT NULL COMMENT '图片URL',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_comment_id (comment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价图片表';
