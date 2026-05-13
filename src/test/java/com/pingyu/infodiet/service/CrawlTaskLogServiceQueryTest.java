package com.pingyu.infodiet.service;

import com.pingyu.infodiet.common.PageResponse;
import com.pingyu.infodiet.model.entity.CrawlTaskLog;
import com.pingyu.infodiet.service.impl.CrawlTaskLogServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CrawlTaskLogServiceQueryTest {

    @Test
    void pageRecentLogsShouldFilterByTaskTypeAndStatus() {
        InMemoryCrawlTaskLogService service = new InMemoryCrawlTaskLogService();
        service.items.add(CrawlTaskLog.builder().id(1L).taskType("github_daily_flow").taskStatus(1).build());
        service.items.add(CrawlTaskLog.builder().id(2L).taskType("github_daily_flow").taskStatus(2).build());
        service.items.add(CrawlTaskLog.builder().id(3L).taskType("youtube_source_push_flow").taskStatus(2).build());

        PageResponse<CrawlTaskLog> response = service.pageRecentLogs("github", 2, 1, 10);

        assertEquals(1, response.getRecords().size());
        assertEquals(2L, response.getRecords().getFirst().getId());
        assertEquals(1, response.getTotalCount());
    }

    @Test
    void pageRecentLogsShouldRespectPageWindow() {
        InMemoryCrawlTaskLogService service = new InMemoryCrawlTaskLogService();
        service.items.add(CrawlTaskLog.builder().id(1L).taskType("task-1").taskStatus(1).build());
        service.items.add(CrawlTaskLog.builder().id(2L).taskType("task-2").taskStatus(1).build());
        service.items.add(CrawlTaskLog.builder().id(3L).taskType("task-3").taskStatus(1).build());

        PageResponse<CrawlTaskLog> response = service.pageRecentLogs(null, null, 2, 1);

        assertEquals(1, response.getRecords().size());
        assertEquals(2L, response.getRecords().getFirst().getId());
        assertEquals(3, response.getTotalCount());
        assertEquals(2, response.getPageNum());
        assertEquals(1, response.getPageSize());
    }

    @Test
    void getTaskLogDetailShouldReturnSingleLog() {
        InMemoryCrawlTaskLogService service = new InMemoryCrawlTaskLogService();
        service.items.add(CrawlTaskLog.builder().id(9L).taskType("github_daily_flow").taskStatus(2).errorMessage("boom").build());

        CrawlTaskLog log = service.getTaskLogDetail(9L);

        assertEquals("github_daily_flow", log.getTaskType());
        assertEquals("boom", log.getErrorMessage());
        assertNull(service.getTaskLogDetail(99L));
    }

    private static class InMemoryCrawlTaskLogService extends CrawlTaskLogServiceImpl {

        private final List<CrawlTaskLog> items = new ArrayList<>();

        @Override
        public List<CrawlTaskLog> list() {
            return items;
        }

        public CrawlTaskLog findById(Long id) {
            return items.stream()
                    .filter(item -> item.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public PageResponse<CrawlTaskLog> pageRecentLogs(String taskTypeKeyword, Integer taskStatus, int pageNum, int pageSize) {
            List<CrawlTaskLog> filtered = items.stream()
                    .filter(item -> taskTypeKeyword == null || item.getTaskType().contains(taskTypeKeyword))
                    .filter(item -> taskStatus == null || taskStatus.equals(item.getTaskStatus()))
                    .sorted(Comparator.comparing(CrawlTaskLog::getId).reversed())
                    .toList();
            int safePageNum = Math.max(pageNum, 1);
            int safePageSize = Math.max(pageSize, 1);
            int fromIndex = Math.min((safePageNum - 1) * safePageSize, filtered.size());
            int toIndex = Math.min(fromIndex + safePageSize, filtered.size());
            return new PageResponse<>(filtered.size(), safePageNum, safePageSize, filtered.subList(fromIndex, toIndex));
        }

        @Override
        public CrawlTaskLog getTaskLogDetail(Long taskLogId) {
            return findById(taskLogId);
        }
    }
}
