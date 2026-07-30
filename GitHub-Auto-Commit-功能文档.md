# GitHub 仓库自动提交功能文档

> 创建时间：2026-06-04 20:15 (北京时间 UTC+8)
> 作者：SOLO 助手
> 仓库地址：https://github.com/saber463/----

---

## 一、功能概述

为 GitHub 仓库 `saber463/----`（微光畅行项目）配置**每小时自动提交**功能，保持仓库持续活跃。

### 核心逻辑
1. 每小时整点自动执行 PowerShell 脚本
2. 脚本进入本地克隆的仓库目录
3. 拉取远程最新代码（避免冲突）
4. 在 `auto-commits/` 目录下生成/追加时间戳记录
5. 执行 `git add -A` → `git commit` → `git push`

---

## 二、文件清单

| 文件 | 路径 | 说明 |
|------|------|------|
| 自动提交脚本 | `c:\Users\Fenis\.trae-cn\work\6a2168cd3bf2d4aa8e454399\auto-commit.ps1` | PowerShell 脚本，执行自动提交的核心逻辑 |
| 克隆仓库 | `c:\Users\Fenis\.trae-cn\work\6a2168cd3bf2d4aa8e454399\auto-commit-repo` | 从 GitHub 克隆的完整仓库副本 |
| 时间戳日志 | `auto-commit-repo/auto-commits/auto-commit-log-YYYY-MM-DD.md` | 每日自动提交记录文件 |

---

## 三、脚本详细说明

### auto-commit.ps1

```
功能：每小时自动向 GitHub 仓库提交时间戳记录
执行方式：通过 Windows 任务计划程序定时调用
```

#### 执行流程
```
开始
  │
  ├─ 1. 切换到仓库目录
  │     路径: c:\Users\Fenis\.trae-cn\work\6a2168cd3bf2d4aa8e454399\auto-commit-repo
  │
  ├─ 2. 拉取远程最新代码 (git pull)
  │     优先尝试 main 分支，失败则尝试 master 分支
  │
  ├─ 3. 生成北京时间时间戳
  │     格式: yyyy-MM-dd HH:mm:ss
  │
  ├─ 4. 创建/追加日志文件
  │     路径: auto-commits/auto-commit-log-{日期}.md
  │     内容: 提交时间、用户、仓库地址等信息
  │
  ├─ 5. 检查是否有变更 (git status --porcelain)
  │     ├─ 有变更 → git add -A → git commit → git push
  │     └─ 无变更 → 跳过本次操作
  │
  └─ 结束
```

#### 关键参数
- **提交信息格式**: `auto: hourly commit - {时间戳}`
- **日志文件编码**: UTF-8
- **分支策略**: 优先 main，回退 master

---

## 四、定时任务配置

### Windows 任务计划程序

| 配置项 | 值 |
|--------|-----|
| 任务名称 | `GitHubAutoCommitHourly` |
| 触发器 | 每小时执行一次 |
| 执行命令 | `powershell.exe -ExecutionPolicy Bypass -File "c:\Users\Fenis\.trae-cn\work\6a2168cd3bf2d4aa8e454399\auto-commit.ps1"` |
| 运行用户 | Fenis（当前登录用户） |
| 状态 | 已启用 (Enabled) |
| 首次运行 | 2026-06-04 21:35:00 |

### 管理命令

```powershell
# 查看任务状态
schtasks /query /tn "GitHubAutoCommitHourly" /v /fo list

# 手动触发一次执行
schtasks /run /tn "GitHubAutoCommitHourly"

# 删除定时任务
schtasks /delete /tn "GitHubAutoCommitHourly" /f

# 禁用定时任务
schtasks /change /tn "GitHubAutoCommitHourly" /disable

# 启用定时任务
schtasks /change /tn "GitHubAutoCommitHourly" /enable
```

---

## 五、验证结果

### 首次手动执行
- **执行时间**: 2026-06-04 20:15:00 (北京时间)
- **执行结果**: ✅ 成功
- **提交哈希**: `00fbff5`
- **提交信息**: `auto: hourly commit - 2026-06-04 20:15:00`
- **变更文件**: `auto-commits/auto-commit-log-2026-06-04.md` (新建, 11 行)

---

## 六、注意事项

1. **电脑必须开机且登录**：Windows 任务计划程序在交互模式下运行，需要用户 Fenis 已登录
2. **网络连接**：需要能正常访问 GitHub（可能需要代理或 VPN）
3. **Git 认证**：依赖已配置的 Git 凭据（当前用户 saber463 已配置）
4. **日志累积**：每天的提交记录会追加到同一个日期文件中，不会产生过多文件
5. **冲突处理**：脚本每次执行前会先 pull，但如果远程有其他人推送了冲突文件，可能需要手动解决

---

## 七、故障排查

| 问题 | 可能原因 | 解决方案 |
|------|----------|----------|
| push 失败 | 网络不通/认证过期 | 检查网络，重新配置 Git 凭据 |
| 无变更跳过 | 同一小时重复执行 | 正常行为，脚本设计如此 |
| 脚本报错 | 路径不存在/权限不足 | 确认仓库路径存在，检查文件权限 |
| 任务未执行 | 电脑关机/未登录 | 确保电脑开机且用户已登录桌面 |
