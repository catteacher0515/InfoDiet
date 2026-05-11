package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.common.ResultUtils;
import com.pingyu.infodiet.model.dto.user.UserSubscriptionRuleRequest;
import com.pingyu.infodiet.model.entity.UserSubscriptionRule;
import com.pingyu.infodiet.service.SubscriptionMatchService;
import com.pingyu.infodiet.service.UserSubscriptionRuleService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 订阅管理接口
 */
@RestController
@RequestMapping("/subscription/manage")
public class SubscriptionManagementController {

    @Resource
    private UserSubscriptionRuleService userSubscriptionRuleService;

    @Resource
    private SubscriptionMatchService subscriptionMatchService;

    /**
     * 更新订阅规则
     */
    @PutMapping("/rule/update")
    public BaseResponse<Boolean> updateRule(@RequestBody UserSubscriptionRuleRequest request) {
        UserSubscriptionRule userSubscriptionRule = UserSubscriptionRule.builder()
                .id(request.getId())
                .userId(request.getUserId())
                .ruleType(request.getRuleType())
                .ruleValue(request.getRuleValue())
                .ruleWeight(request.getRuleWeight())
                .build();
        return ResultUtils.success(userSubscriptionRuleService.updateRule(userSubscriptionRule));
    }

    /**
     * 预览匹配结果
     */
    @GetMapping("/preview")
    public BaseResponse<Map<Long, List<SubscriptionMatchService.MatchDetail>>> previewMatch() {
        return ResultUtils.success(subscriptionMatchService.matchEnabledUsersWithDetails());
    }
}
