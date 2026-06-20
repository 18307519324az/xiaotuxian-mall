package com.xtx.comment.service;

import com.mybatisflex.core.paginate.Page;
import com.xtx.comment.entity.GoodsComment;
import com.xtx.comment.entity.GoodsCommentPicture;
import com.xtx.comment.mapper.GoodsCommentMapper;
import com.xtx.comment.mapper.GoodsCommentPictureMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 评价应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentAppService {

    private final GoodsCommentMapper goodsCommentMapper;
    private final GoodsCommentPictureMapper goodsCommentPictureMapper;

    /**
     * 获取商品评价统计信息
     *
     * @param goodsId 商品ID
     * @return 评价统计数据
     */
    public Map<String, Object> getCommentStats(Long goodsId) {
        long total = goodsCommentMapper.countByGoodsId(goodsId);
        long good = goodsCommentMapper.countGoodByGoodsId(goodsId);
        long medium = goodsCommentMapper.countMediumByGoodsId(goodsId);
        long bad = goodsCommentMapper.countBadByGoodsId(goodsId);
        long hasPicture = goodsCommentMapper.countHasPictureByGoodsId(goodsId);

        // 获取评价标签统计
        List<Map<String, Object>> tags = getCommentTags(goodsId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", total);
        result.put("good", good);
        result.put("medium", medium);
        result.put("bad", bad);
        result.put("hasPicture", hasPicture);
        result.put("tags", tags);
        return result;
    }

    /**
     * 分页查询商品评价列表
     *
     * @param goodsId    商品ID
     * @param page       页码
     * @param pageSize   每页大小
     * @param hasPicture 是否有图筛选（可选）
     * @param tag        标签筛选（可选）
     * @param orderBy    排序方式
     * @return 评价分页列表
     */
    public Map<String, Object> getCommentPage(Long goodsId, Integer page, Integer pageSize,
                                               Integer hasPicture, String tag, String orderBy) {
        Page<GoodsComment> pageObj = new Page<>(page, pageSize);

        // 如果标签不为空，先筛选出符合条件的评价
        Page<GoodsComment> commentPage;
        if (tag != null && !tag.isEmpty()) {
            // 获取所有评价，在内存中按标签筛选
            Page<GoodsComment> tempPage = goodsCommentMapper.selectByGoodsIdPage(goodsId, pageObj, hasPicture, orderBy);
            List<GoodsComment> filtered = tempPage.getRecords().stream()
                    .filter(c -> c.getTags() != null && c.getTags().contains(tag))
                    .collect(Collectors.toList());
            commentPage = tempPage;
            commentPage.setRecords(filtered);
        } else {
            commentPage = goodsCommentMapper.selectByGoodsIdPage(goodsId, pageObj, hasPicture, orderBy);
        }

        // 组装结果
        List<Map<String, Object>> items = new ArrayList<>();
        for (GoodsComment comment : commentPage.getRecords()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", comment.getId());
            item.put("content", comment.getContent());
            item.put("score", comment.getScore());
            item.put("tags", comment.getTags());
            item.put("createTime", comment.getCreateTime());

            // 查询评价图片
            List<GoodsCommentPicture> pictures = goodsCommentPictureMapper.selectByCommentId(comment.getId());
            item.put("pictures", pictures.stream()
                    .map(GoodsCommentPicture::getPictureUrl)
                    .collect(Collectors.toList()));

            items.add(item);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pages", commentPage.getTotalPage());
        result.put("counts", commentPage.getTotalRow());
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("items", items);
        return result;
    }

    /**
     * 获取商品评价标签统计
     */
    private List<Map<String, Object>> getCommentTags(Long goodsId) {
        // 由于MyBatis-Flex的GroupBy支持有限，这里简化处理
        // 实际生产环境可用SQL查询按标签分组统计
        List<GoodsComment> comments = goodsCommentMapper.selectListByQuery(
                com.mybatisflex.core.query.QueryWrapper.create()
                        .eq("goods_id", goodsId)
                        .eq("is_audit", 1)
        );

        // 解析标签并统计
        Map<String, Integer> tagCountMap = new HashMap<>();
        for (GoodsComment comment : comments) {
            if (comment.getTags() != null && !comment.getTags().isEmpty()) {
                // 标签以逗号分隔的字符串形式存储
                String[] tagArr = comment.getTags().split(",");
                for (String tag : tagArr) {
                    tag = tag.trim();
                    if (!tag.isEmpty()) {
                        tagCountMap.merge(tag, 1, Integer::sum);
                    }
                }
            }
        }

        return tagCountMap.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> tagItem = new LinkedHashMap<>();
                    tagItem.put("name", entry.getKey());
                    tagItem.put("count", entry.getValue());
                    return tagItem;
                })
                .collect(Collectors.toList());
    }
}
