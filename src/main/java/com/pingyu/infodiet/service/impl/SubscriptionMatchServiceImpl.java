package com.pingyu.infodiet.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.pingyu.infodiet.model.entity.ContentItem;
import com.pingyu.infodiet.model.entity.UserProfile;
import com.pingyu.infodiet.service.ContentItemService;
import com.pingyu.infodiet.service.SubscriptionMatchService;
import com.pingyu.infodiet.service.UserKeywordSubscriptionService;
import com.pingyu.infodiet.service.UserProfileService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    private ContentItemService contentItemService;

    /**
     * 匹配启用用户的订阅内容
     */
    @Override
    public Map<Long, List<ContentItem>> matchEnabledUsers() {
        List<UserProfile> enabledUsers = userProfileService.listEnabledUsers();
        List<ContentItem> contentItems = listCandidateContentItems();
        Map<Long, List<ContentItem>> result = new LinkedHashMap<>();
        if (CollUtil.isEmpty(enabledUsers) || CollUtil.isEmpty(contentItems)) {
            return result;
        }
        for (UserProfile userProfile : enabledUsers) {
            List<String> keywords = userKeywordSubscriptionService.listKeywordsByUserId(userProfile.getId());
            if (CollUtil.isEmpty(keywords)) {
                continue;
            }
            List<ContentItem> matchedItems = new ArrayList<>();
            for (ContentItem contentItem : contentItems) {
                if (contentItemService.matchKeywords(contentItem, keywords)) {
                    matchedItems.add(contentItem);
                }
            }
            if (CollUtil.isNotEmpty(matchedItems)) {
                result.put(userProfile.getId(), matchedItems);
            }
        }
        return result;
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
