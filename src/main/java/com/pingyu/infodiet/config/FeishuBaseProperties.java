package com.pingyu.infodiet.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 飞书多维表格配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "feishu.base")
public class FeishuBaseProperties {

    /**
     * 飞书应用 ID
     */
    private String appId;

    /**
     * 飞书应用密钥
     */
    private String appSecret;

    /**
     * 多维表格 appToken
     */
    private String appToken;

    /**
     * 多维表格 tableId
     */
    private String tableId;
}
