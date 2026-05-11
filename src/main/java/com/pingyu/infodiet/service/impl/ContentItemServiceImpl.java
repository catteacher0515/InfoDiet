package com.pingyu.infodiet.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.pingyu.infodiet.mapper.ContentItemMapper;
import com.pingyu.infodiet.model.dto.content.UnifiedContentItemDTO;
import com.pingyu.infodiet.model.dto.content.UnifiedContentQueryRequest;
import com.pingyu.infodiet.model.dto.github.GithubTrendingItemDTO;
import com.pingyu.infodiet.model.dto.youtube.YoutubeVideoItemDTO;
import com.pingyu.infodiet.model.entity.ContentItem;
import com.pingyu.infodiet.model.enums.ContentPlatformEnum;
import com.pingyu.infodiet.service.ContentItemService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 内容抓取表 服务层实现。
 *
 * @author pingyu
 */
@Service
public class ContentItemServiceImpl extends ServiceImpl<ContentItemMapper, ContentItem> implements ContentItemService {

    /**
     * 将 DTO 转换为 ContentItem
     */
    @Override
    public ContentItem convertGithubTrendingItem(GithubTrendingItemDTO dto) {
        LocalDateTime now = now();
        return ContentItem.builder()
                .platform(ContentPlatformEnum.GITHUB.getValue())
                .sourceId(dto.getRepoFullName())
                .title(dto.getRepoName())
                .description(dto.getDescription())
                .contentUrl(dto.getRepoUrl())
                .authorName(dto.getAuthorName())
                .authorUrl(dto.getAuthorUrl())
                .language(dto.getLanguage())
                .starCount(dto.getStarCount())
                .todayStarCount(dto.getTodayStarCount())
                .keywordMatched(0)
                .pushStatus(0)
                .crawlDate(Date.valueOf(now.toLocalDate()))
                .crawlTime(now)
                .build();
    }

    /**
     * 将 YouTube DTO 转换为 ContentItem
     */
    @Override
    public ContentItem convertYoutubeVideoItem(YoutubeVideoItemDTO dto) {
        LocalDateTime now = now();
        return ContentItem.builder()
                .platform(ContentPlatformEnum.YOUTUBE.getValue())
                .sourceId(dto.getVideoId())
                .title(dto.getTitle())
                .contentType("video")
                .description(dto.getDescription())
                .contentUrl(dto.getVideoUrl())
                .authorName(dto.getAuthorName())
                .authorUrl(dto.getAuthorUrl())
                .viewCount(0)
                .publishTime(dto.getPublishTime())
                .keywordMatched(0)
                .pushStatus(0)
                .crawlDate(Date.valueOf(now.toLocalDate()))
                .crawlTime(now)
                .build();
    }

    /**
     * 批量保存 GitHub Trending 抓取结果
     */
    @Override
    @CacheEvict(cacheNames = {"unifiedContentItems", "matchEnabledUsersWithDetails"}, allEntries = true)
    public SaveResult saveGithubTrendingItems(List<GithubTrendingItemDTO> dtoList) {
        if (CollUtil.isEmpty(dtoList)) {
            return new SaveResult(0, 0, 0);
        }
        int savedCount = 0;
        int skippedCount = 0;
        for (GithubTrendingItemDTO dto : dtoList) {
            ContentItem contentItem = convertGithubTrendingItem(dto);
            boolean exists = existsByPlatformAndSourceIdAndCrawlDate(
                    contentItem.getPlatform(),
                    contentItem.getSourceId(),
                    contentItem.getCrawlDate()
            );
            if (exists) {
                skippedCount++;
                continue;
            }
            boolean saved = this.save(contentItem);
            if (saved) {
                savedCount++;
            }
        }
        return new SaveResult(dtoList.size(), savedCount, skippedCount);
    }

