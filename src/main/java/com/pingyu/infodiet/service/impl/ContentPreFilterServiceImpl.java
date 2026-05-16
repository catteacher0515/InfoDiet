package com.pingyu.infodiet.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.pingyu.infodiet.config.InfoDietProperties;
import com.pingyu.infodiet.model.entity.ContentItem;
import com.pingyu.infodiet.service.ContentItemService;
import com.pingyu.infodiet.service.ContentPreFilterService;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

/**
 * 内容预筛服务实现
 */
@Service
public class ContentPreFilterServiceImpl implements ContentPreFilterService {

    private static final int STATUS_PENDING = 0;
    private static final int STATUS_PASSED = 1;
    private static final int STATUS_FILTERED = 2;

    @Resource
    private ContentItemService contentItemService;

    @Resource
    protected InfoDietProperties infoDietProperties;

    /**
     * 按系统配置执行预筛
     */
    @Override
    public ContentItemService.PreFilterResult runSystemPreFilter() {
        List<String> includeKeywords = infoDietProperties == null ? List.of() : infoDietProperties.getKeywords();
        List<String> excludeKeywords = infoDietProperties == null ? List.of() : infoDietProperties.getPreFilterExcludeKeywords();
        return runPreFilter(includeKeywords, excludeKeywords);
    }

    /**
     * 按指定规则执行预筛
     */
    @Override
    @CacheEvict(cacheNames = {"unifiedContentItems", "matchEnabledUsersWithDetails"}, allEntries = true)
    public ContentItemService.PreFilterResult runPreFilter(List<String> includeKeywords, List<String> excludeKeywords) {
        List<ContentItem> contentItems = listPendingPreFilterItems();
        if (CollUtil.isEmpty(contentItems)) {
            return new ContentItemService.PreFilterResult(0, 0, 0, 0);
        }
        int passedCount = 0;
        int filteredCount = 0;
        int skippedCount = 0;
        for (ContentItem contentItem : contentItems) {
            PreFilterDecision decision = evaluate(contentItem, includeKeywords, excludeKeywords);
            if (decision == null) {
                skippedCount++;
                continue;
            }
            contentItem.setPreFilterStatus(decision.status());
            contentItem.setPreFilterReason(decision.reason());
            updateById(contentItem);
            if (decision.status() == STATUS_PASSED) {
                passedCount++;
            } else if (decision.status() == STATUS_FILTERED) {
                filteredCount++;
            } else {
                skippedCount++;
            }
        }
        return new ContentItemService.PreFilterResult(contentItems.size(), passedCount, filteredCount, skippedCount);
    }

    /**
     * 评估单条内容预筛结果
     */
    @Override
    public PreFilterDecision evaluate(ContentItem contentItem, List<String> includeKeywords, List<String> excludeKeywords) {
        if (contentItem == null) {
            return new PreFilterDecision(STATUS_PENDING, "内容为空");
        }
        String contentText = buildContentText(contentItem);
        String excludeKeyword = findFirstMatchedKeyword(contentText, excludeKeywords);
        if (StrUtil.isNotBlank(excludeKeyword)) {
            return new PreFilterDecision(STATUS_FILTERED, "命中排除关键词: " + excludeKeyword);
        }
        if (CollUtil.isEmpty(includeKeywords)) {
            return new PreFilterDecision(STATUS_PASSED, "未配置包含关键词，默认通过");
        }
        String includeKeyword = findFirstMatchedKeyword(contentText, includeKeywords);
        if (StrUtil.isNotBlank(includeKeyword)) {
            return new PreFilterDecision(STATUS_PASSED, "命中包含关键词: " + includeKeyword);
        }
        return new PreFilterDecision(STATUS_FILTERED, "未命中包含关键词");
    }

    /**
     * 查询待预筛内容
     */
    protected List<ContentItem> listPendingPreFilterItems() {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("isDelete", 0)
                .eq("preFilterStatus", STATUS_PENDING);
        return contentItemService.list(queryWrapper);
    }

    /**
     * 更新预筛结果
     */
    protected boolean updateById(ContentItem contentItem) {
        return contentItemService.updateById(contentItem);
    }

    /**
     * 构建检索文本
     */
    protected String buildContentText(ContentItem contentItem) {
        return (StrUtil.blankToDefault(contentItem.getTitle(), "") + " "
                + StrUtil.blankToDefault(contentItem.getDescription(), "") + " "
                + StrUtil.blankToDefault(contentItem.getAuthorName(), "") + " "
                + StrUtil.blankToDefault(contentItem.getSourceCategory(), "") + " "
                + StrUtil.blankToDefault(contentItem.getSourceTier(), ""))
                .toLowerCase(Locale.ROOT);
    }

    /**
     * 查找首个命中关键词
     */
    protected String findFirstMatchedKeyword(String contentText, List<String> keywords) {
        if (StrUtil.isBlank(contentText) || CollUtil.isEmpty(keywords)) {
            return null;
        }
        for (String keyword : keywords) {
            if (StrUtil.isBlank(keyword)) {
                continue;
            }
            String safeKeyword = keyword.trim().toLowerCase(Locale.ROOT);
            if (contentText.contains(safeKeyword)) {
                return safeKeyword;
            }
        }
        return null;
    }
}
