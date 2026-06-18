package com.xtx.goods.mapper;

import com.mybatisflex.core.BaseMapper;
import com.xtx.goods.entity.Topic;
import org.apache.ibatis.annotations.Mapper;

/**
 * 专题 Mapper
 * 提供专题活动的基础 CRUD 操作
 */
@Mapper
public interface TopicMapper extends BaseMapper<Topic> {

}
