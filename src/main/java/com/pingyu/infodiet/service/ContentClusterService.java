package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.dto.content.ContentEventClusterDTO;
import com.pingyu.infodiet.model.dto.content.UnifiedContentItemDTO;

import java.util.List;

/**
 * 内容聚类服务
 */
public interface ContentClusterService {

    /**
     * 查询精选内容事件簇
     */
    List<ContentEventClusterDTO> listFeaturedClusters();

    /**
     * 对内容列表执行事件聚类
     */
    List<ContentEventClusterDTO> clusterContentItems(List<UnifiedContentItemDTO> items);
}
