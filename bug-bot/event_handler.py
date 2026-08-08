"""
BUG诊断机器人 - 飞书长连接事件处理器
=====================================
功能：通过 lark-cli event consume 管道接收飞书消息事件，
      TF-IDF语义搜索BUG知识库，构建Card 2.0卡片回复。
架构：
  lark-cli event consume im.message.receive_v1 --as bot
    ↓ (NDJSON via stdout pipe)
  event_handler.py (本文件)
    ↓ (subprocess call)
  lark-cli im +messages-send --chat-id/user-id --msg-type interactive --content '<card_json>'

逻辑：
  1. 从 stdin 逐行读取 NDJSON 事件
  2. 解析 im.message.receive_v1 事件，提取消息文本
  3. 对消息文本做 TF-IDF 向量化 + 余弦相似度匹配知识库
  4. 返回 Top 3 匹配结果，格式为飞书消息卡片 2.0
  5. 通过 lark-cli 子进程发送卡片回复
"""
import json
import os
import re
import sys
import subprocess
import logging
import signal
from pathlib import Path
from typing import List, Dict, Tuple, Optional

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
    stream=sys.stderr  # 日志输出到 stderr，避免污染 stdout 管道
)
logger = logging.getLogger(__name__)

# ============================================================
# 配置常量
# ============================================================
BASE_DIR = Path(__file__).parent
KNOWLEDGE_BASE_PATH = BASE_DIR / "knowledge_base.json"

# 是否继续运行（用于优雅退出）
_running = True


def signal_handler(signum, frame):
    """信号处理：优雅退出"""
    global _running
    logger.info(f"收到信号 {signum}，准备退出...")
    _running = False


signal.signal(signal.SIGTERM, signal_handler)
signal.signal(signal.SIGINT, signal_handler)


# ============================================================
# BUG搜索引擎（TF-IDF + 余弦相似度）
# 与原始 app.py 中的 BugSearchEngine 逻辑完全一致
# ============================================================
class BugSearchEngine:
    """
    BUG知识库搜索引擎
    使用TF-IDF向量化 + 余弦相似度实现语义搜索
    中文分词使用jieba
    """

    def __init__(self, knowledge_base_path: Path):
        self.bugs: List[Dict] = []
        self.vectorizer = TfidfVectorizer(
            tokenizer=self._tokenize,
            max_features=5000,
            ngram_range=(1, 2)
        )
        self.tfidf_matrix = None
        self._load_knowledge(knowledge_base_path)

    def _tokenize(self, text: str) -> List[str]:
        """中文分词 + 英文分词"""
        words = jieba.lcut(text)
        return [w for w in words if len(w.strip()) > 1 and not re.match(r'^[\W_]+$', w)]

    def _load_knowledge(self, path: Path):
        """加载BUG知识库并构建TF-IDF矩阵"""
        with open(path, "r", encoding="utf-8") as f:
            self.bugs = json.load(f)

        documents = []
        for bug in self.bugs:
            doc = f"{bug['category']} {bug['symptom']} {bug['root_cause']}"
            documents.append(doc)

        self.tfidf_matrix = self.vectorizer.fit_transform(documents)
        logger.info(f"知识库加载完成: {len(self.bugs)} 条BUG, 特征维度: {self.tfidf_matrix.shape[1]}")

    def search(self, query: str, top_k: int = 3) -> List[Tuple[Dict, float]]:
        """
        搜索最匹配的BUG记录
        参数:
            query: 用户输入的报错信息
            top_k: 返回Top K条结果
        返回:
            [(bug_dict, similarity_score), ...] 按相似度降序排列
        """
        if not query.strip():
            return []

        query_vec = self.vectorizer.transform([query])
        similarities = cosine_similarity(query_vec, self.tfidf_matrix).flatten()
        top_indices = similarities.argsort()[-top_k:][::-1]

        results = []
        for idx in top_indices:
            score = float(similarities[idx])
            if score > 0.01:
                results.append((self.bugs[idx], score))

        return results


