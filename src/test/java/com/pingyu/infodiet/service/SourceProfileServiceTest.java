package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.entity.SourceProfile;
import com.pingyu.infodiet.model.entity.UserSourceSubscription;
import com.pingyu.infodiet.service.impl.SourceProfileServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SourceProfileServiceTest {

    @Test
    void resolveOrCreateBySubscriptionShouldCreateYoutubeChannelProfile() {
        InMemorySourceProfileService service = new InMemorySourceProfileService();

        SourceProfile sourceProfile = service.resolveOrCreateBySubscription(UserSourceSubscription.builder()
                .platform("youtube")
                .sourceType("channel")
                .sourceValue("UC123456")
                .build());

        assertNotNull(sourceProfile);
        assertEquals("youtube", sourceProfile.getPlatform());
        assertEquals("channel", sourceProfile.getProfileType());
        assertEquals("UC123456", sourceProfile.getSourceKey());
        assertEquals("normal", sourceProfile.getSourceCategory());
        assertEquals("T2", sourceProfile.getSourceTier());
    }

    @Test
    void resolveOrCreateByContentShouldReuseExistingProfile() {
        InMemorySourceProfileService service = new InMemorySourceProfileService();
        SourceProfile existing = SourceProfile.builder()
                .id(1L)
                .platform("github")
                .profileType("author")
                .sourceKey("openai")
                .sourceName("openai")
                .sourceUrl("https://github.com/openai")
                .sourceCategory("official")
                .sourceTier("T1")
                .status(1)
                .build();
        service.items.add(existing);

        SourceProfile sourceProfile = service.resolveOrCreateByContent(
                "github", "author", "openai", "openai", "https://github.com/openai"
        );

        assertEquals(1L, sourceProfile.getId());
        assertEquals(1, service.items.size());
    }

    private static class InMemorySourceProfileService extends SourceProfileServiceImpl {

        private final List<SourceProfile> items = new ArrayList<>();
        private long nextId = 1L;

        @Override
        public boolean save(SourceProfile entity) {
            if (entity.getId() == null) {
                entity.setId(nextId++);
            }
            items.add(entity);
            return true;
        }

        @Override
        public boolean updateById(SourceProfile entity) {
            for (SourceProfile item : items) {
                if (item.getId().equals(entity.getId())) {
                    if (entity.getSourceName() != null) {
                        item.setSourceName(entity.getSourceName());
                    }
                    if (entity.getSourceUrl() != null) {
                        item.setSourceUrl(entity.getSourceUrl());
                    }
                    return true;
                }
            }
            return false;
        }

        @Override
        protected SourceProfile getByPlatformAndProfileTypeAndSourceKey(String platform, String profileType, String sourceKey) {
            return items.stream()
                    .filter(item -> platform.equals(item.getPlatform()))
                    .filter(item -> profileType.equals(item.getProfileType()))
                    .filter(item -> sourceKey.equals(item.getSourceKey()))
                    .findFirst()
                    .orElse(null);
        }
    }
}
