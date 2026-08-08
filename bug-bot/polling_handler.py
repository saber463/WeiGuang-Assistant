"""
BUG诊断 + 编程知识机器人 - 轮询模式消息处理器
==============================================
功能：定时拉取飞书聊天消息，检测新消息并自动回复
原理：通过 lark-cli 每5秒拉取一次消息列表，对比已处理消息ID，只处理新消息
优势：不依赖飞书事件订阅配置，开箱即用

双知识库架构：
  ├── BUG知识库 (knowledge_base.json / knowledge_base_v2.json)
  │    来源：项目BUG排查日志，51条记录
  │    字段：category, symptom, root_cause, solution, time, error_code...
  │
  └── 编程知识库 (programming_kb_flat.json)
       来源：9门语言教学知识库，200条记录
       字段：language, level, title, concept, code, key_points...

智能路由策略：
  1. 同时搜索两个知识库
  2. 合并结果按 TF-IDF 余弦相似度降序排列
  3. 取 Top 5，自动标注来源（BUG诊断 / 编程知识）
  4. 卡片自适应：BUG用"报错现象/根因分析/修复方案"，编程用"知识点/详解/代码示例"

架构：
  polling_handler.py (本文件)
    ↓ (每5秒)
  lark-cli im +chat-messages-list --chat-id <chat_id> --as bot
    ↓ (解析JSON)
  双引擎 TF-IDF 搜索 → 合并排序 → 构建卡片
    ↓ (subprocess)
  lark-cli im +messages-send --chat-id <chat_id> --msg-type interactive --content '<card_json>'
"""
import json
import os
import re
import sys
import time
import subprocess
import logging
from pathlib import Path
from typing import List, Dict, Tuple, Set

import jieba
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity

# ============================================================
# 日志配置
# ============================================================
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s [%(levelname)s] %(message)s',
    datefmt='%Y-%m-%d %H:%M:%S',
    stream=sys.stderr
)
logger = logging.getLogger(__name__)

# ============================================================
# 配置常量
# ============================================================
BASE_DIR = Path(__file__).parent

# ---- 双知识库路径 ----
BUG_KB_PATH = BASE_DIR / "knowledge_base.json"          # BUG知识库（主）
BUG_KB_V2_PATH = BASE_DIR / "knowledge_base_v2.json"    # BUG知识库V2（备用）
PROG_KB_PATH = BASE_DIR / "programming_kb_flat.json"     # 编程语言知识库

PROCESSED_IDS_PATH = BASE_DIR / "processed_ids.json"

# 需要监听的聊天列表（chat_id 列表）
WATCH_CHATS = [
    "oc_43a0a03381929b6d372b94a3affc9409",  # 你的P2P聊天
]

# 轮询间隔（秒）
POLL_INTERVAL = 5

# 合并后返回的 Top K 结果数
TOP_K = 5


