package com.xtx.logistics.service;

import com.xtx.common.core.exception.BizException;
import com.xtx.common.core.result.ResultCode;
import com.xtx.logistics.entity.OrderLogistics;
import com.xtx.logistics.entity.OrderLogisticsTrace;
import com.xtx.logistics.mapper.OrderLogisticsMapper;
import com.xtx.logistics.mapper.OrderLogisticsTraceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 物流应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogisticsAppService {

    private final OrderLogisticsMapper orderLogisticsMapper;
    private final OrderLogisticsTraceMapper orderLogisticsTraceMapper;

    /**
     * 获取订单物流信息及轨迹
     *
     * @param orderId 订单ID
     * @return 物流信息及轨迹
     */
    public Map<String, Object> getOrderLogistics(Long orderId) {
        // 查询物流信息
        OrderLogistics logistics = orderLogisticsMapper.selectByOrderId(orderId);
        if (logistics == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "物流信息不存在");
        }

        // 查询物流轨迹
        List<OrderLogisticsTrace> traces = orderLogisticsTraceMapper.selectByLogisticsId(logistics.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("logistics", logistics);
        result.put("traces", traces);
        return result;
    }
}
