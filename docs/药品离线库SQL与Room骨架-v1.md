# 药品离线库 SQL 与 Room 骨架设计 v1.0

> 口径说明：本文档属于首版骨架设计文档，现已被正式基线覆盖。
> 当前唯一正式基线文档为：`docs/2026-05-19-药品离线库正式基线说明.md`。
> 如本文档中的示例表结构与当前 `Room / AppDatabase` 不一致，一律以正式基线与现行代码为准。

## 1. 文档目标

本文档用于为微光畅行项目建立首版本地药品数据库设计规范，并给出 Android 侧 Room 数据层的落地骨架。目标是让安卓、数据清洗、算法和产品四个方向可以围绕统一的数据结构协同开发。

## 2. 设计原则

- 首版优先保证`离线可查`、`字段清晰`、`可快速导入公开数据`。
- 不把复杂医学知识图谱一次性塞进首版数据库，先围绕药名匹配、说明书展示和风险提示建立最小闭环。
- 对于多值字段，首版允许使用 `JSON 字符串` 或 `|` 分隔字符串存储，后续再拆范式。
- Room 层优先保证易接入和易读写，不追求一开始就做成超复杂的关系模型。

## 3. 首版核心表

### 3.1 `drug_master`

用途：药品主表，承载标准药名、商品名、批准文号、厂商等主索引信息。

```sql
CREATE TABLE IF NOT EXISTS drug_master (
    drug_id INTEGER PRIMARY KEY AUTOINCREMENT,
    generic_name TEXT NOT NULL,
    trade_name TEXT,
    approval_no TEXT,
    manufacturer TEXT,
    dosage_form TEXT,
    specification TEXT,
    category_name TEXT,
    search_tokens TEXT,
    created_at INTEGER NOT NULL DEFAULT (strftime('%s','now')),
    updated_at INTEGER NOT NULL DEFAULT (strftime('%s','now'))
);

CREATE INDEX IF NOT EXISTS idx_drug_master_generic_name ON drug_master(generic_name);
CREATE INDEX IF NOT EXISTS idx_drug_master_trade_name ON drug_master(trade_name);
CREATE INDEX IF NOT EXISTS idx_drug_master_approval_no ON drug_master(approval_no);
```

字段说明：

- `generic_name`：标准通用名，作为主检索词。
- `trade_name`：商品名。
- `approval_no`：国药准字等批准文号。
- `search_tokens`：预留给 OCR 模糊命中的搜索词集合。

### 3.2 `drug_alias`

用途：药品别名、OCR 容错词、历史名称映射。

```sql
CREATE TABLE IF NOT EXISTS drug_alias (
    alias_id INTEGER PRIMARY KEY AUTOINCREMENT,
    drug_id INTEGER NOT NULL,
    alias_name TEXT NOT NULL,
    alias_type TEXT NOT NULL DEFAULT 'alias',
    created_at INTEGER NOT NULL DEFAULT (strftime('%s','now')),
    FOREIGN KEY (drug_id) REFERENCES drug_master(drug_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_drug_alias_drug_id ON drug_alias(drug_id);
CREATE INDEX IF NOT EXISTS idx_drug_alias_name ON drug_alias(alias_name);
```

字段说明：

- `alias_type`：可区分 `alias`、`ocr_token`、`short_name`、`legacy_name`。

### 3.3 `drug_detail`

用途：说明书和详情表。

```sql
CREATE TABLE IF NOT EXISTS drug_detail (
    drug_id INTEGER PRIMARY KEY,
    indication TEXT,
    usage_and_dosage TEXT,
    taboo TEXT,
    attention TEXT,
    adverse_reaction TEXT,
    interaction_text TEXT,
    storage_method TEXT,
    valid_period TEXT,
    package_info TEXT,
    source_tag TEXT,
    updated_at INTEGER NOT NULL DEFAULT (strftime('%s','now')),
    FOREIGN KEY (drug_id) REFERENCES drug_master(drug_id) ON DELETE CASCADE
);
```

字段说明：

- `source_tag`：数据来源标识，如 `nmpa`、`datasn`、`manual_curated`。

### 3.4 `user_profile`

用途：用户健康档案。

```sql
CREATE TABLE IF NOT EXISTS user_profile (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    nickname TEXT,
    age_group TEXT,
    disease_tags TEXT,
    allergy_tags TEXT,
    current_drugs TEXT,
    notes TEXT,
    updated_at INTEGER NOT NULL DEFAULT (strftime('%s','now'))
);
```

