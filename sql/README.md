# Camplus 数据库配置说明

## 1. 数据库初始化

你可以直接点击“一键建库.bat”以按顺序执行此文件夹下的所有sql文件。

**注意：** `init.sql` 会删除原来的 `camplus_db` 数据库，请务必先备份数据。

### SQL文件执行顺序

| 顺序 | 文件 | 说明 |
| --- | --- | --- |
| 1 | `init.sql` | 创建数据库 `camplus_db` |
| 2 | `update_v2_basic_structure.sql` | 创建基础表结构 |
| 3 | `update_v3_user_contribution_module.sql` | 更新用户贡献表 |
| 4 | `update_v4_users_table.sql` | 更新用户表结构 |
| 5 | `update_v5_vector_store.sql` | 创建向量存储相关表 |
| 6 | `update_v6_verification_code.sql` | 创建验证码表 |
| 7 | `update_v7_init_admin.sql` | 初始化管理员账户 |
| 8 | `update_v8_data_import.sql` | 数据导入功能表结构调整（sparse_embedding、source列） |
| 9 | `data.sql` | 系统必需数据 |

### 自动数据导入

一键建库脚本在创建数据库后会**自动执行数据导入**，无需单独启动应用：

1. `一键建库.bat` 创建数据库和表
2. 脚本自动调用 `java -jar target/Camplus.jar --import-only --db-user <用户名> --db-pass <密码>`
3. 程序以非 Web 模式启动，解析 RawData 文件、向量化后入库，完成后自动退出

| 文件类型 | 处理方式 | 存储位置 |
| --- | --- | --- |
| `FAQ.txt` | 解析 `Q:/A:` 格式问答对，向量化后存储 | `faq_items` + `faq_vector_store` |
| `.docx` / `.pdf` / `.txt` | 提取文本、分块、向量化后存储 | `knowledge_docs` + `knowledge_vector_store` |
| `.gitkeep` | 跳过 | - |

**导入条件（同时满足才会触发）：**
1. `faq_items` 和 `knowledge_docs` 表均为空
2. `RawData` 目录存在有效文件
3. BGE-M3 向量化模型已正确加载

> 如果建库时导入失败（如模型未加载），后续启动 Camplus 应用时也会自动尝试导入。

### 默认管理员账户

| 字段 | 值 |
| --- | --- |
| 用户名 | Administrator |
| 密码 | 123456 |
| 邮箱 | admin@camplus.com |
| 手机号 | 13800138000 |

## 2. 数据库信息

- **数据库名**: `camplus_db`
- **字符集**: `utf8mb4`
- **排序规则**: `utf8mb4_unicode_ci`

## 3. 表结构

### 3.1 users 用户表

| 字段名 | 类型 | 约束 | 默认值 | 注释 |
| --- | --- | --- | --- | --- |
| user_id | BIGINT UNSIGNED | PRIMARY KEY, AUTO_INCREMENT | - | 用户ID |
| username | VARCHAR(32) | NOT NULL, UNIQUE | - | 用户名 |
| password_hash | VARCHAR(255) | NOT NULL | - | 密码哈希值 |
| email | VARCHAR(64) | DEFAULT, UNIQUE | NULL | 邮箱 |
| phone | VARCHAR(20) | DEFAULT | NULL | 手机号 |
| nickname | VARCHAR(32) | DEFAULT | NULL | 昵称 |
| avatar_url | VARCHAR(255) | DEFAULT | NULL | 头像URL |
| role | TINYINT UNSIGNED | NOT NULL | 0 | 角色，0普通用户，1管理员 |
| status | TINYINT UNSIGNED | NOT NULL | 1 | 账号状态，0禁用，1正常 |
| last_login_time | DATETIME | DEFAULT | NULL | 最后登录时间 |
| login_error_count | INT UNSIGNED | NOT NULL | 0 | 登录错误次数 |
| lock_time | DATETIME | DEFAULT | NULL | 账号锁定截止时间 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### 3.2 faq_items 固定问题表

