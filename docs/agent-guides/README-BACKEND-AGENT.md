# 🔵 后端开发Agent 工作规范

> **分支**: `feature/backend`
> **技术栈**: Python FastAPI + PostgreSQL + MinIO
> **负责人**: [待分配]
> **状态**: 🚀 开发中
> **最后更新**: 2026-05-28

---

## 📋 角色定位

你是**微光同行项目的核心后端工程师**，负责构建整个系统的服务端基础设施，包括：
- 用户认证与授权系统
- 药品数据管理与智能匹配引擎
- 文件存储服务（药盒照片、用户头像等）
- 紧急求助与位置追踪
- 社区服务预约系统
- 数据分析与推送服务

你的代码质量直接决定整个APP的稳定性、性能和可扩展性。

---

## 🎯 Sprint 1 核心任务（第1-2周）

### 优先级：P0 - 必须完成（阻塞其他Agent）

#### 任务1.1：FastAPI项目基础架构搭建 ⏱️ 预计：0.5天

**目标**：建立可运行的后端服务骨架

**交付物**：
```bash
# 项目目录结构
weiguangplus-backend/
├── app/
│   ├── __init__.py
│   ├── main.py                 # FastAPI应用入口
│   ├── config.py               # 配置管理（环境变量）
│   ├── dependencies.py         # 依赖注入（DB会话等）
│   │
│   ├── api/                    # API路由层
│   │   ├── __init__.py
│   │   ├── v1/
│   │   │   ├── __init__.py
│   │   │   ├── auth.py         # 认证接口
│   │   │   ├── users.py        # 用户接口
│   │   │   ├── drugs.py        # 药品接口
│   │   │   └── emergency.py    # 应急接口
│   │   └── deps.py             # 公共依赖
│   │
│   ├── core/                   # 核心业务逻辑
│   │   ├── __init__.py
│   │   ├── auth.py             # JWT认证逻辑
│   │   ├── security.py         # 密码哈希等安全工具
│   │   └── exceptions.py       # 自定义异常
│   │
│   ├── models/                 # SQLAlchemy ORM模型
│   │   ├── __init__.py
│   │   ├── user.py            # 用户模型
│   │   ├── drug.py            # 药品模型
│   │   └── base.py            # 基础模型类
│   │
│   ├── schemas/               # Pydantic请求/响应模型
│   │   ├── __init__.py
│   │   ├── user.py
│   │   ├── drug.py
│   │   └── common.py          # 通用响应格式
│   │
│   ├── services/              # 业务服务层
│   │   ├── __init__.py
│   │   ├── user_service.py
│   │   └── drug_service.py
│   │
│   └── utils/                  # 工具函数
│       ├── __init__.py
│       └── logger.py          # 日志配置
│
├── alembic/                   # 数据库迁移工具
│   ├── versions/
│   └── env.py
│
├── tests/                     # 测试代码
│   ├── conftest.py
│   ├── test_auth.py
│   └── test_drugs.py
│
├── requirements.txt           # Python依赖
├── Dockerfile                 # 容器化部署
├── docker-compose.yml         # 编排PostgreSQL+MinIO+App
└── README.md                  # 本文件
```

**具体要求**：
- [ ] 使用 `fastapi` 框架，`uvicorn` ASGI服务器
- [ ] 配置CORS中间件（允许前端跨域访问）
- [ ] 配置全局异常处理（返回统一JSON错误格式）
- [ ] 集成Swagger UI文档（访问 `/docs`）
- [ ] 配置结构化日志（使用 `loguru` 或 `logging`）
- [ ] 支持环境变量配置（使用 `python-dotenv`）

**验收标准**：
```bash
# 启动服务
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000

# 访问健康检查
curl http://localhost:8000/health
# 返回: {"status": "ok", "version": "0.1.0"}

# 访问API文档
# 浏览器打开 http://localhost:8000/docs
```

---

#### 任务1.2：PostgreSQL数据库设计与ORM实现 ⏱️ 预计：1天

**目标**：设计完整的数据库Schema并实现ORM映射

**核心表设计**：

