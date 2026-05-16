CREATE TABLE IF NOT EXISTS `source_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `platform` varchar(32) NOT NULL,
  `profileType` varchar(64) NOT NULL,
  `sourceKey` varchar(256) NOT NULL,
  `sourceName` varchar(256) DEFAULT NULL,
  `sourceUrl` varchar(1024) DEFAULT NULL,
  `sourceCategory` varchar(64) NOT NULL DEFAULT 'normal',
  `sourceTier` varchar(16) NOT NULL DEFAULT 'T2',
  `status` tinyint NOT NULL DEFAULT '1',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `isDelete` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_platform_profileType` (`platform`,`profileType`),
  KEY `idx_sourceKey` (`sourceKey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
