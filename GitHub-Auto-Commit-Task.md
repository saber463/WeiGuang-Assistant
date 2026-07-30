# GitHub 自动提交定时任务方案

> 创建时间：2026-06-03
> 仓库地址：https://github.com/saber463/----
> 执行频率：每小时自动提交一次
> 任务状态：✅ 已激活

---

## 任务概述

本任务用于实现**每间隔1小时自动提交代码到GitHub仓库**的功能。通过Windows任务计划程序，实现无人值守的自动化代码提交。

---

## 任务参数

| 参数 | 值 | 说明 |
|------|-----|------|
| 任务名称 | GitHub-Auto-Commit | Windows任务计划程序中的任务名 |
| 仓库地址 | https://github.com/saber463/---- | 目标GitHub仓库 |
| 执行频率 | 每小时 | 每小时的第0分钟执行 |
| 时区 | 本地系统时区 | 跟随Windows系统时间 |
| 执行命令 | powershell.exe -ExecutionPolicy Bypass -File f:\java\weiguangplus\auto_commit_v3.ps1 | PowerShell脚本 |
| 下次执行 | 2026/6/3 23:00:00 | 下次触发时间 |
| 任务状态 | Ready / Enabled | 已启用，等待执行 |

---

## 执行操作

每次定时任务触发时，将执行以下Git操作：

1. **git fetch** - 拉取远程最新代码
2. **git rebase/reset** - 同步远程更新（如有）
3. **更新时间戳文件** - 更新 LAST_COMMIT.md 文件内容
4. **git add** - 添加更改的文件
5. **git commit** - 创建带时间戳的提交记录
6. **git push** - 推送到GitHub远程仓库

---

## 提交信息格式

```
Auto commit at 2026-06-03 22:31:21 [skip ci]
```

时间戳精确到秒，便于追踪每次自动提交的时间。`[skip ci]` 标记用于跳过GitHub Actions等CI/CD流程。

---

## 前置条件

确保本地Git配置已正确设置：

- GitHub访问权限（已配置Personal Access Token或SSH Key）
- 仓库已正确配置remote（origin指向https://github.com/saber463/----.git）
- 当前工作目录为仓库根目录（f:\java\weiguangplus）
- PowerShell执行策略允许运行脚本（-ExecutionPolicy Bypass）

---

## 任务管理

### 查看任务状态
```cmd
schtasks /query /tn "GitHub-Auto-Commit" /fo LIST /v
```

### 暂停任务
```cmd
schtasks /change /tn "GitHub-Auto-Commit" /disable
```

### 恢复任务
```cmd
schtasks /change /tn "GitHub-Auto-Commit" /enable
```

### 删除任务
```cmd
schtasks /delete /tn "GitHub-Auto-Commit" /f
```

### 手动立即执行
```cmd
schtasks /run /tn "GitHub-Auto-Commit"
```

---

## 日志查看

- **脚本执行日志**：`f:\java\weiguangplus\auto_commit.log`
- **时间戳文件**：`f:\java\weiguangplus\LAST_COMMIT.md`
- **Windows事件查看器**：任务计划程序相关事件

---

## 注意事项

1. 如果某次检查时没有文件更改，git commit会跳过（时间戳文件每次都会更新，通常不会出现此情况）
2. 建议配合.gitignore排除不需要提交的大文件或敏感文件
3. 频繁提交可能产生大量提交记录，请根据实际需求调整
4. 任务在电池模式下不会启动（可修改电源管理设置）
5. 单次任务最长运行72小时，超过会被强制停止

---

## 变更记录

| 日期 | 操作 | 说明 |
|------|------|------|
| 2026-06-03 | 创建任务 | 初始配置，每小时自动提交 |
| 2026-06-03 | 首次执行 | 手动执行成功，提交Hash: 0cc2451 |

---

## 相关文件

| 文件 | 说明 |
|------|------|
| auto_commit_v3.ps1 | 自动提交核心脚本 |
| LAST_COMMIT.md | 时间戳记录文件（自动更新） |
| auto_commit.log | 执行日志文件 |
| GitHub自动提交功能文档.md | 功能详细说明文档 |
