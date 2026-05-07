package com.pingyu.infodiet.service.impl;

import com.pingyu.infodiet.config.InfoDietProperties;
import com.pingyu.infodiet.model.dto.github.GithubTrendingItemDTO;
import com.pingyu.infodiet.service.ContentItemService;
import com.pingyu.infodiet.service.FeishuPushService;
import com.pingyu.infodiet.service.GithubTrendingService;
import com.pingyu.infodiet.service.InfoDietScheduleService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 信息节食调度服务实现
 */
@Service
public class InfoDietScheduleServiceImpl implements InfoDietScheduleService {

    @Resource
    private GithubTrendingService githubTrendingService;

    @Resource
    private ContentItemService contentItemService;

    @Resource
    private FeishuPushService feishuPushService;

    @Resource
    private InfoDietProperties infoDietProperties;

    /**
     * 执行每日 GitHub 流程
     */
    @Override
    public ScheduleResult runDailyGithubFlow() {
        List<GithubTrendingItemDTO> dtoList = githubTrendingService.crawlGitHubTrending();
        ContentItemService.SaveResult saveResult = contentItemService.saveGithubTrendingItems(dtoList);
        ContentItemService.KeywordFilterResult filterResult = contentItemService
                .filterByKeywords(infoDietProperties.getKeywords());
        FeishuPushService.PushResult pushResult = feishuPushService.pushContentItemsToFeishu();
        return new ScheduleResult(
                dtoList.size(),
                saveResult.getSavedCount(),
                saveResult.getSkippedCount(),
                filterResult.getMatchedCount(),
                filterResult.getUnmatchedCount(),
                pushResult.getSuccessCount(),
                pushResult.getFailedCount()
        );
    }
}
