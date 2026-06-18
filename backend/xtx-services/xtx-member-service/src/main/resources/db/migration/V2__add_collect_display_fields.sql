-- ============================================
-- V2: 添加收藏商品显示字段
-- 用于 /member/collect 接口返回商品名称、描述、价格、图片
-- ============================================

ALTER TABLE `user_collect`
    ADD COLUMN `name`        VARCHAR(200)  DEFAULT ''  COMMENT '收藏商品名称' AFTER `collect_type`,
    ADD COLUMN `description` VARCHAR(500)  DEFAULT ''  COMMENT '收藏商品描述' AFTER `name`,
    ADD COLUMN `price`       DECIMAL(10,2) DEFAULT 0.00 COMMENT '收藏商品价格' AFTER `description`,
    ADD COLUMN `picture`     VARCHAR(500)  DEFAULT ''  COMMENT '收藏商品图片' AFTER `price`;