字段说明：

- `disease_tags`：首版建议使用 JSON 字符串，如 `["高血压","糖尿病"]`。
- `current_drugs`：当前正在服用药物列表，首版允许用 JSON 字符串。

### 3.5 `drug_rule`

用途：本地风险规则表。

```sql
CREATE TABLE IF NOT EXISTS drug_rule (
    rule_id INTEGER PRIMARY KEY AUTOINCREMENT,
    drug_id INTEGER,
    match_field TEXT NOT NULL,
    match_value TEXT NOT NULL,
    rule_type TEXT NOT NULL,
    risk_level TEXT NOT NULL,
    message TEXT NOT NULL,
    enabled INTEGER NOT NULL DEFAULT 1,
    updated_at INTEGER NOT NULL DEFAULT (strftime('%s','now')),
    FOREIGN KEY (drug_id) REFERENCES drug_master(drug_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_drug_rule_drug_id ON drug_rule(drug_id);
CREATE INDEX IF NOT EXISTS idx_drug_rule_match_field ON drug_rule(match_field);
CREATE INDEX IF NOT EXISTS idx_drug_rule_match_value ON drug_rule(match_value);
```

字段说明：

- `match_field`：如 `disease_tag`、`allergy_tag`、`age_group`。
- `match_value`：如 `高血压`、`青霉素过敏`、`elderly`。
- `rule_type`：如 `contraindication`、`caution`、`tts_notice`。
- `risk_level`：如 `high`、`medium`、`low`。

## 4. 推荐导入顺序

1. 先导入 `drug_master`
2. 再导入 `drug_alias`
3. 再导入 `drug_detail`
4. 然后建立 `drug_rule`
5. 用户首次启动 App 后再创建 `user_profile`

## 5. Room 对应关系

| SQL 表 | Room Entity |
|---|---|
| `drug_master` | `DrugMasterEntity` |
| `drug_alias` | `DrugAliasEntity` |
| `drug_detail` | `DrugDetailEntity` |
| `user_profile` | `UserProfileEntity` |
| `drug_rule` | `DrugRuleEntity` |

## 6. Android 侧数据层包结构

```text
data/
  local/
    AppDatabase.kt
    dao/
      DrugDao.kt
      DrugRuleDao.kt
      UserProfileDao.kt
    entity/
      DrugMasterEntity.kt
      DrugAliasEntity.kt
      DrugDetailEntity.kt
      DrugRuleEntity.kt
      UserProfileEntity.kt
  repository/
    DrugRepository.kt
    LocalDrugRepository.kt
```

## 7. Repository 聚合输出建议

首版不直接把 Room Entity 暴露给 UI，而是通过聚合模型输出：

```kotlin
data class DrugInfo(
    val drugId: Long,
    val genericName: String,
    val tradeName: String?,
    val approvalNo: String?,
    val manufacturer: String?,
    val indication: String?,
    val usageAndDosage: String?,
    val taboo: String?,
    val attention: String?,
    val aliases: List<String>,
    val riskPrompts: List<String>
)
```

这样做的好处：

- UI 层只处理展示需要的结构。
- 后续可替换为网络源或更复杂数据库，而不冲击页面层。

## 8. 首版查询策略

### 8.1 OCR 文本查询

按以下顺序匹配：

1. `approval_no` 精确匹配
2. `generic_name` 精确匹配
3. `trade_name` 精确匹配
4. `alias_name` 精确匹配
5. `generic_name` / `trade_name` LIKE 模糊匹配

### 8.2 手语识别查询

按以下顺序匹配：

1. 手语识别结果映射到标准通用名
2. 若失败，则查别名表
3. 若仍失败，则回落到模糊检索结果列表

## 9. 首版数据规模建议

- `比赛版`：200-500 条高频药品数据
- `工程版`：3000-5000 条家庭常备药和慢病常用药
- `全量版`：按授权情况扩展到 3 万条以上

## 10. 存储与性能建议

- 所有大文本字段建议启用本地压缩打包后导入 SQLite。
- 首版 SQLite 不做 FTS5 全文索引，优先使用普通索引降低复杂度。
- 若后续模糊搜索需求增强，再补 `drug_search_fts` 虚拟表。

## 11. 当前落地说明

本轮代码将先按本文档建立 Room 骨架，不直接导入真实药品数据，但会把表、DAO 和 Repository 的主结构补齐，供后续数据清洗脚本和 UI 页面直接接入。
