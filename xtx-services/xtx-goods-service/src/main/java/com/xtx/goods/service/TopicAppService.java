package com.xtx.goods.service;

import java.util.List;
import java.util.Map;

/**
 * 专题应用服务接口
 * 提供专题详情、专题商品列表等只读查询功能
 */
public interface TopicAppService {

    /**
     * 获取专题详情（含专题下商品列表）
     * 字段兼容 Mock 基线，封面图通过 resolvedCover/coverSource 返回
     *
     * @param id 专题ID
     * @return 专题详情 Map
     */
    Map<String, Object> getTopicDetail(String id);

    /**
     * 获取专题下商品列表
     *
     * @param id 专题ID
     * @return 商品列表
     */
    List<Map<String, Object>> getTopicGoods(String id);
}
