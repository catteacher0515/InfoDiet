ALTER TABLE `content_item`
  ADD COLUMN `sourceProfileId` bigint DEFAULT NULL COMMENT '信源档案 ID' AFTER `authorUrl`,
  ADD COLUMN `sourceCategory` varchar(64) NOT NULL DEFAULT 'normal' COMMENT '信源分类' AFTER `sourceProfileId`,
  ADD COLUMN `sourceTier` varchar(16) NOT NULL DEFAULT 'T2' COMMENT '信源等级' AFTER `sourceCategory`;
