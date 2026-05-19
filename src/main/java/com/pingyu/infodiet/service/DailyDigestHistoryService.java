package com.pingyu.infodiet.service;

import com.mybatisflex.core.service.IService;
import com.pingyu.infodiet.model.dto.content.DailyDigestDTO;
import com.pingyu.infodiet.model.entity.DailyDigestHistory;

import java.time.LocalDate;
import java.util.List;

/**
 * AI 日报历史服务
 */
public interface DailyDigestHistoryService extends IService<DailyDigestHistory> {

    /**
     * 保存或更新日报历史
     */
    DailyDigestHistory saveOrUpdateDigest(DailyDigestDTO digest);

    /**
     * 查询最近日报
     */
    List<DailyDigestDTO> listRecentDigests(int limit);

    /**
     * 按日期查询日报详情
     */
    DailyDigestDTO getDigestDTOByDate(LocalDate digestDate);

    /**
     * 按日期查询日报历史
     */
    DailyDigestHistory getByDigestDate(LocalDate digestDate);
}
