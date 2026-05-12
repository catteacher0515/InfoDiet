package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.model.dto.user.WorkspaceSubscriptionsVO;
import com.pingyu.infodiet.model.entity.UserContentPush;
import com.pingyu.infodiet.model.entity.UserSourceSubscription;
import com.pingyu.infodiet.model.entity.UserSubscriptionRule;
import com.pingyu.infodiet.service.WorkspaceService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class WorkspaceControllerTest {

    @Test
    void getMySubscriptionsShouldReturnWorkspaceSubscriptions() {
        WorkspaceService workspaceService = Mockito.mock(WorkspaceService.class);
        when(workspaceService.getMySubscriptions()).thenReturn(WorkspaceSubscriptionsVO.builder()
                .keywords(List.of("ai", "agent"))
                .rules(List.of(UserSubscriptionRule.builder().id(1L).ruleType("author").ruleValue("google").build()))
                .sources(List.of(UserSourceSubscription.builder().id(1L).platform("youtube").sourceType("channel").sourceValue("UC100").build()))
                .build());

        WorkspaceController controller = new WorkspaceController();
        ReflectionTestUtils.setField(controller, "workspaceService", workspaceService);

        BaseResponse<WorkspaceSubscriptionsVO> response = controller.getMySubscriptions();

        assertEquals(0, response.getCode());
        assertEquals(2, response.getData().getKeywords().size());
        assertEquals(1, response.getData().getSources().size());
    }

    @Test
    void listMyPushesShouldReturnPushList() {
        WorkspaceService workspaceService = Mockito.mock(WorkspaceService.class);
        when(workspaceService.listMyPushes()).thenReturn(List.of(
                UserContentPush.builder().id(1L).userId(1L).contentItemId(10L).pushStatus(1).build()
        ));

        WorkspaceController controller = new WorkspaceController();
        ReflectionTestUtils.setField(controller, "workspaceService", workspaceService);

        BaseResponse<List<UserContentPush>> response = controller.listMyPushes();

        assertEquals(0, response.getCode());
        assertEquals(1, response.getData().size());
        assertEquals(10L, response.getData().getFirst().getContentItemId());
    }
}
