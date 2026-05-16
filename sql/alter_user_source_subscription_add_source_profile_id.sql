ALTER TABLE `user_source_subscription`
  ADD COLUMN `sourceProfileId` bigint DEFAULT NULL COMMENT '关联信源档案 ID' AFTER `sourceValue`;
