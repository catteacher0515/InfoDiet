package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.common.ResultUtils;
import com.pingyu.infodiet.model.dto.user.UserSubscriptionRuleRequest;
import com.pingyu.infodiet.model.entity.UserSubscriptionRule;
import com.pingyu.infodiet.service.UserSubscriptionRuleService;
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
 * 用户订阅规则接口
 */
@RestController
@RequestMapping("/user/rule")
public class UserSubscriptionRuleController {

    @Resource
    private UserSubscriptionRuleService userSubscriptionRuleService;

    /**
     * 添加订阅规则
     */
    @PostMapping("/add")
    public BaseResponse<Boolean> addRule(@RequestBody UserSubscriptionRuleRequest request) {
        UserSubscriptionRule userSubscriptionRule = UserSubscriptionRule.builder()
                .userId(request.getUserId())
                .ruleType(request.getRuleType())
                .ruleValue(request.getRuleValue())
                .ruleWeight(request.getRuleWeight())
                .build();
        return ResultUtils.success(userSubscriptionRuleService.addRule(userSubscriptionRule));
    }

    /**
     * 删除订阅规则
     */
    @DeleteMapping("/remove")
    public BaseResponse<Boolean> removeRule(@RequestBody UserSubscriptionRuleRequest request) {
        return ResultUtils.success(userSubscriptionRuleService.removeRule(
                request.getUserId(),
                request.getRuleType(),
                request.getRuleValue()
        ));
    }

    /**
     * 查询启用规则列表
     */
    @GetMapping("/list/enabled/{userId}")
    public BaseResponse<List<UserSubscriptionRule>> listEnabledRulesByUserId(@PathVariable Long userId) {
        return ResultUtils.success(userSubscriptionRuleService.listEnabledRulesByUserId(userId));
    }
}
