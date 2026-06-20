package com.xtx.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 收货地址DTO
 */
@Data
public class AddressDTO {

    /** 收货人姓名 */
    @NotBlank(message = "收货人姓名不能为空")
    private String receiverName;

    /** 收货人电话 */
    @NotBlank(message = "收货人电话不能为空")
    private String receiverPhone;

    /** 省份 */
    @NotBlank(message = "省份不能为空")
    private String province;

    /** 城市 */
    @NotBlank(message = "城市不能为空")
    private String city;

    /** 区县 */
    @NotBlank(message = "区县不能为空")
    private String county;

    /** 详细地址 */
    @NotBlank(message = "详细地址不能为空")
    private String addressDetail;

    /** 邮政编码 */
    private String postalCode;

    /** 是否默认地址 */
    private Boolean isDefault;
}
