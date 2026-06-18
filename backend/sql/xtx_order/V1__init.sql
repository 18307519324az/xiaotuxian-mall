-- 小兔鲜儿订单数据库初始化脚本
CREATE DATABASE IF NOT EXISTS xtx_order DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE xtx_order;

-- 订单信息表
CREATE TABLE IF NOT EXISTS order_info (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    order_no VARCHAR(50) NOT NULL COMMENT '订单编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    pay_money DECIMAL(10,2) DEFAULT 0.00 COMMENT '实付金额（含运费）',
    total_money DECIMAL(10,2) DEFAULT 0.00 COMMENT '商品总金额',
    post_fee DECIMAL(10,2) DEFAULT 0.00 COMMENT '运费',
    total_num INT DEFAULT 0 COMMENT '商品总件数',
    order_state TINYINT DEFAULT 1 COMMENT '订单状态（1-待付款 2-待发货 3-待收货 4-待评价 5-已完成 6-已取消）',
    delivery_time_type TINYINT DEFAULT 1 COMMENT '配送时间类型（1-不限 2-工作日 3-周末/节假日）',
    pay_type TINYINT DEFAULT 1 COMMENT '支付方式（1-在线支付 2-货到付款）',
    pay_channel TINYINT DEFAULT 1 COMMENT '支付渠道（1-微信 2-支付宝 3-模拟支付）',
    buyer_message VARCHAR(500) DEFAULT NULL COMMENT '买家留言',
    receiver_name VARCHAR(50) DEFAULT NULL COMMENT '收货人姓名',
    receiver_phone VARCHAR(20) DEFAULT NULL COMMENT '收货人电话',
    receiver_address JSON DEFAULT NULL COMMENT '收货人地址（JSON快照）',
    pay_time DATETIME DEFAULT NULL COMMENT '支付时间',
    delivery_time DATETIME DEFAULT NULL COMMENT '发货时间',
    consign_time DATETIME DEFAULT NULL COMMENT '确认收货时间',
    end_time DATETIME DEFAULT NULL COMMENT '订单完成时间',
    evaluation_time DATETIME DEFAULT NULL COMMENT '评价时间',
    cancel_reason VARCHAR(500) DEFAULT NULL COMMENT '取消原因',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除（0-未删除 1-已删除）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user_id (user_id),
    KEY idx_order_state (order_state),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单信息表';

-- 订单商品快照表
CREATE TABLE IF NOT EXISTS order_goods (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    sku_id BIGINT NOT NULL COMMENT 'SKU ID',
    goods_id BIGINT DEFAULT NULL COMMENT '商品SPU ID',
    goods_name VARCHAR(200) DEFAULT NULL COMMENT '商品名称',
    goods_image VARCHAR(500) DEFAULT NULL COMMENT '商品图片',
    attrs_text VARCHAR(500) DEFAULT NULL COMMENT '规格文本（如：颜色:黑色 尺寸:M）',
    price DECIMAL(10,2) DEFAULT 0.00 COMMENT '单价',
    count INT DEFAULT 1 COMMENT '购买数量',
    total_price DECIMAL(10,2) DEFAULT 0.00 COMMENT '小计金额',
    total_pay_price DECIMAL(10,2) DEFAULT 0.00 COMMENT '小计实付金额',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_order_id (order_id),
    KEY idx_sku_id (sku_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单商品快照表';

-- 订单状态变更日志表
CREATE TABLE IF NOT EXISTS order_status_log (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    from_state TINYINT DEFAULT NULL COMMENT '变更前状态',
    to_state TINYINT NOT NULL COMMENT '变更后状态',
    operator VARCHAR(100) DEFAULT 'system' COMMENT '操作人',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单状态变更日志表';

-- 订单幂等性控制表
CREATE TABLE IF NOT EXISTS order_idempotent (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    idempotent_key VARCHAR(255) NOT NULL COMMENT '幂等键（用户ID+商品列表MD5）',
    biz_type VARCHAR(50) NOT NULL COMMENT '业务类型',
    biz_id VARCHAR(100) DEFAULT NULL COMMENT '业务ID（订单ID）',
    status TINYINT DEFAULT 0 COMMENT '状态（0-处理中 1-已处理）',
    request_hash VARCHAR(255) DEFAULT NULL COMMENT '请求哈希',
    response_json TEXT DEFAULT NULL COMMENT '响应JSON',
    expire_time DATETIME DEFAULT NULL COMMENT '过期时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_user_key (user_id, idempotent_key(100)),
    KEY idx_expire_time (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单幂等性控制表';
