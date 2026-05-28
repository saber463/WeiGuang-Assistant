# 🟢 前端开发Agent 工作规范

> **分支**: `feature/frontend`
> **技术栈**: Kotlin + Jetpack Compose + Room + Retrofit
> **负责人**: [待分配]
> **状态**: 🚀 开发中
> **最后更新**: 2026-05-28

---

## 📋 角色定位

你是**微光同行项目的Android前端工程师**，负责构建用户直接交互的APP界面。你的工作直接影响用户体验，需要特别关注：

- **无障碍适配**（TalkBack兼容、大字号、高对比度）
- **性能优化**（启动速度、内存占用、帧率流畅）
- **视觉设计**（符合Material3规范、符合视障用户使用习惯）

---

## 🎯 Sprint 1 核心任务（第1-2周）

### 优先级：P0 - 必须完成

#### 任务1.1：从主仓库同步代码并建立开发环境 ⏱️ 预计：0.5天

**目标**：确保本地代码与main分支一致，配置好Android Studio开发环境

**具体步骤**：
```bash
# 进入前端Agent工作目录
cd F:\java\weiguangplus-frontend

# 查看当前分支和提交
git branch -v
git log --oneline -3

# 确保包含完整项目结构
ls app/src/main/java/com/weiguangchangxing/weiguang_plus/
# 应该看到: app/ core/ data/ feature/ ui/ 等目录

# 用Android Studio打开此目录
# File -> Open -> 选择 F:\java\weiguangplus-frontend
```

**验收标准**：
- [ ] Android Studio能正常打开项目且无报错
- [ ] `./gradlew assembleDebug` 编译成功（BUILD SUCCESSFUL）
- [ ] 能在模拟器或真机上安装并运行App

---

#### 任务1.2：登录注册模块UI开发 ⏱️ 预计：1.5天

**目标**：实现用户认证相关的所有界面

##### 1.2.1 登录页面 (LoginScreen.kt)

**界面布局**：
```
┌─────────────────────────────┐
│                             │
│      微光同行 LOGO          │
│    （带无障碍图标♿）         │
│                             │
│  ┌───────────────────────┐  │
│  │  请输入手机号          │  │
│  └───────────────────────┘  │
│                             │
│  ┌───────────────────────┐  │
│  │  请输入密码            │  │
│  └───────────────────────┘  │
│                             │
│     [ 登 录 ]               │
│                             │
│   忘记密码？                │
│                             │
│  ── 其他登录方式 ──        │
│  [验证码登录]              │
│                             │
│  还没有账号？[立即注册]     │
│                             │
└─────────────────────────────┘
```

**功能要求**：
- [ ] 手机号输入框（11位数字校验）
- [ ] 密码输入框（显示/隐藏切换按钮）
- [ ] 登录按钮（loading状态、禁用状态）
- [ ] "忘记密码"链接
- [ ] "立即注册"跳转
- [ ] 表单验证（空值检查、格式校验）
- [ ] 错误提示Toast（手机号格式错误、密码错误等）
- [ ] TalkBack无障碍支持（contentDescription）
- [ ] 弱视模式：支持大字号（sp单位可配置）

**关键代码示例**：
```kotlin
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onForgotPassword: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo区域
        Spacer(modifier = Modifier.height(48.dp))
        
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "微光同行Logo",
            modifier = Modifier.size(120.dp)
        )
        
        Text(
            text = "微光同行",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        // 手机号输入框
        OutlinedTextField(
            value = uiState.phone,
            onValueChange = { viewModel.onPhoneChanged(it) },
            label = { Text("请输入手机号") },
            keyboardOptions = KeyboardOptions(keyboardType = Phone),
            singleLine = true,
            isError = uiState.phoneError != null,
            supportingText = { Text(uiState.phoneError ?: "") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // 密码输入框
        OutlinedTextField(
            value = uiState.password,
            onValueChange = { viewModel.onPasswordChanged(it) },
            label = { Text("请输入密码") },
            visualTransformation = if (uiState.isPasswordVisible) 
                VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = Password),
            trailingIcon = {
                IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                    Icon(
                        imageVector = if (uiState.isPasswordVisible) 
                            Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (uiState.isPasswordVisible) "隐藏密码" else "显示密码"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        // 登录按钮
        Button(
            onClick = { viewModel.login() },
            enabled = !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("登 录", fontSize = 18.sp)
            }
        }
        
        // 底部链接
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onForgotPassword) {
                Text("忘记密码？")
            }
            TextButton(onClick = onNavigateToRegister) {
                Text("还没有账号？立即注册")
            }
        }
    }
}
```

