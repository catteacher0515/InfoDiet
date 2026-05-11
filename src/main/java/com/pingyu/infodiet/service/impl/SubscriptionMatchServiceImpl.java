package com.pingyu.infodiet.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.pingyu.infodiet.mapper.UserContentPushMapper;
import com.pingyu.infodiet.model.entity.ContentItem;
import com.pingyu.infodiet.model.entity.UserContentPush;
import com.pingyu.infodiet.model.entity.UserProfile;
import com.pingyu.infodiet.model.entity.UserSubscriptionRule;
import com.pingyu.infodiet.service.ContentItemService;
import com.pingyu.infodiet.service.SubscriptionMatchService;
import com.pingyu.infodiet.service.UserKeywordSubscriptionService;
import com.pingyu.infodiet.service.UserProfileService;
import com.pingyu.infodiet.service.UserSubscriptionRuleService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 订阅匹配服务实现
 */
@Service
public class SubscriptionMatchServiceImpl implements SubscriptionMatchService {

    @Resource
    private UserProfileService userProfileService;

    @Resource
    private UserKeywordSubscriptionService userKeywordSubscriptionService;

    @Resource
    private UserSubscriptionRuleService userSubscriptionRuleService;

    @Resource
    private ContentItemService contentItemService;

    @Resource
    private UserContentPushMapper userContentPushMapper;

    /**
     * 匹配启用用户的订阅内容
     */
    @Override
    public Map<Long, List<ContentItem>> matchEnabledUsers() {
        Map<Long, List<MatchDetail>> detailResult = matchEnabledUsersWithDetails();
        Map<Long, List<ContentItem>> result = new LinkedHashMap<>();
        for (Map.Entry<Long, List<MatchDetail>> entry : detailResult.entrySet()) {
            result.put(
                    entry.getKey(),
                    entry.getValue().stream()
                            .map(MatchDetail::getContentItem)
                            .toList()
            );
        }
        return result;
    }

    /**
     * 匹配启用用户的订阅内容明细
     */
    @Override
    public Map<Long, List<MatchDetail>> matchEnabledUsersWithDetails() {
        List<UserProfile> enabledUsers = userProfileService.listEnabledUsers();
        List<ContentItem> contentItems = listCandidateContentItems();
        Map<Long, List<MatchDetail>> result = new LinkedHashMap<>();
        if (CollUtil.isEmpty(enabledUsers) || CollUtil.isEmpty(contentItems)) {
            return result;
        }
        for (UserProfile userProfile : enabledUsers) {
            List<UserSubscriptionRule> rules = listActiveRules(userProfile.getId());
            if (CollUtil.isEmpty(rules)) {
                continue;
            }
            List<UserContentPush> pushedItems = listPushedContentByUserId(userProfile.getId());
            List<MatchDetail> matchedItems = new ArrayList<>();
            for (ContentItem contentItem : contentItems) {
                if (hasBeenPushed(contentItem, pushedItems)) {
                    continue;
                }
                MatchDetail matchDetail = buildMatchDetail(contentItem, rules);
                if (matchDetail.getScore() > 0) {
                    matchedItems.add(matchDetail);
                }
            }
            if (CollUtil.isNotEmpty(matchedItems)) {
                result.put(
                        userProfile.getId(),
                        matchedItems.stream()
                                .sorted(Comparator
                                        .comparingInt(SubscriptionMatchService.MatchDetail::getScore).reversed()
                                        .thenComparing(item -> item.getContentItem().getId(), Comparator.reverseOrder()))
                                .toList()
                );
            }
        }
        return result;
    }

    /**
     * 查询当前启用规则
     */
    protected List<UserSubscriptionRule> listActiveRules(Long userId) {
        List<UserSubscriptionRule> result = new ArrayList<>();
        List<UserSubscriptionRule> ruleList = userSubscriptionRuleService.listEnabledRulesByUserId(userId);
        if (CollUtil.isNotEmpty(ruleList)) {
            result.addAll(ruleList);
        }
        List<String> legacyKeywords = userKeywordSubscriptionService == null
                ? List.of()
                : userKeywordSubscriptionService.listKeywordsByUserId(userId);
        if (CollUtil.isNotEmpty(legacyKeywords)) {
            Set<String> existingRuleKeys = new LinkedHashSet<>();
            for (UserSubscriptionRule rule : result) {
                existingRuleKeys.add(buildRuleKey(rule.getRuleType(), rule.getRuleValue()));
            }
            for (String legacyKeyword : legacyKeywords) {
                String ruleKey = buildRuleKey("keyword_include", legacyKeyword);
                if (existingRuleKeys.contains(ruleKey)) {
                    continue;
                }
                result.add(UserSubscriptionRule.builder()
                        .userId(userId)
                        .ruleType("keyword_include")
                        .ruleValue(legacyKeyword)
                        .ruleWeight(1)
                        .status(1)
                        .build());
            }
        }
        return result;
    }

