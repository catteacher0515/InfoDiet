package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.common.ResultUtils;
import com.pingyu.infodiet.model.entity.UserSourceSubscription;
import com.pingyu.infodiet.service.UserSourceSubscriptionService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户订阅源接口
 */
@RestController
@RequestMapping("/user/source")
public class UserSourceSubscriptionController {

    @Resource
    private UserSourceSubscriptionService userSourceSubscriptionService;

    /**
     * 添加订阅源
     */
    @PostMapping("/add")
    public BaseResponse<Boolean> addSourceSubscription(@RequestBody UserSourceSubscription request) {
        return ResultUtils.success(userSourceSubscriptionService.addSourceSubscription(request));
    }

    /**
     * 查询启用订阅源列表
     */
    @GetMapping("/list/enabled")
    public BaseResponse<List<UserSourceSubscription>> listEnabledSourceSubscriptions() {
        return ResultUtils.success(userSourceSubscriptionService.listEnabledSourceSubscriptions());
    }

    /**
     * 查询当前用户订阅源列表
     */
    @GetMapping("/list/{userId}")
    public BaseResponse<List<UserSourceSubscription>> listSourceSubscriptionsByUserId(@PathVariable Long userId) {
        return ResultUtils.success(userSourceSubscriptionService.listSourceSubscriptionsByUserId(userId));
    }
}
