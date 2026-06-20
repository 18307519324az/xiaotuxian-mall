package com.xtx.category.service.impl;

import com.xtx.category.entity.Category;
import com.xtx.category.entity.CategoryBanner;
import com.xtx.category.entity.CategoryFilterBrand;
import com.xtx.category.entity.CategoryGoods;
import com.xtx.category.entity.CategoryGoodsCard;
import com.xtx.category.mapper.CategoryBannerMapper;
import com.xtx.category.mapper.CategoryFilterBrandMapper;
import com.xtx.category.mapper.CategoryGoodsCardMapper;
import com.xtx.category.mapper.CategoryGoodsMapper;
import com.xtx.category.mapper.CategoryMapper;
import com.xtx.category.service.CategoryAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 分类应用服务实现类
 * 从数据库读取分类数据，构建三级分类树、分类详情等
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryAppServiceImpl implements CategoryAppService {

    private final CategoryMapper categoryMapper;
    private final CategoryBannerMapper categoryBannerMapper;
    private final CategoryGoodsMapper categoryGoodsMapper;
    private final CategoryGoodsCardMapper categoryGoodsCardMapper;
    private final CategoryFilterBrandMapper categoryFilterBrandMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY_CATEGORY_HEAD = "category:head";

    @Override
    public List<Map<String, Object>> getHeadCategoryTree() {
        // 优先从缓存获取
        List<Map<String, Object>> cached = (List<Map<String, Object>>) redisTemplate.opsForValue().get(CACHE_KEY_CATEGORY_HEAD);
        if (cached != null) {
            return cached;
        }

        // 查询所有已启用分类
        List<Category> allCategories = categoryMapper.selectAllEnabled();

        // 按层级分组
        Map<Integer, List<Category>> levelMap = allCategories.stream()
                .collect(Collectors.groupingBy(Category::getLevel));

        List<Category> level1List = levelMap.getOrDefault(1, List.of());
        List<Category> level2List = levelMap.getOrDefault(2, List.of());
        List<Category> level3List = levelMap.getOrDefault(3, List.of());

        // 收集所有三级分类ID，批量查询商品关联和商品卡片
        List<Long> l3Ids = level3List.stream().map(Category::getId).collect(Collectors.toList());
        Map<Long, List<CategoryGoods>> goodsMap;
        Map<String, CategoryGoodsCard> goodsCardMap;

        if (!l3Ids.isEmpty()) {
            // 查询所有三级分类下的商品关联
            List<CategoryGoods> allGoods = categoryGoodsMapper.selectByCategoryIds(l3Ids);
            goodsMap = allGoods.stream()
                    .collect(Collectors.groupingBy(CategoryGoods::getCategoryId));

            // 收集所有商品ID，批量查询商品卡片
            List<String> allGoodsIds = allGoods.stream()
                    .map(CategoryGoods::getGoodsId)
                    .distinct()
                    .collect(Collectors.toList());
            if (!allGoodsIds.isEmpty()) {
                List<CategoryGoodsCard> cards = categoryGoodsCardMapper.selectByGoodsIds(allGoodsIds);
                goodsCardMap = cards.stream()
                        .collect(Collectors.toMap(CategoryGoodsCard::getGoodsId, c -> c));
            } else {
                goodsCardMap = new HashMap<>();
            }
        } else {
            goodsMap = new HashMap<>();
            goodsCardMap = new HashMap<>();
        }

        // 构建 l3Id → l1Id 映射
        Map<Long, Long> l3ToL1Map = new HashMap<>();
        for (Category l2 : level2List) {
            for (Category l3 : level3List) {
                if (l3.getParentId().equals(l2.getId())) {
                    l3ToL1Map.put(l3.getId(), l2.getParentId());
                }
            }
        }

        // 构建三级分类树
        List<Map<String, Object>> tree = new ArrayList<>();
        for (Category l1 : level1List) {
            Map<String, Object> l1Node = categoryToMap(l1);

            // 添加二级子节点
            List<Map<String, Object>> l2Children = new ArrayList<>();
            for (Category l2 : level2List) {
                if (l2.getParentId().equals(l1.getId())) {
                    Map<String, Object> l2Node = categoryToMap(l2);

                    // 添加三级子节点
                    List<Map<String, Object>> l3Children = new ArrayList<>();
                    for (Category l3 : level3List) {
                        if (l3.getParentId().equals(l2.getId())) {
                            Map<String, Object> l3Node = categoryToMap(l3);

                            // 附上该三级分类下的商品卡片
                            List<CategoryGoods> catGoods = goodsMap.get(l3.getId());
                            if (catGoods != null && !catGoods.isEmpty()) {
                                List<Map<String, Object>> goodsCards = catGoods.stream()
                                        .map(g -> toGoodsCardMap(goodsCardMap.get(g.getGoodsId())))
                                        .filter(m -> m != null)
                                        .collect(Collectors.toList());
                                l3Node.put("goods", goodsCards);
                            } else {
                                l3Node.put("goods", List.of());
                            }

                            l3Children.add(l3Node);
                        }
                    }
                    l2Node.put("children", l3Children);
                    l2Children.add(l2Node);
                }
            }
            l1Node.put("children", l2Children);

            // 聚合一级分类下所有三级分类的商品（前端左侧悬浮弹层需要）
            List<Map<String, Object>> l1Goods = new ArrayList<>();
            for (Map.Entry<Long, Long> entry : l3ToL1Map.entrySet()) {
                if (entry.getValue().equals(l1.getId())) {
                    List<CategoryGoods> catGoods = goodsMap.get(entry.getKey());
                    if (catGoods != null) {
                        for (CategoryGoods g : catGoods) {
                            Map<String, Object> card = toGoodsCardMap(goodsCardMap.get(g.getGoodsId()));
                            if (card != null) {
                                l1Goods.add(card);
                            }
                        }
                    }
                }
            }
            // 限制一级分类商品数量
            l1Node.put("goods", l1Goods.stream().limit(4).collect(Collectors.toList()));

            tree.add(l1Node);
        }

        // 写入缓存，30 分钟过期
        redisTemplate.opsForValue().set(CACHE_KEY_CATEGORY_HEAD, tree, 30, TimeUnit.MINUTES);

        return tree;
    }

    @Override
    public Map<String, Object> getTopCategoryDetail(String id) {
        Long categoryId = parseLongId(id);
        if (categoryId == null) return null;

        // 查询一级分类
        Category category = categoryMapper.selectOneById(categoryId);
        if (category == null) return null;

        Map<String, Object> result = categoryToMap(category);

        // 查询关联轮播图
        List<CategoryBanner> banners = categoryBannerMapper.selectByCategoryId(categoryId);
        result.put("banners", banners.stream().map(this::bannerToMap).collect(Collectors.toList()));

        // 查询二级子分类（含三级分类和商品）
        List<Category> children = categoryMapper.selectByParentId(categoryId);
        List<Map<String, Object>> subList = new ArrayList<>();
        for (Category child : children) {
            Map<String, Object> subMap = categoryToMap(child);
            subMap.put("parentName", category.getName());

            // 查询三级分类
            List<Category> l3List = categoryMapper.selectByParentId(child.getId());
            List<Map<String, Object>> l3Maps = new ArrayList<>();
            for (Category l3 : l3List) {
                Map<String, Object> l3Map = categoryToMap(l3);

                // 查询三级分类下的商品
                List<String> goodsIds = categoryGoodsMapper.selectGoodsIdsByCategoryId(l3.getId());
                List<CategoryGoodsCard> cards = goodsIds.isEmpty() ? List.of() :
                        categoryGoodsCardMapper.selectByGoodsIds(goodsIds);
                l3Map.put("goods", cards.stream().map(this::toGoodsCardMap).collect(Collectors.toList()));

                l3Maps.add(l3Map);
            }
            subMap.put("children", l3Maps);

            // 子分类上的商品（二级分类直接挂载的商品）
            List<String> subGoodsIds = new ArrayList<>();
            for (Category l3 : l3List) {
                subGoodsIds.addAll(categoryGoodsMapper.selectGoodsIdsByCategoryId(l3.getId()));
            }
            List<CategoryGoodsCard> subCards = subGoodsIds.isEmpty() ? List.of() :
                    categoryGoodsCardMapper.selectByGoodsIds(subGoodsIds);
            subMap.put("goods", subCards.stream().map(this::toGoodsCardMap).collect(Collectors.toList()));

            // origin 接口要求返回的 null 占位字段
            subMap.put("categories", null);
            subMap.put("brands", null);
            subMap.put("saleProperties", null);

            subList.add(subMap);
        }
        result.put("children", subList);

        return result;
    }

    @Override
    public Map<String, Object> getSubCategoryFilter(String id) {
        Long categoryId = parseLongId(id);
        if (categoryId == null) return null;

        // 查询二级分类
        Category category = categoryMapper.selectOneById(categoryId);
        if (category == null) return null;

        Map<String, Object> result = categoryToMap(category);

        // 查询三级子分类作为筛选项
        List<Category> level3List = categoryMapper.selectByParentId(categoryId);
        result.put("children", level3List.stream().map(this::categoryToMap).collect(Collectors.toList()));

        // 查询品牌列表
        List<CategoryFilterBrand> brands = categoryFilterBrandMapper.selectByCategoryId(categoryId);
        result.put("brands", brands.stream().map(this::brandToMap).collect(Collectors.toList()));

        // 占位字段（origin 接口要求 — 前端 sub-filter.vue 会遍历 saleProperties，
        // 若为 null 会导致 TypeError 并使骨架屏一直显示）
        result.put("categories", new ArrayList<>());
        result.put("saleProperties", new ArrayList<>());

        return result;
    }

    @Override
    public Object getCategoryGoods(Map<String, Object> params) {
        Map<String, Object> pageResult = new HashMap<>();
        int page = params.get("page") instanceof Number ? ((Number) params.get("page")).intValue() : 1;
        int pageSize = params.get("pageSize") instanceof Number ? ((Number) params.get("pageSize")).intValue() : 20;
        String categoryId = params.get("categoryId") instanceof String
                ? (String) params.get("categoryId")
                : (params.get("categoryId") instanceof Number
                    ? String.valueOf(((Number) params.get("categoryId")).longValue())
                    : null);

        pageResult.put("page", page);
        pageResult.put("pageSize", pageSize);

        if (categoryId == null) {
            pageResult.put("items", List.of());
            pageResult.put("total", 0);
            return pageResult;
        }

        Long catId = parseLongId(categoryId);
        if (catId == null) {
            pageResult.put("items", List.of());
            pageResult.put("total", 0);
            return pageResult;
        }

        // 收集所有叶子分类 ID（level-3 或 level-2 下无 level-3 的）
        List<Long> leafIds = collectLeafCategoryIds(catId);

        // 查询所有叶子分类下的商品关联
        List<String> goodsIds = new ArrayList<>();
        for (Long leafId : leafIds) {
            goodsIds.addAll(categoryGoodsMapper.selectGoodsIdsByCategoryId(leafId));
        }
        goodsIds = goodsIds.stream().distinct().collect(Collectors.toList());

        if (goodsIds.isEmpty()) {
            pageResult.put("items", List.of());
            pageResult.put("total", 0);
            return pageResult;
        }

        // 查询商品卡片
        List<CategoryGoodsCard> cards = categoryGoodsCardMapper.selectByGoodsIds(goodsIds);
        List<Map<String, Object>> items = cards.stream()
                .map(this::toGoodsCardMap)
                .collect(Collectors.toList());

        // 手动分页
        int total = items.size();
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        if (fromIndex >= total) {
            pageResult.put("items", List.of());
        } else {
            pageResult.put("items", items.subList(fromIndex, toIndex));
        }
        pageResult.put("total", total);
        return pageResult;
    }

    /**
     * 收集指定分类下的所有叶子分类 ID（即挂载商品的最底层分类）
     */
    private List<Long> collectLeafCategoryIds(Long categoryId) {
        Category category = categoryMapper.selectOneById(categoryId);
        if (category == null) return List.of();

        // 如果是 level-3，直接返回自身
        if (category.getLevel() == 3) {
            return List.of(categoryId);
        }

        // 如果是 level-2，查询其下是否有 level-3 子分类
        if (category.getLevel() == 2) {
            List<Category> children = categoryMapper.selectByParentId(categoryId);
            if (!children.isEmpty()) {
                return children.stream().map(Category::getId).collect(Collectors.toList());
            }
            // 没有 level-3 子分类，则自身就是叶子
            return List.of(categoryId);
        }

        // 如果是 level-1，递归收集所有层级的叶子分类
        List<Category> level2List = categoryMapper.selectByParentId(categoryId);
        List<Long> leafIds = new ArrayList<>();
        for (Category l2 : level2List) {
            leafIds.addAll(collectLeafCategoryIds(l2.getId()));
        }
        return leafIds;
    }

    // ========== 私有辅助方法 ==========

    /**
     * 将 Category 实体转为前端 Map
     * 字段命名保持与 Mock 兼容
     */
    private Map<String, Object> categoryToMap(Category category) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", String.valueOf(category.getId()));
        map.put("parentId", String.valueOf(category.getParentId()));
        map.put("name", category.getName());
        map.put("icon", category.getIconUrl());
        map.put("picture", category.getPictureUrl());
        map.put("pictureUrl", category.getPictureUrl());
        map.put("sort", category.getSort());
        map.put("level", category.getLevel());
        return map;
    }

    /**
     * 将商品卡片转为前端 Map
     */
    private Map<String, Object> toGoodsCardMap(CategoryGoodsCard card) {
        if (card == null) return null;
        Map<String, Object> map = new HashMap<>();
        map.put("id", card.getGoodsId());
        map.put("name", card.getName());
        map.put("desc", card.getDescription());
        map.put("price", card.getPrice().toPlainString());
        map.put("picture", card.getPicture());
        map.put("tag", card.getTag());
        map.put("orderNum", 0);
        return map;
    }

    /**
     * 将 Banner 转为前端 Map
     */
    private Map<String, Object> bannerToMap(CategoryBanner banner) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", String.valueOf(banner.getId()));
        map.put("imgUrl", banner.getImgUrl());
        map.put("linkUrl", banner.getLinkUrl());
        return map;
    }

    /**
     * 将品牌转为前端 Map
     */
    private Map<String, Object> brandToMap(CategoryFilterBrand brand) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", brand.getBrandId());
        map.put("name", brand.getBrandName());
        map.put("letter", brand.getBrandLetter());
        return map;
    }

    /**
     * 安全地将 String ID 转为 Long
     */
    private Long parseLongId(String id) {
        if (id == null || id.isEmpty()) return null;
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            log.warn("无效的 ID 格式: {}", id);
            return null;
        }
    }
}