# ============================================================
# 飞书消息卡片构建器（Card 2.0 格式）
# ============================================================
class CardBuilder:
    """
    飞书消息卡片构建器
    构建 Card 2.0 格式的交互式卡片
    风格：简单易懂 + 不失专业性
    """

    @staticmethod
    def build_diagnosis_card(query: str, results: List[Tuple[Dict, float]]) -> Dict:
        """
        构建诊断结果卡片（Card 2.0 格式）
        包含：标题 + 搜索摘要 + 每条结果（报错代码 + 原因 + 方案）
        """
        elements = []

        # 搜索摘要
        if results:
            elements.append({
                "tag": "markdown",
                "content": f"根据你的报错信息，找到 **{len(results)}** 条相关BUG记录："
            })
        else:
            elements.append({
                "tag": "markdown",
                "content": f"未找到与「{query[:50]}」相关的BUG记录。\n\n请在知识库中手动添加，或尝试更详细的报错描述。"
            })
            return {
                "schema": "2.0",
                "config": {"width_mode": "fill"},
                "header": {
                    "title": {"tag": "plain_text", "content": "BUG诊断结果"},
                    "template": "red"
                },
                "body": {"direction": "vertical", "elements": elements}
            }

        # 每条BUG结果
        for i, (bug, score) in enumerate(results):
            match_pct = f"{score * 100:.0f}%"
            star = "⭐" if score > 0.5 else ("🔶" if score > 0.2 else "🔹")

            elements.append({"tag": "hr"})

            # 标题行：匹配度 + 分类
            elements.append({
                "tag": "markdown",
                "content": f"### {star} 匹配度 {match_pct} | {bug['category']}"
            })

            # 报错现象
            symptom_short = bug['symptom'][:200] + ("..." if len(bug['symptom']) > 200 else "")
            elements.append({
                "tag": "markdown",
                "content": f"**报错现象**\n{symptom_short}"
            })

            # 报错原因
            cause_short = bug['root_cause'][:200] + ("..." if len(bug['root_cause']) > 200 else "")
            elements.append({
                "tag": "markdown",
                "content": f"**根因分析**\n{cause_short}"
            })

            # 解决方案
            solution_short = bug['solution'][:200] + ("..." if len(bug['solution']) > 200 else "")
            elements.append({
                "tag": "markdown",
                "content": f"**修复方案**\n{solution_short}"
            })

        # 底部提示
        elements.append({"tag": "hr"})
        elements.append({
            "tag": "markdown",
            "content": f"*知识库共 {len(results)} 条匹配 | 发送「帮助」查看使用说明*"
        })

        return {
            "schema": "2.0",
            "config": {"width_mode": "fill"},
            "header": {
                "title": {"tag": "plain_text", "content": f"BUG诊断: {query[:30]}..."},
                "template": "red" if results and results[0][1] > 0.5 else "blue"
            },
            "body": {"direction": "vertical", "elements": elements}
        }

    @staticmethod
    def build_help_card() -> Dict:
        """构建帮助卡片（Card 2.0 格式）"""
        return {
            "schema": "2.0",
            "config": {"width_mode": "fill"},
            "header": {
                "title": {"tag": "plain_text", "content": "BUG诊断机器人 - 使用说明"},
                "template": "blue"
            },
            "body": {
                "direction": "vertical",
                "elements": [
                    {
                        "tag": "markdown",
                        "content": (
                            "**使用方法**\n\n"
                            "直接发送报错信息或命令行报错，机器人会自动搜索匹配的BUG记录并返回诊断结果。\n\n"
                            "**示例输入**\n"
                            "- `点击按钮闪退`\n"
                            "- `UnsatisfiedLinkError: dlopen failed`\n"
                            "- `闹钟长按10秒关不掉`\n"
                            "- `java.lang.SecurityException: Starting FGS`\n"
                            "- `TTS语音播报无声`\n\n"
                            "**回复格式**\n"
                            "每条匹配结果包含：\n"
                            "1. 报错现象\n"
                            "2. 根因分析\n"
                            "3. 修复方案\n\n"
                            "**提示**\n"
                            "描述越详细，匹配越精准。可以粘贴完整的报错堆栈。"
                        )
                    },
                    {"tag": "hr"},
                    {
                        "tag": "markdown",
                        "content": "*知识库来源: BUG排查日志.md | 微光同行项目*"
                    }
                ]
            }
        }


