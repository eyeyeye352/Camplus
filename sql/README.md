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

| 字段名     | 类型  | 约束                           | 注释   |
| ------- | --- | ---------------------------- | ---- |
| doc\_id | INT | PRIMARY KEY, AUTO\_INCREMENT | 资料ID |

#### 3.4 qa\_logs 问答记录表

| 字段名         | 类型  | 约束                           | 注释     |
| ----------- | --- | ---------------------------- | ------ |
| qa\_log\_id | INT | PRIMARY KEY, AUTO\_INCREMENT | 问答记录ID |

#### 3.5 user\_contributions 用户贡献表

| 字段名              | 类型  | 约束                           | 注释   |
| ---------------- | --- | ---------------------------- | ---- |
| contribution\_id | INT | PRIMARY KEY, AUTO\_INCREMENT | 贡献ID |

#### 3.6 admin\_operation\_logs 管理员操作日志表

| 字段名         | 类型  | 约束                           | 注释     |
| ----------- | --- | ---------------------------- | ------ |
| op\_log\_id | INT | PRIMARY KEY, AUTO\_INCREMENT | 操作日志ID |
