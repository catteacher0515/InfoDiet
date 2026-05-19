package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.model.dto.content.ContentEventClusterDTO;
import com.pingyu.infodiet.model.dto.content.ContentItemKeywordFilterRequest;
import com.pingyu.infodiet.model.dto.content.DailyDigestDTO;
import com.pingyu.infodiet.model.dto.content.UnifiedContentItemDTO;
import com.pingyu.infodiet.model.dto.content.UnifiedContentQueryRequest;
import com.pingyu.infodiet.service.ContentClusterService;
import com.pingyu.infodiet.service.ContentItemService;
import com.pingyu.infodiet.service.ContentPreFilterService;
import com.pingyu.infodiet.service.ContentScoringService;
import com.pingyu.infodiet.service.ContentSelectionService;
import com.pingyu.infodiet.service.DailyDigestService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class ContentItemControllerTest {

    @Test
    void filterByKeywordsShouldReturnFilterSummary() {
        ContentItemService contentItemService = Mockito.mock(ContentItemService.class);
        ContentItemService.KeywordFilterResult filterResult = new ContentItemService.KeywordFilterResult(10, 4, 6);
        when(contentItemService.filterByKeywords(List.of("agent", "workflow"))).thenReturn(filterResult);

        ContentItemController controller = new ContentItemController();
        ReflectionTestUtils.setField(controller, "contentItemService", contentItemService);

        ContentItemKeywordFilterRequest request = new ContentItemKeywordFilterRequest();
        request.setKeywords(List.of("agent", "workflow"));

        BaseResponse<ContentItemService.KeywordFilterResult> response = controller.filterByKeywords(request);

        assertEquals(0, response.getCode());
        assertEquals(10, response.getData().getTotalCount());
        assertEquals(4, response.getData().getMatchedCount());
        assertEquals(6, response.getData().getUnmatchedCount());
    }

    @Test
    void runPreFilterShouldReturnPreFilterSummary() {
        ContentPreFilterService contentPreFilterService = Mockito.mock(ContentPreFilterService.class);
        ContentItemService.PreFilterResult preFilterResult = new ContentItemService.PreFilterResult(10, 6, 4, 0);
        when(contentPreFilterService.runSystemPreFilter()).thenReturn(preFilterResult);

        ContentItemController controller = new ContentItemController();
        ReflectionTestUtils.setField(controller, "contentPreFilterService", contentPreFilterService);

        BaseResponse<ContentItemService.PreFilterResult> response = controller.runPreFilter();

        assertEquals(0, response.getCode());
        assertEquals(10, response.getData().getTotalCount());
        assertEquals(6, response.getData().getPassedCount());
        assertEquals(4, response.getData().getFilteredCount());
        assertEquals(0, response.getData().getSkippedCount());
    }

    @Test
    void runQualityScoringShouldReturnScoreSummary() {
        ContentScoringService contentScoringService = Mockito.mock(ContentScoringService.class);
        ContentItemService.QualityScoreResult scoreResult = new ContentItemService.QualityScoreResult(10, 8, 2);
        when(contentScoringService.runQualityScoring()).thenReturn(scoreResult);

        ContentItemController controller = new ContentItemController();
        ReflectionTestUtils.setField(controller, "contentScoringService", contentScoringService);

        BaseResponse<ContentItemService.QualityScoreResult> response = controller.runQualityScoring();

        assertEquals(0, response.getCode());
        assertEquals(10, response.getData().getTotalCount());
        assertEquals(8, response.getData().getScoredCount());
        assertEquals(2, response.getData().getSkippedCount());
    }

    @Test
    void listUnifiedContentItemsShouldReturnUnifiedList() {
        ContentItemService contentItemService = Mockito.mock(ContentItemService.class);
        when(contentItemService.listUnifiedContentItems()).thenReturn(List.of(
                UnifiedContentItemDTO.builder().id(1L).platform("github").title("openai-java").build()
        ));

        ContentItemController controller = new ContentItemController();
        ReflectionTestUtils.setField(controller, "contentItemService", contentItemService);

        BaseResponse<List<UnifiedContentItemDTO>> response = controller.listUnifiedContentItems();

        assertEquals(0, response.getCode());
        assertEquals(1, response.getData().size());
        assertEquals("github", response.getData().getFirst().getPlatform());
    }

    @Test
    void listUnifiedContentItemsWithQueryShouldReturnFilteredList() {
        ContentItemService contentItemService = Mockito.mock(ContentItemService.class);
        UnifiedContentQueryRequest request = new UnifiedContentQueryRequest();
        request.setPlatform("youtube");
        request.setSortBy("metric");
        when(contentItemService.listUnifiedContentItems(request)).thenReturn(List.of(
                UnifiedContentItemDTO.builder().id(2L).platform("youtube").title("Build InfoDiet").build()
        ));

        ContentItemController controller = new ContentItemController();
        ReflectionTestUtils.setField(controller, "contentItemService", contentItemService);

        BaseResponse<List<UnifiedContentItemDTO>> response = controller.listUnifiedContentItems(request);

        assertEquals(0, response.getCode());
        assertEquals(1, response.getData().size());
        assertEquals("youtube", response.getData().getFirst().getPlatform());
    }

    @Test
    void listFeaturedContentItemsShouldReturnFeaturedList() {
        ContentSelectionService contentSelectionService = Mockito.mock(ContentSelectionService.class);
        when(contentSelectionService.listFeaturedContentItems()).thenReturn(List.of(
                UnifiedContentItemDTO.builder().id(10L).platform("github").title("featured").qualityScore(88).build()
        ));

        ContentItemController controller = new ContentItemController();
        ReflectionTestUtils.setField(controller, "contentSelectionService", contentSelectionService);

        BaseResponse<List<UnifiedContentItemDTO>> response = controller.listFeaturedContentItems();

        assertEquals(0, response.getCode());
        assertEquals(1, response.getData().size());
        assertEquals(88, response.getData().getFirst().getQualityScore());
    }

    @Test
    void listFeaturedClustersShouldReturnClusteredFeaturedList() {
        ContentClusterService contentClusterService = Mockito.mock(ContentClusterService.class);
        when(contentClusterService.listFeaturedClusters()).thenReturn(List.of(
                ContentEventClusterDTO.builder()
                        .clusterKey("cluster-1")
                        .clusterTitle("OpenAI releases GPT-5.5")
                        .clusterScore(90)
                        .clusterSize(2)
                        .build()
        ));

        ContentItemController controller = new ContentItemController();
        ReflectionTestUtils.setField(controller, "contentClusterService", contentClusterService);

        BaseResponse<List<ContentEventClusterDTO>> response = controller.listFeaturedClusters();

        assertEquals(0, response.getCode());
        assertEquals(1, response.getData().size());
        assertEquals(2, response.getData().getFirst().getClusterSize());
    }

    @Test
    void generateTodayDigestShouldReturnDigest() {
        DailyDigestService dailyDigestService = Mockito.mock(DailyDigestService.class);
        when(dailyDigestService.generateTodayDigest()).thenReturn(DailyDigestDTO.builder()
                .digestTitle("AI 日报 · 2026-05-16")
                .totalClusterCount(2)
                .totalItemCount(3)
                .summary("今日共筛出 2 条精选事件。")
                .build());

        ContentItemController controller = new ContentItemController();
        ReflectionTestUtils.setField(controller, "dailyDigestService", dailyDigestService);

        BaseResponse<DailyDigestDTO> response = controller.generateTodayDigest();

        assertEquals(0, response.getCode());
        assertEquals(2, response.getData().getTotalClusterCount());
        assertEquals(3, response.getData().getTotalItemCount());
    }

    @Test
    void listRecentDigestsShouldReturnDigestList() {
        DailyDigestService dailyDigestService = Mockito.mock(DailyDigestService.class);
        when(dailyDigestService.listRecentDigests(7)).thenReturn(List.of(
                DailyDigestDTO.builder()
                        .digestTitle("AI 日报 · 2026-05-16")
                        .totalClusterCount(2)
                        .build()
        ));

        ContentItemController controller = new ContentItemController();
        ReflectionTestUtils.setField(controller, "dailyDigestService", dailyDigestService);

        BaseResponse<List<DailyDigestDTO>> response = controller.listRecentDigests(7);

        assertEquals(0, response.getCode());
        assertEquals(1, response.getData().size());
        assertEquals(2, response.getData().getFirst().getTotalClusterCount());
    }

    @Test
    void getDigestByDateShouldReturnDigestDetail() {
        DailyDigestService dailyDigestService = Mockito.mock(DailyDigestService.class);
        when(dailyDigestService.getDigestByDate(java.time.LocalDate.of(2026, 5, 16))).thenReturn(
                DailyDigestDTO.builder()
                        .digestTitle("AI 日报 · 2026-05-16")
                        .totalItemCount(3)
                        .build()
        );

        ContentItemController controller = new ContentItemController();
        ReflectionTestUtils.setField(controller, "dailyDigestService", dailyDigestService);

        BaseResponse<DailyDigestDTO> response = controller.getDigestByDate("2026-05-16");

        assertEquals(0, response.getCode());
        assertEquals(3, response.getData().getTotalItemCount());
    }
}
