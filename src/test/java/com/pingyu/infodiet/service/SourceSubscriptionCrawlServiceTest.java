package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.dto.github.GithubTrendingItemDTO;
import com.pingyu.infodiet.model.dto.youtube.YoutubeVideoItemDTO;
import com.pingyu.infodiet.model.entity.UserSourceSubscription;
import com.pingyu.infodiet.service.impl.SourceSubscriptionCrawlServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SourceSubscriptionCrawlServiceTest {

    @Test
    void crawlYoutubeSourceSubscriptionsShouldAggregateAndSaveResults() {
        UserSourceSubscriptionService userSourceSubscriptionService = Mockito.mock(UserSourceSubscriptionService.class);
        YoutubeCrawlService youtubeCrawlService = Mockito.mock(YoutubeCrawlService.class);
        GithubTrendingService githubTrendingService = Mockito.mock(GithubTrendingService.class);
        ContentItemService contentItemService = Mockito.mock(ContentItemService.class);

        UserSourceSubscription first = UserSourceSubscription.builder()
                .id(1L).platform("youtube").sourceType("channel").sourceValue("UC111").status(1).build();
        UserSourceSubscription second = UserSourceSubscription.builder()
                .id(2L).platform("youtube").sourceType("channel").sourceValue("UC222").status(1).build();

        YoutubeVideoItemDTO firstVideo = new YoutubeVideoItemDTO();
        firstVideo.setVideoId("video-1");
        YoutubeVideoItemDTO secondVideo = new YoutubeVideoItemDTO();
        secondVideo.setVideoId("video-2");
        YoutubeVideoItemDTO thirdVideo = new YoutubeVideoItemDTO();
        thirdVideo.setVideoId("video-3");

        when(userSourceSubscriptionService.listEnabledSourceSubscriptions()).thenReturn(List.of(first, second));
        when(youtubeCrawlService.crawlYoutubeVideos("UC111")).thenReturn(List.of(firstVideo, secondVideo));
        when(youtubeCrawlService.crawlYoutubeVideos("UC222")).thenReturn(List.of(thirdVideo));
        when(contentItemService.saveYoutubeVideoItems(Mockito.anyList()))
                .thenReturn(new ContentItemService.SaveResult(3, 2, 1));

        SourceSubscriptionCrawlServiceImpl service = new SourceSubscriptionCrawlServiceImpl();
        ReflectionTestUtils.setField(service, "userSourceSubscriptionService", userSourceSubscriptionService);
        ReflectionTestUtils.setField(service, "youtubeCrawlService", youtubeCrawlService);
        ReflectionTestUtils.setField(service, "githubTrendingService", githubTrendingService);
        ReflectionTestUtils.setField(service, "contentItemService", contentItemService);

        SourceSubscriptionCrawlService.CrawlResult result = service.crawlYoutubeSourceSubscriptions();

        assertEquals(2, result.getSubscriptionCount());
        assertEquals(3, result.getCrawlCount());
        assertEquals(2, result.getSavedCount());
        assertEquals(1, result.getSkippedCount());

        verify(youtubeCrawlService).crawlYoutubeVideos("UC111");
        verify(youtubeCrawlService).crawlYoutubeVideos("UC222");
    }

    @Test
    void crawlGithubSourceSubscriptionsShouldAggregateAndSaveResults() {
        UserSourceSubscriptionService userSourceSubscriptionService = Mockito.mock(UserSourceSubscriptionService.class);
        YoutubeCrawlService youtubeCrawlService = Mockito.mock(YoutubeCrawlService.class);
        GithubTrendingService githubTrendingService = Mockito.mock(GithubTrendingService.class);
        ContentItemService contentItemService = Mockito.mock(ContentItemService.class);

        UserSourceSubscription repoSubscription = UserSourceSubscription.builder()
                .id(1L).platform("github").sourceType("repo").sourceValue("openai/openai-java").status(1).build();
        UserSourceSubscription authorSubscription = UserSourceSubscription.builder()
                .id(2L).platform("github").sourceType("author").sourceValue("openai").status(1).build();

        GithubTrendingItemDTO repoItem = new GithubTrendingItemDTO();
        repoItem.setRepoFullName("openai/openai-java");
        GithubTrendingItemDTO authorItem = new GithubTrendingItemDTO();
        authorItem.setRepoFullName("openai/codex");

        when(userSourceSubscriptionService.listEnabledSourceSubscriptions())
                .thenReturn(List.of(repoSubscription, authorSubscription));
        when(githubTrendingService.crawlGitHubRepo("openai/openai-java")).thenReturn(repoItem);
        when(githubTrendingService.crawlGitHubAuthorRepositories("openai")).thenReturn(List.of(authorItem));
        when(contentItemService.saveGithubTrendingItems(Mockito.anyList()))
                .thenReturn(new ContentItemService.SaveResult(2, 2, 0));

        SourceSubscriptionCrawlServiceImpl service = new SourceSubscriptionCrawlServiceImpl();
        ReflectionTestUtils.setField(service, "userSourceSubscriptionService", userSourceSubscriptionService);
        ReflectionTestUtils.setField(service, "youtubeCrawlService", youtubeCrawlService);
        ReflectionTestUtils.setField(service, "githubTrendingService", githubTrendingService);
        ReflectionTestUtils.setField(service, "contentItemService", contentItemService);

        SourceSubscriptionCrawlService.CrawlResult result = service.crawlGithubSourceSubscriptions();

        assertEquals(2, result.getSubscriptionCount());
        assertEquals(2, result.getCrawlCount());
        assertEquals(2, result.getSavedCount());
        assertEquals(0, result.getSkippedCount());

        verify(githubTrendingService).crawlGitHubRepo("openai/openai-java");
        verify(githubTrendingService).crawlGitHubAuthorRepositories("openai");
    }

    @Test
    void crawlGithubSourceSubscriptionsShouldDeduplicateSameRepositoryAcrossSubscriptions() {
        UserSourceSubscriptionService userSourceSubscriptionService = Mockito.mock(UserSourceSubscriptionService.class);
        YoutubeCrawlService youtubeCrawlService = Mockito.mock(YoutubeCrawlService.class);
        GithubTrendingService githubTrendingService = Mockito.mock(GithubTrendingService.class);
        ContentItemService contentItemService = Mockito.mock(ContentItemService.class);

        UserSourceSubscription repoSubscription = UserSourceSubscription.builder()
                .id(1L).platform("github").sourceType("repo").sourceValue("openai/openai-java").status(1).build();
        UserSourceSubscription authorSubscription = UserSourceSubscription.builder()
                .id(2L).platform("github").sourceType("author").sourceValue("openai").status(1).build();

        GithubTrendingItemDTO repoItem = new GithubTrendingItemDTO();
        repoItem.setRepoFullName("openai/openai-java");
        GithubTrendingItemDTO duplicateRepoItem = new GithubTrendingItemDTO();
        duplicateRepoItem.setRepoFullName("openai/openai-java");
        GithubTrendingItemDTO authorOnlyItem = new GithubTrendingItemDTO();
        authorOnlyItem.setRepoFullName("openai/codex");

        when(userSourceSubscriptionService.listEnabledSourceSubscriptions())
                .thenReturn(List.of(repoSubscription, authorSubscription));
        when(githubTrendingService.crawlGitHubRepo("openai/openai-java")).thenReturn(repoItem);
        when(githubTrendingService.crawlGitHubAuthorRepositories("openai"))
                .thenReturn(List.of(duplicateRepoItem, authorOnlyItem));
        when(contentItemService.saveGithubTrendingItems(anyList()))
                .thenReturn(new ContentItemService.SaveResult(2, 2, 0));

        SourceSubscriptionCrawlServiceImpl service = new SourceSubscriptionCrawlServiceImpl();
        ReflectionTestUtils.setField(service, "userSourceSubscriptionService", userSourceSubscriptionService);
        ReflectionTestUtils.setField(service, "youtubeCrawlService", youtubeCrawlService);
        ReflectionTestUtils.setField(service, "githubTrendingService", githubTrendingService);
        ReflectionTestUtils.setField(service, "contentItemService", contentItemService);

        SourceSubscriptionCrawlService.CrawlResult result = service.crawlGithubSourceSubscriptions();

        assertEquals(2, result.getSubscriptionCount());
        assertEquals(2, result.getCrawlCount());
        assertEquals(2, result.getSavedCount());
        assertEquals(0, result.getSkippedCount());
    }

    @Test
    void crawlAllSourceSubscriptionsShouldMergeYoutubeAndGithubResults() {
        SourceSubscriptionCrawlServiceImpl service = Mockito.spy(new SourceSubscriptionCrawlServiceImpl());
        doReturn(new SourceSubscriptionCrawlService.CrawlResult(1, 3, 2, 1))
                .when(service).crawlYoutubeSourceSubscriptions();
        doReturn(new SourceSubscriptionCrawlService.CrawlResult(2, 4, 3, 1))
                .when(service).crawlGithubSourceSubscriptions();

        SourceSubscriptionCrawlService.CrawlResult result = service.crawlAllSourceSubscriptions();

        assertEquals(3, result.getSubscriptionCount());
        assertEquals(7, result.getCrawlCount());
        assertEquals(5, result.getSavedCount());
        assertEquals(2, result.getSkippedCount());
    }
}
