package com.pingyu.infodiet.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.pingyu.infodiet.mapper.ContentItemMapper;
import com.pingyu.infodiet.model.dto.github.GithubTrendingItemDTO;
import com.pingyu.infodiet.model.entity.ContentItem;
import com.pingyu.infodiet.model.enums.ContentPlatformEnum;
import com.pingyu.infodiet.service.ContentItemService;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

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
     * 批量保存 GitHub Trending 抓取结果
     */
    @Override
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

}
