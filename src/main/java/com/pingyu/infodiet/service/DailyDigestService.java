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
}
