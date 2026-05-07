package com.pingyu.infodiet.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.lark.oapi.Client;
import com.lark.oapi.service.bitable.v1.model.AppTableRecord;
import com.lark.oapi.service.bitable.v1.model.CreateAppTableRecordReq;
import com.lark.oapi.service.bitable.v1.model.CreateAppTableRecordResp;
import com.mybatisflex.core.query.QueryWrapper;
import com.pingyu.infodiet.config.FeishuBaseProperties;
import com.pingyu.infodiet.exception.ErrorCode;
import com.pingyu.infodiet.exception.ThrowUtils;
import com.pingyu.infodiet.model.entity.ContentItem;
import com.pingyu.infodiet.service.ContentItemService;
import com.pingyu.infodiet.service.FeishuPushService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
            boolean pushed = pushSingleContentItem(contentItem);
            if (pushed) {
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
                return false;
            }
            if (!resp.success()) {
                log.error(
                        "飞书推送失败，contentItemId={}, title={}, code={}, msg={}, requestId={}",
                        contentItem.getId(),
                        contentItem.getTitle(),
                        resp.getCode(),
                        resp.getMsg(),
                        resp.getRequestId()
                );
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error(
                    "飞书推送异常，contentItemId={}, title={}",
                    contentItem.getId(),
                    contentItem.getTitle(),
                    e
            );
            return false;
        }
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
     * 获取当前时间
     */
    protected LocalDateTime now() {
        return LocalDateTime.now();
    }
}
