package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.dto.content.DailyDigestDTO;

/**
 * AI 日报服务
 */
public interface DailyDigestService {

    /**
     * 生成今日日报
     */
    DailyDigestDTO generateTodayDigest();

    /**
     * 查询最近日报
     */
    java.util.List<DailyDigestDTO> listRecentDigests(int limit);

    /**
     * 按日期查询日报详情
     */
    DailyDigestDTO getDigestByDate(java.time.LocalDate digestDate);
}
