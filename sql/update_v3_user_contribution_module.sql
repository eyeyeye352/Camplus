USE camplus_db;

-- 用户贡献模块第一版表结构。
DROP TABLE IF EXISTS user_contributions;

CREATE TABLE user_contributions (
    contribution_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '贡献ID',
    user_id INT NOT NULL COMMENT '提交用户ID，关联 users.user_id',
    contribution_type TINYINT NOT NULL COMMENT '贡献类型：0新增问题，1答案纠错',
    title VARCHAR(128) NOT NULL COMMENT '贡献标题',
    content TEXT NOT NULL COMMENT '贡献内容',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态：0待审核，1通过，2拒绝',
    review_comment VARCHAR(255) COMMENT '审核意见',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户贡献表';
