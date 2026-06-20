package com.xtx.api.member.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * 地址快照 DTO
 * 用于远程调用时获取收货地址的完整信息快照
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddressSnapshotDTO {

    /** 地址 ID */
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 收货人姓名 */
    private String receiverName;

    /** 收货人手机号 */
    private String receiverPhone;

    /** 省份 */
    private String province;

    /** 城市 */
    private String city;

    /** 区县 */
    private String county;

    /** 详细地址 */
    private String addressDetail;

    /** 完整地址（省市区+详细地址拼接） */
    private String fullAddress;
}
