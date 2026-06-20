package com.xtx.home.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.xtx.common.mybatisflex.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 首页横幅实体类
 * 对应数据库表 home_banner，存储首页轮播图信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("home_banner")
public class HomeBanner extends BaseEntity {

    /** 主键ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 横幅图片 URL */
    private String imgUrl;

    /** 点击跳转链接 */
    private String hrefUrl;

    /** 横幅类型 */
    private String type;

    /** 排序权重 */
    private Integer sort;

    /** 状态：0-下架，1-上架 */
    private Integer status;
}
