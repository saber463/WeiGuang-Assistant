# 微光同行（WeiguangPlus）前后端接口契约文档

> **文档版本**: v1.0.0
> **生效日期**: 2026-05-28
> **适用对象**: Android前端开发者（Kotlin + Retrofit）、Python后端开发者（FastAPI）
> **API基础路径**: `https://api.weiguangplus.com/api/v1`
> **维护状态**: 活跃

---

## 目录

- [1. 文档概述](#1-文档概述)
- [2. 通用约定](#2-通用约定)
- [3. 用户认证模块](#3-用户认证模块)
- [4. 用户信息模块](#4-用户信息模块)
- [5. 药品识别模块](#5-药品识别模块)
- [6. 紧急联系人模块](#6-紧急联系人模块)
- [7. 文件上传模块](#7-文件上传模块)
- [8. 附录](#8-附录)

---

## 1. 文档概述

### 1.1 项目简介

**微光同行（WeiguangPlus）** 是一款面向视障/听障/肢障等残障群体的无障碍生活辅助APP，核心功能包括：

| 功能模块 | 描述 | 优先级 |
|---------|------|-------|
| 药品智能识别 | 拍照OCR识别药盒匹配药品库风险评估TTS语音播报 | P0 |
| 紧急求助SOS | 一键触发SOS自动发送短信+位置给紧急联系人 | P0 |
| 紧急联系人管理 | CRUD管理紧急联系人支持排序和优先级 | P0 |
| 用户认证体系 | 手机号注册/登录、JWT双Token、健康档案 | P0 |
| 双向手语互通 | 语音到手语双向转换（P0离线功能） | P0 |
| 全局强提醒 | 震动+铃声+灯光三重提醒系统 | P0 |

### 1.2 技术栈对照

| 层级 | 前端（Android） | 后端（Python） |
|------|-----------------|---------------|
| 语言 | Kotlin | Python 3.11+ |
| 框架 | Jetpack Compose + MVVM | FastAPI |
| 网络 | Retrofit 2.9 + OkHttp 4.12 | Uvicorn (ASGI) |
| 数据库 | Room（本地离线库） | PostgreSQL 15 + SQLAlchemy 2.0 |
| 对象存储 | - | MinIO |
| 认证 | EncryptedSharedPreferences | JWT (python-jose) |
| 图片加载 | Coil 2.5 | Pillow 10.0 |
| 序列化 | Gson 2.10 | Pydantic v2 / orjson |

### 1.3 环境信息

| 环境 | 基础URL | 说明 |
|------|---------|------|
| 开发环境 | `http://localhost:8000/api/v1` | Docker Compose本地启动 |
| 测试环境 | `https://test-api.weiguangplus.com/api/v1` | 内部测试用 |
| 生产环境 | `https://api.weiguangplus.com/api/v1` | 正式发布 |
| API文档 | `{BASE_URL}/docs` | Swagger UI自动生成 |

---

## 2. 通用约定

### 2.1 统一响应格式

所有API接口均遵循以下统一JSON响应结构：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { },
  "error_code": null,
  "timestamp": "2026-05-28T10:30:00.000Z"
}
```

#### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| `code` | integer | 是 | HTTP业务状态码，见错误码枚举表 |
| `message` | string | 是 | 人类可读的状态描述，用于Toast/弹窗展示 |
| `data` | object/null | 是 | 业务数据载荷，成功时返回具体对象，无数据时为null |
| `error_code` | string/null | 是 | 精细错误码，仅错误时返回，如PHONE_ALREADY_EXISTS |
| `timestamp` | string(ISO8601) | 是 | 服务器响应时间戳，UTC时区 |

#### 成功响应示例

```json
// 200 OK - 有数据
{
  "code": 200, "message": "获取成功",
  "data": { "user_id": 1, "nickname": "小明" },
  "error_code": null, "timestamp": "2026-05-28T10:30:00.000Z"
}

// 201 Created - 资源创建成功
{
  "code": 201, "message": "注册成功",
  "data": { "user_id": 1, "phone": "138****8000" },
  "error_code": null, "timestamp": "2026-05-28T10:30:00.000Z"
}
```

#### 错误响应示例

```json
// 400 Bad Request - 参数校验失败
{ "code": 400, "message": "请求参数不合法", "data": null, "error_code": "VALIDATION_ERROR", "timestamp": "..." }

// 401 Unauthorized - Token无效或过期
{ "code": 401, "message": "登录已过期，请重新登录", "data": null, "error_code": "TOKEN_EXPIRED", "timestamp": "..." }

// 409 Conflict - 资源冲突
{ "code": 409, "message": "手机号已被注册", "data": null, "error_code": "PHONE_ALREADY_EXISTS", "timestamp": "..." }

// 500 Internal Server Error
{ "code": 500, "message": "服务器内部错误，请稍后重试", "data": null, "error_code": "INTERNAL_ERROR", "timestamp": "..." }
```

#### HTTP Status与业务code映射关系

| HTTP Status | 业务 code | 含义 | 前端处理建议 |
|------------|----------|------|-------------|
| 200 | 200 | 成功 | 正常展示数据 |
| 201 | 201 | 创建成功 | 展示成功提示，可跳转 |
| 204 | 204 | 无内容（删除成功） | 静默成功或刷新列表 |
| 400 | 400 | 参数错误 | 展示具体字段错误信息 |
| 401 | 401 | 未认证 | 清除Token，跳转登录页 |
| 403 | 403 | 无权限 | 提示暂无权限操作 |
| 404 | 404 | 资源不存在 | 提示数据已删除或不存在 |
| 409 | 409 | 资源冲突 | 根据error_code做差异化提示 |
| 413 | 413 | 文件过大 | 提示文件大小不能超过XX |
| 422 | 422 | 不可处理的实体 | 展示字段级别校验错误 |
| 429 | 429 | 请求过于频繁 | 提示操作太频繁，请稍后再试 |
| 500 | 500 | 服务器内部错误 | 展示通用错误页，提供重试按钮 |

### 2.2 分页规范

所有列表类接口均采用统一分页格式：

```json
{
  "code": 200,
  "data": {
    "total": 156,
    "page": 1,
    "size": 20,
    "has_more": true,
    "items": [{ "id": 1, "name": "item1" }]
  }
}
```

#### data内分页字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| `total` | integer | 是 | 符合条件的总记录数 |
| `page` | integer | 是 | 当前页码，从1开始 |
| `size` | integer | 是 | 每页记录数 |
| `has_more` | boolean | 是 | 是否有下一页 |
| `items` | array | 是 | 当前页的数据数组，可能为空数组 |

#### 分页Query参数

| 参数 | 类型 | 默认值 | 取值范围 | 说明 |
|------|------|--------|---------|------|
| `page` | integer | 1 | 大于等于1 | 页码 |
| `size` | integer | 20 | 1~100 | 每页条数 |

### 2.3 认证机制

#### 2.3.1 JWT双Token架构

本系统采用 Access Token + Refresh Token 双Token机制：

Access Token有效期30分钟，每次API请求携带在Authorization Header中。Refresh Token有效期7天，用于刷新Access Token。

#### 2.3.2 Token格式规范

Header格式：`Authorization: Bearer eyJhbGciOiJIUzI1NiIs...`

Access Token Payload结构：
```json
{
  "sub": "1",
  "phone": "13800138000",
  "type": "access",
  "iat": 1740000000,
  "exp": 1740001800
}
```

Refresh Token Payload结构：
```json
{
  "sub": "1",
  "type": "refresh",
  "iat": 1740000000,
  "exp": 1740604800
}
```

#### 2.3.3 Token有效期配置

| Token类型 | 有效期 | 存储位置 | 用途 |
|----------|--------|---------|------|
| Access Token | 30分钟 | 内存/EncryptedSharedPreferences | 每次API请求携带在Header中 |
| Refresh Token | 7天 | EncryptedSharedPreferences | 用于刷新Access Token |

#### 2.3.4 Token刷新流程

```
用户发起API请求 -> 携带Access Token -> 后端验证
  |-- 200 OK -> 正常返回数据
  +-- 401 Unauthorized
       |-- 使用Refresh Token调用 POST /api/v1/auth/refresh
       |    |-- 刷新成功 -> 保存新Token -> 重试原请求 -> 返回结果
       |    +-- 刷新也失败 -> 清除Token -> 跳转登录页
       +-- （非Token过期类401）-> 直接跳转登录页
```

#### 2.3.5 Token黑名单机制

用户主动退出登录时，后端将当前Access Token加入Redis黑名单（TTL=剩余有效期），实现即时失效。

### 2.4 数据类型规范

| 数据类别 | 格式 | 示例 | 备注 |
|---------|------|------|------|
| 时间戳 | ISO8601 UTC | 2026-05-28T10:30:00.000Z | 前端自行转换本地时区 |
| 日期 | YYYY-MM-DD | 2026-05-28 | 仅日期不含时间 |
| 手机号 | string(11位) | 13800138000 | 纯数字字符串 |
| 手机号脱敏 | string | 138****8000 | 中间4位用*替代 |
| 经纬度 | float(6位小数) | 104.065735, 30.659462 | GCJ02坐标系，精度约0.1米 |
| 金额 | integer（单位：分） | 19900 = 199.00元 | 前端展示需除以100 |
| 置信度 | float(0~1) | 0.92 | 药品识别匹配置信度 |

#### 风险等级枚举（risk_level）

| 值 | 含义 | 前端颜色 |
|---|------|---------|
| LOW | 低风险 | #4CAF50 绿色 |
| MEDIUM | 中等风险 | #FF9800 橙色 |
| HIGH | 高风险 | #F44336 红色 |
| CRITICAL | 极高风险 | #9C27B0 紫红 |

#### SOS事件枚举值

scenario_type（场景类型）：ILLNESS(突发疾病) / LOST(迷路走失) / ACCIDENT(交通事故) / OTHER(其他)

severity（严重程度）：LOW(轻微) / MEDIUM(中等) / HIGH(严重) / CRITICAL(危急)

status（状态）：ACTIVE(进行中) / RESOLVED(已解决) / CANCELLED(已取消)

残疾类型：VISUAL_IMPAIRMENT(视障) / HEARING_IMPAIRMENT(听障) / PHYSICAL_DISABILITY(肢障) / MULTIPLE_DISABILITY(多重残疾)

残疾等级：LEVEL_1(一级极重度) ~ LEVEL_4(四级轻度)

孕妇风险等级：A(无风险) / B(低风险) / C(风险不能排除) / D(有阳性证据) / X(禁用)

### 2.5 版本控制策略

API版本通过URL路径中的 /v1/ 前缀进行控制：
- 向后兼容修改：不升级版本号，直接在v1上迭代
- 不兼容修改：必须升级到 /v2/
- 废弃接口：Header加入 Deprecated: true + 至少3个月过渡期

### 2.6 请求频率限制

| 接口分类 | 限制规则 | 超限error_code |
|---------|---------|---------------|
| 认证接口（注册/登录） | 同IP每分钟最多5次 | RATE_LIMIT_AUTH |
| 文件上传 | 单用户每分钟最多10次 | RATE_LIMIT_UPLOAD |
| SOS触发 | 单用户每小时最多20次 | RATE_LIMIT_SOS |
| 通用查询 | 单用户每秒最多10次 | RATE_LIMIT_GENERAL |

超限返回HTTP 429 + Header: Retry-After: N, X-RateLimit-Remaining: 0

### 2.7 错误码枚举表

#### 系统级错误码（1001 ~ 1099）

| error_code | code | HTTP | 含义 |
|------------|------|------|------|
| SUCCESS | 200 | 200 | 操作成功 |
| CREATED | 201 | 201 | 资源创建成功 |
| VALIDATION_ERROR | 1001 | 400 | 请求参数校验失败 |
| INVALID_JSON_FORMAT | 1002 | 400 | JSON格式非法 |
| MISSING_REQUIRED_FIELD | 1003 | 400 | 缺少必填字段 |
| FIELD_TYPE_MISMATCH | 1004 | 400 | 字段类型不匹配 |
| FIELD_VALUE_OUT_OF_RANGE | 1005 | 400 | 字段值超出允许范围 |
| REQUEST_ENTITY_TOO_LARGE | 1007 | 413 | 请求体过大 |
| TOO_MANY_REQUESTS | 1008 | 429 | 请求过于频繁 |
| TOKEN_MISSING | 1011 | 401 | 缺少认证Token |
| TOKEN_EXPIRED | 1012 | 401 | Token已过期 |
| TOKEN_INVALID | 1013 | 401 | Token格式无效 |
| TOKEN_BLACKLISTED | 1014 | 401 | Token已被注销 |
| REFRESH_TOKEN_EXPIRED | 1015 | 401 | Refresh Token已过期 |
| PERMISSION_DENIED | 1017 | 403 | 权限不足 |
| RESOURCE_NOT_FOUND | 1018 | 404 | 资源不存在 |
| INTERNAL_ERROR | 1099 | 500 | 服务器内部错误 |

#### 业务级错误码（2001 ~ 2099）

| error_code | code | HTTP | 模块 | 含义 |
|------------|------|------|------|------|
| PHONE_ALREADY_EXISTS | 2001 | 409 | auth | 手机号已被注册 |
| PHONE_NOT_FOUND | 2002 | 404 | auth | 手机号未注册 |
| PASSWORD_INCORRECT | 2003 | 401 | auth | 密码错误 |
| PASSWORD_TOO_WEAK | 2004 | 400 | auth | 密码强度不足 |
| OLD_PASSWORD_INCORRECT | 2005 | 400 | auth | 旧密码不正确 |
| NEW_PASSWORD_SAME_AS_OLD | 2006 | 400 | auth | 新旧密码相同 |
| ACCOUNT_DISABLED | 2007 | 403 | auth | 账号被禁用 |
| ACCOUNT_LOCKED | 2008 | 423 | auth | 账号被锁定 |
| USER_NOT_FOUND | 2021 | 404 | users | 用户不存在 |
| NICKNAME_TOO_LONG | 2022 | 400 | users | 昵称过长 |
| DRUG_NOT_FOUND | 2041 | 404 | drugs | 药品不存在 |
| IMAGE_FILE_TOO_LARGE | 2044 | 413 | drugs | 图片过大(最大10MB) |
| IMAGE_FORMAT_INVALID | 2045 | 400 | drugs | 图片格式不支持(jpg/png/webp) |
| CONTACT_NOT_FOUND | 2061 | 404 | emergency | 联系人不存在 |
| CONTACT_LIMIT_EXCEEDED | 2062 | 400 | emergency | 联系人数量达上限(最多10个) |
| CONTACT_PHONE_INVALID | 2063 | 400 | emergency | 手机号格式无效 |
| CONTACT_PHONE_DUPLICATE | 2064 | 409 | emergency | 手机号重复 |
| SOS_EVENT_NOT_FOUND | 2065 | 404 | sos | SOS事件不存在 |
| NO_EMERGENCY_CONTACT | 2068 | 400 | sos | 未设置紧急联系人 |
| FILE_NOT_FOUND | 2081 | 404 | files | 文件不存在 |
| FILE_SIZE_EXCEEDED | 2084 | 413 | files | 文件大小超限 |
| MINIO_CONNECTION_FAILED | 2091 | 503 | minio | MinIO连接失败 |

---

## 3. 用户认证模块

> **模块路径**: `/api/v1/auth/*`
> **认证要求**: 注册和登录无需Token；其余需要Bearer Token

### 3.1 用户注册 POST /api/v1/auth/register

创建新用户账号。

| 项目 | 值 |
|------|-----|
| URL | POST /api/v1/auth/register |
| Content-Type | application/json |
| 认证要求 | 不需要Token |
| 频率限制 | 同IP每分钟最多5次 |

#### 请求参数（Request Body）

| 字段 | 类型 | 必填 | 约束 | 说明 | 示例 |
|------|------|:----:|------|------|------|
| phone | string | 是 | 11位手机号，正则^1[3-9]\d{9}$ | 登录手机号（唯一索引） | 13800138000 |
| password | string | 是 | 8-20字符，含字母和数字 | 登录密码（bcrypt哈希存储） | Passw0rd123 |
| nickname | string | 否 | 2-50字符 | 用户昵称 | 小明 |
| disability_type | string | 否 | 枚举白名单 | 残疾类型 | VISUAL_IMPAIRMENT |
| disability_level | string | 否 | 枚举白名单 | 残疾等级 | LEVEL_1 |

#### curl示例

```bash
curl -X POST 'https://api.weiguangplus.com/api/v1/auth/register' \
  -H 'Content-Type: application/json' \
  -d '{
    "phone": "13800138000",
    "password": "Passw0rd123",
    "nickname": "小明",
    "disability_type": "VISUAL_IMPAIRMENT",
    "disability_level": "LEVEL_1"
  }'
```

#### 成功响应 201 Created

```json
{
  "code": 201, "message": "注册成功",
  "data": {
    "user": {
      "id": 1, "phone": "138****8000", "nickname": "小明",
      "disability_type": "VISUAL_IMPAIRMENT", "disability_level": "LEVEL_1",
      "created_at": "2026-05-28T10:30:00.000Z"
    }
  }, "error_code": null, "timestamp": "2026-05-28T10:30:01.123Z"
}
```

#### 错误响应

| 场景 | code | HTTP | error_code | message |
|------|------|------|-----------|---------|
| 手机号格式错误 | 400 | 400 | VALIDATION_ERROR | 手机号格式不正确 |
| 密码强度不足 | 400 | 400 | PASSWORD_TOO_WEAK | 密码需8-20位含字母和数字 |
| 手机号已存在 | 409 | 409 | PHONE_ALREADY_EXISTS | 该手机号已被注册 |

#### data Schema（RegisterResponseData）

```typescript
interface RegisterResponseData {
  user: {
    id: number;
    phone: string;               // 脱敏后的手机号
    nickname: string | null;
    disability_type: string | null;
    disability_level: string | null;
    created_at: string;           // ISO8601 UTC
  };
}
```

---

### 3.2 用户登录 POST /api/v1/auth/login

使用手机号密码验证身份，返回双Token。

| 项目 | 值 |
|------|-----|
| URL | POST /api/v1/auth/login |
| Content-Type | application/json |
| 认证要求 | 不需要Token |
| 频率限制 | 同IP每分钟5次；同账号每分钟10次 |

#### 请求参数（Request Body）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|:----:|------|------|
| phone | string | 是 | 注册时的手机号 | 13800138000 |
| password | string | 是 | 注册时的密码 | Passw0rd123 |

#### curl示例

```bash
curl -X POST 'https://api.weiguangplus.com/api/v1/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"phone":"13800138000","password":"Passw0rd123"}'
```

#### 成功响应 200 OK

```json
{
  "code": 200, "message": "登录成功",
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIs...",
    "refresh_token": "eyJhbGciOiJIUzI1NiIs...",
    "token_type": "bearer",
    "expires_in": 1800,
    "user": {
      "id": 1, "phone": "138****8000", "nickname": "小明",
      "avatar_url": null,
      "disability_type": "VISUAL_IMPAIRMENT", "disability_level": "LEVEL_1"
    }
  }, "error_code": null, "timestamp": "..."
}
```

#### 错误响应

| 场景 | code | HTTP | error_code | message |
|------|------|------|-----------|---------|
| 手机号或密码错误 | 401 | 401 | PASSWORD_INCORRECT | 手机号或密码错误 |
| 账号禁用 | 403 | 403 | ACCOUNT_DISABLED | 账号已被禁用，请联系客服 |
| 账号锁定 | 423 | 423 | ACCOUNT_LOCKED | 账号因多次输错密码已锁定，请30分钟后重试 |

#### 安全机制

- 密码存储：bcrypt哈希，salt rounds=12
- 连续5次失败锁定30分钟（指数退避）
- 密码比对使用安全compare函数防时序攻击
- 手机号始终脱敏返回

#### data Schema（LoginResponseData）

```typescript
interface LoginResponseData {
  access_token: string;        // JWT Access Token（30分钟有效）
  refresh_token: string;       // JWT Refresh Token（7天有效）
  token_type: "bearer";
  expires_in: number;          // Access Token剩余秒数（通常1800）
  user: {
    id: number;
    phone: string;             // 脱敏
    nickname: string | null;
    avatar_url: string | null;
    disability_type: string | null;
    disability_level: string | null;
  };
}
```

---

### 3.3 刷新Token POST /api/v1/auth/refresh

使用Refresh Token换取新的Access Token。

| 项目 | 值 |
|------|-----|
| URL | POST /api/v1/auth/refresh |
| Content-Type | application/json |
| 认证要求 | 不需要Bearer Token（使用refresh_token本身） |
| 频率限制 | 单用户每分钟最多20次 |

#### 请求参数（Request Body）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|:----:|------|------|
| refresh_token | string | 是 | 登录时获得的Refresh Token | eyJhbG... |

#### curl示例

```bash
curl -X POST 'https://api.weiguangplus.com/api/v1/auth/refresh' \
  -H 'Content-Type: application/json' \
  -d '{"refresh_token":"eyJhbGciOiJIUzI1NiIs..."}'
```

#### 成功响应 200 OK

```json
{
  "code": 200, "message": "Token刷新成功",
  "data": {
    "access_token": "new_access_token...",
    "refresh_token": "new_refresh_token...",
    "token_type": "bearer",
    "expires_in": 1800
  }, "error_code": null, "timestamp": "..."
}
```

注意：每次刷新同时返回新的Refresh Token（续期），前端应同步更新两者。

#### 错误响应

| 场景 | code | error_code | message |
|------|------|-----------|---------|
| Refresh Token过期 | 401 | REFRESH_TOKEN_EXPIRED | Refresh Token已过期，请重新登录 |
| Refresh Token被撤销 | 401 | REFRESH_TOKEN_REVOKED | Refresh Token已失效 |

---

### 3.4 修改密码 PUT /api/v1/users/password

修改当前用户密码。成功后所有Token立即失效。

| 项目 | 值 |
|------|-----|
| URL | PUT /api/v1/users/password |
| Header | Authorization: Bearer token |
| 频率限制 | 单用户每天最多3次 |

#### 请求参数（Request Body）

| 字段 | 类型 | 必填 | 约束 | 说明 | 示例 |
|------|------|:----:|------|------|------|
| old_password | string | 是 | 8-20字符 | 当前密码 | OldPass123 |
| new_password | string | 是 | 8-20字符，含字母和数字 | 新密码 | NewPass456 |

#### curl示例

```bash
curl -X PUT 'https://api.weiguangplus.com/api/v1/users/password' \
  -H 'Authorization: Bearer eyJ...' \
  -H 'Content-Type: application/json' \
  -d '{"old_password":"OldPass123","new_password":"NewPass456"}'
```

#### 成功响应 200 OK

```json
{"code":200,"message":"密码修改成功","data":null,"error_code":null,"timestamp":"..."}
```

重要：修改密码成功后，前端应清除所有Token并跳转登录页。

#### 错误响应

| 场景 | error_code | message |
|------|-----------|---------|
| 旧密码错误 | OLD_PASSWORD_INCORRECT | 当前密码不正确 |
| 新旧相同 | NEW_PASSWORD_SAME_AS_OLD | 新密码不能与当前密码相同 |

---

### 3.5 退出登录 POST /api/v1/auth/logout

注销当前登录态，使Token加入黑名单。

| 项目 | 值 |
|------|-----|
| URL | POST /api/v1/auth/logout |
| Header | Authorization: Bearer token |
| 请求体 | 无 |

#### curl示例

```bash
curl -X POST 'https://api.weiguangplus.com/api/v1/auth/logout' \
  -H 'Authorization: Bearer eyJ...'
```

#### 成功响应 200 OK

```json
{"code":200,"message":"已安全退出","data":null,"error_code":null,"timestamp":"..."}
```

后端行为：
1. 将当前Access Token加入Redis黑名单（TTL=剩余有效期）
2. 可选：将关联Refresh Token也标记为已撤销
3. 前端收到200后清除本地所有Token并跳转登录页

---

## 4. 用户信息模块

> **模块路径**: `/api/v1/users/*`
> **认证要求**: 所有接口均需要Bearer Token

### 4.1 获取个人信息 GET /api/v1/users/profile

获取当前用户的完整个人资料。

| 项目 | 值 |
|------|-----|
| URL | GET /api/v1/users/profile |
| Header | Authorization: Bearer token |
| 频率限制 | 单用户每秒最多10次 |

#### curl示例

```bash
curl -X GET 'https://api.weiguangplus.com/api/v1/users/profile' \
  -H 'Authorization: Bearer eyJ...'
```

#### 成功响应 200 OK

```json
{
  "code": 200, "message": "获取成功",
  "data": {
    "user": {
      "id": 1, "phone": "138****8000", "nickname": "小明",
      "avatar_url": "https://cdn.weiguangplus.com/avatars/user_1.jpg",
      "disability_type": "VISUAL_IMPAIRMENT", "disability_level": "LEVEL_1",
      "is_active": true,
      "created_at": "2026-05-01T08:00:00.000Z",
      "updated_at": "2026-05-28T10:30:00.000Z"
    }
  }, "error_code": null, "timestamp": "..."
}
```

#### data Schema（UserProfileResponseData）

```typescript
interface UserProfileResponseData {
  user: {
    id: number;
    phone: string;               // 脱敏
    nickname: string | null;
    avatar_url: string | null;    // MinIO CDN地址或null
    disability_type: string | null;
    disability_level: string | null;
    is_active: boolean;
    created_at: string;           // ISO8601
    updated_at: string;           // ISO8601
  };
}
```

---

### 4.2 更新个人信息 PUT /api/v1/users/profile

部分更新个人信息（PATCH语义，只传需改的字段）。

| 项目 | 值 |
|------|-----|
| URL | PUT /api/v1/users/profile |
| Content-Type | application/json |
| Header | Authorization: Bearer token |

#### 请求参数（Request Body）

| 字段 | 类型 | 必填 | 约束 | 说明 | 示例 |
|------|------|:----:|------|------|------|
| nickname | string | 否 | 2-50字符 | 昵称 | 阳光小明 |
| disability_type | string | 否 | 枚举白名单 | 残疾类型 | VISUAL_IMPAIRMENT |
| disability_level | string | 否 | 枚举白名单 | 残疾等级 | LEVEL_2 |

注意：所有字段均为选填，未传递的字段保持原值不变。

#### curl示例

```bash
curl -X PUT 'https://api.weiguangplus.com/api/v1/users/profile' \
  -H 'Authorization: Bearer eyJ...' \
  -H 'Content-Type: application/json' \
  -d '{"nickname":"阳光小明","disability_level":"LEVEL_2"}'
```

#### 成功响应 200 OK：返回更新后的完整user对象（同4.1的data结构）

---

### 4.3 获取健康档案 GET /api/v1/users/health-archive

获取过敏史、慢性病、当前用药等健康档案。

| 项目 | 值 |
|------|-----|
| URL | GET /api/v1/users/health-archive |
| Header | Authorization: Bearer token |

#### curl示例

```bash
curl -X GET 'https://api.weiguangplus.com/api/v1/users/health-archive' \
  -H 'Authorization: Bearer eyJ...'
```

#### 成功响应 200 OK

```json
{
  "code": 200, "message": "获取成功",
  "data": {
    "health_archive": {
      "user_id": 1,
      "allergy_history": ["青霉素", "磺胺类药物", "对乙酰氨基酚"],
      "chronic_diseases": ["高血压", "2型糖尿病"],
      "current_medications": [
        {"name":"阿司匹林肠溶片","dosage":"每日1片","frequency":"早餐后"},
        {"name":"盐酸二甲双胍片","dosage":"每次0.5g","frequency":"每日3次"}
      ],
      "blood_type": "A",
      "emergency_notes": "对花生严重过敏，随身携带肾上腺素笔",
      "updated_at": "2026-05-20T15:00:00.000Z"
    }
  }, "error_code": null, "timestamp": "..."
}
```

#### data Schema（HealthArchiveResponseData）

```typescript
interface HealthArchiveResponseData {
  health_archive: {
    user_id: number;
    allergy_history: string[];              // 过敏史列表
    chronic_diseases: string[];             // 慢性病史列表
    current_medications: MedicationItem[];  // 当前用药清单
    blood_type: string | null;              // 血型 A/B/AB/O
    emergency_notes: string | null;         // 紧急备注（最长500字符）
    updated_at: string;                     // 最后更新时间 ISO8601
  };
}

interface MedicationItem {
  name: string;        // 药品名称
  dosage: string;      // 用量描述
  frequency: string;   // 服用频次
}
```

---

### 4.4 更新健康档案 PUT /api/v1/users/health-archive

更新用户的健康档案信息（完全替换，非追加）。

| 项目 | 值 |
|------|-----|
| URL | PUT /api/v1/users/health-archive |
| Content-Type | application/json |
| Header | Authorization: Bearer token |

#### 请求参数（Request Body）

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|:----:|------|------|
| allergy_history | string[] | 否 | 最多20项 | 过敏史列表（完全替换） |
| chronic_diseases | string[] | 否 | 最多20项 | 慢性病史列表 |
| current_medications | array | 否 | 最多30项 | 当前用药清单 |
| blood_type | string | 否 | A/B/AB/O | 血型 |
| emergency_notes | string | 否 | 最长500字符 | 紧急备注 |

#### current_medications 数组元素Schema

| 字段 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| name | string | 是 | 药品名称 |
| dosage | string | 否 | 用量描述 |
| frequency | string | 否 | 服用频次 |

#### curl示例

```bash
curl -X PUT 'https://api.weiguangplus.com/api/v1/users/health-archive' \
  -H 'Authorization: Bearer eyJ...' \
  -H 'Content-Type: application/json' \
  -d '{
    "allergy_history":["青霉素","磺胺类药物","对乙酰氨基酚"],
    "chronic_diseases":["高血压","2型糖尿病"],
    "current_medications":[
      {"name":"阿司匹林肠溶片","dosage":"每日1片","frequency":"早餐后"},
      {"name":"盐酸二甲双胍片","dosage":"每次0.5g","frequency":"每日3次"}
    ],
    "blood_type":"A",
    "emergency_notes":"对花生严重过敏，随身携带肾上腺素笔"
  }'
```

#### 成功响应 200 OK：返回更新后的完整health_archive对象

---

### 4.5 上传头像 POST /api/v1/users/avatar

上传/更换用户头像图片。

| 项目 | 值 |
|------|-----|
| URL | POST /api/v1/users/avatar |
| Content-Type | multipart/form-data |
| Header | Authorization: Bearer token |
| 文件限制 | jpg/jpeg/png/webp；最大5MB |

#### 请求参数（Form Data）

| 字段 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| avatar | file | 是 | 头像图片 |

#### curl示例

```bash
curl -X POST 'https://api.weiguangplus.com/api/v1/users/avatar' \
  -H 'Authorization: Bearer eyJ...' \
  -F 'avatar=@/path/to/avatar.jpg'
```

#### 成功响应 200 OK

```json
{
  "code": 200, "message": "头像上传成功",
  "data": {
    "avatar_url": "https://cdn.weiguangplus.com/avatars/user_1_1716894200.jpg",
    "file_size": 245760, "width": 512, "height": 512
  }, "error_code": null, "timestamp": "..."
}
```

后端处理流程：
1. 校验文件类型和大小
2. Pillow裁剪压缩为512x512正方形
3. 上传至MinIO（bucket: avatars）
4. 更新数据库users.avatar_url字段
5. 返回CDN访问URL

---

## 5. 药品识别模块

> **模块路径**: `/api/v1/drugs/*`
> **认证要求**: 除药品搜索外均需要Bearer Token

### 5.1 上传药品识别记录 POST /api/v1/drugs/recognition

上传药盒照片进行OCR识别、药品匹配和风险评估。这是核心P0接口。

| 项目 | 值 |
|------|-----|
| URL | POST /api/v1/drugs/recognition |
| Content-Type | multipart/form-data |
| Header | Authorization: Bearer token |
| 频率限制 | 单用户每分钟10次 |
| 文件限制 | jpg/jpeg/png/webp；最大10MB |

#### 请求参数（Form Data）

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|:----:|------|------|
| image | file | 是 | 最大10MB | 药盒正面清晰照片 |
| ocr_text | string | 否 | 最大2000字符 | 前端预提取OCR文本（可选，加速匹配） |
| latitude | float | 否 | 6位小数 | 拍摄地点纬度(GCJ02) |
| longitude | float | 否 | 6位小数 | 拍摄地点经度(GCJ02) |

#### curl示例

```bash
curl -X POST 'https://api.weiguangplus.com/api/v1/drugs/recognition' \
  -H 'Authorization: Bearer eyJ...' \
  -F 'image=@/path/to/drug_photo.jpg' \
  -F 'ocr_text=泰诺 对乙酰氨基酚片 0.5g×12片 强生制药' \
  -F 'latitude=30.659462' \
  -F 'longitude=104.065735'
```

#### 成功响应 - 匹配到药品 201 Created

```json
{
  "code": 201, "message": "识别完成",
  "data": {
    "record_id": 1001,
    "matched_drug": {
      "id": 1004, "drug_name": "对乙酰氨基酚片", "brand_name": "泰诺林",
      "category": "解热镇痛药", "specification": "0.5g×12片",
      "manufacturer": "演示样本制药有限公司"
    },
    "risk_assessment": {
      "risk_level": "MEDIUM", "score": 65,
      "allergen_matches": [{
        "allergen": "对乙酰氨基酚", "severity": "HIGH",
        "description": "用户过敏史中包含此成分",
        "source_field": "allergy_history"
      }],
      "interaction_warnings": [
        "避免与酒精同时服用", "肝功能不全者慎用",
        "不可与其他含对乙酰氨基酚的药品同服"
      ],
      "recommendations": [
        "建议咨询医生或药师", "如出现皮疹应立即停药", "服药期间禁止饮酒"
      ]
    },
    "confidence_score": 0.92,
    "recognition_time_ms": 1250,
    "image_url": "https://cdn.weiguangplus.com/drug-photos/user_1_record_1001.jpg",
    "thumbnail_url": "https://cdn.weiguangplus.com/drug-photos/thumbs/user_1_record_1001.jpg",
    "ocr_raw_text": "泰诺林 对乙酰氨基酚片 0.5g×12片...",
    "created_at": "2026-05-28T12:00:00.000Z"
  }, "error_code": null, "timestamp": "..."
}
```

#### 成功响应 - 未匹配到药品 201 Created

```json
{
  "code": 201, "message": "识别完成，但未能匹配到已知药品",
  "data": {
    "record_id": 1002,
    "matched_drug": null, "risk_assessment": null,
    "confidence_score": 0.0, "recognition_time_ms": 850,
    "image_url": "...", "thumbnail_url": "...",
    "ocr_raw_text": "某未知药品包装文字...",
    "suggested_drugs": [
      {"id":1004,"drug_name":"对乙酰氨基酚片","similarity":0.35},
      {"id":1001,"drug_name":"布洛芬缓释胶囊","similarity":0.28}
    ],
    "created_at": "..."
  }, "error_code": null, "timestamp": "..."
}
```

#### data Schema（DrugRecognitionResponseData）

```typescript
interface DrugRecognitionResponseData {
  record_id: number;                              // 识别记录唯一ID
  matched_drug: MatchedDrugInfo | null;            // 匹配到的药品（null表示未匹配）
  risk_assessment: RiskAssessment | null;          // 风险评估结果（未匹配时为null）
  confidence_score: number;                        // 匹配置信度 0.0~1.0
  recognition_time_ms: number;                     // 识别耗时（毫秒）
  image_url: string;                               // 原图URL（MinIO CDN）
  thumbnail_url: string;                           // 缩略图URL（200x200）
  ocr_raw_text: string | null;                     // OCR原始提取文本
  suggested_drugs?: SuggestedDrug[];               // 未匹配时的推荐药品列表（可选）
  created_at: string;                              // 记录创建时间 ISO8601
}

interface MatchedDrugInfo {
  id: number;
  drug_name: string;           // 通用名
  brand_name: string | null;   // 商品名
  category: string;            // 分类
  specification: string;       // 规格
  manufacturer: string;        // 生产厂家
}

interface RiskAssessment {
  risk_level: "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
  score: number;                          // 风险评分 0~100
  allergen_matches: AllergenMatch[];      // 过敏原匹配列表
  interaction_warnings: string[];         // 药物相互作用警告
  recommendations: string[];              // 建议
}

interface AllergenMatch {
  allergen: string;              // 匹配到的过敏原名称
  severity: "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
  description: string;           // 详细说明
  source_field: string;          // 来源字段（allergy_history / chronic_diseases）
}

interface SuggestedDrug {
  id: number;
  drug_name: string;
  similarity: number;            // 相似度 0.0~1.0
}
```

#### 后端核心处理流程

```
接收图片+OCR文本
  Step1: 文件校验和MinIO存储（原图+生成200x200缩略图，quality=80%）
  Step2: OCR文本预处理（如果前端未传则后端执行OCR）
         文本清洗（去除空格、特殊符号）-> 分词提取关键词
  Step3: 四层药品模糊匹配引擎
         L1: 精确匹配（generic_name / trade_name / alias_names）
         L2: 拼音模糊匹配（pinyin_key / initials_key）
         L3: 编辑距离模糊匹配（search_tokens分词匹配）
         L4: 全文检索兜底
         输出: Top-1 匹配结果 + confidence_score
  Step4: 风险评估引擎（仅在匹配成功时执行）
         加载用户健康档案（allergy_history / chronic_diseases / current_medications）
         过敏原正向匹配：遍历药品 allergen_tags vs 用户 allergy_history
         药物相互作用检查：interaction_rules vs 用户 current_medications
         禁忌症/注意事项匹配
         综合加权计算 risk_level + score
  Step5: 持久化和返回
         写入 drug_recognition_records 表
         返回完整识别结果
```

---

### 5.2 查询识别历史记录 GET /api/v1/drugs/history

分页查询当前用户的药品识别历史记录。

| 项目 | 值 |
|------|-----|
| URL | GET /api/v1/drugs/history |
| Header | Authorization: Bearer token |

#### Query参数

| 参数 | 类型 | 默认值 | 取值范围 | 说明 |
|------|------|--------|---------|------|
| page | integer | 1 | >=1 | 页码 |
| size | integer | 20 | 1~100 | 每页数量 |
| start_date | string | - | YYYY-MM-DD | 开始日期（含） |
| end_date | string | - | YYYY-MM-DD | 结束日期（含） |
| risk_level | string | - | LOW/MEDIUM/HIGH/CRITICAL | 按风险等级过滤 |
| keyword | string | - | 最大50字符 | 按药品名关键字搜索 |
| bookmarked_only | boolean | false | - | 仅查收藏的记录 |

#### curl示例

```bash
# 基础查询
curl -X GET 'https://api.weiguangplus.com/api/v1/drugs/history?page=1&size=20' \
  -H 'Authorization: Bearer eyJ...'

# 按日期范围+风险等级过滤
curl -X GET 'https://api.weiguangplus.com/api/v1/drugs/history?start_date=2026-05-01&end_date=2026-05-28&risk_level=HIGH&size=10' \
  -H 'Authorization: Bearer eyJ...'
```

#### 成功响应 200 OK

```json
{
  "code": 200, "message": "查询成功",
  "data": {
    "total": 156, "page": 1, "size": 20, "has_more": true,
    "items": [{
      "record_id": 1001, "drug_id": 1004,
      "drug_name": "对乙酰氨基酚片", "brand_name": "泰诺林",
      "category": "解热镇痛药", "specification": "0.5g×12片",
      "risk_level": "MEDIUM", "confidence_score": 0.92,
      "thumbnail_url": "https://cdn.weiguangplus.com/drug-photos/thumbs/user_1_record_1001.jpg",
      "is_bookmarked": false, "user_notes": null,
      "recognition_time_ms": 1250,
      "created_at": "2026-05-28T12:00:00.000Z"
    }]
  }, "error_code": null, "timestamp": "..."
}
```

#### items元素Schema（DrugHistoryItem）

```typescript
interface DrugHistoryItem {
  record_id: number;
  drug_id: number | null;              // 匹配到的药品ID（null=未匹配）
  drug_name: string | null;            // 药品通用名
  brand_name: string | null;           // 商品名
  category: string | null;             // 药品分类
  specification: string | null;        // 规格
  risk_level: string | null;           // 风险等级
  confidence_score: number;            // 匹配置信度
  thumbnail_url: string;               // 缩略图URL
  is_bookmarked: boolean;              // 是否收藏
  user_notes: string | null;           // 用户备注
  recognition_time_ms: number;         // 识别耗时
  created_at: string;                  // 识别时间 ISO8601
}
```

---

### 5.3 获取药品详情 GET /api/v1/drugs/{drug_id}

根据药品ID获取完整详情 + 当前用户个性化风险标注。

| 项目 | 值 |
|------|-----|
| URL | GET /api/v1/drugs/{drug_id} |
| 路径参数 | drug_id: 药品主键ID(integer) |
| Header | Authorization: Bearer token |

#### curl示例

```bash
curl -X GET 'https://api.weiguangplus.com/api/v1/drugs/1004' \
  -H 'Authorization: Bearer eyJ...'
```

#### 成功响应 200 OK

```json
{
  "code": 200, "message": "获取成功",
  "data": {
    "drug": {
      "id": 1004, "drug_name": "对乙酰氨基酚片", "brand_name": "泰诺林",
      "alias_names": ["扑热息痛","必理通","泰诺","醋氨酚"],
      "category": "解热镇痛药", "dosage_form": "片剂",
      "specification": "0.5g×12片",
      "manufacturer": "演示样本制药有限公司",
      "approval_no": "国药准字H00001004",
      "ingredients": [{"name":"对乙酰氨基酚","amount":"0.5g"}],
      "indications": ["普通感冒或流行性感冒引起的发热","缓解轻至中度疼痛如头痛关节痛偏头痛牙痛"],
      "contraindications": ["严重肝肾功能不全者禁用","对本品过敏者禁用"],
      "precautions": ["服用本品期间不得饮酒或饮用含有酒精的饮料","不能同时服用其他含有解热镇痛药的药品","肝肾功能不全者慎用"],
      "adverse_reactions": ["偶见皮疹荨麻疹药热及粒细胞减少","长期大量用药会导致肝肾功能异常"],
      "interaction_text": "①应用巴比妥类或其他抗痉厥药的病人长期服用可导致依赖性；②与氯霉素同服可增强后者毒性",
      "pregnancy_risk": "B",
      "pediatric_use": "儿童用量请遵医嘱或按说明书使用",
      "elderly_use": "老年患者由于肝肾功能发生减退本品半衰期有所延长易发生不良反应应慎用或适当减量",
      "storage_method": "密封置阴凉干燥处保存",
      "valid_period": "36个月",
      "allergen_tags": ["对乙酰氨基酚","乙酰苯胺类"],
      "is_prescription": false,
      "tts_summary": "对乙酰氨基酚片商品名泰诺林用于感冒发热和止痛每次1片每日不超过3次注意不可超量服用。",
      "source_tag": "seed_demo"
    },
    "user_specific_info": {
      "is_allergic": true,
      "allergy_reason": "用户健康档案标记对'对乙酰氨基酚'过敏",
      "has_interaction_with_current_meds": true,
      "interaction_detail": "与您正在服用的'阿司匹林肠溶片'存在相互作用风险",
      "chronic_disease_warning": "您有'高血压'病史长期高剂量使用需监测血压"
    }
  }, "error_code": null, "timestamp": "..."
}
```

#### data Schema（DrugDetailWithUserInfo）

```typescript
interface DrugDetailWithUserInfo {
  drug: DrugDetail;
  user_specific_info: UserSpecificInfo;  // 个性化风险信息
}

interface DrugDetail {
  id: number;
  drug_name: string;                   // 通用名
  brand_name: string | null;           // 商品名
  alias_names: string[];               // 别名列表
  category: string;                    // 分类
  dosage_form: string | null;          // 剂型
  specification: string;               // 规格
  manufacturer: string;                // 生产厂家
  approval_no: string | null;          // 国药准字
  ingredients: Ingredient[];           // 成分列表
  indications: string[];               // 适应症
  contraindications: string[];         // 禁忌症
  precautions: string[];               // 注意事项
  adverse_reactions: string[];         // 不良反应
  interaction_text: string | null;     // 药物相互作用说明
  pregnancy_risk: string;              // 孕妇风险等级 A/B/C/D/X
  pediatric_use: string | null;        // 儿童用药说明
  elderly_use: string | null;          // 老年人用药说明
  storage_method: string | null;       // 贮藏方法
  valid_period: string | null;         // 有效期
  allergen_tags: string[];             // 过敏原标签
  is_prescription: boolean;            // 是否处方药
  tts_summary: string | null;          // TTS语音播报摘要
  source_tag: string;                  // 数据来源标签
}

interface Ingredient {
  name: string;      // 成分名称
  amount: string;    // 含量
}

interface UserSpecificInfo {
  is_allergic: boolean;                            // 是否对该药品过敏
  allergy_reason: string | null;                   // 过敏原因说明
  has_interaction_with_current_meds: boolean;      // 是否与当前用药有冲突
  interaction_detail: string | null;               // 冲突详情
  chronic_disease_warning: string | null;          // 慢性病相关警告
}
```

---

### 5.4 过敏原批量检查 POST /api/v1/drugs/allergen-check

批量检查多个药品是否与用户健康档案存在过敏或药物相互作用风险。

| 项目 | 值 |
|------|-----|
| URL | POST /api/v1/drugs/allergen-check |
| Content-Type | application/json |
| Header | Authorization: Bearer token |
| 频率限制 | 单用户每分钟30次 |

#### 请求参数（Request Body）

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|:----:|------|------|
| drug_ids | integer[] | 是 | 1~20个药品ID | 要检查的药品ID列表 |
| user_profile | object | 否 | 自定义档案 | 不传则使用当前登录用户的健康档案 |

#### user_profile 对象Schema（可选覆盖）

| 字段 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| allergy_history | string[] | 否 | 过敏史列表（覆盖用户档案） |
| chronic_diseases | string[] | 否 | 慢性病史列表 |
| current_medications | string[] | 否 | 当前用药名称列表 |

#### curl示例

```bash
# 使用当前登录用户档案检查
curl -X POST 'https://api.weiguangplus.com/api/v1/drugs/allergen-check' \
  -H 'Authorization: Bearer eyJ...' \
  -H 'Content-Type: application/json' \
  -d '{"drug_ids":[1004,1002,1001]}'

# 使用自定义档案检查（帮家人/朋友检查）
curl -X POST 'https://api.weiguangplus.com/api/v1/drugs/allergen-check' \
  -H 'Authorization: Bearer eyJ...' \
  -H 'Content-Type: application/json' \
  -d '{
    "drug_ids":[1004,1002,1001],
    "user_profile":{
      "allergy_history":["青霉素","磺胺类药物","碘"],
      "chronic_diseases":["高血压","糖尿病"],
      "current_medications":["阿司匹林","二甲双胍"]
    }
  }'
```

#### 成功响应 200 OK

```json
{
  "code": 200, "message": "检查完成",
  "data": {
    "results": [
      {
        "drug_id": 1002, "drug_name": "阿莫西林胶囊", "brand_name": "阿莫仙",
        "overall_risk": "CRITICAL",
        "matches": [{
          "type": "ALLERGEN",
          "detail": "含青霉素类成分（阿莫西林属于青霉素族抗生素），用户有青霉素过敏史",
          "severity": "CRITICAL",
          "matched_allergen": "青霉素",
          "matched_ingredient": "阿莫西林"
        }]
      },
      {
        "drug_id": 1004, "drug_name": "对乙酰氨基酚片",
        "overall_risk": "HIGH",
        "matches": [
          {"type":"ALLERGEN","detail":"用户过敏史中包含'对乙酰氨基酚'","severity":"HIGH","matched_allergen":"对乙酰氨基酚"},
          {"type":"INTERACTION","detail":"与当前用药'阿司匹林'同服增加出血风险","severity":"MEDIUM","related_medication":"阿司匹林"}
        ]
      },
      {
        "drug_id": 1001, "drug_name": "布洛芬缓释胶囊",
        "overall_risk": "LOW", "matches": []
      }
    ],
    "summary": {
      "total_checked": 3, "safe_count": 1, "warning_count": 0,
      "dangerous_count": 1, "critical_count": 1,
      "highest_risk": "CRITICAL",
      "recommendation": "发现极高风险！阿莫西林含青霉素成分您有青霉素过敏史绝对禁止使用！对乙酰氨基酚也存在过敏风险请谨慎使用或咨询医师。"
    }
  }, "error_code": null, "timestamp": "..."
}
```

#### data Schema（AllergenCheckResult）

```typescript
interface AllergenCheckResult {
  results: AllergenCheckResultItem[];
  summary: AllergenCheckSummary;
}

interface AllergenCheckResultItem {
  drug_id: number;
  drug_name: string;
  brand_name: string | null;
  overall_risk: "SAFE" | "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";  // 综合风险等级
  matches: MatchItem[];           // 风险匹配详情列表
}

interface MatchItem {
  type: "ALLERGEN" | "INTERACTION" | "CONTRAINDICATION" | "PRECAUTION";
  detail: string;                 // 风险详细描述
  severity: "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
  matched_allergen?: string;      // 匹配到的过敏原（仅ALLERGEN类型）
  matched_ingredient?: string;    // 匹配到的成分（仅ALLERGEN类型）
  related_medication?: string;    // 相关当前用药（仅INTERACTION类型）
}

interface AllergenCheckSummary {
  total_checked: number;          // 总检查数
  safe_count: number;             // 安全数量（SAFE/LOW）
  warning_count: number;          // 警告数量（MEDIUM）
  dangerous_count: number;        // 危险数量（HIGH）
  critical_count: number;         // 极危数量（CRITICAL）
  highest_risk: string;           // 最高风险等级
  recommendation: string;         // 综合建议文案（前端可直接展示）
}
```

---

### 5.5 收藏/取消收藏识别记录 POST /api/v1/drugs/{record_id}/bookmark

切换指定识别记录的收藏状态。

| 项目 | 值 |
|------|-----|
| URL | POST /api/v1/drugs/{record_id}/bookmark |
| Content-Type | application/json |
| Header | Authorization: Bearer token |

#### 路径参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| record_id | integer | 是 | 识别记录ID |

#### 请求参数（Request Body）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|:----:|------|------|
| bookmarked | boolean | 是 | 目标收藏状态 | true |

#### curl示例

```bash
# 收藏
curl -X POST 'https://api.weiguangplus.com/api/v1/drugs/1001/bookmark' \
  -H 'Authorization: Bearer eyJ...' -H 'Content-Type: application/json' \
  -d '{"bookmarked":true}'

# 取消收藏
curl -X POST 'https://api.weiguangplus.com/api/v1/drugs/1001/bookmark' \
  -H 'Authorization: Bearer eyJ...' -H 'Content-Type: application/json' \
  -d '{"bookmarked":false}'
```

#### 成功响应 200 OK

```json
{"code":200,"message":"收藏成功","data":{"record_id":1001,"bookmarked":true},"error_code":null,"timestamp":"..."}
```

---

### 5.6 药品模糊搜索 GET /api/v1/drugs/search

全局药品搜索接口（无需登录，供药品选择器使用）。

| 项目 | 值 |
|------|-----|
| URL | GET /api/v1/drugs/search |
| 认证要求 | 不需要Token（公开接口） |
| 频率限制 | IP维度每秒最多20次 |

#### Query参数

| 参数 | 类型 | 必填 | 默认值 | 取值范围 | 说明 |
|------|------|:----:|--------|---------|------|
| keyword | string | 是 | - | 1~50字符 | 搜索关键字（支持药品名/商品名/别名/拼音首字母） |
| page | integer | 否 | 1 | >=1 | 页码 |
| size | integer | 否 | 20 | 1~50 | 每页数量 |
| category | string | 否 | - | - | 按分类过滤 |

#### curl示例

```bash
# 关键字搜索
curl -X GET 'https://api.weiguangplus.com/api/v1/drugs/search?keyword=%E5%B8%83%E6%B4%9B%E8%8A%99&size=10'

# 拼音首字母搜索
curl -X GET 'https://api.weiguangplus.com/api/v1/drugs/search?keyword=blf&size=10'

# 按分类过滤
curl -X GET 'https://api.weiguangplus.com/api/v1/drugs/search?keyword=%E9%99%8D%E5%8E%8B&category=%E9%99%8D%E5%8E%8B%E8%8D%AF'
```

#### 成功响应 200 OK：标准分页格式，items为简化的药品摘要（id/drug_name/brand_name/category/specification/manufacturer）

---

## 6. 紧急联系人模块

> **模块路径**: `/api/v1/emergency-contacts/*`, `/api/v1/sos/*`
> **认证要求**: 所有接口均需要Bearer Token

### 6.1 添加紧急联系人 POST /api/v1/emergency-contacts

为当前用户添加一个紧急联系人。

| 项目 | 值 |
|------|-----|
| URL | POST /api/v1/emergency-contacts |
| Content-Type | application/json |
| Header | Authorization: Bearer token |
| 数量限制 | 每用户最多10个紧急联系人 |

#### 请求参数（Request Body）

| 字段 | 类型 | 必填 | 约束 | 说明 | 示例 |
|------|------|:----:|------|------|------|
| name | string | 是 | 2~20字符 | 联系人姓名 | 张三 |
| phone | string | 是 | 11位手机号 | 联系人手机号 | 13900139000 |
| relation | string | 否 | 最大20字符 | 与用户的关系 | 父亲 |
| priority | integer | 否 | 1~10 | 优先级（越小越优先） | 1 |
| is_enabled | boolean | 否 | - | 是否启用（默认true） | true |

#### curl示例

```bash
curl -X POST 'https://api.weiguangplus.com/api/v1/emergency-contacts' \
  -H 'Authorization: Bearer eyJ...' -H 'Content-Type: application/json' \
  -d '{"name":"张三","phone":"13900139000","relation":"父亲","priority":1,"is_enabled":true}'
```

#### 成功响应 201 Created

```json
{
  "code": 201, "message": "紧急联系人添加成功",
  "data": {
    "contact": {
      "id": 1, "user_id": 1, "name": "张三", "phone": "139****9000",
      "relation": "父亲", "priority": 1, "is_enabled": true,
      "created_at": "2026-05-28T13:00:00.000Z",
      "updated_at": "2026-05-28T13:00:00.000Z"
    }
  }, "error_code": null, "timestamp": "..."
}
```

#### 错误响应

| 场景 | code | error_code | message |
|------|------|-----------|---------|
| 手机号格式错误 | 400 | CONTACT_PHONE_INVALID | 联系人手机号格式不正确 |
| 达到数量上限 | 400 | CONTACT_LIMIT_EXCEEDED | 紧急联系人数量已达上限（最多10个） |
| 手机号重复 | 409 | CONTACT_PHONE_DUPLICATE | 该手机号的紧急联系人已存在 |

#### contact Schema（EmergencyContact）

```typescript
interface EmergencyContact {
  id: number;                    // 联系人记录ID
  user_id: number;               // 所属用户ID
  name: string;                  // 姓名
  phone: string;                 // 脱敏手机号
  relation: string | null;       // 关系
  priority: number;              // 优先级（1最优先）
  is_enabled: boolean;           // 是否启用
  created_at: string;            // ISO8601
  updated_at: string;            // ISO8601
}
```

---

### 6.2 获取紧急联系人列表 GET /api/v1/emergency-contacts

获取当前用户的所有紧急联系人，按priority升序排列。

| 项目 | 值 |
|------|-----|
| URL | GET /api/v1/emergency-contacts |
| Header | Authorization: Bearer token |

注意：此接口返回完整列表（非分页），因为紧急联系人数量有限（<=10个）。

#### curl示例

```bash
curl -X GET 'https://api.weiguangplus.com/api/v1/emergency-contacts' \
  -H 'Authorization: Bearer eyJ...'
```

#### 成功响应 200 OK

```json
{
  "code": 200, "message": "获取成功",
  "data": {
    "contacts": [
      {"id":1,"name":"张三","phone":"139****9000","relation":"父亲","priority":1,"is_enabled":true,"created_at":"...","updated_at":"..."},
      {"id":2,"name":"李四","phone":"138****8000","relation":"母亲","priority":2,"is_enabled":true,"created_at":"...","updated_at":"..."}
    ],
    "total": 2
  }, "error_code": null, "timestamp": "..."
}
```

---

### 6.3 更新紧急联系人 PUT /api/v1/emergency-contacts/{contact_id}

更新指定的紧急联系人信息（PATCH语义，只传需改的字段）。

| 项目 | 值 |
|------|-----|
| URL | PUT /api/v1/emergency-contacts/{contact_id} |
| Content-Type | application/json |
| Header | Authorization: Bearer token |

#### 路径参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| contact_id | integer | 是 | 联系人记录ID |

#### 请求参数（Request Body）

| 字段 | 类型 | 必填 | 约束 | 说明 | 示例 |
|------|------|:----:|------|------|------|
| name | string | 否 | 2~20字符 | 姓名 | 张国强 |
| phone | string | 否 | 11位手机号 | 手机号 | 13900139999 |
| relation | string | 否 | 最大20字符 | 关系 | 爸爸 |
| priority | integer | 否 | 1~10 | 优先级 | 1 |
| is_enabled | boolean | 否 | - | 是否启用 | true |

注意：所有字段均为选填（PATCH语义），未传递的字段保持原值不变。

#### curl示例

```bash
curl -X PUT 'https://api.weiguangplus.com/api/v1/emergency-contacts/1' \
  -H 'Authorization: Bearer eyJ...' -H 'Content-Type: application/json' \
  -d '{"name":"张国强","phone":"13900139999","relation":"爸爸"}'
```

#### 成功响应 200 OK：返回更新后的完整contact对象

#### 错误响应 404 Not Found：CONTACT_NOT_FOUND

---

### 6.4 删除紧急联系人 DELETE /api/v1/emergency-contacts/{contact_id}

删除指定的紧急联系人。

| 项目 | 值 |
|------|-----|
| URL | DELETE /api/v1/emergency-contacts/{contact_id} |
| Header | Authorization: Bearer token |
| 请求体 | 无 |

#### 路径参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| contact_id | integer | 是 | 联系人记录ID |

#### curl示例

```bash
curl -X DELETE 'https://api.weiguangplus.com/api/v1/emergency-contacts/1' \
  -H 'Authorization: Bearer eyJ...'
```

#### 成功响应 200 OK

```json
{"code":200,"message":"紧急联系人删除成功","data":null,"error_code":null,"timestamp":"..."}
```

---

### 6.5 排序紧急联系人 PUT /api/v1/emergency-contacts/sort

批量调整紧急联系人的排列顺序。

| 项目 | 值 |
|------|-----|
| URL | PUT /api/v1/emergency-contacts/sort |
| Content-Type | application/json |
| Header | Authorization: Bearer token |

#### 请求参数（Request Body）

| 字段 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| contact_ids | integer[] | 是 | 按新顺序排列的联系人ID列表 |

#### curl示例

```bash
curl -X PUT 'https://api.weiguangplus.com/api/v1/emergency-contacts/sort' \
  -H 'Authorization: Bearer eyJ...' -H 'Content-Type: application/json' \
  -d '{"contact_ids":[2,1,3]}'
```

#### 成功响应 200 OK

```json
{
  "code": 200, "message": "排序成功",
  "data": {
    "sorted_contacts": [
      {"id":2,"name":"李四","priority":1},
      {"id":1,"name":"张三","priority":2},
      {"id":3,"name":"王五","priority":3}
    ]
  }, "error_code": null, "timestamp": "..."
}
```

---

### 6.6 创建SOS事件 POST /api/v1/sos/events

创建一个新的SOS紧急求助事件，后端自动向启用的紧急联系人发送短信通知。

| 项目 | 值 |
|------|-----|
| URL | POST /api/v1/sos/events |
| Content-Type | application/json |
| Header | Authorization: Bearer token |
| 频率限制 | 单用户每小时最多20次 |

#### 请求参数（Request Body）

| 字段 | 类型 | 必填 | 约束 | 说明 | 示例 |
|------|------|:----:|------|------|------|
| scenario_type | string | 是 | 枚举值 | 求助场景类型 | ILLNESS |
| severity | string | 否 | 枚举值，默认HIGH | 严重程度 | HIGH |
| latitude | float | 否 | 6位小数 | 纬度（GCJ02） | 30.659462 |
| longitude | float | 否 | 6位小数 | 经度（GCJ02） | 104.065735 |
| location_address | string | 否 | 最大200字符 | 地址描述 | 成都市高新区天府大道中段 |
| accuracy | float | 否 | 正数 | 定位精度（米） | 15.5 |
| description | string | 否 | 最大500字符 | 事件补充描述 | 突发胸闷呼吸困难 |
| notify_contacts | boolean | 否 | 默认true | 是否自动通知紧急联系人 | true |

#### scenario_type 枚举值

| 值 | 含义 | 建议的前端UI文案 |
|---|------|-----------------|
| ILLNESS | 突发疾病 | 我身体不适 |
| LOST | 迷路走失 | 我迷路了 |
| ACCIDENT | 交通事故 | 遇到交通事故 |
| OTHER | 其他情况 | 其他紧急情况 |

#### curl示例

```bash
curl -X POST 'https://api.weiguangplus.com/api/v1/sos/events' \
  -H 'Authorization: Bearer eyJ...' -H 'Content-Type: application/json' \
  -d '{
    "scenario_type":"ILLNESS","severity":"HIGH",
    "latitude":30.659462,"longitude":104.065735,
    "location_address":"成都市高新区天府大道中段",
    "accuracy":15.5,
    "description":"突发胸闷，呼吸困难",
    "notify_contacts":true
  }'
```

#### 成功响应 201 Created

```json
{
  "code": 201, "message": "SOS事件已创建，正在通知紧急联系人",
  "data": {
    "event": {
      "id": 5001, "user_id": 1,
      "scenario_type": "ILLNESS", "severity": "HIGH",
      "latitude": 30.659462, "longitude": 104.065735,
      "location_address": "成都市高新区天府大道中段",
      "accuracy": 15.5, "description": "突发胸闷，呼吸困难",
      "status": "ACTIVE",
      "notified_contacts": [1, 2], "sms_sent_count": 2,
      "created_at": "2026-05-28T15:00:00.000Z"
    },
    "notification_result": {
      "success_count": 2, "failed_count": 0,
      "details": [
        {"contact_id":1,"contact_name":"张三","status":"SENT"},
        {"contact_id":2,"contact_name":"李四","status":"SENT"}
      ]
    }
  }, "error_code": null, "timestamp": "..."
}
```

#### 错误响应 400 Bad Request

```json
{"code":400,"message":"尚未设置紧急联系人无法发送SOS求助","data":null,"error_code":"NO_EMERGENCY_CONTACT","timestamp":"..."}
```

#### SOS短信模板

【微光畅行紧急求助】{用户昵称}发起紧急求助。
场景：{场景类型中文}（{严重程度}）
时间：{创建时间}
位置：{地址描述}（纬度:{lat}, 经度:{lng}，精度正负{accuracy}米）
备注：{补充描述}
请尽快联系TA！-- 来自微光同行APP

#### data Schema（SosEventResult）

```typescript
interface SosEventResult {
  event: SosEvent;
  notification_result: NotificationResult;
}

interface SosEvent {
  id: number;
  user_id: number;
  scenario_type: "ILLNESS" | "LOST" | "ACCIDENT" | "OTHER";
  severity: "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
  latitude: number | null;
  longitude: number | null;
  location_address: string | null;
  accuracy: number | null;
  description: string | null;
  status: "ACTIVE" | "RESOLVED" | "CANCELLED";
  notified_contacts: number[];
  sms_sent_count: number;
  resolved_at: string | null;
  resolution_note: string | null;
  created_at: string;
}

interface NotificationResult {
  success_count: number;
  failed_count: number;
  details: NotificationDetail[];
}

interface NotificationDetail {
  contact_id: number;
  contact_name: string;
  status: "SENT" | "FAILED" | "SKIPPED";
  fail_reason?: string;
}
```

#### 后端SOS处理流程

```
接收SOS创建请求
  Step1: 参数校验和用户状态检查（校验必填字段/检查是否有紧急联系人/检查账号状态）
  Step2: 创建SOS事件记录（写入sos_events表 status=ACTIVE）
  Step3: 获取启用的紧急联系人列表（查询emergency_contacts表 is_enabled=true 按priority升序排列）
  Step4: 逐个发送短信通知（SMS模板异步发送 使用SmsManager 记录每个联系人的通知状态）
  Step5: 更新事件记录（更新notified_contacts和sms_sent_count 返回完整事件信息和通知结果）
```

---

### 6.7 查询SOS历史事件 GET /api/v1/sos/events

分页查询当前用户的SOS历史事件。

| 项目 | 值 |
|------|-----|
| URL | GET /api/v1/sos/events |
| Header | Authorization: Bearer token |

#### Query参数

| 参数 | 类型 | 默认值 | 取值范围 | 说明 |
|------|------|--------|---------|------|
| page | integer | 1 | >=1 | 页码 |
| size | integer | 20 | 1~50 | 每页数量 |
| status | string | - | ACTIVE/RESOLVED/CANCELLED | 按状态过滤 |
| scenario_type | string | - | ILLNESS/LOST/ACCIDENT/OTHER | 按场景类型过滤 |
| start_date | string | - | YYYY-MM-DD | 开始日期 |
| end_date | string | - | YYYY-MM-DD | 结束日期 |

#### curl示例

```bash
curl -X GET 'https://api.weiguangplus.com/api/v1/sos/events?page=1&size=20&status=RESOLVED' \
  -H 'Authorization: Bearer eyJ...'
```

#### 成功响应 200 OK：标准分页格式，items包含SOS事件摘要（含scenario_type_label/severity_label等展示用字段）

---

### 6.8 解决SOS事件 PUT /api/v1/sos/events/{sos_id}/resolve

将指定SOS事件标记为已解决。

| 项目 | 值 |
|------|-----|
| URL | PUT /api/v1/sos/events/{sos_id}/resolve |
| Content-Type | application/json |
| Header | Authorization: Bearer token |

#### 路径参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| sos_id | integer | 是 | SOS事件ID |

#### 请求参数（Request Body）

| 字段 | 类型 | 必填 | 约束 | 说明 | 示例 |
|------|------|:----:|------|------|------|
| resolution_note | string | 否 | 最大500字符 | 解决备注 | 已送医治疗情况稳定 |

#### curl示例

```bash
curl -X PUT 'https://api.weiguangplus.com/api/v1/sos/events/5001/resolve' \
  -H 'Authorization: Bearer eyJ...' -H 'Content-Type: application/json' \
  -d '{"resolution_note":"已送医治疗情况稳定"}'
```

#### 成功响应 200 OK

```json
{"code":200,"message":"SOS事件已标记为解决","data":{"event_id":5001,"status":"RESOLVED","resolved_at":"...","resolution_note":"已送医治疗情况稳定"},"error_code":null,"timestamp":"..."}
```

#### 错误响应 409 Conflict：SOS_EVENT_ALREADY_RESOLVED

---

### 6.9 取消SOS事件 PUT /api/v1/sos/events/{sos_id}/cancel

取消一个仍在进行中的SOS事件（误触等情况）。

| 项目 | 值 |
|------|-----|
| URL | PUT /api/v1/sos/events/{sos_id}/cancel |
| Content-Type | application/json |
| Header | Authorization: Bearer token |

#### 路径参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| sos_id | integer | 是 | SOS事件ID |

#### 请求参数（Request Body）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|:----:|------|------|
| cancel_reason | string | 否 | 取消原因 | 误触实际无紧急情况 |

#### curl示例

```bash
curl -X PUT 'https://api.weiguangplus.com/api/v1/sos/events/5002/cancel' \
  -H 'Authorization: Bearer eyJ...' -H 'Content-Type: application/json' \
  -d '{"cancel_reason":"误触实际无紧急情况"}'
```

#### 成功响应 200 OK

```json
{"code":200,"message":"SOS事件已取消","data":{"event_id":5002,"status":"CANCELLED","cancelled_at":"...","cancel_reason":"误触实际无紧急情况"},"error_code":null,"timestamp":"..."}
```

#### 错误响应 409 Conflict：只能取消进行中的SOS事件（非RESOLVED/CANCELLED状态）

---

## 7. 文件上传模块

> **模块路径**: `/api/v1/files/*`
> **底层存储**: MinIO对象存储
> **认证要求**: 所有接口均需要Bearer Token

### 7.1 上传通用文件 POST /api/v1/files/upload

通用文件上传接口，支持图片、文档等多种文件类型。

| 项目 | 值 |
|------|-----|
| URL | POST /api/v1/files/upload |
| Content-Type | multipart/form-data |
| Header | Authorization: Bearer token |
| 频率限制 | 单用户每分钟最多10次 |

#### Form Data参数

| 字段 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| file | file | 是 | 待上传文件 |
| category | string | 否 | 文件分类（avatar/drug_photo/document/other） |
| public | boolean | 否 | 是否公开访问（默认false） |

#### 文件类型限制

| category | 允许格式 | 大小限制 | 存储路径 |
|----------|---------|---------|---------|
| avatar | jpg/jpeg/png/webp | 小于等于5MB | avatars/ |
| drug_photo | jpg/jpeg/png/webp | 小于等于10MB | drug-photos/ |
| document | pdf/doc/docx/xls/xlsx | 小于等于20MB | documents/ |
| other | 见全局白名单 | 小于等于20MB | uploads/ |

#### curl示例

```bash
# 上传头像
curl -X POST 'https://api.weiguangplus.com/api/v1/files/upload' \
  -H 'Authorization: Bearer eyJ...' \
  -F 'file=@/path/to/avatar.png' -F 'category=avatar' -F 'public=true'

# 上传药品照片
curl -X POST 'https://api.weiguangplus.com/api/v1/files/upload' \
  -H 'Authorization: Bearer eyJ...' \
  -F 'file=@/path/to/drug_photo.jpg' -F 'category=drug_photo'
```

#### 成功响应 200 OK

```json
{
  "code": 200, "message": "文件上传成功",
  "data": {
    "file_key": "drug-photos/user_1_1716894800.jpg",
    "file_url": "https://cdn.weiguangplus.com/drug-photos/user_1_1716894800.jpg",
    "file_name": "drug_photo.jpg", "file_size": 1048576, "file_type": "image/jpeg",
    "width": 4032, "height": 3024,
    "thumbnail_url": "https://cdn.weiguangplus.com/drug-photos/thumbs/user_1_1716894800.jpg",
    "category": "drug_photo", "public": false
  }, "error_code": null, "timestamp": "..."
}
```

#### data Schema（FileUploadResponseData）

```typescript
interface FileUploadResponseData {
  file_key: string;           // MinIO对象键（唯一标识）
  file_url: string;           // 文件访问URL（CDN域名）
  file_name: string;          // 原始文件名
  file_size: number;          // 文件大小（字节）
  file_type: string;          // MIME类型
  width?: number;            // 图片宽度（仅图片）
  height?: number;           // 图片高度（仅图片）
  thumbnail_url?: string;    // 缩略图URL（仅图片）
  category: string;          // 文件分类
  public: boolean;           // 是否公开
}
```

#### 后端处理流程

```
接收文件
  Step1: 文件校验（Magic Number校验不依赖扩展名 + 文件大小检查）
  Step2: 生成文件路径（格式: {category}/{user_id}_{unix_timestamp}.{ext}）
  Step3: 图片处理（如果是图片则生成缩略图max 200x200 quality 80% 可选EXIF GPS信息提取）
  Step4: 上传至MinIO（选择目标Bucket公开/私有 设置Content-Type）
  Step5: 返回文件信息（file_key + file_url + thumbnail_url）
```

---

### 7.2 获取预签名URL POST /api/v1/files/presigned-url

生成MinIO文件的预签名下载/上传URL（用于客户端直传或私有文件临时访问）。

| 项目 | 值 |
|------|-----|
| URL | POST /api/v1/files/presigned-url |
| Content-Type | application/json |
| Header | Authorization: Bearer token |

#### 请求参数（Request Body）

| 字段 | 类型 | 必填 | 约束 | 说明 | 示例 |
|------|------|:----:|------|------|------|
| file_key | string | 是 | 有效存在的file_key | 文件对象键 | drug-photos/user_1_1001.jpg |
| operation | string | 是 | upload/download | 操作类型 | download |
| expires_in | integer | 否 | 60~3600秒默认3600 | URL有效期（秒） | 600 |

#### curl示例

```bash
curl -X POST 'https://api.weiguangplus.com/api/v1/files/presigned-url' \
  -H 'Authorization: Bearer eyJ...' -H 'Content-Type: application/json' \
  -d '{"file_key":"drug-photos/user_1_1001.jpg","operation":"download","expires_in":600}'
```

#### 成功响应 200 OK

```json
{
  "code": 200, "message": "预签名URL生成成功",
  "data": {
    "presigned_url": "https://minio.weiguangplus.com/drug-photos/user_1_1001.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&...",
    "expires_at": "2026-05-28T18:10:00.000Z",
    "expires_in": 600
  }, "error_code": null, "timestamp": "..."
}
```

---

### 7.3 删除文件 DELETE /api/v1/files/{file_key}

删除MinIO中的指定文件。

| 项目 | 值 |
|------|-----|
| URL | DELETE /api/v1/files/{file_key} |
| Header | Authorization: Bearer token |

#### 路径参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| file_key | string(URL编码) | 是 | 文件对象键（需URL编码斜杠变为%2F） |

#### curl示例

```bash
# file_key中的斜杠需要URL编码为 %2F
curl -X DELETE 'https://api.weiguangplus.com/api/v1/files/drug-photos%2Fuser_1_1001.jpg' \
  -H 'Authorization: Bearer eyJ...'
```

#### 成功响应 200 OK

```json
{"code":200,"message":"文件删除成功","data":null,"error_code":null,"timestamp":"..."}
```

---

## 8. 附录

### 8.1 数据库表结构速查

#### users（用户表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | SERIAL | PK | 主键 |
| phone | VARCHAR(20) | UNIQUE NOT NULL | 手机号（登录账号） |
| password_hash | VARCHAR(255) | NOT NULL | 密码哈希（bcrypt） |
| nickname | VARCHAR(50) | | 昵称 |
| avatar_url | VARCHAR(500) | | 头像URL（MinIO） |
| disability_type | VARCHAR(50) | | 残疾类型 |
| disability_level | VARCHAR(20) | | 残疾等级 |
| allergy_history | TEXT[] | | 过敏史（PostgreSQL数组） |
| chronic_diseases | TEXT[] | | 慢性病史 |
| current_medications | JSONB | | 当前用药清单 |
| blood_type | CHAR(2) | | 血型 |
| emergency_notes | TEXT | | 紧急备注 |
| is_active | BOOLEAN | DEFAULT TRUE | 账号状态 |
| created_at | TIMESTAMPTZ | DEFAULT NOW() | 创建时间 |
| updated_at | TIMESTAMPTZ | DEFAULT NOW() | 更新时间 |

#### drugs（药品主表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | SERIAL | PK | 主键 |
| drug_name | VARCHAR(200) | NOT NULL | 通用名 |
| brand_name | VARCHAR(200) | | 商品名 |
| alias_names | TEXT[] | | 别名列表 |
| category | VARCHAR(50) | | 分类 |
| dosage_form | VARCHAR(50) | | 剂型 |
| specification | VARCHAR(100) | | 规格 |
| manufacturer | VARCHAR(200) | | 生产厂家 |
| approval_no | VARCHAR(50) | | 国药准字 |
| ingredients | JSONB | | 成分列表[{name amount}] |
| indications | TEXT[] | | 适应症 |
| contraindications | TEXT[] | | 禁忌症 |
| precautions | TEXT[] | | 注意事项 |
| adverse_reactions | TEXT[] | | 不良反应 |
| interaction_text | TEXT | | 相互作用说明 |
| pregnancy_risk | VARCHAR(20) | | 孕妇风险等级(A-X) |
| pediatric_use | TEXT | | 儿童用药 |
| elderly_use | TEXT | | 老年用药 |
| storage_method | TEXT | | 贮藏方法 |
| valid_period | TEXT | | 有效期 |
| allergen_tags | TEXT[] | | 过敏原标签 |
| is_prescription | BOOLEAN | DEFAULT FALSE | 是否处方药 |
| tts_summary | TEXT | | TTS语音摘要 |
| source_tag | VARCHAR(50) | | 数据来源 |
| created_at | TIMESTAMPTZ | DEFAULT NOW() | |
| updated_at | TIMESTAMPTZ | DEFAULT NOW() | |

#### drug_recognition_records（识别记录表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | SERIAL | PK | 主键 |
| user_id | INTEGER | FK->users(id) | 用户ID |
| image_url | VARCHAR(500) | | 原图URL |
| thumbnail_url | VARCHAR(500) | | 缩略图URL |
| ocr_raw_text | TEXT | | OCR原始文本 |
| matched_drug_id | INTEGER | FK->drugs(id) | 匹配药品ID |
| confidence_score | FLOAT | | 匹配置信度(0-1) |
| risk_level | VARCHAR(10) | | 风险等级 |
| risk_details | JSONB | | 详细风险信息 |
| allergen_matches | JSONB | | 过敏原匹配 |
| interaction_warnings | TEXT[] | | 相互作用警告 |
| is_bookmarked | BOOLEAN | DEFAULT FALSE | 是否收藏 |
| user_notes | TEXT | | 用户备注 |
| latitude | FLOAT | | 拍摄纬度 |
| longitude | FLOAT | | 拍摄经度 |
| recognition_time_ms | FLOAT | | 识别耗时(毫秒) |
| created_at | TIMESTAMPTZ | DEFAULT NOW() | |

#### emergency_contacts（紧急联系人表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | SERIAL | PK | 主键 |
| user_id | INTEGER | FK->users(id) | 所属用户 |
| name | VARCHAR(50) | NOT NULL | 姓名 |
| phone | VARCHAR(20) | NOT NULL | 手机号 |
| relation | VARCHAR(20) | | 关系 |
| priority | INTEGER | DEFAULT 0 | 优先级(越小越优先) |
| is_enabled | BOOLEAN | DEFAULT TRUE | 是否启用 |
| created_at | TIMESTAMPTZ | DEFAULT NOW() | |
| updated_at | TIMESTAMPTZ | DEFAULT NOW() | |

#### sos_events（SOS事件表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | SERIAL | PK | 主键 |
| user_id | INTEGER | FK->users(id) | 用户ID |
| scenario_type | VARCHAR(50) | NOT NULL | 场景类型 |
| severity | VARCHAR(20) | DEFAULT HIGH | 严重程度 |
| latitude | FLOAT | | 纬度 |
| longitude | FLOAT | | 经度 |
| location_address | TEXT | | 地址描述 |
| accuracy | FLOAT | | 定位精度(米) |
| description | TEXT | | 补充描述 |
| status | VARCHAR(20) | DEFAULT ACTIVE | 状态 |
| notified_contacts | INTEGER[] | | 已通知联系人ID |
| sms_sent_count | INTEGER | DEFAULT 0 | 发送短信数 |
| resolution_note | TEXT | | 解决备注 |
| resolved_at | TIMESTAMPTZ | | 解决时间 |
| cancelled_at | TIMESTAMPTZ | | 取消时间 |
| cancel_reason | TEXT | | 取消原因 |
| created_at | TIMESTAMPTZ | DEFAULT NOW() | |

### 8.2 前端Retrofit完整接口定义（Kotlin）

以下为Android端可直接使用的Retrofit ApiService接口定义代码：

```kotlin
import com.squareup.multipart.Multipart
import com.squareup.multipart.Part
import retrofit2.Response
import retrofit2.http.*

/**
 * 微光同行API服务接口定义
 * 
 * 基础URL: https://api.weiguangplus.com/api/v1/
 * 统一响应封装: ApiResponse<T>
 * 
 * 使用方式:
 * val apiService = Retrofit.Builder()
 *     .baseUrl("https://api.weiguangplus.com/api/v1/")
 *     .addConverterFactory(GsonConverterFactory.create())
 *     .build()
 *     .create(WeiguangApiService::class.java)
 */
interface WeiguangApiService {

    // ==================== 认证模块 ====================

    /** 用户注册 */
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<UserInfo>>

    /** 用户登录 */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthData>>

    /** 刷新AccessToken */
    @POST("auth/refresh")
    suspend fun refreshAccessToken(@Body request: RefreshTokenRequest): Response<ApiResponse<TokenData>>

    /** 退出登录 */
    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>

    // ==================== 用户信息模块 ====================

    /** 获取个人信息 */
    @GET("users/profile")
    suspend fun getUserProfile(): Response<ApiResponse<UserProfile>>

    /** 更新个人信息 */
    @PUT("users/profile")
    suspend fun updateUserProfile(@Body request: UpdateProfileRequest): Response<ApiResponse<UserProfile>>

    /** 获取健康档案 */
    @GET("users/health-archive")
    suspend fun getHealthArchive(): Response<ApiResponse<HealthArchive>>

    /** 更新健康档案 */
    @PUT("users/health-archive")
    suspend fun updateHealthArchive(@Body request: UpdateHealthArchiveRequest): Response<ApiResponse<HealthArchive>

    /** 上传头像 */
    @POST("users/avatar")
    @Multipart
    suspend fun uploadAvatar(@Part avatar: MultipartBody.Part): Response<ApiResponse<AvatarInfo>>

    /** 修改密码 */
    @PUT("users/password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ApiResponse<Unit>>

    // ==================== 药品识别模块 ====================

    /** 上传药品识别记录（核心P0接口）*/
    @Multipart
    @POST("drugs/recognition")
    suspend fun uploadRecognitionRecord(
        @Part image: MultipartBody.Part,
        @Part ocrText: MultipartBody.Part?,
        @Part("latitude") latitude: Double?,
        @Part("longitude") longitude: Double?
    ): Response<ApiResponse<DrugRecognitionResult>>

    /** 查询识别历史记录（分页）*/
    @GET("drugs/history")
    suspend fun getRecognitionHistory(
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("risk_level") riskLevel: String? = null,
        @Query("keyword") keyword: String? = null,
        @Query("bookmarked_only") bookmarkedOnly: Boolean? = null
    ): Response<ApiResponse<PaginatedData<DrugHistoryItem>>>

    /** 获取药品详情（含个性化风险标注）*/
    @GET("drugs/{drugId}")
    suspend fun getDrugDetail(@Path("drugId") drugId: Int): Response<ApiResponse<DrugDetailWithUserInfo>>

    /** 批量过敏原检查 */
    @POST("drugs/allergen-check")
    suspend fun checkAllergens(@Body request: AllergenCheckRequest): Response<ApiResponse<AllergenCheckResult>>

    /** 收藏/取消收藏识别记录 */
    @POST("drugs/{recordId}/bookmark")
    suspend fun toggleBookmark(
        @Path("recordId") recordId: Int,
        @Body request: BookmarkRequest
    ): Response<ApiResponse<BookmarkResult>>

    /** 药品模糊搜索（公开接口无需Token）*/
    @GET("drugs/search")
    suspend fun searchDrugs(
        @Query("keyword") keyword: String,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("category") category: String? = null
    ): Response<ApiResponse<PaginatedData<DrugSearchItem>>>

    // ==================== 紧急联系人模块 ====================

    /** 添加紧急联系人 */
    @POST("emergency-contacts")
    suspend fun addEmergencyContact(@Body request: AddContactRequest): Response<ApiResponse<ContactInfo>>

    /** 获取紧急联系人列表（全部非分页）*/
    @GET("emergency-contacts")
    suspend fun getEmergencyContacts(): Response<ApiResponse<ContactListResult>>

    /** 更新紧急联系人 */
    @PUT("emergency-contacts/{contactId}")
    suspend fun updateEmergencyContact(
        @Path("contactId") contactId: Int,
        @Body request: UpdateContactRequest
    ): Response<ApiResponse<ContactInfo>>

    /** 删除紧急联系人 */
    @DELETE("emergency-contacts/{contactId}")
    suspend fun deleteEmergencyContact(@Path("contactId") contactId: Int): Response<ApiResponse<Unit>>

    /** 批量排序紧急联系人 */
    @PUT("emergency-contacts/sort")
    suspend fun sortEmergencyContacts(@Body request: SortContactsRequest): Response<ApiResponse<SortResult>>

    // ==================== SOS事件模块 ====================

    /** 创建SOS紧急求助事件 */
    @POST("sos/events")
    suspend fun createSosEvent(@Body request: CreateSosEventRequest): Response<ApiResponse<SosEventResult>>

    /** 查询SOS历史事件（分页）*/
    @GET("sos/events")
    suspend fun getSosEvents(
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("status") status: String? = null,
        @Query("scenario_type") scenarioType: String? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): Response<ApiResponse<PaginatedData<SosEventItem>>)

    /** 解决SOS事件 */
    @PUT("sos/events/{sosId}/resolve")
    suspend fun resolveSosEvent(
        @Path("sosId") sosId: Int,
        @Body request: ResolveSosRequest
    ): Response<ApiResponse<ResolveSosResult>>

    /** 取消SOS事件 */
    @PUT("sos/events/{sosId}/cancel")
    suspend fun cancelSosEvent(
        @Path("sosId") sosId: Int,
        @Body request: CancelSosRequest
    ): Response<ApiResponse<CancelSosResult>>
}
```

### 8.3 后端Pydantic Schema速查（Python）

以下为后端FastAPI项目中常用的Pydantic模型定义参考：

```python
from typing import Optional, List, Generic, TypeVar
from pydantic import BaseModel, Field, field_validator
from enum import Enum
from datetime import datetime

# ==================== 通用响应模型 ====================

class ApiResponse(BaseModel, Generic[T]):
    """统一API响应包装"""
    code: int = Field(..., description="业务状态码")
    message: str = Field(..., description="状态描述")
    data: Optional[T] = Field(None, description="业务数据载荷")
    error_code: Optional[str] = Field(None, description="精细错误码")
    timestamp: datetime = Field(..., description="服务器响应时间戳")

class PaginatedData(BaseModel, Generic[T]):
    """通用分页数据"""
    total: int = Field(..., description="总记录数")
    page: int = Field(..., ge=1, description="当前页码")
    size: int = Field(..., ge=1, le=100, description="每页数量")
    has_more: bool = Field(..., description="是否有下一页")
    items: List[T] = Field(default_factory=list, description="当前页数据")

# ==================== 认证模块 Schema ====================

class RegisterRequest(BaseModel):
    """注册请求"""
    phone: str = Field(..., pattern=r"^1[3-9]\d{9}$", description="手机号")
    password: str = Field(..., min_length=8, max_length=20, description="密码")
    nickname: Optional[str] = Field(None, min_length=2, max_length=50, description="昵称")
    disability_type: Optional[str] = Field(None, description="残疾类型")
    disability_level: Optional[str] = Field(None, description="残疾等级")

class LoginRequest(BaseModel):
    """登录请求"""
    phone: str = Field(..., pattern=r"^1[3-9]\d{9}$")
    password: str = Field(..., min_length=8, max_length=20)

class RefreshTokenRequest(BaseModel):
    """Token刷新请求"""
    refresh_token: str = Field(..., description="Refresh Token")

class ChangePasswordRequest(BaseModel):
    """修改密码请求"""
    old_password: str = Field(..., min_length=8, max_length=20)
    new_password: str = Field(..., min_length=8, max_length=20)

# ==================== 用户模块 Schema ====================

class UpdateProfileRequest(BaseModel):
    """更新个人信息请求（PATCH语义）"""
    nickname: Optional[str] = Field(None, min_length=2, max_length=50)
    disability_type: Optional[str] = None
    disability_level: Optional[str] = None

class MedicationItem(BaseModel):
    """用药条目"""
    name: str = Field(..., description="药品名称")
    dosage: Optional[str] = Field(None, description="用量")
    frequency: Optional[str] = Field(None, description="频次")

class UpdateHealthArchiveRequest(BaseModel):
    """更新健康档案请求"""
    allergy_history: Optional[List[str]] = Field(None, max_length=20)
    chronic_diseases: Optional[List[str]] = Field(None, max_length=20)
    current_medications: Optional[List[MedicationItem]] = Field(None, max_length=30)
    blood_type: Optional[str] = Field(None, pattern=r"^[ABOab]$|^AB$")
    emergency_notes: Optional[str] = Field(None, max_length=500)

# ==================== 紧急联系人 Schema ====================

class AddContactRequest(BaseModel):
    """添加紧急联系人请求"""
    name: str = Field(..., min_length=2, max_length=20)
    phone: str = Field(..., pattern=r"^1[3-9]\d{9}$")
    relation: Optional[str] = Field(None, max_length=20)
    priority: Optional[int] = Field(1, ge=1, le=10)
    is_enabled: Optional[bool] = True

class UpdateContactRequest(BaseModel):
    """更新紧急联系人请求（PATCH语义）"""
    name: Optional[str] = Field(None, min_length=2, max_length=20)
    phone: Optional[str] = Field(None, pattern=r"^1[3-9]\d{9}$")
    relation: Optional[str] = Field(None, max_length=20)
    priority: Optional[int] = Field(None, ge=1, le=10)
    is_enabled: Optional[bool] = None

class SortContactsRequest(BaseModel):
    """排序紧急联系人请求"""
    contact_ids: List[int] = Field(..., min_length=1, description="按新顺序排列的联系人ID列表")

# ==================== SOS事件 Schema ====================

class CreateSosEventRequest(BaseModel):
    """创建SOS事件请求"""
    scenario_type: str = Field(..., description="场景类型 ILLNESS/LOST/ACCIDENT/OTHER")
    severity: Optional[str] = Field("HIGH", description="严重程度 LOW/MEDIUM/HIGH/CRITICAL")
    latitude: Optional[float] = Field(None, description="纬度GCJ02")
    longitude: Optional[float] = Field(None, description="经度GCJ02")
    location_address: Optional[str] = Field(None, max_length=200, description="地址描述")
    accuracy: Optional[float] = Field(None, gt=0, description="定位精度米")
    description: Optional[str] = Field(None, max_length=500, description="补充描述")
    notify_contacts: Optional[bool] = True

class ResolveSosRequest(BASE):
    """解决SOS事件请求"""
    resolution_note: Optional[str] = Field(None, max_length=500, description="解决备注")

class CancelSosRequest(BaseModel):
    """取消SOS事件请求"""
    cancel_reason: Optional[str] = Field(None, description="取消原因")

# ==================== 药品模块 Schema ====================

class AllergenCheckRequest(BaseModel):
    """过敏原批量检查请求"""
    drug_ids: List[int] = Field(..., min_length=1, max_length=20, description="药品ID列表")
    user_profile: Optional['UserProfileForCheck'] = Field(None, description="自定义档案")

class UserProfileForCheck(BaseModel):
    """自定义用户档案（用于帮他人检查）*/
    allergy_history: Optional[List[str]] = None
    chronic_diseases: Optional[List[str]] = None
    current_medications: Optional[List[str]] = None

class BookmarkRequest(BaseModel):
    """收藏/取消收藏请求*/
    bookmarked: bool = Field(..., description="目标收藏状态")
```

### 8.4 接口联调检查清单

#### 前端Android开发者自检清单

- [ ] RetrofitBaseUrl配置正确（区分dev/test/prod环境）
- [ ] OkHttp拦截器链正确配置（AuthInterceptor -> LoggingInterceptor -> TokenRefreshInterceptor）
- [ ] TokenManager能正确保存/读取/清除Token（EncryptedSharedPreferences）
- [ ] 所有网络请求在ViewModel的viewModelScope.launch中发起
- [ ] 401错误能正确触发Token刷新并重试
- [ ] Refresh Token失效时能正确跳转登录页
- [ ] 文件上传使用@Multipart注解且Part name与服务端一致
- [ ] 分页加载逻辑正确处理has_more字段
- [ ] 时间字段正确从ISO8601转换为本地时区展示
- [ ] 手机号显示使用服务端返回的脱敏格式
- [ ] 风险等级颜色映射与文档一致
- [ ] 网络异常时展示友好提示（非系统Toast）

#### 后端Python开发者自检清单

- [ ] FastAPI应用启动正常可访问 /docs Swagger页面
- [ ] CORS中间件允许前端域名跨域
- [ ] JWT密钥从环境变量读取（非硬编码）
- [ ] bcrypt salt rounds=12
- [ ] Access Token过期时间=30分钟 Refresh Token=7天
- [ ] 数据库连接池配置正确（asyncpg + async engine）
- [ ] MinIO连接测试通过（health check）
- [ ] 所有接口返回统一JSON格式（code/message/data/error_code/timestamp）
- [ ] 文件上传做了Magic Number校验（不信任扩展名）
- [ ] 图片上传自动生成200x200缩略图
- [ ] 分页接口返回has_more字段
- [ ] 频率限制中间件生效（可用ab压测验证）
- [ ] Alembic迁移脚本可正常执行
- [ ] 敏感信息（手机号）在响应中始终脱敏
- [ ] 错误日志记录完整（含request_id便于追踪）

#### 联调关键路径测试

```
1. 注册流程: POST /register -> 201 -> 记录返回的phone(脱敏)是否正确
2. 登录流程: POST /login -> 200 -> 保存access_token和refresh_token
3. 受保护接口: GET /profile + Authorization header -> 200
4. Token过期等待或手动篡改 -> 401 -> 触发刷新 -> 重试成功
5. 药品识别: POST /drugs/recognition (multipart) -> 201 -> 检查risk_assessment完整性
6. 过敏原检查: POST /drugs/allergen-check -> 200 -> 检查summary.recommendation文案
7. SOS触发: POST /sos/events -> 201 -> 检查notification_result.details
8. 文件上传: POST /files/upload -> 200 -> 用返回的file_url能否访问图片
```

---

> **文档维护信息**
> 
> **最后更新**: 2026-05-28
> **维护者**: 微光同行多Agent开发团队
> **适用版本**: API v1.0.0
> **变更日志**:
> - v1.0.0 (2026-05-28): 初始版本，定义全部P0接口契约