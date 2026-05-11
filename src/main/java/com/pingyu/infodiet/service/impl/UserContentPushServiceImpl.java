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
            return new CreatePushResult(0, 0, 0);
        }
        int totalCount = 0;
        int createdCount = 0;
        int skippedCount = 0;
        for (Map.Entry<Long, List<ContentItem>> entry : matchResult.entrySet()) {
            Long userId = entry.getKey();
            List<ContentItem> contentItems = sortContentItemsByPriority(entry.getValue());
            if (CollUtil.isEmpty(contentItems)) {
                continue;
            }
            UserProfile userProfile = userProfileService.getUserById(userId);
            String pushChannel = userProfile == null ? "" : StrUtil.blankToDefault(userProfile.getPushChannel(), "");
            int dailyPushLimit = userProfile == null || userProfile.getDailyPushLimit() == null
                    ? Integer.MAX_VALUE
                    : userProfile.getDailyPushLimit();
            int currentPushCount = countTodayPushesByUserId(userId);
            for (ContentItem contentItem : contentItems) {
                totalCount++;
                if (existsByUserIdAndContentItemId(userId, contentItem.getId())) {
                    skippedCount++;
                    continue;
                }
                if (currentPushCount >= dailyPushLimit) {
                    skippedCount++;
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
        return new CreatePushResult(totalCount, createdCount, skippedCount);
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
     * 按优先级排序内容
     */
    protected List<ContentItem> sortContentItemsByPriority(List<ContentItem> contentItems) {
        if (CollUtil.isEmpty(contentItems)) {
            return contentItems;
        }
        return contentItems.stream()
                .sorted(buildContentPriorityComparator())
                .toList();
    }

    /**
     * 构建内容优先级比较器
     */
    private Comparator<ContentItem> buildContentPriorityComparator() {
        return Comparator
                .comparingInt(this::platformPriority)
                .thenComparing(this::githubTodayStarCount, Comparator.reverseOrder())
                .thenComparing(this::githubStarCount, Comparator.reverseOrder())
                .thenComparing(this::contentPublishTime, Comparator.reverseOrder())
                .thenComparing(this::contentCrawlTime, Comparator.reverseOrder())
                .thenComparing(this::contentId, Comparator.reverseOrder());
    }

    /**
     * 平台优先级
     */
    private int platformPriority(ContentItem contentItem) {
        if (contentItem == null) {
            return Integer.MAX_VALUE;
        }
        if ("github".equalsIgnoreCase(contentItem.getPlatform())) {
            return 1;
        }
        if ("youtube".equalsIgnoreCase(contentItem.getPlatform())) {
            return 2;
        }
        return 3;
    }

    /**
     * GitHub 今日 Star 数
     */
    private Integer githubTodayStarCount(ContentItem contentItem) {
        return defaultNumber(contentItem == null ? null : contentItem.getTodayStarCount());
    }

    /**
     * GitHub 总 Star 数
     */
    private Integer githubStarCount(ContentItem contentItem) {
        return defaultNumber(contentItem == null ? null : contentItem.getStarCount());
    }

    /**
     * 内容发布时间
     */
    private LocalDateTime contentPublishTime(ContentItem contentItem) {
        return contentItem == null ? LocalDateTime.MIN : defaultTime(contentItem.getPublishTime());
    }

    /**
     * 内容抓取时间
     */
    private LocalDateTime contentCrawlTime(ContentItem contentItem) {
        return contentItem == null ? LocalDateTime.MIN : defaultTime(contentItem.getCrawlTime());
    }

    /**
     * 内容主键
     */
    private Long contentId(ContentItem contentItem) {
        return contentItem == null ? Long.MIN_VALUE : contentItem.getId();
    }

    /**
     * 数值默认值
     */
    private Integer defaultNumber(Integer value) {
        return value == null ? 0 : value;
    }

    /**
     * 时间默认值
     */
    private LocalDateTime defaultTime(LocalDateTime value) {
        return value == null ? LocalDateTime.MIN : value;
    }

    /**
     * 获取当前时间
     */
    protected LocalDateTime now() {
        return LocalDateTime.now();
    }
}
