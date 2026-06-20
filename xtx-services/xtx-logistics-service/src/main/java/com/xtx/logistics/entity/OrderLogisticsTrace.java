package com.xtx.logistics.entity;

import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物流轨迹实体类
 */
@Data
@Table("order_logistics_trace")
public class OrderLogisticsTrace {

    /** 主键ID */
    private Long id;

    /** 物流ID */
    private Long logisticsId;

    /** 发生时间 */
    private String acceptTime;

    /** 发生地点 */
    private String acceptStation;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;
}
