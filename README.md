# 健身健康管理系统（Fitness System）

一个前后端分离的健身健康管理平台：用户可以记录健康数据、制定运动与饮食计划、使用 AI 生成个性化训练方案并在线问答，同时提供数据统计报表与社区交流功能。

## 技术栈

| 模块 | 技术 | 目录 |
|---|---|---|
| 后端 | Spring Boot + MyBatis-Plus + JWT | `backend/` |
| 前端 | Vue 3 + Vite + Element Plus + Pinia + ECharts | `frontend/` |
| AI 服务 | Python + FastAPI + 本地 BGE 向量检索（RAG）+ LLM | `ai/` |

## 功能概览

- 用户注册登录、JWT 鉴权
- 健康记录（身高、体重、体脂等）与运动记录
- 饮食记录与食物库
- AI 训练计划生成、AI 健康问答
- 数据统计与可视化报表
- 社区发帖、评论、多图上传
- 后台管理

## 目录结构

```
fitness-system/
├── backend/            # Spring Boot 后端（端口 8080，context-path /api）
│   ├── src/main/java/com/fitness/fitness/
│   │   ├── config/      # WebClient、JWT、跨域等配置
│   │   ├── controller/  # 接口层
│   │   ├── entity/      # 实体
│   │   ├── mapper/      # MyBatis-Plus Mapper
│   │   ├── serviceimpl/ # 业务实现
│   │   └── util/        # 工具类
│   ├── src/main/resources/application.yml
│   └── sql/             # 数据库脚本（按 step1 ~ step8 顺序执行）
├── frontend/           # Vue 3 前端（开发端口 5173）
│   └── src/
└── ai/                 # Python AI 服务（端口 8000）
    ├── app/            # FastAPI 入口、RAG、向量库、知识库
    ├── data/           # 向量索引
    └── models/         # 本地向量模型（不入库，见下方说明）
```

## 环境要求

- JDK 8 或以上（按 `backend/pom.xml` 中的版本）
- Maven（或直接使用自带的 `mvnw`）
- MySQL 5.7 / 8.0
- Node.js 16+
- Python 3.9+

## 快速启动

### 1. 初始化数据库

新建数据库 `fitness`，然后按顺序执行 `backend/sql/` 下的脚本：

```
step1_schema.sql
step2_schema.sql
step3_ai_plan.sql
step4_admin.sql
step5_food.sql
step6_comment.sql
step7_report.sql
step8_community_multi_img.sql
```

### 2. 启动后端

修改 `backend/src/main/resources/application.yml` 中的数据源账号密码，然后在 `backend/` 目录执行：

```bash
mvn spring-boot:run
```

启动后接口地址为 `http://127.0.0.1:8080/api`。

### 3. 启动 AI 服务

```bash
cd ai
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
copy .env.example .env      # 填入自己的 LLM API Key
python -m uvicorn app.main:app --host 127.0.0.1 --port 8000
```

> 向量模型 `bge-small-zh-v1.5` 体积较大，未纳入版本库。请从 HuggingFace 下载后放到 `ai/models/bge-small-zh-v1.5/`。
> 缺少模型时，AI 计划与问答接口会返回「AI 服务暂不可用」。

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

浏览器访问 `http://127.0.0.1:5173`。

## 配置说明

| 配置项 | 位置 | 说明 |
|---|---|---|
| 数据库连接 | `backend/src/main/resources/application.yml` | 本地开发默认 `root/123456`，部署时请自行修改 |
| JWT 密钥与有效期 | `backend/src/main/resources/application.yml` | `jwt.secret` / `jwt.expire` |
| AI 服务地址 | `backend/src/main/resources/application.yml` | `ai.server.url`，默认 `http://127.0.0.1:8000` |
| LLM 密钥 | `ai/.env` | 由 `ai/.env.example` 复制而来，**不要提交到仓库** |

## 说明

本项目为毕业设计作品，仅供学习交流使用。
