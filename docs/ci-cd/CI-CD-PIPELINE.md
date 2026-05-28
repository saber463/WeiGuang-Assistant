# 微光同行(WeiguangPlus) - CI/CD自动化流水线完整使用文档

> **文档版本**: v1.0.0
> **最后更新**: 2026-05-28
> **维护者**: @saber463 (微光同行DevOps团队)
> **适用项目**: 微光同行多Agent并行开发项目

---

## 📖 目录

1. [概述与架构](#1-概述与架构)
2. [流水线架构图](#2-流水线架构图)
3. [Workflow详细说明](#3-workflow详细说明)
   - [3.1 后端CI流水线 (ci-backend.yml)](#31-后端ci流水线-ci-backendyml)
   - [3.2 Android前端CI流水线 (ci-android.yml)](#32-android前端ci流水线-ci-androidyml)
   - [3.3 E2E集成测试流水线 (e2e-test.yml)](#33-e2e集成测试流水线-e2e-testyml)
   - [3.4 预发布部署流水线 (deploy-staging.yml)](#34-预发布部署流水线-deploy-stagingyml)
4. [环境变量与Secrets配置](#4-环境变量与secrets配置)
5. [Badge状态徽章配置](#5-badge状态徽章配置)
6. [故障排查指南](#6-故障排查指南)
7. [性能优化建议](#7-性能优化建议)
8. [安全最佳实践](#8-安全最佳实践)
9. [本地调试CI脚本（act工具）](#9-本地调试ci脚本act工具)
10. [附录与参考资源](#10-附录与参考资源)

---

## 1. 概述与架构

### 1.1 项目背景

**微光同行(WeiguangPlus)** 是一个面向视障人士的无障碍辅助Android应用，采用**多Agent并行开发模式**进行协作开发。本CI/CD体系为该项目提供完整的自动化构建、测试、部署能力。

### 1.2 技术栈概览

| 模块 | 技术栈 | 职责 |
|------|--------|------|
| **后端API** | Python 3.11 + FastAPI + SQLAlchemy | RESTful API服务、业务逻辑、数据持久化 |
| **前端App** | Kotlin 1.9.0 + Jetpack Compose + AGP 8.2.0 | Android原生应用、UI交互、设备功能调用 |
| **算法模块** | Python + MediaPipe + ML Kit | 手语识别、药品OCR、物体检测 |
| **测试框架** | pytest + JUnit 4 + Allure | 单元测试、集成测试、E2E测试 |

### 1.3 分支策略

```
main (生产分支)
  ├── feature/backend     → 后端功能开发分支
  ├── feature/frontend    → Android前端功能开发分支
  ├── feature/algorithm   → 算法模块开发分支
  └── feature/test        → 测试用例开发分支
```

### 1.4 CI/CD核心价值

- ✅ **自动化质量门禁**：每次提交自动运行lint、test、security scan
- ✅ **快速反馈循环**：并行Job执行，平均10分钟内完成全部检查
- ✅ **标准化流程**：统一的Issue/PR模板确保信息完整
- ✅ **安全部署**：签名APK构建、Docker镜像推送、SSH远程部署
- ✅ **可追溯性**：完整的构建日志、测试报告、部署记录

---

## 2. 流水线架构图

### 2.1 整体CI/CD流程图

```mermaid
flowchart TD
    subgraph Developer["👨‍💻 开发者操作"]
        A[代码提交 Push / PR创建]
        B[Git Tag推送 v*.*.*]
        C[手动触发 workflow_dispatch]
    end

    subgraph CI_Pipeline["⚙️ CI 流水线"]
        direction TB
        subgraph Backend_CI["🐍 后端CI (ci-backend.yml)"]
            B1[lint-python<br/>Flake8+Black检查]
            B2[test-backend<br/>Pytest单元测试]
            B3[security-scan<br/>Bandit安全扫描]
            B4[type-check<br/>Mypy类型检查]
        end

        subgraph Android_CI["📱 Android CI (ci-android.yml)"]
            A1[build-debug<br/>Debug APK编译]
            A2[unit-test<br/>JUnit单元测试]
            A3[lint-kotlin<br/>KotlinLint检查]
            A4[code-quality<br/>Android Lint分析]
            A5[release-build<br/>Release APK签名]
        end
    end

    subgraph CD_Pipeline["🚀 CD 部署流水线"]
        direction TB
        subgraph E2E_Test["🧪 E2E测试 (e2e-test.yml)"]
            E1[Docker Compose启动<br/>PostgreSQL+MinIO+Backend]
            E2[E2E测试执行<br/>pytest/Newman]
            E3[Allure报告生成]
            E4[环境清理]
        end

        subgraph Deploy["🎯 Staging部署 (deploy-staging.yml)"]
            D1[Release APK构建<br/>正式签名]
            D2[Docker镜像构建<br/>多平台支持]
            D3[镜像推送至GHCR]
            D4[SSH远程部署<br/>Staging服务器]
            D5[Slack/Webhook通知]
        end
    end

    subgraph Outputs["📦 输出产物"]
        O1[APK Artifact<br/>GitHub Releases]
        O2[Docker Image<br/>GHCR Registry]
        O3[Test Reports<br/>Allure HTML]
        O4[Security Scan<br/>Bandit Report]
    end

    %% 触发关系
    A -->|Push to feature/backend or main| Backend_CI
    A -->|Push to feature/frontend or main| Android_CI
    C --> E2E_Test
    A -->|Merge to main| E2E_Test
    B --> Deploy

    %% 内部依赖
    B2 -.->|needs| B1
    A5 -.->|needs| A1 & A2
    E2 -.->|needs| E1
    D1 & D2 --> D4
    D4 --> D5

    %% 输出关系
    A1 --> O1
    A5 --> O1
    D1 --> O1
    D2 --> O2
    B2 --> O3
    E3 --> O3
    B3 --> O4

    %% 样式定义
    classDef trigger fill:#e1f5fe,stroke:#0288d1,stroke-width:2px
    classDef job fill:#fff3e0,stroke:#f57c00,stroke-width:2px
    classDef output fill:#e8f5e9,stroke:#388e3c,stroke-width:2px
    class Developer,A,B,C trigger
    class B1,B2,B3,B4,A1,A2,A3,A4,A5,E1,E2,E3,E4,D1,D2,D3,D4,D5 job
    class O1,O2,O3,O4 output
```

### 2.2 Job并行执行时序图

```mermaid
sequenceDiagram
    participant Dev as 开发者
    participant GH as GitHub
    participant Runner as CI Runner

    Dev->>GH: Push代码 / 创建PR
    GH->>Runner: 触发CI流水线

    par 后端CI并行执行
        Runner->>Runner: Job1: lint-python (flake8+black)
        Runner->>Runner: Job2: test-backend (pytest)
        Runner->>Runner: Job3: security-scan (bandit)
        Runner->>Runner: Job4: type-check (mypy)
    and Android CI并行执行
        Runner->>Runner: Job1: build-debug (assembleDebug)
        Runner->>Runner: Job2: unit-test (testDebugUnitTest)
        Runner->>Runner: Job3: lint-kotlin (ktlintCheck)
        Runner->>Runner: Job4: code-quality (lintDebug)
    end

    Runner-->>GH: 返回所有Job结果

    alt 全部通过
        GH-->>Dev: ✅ PR可合并 / 绿色Check
    else 存在失败
        GH-->>Dev: ❌ PR需修复 / 红色Check + 失败详情
    end
```

---

## 3. Workflow详细说明

### 3.1 后端CI流水线 (ci-backend.yml)

#### 📋 基本信息

| 属性 | 值 |
|------|-----|
| **文件路径** | `.github/workflows/ci-backend.yml` |
| **流水线名称** | `Backend CI Pipeline` |
| **运行环境** | `ubuntu-latest` |
| **超时时间** | lint:10min / test:20min / security:15min / typecheck:15min |
| **Python版本** | 3.11 |

#### 🎯 触发条件

| 触发方式 | 条件 | 说明 |
|----------|------|------|
| **Push** | `main`, `feature/backend` 分支 | 推送代码后自动运行 |
| **Pull Request** | 向 `main`, `feature/backend` 提交PR | PR创建或更新时运行 |
| **手动触发** | `workflow_dispatch` | 在Actions页面点击Run workflow |

#### 🔧 包含的Job

##### Job 1: lint-python (🐍 Lint & Format Check)

**目的**: 确保Python代码符合PEP8规范和团队格式标准

**执行步骤**:
1. 检出代码 (`actions/checkout@v4`)
2. 配置Python 3.11环境 (`actions/setup-python@v5`)
3. 安装lint工具: `flake8`, `black`, `isort`
4. 执行 **Flake8** 检查:
   ```bash
   flake8 backend/ --max-line-length=120 --max-complexity=15 --ignore=E501,W503
   ```
5. 执行 **Black** 格式检查:
   ```bash
   black --check --line-length=120 backend/
   ```
6. 执行 **isort** 导入排序检查:
   ```bash
   isort --check-only --profile black backend/
   ```

**常见问题及解决**:
- ❌ Black格式不通过 → 本地运行 `black .` 自动修复后再提交
- ❌ Flake8报错 → 根据错误码调整代码（如E501行过长需换行）

---

##### Job 2: test-backend (🧪 Unit Tests & Coverage)

**目的**: 运行所有单元测试并统计代码覆盖率

**执行步骤**:
1. 配置Python环境并启用pip缓存
2. 安装依赖: `requirements.txt` + `requirements-dev.txt`
3. 安装测试工具: `pytest`, `pytest-cov`, `pytest-xdist`
4. 执行 **Pytest** 测试套件:
   ```bash
   pytest tests/ \
     -v \
     --cov=app \
     --cov-report=xml:coverage.xml \
     --cov-report=term-missing \
     --cov-fail-under=70 \
     --junitxml=junit-test-results.xml
   ```
5. 上传测试结果Artifact (保留7天)
6. 生成Coverage Summary展示在Actions页面

**输出产物**:
- `junit-test-results.xml`: JUnit格式测试结果
- `coverage.xml`: Cobertura格式覆盖率数据
- Artifact名称: `test-results-backend`

**覆盖率阈值**: 当前设置为70%，可根据项目成熟度逐步提升至80%+

---

##### Job 3: security-scan (🔒 Security Scan)

**目的**: 使用Bandit检测Python代码中的安全漏洞

**执行步骤**:
1. 安装Bandit: `pip install bandit bandit-html-reporter`
2. 执行安全扫描:
   ```bash
   bandit -r backend/ -ll -f json -o bandit-report.json -x tests/
   ```
3. 上传安全扫描报告 (保留14天)

**检测的安全问题类型**:
| 问题代码 | 描述 | 严重程度 |
|----------|------|----------|
| B101 | 生产代码使用assert | Low |
| B102 | exec()使用 | High |
| B105 | 硬编码密码字符串 | High |
| B108 | 硬编码临时目录 | Medium |
| B201 | Flask debug=True开启 | High |
| B601 | SQL注入风险（参数化查询） | High |
| B602 | 子进程命令注入 | High |

---

##### Job 4: type-check (🔎 Type Checking)

**目的**: 使用Mypy验证Python类型注解的正确性

**执行步骤**:
1. 安装Mypy和类型存根: `mypy`, `types-requests`, `types-PyYAML`
2. 执行类型检查:
   ```bash
   mypy backend/app/ \
     --ignore-missing-imports \
     --show-error-codes \
     --show-error-context \
     --warn-return-any
   ```
3. 上传.mypy_cache (保留3天，用于增量分析)

#### 📊 查看结果

1. 进入仓库 → **Actions** 标签页
2. 点击左侧 **"Backend CI Pipeline"** 工作流
3. 选择具体的Run查看详情
4. 每个Job的结果以彩色图标显示:
   - ✅ 绿色勾: 通过
   - ❌ 红色叉: 失败（点击查看详细日志）
   - ⚠️ 黄色警告: 有警告但未阻断

---

### 3.2 Android前端CI流水线 (ci-android.yml)

#### 📋 基本信息

| 属性 | 值 |
|------|-----|
| **文件路径** | `.github/workflows/ci-android.yml` |
| **流水线名称** | `Android CI Pipeline` |
| **运行环境** | `ubuntu-latest` |
| **JDK版本** | 17 (Temurin) |
| **Gradle版本** | 8.2 (Wrapper管理) |
| **目标SDK** | 36 (Android 15) |
| **最低SDK** | 26 (Android 8.0) |

#### 🎯 触发条件

| 触发方式 | 条件 | 说明 |
|----------|------|------|
| **Push** | `main`, `feature/frontend` 分支 | Android相关代码变更时触发 |
| **Pull Request** | 向上述分支提交PR | PR审查时自动验证 |
| **手动触发** | `workflow_dispatch` | 按需重新构建 |

#### 🔧 包含的Job

##### Job 1: build-debug (📱 Build Debug APK)

**目的**: 编译生成Debug版本APK，验证项目可正常构建

**关键步骤**:
1. 配置JDK 17 + Gradle缓存
2. 执行Gradle构建:
   ```bash
   ./gradlew assembleDebug --stacktrace --info
   ```
3. 上传APK Artifact:
   - 命名格式: `weiguangplus-debug-{branch}-{commit-sha}.apk`
   - 保留时间: 14天
   - 下载位置: Actions → Run → Artifacts

**输出位置**: `app/build/outputs/apk/debug/app-debug.apk`

**构建参数优化**:
```properties
GRADLE_OPTS=-Dorg.gradle.daemon=false -Dorg.gradle.parallel=true -Dorg.gradle.caching=true
```

---

##### Job 2: unit-test (🧪 Run Unit Tests)

**目的**: 运行本地单元测试（非Instrumented Test，无需真机/模拟器）

**执行命令**:
```bash
./gradlew testDebugUnitTest --continue --stacktrace
```

**测试范围**: `app/src/test/` 目录下的所有JUnit 4测试

**输出产物**:
- XML格式的测试结果: `**/build/test-results/testDebugUnitTest/`
- Artifact名称: `unit-test-results`
- 保留时间: 7天

**注意事项**:
- `--continue` 参数确保即使某个模块测试失败也继续执行其他模块
- 如果项目中使用了Mockito/MockK，确保已添加为testImplementation依赖

---

##### Job 3: lint-kotlin (🎨 Kotlin Lint Check)

**目的**: 检查Kotlin代码风格和格式规范

**执行逻辑**:
1. 尝试使用Gradle任务 `ktlintCheck`（如果项目配置了ktlint插件）
2. 如果未配置ktlint插件，回退到内置的 `lintDebug` 任务

**输出产物**:
- HTML报告: `app/build/reports/lint-results-debug.html`
- XML报告: `app/build/reports/lint-results-debug.xml`
- Artifact名称: `lint-reports-android`

**如何添加ktlint支持** (可选):
```kotlin
// build.gradle.kts 中添加
plugins {
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
}
```

---

##### Job 4: code-quality (🔍 Android Lint Analysis)

**目的**: 使用Android官方Lint工具进行全面静态分析

**分析维度**:

| 类别 | 检查内容 | 示例 |
|------|----------|------|
| **Correctness** | 代码正确性 | 空指针、类型转换异常 |
| **Security** | 安全问题 | WebView远程代码执行、加密API误用 |
| **Performance** | 性能问题 | 内存泄漏、过度绘制、主线程IO |
| **Usability** | 可用性 | 无障碍标签缺失、触摸目标过小 |
| **Internationalization** | 国际化 | 硬编码中文字符串 |

**执行命令**:
```bash
./gradlew lintDebug --stacktrace
```

**注意**: 项目当前配置了 `lintOptions.abortOnError=false`，Lint警告不会阻断构建，但会生成报告供人工审核。

---

##### Job 5: release-build (🚀 Build Release APK) 【可选】

**目的**: 合并到main分支时构建正式签名的Release APK

**前置条件** (需配置Secrets):
| Secret名称 | 说明 | 获取方式 |
|------------|------|----------|
| `ANDROID_KEYSTORE_BASE64` | Base64编码的JKS密钥库 | `base64 -w 0 release.jks` |
| `KEYSTORE_PASSWORD` | Keystore密码 | 项目安全管理员提供 |
| `KEY_ALIAS` | 密钥别名 | 通常为 `key0` 或自定义 |
| `KEY_PASSWORD` | 密钥密码 | 与Keystore密码相同或不同 |

**输出**:
- Release APK上传至GitHub Releases (Draft状态)
- 需要人工审核后手动Publish

#### 📊 缓存策略详解

Android CI使用**三级缓存**加速构建：

```yaml
# Level 1: actions/setup-java 的 cache: gradle
# 自动缓存 ~/.gradle/wrapper 和 ~/.gradle/caches

# Level 2: actions/cache@v4 手动缓存
- uses: actions/cache@v4
  with:
    path: |
      ~/.gradle/caches      # 依赖下载缓存
      ~/.gradle/wrapper     # Gradle Wrapper分发
      ~/.android/build-cache # Android构建缓存
    key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*') }}
    restore-keys: |
      ${{ runner.os }}-gradle-

# Level 3: Gradle自身的Build Cache (-Dorg.gradle.caching=true)
# 任务级增量编译缓存
```

**首次构建**: ~8-10分钟（下载依赖+全量编译）
**后续构建**: ~3-5分钟（缓存命中+增量编译）

---

### 3.3 E2E集成测试流水线 (e2e-test.yml)

#### 📋 基本信息

| 属性 | 值 |
|------|-----|
| **文件路径** | `.github/workflows/e2e-test.yml` |
| **流水线名称** | `E2E Integration Tests` |
| **运行环境** | `ubuntu-latest` |
| **总超时** | Setup:15min / Tests:45min / Cleanup:5min |
| **Docker编排** | `docker-compose.ci.yml` |

#### 🎯 触发条件

| 触发方式 | 条件 | 说明 |
|----------|------|------|
| **手动触发** | `workflow_dispatch` | 支持选择测试套件和环境 |
| **Push到main** | `main` 分支 | 代码合并后自动回归 |
| **PR合并** | PR closed && merged | 合并后补充验证 |

#### 🎛️ 手动触发参数

当通过 `workflow_dispatch` 手动触发时可选择以下参数：

| 参数名 | 类型 | 选项 | 默认值 | 说明 |
|--------|------|------|--------|------|
| `test_suite` | Choice | all/api-only/db-only/smoke | all | 测试范围选择 |
| `environment` | Choice | staging/production | staging | 目标测试环境 |

#### 🔧 Job结构

##### Job 1: setup-infrastructure (🏗️ Setup Test Infrastructure)

**职责**: 启动完整的Docker服务栈并等待健康检查

**启动的服务**:
1. **PostgreSQL 15-alpine** (端口5432)
   - 用户: `weiguanguser` / 密码: `weiguangpass`
   - 数据库: `weiguangdb`
   - 初始化脚本: `init-database.sql`

2. **MinIO latest** (端口9000/9001)
   - Access Key: 从Secrets获取或默认 `minioadmin`
   - Secret Key: 从Secrets获取或默认 `minioadmin`
   - Console: http://localhost:9001

3. **Backend API** (端口8000)
   - 从 `backend/Dockerfile` 构建
   - 健康检查端点: `/api/health`

**健康检查机制**:
```bash
# PostgreSQL检查
docker exec postgres pg_isready -U weiguanguser

# MinIO检查
curl -sf http://localhost:9000/minio/health/live

# Backend API检查
curl -sf http://localhost:8000/api/health
```

**最大重试次数**: 30次 × 10秒间隔 = 最长等待5分钟

---

##### Job 2: run-e2e-tests (🧪 Run E2E Test Suite)

**职责**: 执行集成测试并生成Allure报告

**测试标记系统** (pytest markers):

| 标记 | 适用场景 | 示例测试内容 |
|------|----------|--------------|
| `api` | API接口测试 | CRUD操作、认证流程、权限校验 |
| `database` | 数据库操作测试 | 事务处理、关联查询、迁移验证 |
| `smoke` | 冒烟测试 | 核心功能快速验证（用户登录、首页加载） |
| *(无标记)* | 全部测试 | 所有E2E测试用例 |

**执行命令示例**:
```bash
# 运行全部测试
pytest tests/e2e/ -v --alluredir=allure-results

# 仅运行API测试
pytest tests/e2e/ -v -m api --alluredir=allure-results

# 仅运行冒烟测试
pytest tests/e2e/ -v -m smoke --alluredir=allure-results
```

**Allure报告特性**:
- 📊 测试用例树状结构展示
- 🔍 失败用例的错误堆栈和截图
- ⏱️ 各测试用例执行耗时排序
- 📈 历史趋势对比（最近20次运行）
- 🌐 环境信息展示

**输出产物**:
| Artifact名称 | 内容 | 保留时间 |
|-------------|------|----------|
| `allure-results-e2e-{run_id}` | Allure原始JSON数据 | 14天 |
| `allure-report-e2e-{run_id}` | 完整HTML可视化报告 | 14天 |

---

##### Job 3: cleanup (🧹 Cleanup Test Environment)

**职责**: 清理所有Docker资源释放Runner磁盘空间

**清理操作**:
```bash
# 停止并删除Compose服务（含匿名卷）
docker compose down -v --remove-orphans

# 清理悬空镜像
docker image prune -f

# 清理未使用的网络
docker network prune -f

# 清理构建缓存
docker builder prune -f
```

**为什么必须清理**:
- GitHub Runner有磁盘空间限制（~14GB可用）
- Docker镜像和卷可能占用数GB空间
- 不清理会导致后续Job因磁盘满而失败

---

### 3.4 预发布部署流水线 (deploy-staging.yml)

#### 📋 基本信息

| 属性 | 值 |
|------|-----|
| **文件路径** | `.github/workflows/deploy-staging.yml` |
| **流水线名称** | `Deploy to Staging` |
| **运行环境** | `ubuntu-latest` |
| **保护环境** | `staging` (GitHub Environment) |
| **总超时** | APK:35min / Docker:20min / Deploy:15min / Notify:5min |

#### 🎯 触发条件

**仅通过Git Tag触发**，支持的Tag格式：

| 格式 | 示例 | 用途 |
|------|------|------|
| `v*.*.*` | `v1.0.0`, `v2.1.3` | 正式版本发布 |
| `v*.*.*-beta.*` | `v1.0.0-beta.1` | Beta测试版 |
| `v*.*.*-rc.*` | `v1.0.0-rc.1` | 发布候选版 |
| `v*.*.*-alpha.*` | `v1.0.0-alpha.1` | Alpha内部测试版 |

**Tag创建方法**:
```bash
# 创建轻量级Tag（推荐用于CI触发）
git tag v1.0.0
git push origin v1.0.0

# 创建带注解的Tag（包含发布说明）
git tag -a v1.0.0 -m "Release version 1.0.0: 初始稳定版"
git push origin v1.0.0
```

#### 🔧 Job结构与依赖链

```
build-release-apk ──┬──▶ build-and-push-docker ──▶ deploy-to-staging ──▶ notify-deployment
                    │
                    └─────────────────────────────────────────────────────────────┘
                                    (都完成后才通知)
```

##### Job 1: build-release-apk

**核心步骤**:
1. 从Git Tag提取版本号: `v1.0.0` → `1.0.0`
2. 解码Keystore: Base64 → JKS文件
3. 构建Release APK:
   ```bash
   ./gradlew assembleRelease \
     -Pandroid.injected.signing.store.file=release.jks \
     -Pandroid.injected.signing.store.password=${PASSWORD} \
     ...
   ```
4. 重命名APK: `WeiguangPlus-v1.0.0-release.apk`
5. 上传至GitHub Releases (Prerelease状态)

**Release Notes自动生成**:
- 通过 `generate_release_notes: true` 自动从Commit历史和PR生成变更日志
- 包含Contributors列表和Issue链接

---

##### Job 2: build-and-push-docker

**镜像标签策略**:

| Tag格式 | 示例 | 更新时机 |
|---------|------|----------|
| 语义化版本 | `ghcr.io/.../backend:v1.0.0` | 每次Tag发布 |
| 次版本 | `ghcr.io/.../backend:v1.0` | 同上 |
| Commit SHA | `ghcr.io/.../backend:abc1234` | 每次构建 |
| Latest | `ghcr.io/.../backend:latest` | 仅main分支 |

**多平台构建支持**:
```yaml
platforms: linux/amd64,linux/arm64
```
- 同时构建x86_64和ARM64架构镜像
- 适用于云服务器(amd64)和边缘设备(arm64)

**Docker Build缓存优化**:
```yaml
cache-from: type=registry,ref=ghcr.io/.../backend:buildcache
cache-to: type=registry,ref=ghcr.io/.../backend:buildcache,mode=max
```
- 从Registry拉取上一版本的层缓存
- 构建后将新缓存推回Registry供下次使用
- 可减少30-50%的构建时间

---

##### Job 3: deploy-to-staging

**部署前准备** (Secrets配置见第4节):
- SSH连接配置 (Host/User/Key)
- 数据库密码
- MinIO访问凭证

**远程部署脚本执行流程**:
```bash
# 1. 拉取最新Docker镜像
docker pull ghcr.io/saber463/weiguangplus-backend:v1.0.0

# 2. 更新环境变量配置
cat > .env.staging << EOF
APP_VERSION=1.0.0
DATABASE_URL=postgresql://...
MINIO_ACCESS_KEY=...
EOF

# 3. 停止旧服务
docker compose -f docker-compose.prod.yml down

# 4. 启动新版本
docker compose -f docker-compose.prod.yml up -d

# 5. 健康检查验证
sleep 15
curl -sf http://localhost:8000/api/health
```

**GitHub Environment保护规则** (建议配置):
- 要求至少1位维护者审批后才允许部署
- 设置等待计时器（如5分钟冷静期）
- 限制只有特定分支可以部署到此环境

---

##### Job 4: notify-deployment

**通知渠道**:

| 渠道 | Secret名称 | 配置方法 |
|------|------------|----------|
| **Slack** | `SLACK_WEBHOOK_URL` | Slack App → Incoming Webhooks |
| **钉钉** | `WEBHOOK_NOTIFY_URL` | 钉钉群 → 智能机器人 → 自定义 |
| **企业微信** | `WEBHOOK_NOTIFY_URL` | 群机器人 → Webhook地址 |
| **飞书** | `WEBHOOK_NOTIFY_URL` | 群机器人 → Webhook地址 |

**通知消息格式**:
```
🎉 WeiguangPlus 部署✅ 成功
━━━━━━━━━━━━━━━━━━━
环境: Staging
版本: v1.0.0
触发者: @saber463
时间: 2026-05-28 18:00 UTC

[查看 Actions Run](https://...)
```

---

## 4. 环境变量与Secrets配置

### 4.1 Secrets清单总览

以下是本项目CI/CD所需的所有GitHub Secrets及其用途说明。

#### 🔐 必须配置的Secrets（核心功能必需）

| Secret名称 | 用途 | 使用位置 | 敏感级别 | 示例值格式 |
|------------|------|----------|----------|-----------|
| **ANDROID_KEYSTORE_BASE64** | APK签名密钥库(Base64编码) | deploy-staging.yml | 🔴极高 | `uQ0AAA...（长Base64串）` |
| **KEYSTORE_PASSWORD** | Keystore密码 | deploy-staging.yml | 🔴极高 | `MyS3cur3P@ss!` |
| **KEY_ALIAS** | 密钥别名 | deploy-staging.yml | 🟠高 | `key0` 或 `release` |
| **KEY_PASSWORD** | 密钥密码 | deploy-staging.yml | 🔴极高 | `MyS3cur3P@ss!` |
| **STAGING_SSH_HOST** | Staging服务器IP/域名 | deploy-staging.yml | 🟠高 | `192.168.1.100` 或 `staging.example.com` |
| **STAGING_SSH_USER** | SSH登录用户名 | deploy-staging.yml | 🟡中 | `deploy` 或 `ubuntu` |
| **STAGING_SSH_PRIVATE_KEY** | SSH私钥(PEM格式) | deploy-staging.yml | 🔴极高 | `-----BEGIN OPENSSH PRIVATE KEY-----\n...` |
| **STAGING_SSH_PORT** | SSH端口(可选) | deploy-staging.yml | 🟡中 | `22` (默认) 或 `2222` |
| **STAGING_DB_PASSWORD** | Staging数据库密码 | deploy-staging.yml | 🔴极高 | `st@ging_db_p@ss123` |
| **MINIO_ACCESS_KEY** | MinIO/S3 Access Key | e2e-test.yml, deploy-staging.yml | 🟠高 | `WK3IGX9Z2EXAMPLE` |
| **MINIO_SECRET_KEY** | MinIO/S3 Secret Key | e2e-test.yml, deploy-staging.yml | 🔴极高 | `aBcDeFgHiJkLmNoPqRsTuVwXyZ0123456789` |

#### 📢 可选配置的Secrets（增强功能）

| Secret名称 | 用途 | 使用位置 | 是否必须 |
|------------|------|----------|----------|
| `SLACK_WEBHOOK_URL` | Slack通知Webhook | deploy-staging.yml | 否 |
| `WEBHOOK_NOTIFY_URL` | 通用Webhook(钉钉/企微等) | deploy-staging.yml | 否 |
| `SECRET_KEY` | JWT/FastAPI密钥 | e2e-test.yml, docker-compose.ci.yml | 否(有默认值) |
| `GITHUB_TOKEN` | GitHub API Token | 自动注入，无需手动配置 | 系统自动 |

### 4.2 Secrets配置教程（图文步骤）

#### 步骤1: 进入仓库Settings

```
GitHub仓库页面 → Settings (左下角) → Secrets and variables → Actions
```

#### 步骤2: 新建Repository Secret

1. 点击 **"New repository secret"** 按钮
2. **Name**: 输入Secret名称（如 `STAGING_SSH_HOST`）
3. **Secret**: 输入对应的值（注意：输入后不可查看只能更新）
4. 点击 **"Add secret"** 保存

#### 步骤3: 逐个添加所有必需Secrets

按照上表顺序逐一添加，建议使用密码管理器(1Password/Bitwarden)复制粘贴避免输入错误。

#### 步骤4: 验证Secrets配置

创建一个测试用的workflow_dispatch触发来验证Secrets是否正确读取。

### 4.3 高级Secret: ANDROID_KEYSTORE_BASE64 生成方法

```bash
# 1. 准备你的JKS文件（假设名为 weiguang-plus-release.jks）
# 2. 将其转换为Base64编码（不同操作系统命令略有差异）

# Linux/macOS:
base64 -w 0 weiguang-plus-release.jks > keystore_base64.txt
cat keystore_base64.txt  # 复制全部内容作为Secret值

# Windows PowerShell:
[Convert]::ToBase64String([IO.File]::ReadAllBytes("weiguang-plus-release.jks")) | Set-Content keystore_base64.txt

# Git Bash (推荐，跨平台一致):
base64 -w 0 weiguang-plus-release.jks | clip  # 直接复制到剪贴板
```

**安全提醒**:
- ⚠️ 绝对不要将原始JKS文件提交到Git仓库！
- ⚠️ 定期轮换签名密钥（建议每年一次）
- ⚠️ 限制能访问Secrets的人员（仅Repo Admins和Deployers）

### 4.4 STAGING_SSH_PRIVATE_KEY 生成方法

```bash
# 1. 生成新的SSH密钥对（如果还没有的话）
ssh-keygen -t ed25519 -C "github-actions-weiguangplus" -f github_actions_deploy_key

# 2. 公钥部署到Staging服务器
ssh-copy-id -i github_actions_deploy_key.pub user@staging-server

# 3. 私钥内容作为Secret值
cat github_actions_deploy_key
# 复制输出的全部内容（包括 -----BEGIN 和 -----END 行）
```

---

## 5. Badge状态徽章配置

### 5.1 什么是Badge？

Badge是显示在README.md中的小型状态图标，可以让访客一眼看到项目的构建状态、版本号等信息。

**效果预览**:
- ![Backend CI](https://github.com/saber463/weiguangplus/workflows/Backend%20CI%20Pipeline/badge.svg)
- ![Android CI](https://github.com/saber463/weiguangplus/workflows/Android%20CI%20Pipeline/badge.svg)
- ![E2E Tests](https://github.com/saber463/weiguangplus/workflows/E2E%20Integration%20Tests/badge.svg)
- ![Release](https://img.shields.io/github/v/release/saber463/weiguangplus?style=flat-square)

### 5.2 Badge Markdown语法

在你的 `README.md` 文件的顶部区域添加以下内容：

```markdown
# 微光同行 WeiguangPlus

### 📊 CI/CD 状态

| 流水线 | 主分支 | 功能分支 |
|--------|--------|----------|
| **后端CI** | [![Backend CI - Main](https://github.com/saber463/weiguangplus/workflows/Backend%20CI%20Pipeline/badge.svg?branch=main)](https://github.com/saber463/weiguangplus/actions?query=workflow%3A%22Backend+CI+Pipeline%22+branch%3Amain) | [![Backend CI - Feature](https://github.com/saber463/weiguangplus/workflows/Backend%20CI%20Pipeline/badge.svg?branch=feature/backend)](https://github.com/saber463/weiguangplus/actions?query=workflow%3A%22Backend+CI+Pipeline%22+branch%3Afeature%2Fbackend) |
| **Android CI** | [![Android CI - Main](https://github.com/saber463/weiguangplus/workflows/Android%20CI%20Pipeline/badge.svg?branch=main)](https://github.com/saber463/weiguangplus/actions?query=workflow%3A%22Android+CI+Pipeline%22+branch%3Amain) | [![Android CI - Feature](https://github.com/saber463/weiguangplus/workflows/Android%20CI%20Pipeline/badge.svg?branch=feature/frontend)](https://github.com/saber463/weiguangplus/actions?query=workflow%3A%22Android+CI+Pipeline%22+branch%3Afeature%2Ffrontend) |
| **E2E测试** | [![E2E Tests](https://github.com/saber463/weiguangplus/workflows/E2E%20Integration%20Tests/badge.svg?branch=main)](https://github.com/saber463/weiguangplus/actions?query=workflow%3A%22E2E+Integration+Tests%22) | - |
| **部署状态** | [![Deploy Staging](https://github.com/saber463/weiguangplus/workflows/Deploy%20to%20Staging/badge.svg)](https://github.com/saber463/weiguangplus/actions?query=workflow%3A%22Deploy+to+Staging%22) | - |

### 📦 版本信息

| 项目 | Badge |
|------|-------|
| **Latest Version** | [![Release](https://img.shields.io/github/v/release/saber463/weiguangplus?style=flat-square&logo=github)](https://github.com/saber463/weiguangplus/releases/latest) |
| **License** | [![License](https://img.shields.io/github/license/saber463/weiguangplus?style=flat-square)](./LICENSE) |
| **Last Commit** | [![Last commit](https://img.shields.io/github/last-commit/saber463/weiguangplus?style=flat-square)](https://github.com/saber463/weiguangplus/commits/main) |
```

### 5.3 自定义Badge样式

Shields.io支持多种样式参数：

| 参数 | 选项 | 示例 |
|------|------|------|
| `style` | plastic, flat, flat-square, for-the-badge | `?style=for-the-badge` |
| `logo` | github, android, python, docker等 | `?logo=android` |
| `labelColor` | 任意hex颜色 | `?labelColor=555555` |
| `color` | brightgreen, yellowgreen, red, orange, blue等 | `?color=blue` |
| `link` | 点击跳转URL | `?link=https://example.com` |

**示例**:
```
https://img.shields.io/github/v/release/saber463/weiguangplus?style=for-the-badge&logo=github&label=Latest%20Release
```

---

## 6. 故障排查指南

### 6.1 常见错误速查表

#### 🔴 后端CI常见错误

| 错误现象 | 可能原因 | 解决方案 |
|----------|----------|----------|
| `ModuleNotFoundError: No module named 'xxx'` | requirements.txt缺少依赖或路径错误 | 检查requirements.txt是否完整；确认BACKEND_DIR路径正确 |
| `flake8 E501 line too long` | 代码行超过120字符 | 重构长行（拆分表达式、使用括号续行） |
| `black would reformat xxx.py` | 代码格式不符合Black规范 | 本地运行 `black .` 自动修复后重新提交 |
| `bandit B105: hardcoded password` | 代码中发现硬编码密码 | 移至环境变量或Secrets；使用占位符+注释说明 |
| `mypy error: Name 'xxx' is not defined` | 类型推断失败或缺少import | 添加正确的import语句或 `# type: ignore` 注释 |
| `coverage < 70%` | 测试覆盖率低于阈值 | 补充测试用例覆盖未测试的代码路径 |
| pip安装超时 | PyPI网络慢或依赖过多 | 启用国内镜像源（见下方配置） |

**pip国内镜像加速配置** (可选):
```bash
# 在workflow中设置
- name: Configure pip mirror
  run: pip config set global.index-url https://pypi.tuna.tsinghua.edu.cn/simple
```

---

#### 🔴 Android CI常见错误

| 错误现象 | 可能原因 | 解决方案 |
|----------|----------|----------|
| `Failed to install SDK` | JDK版本不兼容或网络问题 | 固定distribution为temurin；检查AGP版本兼容性 |
| `Execution failed for task ':app:compileDebugKotlin'` | Kotlin编译错误（语法/类型） | 查看详细错误信息定位具体代码行 |
| `OutOfMemoryError: Java heap space` | Gradle内存不足 | 增加 `org.gradle.jvmargs=-Xmx4g` |
| `License not accepted` | Android SDK许可证未接受 | 添加 `android accept-licenses` 步骤 |
| `keystore password was incorrect` | Signing Secret配置错误 | 重新检查KEYSTORE_PASSWORD等Secret值 |
| `lint abortOnError true` | Lint错误导致构建失败 | 修改lintOptions.abortOnError为false或修复Lint问题 |
| Gradle Daemon崩溃 | 并发构建导致Daemon冲突 | 设置 `-Dorg.gradle.daemon=false` |

**JDK版本兼容性矩阵**:

| AGP版本 | 最低JDK | 最高JDK | 推荐JDK |
|---------|---------|---------|---------|
| 8.0+ | 17 | 21 | 17 (Temurin) |
| 8.2+ | 17 | 21 | 17 (Temurin) |
| 8.4+ | 17 | 21 | 17 (Temurin) |

---

#### 🔴 E2E测试常见错误

| 错误现象 | 可能原因 | 解决方案 |
|----------|----------|----------|
| `Connection refused to postgres:5432` | PostgreSQL未就绪就开始测试 | 增加healthcheck等待时间或retries |
| `docker compose command not found` | Runner未安装Docker Compose V2 | 使用 `docker-compose` (V1语法) 或安装compose-v2 |
| `No such file: docker-compose.ci.yml` | 文件路径错误或未提交 | 确认文件已在仓库根目录且已commit |
| `Allure results not found` | 测试全部跳过或目录路径错误 | 检查tests/e2e/目录是否存在测试文件 |
| `Disk space low` | Docker镜像占用过多空间 | 确保cleanup Job正常运行；增加prune频率 |
| MinIO初始化慢 | 镜像拉取延迟 | 使用固定版本tag而非latest |

---

#### 🔴 部署流水线常见错误

| 错误现象 | 可能原因 | 解决方案 |
|----------|----------|----------|
| `Host key verification failed` | SSH known_hosts未配置 | 添加 `ssh-keyscan` 步骤或设置 `StrictHostKeyChecking=no` |
| `Permission denied (publickey)` | SSH私钥格式错误或权限不对 | 确保私钥为PEM格式且Secret无多余空格 |
| `docker: permission denied` | 远程服务器用户不在docker组 | 将deploy用户加入docker组或使用sudo |
| `image not found` | Docker镜像未成功推送到GHCR | 检查build-and-push-docker Job是否成功完成 |
| `port already in use` | 旧容器未完全停止 | 在deploy脚本中先down再up |
| Webhook发送失败 | URL不可达或格式错误 | 检查Webhook URL有效性；使用curl手动测试 |

### 6.2 日志分析方法

#### 如何定位具体错误？

1. **进入失败的Run**: Actions → 点击红色❌的Run
2. **找到失败的Job**: 点击Job名称展开详情
3. **定位失败步骤**: 找到带有❌红色图标的步骤
4. **展开日志**: 点击该步骤查看完整输出
5. **搜索关键字**: 使用浏览器Ctrl+F搜索 `error`, `Error`, `failed`, `Exception`

#### 日志中的敏感信息脱敏

CI日志可能会意外打印环境变量值。GitHub会自动遮蔽Secrets值的显示，但建议：
- 不在echo/print语句中直接输出敏感变量
- 使用 `${{ secrets.VAR }}` 而非 `${VAR}` 引用Secret
- 定期检查Run日志是否有泄露

### 6.3 重新运行失败的流水线

**方法1: 重新运行全部Job**
```
Actions → Run详情页右上角 → "Re-run all jobs"
```

**方法2: 仅重新运行失败的Job**
```
Actions → Run详情页 → 失败Job旁的下拉菜单 → "Re-run failed jobs"
```

**方法3: 使用不同配置重新运行（workflow_dispatch）**
```
Actions → 左侧工作流 → Run workflow → 选择分支和参数 → Run workflow
```

---

## 7. 性能优化建议

### 7.1 并行Job优化

当前设计已经实现了最大程度的Job并行化：

```
时间轴示意（理想情况）:
0min  ├─ lint-python (10min) ─┤
      ├─ test-backend (20min) ────────┤  ← 总时长 = 最慢的Job
      ├─ security-scan (15min) ────┤      约20分钟（而非 10+20+15+15=60min）
      └─ type-check (15min) ────────┤
```

**进一步优化方向**:
- 使用 **matrix strategy** 同时测试多个Python/JDK版本（会增加总时间但提高覆盖率）
- 将lint拆分为子Job按目录并行（超大型代码库适用）

### 7.2 缓存策略优化

#### 当前缓存命中率提升技巧

**1. 精确化Cache Key**:
```yaml
# 好：基于实际依赖文件hash
key: ${{ runner.os }}-pip-${{ hashFiles('**/requirements*.txt') }}

# 差：过于宽泛的key
key: ${{ runner.os }}-pip
```

**2. 多级restore-keys**:
```yaml
restore-keys: |
  ${{ runner.os }}-pip-${{ hashFiles('requirements.txt') }}  # 精确匹配
  ${{ runner.os }}-pip-                                      # 前缀匹配（兜底）
```

**3. Gradle配置优化**:
```properties
# gradle.properties
org.gradle.caching=true          # 开启构建缓存
org.gradle.parallel=true         # 并行执行独立任务
org.gradle.configureondemand=true # 按需配置项目
org.gradle.jvmargs=-Xmx4g        # 增大内存（加快编译）
kotlin.compiler.execution.strategy=in-process  # 进程内编译（省去进程开销）
```

### 7.3 资源限制与成本控制

| 优化项 | 当前配置 | 建议 | 预期效果 |
|--------|----------|------|----------|
| Runner类型 | ubuntu-latest (2核CPU) | 保持默认 | 平衡性能与成本 |
| Docker内存限制 | Postgres:2G, Backend:2G | CI环境可降至1G | 节约资源 |
| Artifact保留 | 7-14天 | 生产环境缩短至3-5天 | 节约存储费用 |
| 并发控制 | cancel-in-progress: true | 保持启用 | 避免浪费 |

### 7.4 自托管Runner考虑（进阶）

当以下情况出现时，可考虑使用Self-hosted Runner：
- 需要特殊硬件（GPU用于ML模型训练测试）
- 构建需要大量依赖（>10GB）
- 对构建速度有极致要求（<3分钟）
- 数据安全要求不允许代码离开内网

---

## 8. 安全最佳实践

### 8.1 权限最小化原则

每个Workflow文件中都应声明最小必要权限：

```yaml
permissions:
  contents: read       # 大多数CI Job只需读代码
  packages: write      # 仅推送镜像时需要
  deployments: write   # 仅部署Job需要
  issues: write        # 仅自动创建Issue时需要
  id-token: write      # 仅OIDC认证时需要
```

**禁止使用**:
```yaml
permissions: write-all  # ❌ 危险！授予所有权限
```

### 8.2 Secrets加密与管理

**安全原则**:
1. ✅ Secrets一旦存储不可查看/编辑，只能删除重建
2. ✅ 使用强密码（16位以上，大小写+数字+特殊字符）
3. ✅ 定期轮换（签名密钥每年，API密钥每季度）
4. ✅ 使用GitHub Environments限制Secrets的作用域
5. ❌ 不要在代码、日志、Commit中暴露Secrets
6. ❌ 不要在YAML中使用硬编码的凭据

**Secret泄露应急响应**:
1. 立即在GitHub Settings中旋转(删除重建)该Secret
2. 如果是SSH密钥/Token，撤销并重新生成
3. 如果是数据库密码，立即修改
4. 审计该Secret的使用日志确定影响范围

### 8.3 第三方Action安全

**使用 pinned 版本** (而非 @master/@main):
```yaml
# ✅ 好：使用版本tag或SHA
uses: actions/checkout@v4
uses: actions/checkout@a5ac7e51b41094c42402fb0168f34a82eaeaf8f5  # SHA pinning

# ❌ 差：使用分支名（可能被篡改）
uses: actions/checkout@main
uses: actions/checkout@v4  # 但要定期检查更新
```

**建议的操作**:
- 定期检查使用的第三方Action是否有已知CVE
- 使用 Dependabot 监控 Action 版本更新
- 对于关键Action，使用SHA pinning防止供应链攻击

### 8.4 日志脱敏

**自动脱敏**: GitHub会自动遮蔽 `${{ secrets.XXX }}` 引用的值

**手动防护**:
```yaml
# ❌ 危险：可能在日志中泄露
- name: Debug
  run: echo "Connecting to $DATABASE_URL"

# ✅ 安全：使用Secret引用
- name: Debug
  run: echo "Connecting to ${{ secrets.DATABASE_URL }}"
  # 或者完全不输出
  run: echo "Database connection configured"
```

### 8.5 代码签名与完整性

**APK签名安全**:
- 使用单独的CI专用签名密钥（与发布密钥分离）
- Keystore密码使用强随机密码
- 考虑使用Google Play App Signing（密钥由Google托管）

**Docker镜像签名** (可选进阶):
```bash
# 安装cosign工具
# 对镜像进行签名确保来源可信
cosign sign --key cosign.key ghcr.io/saber463/weiguangplus-backend:v1.0.0
```

---

## 9. 本地调试CI脚本（act工具）

### 9.1 什么是act？

**act** 是一个可以在本地运行GitHub Actions的工具，让你无需push代码就能测试Workflow是否正常工作。

**项目地址**: https://github.com/nektos/act

### 9.2 安装act

#### macOS (Homebrew):
```bash
brew install act
```

#### Linux:
```bash
# 方法1: 从GitHub Release下载二进制
curl -s https://raw.githubusercontent.com/nektos/act/master/install.sh | sudo bash

# 方法2: Go安装
go install github.com/nektos/act/latest@latest
```

#### Windows:
```powershell
# 使用Scoop
scoop install act

# 或使用Chocolatey
choco install act
```

### 9.3 常用命令

#### 查看可用的Workflows:
```bash
act -l
```

#### 运行指定Workflow:
```bash
# 运行后端CI流水线
act -j "Backend CI Pipeline"

# 运行Android CI流水线
act -j "Android CI Pipeline"

# 运行特定Job
act -j "lint-python"
```

#### 模拟特定事件:
```bash
# 模拟Push到main分支
act -e push.json -j "Backend CI Pipeline"

# push.json 内容示例:
{
  "ref": "refs/heads/main",
  "after": "abc1234567890",
  "repository": {
    "default_branch": "main"
  }
}
```

#### 使用Docker容器模拟:
```bash
# 使用Ubuntu镜像（与GitHub Runner一致）
act -P ubuntu-latest=node:16-bullseye

# 使用官方Runner镜像（更接近真实环境）
act -P ubuntu-latest=ghcr.io/catthehacker/ubuntu:act-latest
```

#### 详细模式（调试用）:
```bash
# 显示详细日志
act -v -j "lint-python"

# 不执行Dry-run模式（只显示将要运行的步骤）
act -n -j "Backend CI Pipeline"

# 保持容器运行完毕后不删除（便于手动检查）
act --bind --artifact-server=off -j "test-backend"
```

### 9.4 本地Secrets配置

创建 `.secrets` 文件（已在.gitignore中忽略）:

```bash
# .secrets 文件格式（key=value）
GITHUB_TOKEN=your-personal-access-token
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin
SECRET_KEY=test-secret-for-local
```

然后运行:
```bash
act --secret-file .secrets -j "E2E Integration Tests"
```

### 9.5 act的限制和注意事项

| 限制项 | 说明 | 应对方案 |
|--------|------|----------|
| 不支持所有Action | 部分第三方Action可能不兼容 | 替换为等效命令或跳过 |
| Docker环境差异 | 本地Docker与Runner环境略有不同 | 关键验证仍需推送到GitHub |
| 无真实Secrets | 本地无法访问GitHub Secrets | 使用 .secrets 文件模拟 |
| 无并行限制 | 本地可同时运行更多Job | 注意本地资源消耗 |
| services支持有限 | Docker Compose service可能不完全支持 | 手动启动服务或简化测试 |

### 9.6 推荐的本地调试工作流

```bash
# 1. 先列出所有Workflow确认名称
act -l

# 2. Dry-run查看将要执行的步骤
act -n -j "Backend CI Pipeline"

# 3. 运行单个Job进行快速验证
act -j "lint-python"

# 4. 运行完整Workflow（时间较长）
act -j "Backend CI Pipeline"

# 5. 如果遇到问题，使用verbose模式排查
act -v -j "lint-python" 2>&1 | tee act_debug.log
```

---

## 10. 附录与参考资源

### 10.1 文件清单索引

| 文件路径 | 用途 | 相关章节 |
|----------|------|----------|
| `.github/workflows/ci-backend.yml` | 后端Python CI流水线 | §3.1 |
| `.github/workflows/ci-android.yml` | Android前端CI流水线 | §3.2 |
| `.github/workflows/e2e-test.yml` | E2E集成测试流水线 | §3.3 |
| `.github/workflows/deploy-staging.yml` | Staging部署流水线 | §3.4 |
| `.github/ISSUE_TEMPLATE/bug_report.md` | Bug报告模板 | - |
| `.github/ISSUE_TEMPLATE/feature_request.md` | 功能请求模板 | - |
| `.github/ISSUE_TEMPLATE/performance_issue.md` | 性能问题模板 | - |
| `.github/PULL_REQUEST_TEMPLATE.md` | PR描述模板 | - |
| `.github/dependabot.yml` | 依赖自动更新配置 | - |
| `CODEOWNERS` | 代码审查者分配 | - |
| `docker-compose.ci.yml` | CI专用Docker编排 | §3.3 |
| `CI-CD-PIPELINE.md` | 本文档（你正在阅读） | - |

### 10.2 官方文档链接

| 资源 | URL |
|------|-----|
| GitHub Actions文档 | https://docs.github.com/en/actions |
| Workflow语法参考 | https://docs.github.com/en/actions/reference/workflow-syntax-for-github-actions |
| Contexts表达式 | https://docs.github.com/en/actions/learn-github-actions/contexts |
| Secrets管理 | https://docs.github.com/en/actions/security-guides/using-secrets-in-github-actions |
| Environments | https://docs.github.com/en/actions/deployment/using-environments-for-deployment |
| Dependabot | https://docs.github.com/en/code-security/dependabot |
| CODEOWNERS | https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/customizing-your-repository/about-code-owners |
| Actions Marketplace | https://github.com/marketplace?type=actions |
| act工具文档 | https://github.com/nektos/act |
| Shields.io徽章 | https://shields.io/ |
| Allure报告 | https://docs.qameta.io/allure/ |
| Bandit安全扫描 | https://bandit.readthedocs.io/ |
| Mypy类型检查 | https://mypy.readthedocs.io/ |
| Flake8代码风格 | https://flake8.pycqa.org/ |
| Black代码格式化 | https://black.readthedocs.io/ |

### 10.3 社区与支持

- **GitHub Discussions**: 在仓库的Discussions标签页提问
- **Issues**: 发现Bug或有改进建议请提Issue
- **邮件联系**: （如有需要可在此填写联系方式）

---

## 📝 文档修订历史

| 版本 | 日期 | 作者 | 变更内容 |
|------|------|------|----------|
| v1.0.0 | 2026-05-28 | @saber463 | 初始版本，建立完整CI/CD体系 |

---

> 💡 **提示**: 本文档会随着项目的演进持续更新。如果你发现任何过时或不准确的信息，欢迎提交PR修正！

> ⚠️ **免责声明**: 本CI/CD配置为微光同行项目量身定制，其他项目在使用前请根据自身技术栈进行调整。
