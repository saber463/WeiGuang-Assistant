# 🟡 测试Agent 工作规范

> **分支**: `feature/test`
> **技术栈**: pytest + requests + Allure + Espresso + UI Automator
> **负责人**: [待分配]
> **状态**: 🚀 开发中
> **最后更新**: 2026-05-28

---

## 📋 角色定位

你是**微光同行项目的质量守护者（QA）**，负责确保整个系统的稳定性、可靠性和用户体验。你的核心职责包括：

- **后端接口自动化测试**：验证API的正确性、性能和安全性
- **Android端UI/单元测试**：确保前端功能符合需求、无崩溃
- **集成测试与E2E测试**：端到端业务流程验证
- **兼容性测试**：多机型、多Android版本覆盖
- **Bug追踪与回归测试**：发现问题→报告→跟踪修复→验证关闭

你的工作直接决定产品上线后的用户口碑！

---

## 🎯 Sprint 1 核心任务（第1-2周）

### 优先级：P0 - 必须完成

#### 任务1.1：搭建自动化测试框架 ⏱️ 预计：0.5天

**目标**：建立完整的测试基础设施

##### 1.1.1 后端接口测试框架（Python pytest）

**项目结构**：
```
weiguangplus-test/
├── tests/
│   ├── conftest.py                    # 全局fixture配置
│   ├── api/                           # 接口测试
│   │   ├── __init__.py
│   │   ├── test_auth.py              # 认证模块测试
│   │   ├── test_users.py             # 用户模块测试
│   │   ├── test_drugs.py             # 药品识别测试
│   │   ├── test_emergency.py         # SOS应急测试
│   │   └── test_health_check.py      # 健康检查测试
│   │
│   ├── unit/                          # 单元测试（辅助函数）
│   │   ├── test_utils.py
│   │   └── test_validators.py
│   │
│   ├── performance/                   # 性能测试
│   │   ├── test_load_testing.py      # 负载测试
│   │   └── benchmarks/               # 性能基准数据
│   │
│   └── integration/                   # 集成测试
│       ├── test_e2e_login_flow.py    # E2E: 登录流程
│       └── test_e2e_drug_recognition.py # E2E: 识药流程
│
├── fixtures/                         # 测试数据工厂
│   ├── __init__.py
│   ├── user_factory.py               # 用户测试数据生成器
│   ├── drug_factory.py               # 药品测试数据生成器
│   └── image_factory.py              # 图片测试数据生成器
│
├── utils/                            # 测试工具库
│   ├── __init__.py
│   ├── api_client.py                 # API请求封装
│   ├── assertions.py                 # 自定义断言方法
│   └── db_helpers.py                # 数据库操作辅助
│
├── config/                           # 环境配置
│   ├── development.yaml              # 开发环境
│   ├── staging.yaml                  # 预发布环境
│   └── production.yaml               # 生产环境（只读）
│
├── reports/                          # 测试报告输出目录
│   └── .gitkeep
│
├── requirements.txt
├── pytest.ini                        # pytest全局配置
├── setup.cfg                         # allure插件配置
└── README.md                         # 本文件
```

