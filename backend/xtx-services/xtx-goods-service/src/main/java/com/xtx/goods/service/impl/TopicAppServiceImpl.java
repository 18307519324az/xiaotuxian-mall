package com.xtx.goods.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.xtx.common.core.exception.BizException;
import com.xtx.common.core.result.ResultCode;
import com.xtx.goods.entity.Goods;
import com.xtx.goods.entity.SpecialGoods;
import com.xtx.goods.entity.Topic;
import com.xtx.goods.mapper.GoodsMapper;
import com.xtx.goods.mapper.SpecialGoodsMapper;
import com.xtx.goods.mapper.TopicMapper;
import com.xtx.goods.service.TopicAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 专题应用服务实现类
 * 提供专题详情、专题商品列表的查询与组装
 * 封面图兜底 + resolvedCover/coverSource 逻辑复刻 Mock buildSpecialDetail
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TopicAppServiceImpl implements TopicAppService {

    private final TopicMapper topicMapper;
    private final SpecialGoodsMapper specialGoodsMapper;
    private final GoodsMapper goodsMapper;

    /** 全局兜底图片（与 Mock DEFAULT_FALLBACK_IMAGE 一致） */
    private static final String FALLBACK_IMAGE = "https://yjy-xiaotuxian-dev.oss-cn-beijing.aliyuncs.com/picture/2021-04-22/7f6a7b20-7902-4b43-b9c5-f33151ef1334.jpg";

    /**
     * 判断是否为无效封面图（与 Mock isInvalidTopicCover 一致）
     */
    private boolean isInvalidCover(String url) {
        if (url == null || url.isBlank()) return true;
        String lower = url.toLowerCase();
        return lower.contains("none") || lower.contains("placeholder") ||
               lower.contains("default") || lower.contains("jnnww") ||
               lower.contains("jiangnan") || lower.contains("brand") ||
               lower.endsWith(".svg") || lower.startsWith("data:");
    }

    /**
     * 判断专题是否没有独立专题封面（v0.9.7 新增专题使用占位图，应强制使用商品图）
     */
    private boolean isFakeCoverTopic(String topicId) {
        return topicId != null && (topicId.startsWith("v0.9.7") || topicId.startsWith("v0.9.6"));
    }

    /**
     * 获取专题下的商品列表（通过 special_goods 关联查询）
     */
    private List<Goods> getTopicGoodsList(String topicId) {
        // 查询关联记录
        List<SpecialGoods> relations = specialGoodsMapper.selectBySpecialId(topicId);
        if (relations.isEmpty()) {
            return List.of();
        }

        // 提取商品ID并按关联排序
        List<Long> goodsIds = relations.stream()
                .map(SpecialGoods::getGoodsId)
                .collect(Collectors.toList());

        // 批量查询商品
        List<Goods> goodsList = goodsMapper.selectListByQuery(
                QueryWrapper.create()
                        .in("id", goodsIds)
                        .eq("status", 1)
        );

        // 按关联顺序排序
        Map<Long, Goods> goodsMap = goodsList.stream()
                .collect(Collectors.toMap(Goods::getId, g -> g, (a, b) -> a));
        return relations.stream()
                .map(r -> goodsMap.get(r.getGoodsId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 获取第一张有效商品图片
     */
    private String getFirstValidGoodsPicture(List<Goods> goodsList) {
        if (goodsList != null) {
            for (Goods g : goodsList) {
                String pic = g.getPicture();
                if (pic != null && !pic.isBlank() && !isInvalidCover(pic)) return pic;
            }
        }
        return null;
    }

    /**
     * 将 BigDecimal 元格式化为保留两位小数的字符串
     */
    private String formatPriceYuan(BigDecimal price) {
        if (price == null) return null;
        return price.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }

    /**
     * 生成专题主题（基于 ID 前缀确定主题分类）
     */
    private String buildTheme(String topicId, String title) {
        if (topicId != null) {
            if (topicId.startsWith("v0.9.7-topic-kitchen")) return "厨房美食";
            if (topicId.startsWith("v0.9.7-topic-home-storage")) return "居家生活";
            if (topicId.startsWith("v0.9.7-topic-baby")) return "母婴儿童";
            if (topicId.startsWith("v0.9.7-topic-sport")) return "运动户外";
            if (topicId.startsWith("v0.9.7-topic-digital")) return "数码办公";
            if (topicId.startsWith("v0.9.7-topic-guofeng")) return "国风服饰";
        }
        if (title != null && !title.isBlank()) {
            if (title.contains("男士") || title.contains("洗面")) return "男士护肤";
            if (title.contains("挂烫") || title.contains("蒸汽")) return "家居生活";
            if (title.contains("气垫") || title.contains("水润")) return "美妆护肤";
        }
        return "专题精选";
    }

    @Override
    public Map<String, Object> getTopicDetail(String id) {
        // 查询专题基本信息
        Topic topic = topicMapper.selectOneById(id);
        if (topic == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "专题不存在");
        }

        // 查询专题下商品
        List<Goods> goodsList = getTopicGoodsList(id);
        List<Map<String, Object>> goodsMaps = goodsList.stream()
                .map(this::goodsToMap)
                .collect(Collectors.toList());

        // 构建专题详情
        Map<String, Object> detail = new HashMap<>();
        detail.put("id", topic.getId());
        detail.put("title", topic.getTitle() != null ? topic.getTitle() : "");
        detail.put("summary", topic.getSummary() != null ? topic.getSummary() : "");
        detail.put("theme", buildTheme(topic.getId(), topic.getTitle()));
        detail.put("collectNum", topic.getCollectNum() != null ? topic.getCollectNum() : 0);
        detail.put("viewNum", topic.getViewNum() != null ? topic.getViewNum() : 0);
        detail.put("replyNum", topic.getReplyNum() != null ? topic.getReplyNum() : 0);
        detail.put("lowestPrice", topic.getLowestPrice() != null ? topic.getLowestPrice().intValue() : 0);
        detail.put("detailBlocks", List.of());
        detail.put("goods", goodsMaps);

        // 封面兜底 + resolvedCover/coverSource（复刻 Mock 逻辑）
        String coverUrl = topic.getCover();
        String firstGoodsPic = getFirstValidGoodsPicture(goodsList);
        if (firstGoodsPic == null) firstGoodsPic = FALLBACK_IMAGE;

        boolean coverNeedsReplacement = isFakeCoverTopic(topic.getId())
                || isInvalidCover(coverUrl);

        String resolvedCover;
        String coverSource;
        String coverGoodsId = "";

        if (coverNeedsReplacement) {
            resolvedCover = firstGoodsPic;
            coverSource = "goods-first";
            // 查找第一张有效商品图对应的商品ID
            if (goodsList != null) {
                for (Goods g : goodsList) {
                    if (g.getPicture() != null && !g.getPicture().isBlank() && !isInvalidCover(g.getPicture())) {
                        coverGoodsId = String.valueOf(g.getId());
                        break;
                    }
                }
            }
        } else {
            resolvedCover = coverUrl;
            coverSource = "topic-cover";
        }

        // 兼容 Mock 的字段命名
        detail.put("cover", resolvedCover);
        detail.put("banner", resolvedCover);
        detail.put("resolvedCover", resolvedCover);
        detail.put("coverSource", coverSource);
        detail.put("coverGoodsId", coverGoodsId);

        // 兼容字段（防止 undefined）
        detail.putIfAbsent("collected", false);

        return detail;
    }

    @Override
    public List<Map<String, Object>> getTopicGoods(String id) {
        // 先检查专题是否存在
        Topic topic = topicMapper.selectOneById(id);
        if (topic == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "专题不存在");
        }

        List<Goods> goodsList = getTopicGoodsList(id);
        return goodsList.stream()
                .map(this::goodsToMap)
                .collect(Collectors.toList());
    }

    /**
     * 将商品转为专题商品列表格式（与 Mock toGoodsCard 兼容）
     */
    private Map<String, Object> goodsToMap(Goods goods) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", String.valueOf(goods.getId()));
        map.put("name", goods.getName() != null ? goods.getName() : "");
        map.put("picture", goods.getPicture() != null ? goods.getPicture() : "");
        map.put("price", formatPriceYuan(goods.getPrice()));
        map.put("desc", goods.getDescription() != null ? goods.getDescription() : "");
        map.put("brandId", goods.getBrandId() != null ? String.valueOf(goods.getBrandId()) : "");
        map.put("brandName", goods.getBrandName() != null ? goods.getBrandName() : "");
        map.put("salesCount", goods.getSalesCount() != null ? goods.getSalesCount() : 0);
        map.put("collectCount", goods.getCollectCount() != null ? goods.getCollectCount() : 0);
        return map;
    }
}
