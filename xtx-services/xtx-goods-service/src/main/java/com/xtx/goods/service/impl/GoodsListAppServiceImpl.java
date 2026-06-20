package com.xtx.goods.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.xtx.goods.entity.Goods;
import com.xtx.goods.mapper.GoodsMapper;
import com.xtx.goods.service.GoodsListAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品列表应用服务实现类
 * 提供分类商品列表的分页、排序、筛选和关键词搜索功能
 * v1.6 切片：替代 Mock POST /category/goods/temporary
 *
 * 字段映射与 Mock toGoodsCard() 兼容：
 * - id → String
 * - name → String
 * - desc → String（goods.description）
 * - tag → String（goods.tag）
 * - price → String "99.00"（两位小数）
 * - picture → String（goods.picture 原值）
 * - orderNum → Number（goods.sort 排序权重）
 * - brandId → String
 * - brandName → String
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoodsListAppServiceImpl implements GoodsListAppService {

    private final GoodsMapper goodsMapper;

    /**
     * 将字符串 ID 转为 Long，失败时返回 null
     */
    private Long parseLongIdOrNull(String id) {
        if (id == null || id.isBlank()) return null;
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 将 BigDecimal 元格式化为保留两位小数的字符串，与 Mock 基线 String 格式对齐
     */
    private String formatPriceYuan(BigDecimal price) {
        if (price == null) return "0.00";
        return price.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }

    @Override
    public Map<String, Object> getGoodsList(String categoryId, int page, int pageSize,
                                            String sortField, String sortMethod,
                                            boolean inventoryOnly, boolean discountOnly,
                                            String brandId, String keyword) {
        // 参数校验
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 20;
        if (pageSize > 100) pageSize = 100;

        Long catId = parseLongIdOrNull(categoryId);
        Long brandLongId = parseLongIdOrNull(brandId);

        // 调用 Mapper 分页查询
        Page<Goods> goodsPage = goodsMapper.selectGoodsList(
                page, pageSize,
                catId, keyword,
                brandLongId,
                inventoryOnly, discountOnly,
                sortField, sortMethod
        );

        // 构建响应（与 Mock toGoodsCard() 字段对齐）
        List<Map<String, Object>> items = new ArrayList<>();
        for (Goods goods : goodsPage.getRecords()) {
            items.add(goodsToGoodsCard(goods));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("counts", (int) goodsPage.getTotalRow());
        result.put("pageSize", (int) goodsPage.getPageSize());
        result.put("page", (int) goodsPage.getPageNumber());
        result.put("pages", (int) goodsPage.getTotalPage());
        result.put("items", items);

        return result;
    }

    /**
     * 将 Goods 实体转为商品卡片格式（与 Mock toGoodsCard() 兼容）
     */
    private Map<String, Object> goodsToGoodsCard(Goods goods) {
        Map<String, Object> card = new HashMap<>();
        card.put("id", String.valueOf(goods.getId()));
        card.put("name", goods.getName() != null ? goods.getName() : "");
        card.put("desc", goods.getDescription() != null ? goods.getDescription() : "");
        // tag 兜底 desc
        String tag = goods.getTag() != null && !goods.getTag().isBlank()
                ? goods.getTag()
                : (goods.getDescription() != null ? goods.getDescription() : "");
        card.put("tag", tag);
        card.put("price", formatPriceYuan(goods.getPrice()));
        card.put("picture", goods.getPicture() != null ? goods.getPicture() : "");
        card.put("orderNum", goods.getSort() != null ? goods.getSort() : 100);
        card.put("brandId", goods.getBrandId() != null ? String.valueOf(goods.getBrandId()) : "");
        card.put("brandName", goods.getBrandName() != null ? goods.getBrandName() : "");
        return card;
    }
}
