# GitHub 自动提交功能文档

> 创建时间：2026-06-03
> 仓库地址：https://github.com/saber463/----
> 执行频率：每小时自动提交一次
> 文档版本：v1.0

---

## 一、功能概述

本功能实现**每间隔1小时自动提交代码到GitHub仓库**的自动化任务。通过SOLO平台的定时任务功能，结合PowerShell脚本，实现无人值守的自动化代码提交，保持仓库活跃状态。

### 核心特性
- **定时执行**：每小时自动触发一次提交
- **智能同步**：每次提交前自动拉取远程最新代码，避免冲突
- **网络重试**：网络异常时自动重试（最多3次，间隔30秒）
- **错误恢复**：推送失败时保留本地提交，支持下次自动恢复
- **完整日志**：详细记录每次执行过程和结果

---

## 二、文件结构

```
f:\java\weiguangplus\
├── ----\                          # GitHub仓库本地副本
│   ├── .gitattributes             # Git属性配置
│   ├── README.md                  # 仓库说明文档
│   └── 吉祥物.png                 # 项目吉祥物图片
│
├── auto_commit_v3.ps1             # 自动提交脚本（核心）
├── GitHub-Auto-Commit-Task.md     # 任务配置说明文档
├── GitHub自动提交功能文档.md       # 本文档（功能详细说明）
├── LAST_COMMIT.md                 # 时间戳文件（自动更新）
├── auto_commit.log                # 执行日志文件
└── 项目开发BUG排查日志.md          # BUG排查记录文档
```

---

## 三、核心脚本详解

### 3.1 脚本文件：`auto_commit_v3.ps1`

**功能定位**：自动提交的核心执行脚本，负责完整的Git操作流程。

**脚本头部信息**：
```powershell
# =============================================================================
# GitHub Auto Commit Script v3.0
# Project: Weiguang Plus
# Repo: https://github.com/saber463/----
# Features:
#   - Hourly auto commit with timestamp file update
#   - Network retry mechanism (3 retries, 30s interval)
#   - Push failure does not crash (local commit preserved)
# Author: SOLO
# Created: 2026-05-31
# Updated: 2026-06-03
# =============================================================================
```

### 3.2 配置参数

| 参数名 | 值 | 说明 |
|--------|-----|------|
| `$RepoUrl` | `https://github.com/saber463/----.git` | 远程仓库地址 |
| `$BranchName` | `main` | 目标分支名称 |
| `$LocalRepoPath` | `f:\java\weiguangplus` | 本地仓库路径 |
| `$TimestampFile` | `LAST_COMMIT.md` | 时间戳文件名 |
| `$LogFile` | `f:\java\weiguangplus\auto_commit.log` | 日志文件路径 |
| `$MaxRetries` | `3` | 网络操作最大重试次数 |
| `$RetryDelaySeconds` | `30` | 重试间隔时间（秒） |

### 3.3 核心函数

#### `Log-Message` 函数
**功能**：记录日志到控制台和日志文件
**参数**：
- `$Msg`：日志消息内容
- `$Clr`：控制台输出颜色（White/Red/Green/Yellow/Gray/Cyan）
**逻辑**：
1. 生成带时间戳的日志条目格式：`[yyyy-MM-dd HH:mm:ss] 消息内容`
2. 输出到PowerShell控制台（带颜色）
3. 追加写入日志文件（UTF-8编码）
4. 捕获写入异常，避免日志失败导致脚本中断

#### `Run-Git` 函数
**功能**：执行Git命令并支持自动重试
**参数**：
- `$Desc`：操作描述（用于日志显示）
- `$GitCmd`：Git命令字符串（不含`git`前缀）
**逻辑**：
1. 循环执行最多`$MaxRetries`次
2. 每次执行使用`Invoke-Expression`调用Git命令
3. 捕获所有输出（包括标准输出和错误输出）
4. 检查退出码`$LASTEXITCODE`：
   - `0`表示成功，立即返回`$true`
   - 非`0`表示失败，等待`$RetryDelaySeconds`秒后重试
5. 超过最大重试次数后返回`$false`

### 3.4 主执行流程

