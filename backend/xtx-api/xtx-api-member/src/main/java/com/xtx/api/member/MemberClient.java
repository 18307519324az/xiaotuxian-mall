package com.xtx.api.member;

import com.xtx.api.member.dto.AddressSnapshotDTO;
import com.xtx.common.core.result.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 会员服务 Feign 远程调用客户端
 * 提供会员地址信息查询接口
 */
@FeignClient(name = "xtx-member-service", url = "${services.member:http://localhost:8110}", contextId = "memberClient", path = "/inner/members")
public interface MemberClient {

    /**
     * 查询用户指定地址的快照信息
     *
     * @param userId    用户 ID
     * @param addressId 地址 ID
     * @return 地址快照
     */
    @GetMapping("/{userId}/addresses/{addressId}")
    ApiResponse<AddressSnapshotDTO> getAddressSnapshot(@PathVariable("userId") Long userId,
                                                       @PathVariable("addressId") Long addressId);
}
