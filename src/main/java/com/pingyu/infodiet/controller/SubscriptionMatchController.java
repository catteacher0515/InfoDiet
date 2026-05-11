package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.common.ResultUtils;
import com.pingyu.infodiet.model.entity.ContentItem;
import com.pingyu.infodiet.service.SubscriptionMatchService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 订阅匹配接口
 */
@RestController
@RequestMapping("/subscription/match")
public class SubscriptionMatchController {

    @Resource
    private SubscriptionMatchService subscriptionMatchService;

    /**
     * 查询启用用户的匹配结果
     */
    @GetMapping("/enabled")
    public BaseResponse<Map<Long, List<ContentItem>>> matchEnabledUsers() {
        return ResultUtils.success(subscriptionMatchService.matchEnabledUsers());
    }
}
