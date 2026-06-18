package com.xtx.member.dto;

import lombok.Data;

/**
 * 收货地址前端响应 VO
 * 字段名与前端契约对齐
 */
@Data
public class AddressVO {

    /** 地址ID */
    private String id;

    /** 收货人姓名 */
    private String receiver;

    /** 收货人电话 */
    private String contact;

    /** 省份编码 */
    private String provinceCode;

    /** 城市编码 */
    private String cityCode;

    /** 区县编码 */
    private String countyCode;

    /** 完整地址（省市区拼接） */
    private String fullLocation;

    /** 详细地址 */
    private String address;

    /** 邮政编码 */
    private String postalCode;

    /** 是否默认地址（0-否 1-是） */
    private Integer isDefault;

    /** 地址标签 */
    private String addressTags;
}
