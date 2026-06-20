package com.xtx.order.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单幂等性控制实体类
 */
@Data
@Table("order_idempotent")
public class OrderIdempotent {

    /** 主键ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 幂等键（用户ID+商品列表MD5） */
    private String idempotentKey;

    /** 业务类型 */
    private String bizType;

    /** 业务ID（订单ID） */
    private String bizId;

    /** 状态（0-处理中 1-已处理） */
    private Integer status;

    /** 请求哈希 */
    private String requestHash;

    /** 响应JSON */
    private String responseJson;

    /** 过期时间 */
    private LocalDateTime expireTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
