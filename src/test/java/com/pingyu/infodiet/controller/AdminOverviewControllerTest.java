package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.model.dto.dashboard.AdminSubscriptionOverviewVO;
import com.pingyu.infodiet.service.AdminOverviewService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class AdminOverviewControllerTest {

    @Test
    void getSubscriptionOverviewShouldReturnSuccess() {
        AdminOverviewService adminOverviewService = Mockito.mock(AdminOverviewService.class);
        when(adminOverviewService.getSubscriptionOverview()).thenReturn(AdminSubscriptionOverviewVO.builder()
                .keywordCount(3)
                .ruleCount(2)
                .sourceCount(4)
                .enabledUserCount(2)
                .build());

        AdminOverviewController controller = new AdminOverviewController();
        ReflectionTestUtils.setField(controller, "adminOverviewService", adminOverviewService);

        BaseResponse<AdminSubscriptionOverviewVO> response = controller.getSubscriptionOverview();

        assertEquals(0, response.getCode());
        assertEquals(3, response.getData().getKeywordCount());
        assertEquals(4, response.getData().getSourceCount());
    }
}
