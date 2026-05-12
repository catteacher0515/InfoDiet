package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.exception.BusinessException;
import com.pingyu.infodiet.model.auth.LoginUser;
import com.pingyu.infodiet.model.auth.LoginUserContext;
import com.pingyu.infodiet.model.dto.user.UserListItemVO;
import com.pingyu.infodiet.model.entity.UserProfile;
import com.pingyu.infodiet.service.UserProfileService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class UserProfileControllerTest {

    @Test
    void createUserShouldReturnUserId() {
        UserProfileService userProfileService = Mockito.mock(UserProfileService.class);
        when(userProfileService.createUser(Mockito.any(UserProfile.class))).thenReturn(1L);

        UserProfileController controller = new UserProfileController();
        ReflectionTestUtils.setField(controller, "userProfileService", userProfileService);

        BaseResponse<Long> response = controller.createUser(UserProfile.builder().nickname("pingyu").build());

        assertEquals(0, response.getCode());
        assertEquals(1L, response.getData());
    }

    @Test
    void createUserShouldAcceptPushStrategyFields() {
        UserProfileService userProfileService = Mockito.mock(UserProfileService.class);
        when(userProfileService.createUser(Mockito.any(UserProfile.class))).thenReturn(2L);

        UserProfileController controller = new UserProfileController();
        ReflectionTestUtils.setField(controller, "userProfileService", userProfileService);

        UserProfile request = UserProfile.builder()
                .nickname("pingyu")
                .dailyPushLimit(3)
                .pushCooldownHours(12)
                .build();
        BaseResponse<Long> response = controller.createUser(request);

        assertEquals(0, response.getCode());
        assertEquals(2L, response.getData());
    }

    @Test
    void listEnabledUsersShouldReturnUsers() {
        UserProfileService userProfileService = Mockito.mock(UserProfileService.class);
        when(userProfileService.listEnabledUsers()).thenReturn(List.of(
                UserProfile.builder().id(1L).nickname("pingyu").build()
        ));

        UserProfileController controller = new UserProfileController();
        ReflectionTestUtils.setField(controller, "userProfileService", userProfileService);

        BaseResponse<List<UserProfile>> response = controller.listEnabledUsers();

        assertEquals(0, response.getCode());
        assertEquals(1, response.getData().size());
        assertEquals("pingyu", response.getData().getFirst().getNickname());
    }

    @Test
    void updateUserShouldAllowCooldownToBeResetToZero() {
        UserProfileService userProfileService = Mockito.mock(UserProfileService.class);
        when(userProfileService.updateUser(Mockito.any(UserProfile.class))).thenReturn(true);

        UserProfileController controller = new UserProfileController();
        ReflectionTestUtils.setField(controller, "userProfileService", userProfileService);

        BaseResponse<Boolean> response = controller.updateUser(UserProfile.builder()
                .id(1L)
                .pushCooldownHours(0)
                .build());

        assertEquals(0, response.getCode());
        assertEquals(true, response.getData());
    }

    @Test
    void listUsersShouldReturnUserList() {
        UserProfileService userProfileService = Mockito.mock(UserProfileService.class);
        when(userProfileService.listUsers()).thenReturn(List.of(
                UserListItemVO.builder().id(1L).nickname("pingyu").username("pingyu").role("admin").status(1).build()
        ));

        UserProfileController controller = new UserProfileController();
        ReflectionTestUtils.setField(controller, "userProfileService", userProfileService);
        LoginUserContext.set(LoginUser.builder().userId(1L).username("pingyu").role("admin").build());

        BaseResponse<List<UserListItemVO>> response = controller.listUsers();

        assertEquals(0, response.getCode());
        assertEquals(1, response.getData().size());
        assertEquals("admin", response.getData().getFirst().getRole());
        LoginUserContext.clear();
    }

    @Test
    void listUsersShouldRequireAdminRole() {
        UserProfileService userProfileService = Mockito.mock(UserProfileService.class);

        UserProfileController controller = new UserProfileController();
        ReflectionTestUtils.setField(controller, "userProfileService", userProfileService);
        LoginUserContext.set(LoginUser.builder().userId(2L).username("user").role("user").build());

        assertThrows(BusinessException.class, controller::listUsers);
        LoginUserContext.clear();
    }
}
