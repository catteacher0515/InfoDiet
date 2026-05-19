package com.pingyu.infodiet.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.pingyu.infodiet.mapper.UserContentPushMapper;
import com.pingyu.infodiet.common.PageResponse;
import com.pingyu.infodiet.model.dto.ops.FailedPushOverviewVO;
import com.pingyu.infodiet.model.entity.ContentItem;
import com.pingyu.infodiet.model.entity.UserContentPush;
import com.pingyu.infodiet.model.entity.UserProfile;
import com.pingyu.infodiet.service.AlertRecordService;
import com.pingyu.infodiet.service.ContentItemService;
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

    @Resource
    private ContentItemService contentItemService;

    @Resource
    private AlertRecordService alertRecordService;

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
                .filter(item -> contentItemExists(item.getContentItemId()))
                .sorted(Comparator.comparing(UserContentPush::getCreateTime, Comparator.nullsLast(LocalDateTime::compareTo)))
                .toList();
    }

    /**
     * 将内容已不存在的待推送记录直接标记为失败终态
     */
    @Override
    public int markMissingContentPushesAsFailed(String pushChannel) {
        if (StrUtil.isBlank(pushChannel)) {
            return 0;
        }
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("pushChannel", pushChannel.trim())
                .eq("pushStatus", 0)
                .eq("queueStatus", 0);
        int affectedCount = 0;
        for (UserContentPush item : this.list(queryWrapper)) {
            if (contentItemExists(item.getContentItemId())) {
                continue;
            }
            boolean updated = markMissingContentPushAsFailed(item.getId());
            if (updated) {
                affectedCount++;
            }
        }
        return affectedCount;
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
     * 重试失败推送
     */
    @Override
    public boolean retryFailedPush(Long pushId) {
        UserContentPush userContentPush = this.getById(pushId);
        if (userContentPush == null || userContentPush.getPushStatus() == null || userContentPush.getPushStatus() != 2) {
            return false;
        }
        return this.updateChain()
                .set("pushStatus", 0)
                .set("queueStatus", 0)
                .set("retryCount", 0)
                .set("failReason", null)
                .set("nextRetryTime", null)
                .where("id = ?", pushId)
                .and("pushStatus = ?", 2)
                .update();
    }

    /**
     * 查询失败推送列表
     */
    @Override
    public List<UserContentPush> listFailedPushesByChannel(String pushChannel) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("pushChannel", pushChannel)
                .eq("pushStatus", 2)
                .orderBy("updateTime", false);
        return this.list(queryWrapper);
    }

    /**
     * 分页查询失败推送
     */
    @Override
    public PageResponse<UserContentPush> pageFailedPushes(String pushChannel, String keyword, Integer retryCount, int pageNum, int pageSize) {
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = pageSize <= 0 ? 10 : Math.min(pageSize, 100);
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("pushStatus", 2);
        if (StrUtil.isNotBlank(pushChannel)) {
            queryWrapper.eq("pushChannel", pushChannel.trim());
        }
        if (retryCount != null) {
            queryWrapper.eq("retryCount", retryCount);
        }
        List<UserContentPush> pushList = this.list(queryWrapper).stream()
                .filter(item -> matchesKeyword(item, keyword))
                .sorted(Comparator.comparing(UserContentPush::getUpdateTime, Comparator.nullsLast(LocalDateTime::compareTo)).reversed())
                .toList();
        int fromIndex = Math.min((safePageNum - 1) * safePageSize, pushList.size());
        int toIndex = Math.min(fromIndex + safePageSize, pushList.size());
        return new PageResponse<>(pushList.size(), safePageNum, safePageSize, pushList.subList(fromIndex, toIndex));
    }

    /**
     * 查询当前用户推送记录
     */
    @Override
    public List<UserContentPush> listPushesByUserId(Long userId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("userId", userId)
                .orderBy("createTime", false);
        return this.list(queryWrapper);
    }

    /**
     * 批量重试失败推送
     */
    @Override
    public BatchRetryResult retryFailedPushes(List<Long> pushIdList) {
        if (CollUtil.isEmpty(pushIdList)) {
            return new BatchRetryResult(0, 0, 0);
        }
        int successCount = 0;
        for (Long pushId : pushIdList) {
            if (retryFailedPush(pushId)) {
                successCount++;
            }
        }
        return new BatchRetryResult(pushIdList.size(), successCount, pushIdList.size() - successCount);
    }

    /**
     * 查询失败推送联动视图
     */
    @Override
    public FailedPushOverviewVO getFailedPushOverview(Long pushId) {
        UserContentPush push = this.getById(pushId);
        if (push == null) {
            return null;
        }
        ContentItem contentItem = push.getContentItemId() == null ? null : contentItemService.getById(push.getContentItemId());
        return FailedPushOverviewVO.builder()
                .push(push)
                .contentItem(contentItem)
                .alertRecord(alertRecordService.getBySource("user_content_push", pushId))
                .build();
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
     * 判断是否匹配关键字
     */
    protected boolean matchesKeyword(UserContentPush item, String keyword) {
        if (StrUtil.isBlank(keyword)) {
            return true;
        }
        String safeKeyword = keyword.trim();
        return String.valueOf(item.getId()).contains(safeKeyword)
                || String.valueOf(item.getUserId()).contains(safeKeyword)
                || String.valueOf(item.getContentItemId()).contains(safeKeyword)
                || StrUtil.containsIgnoreCase(StrUtil.blankToDefault(item.getFailReason(), ""), safeKeyword);
    }

    /**
     * 判断待推送内容是否仍然存在，避免历史坏样本重新进入异步链路
     */
    protected boolean contentItemExists(Long contentItemId) {
        return contentItemId != null && contentItemService.getById(contentItemId) != null;
    }

    /**
     * 将单条缺失内容的待推送记录终态化
     */
    protected boolean markMissingContentPushAsFailed(Long pushId) {
        return this.updateChain()
                .set("pushStatus", 2)
                .set("queueStatus", 3)
                .set("failReason", "内容不存在")
                .set("nextRetryTime", null)
                .where("id = ?", pushId)
                .and("pushStatus = ?", 0)
                .and("queueStatus = ?", 0)
                .update();
    }

    /**
     * 获取当前时间
     */
    protected LocalDateTime now() {
        return LocalDateTime.now();
    }
}
