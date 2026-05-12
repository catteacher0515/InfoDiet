package com.pingyu.infodiet.service.impl;

import cn.hutool.core.util.StrUtil;
import com.pingyu.infodiet.exception.BusinessException;
import com.pingyu.infodiet.exception.ErrorCode;
import com.pingyu.infodiet.model.auth.LoginUserContext;
import com.pingyu.infodiet.model.dto.content.UnifiedContentItemDTO;
import com.pingyu.infodiet.model.dto.content.WorkspaceContentQueryRequest;
import com.pingyu.infodiet.model.dto.user.WorkspaceSubscriptionsVO;
import com.pingyu.infodiet.model.entity.ContentItem;
import com.pingyu.infodiet.model.entity.UserContentPush;
import com.pingyu.infodiet.model.entity.UserSourceSubscription;
import com.pingyu.infodiet.model.entity.UserSubscriptionRule;
import com.pingyu.infodiet.service.ContentItemService;
import com.pingyu.infodiet.service.SubscriptionMatchService;
import com.pingyu.infodiet.service.UserContentPushService;
import com.pingyu.infodiet.service.UserKeywordSubscriptionService;
import com.pingyu.infodiet.service.UserSourceSubscriptionService;
import com.pingyu.infodiet.service.UserSubscriptionRuleService;
import com.pingyu.infodiet.service.WorkspaceService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户工作台服务实现
 */
@Service
public class WorkspaceServiceImpl implements WorkspaceService {

    @Resource
    private UserKeywordSubscriptionService userKeywordSubscriptionService;

    @Resource
    private UserSubscriptionRuleService userSubscriptionRuleService;

    @Resource
    private UserSourceSubscriptionService userSourceSubscriptionService;

    @Resource
    private SubscriptionMatchService subscriptionMatchService;

    @Resource
    private ContentItemService contentItemService;

    @Resource
    private UserContentPushService userContentPushService;

    /**
     * 查询当前用户订阅聚合信息
     */
    @Override
    public WorkspaceSubscriptionsVO getMySubscriptions() {
        Long userId = LoginUserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return WorkspaceSubscriptionsVO.builder()
                .keywords(userKeywordSubscriptionService.listKeywordsByUserId(userId))
                .rules(userSubscriptionRuleService.listEnabledRulesByUserId(userId))
                .sources(userSourceSubscriptionService.listEnabledSourceSubscriptions().stream()
                        .filter(item -> userId.equals(item.getUserId()))
                        .toList())
                .build();
    }

    /**
     * 新增当前用户关键词
     */
    @Override
    public boolean addMyKeyword(String keyword) {
        Long userId = requireLoginUserId();
        return userKeywordSubscriptionService.addKeyword(userId, keyword);
    }

    /**
     * 删除当前用户关键词
     */
    @Override
    public boolean removeMyKeyword(String keyword) {
        Long userId = requireLoginUserId();
        return userKeywordSubscriptionService.removeKeyword(userId, keyword);
    }

    /**
     * 新增当前用户规则
     */
    @Override
    public boolean addMyRule(UserSubscriptionRule userSubscriptionRule) {
        Long userId = requireLoginUserId();
        userSubscriptionRule.setUserId(userId);
        return userSubscriptionRuleService.addRule(userSubscriptionRule);
    }

    /**
     * 删除当前用户规则
     */
    @Override
    public boolean removeMyRule(String ruleType, String ruleValue) {
        Long userId = requireLoginUserId();
        return userSubscriptionRuleService.removeRule(userId, ruleType, ruleValue);
    }

    /**
     * 新增当前用户订阅源
     */
    @Override
    public boolean addMySource(UserSourceSubscription userSourceSubscription) {
        Long userId = requireLoginUserId();
        userSourceSubscription.setUserId(userId);
        return userSourceSubscriptionService.addSourceSubscription(userSourceSubscription);
    }

    /**
     * 删除当前用户订阅源
     */
    @Override
    public boolean removeMySource(Long sourceSubscriptionId) {
        Long userId = requireLoginUserId();
        return userSourceSubscriptionService.removeSourceSubscription(userId, sourceSubscriptionId);
    }

    /**
     * 查询当前用户内容列表
     */
    @Override
    public List<UnifiedContentItemDTO> listMyContentItems(WorkspaceContentQueryRequest request) {
        Long userId = requireLoginUserId();
        List<ContentItem> contentItems = subscriptionMatchService.matchEnabledUsers()
                .getOrDefault(userId, List.of());
        return contentItems.stream()
                .map(contentItemService::convertToUnifiedContentItem)
                .filter(item -> matchPlatform(item, request))
                .filter(item -> matchContentType(item, request))
                .limit(resolveLimit(request))
                .toList();
    }

    /**
     * 查询当前用户推送记录
     */
    @Override
    public List<UserContentPush> listMyPushes() {
        Long userId = requireLoginUserId();
        return userContentPushService.listPushesByUserId(userId);
    }

    private Long requireLoginUserId() {
        Long userId = LoginUserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return userId;
    }

    private boolean matchPlatform(UnifiedContentItemDTO item, WorkspaceContentQueryRequest request) {
        return request == null || StrUtil.isBlank(request.getPlatform())
                || StrUtil.equalsIgnoreCase(item.getPlatform(), request.getPlatform());
    }

    private boolean matchContentType(UnifiedContentItemDTO item, WorkspaceContentQueryRequest request) {
        return request == null || StrUtil.isBlank(request.getContentType())
                || StrUtil.equalsIgnoreCase(item.getContentType(), request.getContentType());
    }

    private long resolveLimit(WorkspaceContentQueryRequest request) {
        if (request == null || request.getLimit() == null || request.getLimit() <= 0) {
            return Long.MAX_VALUE;
        }
        return request.getLimit();
    }
}
