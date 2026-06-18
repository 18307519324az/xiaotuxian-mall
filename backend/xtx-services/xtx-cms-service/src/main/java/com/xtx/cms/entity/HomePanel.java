package com.xtx.cms.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 首页面板实体类
 * 对应数据库表 home_panel，存储首页商品板块配置（新品推荐、人气推荐等）
 */
@Data
@Table("home_panel")
public class HomePanel {

    /** 主键ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 面板标题 */
    private String title;

    /** 面板类型：NEW-新品推荐，HOT-人气推荐 */
    private String type;

    /** 排序权重（值越大越靠前） */
    private Integer sort;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    /** 关联商品ID列表（JSON 数组格式） */
    private String goodsIds;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
