package com.pingyu.infodiet.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.pingyu.infodiet.mapper.CrawlTaskLogMapper;
import com.pingyu.infodiet.model.entity.CrawlTaskLog;
import com.pingyu.infodiet.service.CrawlTaskLogService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 采集任务日志服务实现
 */
@Service
public class CrawlTaskLogServiceImpl extends ServiceImpl<CrawlTaskLogMapper, CrawlTaskLog>
        implements CrawlTaskLogService {

    /**
     * 构建成功任务日志
     */
    @Override
    public CrawlTaskLog buildSuccessLog(
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
    ) {
        LocalDateTime endTime = now();
        return CrawlTaskLog.builder()
                .taskType(taskType)
                .triggerSource(StrUtil.blankToDefault(triggerSource, "system"))
                .taskStatus(1)
                .totalSourceCount(totalSourceCount)
                .crawlCount(crawlCount)
                .savedCount(savedCount)
                .skippedCount(skippedCount)
                .matchedCount(matchedCount)
                .unmatchedCount(unmatchedCount)
                .enqueuedCount(enqueuedCount)
                .enqueueSkippedCount(enqueueSkippedCount)
                .startTime(startTime)
                .endTime(endTime)
                .durationMs(Duration.between(startTime, endTime).toMillis())
                .build();
    }

    /**
     * 构建失败任务日志
     */
    @Override
    public CrawlTaskLog buildFailedLog(String taskType, String triggerSource, LocalDateTime startTime, Throwable throwable) {
        LocalDateTime endTime = now();
        return CrawlTaskLog.builder()
                .taskType(taskType)
                .triggerSource(StrUtil.blankToDefault(triggerSource, "system"))
                .taskStatus(2)
                .errorMessage(throwable == null ? "未知异常" : StrUtil.blankToDefault(throwable.getMessage(), "未知异常"))
                .startTime(startTime)
                .endTime(endTime)
                .durationMs(Duration.between(startTime, endTime).toMillis())
                .build();
    }

    /**
     * 查询最近任务日志
     */
    @Override
    public List<CrawlTaskLog> listRecentLogs(int limit) {
        int safeLimit = limit <= 0 ? 10 : Math.min(limit, 100);
        QueryWrapper queryWrapper = QueryWrapper.create()
                .orderBy("startTime", false)
                .limit(safeLimit);
        return this.list(queryWrapper);
    }

    /**
     * 获取当前时间
     */
    protected LocalDateTime now() {
        return LocalDateTime.now();
    }
}
