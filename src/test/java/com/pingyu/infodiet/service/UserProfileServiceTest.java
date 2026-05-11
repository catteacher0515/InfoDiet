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

    @Test
    void updateUserShouldSupportSettingCooldownToZero() {
        InMemoryUserProfileService service = new InMemoryUserProfileService();
        service.items.add(UserProfile.builder()
                .id(1L)
                .nickname("pingyu")
                .dailyPushLimit(3)
                .pushCooldownHours(24)
                .status(1)
                .build());

        boolean updated = service.updateUser(UserProfile.builder()
                .id(1L)
                .pushCooldownHours(0)
                .build());

        assertEquals(true, updated);
        assertEquals(0, service.items.getFirst().getPushCooldownHours());
        assertEquals(3, service.items.getFirst().getDailyPushLimit());
    }

    private static class InMemoryUserProfileService extends UserProfileServiceImpl {

        private final List<UserProfile> items = new ArrayList<>();

        @Override
        public boolean save(UserProfile entity) {
            items.add(entity);
            return true;
        }

        @Override
        public boolean updateById(UserProfile entity) {
            for (UserProfile item : items) {
                if (!item.getId().equals(entity.getId())) {
                    continue;
                }
                if (entity.getNickname() != null) {
                    item.setNickname(entity.getNickname());
                }
                if (entity.getFeishuUserId() != null) {
                    item.setFeishuUserId(entity.getFeishuUserId());
                }
                if (entity.getPushChannel() != null) {
                    item.setPushChannel(entity.getPushChannel());
                }
                if (entity.getDailyPushLimit() != null) {
                    item.setDailyPushLimit(entity.getDailyPushLimit());
                }
                if (entity.getPushCooldownHours() != null) {
                    item.setPushCooldownHours(entity.getPushCooldownHours());
                }
                if (entity.getStatus() != null) {
                    item.setStatus(entity.getStatus());
                }
                return true;
            }
            return false;
        }

        @Override
        public UserProfile getById(java.io.Serializable id) {
            return items.stream()
                    .filter(item -> item.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<UserProfile> list(com.mybatisflex.core.query.QueryWrapper queryWrapper) {
            return items.stream().filter(item -> item.getStatus() != null && item.getStatus() == 1).toList();
        }
    }
}
