# -*- coding: utf-8 -*-
"""FitnessAI RAG 服务。

对外接口（与 Java 后端 AiHttpUtil 保持一致）：
- GET  /health            健康检查（含向量索引与大模型配置状态）
- POST /rag/chat          健康问答   body: {question, health}
- POST /rag/plan/diet     饮食方案   body: {health, planType, remark}
- POST /rag/plan/sport    运动方案   body: {health, planType, remark}
返回统一包含 answer(string) 与 sources(list)，Java 侧直接读取这两个字段。
"""
import os
import sys

# 兼容直接运行本文件（python app/main.py）：补齐包上下文，使相对导入生效
if __name__ == "__main__" and not __package__:
    sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
    __package__ = "app"

from typing import Any, Dict, Optional

import uvicorn
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

from . import llm, vector_store
from .rag import answer_question, build_diet_plan, build_sport_plan

app = FastAPI(title="FitnessAI RAG Service", version="0.2.0", description="本地健康知识库向量检索 + 在线大模型问答与方案生成")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


class ChatRequest(BaseModel):
    question: str
    health: Optional[Dict[str, Any]] = None


class PlanRequest(BaseModel):
    health: Dict[str, Any]
    planType: Optional[str] = None
    remark: Optional[str] = ""


@app.get("/health")
def health() -> Dict[str, Any]:
    return {
        "status": "ok",
        "service": "fitness-ai",
        "version": "0.2.0",
        "llm_configured": llm.is_configured(),
        "llm_model": llm.model_name(),
        "vector": vector_store.status(),
    }


@app.post("/rag/chat")
def rag_chat(req: ChatRequest) -> Dict[str, Any]:
    return answer_question(req.question, req.health or {})


@app.post("/rag/plan/diet")
def rag_plan_diet(req: PlanRequest) -> Dict[str, Any]:
    return build_diet_plan(req.health or {}, req.remark or "")


@app.post("/rag/plan/sport")
def rag_plan_sport(req: PlanRequest) -> Dict[str, Any]:
    return build_sport_plan(req.health or {}, req.remark or "")


if __name__ == "__main__":
    uvicorn.run(app, host="127.0.0.1", port=8000, reload=False)
