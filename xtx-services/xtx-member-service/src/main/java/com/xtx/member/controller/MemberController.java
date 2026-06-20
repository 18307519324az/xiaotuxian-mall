package com.xtx.member.controller;

import com.xtx.common.web.annotation.FrontController;
import com.xtx.common.web.annotation.XUserId;
import com.xtx.common.core.result.FrontResponse;
import com.xtx.member.dto.AddressDTO;
import com.xtx.member.dto.AddressVO;
import com.xtx.member.service.MemberAppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 会员前端控制器
 */
@FrontController
@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberAppService memberAppService;

    /**
     * 获取收货地址列表
     *
     * @param userId 用户ID
     * @return 地址列表
     */
    @GetMapping("/address")
    public FrontResponse<List<AddressVO>> getAddressList(@XUserId Long userId) {
        List<AddressVO> list = memberAppService.getAddressList(userId);
        return FrontResponse.success(list);
    }

    /**
     * 新增收货地址
     *
     * @param userId 用户ID
     * @param dto    地址信息
     * @return 操作结果
     */
    @PostMapping("/address")
    public FrontResponse<Void> addAddress(@XUserId Long userId, @RequestBody @Valid AddressDTO dto) {
        memberAppService.addAddress(userId, dto);
        return FrontResponse.success();
    }

    /**
     * 更新收货地址
     *
     * @param id   地址ID
     * @param dto  地址信息
     * @return 操作结果
     */
    @PutMapping("/address/{id}")
    public FrontResponse<Void> updateAddress(@PathVariable Long id, @RequestBody @Valid AddressDTO dto) {
        memberAppService.updateAddress(id, dto);
        return FrontResponse.success();
    }

    /**
     * 删除收货地址
     *
     * @param userId 用户ID
     * @param id     地址ID
     * @return 操作结果
     */
    @DeleteMapping("/address/{id}")
    public FrontResponse<Void> deleteAddress(@XUserId Long userId, @PathVariable Long id) {
        memberAppService.deleteAddress(userId, id);
        return FrontResponse.success();
    }

    /**
     * 设置默认地址
     *
     * @param userId 用户ID
     * @param id     地址ID
     * @return 操作结果
     */
    @PutMapping("/address/{id}/default")
    public FrontResponse<Void> setDefaultAddress(@XUserId Long userId, @PathVariable Long id) {
        memberAppService.setDefaultAddress(userId, id);
        return FrontResponse.success();
    }

    /**
     * 获取用户收藏列表
     *
     * @param userId      用户ID
     * @param page        页码
     * @param pageSize    每页大小
     * @param collectType 收藏类型（可选）
     * @return 收藏列表及分页信息
     */
    @GetMapping("/collect")
    public FrontResponse<Map<String, Object>> getCollectList(@XUserId Long userId,
                                                               @RequestParam Integer page,
                                                               @RequestParam Integer pageSize,
                                                               @RequestParam(required = false) Integer collectType) {
        Map<String, Object> result = memberAppService.getCollectList(userId, page, pageSize, collectType);
        return FrontResponse.success(result);
    }
}
