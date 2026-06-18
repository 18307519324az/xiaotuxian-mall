package com.xtx.inventory.mapper;

import com.mybatisflex.core.BaseMapper;
import com.xtx.inventory.entity.StockChangeLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存变更日志 Mapper
 * 提供库存变更日志的写入与查询
 */
@Mapper
public interface StockChangeLogMapper extends BaseMapper<StockChangeLog> {
}
