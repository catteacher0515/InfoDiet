alter table content_item
    add column qualityScore int not null default 0 comment '内容质量分' after preFilterReason,
    add column qualityScoreReason varchar(1024) null comment '质量分说明' after qualityScore;

create index idx_preFilterStatus_qualityScore on content_item (preFilterStatus, qualityScore);