# ============================================================
# 通用搜索引擎（TF-IDF + 余弦相似度）
# 可复用于 BUG知识库 和 编程知识库
# ============================================================
class SearchEngine:
    """
    通用知识库搜索引擎
    使用 TF-IDF 向量化 + 余弦相似度实现语义搜索
    中文分词使用 jieba

    用法：
        engine = SearchEngine("BUG知识库", Path("bugs.json"))
        results = engine.search("点击按钮闪退", top_k=3)
    """

    def __init__(self, name: str, kb_path: Path):
        """
        初始化搜索引擎

        参数:
            name: 知识库名称（用于日志标识，如 "BUG知识库"、"编程知识库"）
            kb_path: JSON 知识库文件路径
        """
        self.name = name
        self.records: List[Dict] = []
        self.vectorizer = TfidfVectorizer(
            tokenizer=self._tokenize,
            max_features=5000,
            ngram_range=(1, 2)
        )
        self.tfidf_matrix = None
        self._load_kb(kb_path)

    def _tokenize(self, text: str) -> List[str]:
        """中文分词 + 英文分词，过滤单字和纯符号"""
        words = jieba.lcut(text)
        return [w for w in words if len(w.strip()) > 1 and not re.match(r'^[\W_]+$', w)]

    def _load_kb(self, path: Path):
        """加载知识库 JSON 并构建 TF-IDF 矩阵"""
        with open(path, "r", encoding="utf-8") as f:
            self.records = json.load(f)

        # 构建搜索文档：category + symptom + root_cause 合并
        documents = []
        for rec in self.records:
            doc = f"{rec.get('category', '')} {rec.get('symptom', '')} {rec.get('root_cause', '')}"
            documents.append(doc)

        self.tfidf_matrix = self.vectorizer.fit_transform(documents)
        logger.info(f"[{self.name}] 加载完成: {len(self.records)} 条记录, 特征维度: {self.tfidf_matrix.shape[1]}")

    def search(self, query: str, top_k: int = 3) -> List[Tuple[Dict, float]]:
        """
        搜索最匹配的记录

        参数:
            query: 用户查询文本
            top_k: 返回 Top K 条结果

        返回:
            [(record_dict, similarity_score), ...] 按相似度降序
        """
        if not query.strip():
            return []

        query_vec = self.vectorizer.transform([query])
        similarities = cosine_similarity(query_vec, self.tfidf_matrix).flatten()
        top_indices = similarities.argsort()[-top_k:][::-1]

        results = []
        for idx in top_indices:
            score = float(similarities[idx])
            if score > 0.01:  # 过滤完全不相关的
                results.append((self.records[idx], score))

        return results


# ============================================================
# 双引擎合并搜索
# ============================================================
class DualSearchEngine:
    """
    双知识库搜索引擎
    封装 BUG知识库 + 编程知识库，并行搜索后合并排序

    合并策略：
      1. 两个引擎分别搜索 Top N
      2. 合并所有结果
      3. 按 TF-IDF 余弦相似度降序排列
      4. 取 Top K，每条结果标注来源（source 字段）
    """

    def __init__(self):
        self.bug_engine = None
        self.prog_engine = None
        self._init_engines()

    def _init_engines(self):
        """初始化两个搜索引擎"""
        # BUG知识库：优先用 V2，不存在则用 V1
        bug_path = BUG_KB_V2_PATH if BUG_KB_V2_PATH.exists() else BUG_KB_PATH
        if not bug_path.exists():
            logger.error(f"BUG知识库不存在: {bug_path}")
            sys.exit(1)

        self.bug_engine = SearchEngine("BUG知识库", bug_path)

        # 编程知识库
        if PROG_KB_PATH.exists():
            self.prog_engine = SearchEngine("编程知识库", PROG_KB_PATH)
        else:
            logger.warning(f"编程知识库不存在: {PROG_KB_PATH}，将仅使用BUG知识库")
            self.prog_engine = None

    def search(self, query: str, top_k: int = TOP_K) -> List[Tuple[Dict, float, str]]:
        """
        双引擎搜索 + 合并排序

        参数:
            query: 用户查询文本
            top_k: 最终返回的 Top K 条结果

        返回:
            [(record_dict, similarity_score, source_label), ...]
            source_label 为 "🐛 BUG诊断" 或 "📚 编程知识"
        """
        all_results = []

        # BUG引擎搜索
        bug_results = self.bug_engine.search(query, top_k=top_k)
        for rec, score in bug_results:
            all_results.append((rec, score, "🐛 BUG诊断"))

        # 编程引擎搜索（如果存在）
        if self.prog_engine:
            prog_results = self.prog_engine.search(query, top_k=top_k)
            for rec, score in prog_results:
                all_results.append((rec, score, "📚 编程知识"))

        # 按相似度降序排列
        all_results.sort(key=lambda x: x[1], reverse=True)

        return all_results[:top_k]


