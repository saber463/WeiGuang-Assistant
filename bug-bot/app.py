"""
BUG诊断机器人 - Flask后端服务
功能：接收飞书机器人消息 → TF-IDF语义搜索BUG知识库 → 返回卡片格式诊断结果
逻辑：
  1. 飞书用户发送报错信息 → 飞书回调 /webhook
  2. 对消息文本做TF-IDF向量化 + 余弦相似度匹配知识库
  3. 返回Top 3匹配结果，格式为飞书消息卡片（报错代码 + 原因 + 方案）
"""
import json
import os
import re
import logging
from pathlib import Path
from typing import List, Dict, Tuple, Optional

import jieba
from flask import Flask, request, jsonify
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity

# 飞书SDK
import lark_oapi as lark
from lark_oapi.api.im.v1 import CreateMessageRequest, CreateMessageRequestBody, CreateMessageResponse

# ============================================================
# 日志配置
# ============================================================
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s [%(levelname)s] %(message)s',
    datefmt='%Y-%m-%d %H:%M:%S'
)
logger = logging.getLogger(__name__)

# ============================================================
# 配置常量
# ============================================================
BASE_DIR = Path(__file__).parent
KNOWLEDGE_BASE_PATH = BASE_DIR / "knowledge_base.json"

# 飞书应用配置（从环境变量读取，安全考虑）
FEISHU_APP_ID = os.environ.get("FEISHU_APP_ID", "cli_aafe3e6497781d0c")
FEISHU_APP_SECRET = os.environ.get("FEISHU_APP_SECRET", "")
FEISHU_VERIFY_TOKEN = os.environ.get("FEISHU_VERIFY_TOKEN", "bug_bot_verify_2026")

# ============================================================
# BUG搜索引擎（TF-IDF + 余弦相似度）
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
        # jieba分词
        words = jieba.lcut(text)
        # 过滤掉单字和纯标点
        return [w for w in words if len(w.strip()) > 1 and not re.match(r'^[\W_]+$', w)]

    def _load_knowledge(self, path: Path):
        """加载BUG知识库并构建TF-IDF矩阵"""
        with open(path, "r", encoding="utf-8") as f:
            self.bugs = json.load(f)

        # 构建搜索文档：分类 + 现象 + 根因 合并作为搜索文本
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

        # 查询文本向量化
        query_vec = self.vectorizer.transform([query])
        # 计算余弦相似度
        similarities = cosine_similarity(query_vec, self.tfidf_matrix).flatten()
        # 获取Top K索引
        top_indices = similarities.argsort()[-top_k:][::-1]

        results = []
        for idx in top_indices:
            score = float(similarities[idx])
            if score > 0.01:  # 过滤掉完全不相关的
                results.append((self.bugs[idx], score))

        return results