    /**
     * 批量保存 YouTube 抓取结果
     */
    @Override
    @CacheEvict(cacheNames = {"unifiedContentItems", "matchEnabledUsersWithDetails"}, allEntries = true)
    public SaveResult saveYoutubeVideoItems(List<YoutubeVideoItemDTO> dtoList) {
        if (CollUtil.isEmpty(dtoList)) {
            return new SaveResult(0, 0, 0);
        }
        int savedCount = 0;
        int skippedCount = 0;
        for (YoutubeVideoItemDTO dto : dtoList) {
            ContentItem contentItem = convertYoutubeVideoItem(dto);
            boolean exists = existsByPlatformAndSourceIdAndCrawlDate(
                    contentItem.getPlatform(),
                    contentItem.getSourceId(),
                    contentItem.getCrawlDate()
            );
            if (exists) {
                skippedCount++;
                continue;
            }
            boolean saved = this.save(contentItem);
            if (saved) {
                savedCount++;
            }
        }
        return new SaveResult(dtoList.size(), savedCount, skippedCount);
    }

    /**
     * 判断内容是否命中关键词
     */
    @Override
    public boolean matchKeywords(ContentItem contentItem, List<String> keywords) {
        if (contentItem == null || CollUtil.isEmpty(keywords)) {
            return false;
        }
        String title = StrUtil.blankToDefault(contentItem.getTitle(), "");
        String description = StrUtil.blankToDefault(contentItem.getDescription(), "");
        String contentText = (title + " " + description).toLowerCase(Locale.ROOT);
        for (String keyword : keywords) {
            if (StrUtil.isBlank(keyword)) {
                continue;
            }
            if (contentText.contains(keyword.trim().toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据关键词批量过滤内容
     */
    @Override
    @CacheEvict(cacheNames = {"unifiedContentItems", "matchEnabledUsersWithDetails"}, allEntries = true)
    public KeywordFilterResult filterByKeywords(List<String> keywords) {
        List<ContentItem> contentItems = listUnmatchedItems();
        if (CollUtil.isEmpty(contentItems)) {
            return new KeywordFilterResult(0, 0, 0);
        }
        int matchedCount = 0;
        int unmatchedCount = 0;
        for (ContentItem contentItem : contentItems) {
            boolean matched = matchKeywords(contentItem, keywords);
            if (matched) {
                contentItem.setKeywordMatched(1);
                this.updateById(contentItem);
                matchedCount++;
            } else {
                unmatchedCount++;
            }
        }
        return new KeywordFilterResult(contentItems.size(), matchedCount, unmatchedCount);
    }

    /**
     * 转换为统一内容视图
     */
    @Override
    public UnifiedContentItemDTO convertToUnifiedContentItem(ContentItem contentItem) {
        if (contentItem == null) {
            return null;
        }
        return UnifiedContentItemDTO.builder()
                .id(contentItem.getId())
                .platform(contentItem.getPlatform())
                .sourceId(contentItem.getSourceId())
                .title(contentItem.getTitle())
                .contentType(resolveContentType(contentItem))
                .description(contentItem.getDescription())
                .contentUrl(contentItem.getContentUrl())
                .authorName(contentItem.getAuthorName())
                .authorUrl(contentItem.getAuthorUrl())
                .primaryMetricValue(resolvePrimaryMetricValue(contentItem))
                .primaryMetricLabel(resolvePrimaryMetricLabel(contentItem))
                .secondaryMetricValue(resolveSecondaryMetricValue(contentItem))
                .secondaryMetricLabel(resolveSecondaryMetricLabel(contentItem))
                .publishTime(contentItem.getPublishTime())
                .crawlTime(contentItem.getCrawlTime())
                .sortTime(resolveSortTime(contentItem))
                .dedupKey(buildDedupKey(contentItem))
                .build();
    }

    /**
     * 查询统一内容列表
     */
    @Override
    public List<UnifiedContentItemDTO> listUnifiedContentItems() {
        return listUnifiedContentItems(new UnifiedContentQueryRequest());
    }

    /**
     * 按条件查询统一内容列表
     */
    @Override
    @Cacheable(
            cacheNames = "unifiedContentItems",
            key = "T(java.util.Objects).toString(#request == null ? 'default' : #request.platform + '|' + #request.contentType + '|' + #request.sortBy + '|' + #request.limit)"
    )
    public List<UnifiedContentItemDTO> listUnifiedContentItems(UnifiedContentQueryRequest request) {
        List<ContentItem> contentItems = this.list(QueryWrapper.create().eq("isDelete", 0));
        if (CollUtil.isEmpty(contentItems)) {
            return List.of();
        }
        List<UnifiedContentItemDTO> unifiedItems = contentItems.stream()
                .map(this::convertToUnifiedContentItem)
                .filter(item -> item != null && StrUtil.isNotBlank(item.getDedupKey()))
                .toList();
        return filterAndSortUnifiedItems(deduplicateAndSortUnifiedItems(unifiedItems), request);
    }

    /**
     * 获取当前时间
     */
    protected LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * 判断当天内容是否已存在
     */
    protected boolean existsByPlatformAndSourceIdAndCrawlDate(String platform, String sourceId, Date crawlDate) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("platform", platform)
                .eq("sourceId", sourceId)
                .eq("crawlDate", crawlDate);
        return this.mapper.selectCountByQuery(queryWrapper) > 0;
    }

    /**
     * 查询待筛选内容
     */
    protected List<ContentItem> listUnmatchedItems() {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("keywordMatched", 0);
        return this.mapper.selectListByQuery(queryWrapper);
    }

    /**
     * 解析内容类型
     */
    protected String resolveContentType(ContentItem contentItem) {
        if (StrUtil.isNotBlank(contentItem.getContentType())) {
            return contentItem.getContentType();
        }
        if (ContentPlatformEnum.GITHUB.getValue().equals(contentItem.getPlatform())) {
            return "repository";
        }
        return "content";
    }

    /**
     * 解析主指标值
     */
    protected Integer resolvePrimaryMetricValue(ContentItem contentItem) {
        if (ContentPlatformEnum.GITHUB.getValue().equals(contentItem.getPlatform())) {
            return defaultInt(contentItem.getStarCount());
        }
        if (ContentPlatformEnum.YOUTUBE.getValue().equals(contentItem.getPlatform())) {
            return defaultInt(contentItem.getViewCount());
        }
        return 0;
    }

    /**
     * 解析主指标标签
     */
    protected String resolvePrimaryMetricLabel(ContentItem contentItem) {
        if (ContentPlatformEnum.GITHUB.getValue().equals(contentItem.getPlatform())) {
            return "stars";
        }
        if (ContentPlatformEnum.YOUTUBE.getValue().equals(contentItem.getPlatform())) {
            return "views";
        }
        return "metric";
    }

    /**
     * 解析次指标值
     */
    protected Integer resolveSecondaryMetricValue(ContentItem contentItem) {
        if (ContentPlatformEnum.GITHUB.getValue().equals(contentItem.getPlatform())) {
            return defaultInt(contentItem.getTodayStarCount());
        }
        return 0;
    }

    /**
     * 解析次指标标签
     */
    protected String resolveSecondaryMetricLabel(ContentItem contentItem) {
        if (ContentPlatformEnum.GITHUB.getValue().equals(contentItem.getPlatform())) {
            return "todayStars";
        }
        return "";
    }

    /**
     * 解析排序时间
     */
    protected LocalDateTime resolveSortTime(ContentItem contentItem) {
        return contentItem.getPublishTime() != null ? contentItem.getPublishTime() : contentItem.getCrawlTime();
    }

    /**
     * 构建去重键
     */
    protected String buildDedupKey(ContentItem contentItem) {
        if (contentItem == null) {
            return "";
        }
        String normalizedTitle = normalizeText(contentItem.getTitle());
        String normalizedAuthor = normalizeText(contentItem.getAuthorName());
        if (StrUtil.isNotBlank(normalizedTitle) && StrUtil.isNotBlank(normalizedAuthor)) {
            return normalizedTitle + "#" + normalizedAuthor;
        }
        String normalizedPlatform = normalizeText(contentItem.getPlatform());
        String normalizedSourceId = normalizeText(contentItem.getSourceId());
        if (StrUtil.isNotBlank(normalizedPlatform) && StrUtil.isNotBlank(normalizedSourceId)) {
            return normalizedPlatform + "#" + normalizedSourceId;
        }
        return "";
    }

    /**
     * 去重并排序统一内容
     */
    protected List<UnifiedContentItemDTO> deduplicateAndSortUnifiedItems(List<UnifiedContentItemDTO> items) {
        Map<String, UnifiedContentItemDTO> itemMap = new LinkedHashMap<>();
        for (UnifiedContentItemDTO item : items) {
            UnifiedContentItemDTO existing = itemMap.get(item.getDedupKey());
            if (existing == null || compareUnifiedItem(item, existing) < 0) {
                itemMap.put(item.getDedupKey(), item);
            }
        }
        List<UnifiedContentItemDTO> result = new ArrayList<>(itemMap.values());
        result.sort(Comparator
                .comparing(UnifiedContentItemDTO::getSortTime,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(UnifiedContentItemDTO::getPrimaryMetricValue,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(UnifiedContentItemDTO::getId,
                        Comparator.nullsLast(Comparator.reverseOrder())));
        return result;
    }

    /**
     * 过滤并排序统一内容
     */
    protected List<UnifiedContentItemDTO> filterAndSortUnifiedItems(
            List<UnifiedContentItemDTO> items,
            UnifiedContentQueryRequest request
    ) {
        if (CollUtil.isEmpty(items)) {
            return List.of();
        }
        UnifiedContentQueryRequest safeRequest = request == null ? new UnifiedContentQueryRequest() : request;
        List<UnifiedContentItemDTO> filteredItems = items.stream()
                .filter(item -> matchPlatform(item, safeRequest.getPlatform()))
                .filter(item -> matchContentType(item, safeRequest.getContentType()))
                .sorted(buildUnifiedSortComparator(safeRequest.getSortBy()))
                .toList();
        Integer limit = safeRequest.getLimit();
        if (limit == null || limit <= 0 || filteredItems.size() <= limit) {
            return filteredItems;
        }
        return filteredItems.subList(0, limit);
    }

    /**
     * 比较统一内容优先级
     */
    protected int compareUnifiedItem(UnifiedContentItemDTO current, UnifiedContentItemDTO existing) {
        Comparator<UnifiedContentItemDTO> comparator = Comparator
                .comparing(UnifiedContentItemDTO::getSortTime,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(UnifiedContentItemDTO::getPrimaryMetricValue,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(UnifiedContentItemDTO::getId,
                        Comparator.nullsLast(Comparator.reverseOrder()));
        return comparator.compare(existing, current);
    }

    /**
     * 统一内容排序器
     */
    protected Comparator<UnifiedContentItemDTO> buildUnifiedSortComparator(String sortBy) {
        if (StrUtil.equalsIgnoreCase(StrUtil.trim(sortBy), "metric")) {
            return Comparator
                    .comparing(UnifiedContentItemDTO::getPrimaryMetricValue,
                            Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(UnifiedContentItemDTO::getSortTime,
                            Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(UnifiedContentItemDTO::getId,
                            Comparator.nullsLast(Comparator.reverseOrder()));
        }
        return Comparator
                .comparing(UnifiedContentItemDTO::getSortTime,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(UnifiedContentItemDTO::getPrimaryMetricValue,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(UnifiedContentItemDTO::getId,
                        Comparator.nullsLast(Comparator.reverseOrder()));
    }

    /**
     * 判断平台是否匹配
     */
    protected boolean matchPlatform(UnifiedContentItemDTO item, String platform) {
        return StrUtil.isBlank(platform) || StrUtil.equalsIgnoreCase(StrUtil.trim(item.getPlatform()), StrUtil.trim(platform));
    }

    /**
     * 判断内容类型是否匹配
     */
    protected boolean matchContentType(UnifiedContentItemDTO item, String contentType) {
        return StrUtil.isBlank(contentType) || StrUtil.equalsIgnoreCase(StrUtil.trim(item.getContentType()), StrUtil.trim(contentType));
    }

    /**
     * 默认整数值
     */
    protected Integer defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    /**
     * 标准化文本
     */
    protected String normalizeText(String value) {
        return StrUtil.blankToDefault(StrUtil.trim(value), "").toLowerCase(Locale.ROOT);
    }

}
