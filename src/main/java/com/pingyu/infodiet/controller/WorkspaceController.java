package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.common.DeleteRequest;
import com.pingyu.infodiet.common.ResultUtils;
import com.pingyu.infodiet.model.dto.content.UnifiedContentItemDTO;
import com.pingyu.infodiet.model.dto.content.WorkspaceContentQueryRequest;
import com.pingyu.infodiet.model.dto.user.UserKeywordSubscriptionRequest;
import com.pingyu.infodiet.model.dto.user.UserPushConfigRequest;
import com.pingyu.infodiet.model.dto.user.UserPushConfigVO;
import com.pingyu.infodiet.model.dto.user.UserSubscriptionRuleRequest;
import com.pingyu.infodiet.model.entity.UserContentPush;
import com.pingyu.infodiet.model.dto.user.WorkspaceSubscriptionsVO;
import com.pingyu.infodiet.model.entity.UserSourceSubscription;
import com.pingyu.infodiet.model.entity.UserSubscriptionRule;
import com.pingyu.infodiet.service.WorkspaceService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户工作台接口
 */
@RestController
@RequestMapping("/workspace")
public class WorkspaceController {

    @Resource
    private WorkspaceService workspaceService;

    /**
     * 查询我的订阅
     */
    @GetMapping("/subscriptions/me")
    public BaseResponse<WorkspaceSubscriptionsVO> getMySubscriptions() {
        return ResultUtils.success(workspaceService.getMySubscriptions());
    }

    /**
     * 查询我的推送配置
     */
    @GetMapping("/push-config/me")
    public BaseResponse<UserPushConfigVO> getMyPushConfig() {
        return ResultUtils.success(workspaceService.getMyPushConfig());
    }

    /**
     * 新增我的关键词
     */
    @PostMapping("/keywords")
    public BaseResponse<Boolean> addMyKeyword(@RequestBody UserKeywordSubscriptionRequest request) {
        return ResultUtils.success(workspaceService.addMyKeyword(request.getKeyword()));
    }

    /**
     * 更新我的推送配置
     */
    @PostMapping("/push-config/me")
    public BaseResponse<Boolean> updateMyPushConfig(@RequestBody UserPushConfigRequest request) {
        return ResultUtils.success(workspaceService.updateMyPushConfig(request));
    }

    /**
     * 删除我的关键词
     */
    @DeleteMapping("/keywords")
    public BaseResponse<Boolean> removeMyKeyword(@RequestBody UserKeywordSubscriptionRequest request) {
        return ResultUtils.success(workspaceService.removeMyKeyword(request.getKeyword()));
    }

    /**
     * 新增我的规则
     */
    @PostMapping("/rules")
    public BaseResponse<Boolean> addMyRule(@RequestBody UserSubscriptionRuleRequest request) {
        UserSubscriptionRule userSubscriptionRule = UserSubscriptionRule.builder()
                .ruleType(request.getRuleType())
                .ruleValue(request.getRuleValue())
                .ruleWeight(request.getRuleWeight())
                .build();
        return ResultUtils.success(workspaceService.addMyRule(userSubscriptionRule));
    }

    /**
     * 删除我的规则
     */
    @DeleteMapping("/rules")
    public BaseResponse<Boolean> removeMyRule(@RequestBody UserSubscriptionRuleRequest request) {
        return ResultUtils.success(workspaceService.removeMyRule(request.getRuleType(), request.getRuleValue()));
    }

    /**
     * 新增我的订阅源
     */
    @PostMapping("/sources")
    public BaseResponse<Boolean> addMySource(@RequestBody UserSourceSubscription request) {
        return ResultUtils.success(workspaceService.addMySource(request));
    }

    /**
     * 删除我的订阅源
     */
    @DeleteMapping("/sources")
    public BaseResponse<Boolean> removeMySource(@RequestBody DeleteRequest request) {
        return ResultUtils.success(workspaceService.removeMySource(request.getId()));
    }

    /**
     * 查询我的内容列表
     */
    @GetMapping("/content/me")
    public BaseResponse<List<UnifiedContentItemDTO>> listMyContentItems(WorkspaceContentQueryRequest request) {
        return ResultUtils.success(workspaceService.listMyContentItems(request));
    }

    /**
     * 查询我的推送记录
     */
    @GetMapping("/pushes/me")
    public BaseResponse<List<UserContentPush>> listMyPushes() {
        return ResultUtils.success(workspaceService.listMyPushes());
    }
}
