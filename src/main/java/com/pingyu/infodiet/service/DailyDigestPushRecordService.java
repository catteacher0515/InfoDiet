package com.pingyu.infodiet.service;

import com.mybatisflex.core.service.IService;
import com.pingyu.infodiet.common.PageResponse;
import com.pingyu.infodiet.model.dto.content.DailyDigestDTO;
import com.pingyu.infodiet.model.entity.DailyDigestPushRecord;
import com.pingyu.infodiet.model.entity.UserProfile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 日报推送记录服务
 */
public interface DailyDigestPushRecordService extends IService<DailyDigestPushRecord> {

    /**
     * 保存或更新推送记录
     */
    DailyDigestPushRecord saveOrUpdatePushRecord(
            DailyDigestDTO digest,
            UserProfile userProfile,
            String pushChannel,
            boolean success,
            String failReason,
            LocalDateTime pushTime
    );

    /**
     * 按日期、用户和渠道查询推送记录
     */
    DailyDigestPushRecord getByDigestDateAndUserIdAndPushChannel(LocalDate digestDate, Long userId, String pushChannel);

    /**
     * 查询最近失败记录
     */
    List<DailyDigestPushRecord> listRecentFailedRecords(int limit);

    /**
     * 分页查询推送记录
     */
    PageResponse<DailyDigestPushRecord> pagePushRecords(Integer pushStatus, String keyword, LocalDate digestDate, int pageNum, int pageSize);
}
