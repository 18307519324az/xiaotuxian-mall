package com.xtx.order.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xtx.order.entity.StockCompensationTask;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StockCompensationTaskMapper extends BaseMapper<StockCompensationTask> {

    default List<StockCompensationTask> selectRetryableTasks() {
        return selectListByQuery(QueryWrapper.create()
                .in("status", 0, 2)
                .orderBy("create_time", true));
    }

    default StockCompensationTask selectByOrderNoAndSkuId(String orderNo, Long skuId) {
        return selectOneByQuery(QueryWrapper.create()
                .eq("order_no", orderNo)
                .eq("sku_id", skuId));
    }
}
