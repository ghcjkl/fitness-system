# -*- coding: utf-8 -*-
"""OpenAI 兼容大模型客户端（默认 DeepSeek）。

配置全部走环境变量（也支持项目根目录的 .env 文件，环境变量优先）：
- LLM_API_KEY   必填，未配置时 chat() 直接返回 None，由上层使用模板生成
- LLM_BASE_URL  默认 https://api.deepseek.com/v1
- LLM_MODEL     默认 deepseek-chat
- LLM_TIMEOUT   默认 45 秒
"""
import logging
import os
from typing import Any, Dict, List, Optional

import requests

logger = logging.getLogger("fitness_ai.llm")

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

DEFAULT_BASE_URL = "https://api.deepseek.com/v1"
DEFAULT_MODEL = "deepseek-chat"


def _load_env_file() -> None:
    """读取项目根目录 .env，便于本地保存 Key；已存在的环境变量不会被覆盖。"""
    path = os.path.join(BASE_DIR, ".env")
    if not os.path.isfile(path):
        return
    try:
        with open(path, "r", encoding="utf-8-sig") as fp:
            for line in fp:
                line = line.strip()
                if not line or line.startswith("#") or "=" not in line:
                    continue
                key, value = line.split("=", 1)
                key = key.strip()
                value = value.strip().strip('"').strip("'")
                if key and key not in os.environ:
                    os.environ[key] = value
    except Exception as exc:  # noqa: BLE001 配置读取失败不影响服务启动
        logger.warning("读取 .env 失败: %s", exc)


_load_env_file()


def api_key() -> str:
    return (os.getenv("LLM_API_KEY") or "").strip()


def is_configured() -> bool:
    return bool(api_key())


def model_name() -> str:
    return (os.getenv("LLM_MODEL") or DEFAULT_MODEL).strip()


def base_url() -> str:
    return (os.getenv("LLM_BASE_URL") or DEFAULT_BASE_URL).strip().rstrip("/")


def chat(messages: List[Dict[str, str]], temperature: float = 0.5,
         max_tokens: int = 900) -> Optional[str]:
    """调用对话接口；任何异常都返回 None，保证问答链路可用。"""
    key = api_key()
    if not key:
        return None
    timeout = float(os.getenv("LLM_TIMEOUT") or 45)
    payload: Dict[str, Any] = {
        "model": model_name(),
        "messages": messages,
        "temperature": temperature,
        "max_tokens": max_tokens,
        "stream": False,
    }
    try:
        resp = requests.post(
            base_url() + "/chat/completions",
            headers={
                "Authorization": "Bearer " + key,
                "Content-Type": "application/json",
            },
            json=payload,
            timeout=(5, timeout),
        )
        if resp.status_code != 200:
            logger.warning("大模型返回异常状态 %s: %s", resp.status_code, resp.text[:300])
            return None
        data = resp.json()
        content = data["choices"][0]["message"]["content"]
        content = (content or "").strip()
        return content or None
    except Exception as exc:  # noqa: BLE001 网络/解析失败统一降级
        logger.warning("大模型调用失败，降级为模板生成: %s", exc)
        return None
