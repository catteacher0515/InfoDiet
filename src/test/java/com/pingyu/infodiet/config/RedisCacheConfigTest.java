package com.pingyu.infodiet.config;

import com.pingyu.infodiet.model.entity.UserProfile;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RedisCacheConfigTest {

    @Test
    void serializerShouldSupportLocalDateTime() {
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(
                new com.fasterxml.jackson.databind.ObjectMapper()
                        .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                        .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        );

        byte[] bytes = serializer.serialize(Map.of("publishTime", LocalDateTime.of(2026, 5, 11, 23, 30)));

        assertNotNull(bytes);
    }

    @Test
    void serializerShouldKeepEntityTypeInformation() {
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper()
                .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        GenericJackson2JsonRedisSerializer serializer = GenericJackson2JsonRedisSerializer.builder()
                .defaultTyping(true)
                .objectMapper(objectMapper)
                .build();

        List<UserProfile> source = new ArrayList<>();
        source.add(UserProfile.builder().id(1L).nickname("test-user").pushChannel("feishu").status(1).build());

        byte[] bytes = serializer.serialize(source);
        Object result = serializer.deserialize(bytes);

        assertTrue(result instanceof List<?>);
        assertTrue(((List<?>) result).getFirst() instanceof UserProfile);
        assertEquals("test-user", ((UserProfile) ((List<?>) result).getFirst()).getNickname());
    }
}
