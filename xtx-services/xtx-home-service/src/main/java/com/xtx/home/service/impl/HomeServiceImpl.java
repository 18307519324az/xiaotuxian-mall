package com.xtx.home.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.xtx.api.goods.GoodsClient;
import com.xtx.api.goods.dto.GoodsSimpleDTO;
import com.xtx.common.core.exception.BizException;
import com.xtx.common.core.result.ApiResponse;
import com.xtx.common.core.result.ResultCode;
import com.xtx.home.entity.HomeBanner;
import com.xtx.home.entity.HomeBrand;
import com.xtx.home.entity.HomeFloor;
import com.xtx.home.entity.HomeSpecial;
import com.xtx.home.mapper.HomeBannerMapper;
import com.xtx.home.mapper.HomeBrandMapper;
import com.xtx.home.mapper.HomeFloorMapper;
import com.xtx.home.mapper.HomeSpecialMapper;
import com.xtx.home.service.HomeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 首页应用服务实现类
 * 实现首页各板块数据的查询、缓存、Feign 远程调用及数据组装
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private final HomeBannerMapper homeBannerMapper;
    private final HomeBrandMapper homeBrandMapper;
    private final HomeSpecialMapper homeSpecialMapper;
    private final HomeFloorMapper homeFloorMapper;
    private final GoodsClient goodsClient;
    private final RedisTemplate<String, Object> redisTemplate;

    /** 横幅缓存 Key，30 分钟过期 */
    private static final String CACHE_KEY_BANNER = "home:banner";

    /** 品牌缓存 Key 前缀，30 分钟过期 */
    private static final String CACHE_KEY_BRAND_PREFIX = "home:brand:limit:";

    /** 专题缓存 Key 前缀，30 分钟过期 */
    private static final String CACHE_KEY_SPECIAL_PREFIX = "home:special:limit:";

    /** 首页楼层缓存 Key，10 分钟过期 */
    private static final String CACHE_KEY_HOME_GOODS = "home:goods";

    /** 新品缓存 Key 前缀，10 分钟过期 */
    private static final String CACHE_KEY_NEW_PREFIX = "home:new:limit:";

    /** 热销缓存 Key 前缀，10 分钟过期 */
    private static final String CACHE_KEY_HOT_PREFIX = "home:hot:limit:";

    @Override
    public List<Map<String, Object>> getBanners() {
        // 优先从缓存获取
        String cacheKey = CACHE_KEY_BANNER;
        List<Map<String, Object>> cached = (List<Map<String, Object>>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 查询数据库，按 sort 升序排列
        List<HomeBanner> banners = homeBannerMapper.selectListByQuery(
                QueryWrapper.create().eq("status", 1).orderBy("sort", true));

        List<Map<String, Object>> result = banners.stream().map(b -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", String.valueOf(b.getId()));
            map.put("imgUrl", b.getImgUrl());
            map.put("hrefUrl", b.getHrefUrl());
            map.put("type", b.getType());
            return map;
        }).collect(Collectors.toList());

        // 写入缓存，30 分钟过期
        redisTemplate.opsForValue().set(cacheKey, result, 30, TimeUnit.MINUTES);

        return result;
    }

    @Override
    public List<Map<String, Object>> getBrands(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }

        // 优先从缓存获取
        String cacheKey = CACHE_KEY_BRAND_PREFIX + limit;
        List<Map<String, Object>> cached = (List<Map<String, Object>>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 查询数据库，按 sort 升序排列
        List<HomeBrand> brands = homeBrandMapper.selectListByQuery(
                QueryWrapper.create().eq("status", 1).orderBy("sort", true).limit(limit));

        List<Map<String, Object>> result = brands.stream().map(b -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", String.valueOf(b.getId()));
            map.put("name", b.getName());
            map.put("picture", b.getPicture());
            map.put("logo", b.getLogo());
            return map;
        }).collect(Collectors.toList());

        // 写入缓存，30 分钟过期
        redisTemplate.opsForValue().set(cacheKey, result, 30, TimeUnit.MINUTES);

        return result;
    }

    @Override
    public List<Map<String, Object>> getSpecials(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }

        // 优先从缓存获取
        String cacheKey = CACHE_KEY_SPECIAL_PREFIX + limit;
        List<Map<String, Object>> cached = (List<Map<String, Object>>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 查询数据库，按 sort 升序排列
        List<HomeSpecial> specials = homeSpecialMapper.selectListByQuery(
                QueryWrapper.create().eq("status", 1).orderBy("sort", true).limit(limit));

        List<Map<String, Object>> result = specials.stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", String.valueOf(s.getId()));
            map.put("title", s.getTitle());
            map.put("cover", s.getCover());
            map.put("summary", s.getSummary());
            map.put("lowestPrice", s.getLowestPrice());
            return map;
        }).collect(Collectors.toList());

        // 写入缓存，30 分钟过期
        redisTemplate.opsForValue().set(cacheKey, result, 30, TimeUnit.MINUTES);

        return result;
    }

    @Override
    public List<Map<String, Object>> getHomeGoods() {
        // 优先从缓存获取
        List<Map<String, Object>> cached = (List<Map<String, Object>>) redisTemplate.opsForValue().get(CACHE_KEY_HOME_GOODS);
        if (cached != null) {
            return cached;
        }

        // 查询所有上架的楼层配置，按 sort 升序排列
        List<HomeFloor> floors = homeFloorMapper.selectListByQuery(
                QueryWrapper.create().eq("status", 1).orderBy("sort", true));

        List<Map<String, Object>> result = new ArrayList<>();

        for (HomeFloor floor : floors) {
            Map<String, Object> section = new HashMap<>();
            section.put("id", String.valueOf(floor.getCategoryId()));
            section.put("name", floor.getCategoryName());
            section.put("picture", floor.getPicture());
            section.put("saleInfo", floor.getSaleInfo() != null ? floor.getSaleInfo() : "");
            section.put("children", new ArrayList<>());

            // 通过 Feign 调用 goods-service 获取该分类下的商品
            List<Map<String, Object>> goodsCards = getGoodsCardsByTopCategory(floor.getCategoryId(), 8);
            section.put("goods", goodsCards);

            result.add(section);
        }

        // 写入缓存，10 分钟过期
        redisTemplate.opsForValue().set(CACHE_KEY_HOME_GOODS, result, 10, TimeUnit.MINUTES);

        return result;
    }

    @Override
    public List<Map<String, Object>> getNewGoods(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 4;
        }

        // 优先从缓存获取
        String cacheKey = CACHE_KEY_NEW_PREFIX + limit;
        List<Map<String, Object>> cached = (List<Map<String, Object>>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 通过 Feign 调用 goods-service 获取新品
        List<Map<String, Object>> result = fetchGoodsCards(goodsClient.getNewGoods(limit));

        // 写入缓存，10 分钟过期
        redisTemplate.opsForValue().set(cacheKey, result, 10, TimeUnit.MINUTES);

        return result;
    }

    @Override
    public List<Map<String, Object>> getHotGoods(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 4;
        }

        // 优先从缓存获取
        String cacheKey = CACHE_KEY_HOT_PREFIX + limit;
        List<Map<String, Object>> cached = (List<Map<String, Object>>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 通过 Feign 调用 goods-service 获取热销商品
        List<Map<String, Object>> result = fetchGoodsCards(goodsClient.getHotGoods(limit));

        // 写入缓存，10 分钟过期
        redisTemplate.opsForValue().set(cacheKey, result, 10, TimeUnit.MINUTES);

        return result;
    }

    /**
     * 通过 Feign 调用 goods-service 获取指定一级分类下的商品卡片
     *
     * @param topCategoryId 一级分类 ID
     * @param limit         返回数量限制
     * @return 商品卡片列表
     */
    private List<Map<String, Object>> getGoodsCardsByTopCategory(Long topCategoryId, Integer limit) {
        try {
            ApiResponse<List<GoodsSimpleDTO>> response = goodsClient.getGoodsByTopCategory(
                    String.valueOf(topCategoryId), limit);
            if (response != null && response.getData() != null) {
                return response.getData().stream().map(this::goodsSimpleDtoToCard).collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.warn("Feign 调用 goods-service 获取分类商品失败: categoryId={}", topCategoryId, e);
        }
        return new ArrayList<>();
    }

    /**
     * 将 Feign 响应的 GoodsSimpleDTO 列表转为统一的商品卡片 Map 格式
     *
     * @param response Feign 响应
     * @return 商品卡片 Map 列表
     */
    private List<Map<String, Object>> fetchGoodsCards(ApiResponse<List<GoodsSimpleDTO>> response) {
        if (response == null || response.getData() == null) {
            return new ArrayList<>();
        }
        return response.getData().stream().map(this::goodsSimpleDtoToCard).collect(Collectors.toList());
    }

    /**
     * 将 GoodsSimpleDTO 转为前端需要的商品卡片 Map
     * price 转为字符串元格式，ID 转为字符串
     *
     * @param dto 商品简单 DTO
     * @return 商品卡片 Map
     */
    private Map<String, Object> goodsSimpleDtoToCard(GoodsSimpleDTO dto) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", String.valueOf(dto.getId()));
        map.put("name", dto.getName());
        map.put("desc", StrUtil.maxLength(dto.getDesc(), 100));
        // price 必须是字符串元格式，例如 "49.00"
        map.put("price", dto.getPrice() != null ? dto.getPrice().setScale(2).toString() : "0.00");
        map.put("picture", dto.getPicture() != null ? dto.getPicture() : "");
        map.put("alt", dto.getDesc());
        map.put("title", dto.getName());
        return map;
    }
}
