package com.xtx.member.controller;

import com.xtx.common.core.result.ApiResponse;
import com.xtx.member.dto.AddressSnapshotDTO;
import com.xtx.member.service.MemberAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 会员内部控制器（服务间调用）
 */
@RestController
@RequestMapping("/inner/members")
@RequiredArgsConstructor
public class MemberInnerController {

    private final MemberAppService memberAppService;

    /**
     * 获取地址快照（供订单服务下单时使用）
     *
     * @param userId    用户ID
     * @param addressId 地址ID
     * @return 地址快照
     */
    @GetMapping("/{userId}/addresses/{addressId}")
    public ApiResponse<AddressSnapshotDTO> getAddressSnapshot(@PathVariable Long userId,
                                                                @PathVariable Long addressId) {
        AddressSnapshotDTO snapshot = memberAppService.getAddressSnapshot(userId, addressId);
        return ApiResponse.success(snapshot);
    }
}
