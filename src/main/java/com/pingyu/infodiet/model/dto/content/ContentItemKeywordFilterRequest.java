package com.pingyu.infodiet.model.dto.content;

import lombok.Data;

import java.util.List;

/**
 * 关键词过滤请求
 */
@Data
public class ContentItemKeywordFilterRequest {

    /**
     * 关键词列表
     */
    private List<String> keywords;
}