##### 1.2.2 注册页面 (RegisterScreen.kt)

**表单字段**：
- [ ] 手机号（必填，11位）
- [ ] 验证码（必填，6位数字）⚠️ *Sprint 1可先用固定验证码"123456"*
- [ ] 设置密码（必填，8-20位，含字母+数字）
- [ ] 确认密码（必填，需与设置密码一致）
- [ ] 昵称（选填，2-20字符）
- [ ] 残疾类型（下拉选择：视障一级/二级/三级/四级、听障等）
- [ ] 用户协议勾选（必须同意才能注册）

**功能要求**：
- [ ] 实时表单验证（每个字段失焦时校验）
- [ ] 密码强度指示器（弱/中/强）
- [ ] 发送验证码倒计时（60秒后可重发）
- [ ] 注册成功后自动跳转到登录页或首页
- [ ] 无障碍支持（每个输入框都有清晰的label和hint）

##### 1.2.3 AuthViewModel（认证ViewModel）

**职责**：
- [ ] 管理登录/注册状态（phone, password, loading, error等）
- [ ] 调用后端API（通过Repository层）
- [ ] 处理Token存储（DataStore/EncryptedSharedPreferences）
- [ ] 自动刷新Token（Token过期时静默续期）

**接口定义**：
```kotlin
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onPhoneChanged(phone: String) {
        _uiState.update { it.copy(phone = phone, phoneError = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun login() {
        viewModelScope.launch {
            // 参数校验
            if (!validateInput()) return@launch
            
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val response = authRepository.login(
                    phone = _uiState.value.phone,
                    password = _uiState.value.password
                )
                
                // 保存Token
                tokenManager.saveTokens(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken
                )
                
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false, 
                    error = e.message ?: "登录失败"
                )}
            }
        }
    }

    private fun validateInput(): Boolean {
        // 实现手机号、密码格式校验逻辑
        return true
    }
}

data class AuthUiState(
    val phone: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val phoneError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)
```

**验收标准**：
```bash
# 编译检查
./gradlew :app:compileDebugKotlin

# 功能测试清单
1. 打开App → 自动跳转到登录页 ✅
2. 输入无效手机号 → 显示"请输入正确的11位手机号" ✅
3. 输入有效账号密码 → 点击登录 → 显示loading → 跳转首页 ✅
4. 输入错误密码 → 显示"手机号或密码错误"提示 ✅
5. 点击"立即注册" → 跳转到注册页 ✅
6. 注册流程完整走通 → 返回登录页自动填充手机号 ✅
7. 开启TalkBack → 所有元素都能正确朗读 ✅
8. 切换到弱视模式 → 字体放大1.5倍 ✅
```

---

#### 任务1.3：药品识别界面重构 ⏱️ 预计：2天

**目标**：优化现有的药品OCR识别流程，提升用户体验和识别准确率

##### 1.3.1 拍照界面优化 (CameraPreviewScreen.kt)

**当前问题**（基于代码审查发现）：
- ❌ 缺少对焦辅助框
- ❌ 没有边缘检测提示
- ❌ 手持抖动补偿不足
- ❌ 多语言提示不够友好

**优化方案**：

