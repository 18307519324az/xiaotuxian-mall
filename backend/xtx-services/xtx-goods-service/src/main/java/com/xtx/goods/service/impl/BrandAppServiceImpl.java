package com.xtx.goods.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.xtx.common.core.exception.BizException;
import com.xtx.common.core.result.ResultCode;
import com.xtx.goods.entity.Brand;
import com.xtx.goods.entity.Goods;
import com.xtx.goods.mapper.BrandMapper;
import com.xtx.goods.mapper.GoodsMapper;
import com.xtx.goods.service.BrandAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 品牌应用服务实现类
 * 提供品牌详情和品牌下商品列表的查询功能
 * 缺失字段通过 putIfAbsent 兜底，与 v1.3 商品详情做法一致
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BrandAppServiceImpl implements BrandAppService {

    private final BrandMapper brandMapper;
    private final GoodsMapper goodsMapper;

    /**
     * 将字符串 ID 转为 Long，解析失败时抛出 404
     */
    private Long parseLongId(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "品牌不存在");
        }
    }

    /**
     * 将 BigDecimal 元格式化为保留两位小数的字符串，与 Mock 基线 String 格式对齐
     */
    private String formatPriceYuan(BigDecimal price) {
        if (price == null) return null;
        return price.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }

    @Override
    public Map<String, Object> getBrandDetail(String id) {
        Long brandId = parseLongId(id);

        // 查询品牌基本信息
        Brand brand = brandMapper.selectOneById(brandId);
        if (brand == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "品牌不存在");
        }

        // 查询该品牌下在售商品列表
        List<Goods> goodsList = goodsMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("brand_id", brandId)
                        .eq("status", 1)
        );

        // 构建品牌详情结构
        Map<String, Object> detail = new HashMap<>();
        detail.put("id", String.valueOf(brand.getId()));
        detail.put("name", brand.getName() != null ? brand.getName() : "");
        // logo 复用 picture（DB 无独立 logo 字段）
        detail.put("logo", brand.getPicture() != null ? brand.getPicture() : "");
        detail.put("picture", brand.getPicture() != null ? brand.getPicture() : "");
        detail.put("desc", brand.getDesc() != null ? brand.getDesc() : "");

        // 品牌下商品列表
        List<Map<String, Object>> goodsMaps = goodsList.stream()
                .map(this::goodsToBrandGoodsMap)
                .collect(Collectors.toList());
        detail.put("goods", goodsMaps);

        // 兼容 Mock 的额外兜底字段（前端可能使用，防止 undefined 报错）
        detail.putIfAbsent("place", "");
        detail.putIfAbsent("story", brand.getDesc() != null ? brand.getDesc() : "");
        detail.putIfAbsent("serviceTags", List.of());
        detail.putIfAbsent("followed", false);
        detail.putIfAbsent("followCount", 0);

        return detail;
    }

    @Override
    public List<Map<String, Object>> getBrandGoods(String brandId) {
        Long brandLongId = parseLongId(brandId);

        List<Goods> goodsList = goodsMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("brand_id", brandLongId)
                        .eq("status", 1)
        );

        return goodsList.stream()
                .map(this::goodsToBrandGoodsMap)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> listAllBrands(Integer limit) {
        // 查询所有启用品牌
        List<Brand> brands = brandMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("status", 1)
                        .orderBy("id", true)
        );

        // 限制条数
        if (limit != null && limit > 0 && limit < brands.size()) {
            brands = brands.subList(0, limit);
        }

        return brands.stream().map(brand -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", String.valueOf(brand.getId()));
            map.put("name", brand.getName() != null ? brand.getName() : "");
            // nameEn 在 DB 中无独立字段，兜底为空字符串
            map.put("nameEn", "");
            // logo 复用 picture
            map.put("logo", brand.getPicture() != null ? brand.getPicture() : "");
            map.put("picture", brand.getPicture() != null ? brand.getPicture() : "");
            map.put("desc", brand.getDesc() != null ? brand.getDesc() : "");
            // place 在 DB 中无独立字段，兜底为空字符串
            map.put("place", "");
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 将商品转为品牌商品列表格式（与 Mock buildBrandGoods 兼容）
     */
    private Map<String, Object> goodsToBrandGoodsMap(Goods goods) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", String.valueOf(goods.getId()));
        map.put("name", goods.getName() != null ? goods.getName() : "");
        map.put("picture", goods.getPicture() != null ? goods.getPicture() : "");
        map.put("price", formatPriceYuan(goods.getPrice()));
        map.put("brandId", goods.getBrandId() != null ? String.valueOf(goods.getBrandId()) : "");
        map.put("brandName", goods.getBrandName() != null ? goods.getBrandName() : "");
        map.put("desc", goods.getDescription() != null ? goods.getDescription() : "");
        map.put("salesCount", goods.getSalesCount() != null ? goods.getSalesCount() : 0);
        map.put("collectCount", goods.getCollectCount() != null ? goods.getCollectCount() : 0);
        return map;
    }
}
