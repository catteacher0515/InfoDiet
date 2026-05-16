package com.pingyu.infodiet.model.dto.content;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 内容事件聚类视图
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentEventClusterDTO {

    /**
     * 事件簇标识
     */
    private String clusterKey;

    /**
     * 事件簇标题
     */
    private String clusterTitle;

    /**
     * 主条质量分
     */
    private Integer clusterScore;

    /**
     * 事件簇内容数量
     */
    private Integer clusterSize;

    /**
     * 主条内容
     */
    private UnifiedContentItemDTO primaryItem;

    /**
     * 相关内容
     */
    private List<UnifiedContentItemDTO> relatedItems;
}
