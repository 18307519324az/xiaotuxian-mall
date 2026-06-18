package com.xtx.common.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页查询结果。
 * 封装分页返回的数据集合及分页信息。
 *
 * @param <T> 列表数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /** 当前页数据列表 */
    private List<T> items;

    /** 总记录数 */
    private Integer total;

    /** 当前页码 */
    private Integer page;

    /** 每页记录数 */
    private Integer pageSize;
}
