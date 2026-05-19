package com.pingyu.infodiet.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 信息节食配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "info-diet")
public class InfoDietProperties {

    /**
     * 关键词列表
     */
    private List<String> keywords = new ArrayList<>();

    /**
     * GitHub 日流程 cron 表达式
     */
    private String githubDailyCron = "0 0 9 * * ?";

    /**
     * 内容预筛排除关键词
     */
    private List<String> preFilterExcludeKeywords = new ArrayList<>();

    /**
     * 精选最低质量分
     */
    private Integer featuredMinQualityScore = 70;

    /**
     * YouTube 订阅源日流程 cron 表达式
     */
    private String youtubeSourceDailyCron = "0 30 9 * * ?";

    /**
     * AI 日报日推送 cron 表达式
     */
    private String dailyDigestCron = "0 0 8 * * ?";

    /**
     * 推送队列名称
     */
    private String pushQueueName = "info_diet_push_queue";
}
