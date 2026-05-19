package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.entity.CrawlTaskLog;
import com.pingyu.infodiet.config.InfoDietProperties;
import com.pingyu.infodiet.model.dto.github.GithubTrendingItemDTO;
import com.pingyu.infodiet.service.SourceSubscriptionCrawlService;
import com.pingyu.infodiet.service.impl.InfoDietScheduleServiceImpl;
import com.pingyu.infodiet.service.FeishuPushService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InfoDietScheduleServiceTest {

    @Test
    void runDailyGithubFlowShouldOrchestrateWholePipeline() {
        GithubTrendingService githubTrendingService = Mockito.mock(GithubTrendingService.class);
        ContentItemService contentItemService = Mockito.mock(ContentItemService.class);
        ContentPreFilterService contentPreFilterService = Mockito.mock(ContentPreFilterService.class);
        ContentScoringService contentScoringService = Mockito.mock(ContentScoringService.class);
        PushQueueService pushQueueService = Mockito.mock(PushQueueService.class);
        UserContentPushService userContentPushService = Mockito.mock(UserContentPushService.class);
        CrawlTaskLogService crawlTaskLogService = Mockito.mock(CrawlTaskLogService.class);
        AlertRecordService alertRecordService = Mockito.mock(AlertRecordService.class);
        when(crawlTaskLogService.buildSuccessLog(
                any(), any(), any(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt()
        )).thenReturn(CrawlTaskLog.builder().taskType("github_daily_flow").taskStatus(1).build());

        GithubTrendingItemDTO first = new GithubTrendingItemDTO();
        GithubTrendingItemDTO second = new GithubTrendingItemDTO();
        List<GithubTrendingItemDTO> dtoList = List.of(first, second);

        when(githubTrendingService.crawlGitHubTrending()).thenReturn(dtoList);
        when(contentItemService.saveGithubTrendingItems(dtoList))
                .thenReturn(new ContentItemService.SaveResult(2, 1, 1));
        when(contentPreFilterService.runSystemPreFilter())
                .thenReturn(new ContentItemService.PreFilterResult(2, 2, 0, 0));
        when(contentScoringService.runQualityScoring())
                .thenReturn(new ContentItemService.QualityScoreResult(2, 2, 0));
        when(contentItemService.filterByKeywords(List.of("agent", "workflow")))
                .thenReturn(new ContentItemService.KeywordFilterResult(2, 1, 1));
        when(userContentPushService.createPendingPushes())
                .thenReturn(new UserContentPushService.CreatePushResult(1, 1, 0));
        when(pushQueueService.enqueuePendingPushes("feishu"))
                .thenReturn(new PushQueueService.EnqueuePushResult(1, 1, 0));

        InfoDietProperties infoDietProperties = new InfoDietProperties();
        infoDietProperties.setKeywords(List.of("agent", "workflow"));

        InfoDietScheduleServiceImpl service = new InfoDietScheduleServiceImpl();
        ReflectionTestUtils.setField(service, "githubTrendingService", githubTrendingService);
        ReflectionTestUtils.setField(service, "contentItemService", contentItemService);
        ReflectionTestUtils.setField(service, "contentPreFilterService", contentPreFilterService);
        ReflectionTestUtils.setField(service, "contentScoringService", contentScoringService);
        ReflectionTestUtils.setField(service, "userContentPushService", userContentPushService);
        ReflectionTestUtils.setField(service, "pushQueueService", pushQueueService);
        ReflectionTestUtils.setField(service, "infoDietProperties", infoDietProperties);
        ReflectionTestUtils.setField(service, "crawlTaskLogService", crawlTaskLogService);
        ReflectionTestUtils.setField(service, "alertRecordService", alertRecordService);

        InfoDietScheduleService.ScheduleResult result = service.runDailyGithubFlow();

        assertEquals(2, result.getCrawlCount());
        assertEquals(1, result.getSavedCount());
        assertEquals(1, result.getSkippedCount());
        assertEquals(1, result.getMatchedCount());
        assertEquals(1, result.getUnmatchedCount());
        assertEquals(1, result.getEnqueuedCount());
        assertEquals(0, result.getEnqueueSkippedCount());

        verify(githubTrendingService).crawlGitHubTrending();
        verify(contentItemService).saveGithubTrendingItems(dtoList);
        verify(contentPreFilterService).runSystemPreFilter();
        verify(contentScoringService).runQualityScoring();
        verify(contentItemService).filterByKeywords(List.of("agent", "workflow"));
        verify(userContentPushService).createPendingPushes();
        verify(pushQueueService).enqueuePendingPushes("feishu");
        verify(crawlTaskLogService).save(any(CrawlTaskLog.class));
    }

    @Test
    void runDailyYoutubeSourceFlowShouldInvokeSourceSubscriptionCrawl() {
        SourceSubscriptionCrawlService sourceSubscriptionCrawlService = Mockito.mock(SourceSubscriptionCrawlService.class);
        ContentPreFilterService contentPreFilterService = Mockito.mock(ContentPreFilterService.class);
        ContentScoringService contentScoringService = Mockito.mock(ContentScoringService.class);
        CrawlTaskLogService crawlTaskLogService = Mockito.mock(CrawlTaskLogService.class);
        AlertRecordService alertRecordService = Mockito.mock(AlertRecordService.class);
        when(crawlTaskLogService.buildSuccessLog(
                any(), any(), any(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt()
        )).thenReturn(CrawlTaskLog.builder().taskType("youtube_source_crawl_flow").taskStatus(1).build());
        when(sourceSubscriptionCrawlService.crawlAllSourceSubscriptions())
                .thenReturn(new SourceSubscriptionCrawlService.CrawlResult(2, 6, 4, 2));
        when(contentPreFilterService.runSystemPreFilter())
                .thenReturn(new ContentItemService.PreFilterResult(6, 4, 2, 0));
        when(contentScoringService.runQualityScoring())
                .thenReturn(new ContentItemService.QualityScoreResult(4, 4, 0));

        InfoDietScheduleServiceImpl service = new InfoDietScheduleServiceImpl();
        ReflectionTestUtils.setField(service, "sourceSubscriptionCrawlService", sourceSubscriptionCrawlService);
        ReflectionTestUtils.setField(service, "contentPreFilterService", contentPreFilterService);
        ReflectionTestUtils.setField(service, "contentScoringService", contentScoringService);
        ReflectionTestUtils.setField(service, "crawlTaskLogService", crawlTaskLogService);
        ReflectionTestUtils.setField(service, "alertRecordService", alertRecordService);

        SourceSubscriptionCrawlService.CrawlResult result = service.runDailyYoutubeSourceFlow();

        assertEquals(2, result.getSubscriptionCount());
        assertEquals(6, result.getCrawlCount());
        assertEquals(4, result.getSavedCount());
        assertEquals(2, result.getSkippedCount());
        verify(sourceSubscriptionCrawlService).crawlAllSourceSubscriptions();
        verify(contentPreFilterService).runSystemPreFilter();
        verify(contentScoringService).runQualityScoring();
        verify(crawlTaskLogService).save(any(CrawlTaskLog.class));
    }

    @Test
    void runDailyYoutubeSourcePushFlowShouldOrchestrateWholePipeline() {
        SourceSubscriptionCrawlService sourceSubscriptionCrawlService = Mockito.mock(SourceSubscriptionCrawlService.class);
        ContentPreFilterService contentPreFilterService = Mockito.mock(ContentPreFilterService.class);
        ContentScoringService contentScoringService = Mockito.mock(ContentScoringService.class);
        UserContentPushService userContentPushService = Mockito.mock(UserContentPushService.class);
        PushQueueService pushQueueService = Mockito.mock(PushQueueService.class);
        CrawlTaskLogService crawlTaskLogService = Mockito.mock(CrawlTaskLogService.class);
        AlertRecordService alertRecordService = Mockito.mock(AlertRecordService.class);
        when(crawlTaskLogService.buildSuccessLog(
                any(), any(), any(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt()
        )).thenReturn(CrawlTaskLog.builder().taskType("youtube_source_push_flow").taskStatus(1).build());

        when(sourceSubscriptionCrawlService.crawlAllSourceSubscriptions())
                .thenReturn(new SourceSubscriptionCrawlService.CrawlResult(2, 6, 4, 2));
        when(contentPreFilterService.runSystemPreFilter())
                .thenReturn(new ContentItemService.PreFilterResult(6, 4, 2, 0));
        when(contentScoringService.runQualityScoring())
                .thenReturn(new ContentItemService.QualityScoreResult(4, 4, 0));
        when(userContentPushService.createPendingPushes())
                .thenReturn(new UserContentPushService.CreatePushResult(5, 3, 2));
        when(pushQueueService.enqueuePendingPushes("feishu"))
                .thenReturn(new PushQueueService.EnqueuePushResult(3, 3, 0));

        InfoDietScheduleServiceImpl service = new InfoDietScheduleServiceImpl();
        ReflectionTestUtils.setField(service, "sourceSubscriptionCrawlService", sourceSubscriptionCrawlService);
        ReflectionTestUtils.setField(service, "contentPreFilterService", contentPreFilterService);
        ReflectionTestUtils.setField(service, "contentScoringService", contentScoringService);
        ReflectionTestUtils.setField(service, "userContentPushService", userContentPushService);
        ReflectionTestUtils.setField(service, "pushQueueService", pushQueueService);
        ReflectionTestUtils.setField(service, "crawlTaskLogService", crawlTaskLogService);
        ReflectionTestUtils.setField(service, "alertRecordService", alertRecordService);

        InfoDietScheduleService.YoutubeSourceScheduleResult result = service.runDailyYoutubeSourcePushFlow();

        assertEquals(2, result.getSubscriptionCount());
        assertEquals(6, result.getCrawlCount());
        assertEquals(4, result.getSavedCount());
        assertEquals(2, result.getSkippedCount());
        assertEquals(3, result.getPendingPushCreatedCount());
        assertEquals(2, result.getPendingPushSkippedCount());
        assertEquals(3, result.getEnqueuedCount());
        assertEquals(0, result.getEnqueueSkippedCount());

        verify(sourceSubscriptionCrawlService).crawlAllSourceSubscriptions();
        verify(contentPreFilterService).runSystemPreFilter();
        verify(contentScoringService).runQualityScoring();
        verify(userContentPushService).createPendingPushes();
        verify(pushQueueService).enqueuePendingPushes("feishu");
        verify(crawlTaskLogService).save(any(CrawlTaskLog.class));
    }

    @Test
    void runDailyDigestPushFlowShouldPushDigestAndWriteTaskLog() {
        FeishuPushService feishuPushService = Mockito.mock(FeishuPushService.class);
        CrawlTaskLogService crawlTaskLogService = Mockito.mock(CrawlTaskLogService.class);
        AlertRecordService alertRecordService = Mockito.mock(AlertRecordService.class);
        when(feishuPushService.pushTodayDigestToFeishu())
                .thenReturn(new FeishuPushService.PushResult(5, 4, 1));
        when(crawlTaskLogService.buildSuccessLog(
                any(), any(), any(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt()
        )).thenReturn(CrawlTaskLog.builder().taskType("daily_digest_push_flow").taskStatus(1).build());

        InfoDietScheduleServiceImpl service = new InfoDietScheduleServiceImpl();
        ReflectionTestUtils.setField(service, "feishuPushService", feishuPushService);
        ReflectionTestUtils.setField(service, "crawlTaskLogService", crawlTaskLogService);
        ReflectionTestUtils.setField(service, "alertRecordService", alertRecordService);

        FeishuPushService.PushResult result = service.runDailyDigestPushFlow();

        assertEquals(5, result.getTotalCount());
        assertEquals(4, result.getSuccessCount());
        assertEquals(1, result.getFailedCount());
        verify(feishuPushService).pushTodayDigestToFeishu();
        verify(crawlTaskLogService).save(any(CrawlTaskLog.class));
    }

    @Test
    void runDailyGithubFlowShouldWriteFailureTaskLogWhenExceptionThrown() {
        GithubTrendingService githubTrendingService = Mockito.mock(GithubTrendingService.class);
        CrawlTaskLogService crawlTaskLogService = Mockito.mock(CrawlTaskLogService.class);
        AlertRecordService alertRecordService = Mockito.mock(AlertRecordService.class);
        when(githubTrendingService.crawlGitHubTrending()).thenThrow(new IllegalStateException("GitHub 抓取失败"));
        when(crawlTaskLogService.buildFailedLog(any(), any(), any(), any()))
                .thenReturn(CrawlTaskLog.builder().taskType("github_daily_flow").taskStatus(2).errorMessage("GitHub 抓取失败").build());

        InfoDietScheduleServiceImpl service = new InfoDietScheduleServiceImpl();
        ReflectionTestUtils.setField(service, "githubTrendingService", githubTrendingService);
        ReflectionTestUtils.setField(service, "crawlTaskLogService", crawlTaskLogService);
        ReflectionTestUtils.setField(service, "alertRecordService", alertRecordService);

        try {
            service.runDailyGithubFlow();
        } catch (IllegalStateException e) {
            assertEquals("GitHub 抓取失败", e.getMessage());
        }

        var captor = org.mockito.ArgumentCaptor.forClass(CrawlTaskLog.class);
        verify(crawlTaskLogService).save(captor.capture());
        CrawlTaskLog taskLog = captor.getValue();
        assertEquals("github_daily_flow", taskLog.getTaskType());
        assertEquals(2, taskLog.getTaskStatus());
        assertNotNull(taskLog.getErrorMessage());
        verify(alertRecordService).createOrUpdateAlert(
                Mockito.eq("task_failed"),
                Mockito.eq("error"),
                Mockito.eq("crawl_task_log"),
                Mockito.isNull(),
                Mockito.eq("调度任务执行失败"),
                Mockito.contains("GitHub 抓取失败")
        );
    }

    @Test
    void runDailyDigestPushFlowShouldWriteFailureTaskLogWhenExceptionThrown() {
        FeishuPushService feishuPushService = Mockito.mock(FeishuPushService.class);
        CrawlTaskLogService crawlTaskLogService = Mockito.mock(CrawlTaskLogService.class);
        AlertRecordService alertRecordService = Mockito.mock(AlertRecordService.class);
        when(feishuPushService.pushTodayDigestToFeishu()).thenThrow(new IllegalStateException("日报推送失败"));
        when(crawlTaskLogService.buildFailedLog(any(), any(), any(), any()))
                .thenReturn(CrawlTaskLog.builder().taskType("daily_digest_push_flow").taskStatus(2).errorMessage("日报推送失败").build());

        InfoDietScheduleServiceImpl service = new InfoDietScheduleServiceImpl();
        ReflectionTestUtils.setField(service, "feishuPushService", feishuPushService);
        ReflectionTestUtils.setField(service, "crawlTaskLogService", crawlTaskLogService);
        ReflectionTestUtils.setField(service, "alertRecordService", alertRecordService);

        try {
            service.runDailyDigestPushFlow();
        } catch (IllegalStateException e) {
            assertEquals("日报推送失败", e.getMessage());
        }

        var captor = org.mockito.ArgumentCaptor.forClass(CrawlTaskLog.class);
        verify(crawlTaskLogService).save(captor.capture());
        CrawlTaskLog taskLog = captor.getValue();
        assertEquals("daily_digest_push_flow", taskLog.getTaskType());
        assertEquals(2, taskLog.getTaskStatus());
        assertNotNull(taskLog.getErrorMessage());
        verify(alertRecordService).createOrUpdateAlert(
                Mockito.eq("task_failed"),
                Mockito.eq("error"),
                Mockito.eq("crawl_task_log"),
                Mockito.isNull(),
                Mockito.eq("调度任务执行失败"),
                Mockito.contains("日报推送失败")
        );
    }

    @Test
    void rerunTaskShouldDispatchByTaskType() {
        InfoDietScheduleServiceImpl service = Mockito.spy(new InfoDietScheduleServiceImpl());
        Mockito.doReturn(new InfoDietScheduleService.ScheduleResult(1, 1, 0, 1, 0, 1, 0))
                .when(service).runDailyGithubFlow();
        Mockito.doReturn(new FeishuPushService.PushResult(2, 2, 0))
                .when(service).runDailyDigestPushFlow();

        Object result = service.rerunTask("github_daily_flow");
        Object digestResult = service.rerunTask("daily_digest_push_flow");

        assertNotNull(result);
        assertNotNull(digestResult);
        verify(service).runDailyGithubFlow();
        verify(service).runDailyDigestPushFlow();
    }

    @Test
    void rerunTaskShouldThrowWhenTaskTypeUnsupported() {
        InfoDietScheduleServiceImpl service = new InfoDietScheduleServiceImpl();

        assertThrows(IllegalArgumentException.class, () -> service.rerunTask("unknown_task"));
    }
}
