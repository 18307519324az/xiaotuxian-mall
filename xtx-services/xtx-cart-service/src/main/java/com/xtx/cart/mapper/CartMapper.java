package com.xtx.cart.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xtx.cart.entity.Cart;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * 购物车 Mapper 接口
 */
@Mapper
public interface CartMapper extends BaseMapper<Cart> {

    /**
     * 根据用户ID查询购物车列表，按更新时间降序排列
     * 过滤已逻辑删除的记录
     *
     * @param userId 用户ID
     * @return 购物车列表
     */
    default List<Cart> selectByUserId(Long userId) {
        return selectListByQuery(
                QueryWrapper.create()
                        .eq("user_id", userId)
                        .eq("deleted", 0)
                        .orderBy("update_time", false)
        );
    }

    /**
     * 根据用户ID和SKU ID查询购物车记录
     * 过滤已逻辑删除的记录
     *
     * @param userId 用户ID
     * @param skuId  SKU ID
     * @return 购物车记录
     */
    default Cart selectByUserIdAndSkuId(Long userId, Long skuId) {
        return selectOneByQuery(
                QueryWrapper.create()
                        .eq("user_id", userId)
                        .eq("sku_id", skuId)
                        .eq("deleted", 0)
        );
    }
}
