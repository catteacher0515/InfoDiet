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
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 用户内容推送表 服务层实现。
 */
@Service
public class UserContentPushServiceImpl extends ServiceImpl<UserContentPushMapper, UserContentPush>
        implements UserContentPushService {

    private static final int DEFAULT_MAX_RETRY_COUNT = 3;
    private static final int RETRY_DELAY_MINUTES = 5;

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
                        .queueStatus(0)
                        .retryCount(0)
                        .maxRetryCount(DEFAULT_MAX_RETRY_COUNT)
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
     * 查询可入队推送记录
     */
    @Override
    public List<UserContentPush> listEnqueueablePushesByChannel(String pushChannel) {
        LocalDateTime now = now();
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("pushChannel", pushChannel)
                .eq("pushStatus", 0)
                .eq("queueStatus", 0);
        return this.list(queryWrapper).stream()
                .filter(item -> item.getNextRetryTime() == null || !item.getNextRetryTime().isAfter(now))
                .sorted(Comparator.comparing(UserContentPush::getCreateTime, Comparator.nullsLast(LocalDateTime::compareTo)))
                .toList();
    }

    /**
     * 标记已入队
     */
    @Override
    public boolean markQueued(Long pushId) {
        LocalDateTime now = now();
        return this.updateChain()
                .set("queueStatus", 1)
                .set("lastQueueTime", now)
                .where("id = ?", pushId)
                .and("pushStatus = ?", 0)
                .and("queueStatus = ?", 0)
                .update();
    }

    /**
     * 标记消费中
     */
    @Override
    public boolean markConsuming(Long pushId) {
        return this.updateChain()
                .set("queueStatus", 2)
                .where("id = ?", pushId)
                .and("pushStatus = ?", 0)
                .and("queueStatus = ?", 1)
                .update();
    }

    /**
     * 标记推送成功
     */
    @Override
    public boolean markPushSuccess(Long pushId) {
        LocalDateTime now = now();
        return this.updateChain()
                .set("pushStatus", 1)
                .set("queueStatus", 3)
                .set("pushTime", now)
                .set("failReason", null)
                .set("nextRetryTime", null)
                .where("id = ?", pushId)
                .and("pushStatus = ?", 0)
                .update();
    }

    /**
     * 标记推送失败
     */
    @Override
    public boolean markPushFailed(Long pushId, String failReason) {
        UserContentPush userContentPush = this.getById(pushId);
        if (userContentPush == null || userContentPush.getPushStatus() == 1) {
            return false;
        }
        int currentRetryCount = userContentPush.getRetryCount() == null ? 0 : userContentPush.getRetryCount();
        int maxRetryCount = userContentPush.getMaxRetryCount() == null
                ? DEFAULT_MAX_RETRY_COUNT
                : userContentPush.getMaxRetryCount();
        int nextRetryCount = currentRetryCount + 1;
        LocalDateTime now = now();
        if (nextRetryCount >= maxRetryCount) {
            return this.updateChain()
                    .set("pushStatus", 2)
                    .set("queueStatus", 3)
                    .set("retryCount", nextRetryCount)
                    .set("failReason", failReason)
                    .set("nextRetryTime", null)
                    .where("id = ?", pushId)
                    .and("pushStatus = ?", 0)
                    .update();
        }
        return this.updateChain()
                .set("pushStatus", 0)
                .set("queueStatus", 0)
                .set("retryCount", nextRetryCount)
                .set("failReason", failReason)
                .set("nextRetryTime", now.plusMinutes(RETRY_DELAY_MINUTES))
                .where("id = ?", pushId)
                .and("pushStatus = ?", 0)
                .update();
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
