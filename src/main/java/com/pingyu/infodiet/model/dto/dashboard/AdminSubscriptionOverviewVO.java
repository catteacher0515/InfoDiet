package com.pingyu.infodiet.model.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理区订阅总览
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminSubscriptionOverviewVO {

    /**
     * 关键词订阅总数
     */
    private int keywordCount;

    /**
     * 规则总数
     */
    private int ruleCount;

    /**
     * 订阅源总数
     */
    private int sourceCount;

    /**
     * 启用用户数
     */
    private int enabledUserCount;

    /**
     * 平均每用户关键词数
     */
    private double avgKeywordPerUser;

    /**
     * 平均每用户规则数
     */
    private double avgRulePerUser;

    /**
     * 平均每用户订阅源数
     */
    private double avgSourcePerUser;

    /**
     * YouTube 订阅源数
     */
    private int youtubeSourceCount;

    /**
     * GitHub 订阅源数
     */
    private int githubSourceCount;

    /**
     * 频道订阅数
     */
    private int channelSourceCount;

    /**
     * 仓库订阅数
     */
    private int repoSourceCount;

    /**
     * 作者订阅数
     */
    private int authorSourceCount;
}
