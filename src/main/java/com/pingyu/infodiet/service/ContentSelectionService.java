package com.pingyu.infodiet.service;

import com.pingyu.infodiet.model.dto.content.UnifiedContentItemDTO;

import java.util.List;

/**
 * 内容精选服务
 */
public interface ContentSelectionService {

    /**
     * 查询精选内容
     */
    List<UnifiedContentItemDTO> listFeaturedContentItems();
}
