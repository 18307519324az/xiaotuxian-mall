-- ============================================
-- V5: 首页模块建表
-- ============================================

-- 1. 首页横幅表
CREATE TABLE IF NOT EXISTS `home_banner` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `img_url`      VARCHAR(512) NOT NULL DEFAULT '' COMMENT '横幅图片 URL',
    `href_url`     VARCHAR(512) NOT NULL DEFAULT '' COMMENT '跳转链接',
    `type`         VARCHAR(32)  NOT NULL DEFAULT '1' COMMENT '横幅类型',
    `sort`         INT          NOT NULL DEFAULT 0 COMMENT '排序权重',
    `status`       TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `deleted`      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `created_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页横幅表';

-- 2. 首页品牌表
CREATE TABLE IF NOT EXISTS `home_brand` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`         VARCHAR(128) NOT NULL DEFAULT '' COMMENT '品牌名称',
    `picture`      VARCHAR(512) NOT NULL DEFAULT '' COMMENT '品牌图片',
    `logo`         VARCHAR(512) NOT NULL DEFAULT '' COMMENT '品牌 Logo',
    `sort`         INT          NOT NULL DEFAULT 0 COMMENT '排序权重',
    `status`       TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `deleted`      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `created_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页品牌表';

-- 3. 首页专题表
CREATE TABLE IF NOT EXISTS `home_special` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title`        VARCHAR(256) NOT NULL DEFAULT '' COMMENT '专题标题',
    `cover`        VARCHAR(512) NOT NULL DEFAULT '' COMMENT '专题封面图',
    `summary`      VARCHAR(512) NOT NULL DEFAULT '' COMMENT '专题摘要',
    `lowest_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '最低价格',
    `sort`         INT          NOT NULL DEFAULT 0 COMMENT '排序权重',
    `status`       TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `deleted`      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `created_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页专题表';

-- 4. 专题商品关联表
CREATE TABLE IF NOT EXISTS `home_special_goods` (
    `id`           BIGINT  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `special_id`   BIGINT  NOT NULL DEFAULT 0 COMMENT '专题ID',
    `goods_id`     BIGINT  NOT NULL DEFAULT 0 COMMENT '商品ID',
    `sort`         INT     NOT NULL DEFAULT 0 COMMENT '排序权重',
    `status`       TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `deleted`      TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_special_id` (`special_id`),
    KEY `idx_goods_id` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专题商品关联表';

-- 5. 首页楼层表
CREATE TABLE IF NOT EXISTS `home_floor` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `category_id`   BIGINT       NOT NULL DEFAULT 0 COMMENT '一级分类ID',
    `category_name` VARCHAR(128) NOT NULL DEFAULT '' COMMENT '一级分类名称（冗余）',
    `picture`       VARCHAR(512) NOT NULL DEFAULT '' COMMENT '楼层运营图片',
    `sale_info`     VARCHAR(256) NOT NULL DEFAULT '' COMMENT '促销文案',
    `sort`          INT          NOT NULL DEFAULT 0 COMMENT '排序权重',
    `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `deleted`       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `created_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`),
    KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页楼层表';
