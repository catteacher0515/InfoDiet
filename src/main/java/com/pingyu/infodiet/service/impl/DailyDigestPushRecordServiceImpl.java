package com.pingyu.infodiet.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.pingyu.infodiet.common.PageResponse;
import com.pingyu.infodiet.mapper.DailyDigestPushRecordMapper;
import com.pingyu.infodiet.model.dto.content.DailyDigestDTO;
import com.pingyu.infodiet.model.entity.DailyDigestPushRecord;
import com.pingyu.infodiet.model.entity.UserProfile;
import com.pingyu.infodiet.service.DailyDigestPushRecordService;
import org.springframework.stereotype.Service;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 日报推送记录服务实现
 */
@Service
public class DailyDigestPushRecordServiceImpl extends ServiceImpl<DailyDigestPushRecordMapper, DailyDigestPushRecord>
        implements DailyDigestPushRecordService {

    /**
     * 保存或更新推送记录
     */
    @Override
    public DailyDigestPushRecord saveOrUpdatePushRecord(
            DailyDigestDTO digest,
            UserProfile userProfile,
            String pushChannel,
            boolean success,
            String failReason,
            LocalDateTime pushTime
    ) {
        if (digest == null || digest.getDigestDate() == null || userProfile == null || userProfile.getId() == null) {
            return null;
        }
        DailyDigestPushRecord existing = getByDigestDateAndUserIdAndPushChannel(
                digest.getDigestDate(),
                userProfile.getId(),
                pushChannel
        );
        int pushStatus = success ? 1 : 2;
        if (existing == null) {
            DailyDigestPushRecord record = DailyDigestPushRecord.builder()
                    .digestDate(digest.getDigestDate())
                    .digestTitle(digest.getDigestTitle())
                    .userId(userProfile.getId())
                    .pushChannel(StrUtil.blankToDefault(pushChannel, ""))
                    .receiveId(StrUtil.blankToDefault(userProfile.getFeishuUserId(), ""))
                    .pushStatus(pushStatus)
                    .pushTime(pushTime)
                    .failReason(success ? null : StrUtil.blankToDefault(failReason, ""))
                    .build();
            try {
                this.save(record);
                return record;
            } catch (DuplicateKeyException e) {
                DailyDigestPushRecord concurrentExisting = getByDigestDateAndUserIdAndPushChannel(
                        digest.getDigestDate(),
                        userProfile.getId(),
                        pushChannel
                );
                if (concurrentExisting == null) {
                    throw e;
                }
                existing = concurrentExisting;
            }
        }
        DailyDigestPushRecord updateRecord = DailyDigestPushRecord.builder()
                .id(existing.getId())
                .digestTitle(digest.getDigestTitle())
                .receiveId(StrUtil.blankToDefault(userProfile.getFeishuUserId(), ""))
                .pushStatus(pushStatus)
                .pushTime(pushTime)
                .failReason(success ? null : StrUtil.blankToDefault(failReason, ""))
                .build();
        this.updateById(updateRecord);
        existing.setDigestTitle(updateRecord.getDigestTitle());
        existing.setReceiveId(updateRecord.getReceiveId());
        existing.setPushStatus(updateRecord.getPushStatus());
        existing.setPushTime(updateRecord.getPushTime());
        existing.setFailReason(updateRecord.getFailReason());
        return existing;
    }

    /**
     * 按日期、用户和渠道查询推送记录
     */
    @Override
    public DailyDigestPushRecord getByDigestDateAndUserIdAndPushChannel(LocalDate digestDate, Long userId, String pushChannel) {
        if (digestDate == null || userId == null || StrUtil.isBlank(pushChannel)) {
            return null;
        }
        return this.getOne(QueryWrapper.create()
                .eq("digestDate", digestDate)
                .eq("userId", userId)
                .eq("pushChannel", pushChannel)
                .limit(1));
    }

    /**
     * 查询最近失败记录
     */
    @Override
    public List<DailyDigestPushRecord> listRecentFailedRecords(int limit) {
        int safeLimit = limit <= 0 ? 10 : Math.min(limit, 100);
        return this.list(QueryWrapper.create()
                .eq("pushStatus", 2)
                .orderBy("pushTime", false)
                .limit(safeLimit));
    }

    /**
     * 分页查询推送记录
     */
    @Override
    public PageResponse<DailyDigestPushRecord> pagePushRecords(Integer pushStatus, String keyword, LocalDate digestDate, int pageNum, int pageSize) {
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = pageSize <= 0 ? 10 : Math.min(pageSize, 100);
        List<DailyDigestPushRecord> records = this.list(QueryWrapper.create()
                .orderBy("pushTime", false)).stream()
                .filter(item -> pushStatus == null || pushStatus.equals(item.getPushStatus()))
                .filter(item -> digestDate == null || digestDate.equals(item.getDigestDate()))
                .filter(item -> matchesKeyword(item, keyword))
                .toList();
        int fromIndex = Math.min((safePageNum - 1) * safePageSize, records.size());
        int toIndex = Math.min(fromIndex + safePageSize, records.size());
        return new PageResponse<>(records.size(), safePageNum, safePageSize, records.subList(fromIndex, toIndex));
    }

    /**
     * 关键字匹配
     */
    protected boolean matchesKeyword(DailyDigestPushRecord record, String keyword) {
        if (record == null || StrUtil.isBlank(keyword)) {
            return true;
        }
        String trimmedKeyword = keyword.trim();
        return String.valueOf(record.getId()).contains(trimmedKeyword)
                || String.valueOf(record.getUserId()).contains(trimmedKeyword)
                || StrUtil.containsIgnoreCase(record.getReceiveId(), trimmedKeyword)
                || StrUtil.containsIgnoreCase(record.getDigestTitle(), trimmedKeyword)
                || StrUtil.containsIgnoreCase(record.getFailReason(), trimmedKeyword);
    }
}
