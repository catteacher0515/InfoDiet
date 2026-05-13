package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.common.PageResponse;
import com.pingyu.infodiet.model.dto.ops.FailedPushOverviewVO;
import com.pingyu.infodiet.model.entity.UserContentPush;
import com.pingyu.infodiet.service.PushQueueService;
import com.pingyu.infodiet.service.UserContentPushService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class UserContentPushControllerTest {

    @Test
    void pageFailedPushesShouldReturnPageResponse() {
        UserContentPushService userContentPushService = Mockito.mock(UserContentPushService.class);
        PushQueueService pushQueueService = Mockito.mock(PushQueueService.class);
        when(userContentPushService.pageFailedPushes("feishu", "token", 2, 1, 10)).thenReturn(
                new PageResponse<>(1, 1, 10, java.util.List.of(UserContentPush.builder().id(2L).failReason("token expired").build()))
        );

        UserContentPushController controller = new UserContentPushController();
        ReflectionTestUtils.setField(controller, "userContentPushService", userContentPushService);
        ReflectionTestUtils.setField(controller, "pushQueueService", pushQueueService);

        BaseResponse<PageResponse<UserContentPush>> response = controller.pageFailedPushes("token", 2, 1, 10);

        assertEquals(0, response.getCode());
        assertEquals(1, response.getData().getTotalCount());
        assertEquals(2L, response.getData().getRecords().getFirst().getId());
    }

    @Test
    void getFailedPushOverviewShouldReturnSuccess() {
        UserContentPushService userContentPushService = Mockito.mock(UserContentPushService.class);
        PushQueueService pushQueueService = Mockito.mock(PushQueueService.class);
        when(userContentPushService.getFailedPushOverview(1L)).thenReturn(FailedPushOverviewVO.builder()
                .push(UserContentPush.builder().id(1L).contentItemId(10L).build())
                .build());

        UserContentPushController controller = new UserContentPushController();
        ReflectionTestUtils.setField(controller, "userContentPushService", userContentPushService);
        ReflectionTestUtils.setField(controller, "pushQueueService", pushQueueService);

        BaseResponse<FailedPushOverviewVO> response = controller.getFailedPushOverview(1L);

        assertEquals(0, response.getCode());
        assertEquals(10L, response.getData().getPush().getContentItemId());
    }
}
