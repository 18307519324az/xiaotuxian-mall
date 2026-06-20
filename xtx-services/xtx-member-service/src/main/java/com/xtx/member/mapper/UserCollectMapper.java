package com.xtx.member.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.xtx.member.entity.UserCollect;
import org.apache.ibatis.annotations.Mapper;


/**
 * 用户收藏 Mapper 接口
 */
@Mapper
public interface UserCollectMapper extends BaseMapper<UserCollect> {

    /**
     * 分页查询用户收藏列表
     *
     * @param userId      用户ID
     * @param page        分页对象
     * @param collectType 收藏类型（可选）
     * @return 分页结果
     */
    default Page<UserCollect> selectByUserIdPage(Long userId, Page<UserCollect> page, Integer collectType) {
        QueryWrapper query = QueryWrapper.create()
                .eq("user_id", userId);
        if (collectType != null) {
            query.eq("collect_type", collectType);
        }
        query.orderBy("create_time", false);
        return paginate(page, query);
    }
}