| 字段名 | 类型 | 约束 | 默认值 | 注释 |
| --- | --- | --- | --- | --- |
| faq_id | INT | PRIMARY KEY, AUTO_INCREMENT | - | 固定问题ID |
| question_count | INT | DEFAULT | 0 | 提问次数 |
| like_count | INT | DEFAULT | 0 | 点赞次数 |
| hot_score | INT | DEFAULT | 0 | 热度分 |
| display_status | TINYINT | DEFAULT | 0 | 展示状态（0为下架，1为展示） |
| create_time | DATETIME | DEFAULT | CURRENT_TIMESTAMP | 创建时间 |
| question | VARCHAR(500) | NOT NULL | - | 问题 |
| answer | TEXT | - | - | 答案 |
| source | VARCHAR(50) | DEFAULT | manual | 数据来源：manual手动, import导入 |

### 3.3 knowledge_docs 知识资料表

| 字段名 | 类型 | 约束 | 默认值 | 注释 |
| --- | --- | --- | --- | --- |
| doc_id | INT | PRIMARY KEY, AUTO_INCREMENT | - | 资料ID |
| doc_name | VARCHAR(255) | DEFAULT | NULL | 文档名称 |
| doc_path | VARCHAR(500) | DEFAULT | NULL | 文档路径 |
| doc_content | TEXT | DEFAULT | NULL | 完整文档内容 |
| doc_type | VARCHAR(50) | DEFAULT | NULL | 文档类型（txt/pdf/docx等） |
| chunk_count | INT | DEFAULT | 0 | 切片数量 |
| created_at | DATETIME | DEFAULT | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | DEFAULT | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### 3.4 user_contributions 用户贡献表

| 字段名 | 类型 | 约束 | 默认值 | 注释 |
| --- | --- | --- | --- | --- |
| contribution_id | INT | PRIMARY KEY, AUTO_INCREMENT | - | 贡献ID |
| user_id | INT | NOT NULL | - | 提交用户ID，关联 users.user_id |
| contribution_type | TINYINT | NOT NULL | - | 贡献类型：0新增问题，1答案纠错 |
| title | VARCHAR(128) | NOT NULL | - | 贡献标题 |
| content | TEXT | NOT NULL | - | 贡献内容 |
| status | TINYINT | NOT NULL | 0 | 审核状态：0待审核，1通过，2拒绝 |
| review_comment | VARCHAR(255) | DEFAULT | NULL | 审核意见 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 提交时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### 3.5 verification_codes 验证码表

| 字段名 | 类型 | 约束 | 默认值 | 注释 |
| --- | --- | --- | --- | --- |
| id | BIGINT UNSIGNED | PRIMARY KEY, AUTO_INCREMENT | - | 验证码ID |
| target | VARCHAR(128) | NOT NULL | - | 目标邮箱或手机号 |
| code | VARCHAR(8) | NOT NULL | - | 验证码 |
| type | VARCHAR(16) | NOT NULL | - | 类型：email/phone |
| status | TINYINT UNSIGNED | NOT NULL | 0 | 状态：0未使用，1已使用，2已失效 |
| error_count | INT UNSIGNED | NOT NULL | 0 | 验证错误次数 |
| expire_time | DATETIME | NOT NULL | - | 过期时间 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### 3.6 faq_vector_store FAQ向量存储表

| 字段名 | 类型 | 约束 | 默认值 | 注释 |
| --- | --- | --- | --- | --- |
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | - | 主键ID |
| faq_id | INT | NOT NULL, FOREIGN KEY | - | 关联的faq_items表的faq_id |
| question | TEXT | NOT NULL | - | 原始问题文本 |
| answer | TEXT | DEFAULT | NULL | 原始答案文本 |
| question_embedding | BLOB | NOT NULL | - | 问题文本的向量表示（bge-m3模型输出） |
| answer_embedding | BLOB | DEFAULT | NULL | 答案文本的向量表示 |
| combined_embedding | BLOB | NOT NULL | - | 问题+答案合并后的向量表示（用于综合检索） |
| sparse_embedding | TEXT | DEFAULT | NULL | 稀疏向量JSON表示（用于混合检索） |
| created_at | DATETIME | DEFAULT | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | DEFAULT | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### 3.7 knowledge_vector_store 知识库向量存储表

