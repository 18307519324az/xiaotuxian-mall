USE xtx_order;

ALTER TABLE order_info
    ADD COLUMN coupon_id VARCHAR(64) DEFAULT NULL AFTER buyer_message,
    ADD COLUMN coupon_name VARCHAR(100) DEFAULT NULL AFTER coupon_id,
    ADD COLUMN coupon_type VARCHAR(32) DEFAULT NULL AFTER coupon_name,
    ADD COLUMN discount_goods_amount DECIMAL(10,2) DEFAULT 0.00 AFTER coupon_type,
    ADD COLUMN discount_freight_amount DECIMAL(10,2) DEFAULT 0.00 AFTER discount_goods_amount,
    ADD COLUMN discount_amount DECIMAL(10,2) DEFAULT 0.00 AFTER discount_freight_amount,
    ADD COLUMN gift_card_amount DECIMAL(10,2) DEFAULT 0.00 AFTER discount_amount,
    ADD COLUMN gift_card_code VARCHAR(64) DEFAULT NULL AFTER gift_card_amount;
