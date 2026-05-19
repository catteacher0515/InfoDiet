package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.auth.LoginUser;
import com.pingyu.infodiet.model.auth.LoginUserContext;
import com.pingyu.infodiet.model.dto.user.UserPushConfigRequest;
import com.pingyu.infodiet.model.dto.user.UserPushConfigVO;
import com.pingyu.infodiet.model.entity.UserProfile;
import com.pingyu.infodiet.service.impl.WorkspaceServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorkspacePushConfigServiceTest {

    @AfterEach
    void clearLoginUserContext() {
        LoginUserContext.clear();
    }

    @Test
    void getMyPushConfigShouldReturnCurrentUserConfig() {
        WorkspaceServiceImpl service = new WorkspaceServiceImpl();
        UserProfileService userProfileService = mock(UserProfileService.class);
        when(userProfileService.getUserById(1L)).thenReturn(UserProfile.builder()
                .id(1L)
                .feishuUserId("ou_test")
                .pushChannel("feishu")
                .dailyPushLimit(6)
                .pushCooldownHours(3)
                .build());
        ReflectionTestUtils.setField(service, "userProfileService", userProfileService);
        LoginUserContext.set(LoginUser.builder().userId(1L).username("user").role("user").build());

        UserPushConfigVO response = service.getMyPushConfig();

        assertEquals("ou_test", response.getFeishuUserId());
        assertEquals("feishu", response.getPushChannel());
        assertEquals(6, response.getDailyPushLimit());
        assertEquals(3, response.getPushCooldownHours());
    }

    @Test
    void updateMyPushConfigShouldDelegateToUserProfileService() {
        WorkspaceServiceImpl service = new WorkspaceServiceImpl();
        UserProfileService userProfileService = mock(UserProfileService.class);
        when(userProfileService.updateUserPushConfig(1L, buildRequest("ou_save", "feishu", 4, 0))).thenReturn(true);
        ReflectionTestUtils.setField(service, "userProfileService", userProfileService);
        LoginUserContext.set(LoginUser.builder().userId(1L).username("user").role("user").build());

        boolean updated = service.updateMyPushConfig(buildRequest("ou_save", "feishu", 4, 0));

        assertEquals(true, updated);
    }

    private UserPushConfigRequest buildRequest(String feishuUserId, String pushChannel, Integer dailyPushLimit, Integer pushCooldownHours) {
        UserPushConfigRequest request = new UserPushConfigRequest();
        request.setFeishuUserId(feishuUserId);
        request.setPushChannel(pushChannel);
        request.setDailyPushLimit(dailyPushLimit);
        request.setPushCooldownHours(pushCooldownHours);
        return request;
    }
}
