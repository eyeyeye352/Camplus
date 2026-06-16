# Camplus 数据库配置说明

你可以直接点击“一键建库.bat”以按顺序执行此文件夹下的所有sql文件。
注意：init.sql会删除你原来的camplus_db数据库。请注意备份数据。

### 1. 数据库名：camplus\_db

### 2.关联关系（待完善）：

（暂无）

### 3. 数据库结构（待完善）：

#### 3.1 users 用户表

| 字段名             | 类型             | 约束                                          | 默认值                        | 注释         |
| --------------- | -------------- | ------------------------------------------- | -------------------------- |------------|
| user_id         | BIGINT         | PRIMARY KEY, AUTO_INCREMENT                | -                          | 用户ID       |
| username        | VARCHAR(32)    | NOT NULL                                    | -                          | 用户名        |
| password_hash   | VARCHAR(255)   | NOT NULL                                    | -                          | 密码哈希值      |
| email           | VARCHAR(64)    | DEFAULT                                     | NULL                       | 邮箱         |
| phone           | VARCHAR(20)    | DEFAULT                                     | NULL                       | 手机号        |
| nickname        | VARCHAR(32)    | DEFAULT                                     | NULL                       | 昵称         |
| avatar_url      | VARCHAR(255)   | DEFAULT                                     | NULL                       | 头像URL      |
| role            | TINYINT        | NOT NULL                                    | 0                          | 角色，0普通用户，1管理员 |
| status          | TINYINT        | NOT NULL                                    | 1                          | 账号状态，0禁用，1正常 |
| last_login_time | DATETIME       | DEFAULT                                     | NULL                       | 最后登录时间     |
| login_error_count | INT          | NOT NULL                                    | 0                          | 登录错误次数     |
| lock_time       | DATETIME       | DEFAULT                                     | NULL                       | 账号锁定截止时间   |
| create_time     | DATETIME       | NOT NULL                                    | CURRENT_TIMESTAMP          | 创建时间       |
| update_time     | DATETIME       | NOT NULL                                    | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间       |


#### 3.2 faq\_items 固定问题表

| 字段名             | 类型           | 约束                           | 默认值                | 注释              |
| --------------- | ------------ | ---------------------------- | ------------------ | --------------- |
| faq\_id         | INT          | PRIMARY KEY, AUTO\_INCREMENT | -                  | 固定问题ID          |
| question\_count | INT          | -                            | 0                  | 提问次数            |
| like\_count     | INT          | -                            | 0                  | 点赞次数            |
| hot\_score      | INT          | -                            | 0                  | 热度分             |
| display\_status | TINYINT      | -                            | 0                  | 展示状态（0为下架，1为展示） |
| create\_time    | DATETIME     | -                            | CURRENT\_TIMESTAMP | 创建时间            |
| question        | VARCHAR(500) | NOT NULL                     | -                  | 问题              |
| answer          | TEXT         | -                            | -                  | 答案              |

#### 3.3 knowledge\_docs 知识资料表

| 字段名     | 类型           | 约束                           | 默认值                        | 注释              |
| ------- | ------------ | ---------------------------- | -------------------------- | --------------- |
| doc\_id | INT          | PRIMARY KEY, AUTO\_INCREMENT | -                          | 资料ID            |
| doc_name | VARCHAR(255) | -                            | NULL                       | 文档名称           |
| doc_path | VARCHAR(500) | -                            | NULL                       | 文档路径           |
| doc_content | TEXT      | -                            | NULL                       | 完整文档内容         |
| doc_type | VARCHAR(50)  | -                            | NULL                       | 文档类型（txt/pdf/docx等） |
| chunk_count | INT      | -                            | 0                          | 切片数量           |
| created_at | DATETIME    | -                            | CURRENT_TIMESTAMP          | 创建时间           |
| updated_at | DATETIME    | -                            | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间           |

