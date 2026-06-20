package com.xtx.goods.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品图片实体类
 * 对应数据库表 goods_picture，存储商品展示图片
 */
@Data
@Table("goods_picture")
public class GoodsPicture {

    /** 主键ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 商品ID */
    private Long goodsId;

    /** 图片地址 */
    private String pictureUrl;

    /** 是否主图：0-否，1-是 */
    private Integer isMain;

    /** 排序权重 */
    private Integer sort;

    /** 创建时间 */
    private LocalDateTime createTime;
}
