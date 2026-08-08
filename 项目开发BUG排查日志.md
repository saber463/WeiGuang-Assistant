# 项目开发BUG排查日志

> **主文档已迁移至 [BUG_LOG.md](BUG_LOG.md)** — 包含全部51条BUG的完整排查记录（问题现象、报错代码、复现步骤、根因分析、修复方案、涉及文件）
> 
> 本文件保留作为历史记录引用。新增BUG请直接编辑 `bug-bot/knowledge_base_v2.json` 后运行 `python bug-bot/gen_bug_manual.py` 重新生成主文档。

---

## 快速导航

| 文档 | 说明 |
|------|------|
| [BUG_LOG.md](BUG_LOG.md) | **主文档** — 51条SafeGuard项目BUG完整手册 |
| [BUG修复日志.md](BUG修复日志.md) | 历史记录 — 旧版APP编译错误修复记录 |
| [BUG排查日志.md](BUG排查日志.md) | 历史记录 — 加密模块/FENSBox相关BUG |
| [BUG日志.md](BUG日志.md) | 历史记录 — 旧版模板 |

---

## 新增BUG填写模板

> 新BUG请使用以下模板，添加到 `bug-bot/knowledge_base_v2.json` 中：

```json
{
    "id": "bug_NNN",
    "time": "YYYY-MM-DD HH:MM",
    "category": "主分类",
    "sub_category": "子分类",
    "language": "Kotlin",
    "symptom": "问题现象描述",
    "error_code": "报错代码/异常类名",
    "trigger": "触发条件",
    "root_cause": "根因分析",
    "solution": "修复方案",
    "files": ["涉及文件1.kt", "涉及文件2.kt"],
    "severity": "critical/high/medium/low",
    "tags": ["标签1", "标签2"]
}
```

然后运行 `python bug-bot/gen_bug_manual.py` 重新生成 BUG_LOG.md。