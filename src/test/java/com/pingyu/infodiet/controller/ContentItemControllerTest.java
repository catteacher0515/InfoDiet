package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.model.dto.content.ContentItemKeywordFilterRequest;
import com.pingyu.infodiet.model.dto.content.UnifiedContentItemDTO;
import com.pingyu.infodiet.model.dto.content.UnifiedContentQueryRequest;
import com.pingyu.infodiet.service.ContentItemService;
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
}
