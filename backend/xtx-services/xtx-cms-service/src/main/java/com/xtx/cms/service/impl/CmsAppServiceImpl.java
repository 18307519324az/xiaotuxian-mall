package com.xtx.cms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.xtx.api.goods.GoodsClient;
import com.xtx.api.goods.dto.GoodsSimpleDTO;
import com.xtx.cms.entity.HomeBanner;
import com.xtx.cms.entity.HomePanel;
import com.xtx.cms.entity.Special;
import com.xtx.cms.mapper.HomeBannerMapper;
import com.xtx.cms.mapper.HomePanelMapper;
import com.xtx.cms.mapper.SpecialMapper;
import com.xtx.cms.service.CmsAppService;
import com.xtx.common.core.result.ApiResponse;
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
 * 内容管理应用服务实现类
 * 实现首页各板块数据的查询、组装与缓存逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CmsAppServiceImpl implements CmsAppService {

    private final HomeBannerMapper homeBannerMapper;
    private final HomePanelMapper homePanelMapper;
    private final SpecialMapper specialMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final GoodsClient goodsClient;

    /**
     * 品牌缓存 Key 前缀
     */
    private static final String CACHE_KEY_BANNER = "home:banner";

    @Override
    public List<Map<String, Object>> getBrandList(Integer limit) {
        // 返回静态 PRD 模拟品牌列表数据
        List<Map<String, Object>> brandList = new ArrayList<>();

        addBrand(brandList, 1L, "三星", "https://picsum.photos/seed/brand1/240/305", "S");
        addBrand(brandList, 2L, "华为", "https://picsum.photos/seed/brand2/240/305", "H");
        addBrand(brandList, 3L, "小米", "https://picsum.photos/seed/brand3/240/305", "X");
        addBrand(brandList, 4L, "vivo", "https://picsum.photos/seed/brand4/240/305", "V");
        addBrand(brandList, 5L, "oppo", "https://picsum.photos/seed/brand5/240/305", "O");
        addBrand(brandList, 6L, "荣耀", "https://picsum.photos/seed/brand6/240/305", "Y");
        addBrand(brandList, 7L, "苹果", "https://picsum.photos/seed/brand7/240/305", "P");
        addBrand(brandList, 8L, "一加", "https://picsum.photos/seed/brand8/240/305", "J");
        addBrand(brandList, 9L, "魅族", "https://picsum.photos/seed/brand9/240/305", "M");
        addBrand(brandList, 10L, "努比亚", "https://picsum.photos/seed/brand10/240/305", "N");
        addBrand(brandList, 11L, "谷歌", "https://picsum.photos/seed/brand11/240/305", "G");
        addBrand(brandList, 12L, "联想", "https://picsum.photos/seed/brand12/240/305", "L");

        return limit != null && limit < brandList.size() ? brandList.subList(0, limit) : brandList;
    }

    @Override
    public List<HomeBanner> getBanners() {
        // 优先从缓存获取
        String cacheKey = CACHE_KEY_BANNER;
        List<HomeBanner> cached = (List<HomeBanner>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 从数据库查询
        List<HomeBanner> banners = homeBannerMapper.selectEnabledSorted();

        // 写入缓存，10 分钟过期
        redisTemplate.opsForValue().set(cacheKey, banners, 10, TimeUnit.MINUTES);

        return banners;
    }

    @Override
    public List<Map<String, Object>> getBannerMaps() {
        // 获取原始轮播图数据，将 linkUrl 转为前端需要的 hrefUrl
        List<HomeBanner> banners = getBanners();
        return banners.stream().map(b -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", b.getId());
            map.put("imgUrl", b.getImgUrl());
            map.put("hrefUrl", b.getLinkUrl());
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getNewGoods() {
        List<HomePanel> panels = homePanelMapper.selectEnabledByType("NEW");
        if (panels.isEmpty()) {
            return List.of();
        }

        // 从面板中提取商品 ID
        HomePanel panel = panels.get(0);
        List<Long> goodsIds = parseGoodsIds(panel.getGoodsIds());
        if (goodsIds.isEmpty()) {
            return List.of();
        }

        // 通过 Feign 客户端调用商品服务获取商品数据
        try {
            ApiResponse<List<GoodsSimpleDTO>> response = goodsClient.listGoodsByIds(goodsIds);
            if (response.getData() == null) {
                return List.of();
            }
            return response.getData().stream()
                    .map(g -> BeanUtil.beanToMap(g, false, true))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("调用商品服务获取新品推荐失败", e);
            return List.of();
        }
    }

    @Override
    public List<Map<String, Object>> getHotGoods() {
        List<HomePanel> panels = homePanelMapper.selectEnabledByType("HOT");
        if (panels.isEmpty()) {
            return List.of();
        }

        // 从面板中提取商品 ID
        HomePanel panel = panels.get(0);
        List<Long> goodsIds = parseGoodsIds(panel.getGoodsIds());
        if (goodsIds.isEmpty()) {
            return List.of();
        }

        // 通过 Feign 客户端调用商品服务获取商品数据
        try {
            ApiResponse<List<GoodsSimpleDTO>> response = goodsClient.listGoodsByIds(goodsIds);
            if (response.getData() == null) {
                return List.of();
            }
            return response.getData().stream()
                    .map(g -> {
                        Map<String, Object> map = BeanUtil.beanToMap(g, false, true);
                        // 前端 home-hot.vue 使用 title 和 alt 字段
                        map.put("title", map.remove("name"));
                        map.put("alt", map.remove("desc"));
                        return map;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("调用商品服务获取人气推荐失败", e);
            return List.of();
        }
    }

    @Override
    public List<Map<String, Object>> getGoodsBlock() {
        // 首页商品板块：组装多个分组数据，格式适配前端 home-product.vue
        // 每个分组需包含：id, name, children[{id,name}], picture, saleInfo, goods[{id,picture,name,tag,price}]
        List<Map<String, Object>> blockList = new ArrayList<>();

        // 新品推荐板块
        List<HomePanel> newPanels = homePanelMapper.selectEnabledByType("NEW");
        if (!newPanels.isEmpty()) {
            List<Long> goodsIds = parseGoodsIds(newPanels.get(0).getGoodsIds());
            if (!goodsIds.isEmpty()) {
                try {
                    ApiResponse<List<GoodsSimpleDTO>> response = goodsClient.listGoodsByIds(goodsIds);
                    if (response.getData() != null && !response.getData().isEmpty()) {
                        blockList.add(buildProductBlock(1L, "新鲜好物",
                                "新品推荐", 101L,
                                "https://picsum.photos/seed/newcover/240/610",
                                "新鲜出炉 品质靠谱", response.getData()));
                    }
                } catch (Exception e) {
                    log.error("获取新品板块商品失败", e);
                }
            }
        }

        // 人气推荐板块
        List<HomePanel> hotPanels = homePanelMapper.selectEnabledByType("HOT");
        if (!hotPanels.isEmpty()) {
            List<Long> goodsIds = parseGoodsIds(hotPanels.get(0).getGoodsIds());
            if (!goodsIds.isEmpty()) {
                try {
                    ApiResponse<List<GoodsSimpleDTO>> response = goodsClient.listGoodsByIds(goodsIds);
                    if (response.getData() != null && !response.getData().isEmpty()) {
                        blockList.add(buildProductBlock(2L, "人气推荐",
                                "人气爆款", 102L,
                                "https://picsum.photos/seed/hotcover/240/610",
                                "人气爆款 不容错过", response.getData()));
                    }
                } catch (Exception e) {
                    log.error("获取人气板块商品失败", e);
                }
            }
        }

        return blockList;
    }

    /**
     * 构建前端 home-product.vue 所需的商品板块数据结构
     *
     * @param id         板块 ID
     * @param name       板块名称
     * @param subName    子分类名称
     * @param subId      子分类 ID
     * @param picture    板块封面图
     * @param saleInfo   促销信息
     * @param goods      商品列表（GoodsSimpleDTO）
     * @return 板块 Map
     */
    private Map<String, Object> buildProductBlock(Long id, String name, String subName, Long subId,
                                                   String picture, String saleInfo, List<GoodsSimpleDTO> goods) {
        Map<String, Object> block = new HashMap<>();
        block.put("id", id);
        block.put("name", name);
        // children 为子分类数组
        Map<String, Object> child = new HashMap<>();
        child.put("id", subId);
        child.put("name", subName);
        block.put("children", List.of(child));
        block.put("picture", picture);
        block.put("saleInfo", saleInfo);
        // goods 需要转换为包含 tag 字段的 Map（前端 home-goods.vue 用 tag 而非 desc）
        block.put("goods", goods.stream().map(g -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", g.getId());
            item.put("name", g.getName());
            item.put("picture", g.getPicture());
            item.put("price", g.getPrice());
            item.put("tag", g.getDesc());
            return item;
        }).collect(Collectors.toList()));
        return block;
    }

    @Override
    public List<Special> getSpecialList() {
        return specialMapper.selectEnabledLatest();
    }

    @Override
    public List<Map<String, Object>> getSpecialMaps() {
        List<Special> specials = getSpecialList();
        return specials.stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            map.put("cover", s.getCover());
            map.put("title", s.getTitle());
            // 数据库字段 subtitle 映射为前端需要的 summary
            map.put("summary", s.getSubtitle());
            // 模拟缺失字段（实际应从订单/评论服务聚合）
            map.put("lowestPrice", 0);
            map.put("collectNum", 0);
            map.put("viewNum", 0);
            map.put("replyNum", 0);
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 添加品牌到列表
     *
     * @param brandList 品牌列表
     * @param id        品牌ID
     * @param name      品牌名称
     * @param picture   品牌图片
     * @param letter    品牌首字母
     */
    private void addBrand(List<Map<String, Object>> brandList, Long id, String name, String picture, String letter) {
        Map<String, Object> brand = new HashMap<>();
        brand.put("id", id);
        brand.put("name", name);
        brand.put("picture", picture);
        brand.put("letter", letter);
        brand.put("place", "品牌产地");
        brand.put("desc", name + "品牌，品质保证，值得信赖");
        brandList.add(brand);
    }

    /**
     * 解析面板中的商品 ID JSON 字符串
     *
     * @param goodsIdsJson 商品 ID 的 JSON 数组字符串
     * @return 商品 ID 列表
     */
    private List<Long> parseGoodsIds(String goodsIdsJson) {
        if (goodsIdsJson == null || goodsIdsJson.isBlank()) {
            return List.of();
        }
        try {
            return JSONUtil.parseArray(goodsIdsJson).toList(Long.class);
        } catch (Exception e) {
            log.warn("解析商品 ID 列表失败: {}", goodsIdsJson, e);
            return List.of();
        }
    }
}
