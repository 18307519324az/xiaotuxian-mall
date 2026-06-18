-- ============================================
-- V6: 创建专题表 + 专题商品关联表
--
-- 配合 v1.5 专题详情真实服务只读切片:
-- topic 表存储专题基本信息（标题、封面、统计数）
-- special_goods 表存储专题与商品的关联关系
-- ============================================

CREATE TABLE IF NOT EXISTS topic (
    id VARCHAR(64) NOT NULL PRIMARY KEY COMMENT '专题ID（兼容 Mock 字符串 ID，如 v0.9.7-topic-kitchen）',
    title VARCHAR(200) NOT NULL COMMENT '专题标题',
    summary VARCHAR(500) DEFAULT '' COMMENT '专题摘要',
    cover VARCHAR(500) DEFAULT '' COMMENT '专题封面图 URL',
    collect_num INT DEFAULT 0 COMMENT '收藏数',
    view_num INT DEFAULT 0 COMMENT '浏览数',
    reply_num INT DEFAULT 0 COMMENT '评论数',
    lowest_price DECIMAL(10,2) DEFAULT 0.00 COMMENT '最低价格',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    sort_order INT DEFAULT 0 COMMENT '排序权重',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专题活动表';

CREATE TABLE IF NOT EXISTS special_goods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    special_id VARCHAR(64) NOT NULL COMMENT '专题ID，关联 topic.id',
    goods_id BIGINT NOT NULL COMMENT '商品ID，关联 goods.id',
    sort_order INT DEFAULT 0 COMMENT '排序权重',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_special_id (special_id),
    INDEX idx_goods_id (goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专题商品关联表';