**界面布局**：
```
┌─────────────────────────────┐
│ ← 返回       药品识别       │
│─────────────────────────────│
│                             │
│    ┌─────────────────┐      │
│    │                 │      │
│    │   相机预览区     │      │
│    │                 │      │
│    │  ┌───────────┐  │      │
│    │  │ 对焦框    │  │      │
│    │  └───────────┘  │      │
│    │                 │      │
│    └─────────────────┘      │
│                             │
│  💡 将药盒放入框内对准拍摄   │
│                             │
│  ┌────────┐  ┌────────┐    │
│  │ 📷拍照  │  │🖼️相册  │    │
│  └────────┘  └────────┘    │
│                             │
│  最近识别:                   │
│  ┌────┐ ┌────┐ ┌────┐     │
│  │泰诺│ │阿莫│ │布洛│     │
│  └────┘ └────┘ └────┘     │
└─────────────────────────────┘
```

**新增功能**：

1. **智能对焦框**
   ```kotlin
   @Composable
   fun FocusOverlay(
       isFocused: Boolean,
       modifier: Modifier = Modifier
   ) {
       Canvas(modifier = modifier) {
           val cornerLength = size.width * 0.15f
           val strokeWidth = 4.dp.toPx()
           
           // 四个角落的L形标记
           drawLine(
               color = if (isFocused) Color.Green else Color.White,
               start = Offset(0f, cornerLength),
               end = Offset(0f, 0f),
               strokeWidth = strokeWidth,
               cap = StrokeCap.Round
           )
           // ... 绘制其他三个角
           
           // 边缘检测动画（扫描线效果）
           if (!isFocused) {
               drawRect(
                   color = Color.White.copy(alpha = 0.3f),
                   style = Stroke(width = 2.dp.toPx())
               )
           }
       }
   }
   ```

2. **手持稳定检测**
   ```kotlin
   class StabilityDetector {
       private var lastAcceleration = FloatArray(3)
       private var shakeCount = 0
       
       fun detectShake(currentAcceleration: FloatArray): Boolean {
           val xDiff = abs(currentAcceleration[0] - lastAcceleration[0])
           val yDiff = abs(currentAcceleration[1] - lastAcceleration[1])
           val zDiff = abs(currentAcceleration[2] - lastAcceleration[2])
           
           val totalShake = xDiff + yDiff + zDiff
           
           return if (totalShake > SHAKE_THRESHOLD) {
               shakeCount++
               shakeCount > MAX_SHAKE_COUNT
           } else {
               shakeCount = 0
               false
           }
       }
       
       companion object {
           const val SHAKE_THRESHOLD = 3.0f
           const val MAX_SHAKE_COUNT = 5
       }
   }
   ```

3. **多语言语音引导**
   ```kotlin
   // 使用TTS播报操作指引
   fun provideVoiceGuidance(context: Context, step: CameraStep) {
       val message = when (step) {
           CameraStep.INITIALIZE -> "请将药盒正面朝向摄像头，保持平稳"
           CameraStep.FOCUSING -> "正在对焦，请稍候"
           CameraStep.STABLE -> "已稳定，可以拍照了"
           CameraStep.CAPTURED -> "照片已拍摄，正在识别中..."
           CameraStep.RESULT_READY -> "识别完成，请查看结果"
       }
       
       val tts = TextToSpeech(context) { status ->
           if (status == TextToSpeech.SUCCESS) {
               tts.language = Locale.CHINESE
               tts.speak(message, TextToSpeech.QUEUE_ADD, null, null)
           }
       }
   }
   ```

**具体要求**：
- [ ] 添加半透明对焦框（四角L形标记）
- [ ] 实现手持稳定检测（加速度传感器）
- [ ] 不稳定时显示"请保持手机平稳"提示
- [ ] 拍照成功后添加缩略图预览
- [ ] 支持从相册选择图片（权限申请）
- [ ] 历史识别记录快速访问（横向滚动列表）
- [ ] TTS语音引导（每一步都有语音提示）
- [ ] 支持前后摄像头切换（自拍模式用于识别手部标签）

##### 1.3.2 识别结果展示优化 (DrugResultScreen.kt)

**当前问题**：
- ❌ 结果展示过于简陋
- ❌ 缺少风险可视化
- ❌ 没有收藏和分享功能

**优化后的卡片式布局**：

