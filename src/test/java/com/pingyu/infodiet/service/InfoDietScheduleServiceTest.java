package com.pingyu.infodiet.service;

import com.pingyu.infodiet.config.InfoDietProperties;
import com.pingyu.infodiet.model.dto.github.GithubTrendingItemDTO;
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
        FeishuPushService feishuPushService = Mockito.mock(FeishuPushService.class);

        GithubTrendingItemDTO first = new GithubTrendingItemDTO();
        GithubTrendingItemDTO second = new GithubTrendingItemDTO();
        List<GithubTrendingItemDTO> dtoList = List.of(first, second);

        when(githubTrendingService.crawlGitHubTrending()).thenReturn(dtoList);
        when(contentItemService.saveGithubTrendingItems(dtoList))
                .thenReturn(new ContentItemService.SaveResult(2, 1, 1));
        when(contentItemService.filterByKeywords(List.of("agent", "workflow")))
                .thenReturn(new ContentItemService.KeywordFilterResult(2, 1, 1));
        when(feishuPushService.pushContentItemsToFeishu())
                .thenReturn(new FeishuPushService.PushResult(1, 1, 0));

        InfoDietProperties infoDietProperties = new InfoDietProperties();
        infoDietProperties.setKeywords(List.of("agent", "workflow"));

        InfoDietScheduleServiceImpl service = new InfoDietScheduleServiceImpl();
        ReflectionTestUtils.setField(service, "githubTrendingService", githubTrendingService);
        ReflectionTestUtils.setField(service, "contentItemService", contentItemService);
        ReflectionTestUtils.setField(service, "feishuPushService", feishuPushService);
        ReflectionTestUtils.setField(service, "infoDietProperties", infoDietProperties);

        InfoDietScheduleService.ScheduleResult result = service.runDailyGithubFlow();

        assertEquals(2, result.getCrawlCount());
        assertEquals(1, result.getSavedCount());
        assertEquals(1, result.getSkippedCount());
        assertEquals(1, result.getMatchedCount());
        assertEquals(1, result.getUnmatchedCount());
        assertEquals(1, result.getPushSuccessCount());
        assertEquals(0, result.getPushFailedCount());

        verify(githubTrendingService).crawlGitHubTrending();
        verify(contentItemService).saveGithubTrendingItems(dtoList);
        verify(contentItemService).filterByKeywords(List.of("agent", "workflow"));
        verify(feishuPushService).pushContentItemsToFeishu();
    }
}
