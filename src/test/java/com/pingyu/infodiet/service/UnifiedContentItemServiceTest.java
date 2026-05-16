package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.dto.content.UnifiedContentQueryRequest;
import com.pingyu.infodiet.model.dto.content.UnifiedContentItemDTO;
import com.pingyu.infodiet.model.entity.ContentItem;
import com.pingyu.infodiet.service.impl.ContentItemServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UnifiedContentItemServiceTest {

    @Test
    void convertToUnifiedContentItemShouldMapGithubMetrics() {
        ContentItemServiceImpl service = new ContentItemServiceImpl();
        ContentItem item = ContentItem.builder()
                .id(1L)
                .platform("github")
                .sourceId("openai/openai-java")
                .title("openai-java")
                .authorName("openai")
                .sourceProfileId(1L)
                .sourceCategory("official")
                .sourceTier("T1")
                .starCount(1500)
                .todayStarCount(200)
                .crawlTime(LocalDateTime.of(2026, 5, 11, 10, 0))
                .build();

        UnifiedContentItemDTO dto = service.convertToUnifiedContentItem(item);

        assertEquals("repository", dto.getContentType());
        assertEquals(1500, dto.getPrimaryMetricValue());
        assertEquals("stars", dto.getPrimaryMetricLabel());
        assertEquals(200, dto.getSecondaryMetricValue());
        assertEquals("todayStars", dto.getSecondaryMetricLabel());
        assertEquals("openai-java#openai", dto.getDedupKey());
        assertEquals(1L, dto.getSourceProfileId());
        assertEquals("official", dto.getSourceCategory());
        assertEquals("T1", dto.getSourceTier());
    }

    @Test
    void convertToUnifiedContentItemShouldMapYoutubeMetrics() {
        ContentItemServiceImpl service = new ContentItemServiceImpl();
        ContentItem item = ContentItem.builder()
                .id(2L)
                .platform("youtube")
                .sourceId("video-1")
                .title("Build InfoDiet with Java")
                .contentType("video")
                .authorName("Pingyu Channel")
                .viewCount(3000)
                .publishTime(LocalDateTime.of(2026, 5, 10, 8, 0))
                .crawlTime(LocalDateTime.of(2026, 5, 11, 10, 0))
                .build();

        UnifiedContentItemDTO dto = service.convertToUnifiedContentItem(item);

        assertEquals("video", dto.getContentType());
        assertEquals(3000, dto.getPrimaryMetricValue());
        assertEquals("views", dto.getPrimaryMetricLabel());
        assertEquals(0, dto.getSecondaryMetricValue());
        assertEquals("", dto.getSecondaryMetricLabel());
        assertEquals(LocalDateTime.of(2026, 5, 10, 8, 0), dto.getSortTime());
    }

    @Test
    void listUnifiedContentItemsShouldDeduplicateAcrossPlatformsAndSortByTime() {
        InMemoryUnifiedContentItemService service = new InMemoryUnifiedContentItemService();
        service.items.add(ContentItem.builder()
                .id(10L)
                .platform("github")
                .sourceId("openai/openai-java")
                .title("openai-java")
                .authorName("openai")
                .starCount(1500)
                .crawlTime(LocalDateTime.of(2026, 5, 11, 10, 0))
                .build());
        service.items.add(ContentItem.builder()
                .id(11L)
                .platform("youtube")
                .sourceId("video-1")
                .title("openai-java")
                .authorName("openai")
                .viewCount(3000)
                .publishTime(LocalDateTime.of(2026, 5, 10, 8, 0))
                .crawlTime(LocalDateTime.of(2026, 5, 11, 9, 0))
                .contentType("video")
                .build());
        service.items.add(ContentItem.builder()
                .id(12L)
                .platform("youtube")
                .sourceId("video-2")
                .title("Build InfoDiet with Java")
                .authorName("Pingyu Channel")
                .viewCount(2000)
                .publishTime(LocalDateTime.of(2026, 5, 11, 11, 0))
                .crawlTime(LocalDateTime.of(2026, 5, 11, 11, 30))
                .contentType("video")
                .build());

        List<UnifiedContentItemDTO> result = service.listUnifiedContentItems();

        assertEquals(2, result.size());
        assertEquals(12L, result.get(0).getId());
        assertEquals(11L, result.get(1).getId());
    }

    @Test
    void listUnifiedContentItemsShouldSupportPlatformFilterAndMetricSort() {
        InMemoryUnifiedContentItemService service = new InMemoryUnifiedContentItemService();
        service.items.add(ContentItem.builder()
                .id(21L)
                .platform("github")
                .sourceId("openai/openai-java")
                .title("openai-java")
                .authorName("openai")
                .starCount(1500)
                .crawlTime(LocalDateTime.of(2026, 5, 11, 10, 0))
                .build());
        service.items.add(ContentItem.builder()
                .id(22L)
                .platform("github")
                .sourceId("spring-projects/spring-ai")
                .title("spring-ai")
                .authorName("spring-projects")
                .starCount(2000)
                .crawlTime(LocalDateTime.of(2026, 5, 10, 10, 0))
                .build());
        service.items.add(ContentItem.builder()
                .id(23L)
                .platform("youtube")
                .sourceId("video-3")
                .title("Build InfoDiet with Java")
                .authorName("Pingyu Channel")
                .contentType("video")
                .viewCount(5000)
                .publishTime(LocalDateTime.of(2026, 5, 11, 12, 0))
                .crawlTime(LocalDateTime.of(2026, 5, 11, 12, 30))
                .build());

        UnifiedContentQueryRequest request = new UnifiedContentQueryRequest();
        request.setPlatform("github");
        request.setContentType("repository");
        request.setSortBy("metric");
        request.setLimit(1);

        List<UnifiedContentItemDTO> result = service.listUnifiedContentItems(request);

        assertEquals(1, result.size());
        assertEquals(22L, result.getFirst().getId());
        assertEquals("github", result.getFirst().getPlatform());
        assertEquals("repository", result.getFirst().getContentType());
    }

    @Test
    void unifiedContentMethodsShouldDeclareCacheAnnotations() throws NoSuchMethodException {
        Method listMethod = ContentItemServiceImpl.class.getDeclaredMethod(
                "listUnifiedContentItems",
                UnifiedContentQueryRequest.class
        );
        Method saveMethod = ContentItemServiceImpl.class.getDeclaredMethod("saveGithubTrendingItems", List.class);

        Cacheable cacheable = listMethod.getAnnotation(Cacheable.class);
        CacheEvict cacheEvict = saveMethod.getAnnotation(CacheEvict.class);

        assertEquals("unifiedContentItems", cacheable.cacheNames()[0]);
        assertEquals(true, cacheEvict.allEntries());
    }

    private static class InMemoryUnifiedContentItemService extends ContentItemServiceImpl {

        private final List<ContentItem> items = new ArrayList<>();

        @Override
        public List<ContentItem> list(com.mybatisflex.core.query.QueryWrapper queryWrapper) {
            return items;
        }
    }
}