```
┌─────────────────────────────────────┐
│ ← 返回       识别结果               │
│─────────────────────────────────────│
│                                     │
│  ┌─────────────────────────────┐   │
│  │  📸 药盒照片缩略图           │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  🔴 高风险                  │   │
│  │                             │   │
│  │  药品名称: 对乙酰氨基酚片    │   │
│  │  商品名: 泰诺               │   │
│  │  规格: 0.5g×12片            │   │
│  │  生产厂家: 强生制药          │   │
│  └─────────────────────────────┘   │
│                                     │
│  ⚠️ 风险提示                      │
│  ┌─────────────────────────────┐   │
│  │  🔴 过敏警告                │   │
│  │  您对该药品成分过敏！        │   │
│  │  成因: 含有对乙酰氨基酚      │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  🟡 注意事项                │   │
│  │  • 避免与酒精同时服用        │   │
│  │  • 肝功能不全者慎用          │   │
│  │  • 不可超量服用             │   │
│  └─────────────────────────────┘   │
│                                     │
│  💊 用法用量（TTS播报按钮）       │
│  ┌─────────────────────────────┐   │
│  │  成人: 每次1片，每日不超过3次│   │
│  │  儿童: 请遵医嘱使用          │   │
│  │  [🔊 语音播报]              │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌────────┐ ┌────────┐ ┌────────┐  │
│  │ ⭐收藏  │  │📤分享  │  │🔄重新  │  │
│  └────────┘ └────────┘ └────────┘  │
│                                     │
│  相关药品推荐                     │
│  ┌────┐ ┌────┐ ┌────┐           │
│  │布洛芬│ │阿司匹│ │感冒灵│         │
│  └────┘ └────┘ └────┘           │
└─────────────────────────────────────┘
```

**风险等级颜色编码**：
- 🟢 **LOW（低风险）**: `Color(0xFF4CAF50)` - 绿色
- 🟡 **MEDIUM（中等风险）**: `Color(0xFFFF9800)` - 橙色  
- 🔴 **HIGH（高风险）**: `Color(0xFFF44336)` - 红色
- ⚫ **CRITICAL（极高风险）**: `Color(0xFF9C27B0)` - 紫红+闪烁动画

**交互功能**：
- [ ] TTS一键播报全部信息
- [ ] 收藏到个人药箱
- [ ] 分享给家人/医生（生成图片或文本）
- [ ] 查看详细说明书（展开/折叠）
- [ ] 查看同类药品对比
- [ ] 设置用药提醒（联动提醒系统）

**验收标准**：
```bash
# UI测试
1. 打开相机 → 显示对焦框和引导文字 ✅
2. 拍照 → 显示加载动画 → 展示结果卡片 ✅
3. 结果卡片包含：药品名称、风险等级、过敏原匹配 ✅
4. 点击"语音播报" → TTS朗读药品信息 ✅
5. 点击"收藏" → 提示"已添加到我的药箱" ✅
6. 切换弱视模式 → 字体放大+高对比度 ✅
7. 开启TalkBack → 所有元素可被朗读 ✅
```

---

#### 任务1.4：网络层封装与接口联调 ⏱️ 预计：1天

**目标**：搭建Retrofit网络请求框架，对接后端API

##### 1.4.1 Retrofit配置

**依赖添加**（build.gradle）:
```gradle
dependencies {
    // Retrofit & OkHttp
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'
    
    // Gson
    implementation 'com.google.code.gson:gson:2.10.1'
}
```