**conftest.py（全局Fixture）**:
```python
"""
Pytest全局配置文件
功能：
- 定义API客户端Fixture
- 配置数据库连接
- 设置测试环境变量
- 自动清理测试数据
"""

import pytest
import httpx
import asyncio
from typing import AsyncGenerator
import yaml


# ============================================================
# Fixture: 异步HTTP客户端（用于FastAPI接口测试）
# ============================================================
@pytest.fixture(scope="session")
def api_base_url() -> str:
    """从环境变量或配置文件读取API基础URL"""
    import os
    return os.getenv(
        "API_BASE_URL", 
        "http://localhost:8000"
    )


@pytest.fixture(scope="function")
async def async_client(api_base_url: str) -> AsyncGenerator[httpx.AsyncClient, None]:
    """
    异步HTTP客户端（每个测试函数一个实例）
    
    使用方式：
    ```python
    async def test_login(async_client):
        response = await async_client.post("/api/v1/auth/login", json={...})
        assert response.status_code == 200
    ```
    """
    async with httpx.AsyncClient(base_url=api_base_url) as client:
        yield client


@pytest.fixture(scope="function")
def sync_client(api_base_url: str) -> httpx.Client:
    """同步HTTP客户端（用于简单测试场景）"""
    with httpx.Client(base_url=api_base_url) as client:
        yield client


# ============================================================
# Fixture: 测试用户认证Token
# ============================================================
@pytest.fixture(scope="session")
async def auth_tokens(api_base_url: str) -> dict:
    """
    获取测试用的认证Token（Session级别复用）
    
    Returns:
        {
            'access_token': 'eyJ...',
            'refresh_token': 'eyJ...',
            'user_id': 1,
            'phone': '138****0001'
        }
    """
    async with httpx.AsyncClient(base_url=api_base_url) as client:
        # 使用预置的测试账号登录
        response = await client.post("/api/v1/auth/login", json={
            "phone": "13800138001",  # 测试专用账号
            "password": "Test123456"
        })
        
        assert response.status_code == 200, "测试账号登录失败！请确认数据库已初始化测试数据。"
        
        data = response.json()
        return {
            'access_token': data['data']['access_token'],
            'refresh_token': data['data']['refresh_token'],
            'user_id': data['data']['user']['id'],
            'phone': data['data']['user']['phone']
        }


@pytest.fixture(scope="function")
def authenticated_headers(auth_tokens: dict) -> dict:
    """返回带Authorization头的请求头字典"""
    return {
        "Authorization": f"Bearer {auth_tokens['access_token']}",
        "Content-Type": "application/json"
    }


# ============================================================
# Fixture: 数据库清理
# ============================================================
@pytest.fixture(autouse=True, scope="function")
def cleanup_test_data():
    """
    每个测试函数执行前后自动清理测试数据
    
    注意：此fixture需要配合数据库事务使用，
    在测试开始前开启事务，结束后回滚。
    """
    # 测试前的准备工作
    print("\n🧹 [Setup] 准备测试环境...")
    
    yield
    
    # 测试后的清理工作
    print("🧹 [Teardown] 清理测试数据...")


# ============================================================
# Pytest命令行参数自定义
# ============================================================
def pytest_addoption(parser):
    """添加自定义命令行选项"""
    parser.addoption(
        "--env",
        action="store",
        default="development",
        choices=["development", "staging", "production"],
        help="选择测试环境 (default: development)"
    )
    parser.addoption(
        "--slow",
        action="store_true",
        default=False,
        help="运行标记为slow的测试用例"
    )
    parser.addoption(
        "--performance",
        action="store_true",
        default=False,
        help="运行性能基准测试"


@pytest.fixture(scope="session")
def env_config(request):
    """加载指定环境的配置文件"""
    env_name = request.config.getoption("--env")
    config_path = f"config/{env_name}.yaml"
    
    with open(config_path, 'r', encoding='utf-8') as f:
        config = yaml.safe_load(f)
    
    return config
```

**pytest.ini（全局配置）**:
```ini
[pytest]
# 测试发现路径
testpaths = tests

# Python文件匹配模式
python_files = test_*.py *_test.py
python_classes = Test*
python_functions = test_*

# 标记定义
markers =
    slow: 标记运行时间较长的测试用例
    smoke: 冒烟测试关键路径
    regression: 回归测试
    unit: 单元测试
    integration: 集成测试
    e2e: 端到端测试
    performance: 性能测试
    security: 安全测试

# 日志配置
log_cli = true
log_cli_level = INFO

# 失败时立即停止（可选）
# xfail_strict = true

# 并行测试（需安装pytest-xdist）
# addopts = -v -s --tb=short --alluredir=reports/allure-results
addopts = -v --tb=short --alluredir=reports/allure-results

# 最小Python版本要求
minversion = 3.9

# 插件列表
plugins = 
    allure-pytest
    pytest-asyncio
    pytest-cov
```

##### 1.1.2 Android UI测试框架（Espresso）

**目录结构**（在 `weiguangplus-frontend` 项目中）：
```
app/src/androidTest/java/com/weiguangchangxing/weiguang_plus/
├── BaseTest.kt                    # 测试基类
├── LoginActivityTest.kt           # 登录页测试
├── DrugRecognitionTest.kt         # 药品识别测试
├── EmergencySOSTest.kt            # SOS求助测试
├── NavigationTest.kt             # 导航栏测试
└── AccessibilityTest.kt          # 无障碍适配测试
```

