package com.xtx.payment.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 支付回调日志实体类
 */
@Data
@Table("pay_callback_log")
public class PayCallbackLog {

    /** 主键ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 支付单号 */
    private String payNo;

    /** 回调渠道 */
    private String channel;

    /** 原始回调数据 */
    private String rawCallbackData;

    /** 处理状态（0-未处理 1-处理成功 2-处理失败） */
    private Integer processed;

    /** 错误信息 */
    private String errorMsg;

    /** 创建时间 */
    private LocalDateTime createTime;
}