**API Service定义**:
```kotlin
interface WeiguangApiService {
    
    // 认证相关
    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<UserResponse>>
    
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponse>>
    
    @POST("api/v1/auth/refresh")
    suspend fun refreshAccessToken(@Body request: RefreshTokenRequest): Response<ApiResponse<TokenResponse>>
    
    // 用户相关
    @GET("api/v1/users/profile")
    suspend fun getUserProfile(): Response<ApiResponse<UserProfileResponse>>
    
    @PUT("api/v1/users/password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ApiResponse<Unit>>
    
    // 药品相关
    @Multipart
    @POST("api/v1/drugs/recognition")
    suspend fun uploadRecognitionRecord(
        @Part image: MultipartBody.Part,
        @Part ocrText: MultipartBody.Part?
    ): Response<ApiResponse<DrugRecognitionResponse>>
    
    @GET("api/v1/drugs/history")
    suspend fun getRecognitionHistory(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PaginatedResponse<DrugHistoryItem>>)
    
    @GET("api/v1/drugs/{drugId}")
    suspend fun getDrugDetail(@Path("drugId") drugId: Int): Response<ApiResponse<DrugDetailResponse>>
    
    @POST("api/v1/drugs/allergen-check")
    suspend fun checkAllergens(@Body request: AllergenCheckRequest): Response<ApiResponse<AllergenCheckResponse>>
    
    // 紧急联系人
    @POST("api/v1/emergency-contacts")
    suspend fun addEmergencyContact(@Body request: ContactRequest): Response<ApiResponse<ContactResponse>>
    
    @GET("api/v1/emergency-contacts")
    suspend fun getEmergencyContacts(): Response<ApiResponse<List<ContactResponse>>>
    
    // SOS事件
    @POST("api/v1/sos/events")
    suspend fun createSosEvent(@Body request: SosEventRequest): Response<ApiResponse<SosEventResponse>>
}
```

##### 1.4.2 OkHttp拦截器配置

**AuthInterceptor（自动附加Token）**:
```kotlin
class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        val token = tokenManager.getAccessToken()
        
        if (token.isNotEmpty()) {
            val authenticatedRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            
            return chain.proceed(authenticatedRequest)
        }
        
        return chain.proceed(originalRequest)
    }
}
```

**TokenRefreshInterceptor（401自动刷新）**:
```kotlin
class TokenRefreshInterceptor(
    private val tokenManager: TokenManager,
    private val apiService: WeiguangApiService
) : Interceptor {
    
    @Volatile
    private var isRefreshing = false
    
    private val mutex = Mutex()
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        
        // 如果是401未授权且不是刷新Token接口本身
        if (response.code == 401 && !isRefreshing && !isTokenRefreshRequest(chain.request())) {
            
            runBlocking {
                mutex.withLock {
                    if (!isRefreshing) {
                        isRefreshing = true
                        
                        try {
                            // 同步刷新Token
                            val newToken = synchronizedRefreshToken()
                            
                            if (newToken != null) {
                                // 用新Token重新发起原始请求
                                response.close()
                                val newRequest = chain.request().newBuilder()
                                    .header("Authorization", "Bearer $newToken")
                                    .build()
                                return@withLock chain.proceed(newRequest)
                            }
                        } finally {
                            isRefreshing = false
                        }
                    }
                }
            }
        }
        
        return response
    }
    
    private suspend fun synchronizedRefreshToken(): String? {
        return try {
            val refreshToken = tokenManager.getRefreshToken()
            val response = apiService.refreshAccessToken(
                RefreshTokenRequest(refreshToken = refreshToken)
            )
            
            if (response.isSuccessful && response.body()?.code == 200) {
                val newAccessToken = response.body()!!.data!!.accessToken
                tokenManager.saveAccessToken(newAccessToken)
                newAccessToken
            } else {
                // Refresh Token也失效，需要重新登录
                tokenManager.clearTokens()
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
```

**LoggingInterceptor（调试日志）**:
```kotlin
class LoggingInterceptor : Interceptor {
    
    private val logger = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) 
            HttpLoggingInterceptor.Level.BODY 
        else 
            HttpLoggingInterceptor.Level.NONE
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        logger.intercept(chain)
        
        // 自定义日志输出（可选）
        Log.d("NetworkAPI", "Request: ${request.url}")
        
        return chain.proceed(request)
    }
}
```

##### 1.4.3 Repository层实现

**AuthRepository**:
```kotlin
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: WeiguangApiService,
    private val tokenManager: TokenManager
) : AuthRepository {
    
    override suspend fun login(phone: String, password: String): Result<AuthResponse> {
        return try {
            val response = apiService.login(LoginRequest(phone, password))
            
            if (response.isSuccessful && response.body()?.code == 200) {
                val authData = response.body()!!.data!!
                
                // 保存Token
                tokenManager.saveTokens(
                    accessToken = authData.accessToken,
                    refreshToken = authData.refreshToken
                )
                
                Result.success(authData)
            } else {
                val errorMessage = response.body()?.message ?: "登录失败"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun register(request: RegisterRequest): Result<UserResponse> {
        // 类似实现...
    }
}
```

