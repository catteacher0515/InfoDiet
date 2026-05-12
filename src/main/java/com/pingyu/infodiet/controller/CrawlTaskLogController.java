package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.common.ResultUtils;
import com.pingyu.infodiet.model.entity.CrawlTaskLog;
import com.pingyu.infodiet.service.CrawlTaskLogService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 采集任务日志接口
 */
@RestController
@RequestMapping("/crawl-task-log")
public class CrawlTaskLogController {

    @Resource
    private CrawlTaskLogService crawlTaskLogService;

    /**
     * 查询最近任务日志
     */
    @GetMapping("/recent")
    public BaseResponse<List<CrawlTaskLog>> listRecentLogs(@RequestParam(defaultValue = "10") int limit) {
        return ResultUtils.success(crawlTaskLogService.listRecentLogs(limit));
    }
}
