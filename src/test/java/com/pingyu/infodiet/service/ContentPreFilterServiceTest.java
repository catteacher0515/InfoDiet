package com.pingyu.infodiet.service;

import com.pingyu.infodiet.config.InfoDietProperties;
import com.pingyu.infodiet.model.entity.ContentItem;
import com.pingyu.infodiet.service.impl.ContentPreFilterServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContentPreFilterServiceTest {

    @Test
    void runPreFilterShouldPassAiRelevantContentAndFilterNoise() {
        InMemoryContentPreFilterService service = new InMemoryContentPreFilterService();
        service.items.add(ContentItem.builder()
                .id(1L)
                .title("OpenAI agent workflow")
                .description("Build an AI coding workflow")
                .preFilterStatus(0)
                .build());
        service.items.add(ContentItem.builder()
                .id(2L)
                .title("Weekend vlog")
                .description("music and travel record")
                .preFilterStatus(0)
                .build());

        ContentItemService.PreFilterResult result = service.runPreFilter(
                List.of("ai", "agent", "workflow"),
                List.of("vlog", "music")
        );

        assertEquals(2, result.getTotalCount());
        assertEquals(1, result.getPassedCount());
        assertEquals(1, result.getFilteredCount());
        assertEquals(0, result.getSkippedCount());
        assertEquals(1, service.items.get(0).getPreFilterStatus());
        assertEquals("命中包含关键词: ai", service.items.get(0).getPreFilterReason());
        assertEquals(2, service.items.get(1).getPreFilterStatus());
        assertEquals("命中排除关键词: vlog", service.items.get(1).getPreFilterReason());
    }

    @Test
    void runSystemPreFilterShouldUseInfoDietProperties() {
        InMemoryContentPreFilterService service = new InMemoryContentPreFilterService();
        InfoDietProperties infoDietProperties = new InfoDietProperties();
        infoDietProperties.setKeywords(List.of("agent"));
        infoDietProperties.setPreFilterExcludeKeywords(List.of("finance"));
        ReflectionTestUtils.setField(service, "infoDietProperties", infoDietProperties);

        service.items.add(ContentItem.builder()
                .id(3L)
                .title("finance bot")
                .description("agent for finance")
                .preFilterStatus(0)
                .build());

        ContentItemService.PreFilterResult result = service.runSystemPreFilter();

        assertEquals(1, result.getFilteredCount());
        assertEquals(2, service.items.getFirst().getPreFilterStatus());
    }

    private static class InMemoryContentPreFilterService extends ContentPreFilterServiceImpl {

        private final List<ContentItem> items = new ArrayList<>();

        @Override
        protected List<ContentItem> listPendingPreFilterItems() {
            return items.stream()
                    .filter(item -> item.getPreFilterStatus() == null || item.getPreFilterStatus() == 0)
                    .toList();
        }

        @Override
        public boolean updateById(ContentItem entity) {
            return true;
        }
    }
}