##### 表1：users（用户表）
```sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    phone VARCHAR(20) UNIQUE NOT NULL,           -- 手机号（登录账号）
    password_hash VARCHAR(255) NOT NULL,          -- 密码哈希（bcrypt）
    nickname VARCHAR(50),                        -- 昵称
    avatar_url VARCHAR(500),                     -- 头像URL（MinIO）
    disability_type VARCHAR(50),                -- 残疾类型（视障/听障/肢障等）
    disability_level VARCHAR(20),               -- 残疾等级（一级/二级/三级/四级）
    
    -- 健康档案
    allergy_history TEXT[],                      -- 过敏史（JSON数组）
    chronic_diseases TEXT[],                     -- 慢性病史
    current_medications JSONB,                  -- 当前用药清单
    
    -- 紧急联系人
    emergency_contacts JSONB DEFAULT '[]',      -- 紧急联系人列表
    
    -- 系统字段
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_users_phone ON users(phone);
```

##### 表2：drugs（药品主表）
```sql
CREATE TABLE drugs (
    id SERIAL PRIMARY KEY,
    drug_name VARCHAR(200) NOT NULL,             -- 通用名
    brand_name VARCHAR(200),                     -- 商品名
    alias_names TEXT[],                          -- 别名列表（用于OCR模糊匹配）
    
    -- 分类信息
    category VARCHAR(50),                        -- 药品分类（感冒/止痛/降压/降糖等）
    dosage_form VARCHAR(50),                     -- 剂型（片剂/胶囊/注射液等）
    specification VARCHAR(100),                  -- 规格（如"10mg*12片"）
    manufacturer VARCHAR(200),                   -- 生产厂家
    
    -- 成分信息
    ingredients JSONB,                          -- 成分列表 [{name, amount}]
    
    -- 适应症与禁忌
    indications TEXT[],                          -- 适应症
    contraindications TEXT[],                    -- 禁忌症
    precautions TEXT[],                          -- 注意事项
    adverse_reactions TEXT[],                    -- 不良反应
    
    -- 特殊人群用药
    pregnancy_risk VARCHAR(20),                 -- 孕妇风险等级（A/B/C/D/X）
    pediatric_use TEXT,                          -- 儿童用药说明
    elderly_use TEXT,                            -- 老年人用药说明
    
    -- 过敏原标签
    allergen_tags TEXT[],                       -- 过敏原标签（青霉素/磺胺/碘等）
    interaction_rules JSONB,                    -- 药物相互作用规则
    
    -- 系统字段
    is_prescription BOOLEAN DEFAULT FALSE,      -- 是否处方药
    data_source VARCHAR(50) DEFAULT 'manual',   -- 数据来源
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_drugs_name ON drugs(drug_name);
CREATE INDEX idx_drugs_brand ON drugs(brand_name);
CREATE INDEX idx_drugs_category ON drugs(category);
CREATE INDEX idx_drugs_allergen ON drugs USING GIN(allergen_tags);
```

##### 表3：drug_recognition_records（识别记录表）
```sql
CREATE TABLE drug_recognition_records (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    
    -- 识别结果
    image_url VARCHAR(500),                     -- 原始图片URL（MinIO）
    ocr_raw_text TEXT,                           -- OCR原始文本
    matched_drug_id INTEGER REFERENCES drugs(id),-- 匹配到的药品ID
    confidence_score FLOAT,                      -- 匹配置信度 (0-1)
    
    -- 风险评估结果
    risk_level VARCHAR(10),                      -- 风险等级（LOW/MEDIUM/HIGH/CRITICAL）
    risk_details JSONB,                         -- 详细风险信息
    allergen_matches JSONB,                      -- 匹配到的过敏原
    interaction_warnings TEXT[],                -- 药物相互作用警告
    
    -- 用户操作
    is_bookmarked BOOLEAN DEFAULT FALSE,        -- 是否收藏
    user_notes TEXT,                             -- 用户备注
    
    -- 系统字段
    recognition_time FLOAT,                      -- 识别耗时（秒）
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_records_user ON drug_recognition_records(user_id);
CREATE INDEX idx_records_time ON drug_recognition_records(created_at);
```

