package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.entity.ContentItem;
import com.pingyu.infodiet.model.entity.UserContentPush;
import com.pingyu.infodiet.model.entity.UserProfile;
import com.pingyu.infodiet.model.entity.UserSubscriptionRule;
import com.pingyu.infodiet.model.dto.content.UnifiedContentItemDTO;
import com.pingyu.infodiet.service.impl.SubscriptionMatchServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
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

    @Test
    void matchEnabledUsersShouldExcludeAlreadyPushedContent() {
        UserProfileService userProfileService = Mockito.mock(UserProfileService.class);
        UserSubscriptionRuleService userSubscriptionRuleService = Mockito.mock(UserSubscriptionRuleService.class);
        ContentItemService contentItemService = new com.pingyu.infodiet.service.impl.ContentItemServiceImpl();

        UserProfile user = UserProfile.builder().id(1L).nickname("pingyu").status(1).build();

        when(userProfileService.listEnabledUsers()).thenReturn(List.of(user));
        when(userSubscriptionRuleService.listEnabledRulesByUserId(1L)).thenReturn(List.of(
                UserSubscriptionRule.builder().userId(1L).ruleType("keyword_include").ruleValue("agent").ruleWeight(3).status(1).build()
        ));

        TestableSubscriptionMatchService service = new TestableSubscriptionMatchService();
        ReflectionTestUtils.setField(service, "userProfileService", userProfileService);
        ReflectionTestUtils.setField(service, "userSubscriptionRuleService", userSubscriptionRuleService);
        ReflectionTestUtils.setField(service, "contentItemService", contentItemService);

        service.contentItems = List.of(
                ContentItem.builder().id(301L).title("agent workflow").description("match").build(),
                ContentItem.builder().id(302L).title("agent skills").description("match").build()
        );
        service.userPushes = List.of(
                UserContentPush.builder().userId(1L).contentItemId(301L).pushStatus(1).build()
        );

        Map<Long, List<ContentItem>> result = service.matchEnabledUsers();

        assertEquals(List.of(302L), result.get(1L).stream().map(ContentItem::getId).toList());
    }

    @Test
    void matchEnabledUsersWithDetailsShouldReturnScoreAndMatchedRules() {
        UserProfileService userProfileService = Mockito.mock(UserProfileService.class);
        UserSubscriptionRuleService userSubscriptionRuleService = Mockito.mock(UserSubscriptionRuleService.class);
        ContentItemService contentItemService = new com.pingyu.infodiet.service.impl.ContentItemServiceImpl();

        UserProfile user = UserProfile.builder().id(1L).nickname("pingyu").status(1).build();

        when(userProfileService.listEnabledUsers()).thenReturn(List.of(user));
        when(userSubscriptionRuleService.listEnabledRulesByUserId(1L)).thenReturn(List.of(
                UserSubscriptionRule.builder().userId(1L).ruleType("author").ruleValue("Google for Developers").ruleWeight(5).status(1).build(),
                UserSubscriptionRule.builder().userId(1L).ruleType("keyword_include").ruleValue("agent").ruleWeight(3).status(1).build()
        ));

        TestableSubscriptionMatchService service = new TestableSubscriptionMatchService();
        ReflectionTestUtils.setField(service, "userProfileService", userProfileService);
        ReflectionTestUtils.setField(service, "userSubscriptionRuleService", userSubscriptionRuleService);
        ReflectionTestUtils.setField(service, "contentItemService", contentItemService);

        service.contentItems = List.of(
                ContentItem.builder()
                        .id(401L)
                        .title("Building agents")
                        .description("agent workflow")
                        .authorName("Google for Developers")
                        .build()
        );

        Map<Long, List<SubscriptionMatchService.MatchDetail>> result = service.matchEnabledUsersWithDetails();

        assertEquals(1, result.size());
        assertEquals(401L, result.get(1L).getFirst().getContentItem().getId());
        assertEquals(8, result.get(1L).getFirst().getScore());
        assertEquals(List.of("author:Google for Developers", "keyword_include:agent"), result.get(1L).getFirst().getMatchedRules());
    }

    @Test
    void matchEnabledUsersShouldUseUnifiedCandidatesToAvoidDuplicateContent() {
        UserProfileService userProfileService = Mockito.mock(UserProfileService.class);
        UserSubscriptionRuleService userSubscriptionRuleService = Mockito.mock(UserSubscriptionRuleService.class);
        ContentItemService contentItemService = new com.pingyu.infodiet.service.impl.ContentItemServiceImpl();

        UserProfile user = UserProfile.builder().id(1L).nickname("pingyu").status(1).build();

        when(userProfileService.listEnabledUsers()).thenReturn(List.of(user));
        when(userSubscriptionRuleService.listEnabledRulesByUserId(1L)).thenReturn(List.of(
                UserSubscriptionRule.builder().userId(1L).ruleType("keyword_include").ruleValue("agent").ruleWeight(3).status(1).build()
        ));

        TestableSubscriptionMatchService service = new TestableSubscriptionMatchService();
        ReflectionTestUtils.setField(service, "userProfileService", userProfileService);
        ReflectionTestUtils.setField(service, "userSubscriptionRuleService", userSubscriptionRuleService);
        ReflectionTestUtils.setField(service, "contentItemService", contentItemService);

        ContentItem duplicateOldItem = ContentItem.builder()
                .id(501L)
                .platform("github")
                .sourceId("vercel-labs/open-agents")
                .title("open agents")
                .description("agent workflow")
                .authorName("vercel")
                .build();
        ContentItem duplicateNewItem = ContentItem.builder()
                .id(502L)
                .platform("youtube")
                .sourceId("video-dup")
                .title("open agents")
                .description("agent workflow")
                .authorName("vercel")
                .build();

        service.unifiedCandidates = List.of(
                UnifiedContentItemDTO.builder().id(502L).dedupKey("open agents#vercel").build()
        );
        service.contentItems = List.of(duplicateOldItem, duplicateNewItem);

        Map<Long, List<ContentItem>> result = service.matchEnabledUsers();

        assertEquals(List.of(502L), result.get(1L).stream().map(ContentItem::getId).toList());
    }

    @Test
    void matchEnabledUsersShouldTreatUnifiedDuplicateAsAlreadyPushed() {
        UserProfileService userProfileService = Mockito.mock(UserProfileService.class);
        UserSubscriptionRuleService userSubscriptionRuleService = Mockito.mock(UserSubscriptionRuleService.class);
        ContentItemService contentItemService = new com.pingyu.infodiet.service.impl.ContentItemServiceImpl();

        UserProfile user = UserProfile.builder().id(1L).nickname("pingyu").status(1).build();

        when(userProfileService.listEnabledUsers()).thenReturn(List.of(user));
        when(userSubscriptionRuleService.listEnabledRulesByUserId(1L)).thenReturn(List.of(
                UserSubscriptionRule.builder().userId(1L).ruleType("keyword_include").ruleValue("agent").ruleWeight(3).status(1).build()
        ));

        TestableSubscriptionMatchService service = new TestableSubscriptionMatchService();
        ReflectionTestUtils.setField(service, "userProfileService", userProfileService);
        ReflectionTestUtils.setField(service, "userSubscriptionRuleService", userSubscriptionRuleService);
        ReflectionTestUtils.setField(service, "contentItemService", contentItemService);

        ContentItem pushedGithubItem = ContentItem.builder()
                .id(601L)
                .platform("github")
                .sourceId("vercel-labs/open-agents")
                .title("open agents")
                .description("agent workflow")
                .authorName("vercel")
                .build();
        ContentItem duplicateYoutubeItem = ContentItem.builder()
                .id(602L)
                .platform("youtube")
                .sourceId("video-open-agents")
                .title("open agents")
                .description("agent workflow")
                .authorName("vercel")
                .build();

        service.contentItems = List.of(duplicateYoutubeItem);
        service.contentById = Map.of(601L, pushedGithubItem);
        service.userPushes = List.of(
                UserContentPush.builder().userId(1L).contentItemId(601L).pushStatus(1).build()
        );

        Map<Long, List<ContentItem>> result = service.matchEnabledUsers();

        assertEquals(0, result.size());
    }

    @Test
    void matchEnabledUsersWithDetailsShouldDeclareRedisCache() throws NoSuchMethodException {
        Method method = SubscriptionMatchServiceImpl.class.getDeclaredMethod("matchEnabledUsersWithDetails");
        Cacheable cacheable = method.getAnnotation(Cacheable.class);

        assertEquals("matchEnabledUsersWithDetails", cacheable.cacheNames()[0]);
    }

    private static class TestableSubscriptionMatchService extends SubscriptionMatchServiceImpl {

        private List<ContentItem> contentItems = List.of();
        private List<UserContentPush> userPushes = List.of();
        private List<UnifiedContentItemDTO> unifiedCandidates = null;
        private Map<Long, ContentItem> contentById = Map.of();

        @Override
        protected List<ContentItem> listCandidateContentItems() {
            if (unifiedCandidates != null) {
                return super.listCandidateContentItems();
            }
            return contentItems;
        }

        @Override
        protected List<UnifiedContentItemDTO> listUnifiedCandidateContentItems() {
            return unifiedCandidates;
        }

        @Override
        protected List<UserContentPush> listPushedContentByUserId(Long userId) {
            return userPushes.stream()
                    .filter(item -> userId.equals(item.getUserId()))
                    .toList();
        }

        @Override
        protected ContentItem getContentItemById(Long contentItemId) {
            ContentItem contentItem = contentById.get(contentItemId);
            if (contentItem != null) {
                return contentItem;
            }
            return contentItems.stream()
                    .filter(item -> contentItemId.equals(item.getId()))
                    .findFirst()
                    .orElse(null);
        }
    }
}
