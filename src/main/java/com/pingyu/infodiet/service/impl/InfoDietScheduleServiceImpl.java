package com.pingyu.infodiet.service.impl;

import com.pingyu.infodiet.config.InfoDietProperties;
import com.pingyu.infodiet.model.dto.github.GithubTrendingItemDTO;
import com.pingyu.infodiet.model.entity.CrawlTaskLog;
import com.pingyu.infodiet.service.AlertRecordService;
import com.pingyu.infodiet.service.CrawlTaskLogService;
import com.pingyu.infodiet.service.ContentItemService;
import com.pingyu.infodiet.service.GithubTrendingService;
import com.pingyu.infodiet.service.InfoDietScheduleService;
import com.pingyu.infodiet.service.PushQueueService;
import com.pingyu.infodiet.service.SourceSubscriptionCrawlService;
import com.pingyu.infodiet.service.UserContentPushService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 信息节食调度服务实现
 */
@Service
public class InfoDietScheduleServiceImpl implements InfoDietScheduleService {

    @Resource
    private GithubTrendingService githubTrendingService;

    @Resource
    private ContentItemService contentItemService;

    @Resource
    private UserContentPushService userContentPushService;

    @Resource
    private PushQueueService pushQueueService;

    @Resource
    private InfoDietProperties infoDietProperties;

    @Resource
    private SourceSubscriptionCrawlService sourceSubscriptionCrawlService;

    @Resource
    private CrawlTaskLogService crawlTaskLogService;

    @Resource
    private AlertRecordService alertRecordService;

    /**
     * 执行每日 GitHub 流程
     */
    @Override
    public ScheduleResult runDailyGithubFlow() {
        LocalDateTime startTime = now();
        try {
            List<GithubTrendingItemDTO> dtoList = githubTrendingService.crawlGitHubTrending();
            ContentItemService.SaveResult saveResult = contentItemService.saveGithubTrendingItems(dtoList);
            ContentItemService.KeywordFilterResult filterResult = contentItemService
                    .filterByKeywords(infoDietProperties.getKeywords());
            userContentPushService.createPendingPushes();
            PushQueueService.EnqueuePushResult enqueuePushResult = pushQueueService.enqueuePendingPushes("feishu");
            ScheduleResult result = new ScheduleResult(
                    dtoList.size(),
                    saveResult.getSavedCount(),
                    saveResult.getSkippedCount(),
                    filterResult.getMatchedCount(),
                    filterResult.getUnmatchedCount(),
                    enqueuePushResult.enqueuedCount(),
                    enqueuePushResult.skippedCount()
            );
            CrawlTaskLog taskLog = crawlTaskLogService.buildSuccessLog(
                    "github_daily_flow",
                    "system",
                    startTime,
                    0,
                    result.getCrawlCount(),
                    result.getSavedCount(),
                    result.getSkippedCount(),
                    result.getMatchedCount(),
                    result.getUnmatchedCount(),
                    result.getEnqueuedCount(),
                    result.getEnqueueSkippedCount()
            );
            crawlTaskLogService.save(taskLog);
            return result;
        } catch (RuntimeException e) {
            crawlTaskLogService.save(crawlTaskLogService.buildFailedLog("github_daily_flow", "system", startTime, e));
            createTaskFailedAlert(e);
            throw e;
        }
    }

    /**
     * 执行每日 YouTube 订阅源采集流程
     */
    @Override
    public SourceSubscriptionCrawlService.CrawlResult runDailyYoutubeSourceFlow() {
        LocalDateTime startTime = now();
        try {
            SourceSubscriptionCrawlService.CrawlResult result = sourceSubscriptionCrawlService.crawlAllSourceSubscriptions();
            crawlTaskLogService.save(crawlTaskLogService.buildSuccessLog(
                    "youtube_source_crawl_flow",
                    "system",
                    startTime,
                    result.getSubscriptionCount(),
                    result.getCrawlCount(),
                    result.getSavedCount(),
                    result.getSkippedCount(),
                    0,
                    0,
                    0,
                    0
            ));
            return result;
        } catch (RuntimeException e) {
            crawlTaskLogService.save(crawlTaskLogService.buildFailedLog("youtube_source_crawl_flow", "system", startTime, e));
            createTaskFailedAlert(e);
            throw e;
        }
    }

    /**
     * 执行每日 YouTube 订阅源推送流程
     */
    @Override
    public YoutubeSourceScheduleResult runDailyYoutubeSourcePushFlow() {
        LocalDateTime startTime = now();
        try {
            SourceSubscriptionCrawlService.CrawlResult crawlResult = sourceSubscriptionCrawlService
                    .crawlAllSourceSubscriptions();
            UserContentPushService.CreatePushResult createPushResult = userContentPushService.createPendingPushes();
            PushQueueService.EnqueuePushResult enqueuePushResult = pushQueueService.enqueuePendingPushes("feishu");
            YoutubeSourceScheduleResult result = new YoutubeSourceScheduleResult(
                    crawlResult.getSubscriptionCount(),
                    crawlResult.getCrawlCount(),
                    crawlResult.getSavedCount(),
                    crawlResult.getSkippedCount(),
                    createPushResult.getCreatedCount(),
                    createPushResult.getSkippedCount(),
                    enqueuePushResult.enqueuedCount(),
                    enqueuePushResult.skippedCount()
            );
            crawlTaskLogService.save(crawlTaskLogService.buildSuccessLog(
                    "youtube_source_push_flow",
                    "system",
                    startTime,
                    result.getSubscriptionCount(),
                    result.getCrawlCount(),
                    result.getSavedCount(),
                    result.getSkippedCount(),
                    result.getPendingPushCreatedCount(),
                    result.getPendingPushSkippedCount(),
                    result.getEnqueuedCount(),
                    result.getEnqueueSkippedCount()
            ));
            return result;
        } catch (RuntimeException e) {
            crawlTaskLogService.save(crawlTaskLogService.buildFailedLog("youtube_source_push_flow", "system", startTime, e));
            createTaskFailedAlert(e);
            throw e;
        }
    }

    /**
     * 记录任务失败告警
     */
    protected void createTaskFailedAlert(RuntimeException e) {
        alertRecordService.createOrUpdateAlert(
                "task_failed",
                "error",
                "crawl_task_log",
                null,
                "调度任务执行失败",
                e == null ? "未知异常" : e.getMessage()
        );
    }

    /**
     * 获取当前时间
     */
    protected LocalDateTime now() {
        return LocalDateTime.now();
    }
}