**BaseTest.kt（Espresso基类）**:
```kotlin
/**
 * Espresso UI测试基类
 * 
 * 功能：
 * - 统一的Activity启动规则
 * - 通用的等待和断言方法
 * - 截图失败处理
 * - TalkBack兼容性测试支持
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
abstract class BaseActivityTest {
    
    @get:Rule
    val activityRule = ActivityTestRule(
        MainActivity::class.java,
        initialTouchMode = false,
        launchActivity = false  // 不自动启动，由子类控制
    )
    
    @get:Rule
    val screenshotRule = ScreenshotFailureRule()  // 失败自动截图
    
    /**
     * 等待View出现并返回
     */
    protected fun waitForView(@IdRes viewId: Int, timeoutMs: Long = 5000): ViewInteraction {
        return onView(withId(viewId))
            .check(matches(isDisplayed()))
            .also {
                // 可选：添加日志记录
                Log.d("EspressoTest", "✓ View #$viewId 已显示")
            }
    }
    
    /**
     * 等待文本出现
     */
    protected fun waitForText(text: String): ViewInteraction {
        return onView(withText(text))
            .check(matches(isDisplayed()))
    }
    
    /**
     * 模拟点击并验证跳转
     */
    protected fun clickAndVerify(
        @IdRes clickTarget: Int,
        expectedText: String? = null,
        @IdRes expectedView: Int? = null
    ) {
        // 点击目标
        onView(withId(clickTarget)).perform(click())
        
        // 等待页面切换动画完成
        SystemClock.sleep(500)
        
        // 验证结果
        expectedText?.let { waitForText(it) }
        expectedView?.let { waitForView(it) }
    }
    
    /**
     * 输入文本到EditText
     */
    protected fun typeIntoEditText(@IdRes editTextId: Int, text: String) {
        onView(withId(editTextId))
            .perform(replaceText(text))  // 清空后输入新文本
    }
}
```

---

#### 任务1.2：编写核心接口测试用例 ⏱️ 预计：1.5天

**目标**：覆盖所有P0优先级的API接口

##### 1.2.1 认证模块测试 (`tests/api/test_auth.py`)

