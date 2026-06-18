package com.xtx.category.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xtx.category.entity.CategoryFilterBrand;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 分类筛选品牌 Mapper
 */
@Mapper
public interface CategoryFilterBrandMapper extends BaseMapper<CategoryFilterBrand> {

    /**
     * 根据分类ID查询品牌列表
     *
     * @param categoryId 分类ID
     * @return 品牌列表
     */
    default List<CategoryFilterBrand> selectByCategoryId(Long categoryId) {
        return selectListByQuery(
                QueryWrapper.create()
                        .eq("category_id", categoryId)
                        .orderBy("sort", false)
        );
    }
}