```
开始
  │
  ├─ Step 1: 检查环境
  │   └─ 验证Git是否安装（git --version）
  │
  ├─ Step 2: 验证本地仓库
  │   └─ 检查.git目录是否存在
  │   └─ 切换工作目录到本地仓库
  │
  ├─ Step 3: 同步远程代码
  │   └─ 执行 git fetch origin main
  │   └─ 比较本地HEAD和远程origin/main
  │   └─ 如果远程有新提交：
  │       ├─ 尝试 git rebase origin/main
  │       └─ 如果rebase失败：git reset --hard origin/main
  │
  ├─ Step 4: 检查Git配置
  │   └─ 验证user.name是否为"saber463"
  │   └─ 验证user.email是否为"1002668039@qq.com"
  │   └─ 配置不正确时自动修正
  │
  ├─ Step 5: 更新时间戳文件
  │   └─ 生成包含当前时间、提交次数、仓库信息的Markdown内容
  │   └─ 写入 LAST_COMMIT.md 文件（UTF-8编码）
  │
  ├─ Step 6: 提交更改
  │   └─ 执行 git add LAST_COMMIT.md
  │   └─ 检查是否有待提交更改（git status --porcelain）
  │   └─ 如果有更改：
  │       ├─ 创建提交：git commit -m "Auto commit at 时间 [skip ci]"
  │       └─ 提交失败时抛出异常
  │   └─ 如果无更改：跳过提交
  │
  ├─ Step 7: 推送到远程
  │   └─ 执行 git push origin main（带重试机制）
  │   └─ 如果推送失败：
  │       ├─ 执行 git pull --rebase origin main
  │       └─ 再次尝试推送
  │   └─ 推送成功：记录提交哈希
  │   └─ 推送失败：记录警告，保留本地提交
  │
  └─ 结束：记录下次执行时间
```

---

## 四、时间戳文件格式

### `LAST_COMMIT.md` 文件内容示例

```markdown
# Auto Commit Timestamp

> Maintained by auto-commit script v3.0

## Last Update
**2026-06-03 15:00:00**

## Repo Info
- Repo: https://github.com/saber463/----
- Branch: main
- Total commits: 42
- Status: Active

---
*Generated at 2026-06-03 15:00:00 by SOLO*
```

**设计目的**：
- 每次提交都有实际文件内容变更，确保提交有效
- 记录仓库统计信息，便于追踪
- Markdown格式清晰易读

---

## 五、定时任务配置

### 5.1 任务参数

| 参数 | 值 |
|------|-----|
| 任务名称 | GitHub Auto Commit |
| 执行频率 | 每小时（每小时的第0分钟） |
| Cron表达式 | `0 * * * *` |
| 时区 | Asia/Shanghai（北京时间，UTC+8） |
| 执行命令 | PowerShell脚本 `auto_commit_v3.ps1` |

### 5.2 Cron表达式说明

```
0 * * * *
│ │ │ │ │
│ │ │ │ └─ 星期几 (0-7, 0和7都表示周日)
│ │ │ └── 月份 (1-12)
│ │ └──── 日期 (1-31)
│ └───── 小时 (0-23)
└────── 分钟 (0-59)
```

`0 * * * *` 表示：**每小时的第0分钟执行一次**

执行时间点示例：
- 09:00:00
- 10:00:00
- 11:00:00
- ...

---

## 六、日志记录

### 6.1 日志文件位置
`f:\java\weiguangplus\auto_commit.log`

### 6.2 日志格式
```
[yyyy-MM-dd HH:mm:ss] 消息内容
```

