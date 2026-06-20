package com.xtx.order.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("stock_compensation_task")
public class StockCompensationTask {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String orderNo;

    private String requestId;

    private Long skuId;

    private Integer count;

    private Integer status;

    private String failReason;

    private Integer retryCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
