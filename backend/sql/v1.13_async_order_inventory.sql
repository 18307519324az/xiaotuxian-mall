USE xtx_inventory;

ALTER TABLE stock_sku
    ADD COLUMN IF NOT EXISTS version INT DEFAULT 1 COMMENT '乐观锁版本号';

ALTER TABLE stock_reservation
    ADD UNIQUE KEY uk_order_sku(order_no, sku_id);

INSERT INTO stock_sku (sku_id, total_stock, available_stock, locked_stock, sold_stock, version)
VALUES
    (1027026, 1000, 1000, 0, 0, 1),
    (1027027, 1000, 1000, 0, 0, 1)
ON DUPLICATE KEY UPDATE
    total_stock = VALUES(total_stock),
    available_stock = VALUES(available_stock),
    locked_stock = VALUES(locked_stock),
    sold_stock = VALUES(sold_stock),
    version = VALUES(version);

USE xtx_order;

ALTER TABLE order_info
    ADD UNIQUE KEY uk_order_no(order_no);

CREATE TABLE IF NOT EXISTS stock_compensation_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(64) NOT NULL,
    request_id VARCHAR(64) NOT NULL,
    sku_id BIGINT NOT NULL,
    count INT NOT NULL,
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0待处理 1成功 2失败',
    fail_reason VARCHAR(500),
    retry_count INT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL,
    update_time DATETIME
);

ALTER TABLE stock_compensation_task
    ADD UNIQUE KEY uk_compensation_order_sku(order_no, sku_id);
