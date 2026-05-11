package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.entity.UserProfile;
import com.pingyu.infodiet.service.impl.UserProfileServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserProfileServiceTest {

    @Test
    void createUserShouldSaveAndReturnUserId() {
        InMemoryUserProfileService service = new InMemoryUserProfileService();
        UserProfile userProfile = UserProfile.builder()
                .id(1L)
                .nickname("pingyu")
                .status(1)
                .build();

        Long userId = service.createUser(userProfile);

        assertEquals(1L, userId);
        assertEquals(1, service.items.size());
    }

    @Test
    void listEnabledUsersShouldOnlyReturnEnabledUsers() {
        InMemoryUserProfileService service = new InMemoryUserProfileService();
        service.items.add(UserProfile.builder().id(1L).nickname("enabled").status(1).build());
        service.items.add(UserProfile.builder().id(2L).nickname("disabled").status(0).build());

        List<UserProfile> enabledUsers = service.listEnabledUsers();

        assertEquals(1, enabledUsers.size());
        assertEquals("enabled", enabledUsers.getFirst().getNickname());
    }

    @Test
    void createUserShouldKeepPushStrategyFields() {
        InMemoryUserProfileService service = new InMemoryUserProfileService();
        UserProfile userProfile = UserProfile.builder()
                .id(2L)
                .nickname("strategy-user")
                .dailyPushLimit(5)
                .pushCooldownHours(6)
                .status(1)
                .build();

        Long userId = service.createUser(userProfile);

        assertEquals(2L, userId);
        assertEquals(1, service.items.size());
        assertEquals(5, service.items.getFirst().getDailyPushLimit());
        assertEquals(6, service.items.getFirst().getPushCooldownHours());
    }

    private static class InMemoryUserProfileService extends UserProfileServiceImpl {

        private final List<UserProfile> items = new ArrayList<>();

        @Override
        public boolean save(UserProfile entity) {
            items.add(entity);
            return true;
        }

        @Override
        public List<UserProfile> list(com.mybatisflex.core.query.QueryWrapper queryWrapper) {
            return items.stream().filter(item -> item.getStatus() != null && item.getStatus() == 1).toList();
        }
    }
}
