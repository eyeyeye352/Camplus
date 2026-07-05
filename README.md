# Camplus

Camplus 是一个基于 Spring Boot 的智能校园问答系统，集成了 AI 向量检索和大语言模型，提供高效的知识问答服务。

## 目录

- [项目概述](#项目概述)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [功能模块](#功能模块)
- [环境要求](#环境要求)
- [快速开始](#快速开始)
- [数据库配置](#数据库配置)
- [AI模型配置](#ai模型配置)
- [启动方式](#启动方式)
- [API接口](#api接口)
- [开发规范](#开发规范)
- [常见问题](#常见问题)

## 项目概述

Camplus 旨在为校园用户提供智能问答服务，通过以下核心技术实现：

1. **文本向量化**：使用 BGE-M3 ONNX 模型将文本转换为向量表示
2. **向量检索**：在向量数据库中检索相似内容
3. **RAG问答**：结合检索上下文和大语言模型生成精准答案

## 技术栈

| 分类 | 技术 | 版本 |
|------|------|------|
| 框架 | Spring Boot | 3.3.0 |
| 语言 | Java | 17 |
| ORM | MyBatis | 3.5.14 |
| 数据库 | MySQL | 8.0+ |
| 向量化模型 | BGE-M3 ONNX | - |
| 大语言模型 | Qwen2.5:7b | Q4_K_M |
| LLM框架 | LangChain4j | 0.32.0 |
| 向量检索 | ONNX Runtime | 1.17.3 |

## 项目结构

```
Camplus/
├── src/main/java/com/camplus/
│   ├── admin/          # 管理员模块
│   │   ├── controller/ # 控制器
│   │   ├── service/    # 服务层
│   │   ├── runner/     # 数据导入运行器
│   │   └── pojo/       # DTO对象
│   ├── common/         # 公共模块
│   │   ├── controller/ # 日志控制器
│   │   └── Result.java # 统一响应封装
│   ├── config/         # 配置模块
│   │   ├── DatabaseConfigService.java    # 数据库配置服务
│   │   ├── DatabaseConfigController.java # 数据库配置接口
│   │   ├── DynamicDataSourceConfig.java  # 动态数据源配置
│   │   └── DelegatingDataSource.java     # 代理数据源
│   ├── contribution/   # 用户贡献模块
│   │   ├── controller/
│   │   ├── service/
│   │   ├── mappers/
│   │   └── pojo/
│   ├── faq/            # FAQ问答模块
│   │   ├── controller/
│   │   ├── service/
│   │   ├── mappers/
│   │   ├── scheduler/  # 定时任务（热度更新）
│   │   └── pojo/
│   ├── login/          # 登录注册模块
│   │   ├── controller/
│   │   ├── service/
│   │   ├── mappers/
│   │   ├── entity/     # 实体类
│   │   └── util/       # 工具类（MD5加密）
│   ├── qa/             # 问答核心模块
│   │   ├── QaController.java        # 问答控制器
│   │   ├── CampusAssistant.java     # 校园助手核心逻辑
│   │   └── RagConfig.java           # RAG配置
│   ├── vector/         # AI向量服务模块
│   │   ├── controller/
│   │   ├── service/
│   │   ├── mappers/
│   │   └── pojo/
│   ├── CamplusApplication.java      # 启动类
│   └── WebConfig.java               # Web配置
├── src/main/resources/
│   ├── application.yml              # 应用配置
│   ├── mybatis/mappers/             # MyBatis映射文件
│   └── docs/                        # 文档资源
├── src/main/webapp/                 # 前端静态资源
│   ├── home/        # 首页
│   ├── login/       # 登录注册页
│   ├── qa/          # 问答页
│   ├── userInfo/    # 用户信息页
│   ├── admin/       # 管理员页
│   └── assets/      # 静态资源（字体、音乐）
├── sql/             # 数据库脚本
├── 使用说明/         # 使用说明文档
├── RawData/         # 原始数据（自动导入）
└── pom.xml          # Maven配置
```

## 功能模块

### 1. 用户管理

**登录逻辑** ([UserController.java](src/main/java/com/camplus/login/controller/UserController.java), [UserServiceImpl.java](src/main/java/com/camplus/login/service/impl/UserServiceImpl.java))

```
用户输入账号密码 → 后端按用户名查询 → 无结果则按邮箱查询
    → 密码MD5加密比对 → 验证通过返回用户信息
```

- 支持用户名/邮箱两种登录方式
- 密码使用MD5加密存储
- 登录成功返回完整用户信息（userId、username、email、role等）

**注册逻辑**

```
用户输入邮箱和密码 → 发送验证码 → 验证验证码
    → 检查邮箱是否已注册 → 密码MD5加密 → 创建用户
    → 将邮箱设为默认用户名 → 返回用户信息
```

- 仅支持邮箱注册，需验证码验证
- 注册成功后自动将邮箱作为用户名

**个人信息修改**

| 修改项 | 校验逻辑 |
|--------|----------|
| 用户名 | 检查是否被其他用户占用 |
| 邮箱 | 检查是否被其他用户占用 |
| 密码 | 验证旧密码正确后更新新密码 |

### 2. FAQ问答系统

**热点问题获取** ([FaqController.java](src/main/java/com/camplus/faq/controller/FaqController.java))

- 按热度分（hot_score）降序排列
- 默认返回前10条，可通过limit参数调整

**热度计算流程** ([FaqServiceImpl.java](src/main/java/com/camplus/faq/service/serviceImpl/FaqServiceImpl.java), [HotUpdateScheduler.java](src/main/java/com/camplus/faq/scheduler/HotUpdateScheduler.java))

```
每日凌晨2点定时任务执行：
1. updateHotScores(): hot_score += question_count × 10  (累加今日热度)
2. resetDailyStats(): question_count = 0  (重置今日查询次数)
   hot_score = hot_score / 2  (热度减半，衰减历史热度)
```

**FAQ点击记录**

```
用户点击FAQ → recordClick(faqId) → question_count + 1
```

### 3. 问答核心模块

**完整问答流程** ([QaController.java](src/main/java/com/camplus/qa/QaController.java))

```
用户提问 → checkFaqHit(question) 检查FAQ命中
    ├── 命中 → updateFaqStats(faqId) 更新统计 → 返回FAQ答案
    └── 未命中 → campusAssistant.answer(question) 调用大模型
        ├── 答案有效 → saveToFaq(question, answer) 自动学习
        └── 答案无效（"暂无相关信息"）→ 不保存
```

**FAQ命中检查**

- 使用向量检索在faq_vector_store表中匹配
- 相似度阈值：0.55
- 返回最相似的1条结果

**自动学习机制**

当FAQ未命中且大模型生成有效答案时：
1. 将问答对插入faq_items表（source="auto"）
2. 对问答对进行向量化
3. 将向量存入faq_vector_store表

**大语言模型调用** ([CampusAssistant.java](src/main/java/com/camplus/qa/CampusAssistant.java))

```java
@SystemMessage({
    "你是一个幽默、专业的校园生活助手。",
    "请严格根据检索到的校园规章制度片段来回答问题。",
    "如果提供的片段中找不到答案，请直接回复'暂无相关方面的信息'，不要编造。",
    "如果可以回答，请用自然、友好的语言详细解答。"
})
```

### 4. AI向量服务

**文本向量化** ([VectorServiceImpl.java](src/main/java/com/camplus/vector/service/VectorServiceImpl.java))

```
输入文本 → BgeM3OnnxService.encode(text)
    → 返回 denseVector(稠密向量) + sparseVector(稀疏向量)
```

- 使用BGE-M3 ONNX模型
- 模型路径：`D:\models\bge-m3-onnx\`（硬编码）

**向量检索**

```
查询文本 → 向量化 → 加载目标表所有向量
    → 计算混合相似度（稠密×0.6 + 稀疏×0.4）
    → 按相似度排序 → 返回前K条（默认1条）
```

- 支持两张表：`faq_vector_store`、`knowledge_vector_store`
- 相似度阈值：0.6（默认）
- 混合检索：稠密向量（语义相似度）+ 稀疏向量（关键词匹配）

**相似度计算**

| 向量类型 | 计算方法 | 权重 |
|----------|----------|------|
| 稠密向量 | 余弦相似度 | 0.6 |
| 稀疏向量 | 词法相似度 | 0.4 |

### 5. 文档处理

**多格式支持** ([KnowledgeExtractServiceImpl.java](src/main/java/com/camplus/admin/service/impl/KnowledgeExtractServiceImpl.java))

| 文件类型 | 处理方式 | 解析库 |
|----------|----------|--------|
| FAQ.txt | 解析Q:/A:格式问答对 | 正则表达式 |
| .txt | 直接读取文本 | Java NIO |
| .pdf | 提取文本内容 | Apache PDFBox |
| .docx | 提取文本内容 | Apache POI |
| .doc | 提取文本内容 | Apache POI |
| .csv | 解析问答对（支持转义） | 自定义解析器 |

**FAQ.txt格式**

```
Q: 学校饭堂有几个？
A: 学校饭堂有三个，一号饭堂，二号饭堂，三号饭堂

Q: 选课流程是什么？
A: 选课流程如下...
```

### 6. 数据导入

**自动导入触发条件** ([DataImportRunner.java](src/main/java/com/camplus/admin/runner/DataImportRunner.java))

```
同时满足以下条件才会触发：
1. faq_items表为空
2. knowledge_docs表为空
3. RawData目录存在有效文件（非.gitkeep、非.bat）
4. BGE-M3模型已初始化
```

**两种运行模式**

| 模式 | 触发方式 | 行为 |
|------|----------|------|
| 正常模式 | 应用启动时 | 检测条件满足则自动导入，继续运行 |
| 导入模式 | `--import-only`参数 | 执行导入后立即退出（建库脚本调用） |

**导入流程**

```
RawData目录 → 遍历文件 → 提取文本 → 向量化 → 存入数据库
    ├── FAQ问答对 → faq_items + faq_vector_store
    └── 文档 → knowledge_docs + knowledge_vector_store（分块存储）
```

### 7. 用户贡献

**贡献审核流程** ([UserContributionServiceImpl.java](src/main/java/com/camplus/admin/service/impl/UserContributionServiceImpl.java))

```
用户提交贡献 → 状态为"待审核"(0)
    → 管理员审核通过(1) → saveToFaq()保存到FAQ → 向量化存储
    → 管理员审核拒绝(2) → 更新审核意见
```

- 贡献类型：0=新增问题，1=答案纠错
- 审核状态：0=待审核，1=通过，2=拒绝

### 8. 数据库配置

**动态配置机制** ([DatabaseConfigService.java](src/main/java/com/camplus/config/DatabaseConfigService.java))

```
应用启动 → 尝试默认连接(Camplus_sql/123456)
    ├── 成功 → 正常运行
    └── 失败 → 应用仍启动，但访问任何页面重定向到/db-config.html
```

**配置页面功能**

- 测试连接：验证用户名密码是否正确
- 保存配置：更新数据库连接并刷新数据源
- 配置成功后自动跳转首页

**数据源代理** ([DelegatingDataSource.java](src/main/java/com/camplus/config/DelegatingDataSource.java))

- 延迟获取真实连接
- 未配置时抛出明确错误信息
- 配置成功后切换到真实数据源

### 9. 管理员功能

- **用户管理**：查看和管理用户列表
- **贡献审核**：审核用户提交的贡献，通过后自动保存到FAQ
- **知识导入**：手动上传文档并导入知识库

## 环境要求

### 硬件要求
- 至少 8GB 显存（用于运行 Qwen2.5:7b 模型）

### 软件要求
| 软件 | 版本 | 说明 |
|------|------|------|
| JDK | 17 | Java开发环境 |
| Maven | 3.9+ | 项目构建工具 |
| MySQL | 8.0+ | 数据库 |
| Ollama | 最新 | 大语言模型运行时 |

## 快速开始

### 1. 克隆项目

```bash
git clone <仓库地址>
cd Camplus
```

### 2. 配置MySQL

确保MySQL服务已启动，默认配置：

- 数据库名：`camplus_db`
- 用户名：`Camplus_sql`（默认），可在运行时配置
- 密码：`123456`（默认），可在运行时配置

### 3. 配置AI模型

#### 配置Ollama

1. 下载并安装 Ollama：[https://ollama.com/download](https://ollama.com/download)

2. 设置镜像源（加速下载）：
   ```bash
   set OLLAMA_REGISTRY=https://modelscope.cn/api/v1/models
   set OLLAMA_HF_MIRROR=https://hf-mirror.com
   ```

3. 重启 Ollama 服务后下载模型：
   ```bash
   ollama pull qwen2.5:7b
   ```

#### 配置BGE-M3 ONNX模型

1. 创建模型目录：
   ```bash
   mkdir -p D:\models\bge-m3-onnx
   ```

2. 下载以下7个文件到 `D:\models\bge-m3-onnx\`：
   - model.onnx
   - model.onnx.data
   - config.json
   - ort_config.json
   - sentencepiece.bpe.model
   - tokenizer.json
   - tokenizer_config.json

   下载地址：[https://hf-mirror.com/aapot/bge-m3-onnx/tree/main](https://hf-mirror.com/aapot/bge-m3-onnx/tree/main)

### 4. 初始化数据库

运行 `sql/一键建库.bat`，按提示输入MySQL用户名和密码：

```bash
cd sql
一键建库.bat
```

脚本会自动：
1. 创建数据库 `camplus_db`
2. 创建表结构
3. 初始化管理员账户
4. 编译项目并导入数据

### 5. 启动应用

运行根目录下的 `编译运行项目.bat`：

```bash
编译运行项目.bat
```

首次启动时，若默认数据库连接失败，系统会自动跳转到数据库配置页面 `/db-config.html`，输入正确的MySQL用户名和密码即可。

### 6. 访问应用

打开浏览器访问：[http://localhost:8080](http://localhost:8080)

## 数据库配置

### 默认账户

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | Administrator | 123456 |

### 数据库表结构

主要数据表：

| 表名 | 说明 |
|------|------|
| users | 用户表 |
| faq_items | FAQ问答表 |
| faq_vector_store | FAQ向量存储表 |
| knowledge_docs | 知识文档表 |
| knowledge_vector_store | 知识库向量存储表 |
| user_contributions | 用户贡献表 |
| verification_codes | 验证码表 |

详细表结构请参考 [sql/README.md](sql/README.md)

## AI模型配置

### 配置参数

| 参数 | 说明 | 默认值 |
|------|------|--------|
| bge-m3.model-path | BGE-M3模型路径 | D:/models/bge-m3-onnx/ |
| bge-m3.dense-weight | 稠密向量权重 | 0.6 |
| bge-m3.sparse-weight | 稀疏向量权重 | 0.4 |
| ollama.base-url | Ollama服务地址 | http://localhost:11434 |
| ollama.chat-model-name | 大语言模型名称 | qwen2.5:7b |
| ollama.temperature | 生成温度 | 0.7 |

### 问答流程

1. **向量化**：将用户问题转换为向量表示
2. **固定查询**：在FAQ向量表中检索相似问题（相似度阈值0.6）
3. **文档检索**：若FAQ未命中，在知识库中检索相关文档片段
4. **答案生成**：将问题和上下文交给大语言模型生成答案

## 启动方式

### 方式一：一键启动（推荐）

```bash
编译运行项目.bat
```

### 方式二：Maven启动

```bash
mvn spring-boot:run
```

### 方式三：Jar包启动

```bash
mvn clean package -DskipTests
java -jar target/Camplus.jar
```

### 数据导入模式

```bash
java -jar target/Camplus.jar --import-only --db-user <用户名> --db-pass <密码>
```

## API接口

### 用户接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /login | POST | 用户登录 |
| /register | POST | 用户注册 |
| /user/updateUsername | POST | 修改用户名 |
| /user/updateEmail | POST | 修改邮箱 |
| /user/updatePhone | POST | 修改手机号 |
| /user/updatePassword | POST | 修改密码 |

### FAQ接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/faq/list | GET | 获取FAQ列表 |
| /api/faq/hot | GET | 获取热点问题 |
| /api/faq/search | POST | 搜索FAQ |

### AI向量接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/vector/embed | POST | 文本向量化 |
| /api/vector/search | POST | 向量检索 |
| /api/vector/generate | POST | 答案生成 |
| /api/vector/rag | POST | 完整RAG问答 |
| /api/vector/health | GET | 健康检查 |

### 数据库配置接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/db/status | GET | 查询连接状态 |
| /api/db/validate | POST | 验证用户名密码 |
| /api/db/update | POST | 更新连接配置 |

### 贡献接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/contribution/submit | POST | 提交贡献 |
| /api/contribution/list | GET | 获取贡献列表 |
| /api/contribution/review | POST | 审核贡献 |

## 开发规范

### 代码风格

- Java代码遵循阿里巴巴Java开发手册
- 前端代码使用ES6+语法
- SQL语句使用MyBatis XML映射

### 分支管理

- main：主分支，稳定版本
- dev：开发分支，日常开发
- feature/xxx：功能分支，新功能开发

### SQL文件管理

- 新建 `update_版本号_修改内容.sql` 文件
- 版本号格式：`v1`, `v2`, `v3`...
- 修改关联字段需更新 `sql/README.md`

## 常见问题

### 1. 数据库连接失败

**原因**：默认数据库用户名/密码不正确

**解决**：
1. 确保MySQL服务已启动
2. 在 `/db-config.html` 页面输入正确的用户名和密码
3. 或通过命令行参数传入：`--db-user <用户名> --db-pass <密码>`

### 2. 模型加载失败

**BGE-M3模型加载失败**：
- 确保模型文件存在于 `D:\models\bge-m3-onnx\`
- 检查是否包含7个必要文件

**Qwen2.5模型加载失败**：
- 确保Ollama服务已启动：`ollama serve`
- 检查模型是否已下载：`ollama list`
- 确保模型名称正确：`qwen2.5:7b`

### 3. 数据导入失败

**原因**：
- BGE-M3模型未正确加载
- 数据库连接失败
- RawData目录无有效文件

**解决**：
- 确认模型已正确配置
- 确认数据库连接正常
- 在RawData目录放入FAQ.txt或其他文档文件

### 4. 应用启动失败

**检查步骤**：
1. 确认JDK版本为17
2. 确认MySQL服务运行正常
3. 检查端口8080是否被占用
4. 查看日志文件定位问题

## 许可证

本项目为软件综合实训作业，仅供学习使用。

---

**更新日期**：2026-07-05  
**项目版本**：1.0-SNAPSHOT