package com.pingyu.infodiet.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.lark.oapi.Client;
import com.lark.oapi.core.utils.Jsons;
import com.lark.oapi.service.bitable.v1.model.AppTableRecord;
import com.lark.oapi.service.bitable.v1.model.CreateAppTableRecordReq;
import com.lark.oapi.service.bitable.v1.model.CreateAppTableRecordResp;
import com.lark.oapi.service.im.v1.model.CreateMessageReq;
import com.lark.oapi.service.im.v1.model.CreateMessageReqBody;
import com.lark.oapi.service.im.v1.model.CreateMessageResp;
import com.mybatisflex.core.query.QueryWrapper;
import com.pingyu.infodiet.config.FeishuBaseProperties;
import com.pingyu.infodiet.exception.ErrorCode;
import com.pingyu.infodiet.exception.ThrowUtils;
import com.pingyu.infodiet.model.dto.content.ContentEventClusterDTO;
import com.pingyu.infodiet.model.dto.content.DailyDigestDTO;
import com.pingyu.infodiet.model.entity.ContentItem;
import com.pingyu.infodiet.model.entity.UserContentPush;
import com.pingyu.infodiet.model.entity.UserProfile;
import com.pingyu.infodiet.service.AlertRecordService;
import com.pingyu.infodiet.service.ContentItemService;
import com.pingyu.infodiet.service.DailyDigestService;
import com.pingyu.infodiet.service.FeishuPushService;
import com.pingyu.infodiet.service.UserContentPushService;
import com.pingyu.infodiet.service.UserProfileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 飞书推送服务实现
 */
@Service
@Slf4j
public class FeishuPushServiceImpl implements FeishuPushService {

    @Resource
    private ContentItemService contentItemService;

    @Resource
    private FeishuBaseProperties feishuBaseProperties;

    @Resource
    private UserContentPushService userContentPushService;

    @Resource
    private AlertRecordService alertRecordService;

    @Resource
    private DailyDigestService dailyDigestService;

    @Resource
    private UserProfileService userProfileService;

    /**
     * 查询待推送内容
     */
    @Override
    public List<ContentItem> listPendingPushItems() {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("keywordMatched", 1)
                .eq("pushStatus", 0);
        return contentItemService.list(queryWrapper);
    }

    /**
     * 推送内容到飞书
     */
    @Override
    public PushResult pushContentItemsToFeishu() {
        List<ContentItem> contentItems = listPendingPushItems();
        if (CollUtil.isEmpty(contentItems)) {
            return new PushResult(0, 0, 0);
        }
        validateFeishuConfig();
        List<Long> pushedItemIds = new ArrayList<>();
        int successCount = 0;
        int failedCount = 0;
        for (ContentItem contentItem : contentItems) {
            PushAttemptResult pushAttemptResult = pushSingleContentItemWithResult(contentItem);
            if (pushAttemptResult.success()) {
                pushedItemIds.add(contentItem.getId());
                successCount++;
            } else {
                failedCount++;
            }
        }
        markItemsAsPushed(pushedItemIds);
        return new PushResult(contentItems.size(), successCount, failedCount);
    }

    /**
     * 推送用户内容到飞书
     */
    @Override
    public PushResult pushUserContentItemsToFeishu() {
        List<UserContentPush> userContentPushes = listPendingUserPushItems();
        if (CollUtil.isEmpty(userContentPushes)) {
            return new PushResult(0, 0, 0);
        }
        validateFeishuConfig();
        int successCount = 0;
        int failedCount = 0;
        for (UserContentPush userContentPush : userContentPushes) {
            ContentItem contentItem = contentItemService.getById(userContentPush.getContentItemId());
            if (contentItem == null) {
                userContentPushService.markPushFailed(userContentPush.getId(), "内容不存在");
                failedCount++;
                continue;
            }
            PushAttemptResult pushAttemptResult = pushSingleContentItemWithResult(contentItem);
            if (pushAttemptResult.success()) {
                userContentPushService.markPushSuccess(userContentPush.getId());
                successCount++;
            } else {
                userContentPushService.markPushFailed(
                        userContentPush.getId(),
                        StrUtil.blankToDefault(pushAttemptResult.failReason(), "飞书推送失败")
                );
                failedCount++;
            }
        }
        return new PushResult(userContentPushes.size(), successCount, failedCount);
    }

    /**
     * 推送单条用户内容到飞书
     */
    @Override
    public boolean pushSingleUserContentItemToFeishu(Long pushId) {
        if (pushId == null) {
            return false;
        }
        UserContentPush userContentPush = userContentPushService.getById(pushId);
        if (userContentPush == null) {
            return false;
        }
        validateFeishuConfig();
        ContentItem contentItem = contentItemService.getById(userContentPush.getContentItemId());
        if (contentItem == null) {
            userContentPushService.markPushFailed(userContentPush.getId(), "内容不存在");
            createFinalPushFailedAlertIfNeeded(userContentPush, "内容不存在");
            return false;
        }
        PushAttemptResult pushAttemptResult = pushSingleContentItemWithResult(contentItem);
        if (pushAttemptResult.success()) {
            userContentPushService.markPushSuccess(userContentPush.getId());
            return true;
        }
        userContentPushService.markPushFailed(
                userContentPush.getId(),
                StrUtil.blankToDefault(pushAttemptResult.failReason(), "飞书推送失败")
        );
        createFinalPushFailedAlertIfNeeded(
                userContentPush,
                StrUtil.blankToDefault(pushAttemptResult.failReason(), "飞书推送失败")
        );
        return false;
    }

