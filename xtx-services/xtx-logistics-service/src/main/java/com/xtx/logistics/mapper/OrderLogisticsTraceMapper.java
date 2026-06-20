package com.xtx.logistics.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xtx.logistics.entity.OrderLogisticsTrace;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * 物流轨迹 Mapper 接口
 */
@Mapper
public interface OrderLogisticsTraceMapper extends BaseMapper<OrderLogisticsTrace> {

    /**
     * 根据物流ID查询轨迹列表，按时间升序排列
     *
     * @param logisticsId 物流ID
     * @return 轨迹列表
     */
    default List<OrderLogisticsTrace> selectByLogisticsId(Long logisticsId) {
        return selectListByQuery(
                QueryWrapper.create()
                        .eq("logistics_id", logisticsId)
                        .orderBy("id", true)
        );
    }
}
