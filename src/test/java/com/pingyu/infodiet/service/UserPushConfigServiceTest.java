package com.pingyu.infodiet.service;

import com.pingyu.infodiet.exception.BusinessException;
import com.pingyu.infodiet.model.dto.user.UserPushConfigRequest;
import com.pingyu.infodiet.model.entity.UserProfile;
import com.pingyu.infodiet.service.impl.UserProfileServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class UserPushConfigServiceTest {

    @Test
    void updateUserPushConfigShouldUpdateOnlyPushFields() {
        InMemoryPushConfigUserProfileService service = new InMemoryPushConfigUserProfileService();
        service.items.add(UserProfile.builder()
                .id(1L)
                .nickname("old-name")
                .username("old-user")
                .pushChannel("feishu")
                .dailyPushLimit(3)
                .pushCooldownHours(12)
                .feishuUserId("ou_old")
                .status(1)
                .build());

        boolean updated = service.updateUserPushConfig(1L, buildRequest("  ou_new  ", "feishu", 9, 0));

        assertEquals(true, updated);
        assertEquals("old-name", service.items.getFirst().getNickname());
        assertEquals("old-user", service.items.getFirst().getUsername());
        assertEquals("ou_new", service.items.getFirst().getFeishuUserId());
        assertEquals("feishu", service.items.getFirst().getPushChannel());
        assertEquals(9, service.items.getFirst().getDailyPushLimit());
        assertEquals(0, service.items.getFirst().getPushCooldownHours());
    }

    @Test
    void updateUserPushConfigShouldAllowBlankFeishuUserIdToClear() {
        InMemoryPushConfigUserProfileService service = new InMemoryPushConfigUserProfileService();
        service.items.add(UserProfile.builder()
                .id(1L)
                .feishuUserId("ou_old")
                .pushChannel("feishu")
                .dailyPushLimit(3)
                .pushCooldownHours(12)
                .status(1)
                .build());

        boolean updated = service.updateUserPushConfig(1L, buildRequest("   ", "feishu", 5, 1));

        assertEquals(true, updated);
        assertEquals(null, service.items.getFirst().getFeishuUserId());
        assertEquals(5, service.items.getFirst().getDailyPushLimit());
        assertEquals(1, service.items.getFirst().getPushCooldownHours());
    }

    @Test
    void updateUserPushConfigShouldRejectUnsupportedChannel() {
        InMemoryPushConfigUserProfileService service = new InMemoryPushConfigUserProfileService();
        service.items.add(UserProfile.builder().id(1L).status(1).build());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.updateUserPushConfig(1L, buildRequest("ou_x", "email", 5, 1)));

        assertEquals("仅支持 feishu 推送渠道", exception.getMessage());
    }

    @Test
    void updateUserPushConfigShouldRejectInvalidDailyLimit() {
        InMemoryPushConfigUserProfileService service = new InMemoryPushConfigUserProfileService();
        service.items.add(UserProfile.builder().id(1L).status(1).build());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.updateUserPushConfig(1L, buildRequest("ou_x", "feishu", 0, 1)));

        assertEquals("每日推送上限必须大于 0", exception.getMessage());
    }

    @Test
    void updateUserPushConfigShouldRejectNegativeCooldown() {
        InMemoryPushConfigUserProfileService service = new InMemoryPushConfigUserProfileService();
        service.items.add(UserProfile.builder().id(1L).status(1).build());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.updateUserPushConfig(1L, buildRequest("ou_x", "feishu", 5, -1)));

        assertEquals("推送冷却小时数不能小于 0", exception.getMessage());
    }

    private UserPushConfigRequest buildRequest(String feishuUserId, String pushChannel, Integer dailyPushLimit, Integer pushCooldownHours) {
        UserPushConfigRequest request = new UserPushConfigRequest();
        request.setFeishuUserId(feishuUserId);
        request.setPushChannel(pushChannel);
        request.setDailyPushLimit(dailyPushLimit);
        request.setPushCooldownHours(pushCooldownHours);
        return request;
    }

    private static class InMemoryPushConfigUserProfileService extends UserProfileServiceImpl {

        private final List<UserProfile> items = new ArrayList<>();

        @Override
        public UserProfile getById(java.io.Serializable id) {
            return items.stream()
                    .filter(item -> item.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        @SuppressWarnings("unchecked")
        public com.mybatisflex.core.update.UpdateChain<UserProfile> updateChain() {
            com.mybatisflex.core.update.UpdateChain<UserProfile> updateChain = mock(com.mybatisflex.core.update.UpdateChain.class);
            Map<String, Object> updates = new LinkedHashMap<>();
            Long[] targetId = new Long[1];
            doAnswer(invocation -> {
                updates.put(invocation.getArgument(0), invocation.getArgument(1));
                return updateChain;
            }).when(updateChain).set(anyString(), any());
            doAnswer(invocation -> {
                Object value = invocation.getArgument(1);
                if ("id = ?".equals(invocation.getArgument(0)) && value instanceof Long id) {
                    targetId[0] = id;
                }
                return updateChain;
            }).when(updateChain).where(anyString(), any());
            doAnswer(invocation -> {
                for (UserProfile item : items) {
                    if (targetId[0] == null || !item.getId().equals(targetId[0])) {
                        continue;
                    }
                    item.setFeishuUserId((String) updates.get("feishuUserId"));
                    item.setPushChannel((String) updates.get("pushChannel"));
                    item.setDailyPushLimit((Integer) updates.get("dailyPushLimit"));
                    item.setPushCooldownHours((Integer) updates.get("pushCooldownHours"));
                    return true;
                }
                return false;
            }).when(updateChain).update();
            return updateChain;
        }
    }
}
