package com.xtx.member.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xtx.member.entity.UserAddress;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * 用户收货地址 Mapper 接口
 */
@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddress> {

    /**
     * 根据用户ID查询地址列表，按默认地址优先、更新时间降序排列
     *
     * @param userId 用户ID
     * @return 地址列表
     */
    default List<UserAddress> selectByUserId(Long userId) {
        return selectListByQuery(
                QueryWrapper.create()
                        .eq("user_id", userId)
                        .orderBy("is_default", false)
                        .orderBy("update_time", false)
        );
    }

    /**
     * 根据用户ID和地址ID查询地址详情
     *
     * @param userId 用户ID
     * @param id     地址ID
     * @return 地址信息
     */
    default UserAddress selectByUserIdAndId(Long userId, Long id) {
        return selectOneByQuery(
                QueryWrapper.create()
                        .eq("user_id", userId)
                        .eq("id", id)
        );
    }
}
