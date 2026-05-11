package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.service.UserContentPushService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class UserContentPushControllerTest {

    @Test
    void createPendingPushesShouldReturnCreateSummary() {
        UserContentPushService userContentPushService = Mockito.mock(UserContentPushService.class);
        UserContentPushService.CreatePushResult createPushResult =
                new UserContentPushService.CreatePushResult(6, 4, 2);
        when(userContentPushService.createPendingPushes()).thenReturn(createPushResult);

        UserContentPushController controller = new UserContentPushController();
        ReflectionTestUtils.setField(controller, "userContentPushService", userContentPushService);

        BaseResponse<UserContentPushService.CreatePushResult> response = controller.createPendingPushes();

        assertEquals(0, response.getCode());
        assertEquals(6, response.getData().getTotalCount());
        assertEquals(4, response.getData().getCreatedCount());
        assertEquals(2, response.getData().getSkippedCount());
    }
}
