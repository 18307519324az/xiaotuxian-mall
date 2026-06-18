package com.xtx.member.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户收藏实体类
 */
@Data
@Table("user_collect")
public class UserCollect {

    /** 主键ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 收藏目标ID */
    private Long targetId;

    /** 收藏类型（1-商品 2-专题 3-品牌） */
    private Integer collectType;

    /** 收藏商品名称 */
    private String name;

    /** 收藏商品描述 */
    @JsonProperty("desc")
    private String description;

    /** 收藏商品价格 */
    private BigDecimal price;

    /** 收藏商品图片 */
    private String picture;

    /** 创建时间 */
    private LocalDateTime createTime;
}