    /**
     * 推送今日日报到飞书
     */
    @Override
    public PushResult pushTodayDigestToFeishu() {
        validateFeishuConfig();
        DailyDigestDTO dailyDigest = dailyDigestService.generateTodayDigest();
        List<UserProfile> targetUsers = listDigestTargetUsers();
        if (CollUtil.isEmpty(targetUsers)) {
            return new PushResult(0, 0, 0);
        }
        Client client = buildClient();
        String content = buildDigestTextContent(dailyDigest);
        int successCount = 0;
        int failedCount = 0;
        for (UserProfile userProfile : targetUsers) {
            if (sendTextMessage(client, userProfile.getFeishuUserId(), content)) {
                successCount++;
            } else {
                failedCount++;
            }
        }
        return new PushResult(targetUsers.size(), successCount, failedCount);
    }

    /**
     * 批量更新推送状态
     */
    @Override
    public boolean markItemsAsPushed(List<Long> itemIds) {
        if (CollUtil.isEmpty(itemIds)) {
            return true;
        }
        LocalDateTime now = now();
        for (Long itemId : itemIds) {
            ContentItem contentItem = new ContentItem();
            contentItem.setId(itemId);
            contentItem.setPushStatus(1);
            contentItem.setPushTime(now);
            contentItemService.updateById(contentItem);
        }
        return true;
    }

    /**
     * 推送单条内容
     */
    protected boolean pushSingleContentItem(ContentItem contentItem) {
        return pushSingleContentItemWithResult(contentItem).success();
    }

    /**
     * 推送单条内容并返回结果
     */
    protected PushAttemptResult pushSingleContentItemWithResult(ContentItem contentItem) {
        try {
            Client client = buildClient();
            Map<String, Object> fields = buildFeishuRecordFields(contentItem);
            AppTableRecord appTableRecord = AppTableRecord.newBuilder()
                    .fields(fields)
                    .build();
            CreateAppTableRecordReq req = CreateAppTableRecordReq.newBuilder()
                    .appToken(feishuBaseProperties.getAppToken())
                    .tableId(feishuBaseProperties.getTableId())
                    .appTableRecord(appTableRecord)
                    .build();
            CreateAppTableRecordResp resp = client.bitable().appTableRecord().create(req);
            if (resp == null) {
                log.error("飞书推送失败，响应为空，contentItemId={}", contentItem.getId());
                return new PushAttemptResult(false, "飞书响应为空");
            }
            if (!resp.success()) {
                String failReason = String.format(
                        "code=%s,msg=%s",
                        resp.getCode(),
                        StrUtil.blankToDefault(resp.getMsg(), "")
                );
                log.error(
                        "飞书推送失败，contentItemId={}, title={}, code={}, msg={}, requestId={}",
                        contentItem.getId(),
                        contentItem.getTitle(),
                        resp.getCode(),
                        resp.getMsg(),
                        resp.getRequestId()
                );
                return new PushAttemptResult(false, failReason);
            }
            return new PushAttemptResult(true, null);
        } catch (Exception e) {
            log.error(
                    "飞书推送异常，contentItemId={}, title={}",
                    contentItem.getId(),
                    contentItem.getTitle(),
                    e
            );
            return new PushAttemptResult(false, e.getMessage());
        }
    }

    /**
     * 查询待推送用户内容
     */
    protected List<UserContentPush> listPendingUserPushItems() {
        return userContentPushService.listEnqueueablePushesByChannel("feishu");
    }

    /**
     * 记录最终失败推送告警
     */
    protected void createFinalPushFailedAlertIfNeeded(UserContentPush userContentPush, String failReason) {
        if (!isFinalFailure(userContentPush)) {
            return;
        }
        alertRecordService.createOrUpdateAlert(
                "push_final_failed",
                "error",
                "user_content_push",
                userContentPush.getId(),
                "用户内容推送最终失败",
                "pushId=" + userContentPush.getId() + ", reason=" + failReason
        );
    }

    /**
     * 判断是否达到最终失败条件
     */
    protected boolean isFinalFailure(UserContentPush userContentPush) {
        if (userContentPush == null) {
            return false;
        }
        int currentRetryCount = userContentPush.getRetryCount() == null ? 0 : userContentPush.getRetryCount();
        int maxRetryCount = userContentPush.getMaxRetryCount() == null ? 3 : userContentPush.getMaxRetryCount();
        return currentRetryCount + 1 >= maxRetryCount;
    }

