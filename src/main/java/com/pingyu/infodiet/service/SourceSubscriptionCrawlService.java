package com.pingyu.infodiet.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订阅源采集服务。
 */
public interface SourceSubscriptionCrawlService {

    /**
     * 抓取 YouTube 频道订阅源
     */
    CrawlResult crawlYoutubeSourceSubscriptions();

    /**
     * 抓取 GitHub 订阅源
     */
    CrawlResult crawlGithubSourceSubscriptions();

    /**
     * 抓取全部订阅源
     */
    CrawlResult crawlAllSourceSubscriptions();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class CrawlResult {

        /**
         * 本次处理订阅源数量
         */
        private int subscriptionCount;

        /**
         * 本次抓取内容数量
         */
        private int crawlCount;

        /**
         * 本次新增入库数量
         */
        private int savedCount;

        /**
         * 本次跳过数量
         */
        private int skippedCount;
    }
}
