package com.pingyu.infodiet.config;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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
}
