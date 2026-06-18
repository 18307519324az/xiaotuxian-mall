-- ============================================
-- V10: 初始化购物车表
-- ============================================
-- 使用逻辑删除方式，deleted=1 表示已删除
-- 订单结算后不实际删除记录，置 deleted=1
-- ============================================

CREATE TABLE IF NOT EXISTS `cart` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `user_id`       BIGINT       NOT NULL                 COMMENT '用户ID',
    `sku_id`        BIGINT       NOT NULL                 COMMENT 'SKU ID',
    `count`         INT          NOT NULL DEFAULT 1       COMMENT '购买数量',
    `selected`      TINYINT      NOT NULL DEFAULT 1       COMMENT '是否选中：1-选中，0-未选中',
    `is_effective`  TINYINT      NOT NULL DEFAULT 1       COMMENT '是否有效：1-有效，0-无效',
    `deleted`       TINYINT      NOT NULL DEFAULT 0       COMMENT '是否删除：1-删除，0-正常',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_user_sku` (`user_id`, `sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';
