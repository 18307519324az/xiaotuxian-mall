-- ============================================
-- v1.2 分类真实服务切片种子数据说明
--
-- 注意：实际数据已通过 Flyway V2__init_category_data.sql 初始化。
-- 此文件仅作为文档，记录 v1.2 验收时的分类数据基准。
-- 如需重置数据库，直接运行 Flyway 迁移即可。
-- ============================================

-- 一级分类（9 个，符合 v1.2 要求 ≥ 8 个）
-- 每个一级分类都有有效的中文名称和图片 URL
SELECT id, name, picture_url, level, status FROM category WHERE level = 1 ORDER BY sort DESC;

-- 二级分类数量检查
SELECT CONCAT('一级分类 ', c.name, ' 下有 ', COUNT(*), ' 个二级分类') AS summary
FROM category c
JOIN category child ON child.parent_id = c.id AND child.level = 2
WHERE c.level = 1
GROUP BY c.id, c.name
ORDER BY c.sort DESC;

-- 所有分类图片非空检查
SELECT COUNT(*) AS total_categories,
       SUM(CASE WHEN name IS NULL OR name = '' THEN 1 ELSE 0 END) AS empty_name,
       SUM(CASE WHEN picture_url IS NULL OR picture_url = '' THEN 1 ELSE 0 END) AS empty_picture
FROM category WHERE status = 1;
