package com.pingyu.infodiet.service;

import com.pingyu.infodiet.service.SourceSubscriptionCrawlService.CrawlResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 信息节食调度服务
 */
public interface InfoDietScheduleService {

    /**
     * 执行每日 GitHub 流程
     */
    ScheduleResult runDailyGithubFlow();

    /**
     * 执行每日 YouTube 订阅源采集流程
     */
    CrawlResult runDailyYoutubeSourceFlow();

    /**
     * 执行每日 YouTube 订阅源推送流程
     */
    YoutubeSourceScheduleResult runDailyYoutubeSourcePushFlow();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class ScheduleResult {

        /**
         * 抓取数量
         */
        private int crawlCount;

        /**
         * 新增数量
         */
        private int savedCount;

        /**
         * 跳过数量
         */
        private int skippedCount;

        /**
         * 命中数量
         */
        private int matchedCount;

        /**
         * 未命中数量
         */
        private int unmatchedCount;

        /**
         * 成功入队数量
         */
        private int enqueuedCount;

        /**
         * 入队跳过数量
         */
        private int enqueueSkippedCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class YoutubeSourceScheduleResult {

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

        /**
         * 本次新增待推送数量
         */
        private int pendingPushCreatedCount;

        /**
         * 本次跳过待推送数量
         */
        private int pendingPushSkippedCount;

        /**
         * 本次成功入队数量
         */
        private int enqueuedCount;

        /**
         * 本次入队跳过数量
         */
        private int enqueueSkippedCount;
    }
}
