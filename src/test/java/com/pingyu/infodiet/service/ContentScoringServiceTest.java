package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.entity.ContentItem;
import com.pingyu.infodiet.service.impl.ContentScoringServiceImpl;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContentScoringServiceTest {

    @Test
    void evaluateShouldGiveHigherScoreToHighTrustHighHeatContent() {
        ContentScoringServiceImpl service = new ContentScoringServiceImpl();

        ContentItem contentItem = ContentItem.builder()
                .platform("github")
                .sourceTier("T1")
                .sourceCategory("official")
                .todayStarCount(1200)
                .starCount(18000)
                .crawlTime(LocalDateTime.now().minusHours(3))
                .preFilterStatus(1)
                .build();

        ContentScoringService.ScoreDetail scoreDetail = service.evaluate(contentItem);

        assertTrue(scoreDetail.score() >= 80);
        assertTrue(scoreDetail.reason().contains("tier:T1"));
        assertTrue(scoreDetail.reason().contains("category:official"));
    }

    @Test
    void runQualityScoringShouldOnlyScorePassedPreFilterItems() {
        InMemoryContentScoringService service = new InMemoryContentScoringService();
        service.items.add(ContentItem.builder()
                .id(1L)
                .platform("github")
                .sourceTier("T1")
                .sourceCategory("official")
                .todayStarCount(300)
                .starCount(2000)
                .crawlTime(LocalDateTime.now().minusHours(5))
                .preFilterStatus(1)
                .qualityScore(0)
                .build());
        service.items.add(ContentItem.builder()
                .id(2L)
                .platform("youtube")
                .sourceTier("T2")
                .sourceCategory("normal")
                .viewCount(500)
                .crawlTime(LocalDateTime.now().minusDays(2))
                .preFilterStatus(2)
                .qualityScore(0)
                .build());

        ContentItemService.QualityScoreResult result = service.runQualityScoring();

        assertEquals(1, result.getTotalCount());
        assertEquals(1, result.getScoredCount());
        assertEquals(0, result.getSkippedCount());
        assertTrue(service.items.get(0).getQualityScore() > 0);
        assertEquals(0, service.items.get(1).getQualityScore());
    }

    private static class InMemoryContentScoringService extends ContentScoringServiceImpl {

        private final List<ContentItem> items = new ArrayList<>();

        @Override
        protected List<ContentItem> listScoreableItems() {
            return items.stream()
                    .filter(item -> item.getPreFilterStatus() != null && item.getPreFilterStatus() == 1)
                    .toList();
        }

        @Override
        protected boolean updateById(ContentItem contentItem) {
            return true;
        }
    }
}
