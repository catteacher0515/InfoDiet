package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.auth.LoginUser;
import com.pingyu.infodiet.model.auth.LoginUserContext;
import com.pingyu.infodiet.model.dto.user.WorkspaceSubscriptionsVO;
import com.pingyu.infodiet.model.entity.UserContentPush;
import com.pingyu.infodiet.model.entity.UserSourceSubscription;
import com.pingyu.infodiet.model.entity.UserSubscriptionRule;
import com.pingyu.infodiet.service.impl.WorkspaceServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorkspaceServiceTest {

    @AfterEach
    void clearLoginUserContext() {
        LoginUserContext.clear();
    }

    @Test
    void getMySubscriptionsShouldAggregateCurrentUserData() {
        WorkspaceServiceImpl service = new WorkspaceServiceImpl();
        UserKeywordSubscriptionService userKeywordSubscriptionService = mock(UserKeywordSubscriptionService.class);
        UserSubscriptionRuleService userSubscriptionRuleService = mock(UserSubscriptionRuleService.class);
        UserSourceSubscriptionService userSourceSubscriptionService = mock(UserSourceSubscriptionService.class);

        when(userKeywordSubscriptionService.listKeywordsByUserId(1L)).thenReturn(List.of("ai", "agent"));
        when(userSubscriptionRuleService.listEnabledRulesByUserId(1L)).thenReturn(List.of(
                UserSubscriptionRule.builder().id(1L).ruleType("author").ruleValue("google").ruleWeight(5).build()
        ));
        when(userSourceSubscriptionService.listSourceSubscriptionsByUserId(1L)).thenReturn(List.of(
                UserSourceSubscription.builder().id(1L).userId(1L).platform("youtube").sourceType("channel").sourceValue("UC100").status(1).build(),
                UserSourceSubscription.builder().id(2L).userId(1L).platform("github").sourceType("author").sourceValue("openai").status(0).build()
        ));

        ReflectionTestUtils.setField(service, "userKeywordSubscriptionService", userKeywordSubscriptionService);
        ReflectionTestUtils.setField(service, "userSubscriptionRuleService", userSubscriptionRuleService);
        ReflectionTestUtils.setField(service, "userSourceSubscriptionService", userSourceSubscriptionService);
        LoginUserContext.set(LoginUser.builder().userId(1L).username("user").role("user").build());

        WorkspaceSubscriptionsVO response = service.getMySubscriptions();

        assertEquals(2, response.getKeywords().size());
        assertEquals(1, response.getRules().size());
        assertEquals(1, response.getSources().size());
        assertEquals("youtube", response.getSources().getFirst().getPlatform());
    }

    @Test
    void listMyPushesShouldReturnCurrentUserPushes() {
        WorkspaceServiceImpl service = new WorkspaceServiceImpl();
        UserContentPushService userContentPushService = mock(UserContentPushService.class);
        when(userContentPushService.listPushesByUserId(1L)).thenReturn(List.of(
                UserContentPush.builder().id(1L).userId(1L).contentItemId(10L).pushStatus(1).build()
        ));

        ReflectionTestUtils.setField(service, "userContentPushService", userContentPushService);
        LoginUserContext.set(LoginUser.builder().userId(1L).username("user").role("user").build());

        List<UserContentPush> response = service.listMyPushes();

        assertEquals(1, response.size());
        assertEquals(10L, response.getFirst().getContentItemId());
    }
}
