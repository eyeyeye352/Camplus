# Camplus 数据库配置说明

### 1. 数据库名：camplus\_db

### 2.关联关系（待完善）：

（暂无）

### 3. 数据库结构（待完善）：

#### 3.1 users 用户表

| 字段名      | 类型          | 约束                           | 注释   |
| -------- | ----------- | ---------------------------- | ---- |
| user\_id | INT         | PRIMARY KEY, AUTO\_INCREMENT | 用户ID |
| username | VARCHAR(50) | NOT NULL                     | 用户名  |

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

