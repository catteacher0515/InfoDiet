package com.pingyu.infodiet.service.impl;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.pingyu.infodiet.mapper.DailyDigestHistoryMapper;
import com.pingyu.infodiet.model.dto.content.DailyDigestDTO;
import com.pingyu.infodiet.model.entity.DailyDigestHistory;
import com.pingyu.infodiet.service.DailyDigestHistoryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * AI 日报历史服务实现
 */
@Service
public class DailyDigestHistoryServiceImpl extends ServiceImpl<DailyDigestHistoryMapper, DailyDigestHistory>
        implements DailyDigestHistoryService {

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 保存或更新日报历史
     */
    @Override
    public DailyDigestHistory saveOrUpdateDigest(DailyDigestDTO digest) {
        if (digest == null || digest.getDigestDate() == null) {
            return null;
        }
        DailyDigestHistory existing = getByDigestDate(digest.getDigestDate());
        String digestContent = toJson(digest);
        if (existing == null) {
            DailyDigestHistory history = DailyDigestHistory.builder()
                    .digestDate(digest.getDigestDate())
                    .digestTitle(digest.getDigestTitle())
                    .totalClusterCount(digest.getTotalClusterCount())
                    .totalItemCount(digest.getTotalItemCount())
                    .summary(digest.getSummary())
                    .digestContent(digestContent)
                    .build();
            try {
                this.save(history);
                return history;
            } catch (DuplicateKeyException e) {
                DailyDigestHistory concurrentExisting = getByDigestDate(digest.getDigestDate());
                if (concurrentExisting == null) {
                    throw e;
                }
                existing = concurrentExisting;
            }
        }
        DailyDigestHistory updateRecord = DailyDigestHistory.builder()
                .id(existing.getId())
                .digestTitle(digest.getDigestTitle())
                .totalClusterCount(digest.getTotalClusterCount())
                .totalItemCount(digest.getTotalItemCount())
                .summary(digest.getSummary())
                .digestContent(digestContent)
                .build();
        this.updateById(updateRecord);
        existing.setDigestTitle(updateRecord.getDigestTitle());
        existing.setTotalClusterCount(updateRecord.getTotalClusterCount());
        existing.setTotalItemCount(updateRecord.getTotalItemCount());
        existing.setSummary(updateRecord.getSummary());
        existing.setDigestContent(updateRecord.getDigestContent());
        return existing;
    }

    /**
     * 查询最近日报
     */
    @Override
    public List<DailyDigestDTO> listRecentDigests(int limit) {
        int safeLimit = limit <= 0 ? 7 : Math.min(limit, 30);
        List<DailyDigestHistory> historyList = this.list(QueryWrapper.create()
                .orderBy("digestDate", false)
                .limit(safeLimit));
        if (historyList == null || historyList.isEmpty()) {
            return Collections.emptyList();
        }
        return historyList.stream()
                .map(this::toDigestDTO)
                .toList();
    }

    /**
     * 按日期查询日报详情
     */
    @Override
    public DailyDigestDTO getDigestDTOByDate(LocalDate digestDate) {
        DailyDigestHistory history = getByDigestDate(digestDate);
        return history == null ? null : toDigestDTO(history);
    }

    /**
     * 按日期查询日报历史
     */
    @Override
    public DailyDigestHistory getByDigestDate(LocalDate digestDate) {
        if (digestDate == null) {
            return null;
        }
        return this.getOne(QueryWrapper.create()
                .eq("digestDate", digestDate)
                .limit(1));
    }

    /**
     * 转换为日报 DTO
     */
    protected DailyDigestDTO toDigestDTO(DailyDigestHistory history) {
        if (history == null) {
            return null;
        }
        if (StrUtil.isBlank(history.getDigestContent())) {
            return DailyDigestDTO.builder()
                    .digestDate(history.getDigestDate())
                    .digestTitle(history.getDigestTitle())
                    .totalClusterCount(history.getTotalClusterCount())
                    .totalItemCount(history.getTotalItemCount())
                    .summary(history.getSummary())
                    .build();
        }
        try {
            return jsonMapper().readValue(history.getDigestContent(), DailyDigestDTO.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("日报历史反序列化失败", e);
        }
    }

    /**
     * 序列化日报内容
     */
    protected String toJson(DailyDigestDTO digest) {
        try {
            return jsonMapper().writeValueAsString(digest);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("日报历史序列化失败", e);
        }
    }

    /**
     * 获取支持 Java 时间类型的 JSON 映射器
     */
    protected ObjectMapper jsonMapper() {
        ObjectMapper mapper = objectMapper;
        if (mapper == null) {
            return new ObjectMapper().findAndRegisterModules();
        }
        try {
            ObjectMapper copied = mapper.copy();
            if (copied != null) {
                mapper = copied;
            }
        } catch (RuntimeException ignored) {
            // 回退到原始 mapper，避免 mock 或定制实现缺少 copy 支持时直接失败
        }
        return mapper.findAndRegisterModules();
    }
}
