# -*- coding: utf-8 -*-
"""向量检索：知识分块 + 本地 bge 编码 + numpy 余弦相似度。

设计要点：
- 知识库来自 knowledge.KNOWLEDGE，按句号/分号切块并拼接，块与块之间保留少量重叠；
- 编码使用本地 BAAI/bge-small-zh-v1.5，向量归一化后用点积即余弦相似度；
- 索引落盘到 data/vector_index.npy + data/vector_index.json，知识库或模型变化时自动重建；
- 模型不可用时 search() 返回 None，由上层 rag.py 降级为关键词检索。
"""
import hashlib
import json
import logging
import os
import re
import threading
from typing import Any, Dict, List, Optional

from .knowledge import KNOWLEDGE

logger = logging.getLogger("fitness_ai.vector")

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
MODEL_DIR = os.getenv("EMBED_MODEL_PATH") or os.path.join(BASE_DIR, "models", "bge-small-zh-v1.5")
INDEX_DIR = os.getenv("RAG_INDEX_DIR") or os.path.join(BASE_DIR, "data")
CHUNK_SIZE = int(os.getenv("RAG_CHUNK_SIZE") or 180)
CHUNK_OVERLAP = int(os.getenv("RAG_CHUNK_OVERLAP") or 20)
MIN_SCORE = float(os.getenv("RAG_MIN_SCORE") or 0.30)

_SENTENCE_RE = re.compile(r"(?<=[。；;！!？?])")

_model = None
_model_failed = False
_model_lock = threading.Lock()
_index_lock = threading.Lock()
_matrix = None
_chunks: List[Dict[str, Any]] = []
_index_signature = ""


def _split_chunks(text: str) -> List[str]:
    """按句子切块，块长不超过 CHUNK_SIZE，相邻块保留 CHUNK_OVERLAP 字重叠。"""
    sentences = [s for s in _SENTENCE_RE.split(text) if s and s.strip()]
    if not sentences:
        return []
    chunks: List[str] = []
    current = ""
    for sentence in sentences:
        if len(current) + len(sentence) <= CHUNK_SIZE:
            current += sentence
            continue
        if current:
            chunks.append(current)
            tail = current[-CHUNK_OVERLAP:] if CHUNK_OVERLAP > 0 else ""
            current = tail + sentence
        else:
            current = sentence
        # 单句本身超长时继续按定长切分，避免生成过大块
        while len(current) > CHUNK_SIZE:
            chunks.append(current[:CHUNK_SIZE])
            current = current[CHUNK_SIZE - CHUNK_OVERLAP:]
    if current.strip():
        chunks.append(current)
    return chunks


def _build_chunks() -> List[Dict[str, Any]]:
    chunks: List[Dict[str, Any]] = []
    for entry in KNOWLEDGE:
        pieces = _split_chunks(entry["content"]) or [entry["content"]]
        for i, piece in enumerate(pieces):
            chunks.append({
                "id": entry["id"],
                "title": entry["title"],
                "chunk_id": "%s#%d" % (entry["id"], i),
                # 标题参与编码，提升“减脂怎么吃”这类问法与正文的匹配度
                "text": piece.strip(),
                "embed_text": "%s：%s" % (entry["title"], piece.strip()),
            })
    return chunks


def _signature(chunks: List[Dict[str, Any]]) -> str:
    raw = json.dumps(chunks, ensure_ascii=False, sort_keys=True)
    return hashlib.md5(raw.encode("utf-8")).hexdigest()


def _load_model():
    """懒加载本地模型；加载失败只记录一次日志并永久返回 None。"""
    global _model, _model_failed
    if _model is not None:
        return _model
    if _model_failed:
        return None
    with _model_lock:
        if _model is not None:
            return _model
        if _model_failed:
            return None
        if not os.path.isdir(MODEL_DIR):
            logger.warning("向量模型目录不存在，降级为关键词检索: %s", MODEL_DIR)
            _model_failed = True
            return None
        try:
            from sentence_transformers import SentenceTransformer

            _model = SentenceTransformer(MODEL_DIR, device=os.getenv("EMBED_DEVICE") or "cpu")
            logger.info("向量模型加载完成: %s", MODEL_DIR)
        except Exception as exc:  # noqa: BLE001 依赖缺失时整体降级
            logger.warning("向量模型加载失败，降级为关键词检索: %s", exc)
            _model_failed = True
            _model = None
    return _model


