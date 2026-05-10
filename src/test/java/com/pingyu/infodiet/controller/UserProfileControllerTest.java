package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.model.entity.UserProfile;
import com.pingyu.infodiet.service.UserProfileService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
