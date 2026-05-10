package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.common.ResultUtils;
import com.pingyu.infodiet.model.dto.user.UserKeywordSubscriptionRequest;
import com.pingyu.infodiet.model.entity.UserKeywordSubscription;
import com.pingyu.infodiet.service.UserKeywordSubscriptionService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户关键词订阅接口
 */
@RestController
@RequestMapping("/user/keyword")
public class UserKeywordSubscriptionController {

    @Resource
    private UserKeywordSubscriptionService userKeywordSubscriptionService;

    /**
     * 添加关键词订阅
     */
    @PostMapping("/add")
    public BaseResponse<Boolean> addKeyword(@RequestBody UserKeywordSubscriptionRequest request) {
        return ResultUtils.success(userKeywordSubscriptionService.addKeyword(
                request.getUserId(),
                request.getKeyword()
        ));
    }

    /**
     * 删除关键词订阅
     */
    @DeleteMapping("/remove")
    public BaseResponse<Boolean> removeKeyword(@RequestBody UserKeywordSubscriptionRequest request) {
        return ResultUtils.success(userKeywordSubscriptionService.removeKeyword(
                request.getUserId(),
                request.getKeyword()
        ));
    }

    /**
     * 查询用户关键词列表
     */
    @GetMapping("/list/{userId}")
    public BaseResponse<List<String>> listKeywordsByUserId(@PathVariable Long userId) {
        return ResultUtils.success(userKeywordSubscriptionService.listKeywordsByUserId(userId));
    }

    /**
     * 查询启用订阅列表
     */
    @GetMapping("/list/enabled/{userId}")
    public BaseResponse<List<UserKeywordSubscription>> listEnabledSubscriptionsByUserId(@PathVariable Long userId) {
        return ResultUtils.success(userKeywordSubscriptionService.listEnabledSubscriptionsByUserId(userId));
    }
}
