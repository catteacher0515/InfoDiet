package com.pingyu.infodiet.config;

import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.interceptor.CacheErrorHandler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class CacheFailureTolerantConfigTest {

    @Test
    void errorHandlerShouldSwallowCacheReadWriteFailures() {
        CacheFailureTolerantConfig config = new CacheFailureTolerantConfig();
        CacheErrorHandler handler = config.errorHandler();
        Cache cache = new ConcurrentMapCache("enabledUsers");
        RuntimeException exception = new RuntimeException("mock redis serialization failure");

        assertDoesNotThrow(() -> handler.handleCacheGetError(exception, cache, "all"));
        assertDoesNotThrow(() -> handler.handleCachePutError(exception, cache, "all", "value"));
        assertDoesNotThrow(() -> handler.handleCacheEvictError(exception, cache, "all"));
        assertDoesNotThrow(() -> handler.handleCacheClearError(exception, cache));
    }
}
