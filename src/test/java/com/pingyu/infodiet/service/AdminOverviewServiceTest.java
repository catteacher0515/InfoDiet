package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.dto.dashboard.AdminSubscriptionOverviewVO;
import com.pingyu.infodiet.model.dto.user.UserListItemVO;
import com.pingyu.infodiet.model.entity.UserSourceSubscription;
import com.pingyu.infodiet.service.impl.AdminOverviewServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminOverviewServiceTest {

    @Test
    void getSubscriptionOverviewShouldAggregateStatistics() {
        AdminOverviewServiceImpl service = new AdminOverviewServiceImpl();
        UserProfileService userProfileService = mock(UserProfileService.class);
        UserKeywordSubscriptionService userKeywordSubscriptionService = mock(UserKeywordSubscriptionService.class);
        UserSubscriptionRuleService userSubscriptionRuleService = mock(UserSubscriptionRuleService.class);
        UserSourceSubscriptionService userSourceSubscriptionService = mock(UserSourceSubscriptionService.class);

        when(userProfileService.listUsers()).thenReturn(List.of(
                UserListItemVO.builder().id(1L).build(),
                UserListItemVO.builder().id(2L).build()
        ));
        when(userKeywordSubscriptionService.listKeywordsByUserId(1L)).thenReturn(List.of("ai", "agent"));
        when(userKeywordSubscriptionService.listKeywordsByUserId(2L)).thenReturn(List.of("java"));
        when(userSubscriptionRuleService.listEnabledRulesByUserId(1L)).thenReturn(List.of(
                com.pingyu.infodiet.model.entity.UserSubscriptionRule.builder().id(1L).build()
        ));
        when(userSubscriptionRuleService.listEnabledRulesByUserId(2L)).thenReturn(List.of(
                com.pingyu.infodiet.model.entity.UserSubscriptionRule.builder().id(2L).build(),
                com.pingyu.infodiet.model.entity.UserSubscriptionRule.builder().id(3L).build()
        ));
        when(userSourceSubscriptionService.listEnabledSourceSubscriptions()).thenReturn(List.of(
                UserSourceSubscription.builder().platform("youtube").sourceType("channel").build(),
                UserSourceSubscription.builder().platform("github").sourceType("repo").build(),
                UserSourceSubscription.builder().platform("github").sourceType("author").build()
        ));

        ReflectionTestUtils.setField(service, "userProfileService", userProfileService);
        ReflectionTestUtils.setField(service, "userKeywordSubscriptionService", userKeywordSubscriptionService);
        ReflectionTestUtils.setField(service, "userSubscriptionRuleService", userSubscriptionRuleService);
        ReflectionTestUtils.setField(service, "userSourceSubscriptionService", userSourceSubscriptionService);

        AdminSubscriptionOverviewVO result = service.getSubscriptionOverview();

        assertEquals(3, result.getKeywordCount());
        assertEquals(3, result.getRuleCount());
        assertEquals(3, result.getSourceCount());
        assertEquals(2, result.getEnabledUserCount());
        assertEquals(1, result.getYoutubeSourceCount());
        assertEquals(2, result.getGithubSourceCount());
        assertEquals(1, result.getChannelSourceCount());
        assertEquals(1, result.getRepoSourceCount());
        assertEquals(1, result.getAuthorSourceCount());
    }
}
