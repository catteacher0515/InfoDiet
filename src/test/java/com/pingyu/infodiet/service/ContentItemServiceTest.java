package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.dto.github.GithubTrendingItemDTO;
import com.pingyu.infodiet.model.entity.ContentItem;
import com.pingyu.infodiet.service.impl.ContentItemServiceImpl;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ContentItemServiceTest {

    @Test
    void convertGithubTrendingItemShouldMapFieldsAndFillSystemDefaults() {
        ContentItemServiceImpl service = new ContentItemServiceImpl();

        GithubTrendingItemDTO dto = new GithubTrendingItemDTO();
        dto.setRepoFullName("openai/openai-java");
        dto.setRepoName("openai-java");
        dto.setRepoUrl("https://github.com/openai/openai-java");
        dto.setDescription("Java library for the OpenAI API");
        dto.setAuthorName("openai");
        dto.setAuthorUrl("https://github.com/openai");
        dto.setLanguage("Java");
        dto.setStarCount(1234);
        dto.setTodayStarCount(345);

        ContentItem item = service.convertGithubTrendingItem(dto);

        assertEquals("github", item.getPlatform());
        assertEquals("openai/openai-java", item.getSourceId());
        assertEquals("openai-java", item.getTitle());
        assertEquals("Java library for the OpenAI API", item.getDescription());
        assertEquals("https://github.com/openai/openai-java", item.getContentUrl());
        assertEquals("openai", item.getAuthorName());
        assertEquals("https://github.com/openai", item.getAuthorUrl());
        assertEquals("Java", item.getLanguage());
        assertEquals(1234, item.getStarCount());
        assertEquals(345, item.getTodayStarCount());
        assertEquals(0, item.getKeywordMatched());
        assertEquals(0, item.getPushStatus());
        assertEquals(Date.valueOf(LocalDate.now()), item.getCrawlDate());
        assertNotNull(item.getCrawlTime());
    }

    @Test
    void saveGithubTrendingItemsShouldSkipItemsThatAlreadyExistToday() {
        LocalDateTime now = LocalDateTime.now();

        InMemoryContentItemService service = new InMemoryContentItemService(now);
        GithubTrendingItemDTO dto = new GithubTrendingItemDTO();
        dto.setRepoFullName("openai/openai-java");
        dto.setRepoName("openai-java");
        dto.setRepoUrl("https://github.com/openai/openai-java");

        ContentItem savedItem = ContentItem.builder()
                .platform("github")
                .sourceId("openai/openai-java")
                .crawlDate(Date.valueOf(now.toLocalDate()))
                .build();
        service.items.add(savedItem);

        ContentItemService.SaveResult result = service.saveGithubTrendingItems(List.of(dto));

        assertEquals(1, result.getTotalCount());
        assertEquals(0, result.getSavedCount());
        assertEquals(1, result.getSkippedCount());
    }

    private static class InMemoryContentItemService extends ContentItemServiceImpl {

        private final List<ContentItem> items = new java.util.ArrayList<>();
        private final LocalDateTime fixedNow;

        private InMemoryContentItemService(LocalDateTime fixedNow) {
            this.fixedNow = fixedNow;
        }

        @Override
        protected LocalDateTime now() {
            return fixedNow;
        }

        @Override
        protected boolean existsByPlatformAndSourceIdAndCrawlDate(String platform, String sourceId, Date crawlDate) {
            return items.stream().anyMatch(item ->
                    platform.equals(item.getPlatform())
                            && sourceId.equals(item.getSourceId())
                            && crawlDate.equals(item.getCrawlDate()));
        }

        @Override
        public boolean save(ContentItem entity) {
            items.add(entity);
            return true;
        }
    }
}
