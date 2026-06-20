package com.xtx.common.mybatisflex.model;

import com.mybatisflex.annotation.Column;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 实体基类。
 * 所有数据库实体可继承此类，统一审计字段（创建时间、更新时间、逻辑删除标记）。
 */
@Data
public class BaseEntity {

    /** 创建时间，插入时自动填充当前时间 */
    @Column(onInsertValue = "NOW()")
    private LocalDateTime createTime;

    /** 更新时间，插入和更新时自动填充当前时间 */
    @Column(onInsertValue = "NOW()", onUpdateValue = "NOW()")
    private LocalDateTime updateTime;

    /** 逻辑删除标记（0-未删除，1-已删除），默认未删除 */
    @Column(isLogicDelete = true)
    private Integer deleted = 0;
}
