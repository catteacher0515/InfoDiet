package com.pingyu.infodiet.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "info-diet.jwt")
public class JwtProperties {

    /**
     * 签名密钥
     */
    private String secret = "info-diet-default-secret";

    /**
     * 过期时间，单位秒
     */
    private long expireSeconds = 604800L;
}
