# GitHub 自动提交方案文档 (v3.0)

> **项目目的**: 实现每小时自动向 GitHub 仓库提交更新，保持仓库活跃
> **适用仓库**: https://github.com/saber463/----
> **创建时间**: 2026-05-31
> **最后更新**: 2026-06-03
> **作者**: 战略参谋 (SOLO)

---

## 一、方案概述

通过 PowerShell 脚本 + Windows 任务计划程序，实现每小时自动提交功能：

- **核心脚本**: `auto_commit_v3.ps1` — 执行 Git 同步、提交、推送（带网络重试）
- **定时调度**: Windows 任务计划程序 — 每小时触发一次
- **日志记录**: `auto_commit.log` — 完整的执行日志
- **时间戳文件**: `LAST_COMMIT.md` — 记录每次提交信息

---

## 二、架构设计

```
┌─────────────────────────────────────────────────┐
│           Windows 任务计划程序                     │
│         (每小时触发一次)                            │
└──────────────────┬──────────────────────────────┘
                   │ 触发
                   ▼
┌─────────────────────────────────────────────────┐
│         auto_commit_v3.ps1                       │
│                                                   │
│  Step 1: 环境检查 (Git)                           │
│  Step 2: 验证本地仓库                              │
│  Step 3: 同步远程代码 (fetch + rebase/reset)        │
│          ↑ 带重试机制 (最多3次, 间隔30秒)           │
│  Step 4: 检查 Git 用户配置                         │
│  Step 5: 更新时间戳文件 LAST_COMMIT.md             │
│  Step 6: git add + git commit                     │
│  Step 7: git push (带重试, 失败不退出)              │
│                                                   │
└──────────────────┬──────────────────────────────┘
                   │ 写入
                   ▼
┌─────────────────────────────────────────────────┐
│  auto_commit.log (执行日志)                       │
│  LAST_COMMIT.md (时间戳文件，提交到仓库)            │
└─────────────────────────────────────────────────┘
```

---

## 三、文件说明

| 文件 | 路径 | 用途 |
|------|------|------|
| 自动提交脚本 v3 | `f:\java\weiguangplus\auto_commit_v3.ps1` | 核心脚本，执行 Git 提交推送（带重试） |
| 自动提交脚本 v2 | `f:\java\weiguangplus\auto_commit_v2.ps1` | 旧版脚本（已弃用，保留备份） |
| 执行日志 | `f:\java\weiguangplus\auto_commit.log` | 记录每次执行的详细信息 |
| 时间戳文件 | `f:\java\weiguangplus\LAST_COMMIT.md` | 提交到仓库，记录最后提交时间 |
| 本地仓库 | `f:\java\weiguangplus\` | 工作目录，直接在此操作 |

---

## 四、定时任务配置

| 配置项 | 值 |
|--------|-----|
| 任务名称 | `WeiguangPlus_AutoCommit` |
| 执行频率 | 每 60 分钟一次 |
| 持续时间 | 无限期 |
| 运行用户 | `Fenis`（当前用户） |
| 电池模式 | ✅ 电池供电时也运行 |
| 错过执行 | ✅ 网络可用时立即补执行 |
| 执行超时 | 10 分钟 |

---

## 五、脚本功能详解 (auto_commit_v3.ps1)

### 5.1 配置参数

```powershell
$RepoUrl = "https://github.com/saber463/----.git"     # 远程仓库地址
$BranchName = "main"                                    # 目标分支
$LocalRepoPath = "f:\java\weiguangplus"                # 本地仓库路径
$TimestampFile = "LAST_COMMIT.md"                      # 时间戳文件名
$LogFile = "f:\java\weiguangplus\auto_commit.log"      # 日志文件路径
$MaxRetries = 3                                          # 网络操作最大重试次数
$RetryDelaySeconds = 30                                  # 每次重试间隔（秒）
```

### 5.2 执行流程

1. **环境检查**: 验证 Git 是否可用
2. **仓库验证**: 确认本地是有效的 Git 仓库
3. **远程同步**: fetch + rebase（失败则 reset），确保本地与远程一致（带重试）
4. **用户配置**: 确认 Git 用户名和邮箱正确
5. **时间戳更新**: 生成新的 `LAST_COMMIT.md` 文件（确保每次都有变更）
6. **提交推送**: git add → git commit → git push（带重试，失败不退出）

### 5.3 容错机制

- **网络重试**: fetch 和 push 操作失败后自动重试，最多 3 次，每次间隔 30 秒
- **远程同步降级**: rebase 失败时自动降级为 `git reset --hard`
- **推送失败保护**: push 彻底失败后不会抛异常退出，本地 commit 保留，下次运行时自动推送
- **完整日志**: 所有操作都有日志记录，便于排查问题

### 5.4 核心函数说明

- `Log-Message`: 自定义日志函数，同时输出到控制台和日志文件
- `Run-Git`: 带重试的 Git 命令执行函数，使用 `Invoke-Expression` 执行 git 命令字符串

---

## 六、版本迭代记录

### v3.0 (2026-06-03) 相比 v2.0 的改进

| 改进点 | v2.0 | v3.0 |
|--------|------|------|
| 网络重试 | push 失败只重试 1 次 | fetch/push 均支持最多 3 次重试，间隔 30 秒 |
| 推送失败行为 | 抛异常退出（exit 1） | 记录警告，本地 commit 保留，不退出（exit 0） |
| Git 命令执行 | 直接调用 git | 通过 `Run-Git` 函数统一管理，支持重试 |
| 脚本大小 | 220 行（含大量注释） | 173 行（更精简） |

### v2.0 (2026-05-31) 相比 v1.0 的改进

| 问题 | v1.0 | v2.0 |
|------|------|------|
| 仓库路径 | 使用临时目录 `$env:TEMP` | 直接使用工作目录 `f:\java\weiguangplus` |
| 远程同步 | 只用 `git pull`，冲突时失败 | fetch + rebase，失败降级为 reset |
| 日志函数 | 与系统 `Write-Log` 冲突 | 自定义 `Log-Message` 函数 |
| 时间戳文件 | 简单文本文件 | Markdown 格式，信息更丰富 |
| 推送失败 | 直接报错退出 | 自动 pull 后重试 |
| 电池模式 | 未配置 | 电池供电时也运行 |

---

## 七、任务管理命令

```powershell
# 查看任务状态
Get-ScheduledTask -TaskName "WeiguangPlus_AutoCommit"

