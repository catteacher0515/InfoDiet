package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.common.PageResponse;
import com.pingyu.infodiet.model.entity.CrawlTaskLog;
import com.pingyu.infodiet.service.CrawlTaskLogService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class CrawlTaskLogControllerQueryTest {

    @Test
    void pageRecentLogsShouldReturnPageResponse() {
        CrawlTaskLogService crawlTaskLogService = Mockito.mock(CrawlTaskLogService.class);
        when(crawlTaskLogService.pageRecentLogs("github", 2, 1, 5)).thenReturn(
                new PageResponse<>(1, 1, 5, List.of(CrawlTaskLog.builder().id(2L).taskType("github_daily_flow").build()))
        );

        CrawlTaskLogController controller = new CrawlTaskLogController();
        ReflectionTestUtils.setField(controller, "crawlTaskLogService", crawlTaskLogService);

        BaseResponse<PageResponse<CrawlTaskLog>> response = controller.pageRecentLogs("github", 2, 1, 5);

        assertEquals(0, response.getCode());
        assertEquals(1, response.getData().getTotalCount());
        assertEquals(2L, response.getData().getRecords().getFirst().getId());
    }

    @Test
    void getTaskLogDetailShouldReturnSingleLog() {
        CrawlTaskLogService crawlTaskLogService = Mockito.mock(CrawlTaskLogService.class);
        when(crawlTaskLogService.getTaskLogDetail(8L)).thenReturn(CrawlTaskLog.builder().id(8L).taskType("youtube_source_push_flow").build());

        CrawlTaskLogController controller = new CrawlTaskLogController();
        ReflectionTestUtils.setField(controller, "crawlTaskLogService", crawlTaskLogService);

        BaseResponse<CrawlTaskLog> response = controller.getTaskLogDetail(8L);

        assertEquals(0, response.getCode());
        assertEquals("youtube_source_push_flow", response.getData().getTaskType());
    }
}
