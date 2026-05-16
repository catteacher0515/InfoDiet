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
     * YouTube 订阅源日流程 cron 表达式
     */
    private String youtubeSourceDailyCron = "0 30 9 * * ?";

    /**
     * 推送队列名称
     */
    private String pushQueueName = "info_diet_push_queue";
}
