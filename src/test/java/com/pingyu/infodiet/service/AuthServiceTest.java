package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.auth.LoginUser;
import com.pingyu.infodiet.model.auth.LoginUserContext;
import com.pingyu.infodiet.model.dto.auth.AdminCreateUserRequest;
import com.pingyu.infodiet.model.dto.auth.AuthLoginRequest;
import com.pingyu.infodiet.model.dto.auth.AuthRegisterRequest;
import com.pingyu.infodiet.model.dto.auth.LoginUserVO;
import com.pingyu.infodiet.model.entity.UserProfile;
import com.pingyu.infodiet.service.impl.AuthServiceImpl;
import com.pingyu.infodiet.utils.JwtUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AuthServiceTest {

    @AfterEach
    void clearLoginUserContext() {
        LoginUserContext.clear();
    }

    @Test
    void registerShouldCreateUserWithEncodedPassword() {
        InMemoryAuthService service = new InMemoryAuthService();
        AuthRegisterRequest request = new AuthRegisterRequest();
        request.setNickname("pingyu");
        request.setUsername("pingyu");
        request.setPassword("123456");

        Long userId = service.register(request);

        assertEquals(1L, userId);
        assertEquals("pingyu", service.getStoredUsers().getFirst().getUsername());
        assertEquals("admin", service.getStoredUsers().getFirst().getRole());
        assertNotNull(service.getStoredUsers().getFirst().getPassword());
    }

    @Test
    void registerShouldCreateNormalUserAfterAdminBootstrap() {
        InMemoryAuthService service = new InMemoryAuthService();
        service.seedUser(UserProfile.builder()
                .nickname("admin")
                .username("admin")
                .password("encoded")
                .role("admin")
                .status(1)
                .build());

        AuthRegisterRequest request = new AuthRegisterRequest();
        request.setNickname("user-1");
        request.setUsername("user-1");
        request.setPassword("123456");

        Long userId = service.register(request);

        assertEquals(2L, userId);
        assertEquals("user", service.getStoredUsers().getLast().getRole());
    }

    @Test
    void loginShouldReturnTokenAndRole() {
        InMemoryAuthService service = new InMemoryAuthService();
        service.seedUser(UserProfile.builder()
                .nickname("pingyu")
                .username("pingyu")
                .password(cn.hutool.crypto.digest.BCrypt.hashpw("123456"))
                .role("user")
                .status(1)
                .build());

        AuthLoginRequest request = new AuthLoginRequest();
        request.setUsername("pingyu");
        request.setPassword("123456");
        LoginUserVO loginUserVO = service.login(request);

        assertEquals("user", loginUserVO.getRole());
        assertNotNull(loginUserVO.getToken());
    }

    @Test
    void getCurrentUserShouldReturnContextUser() {
        InMemoryAuthService service = new InMemoryAuthService();
        Long userId = service.seedUser(UserProfile.builder()
                .nickname("admin")
                .username("admin")
                .password("encoded")
                .role("admin")
                .status(1)
                .build());
        LoginUserContext.set(LoginUser.builder().userId(userId).username("admin").role("admin").build());

        LoginUserVO loginUserVO = service.getCurrentUser();

        assertEquals("admin", loginUserVO.getRole());
        assertEquals("admin", loginUserVO.getUsername());
    }

    @Test
    void adminCreateUserShouldRequireAdminRole() {
        InMemoryAuthService service = new InMemoryAuthService();
        LoginUserContext.set(LoginUser.builder().userId(1L).username("admin").role("admin").build());

        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setNickname("user-1");
        request.setUsername("user-1");
        request.setPassword("123456");
        request.setRole("user");
        Long userId = service.adminCreateUser(request);

        assertEquals(1L, userId);
        assertEquals("user", service.getStoredUsers().getFirst().getRole());
    }

    private static class InMemoryAuthService extends AuthServiceImpl {

        private final InMemoryUserProfileService inMemoryUserProfileService = new InMemoryUserProfileService();

        private InMemoryAuthService() {
            this.userProfileService = inMemoryUserProfileService;
            this.jwtUtils = new StubJwtUtils();
        }

        private List<UserProfile> getStoredUsers() {
            return inMemoryUserProfileService.items;
        }

        private Long seedUser(UserProfile userProfile) {
            return inMemoryUserProfileService.createUser(userProfile);
        }
    }

    private static class InMemoryUserProfileService extends com.pingyu.infodiet.service.impl.UserProfileServiceImpl {

        private final List<UserProfile> items = new ArrayList<>();
        private long idCounter = 1L;

        @Override
        public boolean save(UserProfile entity) {
            if (entity.getId() == null) {
                entity.setId(idCounter++);
            }
            items.add(entity);
            return true;
        }

        @Override
        public boolean updateById(UserProfile entity) {
            return true;
        }

        @Override
        public UserProfile getById(java.io.Serializable id) {
            return items.stream()
                    .filter(item -> item.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public UserProfile getOne(com.mybatisflex.core.query.QueryWrapper queryWrapper) {
            String sql = queryWrapper.toSQL();
            int index = sql.lastIndexOf("username = ");
            String username = sql.substring(index + "username = ".length()).replace("'", "").trim();
            return items.stream()
                    .filter(item -> username.equals(item.getUsername()))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<UserProfile> list() {
            return items;
        }
    }

    private static class StubJwtUtils extends JwtUtils {

        @Override
        public String createToken(Long userId, String username, String role) {
            return "token-" + userId + "-" + role;
        }
    }
}