### 6.3 日志示例
```
[2026-06-03 14:00:00] ========================================
[2026-06-03 14:00:00] Start auto commit task (v3.0)
[2026-06-03 14:00:00] Repo: https://github.com/saber463/----.git
[2026-06-03 14:00:00] Branch: main
[2026-06-03 14:00:00] Local: f:\java\weiguangplus
[2026-06-03 14:00:01] Step 1: Check environment...
[2026-06-03 14:00:01]   Git: git version 2.43.0.windows.1
[2026-06-03 14:00:01] Step 2: Validate local repo...
[2026-06-03 14:00:01]   Local repo OK
[2026-06-03 14:00:02] Step 3: Sync with remote...
[2026-06-03 14:00:02]   [1/3] git fetch...
[2026-06-03 14:00:03]   git fetch OK (attempt 1)
[2026-06-03 14:00:03]   Already up to date
[2026-06-03 14:00:03] Step 4: Check git config...
[2026-06-03 14:00:03]   User: saber463 <1002668039@qq.com>
[2026-06-03 14:00:03] Step 5: Update timestamp file...
[2026-06-03 14:00:03]   Updated: LAST_COMMIT.md
[2026-06-03 14:00:03] Step 6: Commit changes...
[2026-06-03 14:00:04]   Committed: Auto commit at 2026-06-03 14:00:03 [skip ci]
[2026-06-03 14:00:04] Step 7: Push to remote (with retry)...
[2026-06-03 14:00:04]   [1/3] git push...
[2026-06-03 14:00:05]   git push OK (attempt 1)
[2026-06-03 14:00:05]   Push OK! Hash: a1b2c3d
[2026-06-03 14:00:05] ========================================
[2026-06-03 14:00:05] Auto commit task completed!
[2026-06-03 14:00:05] Next run: 2026-06-03 15:00:05
[2026-06-03 14:00:05] ========================================
```

---

## 七、异常处理机制

### 7.1 网络异常
- **现象**：`git fetch` 或 `git push` 因网络问题失败
- **处理**：自动重试3次，每次间隔30秒
- **恢复**：重试成功后继续执行；失败后保留本地提交，下次执行时再次尝试

### 7.2 代码冲突
- **现象**：远程有新提交，本地也有修改
- **处理**：先执行 `git rebase origin/main`，如果失败则 `git reset --hard origin/main`
- **注意**：`reset --hard` 会丢弃本地未提交的修改，但本脚本只修改 `LAST_COMMIT.md`，影响可控

### 7.3 无更改提交
- **现象**：时间戳文件内容未变化（极少发生）
- **处理**：检测到无更改时跳过提交，记录日志

### 7.4 Git未安装
- **现象**：`git --version` 执行失败
- **处理**：抛出异常，记录错误日志，退出脚本

---

## 八、使用说明

### 8.1 启动定时任务
定时任务创建后，将自动按照设定频率执行，无需人工干预。

### 8.2 查看执行状态
- **查看日志**：打开 `auto_commit.log` 文件查看历史执行记录
- **查看时间戳**：打开 `LAST_COMMIT.md` 查看最新提交时间

### 8.3 暂停/恢复任务
如需暂停或恢复定时任务，请联系管理员操作。

### 8.4 手动执行
如需手动触发一次提交，可在PowerShell中执行：
```powershell
.\auto_commit_v3.ps1
```

---

## 九、安全与权限

### 9.1 Git身份配置
脚本自动确保Git用户配置正确：
- `user.name`: `saber463`
- `user.email`: `1002668039@qq.com`

### 9.2 远程访问
- 使用HTTPS协议访问GitHub
- 需要确保本地已配置GitHub凭据（Personal Access Token或密码）
- 首次推送可能需要手动输入凭据

### 9.3 提交标识
所有自动提交的信息格式为：
```
Auto commit at YYYY-MM-DD HH:MM:SS [skip ci]
```
`[skip ci]` 标记用于跳过GitHub Actions等CI/CD流程，避免不必要的构建。

---

## 十、维护与更新

### 10.1 版本历史

| 版本 | 日期 | 更新内容 |
|------|------|----------|
| v1.0 | 2026-06-03 | 初始版本，每小时自动提交功能上线 |

### 10.2 未来优化方向
- [ ] 支持多仓库同时提交
- [ ] 添加邮件/通知功能，推送失败时告警
- [ ] 支持自定义提交信息前缀
- [ ] 添加执行统计（成功率、平均耗时等）

---

## 十一、联系与支持

如有问题或需要调整配置，请联系项目管理员。

---

*文档生成时间：2026-06-03*
*维护者：SOLO*
