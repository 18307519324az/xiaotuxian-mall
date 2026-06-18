package com.xtx.comment.entity;

import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品评价实体类
 */
@Data
@Table("goods_comment")
public class GoodsComment {

    /** 主键ID */
    private Long id;

    /** 商品SPU ID */
    private Long goodsId;

    /** 订单ID */
    private Long orderId;

    /** SKU ID */
    private Long skuId;

    /** 用户ID */
    private Long userId;

    /** 评价内容 */
    private String content;

    /** 评分（1-5星） */
    private Integer score;

    /** 标签（JSON数组字符串） */
    private String tags;

    /** 是否有图（0-无 1-有） */
    private Integer hasPicture;

    /** 审核状态（0-待审核 1-审核通过 2-审核不通过） */
    private Integer isAudit;

    /** 创建时间 */
    private LocalDateTime createTime;
}