```python
"""
认证模块接口测试
覆盖范围：
- 用户注册（正常/异常场景）
- 用户登录（正常/密码错误/账号不存在）
- Token刷新（正常/过期/无效RefreshToken）
- 修改密码（正常/旧密码错误/未认证）
"""

import pytest
from httpx import ASYNC_STATUS_CODES
import time


class TestUserRegistration:
    """用户注册接口测试"""
    
    @pytest.mark.smoke
    @pytest.mark.asyncio
    async def test_register_success(self, async_client):
        """测试正常注册流程"""
        import random
        
        # 生成唯一手机号（避免重复）
        phone = f"139{random.randint(10000000, 99999999)}"
        password = "SecurePass123!"
        
        response = await async_client.post("/api/v1/auth/register", json={
            "phone": phone,
            "password": password,
            "nickname": f"测试用户{random.randint(1000,9999)}",
            "disability_type": "视障",
            "disability_level": "一级"
        })
        
        assert response.status_code == 201
        data = response.json()
        assert data["code"] == 201
        assert data["message"] == "注册成功"
        assert "user" in data["data"]
        assert data["data"]["user"]["phone"] == f"139****{phone[3:]}"
    
    @pytest.mark.asyncio
    async def test_register_duplicate_phone(self, async_client):
        """测试重复手机号注册（应失败）"""
        # 先注册一次
        await async_client.post("/api/v1/auth/register", json={
            "phone": "13800001111",
            "password": "Password123!",
            "nickname": "原始用户"
        })
        
        # 再次注册相同手机号
        response = await async_client.post("/api/v1/auth/register", json={
            "phone": "13800001111",  # 相同手机号
            "password": "AnotherPass456",
            "nickname": "重复用户"
        })
        
        assert response.status_code == 409  # Conflict
        assert "已存在" in response.json()["message"]
    
    @pytest.mark.asyncio
    async def test_register_invalid_phone_format(self, async_client):
        """测试无效手机号格式"""
        test_cases = [
            ("12345", "手机号太短"),
            ("abcdefghijk", "非数字"),
            ("138001380001", "超过11位"),
            ("", "空字符串")
        ]
        
        for phone, desc in test_cases:
            response = await async_client.post("/api/v1/auth/register", json={
                "phone": phone,
                "password": "ValidPass123"
            })
            
            assert response.status_code == 422  # Unprocessable Entity
            assert "手机号" in response.json()["message"].lower()
    
    @pytest.mark.asyncio
    async def test_register_weak_password(self, async_client):
        """测试弱密码拒绝"""
        weak_passwords = [
            "123",           # 太短
            "password",      # 常见弱密码
            "aaaaaa",        # 纯字母
            "111111",        # 纯数字
            ""               # 空
        ]
        
        for pwd in weak_passwords:
            response = await async_client.post("/api/v1/auth/register", json={
                "phone": f"139{time.time_ns() % 100000000:08d}",  # 动态生成
                "password": pwd
            })
            
            assert response.status_code == 422
            assert "密码" in response.json()["message"]


class TestUserLogin:
    """用户登录接口测试"""
    
    @pytest.mark.smoke
    @pytest.mark.asyncio
    async def test_login_success(self, async_client):
        """测试正常登录"""
        # 前置条件：先注册用户
        await async_client.post("/api/v1/auth/register", json={
            "phone": "13800222000",
            "password": "LoginTest123",
            "nickname": "登录测试用户"
        })
        
        # 执行登录
        response = await async_client.post("/api/v1/auth/login", json={
            "phone": "13800222000",
            "password": "LoginTest123"
        })
        
        assert response.status_code == 200
        data = response.json()
        
        # 验证返回Token
        assert "access_token" in data["data"]
        assert "refresh_token" in data["data"]
        assert data["data"]["token_type"] == "bearer"
        assert data["data"]["expires_in"] > 0
        
        # 验证JWT格式（Header.Payload.Signature）
        token_parts = data["data"]["access_token"].split(".")
        assert len(token_parts) == 3, "Invalid JWT format"
    
    @pytest.mark.asyncio
    async def test_login_wrong_password(self, async_client):
        """测试错误密码"""
        response = await async_client.post("/api/v1/auth/login", json={
            "phone": "13800222000",
            "password": "WrongPassword!!!"
        })
        
        assert response.status_code == 401  # Unauthorized
        assert "密码" in response.json()["message"] or "失败" in response.json()["message"]
    
    @pytest.mark.asyncio
    async def test_login_nonexistent_user(self, async_client):
        """测试不存在的账号"""
        response = await async_client.post("/api/v1/auth/login", json={
            "phone": "19999999999",  # 不存在的号码
            "password": "AnyPassword123"
        })
        
        assert response.status_code == 404 or response.status_code == 401
        # 可能返回"用户不存在"或"密码错误"（安全考虑不明确提示）


class TestTokenRefresh:
    """Token刷新接口测试"""
    
    @pytest.mark.asyncio
    async def test_refresh_success(self, async_client, auth_tokens):
        """测试正常刷新Token"""
        response = await async_client.post("/api/v1/auth/refresh", json={
            "refresh_token": auth_tokens["refresh_token"]
        })
        
        assert response.status_code == 200
        new_token = response.json()["data"]["access_token"]
        
        # 验证新Token可用
        verify_resp = await async_client.get(
            "/api/v1/users/profile",
            headers={"Authorization": f"Bearer {new_token}"}
        )
        assert verify_resp.status_code == 200
    
    @pytest.mark.asyncio
    async def test_refresh_expired_token(self, async_client):
        """测试过期/无效的RefreshToken"""
        response = await async_client.post("/api/v1/auth/refresh", json={
            "refresh_token": "invalid.expired.token.here"
        })
        
        assert response.status_code == 401
        assert "expired" in response.json()["message"].lower() or \
               "invalid" in response.json()["message"].lower()


class TestChangePassword:
    """修改密码接口测试"""
    
    @pytest.mark.asyncio
    async def test_change_password_success(self, async_client, authenticated_headers):
        """测试正常修改密码"""
        response = await async_client.put(
            "/api/v1/users/password",
            headers=authenticated_headers,
            json={
                "old_password": "Test123456",
                "new_password": "NewSecurePass789"
            }
        )
        
        assert response.status_code == 200
        
        # 验证新密码可以登录
        login_resp = await async_client.post("/api/v1/auth/login", json={
            "phone": "13800138001",
            "password": "NewSecurePass789"
        })
        assert login_resp.status_code == 200
    
    @pytest.mark.asyncio
    async def test_change_password_wrong_old(self, async_client, authenticated_headers):
        """测试旧密码错误"""
        response = await async_client.put(
            "/api/v1/users/password",
            headers=authenticated_headers,
            json={
                "old_password": "WrongOldPassword",
                "new_password": "NewPass123"
            }
        )
        
        assert response.status_code == 400
        assert "旧密码" in response.json()["message"]
    
    @pytest.mark.asyncio
    async def test_change_password_unauthorized(self, async_client):
        """测试未认证访问（无Token）"""
        response = await async_client.put(
            "/api/v1/users/password",
            json={"old_password": "xxx", "new_password": "yyy"}
            # 注意：没有传 Authorization header
        )
        
        assert response.status_code == 401  # Missing authentication
```

