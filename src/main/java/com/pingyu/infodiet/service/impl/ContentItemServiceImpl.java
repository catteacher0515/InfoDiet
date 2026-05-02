package com.pingyu.infodiet.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.pingyu.infodiet.model.entity.ContentItem;
import com.pingyu.infodiet.mapper.ContentItemMapper;
import com.pingyu.infodiet.service.ContentItemService;
import org.springframework.stereotype.Service;

/**
 * 内容抓取表 服务层实现。
 *
 * @author pingyu
 */
@Service
public class ContentItemServiceImpl extends ServiceImpl<ContentItemMapper, ContentItem>  implements ContentItemService{

}
