package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.model.entity.UserSourceSubscription;
import com.pingyu.infodiet.service.UserSourceSubscriptionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class UserSourceSubscriptionControllerTest {

    @Test
    void addSourceSubscriptionShouldReturnSuccess() {
        UserSourceSubscriptionService userSourceSubscriptionService = Mockito.mock(UserSourceSubscriptionService.class);
        when(userSourceSubscriptionService.addSourceSubscription(any(UserSourceSubscription.class))).thenReturn(true);

        UserSourceSubscriptionController controller = new UserSourceSubscriptionController();
        ReflectionTestUtils.setField(controller, "userSourceSubscriptionService", userSourceSubscriptionService);

        BaseResponse<Boolean> response = controller.addSourceSubscription(UserSourceSubscription.builder()
                .userId(1L)
                .platform("youtube")
                .sourceType("channel")
                .sourceValue("UC123456")
                .build());

        assertEquals(0, response.getCode());
        assertEquals(true, response.getData());
    }

    @Test
    void listEnabledSourceSubscriptionsShouldReturnList() {
        UserSourceSubscriptionService userSourceSubscriptionService = Mockito.mock(UserSourceSubscriptionService.class);
        when(userSourceSubscriptionService.listEnabledSourceSubscriptions()).thenReturn(List.of(
                UserSourceSubscription.builder().id(1L).platform("youtube").sourceType("channel").sourceValue("UC123456").build()
        ));

        UserSourceSubscriptionController controller = new UserSourceSubscriptionController();
        ReflectionTestUtils.setField(controller, "userSourceSubscriptionService", userSourceSubscriptionService);

        BaseResponse<List<UserSourceSubscription>> response = controller.listEnabledSourceSubscriptions();

        assertEquals(0, response.getCode());
        assertEquals(1, response.getData().size());
        assertEquals("youtube", response.getData().getFirst().getPlatform());
    }

    @Test
    void listSourceSubscriptionsByUserIdShouldReturnList() {
        UserSourceSubscriptionService userSourceSubscriptionService = Mockito.mock(UserSourceSubscriptionService.class);
        when(userSourceSubscriptionService.listSourceSubscriptionsByUserId(1L)).thenReturn(List.of(
                UserSourceSubscription.builder().id(1L).userId(1L).platform("youtube").sourceType("channel").sourceValue("UC123456").build()
        ));

        UserSourceSubscriptionController controller = new UserSourceSubscriptionController();
        ReflectionTestUtils.setField(controller, "userSourceSubscriptionService", userSourceSubscriptionService);

        BaseResponse<List<UserSourceSubscription>> response = controller.listSourceSubscriptionsByUserId(1L);

        assertEquals(0, response.getCode());
        assertEquals(1, response.getData().size());
        assertEquals(1L, response.getData().getFirst().getUserId());
    }
}