# ============================================================
# 消息发送器（通过 lark-cli 子进程）
# ============================================================
def send_card(chat_id: str, chat_type: str, card_json: str) -> bool:
    """
    通过 lark-cli 发送卡片消息
    参数:
        chat_id: 会话ID（群聊为 oc_xxx，私聊为 ou_xxx）
        chat_type: 会话类型（p2p 或 group）
        card_json: 卡片 JSON 字符串
    返回:
        是否发送成功
    """
    try:
        # 群聊用 --chat-id，私聊用 --user-id
        if chat_type == "group":
            cmd = [
                "lark-cli", "im", "+messages-send",
                "--chat-id", chat_id,
                "--msg-type", "interactive",
                "--content", card_json
            ]
        else:
            cmd = [
                "lark-cli", "im", "+messages-send",
                "--user-id", chat_id,
                "--msg-type", "interactive",
                "--content", card_json
            ]

        result = subprocess.run(
            cmd,
            capture_output=True,
            text=True,
            timeout=15,
            cwd=str(BASE_DIR)
        )

        if result.returncode == 0:
            logger.info(f"消息发送成功 -> {chat_id}")
            return True
        else:
            logger.error(f"消息发送失败: {result.stderr[:500]}")
            return False

    except subprocess.TimeoutExpired:
        logger.error(f"消息发送超时 -> {chat_id}")
        return False
    except Exception as e:
        logger.error(f"消息发送异常: {e}")
        return False


# ============================================================
# 事件处理主循环
# ============================================================
def process_event(event: Dict, engine: BugSearchEngine):
    """
    处理单个飞书事件
    参数:
        event: 飞书事件 JSON（已从 NDJSON 解析）
        engine: BUG搜索引擎实例
    """
    # 只处理消息接收事件
    event_type = event.get("event_type", "")
    if event_type != "im.message.receive_v1":
        return

    # 提取关键字段
    message_type = event.get("message_type", "")
    content_text = event.get("content", "").strip()
    chat_id = event.get("chat_id", "")
    chat_type = event.get("chat_type", "")
    sender_id = event.get("sender_id", "")

    if not content_text or not chat_id:
        return

    # 确定发送目标：群聊用 chat_id，私聊用 sender_id
    target_id = chat_id if chat_type == "group" else sender_id

    logger.info(f"收到消息: chat={chat_id}, type={message_type}, text={content_text[:100]}")

    # 判断消息类型，构建回复
    if content_text.lower() in ["帮助", "help", "使用说明", "?"]:
        card = CardBuilder.build_help_card()
    else:
        results = engine.search(content_text, top_k=3)
        card = CardBuilder.build_diagnosis_card(content_text, results)

    # 发送卡片
    card_json = json.dumps(card, ensure_ascii=False)
    send_card(target_id, chat_type, card_json)


def main():
    """主入口：从 stdin 读取 NDJSON 事件流，逐条处理"""
    logger.info("=" * 50)
    logger.info("BUG诊断机器人 - 长连接事件处理器 启动")
    logger.info(f"知识库路径: {KNOWLEDGE_BASE_PATH}")
    logger.info("=" * 50)

    # 初始化搜索引擎（加载知识库）
    try:
        engine = BugSearchEngine(KNOWLEDGE_BASE_PATH)
    except Exception as e:
        logger.error(f"知识库加载失败: {e}")
        sys.exit(1)

    logger.info("开始监听飞书事件...")

    # 主循环：逐行读取 stdin 中的 NDJSON
    for line in sys.stdin:
        if not _running:
            break

        line = line.strip()
        if not line:
            continue

        try:
            event = json.loads(line)
            process_event(event, engine)
        except json.JSONDecodeError as e:
            logger.warning(f"JSON解析失败: {e}, 原始数据: {line[:200]}")
        except Exception as e:
            logger.error(f"事件处理异常: {e}", exc_info=True)

    logger.info("事件处理器已退出")


if __name__ == "__main__":
    main()