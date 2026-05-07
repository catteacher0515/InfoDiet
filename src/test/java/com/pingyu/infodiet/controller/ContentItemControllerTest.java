package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.model.dto.content.ContentItemKeywordFilterRequest;
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
}
