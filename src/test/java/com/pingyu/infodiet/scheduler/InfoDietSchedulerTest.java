package com.pingyu.infodiet.scheduler;

import com.pingyu.infodiet.service.InfoDietScheduleService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InfoDietSchedulerTest {

    @Test
    void runDailyGithubFlowShouldInvokeScheduleService() {
        InfoDietScheduleService infoDietScheduleService = Mockito.mock(InfoDietScheduleService.class);
        when(infoDietScheduleService.runDailyGithubFlow()).thenReturn(
                new InfoDietScheduleService.ScheduleResult(15, 6, 9, 4, 11, 4, 0)
        );

        InfoDietScheduler scheduler = new InfoDietScheduler();
        ReflectionTestUtils.setField(scheduler, "infoDietScheduleService", infoDietScheduleService);

        scheduler.runDailyGithubFlow();

        verify(infoDietScheduleService, times(1)).runDailyGithubFlow();
    }
}
