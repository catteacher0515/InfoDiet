package com.pingyu.infodiet.controller;

import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.model.dto.auth.AdminCreateUserRequest;
import com.pingyu.infodiet.model.dto.auth.AuthLoginRequest;
import com.pingyu.infodiet.model.dto.auth.AuthRegisterRequest;
import com.pingyu.infodiet.model.dto.auth.LoginUserVO;
import com.pingyu.infodiet.service.AuthService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    @Test
    void registerShouldReturnUserId() {
        AuthService authService = Mockito.mock(AuthService.class);
        when(authService.register(Mockito.any(AuthRegisterRequest.class))).thenReturn(1L);

        AuthController controller = new AuthController();
        ReflectionTestUtils.setField(controller, "authService", authService);

        AuthRegisterRequest request = new AuthRegisterRequest();
        request.setNickname("pingyu");
        request.setUsername("pingyu");
        request.setPassword("123456");
        BaseResponse<Long> response = controller.register(request);

        assertEquals(0, response.getCode());
        assertEquals(1L, response.getData());
    }

    @Test
    void loginShouldReturnToken() {
        AuthService authService = Mockito.mock(AuthService.class);
        when(authService.login(Mockito.any(AuthLoginRequest.class))).thenReturn(LoginUserVO.builder()
                .id(1L)
                .username("pingyu")
                .role("user")
                .token("jwt-token")
                .build());

        AuthController controller = new AuthController();
        ReflectionTestUtils.setField(controller, "authService", authService);

        AuthLoginRequest request = new AuthLoginRequest();
        request.setUsername("pingyu");
        request.setPassword("123456");
        BaseResponse<LoginUserVO> response = controller.login(request);

        assertEquals(0, response.getCode());
        assertEquals("jwt-token", response.getData().getToken());
    }

    @Test
    void getCurrentUserShouldReturnLoginUser() {
        AuthService authService = Mockito.mock(AuthService.class);
        when(authService.getCurrentUser()).thenReturn(LoginUserVO.builder()
                .id(2L)
                .nickname("admin")
                .username("admin")
                .role("admin")
                .build());

        AuthController controller = new AuthController();
        ReflectionTestUtils.setField(controller, "authService", authService);

        BaseResponse<LoginUserVO> response = controller.getCurrentUser();

        assertEquals(0, response.getCode());
        assertEquals("admin", response.getData().getRole());
    }

    @Test
    void adminCreateUserShouldReturnUserId() {
        AuthService authService = Mockito.mock(AuthService.class);
        when(authService.adminCreateUser(Mockito.any(AdminCreateUserRequest.class))).thenReturn(3L);

        AuthController controller = new AuthController();
        ReflectionTestUtils.setField(controller, "authService", authService);

        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setNickname("user-1");
        request.setUsername("user-1");
        request.setPassword("123456");
        request.setRole("user");
        BaseResponse<Long> response = controller.adminCreateUser(request);

        assertEquals(0, response.getCode());
        assertEquals(3L, response.getData());
    }
}
