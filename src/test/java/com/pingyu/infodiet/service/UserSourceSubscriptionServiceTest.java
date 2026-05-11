package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.entity.UserSourceSubscription;
import com.pingyu.infodiet.service.impl.UserSourceSubscriptionServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserSourceSubscriptionServiceTest {

    @Test
    void addSourceSubscriptionShouldCreateEnabledSubscription() {
        InMemoryUserSourceSubscriptionService service = new InMemoryUserSourceSubscriptionService();

        boolean result = service.addSourceSubscription(UserSourceSubscription.builder()
                .userId(1L)
                .platform(" youtube ")
                .sourceType(" channel ")
                .sourceValue(" UC123456 ")
                .build());

        assertEquals(true, result);
        assertEquals(1, service.items.size());
        assertEquals("youtube", service.items.getFirst().getPlatform());
        assertEquals("channel", service.items.getFirst().getSourceType());
        assertEquals("UC123456", service.items.getFirst().getSourceValue());
        assertEquals(1, service.items.getFirst().getStatus());
    }

    @Test
    void listEnabledSourceSubscriptionsShouldOnlyReturnEnabledItems() {
        InMemoryUserSourceSubscriptionService service = new InMemoryUserSourceSubscriptionService();
        service.items.add(UserSourceSubscription.builder().id(1L).userId(1L).platform("youtube").sourceType("channel").sourceValue("UC123").status(1).build());
        service.items.add(UserSourceSubscription.builder().id(2L).userId(1L).platform("github").sourceType("repo").sourceValue("openai/openai-java").status(0).build());

        List<UserSourceSubscription> result = service.listEnabledSourceSubscriptions();

        assertEquals(1, result.size());
        assertEquals("youtube", result.getFirst().getPlatform());
    }

    @Test
    void sourceSubscriptionMethodsShouldDeclareCacheAnnotations() throws NoSuchMethodException {
        Method listMethod = UserSourceSubscriptionServiceImpl.class.getDeclaredMethod("listEnabledSourceSubscriptions");
        Method addMethod = UserSourceSubscriptionServiceImpl.class.getDeclaredMethod(
                "addSourceSubscription",
                UserSourceSubscription.class
        );

        Cacheable cacheable = listMethod.getAnnotation(Cacheable.class);
        CacheEvict cacheEvict = addMethod.getAnnotation(CacheEvict.class);

        assertEquals("enabledSourceSubscriptions", cacheable.cacheNames()[0]);
        assertEquals(true, cacheEvict.allEntries());
    }

    private static class InMemoryUserSourceSubscriptionService extends UserSourceSubscriptionServiceImpl {

        private final List<UserSourceSubscription> items = new ArrayList<>();

        @Override
        public boolean save(UserSourceSubscription entity) {
            items.add(entity);
            return true;
        }

        @Override
        public List<UserSourceSubscription> list(com.mybatisflex.core.query.QueryWrapper queryWrapper) {
            return items.stream()
                    .filter(item -> item.getStatus() != null && item.getStatus() == 1)
                    .toList();
        }
    }
}