# 查看任务详细信息
Get-ScheduledTask -TaskName "WeiguangPlus_AutoCommit" | Get-ScheduledTaskInfo

# 立即手动运行一次
Start-ScheduledTask -TaskName "WeiguangPlus_AutoCommit"

# 停止任务
Stop-ScheduledTask -TaskName "WeiguangPlus_AutoCommit"

# 禁用任务
Disable-ScheduledTask -TaskName "WeiguangPlus_AutoCommit"

# 启用任务
Enable-ScheduledTask -TaskName "WeiguangPlus_AutoCommit"

# 删除任务
Unregister-ScheduledTask -TaskName "WeiguangPlus_AutoCommit" -Confirm:$false

# 查看执行日志（最近50行）
Get-Content "f:\java\weiguangplus\auto_commit.log" -Tail 50
```

---

## 八、手动测试

```powershell
# 手动执行一次 v3.0 脚本（验证脚本是否正常）
PowerShell -ExecutionPolicy Bypass -NoProfile -File "f:\java\weiguangplus\auto_commit_v3.ps1"
```

---

## 九、常见问题排查

### Q1: 提示 "Access is denied"
- 创建/修改定时任务需要管理员权限
- 右键 PowerShell → 以管理员身份运行

### Q2: 推送失败
- 检查网络连接
- 确认 Git 凭证是否有效：`git push --dry-run`
- 查看日志文件获取详细错误
- v3.0 会自动重试 3 次，如果仍然失败，本地 commit 会保留

### Q3: 定时任务不执行
- 检查任务状态：`Get-ScheduledTask -TaskName "WeiguangPlus_AutoCommit"`
- 确认电脑没有休眠
- 检查 Windows 事件查看器中的任务计划程序日志

### Q4: 日志文件不存在
- 脚本首次运行时会自动创建
- 如果目录权限不足，检查 `f:\java\weiguangplus` 的写入权限

### Q5: 上次运行失败但这次成功了
- v3.0 的设计就是"失败不退出"，本地 commit 会在下次成功时自动推送
- 这是正常行为，说明重试机制正在工作

---

**文档版本**: v3.0
**最后更新**: 2026-06-03
