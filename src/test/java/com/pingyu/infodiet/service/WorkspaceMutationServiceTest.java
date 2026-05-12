package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.auth.LoginUser;
import com.pingyu.infodiet.model.auth.LoginUserContext;
import com.pingyu.infodiet.model.dto.content.WorkspaceContentQueryRequest;
import com.pingyu.infodiet.model.entity.ContentItem;
import com.pingyu.infodiet.model.entity.UserSourceSubscription;
import com.pingyu.infodiet.model.entity.UserSubscriptionRule;
import com.pingyu.infodiet.service.impl.WorkspaceServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorkspaceMutationServiceTest {

    @AfterEach
    void clearLoginUserContext() {
        LoginUserContext.clear();
    }

    @Test
    void addMyKeywordShouldDelegateWithCurrentUser() {
        WorkspaceServiceImpl service = new WorkspaceServiceImpl();
        UserKeywordSubscriptionService userKeywordSubscriptionService = mock(UserKeywordSubscriptionService.class);
        when(userKeywordSubscriptionService.addKeyword(1L, "agent")).thenReturn(true);

        ReflectionTestUtils.setField(service, "userKeywordSubscriptionService", userKeywordSubscriptionService);
        LoginUserContext.set(LoginUser.builder().userId(1L).username("user").role("user").build());

        boolean result = service.addMyKeyword("agent");

        assertEquals(true, result);
    }

    @Test
    void addMyRuleShouldBindCurrentUserId() {
        WorkspaceServiceImpl service = new WorkspaceServiceImpl();
        UserSubscriptionRuleService userSubscriptionRuleService = mock(UserSubscriptionRuleService.class);
        when(userSubscriptionRuleService.addRule(org.mockito.ArgumentMatchers.any())).thenReturn(true);

        ReflectionTestUtils.setField(service, "userSubscriptionRuleService", userSubscriptionRuleService);
        LoginUserContext.set(LoginUser.builder().userId(1L).username("user").role("user").build());

        boolean result = service.addMyRule(UserSubscriptionRule.builder()
                .ruleType("author")
                .ruleValue("openai")
                .ruleWeight(5)
                .build());

        assertEquals(true, result);
    }

    @Test
    void listMyContentItemsShouldReturnMatchedContent() {
        WorkspaceServiceImpl service = new WorkspaceServiceImpl();
        SubscriptionMatchService subscriptionMatchService = mock(SubscriptionMatchService.class);
        ContentItemService contentItemService = mock(ContentItemService.class);
        ContentItem contentItem = ContentItem.builder().id(1L).platform("github").title("agent repo").contentType("repository").build();

        when(subscriptionMatchService.matchEnabledUsers()).thenReturn(Map.of(1L, List.of(contentItem)));
        when(contentItemService.convertToUnifiedContentItem(contentItem))
                .thenReturn(com.pingyu.infodiet.model.dto.content.UnifiedContentItemDTO.builder()
                        .id(1L)
                        .platform("github")
                        .title("agent repo")
                        .contentType("repository")
                        .build());

        ReflectionTestUtils.setField(service, "subscriptionMatchService", subscriptionMatchService);
        ReflectionTestUtils.setField(service, "contentItemService", contentItemService);
        LoginUserContext.set(LoginUser.builder().userId(1L).username("user").role("user").build());

        var result = service.listMyContentItems(new WorkspaceContentQueryRequest());

        assertEquals(1, result.size());
        assertEquals("github", result.getFirst().getPlatform());
    }

    @Test
    void addMySourceShouldBindCurrentUserId() {
        WorkspaceServiceImpl service = new WorkspaceServiceImpl();
        UserSourceSubscriptionService userSourceSubscriptionService = mock(UserSourceSubscriptionService.class);
        when(userSourceSubscriptionService.addSourceSubscription(org.mockito.ArgumentMatchers.any())).thenReturn(true);

        ReflectionTestUtils.setField(service, "userSourceSubscriptionService", userSourceSubscriptionService);
        LoginUserContext.set(LoginUser.builder().userId(1L).username("user").role("user").build());

        boolean result = service.addMySource(UserSourceSubscription.builder()
                .platform("youtube")
                .sourceType("channel")
                .sourceValue("UC100")
                .build());

        assertEquals(true, result);
    }

    @Test
    void removeMySourceShouldDelegateWithCurrentUser() {
        WorkspaceServiceImpl service = new WorkspaceServiceImpl();
        UserSourceSubscriptionService userSourceSubscriptionService = mock(UserSourceSubscriptionService.class);
        when(userSourceSubscriptionService.removeSourceSubscription(1L, 100L)).thenReturn(true);

        ReflectionTestUtils.setField(service, "userSourceSubscriptionService", userSourceSubscriptionService);
        LoginUserContext.set(LoginUser.builder().userId(1L).username("user").role("user").build());

        boolean result = service.removeMySource(100L);

        assertEquals(true, result);
    }
}
