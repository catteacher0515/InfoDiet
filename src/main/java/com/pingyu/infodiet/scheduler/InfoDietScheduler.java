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
}
