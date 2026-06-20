package com.xtx.order.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单状态变更日志实体类
 */
@Data
@Table("order_status_log")
public class OrderStatusLog {

    /** 主键ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 订单ID */
    private Long orderId;

    /** 变更前状态 */
    private Integer fromState;

    /** 变更后状态 */
    private Integer toState;

    /** 操作人 */
    private String operator;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;
}
