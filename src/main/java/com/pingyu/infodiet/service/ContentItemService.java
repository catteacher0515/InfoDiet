package com.pingyu.infodiet.service;

import com.mybatisflex.core.service.IService;
import com.pingyu.infodiet.model.dto.github.GithubTrendingItemDTO;
import com.pingyu.infodiet.model.entity.ContentItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 内容抓取表 服务层。
 *
 * @author pingyu
 */
public interface ContentItemService extends IService<ContentItem> {

    /**
     * 将 GitHub Trending 抓取结果转换为内容项
     *
     * @param dto GitHub Trending 抓取结果
     * @return 内容项
     */
    ContentItem convertGithubTrendingItem(GithubTrendingItemDTO dto);

    /**
     * 批量保存 GitHub Trending 抓取结果
     *
     * @param dtoList GitHub Trending 抓取结果列表
     * @return 保存结果
     */
    SaveResult saveGithubTrendingItems(List<GithubTrendingItemDTO> dtoList);

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class SaveResult {

        /**
         * 本次抓取总数
         */
        private int totalCount;

        /**
         * 实际新增数
         */
        private int savedCount;

        /**
         * 跳过数
         */
        private int skippedCount;
    }
}
