package com.pingyu.infodiet.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.pingyu.infodiet.model.entity.ContentItem;
import com.pingyu.infodiet.service.ContentItemService;
import com.pingyu.infodiet.service.ContentScoringService;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 内容评分服务实现
 */
@Service
public class ContentScoringServiceImpl implements ContentScoringService {

    @Resource
    private ContentItemService contentItemService;

    /**
     * 执行内容评分
     */
    @Override
    @CacheEvict(cacheNames = {"unifiedContentItems", "matchEnabledUsersWithDetails"}, allEntries = true)
    public ContentItemService.QualityScoreResult runQualityScoring() {
        List<ContentItem> contentItems = listScoreableItems();
        if (CollUtil.isEmpty(contentItems)) {
            return new ContentItemService.QualityScoreResult(0, 0, 0);
        }
        int scoredCount = 0;
        int skippedCount = 0;
        for (ContentItem contentItem : contentItems) {
            ScoreDetail scoreDetail = evaluate(contentItem);
            if (scoreDetail == null) {
                skippedCount++;
                continue;
            }
            contentItem.setQualityScore(scoreDetail.score());
            contentItem.setQualityScoreReason(scoreDetail.reason());
            if (updateById(contentItem)) {
                scoredCount++;
            } else {
                skippedCount++;
            }
        }
        return new ContentItemService.QualityScoreResult(contentItems.size(), scoredCount, skippedCount);
    }

    /**
     * 计算单条内容质量分
     */
    @Override
    public ScoreDetail evaluate(ContentItem contentItem) {
        if (contentItem == null) {
            return new ScoreDetail(0, "内容为空");
        }
        List<String> reasonParts = new ArrayList<>();
        int score = 0;

        int tierScore = resolveTierScore(contentItem.getSourceTier());
        score += tierScore;
        reasonParts.add("tier:" + StrUtil.blankToDefault(contentItem.getSourceTier(), "default") + "=" + tierScore);

        int categoryScore = resolveCategoryScore(contentItem.getSourceCategory());
        score += categoryScore;
        reasonParts.add("category:" + StrUtil.blankToDefault(contentItem.getSourceCategory(), "normal") + "=" + categoryScore);

        int metricScore = resolveMetricScore(contentItem);
        score += metricScore;
        reasonParts.add("metric=" + metricScore);

        int recencyScore = resolveRecencyScore(contentItem);
        score += recencyScore;
        reasonParts.add("recency=" + recencyScore);

        int finalScore = Math.min(score, 100);
        reasonParts.add("final=" + finalScore);
        return new ScoreDetail(finalScore, String.join("; ", reasonParts));
    }

    /**
     * 查询待评分内容
     */
    protected List<ContentItem> listScoreableItems() {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("isDelete", 0)
                .eq("preFilterStatus", 1);
        return contentItemService.list(queryWrapper);
    }

    /**
     * 更新评分结果
     */
    protected boolean updateById(ContentItem contentItem) {
        return contentItemService.updateById(contentItem);
    }

    /**
     * 解析信源等级分
     */
    protected int resolveTierScore(String sourceTier) {
        String normalizedTier = normalize(sourceTier);
        return switch (normalizedTier) {
            case "t1" -> 40;
            case "t1.5" -> 32;
            case "t2" -> 24;
            default -> 16;
        };
    }

    /**
     * 解析信源分类分
     */
    protected int resolveCategoryScore(String sourceCategory) {
        String normalizedCategory = normalize(sourceCategory);
        return switch (normalizedCategory) {
            case "official" -> 20;
            case "official_social" -> 16;
            case "media" -> 12;
            case "kol" -> 8;
            default -> 4;
        };
    }

    /**
     * 解析平台指标分
     */
    protected int resolveMetricScore(ContentItem contentItem) {
        String platform = normalize(contentItem.getPlatform());
        if ("github".equals(platform)) {
            return resolveGithubMetricScore(contentItem);
        }
        if ("youtube".equals(platform)) {
            return resolveYoutubeMetricScore(contentItem);
        }
        return 0;
    }

    /**
     * 解析 GitHub 指标分
     */
    protected int resolveGithubMetricScore(ContentItem contentItem) {
        int todayStars = defaultInt(contentItem.getTodayStarCount());
        int totalStars = defaultInt(contentItem.getStarCount());
        int todayScore;
        if (todayStars >= 2000) {
            todayScore = 25;
        } else if (todayStars >= 1000) {
            todayScore = 22;
        } else if (todayStars >= 300) {
            todayScore = 18;
        } else if (todayStars >= 100) {
            todayScore = 12;
        } else if (todayStars > 0) {
            todayScore = 6;
        } else {
            todayScore = 0;
        }

        int totalScore;
        if (totalStars >= 50000) {
            totalScore = 10;
        } else if (totalStars >= 10000) {
            totalScore = 8;
        } else if (totalStars >= 1000) {
            totalScore = 5;
        } else if (totalStars > 0) {
            totalScore = 2;
        } else {
            totalScore = 0;
        }
        return todayScore + totalScore;
    }

    /**
     * 解析 YouTube 指标分
     */
    protected int resolveYoutubeMetricScore(ContentItem contentItem) {
        int viewCount = defaultInt(contentItem.getViewCount());
        if (viewCount >= 100000) {
            return 25;
        }
        if (viewCount >= 50000) {
            return 20;
        }
        if (viewCount >= 10000) {
            return 15;
        }
        if (viewCount >= 1000) {
            return 8;
        }
        if (viewCount > 0) {
            return 4;
        }
        return 0;
    }

    /**
     * 解析时效分
     */
    protected int resolveRecencyScore(ContentItem contentItem) {
        LocalDateTime sortTime = contentItem.getPublishTime() != null
                ? contentItem.getPublishTime()
                : contentItem.getCrawlTime();
        if (sortTime == null) {
            return 0;
        }
        long hours = Math.abs(Duration.between(sortTime, now()).toHours());
        if (hours <= 24) {
            return 10;
        }
        if (hours <= 72) {
            return 6;
        }
        if (hours <= 168) {
            return 3;
        }
        return 0;
    }

    /**
     * 标准化文本
     */
    protected String normalize(String value) {
        return StrUtil.blankToDefault(StrUtil.trim(value), "").toLowerCase(Locale.ROOT);
    }

    /**
     * 默认整数
     */
    protected int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    /**
     * 当前时间
     */
    protected LocalDateTime now() {
        return LocalDateTime.now();
    }
}