##### 表4：sos_events（SOS事件表）
```sql
CREATE TABLE sos_events (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    
    -- SOS信息
    scenario_type VARCHAR(50) NOT NULL,          -- 场景类型（疾病/迷路/事故/其他）
    severity VARCHAR(20) DEFAULT 'HIGH',         -- 严重程度（LOW/MEDIUM/HIGH/CRITICAL）
    
    -- 位置信息
    latitude FLOAT,
    longitude FLOAT,
    location_address TEXT,                       -- 地址描述
    accuracy FLOAT,                              -- 定位精度（米）
    
    -- 联系人通知
    notified_contacts INTEGER[] DEFAULT '{}',     -- 已通知的联系人ID列表
    sms_sent_count INTEGER DEFAULT 0,            -- 发送短信数量
    
    -- 状态
    status VARCHAR(20) DEFAULT 'ACTIVE',         -- ACTIVE/RESOLVED/CANCELLED
    resolved_at TIMESTAMP WITH TIME ZONE,
    resolution_note TEXT,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_sos_user ON sos_events(user_id);
CREATE INDEX idx_sos_status ON sos_events(status);
CREATE INDEX idx_sos_time ON sos_events(created_at);
```

**具体要求**：
- [ ] 使用SQLAlchemy ORM定义所有模型
- [ ] 实现 `BaseModel` 基类（包含id、created_at、updated_at通用字段）
- [ ] 编写Alembic迁移脚本（初始建库 + 后续变更）
- [ ] 创建数据库连接池配置（异步engine + session管理）
- [ ] 编写数据初始化脚本（从CSV导入药品数据到PostgreSQL）

**验收标准**：
```bash
# 执行数据库迁移
alembic upgrade head

# 验证表创建成功
psql -U postgres -d weiguangplus -c "\dt"
# 应该看到: users, drugs, drug_recognition_records, sos_events 等表

# 导入测试数据
python tools/import_drug_data.py
# 验证数据导入
psql -c "SELECT COUNT(*) FROM drugs;" 
# 预期: > 500条记录
```

---

#### 任务1.3：用户认证模块 ⏱️ 预计：1天

**目标**：实现完整的注册、登录、Token刷新流程

**API接口设计**：

##### POST /api/v1/auth/register - 用户注册
```json
// Request
{
    "phone": "13800138000",
    "password": "YourSecurePassword123",
    "nickname": "小明",
    "disability_type": "视障",
    "disability_level": "一级"
}

// Response 201 Created
{
    "code": 201,
    "message": "注册成功",
    "data": {
        "user": {
            "id": 1,
            "phone": "138****8000",
            "nickname": "小明",
            "created_at": "2026-05-28T10:00:00Z"
        }
    }
}

// Error 400 Bad Request
{
    "code": 400,
    "message": "手机号已被注册",
    "error_code": "PHONE_ALREADY_EXISTS"
}
```

##### POST /api/v1/auth/login - 用户登录
```json
// Request
{
    "phone": "13800138000",
    "password": "YourSecurePassword123"
}

// Response 200 OK
{
    "code": 200,
    "message": "登录成功",
    "data": {
        "access_token": "eyJhbGciOiJIUzI1NiIs...",
        "refresh_token": "eyJhbGciOiJIUzI1NiIs...",
        "token_type": "bearer",
        "expires_in": 3600,
        "user": {
            "id": 1,
            "phone": "138****8000",
            "nickname": "小明",
            "avatar_url": null
        }
    }
}
```

##### POST /api/v1/auth/refresh - 刷新Token
```json
// Request
{
    "refresh_token": "eyJhbGciOiJIUzI1NiIs..."
}

// Response 200 OK
{
    "code": 200,
    "data": {
        "access_token": "new_access_token...",
        "expires_in": 3600
    }
}
```

##### PUT /api/v1/users/password - 修改密码
```json
// Request (需Authorization Header)
{
    "old_password": "OldPassword123",
    "new_password": "NewSecurePassword456"
}
```

