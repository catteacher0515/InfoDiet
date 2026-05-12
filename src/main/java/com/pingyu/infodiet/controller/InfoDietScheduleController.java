package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.common.ResultUtils;
import com.pingyu.infodiet.service.InfoDietScheduleService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 信息节食调度接口
 */
@RestController
@RequestMapping("/schedule/github")
public class InfoDietScheduleController {

    @Resource
    private InfoDietScheduleService infoDietScheduleService;

    /**
     * 手动触发每日 GitHub 流程
     */
    @PostMapping("/daily/run")
    public BaseResponse<InfoDietScheduleService.ScheduleResult> runDailyGithubFlow() {
        return ResultUtils.success(infoDietScheduleService.runDailyGithubFlow());
    }

    /**
     * 手动触发每日 YouTube 订阅源推送流程
     */
    @PostMapping("/youtube/source/daily/run")
    public BaseResponse<InfoDietScheduleService.YoutubeSourceScheduleResult> runDailyYoutubeSourcePushFlow() {
        return ResultUtils.success(infoDietScheduleService.runDailyYoutubeSourcePushFlow());
    }

    /**
     * 按任务类型重跑
     */
    @PostMapping("/rerun")
    public BaseResponse<Object> rerunTask(@RequestParam String taskType) {
        return ResultUtils.success(infoDietScheduleService.rerunTask(taskType));
    }
}
