USE camplus_db;
-- 第一版修正users表结构

DROP TABLE IF EXISTS users;

CREATE TABLE `users` (
  `user_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(32) NOT NULL COMMENT '用户名',
  `password_hash` varchar(255) NOT NULL COMMENT '密码哈希值',
  `email` varchar(64) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `nickname` varchar(32) DEFAULT NULL COMMENT '昵称',
  `avatar_url` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `role` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '角色，0普通用户，1管理员',
  `status` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '账号状态，0禁用，1正常',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `login_error_count` int unsigned NOT NULL DEFAULT '0' COMMENT '登录错误次数',
  `lock_time` datetime DEFAULT NULL COMMENT '账号锁定截止时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
