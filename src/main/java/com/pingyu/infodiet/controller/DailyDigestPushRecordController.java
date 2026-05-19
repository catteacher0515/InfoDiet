package com.pingyu.infodiet.controller;

import cn.hutool.core.util.StrUtil;
import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.common.PageResponse;
import com.pingyu.infodiet.common.ResultUtils;
import com.pingyu.infodiet.model.entity.DailyDigestPushRecord;
import com.pingyu.infodiet.service.DailyDigestPushRecordService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * AI 日报推送记录接口
 */
@RestController
@RequestMapping("/daily-digest-push-record")
public class DailyDigestPushRecordController {

    @Resource
    private DailyDigestPushRecordService dailyDigestPushRecordService;

    /**
     * 查询最近失败记录
     */
    @GetMapping("/failed/recent")
    public BaseResponse<List<DailyDigestPushRecord>> listRecentFailedRecords(@RequestParam(defaultValue = "10") int limit) {
        return ResultUtils.success(dailyDigestPushRecordService.listRecentFailedRecords(limit));
    }

    /**
     * 分页查询推送记录
     */
    @GetMapping("/page")
    public BaseResponse<PageResponse<DailyDigestPushRecord>> pagePushRecords(
            @RequestParam(required = false) Integer pushStatus,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String digestDate,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return ResultUtils.success(dailyDigestPushRecordService.pagePushRecords(
                pushStatus,
                keyword,
                StrUtil.isBlank(digestDate) ? null : LocalDate.parse(digestDate),
                pageNum,
                pageSize
        ));
    }
}
