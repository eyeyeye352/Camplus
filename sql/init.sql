-- ============================================================
-- Camplus 数据库初始化脚本
-- 一键建库.bat 会先执行此文件创建库和所有表，再执行 data.sql
-- ============================================================

DROP DATABASE IF EXISTS camplus_db;

CREATE DATABASE camplus_db
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE camplus_db;

-- ============================================================
-- 1. users 用户表
-- ============================================================
CREATE TABLE users (
    user_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(32) NOT NULL COMMENT '用户名',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希值',
    email VARCHAR(64) DEFAULT NULL COMMENT '邮箱',
    role TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '角色，0普通用户，1管理员',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================================
-- 2. faq_items 固定问题表
-- ============================================================
CREATE TABLE faq_items (
    faq_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '固定问题ID',
    question VARCHAR(500) NOT NULL COMMENT '问题',
    answer TEXT COMMENT '答案',
    question_count INT DEFAULT 0 COMMENT '今日查询次数（每日凌晨重置为0）',
    hot_score INT DEFAULT 0 COMMENT '热度分（每日凌晨减半，今日查询次数*10累加到热度分）',
    display_status TINYINT DEFAULT 0 COMMENT '展示状态（0为下架，1为展示）',
    source VARCHAR(50) DEFAULT 'manual' COMMENT '数据来源：manual手动, import导入, auto自动',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='固定问题表';

-- ============================================================
-- 3. knowledge_docs 知识资料表
-- ============================================================
CREATE TABLE knowledge_docs (
    doc_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '资料ID',
    doc_name VARCHAR(255) DEFAULT NULL COMMENT '文档名称',
    doc_path VARCHAR(500) DEFAULT NULL COMMENT '文档路径',
    doc_content TEXT COMMENT '完整文档内容',
    doc_type VARCHAR(50) DEFAULT NULL COMMENT '文档类型（txt/pdf/docx等）',
    chunk_count INT DEFAULT 0 COMMENT '切片数量',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识资料表';

-- ============================================================
-- 4. faq_vector_store FAQ向量存储表
-- ============================================================
CREATE TABLE faq_vector_store (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    faq_id INT NOT NULL COMMENT '关联的faq_items表的faq_id',
    question TEXT NOT NULL COMMENT '原始问题文本',
    answer TEXT COMMENT '原始答案文本',
    question_embedding BLOB COMMENT '问题文本的向量表示（bge-m3模型输出）',
    answer_embedding BLOB COMMENT '答案文本的向量表示',
    combined_embedding BLOB NOT NULL COMMENT '问题+答案合并后的向量表示（用于综合检索）',
    sparse_embedding TEXT COMMENT '稀疏向量JSON表示（用于混合检索）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_faq_id (faq_id),
    CONSTRAINT fk_faq_vector FOREIGN KEY (faq_id) REFERENCES faq_items(faq_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='FAQ向量存储表';

-- ============================================================
-- 5. knowledge_vector_store 知识库向量存储表
-- ============================================================
CREATE TABLE knowledge_vector_store (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    doc_id INT NOT NULL COMMENT '关联的knowledge_docs表的doc_id',
    chunk_index INT NOT NULL COMMENT '文档切片索引',
    chunk_content TEXT NOT NULL COMMENT '切片后的文本内容',
    chunk_embedding BLOB NOT NULL COMMENT '文本切片的向量表示',
    chunk_metadata JSON COMMENT '切片元数据（如来源文档名、页码、章节等）',
    sparse_embedding TEXT COMMENT '稀疏向量JSON表示（用于混合检索）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_doc_id (doc_id),
    CONSTRAINT fk_knowledge_vector FOREIGN KEY (doc_id) REFERENCES knowledge_docs(doc_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库向量存储表';

-- ============================================================
-- 6. vector_search_temp 向量检索临时结果表
-- ============================================================
CREATE TABLE vector_search_temp (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    search_id VARCHAR(64) NOT NULL COMMENT '检索请求唯一标识',
    table_name VARCHAR(100) NOT NULL COMMENT '检索的表名',
    record_id BIGINT NOT NULL COMMENT '匹配的记录ID',
    similarity_score DECIMAL(10,6) NOT NULL COMMENT '相似度分数（0-1）',
    content_summary TEXT COMMENT '匹配内容摘要',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_search_id (search_id),
    INDEX idx_table_record (table_name, record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='向量检索临时结果表';

-- ============================================================
-- 7. user_contributions 用户贡献表
-- ============================================================
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户贡献表';

-- ============================================================
-- 8. verification_codes 验证码表
-- ============================================================
CREATE TABLE verification_codes (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '验证码ID',
    target VARCHAR(128) NOT NULL COMMENT '目标邮箱或手机号',
    code VARCHAR(8) NOT NULL COMMENT '验证码',
    type VARCHAR(16) NOT NULL COMMENT '类型：email/phone',
    status TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '状态：0未使用，1已使用，2已失效',
    error_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '验证错误次数',
    expire_time DATETIME NOT NULL COMMENT '过期时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_target_type (target, type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='验证码表';
