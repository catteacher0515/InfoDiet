package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.exception.BusinessException;
import com.pingyu.infodiet.model.auth.LoginUser;
import com.pingyu.infodiet.model.auth.LoginUserContext;
import com.pingyu.infodiet.model.dto.user.UserPushConfigRequest;
import com.pingyu.infodiet.service.UserProfileService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class UserPushConfigControllerTest {

    @AfterEach
    void clearLoginUserContext() {
        LoginUserContext.clear();
    }

    @Test
    void updateUserPushConfigShouldRequireAdminRole() {
        UserProfileService userProfileService = Mockito.mock(UserProfileService.class);

        UserProfileController controller = new UserProfileController();
        ReflectionTestUtils.setField(controller, "userProfileService", userProfileService);
        LoginUserContext.set(LoginUser.builder().userId(2L).username("user").role("user").build());

        UserPushConfigRequest request = new UserPushConfigRequest();
        request.setPushChannel("feishu");
        request.setDailyPushLimit(10);
        request.setPushCooldownHours(0);

        assertThrows(BusinessException.class, () -> controller.updateUserPushConfig(1L, request));
    }

    @Test
    void updateUserPushConfigShouldReturnSuccessForAdmin() {
        UserProfileService userProfileService = Mockito.mock(UserProfileService.class);
        when(userProfileService.updateUserPushConfig(Mockito.eq(1L), Mockito.any(UserPushConfigRequest.class))).thenReturn(true);

        UserProfileController controller = new UserProfileController();
        ReflectionTestUtils.setField(controller, "userProfileService", userProfileService);
        LoginUserContext.set(LoginUser.builder().userId(1L).username("admin").role("admin").build());

        UserPushConfigRequest request = new UserPushConfigRequest();
        request.setFeishuUserId("ou_admin");
        request.setPushChannel("feishu");
        request.setDailyPushLimit(12);
        request.setPushCooldownHours(1);
        BaseResponse<Boolean> response = controller.updateUserPushConfig(1L, request);

        assertEquals(0, response.getCode());
        assertEquals(true, response.getData());
    }
}
