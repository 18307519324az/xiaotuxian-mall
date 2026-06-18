package com.xtx.member.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户收货地址实体类
 */
@Data
@Table("user_address")
public class UserAddress {

    /** 主键ID */
    @Id
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 收货人姓名 */
    private String receiverName;

    /** 收货人电话 */
    private String receiverPhone;

    /** 省份 */
    private String province;

    /** 城市 */
    private String city;

    /** 区县 */
    private String county;

    /** 详细地址 */
    private String addressDetail;

    /** 完整地址（省市区+详细地址） */
    private String fullAddress;

    /** 邮政编码 */
    private String postalCode;

    /** 是否默认地址（0-否 1-是） */
    private Integer isDefault;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
