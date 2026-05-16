package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.common.ResultUtils;
import com.pingyu.infodiet.service.FeishuPushService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 飞书推送接口
 */
@RestController
@RequestMapping("/feishu/push")
public class FeishuPushController {

    @Resource
    private FeishuPushService feishuPushService;

    /**
     * 手动推送内容到飞书
     */
    @PostMapping("/content")
    public BaseResponse<FeishuPushService.PushResult> pushContentItemsToFeishu() {
        return ResultUtils.success(feishuPushService.pushContentItemsToFeishu());
    }

    /**
     * 手动推送用户内容到飞书
     */
    @PostMapping("/user-content")
    public BaseResponse<FeishuPushService.PushResult> pushUserContentItemsToFeishu() {
        return ResultUtils.success(feishuPushService.pushUserContentItemsToFeishu());
    }

    /**
     * 手动推送今日日报到飞书
     */
    @PostMapping("/digest/today")
    public BaseResponse<FeishuPushService.PushResult> pushTodayDigestToFeishu() {
        return ResultUtils.success(feishuPushService.pushTodayDigestToFeishu());
    }
}
