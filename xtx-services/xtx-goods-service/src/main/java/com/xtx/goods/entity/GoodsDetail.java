package com.xtx.goods.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品详情实体类
 * 对应数据库表 goods_detail，存储商品的详细描述图片和属性
 */
@Data
@Table("goods_detail")
public class GoodsDetail {

    /** 主键ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 商品ID */
    private Long goodsId;

    /** 详情图片地址列表（JSON 数组格式），对应 SQL 列 detail_images */
    @Column("detail_images")
    private String detailPictures;

    /** 详情属性列表（JSON 格式），对应 SQL 列 properties */
    @Column("properties")
    private String detailProperties;

    /** 详情HTML */
    private String detailHtml;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
