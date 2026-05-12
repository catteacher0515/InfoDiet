package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.service.PushQueueService;
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
                new UserContentPushService.CreatePushResult(6, 4, 2, 1, 1, 0);
        when(userContentPushService.createPendingPushes()).thenReturn(createPushResult);

        UserContentPushController controller = new UserContentPushController();
        ReflectionTestUtils.setField(controller, "userContentPushService", userContentPushService);

        BaseResponse<UserContentPushService.CreatePushResult> response = controller.createPendingPushes();

        assertEquals(0, response.getCode());
        assertEquals(6, response.getData().getTotalCount());
        assertEquals(4, response.getData().getCreatedCount());
        assertEquals(2, response.getData().getSkippedCount());
        assertEquals(1, response.getData().getSkippedByExistingCount());
        assertEquals(1, response.getData().getSkippedByLimitCount());
        assertEquals(0, response.getData().getSkippedByCooldownCount());
    }

    @Test
    void enqueuePendingPushesShouldReturnEnqueueSummary() {
        PushQueueService pushQueueService = Mockito.mock(PushQueueService.class);
        PushQueueService.EnqueuePushResult enqueuePushResult = new PushQueueService.EnqueuePushResult(4, 4, 0);
        when(pushQueueService.enqueuePendingPushes("feishu")).thenReturn(enqueuePushResult);

        UserContentPushController controller = new UserContentPushController();
        ReflectionTestUtils.setField(controller, "pushQueueService", pushQueueService);

        BaseResponse<PushQueueService.EnqueuePushResult> response = controller.enqueuePendingPushes();

        assertEquals(0, response.getCode());
        assertEquals(4, response.getData().totalCount());
        assertEquals(4, response.getData().enqueuedCount());
        assertEquals(0, response.getData().skippedCount());
    }
}
