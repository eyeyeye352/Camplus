USE camplus_db;

CREATE TABLE IF NOT EXISTS `verification_codes` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '验证码ID',
  `target` varchar(128) NOT NULL COMMENT '目标邮箱或手机号',
  `code` varchar(8) NOT NULL COMMENT '验证码',
  `type` varchar(16) NOT NULL COMMENT '类型：email/phone',
  `status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '状态：0未使用，1已使用，2已失效',
  `error_count` int unsigned NOT NULL DEFAULT '0' COMMENT '验证错误次数',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_target_type` (`target`, `type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='验证码表';