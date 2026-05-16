package com.pingyu.infodiet.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.pingyu.infodiet.model.dto.content.ContentEventClusterDTO;
import com.pingyu.infodiet.model.dto.content.DailyDigestDTO;
import com.pingyu.infodiet.model.dto.content.DailyDigestSectionDTO;
import com.pingyu.infodiet.model.dto.content.UnifiedContentItemDTO;
import com.pingyu.infodiet.service.ContentClusterService;
import com.pingyu.infodiet.service.DailyDigestService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * AI 日报服务实现
 */
@Service
public class DailyDigestServiceImpl implements DailyDigestService {

    @Resource
    private ContentClusterService contentClusterService;

    protected LocalDate fixedToday;

    /**
     * 生成今日日报
     */
    @Override
    public DailyDigestDTO generateTodayDigest() {
        List<ContentEventClusterDTO> clusters = contentClusterService.listFeaturedClusters();
        LocalDate today = today();
        List<DailyDigestSectionDTO> sections = buildSections(clusters);
        int totalItemCount = clusters.stream()
                .map(ContentEventClusterDTO::getClusterSize)
                .filter(size -> size != null)
                .mapToInt(Integer::intValue)
                .sum();
        return DailyDigestDTO.builder()
                .digestDate(today)
                .digestTitle("AI 日报 · " + today)
                .totalClusterCount(clusters.size())
                .totalItemCount(totalItemCount)
                .summary(buildSummary(clusters))
                .sections(sections)
                .build();
    }

    /**
     * 构建日报分组
     */
    protected List<DailyDigestSectionDTO> buildSections(List<ContentEventClusterDTO> clusters) {
        List<ContentEventClusterDTO> repositoryClusters = new ArrayList<>();
        List<ContentEventClusterDTO> videoClusters = new ArrayList<>();
        List<ContentEventClusterDTO> otherClusters = new ArrayList<>();
        for (ContentEventClusterDTO cluster : clusters) {
            String contentType = resolveContentType(cluster);
            if ("repository".equalsIgnoreCase(contentType)) {
                repositoryClusters.add(cluster);
                continue;
            }
            if ("video".equalsIgnoreCase(contentType)) {
                videoClusters.add(cluster);
                continue;
            }
            otherClusters.add(cluster);
        }

        List<DailyDigestSectionDTO> sections = new ArrayList<>();
        appendSection(sections, "仓库 / 项目", repositoryClusters);
        appendSection(sections, "视频 / 讲解", videoClusters);
        appendSection(sections, "其他精选", otherClusters);
        return sections;
    }

    /**
     * 追加日报分组
     */
    protected void appendSection(List<DailyDigestSectionDTO> sections, String sectionTitle, List<ContentEventClusterDTO> clusters) {
        if (CollUtil.isEmpty(clusters)) {
            return;
        }
        List<ContentEventClusterDTO> sortedClusters = clusters.stream()
                .sorted(Comparator
                        .comparing(ContentEventClusterDTO::getClusterScore, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(ContentEventClusterDTO::getClusterSize, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(ContentEventClusterDTO::getClusterTitle, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
        sections.add(DailyDigestSectionDTO.builder()
                .sectionTitle(sectionTitle)
                .itemCount(sortedClusters.size())
                .clusters(sortedClusters)
                .build());
    }

    /**
     * 构建日报摘要
     */
    protected String buildSummary(List<ContentEventClusterDTO> clusters) {
        if (CollUtil.isEmpty(clusters)) {
            return "今日暂无符合条件的精选内容。";
        }
        List<String> titles = clusters.stream()
                .limit(3)
                .map(ContentEventClusterDTO::getClusterTitle)
                .filter(StrUtil::isNotBlank)
                .toList();
        if (CollUtil.isEmpty(titles)) {
            return "今日共有 " + clusters.size() + " 条精选事件。";
        }
        return "今日共筛出 " + clusters.size() + " 条精选事件，重点包括：" + String.join("；", titles) + "。";
    }

    /**
     * 解析主条内容类型
     */
    protected String resolveContentType(ContentEventClusterDTO cluster) {
        UnifiedContentItemDTO primaryItem = cluster == null ? null : cluster.getPrimaryItem();
        return primaryItem == null ? "" : StrUtil.blankToDefault(primaryItem.getContentType(), "");
    }

    /**
     * 获取当前日期
     */
    protected LocalDate today() {
        return fixedToday != null ? fixedToday : LocalDate.now();
    }
}
