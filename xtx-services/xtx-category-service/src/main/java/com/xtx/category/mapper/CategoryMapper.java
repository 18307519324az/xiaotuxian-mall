package com.xtx.category.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xtx.category.entity.Category;
import org.apache.ibatis.annotations.Mapper;


/**
 * 商品分类 Mapper
 * 提供商品分类的树形结构查询
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    /**
     * 根据父级分类ID查询已启用的子分类列表
     *
     * @param parentId 父级分类ID
     * @return 子分类列表，按排序权重降序排列
     */
    default java.util.List<Category> selectByParentId(Long parentId) {
        return selectListByQuery(
                QueryWrapper.create()
                        .eq("parent_id", parentId)
                        .eq("status", 1)
                        .orderBy("sort", false)
        );
    }

    /**
     * 查询所有已启用的分类
     *
     * @return 所有已启用的分类列表
     */
    default java.util.List<Category> selectAllEnabled() {
        return selectListByQuery(
                QueryWrapper.create()
                        .eq("status", 1)
        );
    }
}
