package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.entity.ContentItem;
import com.pingyu.infodiet.service.impl.ContentItemServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContentItemKeywordFilterTest {

    @Test
    void matchKeywordsShouldCheckTitleAndDescription() {
        ContentItemServiceImpl service = new ContentItemServiceImpl();

        ContentItem item = ContentItem.builder()
                .title("DeepSeek-TUI")
                .description("Coding agent for DeepSeek models that runs in your terminal")
                .build();

        boolean matched = service.matchKeywords(item, List.of("agent", "workflow"));

        assertTrue(matched);
    }

    @Test
    void filterByKeywordsShouldOnlyUpdateUnmatchedItems() {
        InMemoryKeywordFilterService service = new InMemoryKeywordFilterService();

        ContentItem matchedItem = ContentItem.builder()
                .id(1L)
                .title("DeepSeek-TUI")
                .description("Coding agent for DeepSeek models that runs in your terminal")
                .keywordMatched(0)
                .build();
        ContentItem unmatchedItem = ContentItem.builder()
                .id(2L)
                .title("financial-services")
                .description("Tools for finance teams")
                .keywordMatched(0)
                .build();
        ContentItem alreadyMatchedItem = ContentItem.builder()
                .id(3L)
                .title("old-item")
                .description("Already matched before")
                .keywordMatched(1)
                .build();

        service.items.add(matchedItem);
        service.items.add(unmatchedItem);
        service.items.add(alreadyMatchedItem);

        ContentItemService.KeywordFilterResult result = service.filterByKeywords(List.of("agent", "DeepSeek"));

        assertEquals(2, result.getTotalCount());
        assertEquals(1, result.getMatchedCount());
        assertEquals(1, result.getUnmatchedCount());
        assertEquals(1, matchedItem.getKeywordMatched());
        assertEquals(0, unmatchedItem.getKeywordMatched());
        assertEquals(1, alreadyMatchedItem.getKeywordMatched());
        assertEquals(1, service.updatedItems.size());
    }

    private static class InMemoryKeywordFilterService extends ContentItemServiceImpl {

        private final List<ContentItem> items = new ArrayList<>();
        private final List<ContentItem> updatedItems = new ArrayList<>();

        @Override
        protected List<ContentItem> listUnmatchedItems() {
            return items.stream().filter(item -> item.getKeywordMatched() == null || item.getKeywordMatched() == 0).toList();
        }

        @Override
        public boolean updateById(ContentItem entity) {
            updatedItems.add(entity);
            return true;
        }
    }
}