# ============================================================
# 飞书消息卡片构建器（双知识库版本）
# ============================================================
class CardBuilder:
    """
    飞书消息卡片构建器 - Card 2.0 格式
    支持 BUG诊断 和 编程知识 两种卡片样式
    """

    @staticmethod
    def build_diagnosis_card(query: str, results: List[Tuple[Dict, float, str]]) -> Dict:
        """
        构建诊断结果卡片（双知识库合并版）

        参数:
            query: 用户原始查询
            results: [(record, score, source_label), ...]

        返回:
            飞书 Card 2.0 JSON
        """
        elements = []

        # ---- 无结果 ----
        if not results:
            elements.append({
                "tag": "markdown",
                "content": (
                    f"未找到与「{query[:50]}」相关的记录。\n\n"
                    f"**试试这些：**\n"
                    f"- 粘贴完整报错信息 → 匹配BUG诊断\n"
                    f"- 输入编程问题 → 匹配编程知识\n"
                    f"- 发送「帮助」查看使用说明"
                )
            })
            return {
                "schema": "2.0",
                "config": {"width_mode": "fill"},
                "header": {
                    "title": {"tag": "plain_text", "content": "智能诊断结果"},
                    "template": "red"
                },
                "body": {"direction": "vertical", "elements": elements}
            }

        # ---- 有结果：顶部摘要 ----
        # 分别统计两种来源数量
        bug_count = sum(1 for _, _, src in results if "BUG" in src)
        prog_count = sum(1 for _, _, src in results if "编程" in src)

        summary_parts = []
        if bug_count > 0:
            summary_parts.append(f"**{bug_count}** 条BUG诊断")
        if prog_count > 0:
            summary_parts.append(f"**{prog_count}** 条编程知识")
        summary = "、".join(summary_parts)

        elements.append({
            "tag": "markdown",
            "content": f"根据你的问题，找到 {summary}："
        })

        # ---- 每条结果 ----
        for i, (rec, score, source) in enumerate(results):
            match_pct = f"{score * 100:.0f}%"
            star = "⭐" if score > 0.5 else ("🔶" if score > 0.2 else "🔹")

            elements.append({"tag": "hr"})

            # 标题行：来源 + 匹配度 + 分类
            if "BUG" in source:
                category = rec.get('category', '未知')
                elements.append({
                    "tag": "markdown",
                    "content": f"### {star} {source} | 匹配度 {match_pct} | {category}"
                })
            else:
                # 编程知识：显示语言 + 级别
                language = rec.get('language', '')
                level = rec.get('level', '')
                title = rec.get('title', '')
                elements.append({
                    "tag": "markdown",
                    "content": f"### {star} {source} | {match_pct} | {language}-{level} | {title}"
                })

            # ---- 内容区：根据来源不同，标签不同 ----
            if "BUG" in source:
                # BUG诊断：报错现象 / 根因分析 / 修复方案
                symptom = rec.get('symptom', '')[:200]
                if len(rec.get('symptom', '')) > 200:
                    symptom += "..."
                elements.append({
                    "tag": "markdown",
                    "content": f"**报错现象**\n{symptom}"
                })

                cause = rec.get('root_cause', '')[:200]
                if len(rec.get('root_cause', '')) > 200:
                    cause += "..."
                elements.append({
                    "tag": "markdown",
                    "content": f"**根因分析**\n{cause}"
                })

                solution = rec.get('solution', '')[:200]
                if len(rec.get('solution', '')) > 200:
                    solution += "..."
                elements.append({
                    "tag": "markdown",
                    "content": f"**修复方案**\n{solution}"
                })
            else:
                # 编程知识：知识点概述 / 详解 / 代码示例
                concept = rec.get('concept', '')[:200]
                if len(rec.get('concept', '')) > 200:
                    concept += "..."
                elements.append({
                    "tag": "markdown",
                    "content": f"**知识点**\n{concept}"
                })

                explanation = rec.get('root_cause', '')[:200]
                if len(rec.get('root_cause', '')) > 200:
                    explanation += "..."
                elements.append({
                    "tag": "markdown",
                    "content": f"**详解**\n{explanation}"
                })

                # 代码示例：截取前500字符，超过则省略
                code = rec.get('solution', '')
                if len(code) > 500:
                    code = code[:500] + "\n... (代码过长，已截断)"
                if code.strip():
                    elements.append({
                        "tag": "markdown",
                        "content": f"**代码示例**\n```\n{code}\n```"
                    })

                # 关键点
                key_points = rec.get('key_points', [])
                if key_points:
                    points_text = "\n".join(f"- {p}" for p in key_points[:4])
                    elements.append({
                        "tag": "markdown",
                        "content": f"**关键要点**\n{points_text}"
                    })

        # ---- 底部提示 ----
        elements.append({"tag": "hr"})
        elements.append({
            "tag": "markdown",
            "content": (
                f"*双知识库检索 | BUG {bug_count}条 + 编程 {prog_count}条 | "
                f"发送「帮助」查看使用说明*"
            )
        })

        # 卡片头部颜色：高分BUG用红色，其他用蓝色
        top_score = results[0][1]
        top_source = results[0][2]
        if top_score > 0.5 and "BUG" in top_source:
            header_template = "red"
        else:
            header_template = "blue"

        return {
            "schema": "2.0",
            "config": {"width_mode": "fill"},
            "header": {
                "title": {"tag": "plain_text", "content": f"智能诊断: {query[:30]}..."},
                "template": header_template
            },
            "body": {"direction": "vertical", "elements": elements}
        }

    @staticmethod
    def build_help_card() -> Dict:
        """构建帮助卡片（双知识库版）"""
        return {
            "schema": "2.0",
            "config": {"width_mode": "fill"},
            "header": {
                "title": {"tag": "plain_text", "content": "智能诊断机器人 - 使用说明"},
                "template": "blue"
            },
            "body": {
                "direction": "vertical",
                "elements": [
                    {
                        "tag": "markdown",
                        "content": (
                            "**双知识库智能检索**\n\n"
                            "机器人同时搜索两个知识库，自动返回最匹配的结果：\n"
                            "🐛 **BUG诊断知识库**（51条）\n"
                            "📚 **编程语言知识库**（200条，9门语言）\n\n"
                            "---\n\n"
                            "**🐛 BUG诊断示例**\n"
                            "- `点击按钮闪退`\n"
                            "- `UnsatisfiedLinkError: dlopen failed`\n"
                            "- `闹钟长按10秒关不掉`\n"
                            "- `java.lang.SecurityException: Starting FGS`\n"
                            "- `TTS语音播报无声`\n\n"
                            "**📚 编程知识示例**\n"
                            "- `Python列表推导式怎么写`\n"
                            "- `flex布局怎么实现两端对齐`\n"
                            "- `Kotlin协程coroutine`\n"
                            "- `React useState怎么用`\n"
                            "- `Java面向对象继承多态`\n\n"
                            "---\n\n"
                            "**回复格式**\n"
                            "每条结果标注来源和匹配度：\n"
                            "🐛 BUG诊断 → 报错现象 / 根因分析 / 修复方案\n"
                            "📚 编程知识 → 知识点 / 详解 / 代码示例\n\n"
                            "**提示**\n"
                            "描述越详细，匹配越精准。"
                        )
                    },
                    {"tag": "hr"},
                    {
                        "tag": "markdown",
                        "content": "*知识库: BUG排查日志 + 编程语言教学 | 微光同行项目*"
                    }
                ]
            }
        }


