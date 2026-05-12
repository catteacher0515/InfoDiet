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
        assertEquals(6, response.getData().getEnqueuedCount());
        assertEquals(0, response.getData().getEnqueueSkippedCount());
    }

    @Test
    void runDailyYoutubeSourcePushFlowShouldReturnScheduleSummary() {
        InfoDietScheduleService infoDietScheduleService = Mockito.mock(InfoDietScheduleService.class);
        InfoDietScheduleService.YoutubeSourceScheduleResult scheduleResult =
                new InfoDietScheduleService.YoutubeSourceScheduleResult(2, 6, 4, 2, 3, 2, 3, 0);
        when(infoDietScheduleService.runDailyYoutubeSourcePushFlow()).thenReturn(scheduleResult);

        InfoDietScheduleController controller = new InfoDietScheduleController();
        ReflectionTestUtils.setField(controller, "infoDietScheduleService", infoDietScheduleService);

        BaseResponse<InfoDietScheduleService.YoutubeSourceScheduleResult> response =
                controller.runDailyYoutubeSourcePushFlow();

        assertEquals(0, response.getCode());
        assertEquals(2, response.getData().getSubscriptionCount());
        assertEquals(6, response.getData().getCrawlCount());
        assertEquals(4, response.getData().getSavedCount());
        assertEquals(2, response.getData().getSkippedCount());
        assertEquals(3, response.getData().getPendingPushCreatedCount());
        assertEquals(2, response.getData().getPendingPushSkippedCount());
        assertEquals(3, response.getData().getEnqueuedCount());
        assertEquals(0, response.getData().getEnqueueSkippedCount());
    }
}
