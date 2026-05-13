package com.pingyu.infodiet.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    /**
     * 总数
     */
    private long totalCount;

    /**
     * 当前页
     */
    private int pageNum;

    /**
     * 每页大小
     */
    private int pageSize;

    /**
     * 当前页数据
     */
    private List<T> records;
}
