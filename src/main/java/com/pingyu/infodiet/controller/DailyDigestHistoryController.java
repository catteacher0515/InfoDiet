package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.common.ResultUtils;
import com.pingyu.infodiet.model.dto.content.DailyDigestDTO;
import com.pingyu.infodiet.service.DailyDigestHistoryService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * AI 日报历史接口
 */
@RestController
@RequestMapping("/daily-digest")
public class DailyDigestHistoryController {

    @Resource
    private DailyDigestHistoryService dailyDigestHistoryService;

    /**
     * 查询最近日报
     */
    @GetMapping("/recent")
    public BaseResponse<List<DailyDigestDTO>> listRecentDigests(@RequestParam(defaultValue = "7") int limit) {
        return ResultUtils.success(dailyDigestHistoryService.listRecentDigests(limit));
    }

    /**
     * 按日期查询日报详情
     */
    @GetMapping("/detail/{digestDate}")
    public BaseResponse<DailyDigestDTO> getDigestByDate(@PathVariable String digestDate) {
        return ResultUtils.success(dailyDigestHistoryService.getDigestDTOByDate(LocalDate.parse(digestDate)));
    }
}
