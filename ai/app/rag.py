# -*- coding: utf-8 -*-
"""RAG 引擎：向量检索（bge + numpy 余弦）+ 在线大模型生成。

/rag/chat 链路：问题向量化召回知识块 → 拼装健康档案与资料 → 调用 OpenAI 兼容模型；
模型未配置或调用失败时自动降级为本地模板生成，对外 JSON 结构保持不变。
build_diet_plan / build_sport_plan 沿用原有模板逻辑，不受影响。
"""
import re
from typing import Any, Dict, List, Optional

from . import llm, vector_store
from .knowledge import KNOWLEDGE

DAIRY = ("牛奶", "乳糖", "乳制品", "奶制品", "酸奶")
EGG = ("鸡蛋", "蛋类", "蛋清")
SEAFOOD = ("海鲜", "鱼", "虾", "蟹", "贝")
NUT = ("花生", "坚果", "杏仁", "核桃")
SPICY = ("辣",)
VEG = ("素食", "不吃肉", "全素", "纯素")

DEFAULT_IDS = ("kb-013", "kb-001", "kb-008")


def _num(health: Dict[str, Any], key: str) -> Optional[float]:
    try:
        val = health.get(key)
        if val is None or val == "":
            return None
        return float(val)
    except (TypeError, ValueError):
        return None


def _gender_text(health: Dict[str, Any]) -> str:
    g = health.get("gender")
    if g in (1, "1", "男", "M", "m"):
        return "男"
    if g in (2, "2", "女", "F", "f"):
        return "女"
    return ""


def _bmi(health: Dict[str, Any]) -> Optional[float]:
    h = _num(health, "height")
    w = _num(health, "weight")
    if not h or not w or h <= 0:
        return None
    return round(w / ((h / 100.0) ** 2), 1)


def _bmi_note(bmi: Optional[float]) -> str:
    if bmi is None:
        return ""
    if bmi < 18.5:
        return "偏瘦"
    if bmi < 24:
        return "正常"
    if bmi < 28:
        return "超重"
    return "肥胖"


def _profile_summary(health: Dict[str, Any]) -> str:
    if not health:
        return "你还没有填写健康档案，以下建议为通用版本。"
    parts = []
    if health.get("age"):
        parts.append("%s岁" % health.get("age"))
    if _gender_text(health):
        parts.append(_gender_text(health))
    h, w = _num(health, "height"), _num(health, "weight")
    if h:
        parts.append("身高%.0fcm" % h)
    if w:
        parts.append("体重%.1fkg" % w)
    bmi = _bmi(health)
    if bmi is not None:
        parts.append("BMI %.1f（%s）" % (bmi, _bmi_note(bmi)))
    if health.get("bodyFat"):
        parts.append("体脂率%s%%" % health.get("bodyFat"))
    if health.get("target"):
        parts.append("目标：%s" % health.get("target"))
    if not parts:
        return "你的健康档案信息较少，以下建议为通用版本。"
    return "你的健康档案：" + "，".join(parts) + "。"


def _context_text(health: Dict[str, Any], extra: str = "") -> str:
    fields = ("target", "disease", "dietAvoid", "allergy", "oldInjury")
    return " ".join([str(health.get(k) or "") for k in fields] + [extra or ""])


def _has(text: str, words) -> bool:
    return any(word in text for word in words)


def retrieve(text: str, k: int = 3) -> List[Dict[str, Any]]:
    """轻量检索：标签命中为主，标题二元组为辅。"""
    text = text or ""
    scored = []
    for entry in KNOWLEDGE:
        score = 0.0
        if entry["title"] in text:
            score += 4
        for tag in entry["tags"]:
            if tag.lower() in text.lower():
                score += 3
        for i in range(len(text) - 1):
            bigram = text[i:i + 2]
            if len(bigram.strip()) == 2 and bigram in entry["title"]:
                score += 0.4
        scored.append((score, entry))
    scored.sort(key=lambda pair: pair[0], reverse=True)
    picked = [entry for score, entry in scored if score > 0][:k]
    if not picked:
        picked = [entry for entry in KNOWLEDGE if entry["id"] in DEFAULT_IDS][:k]
    return picked


