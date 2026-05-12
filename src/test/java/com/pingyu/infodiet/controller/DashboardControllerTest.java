package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.model.dto.dashboard.AdminDashboardVO;
import com.pingyu.infodiet.model.dto.dashboard.OpsDashboardVO;
import com.pingyu.infodiet.model.dto.dashboard.WorkspaceDashboardVO;
import com.pingyu.infodiet.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class DashboardControllerTest {

    @Test
    void getWorkspaceDashboardShouldReturnSummary() {
        DashboardService dashboardService = Mockito.mock(DashboardService.class);
        when(dashboardService.getWorkspaceDashboard()).thenReturn(WorkspaceDashboardVO.builder()
                .keywordCount(3)
                .sourceCount(2)
                .totalPushCount(5)
                .successPushCount(4)
                .failedPushCount(1)
                .build());

        DashboardController controller = new DashboardController();
        ReflectionTestUtils.setField(controller, "dashboardService", dashboardService);

        BaseResponse<WorkspaceDashboardVO> response = controller.getWorkspaceDashboard();

        assertEquals(0, response.getCode());
        assertEquals(3, response.getData().getKeywordCount());
    }

    @Test
    void getAdminDashboardShouldReturnSummary() {
        DashboardService dashboardService = Mockito.mock(DashboardService.class);
        when(dashboardService.getAdminDashboard()).thenReturn(AdminDashboardVO.builder()
                .userCount(8)
                .enabledUserCount(6)
                .keywordSubscriptionCount(20)
                .sourceSubscriptionCount(12)
                .build());

        DashboardController controller = new DashboardController();
        ReflectionTestUtils.setField(controller, "dashboardService", dashboardService);

        BaseResponse<AdminDashboardVO> response = controller.getAdminDashboard();

        assertEquals(0, response.getCode());
        assertEquals(8, response.getData().getUserCount());
    }

    @Test
    void getOpsDashboardShouldReturnSummary() {
        DashboardService dashboardService = Mockito.mock(DashboardService.class);
        when(dashboardService.getOpsDashboard()).thenReturn(OpsDashboardVO.builder()
                .recentTaskCount(5)
                .pendingAlertCount(2)
                .failedPushCount(3)
                .build());

        DashboardController controller = new DashboardController();
        ReflectionTestUtils.setField(controller, "dashboardService", dashboardService);

        BaseResponse<OpsDashboardVO> response = controller.getOpsDashboard();

        assertEquals(0, response.getCode());
        assertEquals(3, response.getData().getFailedPushCount());
    }
}
