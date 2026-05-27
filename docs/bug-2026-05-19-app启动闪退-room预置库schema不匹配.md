# 2026-05-19 App 启动闪退：Room 预置数据库 Schema 不匹配

## 1. 问题现象

- 手机点击桌面图标冷启动后立刻闪退。
- 典型崩溃堆栈（节选）：

```text
java.lang.IllegalStateException: Pre-packaged database has an invalid schema: drug_master(...)
Expected: ... drug_id ... notNull=true ... created_at defaultValue='undefined'
Found:    ... drug_id ... notNull=false ... created_at defaultValue='strftime('%s','now')'
at androidx.room.RoomOpenHelper.onCreate(RoomOpenHelper.kt:73)
...
```

## 2. 影响范围

- 只要走到 Room 打开数据库（尤其是启动阶段 `ensureSeedData` / 任意 DAO 查询）就会触发。
- 属于“启动即崩”的高优先级问题。

## 3. 根因分析（为什么会出现）

Room 在使用 `createFromAsset("db/drugs.db")` 时，会对预置数据库的表结构做严格校验（包括：
- 列是否 `NOT NULL`
- 默认值 `DEFAULT` 是否一致
- 索引、主键等结构是否一致）

本次崩溃的根因是：

- 预置离线库 `app/src/main/assets/db/drugs.db` 中 `drug_master` 表结构与 App 侧 `DrugMasterEntity`（以及 `AppDatabase version=4` 的期望结构）不一致：
  - `drug_id` 在预置库里未被 Room 识别为 `NOT NULL`（或未显式声明导致 PRAGMA 结果不同）
  - `created_at/updated_at` 在预置库里声明了 `DEFAULT strftime('%s','now')`，而实体未声明列默认值，Room 期望默认值为 `undefined`
- Room 校验失败后抛出 `IllegalStateException`，导致进程崩溃。

## 4. 修复方案（怎么修复）

### 4.1 统一 Schema：去掉 SQL 默认值，改由构建脚本填充时间戳

- 修改 `docs/sql/drug_offline_schema.sql`：
  - 移除各表中与实体不一致的 `DEFAULT ...` 声明（包括 `strftime('%s','now')`、`'alias'`、`'word'`、`1` 等）
  - 保持列类型、`NOT NULL`、索引与 Room Entity 一致

### 4.2 修复种子构建脚本：补齐 `drug_master` 的 created_at/updated_at

- 修改 `tools/build_seed_db.py`：
  - `drug_master.csv` 不包含 `created_at/updated_at` 两列
  - 由于移除了 SQL `DEFAULT`，构建时需要脚本为每行补齐 `created_at/updated_at`（使用当前时间戳毫秒）

### 4.3 重新生成预置数据库

执行：

```bash
python tools/build_seed_db.py
```

确保生成的 `app/src/main/assets/db/drugs.db` 与 Room 期望一致。

### 4.4 运行时兜底（防再次启动即崩）

- 修改 `DrugDatabaseProvider`：
  - 若捕获到 `Pre-packaged database has an invalid schema`，自动切换为“不使用 assets 预置库”的构建方式，并删除旧库后重建，保证至少能进入 App（使用代码 seed 数据兜底）。
  - 将兜底逻辑放在数据库构建/打开阶段（`buildDatabase` 强制触发 open 并捕获），避免 `getRepository()` 在 `ensureSeedData()` 之前就先打开数据库而直接崩溃。

### 4.5 启动防崩（页面层兜底）

- 修改 `MainActivity` 的 Compose 启动链路：
  - 初始化 `repository/ensureSeedData` 失败时不再抛异常导致闪退
  - 展示“启动失败”页面并提供“重置药品库重试”按钮，确保启动过程具备自愈能力（便于真机演示与现场排障）

## 5. 验证方式（如何确认修好了）

- 真机安装新包，冷启动 App：
  - 不再闪退
  - 能正常进入首屏
- 若需要二次确认，可通过 Room 查询任意表（例如进入“药品”页面）确保数据库可正常打开和查询。

## 6. 本次修改文件清单

- `docs/sql/drug_offline_schema.sql`
- `tools/build_seed_db.py`
- `app/src/main/assets/db/drugs.db`
- `app/src/main/java/com/weiguangchangxing/weiguang_plus/data/local/DrugDatabaseProvider.kt`
- `app/src/main/java/com/weiguangchangxing/weiguang_plus/MainActivity.kt`
