package com.xtx.member.service;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.paginate.Page;
import com.xtx.common.core.exception.BizException;
import com.xtx.common.core.result.ResultCode;
import com.xtx.member.dto.AddressDTO;
import com.xtx.member.dto.AddressSnapshotDTO;
import com.xtx.member.dto.AddressVO;
import com.xtx.member.entity.UserAddress;
import com.xtx.member.entity.UserCollect;
import com.xtx.member.mapper.UserAddressMapper;
import com.xtx.member.mapper.UserCollectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 会员应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberAppService {

    private final UserAddressMapper userAddressMapper;
    private final UserCollectMapper userCollectMapper;

    /**
     * 获取用户收货地址列表
     *
     * @param userId 用户ID
     * @return 地址列表（VO 格式，前端字段对齐）
     */
    public List<AddressVO> getAddressList(Long userId) {
        List<UserAddress> addresses = userAddressMapper.selectByUserId(userId);
        List<AddressVO> result = new ArrayList<>(addresses.size());
        for (UserAddress addr : addresses) {
            result.add(toAddressVO(addr));
        }
        return result;
    }

    /**
     * 获取地址详情
     *
     * @param id 地址ID
     * @return 地址信息（VO 格式）
     */
    public AddressVO getAddressDetail(Long id) {
        UserAddress address = userAddressMapper.selectOneById(id);
        if (address == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "地址不存在");
        }
        return toAddressVO(address);
    }

    /**
     * 删除收货地址
     *
     * @param userId 用户ID
     * @param id     地址ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteAddress(Long userId, Long id) {
        UserAddress address = userAddressMapper.selectByUserIdAndId(userId, id);
        if (address == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "地址不存在");
        }
        userAddressMapper.deleteById(id);
        log.info("删除收货地址成功, userId={}, addressId={}", userId, id);
    }

    /**
     * 新增收货地址
     *
     * @param userId 用户ID
     * @param dto    地址信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void addAddress(Long userId, AddressDTO dto) {
        // 如果设为默认地址，先取消其他默认地址
        if (dto.getIsDefault() != null && dto.getIsDefault()) {
            clearDefaultAddress(userId);
        }

        UserAddress address = new UserAddress();
        BeanUtil.copyProperties(dto, address);
        address.setUserId(userId);
        // 拼接完整地址
        address.setFullAddress(dto.getProvince() + dto.getCity() + dto.getCounty() + dto.getAddressDetail());
        address.setIsDefault(dto.getIsDefault() != null && dto.getIsDefault() ? 1 : 0);
        address.setCreateTime(LocalDateTime.now());
        address.setUpdateTime(LocalDateTime.now());
        userAddressMapper.insert(address);
        log.info("新增收货地址成功, userId={}, addressId={}", userId, address.getId());
    }

    /**
     * 更新收货地址
     *
     * @param id  地址ID
     * @param dto 地址信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateAddress(Long id, AddressDTO dto) {
        UserAddress address = userAddressMapper.selectOneById(id);
        if (address == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "地址不存在");
        }

        // 如果设为默认地址，先取消其他默认地址
        if (dto.getIsDefault() != null && dto.getIsDefault()) {
            clearDefaultAddress(address.getUserId());
        }

        BeanUtil.copyProperties(dto, address);
        address.setFullAddress(dto.getProvince() + dto.getCity() + dto.getCounty() + dto.getAddressDetail());
        address.setIsDefault(dto.getIsDefault() != null && dto.getIsDefault() ? 1 : 0);
        address.setUpdateTime(LocalDateTime.now());
        userAddressMapper.update(address);
        log.info("更新收货地址成功, addressId={}", id);
    }

    /**
     * 设置默认地址
     *
     * @param userId 用户ID
     * @param id     地址ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultAddress(Long userId, Long id) {
        UserAddress address = userAddressMapper.selectByUserIdAndId(userId, id);
        if (address == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "地址不存在");
        }
        // 清除所有默认地址
        clearDefaultAddress(userId);
        // 设置新默认地址
        address.setIsDefault(1);
        address.setUpdateTime(LocalDateTime.now());
        userAddressMapper.update(address);
        log.info("设置默认地址成功, userId={}, addressId={}", userId, id);
    }

    /**
     * 获取用户收藏列表（分页）
     *
     * @param userId      用户ID
     * @param page        页码
     * @param pageSize    每页大小
     * @param collectType 收藏类型（可选）
     * @return 收藏列表及分页信息
     */
    public Map<String, Object> getCollectList(Long userId, Integer page, Integer pageSize, Integer collectType) {
        Page<UserCollect> pageObj = new Page<>(page, pageSize);
        Page<UserCollect> collectPage = userCollectMapper.selectByUserIdPage(userId, pageObj, collectType);

        Map<String, Object> result = new HashMap<>();
        result.put("pages", collectPage.getTotalPage());
        result.put("counts", collectPage.getTotalRow());
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("items", collectPage.getRecords());
        return result;
    }

    /**
     * 将 UserAddress 实体转换为前端适配的 AddressVO
     */
    private AddressVO toAddressVO(UserAddress addr) {
        AddressVO vo = new AddressVO();
        vo.setId(String.valueOf(addr.getId()));
        vo.setReceiver(addr.getReceiverName());
        vo.setContact(addr.getReceiverPhone());
        // 省市区编码未存储，留空（前端使用 fullLocation 展示）
        vo.setProvinceCode("");
        vo.setCityCode("");
        vo.setCountyCode("");
        vo.setFullLocation(addr.getProvince() + " " + addr.getCity() + " " + addr.getCounty());
        vo.setAddress(addr.getAddressDetail());
        vo.setPostalCode(addr.getPostalCode());
        vo.setIsDefault(addr.getIsDefault());
        vo.setAddressTags("");
        return vo;
    }

    /**
     * 获取地址快照（供内部Feign调用）
     *
     * @param userId    用户ID
     * @param addressId 地址ID
     * @return 地址快照
     */
    public AddressSnapshotDTO getAddressSnapshot(Long userId, Long addressId) {
        UserAddress address = userAddressMapper.selectByUserIdAndId(userId, addressId);
        if (address == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "地址不存在");
        }

        AddressSnapshotDTO snapshot = new AddressSnapshotDTO();
        snapshot.setReceiverName(address.getReceiverName());
        snapshot.setReceiverPhone(address.getReceiverPhone());
        snapshot.setProvince(address.getProvince());
        snapshot.setCity(address.getCity());
        snapshot.setCounty(address.getCounty());
        snapshot.setAddressDetail(address.getAddressDetail());
        snapshot.setFullAddress(address.getFullAddress());
        snapshot.setPostalCode(address.getPostalCode());
        return snapshot;
    }

    /**
     * 清除用户所有默认地址标记
     *
     * @param userId 用户ID
     */
    private void clearDefaultAddress(Long userId) {
        List<UserAddress> addresses = userAddressMapper.selectByUserId(userId);
        for (UserAddress addr : addresses) {
            if (addr.getIsDefault() != null && addr.getIsDefault() == 1) {
                addr.setIsDefault(0);
                addr.setUpdateTime(LocalDateTime.now());
                userAddressMapper.update(addr);
            }
        }
    }
}
