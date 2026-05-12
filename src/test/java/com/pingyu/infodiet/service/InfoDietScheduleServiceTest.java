package com.pingyu.infodiet.service;

import com.pingyu.infodiet.config.InfoDietProperties;
import com.pingyu.infodiet.model.dto.github.GithubTrendingItemDTO;
import com.pingyu.infodiet.service.SourceSubscriptionCrawlService;
import com.pingyu.infodiet.service.impl.InfoDietScheduleServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InfoDietScheduleServiceTest {

    @Test
    void runDailyGithubFlowShouldOrchestrateWholePipeline() {
        GithubTrendingService githubTrendingService = Mockito.mock(GithubTrendingService.class);
        ContentItemService contentItemService = Mockito.mock(ContentItemService.class);
        PushQueueService pushQueueService = Mockito.mock(PushQueueService.class);
        UserContentPushService userContentPushService = Mockito.mock(UserContentPushService.class);

        GithubTrendingItemDTO first = new GithubTrendingItemDTO();
        GithubTrendingItemDTO second = new GithubTrendingItemDTO();
        List<GithubTrendingItemDTO> dtoList = List.of(first, second);

        when(githubTrendingService.crawlGitHubTrending()).thenReturn(dtoList);
        when(contentItemService.saveGithubTrendingItems(dtoList))
                .thenReturn(new ContentItemService.SaveResult(2, 1, 1));
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
        ReflectionTestUtils.setField(service, "userContentPushService", userContentPushService);
        ReflectionTestUtils.setField(service, "pushQueueService", pushQueueService);
        ReflectionTestUtils.setField(service, "infoDietProperties", infoDietProperties);

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
        verify(contentItemService).filterByKeywords(List.of("agent", "workflow"));
        verify(userContentPushService).createPendingPushes();
        verify(pushQueueService).enqueuePendingPushes("feishu");
    }

    @Test
    void runDailyYoutubeSourceFlowShouldInvokeSourceSubscriptionCrawl() {
        SourceSubscriptionCrawlService sourceSubscriptionCrawlService = Mockito.mock(SourceSubscriptionCrawlService.class);
        when(sourceSubscriptionCrawlService.crawlAllSourceSubscriptions())
                .thenReturn(new SourceSubscriptionCrawlService.CrawlResult(2, 6, 4, 2));

        InfoDietScheduleServiceImpl service = new InfoDietScheduleServiceImpl();
        ReflectionTestUtils.setField(service, "sourceSubscriptionCrawlService", sourceSubscriptionCrawlService);

        SourceSubscriptionCrawlService.CrawlResult result = service.runDailyYoutubeSourceFlow();

        assertEquals(2, result.getSubscriptionCount());
        assertEquals(6, result.getCrawlCount());
        assertEquals(4, result.getSavedCount());
        assertEquals(2, result.getSkippedCount());
        verify(sourceSubscriptionCrawlService).crawlAllSourceSubscriptions();
    }

    @Test
    void runDailyYoutubeSourcePushFlowShouldOrchestrateWholePipeline() {
        SourceSubscriptionCrawlService sourceSubscriptionCrawlService = Mockito.mock(SourceSubscriptionCrawlService.class);
        UserContentPushService userContentPushService = Mockito.mock(UserContentPushService.class);
        PushQueueService pushQueueService = Mockito.mock(PushQueueService.class);

        when(sourceSubscriptionCrawlService.crawlAllSourceSubscriptions())
                .thenReturn(new SourceSubscriptionCrawlService.CrawlResult(2, 6, 4, 2));
        when(userContentPushService.createPendingPushes())
                .thenReturn(new UserContentPushService.CreatePushResult(5, 3, 2));
        when(pushQueueService.enqueuePendingPushes("feishu"))
                .thenReturn(new PushQueueService.EnqueuePushResult(3, 3, 0));

        InfoDietScheduleServiceImpl service = new InfoDietScheduleServiceImpl();
        ReflectionTestUtils.setField(service, "sourceSubscriptionCrawlService", sourceSubscriptionCrawlService);
        ReflectionTestUtils.setField(service, "userContentPushService", userContentPushService);
        ReflectionTestUtils.setField(service, "pushQueueService", pushQueueService);

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
        verify(userContentPushService).createPendingPushes();
        verify(pushQueueService).enqueuePendingPushes("feishu");
    }
}
