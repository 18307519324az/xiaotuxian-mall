/*
 * ============================================
 * V3: Fix goods_spec_value.spec_id 和 goods_sku_spec_value.spec_id
 *
 * 问题描述:
 *   数据导入时 goods_spec_value.spec_id 和 goods_sku_spec_value.spec_id
 *   全部被设为 0，导致无法关联到 goods_spec 表。
 *   后果：商品详情页规格区域空白（有 spec 名称但无 values 列表）。
 *
 * 修复方法:
 *   利用 goods_sku_spec_value 在每个 SKU 内的插入顺序（按 id ASC）
 *   确定每个 spec_value 对应的 spec 位置（第 0 个 → 第 0 个 spec，依此类推）。
 *
 * 涉及商品: 158 个单规格 + 63 个双规格 + 2 个三规格
 * ============================================
 */

-- Step 1: 更新 goods_sku_spec_value.spec_id
-- 原理：每个 SKU 的 gssv 记录按 id 排序后的第 N 条对应 goods_spec 的第 N 个 spec
UPDATE goods_sku_spec_value gssv
JOIN (
    -- 子查询：为每个 SKU 的每条 gssv 计算位置序号
    SELECT
        gssv2.id,
        ROW_NUMBER() OVER (PARTITION BY gssv2.sku_id ORDER BY gssv2.id) - 1 AS pos
    FROM goods_sku_spec_value gssv2
    WHERE gssv2.spec_id = 0
) sq ON gssv.id = sq.id
JOIN goods_sku sku ON gssv.sku_id = sku.id
JOIN (
    -- 子查询：为每个商品的每个 spec 计算位置序号
    SELECT
        gs.id AS spec_id,
        gs.goods_id,
        ROW_NUMBER() OVER (PARTITION BY gs.goods_id ORDER BY gs.sort, gs.id) - 1 AS pos
    FROM goods_spec gs
) gsp ON sku.goods_id = gsp.goods_id AND sq.pos = gsp.pos
SET gssv.spec_id = gsp.spec_id
WHERE gssv.spec_id = 0;

-- Step 2: 更新 goods_spec_value.spec_id（通过已修复的 goods_sku_spec_value 回写）
-- 安全措施：仅当 spec_value 只属于一个 spec_id 时才更新
UPDATE goods_spec_value gsv
JOIN (
    SELECT gssv.spec_value_id, MIN(gssv.spec_id) AS correct_spec_id
    FROM goods_sku_spec_value gssv
    WHERE gssv.spec_id != 0
    GROUP BY gssv.spec_value_id
    HAVING MIN(gssv.spec_id) = MAX(gssv.spec_id)  -- 确保所有引用指向同一 spec
) fix ON gsv.id = fix.spec_value_id
SET gsv.spec_id = fix.correct_spec_id
WHERE gsv.spec_id = 0;

-- Step 3: 验证修复结果
SELECT '=== 修复统计 ===' AS '';
SELECT CONCAT('goods_sku_spec_value 剩余 spec_id=0: ', COUNT(*)) AS result
FROM goods_sku_spec_value WHERE spec_id = 0;
SELECT CONCAT('goods_spec_value 剩余 spec_id=0: ', COUNT(*)) AS result
FROM goods_spec_value WHERE spec_id = 0;
SELECT CONCAT('goods_sku_spec_value 已修复 spec_id>0: ', COUNT(*)) AS result
FROM goods_sku_spec_value WHERE spec_id > 0;
SELECT CONCAT('goods_spec_value 已修复 spec_id>0: ', COUNT(*)) AS result
FROM goods_spec_value WHERE spec_id > 0;

-- Step 4: 若仍有残留的 spec_id=0，记录警告
SELECT CONCAT('WARNING: 仍有 ', COUNT(*), ' 条 goods_spec_value.spec_id=0') AS warning
FROM goods_spec_value WHERE spec_id = 0
HAVING COUNT(*) > 0;

SELECT CONCAT('WARNING: 仍有 ', COUNT(*), ' 条 goods_sku_spec_value.spec_id=0') AS warning
FROM goods_sku_spec_value WHERE spec_id = 0
HAVING COUNT(*) > 0;
