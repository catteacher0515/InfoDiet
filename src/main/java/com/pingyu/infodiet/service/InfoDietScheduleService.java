package com.pingyu.infodiet.service;

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
         * 推送成功数量
         */
        private int pushSuccessCount;

        /**
         * 推送失败数量
         */
        private int pushFailedCount;
    }
}
