package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.common.PageResponse;
import com.pingyu.infodiet.common.ResultUtils;
import com.pingyu.infodiet.model.dto.ops.FailedPushOverviewVO;
import com.pingyu.infodiet.model.entity.UserContentPush;
import com.pingyu.infodiet.service.PushQueueService;
import com.pingyu.infodiet.service.UserContentPushService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户内容推送接口
 */
@RestController
@RequestMapping("/user-content-push")
public class UserContentPushController {

    @Resource
    private UserContentPushService userContentPushService;

    @Resource
    private PushQueueService pushQueueService;

    /**
     * 生成待推送记录
     */
    @PostMapping("/create")
    public BaseResponse<UserContentPushService.CreatePushResult> createPendingPushes() {
        return ResultUtils.success(userContentPushService.createPendingPushes());
    }

    /**
     * 将待推送记录加入队列
     */
    @PostMapping("/enqueue")
    public BaseResponse<PushQueueService.EnqueuePushResult> enqueuePendingPushes() {
        return ResultUtils.success(pushQueueService.enqueuePendingPushes("feishu"));
    }

    /**
     * 重试失败推送
     */
    @PostMapping("/retry")
    public BaseResponse<Boolean> retryFailedPush(@RequestParam Long pushId) {
        return ResultUtils.success(userContentPushService.retryFailedPush(pushId));
    }

    /**
     * 查询失败推送列表
     */
    @GetMapping("/failed/list")
    public BaseResponse<List<UserContentPush>> listFailedPushes() {
        return ResultUtils.success(userContentPushService.listFailedPushesByChannel("feishu"));
    }

    /**
     * 分页查询失败推送
     */
    @GetMapping("/failed/page")
    public BaseResponse<PageResponse<UserContentPush>> pageFailedPushes(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer retryCount,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return ResultUtils.success(userContentPushService.pageFailedPushes("feishu", keyword, retryCount, pageNum, pageSize));
    }

    /**
     * 批量重试失败推送
     */
    @PostMapping("/failed/retry")
    public BaseResponse<UserContentPushService.BatchRetryResult> retryFailedPushes(@RequestBody List<Long> pushIdList) {
        return ResultUtils.success(userContentPushService.retryFailedPushes(pushIdList));
    }

    /**
     * 查询失败推送联动视图
     */
    @GetMapping("/failed/overview")
    public BaseResponse<FailedPushOverviewVO> getFailedPushOverview(@RequestParam Long pushId) {
        return ResultUtils.success(userContentPushService.getFailedPushOverview(pushId));
    }
}
