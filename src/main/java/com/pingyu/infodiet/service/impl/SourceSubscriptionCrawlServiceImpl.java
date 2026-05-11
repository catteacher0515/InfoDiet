package com.pingyu.infodiet.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.pingyu.infodiet.model.dto.github.GithubTrendingItemDTO;
import com.pingyu.infodiet.model.dto.youtube.YoutubeVideoItemDTO;
import com.pingyu.infodiet.model.entity.UserSourceSubscription;
import com.pingyu.infodiet.service.ContentItemService;
import com.pingyu.infodiet.service.GithubTrendingService;
import com.pingyu.infodiet.service.SourceSubscriptionCrawlService;
import com.pingyu.infodiet.service.UserSourceSubscriptionService;
import com.pingyu.infodiet.service.YoutubeCrawlService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 订阅源采集服务实现。
 */
@Service
public class SourceSubscriptionCrawlServiceImpl implements SourceSubscriptionCrawlService {

    @Resource
    private UserSourceSubscriptionService userSourceSubscriptionService;

    @Resource
    private YoutubeCrawlService youtubeCrawlService;

    @Resource
    private GithubTrendingService githubTrendingService;

    @Resource
    private ContentItemService contentItemService;

    /**
     * 抓取 YouTube 频道订阅源
     */
    @Override
    public CrawlResult crawlYoutubeSourceSubscriptions() {
        List<UserSourceSubscription> enabledSubscriptions =
                userSourceSubscriptionService.listEnabledSourceSubscriptions();
        if (CollUtil.isEmpty(enabledSubscriptions)) {
            return new CrawlResult(0, 0, 0, 0);
        }

        List<UserSourceSubscription> youtubeChannelSubscriptions = enabledSubscriptions.stream()
                .filter(this::isYoutubeChannelSubscription)
                .toList();
        if (CollUtil.isEmpty(youtubeChannelSubscriptions)) {
            return new CrawlResult(0, 0, 0, 0);
        }

        List<YoutubeVideoItemDTO> allItems = new ArrayList<>();
        for (UserSourceSubscription subscription : youtubeChannelSubscriptions) {
            List<YoutubeVideoItemDTO> dtoList = youtubeCrawlService
                    .crawlYoutubeVideos(subscription.getSourceValue());
            if (CollUtil.isNotEmpty(dtoList)) {
                allItems.addAll(dtoList);
            }
        }
        List<YoutubeVideoItemDTO> deduplicatedItems = deduplicateYoutubeItems(allItems);
        ContentItemService.SaveResult saveResult = contentItemService.saveYoutubeVideoItems(deduplicatedItems);
        return new CrawlResult(
                youtubeChannelSubscriptions.size(),
                deduplicatedItems.size(),
                saveResult.getSavedCount(),
                saveResult.getSkippedCount()
        );
    }

    /**
     * 抓取 GitHub 订阅源
     */
    @Override
    public CrawlResult crawlGithubSourceSubscriptions() {
        List<UserSourceSubscription> enabledSubscriptions =
                userSourceSubscriptionService.listEnabledSourceSubscriptions();
        if (CollUtil.isEmpty(enabledSubscriptions)) {
            return new CrawlResult(0, 0, 0, 0);
        }

        List<UserSourceSubscription> githubSubscriptions = enabledSubscriptions.stream()
                .filter(this::isGithubSourceSubscription)
                .toList();
        if (CollUtil.isEmpty(githubSubscriptions)) {
            return new CrawlResult(0, 0, 0, 0);
        }

        List<GithubTrendingItemDTO> allItems = new ArrayList<>();
        for (UserSourceSubscription subscription : githubSubscriptions) {
            if (isGithubRepoSubscription(subscription)) {
                GithubTrendingItemDTO dto = githubTrendingService.crawlGitHubRepo(subscription.getSourceValue());
                if (dto != null) {
                    allItems.add(dto);
                }
                continue;
            }
            if (isGithubAuthorSubscription(subscription)) {
                List<GithubTrendingItemDTO> dtoList = githubTrendingService
                        .crawlGitHubAuthorRepositories(subscription.getSourceValue());
                if (CollUtil.isNotEmpty(dtoList)) {
                    allItems.addAll(dtoList);
                }
            }
        }
        List<GithubTrendingItemDTO> deduplicatedItems = deduplicateGithubItems(allItems);
        ContentItemService.SaveResult saveResult = contentItemService.saveGithubTrendingItems(deduplicatedItems);
        return new CrawlResult(
                githubSubscriptions.size(),
                deduplicatedItems.size(),
                saveResult.getSavedCount(),
                saveResult.getSkippedCount()
        );
    }

