package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.service.FeishuPushService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class FeishuPushControllerTest {

    @Test
    void pushContentItemsToFeishuShouldReturnPushSummary() {
        FeishuPushService feishuPushService = Mockito.mock(FeishuPushService.class);
        FeishuPushService.PushResult pushResult = new FeishuPushService.PushResult(6, 4, 2);
        when(feishuPushService.pushContentItemsToFeishu()).thenReturn(pushResult);

        FeishuPushController controller = new FeishuPushController();
        ReflectionTestUtils.setField(controller, "feishuPushService", feishuPushService);

        BaseResponse<FeishuPushService.PushResult> response = controller.pushContentItemsToFeishu();

        assertEquals(0, response.getCode());
        assertEquals(6, response.getData().getTotalCount());
        assertEquals(4, response.getData().getSuccessCount());
        assertEquals(2, response.getData().getFailedCount());
    }
}
