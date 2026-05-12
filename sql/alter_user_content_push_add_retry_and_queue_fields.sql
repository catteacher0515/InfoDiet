use pingyu_info_diet;

alter table user_content_push
    add column queueStatus tinyint not null default 0 comment '队列状态 0-待入队 1-已入队 2-消费中 3-已完成' after pushStatus,
    add column retryCount int not null default 0 comment '已重试次数' after queueStatus,
    add column maxRetryCount int not null default 3 comment '最大重试次数' after retryCount,
    add column nextRetryTime datetime null comment '下次重试时间' after maxRetryCount,
    add column lastQueueTime datetime null comment '最近入队时间' after nextRetryTime;

create index idx_queueStatus_retryTime on user_content_push (queueStatus, nextRetryTime);
