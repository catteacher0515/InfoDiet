package com.pingyu.infodiet.controller;

import com.mybatisflex.core.paginate.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import com.pingyu.infodiet.model.entity.ContentItem;
import com.pingyu.infodiet.service.ContentItemService;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * 内容抓取表 控制层。
 *
 * @author pingyu
 */
@RestController
@RequestMapping("/contentItem")
public class ContentItemController {

    @Autowired
    private ContentItemService contentItemService;

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

}
