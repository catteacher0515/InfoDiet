package com.pingyu.infodiet.service.impl;

import com.pingyu.infodiet.config.InfoDietProperties;
import com.pingyu.infodiet.model.dto.content.UnifiedContentItemDTO;
import com.pingyu.infodiet.model.dto.content.UnifiedContentQueryRequest;
import com.pingyu.infodiet.service.ContentItemService;
import com.pingyu.infodiet.service.ContentSelectionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 内容精选服务实现
 */
@Service
public class ContentSelectionServiceImpl implements ContentSelectionService {

    @Resource
    private ContentItemService contentItemService;

    @Resource
    private InfoDietProperties infoDietProperties;

    /**
     * 查询精选内容
     */
    @Override
    public List<UnifiedContentItemDTO> listFeaturedContentItems() {
        UnifiedContentQueryRequest request = new UnifiedContentQueryRequest();
        request.setSortBy("score");
        request.setMinQualityScore(resolveFeaturedMinQualityScore());
        return contentItemService.listUnifiedContentItems(request);
    }

    /**
     * 获取精选最低分
     */
    protected Integer resolveFeaturedMinQualityScore() {
        if (infoDietProperties == null || infoDietProperties.getFeaturedMinQualityScore() == null) {
            return 70;
        }
        return infoDietProperties.getFeaturedMinQualityScore();
    }
}
