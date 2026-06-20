-- ============================================
-- V4: 商品表增加 brand_picture 字段
--
-- 配合 fix-goods-brand-data.js 品牌修复:
-- 为 goods 表增加 brand_picture 字段，存储品牌图片 URL。
-- 原有 brand_logo 存储品牌 Logo，brand_picture 存储品牌图片。
-- 两者在大部分场景下值相同，但语义不同。
--
-- 同时添加 brand_code 字段用于非数字品牌 ID 存储
-- (例如 Mock 中的 "spider99999999999" 等字符串 ID)。
-- ============================================

ALTER TABLE goods
    ADD COLUMN brand_picture VARCHAR(500) DEFAULT NULL COMMENT '品牌图片 URL'
    AFTER brand_logo;

ALTER TABLE goods
    ADD COLUMN brand_code VARCHAR(64) DEFAULT NULL COMMENT '品牌编码（字符串品牌 ID，兼容非数字 ID）'
    AFTER brand_picture;
