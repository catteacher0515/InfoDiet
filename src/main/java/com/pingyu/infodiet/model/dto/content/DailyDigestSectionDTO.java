package com.pingyu.infodiet.model.dto.content;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 日报分组
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyDigestSectionDTO {

    /**
     * 分组标题
     */
    private String sectionTitle;

    /**
     * 分组内容数
     */
    private Integer itemCount;

    /**
     * 事件簇列表
     */
    private List<ContentEventClusterDTO> clusters;
}
