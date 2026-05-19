package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.model.dto.user.UserPushConfigRequest;
import com.pingyu.infodiet.model.dto.user.UserPushConfigVO;
import com.pingyu.infodiet.service.WorkspaceService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class WorkspacePushConfigControllerTest {

    @Test
    void getMyPushConfigShouldReturnCurrentUserConfig() {
        WorkspaceService workspaceService = Mockito.mock(WorkspaceService.class);
        when(workspaceService.getMyPushConfig()).thenReturn(UserPushConfigVO.builder()
                .feishuUserId("ou_123")
                .pushChannel("feishu")
                .dailyPushLimit(8)
                .pushCooldownHours(2)
                .build());

        WorkspaceController controller = new WorkspaceController();
        ReflectionTestUtils.setField(controller, "workspaceService", workspaceService);

        BaseResponse<UserPushConfigVO> response = controller.getMyPushConfig();

        assertEquals(0, response.getCode());
        assertEquals("feishu", response.getData().getPushChannel());
        assertEquals(8, response.getData().getDailyPushLimit());
    }

    @Test
    void updateMyPushConfigShouldReturnSuccess() {
        WorkspaceService workspaceService = Mockito.mock(WorkspaceService.class);
        when(workspaceService.updateMyPushConfig(Mockito.any(UserPushConfigRequest.class))).thenReturn(true);

        WorkspaceController controller = new WorkspaceController();
        ReflectionTestUtils.setField(controller, "workspaceService", workspaceService);

        UserPushConfigRequest request = new UserPushConfigRequest();
        request.setFeishuUserId("ou_456");
        request.setPushChannel("feishu");
        request.setDailyPushLimit(5);
        request.setPushCooldownHours(0);
        BaseResponse<Boolean> response = controller.updateMyPushConfig(request);

        assertEquals(0, response.getCode());
        assertEquals(true, response.getData());
    }
}