    /**
     * 抓取全部订阅源
     */
    @Override
    public CrawlResult crawlAllSourceSubscriptions() {
        CrawlResult youtubeResult = crawlYoutubeSourceSubscriptions();
        CrawlResult githubResult = crawlGithubSourceSubscriptions();
        return new CrawlResult(
                youtubeResult.getSubscriptionCount() + githubResult.getSubscriptionCount(),
                youtubeResult.getCrawlCount() + githubResult.getCrawlCount(),
                youtubeResult.getSavedCount() + githubResult.getSavedCount(),
                youtubeResult.getSkippedCount() + githubResult.getSkippedCount()
        );
    }

    /**
     * 判断是否为 YouTube 频道订阅
     */
    protected boolean isYoutubeChannelSubscription(UserSourceSubscription subscription) {
        return subscription != null
                && StrUtil.equalsIgnoreCase(StrUtil.trim(subscription.getPlatform()), "youtube")
                && StrUtil.equalsIgnoreCase(StrUtil.trim(subscription.getSourceType()), "channel")
                && StrUtil.isNotBlank(subscription.getSourceValue());
    }

    /**
     * 判断是否为 GitHub 订阅
     */
    protected boolean isGithubSourceSubscription(UserSourceSubscription subscription) {
        return subscription != null
                && StrUtil.equalsIgnoreCase(StrUtil.trim(subscription.getPlatform()), "github")
                && StrUtil.isNotBlank(subscription.getSourceValue())
                && (isGithubRepoSubscription(subscription) || isGithubAuthorSubscription(subscription));
    }

    /**
     * 判断是否为 GitHub 仓库订阅
     */
    protected boolean isGithubRepoSubscription(UserSourceSubscription subscription) {
        return subscription != null
                && StrUtil.equalsIgnoreCase(StrUtil.trim(subscription.getSourceType()), "repo");
    }

    /**
     * 判断是否为 GitHub 作者订阅
     */
    protected boolean isGithubAuthorSubscription(UserSourceSubscription subscription) {
        return subscription != null
                && StrUtil.equalsIgnoreCase(StrUtil.trim(subscription.getSourceType()), "author");
    }

    /**
     * 对 YouTube 结果按 videoId 去重
     */
    protected List<YoutubeVideoItemDTO> deduplicateYoutubeItems(List<YoutubeVideoItemDTO> items) {
        if (CollUtil.isEmpty(items)) {
            return List.of();
        }
        Map<String, YoutubeVideoItemDTO> itemMap = new LinkedHashMap<>();
        for (YoutubeVideoItemDTO item : items) {
            if (item == null || StrUtil.isBlank(item.getVideoId())) {
                continue;
            }
            itemMap.putIfAbsent(StrUtil.trim(item.getVideoId()), item);
        }
        return new ArrayList<>(itemMap.values());
    }

    /**
     * 对 GitHub 结果按 repoFullName 去重
     */
    protected List<GithubTrendingItemDTO> deduplicateGithubItems(List<GithubTrendingItemDTO> items) {
        if (CollUtil.isEmpty(items)) {
            return List.of();
        }
        Map<String, GithubTrendingItemDTO> itemMap = new LinkedHashMap<>();
        for (GithubTrendingItemDTO item : items) {
            if (item == null || StrUtil.isBlank(item.getRepoFullName())) {
                continue;
            }
            itemMap.putIfAbsent(StrUtil.trim(item.getRepoFullName()), item);
        }
        return new ArrayList<>(itemMap.values());
    }
}
