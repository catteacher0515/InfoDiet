package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.auth.LoginUser;
import com.pingyu.infodiet.model.auth.LoginUserContext;
import com.pingyu.infodiet.model.dto.dashboard.AdminDashboardVO;
import com.pingyu.infodiet.model.dto.dashboard.OpsDashboardVO;
import com.pingyu.infodiet.model.dto.dashboard.WorkspaceDashboardVO;
import com.pingyu.infodiet.model.entity.AlertRecord;
import com.pingyu.infodiet.model.entity.CrawlTaskLog;
import com.pingyu.infodiet.model.entity.UserContentPush;
import com.pingyu.infodiet.model.entity.UserKeywordSubscription;
import com.pingyu.infodiet.model.entity.UserProfile;
import com.pingyu.infodiet.model.entity.UserSourceSubscription;
import com.pingyu.infodiet.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DashboardServiceTest {

    @AfterEach
    void clearLoginUserContext() {
        LoginUserContext.clear();
    }

    @Test
    void getWorkspaceDashboardShouldAggregateCurrentUserData() {
        DashboardServiceImpl service = new DashboardServiceImpl();
        UserKeywordSubscriptionService userKeywordSubscriptionService = mock(UserKeywordSubscriptionService.class);
        UserSourceSubscriptionService userSourceSubscriptionService = mock(UserSourceSubscriptionService.class);
        UserContentPushService userContentPushService = mock(UserContentPushService.class);

        when(userKeywordSubscriptionService.listKeywordsByUserId(1L)).thenReturn(List.of("ai", "agent"));
        when(userSourceSubscriptionService.listEnabledSourceSubscriptions()).thenReturn(List.of(
                UserSourceSubscription.builder().userId(1L).build(),
                UserSourceSubscription.builder().userId(1L).build(),
                UserSourceSubscription.builder().userId(2L).build()
        ));
        when(userContentPushService.list()).thenReturn(List.of(
                UserContentPush.builder().userId(1L).pushStatus(1).build(),
                UserContentPush.builder().userId(1L).pushStatus(2).build(),
                UserContentPush.builder().userId(2L).pushStatus(1).build()
        ));

        ReflectionTestUtils.setField(service, "userKeywordSubscriptionService", userKeywordSubscriptionService);
        ReflectionTestUtils.setField(service, "userSourceSubscriptionService", userSourceSubscriptionService);
        ReflectionTestUtils.setField(service, "userContentPushService", userContentPushService);
        LoginUserContext.set(LoginUser.builder().userId(1L).username("user").role("user").build());

        WorkspaceDashboardVO dashboard = service.getWorkspaceDashboard();

        assertEquals(2, dashboard.getKeywordCount());
        assertEquals(2, dashboard.getSourceCount());
        assertEquals(2, dashboard.getTotalPushCount());
        assertEquals(1, dashboard.getSuccessPushCount());
        assertEquals(1, dashboard.getFailedPushCount());
    }

    @Test
    void getAdminDashboardShouldAggregatePlatformData() {
        DashboardServiceImpl service = new DashboardServiceImpl();
        UserProfileService userProfileService = mock(UserProfileService.class);
        UserKeywordSubscriptionService userKeywordSubscriptionService = mock(UserKeywordSubscriptionService.class);
        UserSourceSubscriptionService userSourceSubscriptionService = mock(UserSourceSubscriptionService.class);

        when(userProfileService.list()).thenReturn(List.of(
                UserProfile.builder().id(1L).build(),
                UserProfile.builder().id(2L).build()
        ));
        when(userProfileService.listEnabledUsers()).thenReturn(List.of(
                UserProfile.builder().id(1L).status(1).build()
        ));
        when(userKeywordSubscriptionService.list()).thenReturn(List.of(
                UserKeywordSubscription.builder().id(1L).build(),
                UserKeywordSubscription.builder().id(2L).build(),
                UserKeywordSubscription.builder().id(3L).build()
        ));
        when(userSourceSubscriptionService.list()).thenReturn(List.of(
                UserSourceSubscription.builder().id(1L).build()
        ));

        ReflectionTestUtils.setField(service, "userProfileService", userProfileService);
        ReflectionTestUtils.setField(service, "userKeywordSubscriptionService", userKeywordSubscriptionService);
        ReflectionTestUtils.setField(service, "userSourceSubscriptionService", userSourceSubscriptionService);
        LoginUserContext.set(LoginUser.builder().userId(1L).username("admin").role("admin").build());

        AdminDashboardVO dashboard = service.getAdminDashboard();

        assertEquals(2, dashboard.getUserCount());
        assertEquals(1, dashboard.getEnabledUserCount());
        assertEquals(3, dashboard.getKeywordSubscriptionCount());
        assertEquals(1, dashboard.getSourceSubscriptionCount());
    }

    @Test
    void getOpsDashboardShouldAggregateOpsData() {
        DashboardServiceImpl service = new DashboardServiceImpl();
        CrawlTaskLogService crawlTaskLogService = mock(CrawlTaskLogService.class);
        AlertRecordService alertRecordService = mock(AlertRecordService.class);
        UserContentPushService userContentPushService = mock(UserContentPushService.class);

        when(crawlTaskLogService.listRecentLogs(10)).thenReturn(List.of(
                CrawlTaskLog.builder().id(1L).build(),
                CrawlTaskLog.builder().id(2L).build()
        ));
        when(alertRecordService.listPendingAlerts()).thenReturn(List.of(
                AlertRecord.builder().id(1L).build()
        ));
        when(userContentPushService.listFailedPushesByChannel("feishu")).thenReturn(List.of(
                UserContentPush.builder().id(1L).build(),
                UserContentPush.builder().id(2L).build(),
                UserContentPush.builder().id(3L).build()
        ));

        ReflectionTestUtils.setField(service, "crawlTaskLogService", crawlTaskLogService);
        ReflectionTestUtils.setField(service, "alertRecordService", alertRecordService);
        ReflectionTestUtils.setField(service, "userContentPushService", userContentPushService);
        LoginUserContext.set(LoginUser.builder().userId(1L).username("admin").role("admin").build());

        OpsDashboardVO dashboard = service.getOpsDashboard();

        assertEquals(2, dashboard.getRecentTaskCount());
        assertEquals(1, dashboard.getPendingAlertCount());
        assertEquals(3, dashboard.getFailedPushCount());
    }
}
