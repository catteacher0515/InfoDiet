package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.model.dto.content.ContentItemKeywordFilterRequest;
import com.pingyu.infodiet.model.dto.content.UnifiedContentItemDTO;
import com.pingyu.infodiet.model.dto.content.UnifiedContentQueryRequest;
import com.pingyu.infodiet.service.ContentItemService;
import com.pingyu.infodiet.service.ContentPreFilterService;
import com.pingyu.infodiet.service.ContentScoringService;
import com.pingyu.infodiet.service.ContentSelectionService;
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
}