def _load_index_files():
    """尝试读取磁盘索引，返回 (matrix, chunks) 或 (None, None)。"""
    npz_path = os.path.join(INDEX_DIR, "vector_index.npy")
    meta_path = os.path.join(INDEX_DIR, "vector_index.json")
    if not (os.path.isfile(npz_path) and os.path.isfile(meta_path)):
        return None, None
    try:
        import numpy as np

        with open(meta_path, "r", encoding="utf-8") as fp:
            meta = json.load(fp)
        matrix = np.load(npz_path)
        if matrix.shape[0] != len(meta.get("chunks", [])):
            return None, None
        return matrix, meta["chunks"]
    except Exception as exc:  # noqa: BLE001 索引损坏时重建
        logger.warning("向量索引读取失败，将重建: %s", exc)
        return None, None


def _save_index(matrix, chunks: List[Dict[str, Any]], signature: str) -> None:
    try:
        import numpy as np

        os.makedirs(INDEX_DIR, exist_ok=True)
        np.save(os.path.join(INDEX_DIR, "vector_index.npy"), matrix)
        with open(os.path.join(INDEX_DIR, "vector_index.json"), "w", encoding="utf-8") as fp:
            json.dump({
                "model": MODEL_DIR,
                "signature": signature,
                "chunks": chunks,
            }, fp, ensure_ascii=False)
    except Exception as exc:  # noqa: BLE001 落盘失败不影响在线检索
        logger.warning("向量索引写入失败（不影响本次检索）: %s", exc)


def _ensure_index(model):
    """确保内存索引可用：优先复用磁盘索引，签名不一致时重新编码。"""
    global _matrix, _chunks, _index_signature
    with _index_lock:
        chunks = _build_chunks()
        signature = _signature(chunks)
        if _matrix is not None and _index_signature == signature:
            return _matrix, _chunks
        matrix, cached_chunks = _load_index_files()
        if matrix is not None:
            cached_signature = _signature(cached_chunks)
            if cached_signature == signature:
                _matrix, _chunks, _index_signature = matrix, cached_chunks, signature
                return _matrix, _chunks
        vectors = model.encode([c["embed_text"] for c in chunks], normalize_embeddings=True)
        _matrix, _chunks, _index_signature = vectors, chunks, signature
        _save_index(vectors, chunks, signature)
        logger.info("向量索引重建完成，共 %d 个知识块", len(chunks))
        return _matrix, _chunks


def search(query: str, top_k: int = 4) -> Optional[List[Dict[str, Any]]]:
    """向量检索；模型不可用或无有效命中时返回 None。"""
    query = (query or "").strip()
    if not query:
        return None
    model = _load_model()
    if model is None:
        return None
    try:
        import numpy as np

        matrix, chunks = _ensure_index(model)
        if matrix is None or len(chunks) == 0:
            return None
        vector = model.encode([query], normalize_embeddings=True)[0]
        scores = matrix @ vector
        order = np.argsort(-scores)[:max(1, top_k)]
        hits = []
        for i in order:
            score = float(scores[i])
            if score < MIN_SCORE:
                continue
            chunk = dict(chunks[int(i)])
            chunk["score"] = round(score, 4)
            hits.append(chunk)
        return hits or None
    except Exception as exc:  # noqa: BLE001 检索失败交给上层降级
        logger.warning("向量检索失败，降级为关键词检索: %s", exc)
        return None


def status() -> Dict[str, Any]:
    """供 /health 展示，不触发模型加载。"""
    return {
        "embedding_model": os.path.basename(MODEL_DIR),
        "model_available": os.path.isdir(MODEL_DIR),
        "index_ready": os.path.isfile(os.path.join(INDEX_DIR, "vector_index.npy")),
        "loaded": _matrix is not None,
    }