**验收标准**：
```bash
# 网络联调测试
1. 登录 → 后端返回Token → 本地保存成功 ✅
2. 使用Token访问受保护接口 → 200 OK ✅
3. Token过期 → 自动刷新 → 请求重试成功 ✅
4. Refresh Token失效 → 跳转登录页 ✅
5. 网络断开 → 显示友好的离线提示 ✅
6. 请求超时（>10s）→ 显示"网络较慢，请稍后重试" ✅
```

---

### 优先级：P1 - Sprint 1 可选

#### 任务1.5：首页重构与Dashboard展示 ⏱️ 预计：1天

**目标**：打造个性化、信息丰富的首页体验

**新增内容**：
- [ ] 今日待办事项卡片（用药提醒、预约提醒等）
- [ ] 快捷入口网格（最近使用的3个功能）
- [ ] 健康数据概览（今日识别次数、SOS次数等）
- [ ] 天气与出行建议（结合位置服务）
- [ ] 社区动态推送（帮扶活动通知等）

#### 任务1.6：弱视模式增强 ⏱️ 预计：0.5天

**功能**：
- [ ] 全局字体大小切换（小/中/大/超大 4档）
- [ ] 高对比度主题（黑白反转、黄色背景黑字等）
- [ ] 图标+文字双重标识（避免纯图标难以辨认）
- [ ] 触控目标尺寸 >= 48dp（方便精准点击）
- [ ] 减少动画效果（避免视觉干扰）

---

## 🔒 开发约束

### ✅ 允许的操作
1. 在 `feature/frontend` 分支内自由修改UI代码
2. 从 `main` 分支定期同步最新代码
3. 创建子分支进行功能开发（`feature/frontend-xxx`）
4. 编写Espresso UI测试
5. 添加详细中文注释

### ❌ 禁止的操作
1. **禁止修改后端Python代码**
2. **禁止修改算法模型文件**
3. **禁止直接推送到 `main` 分支**
4. **禁止硬编码API地址**（使用BuildConfig或环境变量）
5. **禁止提交调试日志中的敏感信息**

### 🔄 与后端Agent协作协议

**需求模板**：
```
【前端需求 + feature/frontend + XXX界面开发】

请求方: 前端开发Agent
接收方: 后端开发Agent
优先级: P0/P1/P2
界面名称: XXXScreen
需要的接口: 
  - GET /api/v1/xxx (参数说明)
  - POST /api/v1/xxx (JSON格式)
期望响应格式: {...}
特殊要求: 
  - 需要分页吗？
  - 需要缓存策略吗？
  - 错误处理方式？
截止时间: YYYY-MM-DD
```

---

## 📝 Kotlin编码规范

### 命名规范
| 类型 | 规范 | 示例 |
|------|------|------|
| **类/Interface** | 大驼峰 | `LoginScreen`, `AuthRepository` |
| **函数/方法** | 小驼峰 | `getUserProfile()`, `onLoginClick()` |
| **变量** | 小驼峰 | `userName`, `isLoading` |
| **常量** | 全大写+下划线 | `MAX_RETRY_COUNT`, `BASE_URL` |
| **Compose函数** | 大驼峰（名词） | `LoginScreen()`, `DrugCard()` |
| **ViewModel** | xxxViewModel | `AuthViewModel`, `DrugViewModel` |
| **Repository** | xxxRepositoryImpl | `UserRepositoryImpl` |

### 注释规范
```kotlin
/**
 * 用户登录ViewModel
 * 
 * 职责：
 * - 管理登录表单状态（手机号、密码、loading等）
 * - 调用AuthRepository执行登录逻辑
 * - 处理Token持久化存储
 * 
 * 使用方式：
 * ```kotlin
 * val viewModel: AuthViewModel = hiltViewModel()
 * ```
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {
    
    /**
     * 执行用户登录
     * 
     * @param phone 手机号（11位数字）
     * @param password 密码（8-20位，含字母和数字）
     * @return Result<AuthResponse> 登录成功返回Token信息，失败返回异常
     */
    suspend fun login(phone: String, password: String): Result<AuthResponse> {
        // ...
    }
}
```