**具体要求**：
- [ ] 使用 `python-jose` 库实现JWT Token生成和验证
- [ ] Access Token有效期：**30分钟**
- [ ] Refresh Token有效期：**7天**（支持自动续期）
- [ ] 密码使用 `bcrypt` 哈希存储（salt rounds=12）
- [ ] 手机号脱敏显示（138****8000）
- [ ] 实现Token黑名单机制（用户登出时失效）
- [ ] 接口权限装饰器：`@require_auth`

**验收标准**：
```bash
# 注册新用户
curl -X POST http://localhost:8000/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"phone":"13900139000","password":"test123456"}'

# 登录获取Token
curl -X POST http://localhost:8000/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phone":"13900139000","password":"test123456"}'
# 保存返回的 access_token

# 使用Token访问受保护接口
curl http://localhost:8000/api/v1/users/profile \
  -H "Authorization: Bearer <your_access_token>"
# 应返回用户个人信息

# 测试Token过期（等待30分钟或手动篡改Token）
# 应返回 401 Unauthorized
```

---

#### 任务1.4：药品识别相关接口 ⏱️ 预计：1.5天

**目标**：实现药品OCR结果存储、查询、风险匹配的核心业务逻辑

**API接口设计**：

##### POST /api/v1/drugs/recognition - 上传识别记录
```json
// Request (multipart/form-data)
// Fields:
//   - image: 图片文件（药盒照片）
//   - ocr_text: OCR提取的原始文本（可选，前端已预提取）
// Headers:
//   - Authorization: Bearer <token>

// Response 201 Created
{
    "code": 201,
    "data": {
        "record_id": 1001,
        "matched_drug": {
            "id": 42,
            "drug_name": "对乙酰氨基酚片",
            "brand_name": "泰诺",
            "category": "解热镇痛",
            "specification": "0.5g×12片"
        },
        "risk_assessment": {
            "risk_level": "MEDIUM",
            "allergen_matches": [
                {
                    "allergen": "对乙酰氨基酚过敏",
                    "severity": "HIGH",
                    "description": "用户过敏史中包含此成分"
                }
            ],
            "interaction_warnings": [
                "避免与酒精同时服用",
                "肝功能不全者慎用"
            ],
            "recommendations": [
                "建议咨询医生或药师",
                "如出现皮疹应立即停药"
            ]
        },
        "confidence_score": 0.92
    }
}
```

##### GET /api/v1/drugs/history - 查询历史记录
```json
// Query Parameters:
//   - page: 页码（默认1）
//   - size: 每页数量（默认20）
//   - start_date: 开始日期（可选，YYYY-MM-DD）
//   - end_date: 结束日期（可选）
//   - risk_level: 风险等级过滤（可选：LOW/MEDIUM/HIGH/CRITICAL）

// Response 200 OK
{
    "code": 200,
    "data": {
        "total": 156,
        "page": 1,
        "size": 20,
        "items": [
            {
                "record_id": 1001,
                "drug_name": "对乙酰氨基酚片",
                "brand_name": "泰诺",
                "risk_level": "MEDIUM",
                "recognition_time": "2026-05-28T15:30:00Z",
                "image_thumbnail": "http://minio:9000/bucket/thumbs/xxx.jpg"
            }
        ]
    }
}
```

##### GET /api/v1/drugs/{drug_id} - 获取药品详情
```json
// Response 200 OK
{
    "code": 200,
    "data": {
        "drug": {
            "id": 42,
            "drug_name": "对乙酰氨基酚片",
            "brand_name": "泰诺",
            "alias_names": ["扑热息痛", "必理通", "泰诺林"],
            "category": "解热镇痛",
            "manufacturer": "强生制药",
            "indications": ["普通感冒", "流行性感冒引起的发热"],
            "contraindications": ["严重肝肾功能不全者禁用"],
            "pregnancy_risk": "B",
            "ingredients": [
                {"name": "对乙酰氨基酚", "amount": "0.5g"}
            ]
        },
        "user_specific_info": {
            "is_allergic": true,
            "allergy_reason": "用户标记对'对乙酰氨基酚'过敏",
            "has_interaction_with_current_meds": false
        }
    }
}
```

