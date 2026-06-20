package com.xtx.goods.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 品牌实体类
 * 对应数据库表 brand，存储商品品牌信息
 */
@Data
@Table("brand")
public class Brand {

    /** 主键ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 品牌名称 */
    private String name;

    /** 品牌描述 */
    private String desc;

    /** 品牌图片地址 */
    private String picture;

    /** 品牌首字母 */
    private String letter;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
