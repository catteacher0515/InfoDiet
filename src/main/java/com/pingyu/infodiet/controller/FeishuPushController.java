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
}
