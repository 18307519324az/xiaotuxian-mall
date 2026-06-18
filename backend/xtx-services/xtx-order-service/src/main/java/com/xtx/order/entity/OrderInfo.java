package com.xtx.order.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单信息实体类
 */
@Data
@Table("order_info")
public class OrderInfo {

    /** 主键ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 订单编号 */
    private String orderNo;

    /** 用户ID */
    private Long userId;

    /** 实付金额（含运费） */
    private BigDecimal payMoney;

    /** 商品总金额 */
    private BigDecimal totalMoney;

    /** 运费 */
    private BigDecimal postFee;

    /** 商品总件数 */
    private Integer totalNum;

    /** 订单状态：1-待付款 2-待发货 3-待收货 4-待评价 5-已完成 6-已取消 */
    private Integer orderState;

    /** 配送时间类型：1-不限 2-工作日 3-周末/节假日 */
    private Integer deliveryTimeType;

    /** 支付方式：1-在线支付 2-货到付款 */
    private Integer payType;

    /** 支付渠道：1-微信 2-支付宝 3-模拟支付 */
    private Integer payChannel;

    /** 买家留言 */
    private String buyerMessage;

    /** 收货人姓名 */
    private String receiverName;

    /** 收货人电话 */
    private String receiverPhone;

    /** 收货人地址（JSON快照，包含省市区详细地址等） */
    private String receiverAddress;

    /** 支付时间 */
    private LocalDateTime payTime;

    /** 发货时间 */
    private LocalDateTime deliveryTime;

    /** 确认收货时间 */
    private LocalDateTime consignTime;

    /** 订单完成时间 */
    private LocalDateTime endTime;

    /** 评价时间 */
    private LocalDateTime evaluationTime;

    /** 取消原因 */
    private String cancelReason;

    /** 是否删除（0-未删除 1-已删除） */
    private Integer isDeleted;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
