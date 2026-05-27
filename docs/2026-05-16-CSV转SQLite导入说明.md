# 2026-05-16 CSV 转 SQLite 导入说明

> 口径说明：本文档属于阶段性导入说明，现已被正式基线覆盖。
> 当前唯一正式基线文档为：`docs/2026-05-19-药品离线库正式基线说明.md`。
> 从当前版本开始，`Room` 决定结构，`CSV` 决定内容，`build_seed_db.py` 只负责按正式 schema 生成 `drugs.db`。

## 1. 目标

这份文档用于把首版药品样本从“代码内置种子数据”推进到“可维护的 CSV 数据源 + 可生成的 SQLite 离线库”。

本轮产物包括：

- `data/seed/*.csv`
- `tools/build_seed_db.py`
- 生成目标：`app/src/main/assets/db/drugs.db`

## 2. 当前文件结构

```text
data/seed/
  drug_master.csv
  drug_detail.csv
  drug_alias.csv
  drug_sign_mapping.csv
  drug_rule.csv

tools/
  build_seed_db.py

docs/sql/
  drug_offline_schema.sql

app/src/main/assets/db/
  drugs.db
```

## 3. 五份 CSV 各自用途

### 3.1 `drug_master.csv`

主检索表，保存：

- 药品通用名
- 商品名
- 批准文号
- 厂商
- 剂型
- 规格
- 分类

### 3.2 `drug_detail.csv`

说明书详情表，保存：

- 适应症
- 用法用量
- 禁忌
- 注意事项
- 不良反应
- TTS 摘要

### 3.3 `drug_alias.csv`

别名和 OCR 容错词表，保存：

- 短名称
- OCR 高概率命中词
- 同义词

### 3.4 `drug_sign_mapping.csv`

药品与手语资源映射表，保存：

- 手语关键词
- 手语显示文案
- 视频路径
- 图示路径

### 3.5 `drug_rule.csv`

风险规则表，保存：

- 匹配字段
- 匹配值
- 风险等级
- 风险提醒文案
- TTS 提醒文案

## 4. 脚本做了什么

`build_seed_db.py` 的职责：

1. 读取 `docs/sql/drug_offline_schema.sql`
2. 初始化 SQLite 表结构和索引
3. 顺序导入 5 份 CSV
4. 输出到 `app/src/main/assets/db/drugs.db`

## 5. 实操命令

在项目根目录执行：

```bash
python tools/build_seed_db.py
```

如果你想输出到其他位置：

```bash
python tools/build_seed_db.py --output F:\temp\drugs.db
```

## 6. 导入顺序

脚本按以下顺序导入：

1. `drug_master`
2. `drug_detail`
3. `drug_alias`
4. `drug_sign_mapping`
5. `drug_rule`

这样可以确保外键依赖正确。

## 7. 当前首版样本

当前默认导入 3 个演示药品：

- 布洛芬缓释胶囊
- 阿莫西林胶囊
- 盐酸二甲双胍片

并同步导入：

- 别名词
- 手语映射
- 风险规则

## 8. 为什么这一步很重要

之前 App 查询链路已经能跑，但数据还在 Kotlin 代码里。现在改成 CSV 后，后续好处很明显：

- 非开发同学也能维护药品数据
- 公开数据清洗结果更容易导入
- 后续替换为 100 条、500 条、3000 条高频药时不需要手改 Kotlin
- 更容易切到正式 `assets/db/drugs.db`

## 9. 下一步如何接到正式 App 资产库

当前 Android 端还是优先走 `ensureSeedData()` 代码种子逻辑。

下一步建议这样改：

1. 先运行脚本生成 `app/src/main/assets/db/drugs.db`
2. 在 `DrugDatabaseProvider` 中增加：
   - 检查私有数据库目录
   - 若不存在，则从 `assets/db/drugs.db` 复制
3. 保留当前代码种子作为开发环境兜底

## 10. 数据维护建议

### 推荐维护规则

- `drug_id` 固定，不要频繁改
- 文本字段避免换行和复杂引号
- 风险文案尽量短句，方便 TTS 播报
- 手语映射先保证高频药，再逐步扩展

### 新增药品时的最少操作

1. 往 `drug_master.csv` 增加 1 行
2. 往 `drug_detail.csv` 增加 1 行
3. 视情况补 `drug_alias.csv`
4. 补 `drug_sign_mapping.csv`
5. 补 `drug_rule.csv`
6. 重新运行脚本生成 `drugs.db`

## 11. 推荐下一步

最推荐的下一步有两件：

1. 继续扩充 `data/seed` 到 20-50 个高频药
2. 把 Android 端切换为优先加载 `assets/db/drugs.db`