def _sources(entries: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
    return [{"id": e["id"], "title": e["title"]} for e in entries]


def _cautions(health: Dict[str, Any], text: str) -> List[str]:
    tips = []
    disease = str(health.get("disease") or "")
    if "尿酸" in disease or "痛风" in disease or "尿酸" in text:
        tips.append("尿酸偏高：限制动物内脏、浓肉汤、海鲜和啤酒，每日饮水 1500-2000ml。")
    if "脂肪肝" in disease:
        tips.append("轻度脂肪肝：控制体重和精制糖，减少酒精，半年复查腹部彩超。")
    if _has(disease, ("血脂", "胆固醇", "甘油三酯")):
        tips.append("血脂偏高：每日烹调油 25-30g，少吃油炸食品，3 个月复查血脂。")
    allergy = str(health.get("allergy") or "").replace("过敏", "")
    if allergy:
        tips.append("已知过敏食材「%s」需完全避开，外出就餐主动告知忌口。" % allergy)
    injury = str(health.get("oldInjury") or "")
    if injury:
        tips.append("运动旧伤「%s」：避免跳跃和大重量冲击，训练前充分热身，疼痛加重立即停止。" % injury)
    return tips


SYSTEM_PROMPT = (
    "你是「轻健身」App 的专业健身与营养教练，回答必须使用简体中文。\n"
    "规则：\n"
    "1. 只依据提供的【知识资料】和【用户健康档案】作答，资料没有覆盖的内容不要编造，可以说明知识库暂无相关内容。\n"
    "2. 建议要具体可执行（吃什么、吃多少、怎么练、练多久），并结合用户的目标、疾病、过敏和旧伤。\n"
    "3. 涉及疾病、用药或明显不适时提醒及时就医，不做诊断、不推荐药物。\n"
    "4. 输出纯文本，不要使用 Markdown 标题、表格或代码块；用“1. 2. 3.”分点，控制在 400 字以内。"
)

DISCLAIMER = "以上为基于本地知识库的一般性建议，不能替代医生诊断；如有明显不适请及时就医。"


def _health_prompt(health: Dict[str, Any]) -> str:
    if not health:
        return "用户未填写健康档案，请给通用建议。"
    lines = [_profile_summary(health)]
    extra = (("disease", "既往疾病"), ("allergy", "过敏史"), ("oldInjury", "运动旧伤"), ("dietAvoid", "饮食忌口"))
    for key, label in extra:
        value = str(health.get(key) or "").strip()
        if value:
            lines.append("%s：%s" % (label, value))
    return "\n".join(lines)


def _retrieve_docs(question: str, health: Dict[str, Any], k: int = 4):
    """优先向量召回，模型不可用或无命中时退回关键词检索，返回 (文档列表, 检索方式)。

    向量检索只使用用户问题，避免健康档案里的疾病/旧伤关键词干扰语义排序；
    关键词检索仍拼接健康档案，保持原有的疾病/忌口召回能力。
    """
    hits = vector_store.search(question, top_k=k)
    if hits:
        docs, seen = [], set()
        for hit in hits:
            if hit["id"] in seen:
                continue
            seen.add(hit["id"])
            docs.append({"id": hit["id"], "title": hit["title"], "content": hit["text"]})
        if docs:
            return docs, "bge"
    query = question + " " + _context_text(health)
    entries = retrieve(query, k=3)
    docs = [{"id": e["id"], "title": e["title"], "content": e["content"]} for e in entries]
    return docs, "keyword"


def _prompt_messages(question: str, health: Dict[str, Any], docs: List[Dict[str, Any]]):
    refs = "\n".join("[%s %s] %s" % (d["id"], d["title"], d["content"]) for d in docs)
    user = (
        "【用户健康档案】\n%s\n\n"
        "【知识资料】\n%s\n\n"
        "【用户问题】\n%s\n\n"
        "请直接给出你的回答。" % (_health_prompt(health), refs, question or "健康咨询")
    )
    return [{"role": "system", "content": SYSTEM_PROMPT}, {"role": "user", "content": user}]


def _template_answer(question: str, health: Dict[str, Any], docs: List[Dict[str, Any]]) -> str:
    lines = []
    summary = _profile_summary(health)
    if summary:
        lines.append(summary)
    lines.append("围绕「%s」，结合本地健康知识库整理如下建议：" % (question or "健康咨询"))
    lines.append("")
    for i, doc in enumerate(docs, 1):
        lines.append("%d. 【%s】%s" % (i, doc["title"], doc["content"]))
    tips = _cautions(health, question + " " + _context_text(health))
    if tips:
        lines.append("")
        lines.append("需要特别注意：")
        for tip in tips:
            lines.append("- " + tip)
    lines.append("")
    lines.append("参考依据：")
    for doc in docs:
        lines.append("- %s %s" % (doc["id"], doc["title"]))
    lines.append("")
    lines.append(DISCLAIMER)
    return "\n".join(lines)


def answer_question(question: str, health: Dict[str, Any]) -> Dict[str, Any]:
    health = health or {}
    question = (question or "").strip()
    docs, retrieval = _retrieve_docs(question, health, k=4)

    answer = None
    if llm.is_configured():
        answer = llm.chat(_prompt_messages(question, health, docs), temperature=0.5, max_tokens=900)

    if answer:
        return {
            "answer": answer,
            "sources": _sources(docs),
            "engine": "llm-rag-v1",
            "retrieval": retrieval,
        }
    return {
        "answer": _template_answer(question, health, docs),
        "sources": _sources(docs),
        "engine": "template-rag-v1",
        "retrieval": retrieval,
    }


def _calorie_target(health: Dict[str, Any]) -> int:
    weight, height, age = _num(health, "weight"), _num(health, "height"), _num(health, "age")
    if weight and height and age:
        offset = 5 if _gender_text(health) == "男" else -161
        bmr = 10 * weight + 6.25 * height - 5 * age + offset
        target = bmr * 1.375
        goal = str(health.get("target") or "")
        if "减脂" in goal:
            target -= 300
        elif "增肌" in goal:
            target += 300
        return max(1200, int(round(target)))
    return 2000


def _macros(health: Dict[str, Any], calories: int) -> Dict[str, int]:
    weight = _num(health, "weight") or 60
    goal = str(health.get("target") or "")
    ratio = 1.8 if "增肌" in goal else (1.6 if "减脂" in goal else 1.4)
    protein = int(round(weight * ratio))
    fat = int(round(calories * 0.25 / 9))
    carbs = max(0, int(round((calories - protein * 4 - fat * 9) / 4)))
    return {"protein": protein, "fat": fat, "carbs": carbs}


def _meals(health: Dict[str, Any], remark: str):
    text = _context_text(health, remark)
    dairy = _has(text, DAIRY)
    egg = _has(text, EGG)
    seafood = _has(text, SEAFOOD)
    nut = _has(text, NUT)
    veg = _has(text, VEG)

    breakfast = ["燕麦 50g", "无糖豆浆 300ml" if dairy else "低脂牛奶 250ml",
                 "豆腐 100g" if egg else "水煮蛋 2 个", "苹果 1 个"]
    lunch = ["糙米饭 150g", "北豆腐 200g" if veg else "鸡胸肉 150g", "西兰花 200g", "橄榄油 5g"]
    dinner_protein = "北豆腐 200g" if veg else ("鸡胸肉 150g" if seafood else "清蒸鲈鱼 150g")
    dinner = ["红薯 150g", dinner_protein, "凉拌菠菜 200g"]
    snack = ["无糖豆浆 300ml" if dairy else "无糖酸奶 150g", "蓝莓 100g" if nut else "杏仁 10g"]

    notes = []
    if dairy:
        notes.append("牛奶/乳制品替换为无糖豆浆或豆腐。")
    if egg:
        notes.append("鸡蛋替换为豆腐或鸡胸肉。")
    if seafood:
        notes.append("鱼虾贝类替换为鸡胸肉或北豆腐。")
    if nut:
        notes.append("坚果替换为蓝莓等低敏水果。")
    if veg:
        notes.append("蛋白质以豆制品为主，注意补充维生素 B12。")
    if _has(text, SPICY):
        notes.append("所有菜品不放辣椒，以清蒸、水煮、凉拌为主。")

    return breakfast, lunch, dinner, snack, notes


def build_diet_plan(health: Dict[str, Any], remark: str = "") -> Dict[str, Any]:
    health = health or {}
    calories = _calorie_target(health)
    macros = _macros(health, calories)
    breakfast, lunch, dinner, snack, notes = _meals(health, remark)
    goal = str(health.get("target") or "均衡饮食")

    docs = []
    diet_id = "kb-002" if "增肌" in goal else "kb-001"
    docs += [entry for entry in KNOWLEDGE if entry["id"] in (diet_id, "kb-003", "kb-004")]
    context = _context_text(health, remark)
    if _has(context, DAIRY + EGG + SEAFOOD + NUT + VEG):
        docs.append(_get("kb-012"))
    if "尿酸" in context or "痛风" in context:
        docs.append(_get("kb-006"))
    if "脂肪肝" in context:
        docs.append(_get("kb-007"))
    if _has(context, ("血脂", "胆固醇", "甘油三酯")):
        docs.append(_get("kb-005"))
    docs = _dedup(docs)

    shares = {"breakfast": 0.25, "lunch": 0.40, "dinner": 0.30, "snack": 0.10}
    lines = [
        "个性化饮食方案（目标：%s）" % goal,
        _profile_summary(health),
        "",
        "每日目标：约 %d kcal｜蛋白质 %dg｜脂肪 %dg｜碳水 %dg"
        % (calories, macros["protein"], macros["fat"], macros["carbs"]),
        "",
        "早餐（约 25%%，约 %d kcal）" % round(calories * shares["breakfast"]),
        "- " + " + ".join(breakfast),
        "午餐（约 40%%，约 %d kcal）" % round(calories * shares["lunch"]),
        "- " + " + ".join(lunch),
        "晚餐（约 30%%，约 %d kcal）" % round(calories * shares["dinner"]),
        "- " + " + ".join(dinner),
        "加餐（约 10%%，约 %d kcal）" % round(calories * shares["snack"]),
        "- " + " + ".join(snack),
        "",
        "执行要点：",
        "- 每餐先吃蔬菜和蛋白质，再吃主食，有助于控制血糖和食欲。",
        "- 烹饪少油少盐，全天饮水 1500-2000ml。",
        "- 每周固定同一时间称重，按每周 0.5-1kg 的节奏微调热量。",
    ]
    if notes:
        lines.append("")
        lines.append("针对你的忌口/健康状况：")
        for note in notes:
            lines.append("- " + note)
    if remark:
        lines.append("")
        lines.append("补充要求：%s（已尽量在上面的搭配中避开）" % remark)
    lines += ["", "参考依据："]
    for doc in docs:
        lines.append("- %s %s" % (doc["id"], doc["title"]))

    return {
        "answer": "\n".join(lines),
        "sources": _sources(docs),
        "engine": "local-rag-keyword-v1",
    }


def _get(kb_id: str) -> Dict[str, Any]:
    for entry in KNOWLEDGE:
        if entry["id"] == kb_id:
            return entry
    return KNOWLEDGE[0]


def _dedup(entries: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
    seen, result = set(), []
    for entry in entries:
        if entry["id"] not in seen:
            seen.add(entry["id"])
            result.append(entry)
    return result


def build_sport_plan(health: Dict[str, Any], remark: str = "") -> Dict[str, Any]:
    health = health or {}
    weight = _num(health, "weight") or 60
    goal = str(health.get("target") or "减脂")
    injury = str(health.get("oldInjury") or "")
    knee = _has(injury, ("膝", "半月板", "髌"))
    waist = _has(injury, ("腰", "腰椎", "间盘"))
    shoulder = _has(injury, ("肩", "肩袖"))

    cardio = "游泳或椭圆机" if knee else "快走或慢跑"
    cardio_alt = "椭圆机" if knee else "骑行"
    lower_body = "臀桥、坐姿腿屈伸（避免深蹲和跳跃）" if (knee or waist) else "深蹲、臀桥、箭步蹲"
    upper_body = "高位下拉、轻重量划船（避免推举）" if shoulder else "俯卧撑、哑铃划船、肩推"

    if "增肌" in goal:
        week = [
            ("周一", "力量：胸 + 三头（卧推、俯卧撑、臂屈伸）", 60, 5.0),
            ("周二", "力量：背 + 二头（划船、下拉、弯举）", 60, 5.0),
            ("周三", "休息 / 拉伸", 20, 2.5),
            ("周四", "力量：腿 + 肩（%s）" % lower_body, 60, 5.0),
            ("周五", "低强度有氧：%s" % cardio, 30, 4.5),
            ("周六", "力量：全身循环（%s）" % upper_body, 50, 5.0),
            ("周日", "完全休息", 0, 0),
        ]
    elif "塑形" in goal:
        week = [
            ("周一", "有氧：%s" % cardio, 40, 6.0),
            ("周二", "力量：臀腿 + 核心（%s）" % lower_body, 45, 5.0),
            ("周三", "休息 / 拉伸", 20, 2.5),
            ("周四", "力量：上肢（%s）" % upper_body, 40, 5.0),
            ("周五", "有氧：%s" % cardio_alt, 40, 6.0),
            ("周六", "全身循环：自重训练 + 核心", 30, 5.0),
            ("周日", "完全休息", 0, 0),
        ]
    else:
        week = [
            ("周一", "有氧：%s" % cardio, 45, 6.0),
            ("周二", "力量：上肢（%s）" % upper_body, 40, 5.0),
            ("周三", "主动恢复：快走 + 拉伸", 30, 3.5),
            ("周四", "有氧：%s" % cardio_alt, 40, 6.5),
            ("周五", "力量：下肢（%s）" % lower_body, 40, 5.0),
            ("周六", "间歇训练：%s（快 1 分钟 / 慢 2 分钟）" % cardio, 30, 8.0),
            ("周日", "完全休息", 0, 0),
        ]

    lines = [
        "个性化运动方案（目标：%s）" % goal,
        _profile_summary(health),
        "",
        "每周安排（力量与有氧交替，至少保留 1-2 天恢复）：",
    ]
    for day, activity, minutes, met in week:
        if minutes:
            kcal = int(round(met * 3.5 * weight / 200 * minutes))
            lines.append("%s  %s，约 %d 分钟，消耗约 %d kcal" % (day, activity, minutes, kcal))
        else:
            lines.append("%s  %s" % (day, activity))

    lines += [
        "",
        "执行要点：",
        "- 训练前动态热身 10 分钟，训练后拉伸 10 分钟。",
        "- 有氧心率控制在最大心率（220 减年龄）的 60%-75%。",
        "- 力量训练动作质量优先，出现关节疼痛立即停止。",
        "- 同一肌群间隔 48 小时，保证 7-8 小时睡眠。",
    ]
    if injury:
        lines.append("- 你的旧伤「%s」：已避开跳跃和大重量动作，必要时改为游泳或椭圆机。" % injury)
    match = re.search(r"每周\s*([2-7])\s*次", remark or "")
    if match:
        lines.append("- 你提到每周 %s 次，可从上表挑选 %s 天执行，其余改为主动恢复。" % (match.group(1), match.group(1)))
    if remark:
        lines.append("- 补充要求：%s" % remark)

    docs = [_get("kb-008"), _get("kb-009"), _get("kb-010")]
    if injury:
        docs.append(_get("kb-011"))
    lines += ["", "参考依据："]
    for doc in docs:
        lines.append("- %s %s" % (doc["id"], doc["title"]))

    return {
        "answer": "\n".join(lines),
        "sources": _sources(docs),
        "engine": "local-rag-keyword-v1",
    }
