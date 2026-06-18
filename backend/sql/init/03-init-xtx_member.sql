-- ============================================
-- 03-init-xtx_member.sql
-- 小兔鲜儿会员数据库初始化
-- v1.7: 合并 sql/xtx_member/V1__init.sql + Flyway V2 变更
-- ============================================

CREATE DATABASE IF NOT EXISTS xtx_member DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE xtx_member;

-- 用户收货地址表
CREATE TABLE IF NOT EXISTS user_address (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    receiver_name VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    receiver_phone VARCHAR(20) NOT NULL COMMENT '收货人电话',
    province VARCHAR(50) NOT NULL COMMENT '省份',
    city VARCHAR(50) NOT NULL COMMENT '城市',
    county VARCHAR(50) NOT NULL COMMENT '区县',
    address_detail VARCHAR(200) NOT NULL COMMENT '详细地址',
    full_address VARCHAR(500) DEFAULT NULL COMMENT '完整地址（省市区+详细地址）',
    postal_code VARCHAR(10) DEFAULT NULL COMMENT '邮政编码',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认地址（0-否 1-是）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收货地址表';

-- 用户收藏表
CREATE TABLE IF NOT EXISTS user_collect (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    target_id BIGINT NOT NULL COMMENT '收藏目标ID',
    collect_type TINYINT NOT NULL COMMENT '收藏类型（1-商品 2-专题 3-品牌）',
    name VARCHAR(200) DEFAULT '' COMMENT '收藏商品名称（V2 新增）',
    description VARCHAR(500) DEFAULT '' COMMENT '收藏商品描述（V2 新增）',
    price DECIMAL(10,2) DEFAULT 0.00 COMMENT '收藏商品价格（V2 新增）',
    picture VARCHAR(500) DEFAULT '' COMMENT '收藏商品图片（V2 新增）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_target (user_id, target_id, collect_type),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏表';

-- 初始化种子地址（用户ID=1的默认地址）
INSERT INTO user_address(user_id, receiver_name, receiver_phone, province, city, county, address_detail, full_address, postal_code, is_default)
VALUES(1, '张三', '13800000001', '广东省', '深圳市', '南山区', '科技园南区A栋1001室', '广东省深圳市南山区科技园南区A栋1001室', '518000', 1);
