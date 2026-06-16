USE camplus_db;

-- 1. faq_items 对应的向量存储表
-- 存储FAQ问题和答案的向量表示，用于相似度检索
CREATE TABLE IF NOT EXISTS faq_vector_store (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    faq_id INT NOT NULL COMMENT '关联的faq_items表的faq_id',
    question TEXT NOT NULL COMMENT '原始问题文本',
    answer TEXT COMMENT '原始答案文本',
    question_embedding BLOB NOT NULL COMMENT '问题文本的向量表示（bge-m3模型输出）',
    answer_embedding BLOB COMMENT '答案文本的向量表示',
    combined_embedding BLOB NOT NULL COMMENT '问题+答案合并后的向量表示（用于综合检索）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_faq_id (faq_id),
    CONSTRAINT fk_faq_vector FOREIGN KEY (faq_id) REFERENCES faq_items(faq_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='FAQ向量存储表';

-- 2. knowledge_docs 对应的向量存储表
-- 存储知识库文档的向量表示，支持按内容片段检索
CREATE TABLE IF NOT EXISTS knowledge_vector_store (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    doc_id INT NOT NULL COMMENT '关联的knowledge_docs表的doc_id',
    chunk_index INT NOT NULL COMMENT '文档切片索引（同一文档可能被切分为多个片段）',
    chunk_content TEXT NOT NULL COMMENT '切片后的文本内容',
    chunk_embedding BLOB NOT NULL COMMENT '文本切片的向量表示',
    chunk_metadata JSON COMMENT '切片元数据（如来源文档名、页码、章节等）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_doc_id (doc_id),
    CONSTRAINT fk_knowledge_vector FOREIGN KEY (doc_id) REFERENCES knowledge_docs(doc_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库向量存储表';

-- 3. 更新knowledge_docs表，添加必要字段
ALTER TABLE knowledge_docs
ADD COLUMN IF NOT EXISTS doc_name VARCHAR(255) COMMENT '文档名称',
ADD COLUMN IF NOT EXISTS doc_path VARCHAR(500) COMMENT '文档路径',
ADD COLUMN IF NOT EXISTS doc_content TEXT COMMENT '完整文档内容',
ADD COLUMN IF NOT EXISTS doc_type VARCHAR(50) COMMENT '文档类型（txt/pdf/docx等）',
ADD COLUMN IF NOT EXISTS chunk_count INT DEFAULT 0 COMMENT '切片数量',
ADD COLUMN IF NOT EXISTS created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
ADD COLUMN IF NOT EXISTS updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- 4. 创建向量检索临时表（用于存储检索结果）
CREATE TABLE IF NOT EXISTS vector_search_temp (
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