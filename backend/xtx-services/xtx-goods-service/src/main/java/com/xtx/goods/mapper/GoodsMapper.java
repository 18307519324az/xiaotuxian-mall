package com.xtx.goods.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryWrapper;
import com.xtx.goods.entity.Goods;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * 商品 Mapper
 * 提供商品信息的基础 CRUD 及自定义查询
 */
@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {

    /**
     * 根据三级分类ID查询商品列表，按 sort 权重和销量降序排列
     *
     * @param categoryId 三级分类ID
     * @param excludeId  排除的商品ID（用于"相关推荐"）
     * @param limit      限制数量
     * @return 商品列表
     */
    default List<Goods> selectByCategoryIdSorted(Long categoryId, Long excludeId, Integer limit) {
        return selectListByQuery(
                QueryWrapper.create()
                        .eq("category_id", categoryId)
                        .ne("id", excludeId)
                        .eq("status", 1)
                        .orderBy("sort", false)
                        .orderBy("sales_count", false)
                        .orderBy("id", true)
                        .limit(limit)
        );
    }

    /**
     * 根据商品ID列表批量查询
     *
     * @param ids 商品ID列表
     * @return 商品列表
     */
    default List<Goods> selectBatchByIds(List<Long> ids) {
        return selectListByQuery(
                QueryWrapper.create()
                        .in("id", ids)
                        .eq("status", 1)
        );
    }

    /**
     * 根据分类ID列表查询商品
     *
     * @param categoryIds 分类ID列表（三级分类）
     * @return 商品列表
     */
    default List<Goods> selectByCategoryIds(List<Long> categoryIds) {
        return selectListByQuery(
                QueryWrapper.create()
                        .in("category_id", categoryIds)
                        .eq("status", 1)
        );
    }

    /**
     * 根据一级分类ID查询商品列表，按 sort 权重降序排列
     *
     * @param topCategoryId 一级分类ID
     * @param limit         限制数量
     * @return 商品列表
     */
    default List<Goods> selectByTopCategoryIdSorted(Long topCategoryId, Integer limit) {
        return selectListByQuery(
                QueryWrapper.create()
                        .eq("top_category_id", topCategoryId)
                        .eq("status", 1)
                        .orderBy("sort", false)
                        .orderBy("sales_count", false)
                        .limit(limit)
        );
    }

    /**
     * 查询最新商品列表，按 sort 权重降序排列
     *
     * @param limit 限制数量
     * @return 商品列表
     */
    default List<Goods> selectNewGoods(Integer limit) {
        return selectListByQuery(
                QueryWrapper.create()
                        .eq("status", 1)
                        .orderBy("sort", false)
                        .orderBy("id", false)
                        .limit(limit)
        );
    }

    /**
     * 查询热门商品列表，按销量降序排列
     *
     * @param limit 限制数量
     * @return 商品列表
     */
    default List<Goods> selectHotGoods(Integer limit) {
        return selectListByQuery(
                QueryWrapper.create()
                        .eq("status", 1)
                        .orderBy("sales_count", false)
                        .orderBy("sort", false)
                        .limit(limit)
        );
    }

    /**
     * 根据品牌ID查询在售商品列表
     * 用于品牌详情页展示该品牌下的商品
     *
     * @param brandId 品牌ID
     * @return 商品列表
     */
    default List<Goods> selectByBrandId(Long brandId) {
        return selectListByQuery(
                QueryWrapper.create()
                        .eq("brand_id", brandId)
                        .eq("status", 1)
        );
    }

    /**
     * 分页查询分类商品列表，支持排序、筛选和关键词搜索
     * 用于 v1.6 商品列表真实服务切片，替代 Mock POST /category/goods/temporary
     *
     * @param pageNum      页码（从 1 开始）
     * @param pageSize     每页数量
     * @param categoryId   分类ID（可选，匹配 category_id/parent_category_id/top_category_id）
     * @param keyword      关键词（可选，匹配 name LIKE）
     * @param brandId      品牌ID（可选）
     * @param inventoryOnly 是否仅显示有货（inventory > 0）
     * @param discountOnly 是否仅显示特惠（old_price > price）
     * @param sortField    排序字段（null=默认，price, publishTime, orderNum, evaluateNum）
     * @param sortMethod   排序方向（asc, desc）
     * @return 分页结果
     */
    default Page<Goods> selectGoodsList(int pageNum, int pageSize,
                                        Long categoryId, String keyword,
                                        Long brandId,
                                        boolean inventoryOnly, boolean discountOnly,
                                        String sortField, String sortMethod) {
        QueryWrapper qw = QueryWrapper.create().eq("status", 1);

        // 分类筛选（支持三级/二级/一级分类）
        if (categoryId != null) {
            qw.and("(category_id = ? OR parent_category_id = ? OR top_category_id = ?)",
                    categoryId, categoryId, categoryId);
        }

        // 关键词模糊搜索（商品名）
        if (keyword != null && !keyword.isBlank()) {
            qw.and("name LIKE ?", "%" + keyword + "%");
        }

        // 品牌筛选
        if (brandId != null) {
            qw.and("brand_id = ?", brandId);
        }

        // 仅显示有货
        if (inventoryOnly) {
            qw.and("inventory > 0");
        }

        // 仅显示特惠（old_price > price 表示有折扣）
        if (discountOnly) {
            qw.and("old_price > 0 AND old_price > price");
        }

        // 排序
        if (sortField != null && !sortField.isBlank()) {
            boolean isAsc = "asc".equalsIgnoreCase(sortMethod);
            switch (sortField) {
                case "price":
                    qw.orderBy("price", isAsc);
                    break;
                case "publishTime":
                    qw.orderBy("create_time", isAsc);
                    break;
                case "orderNum":
                    qw.orderBy("sales_count", isAsc);
                    break;
                case "evaluateNum":
                    qw.orderBy("comment_count", isAsc);
                    break;
                default:
                    qw.orderBy("sort", false)
                            .orderBy("sales_count", false)
                            .orderBy("id", true);
                    break;
            }
        } else {
            // 默认排序：sort权重 DESC, sales_count DESC, id ASC
            qw.orderBy("sort", false)
                    .orderBy("sales_count", false)
                    .orderBy("id", true);
        }

        return this.paginate(new Page<>(pageNum, pageSize), qw);
    }
}
