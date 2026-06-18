package com.xtx.api.category.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResultVO {

    /** 当前页码 */
    private Integer page;

    /** 每页条数 */
    private Integer pageSize;

    /** 总记录数 */
    private Integer total;

    /** 当前页数据 */
    private List<GoodsCardVO> items;
}