##### 1.2.2 药品识别模块测试 (`tests/api/test_drugs.py`)

```python
"""
药品识别相关接口测试
覆盖范围：
- 上传识别记录（图片+OCR文本）
- 查询历史记录（分页、过滤）
- 获取药品详情
- 过敏原批量检查
"""

import pytest
import base64
from pathlib import Path


class TestDrugRecognitionUpload:
    """药品OCR识别上传测试"""
    
    @pytest.fixture
    def sample_drug_image(self) -> bytes:
        """加载测试用药盒图片"""
        image_path = Path(__file__).parent.parent / "fixtures" / "images" / "tylenol_box.jpg"
        if not image_path.exists():
            # 如果没有真实图片，生成一个简单的测试图片
            from PIL import Image
            import io
            
            img = Image.new('RGB', (400, 300), color='red')
            buffer = io.BytesIO()
            img.save(buffer, format='JPEG')
            return buffer.getvalue()
        
        return image_path.read_bytes()
    
    @pytest.mark.smoke
    @pytest.mark.asyncio
    async def test_upload_recognition_success(
        self, 
        async_client, 
        authenticated_headers,
        sample_drug_image
    ):
        """测试成功上传药盒照片进行识别"""
        files = {
            "image": ("drug_photo.jpg", sample_drug_image, "image/jpeg")
        }
        data = {
            "ocr_text": "泰诺 对乙酰氨基酚片 0.5g×12片 强生制药"
        }
        
        response = await async_client.post(
            "/api/v1/drugs/recognition",
            headers={**authenticated_headers},  # 移除Content-Type让httpx自动设置multipart
            files=files,
            data=data
        )
        
        assert response.status_code == 201
        result = response.json()["data"]
        
        # 验证返回字段完整性
        assert "record_id" in result
        assert "matched_drug" in result
        assert "risk_assessment" in result
        assert "confidence_score" in result
        
        # 验证风险等级是合法值
        valid_risks = ["LOW", "MEDIUM", "HIGH", "CRITICAL"]
        assert result["risk_assessment"]["risk_level"] in valid_risks
    
    @pytest.mark.asyncio
    async def test_upload_without_auth(self, async_client, sample_drug_image):
        """测试未认证上传（应被拒绝）"""
        files = {"image": ("test.jpg", sample_drug_image, "image/jpeg")}
        
        response = await async_client.post(
            "/api/v1/drugs/recognition",
            files=files
        )
        
        assert response.status_code == 401
    
    @pytest.mark.asyncio
    async def test_upload_invalid_image(self, async_client, authenticated_headers):
        """测试上传非法文件格式"""
        files = {
            "image": ("test.txt", b"This is not an image!", "text/plain")
        }
        
        response = await async_client.post(
            "/api/v1/drugs/recognition",
            headers=authenticated_headers,
            files=files
        )
        
        assert response.status_code == 422  # Unprocessable Entity


class TestDrugHistoryQuery:
    """药品识别历史查询测试"""
    
    @pytest.mark.asyncio
    async def test_query_history_default_params(
        self, 
        async_client, 
        authenticated_headers
    ):
        """测试默认分页查询"""
        response = await async_client.get(
            "/api/v1/drugs/history",
            headers=authenticated_headers
        )
        
        assert response.status_code == 200
        data = response.json()["data"]
        
        # 验证分页字段
        assert "total" in data
        assert "page" in data
        assert "size" in data
        assert "items" in data
        assert isinstance(data["items"], list)
        
        # 默认每页20条
        assert len(data["items"]) <= 20
    
    @pytest.mark.asyncio
    async def test_query_history_with_pagination(
        self, 
        async_client, 
        authenticated_headers
    ):
        """测试自定义分页参数"""
        response = await async_client.get(
            "/api/v1/drugs/history?page=2&size=5",
            headers=authenticated_headers
        )
        
        assert response.status_code == 200
        data = response.json()["data"]
        
        assert data["page"] == 2
        assert data["size"] == 5
        assert len(data["items"]) <= 5
    
    @pytest.mark.asyncio
    async def test_query_history_filter_by_risk(
        self, 
        async_client, 
        authenticated_headers
    ):
        """测试按风险等级过滤"""
        response = await async_client.get(
            "/api/v1/drugs/history?risk_level=HIGH",
            headers=authenticated_headers
        )
        
        assert response.status_code == 200
        items = response.json()["data"]["items"]
        
        # 验证所有返回结果的risk_level都是HIGH
        for item in items:
            assert item["risk_level"] == "HIGH"


class TestAllergenCheck:
    """过敏原批量检查测试"""
    
    @pytest.mark.smoke
    @pytest.mark.asyncio
    async def test_allergen_check_safe_drugs(
        self, 
        async_client, 
        authenticated_headers
    ):
        """测试对安全药品的过敏原检查（应返回LOW风险）"""
        response = await async_client.post(
            "/api/v1/drugs/allergen-check",
            headers=authenticated_headers,
            json={
                "drug_ids": [1, 2, 3],  # 假设这些ID对应维生素C等安全药物
                "user_profile": {
                    "allergy_history": ["青霉素"],  # 仅对青霉素过敏
                    "chronic_diseases": ["高血压"]
                }
            }
        )
        
        assert response.status_code == 200
        result = response.json()["data"]
        
        # 验证返回结构
        assert "results" in result
        assert "summary" in result
        assert "safe_count" in result["summary"]
        assert "dangerous_count" in result["summary"]
    
    @pytest.mark.asyncio
    async def test_allergen_check_dangerous_combination(
        self, 
        async_client, 
        authenticated_headers
    ):
        """测试危险药物组合（包含用户过敏原）"""
        response = await async_client.post(
            "/api/v1/drugs/allergen-check",
            headers=authenticated_headers,
            json={
                "drug_ids": [42],  # 假设42号是阿莫西林（含青霉素成分）
                "user_profile": {
                    "allergy_history": ["青霉素", "磺胺类药物"],
                    "current_medications": ["阿司匹林"]
                }
            }
        )
        
        assert response.status_code == 200
        result = response.json()["data"]
        
        # 应该检测到高风险
        assert result["summary"]["dangerous_count"] >= 1
        assert result["results"][0]["overall_risk"] in ["HIGH", "CRITICAL"]
```

