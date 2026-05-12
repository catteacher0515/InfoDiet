package com.pingyu.infodiet.service.impl;

import cn.hutool.core.util.StrUtil;
import com.pingyu.infodiet.model.dto.dashboard.AdminSubscriptionOverviewVO;
import com.pingyu.infodiet.model.dto.user.UserListItemVO;
import com.pingyu.infodiet.model.entity.UserSourceSubscription;
import com.pingyu.infodiet.service.AdminOverviewService;
import com.pingyu.infodiet.service.UserKeywordSubscriptionService;
import com.pingyu.infodiet.service.UserProfileService;
import com.pingyu.infodiet.service.UserSourceSubscriptionService;
import com.pingyu.infodiet.service.UserSubscriptionRuleService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 管理区总览服务实现
 */
@Service
public class AdminOverviewServiceImpl implements AdminOverviewService {

    @Resource
    private UserProfileService userProfileService;

    @Resource
    private UserKeywordSubscriptionService userKeywordSubscriptionService;

    @Resource
    private UserSubscriptionRuleService userSubscriptionRuleService;

    @Resource
    private UserSourceSubscriptionService userSourceSubscriptionService;

    /**
     * 查询订阅总览
     */
    @Override
    public AdminSubscriptionOverviewVO getSubscriptionOverview() {
        List<UserListItemVO> users = userProfileService.listUsers();
        List<UserSourceSubscription> sources = userSourceSubscriptionService.listEnabledSourceSubscriptions();

        int enabledUserCount = users.size();
        int keywordCount = users.stream()
                .mapToInt(user -> userKeywordSubscriptionService.listKeywordsByUserId(user.getId()).size())
                .sum();
        int ruleCount = users.stream()
                .mapToInt(user -> userSubscriptionRuleService.listEnabledRulesByUserId(user.getId()).size())
                .sum();
        int sourceCount = sources.size();

        return AdminSubscriptionOverviewVO.builder()
                .keywordCount(keywordCount)
                .ruleCount(ruleCount)
                .sourceCount(sourceCount)
                .enabledUserCount(enabledUserCount)
                .avgKeywordPerUser(calculateAverage(keywordCount, enabledUserCount))
                .avgRulePerUser(calculateAverage(ruleCount, enabledUserCount))
                .avgSourcePerUser(calculateAverage(sourceCount, enabledUserCount))
                .youtubeSourceCount(countByPlatform(sources, "youtube"))
                .githubSourceCount(countByPlatform(sources, "github"))
                .channelSourceCount(countBySourceType(sources, "channel"))
                .repoSourceCount(countBySourceType(sources, "repo"))
                .authorSourceCount(countBySourceType(sources, "author"))
                .build();
    }

    private double calculateAverage(int total, int size) {
        if (size <= 0) {
            return 0D;
        }
        return Math.round((double) total / size * 100.0D) / 100.0D;
    }

    private int countByPlatform(List<UserSourceSubscription> sources, String platform) {
        return (int) sources.stream()
                .filter(item -> StrUtil.equalsIgnoreCase(item.getPlatform(), platform))
                .count();
    }

    private int countBySourceType(List<UserSourceSubscription> sources, String sourceType) {
        return (int) sources.stream()
                .filter(item -> StrUtil.equalsIgnoreCase(item.getSourceType(), sourceType))
                .count();
    }
}
