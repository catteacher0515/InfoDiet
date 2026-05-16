package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.dto.youtube.YoutubeVideoItemDTO;
import com.pingyu.infodiet.model.entity.ContentItem;
import com.pingyu.infodiet.model.entity.SourceProfile;
import com.pingyu.infodiet.service.impl.ContentItemServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ContentItemYoutubeServiceTest {

    @Test
    void convertYoutubeVideoItemShouldMapFieldsAndFillSystemDefaults() {
        ContentItemServiceImpl service = new ContentItemServiceImpl();
        SourceProfileService sourceProfileService = Mockito.mock(SourceProfileService.class);
        ReflectionTestUtils.setField(service, "sourceProfileService", sourceProfileService);

        YoutubeVideoItemDTO dto = new YoutubeVideoItemDTO();
        dto.setVideoId("video123");
        dto.setChannelId("UC123456");
        dto.setTitle("Build InfoDiet with Java");
        dto.setVideoUrl("https://www.youtube.com/watch?v=video123");
        dto.setDescription("Build your own information diet system");
        dto.setAuthorName("Pingyu Channel");
        dto.setAuthorUrl("https://www.youtube.com/channel/UC123456");
        dto.setPublishTime(LocalDateTime.of(2026, 5, 10, 7, 30));
        SourceProfile sourceProfile = SourceProfile.builder()
                .id(200L)
                .platform("youtube")
                .profileType("channel")
                .sourceKey("UC123456")
                .sourceCategory("kol")
                .sourceTier("T2")
                .build();
        Mockito.when(sourceProfileService.resolveOrCreateByContent(
                "youtube", "channel", "UC123456", "Pingyu Channel", "https://www.youtube.com/channel/UC123456"
        )).thenReturn(sourceProfile);

        ContentItem item = service.convertYoutubeVideoItem(dto);

        assertEquals("youtube", item.getPlatform());
        assertEquals("video123", item.getSourceId());
        assertEquals("Build InfoDiet with Java", item.getTitle());
        assertEquals("video", item.getContentType());
        assertEquals("https://www.youtube.com/watch?v=video123", item.getContentUrl());
        assertEquals("Build your own information diet system", item.getDescription());
        assertEquals("Pingyu Channel", item.getAuthorName());
        assertEquals("https://www.youtube.com/channel/UC123456", item.getAuthorUrl());
        assertEquals(200L, item.getSourceProfileId());
        assertEquals("kol", item.getSourceCategory());
        assertEquals("T2", item.getSourceTier());
        assertEquals(0, item.getViewCount());
        assertEquals(LocalDateTime.of(2026, 5, 10, 7, 30), item.getPublishTime());
        assertEquals(0, item.getKeywordMatched());
        assertEquals(0, item.getPushStatus());
        assertEquals(Date.valueOf(LocalDate.now()), item.getCrawlDate());
        assertNotNull(item.getCrawlTime());
    }

    @Test
    void saveYoutubeVideoItemsShouldSkipItemsThatAlreadyExistToday() {
        LocalDateTime now = LocalDateTime.now();

        InMemoryYoutubeContentItemService service = new InMemoryYoutubeContentItemService(now);
        YoutubeVideoItemDTO dto = new YoutubeVideoItemDTO();
        dto.setVideoId("video123");
        dto.setTitle("Build InfoDiet with Java");
        dto.setVideoUrl("https://www.youtube.com/watch?v=video123");

        ContentItem savedItem = ContentItem.builder()
                .platform("youtube")
                .sourceId("video123")
                .crawlDate(Date.valueOf(now.toLocalDate()))
                .build();
        service.items.add(savedItem);

        ContentItemService.SaveResult result = service.saveYoutubeVideoItems(List.of(dto));

        assertEquals(1, result.getTotalCount());
        assertEquals(0, result.getSavedCount());
        assertEquals(1, result.getSkippedCount());
    }

    private static class InMemoryYoutubeContentItemService extends ContentItemServiceImpl {

        private final List<ContentItem> items = new java.util.ArrayList<>();
        private final LocalDateTime fixedNow;

        private InMemoryYoutubeContentItemService(LocalDateTime fixedNow) {
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
