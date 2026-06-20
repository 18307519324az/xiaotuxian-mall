package com.xtx.goods.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xtx.goods.entity.GoodsPicture;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * 商品图片 Mapper
 * 提供商品图片的基础 CRUD 操作
 */
@Mapper
public interface GoodsPictureMapper extends BaseMapper<GoodsPicture> {

    /**
     * 根据商品ID查询图片列表，按排序权重降序排列
     *
     * @param goodsId 商品ID
     * @return 图片列表
     */
    default List<GoodsPicture> selectByGoodsId(Long goodsId) {
        return selectListByQuery(
                QueryWrapper.create()
                        .eq("goods_id", goodsId)
                        .orderBy("sort", false)
        );
    }
}