**验收标准**：
```bash
# 运行全部接口测试
pytest tests/api/ -v --cov=../app --cov-report=html

# 预期结果：
# 收集到: XX个测试用例
# 通过: XX passed
# 失败: 0 failed
# 跳过: X skipped
# 覆盖率: >= 80%
# 总耗时: < 60秒（不含慢速测试）

# 生成Allure报告
allure serve reports/allure-results

# 打开浏览器查看可视化报告
# 应包含：
# - 测试套件树状图
# - 每个用例的详细日志
# - 失败截图（如有）
# - 趋势图表
```

---

#### 任务1.3：Android UI冒烟测试 ⏱️ 预计：1天

**目标**：验证APP核心路径可正常运行

**必须覆盖的关键路径**：

| 编号 | 测试场景 | 步骤 | 预期结果 | 优先级 |
|------|---------|------|---------|--------|
| SMK-001 | App冷启动 | 点击App图标 → 显示Splash → 进入首页 | 启动时间<3秒，无ANR | P0 |
| SMK-002 | 登录完整流程 | 输入账号密码 → 点击登录 → 跳转首页 | 成功登录，显示用户昵称 | P0 |
| SMK-003 | 注册新用户 | 点击注册 → 填写表单 → 提交 → 返回登录页 | 注册成功，手机号自动填充 | P0 |
| SMK-004 | 药品识别流程 | 打开相机 → 拍照 → 等待识别 → 显示结果卡 | 结果卡片展示正确信息 | P0 |
| SMK-005 | SOS求助流程 | 点击SOS → 选择场景 → 确认发送 | SMS发送成功，位置上传成功 | P0 |
| SMK-006 | 底部导航切换 | 依次点击12个Tab | 每个Tab页面正常显示 | P0 |
| SMK-007 | 权限弹窗处理 | 触发相机/定位/存储权限弹窗 | 弹窗出现且允许/拒绝按钮可点击 | P0 |
| SMK-008 | 弱视模式切换 | 设置中切换大字号 | 字体放大1.5倍，界面布局正常 | P1 |

