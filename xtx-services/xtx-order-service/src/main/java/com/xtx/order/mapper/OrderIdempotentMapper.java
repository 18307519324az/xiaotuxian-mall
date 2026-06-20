package com.xtx.order.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xtx.order.entity.OrderIdempotent;
import org.apache.ibatis.annotations.Mapper;


/**
 * 订单幂等性 Mapper 接口
 */
@Mapper
public interface OrderIdempotentMapper extends BaseMapper<OrderIdempotent> {

    /**
     * 根据用户ID和幂等键查询幂等记录
     *
     * @param userId   用户ID
     * @param key      幂等键
     * @param bizType  业务类型
     * @return 幂等记录
     */
    default OrderIdempotent selectLatestByUserIdAndKey(Long userId, String key, String bizType) {
        return selectOneByQuery(
                QueryWrapper.create()
                        .eq("user_id", userId)
                        .eq("idempotent_key", key)
                        .eq("biz_type", bizType)
                        .orderBy("create_time", false)
                        .orderBy("id", false)
                        .limit(1)
        );
    }
}