#### 3.4 faq\_vector\_store FAQ向量存储表

| 字段名              | 类型     | 约束                           | 默认值                        | 注释                 |
| ---------------- | ------ | ---------------------------- | -------------------------- | ------------------ |
| id               | BIGINT | PRIMARY KEY, AUTO\_INCREMENT | -                          | 主键ID               |
| faq_id           | INT    | NOT NULL, FOREIGN KEY        | -                          | 关联的faq_items表的faq_id |
| question         | TEXT   | NOT NULL                     | -                          | 原始问题文本            |
| answer           | TEXT   | -                            | NULL                       | 原始答案文本            |
| question_embedding | BLOB  | NOT NULL                     | -                          | 问题文本的向量表示         |
| answer_embedding   | BLOB  | -                            | NULL                       | 答案文本的向量表示         |
| combined_embedding | BLOB  | NOT NULL                     | -                          | 问题+答案合并后的向量表示    |
| created_at       | DATETIME | -                            | CURRENT_TIMESTAMP          | 创建时间             |
| updated_at       | DATETIME | -                            | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间             |

#### 3.5 knowledge\_vector\_store 知识库向量存储表

| 字段名         | 类型     | 约束                           | 默认值                        | 注释                |
| ----------- | ------ | ---------------------------- | -------------------------- | ----------------- |
| id          | BIGINT | PRIMARY KEY, AUTO\_INCREMENT | -                          | 主键ID              |
| doc_id      | INT    | NOT NULL, FOREIGN KEY        | -                          | 关联的knowledge_docs表的doc_id |
| chunk_index | INT    | NOT NULL                     | -                          | 文档切片索引           |
| chunk_content | TEXT  | NOT NULL                     | -                          | 切片后的文本内容        |
| chunk_embedding | BLOB | NOT NULL                     | -                          | 文本切片的向量表示       |
| chunk_metadata | JSON | -                            | NULL                       | 切片元数据（文档名、页码等） |
| created_at  | DATETIME | -                            | CURRENT_TIMESTAMP          | 创建时间            |
| updated_at  | DATETIME | -                            | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间            |

#### 3.6 vector\_search\_temp 向量检索临时结果表

| 字段名             | 类型           | 约束                           | 默认值                        | 注释           |
| --------------- | ------------ | ---------------------------- | -------------------------- | ------------ |
| id              | BIGINT       | PRIMARY KEY, AUTO\_INCREMENT | -                          | 主键ID         |
| search_id       | VARCHAR(64)  | NOT NULL                     | -                          | 检索请求唯一标识     |
| table_name      | VARCHAR(100) | NOT NULL                     | -                          | 检索的表名        |
| record_id       | BIGINT       | NOT NULL                     | -                          | 匹配的记录ID      |
| similarity_score | DECIMAL(10,6) | NOT NULL                    | -                          | 相似度分数（0-1）   |
| content_summary | TEXT         | -                            | NULL                       | 匹配内容摘要      |
| created_at      | DATETIME     | -                            | CURRENT_TIMESTAMP          | 创建时间         |

#### 3.7 qa\_logs 问答记录表

| 字段名         | 类型  | 约束                           | 注释     |
| ----------- | --- | ---------------------------- | ------ |
| qa\_log\_id | INT | PRIMARY KEY, AUTO\_INCREMENT | 问答记录ID |

#### 3.8 user\_contributions 用户贡献表

| 字段名              | 类型  | 约束                           | 注释   |
| ---------------- | --- | ---------------------------- | ---- |
| contribution\_id | INT | PRIMARY KEY, AUTO\_INCREMENT | 贡献ID |

#### 3.9 admin\_operation\_logs 管理员操作日志表

| 字段名         | 类型  | 约束                           | 注释     |
| ----------- | --- | ---------------------------- | ------ |
| op\_log\_id | INT | PRIMARY KEY, AUTO\_INCREMENT | 操作日志ID |
