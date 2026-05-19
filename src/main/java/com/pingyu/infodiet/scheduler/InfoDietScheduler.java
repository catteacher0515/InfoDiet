package com.pingyu.infodiet.scheduler;

import com.pingyu.infodiet.service.InfoDietScheduleService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 信息节食定时任务
 */
@Component
@Slf4j
public class InfoDietScheduler {

    @Resource
    private InfoDietScheduleService infoDietScheduleService;

    /**
     * 定时执行每日 GitHub 流程
     */
    @Scheduled(cron = "${info-diet.github-daily-cron:0 0 9 * * ?}")
    public void runDailyGithubFlow() {
        InfoDietScheduleService.ScheduleResult result = infoDietScheduleService.runDailyGithubFlow();
        log.info("信息节食定时任务执行完成：{}", result);
    }

    /**
     * 定时执行每日 YouTube 订阅源采集
     */
    @Scheduled(cron = "${info-diet.youtube-source-daily-cron:0 30 9 * * ?}")
    public void runDailyYoutubeSourcePushFlow() {
        var result = infoDietScheduleService.runDailyYoutubeSourcePushFlow();
        log.info("信息节食 YouTube 订阅源定时推送完成：{}", result);
    }

    /**
     * 定时执行 AI 日报推送
     */
    @Scheduled(cron = "${info-diet.daily-digest-cron:0 0 8 * * ?}")
    public void runDailyDigestPushFlow() {
        var result = infoDietScheduleService.runDailyDigestPushFlow();
        log.info("信息节食 AI 日报定时推送完成：{}", result);
    }
}
