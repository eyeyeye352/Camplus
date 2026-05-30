USE camplus_db;

-- 1. users 用户表
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名'
);

-- 2. faq_items 固定问题表
CREATE TABLE faq_items (
    faq_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '固定问题ID',
    question_count INT DEFAULT 0 COMMENT '提问次数',
    like_count INT DEFAULT 0 COMMENT '点赞次数',
    hot_score INT DEFAULT 0 COMMENT '热度分',
    display_status TINYINT DEFAULT 0 COMMENT '展示状态（0为下架，1为展示）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    question VARCHAR(500) NOT NULL COMMENT '问题',
    answer TEXT COMMENT '答案'
);

-- 3. knowledge_docs 知识资料表
CREATE TABLE knowledge_docs (
    doc_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '资料ID'
);

-- 4. qa_logs 问答记录表
CREATE TABLE qa_logs (
    qa_log_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '问答记录ID'
);

-- 5. user_contributions 用户贡献表
CREATE TABLE user_contributions (
    contribution_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '贡献ID'
);

-- 6. admin_operation_logs 管理员操作日志表
CREATE TABLE admin_operation_logs (
    op_log_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '操作日志ID'
);