# ============================================================
# 已处理消息ID管理
# ============================================================
def load_processed_ids() -> Set[str]:
    """加载已处理的消息ID集合"""
    if PROCESSED_IDS_PATH.exists():
        with open(PROCESSED_IDS_PATH, "r", encoding="utf-8") as f:
            return set(json.load(f))
    return set()


def save_processed_ids(ids: Set[str]):
    """保存已处理的消息ID集合"""
    with open(PROCESSED_IDS_PATH, "w", encoding="utf-8") as f:
        json.dump(list(ids), f)


# ============================================================
# 消息拉取与回复
# ============================================================
def fetch_messages(chat_id: str, limit: int = 10) -> List[Dict]:
    """通过 lark-cli 拉取聊天消息"""
    cmd = [
        "lark-cli", "im", "+chat-messages-list",
        "--chat-id", chat_id,
        "--as", "bot",
        "--page-size", str(limit)
    ]
    try:
        result = subprocess.run(cmd, capture_output=True, text=True, timeout=15)
        if result.returncode != 0:
            logger.error(f"拉取消息失败: {result.stderr[:300]}")
            return []

        data = json.loads(result.stdout)
        if data.get("ok") and data.get("data", {}).get("messages"):
            return data["data"]["messages"]
        return []
    except Exception as e:
        logger.error(f"拉取消息异常: {e}")
        return []


