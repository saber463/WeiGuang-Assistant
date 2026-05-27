# 2026-05-19 构建失败：Kotlin Daemon “Unknown or invalid session”

## 1. 问题现象

执行 Gradle 构建（例如 `:app:assembleDebug` / `kaptGenerateStubsDebugKotlin`）时失败，日志包含：

```text
Daemon compilation failed: Unknown or invalid session 1
java.lang.Exception: Unknown or invalid session 1
...
Using fallback strategy: Compile without Kotlin daemon
Try ./gradlew --stop if this issue persists.
```

## 2. 根因分析（为什么会出现）

- Kotlin 编译默认会通过 Kotlin Daemon 进程处理编译请求。
- 在 Windows/频繁中断构建/IDE 与命令行混用/残留进程等场景下，Kotlin Daemon 的会话状态可能失效：
  - 旧的 daemon 进程被回收或异常退出，但 Gradle 仍尝试复用该 session
  - 导致编译服务端返回 “Unknown or invalid session”，从而构建失败

这类问题属于“构建基础设施不稳定”而非业务代码问题。

## 3. 修复方案（怎么修复）

### 3.1 根本性规避：让 Kotlin 编译不再依赖 Kotlin Daemon

在项目级 `gradle.properties` 增加：

```properties
kotlin.compiler.execution.strategy=in-process
```

效果：
- Kotlin 编译在 Gradle 进程内执行，避开 Kotlin Daemon 的 session 失效问题

### 3.2 现场应急：停止残留进程

若仍偶发，可执行：

```bash
./gradlew --stop
```

或重启 IDE/系统，清理残留进程后再构建。

## 4. 验证方式

- 重新执行构建任务（如 `:app:assembleDebug`），不再出现 “Unknown or invalid session”。

## 5. 本次修改文件清单

- `gradle.properties`

