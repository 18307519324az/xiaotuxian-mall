package com.xtx.member.dto;

import lombok.Data;

/**
 * 收货地址快照DTO（供订单服务使用）
 */
@Data
public class AddressSnapshotDTO {

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

    /** 完整地址 */
    private String fullAddress;

    /** 邮政编码 */
    private String postalCode;
}
