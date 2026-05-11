package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.entity.ContentItem;
import com.pingyu.infodiet.model.entity.UserProfile;
import com.pingyu.infodiet.service.impl.SubscriptionMatchServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class SubscriptionMatchServiceTest {

    @Test
    void matchEnabledUsersShouldReturnMatchedContentByUser() {
        UserProfileService userProfileService = Mockito.mock(UserProfileService.class);
        UserKeywordSubscriptionService userKeywordSubscriptionService = Mockito.mock(UserKeywordSubscriptionService.class);
        ContentItemService contentItemService = new com.pingyu.infodiet.service.impl.ContentItemServiceImpl();

        UserProfile firstUser = UserProfile.builder().id(1L).nickname("pingyu").status(1).build();
        UserProfile secondUser = UserProfile.builder().id(2L).nickname("cat").status(1).build();

        when(userProfileService.listEnabledUsers()).thenReturn(List.of(firstUser, secondUser));
        when(userKeywordSubscriptionService.listKeywordsByUserId(1L)).thenReturn(List.of("agent", "gemini"));
        when(userKeywordSubscriptionService.listKeywordsByUserId(2L)).thenReturn(List.of("java"));

        TestableSubscriptionMatchService service = new TestableSubscriptionMatchService();
        ReflectionTestUtils.setField(service, "userProfileService", userProfileService);
        ReflectionTestUtils.setField(service, "userKeywordSubscriptionService", userKeywordSubscriptionService);
        ReflectionTestUtils.setField(service, "contentItemService", contentItemService);

        service.contentItems = List.of(
                ContentItem.builder().id(101L).title("agent workflow").description("build with gemini").build(),
                ContentItem.builder().id(102L).title("spring boot").description("java tutorial").build(),
                ContentItem.builder().id(103L).title("design note").description("no keyword").build()
        );

        Map<Long, List<ContentItem>> result = service.matchEnabledUsers();

        assertEquals(2, result.size());
        assertEquals(List.of(101L), result.get(1L).stream().map(ContentItem::getId).toList());
        assertEquals(List.of(102L), result.get(2L).stream().map(ContentItem::getId).toList());
    }

    @Test
    void matchEnabledUsersShouldSkipUsersWithoutKeywords() {
        UserProfileService userProfileService = Mockito.mock(UserProfileService.class);
        UserKeywordSubscriptionService userKeywordSubscriptionService = Mockito.mock(UserKeywordSubscriptionService.class);
        ContentItemService contentItemService = new com.pingyu.infodiet.service.impl.ContentItemServiceImpl();

        UserProfile user = UserProfile.builder().id(1L).nickname("pingyu").status(1).build();

        when(userProfileService.listEnabledUsers()).thenReturn(List.of(user));
        when(userKeywordSubscriptionService.listKeywordsByUserId(1L)).thenReturn(List.of());

        TestableSubscriptionMatchService service = new TestableSubscriptionMatchService();
        ReflectionTestUtils.setField(service, "userProfileService", userProfileService);
        ReflectionTestUtils.setField(service, "userKeywordSubscriptionService", userKeywordSubscriptionService);
        ReflectionTestUtils.setField(service, "contentItemService", contentItemService);

        Map<Long, List<ContentItem>> result = service.matchEnabledUsers();

        assertEquals(0, result.size());
    }

    private static class TestableSubscriptionMatchService extends SubscriptionMatchServiceImpl {

        private List<ContentItem> contentItems = List.of();

        @Override
        protected List<ContentItem> listCandidateContentItems() {
            return contentItems;
        }
    }
}
