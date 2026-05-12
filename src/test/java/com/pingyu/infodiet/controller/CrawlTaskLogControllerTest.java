package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.model.entity.CrawlTaskLog;
import com.pingyu.infodiet.service.CrawlTaskLogService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class CrawlTaskLogControllerTest {

    @Test
    void listRecentLogsShouldReturnTaskLogs() {
        CrawlTaskLogService crawlTaskLogService = Mockito.mock(CrawlTaskLogService.class);
        when(crawlTaskLogService.listRecentLogs(5)).thenReturn(List.of(
                CrawlTaskLog.builder().id(1L).taskType("github_daily_flow").taskStatus(1).build(),
                CrawlTaskLog.builder().id(2L).taskType("youtube_source_push_flow").taskStatus(2).build()
        ));

        CrawlTaskLogController controller = new CrawlTaskLogController();
        ReflectionTestUtils.setField(controller, "crawlTaskLogService", crawlTaskLogService);

        BaseResponse<List<CrawlTaskLog>> response = controller.listRecentLogs(5);

        assertEquals(0, response.getCode());
        assertEquals(2, response.getData().size());
        assertEquals("github_daily_flow", response.getData().getFirst().getTaskType());
        assertEquals(2, response.getData().getLast().getTaskStatus());
    }
}
