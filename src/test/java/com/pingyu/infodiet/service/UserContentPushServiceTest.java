package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.entity.ContentItem;
import com.pingyu.infodiet.model.entity.UserContentPush;
import com.pingyu.infodiet.service.impl.UserContentPushServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class UserContentPushServiceTest {

    @Test
    void createPendingPushesShouldGeneratePushRecordsFromMatchResult() {
        SubscriptionMatchService subscriptionMatchService = Mockito.mock(SubscriptionMatchService.class);
        UserProfileService userProfileService = Mockito.mock(UserProfileService.class);

        ContentItem firstItem = ContentItem.builder().id(101L).title("agent workflow").build();
        ContentItem secondItem = ContentItem.builder().id(102L).title("java tutorial").build();

        when(subscriptionMatchService.matchEnabledUsers()).thenReturn(Map.of(
                1L, List.of(firstItem, secondItem)
        ));
        when(userProfileService.getUserById(1L)).thenReturn(
                com.pingyu.infodiet.model.entity.UserProfile.builder()
                        .id(1L)
                        .pushChannel("feishu")
                        .build()
        );

        InMemoryUserContentPushService service = new InMemoryUserContentPushService();
        ReflectionTestUtils.setField(service, "subscriptionMatchService", subscriptionMatchService);
        ReflectionTestUtils.setField(service, "userProfileService", userProfileService);

        UserContentPushService.CreatePushResult result = service.createPendingPushes();

        assertEquals(2, result.getTotalCount());
        assertEquals(2, result.getCreatedCount());
        assertEquals(0, result.getSkippedCount());
        assertEquals(2, service.savedItems.size());
        assertEquals(1L, service.savedItems.get(0).getUserId());
        assertEquals("feishu", service.savedItems.get(0).getPushChannel());
        assertEquals(0, service.savedItems.get(0).getPushStatus());
    }

    @Test
    void createPendingPushesShouldSkipExistingPushRecords() {
        SubscriptionMatchService subscriptionMatchService = Mockito.mock(SubscriptionMatchService.class);
        UserProfileService userProfileService = Mockito.mock(UserProfileService.class);

        ContentItem firstItem = ContentItem.builder().id(101L).title("agent workflow").build();

        when(subscriptionMatchService.matchEnabledUsers()).thenReturn(Map.of(
                1L, List.of(firstItem)
        ));
        when(userProfileService.getUserById(1L)).thenReturn(
                com.pingyu.infodiet.model.entity.UserProfile.builder()
                        .id(1L)
                        .pushChannel("feishu")
                        .build()
        );

        InMemoryUserContentPushService service = new InMemoryUserContentPushService();
        ReflectionTestUtils.setField(service, "subscriptionMatchService", subscriptionMatchService);
        ReflectionTestUtils.setField(service, "userProfileService", userProfileService);
        service.savedItems.add(UserContentPush.builder().userId(1L).contentItemId(101L).build());

        UserContentPushService.CreatePushResult result = service.createPendingPushes();

        assertEquals(1, result.getTotalCount());
        assertEquals(0, result.getCreatedCount());
        assertEquals(1, result.getSkippedCount());
        assertEquals(1, service.savedItems.size());
    }

    @Test
    void createPendingPushesShouldRespectDailyPushLimit() {
        SubscriptionMatchService subscriptionMatchService = Mockito.mock(SubscriptionMatchService.class);
        UserProfileService userProfileService = Mockito.mock(UserProfileService.class);

        ContentItem firstItem = ContentItem.builder().id(101L).title("agent workflow").build();
        ContentItem secondItem = ContentItem.builder().id(102L).title("java tutorial").build();

        when(subscriptionMatchService.matchEnabledUsers()).thenReturn(Map.of(
                1L, List.of(firstItem, secondItem)
        ));
        when(userProfileService.getUserById(1L)).thenReturn(
                com.pingyu.infodiet.model.entity.UserProfile.builder()
                        .id(1L)
                        .pushChannel("feishu")
                        .dailyPushLimit(1)
                        .build()
        );

        InMemoryUserContentPushService service = new InMemoryUserContentPushService();
        ReflectionTestUtils.setField(service, "subscriptionMatchService", subscriptionMatchService);
        ReflectionTestUtils.setField(service, "userProfileService", userProfileService);
        service.savedItems.add(UserContentPush.builder()
                .userId(1L)
                .contentItemId(100L)
                .pushStatus(1)
                .build());

        UserContentPushService.CreatePushResult result = service.createPendingPushes();

        assertEquals(2, result.getTotalCount());
        assertEquals(0, result.getCreatedCount());
        assertEquals(2, result.getSkippedCount());
        assertEquals(1, service.savedItems.size());
    }

    @Test
    void createPendingPushesShouldPreferHigherPriorityGithubContentWhenLimited() {
        SubscriptionMatchService subscriptionMatchService = Mockito.mock(SubscriptionMatchService.class);
        UserProfileService userProfileService = Mockito.mock(UserProfileService.class);

        ContentItem lowPriorityItem = ContentItem.builder()
                .id(101L)
                .platform("github")
                .title("low priority repo")
                .todayStarCount(10)
                .starCount(200)
                .build();
        ContentItem highPriorityItem = ContentItem.builder()
                .id(102L)
                .platform("github")
                .title("high priority repo")
                .todayStarCount(100)
                .starCount(500)
                .build();

        when(subscriptionMatchService.matchEnabledUsers()).thenReturn(Map.of(
                1L, List.of(lowPriorityItem, highPriorityItem)
        ));
        when(userProfileService.getUserById(1L)).thenReturn(
                com.pingyu.infodiet.model.entity.UserProfile.builder()
                        .id(1L)
                        .pushChannel("feishu")
                        .dailyPushLimit(1)
                        .build()
        );

        InMemoryUserContentPushService service = new InMemoryUserContentPushService();
        ReflectionTestUtils.setField(service, "subscriptionMatchService", subscriptionMatchService);
        ReflectionTestUtils.setField(service, "userProfileService", userProfileService);

        UserContentPushService.CreatePushResult result = service.createPendingPushes();

        assertEquals(2, result.getTotalCount());
        assertEquals(1, result.getCreatedCount());
        assertEquals(1, result.getSkippedCount());
        assertEquals(1, service.savedItems.size());
        assertEquals(102L, service.savedItems.get(0).getContentItemId());
    }

    @Test
    void createPendingPushesShouldPreferNewerYoutubeContentWhenLimited() {
        SubscriptionMatchService subscriptionMatchService = Mockito.mock(SubscriptionMatchService.class);
        UserProfileService userProfileService = Mockito.mock(UserProfileService.class);

        ContentItem olderVideo = ContentItem.builder()
                .id(201L)
                .platform("youtube")
                .title("older video")
                .publishTime(LocalDateTime.of(2026, 5, 9, 10, 0))
                .build();
        ContentItem newerVideo = ContentItem.builder()
                .id(202L)
                .platform("youtube")
                .title("newer video")
                .publishTime(LocalDateTime.of(2026, 5, 10, 10, 0))
                .build();

        when(subscriptionMatchService.matchEnabledUsers()).thenReturn(Map.of(
                1L, List.of(olderVideo, newerVideo)
        ));
        when(userProfileService.getUserById(1L)).thenReturn(
                com.pingyu.infodiet.model.entity.UserProfile.builder()
                        .id(1L)
                        .pushChannel("feishu")
                        .dailyPushLimit(1)
                        .build()
        );

        InMemoryUserContentPushService service = new InMemoryUserContentPushService();
        ReflectionTestUtils.setField(service, "subscriptionMatchService", subscriptionMatchService);
        ReflectionTestUtils.setField(service, "userProfileService", userProfileService);

        UserContentPushService.CreatePushResult result = service.createPendingPushes();

        assertEquals(2, result.getTotalCount());
        assertEquals(1, result.getCreatedCount());
        assertEquals(1, result.getSkippedCount());
        assertEquals(1, service.savedItems.size());
        assertEquals(202L, service.savedItems.get(0).getContentItemId());
    }

    private static class InMemoryUserContentPushService extends UserContentPushServiceImpl {

        private final List<UserContentPush> savedItems = new ArrayList<>();

        @Override
        protected boolean existsByUserIdAndContentItemId(Long userId, Long contentItemId) {
            return savedItems.stream().anyMatch(item ->
                    userId.equals(item.getUserId()) && contentItemId.equals(item.getContentItemId()));
        }

        @Override
        protected int countTodayPushesByUserId(Long userId) {
            return (int) savedItems.stream()
                    .filter(item -> userId.equals(item.getUserId()))
                    .count();
        }

        @Override
        public boolean save(UserContentPush entity) {
            savedItems.add(entity);
            return true;
        }
    }
}