---

## 🧪 测试要求

### 单元测试（JUnit + Mockk）
```bash
# 运行测试
./gradlew testDebugUnitTest

# 目标覆盖率 >= 70%
```

**必须覆盖**：
- [ ] ViewModel的所有公开方法
- [ ] Repository的数据转换逻辑
- [ ] 工具类（日期格式化、距离计算等）
- [ ] 表单验证规则

### UI测试（Espresso）
```bash
# 运行UI测试
./gradlew connectedDebugAndroidTest
```

**关键路径测试**：
1. 启动App → 登录 → 首页正常显示
2. 药品识别完整流程：相机→拍照→结果展示→TTS播报
3. SOS求助流程：点击SOS→选择场景→发送短信→位置上传
4. 权限弹窗：相机/麦克风/定位/通知 → 全部允许/拒绝处理

---

## 🚀 性能优化指标

| 指标 | 目标值 | 当前基线 |
|------|--------|---------|
| **冷启动时间** | < 3秒 | 待测量 |
| **页面切换帧率** | ≥ 55fps | 待测量 |
| **内存占用（正常运行）** | < 200MB | 待测量 |
| **APK体积（Release）** | < 50MB | 待测量 |
| **电池消耗（后台挂起）** | < 5%/小时 | 待测量 |
| **网络请求平均延迟** | < 500ms | 待测量 |

---

## 📦 关键依赖版本

```gradle
// Core
implementation 'androidx.core:core-ktx:1.12.0'
implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.7.0'
implementation 'androidx.activity:activity-compose:1.8.2'

// Compose BOM
implementation platform('androidx.compose:compose-bom:2023.08.00')
implementation 'androidx.compose.ui:ui'
implementation 'androidx.compose.ui:ui-graphics'
implementation 'androidx.compose.material3:material3'

// Navigation
implementation 'androidx.navigation:navigation-compose:2.7.6'

// Hilt DI
implementation 'com.google.dagger:hilt-android:2.48'
kapt 'com.google.dagger:hilt-compiler:2.48'

// Networking
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.okhttp3:okhttp:4.12.0'

// Image Loading
implementation 'io.coil-kt:coil-compose:2.5.0'

// Room Database
implementation 'androidx.room:room-ktx:2.6.1'
kapt 'androidx.room:room-compiler:2.6.1'

// DataStore
implementation 'androidx.datastore:datastore-preferences:1.0.0'

// Testing
testImplementation 'junit:junit:4.13.2'
testImplementation 'io.mockk:mockk:1.13.8'
androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
```

---

## 📊 进度汇报模板

```markdown
## 前端开发进度报告

**模块**: XXX模块
**Agent**: 前端开发Agent
**分支**: feature/frontend
**时间**: 2026-MM-DD HH:MM

### 当前进度
- [x] 已完成任务1（登录页UI）
- [ ] 进行中任务2（药品识别重构，完成度70%）
- [ ] 待开始任务3（网络层封装）

### 产出物
- 新增/修改文件: X个
- 新增Screen/Composable: N个
- 代码行数: +XXXX / -YY
- UI组件: N个可复用组件

### 测试结果
- 单元测试: XX passed / YY failed
- UI自动化测试: ZZ% 通过率
- 性能指标: 启动X.X秒，内存XXMB

### 设计稿对比
- [ ] 登录页: ✅ 100%还原
- [ ] 药品识别页: ⚠️ 90%（待优化动画）
- [ ] 风险提示卡: ✅ 已实现

### 风险与阻塞
- 风险1: CameraX在部分机型闪退（已定位原因）
- 阻塞项: 等待后端API文档最终确认

### 下一步计划
1. 完成药品识别界面剩余30%
2. 开始网络层Retrofit封装
3. 编写接口联调测试用例
```

---

*本文件由微光同行多Agent并行开发系统自动生成*
*遵循前端开发规范 v1.0 | 最后更新: 2026-05-28*
