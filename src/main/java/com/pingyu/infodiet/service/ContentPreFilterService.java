package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.entity.ContentItem;

import java.util.List;

/**
 * 内容预筛服务
 */
public interface ContentPreFilterService {

    /**
     * 按系统配置执行预筛
     */
    ContentItemService.PreFilterResult runSystemPreFilter();

    /**
     * 按指定规则执行预筛
     */
    ContentItemService.PreFilterResult runPreFilter(List<String> includeKeywords, List<String> excludeKeywords);

    /**
     * 评估单条内容预筛结果
     */
    PreFilterDecision evaluate(ContentItem contentItem, List<String> includeKeywords, List<String> excludeKeywords);

    record PreFilterDecision(int status, String reason) {
    }
}
