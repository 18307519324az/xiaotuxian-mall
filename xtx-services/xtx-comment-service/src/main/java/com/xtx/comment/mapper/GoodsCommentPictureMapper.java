package com.xtx.comment.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xtx.comment.entity.GoodsCommentPicture;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * 评价图片 Mapper 接口
 */
@Mapper
public interface GoodsCommentPictureMapper extends BaseMapper<GoodsCommentPicture> {

    /**
     * 根据评价ID查询图片列表
     *
     * @param commentId 评价ID
     * @return 图片列表
     */
    default List<GoodsCommentPicture> selectByCommentId(Long commentId) {
        return selectListByQuery(
                QueryWrapper.create().eq("comment_id", commentId)
        );
    }
}