**Espresso测试示例**：
```kotlin
@LargeTest
@RunWith(AndroidJUnit4::class)
class SmokeTest : BaseActivityTest() {
    
    @Test
    fun testCompleteLoginFlow() {
        // Step 1: 启动App
        activityRule.launchActivity(null)
        
        // Step 2: 等待登录页加载
        waitForView(R.id.edit_text_phone)
        waitForView(R.id.edit_text_password)
        waitForView(R.id.button_login)
        
        // Step 3: 输入手机号
        onView(withId(R.id.edit_text_phone))
            .perform(typeText("13800138001"))
        
        // Step 4: 输入密码
        onView(withId(R.id.edit_text_password))
            .perform(typeText("Test123456"))
        
        // Step 5: 点击登录按钮
        onView(withId(R.id.button_login))
            .perform(click())
        
        // Step 6: 等待跳转到首页（最多10秒）
        Thread.sleep(3000)
        
        // Step 7: 验证进入首页
        onView(withId(R.id.bottom_navigation))
            .check(matches(isDisplayed()))
        
        onView(withText("首页"))  // 或其他首页标志性元素
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testDrugRecognitionFlow() {
        activityRule.launchActivity(null)
        
        // 导航到药品识别Tab
        // ... (点击底部导航第4个Tab)
        
        // 点击拍照按钮
        onView(withId(R.id.btn_capture_photo))
            .perform(click())
        
        // 验证相机预览界面打开
        waitForView(R.id.camera_preview)
        
        // 模拟拍照（需要Mock CameraX或使用测试图片）
        // ...
        
        // 验证结果显示
        waitForView(R.id.card_drug_result)
        onView(withId(R.id.text_drug_name))
            .check(matches(not(withText(""))))
    }
}
```

---

### 优先级：P1 - Sprint 1 可选

#### 任务1.4：兼容性测试矩阵 ⏱️ 预计：持续进行

**机型覆盖计划**：

| 品牌 | 型号 | Android版本 | 屏幕尺寸 | 内存 | 优先级 |
|------|------|------------|---------|------|--------|
| 华为 | Mate 60 Pro | 14.0 | 6.82寸 | 16GB | P0 |
| 华为 | nova 12 SE | 12.0 | 6.67寸 | 8GB | P1 |
| 小米 | 14 Ultra | 14.0 | 6.73寸 | 16GB | P0 |
| 小米 | Redmi Note 13 | 13.0 | 6.67寸 | 8GB | P1 |
| OPPO | Find X6 Pro | 14.0 | 6.74寸 | 16GB | P1 |
| vivo | X100 Pro | 14.0 | 6.78寸 | 16GB | P1 |
| 三星 | Galaxy S24 Ultra | 14.0 | 6.8寸 | 12GB | P0 |
| 三星 | A54 | 13.0 | 6.4寸 | 8GB | P1 |
| 低端机 | 红米9A | 10.0 | 6.53寸 | 4GB | P2 |

#### 任务1.5：性能基准测试 ⏱️ 预计：1天

**指标要求**：

| 指标 | 目标值 | 测量方法 |
|------|--------|---------|
| 冷启动时间 | < 3秒 | logcat + 手动计时 |
| 页面切换帧率 | ≥ 55fps | GPU Profiler |
| 内存占用（稳态） | < 200MB | Android Profiler |
| APK体积（Release） | < 50MB | build输出 |
| CPU占用（空闲） | < 5% | Perfdog/Systrace |
| 电池消耗（后台1小时） | < 3% | Battery Historian |
| 网络请求平均延迟 | < 500ms | Charles/Fiddler抓包 |

---

## 🔒 开发约束

### ✅ 允许的操作
1. 在 `feature/test` 分支内自由编写测试代码
2. 发现Bug立即上报主Agent（通过Issue系统）
3. 编写自动化测试脚本
4. 生成测试报告（Allure HTML / JUnit XML）

### ❌ 禁止的操作
1. **禁止修改任何业务代码**（只写测试代码）
2. **禁止自行修复Bug**（报告给对应开发Agent）
3. **禁止在生产环境执行破坏性测试**
4. **禁止提交包含真实用户数据的测试日志**

### 🔄 Bug报告协议

**Issue模板**：
```markdown
## Bug标题: [简短描述]

**严重程度**: 🔴 Critical / 🟠 High / 🟡 Medium / 🟢 Low  
**影响范围**: [哪些功能受影响]  
**发现版本**: v1.2 (commit xxxxxxx)  
**测试环境**: [设备型号/Android版本/API Level]

### 重现步骤
1. 打开App，进入XXX页面
2. 操作YYY
3. 观察到ZZZ异常行为

### 预期行为
[描述应该发生的正常情况]

### 实际行为
[描述实际观察到的异常]

### 错误日志
```
[粘贴Logcat/服务器日志/浏览器Console]
```

### 截图/录屏
[附上问题截图或屏幕录制]

### 复现概率
- [ ] 100%必现
- [ ] 偶尔出现（约X%概率）
- [ ] 仅特定条件下出现

### 临时解决方案
[如果有的话]

### 建议
[你认为应该如何修复]
```

