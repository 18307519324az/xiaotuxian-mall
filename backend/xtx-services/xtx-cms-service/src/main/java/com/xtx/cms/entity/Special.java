package com.xtx.cms.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 专题活动实体类
 * 对应数据库表 special，存储精选专题活动数据
 */
@Data
@Table("special")
public class Special {

    /** 主键ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 专题标题 */
    private String title;

    /** 专题副标题（前端映射为 summary） */
    private String subtitle;

    /** 专题封面 */
    private String cover;

    /** 专题内容 */
    private String content;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    /** 排序权重 */
    private Integer sortOrder;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
