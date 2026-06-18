-- ============================================
-- V7: 修正 BaseEntity 字段名不匹配问题
-- BaseEntity.createTime → create_time (而非 created_time)
-- BaseEntity.updateTime → update_time (而非 updated_time)
-- ============================================

-- 1. home_banner: created_time → create_time, updated_time → update_time
ALTER TABLE `home_banner`
    CHANGE COLUMN `created_time` `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    CHANGE COLUMN `updated_time` `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- 2. home_brand: created_time → create_time, updated_time → update_time
ALTER TABLE `home_brand`
    CHANGE COLUMN `created_time` `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    CHANGE COLUMN `updated_time` `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- 3. home_special: created_time → create_time, updated_time → update_time
ALTER TABLE `home_special`
    CHANGE COLUMN `created_time` `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    CHANGE COLUMN `updated_time` `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- 4. home_special_goods: 新增缺少的字段
ALTER TABLE `home_special_goods`
    ADD COLUMN `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    ADD COLUMN `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- 5. home_floor: created_time → create_time, updated_time → update_time
ALTER TABLE `home_floor`
    CHANGE COLUMN `created_time` `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    CHANGE COLUMN `updated_time` `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
