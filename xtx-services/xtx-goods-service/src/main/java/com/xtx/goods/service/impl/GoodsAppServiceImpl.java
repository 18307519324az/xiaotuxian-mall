package com.xtx.goods.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xtx.goods.client.CategoryClient;
import com.xtx.goods.entity.Goods;
import com.xtx.goods.entity.GoodsDetail;
import com.xtx.goods.entity.GoodsPicture;
import com.xtx.goods.entity.GoodsSku;
import com.xtx.goods.entity.GoodsSkuSpecValue;
import com.xtx.goods.entity.GoodsSpec;
import com.xtx.goods.entity.GoodsSpecValue;
import com.xtx.goods.mapper.GoodsDetailMapper;
import com.xtx.goods.mapper.GoodsMapper;
import com.xtx.goods.mapper.GoodsPictureMapper;
import com.xtx.goods.mapper.GoodsSkuMapper;
import com.xtx.goods.mapper.GoodsSkuSpecValueMapper;
import com.xtx.goods.mapper.GoodsSpecMapper;
import com.xtx.goods.mapper.GoodsSpecValueMapper;
import com.xtx.goods.service.GoodsAppService;
import com.xtx.common.core.exception.BizException;
import com.xtx.common.core.result.ApiResponse;
import com.xtx.common.core.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 商品应用服务实现类
 * 实现商品详情的多表聚合查询、相关推荐、SKU 查询等业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoodsAppServiceImpl implements GoodsAppService {

    private final GoodsMapper goodsMapper;
    private final GoodsPictureMapper goodsPictureMapper;
    private final GoodsDetailMapper goodsDetailMapper;
    private final GoodsSpecMapper goodsSpecMapper;
    private final GoodsSpecValueMapper goodsSpecValueMapper;
    private final GoodsSkuMapper goodsSkuMapper;
    private final GoodsSkuSpecValueMapper goodsSkuSpecValueMapper;
    private final CategoryClient categoryClient;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 商品详情缓存 Key 前缀（v2 刷新 SKU 价格元格式）
     */
    private static final String CACHE_KEY_DETAIL = "goods:v2:detail:";

    /**
     * 将字符串 ID 转为 Long，解析失败时抛出 404
     */
    private Long parseLongId(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "商品不存在");
        }
    }

    @Override
    public Map<String, Object> getGoodsDetail(String id) {
        Long goodsId = parseLongId(id);

        // 优先从缓存获取
        String cacheKey = CACHE_KEY_DETAIL + goodsId;
        Map<String, Object> cached = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 查询商品基本信息
        Goods goods = goodsMapper.selectOneById(goodsId);
        if (goods == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "商品不存在");
        }

        // 构建商品详情结构（PRD 7.4.2 格式 + 前端适配字段）
        Map<String, Object> detail = new HashMap<>();

        // 基本信息（ID 转为 String 以兼容前端；price/oldPrice 转为 String "49.00" 对齐 Mock 基线）
        detail.put("id", String.valueOf(goods.getId()));
        detail.put("name", goods.getName());
        detail.put("spuCode", goods.getSpuCode());
        detail.put("desc", goods.getDescription() != null ? goods.getDescription() : "");
        detail.put("price", formatPriceYuan(goods.getPrice()));
        detail.put("oldPrice", formatPriceYuan(goods.getOldPrice()));
        detail.put("discount", goods.getDiscount());
        detail.put("picture", goods.getPicture());
        detail.put("salesCount", goods.getSalesCount());
        detail.put("commentCount", goods.getCommentCount());
        detail.put("collectCount", goods.getCollectCount());
        detail.put("isPreSale", goods.getIsPreSale() != null && goods.getIsPreSale() == 1);
        detail.put("isCollect", goods.getIsCollect() != null && goods.getIsCollect() == 1);
        detail.put("inventory", goods.getInventory());
        detail.put("tag", goods.getTag());

        // 品牌信息（优先从 goods 表的 brand_name/brand_logo 冗余字段读取，兼容非数值型品牌 ID）
        // 若 goods 表有 brand_id 且 brand 表存在，则补充更多字段
        if (goods.getBrandName() != null && !goods.getBrandName().isBlank()) {
            Map<String, Object> brandMap = new HashMap<>();
            brandMap.put("id", goods.getBrandId() != null ? String.valueOf(goods.getBrandId()) : "");
            brandMap.put("name", goods.getBrandName());
            brandMap.put("nameEn", "");
            String logo = goods.getBrandLogo() != null ? goods.getBrandLogo() : "";
            brandMap.put("picture", logo);
            brandMap.put("logo", logo);
            detail.put("brand", brandMap);
        }

        // 分类信息（从三级分类 ID 解析完整分类链并填充名称）
        List<Map<String, Object>> categories = resolveCategoryChain(goods.getCategoryId());
        detail.put("categories", categories);

        // 主图列表 + mainPictures（前端 goods-image 需要 URL 数组）
        List<GoodsPicture> pictures = goodsPictureMapper.selectByGoodsId(goodsId);
        List<String> mainPictures = new ArrayList<>();
        List<Map<String, Object>> pictureList = pictures.stream().map(pic -> {
            Map<String, Object> picMap = new HashMap<>();
            picMap.put("id", String.valueOf(pic.getId()));
            picMap.put("pictureUrl", pic.getPictureUrl());
            picMap.put("isMain", pic.getIsMain() != null && pic.getIsMain() == 1);
            mainPictures.add(pic.getPictureUrl());
            return picMap;
        }).collect(Collectors.toList());
        detail.put("pictures", pictureList);
        detail.put("mainPictures", mainPictures);

        // 商品详情（前端 goods-detail 需要 details.properties 和 details.pictures）
        GoodsDetail goodsDetail = goodsDetailMapper.selectByGoodsId(goodsId);
        if (goodsDetail != null) {
            Map<String, Object> detailMap = new HashMap<>();
            // 解析属性 JSON → [{name, value}]
            String propsJson = goodsDetail.getDetailProperties();
            if (propsJson != null && !propsJson.isBlank()) {
                try {
                    JSONObject jsonObj = JSONUtil.parseObj(propsJson);
                    List<Map<String, Object>> propList = new ArrayList<>();
                    for (Map.Entry<String, Object> entry : jsonObj.entrySet()) {
                        Map<String, Object> prop = new HashMap<>();
                        prop.put("name", entry.getKey());
                        prop.put("value", entry.getValue());
                        propList.add(prop);
                    }
                    detailMap.put("properties", propList);
                } catch (Exception e) {
                    log.warn("解析商品属性 JSON 失败: goodsId={}", goodsId, e);
                    detailMap.put("properties", List.of());
                }
            } else {
                detailMap.put("properties", List.of());
            }
            // 解析详情图片 JSON 数组 → [url1, url2, ...]
            String picsJson = goodsDetail.getDetailPictures();
            if (picsJson != null && !picsJson.isBlank()) {
                try {
                    detailMap.put("pictures", JSONUtil.parseArray(picsJson).toList(String.class));
                } catch (Exception e) {
                    log.warn("解析商品详情图片 JSON 失败: goodsId={}", goodsId, e);
                    detailMap.put("pictures", List.of());
                }
            } else {
                detailMap.put("pictures", List.of());
            }
            detail.put("details", detailMap);
        }

        // 规格维度及规格值
        List<GoodsSpec> specs = goodsSpecMapper.selectByGoodsId(goodsId);
        List<Map<String, Object>> specList = specs.stream().map(spec -> {
            Map<String, Object> specMap = new HashMap<>();
            specMap.put("id", String.valueOf(spec.getId()));
            specMap.put("name", spec.getName());
            specMap.put("sort", spec.getSort());
            // 查询该规格下的所有可选值
            List<GoodsSpecValue> values = goodsSpecValueMapper.selectBySpecId(spec.getId());
            List<Map<String, Object>> valueList = values.stream().map(val -> {
                Map<String, Object> valMap = new HashMap<>();
                valMap.put("id", String.valueOf(val.getId()));
                valMap.put("name", val.getName());
                valMap.put("picture", val.getPicture());
                return valMap;
            }).collect(Collectors.toList());
            specMap.put("values", valueList);
            return specMap;
        }).collect(Collectors.toList());
        detail.put("specs", specList);

        // 构建 specId → specName 映射（用于 SKU specs 数组）
        Map<Long, String> specNameMap = specs.stream()
                .collect(Collectors.toMap(GoodsSpec::getId, GoodsSpec::getName));

        // SKU 列表
        List<GoodsSku> skus = goodsSkuMapper.selectByGoodsId(goodsId);
        List<Map<String, Object>> skuList = skus.stream().map(sku -> {
            Map<String, Object> skuMap = new HashMap<>();
            skuMap.put("id", String.valueOf(sku.getId()));
            skuMap.put("skuCode", sku.getSkuCode());
            // 前端直接展示 {{ sku.price }}，必须返回元格式字符串 "49.00"
            skuMap.put("price", sku.getPrice() != null ? formatPriceYuan(BigDecimal.valueOf(sku.getPrice(), 2)) : null);
            skuMap.put("oldPrice", sku.getOldPrice() != null ? formatPriceYuan(BigDecimal.valueOf(sku.getOldPrice(), 2)) : null);
            skuMap.put("picture", sku.getPicture());
            // Mock 基线要求：isEffective 返回 Boolean
            skuMap.put("isEffective", sku.getIsEffective() != null && sku.getIsEffective() == 1);
            skuMap.put("inventory", sku.getInventory());
            // 构建前端 SKU specs 数组 [{name, valueName}]
            List<GoodsSkuSpecValue> skuSpecValues = goodsSkuSpecValueMapper.selectBySkuId(sku.getId());
            List<Map<String, Object>> skuSpecsList = skuSpecValues.stream().map(ssv -> {
                Map<String, Object> specItem = new HashMap<>();
                specItem.put("name", specNameMap.getOrDefault(ssv.getSpecId(), ""));
                specItem.put("valueName", ssv.getSpecValueName());
                return specItem;
            }).collect(Collectors.toList());
            skuMap.put("specs", skuSpecsList);
            // 保留原 specValues 格式以兼容
            List<Map<String, Object>> skuSpecValueList = skuSpecValues.stream().map(ssv -> {
                Map<String, Object> ssvMap = new HashMap<>();
                ssvMap.put("specId", String.valueOf(ssv.getSpecId()));
                ssvMap.put("specValueId", String.valueOf(ssv.getSpecValueId()));
                ssvMap.put("specValueName", ssv.getSpecValueName());
                return ssvMap;
            }).collect(Collectors.toList());
            skuMap.put("specValues", skuSpecValueList);
            return skuMap;
        }).collect(Collectors.toList());
        detail.put("skus", skuList);

        // 规格值降级：若 goods_spec_value 表缺少数据导致某些规格值为空，
        // 从 SKU 级 goods_sku_spec_value 中提取不同的规格值名称进行回填。
        // 这样确保前端规格按钮始终可见（有 label 就有可点击的按钮）。
        for (Map<String, Object> spec : specList) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> values = (List<Map<String, Object>>) spec.get("values");
            if (values != null && values.isEmpty()) {
                String specId = (String) spec.get("id");
                List<String> distinctNames = new ArrayList<>();
                for (Map<String, Object> skuItem : skuList) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> skuSpecValues = (List<Map<String, Object>>) skuItem.get("specValues");
                    if (skuSpecValues != null) {
                        for (Map<String, Object> ssv : skuSpecValues) {
                            if (specId.equals(ssv.get("specId"))) {
                                String name = (String) ssv.get("specValueName");
                                if (name != null && !distinctNames.contains(name)) {
                                    distinctNames.add(name);
                                }
                            }
                        }
                    }
                }
                if (!distinctNames.isEmpty()) {
                    List<Map<String, Object>> fallbackValues = new ArrayList<>();
                    for (String name : distinctNames) {
                        Map<String, Object> val = new HashMap<>();
                        val.put("id", "");
                        val.put("name", name);
                        val.put("picture", "");
                        fallbackValues.add(val);
                    }
                    spec.put("values", fallbackValues);
                    log.info("spec value fallback: goodsId={}, spec={}, derived {} values from SKU data",
                            goodsId, spec.get("name"), fallbackValues.size());
                }
            }
        }

        // 商品维度汇总字段
        int totalInventory = skus.stream()
                .mapToInt(s -> s.getInventory() != null ? s.getInventory() : 0)
                .sum();
        Integer maxOldPriceCents = skus.stream()
                .map(GoodsSku::getOldPrice)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(null);
        detail.put("inventory", totalInventory);
        // 用 SKU 最高原价覆盖 goods 原价（折扣基于 SKU 维度），需转分→元→String
        if (maxOldPriceCents != null) {
            detail.put("oldPrice", formatPriceYuan(BigDecimal.valueOf(maxOldPriceCents, 2)));
        }

        // 兼容 Mock 的额外兜底字段（前端可能使用，防止 undefined 报错）
        detail.putIfAbsent("userAddresses", List.of());
        detail.putIfAbsent("similarProducts", List.of());
        detail.putIfAbsent("mainVideos", List.of());
        detail.putIfAbsent("videoScale", 1);
        detail.putIfAbsent("hotByDay", List.of());
        detail.putIfAbsent("publishTime", "");

        // 写入缓存，10 分钟过期
        redisTemplate.opsForValue().set(cacheKey, detail, 10, TimeUnit.MINUTES);

        return detail;
    }

    /**
     * 将 Integer 分转为 BigDecimal 元，用于返回给前端
     */
    private BigDecimal formatPrice(Integer priceCents) {
        if (priceCents == null) return null;
        return BigDecimal.valueOf(priceCents, 2);
    }

    /**
     * 将 BigDecimal 元格式化为保留两位小数的字符串，与 Mock 基线 String 格式对齐
     * 如 49.00 → "49.00"，保证前端价格显示一致
     */
    private String formatPriceYuan(BigDecimal price) {
        if (price == null) return null;
        return price.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }

    /**
     * 从三级分类 ID 解析完整分类链（一级→二级→三级），并通过 Feign 填充名称。
     * <p>
     * 前端面包屑要求 categories 数组至少有 2 个元素（categories[0] 为一级、categories[1] 为二级），
     * 因此当分类链只有 1 项时（通常是商品直接挂在了一级分类下），自动补齐冗余项防止页面白屏。
     * </p>
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> resolveCategoryChain(Long categoryId) {
        if (categoryId == null) {
            return List.of();
        }
        try {
            ApiResponse<List<Map<String, Object>>> response = categoryClient.getCategoryChain(categoryId);
            if (response.getData() != null) {
                List<Map<String, Object>> chain = response.getData();
                // 前端面包屑需要至少 2 级分类，若只有 1 级则补齐
                if (chain.size() >= 2) {
                    return chain;
                }
                // 补齐：将唯一分类作为一级，再复制一份作为"二级"
                // 前端仅访问 categories[0].id/name 和 categories[1].id/name，不会因此报错
                List<Map<String, Object>> padded = new ArrayList<>(chain);
                Map<String, Object> first = chain.get(0);
                Map<String, Object> second = new HashMap<>(first);
                // 标记为二级避免前端 level 校验异常
                second.put("level", "level2");
                padded.add(second);
                return padded;
            }
        } catch (Exception e) {
            log.warn("远程查询分类链失败: categoryId={}", categoryId, e);
        }
        // 降级：仅返回 ID（同样补齐 2 级）
        List<Map<String, Object>> fallback = new ArrayList<>();
        Map<String, Object> cat = new HashMap<>();
        cat.put("id", String.valueOf(categoryId));
        cat.put("name", "分类");
        cat.put("level", "level1");
        fallback.add(cat);
        Map<String, Object> cat2 = new HashMap<>(cat);
        cat2.put("level", "level2");
        fallback.add(cat2);
        return fallback;
    }

    @Override
    public List<Map<String, Object>> getRelevantGoods(String id, Integer limit) {
        Long goodsId = parseLongId(id);
        Goods goods = goodsMapper.selectOneById(goodsId);
        if (goods == null) {
            return List.of();
        }

        if (limit == null || limit <= 0) {
            limit = 10;
        }

        // 按相同分类和销量排序
        List<Goods> relevantList = goodsMapper.selectByCategoryIdSorted(
                goods.getCategoryId(), goodsId, limit);

        return relevantList.stream().map(this::goodsToSimpleMap).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getHotGoods(String id, Integer type, Integer limit) {
        Long goodsId = parseLongId(id);
        Goods goods = goodsMapper.selectOneById(goodsId);
        if (goods == null) {
            return List.of();
        }

        if (limit == null || limit <= 0) {
            limit = 10;
        }

        // 查同类目下热门商品（按销量排序）
        List<Goods> hotList = goodsMapper.selectByCategoryIdSorted(
                goods.getCategoryId(), goodsId, limit);

        return hotList.stream().map(this::goodsToSimpleMap).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getSkuInfo(String skuId) {
        Long skuLongId = parseLongId(skuId);
        GoodsSku sku = goodsSkuMapper.selectOneById(skuLongId);
        if (sku == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "SKU 不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", String.valueOf(sku.getId()));
        result.put("goodsId", String.valueOf(sku.getGoodsId()));
        result.put("skuCode", sku.getSkuCode());
        // 前端直接展示，必须返回元格式字符串 "49.00"
        result.put("price", sku.getPrice() != null ? formatPriceYuan(BigDecimal.valueOf(sku.getPrice(), 2)) : null);
        result.put("oldPrice", sku.getOldPrice() != null ? formatPriceYuan(BigDecimal.valueOf(sku.getOldPrice(), 2)) : null);
        result.put("picture", sku.getPicture());
        result.put("inventory", sku.getInventory());
        // Mock 基线要求：isEffective 返回 Boolean
        result.put("isEffective", sku.getIsEffective() != null && sku.getIsEffective() == 1);
        result.put("attrsText", sku.getAttrsText());

        // 查询规格值组合
        List<GoodsSkuSpecValue> specValues = goodsSkuSpecValueMapper.selectBySkuId(skuLongId);
        List<Map<String, Object>> specValueList = specValues.stream().map(ssv -> {
            Map<String, Object> sv = new HashMap<>();
            sv.put("specId", String.valueOf(ssv.getSpecId()));
            sv.put("specValueId", String.valueOf(ssv.getSpecValueId()));
            sv.put("specValueName", ssv.getSpecValueName());
            return sv;
        }).collect(Collectors.toList());
        result.put("specValues", specValueList);

        return result;
    }

    @Override
    public Map<String, Object> getSkuStock(String skuId) {
        Long skuLongId = parseLongId(skuId);
        GoodsSku sku = goodsSkuMapper.selectOneById(skuLongId);
        if (sku == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "SKU 不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", String.valueOf(sku.getId()));
        result.put("stock", sku.getInventory());
        // 前端直接展示，必须返回元格式字符串 "49.00"
        result.put("price", sku.getPrice() != null ? formatPriceYuan(BigDecimal.valueOf(sku.getPrice(), 2)) : null);
        result.put("isEffective", sku.getIsEffective() != null && sku.getIsEffective() == 1);
        return result;
    }

    @Override
    public List<Map<String, Object>> listSkuSnapshots(List<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            return List.of();
        }
        List<GoodsSku> skus = goodsSkuMapper.selectBatchByIds(skuIds);
        return skus.stream().map(sku -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", String.valueOf(sku.getId()));
            map.put("goodsId", String.valueOf(sku.getGoodsId()));
            map.put("skuCode", sku.getSkuCode());
            map.put("price", sku.getPrice() != null ? formatPriceYuan(BigDecimal.valueOf(sku.getPrice(), 2)) : null);
            map.put("oldPrice", sku.getOldPrice() != null ? formatPriceYuan(BigDecimal.valueOf(sku.getOldPrice(), 2)) : null);
            map.put("picture", sku.getPicture());
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> listGoodsByIds(List<Long> goodsIds) {
        if (goodsIds == null || goodsIds.isEmpty()) {
            return List.of();
        }
        List<Goods> goodsList = goodsMapper.selectBatchByIds(goodsIds);
        return goodsList.stream().map(this::goodsToSimpleMap).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> listGoodsByCategoryIds(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return List.of();
        }
        List<Goods> goodsList = goodsMapper.selectByCategoryIds(categoryIds);
        return goodsList.stream().map(this::goodsToSimpleMap).collect(Collectors.toList());
    }

    /**
     * 将商品转为简单 Map（用于列表展示）
     */
    private Map<String, Object> goodsToSimpleMap(Goods goods) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", String.valueOf(goods.getId()));
        map.put("name", goods.getName());
        map.put("spuCode", goods.getSpuCode());
        map.put("desc", goods.getDescription() != null ? goods.getDescription() : "");
        map.put("categoryId", String.valueOf(goods.getCategoryId()));
        map.put("price", formatPriceYuan(goods.getPrice()));
        map.put("oldPrice", formatPriceYuan(goods.getOldPrice()));
        map.put("discount", goods.getDiscount());
        map.put("picture", goods.getPicture());
        map.put("tag", goods.getTag());
        map.put("salesCount", goods.getSalesCount());
        map.put("commentCount", goods.getCommentCount());
        map.put("collectCount", goods.getCollectCount());
        map.put("isPreSale", goods.getIsPreSale() != null && goods.getIsPreSale() == 1);
        map.put("inventory", goods.getInventory());
        return map;
    }

    /**
     * 添加分类信息到列表
     */
    private void addCategory(List<Map<String, Object>> categories, Long categoryId, String level) {
        if (categoryId == null) {
            return;
        }
        try {
            Map<String, Object> catMap = new HashMap<>();
            catMap.put("id", String.valueOf(categoryId));
            catMap.put("level", level);
            categories.add(catMap);
        } catch (Exception e) {
            log.warn("添加分类信息失败: id={}, level={}", categoryId, level);
        }
    }

    @Override
    public List<Map<String, Object>> getNewGoodsList(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        List<Goods> goodsList = goodsMapper.selectNewGoods(limit);
        return goodsList.stream().map(this::goodsToSimpleMap).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getHotGoodsList(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        List<Goods> goodsList = goodsMapper.selectHotGoods(limit);
        return goodsList.stream().map(this::goodsToSimpleMap).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getGoodsByTopCategory(Long topCategoryId, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        List<Goods> goodsList = goodsMapper.selectByTopCategoryIdSorted(topCategoryId, limit);
        return goodsList.stream().map(this::goodsToSimpleMap).collect(Collectors.toList());
    }
}
