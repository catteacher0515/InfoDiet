package com.pingyu.infodiet.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 信息节食配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "info-diet")
public class InfoDietProperties {

    /**
     * 关键词列表
     */
    private List<String> keywords = new ArrayList<>();
}