def send_card(chat_id: str, card: Dict) -> bool:
    """通过 lark-cli 发送卡片消息"""
    card_json = json.dumps(card, ensure_ascii=False)
    cmd = [
        "lark-cli", "im", "+messages-send",
        "--chat-id", chat_id,
        "--msg-type", "interactive",
        "--content", card_json,
        "--as", "bot"
    ]
    try:
        result = subprocess.run(cmd, capture_output=True, text=True, timeout=15)
        if result.returncode == 0:
            logger.info(f"消息发送成功 -> {chat_id}")
            return True
        else:
            logger.error(f"消息发送失败: {result.stderr[:300]}")
            return False
    except Exception as e:
        logger.error(f"消息发送异常: {e}")
        return False


def process_new_messages(chat_id: str, engine: DualSearchEngine, processed: Set[str]):
    """
    处理聊天中的新消息

    参数:
        chat_id: 飞书聊天ID
        engine: 双引擎搜索引擎
        processed: 已处理消息ID集合
    """
    messages = fetch_messages(chat_id)
    new_count = 0

    for msg in messages:
        msg_id = msg.get("message_id", "")
        if msg_id in processed:
            continue

        # 只处理用户的文本消息，忽略机器人自己的消息
        sender = msg.get("sender", {})
        if sender.get("sender_type") != "user":
            processed.add(msg_id)
            continue

        content = msg.get("content", "").strip()
        if not content:
            processed.add(msg_id)
            continue

        # 只处理文本消息
        if msg.get("msg_type") != "text":
            processed.add(msg_id)
            continue

        logger.info(f"新消息 [{msg_id[:20]}...]: {content[:100]}")

        # 判断消息类型，构建回复
        if content.lower() in ["帮助", "help", "使用说明", "?"]:
            card = CardBuilder.build_help_card()
        else:
            # 双引擎搜索
            results = engine.search(content, top_k=TOP_K)
            card = CardBuilder.build_diagnosis_card(content, results)

        # 发送卡片
        if send_card(chat_id, card):
            new_count += 1

        # 标记为已处理
        processed.add(msg_id)

    if new_count > 0:
        save_processed_ids(processed)

    return new_count


# ============================================================
# 主循环
# ============================================================
def main():
    """主入口：轮询模式消息处理"""
    logger.info("=" * 50)
    logger.info("智能诊断机器人 - 双知识库轮询模式 启动")
    logger.info(f"BUG知识库: {BUG_KB_PATH}")
    logger.info(f"编程知识库: {PROG_KB_PATH}")
    logger.info(f"监听聊天: {WATCH_CHATS}")
    logger.info(f"轮询间隔: {POLL_INTERVAL}s")
    logger.info(f"合并TopK: {TOP_K}")
    logger.info("=" * 50)

    # 初始化双引擎搜索引擎
    try:
        engine = DualSearchEngine()
    except Exception as e:
        logger.error(f"搜索引擎初始化失败: {e}")
        sys.exit(1)

    # 加载已处理消息ID
    processed = load_processed_ids()
    logger.info(f"已处理消息数: {len(processed)}")

    logger.info("开始轮询监听...")

    while True:
        try:
            for chat_id in WATCH_CHATS:
                new_count = process_new_messages(chat_id, engine, processed)
                if new_count > 0:
                    logger.info(f"聊天 {chat_id[:20]}... 处理了 {new_count} 条新消息")

            time.sleep(POLL_INTERVAL)

        except KeyboardInterrupt:
            logger.info("收到中断信号，退出...")
            break
        except Exception as e:
            logger.error(f"主循环异常: {e}", exc_info=True)
            time.sleep(POLL_INTERVAL)


if __name__ == "__main__":
    main()