package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.dto.content.UnifiedContentItemDTO;
import com.pingyu.infodiet.model.dto.content.WorkspaceContentQueryRequest;
import com.pingyu.infodiet.model.dto.user.UserPushConfigRequest;
import com.pingyu.infodiet.model.dto.user.UserPushConfigVO;
import com.pingyu.infodiet.model.dto.user.WorkspaceSubscriptionsVO;
import com.pingyu.infodiet.model.entity.UserSourceSubscription;
import com.pingyu.infodiet.model.entity.UserSubscriptionRule;
import com.pingyu.infodiet.model.entity.UserContentPush;

import java.util.List;

/**
 * 用户工作台服务
 */
public interface WorkspaceService {

    /**
     * 查询当前用户订阅聚合信息
     */
    WorkspaceSubscriptionsVO getMySubscriptions();

    /**
     * 查询当前用户推送配置
     */
    UserPushConfigVO getMyPushConfig();

    /**
     * 新增当前用户关键词
     */
    boolean addMyKeyword(String keyword);

    /**
     * 更新当前用户推送配置
     */
    boolean updateMyPushConfig(UserPushConfigRequest request);

    /**
     * 删除当前用户关键词
     */
    boolean removeMyKeyword(String keyword);

    /**
     * 新增当前用户规则
     */
    boolean addMyRule(UserSubscriptionRule userSubscriptionRule);

    /**
     * 删除当前用户规则
     */
    boolean removeMyRule(String ruleType, String ruleValue);

    /**
     * 新增当前用户订阅源
     */
    boolean addMySource(UserSourceSubscription userSourceSubscription);

    /**
     * 删除当前用户订阅源
     */
    boolean removeMySource(Long sourceSubscriptionId);

    /**
     * 查询当前用户内容列表
     */
    List<UnifiedContentItemDTO> listMyContentItems(WorkspaceContentQueryRequest request);

    /**
     * 查询当前用户推送记录
     */
    List<UserContentPush> listMyPushes();
}
