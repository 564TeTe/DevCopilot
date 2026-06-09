# DevCopilot 智能协作平台

DevCopilot 是一个面向研发团队的 AI 智能协作后端平台，覆盖文档知识库、代码索引、AI 会话、PR 分析和异步任务中心等场景。

## 技术栈

- Java 17, Spring Boot 3
- MySQL: 业务数据
- PostgreSQL + pgvector: 文档与代码向量检索
- Redis: 会话缓存、任务状态缓存、SSE 状态辅助
- RabbitMQ: 文档解析、向量化、代码索引、PR 分析等异步任务
- SSE: AI 流式回答与任务进度推送
- Flyway: MySQL 表结构迁移
- Springdoc OpenAPI: 接口文档

## 模块结构

```text
devcopilot-common          通用响应、异常、JWT 工具
devcopilot-domain          领域模型、枚举、Repository
devcopilot-application     应用服务、任务编排、AI/RAG 流程
devcopilot-infrastructure  Redis、RabbitMQ、pgvector、文件存储、AI 客户端实现
devcopilot-api             REST API 与 SSE 接口
devcopilot-bootstrap       Spring Boot 启动模块与配置
```

## 本地启动

```bash
docker compose up -d
mvn -pl devcopilot-bootstrap -am spring-boot:run
```

MySQL 为避免和本机服务冲突，宿主机端口映射为 `13306`，容器内仍是 `3306`。PostgreSQL/pgvector 宿主机端口映射为 `15432`，容器内仍是 `5432`。

默认接口文档地址：

```text
http://localhost:18080/swagger-ui/index.html
```

前端工作台：

```bash
cd devcopilot-web
npm install
npm run dev
```

```text
http://localhost:15173
```

RabbitMQ 管理台：

```text
http://localhost:15674
账号/密码: devcopilot / devcopilot
```

RabbitMQ AMQP 宿主机端口为 `15673`，管理台宿主机端口为 `15674`。

## 核心流程

1. 创建项目空间。
2. 创建知识库并上传文档。
3. 文档上传后自动创建异步任务，通过 RabbitMQ 解析文本、切片并写入 MySQL 与 pgvector。
4. 创建 AI 会话，选择知识库问答模式。
5. 用户问题会先做向量检索，召回上下文后通过 SSE 流式输出回答。
6. 提交代码仓库或 PR diff 后，任务中心异步执行代码索引与 PR 分析。

## 主要接口

```text
POST /api/auth/register
POST /api/auth/login

POST /api/projects
GET  /api/projects

POST /api/knowledge-bases
GET  /api/knowledge-bases

POST /api/documents/upload
GET  /api/documents/{id}

POST /api/chat/sessions
POST /api/chat/sessions/{id}/messages
POST /api/chat/sessions/{id}/messages/stream

POST /api/code-repositories
POST /api/code-repositories/{id}/index

POST /api/pr-analysis
GET  /api/pr-analysis/{id}

GET  /api/tasks/{id}
GET  /api/tasks/{id}/stream
```
