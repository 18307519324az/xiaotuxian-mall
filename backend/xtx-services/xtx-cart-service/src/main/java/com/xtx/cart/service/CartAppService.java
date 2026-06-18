package com.xtx.cart.service;

import cn.hutool.core.collection.CollUtil;
import com.xtx.api.cart.dto.CartMergeItemDTO;
import com.xtx.api.goods.GoodsClient;
import com.xtx.api.goods.dto.SkuSnapshotDTO;
import com.xtx.cart.entity.Cart;
import com.xtx.cart.mapper.CartMapper;
import com.xtx.cart.vo.CartVO;
import com.xtx.common.core.exception.BizException;
import com.xtx.common.core.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 购物车应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartAppService {

    /** 购物车单种商品最大数量 */
    private static final int MAX_CART_COUNT = 99;

    private final CartMapper cartMapper;
    private final GoodsClient goodsClient;

    /**
     * 获取用户购物车列表，填充商品详细信息
     *
     * @param userId 用户ID
     * @return 购物车视图对象列表
     */
    public List<CartVO> getCartList(Long userId) {
        // 查询用户购物车记录
        List<Cart> cartList = cartMapper.selectByUserId(userId);
        if (CollUtil.isEmpty(cartList)) {
            return Collections.emptyList();
        }

        // 收集所有SKU ID
        List<Long> skuIds = cartList.stream()
                .map(Cart::getSkuId)
                .collect(Collectors.toList());

        // 通过GoodsClient获取SKU详细信息
        List<SkuSnapshotDTO> skuList = goodsClient.listSkuSnapshots(skuIds).getData();
        // 转为Map方便查找
        Map<Long, SkuSnapshotDTO> skuMap = new HashMap<>();
        if (skuList != null) {
            skuMap = skuList.stream()
                    .collect(Collectors.toMap(SkuSnapshotDTO::getSkuId, Function.identity(), (k1, k2) -> k1));
        }

        // 组装VO
        List<CartVO> voList = new ArrayList<>();
        for (Cart cart : cartList) {
            CartVO vo = new CartVO();
            // ID字段转为String（前端契约）
            vo.setId(String.valueOf(cart.getId()));
            vo.setSkuId(String.valueOf(cart.getSkuId()));
            vo.setCount(cart.getCount());
            vo.setSelected(cart.getSelected() != null && cart.getSelected() == 1);

            // 填充SKU信息
            SkuSnapshotDTO sku = skuMap.get(cart.getSkuId());
            if (sku != null) {
                vo.setName(sku.getGoodsName());
                vo.setAttrsText(sku.getAttrsText());
                vo.setPicture(sku.getPicture());
                // 价格字段转为String（前端契约），保留两位小数
                vo.setPrice(formatPrice(sku.getPrice()));
                vo.setNowPrice(formatPrice(sku.getNowPrice() != null ? sku.getNowPrice() : sku.getPrice()));
                vo.setStock(sku.getStock() != null ? sku.getStock() : 0);
                // 有效性：SKU状态正常且购物车记录有效
                vo.setIsEffective(cart.getIsEffective() != null && cart.getIsEffective() == 1
                        && sku.getStatus() != null && sku.getStatus() == 1);
            } else {
                // SKU不存在视为无效
                vo.setIsEffective(false);
            }

            // 图片兜底
            if (vo.getPicture() == null || vo.getPicture().isBlank()) {
                vo.setPicture("");
            }

            voList.add(vo);
        }

        return voList;
    }

    /**
     * 添加商品到购物车
     *
     * @param userId 用户ID
     * @param skuId  SKU ID
     * @param count  购买数量
     */
    @Transactional(rollbackFor = Exception.class)
    public void addCartItem(Long userId, Long skuId, Integer count) {
        // 验证SKU是否存在且有效
        SkuSnapshotDTO sku = goodsClient.getSkuSnapshot(skuId).getData();
        if (sku == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "商品SKU不存在");
        }
        if (sku.getStatus() == null || sku.getStatus() != 1) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "商品已下架");
        }

        // 查询是否已存在该SKU的购物车记录（含逻辑删除的）
        Cart existing = cartMapper.selectOneByQuery(
                com.mybatisflex.core.query.QueryWrapper.create()
                        .eq("user_id", userId)
                        .eq("sku_id", skuId)
        );
        if (existing != null) {
            if (existing.getDeleted() != null && existing.getDeleted() == 1) {
                // 之前逻辑删除的，恢复并更新数量
                existing.setDeleted(0);
                existing.setCount(Math.min(count, MAX_CART_COUNT));
                existing.setSelected(1);
                existing.setIsEffective(1);
                existing.setUpdateTime(LocalDateTime.now());
                cartMapper.update(existing);
            } else {
                // 已存在且有效则累加数量，上限为MAX_CART_COUNT
                int newCount = Math.min(existing.getCount() + count, MAX_CART_COUNT);
                existing.setCount(newCount);
                existing.setIsEffective(1);
                existing.setUpdateTime(LocalDateTime.now());
                cartMapper.update(existing);
            }
        } else {
            // 不存在则新增
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setSkuId(skuId);
            cart.setCount(Math.min(count, MAX_CART_COUNT));
            cart.setSelected(1);
            cart.setIsEffective(1);
            cart.setDeleted(0);
            cart.setCreateTime(LocalDateTime.now());
            cart.setUpdateTime(LocalDateTime.now());
            cartMapper.insert(cart);
        }
    }

    /**
     * 更新购物车商品（选中状态、数量）
     *
     * @param userId   用户ID
     * @param skuId    SKU ID
     * @param selected 是否选中（可选）
     * @param count    数量（可选）
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateCartItem(Long userId, Long skuId, Boolean selected, Integer count) {
        Cart cart = cartMapper.selectByUserIdAndSkuId(userId, skuId);
        if (cart == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "购物车记录不存在");
        }

        if (selected != null) {
            cart.setSelected(selected ? 1 : 0);
        }
        if (count != null) {
            if (count <= 0) {
                throw new BizException(ResultCode.BAD_REQUEST.getCode(), "数量必须大于0");
            }
            cart.setCount(Math.min(count, MAX_CART_COUNT));
        }
        cart.setUpdateTime(LocalDateTime.now());
        cartMapper.update(cart);
    }

    /**
     * 删除购物车中的商品（逻辑删除）
     *
     * @param userId 用户ID
     * @param skuIds SKU ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCartItems(Long userId, List<Long> skuIds) {
        if (CollUtil.isEmpty(skuIds)) {
            return;
        }
        List<Cart> cartList = cartMapper.selectListByQuery(
                com.mybatisflex.core.query.QueryWrapper.create()
                        .eq("user_id", userId)
                        .in("sku_id", skuIds)
        );
        for (Cart cart : cartList) {
            cart.setDeleted(1);
            cart.setUpdateTime(LocalDateTime.now());
            cartMapper.update(cart);
        }
    }

    /**
     * 批量切换选中状态
     *
     * @param userId   用户ID
     * @param selected 是否选中
     * @param skuIds   SKU ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void toggleSelectAll(Long userId, Boolean selected, List<Long> skuIds) {
        if (CollUtil.isEmpty(skuIds)) {
            // 没有指定SKU ID则更新所有
            List<Cart> cartList = cartMapper.selectByUserId(userId);
            for (Cart cart : cartList) {
                cart.setSelected(selected ? 1 : 0);
                cart.setUpdateTime(LocalDateTime.now());
                cartMapper.update(cart);
            }
        } else {
            // 更新指定SKU ID
            for (Long skuId : skuIds) {
                Cart cart = cartMapper.selectByUserIdAndSkuId(userId, skuId);
                if (cart != null) {
                    cart.setSelected(selected ? 1 : 0);
                    cart.setUpdateTime(LocalDateTime.now());
                    cartMapper.update(cart);
                }
            }
        }
    }

    /**
     * 合并购物车（从本地购物车合并到服务端）
     *
     * @param userId     用户ID
     * @param localItems 本地购物车条目
     * @return 合并后的购物车列表
     */
    @Transactional(rollbackFor = Exception.class)
    public List<CartVO> mergeCart(Long userId, List<CartMergeItemDTO> localItems) {
        if (CollUtil.isNotEmpty(localItems)) {
            for (CartMergeItemDTO item : localItems) {
                if (item.getSkuId() == null) {
                    continue;
                }
                Cart existing = cartMapper.selectByUserIdAndSkuId(userId, item.getSkuId());
                if (existing != null) {
                    // 取较大值，上限MAX_CART_COUNT
                    int newCount = Math.max(existing.getCount(),
                            item.getCount() != null ? item.getCount() : 1);
                    newCount = Math.min(newCount, MAX_CART_COUNT);
                    existing.setCount(newCount);
                    existing.setIsEffective(1);
                    existing.setSelected(item.getSelected() != null && item.getSelected() ? 1 : 0);
                    existing.setUpdateTime(LocalDateTime.now());
                    cartMapper.update(existing);
                } else {
                    Cart cart = new Cart();
                    cart.setUserId(userId);
                    cart.setSkuId(item.getSkuId());
                    cart.setCount(Math.min(
                            item.getCount() != null ? item.getCount() : 1, MAX_CART_COUNT));
                    cart.setSelected(item.getSelected() != null && item.getSelected() ? 1 : 0);
                    cart.setIsEffective(1);
                    cart.setDeleted(0);
                    cart.setCreateTime(LocalDateTime.now());
                    cart.setUpdateTime(LocalDateTime.now());
                    cartMapper.insert(cart);
                }
            }
        }
        // 返回合并后的购物车列表
        return getCartList(userId);
    }

    /**
     * 根据SKU ID列表清理购物车（内部方法，供订单服务调用，逻辑删除）
     *
     * @param userId 用户ID
     * @param skuIds SKU ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void cleanCartBySkuIds(Long userId, List<Long> skuIds) {
        if (CollUtil.isEmpty(skuIds)) {
            return;
        }
        List<Cart> cartList = cartMapper.selectListByQuery(
                com.mybatisflex.core.query.QueryWrapper.create()
                        .eq("user_id", userId)
                        .in("sku_id", skuIds)
        );
        for (Cart cart : cartList) {
            cart.setDeleted(1);
            cart.setUpdateTime(LocalDateTime.now());
            cartMapper.update(cart);
        }
        log.info("清理购物车完成, userId={}, skuIds={}", userId, skuIds);
    }

    /**
     * 将BigDecimal格式化为保留两位小数的String
     */
    private String formatPrice(BigDecimal price) {
        if (price == null) return "0.00";
        return price.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }
}
