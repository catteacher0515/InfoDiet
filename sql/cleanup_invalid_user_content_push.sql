use pingyu_info_diet;

-- 清理历史联调留下的 user_content_push 坏样本。
-- 规则：
-- 1. user_content_push 指向不存在的 content_item
-- 2. 仅对 user_content_push / alert_record 做逻辑删除
-- 3. 不删除正常 content_item，不改真实用户数据

update user_content_push ucp
left join content_item ci on ci.id = ucp.contentItemId and ci.isDelete = 0
set ucp.isDelete = 1
where ucp.isDelete = 0
  and ci.id is null;

update alert_record ar
left join user_content_push ucp on ucp.id = ar.sourceId
set ar.isDelete = 1
where ar.isDelete = 0
  and ar.sourceType = 'user_content_push'
  and (ucp.id is null or ucp.isDelete = 1);