    /**
     * 校验飞书配置
     */
    protected void validateFeishuConfig() {
        ThrowUtils.throwIf(StrUtil.hasBlank(
                feishuBaseProperties.getAppId(),
                feishuBaseProperties.getAppSecret(),
                feishuBaseProperties.getAppToken(),
                feishuBaseProperties.getTableId()
        ), ErrorCode.PARAMS_ERROR, "飞书配置不完整");
    }

    /**
     * 构建飞书客户端
     */
    protected Client buildClient() {
        return Client.newBuilder(
                feishuBaseProperties.getAppId(),
                feishuBaseProperties.getAppSecret()
        ).build();
    }

    /**
     * 查询日报接收用户
     */
    protected List<UserProfile> listDigestTargetUsers() {
        return userProfileService.listEnabledUsers().stream()
                .filter(item -> StrUtil.equalsIgnoreCase(item.getPushChannel(), "feishu"))
                .filter(item -> StrUtil.isNotBlank(item.getFeishuUserId()))
                .toList();
    }

    /**
     * 发送飞书文本消息
     */
    protected boolean sendTextMessage(Client client, String receiveId, String content) {
        try {
            CreateMessageReq req = CreateMessageReq.newBuilder()
                    .receiveIdType("open_id")
                    .createMessageReqBody(CreateMessageReqBody.newBuilder()
                            .receiveId(receiveId)
                            .msgType("text")
                            .content(content)
                            .uuid(UUID.randomUUID().toString())
                            .build())
                    .build();
            CreateMessageResp resp = client.im().message().create(req);
            return resp != null && resp.success();
        } catch (Exception e) {
            log.error("飞书文本消息发送异常，receiveId={}", receiveId, e);
            return false;
        }
    }

    /**
     * 构建日报文本消息
     */
    protected String buildDigestTextContent(DailyDigestDTO dailyDigest) {
        StringBuilder textBuilder = new StringBuilder();
        textBuilder.append("【信息节食 AI 日报】\n");
        textBuilder.append(StrUtil.blankToDefault(dailyDigest.getDigestTitle(), "AI 日报")).append("\n");
        textBuilder.append("精选事件：").append(defaultInt(dailyDigest.getTotalClusterCount())).append(" 条").append("\n");
        textBuilder.append("精选内容：").append(defaultInt(dailyDigest.getTotalItemCount())).append(" 条").append("\n");
        textBuilder.append("摘要：").append(StrUtil.blankToDefault(dailyDigest.getSummary(), "暂无摘要")).append("\n");
        if (CollUtil.isNotEmpty(dailyDigest.getSections())) {
            for (var section : dailyDigest.getSections()) {
                textBuilder.append("\n");
                textBuilder.append("【").append(StrUtil.blankToDefault(section.getSectionTitle(), "未分类")).append("】\n");
                List<ContentEventClusterDTO> clusters = section.getClusters();
                if (CollUtil.isEmpty(clusters)) {
                    continue;
                }
                int limit = Math.min(clusters.size(), 3);
                for (int i = 0; i < limit; i++) {
                    ContentEventClusterDTO cluster = clusters.get(i);
                    textBuilder.append(i + 1)
                            .append(". ")
                            .append(StrUtil.blankToDefault(cluster.getClusterTitle(), "未命名事件"))
                            .append("（score=")
                            .append(defaultInt(cluster.getClusterScore()))
                            .append("，items=")
                            .append(defaultInt(cluster.getClusterSize()))
                            .append("）\n");
                }
            }
        }
        Map<String, String> content = new LinkedHashMap<>();
        content.put("text", textBuilder.toString().trim());
        return Jsons.DEFAULT.toJson(content);
    }

    /**
     * 构建飞书记录字段
     */
    protected Map<String, Object> buildFeishuRecordFields(ContentItem contentItem) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("标题", StrUtil.blankToDefault(contentItem.getTitle(), ""));
        fields.put("描述", StrUtil.blankToDefault(contentItem.getDescription(), ""));
        fields.put("链接", StrUtil.blankToDefault(contentItem.getContentUrl(), ""));
        fields.put("作者", StrUtil.blankToDefault(contentItem.getAuthorName(), ""));
        fields.put("平台", StrUtil.blankToDefault(contentItem.getPlatform(), ""));
        fields.put("语言", StrUtil.blankToDefault(contentItem.getLanguage(), ""));
        fields.put("总 Star 数", contentItem.getStarCount());
        fields.put("今日新增 Star 数", contentItem.getTodayStarCount());
        fields.put("抓取日期", contentItem.getCrawlDate() == null ? "" : contentItem.getCrawlDate().toString());
        return fields;
    }

    /**
     * 默认整数
     */
    protected int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    /**
     * 获取当前时间
     */
    protected LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * 推送结果
     */
    public record PushAttemptResult(boolean success, String failReason) {
    }
}
