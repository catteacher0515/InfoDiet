package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.entity.ContentItem;

/**
 * 内容评分服务
 */
public interface ContentScoringService {

    /**
     * 按系统规则执行内容评分
     */
    ContentItemService.QualityScoreResult runQualityScoring();

    /**
     * 计算单条内容质量分
     */
    ScoreDetail evaluate(ContentItem contentItem);

    record ScoreDetail(int score, String reason) {
    }
}
