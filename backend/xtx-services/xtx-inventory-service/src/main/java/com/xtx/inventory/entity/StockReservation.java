package com.xtx.inventory.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 库存预占记录实体类
 * 对应数据库表 stock_reservation，记录订单预占库存的明细
 */
@Data
@Table("stock_reservation")
public class StockReservation {

    /** 主键ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 订单ID */
    private Long orderId;

    /** 订单编号 */
    private String orderNo;

    /** SKU ID */
    private Long skuId;

    /** 预占数量 */
    private Integer count;

    /** 状态：1-已预占，2-已扣减，3-已释放 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
