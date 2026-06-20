package com.xtx.cms.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 首页轮播图实体类
 * 对应数据库表 home_banner，存储首页顶部轮播广告位数据
 */
@Data
@Table("home_banner")
public class HomeBanner {

    /** 主键ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 轮播图名称 */
    private String name;

    /** 轮播图图片地址 */
    private String imgUrl;

    /** 点击跳转链接地址 */
    private String linkUrl;

    /** 排序权重（值越大越靠前） */
    private Integer sort;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
