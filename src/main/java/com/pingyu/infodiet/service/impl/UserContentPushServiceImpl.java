package com.pingyu.infodiet.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.pingyu.infodiet.mapper.UserContentPushMapper;
import com.pingyu.infodiet.model.entity.ContentItem;
import com.pingyu.infodiet.model.entity.UserContentPush;
import com.pingyu.infodiet.model.entity.UserProfile;
import com.pingyu.infodiet.service.SubscriptionMatchService;
import com.pingyu.infodiet.service.UserContentPushService;
import com.pingyu.infodiet.service.UserProfileService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户内容推送表 服务层实现。
 */
@Service
public class UserContentPushServiceImpl extends ServiceImpl<UserContentPushMapper, UserContentPush>
        implements UserContentPushService {

    @Resource
    private SubscriptionMatchService subscriptionMatchService;

    @Resource
    private UserProfileService userProfileService;

    /**
     * 生成待推送记录
     */
    @Override
    public CreatePushResult createPendingPushes() {
        Map<Long, List<ContentItem>> matchResult = subscriptionMatchService.matchEnabledUsers();
        if (matchResult == null || matchResult.isEmpty()) {
            return new CreatePushResult(0, 0, 0, 0, 0, 0);
        }
        int totalCount = 0;
        int createdCount = 0;
        int skippedCount = 0;
        int skippedByExistingCount = 0;
        int skippedByLimitCount = 0;
        int skippedByCooldownCount = 0;
        for (Map.Entry<Long, List<ContentItem>> entry : matchResult.entrySet()) {
            Long userId = entry.getKey();
            List<ContentItem> contentItems = entry.getValue();
            if (CollUtil.isEmpty(contentItems)) {
                continue;
            }
            UserProfile userProfile = userProfileService.getUserById(userId);
            String pushChannel = userProfile == null ? "" : StrUtil.blankToDefault(userProfile.getPushChannel(), "");
            int dailyPushLimit = userProfile == null || userProfile.getDailyPushLimit() == null
                    ? Integer.MAX_VALUE
                    : userProfile.getDailyPushLimit();
            if (isUserInPushCooldown(userProfile, userId)) {
                totalCount += contentItems.size();
                skippedCount += contentItems.size();
                skippedByCooldownCount += contentItems.size();
                continue;
            }
            int currentPushCount = countTodayPushesByUserId(userId);
            for (ContentItem contentItem : contentItems) {
                totalCount++;
                if (existsByUserIdAndContentItemId(userId, contentItem.getId())) {
                    skippedCount++;
                    skippedByExistingCount++;
                    continue;
                }
                if (currentPushCount >= dailyPushLimit) {
                    skippedCount++;
                    skippedByLimitCount++;
                    continue;
                }
                UserContentPush userContentPush = UserContentPush.builder()
                        .userId(userId)
                        .contentItemId(contentItem.getId())
                        .pushChannel(pushChannel)
                        .pushStatus(0)
                        .build();
                boolean saved = this.save(userContentPush);
                if (saved) {
                    createdCount++;
                    currentPushCount++;
                }
            }
        }
        return new CreatePushResult(
                totalCount,
                createdCount,
                skippedCount,
                skippedByExistingCount,
                skippedByLimitCount,
                skippedByCooldownCount
        );
    }

    /**
     * 查询待推送记录
     */
    @Override
    public List<UserContentPush> listPendingPushesByChannel(String pushChannel) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("pushChannel", pushChannel)
                .eq("pushStatus", 0);
        return this.list(queryWrapper);
    }

    /**
     * 标记推送成功
     */
    @Override
    public boolean markPushSuccess(Long pushId) {
        UserContentPush userContentPush = new UserContentPush();
        userContentPush.setId(pushId);
        userContentPush.setPushStatus(1);
        userContentPush.setPushTime(now());
        userContentPush.setFailReason(null);
        return this.updateById(userContentPush);
    }

    /**
     * 标记推送失败
     */
    @Override
    public boolean markPushFailed(Long pushId, String failReason) {
        UserContentPush userContentPush = new UserContentPush();
        userContentPush.setId(pushId);
        userContentPush.setPushStatus(2);
        userContentPush.setFailReason(failReason);
        return this.updateById(userContentPush);
    }

    /**
     * 判断推送记录是否已存在
     */
    protected boolean existsByUserIdAndContentItemId(Long userId, Long contentItemId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("userId", userId)
                .eq("contentItemId", contentItemId);
        return this.mapper.selectCountByQuery(queryWrapper) > 0;
    }

    /**
     * 统计用户当天推送记录数
     */
    protected int countTodayPushesByUserId(Long userId) {
        LocalDateTime startOfDay = now().toLocalDate().atStartOfDay();
        LocalDateTime startOfNextDay = startOfDay.plusDays(1);
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("userId", userId)
                .ge("createTime", startOfDay)
                .lt("createTime", startOfNextDay);
        return (int) this.mapper.selectCountByQuery(queryWrapper);
    }

    /**
     * 获取用户最近一次成功推送时间
     */
    protected LocalDateTime getLastSuccessPushTime(Long userId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("userId", userId)
                .eq("pushStatus", 1)
                .orderBy("pushTime", false)
                .limit(1);
        List<UserContentPush> pushList = this.list(queryWrapper);
        if (CollUtil.isEmpty(pushList)) {
            return null;
        }
        return pushList.getFirst().getPushTime();
    }

    /**
     * 判断用户是否处于推送冷却期
     */
    protected boolean isUserInPushCooldown(UserProfile userProfile, Long userId) {
        if (userProfile == null || userProfile.getPushCooldownHours() == null || userProfile.getPushCooldownHours() <= 0) {
            return false;
        }
        LocalDateTime lastSuccessPushTime = getLastSuccessPushTime(userId);
        if (lastSuccessPushTime == null) {
            return false;
        }
        LocalDateTime nextAllowedPushTime = lastSuccessPushTime.plusHours(userProfile.getPushCooldownHours());
        return now().isBefore(nextAllowedPushTime);
    }

    /**
     * 获取当前时间
     */
    protected LocalDateTime now() {
        return LocalDateTime.now();
    }
}
