package com.pingyu.infodiet.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.pingyu.infodiet.model.dto.content.ContentEventClusterDTO;
import com.pingyu.infodiet.model.dto.content.UnifiedContentItemDTO;
import com.pingyu.infodiet.service.ContentClusterService;
import com.pingyu.infodiet.service.ContentSelectionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 内容聚类服务实现
 */
@Service
public class ContentClusterServiceImpl implements ContentClusterService {

    private static final Pattern TOKEN_SPLIT_PATTERN = Pattern.compile("[^\\p{IsAlphabetic}\\p{IsDigit}]+");

    private static final Set<String> STOP_WORDS = Set.of(
            "the", "a", "an", "and", "or", "to", "of", "for", "with", "in", "on", "by",
            "from", "how", "build", "using", "use", "guide", "tutorial", "video", "repo",
            "release", "released", "new", "update"
    );

    @Resource
    private ContentSelectionService contentSelectionService;

    /**
     * 查询精选内容事件簇
     */
    @Override
    public List<ContentEventClusterDTO> listFeaturedClusters() {
        return clusterContentItems(contentSelectionService.listFeaturedContentItems());
    }

    /**
     * 对内容列表执行事件聚类
     */
    @Override
    public List<ContentEventClusterDTO> clusterContentItems(List<UnifiedContentItemDTO> items) {
        if (CollUtil.isEmpty(items)) {
            return List.of();
        }
        List<UnifiedContentItemDTO> sortedItems = items.stream()
                .sorted(buildItemComparator())
                .toList();
        List<MutableCluster> clusters = new ArrayList<>();
        for (UnifiedContentItemDTO item : sortedItems) {
            Set<String> tokenSet = extractTokenSet(item.getTitle());
            String normalizedTitle = normalizeTitle(item.getTitle());
            MutableCluster matchedCluster = findMatchedCluster(clusters, tokenSet, normalizedTitle);
            if (matchedCluster == null) {
                clusters.add(new MutableCluster(item, tokenSet, normalizedTitle));
                continue;
            }
            matchedCluster.add(item, tokenSet, normalizedTitle, buildItemComparator());
        }
        return clusters.stream()
                .map(MutableCluster::toDTO)
                .sorted(Comparator
                        .comparing(ContentEventClusterDTO::getClusterScore, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(ContentEventClusterDTO::getClusterSize, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(ContentEventClusterDTO::getClusterTitle, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    /**
     * 查找命中的事件簇
     */
    protected MutableCluster findMatchedCluster(List<MutableCluster> clusters, Set<String> tokenSet, String normalizedTitle) {
        for (MutableCluster cluster : clusters) {
            if (isSameCluster(cluster, tokenSet, normalizedTitle)) {
                return cluster;
            }
        }
        return null;
    }

    /**
     * 判断是否属于同一事件簇
     */
    protected boolean isSameCluster(MutableCluster cluster, Set<String> tokenSet, String normalizedTitle) {
        if (cluster == null) {
            return false;
        }
        if (StrUtil.isNotBlank(normalizedTitle) && StrUtil.equals(normalizedTitle, cluster.normalizedTitle)) {
            return true;
        }
        int overlapCount = countOverlap(cluster.tokenSet, tokenSet);
        if (overlapCount >= 2) {
            return true;
        }
        return overlapCount >= 1
                && (StrUtil.contains(cluster.normalizedTitle, normalizedTitle)
                || StrUtil.contains(normalizedTitle, cluster.normalizedTitle));
    }

    /**
     * 提取标题词集合
     */
    protected Set<String> extractTokenSet(String title) {
        Set<String> tokenSet = new LinkedHashSet<>();
        String normalizedTitle = normalizeTitle(title);
        if (StrUtil.isBlank(normalizedTitle)) {
            return tokenSet;
        }
        String[] parts = TOKEN_SPLIT_PATTERN.split(normalizedTitle);
        for (String part : parts) {
            String token = StrUtil.trim(part);
            if (StrUtil.isBlank(token) || token.length() <= 1 || STOP_WORDS.contains(token)) {
                continue;
            }
            tokenSet.add(token);
        }
        return tokenSet;
    }

    /**
     * 标准化标题
     */
    protected String normalizeTitle(String title) {
        return StrUtil.blankToDefault(StrUtil.trim(title), "").toLowerCase(Locale.ROOT);
    }

    /**
     * 统计交集数量
     */
    protected int countOverlap(Set<String> left, Set<String> right) {
        if (CollUtil.isEmpty(left) || CollUtil.isEmpty(right)) {
            return 0;
        }
        int count = 0;
        for (String token : left) {
            if (right.contains(token)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 内容优先级比较器
     */
    protected Comparator<UnifiedContentItemDTO> buildItemComparator() {
        return Comparator
                .comparing(UnifiedContentItemDTO::getQualityScore, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(UnifiedContentItemDTO::getSortTime, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(UnifiedContentItemDTO::getId, Comparator.nullsLast(Comparator.reverseOrder()));
    }

    private static class MutableCluster {

        private UnifiedContentItemDTO primaryItem;

        private final List<UnifiedContentItemDTO> relatedItems = new ArrayList<>();

        private Set<String> tokenSet;

        private String normalizedTitle;

        private MutableCluster(UnifiedContentItemDTO primaryItem, Set<String> tokenSet, String normalizedTitle) {
            this.primaryItem = primaryItem;
            this.tokenSet = new LinkedHashSet<>(tokenSet);
            this.normalizedTitle = normalizedTitle;
            this.relatedItems.add(primaryItem);
        }

        private void add(
                UnifiedContentItemDTO item,
                Set<String> itemTokenSet,
                String itemNormalizedTitle,
                Comparator<UnifiedContentItemDTO> comparator
        ) {
            this.relatedItems.add(item);
            this.tokenSet.addAll(itemTokenSet);
            if (comparator.compare(item, primaryItem) < 0) {
                this.primaryItem = item;
                this.normalizedTitle = itemNormalizedTitle;
            }
        }

        private ContentEventClusterDTO toDTO() {
            List<UnifiedContentItemDTO> sortedItems = relatedItems.stream()
                    .sorted(Comparator
                            .comparing(UnifiedContentItemDTO::getQualityScore, Comparator.nullsLast(Comparator.reverseOrder()))
                            .thenComparing(UnifiedContentItemDTO::getSortTime, Comparator.nullsLast(Comparator.reverseOrder()))
                            .thenComparing(UnifiedContentItemDTO::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                    .toList();
            return ContentEventClusterDTO.builder()
                    .clusterKey("cluster-" + primaryItem.getId())
                    .clusterTitle(primaryItem.getTitle())
                    .clusterScore(primaryItem.getQualityScore())
                    .clusterSize(sortedItems.size())
                    .primaryItem(primaryItem)
                    .relatedItems(sortedItems)
                    .build();
        }
    }
}
