package com.xtx.comment.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.xtx.comment.entity.GoodsComment;
import org.apache.ibatis.annotations.Mapper;


/**
 * 商品评价 Mapper 接口
 */
@Mapper
public interface GoodsCommentMapper extends BaseMapper<GoodsComment> {

    /**
     * 根据商品ID分页查询评价列表
     *
     * @param goodsId    商品ID
     * @param page       分页对象
     * @param hasPicture 是否有图筛选（可选）
     * @param orderBy    排序方式（create_time-最新, score-评分）
     * @return 分页结果
     */
    default Page<GoodsComment> selectByGoodsIdPage(Long goodsId, Page<GoodsComment> page,
                                                    Integer hasPicture, String orderBy) {
        QueryWrapper query = QueryWrapper.create()
                .eq("goods_id", goodsId)
                .eq("is_audit", 1); // 只查审核通过的
        if (hasPicture != null && hasPicture == 1) {
            query.eq("has_picture", 1);
        }
        if ("score".equals(orderBy)) {
            query.orderBy("score", false);
        } else {
            query.orderBy("create_time", false);
        }
        return paginate(page, query);
    }

    /**
     * 统计商品评价数量
     *
     * @param goodsId 商品ID
     * @return 评价总数
     */
    default long countByGoodsId(Long goodsId) {
        return selectCountByQuery(
                QueryWrapper.create()
                        .eq("goods_id", goodsId)
                        .eq("is_audit", 1)
        );
    }

    /**
     * 统计好评数量（评分>=4）
     *
     * @param goodsId 商品ID
     * @return 好评数量
     */
    default long countGoodByGoodsId(Long goodsId) {
        return selectCountByQuery(
                QueryWrapper.create()
                        .eq("goods_id", goodsId)
                        .eq("is_audit", 1)
                        .ge("score", 4)
        );
    }

    /**
     * 统计中评数量（评分=3）
     *
     * @param goodsId 商品ID
     * @return 中评数量
     */
    default long countMediumByGoodsId(Long goodsId) {
        return selectCountByQuery(
                QueryWrapper.create()
                        .eq("goods_id", goodsId)
                        .eq("is_audit", 1)
                        .eq("score", 3)
        );
    }

    /**
     * 统计差评数量（评分<=2）
     *
     * @param goodsId 商品ID
     * @return 差评数量
     */
    default long countBadByGoodsId(Long goodsId) {
        return selectCountByQuery(
                QueryWrapper.create()
                        .eq("goods_id", goodsId)
                        .eq("is_audit", 1)
                        .le("score", 2)
        );
    }

    /**
     * 统计有图评价数量
     *
     * @param goodsId 商品ID
     * @return 有图评价数量
     */
    default long countHasPictureByGoodsId(Long goodsId) {
        return selectCountByQuery(
                QueryWrapper.create()
                        .eq("goods_id", goodsId)
                        .eq("is_audit", 1)
                        .eq("has_picture", 1)
        );
    }
}
