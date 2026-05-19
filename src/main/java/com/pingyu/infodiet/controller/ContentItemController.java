package com.pingyu.infodiet.controller;

import com.mybatisflex.core.paginate.Page;
import com.pingyu.infodiet.common.BaseResponse;
import com.pingyu.infodiet.common.ResultUtils;
import com.pingyu.infodiet.model.dto.content.ContentEventClusterDTO;
import com.pingyu.infodiet.model.dto.content.ContentItemKeywordFilterRequest;
import com.pingyu.infodiet.model.dto.content.DailyDigestDTO;
import com.pingyu.infodiet.model.dto.content.UnifiedContentItemDTO;
import com.pingyu.infodiet.model.dto.content.UnifiedContentQueryRequest;
import com.pingyu.infodiet.model.entity.ContentItem;
import com.pingyu.infodiet.service.ContentClusterService;
import com.pingyu.infodiet.service.ContentItemService;
import com.pingyu.infodiet.service.ContentPreFilterService;
import com.pingyu.infodiet.service.ContentScoringService;
import com.pingyu.infodiet.service.ContentSelectionService;
import com.pingyu.infodiet.service.DailyDigestService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.time.LocalDate;

/**
 * 内容抓取表 控制层。
 *
 * @author pingyu
 */
@RestController
@RequestMapping("/contentItem")
public class ContentItemController {

    @Resource
    private ContentItemService contentItemService;

    @Resource
    private ContentPreFilterService contentPreFilterService;

    @Resource
    private ContentScoringService contentScoringService;

    @Resource
    private ContentSelectionService contentSelectionService;

    @Resource
    private ContentClusterService contentClusterService;

    @Resource
    private DailyDigestService dailyDigestService;

    /**
     * 保存内容抓取表。
     *
     * @param contentItem 内容抓取表
     * @return {@code true} 保存成功，{@code false} 保存失败
     */
    @PostMapping("save")
    public boolean save(@RequestBody ContentItem contentItem) {
        return contentItemService.save(contentItem);
    }

    /**
     * 根据主键删除内容抓取表。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    public boolean remove(@PathVariable Long id) {
        return contentItemService.removeById(id);
    }

    /**
     * 根据主键更新内容抓取表。
     *
     * @param contentItem 内容抓取表
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    public boolean update(@RequestBody ContentItem contentItem) {
        return contentItemService.updateById(contentItem);
    }

    /**
     * 查询所有内容抓取表。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    public List<ContentItem> list() {
        return contentItemService.list();
    }

    /**
     * 根据主键获取内容抓取表。
     *
     * @param id 内容抓取表主键
     * @return 内容抓取表详情
     */
    @GetMapping("getInfo/{id}")
    public ContentItem getInfo(@PathVariable Long id) {
        return contentItemService.getById(id);
    }

    /**
     * 分页查询内容抓取表。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    public Page<ContentItem> page(Page<ContentItem> page) {
        return contentItemService.page(page);
    }

    /**
     * 根据关键词批量过滤内容
     */
    @PostMapping("filter/keywords")
    public BaseResponse<ContentItemService.KeywordFilterResult> filterByKeywords(
            @RequestBody ContentItemKeywordFilterRequest request
    ) {
        return ResultUtils.success(contentItemService.filterByKeywords(request.getKeywords()));
    }

    /**
     * 执行内容预筛
     */
    @PostMapping("filter/prefilter")
    public BaseResponse<ContentItemService.PreFilterResult> runPreFilter() {
        return ResultUtils.success(contentPreFilterService.runSystemPreFilter());
    }

    /**
     * 执行内容评分
     */
    @PostMapping("filter/score")
    public BaseResponse<ContentItemService.QualityScoreResult> runQualityScoring() {
        return ResultUtils.success(contentScoringService.runQualityScoring());
    }

    /**
     * 查询统一内容列表
     */
    @GetMapping("list/unified")
    public BaseResponse<List<UnifiedContentItemDTO>> listUnifiedContentItems() {
        return ResultUtils.success(contentItemService.listUnifiedContentItems());
    }

    /**
     * 按条件查询统一内容列表
     */
    @GetMapping("list/unified/query")
    public BaseResponse<List<UnifiedContentItemDTO>> listUnifiedContentItems(UnifiedContentQueryRequest request) {
        return ResultUtils.success(contentItemService.listUnifiedContentItems(request));
    }

    /**
     * 查询精选内容列表
     */
    @GetMapping("list/featured")
    public BaseResponse<List<UnifiedContentItemDTO>> listFeaturedContentItems() {
        return ResultUtils.success(contentSelectionService.listFeaturedContentItems());
    }

    /**
     * 查询精选事件簇列表
     */
    @GetMapping("list/featured/clusters")
    public BaseResponse<List<ContentEventClusterDTO>> listFeaturedClusters() {
        return ResultUtils.success(contentClusterService.listFeaturedClusters());
    }

    /**
     * 生成 AI 日报
     */
    @GetMapping("digest/today")
    public BaseResponse<DailyDigestDTO> generateTodayDigest() {
        return ResultUtils.success(dailyDigestService.generateTodayDigest());
    }

    /**
     * 查询最近日报
     */
    @GetMapping("digest/recent")
    public BaseResponse<List<DailyDigestDTO>> listRecentDigests(@org.springframework.web.bind.annotation.RequestParam(defaultValue = "7") int limit) {
        return ResultUtils.success(dailyDigestService.listRecentDigests(limit));
    }

    /**
     * 按日期查询日报详情
     */
    @GetMapping("digest/detail/{digestDate}")
    public BaseResponse<DailyDigestDTO> getDigestByDate(@PathVariable String digestDate) {
        return ResultUtils.success(dailyDigestService.getDigestByDate(LocalDate.parse(digestDate)));
    }

}
