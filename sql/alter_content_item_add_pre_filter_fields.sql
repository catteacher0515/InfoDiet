alter table content_item
    add column preFilterStatus tinyint not null default 0 comment '预筛状态 0-待预筛 1-通过 2-过滤' after keywordMatched,
    add column preFilterReason varchar(512) null comment '预筛结果说明' after preFilterStatus;

create index idx_preFilterStatus_pushStatus on content_item (preFilterStatus, pushStatus);
