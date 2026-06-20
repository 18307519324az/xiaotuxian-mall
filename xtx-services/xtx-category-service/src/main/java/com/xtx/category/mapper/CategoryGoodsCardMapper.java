package com.xtx.category.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xtx.category.entity.CategoryGoodsCard;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 分类商品卡片 Mapper
 */
@Mapper
public interface CategoryGoodsCardMapper extends BaseMapper<CategoryGoodsCard> {

    /**
     * 根据商品ID列表批量查询
     *
     * @param goodsIds 商品ID列表
     * @return 商品卡片列表
     */
    default List<CategoryGoodsCard> selectByGoodsIds(List<String> goodsIds) {
        return selectListByQuery(
                QueryWrapper.create()
                        .in("goods_id", goodsIds)
        );
    }
}