---

## 📦 依赖清单

### requirements.txt
```txt
# HTTP客户端
httpx==0.25.2
requests==2.31.0

# 测试框架
pytest==7.4.4
pytest-asyncio==0.21.1
pytest-cov==4.1.0
pytest-xdist==3.5.0  # 并行测试
pytest-timeout==2.2.0  # 超时控制
pytest-rerunfailures==12.0  # 失败重试

# 报告生成
allure-pytest==2.13.2
allure-python-commons==2.13.2

# 数据验证
pydantic==2.5.2

# Mock工具
pytest-mock==3.12.0
responses==0.23.3  # Mock HTTP请求

# 性能测试
locust==2.20.1

# 工具库
Faker==19.13.1  # 生成随机测试数据
tqdm==4.66.1
loguru==0.7.2
pyyaml==6.0.1
```

---

## 🚀 快速开始指南

### 方式A：运行后端接口测试
```bash
# 1. 克隆测试仓库
cd F:\java\weiguangplus-test

# 2. 创建虚拟环境
python -m venv venv
venv\Scripts\activate  # Windows
# 或 source venv/bin/activate  # Linux/Mac

# 3. 安装依赖
pip install -r requirements.txt

# 4. 确保后端服务已启动（默认 http://localhost:8000）
# 如果未启动，先执行：
# docker-compose up -d db app

# 5. 初始化测试数据库（导入测试数据）
python scripts/init_test_data.py

# 6. 运行全部测试
pytest tests/ -v

# 7. 查看报告
allure serve reports/allure-results
```

### 方式B：运行Android UI测试
```bash
# 1. 在Android Studio中打开 weiguangplus-frontend 项目

# 2. 连接真机或启动模拟器（推荐API 28+）

# 3. 运行Espresso测试
./gradlew connectedDebugAndroidTest

# 4. 查看测试报告
# 报告位置: app/build/reports/androidTests/connected/index.html
```

---

## 📊 进度汇报模板

```markdown
## 测试进度报告

**测试周期**: Sprint 1 (2026-MM-DD ~ 2026-MM-DD)
**Agent**: 测试Agent
**分支**: feature/test

### 测试统计总览
- 新增测试用例: XX个
- 通过: XX passed (XX%)
- 失败: YY failed (YY%)
- 跳过: ZZ skipped
- 总耗时: HH:MM:SS

### 各模块覆盖率
| 模块 | 用例数 | 通过率 | 覆盖率 | 关键Bug数 |
|------|--------|--------|--------|-----------|
| 认证模块 | 15 | 100% | 92% | 0 |
| 用户模块 | 10 | 100% | 85% | 1 |
| 药品识别 | 20 | 95% | 88% | 2 |
| SOS应急 | 8 | 100% | 90% | 0 |
| **总计** | **53** | **98%** | **89%** | **3** |

### Bug清单
| Bug ID | 严重程度 | 模块 | 状态 | 分配给 |
|--------|---------|------|------|--------|
| #001 | 🔴 Critical | 药品OCR | 待修复 | 算法Agent |
| #002 | 🟠 High | 登录Token | 已修复 | 后端Agent |
| #003 | 🟡 Medium | SOS短信 | 待确认 | 后端Agent |

### 兼容性测试结果
| 设备型号 | Android版本 | 核心功能通过率 | 备注 |
|----------|-----------|---------------|------|
| Pixel 7 | 14.0 | 100% | ✅ 全部通过 |
| Redmi Note 11 | 12.0 | 95% | ⚠️ 相机偶发闪退 |
| Huawei Nova 9 | 11.0 | 98% | ✅ 正常 |

### 性能基准数据
- 平均冷启动时间: X.X秒 (目标<3秒)
- 页面平均FPS: XX (目标≥55)
- 内存峰值占用: XXX MB (目标<200MB)

### 下一步计划
1. 跟踪3个未修复Bug的进度
2. 补充边界条件测试用例
3. 开始准备Sprint 2回归测试套件
```

---

*本文件由微光同行多Agent并行开发系统自动生成*
*遵循测试工程规范 v1.0 | 最后更新: 2026-05-28*
