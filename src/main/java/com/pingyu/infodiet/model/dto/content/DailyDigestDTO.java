package com.pingyu.infodiet.model.dto.content;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * AI 日报
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyDigestDTO {

    /**
     * 日报日期
     */
    private LocalDate digestDate;

    /**
     * 日报标题
     */
    private String digestTitle;

    /**
     * 精选事件总数
     */
    private Integer totalClusterCount;

    /**
     * 精选内容总数
     */
    private Integer totalItemCount;

    /**
     * 日报摘要
     */
    private String summary;

    /**
     * 日报分组
     */
    private List<DailyDigestSectionDTO> sections;
}
