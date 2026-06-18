package com.xtx.category.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xtx.category.entity.CategoryBanner;
import org.apache.ibatis.annotations.Mapper;


/**
 * 分类轮播图 Mapper
 * 提供分类关联的轮播图查询
 */
@Mapper
public interface CategoryBannerMapper extends BaseMapper<CategoryBanner> {

    /**
     * 根据分类ID查询轮播图列表
     *
     * @param categoryId 分类ID
     * @return 轮播图列表
     */
    default java.util.List<CategoryBanner> selectByCategoryId(Long categoryId) {
        return selectListByQuery(
                QueryWrapper.create()
                        .eq("category_id", categoryId)
        );
    }
}