# ============================================================
# 飞书消息卡片构建器
# ============================================================
class CardBuilder:
    """
    飞书消息卡片构建器
    将BUG诊断结果格式化为飞书交互式卡片
    风格：简单易懂 + 不失专业性
    """

    @staticmethod
    def build_diagnosis_card(query: str, results: List[Tuple[Dict, float]]) -> Dict:
        """
        构建诊断结果卡片
        格式：标题 + 搜索摘要 + 每条结果（报错代码 + 原因 + 方案）
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
                "config": {"wide_screen_mode": True},
                "header": {
                    "title": {"tag": "plain_text", "content": "BUG诊断结果"},
                    "template": "red"
                },
                "elements": elements
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
            "tag": "note",
            "elements": [
                {"tag": "plain_text",
                 "content": f"搜索时间: {bug.get('time', 'N/A')} | 知识库共 {len(results)} 条匹配 | 发送「帮助」查看使用说明"}
            ]
        })

        return {
            "config": {"wide_screen_mode": True},
            "header": {
                "title": {"tag": "plain_text", "content": f"BUG诊断: {query[:30]}..."},
                "template": "red" if results and results[0][1] > 0.5 else "blue"
            },
            "elements": elements
        }

    @staticmethod
    def build_help_card() -> Dict:
        """构建帮助卡片"""
        return {
            "config": {"wide_screen_mode": True},
            "header": {
                "title": {"tag": "plain_text", "content": "BUG诊断机器人 - 使用说明"},
                "template": "blue"
            },
            "elements": [
                {
                    "tag": "markdown",
                    "content": "**使用方法**\n\n直接发送报错信息或命令行报错，机器人会自动搜索匹配的BUG记录并返回诊断结果。\n\n**示例输入**\n- `点击按钮闪退`\n- `UnsatisfiedLinkError: dlopen failed`\n- `闹钟长按10秒关不掉`\n- `java.lang.SecurityException: Starting FGS`\n- `TTS语音播报无声`\n\n**回复格式**\n每条匹配结果包含：\n1. 报错现象\n2. 根因分析\n3. 修复方案\n\n**提示**\n描述越详细，匹配越精准。可以粘贴完整的报错堆栈。"
                },
                {"tag": "hr"},
                {
                    "tag": "note",
                    "elements": [{"tag": "plain_text", "content": "知识库来源: BUG排查日志.md | 微光同行项目"}]
                }
            ]
        }


# ============================================================
# Flask应用
# ============================================================
app = Flask(__name__)

# 初始化搜索引擎
search_engine = BugSearchEngine(KNOWLEDGE_BASE_PATH)

# 初始化飞书客户端
feishu_client = lark.Client.builder() \
    .app_id(FEISHU_APP_ID) \
    .app_secret(FEISHU_APP_SECRET) \
    .build()


def send_feishu_message(open_id: str, content: str, msg_type: str = "interactive") -> bool:
    """
    发送飞书消息给用户
    参数:
        open_id: 用户的open_id
        content: 消息内容（JSON字符串）
        msg_type: 消息类型（interactive / text）
    """
    try:
        request = CreateMessageRequest.builder() \
            .receive_id_type("open_id") \
            .request_body(
                CreateMessageRequestBody.builder()
                    .receive_id(open_id)
                    .msg_type(msg_type)
                    .content(content)
                    .build()
            ).build()

        response: CreateMessageResponse = feishu_client.im.v1.message.create(request)
        if response.success():
            logger.info(f"消息发送成功 -> {open_id}")
            return True
        else:
            logger.error(f"消息发送失败: {response.code} {response.msg}")
            return False
    except Exception as e:
        logger.error(f"消息发送异常: {e}")
        return False


@app.route("/health", methods=["GET"])
def health():
    """健康检查接口"""
    return jsonify({"status": "ok", "bugs_loaded": len(search_engine.bugs)})


@app.route("/webhook", methods=["POST"])
def webhook():
    """
    飞书事件回调接口
    处理消息接收事件，搜索BUG知识库并回复
    """
    try:
        body = request.get_json()
        logger.info(f"收到飞书回调: {json.dumps(body, ensure_ascii=False)[:500]}")

        # 1. URL验证（飞书配置回调地址时的challenge）
        if body.get("type") == "url_verification":
            token = body.get("token", "")
            challenge = body.get("challenge", "")
            logger.info(f"URL验证: token={token}, challenge={challenge}")
            return jsonify({"challenge": challenge})

        # 2. 事件回调
        header = body.get("header", {})
        event_type = header.get("event_type", "")

        # 只处理消息接收事件
        if event_type != "im.message.receive_v1":
            return jsonify({"code": 0})

        event = body.get("event", {})
        message = event.get("message", {})
        message_type = message.get("message_type", "")
        content_str = message.get("content", "{}")
        sender = event.get("sender", {})
        sender_id = sender.get("sender_id", {}).get("open_id", "")

        if not sender_id:
            logger.warning("无法获取发送者open_id")
            return jsonify({"code": 0})

        # 解析消息内容
        try:
            content = json.loads(content_str)
        except json.JSONDecodeError:
            content = {}

        user_text = content.get("text", "").strip()

        # 空消息忽略
        if not user_text:
            return jsonify({"code": 0})

        logger.info(f"用户消息: open_id={sender_id}, text={user_text[:100]}")

        # 3. 搜索 + 回复
        if user_text.lower() in ["帮助", "help", "使用说明", "?"]:
            # 帮助指令
            card = CardBuilder.build_help_card()
            send_feishu_message(sender_id, json.dumps(card))
        else:
            # BUG诊断搜索
            results = search_engine.search(user_text, top_k=3)
            card = CardBuilder.build_diagnosis_card(user_text, results)
            send_feishu_message(sender_id, json.dumps(card))

        return jsonify({"code": 0})

    except Exception as e:
        logger.error(f"Webhook处理异常: {e}", exc_info=True)
        return jsonify({"code": 0})


# ============================================================
# 命令行测试入口
# ============================================================
def cli_test():
    """命令行测试：直接输入报错信息搜索"""
    print("=" * 60)
    print("  BUG诊断机器人 - 命令行测试模式")
    print("  输入报错信息搜索，输入 quit 退出")
    print("=" * 60)

    while True:
        try:
            query = input("\n请输入报错信息: ").strip()
        except (EOFError, KeyboardInterrupt):
            break

        if not query:
            continue
        if query.lower() in ["quit", "exit", "q"]:
            break
        if query.lower() in ["帮助", "help", "?"]:
            print("\n发送「帮助」查看使用说明（飞书模式）")
            continue

        results = search_engine.search(query, top_k=3)

        if not results:
            print("\n未找到匹配的BUG记录。")
            continue

        print(f"\n找到 {len(results)} 条匹配结果:")
        for i, (bug, score) in enumerate(results):
            print(f"\n{'─' * 50}")
            print(f"  #{i+1}  匹配度: {score*100:.0f}%  分类: {bug['category']}")
            print(f"  时间: {bug['time']}")
            print(f"  现象: {bug['symptom'][:100]}...")
            print(f"  原因: {bug['root_cause'][:100]}...")
            print(f"  方案: {bug['solution'][:100]}...")


if __name__ == "__main__":
    import sys
    if len(sys.argv) > 1 and sys.argv[1] == "test":
        cli_test()
    else:
        print("启动Flask服务: http://0.0.0.0:5000")
        print("Webhook地址: http://<your-domain>:5000/webhook")
        app.run(host="0.0.0.0", port=5000, debug=True)