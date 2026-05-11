package com.pingyu.infodiet.model.dto.content;

import lombok.Data;

/**
 * 统一内容查询请求
 */
@Data
public class UnifiedContentQueryRequest {

    /**
     * 平台
     */
    private String platform;

    /**
     * 内容类型
     */
    private String contentType;

    /**
     * 排序方式 time / metric
     */
    private String sortBy;

    /**
     * 限制条数
     */
    private Integer limit;
}
