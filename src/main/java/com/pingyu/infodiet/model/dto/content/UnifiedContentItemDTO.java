package com.pingyu.infodiet.model.dto.content;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 统一内容视图
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedContentItemDTO {

    /**
     * 内容 ID
     */
    private Long id;

    /**
     * 平台
     */
    private String platform;

    /**
     * 平台内唯一标识
     */
    private String sourceId;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容类型
     */
    private String contentType;

    /**
     * 描述
     */
    private String description;

    /**
     * 内容链接
     */
    private String contentUrl;

    /**
     * 作者
     */
    private String authorName;

    /**
     * 作者链接
     */
    private String authorUrl;

    /**
     * 平台主指标值
     */
    private Integer primaryMetricValue;

    /**
     * 平台主指标标签
     */
    private String primaryMetricLabel;

    /**
     * 平台次指标值
     */
    private Integer secondaryMetricValue;

    /**
     * 平台次指标标签
     */
    private String secondaryMetricLabel;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 抓取时间
     */
    private LocalDateTime crawlTime;

    /**
     * 排序时间
     */
    private LocalDateTime sortTime;

    /**
     * 去重键
     */
    private String dedupKey;
}
