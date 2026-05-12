package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.entity.CrawlTaskLog;
import com.pingyu.infodiet.service.impl.CrawlTaskLogServiceImpl;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CrawlTaskLogServiceTest {

    @Test
    void buildSuccessLogShouldPopulateSummaryFields() {
        InMemoryCrawlTaskLogService service = new InMemoryCrawlTaskLogService();
        LocalDateTime startTime = LocalDateTime.of(2026, 5, 12, 10, 0, 0);
        service.fixedNow = startTime.plusSeconds(8);

        CrawlTaskLog taskLog = service.buildSuccessLog(
                "github_daily_flow",
                "manual",
                startTime,
                0,
                15,
                8,
                7,
                4,
                11,
                4,
                0
        );

        assertEquals("github_daily_flow", taskLog.getTaskType());
        assertEquals("manual", taskLog.getTriggerSource());
        assertEquals(1, taskLog.getTaskStatus());
        assertEquals(15, taskLog.getCrawlCount());
        assertEquals(8, taskLog.getSavedCount());
        assertEquals(7, taskLog.getSkippedCount());
        assertEquals(4, taskLog.getMatchedCount());
        assertEquals(11, taskLog.getUnmatchedCount());
        assertEquals(4, taskLog.getEnqueuedCount());
        assertEquals(0, taskLog.getEnqueueSkippedCount());
        assertEquals(8000L, taskLog.getDurationMs());
        assertNotNull(taskLog.getEndTime());
    }

    @Test
    void buildFailedLogShouldPopulateErrorFields() {
        InMemoryCrawlTaskLogService service = new InMemoryCrawlTaskLogService();
        LocalDateTime startTime = LocalDateTime.of(2026, 5, 12, 10, 0, 0);
        service.fixedNow = startTime.plusSeconds(3);

        CrawlTaskLog taskLog = service.buildFailedLog(
                "youtube_source_push_flow",
                "scheduler",
                startTime,
                new IllegalStateException("推送失败")
        );

        assertEquals("youtube_source_push_flow", taskLog.getTaskType());
        assertEquals("scheduler", taskLog.getTriggerSource());
        assertEquals(2, taskLog.getTaskStatus());
        assertEquals("推送失败", taskLog.getErrorMessage());
        assertEquals(3000L, taskLog.getDurationMs());
        assertNotNull(taskLog.getEndTime());
    }

    private static class InMemoryCrawlTaskLogService extends CrawlTaskLogServiceImpl {

        private final List<CrawlTaskLog> savedItems = new ArrayList<>();
        private LocalDateTime fixedNow = LocalDateTime.now();

        @Override
        public boolean save(CrawlTaskLog entity) {
            savedItems.add(entity);
            return true;
        }

        @Override
        protected LocalDateTime now() {
            return fixedNow;
        }
    }
}
