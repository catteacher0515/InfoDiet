package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.entity.ContentItem;
import com.pingyu.infodiet.model.entity.UserProfile;
import com.pingyu.infodiet.model.entity.UserSubscriptionRule;
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
        UserSubscriptionRuleService userSubscriptionRuleService = Mockito.mock(UserSubscriptionRuleService.class);
        ContentItemService contentItemService = new com.pingyu.infodiet.service.impl.ContentItemServiceImpl();

        UserProfile firstUser = UserProfile.builder().id(1L).nickname("pingyu").status(1).build();
        UserProfile secondUser = UserProfile.builder().id(2L).nickname("cat").status(1).build();

        when(userProfileService.listEnabledUsers()).thenReturn(List.of(firstUser, secondUser));
        when(userSubscriptionRuleService.listEnabledRulesByUserId(1L)).thenReturn(List.of(
                UserSubscriptionRule.builder().userId(1L).ruleType("keyword_include").ruleValue("agent").ruleWeight(3).status(1).build(),
                UserSubscriptionRule.builder().userId(1L).ruleType("author").ruleValue("Google for Developers").ruleWeight(5).status(1).build()
        ));
        when(userSubscriptionRuleService.listEnabledRulesByUserId(2L)).thenReturn(List.of(
                UserSubscriptionRule.builder().userId(2L).ruleType("keyword_include").ruleValue("java").ruleWeight(2).status(1).build()
        ));

        TestableSubscriptionMatchService service = new TestableSubscriptionMatchService();
        ReflectionTestUtils.setField(service, "userProfileService", userProfileService);
        ReflectionTestUtils.setField(service, "userSubscriptionRuleService", userSubscriptionRuleService);
        ReflectionTestUtils.setField(service, "contentItemService", contentItemService);

        service.contentItems = List.of(
                ContentItem.builder().id(101L).title("agent workflow").description("build with gemini").authorName("Other").build(),
                ContentItem.builder().id(102L).title("spring boot").description("java tutorial").authorName("Other").build(),
                ContentItem.builder().id(103L).title("Gemini API update").description("latest release").authorName("Google for Developers").build(),
                ContentItem.builder().id(104L).title("design note").description("no keyword").authorName("Other").build()
        );

        Map<Long, List<ContentItem>> result = service.matchEnabledUsers();

        assertEquals(2, result.size());
        assertEquals(List.of(103L, 101L), result.get(1L).stream().map(ContentItem::getId).toList());
        assertEquals(List.of(102L), result.get(2L).stream().map(ContentItem::getId).toList());
    }

    @Test
    void matchEnabledUsersShouldSkipUsersWithoutRules() {
        UserProfileService userProfileService = Mockito.mock(UserProfileService.class);
        UserSubscriptionRuleService userSubscriptionRuleService = Mockito.mock(UserSubscriptionRuleService.class);
        ContentItemService contentItemService = new com.pingyu.infodiet.service.impl.ContentItemServiceImpl();

        UserProfile user = UserProfile.builder().id(1L).nickname("pingyu").status(1).build();

        when(userProfileService.listEnabledUsers()).thenReturn(List.of(user));
        when(userSubscriptionRuleService.listEnabledRulesByUserId(1L)).thenReturn(List.of());

        TestableSubscriptionMatchService service = new TestableSubscriptionMatchService();
        ReflectionTestUtils.setField(service, "userProfileService", userProfileService);
        ReflectionTestUtils.setField(service, "userSubscriptionRuleService", userSubscriptionRuleService);
        ReflectionTestUtils.setField(service, "contentItemService", contentItemService);

        Map<Long, List<ContentItem>> result = service.matchEnabledUsers();

        assertEquals(0, result.size());
    }

    @Test
    void matchEnabledUsersShouldExcludeContentMatchedByExcludeRule() {
        UserProfileService userProfileService = Mockito.mock(UserProfileService.class);
        UserSubscriptionRuleService userSubscriptionRuleService = Mockito.mock(UserSubscriptionRuleService.class);
        ContentItemService contentItemService = new com.pingyu.infodiet.service.impl.ContentItemServiceImpl();

        UserProfile user = UserProfile.builder().id(1L).nickname("pingyu").status(1).build();

        when(userProfileService.listEnabledUsers()).thenReturn(List.of(user));
        when(userSubscriptionRuleService.listEnabledRulesByUserId(1L)).thenReturn(List.of(
                UserSubscriptionRule.builder().userId(1L).ruleType("keyword_include").ruleValue("agent").ruleWeight(3).status(1).build(),
                UserSubscriptionRule.builder().userId(1L).ruleType("keyword_exclude").ruleValue("finance").ruleWeight(0).status(1).build()
        ));

        TestableSubscriptionMatchService service = new TestableSubscriptionMatchService();
        ReflectionTestUtils.setField(service, "userProfileService", userProfileService);
        ReflectionTestUtils.setField(service, "userSubscriptionRuleService", userSubscriptionRuleService);
        ReflectionTestUtils.setField(service, "contentItemService", contentItemService);

        service.contentItems = List.of(
                ContentItem.builder().id(101L).title("agent workflow").description("general guide").build(),
                ContentItem.builder().id(102L).title("agent for finance").description("finance automation").build()
        );

        Map<Long, List<ContentItem>> result = service.matchEnabledUsers();

        assertEquals(List.of(101L), result.get(1L).stream().map(ContentItem::getId).toList());
    }

    @Test
    void matchEnabledUsersShouldSupportRepoAndChannelRules() {
        UserProfileService userProfileService = Mockito.mock(UserProfileService.class);
        UserSubscriptionRuleService userSubscriptionRuleService = Mockito.mock(UserSubscriptionRuleService.class);
        ContentItemService contentItemService = new com.pingyu.infodiet.service.impl.ContentItemServiceImpl();

        UserProfile user = UserProfile.builder().id(1L).nickname("pingyu").status(1).build();

        when(userProfileService.listEnabledUsers()).thenReturn(List.of(user));
        when(userSubscriptionRuleService.listEnabledRulesByUserId(1L)).thenReturn(List.of(
                UserSubscriptionRule.builder().userId(1L).ruleType("repo").ruleValue("vercel-labs/open-agents").ruleWeight(6).status(1).build(),
                UserSubscriptionRule.builder().userId(1L).ruleType("channel").ruleValue("UC_x5XG1OV2P6uZZ5FSM9Ttw").ruleWeight(4).status(1).build()
        ));

        TestableSubscriptionMatchService service = new TestableSubscriptionMatchService();
        ReflectionTestUtils.setField(service, "userProfileService", userProfileService);
        ReflectionTestUtils.setField(service, "userSubscriptionRuleService", userSubscriptionRuleService);
        ReflectionTestUtils.setField(service, "contentItemService", contentItemService);

        service.contentItems = List.of(
                ContentItem.builder()
                        .id(201L)
                        .platform("github")
                        .sourceId("vercel-labs/open-agents")
                        .title("open-agents")
                        .authorName("vercel-labs")
                        .build(),
                ContentItem.builder()
                        .id(202L)
                        .platform("youtube")
                        .sourceId("video-1")
                        .title("Gemini update")
                        .authorName("Google for Developers")
                        .authorUrl("https://www.youtube.com/channel/UC_x5XG1OV2P6uZZ5FSM9Ttw")
                        .build(),
                ContentItem.builder()
                        .id(203L)
                        .platform("github")
                        .sourceId("other/repo")
                        .title("other repo")
                        .authorName("other")
                        .build()
        );

        Map<Long, List<ContentItem>> result = service.matchEnabledUsers();

        assertEquals(List.of(201L, 202L), result.get(1L).stream().map(ContentItem::getId).toList());
    }

    private static class TestableSubscriptionMatchService extends SubscriptionMatchServiceImpl {

        private List<ContentItem> contentItems = List.of();

        @Override
        protected List<ContentItem> listCandidateContentItems() {
            return contentItems;
        }
    }
}
