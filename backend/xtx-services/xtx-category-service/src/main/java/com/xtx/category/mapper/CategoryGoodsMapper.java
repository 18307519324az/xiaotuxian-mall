package com.xtx.category.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xtx.category.entity.CategoryGoods;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 分类-商品关联 Mapper
 */
@Mapper
public interface CategoryGoodsMapper extends BaseMapper<CategoryGoods> {

    /**
     * 根据分类ID查询关联的商品ID列表
     *
     * @param categoryId 分类ID
     * @return 商品ID列表
     */
    default List<String> selectGoodsIdsByCategoryId(Long categoryId) {
        return selectListByQuery(
                QueryWrapper.create()
                        .select("goods_id")
                        .eq("category_id", categoryId)
                        .orderBy("sort", false)
        ).stream().map(CategoryGoods::getGoodsId).collect(java.util.stream.Collectors.toList());
    }

    /**
     * 根据多个分类ID查询关联的商品ID
     *
     * @param categoryIds 分类ID列表
     * @return 商品关联列表
     */
    default List<CategoryGoods> selectByCategoryIds(List<Long> categoryIds) {
        return selectListByQuery(
                QueryWrapper.create()
                        .in("category_id", categoryIds)
        );
    }
}
