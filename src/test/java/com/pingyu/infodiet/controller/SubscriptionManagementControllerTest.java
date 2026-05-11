package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.model.dto.user.UserSubscriptionRuleRequest;
import com.pingyu.infodiet.model.entity.UserSubscriptionRule;
import com.pingyu.infodiet.service.SubscriptionMatchService;
import com.pingyu.infodiet.service.UserSubscriptionRuleService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class SubscriptionManagementControllerTest {

    @Test
    void updateRuleShouldReturnSuccess() {
        UserSubscriptionRuleService userSubscriptionRuleService = Mockito.mock(UserSubscriptionRuleService.class);
        when(userSubscriptionRuleService.updateRule(any(UserSubscriptionRule.class))).thenReturn(true);

        SubscriptionManagementController controller = new SubscriptionManagementController();
        ReflectionTestUtils.setField(controller, "userSubscriptionRuleService", userSubscriptionRuleService);

        UserSubscriptionRuleRequest request = new UserSubscriptionRuleRequest();
        request.setId(1L);
        request.setUserId(1L);
        request.setRuleType("author");
        request.setRuleValue("Google for Developers");
        request.setRuleWeight(8);

        BaseResponse<Boolean> response = controller.updateRule(request);

        assertEquals(0, response.getCode());
        assertEquals(true, response.getData());
    }

    @Test
    void previewMatchShouldReturnDetailedResult() {
        SubscriptionMatchService subscriptionMatchService = Mockito.mock(SubscriptionMatchService.class);
        when(subscriptionMatchService.matchEnabledUsersWithDetails()).thenReturn(Map.of(
                1L, List.of(new SubscriptionMatchService.MatchDetail(
                        com.pingyu.infodiet.model.entity.ContentItem.builder().id(101L).title("agent workflow").build(),
                        8,
                        List.of("author:Google for Developers", "keyword_include:agent")
                ))
        ));

        SubscriptionManagementController controller = new SubscriptionManagementController();
        ReflectionTestUtils.setField(controller, "subscriptionMatchService", subscriptionMatchService);

        BaseResponse<Map<Long, List<SubscriptionMatchService.MatchDetail>>> response = controller.previewMatch();

        assertEquals(0, response.getCode());
        assertEquals(1, response.getData().size());
        assertEquals(8, response.getData().get(1L).getFirst().getScore());
    }
}
