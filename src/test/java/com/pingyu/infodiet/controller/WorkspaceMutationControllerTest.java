package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.common.DeleteRequest;
import com.pingyu.infodiet.model.dto.content.UnifiedContentItemDTO;
import com.pingyu.infodiet.model.dto.content.WorkspaceContentQueryRequest;
import com.pingyu.infodiet.model.dto.user.UserKeywordSubscriptionRequest;
import com.pingyu.infodiet.model.dto.user.UserSubscriptionRuleRequest;
import com.pingyu.infodiet.model.entity.UserSourceSubscription;
import com.pingyu.infodiet.service.WorkspaceService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class WorkspaceMutationControllerTest {

    @Test
    void addMyKeywordShouldReturnSuccess() {
        WorkspaceService workspaceService = Mockito.mock(WorkspaceService.class);
        when(workspaceService.addMyKeyword("agent")).thenReturn(true);

        WorkspaceController controller = new WorkspaceController();
        ReflectionTestUtils.setField(controller, "workspaceService", workspaceService);

        UserKeywordSubscriptionRequest request = new UserKeywordSubscriptionRequest();
        request.setKeyword("agent");
        BaseResponse<Boolean> response = controller.addMyKeyword(request);

        assertEquals(0, response.getCode());
        assertEquals(true, response.getData());
    }

    @Test
    void addMyRuleShouldReturnSuccess() {
        WorkspaceService workspaceService = Mockito.mock(WorkspaceService.class);
        when(workspaceService.addMyRule(Mockito.any())).thenReturn(true);

        WorkspaceController controller = new WorkspaceController();
        ReflectionTestUtils.setField(controller, "workspaceService", workspaceService);

        UserSubscriptionRuleRequest request = new UserSubscriptionRuleRequest();
        request.setRuleType("author");
        request.setRuleValue("openai");
        request.setRuleWeight(5);
        BaseResponse<Boolean> response = controller.addMyRule(request);

        assertEquals(0, response.getCode());
        assertEquals(true, response.getData());
    }

    @Test
    void addMySourceShouldReturnSuccess() {
        WorkspaceService workspaceService = Mockito.mock(WorkspaceService.class);
        when(workspaceService.addMySource(Mockito.any())).thenReturn(true);

        WorkspaceController controller = new WorkspaceController();
        ReflectionTestUtils.setField(controller, "workspaceService", workspaceService);

        BaseResponse<Boolean> response = controller.addMySource(UserSourceSubscription.builder()
                .platform("youtube")
                .sourceType("channel")
                .sourceValue("UC100")
                .build());

        assertEquals(0, response.getCode());
        assertEquals(true, response.getData());
    }

    @Test
    void removeMySourceShouldReturnSuccess() {
        WorkspaceService workspaceService = Mockito.mock(WorkspaceService.class);
        when(workspaceService.removeMySource(100L)).thenReturn(true);

        WorkspaceController controller = new WorkspaceController();
        ReflectionTestUtils.setField(controller, "workspaceService", workspaceService);

        DeleteRequest request = new DeleteRequest();
        request.setId(100L);
        BaseResponse<Boolean> response = controller.removeMySource(request);

        assertEquals(0, response.getCode());
        assertEquals(true, response.getData());
    }

    @Test
    void listMyContentItemsShouldReturnUnifiedContentList() {
        WorkspaceService workspaceService = Mockito.mock(WorkspaceService.class);
        when(workspaceService.listMyContentItems(Mockito.any())).thenReturn(List.of(
                UnifiedContentItemDTO.builder().id(1L).platform("github").title("agent repo").contentType("repository").build()
        ));

        WorkspaceController controller = new WorkspaceController();
        ReflectionTestUtils.setField(controller, "workspaceService", workspaceService);

        BaseResponse<List<UnifiedContentItemDTO>> response = controller.listMyContentItems(new WorkspaceContentQueryRequest());

        assertEquals(0, response.getCode());
        assertEquals(1, response.getData().size());
        assertEquals("github", response.getData().getFirst().getPlatform());
    }
}