##### POST /api/v1/drugs/allergen-check - 过敏原批量检查
```json
// Request
{
    "drug_ids": [42, 56, 78],  // 要检查的药品ID列表
    "user_profile": {           // 可选，不传则使用当前登录用户档案
        "allergy_history": ["青霉素", "磺胺类药物"],
        "chronic_diseases": ["高血压", "糖尿病"],
        "current_medications": ["阿司匹林"]
    }
}

// Response 200 OK
{
    "code": 200,
    "data": {
        "results": [
            {
                "drug_id": 42,
                "drug_name": "阿莫西林胶囊",
                "overall_risk": "CRITICAL",
                "matches": [
                    {
                        "type": "ALLERGEN",
                        "detail": "含青霉素类成分，用户有青霉素过敏史",
                        "severity": "CRITICAL"
                    }
                ]
            }
        ],
        "summary": {
            "safe_count": 1,
            "warning_count": 1,
            "dangerous_count": 1,
            "recommendation": "⚠️ 发现严重风险！请勿同时服用以上药品"
        }
    }
}
```

**具体要求**：
- [ ] MinIO文件上传集成（图片存储）
- [ ] 药品名称模糊匹配算法（编辑距离 + 同义词词典）
- [ ] 过敏原规则引擎（正向匹配 + 反向排除）
- [ ] 风险等级计算逻辑（基于用户画像加权评分）
- [ ] 图片缩略图生成（用于历史记录列表展示）
- [ ] 分页查询优化（大数据量场景）

**验收标准**：
```bash
# 上传测试图片
curl -X POST http://localhost:8000/api/v1/drugs/recognition \
  -H "Authorization: Bearer <token>" \
  -F "image=@test_drug_photo.jpg"

# 验证返回的risk_assessment包含合理数据
# confidence_score应在0.8-1.0之间

# 查询历史记录
curl "http://localhost:8000/api/v1/drugs/history?page=1&size=10" \
  -H "Authorization: Bearer <token>"
# 验证分页参数正确
```

---

### 优先级：P1 - Sprint 1 可选（时间充裕再做）

#### 任务1.5：紧急联系人 & SOS模块 ⏱️ 预计：1天

**API接口**：
- `POST /api/v1/emergency-contacts` - 添加紧急联系人
- `GET /api/v1/emergency-contacts` - 获取联系人列表
- `PUT /api/v1/emergency-contacts/{id}` - 更新联系人
- `DELETE /api/v1/emergency-contacts/{id}` - 删除联系人
- `POST /api/v1/sos/events` - 创建SOS事件
- `GET /api/v1/sos/events` - 查询SOS历史
- `PUT /api/v1/sos/events/{id}/resolve` - 解决SOS事件

#### 任务1.6：MinIO文件存储集成 ⏱️ 预计：0.5天

**要求**：
- [ ] 配置MinIO客户端连接
- [ ] 实现文件上传/下载/删除/预签名URL生成
- [ ] 自动生成图片缩略图
- [ ] Bucket策略配置（公开/私有分离）

---

## 🔒 开发约束（必须遵守）

### ✅ 允许的操作

1. 在 `feature/backend` 分支内自由开发
2. 从 `main` 分支定期合并最新代码
3. 创建子分支进行功能开发（命名规范：`feature/backend-xxx`）
4. 编写单元测试（目标覆盖率 >= 80%）
5. 添加详细的中文注释

### ❌ 禁止的操作

1. **禁止修改其他Agent负责的文件**（前端UI、Android代码、算法模型）
2. **禁止直接推送到 `main` 分支**（必须通过PR合并）
3. **禁止提交敏感信息**（密码、Token、私钥必须用环境变量）
4. **禁止在代码中硬编码数据库密码或API Key**

### 🔄 协作协议

当需要与其他Agent协作时：

**输出模板**：
```
【后端接口 + feature/backend + 新增XXX接口】

请求方: 后端开发Agent
接收方: 前端开发Agent / 算法Agent
优先级: P0/P1/P2
接口路径: POST /api/v1/xxx
请求格式: {JSON示例}
响应格式: {JSON示例}
错误码: {错误码列表}
验收标准: 如何验证接口正确性
截止时间: YYYY-MM-DD
```

