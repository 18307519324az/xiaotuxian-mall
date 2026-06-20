package com.xtx.comment.entity;

import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价图片实体类
 */
@Data
@Table("goods_comment_picture")
public class GoodsCommentPicture {

    /** 主键ID */
    private Long id;

    /** 评价ID */
    private Long commentId;

    /** 图片URL */
    private String pictureUrl;

    /** 创建时间 */
    private LocalDateTime createTime;
}
