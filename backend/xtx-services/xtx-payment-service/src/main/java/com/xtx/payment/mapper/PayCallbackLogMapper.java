package com.xtx.payment.mapper;

import com.mybatisflex.core.BaseMapper;
import com.xtx.payment.entity.PayCallbackLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付回调日志 Mapper 接口
 */
@Mapper
public interface PayCallbackLogMapper extends BaseMapper<PayCallbackLog> {
}