    /**
     * 计算内容匹配分数
     */
    protected int calculateMatchScore(ContentItem contentItem, List<UserSubscriptionRule> rules) {
        return buildMatchDetail(contentItem, rules).getScore();
    }

    /**
     * 构建匹配明细
     */
    protected MatchDetail buildMatchDetail(ContentItem contentItem, List<UserSubscriptionRule> rules) {
        if (contentItem == null || CollUtil.isEmpty(rules)) {
            return new MatchDetail(contentItem, 0, List.of());
        }
        int score = 0;
        List<String> matchedRules = new ArrayList<>();
        for (UserSubscriptionRule rule : rules) {
            if (isExcludeRuleMatched(contentItem, rule)) {
                return new MatchDetail(contentItem, 0, List.of());
            }
            if (isIncludeRuleMatched(contentItem, rule)) {
                score += defaultWeight(rule.getRuleWeight());
                matchedRules.add(rule.getRuleType() + ":" + rule.getRuleValue());
            }
        }
        return new MatchDetail(contentItem, score, matchedRules);
    }

    /**
     * 判断是否命中排除规则
     */
    protected boolean isExcludeRuleMatched(ContentItem contentItem, UserSubscriptionRule rule) {
        return isRuleType(rule, "keyword_exclude") && matchKeywordRule(contentItem, rule.getRuleValue());
    }

    /**
     * 判断是否命中包含规则
     */
    protected boolean isIncludeRuleMatched(ContentItem contentItem, UserSubscriptionRule rule) {
        if (isRuleType(rule, "keyword_include")) {
            return matchKeywordRule(contentItem, rule.getRuleValue());
        }
        if (isRuleType(rule, "author")) {
            return matchAuthorRule(contentItem, rule.getRuleValue());
        }
        if (isRuleType(rule, "repo")) {
            return matchRepoRule(contentItem, rule.getRuleValue());
        }
        if (isRuleType(rule, "channel")) {
            return matchChannelRule(contentItem, rule.getRuleValue());
        }
        return false;
    }

    /**
     * 判断关键词规则
     */
    protected boolean matchKeywordRule(ContentItem contentItem, String ruleValue) {
        return contentItemService.matchKeywords(contentItem, List.of(ruleValue));
    }

    /**
     * 判断作者规则
     */
    protected boolean matchAuthorRule(ContentItem contentItem, String ruleValue) {
        return equalsIgnoreCase(contentItem.getAuthorName(), ruleValue);
    }

    /**
     * 判断仓库规则
     */
    protected boolean matchRepoRule(ContentItem contentItem, String ruleValue) {
        return equalsIgnoreCase(contentItem.getSourceId(), ruleValue);
    }

    /**
     * 判断频道规则
     */
    protected boolean matchChannelRule(ContentItem contentItem, String ruleValue) {
        String normalizedRuleValue = normalize(ruleValue);
        return equalsIgnoreCase(contentItem.getAuthorName(), ruleValue)
                || normalize(contentItem.getAuthorUrl()).contains(normalizedRuleValue);
    }

    /**
     * 判断规则类型
     */
    protected boolean isRuleType(UserSubscriptionRule rule, String ruleType) {
        return rule != null && StrUtil.equalsIgnoreCase(StrUtil.trim(rule.getRuleType()), ruleType);
    }

    /**
     * 获取默认权重
     */
    protected int defaultWeight(Integer ruleWeight) {
        return ruleWeight == null ? 1 : ruleWeight;
    }

    /**
     * 构建规则唯一键
     */
    protected String buildRuleKey(String ruleType, String ruleValue) {
        return normalize(ruleType) + "#" + normalize(ruleValue);
    }

    /**
     * 忽略大小写判断
     */
    protected boolean equalsIgnoreCase(String left, String right) {
        return StrUtil.equalsIgnoreCase(StrUtil.trim(left), StrUtil.trim(right));
    }

    /**
     * 标准化文本
     */
    protected String normalize(String value) {
        return StrUtil.blankToDefault(StrUtil.trim(value), "").toLowerCase();
    }

    /**
     * 查询用户已推送内容
     */
    protected List<UserContentPush> listPushedContentByUserId(Long userId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("userId", userId)
                .eq("pushStatus", 1);
        return userContentPushMapper.selectListByQuery(queryWrapper);
    }

    /**
     * 判断内容是否已推送
     */
    protected boolean hasBeenPushed(ContentItem contentItem, List<UserContentPush> pushedItems) {
        if (contentItem == null || CollUtil.isEmpty(pushedItems)) {
            return false;
        }
        return pushedItems.stream().anyMatch(item -> contentItem.getId().equals(item.getContentItemId()));
    }

    /**
     * 查询候选内容
     */
    protected List<ContentItem> listCandidateContentItems() {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("isDelete", 0)
                .orderBy("id", false);
        return contentItemService.list(queryWrapper);
    }

}