**示例**：
```
【用户认证接口 + feature/backend + JWT认证体系v1】

请求方: 后端开发Agent
接收方: 前端开发Agent
优先级: P0
接口路径: 
  - POST /api/v1/auth/register
  - POST /api/v1/auth/login
  - POST /api/v1/auth/refresh
请求格式: 见上方详细示例
响应格式: 统一格式 {code, message, data, error_code}
错误码: 
  - 400: 参数错误
  - 401: 未认证/Token过期
  - 403: 无权限
  - 409: 资源冲突（如手机号已存在）
  - 500: 服务器内部错误
验收标准: 
  1. 可成功注册/登录
  2. Token有效期为30分钟
  3. Refresh Token可正常续期
截止时间: Sprint 1 结束前
```

---

## 📝 提交规范

### Commit Message 格式

采用 Conventional Commits 中文版：

```
<type>(<scope>): <简短描述>

<空行>

<详细说明（可选）:

<影响范围>:

<关联Issue>: #123
```

**Type类型**：
- `feat`: 新功能
- `fix`: Bug修复
- `docs`: 文档更新
- `style`: 代码格式调整（不影响逻辑）
- `refactor`: 重构（非新功能、非修复）
- `perf`: 性能优化
- `test`: 测试相关
- `chore`: 构建/工具链/依赖

**Scope示例**：
- `auth`: 认证相关
- `users`: 用户相关
- `drugs`: 药品相关
- `db`: 数据库相关
- `storage`: 文件存储相关

**示例**：
```
feat(auth): 实现JWT双Token认证体系

- Access Token: 30分钟有效期
- Refresh Token: 7天有效期，支持自动续期
- 密码使用bcrypt哈希（salt rounds=12）
- 新增Token黑名单登出机制

影响范围: 所有需要认证的接口
关联Issue: #backend-auth-001
```

---

## 🧪 测试要求

### 单元测试框架
```bash
# 安装测试依赖
pip install pytest pytest-cov pytest-asyncio httpx

# 运行测试
pytest tests/ -v --cov=app --cov-report=html

# 目标覆盖率 >= 80%
```

### 必须编写的测试用例

| 模块 | 最少测试用例数 | 关键覆盖点 |
|------|---------------|-----------|
| auth | 15+ | 正常注册/重复手机号/弱密码/Token过期/Refresh |
| users | 10+ | CRUD操作/数据验证/权限检查 |
| drugs | 20+ | 模糊匹配/过敏原检查/分页查询/风险计算 |
| emergency | 8+ | SOS创建/联系人管理/位置上传 |

### 接口自测清单

每个接口完成后必须手动测试：

```bash
# 正常请求
curl -X POST <url> -H "Content-Type: application/json" -d '<valid_json>'
# 预期: 200/201 + 合理数据

# 缺少必填字段
curl -X POST <url> -H "Content-Type: application/json" -d '{}'
# 预期: 422 Validation Error

# 未认证访问
curl <protected_url>
# 预期: 401 Unauthorized

# 异常数据类型
curl -X POST <url> -H "Content-Type: application/json" -d '{"phone": 123}'
# 预期: 422 Validation Error
```

---

## 📦 依赖清单

### requirements.txt
```txt
# Web框架
fastapi==0.104.1
uvicorn[standard]==0.24.0
python-multipart==0.0.6

# 数据库
sqlalchemy[asyncio]==2.0.23
asyncpg==0.29.0
alembic==1.12.1
psycopg2-binary==2.9.9

# 认证与安全
python-jose[cryptography]==3.3.0
passlib[bcrypt]==1.7.4

# 数据验证
pydantic[email]==2.5.2
pydantic-settings==2.1.0

# 文件存储
minio==7.2.0
Pillow==10.0.0  # 图片处理

# 工具库
python-dotenv==1.0.0
loguru==0.7.2
orjson==3.9.10  # 高性能JSON序列化

# 开发工具
pytest==7.4.4
pytest-asyncio==0.21.1
pytest-cov==4.1.0
httpx==0.25.2  # 异步HTTP客户端（测试用）
```

