package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.model.dto.user.UserSubscriptionRuleRequest;
import com.pingyu.infodiet.model.entity.UserSubscriptionRule;
import com.pingyu.infodiet.service.UserSubscriptionRuleService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class UserSubscriptionRuleControllerTest {

    @Test
    void addRuleShouldReturnSuccess() {
        UserSubscriptionRuleService userSubscriptionRuleService = Mockito.mock(UserSubscriptionRuleService.class);
        when(userSubscriptionRuleService.addRule(any(UserSubscriptionRule.class))).thenReturn(true);

        UserSubscriptionRuleController controller = new UserSubscriptionRuleController();
        ReflectionTestUtils.setField(controller, "userSubscriptionRuleService", userSubscriptionRuleService);

        UserSubscriptionRuleRequest request = new UserSubscriptionRuleRequest();
        request.setUserId(1L);
        request.setRuleType("author");
        request.setRuleValue("Google for Developers");
        request.setRuleWeight(5);

        BaseResponse<Boolean> response = controller.addRule(request);

        assertEquals(0, response.getCode());
        assertEquals(true, response.getData());
    }

    @Test
    void listEnabledRulesByUserIdShouldReturnRules() {
        UserSubscriptionRuleService userSubscriptionRuleService = Mockito.mock(UserSubscriptionRuleService.class);
        when(userSubscriptionRuleService.listEnabledRulesByUserId(1L)).thenReturn(List.of(
                UserSubscriptionRule.builder().id(1L).userId(1L).ruleType("author").ruleValue("Google for Developers").build()
        ));

        UserSubscriptionRuleController controller = new UserSubscriptionRuleController();
        ReflectionTestUtils.setField(controller, "userSubscriptionRuleService", userSubscriptionRuleService);

        BaseResponse<List<UserSubscriptionRule>> response = controller.listEnabledRulesByUserId(1L);

        assertEquals(0, response.getCode());
        assertEquals(1, response.getData().size());
        assertEquals("author", response.getData().getFirst().getRuleType());
    }
}
