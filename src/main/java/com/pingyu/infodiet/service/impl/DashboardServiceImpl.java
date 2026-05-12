package com.pingyu.infodiet.service.impl;

import com.pingyu.infodiet.exception.BusinessException;
import com.pingyu.infodiet.exception.ErrorCode;
import com.pingyu.infodiet.model.auth.LoginUserContext;
import com.pingyu.infodiet.model.dto.dashboard.AdminDashboardVO;
import com.pingyu.infodiet.model.dto.dashboard.OpsDashboardVO;
import com.pingyu.infodiet.model.dto.dashboard.WorkspaceDashboardVO;
import com.pingyu.infodiet.service.AlertRecordService;
import com.pingyu.infodiet.service.CrawlTaskLogService;
import com.pingyu.infodiet.service.DashboardService;
import com.pingyu.infodiet.service.UserContentPushService;
import com.pingyu.infodiet.service.UserKeywordSubscriptionService;
import com.pingyu.infodiet.service.UserProfileService;
import com.pingyu.infodiet.service.UserSourceSubscriptionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 工作台概览服务实现
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    @Resource
    private UserKeywordSubscriptionService userKeywordSubscriptionService;

    @Resource
    private UserSourceSubscriptionService userSourceSubscriptionService;

    @Resource
    private UserContentPushService userContentPushService;

    @Resource
    private UserProfileService userProfileService;

    @Resource
    private CrawlTaskLogService crawlTaskLogService;

    @Resource
    private AlertRecordService alertRecordService;

    /**
     * 查询当前用户工作台概览
     */
    @Override
    public WorkspaceDashboardVO getWorkspaceDashboard() {
        Long userId = LoginUserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        int keywordCount = userKeywordSubscriptionService.listKeywordsByUserId(userId).size();
        int sourceCount = (int) userSourceSubscriptionService.listEnabledSourceSubscriptions().stream()
                .filter(item -> userId.equals(item.getUserId()))
                .count();
        int totalPushCount = (int) userContentPushService.list().stream()
                .filter(item -> userId.equals(item.getUserId()))
                .count();
        int successPushCount = (int) userContentPushService.list().stream()
                .filter(item -> userId.equals(item.getUserId()) && item.getPushStatus() != null && item.getPushStatus() == 1)
                .count();
        int failedPushCount = (int) userContentPushService.list().stream()
                .filter(item -> userId.equals(item.getUserId()) && item.getPushStatus() != null && item.getPushStatus() == 2)
                .count();
        return WorkspaceDashboardVO.builder()
                .keywordCount(keywordCount)
                .sourceCount(sourceCount)
                .totalPushCount(totalPushCount)
                .successPushCount(successPushCount)
                .failedPushCount(failedPushCount)
                .build();
    }

    /**
     * 查询管理端概览
     */
    @Override
    public AdminDashboardVO getAdminDashboard() {
        ensureAdmin();
        int userCount = userProfileService.list().size();
        int enabledUserCount = userProfileService.listEnabledUsers().size();
        int keywordSubscriptionCount = userKeywordSubscriptionService.list().size();
        int sourceSubscriptionCount = userSourceSubscriptionService.list().size();
        return AdminDashboardVO.builder()
                .userCount(userCount)
                .enabledUserCount(enabledUserCount)
                .keywordSubscriptionCount(keywordSubscriptionCount)
                .sourceSubscriptionCount(sourceSubscriptionCount)
                .build();
    }

    /**
     * 查询运维端概览
     */
    @Override
    public OpsDashboardVO getOpsDashboard() {
        ensureAdmin();
        int recentTaskCount = crawlTaskLogService.listRecentLogs(10).size();
        int pendingAlertCount = alertRecordService.listPendingAlerts().size();
        int failedPushCount = userContentPushService.listFailedPushesByChannel("feishu").size();
        return OpsDashboardVO.builder()
                .recentTaskCount(recentTaskCount)
                .pendingAlertCount(pendingAlertCount)
                .failedPushCount(failedPushCount)
                .build();
    }

    private void ensureAdmin() {
        if (!"admin".equals(LoginUserContext.getRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
    }
}
