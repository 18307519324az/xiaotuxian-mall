package com.xtx.goods.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xtx.goods.entity.GoodsDetail;
import org.apache.ibatis.annotations.Mapper;


/**
 * 商品详情 Mapper
 * 提供商品详情数据（描述图片、属性）的查询
 */
@Mapper
public interface GoodsDetailMapper extends BaseMapper<GoodsDetail> {

    /**
     * 根据商品ID查询商品详情
     *
     * @param goodsId 商品ID
     * @return 商品详情实体
     */
    default GoodsDetail selectByGoodsId(Long goodsId) {
        return selectOneByQuery(
                QueryWrapper.create()
                        .eq("goods_id", goodsId)
                        .limit(1)
        );
    }
}
