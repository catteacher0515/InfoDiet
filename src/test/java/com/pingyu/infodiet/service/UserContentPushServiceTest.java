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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        assertEquals(0, result.getSkippedByExistingCount());
        assertEquals(0, result.getSkippedByLimitCount());
        assertEquals(0, result.getSkippedByCooldownCount());
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
        assertEquals(1, result.getSkippedByExistingCount());
        assertEquals(0, result.getSkippedByLimitCount());
        assertEquals(0, result.getSkippedByCooldownCount());
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
        assertEquals(0, result.getSkippedByExistingCount());
        assertEquals(2, result.getSkippedByLimitCount());
        assertEquals(0, result.getSkippedByCooldownCount());
        assertEquals(1, service.savedItems.size());
    }

    @Test
    void createPendingPushesShouldRespectMatchOrderWhenLimited() {
        SubscriptionMatchService subscriptionMatchService = Mockito.mock(SubscriptionMatchService.class);
        UserProfileService userProfileService = Mockito.mock(UserProfileService.class);

        ContentItem higherScoreItem = ContentItem.builder()
                .id(301L)
                .platform("youtube")
                .title("higher score item")
                .publishTime(LocalDateTime.of(2026, 5, 9, 10, 0))
                .build();
        ContentItem lowerScoreButPlatformPreferredItem = ContentItem.builder()
                .id(302L)
                .platform("github")
                .title("lower score item")
                .todayStarCount(10)
                .starCount(100)
                .build();

        when(subscriptionMatchService.matchEnabledUsers()).thenReturn(Map.of(
                1L, List.of(higherScoreItem, lowerScoreButPlatformPreferredItem)
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
        assertEquals(0, result.getSkippedByExistingCount());
        assertEquals(1, result.getSkippedByLimitCount());
        assertEquals(0, result.getSkippedByCooldownCount());
        assertEquals(1, service.savedItems.size());
        assertEquals(301L, service.savedItems.get(0).getContentItemId());
    }

    @Test
    void createPendingPushesShouldSkipWhenUserIsInCooldown() {
        SubscriptionMatchService subscriptionMatchService = Mockito.mock(SubscriptionMatchService.class);
        UserProfileService userProfileService = Mockito.mock(UserProfileService.class);

        ContentItem firstItem = ContentItem.builder().id(401L).title("agent workflow").build();

        when(subscriptionMatchService.matchEnabledUsers()).thenReturn(Map.of(
                1L, List.of(firstItem)
        ));
        when(userProfileService.getUserById(1L)).thenReturn(
                com.pingyu.infodiet.model.entity.UserProfile.builder()
                        .id(1L)
                        .pushChannel("feishu")
                        .dailyPushLimit(5)
                        .pushCooldownHours(6)
                        .build()
        );

        InMemoryUserContentPushService service = new InMemoryUserContentPushService();
        ReflectionTestUtils.setField(service, "subscriptionMatchService", subscriptionMatchService);
        ReflectionTestUtils.setField(service, "userProfileService", userProfileService);
        service.fixedNow = LocalDateTime.of(2026, 5, 11, 22, 0);
        service.savedItems.add(UserContentPush.builder()
                .id(1L)
                .userId(1L)
                .contentItemId(300L)
                .pushStatus(1)
                .pushTime(LocalDateTime.of(2026, 5, 11, 18, 30))
                .build());

        UserContentPushService.CreatePushResult result = service.createPendingPushes();

        assertEquals(1, result.getTotalCount());
        assertEquals(0, result.getCreatedCount());
        assertEquals(1, result.getSkippedCount());
        assertEquals(0, result.getSkippedByExistingCount());
        assertEquals(0, result.getSkippedByLimitCount());
        assertEquals(1, result.getSkippedByCooldownCount());
        assertEquals(1, service.savedItems.size());
    }

    @Test
    void createPendingPushesShouldCreateAfterCooldownExpires() {
        SubscriptionMatchService subscriptionMatchService = Mockito.mock(SubscriptionMatchService.class);
        UserProfileService userProfileService = Mockito.mock(UserProfileService.class);

        ContentItem firstItem = ContentItem.builder().id(402L).title("agent workflow").build();

        when(subscriptionMatchService.matchEnabledUsers()).thenReturn(Map.of(
                1L, List.of(firstItem)
        ));
        when(userProfileService.getUserById(1L)).thenReturn(
                com.pingyu.infodiet.model.entity.UserProfile.builder()
                        .id(1L)
                        .pushChannel("feishu")
                        .dailyPushLimit(5)
                        .pushCooldownHours(6)
                        .build()
        );

        InMemoryUserContentPushService service = new InMemoryUserContentPushService();
        ReflectionTestUtils.setField(service, "subscriptionMatchService", subscriptionMatchService);
        ReflectionTestUtils.setField(service, "userProfileService", userProfileService);
        service.fixedNow = LocalDateTime.of(2026, 5, 11, 22, 0);
        service.savedItems.add(UserContentPush.builder()
                .id(1L)
                .userId(1L)
                .contentItemId(300L)
                .pushStatus(1)
                .pushTime(LocalDateTime.of(2026, 5, 11, 15, 0))
                .build());

        UserContentPushService.CreatePushResult result = service.createPendingPushes();

        assertEquals(1, result.getTotalCount());
        assertEquals(1, result.getCreatedCount());
        assertEquals(0, result.getSkippedCount());
        assertEquals(0, result.getSkippedByExistingCount());
        assertEquals(0, result.getSkippedByLimitCount());
        assertEquals(0, result.getSkippedByCooldownCount());
        assertEquals(2, service.savedItems.size());
        assertEquals(402L, service.savedItems.getLast().getContentItemId());
    }

    @Test
    void listEnqueueablePushesShouldOnlyReturnRetryableRecords() {
        InMemoryUserContentPushService service = new InMemoryUserContentPushService();
        service.fixedNow = LocalDateTime.of(2026, 5, 12, 10, 0);
        service.savedItems.add(UserContentPush.builder()
                .id(1L)
                .pushChannel("feishu")
                .pushStatus(0)
                .queueStatus(0)
                .nextRetryTime(null)
                .build());
        service.savedItems.add(UserContentPush.builder()
                .id(2L)
                .pushChannel("feishu")
                .pushStatus(0)
                .queueStatus(0)
                .nextRetryTime(service.fixedNow.plusMinutes(10))
                .build());
        service.savedItems.add(UserContentPush.builder()
                .id(3L)
                .pushChannel("feishu")
                .pushStatus(0)
                .queueStatus(1)
                .build());
        service.savedItems.add(UserContentPush.builder()
                .id(4L)
                .pushChannel("telegram")
                .pushStatus(0)
                .queueStatus(0)
                .build());

        List<UserContentPush> result = service.listEnqueueablePushesByChannel("feishu");

        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().getId());
    }

    @Test
    void markQueuedShouldUpdateQueueStateOnlyOnce() {
        InMemoryUserContentPushService service = new InMemoryUserContentPushService();
        service.fixedNow = LocalDateTime.of(2026, 5, 12, 10, 0);
        service.savedItems.add(UserContentPush.builder()
                .id(1L)
                .pushStatus(0)
                .queueStatus(0)
                .retryCount(0)
                .build());

        boolean firstResult = service.markQueued(1L);
        boolean secondResult = service.markQueued(1L);

        assertTrue(firstResult);
        assertFalse(secondResult);
        assertEquals(1, service.findById(1L).getQueueStatus());
        assertEquals(service.fixedNow, service.findById(1L).getLastQueueTime());
    }

    @Test
    void markPushFailedShouldScheduleNextRetryWhenRetryCountNotExceeded() {
        InMemoryUserContentPushService service = new InMemoryUserContentPushService();
        service.fixedNow = LocalDateTime.of(2026, 5, 12, 10, 0);
        service.savedItems.add(UserContentPush.builder()
                .id(1L)
                .pushStatus(0)
                .queueStatus(2)
                .retryCount(0)
                .maxRetryCount(3)
                .build());

        boolean result = service.markPushFailed(1L, "飞书超时");

        assertTrue(result);
        UserContentPush updated = service.findById(1L);
        assertEquals(0, updated.getPushStatus());
        assertEquals(0, updated.getQueueStatus());
        assertEquals(1, updated.getRetryCount());
        assertEquals("飞书超时", updated.getFailReason());
        assertEquals(service.fixedNow.plusMinutes(5), updated.getNextRetryTime());
    }

    @Test
    void markPushFailedShouldBecomeFailedWhenRetryCountExceeded() {
        InMemoryUserContentPushService service = new InMemoryUserContentPushService();
        service.fixedNow = LocalDateTime.of(2026, 5, 12, 10, 0);
        service.savedItems.add(UserContentPush.builder()
                .id(1L)
                .pushStatus(0)
                .queueStatus(2)
                .retryCount(2)
                .maxRetryCount(3)
                .build());

        boolean result = service.markPushFailed(1L, "重试上限");

        assertTrue(result);
        UserContentPush updated = service.findById(1L);
        assertEquals(2, updated.getPushStatus());
        assertEquals(3, updated.getQueueStatus());
        assertEquals(3, updated.getRetryCount());
        assertEquals("重试上限", updated.getFailReason());
        assertNull(updated.getNextRetryTime());
    }

    @Test
    void markPushSuccessShouldCompleteQueueState() {
        InMemoryUserContentPushService service = new InMemoryUserContentPushService();
        service.fixedNow = LocalDateTime.of(2026, 5, 12, 10, 0);
        service.savedItems.add(UserContentPush.builder()
                .id(1L)
                .pushStatus(0)
                .queueStatus(2)
                .retryCount(1)
                .failReason("旧错误")
                .build());

        boolean result = service.markPushSuccess(1L);

        assertTrue(result);
        UserContentPush updated = service.findById(1L);
        assertEquals(1, updated.getPushStatus());
        assertEquals(3, updated.getQueueStatus());
        assertEquals(service.fixedNow, updated.getPushTime());
        assertNull(updated.getFailReason());
        assertNull(updated.getNextRetryTime());
    }

    @Test
    void retryFailedPushShouldResetFailedPushRecord() {
        InMemoryUserContentPushService service = new InMemoryUserContentPushService();
        service.fixedNow = LocalDateTime.of(2026, 5, 12, 10, 0);
        service.savedItems.add(UserContentPush.builder()
                .id(1L)
                .pushChannel("feishu")
                .pushStatus(2)
                .queueStatus(3)
                .retryCount(3)
                .maxRetryCount(3)
                .failReason("最终失败")
                .nextRetryTime(LocalDateTime.of(2026, 5, 12, 9, 0))
                .build());

        boolean result = service.retryFailedPush(1L);

        assertTrue(result);
        UserContentPush updated = service.findById(1L);
        assertEquals(0, updated.getPushStatus());
        assertEquals(0, updated.getQueueStatus());
        assertEquals(0, updated.getRetryCount());
        assertNull(updated.getFailReason());
        assertNull(updated.getNextRetryTime());
    }

    @Test
    void listFailedPushesByChannelShouldOnlyReturnFailedPushesOfCurrentChannel() {
        InMemoryUserContentPushService service = new InMemoryUserContentPushService();
        service.savedItems.add(UserContentPush.builder()
                .id(1L)
                .pushChannel("feishu")
                .pushStatus(2)
                .queueStatus(3)
                .build());
        service.savedItems.add(UserContentPush.builder()
                .id(2L)
                .pushChannel("feishu")
                .pushStatus(0)
                .queueStatus(0)
                .build());
        service.savedItems.add(UserContentPush.builder()
                .id(3L)
                .pushChannel("telegram")
                .pushStatus(2)
                .queueStatus(3)
                .build());

        List<UserContentPush> result = service.listFailedPushesByChannel("feishu");

        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().getId());
    }

    @Test
    void retryFailedPushesShouldReturnBatchSummary() {
        InMemoryUserContentPushService service = new InMemoryUserContentPushService();
        service.savedItems.add(UserContentPush.builder()
                .id(1L)
                .pushChannel("feishu")
                .pushStatus(2)
                .queueStatus(3)
                .retryCount(3)
                .build());
        service.savedItems.add(UserContentPush.builder()
                .id(2L)
                .pushChannel("feishu")
                .pushStatus(1)
                .queueStatus(3)
                .retryCount(0)
                .build());

        UserContentPushService.BatchRetryResult result = service.retryFailedPushes(List.of(1L, 2L, 3L));

        assertEquals(3, result.getTotalCount());
        assertEquals(1, result.getSuccessCount());
        assertEquals(2, result.getFailedCount());
        assertEquals(0, service.findById(1L).getPushStatus());
        assertEquals(0, service.findById(1L).getQueueStatus());
    }

    private static class InMemoryUserContentPushService extends UserContentPushServiceImpl {

        private final List<UserContentPush> savedItems = new ArrayList<>();
        private LocalDateTime fixedNow = LocalDateTime.now();

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

        @Override
        public List<UserContentPush> listEnqueueablePushesByChannel(String pushChannel) {
            return savedItems.stream()
                    .filter(item -> pushChannel.equals(item.getPushChannel()))
                    .filter(item -> item.getPushStatus() != null && item.getPushStatus() == 0)
                    .filter(item -> item.getQueueStatus() != null && item.getQueueStatus() == 0)
                    .filter(item -> item.getNextRetryTime() == null || !item.getNextRetryTime().isAfter(fixedNow))
                    .toList();
        }

        @Override
        public List<UserContentPush> listFailedPushesByChannel(String pushChannel) {
            return savedItems.stream()
                    .filter(item -> pushChannel.equals(item.getPushChannel()))
                    .filter(item -> item.getPushStatus() != null && item.getPushStatus() == 2)
                    .toList();
        }

        @Override
        public boolean markQueued(Long pushId) {
            UserContentPush existing = findById(pushId);
            if (existing == null || existing.getPushStatus() == null || existing.getPushStatus() != 0
                    || existing.getQueueStatus() == null || existing.getQueueStatus() != 0) {
                return false;
            }
            existing.setQueueStatus(1);
            existing.setLastQueueTime(fixedNow);
            return true;
        }

        @Override
        public boolean markConsuming(Long pushId) {
            UserContentPush existing = findById(pushId);
            if (existing == null || existing.getPushStatus() == null || existing.getPushStatus() != 0
                    || existing.getQueueStatus() == null || existing.getQueueStatus() != 1) {
                return false;
            }
            existing.setQueueStatus(2);
            return true;
        }

        @Override
        public boolean markPushSuccess(Long pushId) {
            UserContentPush existing = findById(pushId);
            if (existing == null || existing.getPushStatus() == null || existing.getPushStatus() != 0) {
                return false;
            }
            existing.setPushStatus(1);
            existing.setQueueStatus(3);
            existing.setPushTime(fixedNow);
            existing.setFailReason(null);
            existing.setNextRetryTime(null);
            return true;
        }

        @Override
        public boolean markPushFailed(Long pushId, String failReason) {
            UserContentPush existing = findById(pushId);
            if (existing == null || existing.getPushStatus() == null || existing.getPushStatus() == 1) {
                return false;
            }
            int currentRetryCount = existing.getRetryCount() == null ? 0 : existing.getRetryCount();
            int maxRetryCount = existing.getMaxRetryCount() == null ? 3 : existing.getMaxRetryCount();
            int nextRetryCount = currentRetryCount + 1;
            existing.setRetryCount(nextRetryCount);
            existing.setFailReason(failReason);
            if (nextRetryCount >= maxRetryCount) {
                existing.setPushStatus(2);
                existing.setQueueStatus(3);
                existing.setNextRetryTime(null);
            } else {
                existing.setPushStatus(0);
                existing.setQueueStatus(0);
                existing.setNextRetryTime(fixedNow.plusMinutes(5));
            }
            return true;
        }

        @Override
        public boolean retryFailedPush(Long pushId) {
            UserContentPush existing = findById(pushId);
            if (existing == null || existing.getPushStatus() == null || existing.getPushStatus() != 2) {
                return false;
            }
            existing.setPushStatus(0);
            existing.setQueueStatus(0);
            existing.setRetryCount(0);
            existing.setFailReason(null);
            existing.setNextRetryTime(null);
            return true;
        }

        @Override
        protected LocalDateTime now() {
            return fixedNow;
        }

        @Override
        protected LocalDateTime getLastSuccessPushTime(Long userId) {
            return savedItems.stream()
                    .filter(item -> userId.equals(item.getUserId()))
                    .filter(item -> item.getPushStatus() != null && item.getPushStatus() == 1)
                    .map(UserContentPush::getPushTime)
                    .filter(item -> item != null)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
        }

        private UserContentPush findById(Long id) {
            return savedItems.stream()
                    .filter(item -> id.equals(item.getId()))
                    .findFirst()
                    .orElse(null);
        }

    }
}
