package com.pingyu.infodiet.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Configuration;

/**
 * 缓存容错配置
 */
@Configuration
@Slf4j
public class CacheFailureTolerantConfig implements CachingConfigurer {

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Redis 缓存读取失败，已回退到方法直连，cache={}, key={}", cacheName(cache), key, exception);
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("Redis 缓存写入失败，已忽略缓存写入，cache={}, key={}", cacheName(cache), key, exception);
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Redis 缓存删除失败，cache={}, key={}", cacheName(cache), key, exception);
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn("Redis 缓存清空失败，cache={}", cacheName(cache), exception);
            }
        };
    }

    private String cacheName(Cache cache) {
        return cache == null ? "unknown" : cache.getName();
    }
}
