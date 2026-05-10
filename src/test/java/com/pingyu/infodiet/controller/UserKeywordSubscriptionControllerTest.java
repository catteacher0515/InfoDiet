package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.model.dto.user.UserKeywordSubscriptionRequest;
import com.pingyu.infodiet.model.entity.UserKeywordSubscription;
import com.pingyu.infodiet.service.UserKeywordSubscriptionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class UserKeywordSubscriptionControllerTest {

    /**
     * 测试添加关键词订阅
     */
    @Test
    void addKeywordShouldReturnSuccess() {
        UserKeywordSubscriptionService userKeywordSubscriptionService = Mockito.mock(UserKeywordSubscriptionService.class);
        when(userKeywordSubscriptionService.addKeyword(1L, "agent")).thenReturn(true);

        UserKeywordSubscriptionController controller = new UserKeywordSubscriptionController();
        ReflectionTestUtils.setField(controller, "userKeywordSubscriptionService", userKeywordSubscriptionService);

        UserKeywordSubscriptionRequest request = new UserKeywordSubscriptionRequest();
        request.setUserId(1L);
        request.setKeyword("agent");

        BaseResponse<Boolean> response = controller.addKeyword(request);

        assertEquals(0, response.getCode());
        assertEquals(true, response.getData());
    }

    /**
     * 测试查询启用订阅列表
     */
    @Test
    void listEnabledSubscriptionsByUserIdShouldReturnSubscriptions() {
        UserKeywordSubscriptionService userKeywordSubscriptionService = Mockito.mock(UserKeywordSubscriptionService.class);
        when(userKeywordSubscriptionService.listEnabledSubscriptionsByUserId(1L)).thenReturn(List.of(
                UserKeywordSubscription.builder().id(1L).userId(1L).keyword("agent").build()
        ));

        UserKeywordSubscriptionController controller = new UserKeywordSubscriptionController();
        ReflectionTestUtils.setField(controller, "userKeywordSubscriptionService", userKeywordSubscriptionService);

        BaseResponse<List<UserKeywordSubscription>> response = controller.listEnabledSubscriptionsByUserId(1L);

        assertEquals(0, response.getCode());
        assertEquals(1, response.getData().size());
        assertEquals("agent", response.getData().getFirst().getKeyword());
    }
}
