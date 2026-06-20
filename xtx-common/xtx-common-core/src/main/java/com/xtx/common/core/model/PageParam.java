package com.xtx.common.core.model;

import lombok.Data;

/**
 * 分页查询参数。
 * 用于接收前端传递的分页请求参数，默认第一页，每页 20 条。
 */
@Data
public class PageParam {

    /** 当前页码，默认第 1 页 */
    private Integer page = 1;

    /** 每页记录数，默认 20 条 */
    private Integer pageSize = 20;
}
