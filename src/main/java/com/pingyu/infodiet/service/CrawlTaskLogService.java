package com.pingyu.infodiet.service;

import com.mybatisflex.core.service.IService;
import com.pingyu.infodiet.common.PageResponse;
import com.pingyu.infodiet.model.entity.CrawlTaskLog;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 采集任务日志服务
 */
public interface CrawlTaskLogService extends IService<CrawlTaskLog> {

    /**
     * 构建成功任务日志
     */
    CrawlTaskLog buildSuccessLog(
            String taskType,
            String triggerSource,
            LocalDateTime startTime,
            int totalSourceCount,
            int crawlCount,
            int savedCount,
            int skippedCount,
            int matchedCount,
            int unmatchedCount,
            int enqueuedCount,
            int enqueueSkippedCount
    );

    /**
     * 构建失败任务日志
     */
    CrawlTaskLog buildFailedLog(String taskType, String triggerSource, LocalDateTime startTime, Throwable throwable);

    /**
     * 查询最近任务日志
     */
    List<CrawlTaskLog> listRecentLogs(int limit);

    /**
     * 分页查询任务日志
     */
    PageResponse<CrawlTaskLog> pageRecentLogs(String taskTypeKeyword, Integer taskStatus, int pageNum, int pageSize);

    /**
     * 查询任务日志详情
     */
    CrawlTaskLog getTaskLogDetail(Long taskLogId);
}
