package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.entity.UserKeywordSubscription;
import com.pingyu.infodiet.service.impl.UserKeywordSubscriptionServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserKeywordSubscriptionServiceTest {

    @Test
    void addKeywordShouldCreateEnabledSubscription() {
        InMemoryUserKeywordSubscriptionService service = new InMemoryUserKeywordSubscriptionService();

        boolean result = service.addKeyword(1L, " agent ");

        assertEquals(true, result);
        assertEquals(1, service.items.size());
        assertEquals("agent", service.items.getFirst().getKeyword());
        assertEquals(1, service.items.getFirst().getStatus());
    }

    @Test
    void listKeywordsByUserIdShouldOnlyReturnEnabledKeywords() {
        InMemoryUserKeywordSubscriptionService service = new InMemoryUserKeywordSubscriptionService();
        service.items.add(UserKeywordSubscription.builder().id(1L).userId(1L).keyword("agent").status(1).build());
        service.items.add(UserKeywordSubscription.builder().id(2L).userId(1L).keyword("workflow").status(1).build());
        service.items.add(UserKeywordSubscription.builder().id(3L).userId(1L).keyword("ignore").status(0).build());

        List<String> keywords = service.listKeywordsByUserId(1L);

        assertEquals(List.of("agent", "workflow"), keywords);
    }

    @Test
    void removeKeywordShouldDeleteMatchedSubscription() {
        InMemoryUserKeywordSubscriptionService service = new InMemoryUserKeywordSubscriptionService();
        service.items.add(UserKeywordSubscription.builder().id(1L).userId(1L).keyword("agent").status(1).build());
        service.items.add(UserKeywordSubscription.builder().id(2L).userId(1L).keyword("workflow").status(1).build());

        boolean removed = service.removeKeyword(1L, "agent");

        assertEquals(true, removed);
        assertEquals(1, service.items.size());
        assertEquals("workflow", service.items.getFirst().getKeyword());
    }

    private static class InMemoryUserKeywordSubscriptionService extends UserKeywordSubscriptionServiceImpl {

        private final List<UserKeywordSubscription> items = new ArrayList<>();

        @Override
        public boolean save(UserKeywordSubscription entity) {
            items.add(entity);
            return true;
        }

        @Override
        public boolean remove(com.mybatisflex.core.query.QueryWrapper queryWrapper) {
            return items.removeIf(item -> item.getUserId().equals(1L) && "agent".equals(item.getKeyword()));
        }

        @Override
        public List<UserKeywordSubscription> list(com.mybatisflex.core.query.QueryWrapper queryWrapper) {
            return items.stream()
                    .filter(item -> item.getUserId().equals(1L) && item.getStatus() != null && item.getStatus() == 1)
                    .toList();
        }
    }
}