### docker-compose.yml（本地开发用）
```yaml
version: '3.8'

services:
  db:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: weiguangplus
      POSTGRES_USER: weiguangplus
      POSTGRES_PASSWORD: weiguangplus_dev_2026
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  minio:
    image: minio/minio:latest
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin123
    ports:
      - "9000:9000"
      - "9001:9001"
    volumes:
      - minio_data:/data

  app:
    build: .
    command: uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
    ports:
      - "8000:8000"
    depends_on:
      - db
      - minio
    environment:
      DATABASE_URL: postgresql+asyncpg://weiguangplus:weiguangplus_dev_2026@db:5432/weiguangplus
      MINIO_ENDPOINT: minio:9000
      MINIO_ACCESS_KEY: minioadmin
      MINIO_SECRET_KEY: minioadmin123
      SECRET_KEY: your-super-secret-key-change-in-production-2026
    volumes:
      - .:/app

volumes:
  postgres_data:
  minio_data:
```

---

## 🚀 快速启动指南

### 方式A：Docker Compose（推荐✅）
```bash
# 一键启动所有服务（PostgreSQL + MinIO + API）
docker-compose up -d --build

# 查看日志
docker-compose logs -f app

# 访问API文档
open http://localhost:8000/docs

# 停止服务
docker-compose down
```

### 方式B：本地安装（无Docker时）
```bash
# 创建虚拟环境
python -m venv venv
source venv/bin/activate  # Linux/Mac
# 或 venv\Scripts\activate  # Windows

# 安装依赖
pip install -r requirements.txt

# 启动PostgreSQL（确保已安装并运行）
# 默认连接: postgresql://postgres:postgres@localhost:5432/weiguangplus

# 初始化数据库
alembic upgrade head

# 导入药品数据
python tools/init_sample_data.py

# 启动API服务
uvicorn app.main:app --reload --port 8000
```

---

## 📊 进度汇报模板

每个核心模块完成后提交：

```markdown
## 后端开发进度报告

**模块**: XXX模块
**Agent**: 后端开发Agent
**分支**: feature/backend
**时间**: 2026-MM-DD HH:MM

### 当前进度
- [x] 已完成任务1
- [ ] 进行中任务2（完成度: 60%）
- [ ] 待开始任务3

### 产出物
- 新增文件: X个
- 修改文件: Y个
- 代码行数: +XXXX / -YY
- API接口: 新增N个

### 测试结果
- 单元测试: XX passed, YY failed
- 覆盖率: ZZ%
- 性能基准: 平均响应 < XXms

### 风险与阻塞
- 风险1: [描述及应对]
- 阻塞项: [需要哪个Agent的支持]

### 下一步计划
1. ...
2. ...
```

---

## 🔗 相关链接

- **GitHub仓库主页面**: https://github.com/saber463/----
- **完整项目文档**: ../docs/ （40+文档）
- **技术栈定稿**: ../docs/2026-05-19-微光畅行-最终定稿完整版技术栈.md
- **P0模块文档**: ../docs/2026-05-19-P0核心模块开发文档.md
- **数据库设计**: ../docs/药品离线库SQL与Room骨架-v1.md

---

## ❓ 常见问题排查

### Q1: PostgreSQL连接失败？
```bash
# 检查PostgreSQL是否运行
pg_isready

# 检查端口是否被占用
netstat -an | grep 5432

# 查看PostgreSQL日志
tail -f /var/log/postgresql/postgresql-*.log
```

### Q2: Alembic迁移失败？
```bash
# 查看当前版本
alembic current

# 回滚到上一版本
alembic downgrade -1

# 查看迁移历史
alembic history

# 强制标记为最新（慎用）
alembic stamp head
```

### Q3: MinIO上传失败？
```bash
# 检查MinIO服务状态
curl http://localhost:9000/minio/health/live

# 检查Bucket是否存在
mc ls my-bucket/

# 手动创建Bucket
mc mb my-bucket/uploads
```

---

*本文件由微光同行多Agent并行开发系统自动生成*
*遵循后端开发规范 v1.0 | 最后更新: 2026-05-28*
