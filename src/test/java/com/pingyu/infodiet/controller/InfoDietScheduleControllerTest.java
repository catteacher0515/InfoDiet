package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.service.InfoDietScheduleService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class InfoDietScheduleControllerTest {

    @Test
    void runDailyGithubFlowShouldReturnScheduleSummary() {
        InfoDietScheduleService infoDietScheduleService = Mockito.mock(InfoDietScheduleService.class);
        InfoDietScheduleService.ScheduleResult scheduleResult = new InfoDietScheduleService.ScheduleResult(
                15, 10, 5, 6, 9, 6, 0
        );
        when(infoDietScheduleService.runDailyGithubFlow()).thenReturn(scheduleResult);

        InfoDietScheduleController controller = new InfoDietScheduleController();
        ReflectionTestUtils.setField(controller, "infoDietScheduleService", infoDietScheduleService);

        BaseResponse<InfoDietScheduleService.ScheduleResult> response = controller.runDailyGithubFlow();

        assertEquals(0, response.getCode());
        assertEquals(15, response.getData().getCrawlCount());
        assertEquals(10, response.getData().getSavedCount());
        assertEquals(5, response.getData().getSkippedCount());
        assertEquals(6, response.getData().getMatchedCount());
        assertEquals(9, response.getData().getUnmatchedCount());
        assertEquals(6, response.getData().getPushSuccessCount());
        assertEquals(0, response.getData().getPushFailedCount());
    }
}
