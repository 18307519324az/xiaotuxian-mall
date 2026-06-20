package com.xtx.logistics.entity;

import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单物流信息实体类
 */
@Data
@Table("order_logistics")
public class OrderLogistics {

    /** 主键ID */
    private Long id;

    /** 订单ID */
    private Long orderId;

    /** 物流单号 */
    private String logisticsNo;

    /** 物流公司名称 */
    private String companyName;

    /** 物流公司编码 */
    private String companyCode;

    /** 物流状态（1-已揽收 2-运输中 3-派送中 4-已签收） */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