| 字段名 | 类型 | 约束 | 默认值 | 注释 |
| --- | --- | --- | --- | --- |
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | - | 主键ID |
| doc_id | INT | NOT NULL, FOREIGN KEY | - | 关联的knowledge_docs表的doc_id |
| chunk_index | INT | NOT NULL | - | 文档切片索引（同一文档可能被切分为多个片段） |
| chunk_content | TEXT | NOT NULL | - | 切片后的文本内容 |
| chunk_embedding | BLOB | NOT NULL | - | 文本切片的向量表示 |
| chunk_metadata | JSON | DEFAULT | NULL | 切片元数据（如来源文档名、页码、章节等） |
| sparse_embedding | TEXT | DEFAULT | NULL | 稀疏向量JSON表示（用于混合检索） |
| created_at | DATETIME | DEFAULT | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | DEFAULT | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### 3.8 vector_search_temp 向量检索临时结果表

| 字段名 | 类型 | 约束 | 默认值 | 注释 |
| --- | --- | --- | --- | --- |
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | - | 主键ID |
| search_id | VARCHAR(64) | NOT NULL | - | 检索请求唯一标识 |
| table_name | VARCHAR(100) | NOT NULL | - | 检索的表名 |
| record_id | BIGINT | NOT NULL | - | 匹配的记录ID |
| similarity_score | DECIMAL(10,6) | NOT NULL | - | 相似度分数（0-1） |
| content_summary | TEXT | DEFAULT | NULL | 匹配内容摘要 |
| created_at | DATETIME | DEFAULT | CURRENT_TIMESTAMP | 创建时间 |

### 3.9 qa_logs 问答记录表

| 字段名 | 类型 | 约束 | 注释 |
| --- | --- | --- | --- |
| qa_log_id | INT | PRIMARY KEY, AUTO_INCREMENT | 问答记录ID |

### 3.10 admin_operation_logs 管理员操作日志表

| 字段名 | 类型 | 约束 | 注释 |
| --- | --- | --- | --- |
| op_log_id | INT | PRIMARY KEY, AUTO_INCREMENT | 操作日志ID |

## 4. 关联关系

### 4.1 外键关联

| 主表 | 主表字段 | 从表 | 从表字段 | 操作 |
| --- | --- | --- | --- | --- |
| faq_items | faq_id | faq_vector_store | faq_id | ON DELETE CASCADE |
| knowledge_docs | doc_id | knowledge_vector_store | doc_id | ON DELETE CASCADE |

### 4.2 逻辑关联

| 主表 | 主表字段 | 从表 | 从表字段 | 说明 |
| --- | --- | --- | --- | --- |
| users | user_id | user_contributions | user_id | 用户贡献关联 |
| users | email/phone | verification_codes | target | 验证码目标关联 |

### 4.3 索引

| 表名 | 索引名 | 字段 | 类型 |
| --- | --- | --- | --- |
| users | uk_username | username | UNIQUE |
| users | uk_email | email | UNIQUE |
| faq_vector_store | idx_faq_id | faq_id | INDEX |
| knowledge_vector_store | idx_doc_id | doc_id | INDEX |
| vector_search_temp | idx_search_id | search_id | INDEX |
| vector_search_temp | idx_table_record | table_name, record_id | INDEX |
| verification_codes | idx_target_type | target, type | INDEX |

## 5. 待处理问题

### 5.1 数据类型不一致

| 问题 | 详情 | 影响 |
| --- | --- | --- |
| `user_contributions.user_id` 类型不匹配 | `users.user_id` 为 `BIGINT UNSIGNED`，但 `user_contributions.user_id` 仍为 `INT` | 当用户ID超过INT范围（2147483647）时会导致关联失败 |

### 5.2 字符集不一致

| 问题 | 详情 |
| --- | --- |
| 数据库默认字符集 | `init.sql` 设置数据库默认字符集为 `utf8mb4_general_ci` |
| 表字符集 | `update_v4_users_table.sql` 及后续版本的表使用 `utf8mb4_unicode_ci` |
| 影响 | 不同表之间排序规则可能不一致，建议统一使用 `utf8mb4_unicode_ci` |

### 5.3 未完成的表结构

| 表名 | 状态 | 说明 |
| --- | --- | --- |
| `qa_logs` | 骨架表 | 仅包含主键字段，需要添加完整的字段定义 |
| `admin_operation_logs` | 骨架表 | 仅包含主键字段，需要添加完整的字段定义 |

## 6. SQL文件管理规范

- 后续修改表结构时，新建一个 `update_版本号_修改内容.sql` 文件
- 版本号格式：`v1`, `v2`, `v3`...
- 修改了关联字段的话，需要更新本 README.md 中的关联关系部分
- 不要删除别人的表或者字段，有问题请在群里沟通