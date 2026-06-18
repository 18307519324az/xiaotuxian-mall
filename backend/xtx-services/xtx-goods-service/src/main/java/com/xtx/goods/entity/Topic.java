package com.xtx.goods.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 专题活动实体类
 * 对应数据库表 topic，存储精选专题活动数据
 * id 为 VARCHAR 类型，兼容 Mock 的字符串 ID（如 v0.9.7-topic-kitchen）
 */
@Data
@Table("topic")
public class Topic {

    /** 专题ID（VARCHAR，兼容 Mock 字符串 ID） */
    @Id
    private String id;

    /** 专题标题 */
    private String title;

    /** 专题摘要 */
    private String summary;

    /** 专题封面图 URL */
    private String cover;

    /** 收藏数 */
    private Integer collectNum;

    /** 浏览数 */
    private Integer viewNum;

    /** 评论数 */
    private Integer replyNum;

    /** 最低价格（元） */
    private BigDecimal lowestPrice;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    /** 排序权重 */
    private Integer sortOrder;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
