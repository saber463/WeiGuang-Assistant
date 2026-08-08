# 📱 Kotlin 编程语言学习手册

> **分类**：移动端  
> **描述**：Android官方开发语言，简洁现代的JVM语言  
> **生成时间**：2026-08-07  

---

## 目录

- [初级入门](#primary)（8个知识点）
- [中级进阶](#intermediate)（8个知识点）
- [高级精通](#advanced)（6个知识点）

---

## 初级入门

### 1. 环境搭建：Android Studio与第一个项目

> **级别**：初级 | **概念**：Android Studio是Google官方推荐的Android开发IDE，内置SDK管理器、AVD模拟器和Gradle构建系统，一站式完成开发环境搭建。

```kotlin
// ===== 初级：Android 开发环境搭建步骤 =====
// 注意：以下为环境配置说明，非可执行代码

// 步骤1：下载并安装 Android Studio
// 访问 https://developer.android.com/studio 下载最新版
// Windows 用户双击 .exe 安装，Mac 用户拖拽到 Applications

// 步骤2：首次启动时配置 SDK
// Android Studio → SDK Manager → SDK Platforms 标签页
// 勾选最新稳定版 API（如 Android 14 API 34）→ Apply

// 步骤3：创建 AVD 模拟器
// Android Studio → Device Manager → Create Device
// 选择手机型号（如 Pixel 6）→ 选择系统镜像（如 Tiramisu API 33）
// 启动模拟器：点击绿色 ▶ 按钮

// 步骤4：新建第一个 Hello World 项目
// File → New → New Project → Empty Activity → Next
// 填写项目名称：HelloWorld，包名：com.example.helloworld
// 语言选择：Kotlin，最小 SDK 选择：API 24 → Finish

// 步骤5：项目结构说明
// app/src/main/java/        → Kotlin 源代码目录
// app/src/main/res/layout/  → XML 布局文件目录
// app/src/main/AndroidManifest.xml → 应用清单文件
// build.gradle.kts          → 项目构建配置（Kotlin DSL）

// 步骤6：运行项目
// 点击工具栏绿色 ▶ 按钮，选择已创建的 AVD 模拟器
// Gradle 会自动编译并安装 APK 到模拟器
// 模拟器上将显示 "Hello World!" 文字
```

**💡 代码解释**：Android Studio 是安卓开发的官方 IDE，集成 SDK 管理器用于下载不同 API 版本的开发工具包，AVD 管理器用于创建和管理虚拟设备。Gradle 构建系统负责编译、打包和依赖管理。项目创建后自动生成基础 Activity 和布局文件，无需手动编写任何代码即可运行。

**🔑 关键要点**：
- Android Studio 是官方推荐的 IDE，内置 SDK 管理器、AVD 模拟器
- SDK Platform 对应 Android 系统版本，选择 API 24+ 覆盖主流设备
- AVD 模拟器用于本地测试，也可使用真机 USB 调试
- Gradle 是构建工具，build.gradle.kts 管理依赖和编译配置
- 项目结构：java 目录放代码，res 目录放资源，AndroidManifest.xml 声明组件

---

### 2. 变量与数据类型

> **级别**：初级 | **概念**：Kotlin 使用 val 声明不可变变量（只读），var 声明可变变量。支持类型推断，编译器能自动推导变量类型，同时提供 Int、String、Boolean、Double 等基本数据类型。

```kotlin
// ===== 初级：Kotlin 变量与数据类型 =====
fun main() {
    // --- val 声明不可变变量（只读，类似 Java 的 final）---
    val name: String = "小明"     // 显式声明类型为 String
    val age = 25                  // 类型推断：编译器自动推断为 Int 类型
    // name = "小红"              // ❌ 编译错误：val 不可重新赋值

    // --- var 声明可变变量（可重新赋值）---
    var score = 90                // 类型推断为 Int
    score = 95                    // ✅ var 可以重新赋值
    score = 100                   // ✅ 可以多次修改

    // --- 基本数据类型 ---
    val count: Int = 42           // 整数类型，32位
    val price: Double = 19.99     // 双精度浮点数，64位
    val isActive: Boolean = true  // 布尔类型，只有 true 或 false
    val letter: Char = 'A'        // 字符类型，用单引号
    val message: String = "Hello" // 字符串类型，用双引号
    val bigNumber: Long = 9999999999L // 长整型，64位，末尾加 L
    val small: Float = 3.14f      // 单精度浮点数，32位，末尾加 f

    // --- 字符串模板：在字符串中嵌入变量或表达式 ---
    val greeting = "你好，我叫$name，今年${age}岁" // $变量名 或 ${表达式}
    println(greeting)             // 输出：你好，我叫小明，今年25岁

    // --- 多行字符串（原始字符串）：用三个双引号包裹 ---
    val poem = """
        静夜思
        床前明月光
        疑是地上霜
    """.trimIndent()             // trimIndent() 去除公共缩进
    println(poem)
}
```

**💡 代码解释**：val 声明的是只读引用，初始化后不能指向新对象（但对象内部状态可变）。var 可以重新赋值。Kotlin 的类型推断机制让代码更简洁，编译器根据赋值自动确定类型。字符串模板用 $ 嵌入变量，${} 嵌入表达式，比 Java 的拼接更直观。三引号原始字符串支持多行文本，trimIndent() 自动去除缩进。

**🔑 关键要点**：
- val = 不可变引用（只读），var = 可变变量
- 类型推断让代码简洁，编译器自动推导类型
- 字符串模板：$变量名 或 ${表达式}
- 三引号 """ 创建多行原始字符串
- Long 末尾加 L，Float 末尾加 f，Char 用单引号

---

### 3. 空安全（Null Safety）

> **级别**：初级 | **概念**：Kotlin 的空安全机制是杜绝 NullPointerException 的核心设计。类型默认不可为空，可空类型需显式标记 ?。通过安全调用 ?.、Elvis 操作符 ?: 和非空断言 !! 安全处理空值。

```kotlin
// ===== 初级：Kotlin 空安全机制 =====
fun main() {
    // --- 不可空类型：默认不能为 null ---
    var name: String = "小明"
    // name = null               // ❌ 编译错误：String 类型不能为 null

    // --- 可空类型：类型后加 ? 表示可为 null ---
    var nickname: String? = "小可爱" // 可空 String 类型
    nickname = null               // ✅ 可空类型可以赋值为 null

    // --- 安全调用操作符 ?. ---
    // 如果调用者为 null，整个表达式返回 null，不会抛出空指针异常
    val length1: Int? = nickname?.length  // nickname 为 null，length1 = null
    val length2: Int? = name?.length      // name 不为 null，length2 = 2
    println("昵称长度: $length1, 名字长度: $length2")

    // --- 链式安全调用 ---
    data class Address(val city: String?)
    data class User(val address: Address?)
    val user: User? = User(null)          // 用户地址为 null
    val city: String? = user?.address?.city // 链式调用，任一环节为 null 则返回 null
    println("城市: $city")

    // --- Elvis 操作符 ?: ---
    val displayName = nickname ?: "匿名用户"  // nickname 为 null，使用默认值
    println("显示名称: $displayName")
    val realName = name ?: "匿名"            // name 不为 null，使用原值
    println("真实名称: $realName")

    // --- 非空断言 !! ---
    // 告诉编译器确定不为 null，如果为 null 则抛出 NullPointerException
    val known: String? = "确定有值"
    val forced: String = known!!           // 安全：known 确实不为 null
    println("强制取值: $forced")
    // val crash: String = nickname!!      // 💥 运行时崩溃：nickname 为 null

    // --- let 安全调用 + lambda ---
    nickname?.let { n ->
        println("昵称是: $n")               // nickname 为 null，此代码块不执行
    }
    name?.let { n ->
        println("名字是: $n")               // name 不为 null，输出
    }
}
```

**💡 代码解释**：Kotlin 的类型系统在编译期就区分可空和不可空，从源头防止 NullPointerException。?. 安全调用在调用者为 null 时短路返回 null。?: 提供默认值回退。!! 是危险操作，仅在确定不为 null 时使用，否则会崩溃。?.let 模式在非空时执行代码块，是 Kotlin 惯用的空安全写法。

**🔑 关键要点**：
- 默认类型不可为空，可空类型需加 ? 标记
- ?. 安全调用：为 null 时短路返回 null，不抛异常
- ?: Elvis 操作符：为 null 时使用默认值
- !! 非空断言：强制取值，null 时抛 NPE，谨慎使用
- ?.let { } 是惯用模式：非空时才执行代码块

---

### 4. 控制流：if/when 与循环

> **级别**：初级 | **概念**：Kotlin 的 if 是表达式（有返回值），when 是增强版 switch（支持任意类型匹配）。for 循环遍历区间和集合，while 循环处理条件判断。

```kotlin
// ===== 初级：Kotlin 控制流 =====
fun main() {
    // --- if 作为表达式（有返回值）---
    val score = 85
    val grade = if (score >= 90) {
        "优秀"                     // score >= 90 时返回
    } else if (score >= 80) {
        "良好"                     // 80 <= score < 90 时返回
    } else if (score >= 60) {
        "及格"
    } else {
        "不及格"
    }
    println("成绩: $score, 等级: $grade")  // 输出：成绩: 85, 等级: 良好

    // --- when 表达式（增强版 switch）---
    val day = 3
    val dayName = when (day) {
        1 -> "星期一"              // 匹配单个值
        2 -> "星期二"
        3, 4 -> "星期三或四"       // 匹配多个值，用逗号分隔
        in 5..7 -> "工作日末尾"    // 匹配范围 in 5..7
        else -> "无效日期"         // 默认分支
    }
    println("第${day}天: $dayName")       // 输出：第3天: 星期三或四

    // when 也可以用作类型判断
    fun describe(obj: Any): String = when (obj) {
        is String -> "字符串长度: ${obj.length}"  // is 判断类型
        is Int    -> "整数: $obj"
        is Boolean -> if (obj) "真" else "假"
        else      -> "未知类型"
    }
    println(describe("Hello"))   // 输出：字符串长度: 5
    println(describe(42))        // 输出：整数: 42

    // --- for 循环遍历区间 ---
    print("1到5: ")
    for (i in 1..5) {            // 闭区间，包含 1 和 5
        print("$i ")               // 输出：1 2 3 4 5
    }
    println()

    print("1到4(不含5): ")
    for (i in 1 until 5) {       // 半开区间，不包含 5
        print("$i ")               // 输出：1 2 3 4
    }
    println()

    print("步长2: ")
    for (i in 0..10 step 2) {    // 步长为 2
        print("$i ")               // 输出：0 2 4 6 8 10
    }
    println()

    // --- while 循环 ---
    var count = 3
    while (count > 0) {
        println("倒计时: $count")
        count--
    }
}
```

**💡 代码解释**：if-else 是表达式，最后一行自动作为返回值，省去三元运算符。when 比 switch 强大得多，支持值匹配、范围匹配、类型判断，不需要 break。for 循环配合区间操作符（..、until、downTo、step）实现灵活遍历。

**🔑 关键要点**：
- if 是表达式，有返回值，可替代三元运算符
- when 支持值匹配、范围匹配 in、类型判断 is
- .. 是闭区间，until 是半开区间，downTo 递减
- step 控制步长，for 遍历区间和集合
- while 先判断后执行，do-while 至少执行一次

---

### 5. 函数定义与参数

> **级别**：初级 | **概念**：Kotlin 用 fun 关键字定义函数。支持默认参数（调用时可省略）、命名参数（按名称传参）、单表达式函数（省略花括号和 return），让函数定义简洁灵活。

```kotlin
// ===== 初级：Kotlin 函数定义 =====

// --- 基本函数定义：fun 函数名(参数列表): 返回值类型 { 函数体 } ---
fun greet(name: String): String {
    return "你好，$name！"
}

// --- 单表达式函数：当函数体只有一条表达式时可简写 ---
fun add(a: Int, b: Int): Int = a + b   // 用 = 代替花括号和 return
fun square(x: Int) = x * x              // 返回值类型也可省略（类型推断）

// --- 默认参数：参数可以指定默认值，调用时可省略 ---
fun createUser(
    name: String,
    age: Int = 18,              // 默认年龄为 18
    city: String = "北京",       // 默认城市为北京
    isVip: Boolean = false      // 默认非 VIP
): String {
    val vipStatus = if (isVip) "VIP用户" else "普通用户"
    return "$name, ${age}岁, 来自$city, $vipStatus"
}

// --- 命名参数：调用时指定参数名，可打乱顺序 ---
fun sendMessage(to: String, content: String, urgent: Boolean = false) {
    val prefix = if (urgent) "【紧急】" else ""
    println("发送给 $to: $prefix$content")
}

// --- 可变参数 vararg：接受不定数量的同类型参数 ---
fun sumAll(vararg numbers: Int): Int {
    return numbers.sum()        // numbers 在函数内部是 IntArray 类型
}

fun main() {
    println(greet("小明"))                      // 输出：你好，小明！
    println("5 + 3 = ${add(5, 3)}")             // 输出：5 + 3 = 8
    println("6 的平方 = ${square(6)}")           // 输出：6 的平方 = 36

    // 调用带默认参数的函数
    println(createUser("张三"))                  // 只传必填参数，其余用默认值
    println(createUser("李四", age = 25, isVip = true))

    // 调用命名参数（可打乱顺序）
    sendMessage(content = "会议通知", urgent = true, to = "全体成员")

    // 调用可变参数函数
    println("总和: ${sumAll(1, 2, 3, 4, 5)}")   // 输出：总和: 15
    println("总和: ${sumAll(10, 20)}")           // 输出：总和: 30
}
```

**💡 代码解释**：fun 关键字定义函数，参数类型写在参数名后（Pascal 风格）。单表达式函数用 = 连接，省略 return 和花括号。默认参数减少重载需求，命名参数让调用更清晰。vararg 可变参数接收任意数量实参，在函数体内作为数组使用。

**🔑 关键要点**：
- fun 函数名(参数: 类型): 返回类型 { } 定义函数
- 单表达式函数用 = 省略花括号和 return
- 默认参数给参数设定默认值，调用时可省略
- 命名参数按名称传参，可打乱顺序，提高可读性
- vararg 接收不定数量参数，函数内部是数组

---

### 6. 类与对象

> **级别**：初级 | **概念**：Kotlin 的类定义简洁，主构造函数直接写在类名后。init 块用于初始化逻辑，data class 自动生成 equals/hashCode/toString/copy 等方法，大幅减少样板代码。

```kotlin
// ===== 初级：Kotlin 类与对象 =====

// --- 基本类定义：主构造函数直接写在类名后面 ---
class Person(
    val name: String,           // val 声明属性，自动生成 getter
    var age: Int                // var 声明可变属性，自动生成 getter 和 setter
) {
    var hobby: String = "无"    // 类体内定义的属性，有默认值

    // init 初始化块：主构造函数调用时执行，可写初始化逻辑
    init {
        println("Person 初始化: $name, $age 岁")
        require(age >= 0) { "年龄不能为负数" }
    }

    // 次构造函数：用 constructor 关键字，必须委托给主构造函数
    constructor(name: String) : this(name, age = 0) {
        println("使用次构造函数创建 $name")
    }

    // 成员函数（方法）
    fun introduce(): String {
        return "我叫$name，今年${age}岁，爱好是$hobby"
    }

    // 自定义 setter
    var nickname: String = name
        set(value) {
            field = value.trim()    // 自动去除首尾空格
        }
}

// --- data class 数据类：自动生成 equals/hashCode/toString/copy ---
data class User(
    val id: Int,
    val username: String,
    var email: String
)

fun main() {
    val person = Person("小明", 25)
    person.hobby = "编程"
    println(person.introduce())

    val baby = Person("宝宝")                  // 年龄默认为 0
    println(baby.introduce())

    person.nickname = "  阿明  "
    println("昵称: '${person.nickname}'")      // 自动去除空格

    // data class 使用
    val user1 = User(1, "xiaoming", "xm@test.com")
    val user2 = User(1, "xiaoming", "xm@test.com")
    println(user1)                             // 自动生成 toString()
    println("内容相等: ${user1 == user2}")      // 比较属性值

    // copy 方法：复制对象并修改部分属性
    val user3 = user1.copy(email = "new@test.com")
    println(user3)

    // 解构声明：将 data class 属性分别赋值
    val (id, name, email) = user1
    println("解构: id=$id, name=$name, email=$email")
}
```

**💡 代码解释**：Kotlin 类定义将主构造函数与属性声明合并，减少样板代码。init 块在对象创建时执行。data class 专为数据载体设计，自动生成 equals/hashCode/toString/copy/componentN 方法，配合解构声明使用非常方便。

**🔑 关键要点**：
- 主构造函数写在类名后，用 val/var 声明属性
- init 块在对象创建时执行初始化和校验逻辑
- data class 自动生成 equals/hashCode/toString/copy
- copy() 方法复制对象时可选择性修改属性
- 创建对象不需要 new 关键字，解构声明提取 data class 属性

---

### 7. 集合操作

> **级别**：初级 | **概念**：Kotlin 提供丰富的集合 API，包括不可变集合（listOf/setOf/mapOf）和可变集合（mutableListOf/mutableSetOf/mutableMapOf）。filter、map、fold 等函数式操作让数据处理简洁高效。

```kotlin
// ===== 初级：Kotlin 集合操作 =====
fun main() {
    // --- 不可变 List（只读列表）---
    val fruits = listOf("苹果", "香蕉", "橘子", "葡萄", "西瓜")
    println("第一个水果: ${fruits[0]}")          // 索引访问
    println("最后一个: ${fruits.last()}")        // last() 获取最后一个元素
    println("水果数量: ${fruits.size}")           // size 属性获取长度

    // --- 可变 List（可增删改）---
    val mutableFruits = mutableListOf("苹果", "香蕉")
    mutableFruits.add("草莓")                    // 添加元素
    mutableFruits.add(0, "樱桃")                 // 指定位置插入
    mutableFruits.remove("香蕉")                 // 删除指定元素
    println("可变列表: $mutableFruits")

    // --- Set 集合（元素不重复，无序）---
    val tags = setOf("Kotlin", "Android", "开发", "Kotlin") // 重复自动去重
    println("标签集合: $tags")
    println("包含Kotlin: ${tags.contains("Kotlin")}")

    // --- Map 字典（键值对）---
    val scores = mapOf(
        "小明" to 95,              // to 是中缀函数，创建 Pair 键值对
        "小红" to 88,
        "小刚" to 72
    )
    println("小明的成绩: ${scores["小明"]}")     // 通过 key 获取 value

    // --- filter 过滤：筛选满足条件的元素 ---
    val numbers = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    val evenNumbers = numbers.filter { it % 2 == 0 }  // 筛选偶数
    println("偶数: $evenNumbers")

    val highScores = scores.filter { (_, value) -> value >= 80 }
    println("80分以上: $highScores")

    // --- map 映射：将每个元素转换为新值 ---
    val doubled = numbers.map { it * 2 }          // 每个元素乘以 2
    println("翻倍: $doubled")

    // --- fold 折叠：带初始值的累积操作 ---
    val sum = numbers.fold(0) { acc, num -> acc + num }  // 从 0 开始累加
    println("总和: $sum")

    // --- 链式调用：组合多个操作 ---
    val result = numbers
        .filter { it > 3 }                        // 先过滤：保留大于 3 的
        .map { it * 10 }                          // 再映射：乘以 10
        .sortedDescending()                       // 降序排序
    println("链式处理: $result")
}
```

**💡 代码解释**：不可变集合（listOf/mapOf/setOf）创建后内容不可修改，是 Kotlin 推荐的默认选择。可变集合（mutableListOf等）支持增删改。filter 返回满足条件的元素，map 转换每个元素，fold 从初始值开始累积计算。这些操作都返回新集合，支持链式调用。

**🔑 关键要点**：
- listOf/mapOf/setOf 创建不可变集合，mutable 前缀创建可变集合
- filter 返回满足条件的元素，map 转换每个元素
- fold 从初始值开始累积计算，reduce 用第一个元素作初始值
- 集合操作返回新集合，支持链式调用
- it 是 lambda 单参数的隐式名称，to 中缀函数创建 Pair

---

### 8. Android 基础：Activity 与 Intent

> **级别**：初级 | **概念**：Activity 是 Android 四大组件之一，代表一个用户界面屏幕。Intent 用于组件间通信和页面跳转。Toast 显示短暂提示，Log 用于调试输出。

```kotlin
// ===== 初级：Android Activity 基础 =====
package com.example.myapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

// Activity 继承自 AppCompatActivity（兼容旧版 Android 的 Activity 基类）
class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    // onCreate 是 Activity 生命周期入口，在界面创建时调用
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)        // 必须调用父类方法
        setContentView(R.layout.activity_main)    // 设置布局文件

        // Log 日志输出：级别从低到高为 Verbose/Debug/Info/Warn/Error
        Log.d(TAG, "onCreate: Activity 已创建")    // Debug 级别日志

        // Toast 短提示：在屏幕底部弹出短暂消息
        Toast.makeText(this, "欢迎来到我的应用！", Toast.LENGTH_SHORT).show()

        // 通过 findViewById 获取布局中的按钮控件
        val btnNavigate: Button = findViewById(R.id.btn_navigate)

        // 设置按钮点击监听器
        btnNavigate.setOnClickListener {
            // Intent 显式跳转：指定目标 Activity 类
            val intent = Intent(this, SecondActivity::class.java)
            intent.putExtra("username", "小明")     // 键值对方式传递数据
            intent.putExtra("age", 25)
            startActivity(intent)                  // 启动目标 Activity
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: Activity 变为可见")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: Activity 获得焦点")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: Activity 失去焦点")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: Activity 不可见")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: Activity 即将销毁")
    }
}

// ===== 目标 Activity：接收 Intent 传递的数据 =====
class SecondActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SecondActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        val username = intent.getStringExtra("username") ?: "未知用户"
        val age = intent.getIntExtra("age", 0)

        Log.d(TAG, "收到数据: username=$username, age=$age")
        Toast.makeText(this, "你好，$username！", Toast.LENGTH_SHORT).show()
    }
}
```

**💡 代码解释**：Activity 是 Android 界面的基本单元，完整生命周期为 onCreate → onStart → onResume → onPause → onStop → onDestroy。Intent 分显式（指定目标类）和隐式（指定 Action）。Log 日志通过 tag 过滤，Toast 用于轻量级用户提示。

**🔑 关键要点**：
- Activity 继承 AppCompatActivity，onCreate 是入口
- 生命周期：onCreate → onStart → onResume → onPause → onStop → onDestroy
- Intent 实现页面跳转，putExtra 传递数据
- Toast 显示短暂提示，Log 输出调试日志
- findViewById 获取布局控件，setOnClickListener 设置点击事件

---

## 中级进阶

### 1. 扩展函数与扩展属性

> **级别**：中级 | **概念**：扩展函数可以在不继承或修改原始类的情况下，为已有类添加新功能。扩展属性则为类添加新的属性访问器。这是 Kotlin 实现组合优于继承的重要机制。

```kotlin
// ===== 中级：扩展函数与扩展属性 =====

// 为 String 类添加扩展函数：统计字符出现次数
fun String.countChar(char: Char): Int {
    return this.count { it == char }  // this 指向调用该扩展函数的 String 实例
}

// 判断字符串是否为有效的手机号
fun String.isValidPhoneNumber(): Boolean {
    return this.matches(Regex("^1[3-9]\\d{9}$"))
}

// 将字符串中每个单词首字母大写
fun String.toTitleCase(): String {
    return this.split(" ")
        .joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercase() }
        }
}

// 为集合类添加扩展函数：获取第二个元素
fun <T> List<T>.secondOrNull(): T? {
    return if (this.size >= 2) this[1] else null
}

// 计算 List<Int> 的中位数
fun List<Int>.median(): Double {
    if (this.isEmpty()) return 0.0
    val sorted = this.sorted()
    val mid = sorted.size / 2
    return if (sorted.size % 2 == 0) {
        (sorted[mid - 1] + sorted[mid]) / 2.0
    } else {
        sorted[mid].toDouble()
    }
}

// --- 扩展属性 ---
val String.wordCount: Int
    get() = this.trim().split("\\s+".toRegex()).size

val Int.isEven: Boolean
    get() = this % 2 == 0

// 可空类型的扩展函数
fun String?.isNullOrBlank(): Boolean {
    return this == null || this.isBlank()
}

fun main() {
    val text = "hello world kotlin extensions"
    println("'l' 出现次数: ${text.countChar('l')}")        // 输出：4
    println("首字母大写: ${text.toTitleCase()}")            // Hello World Kotlin Extensions
    println("单词数: ${text.wordCount}")                     // 输出：4

    val phone = "13812345678"
    println("$phone 是手机号: ${phone.isValidPhoneNumber()}") // 输出：true

    val numbers = listOf(3, 1, 4, 2, 5, 8, 6, 7)
    println("第二个元素: ${numbers.secondOrNull()}")          // 输出：1
    println("中位数: ${numbers.median()}")                    // 输出：4.5

    val num = 10
    println("$num 是偶数: ${num.isEven}")                    // 输出：true

    val nullStr: String? = null
    println("null 是空: ${nullStr.isNullOrBlank()}")         // 输出：true
}
```

**💡 代码解释**：扩展函数通过 fun 类名.函数名() 定义，在函数体内通过 this 访问被扩展的实例。扩展函数本质是静态方法，编译后不会修改原始类。扩展属性只能定义 getter（和 setter），不能有幕后字段 field。可空类型的扩展函数即使在 null 上调用也不会抛异常。

**🔑 关键要点**：
- 扩展函数语法：fun 接收者类型.函数名() { }，this 指向接收者
- 扩展是静态解析的，不会修改原始类，只是语法糖
- 扩展属性只能定义 getter/setter，不能有幕后字段 field
- 可空类型扩展函数可在 null 上安全调用
- 扩展函数是 Kotlin 实现组合优于继承的关键机制

---

### 2. 协程基础（Coroutines）

> **级别**：中级 | **概念**：协程是 Kotlin 的轻量级并发框架，用同步写法实现异步操作。launch 启动不返回结果的协程，async 启动返回结果的协程。suspend 函数可挂起不阻塞线程，Dispatchers 控制协程运行线程。

```kotlin
// ===== 中级：Kotlin 协程基础 =====
import kotlinx.coroutines.*

// suspend 挂起函数：可在协程中调用，执行耗时操作
suspend fun fetchUserData(userId: Int): String {
    delay(1000)                                  // 模拟网络请求耗时 1 秒
    return "用户${userId}的数据"
}

suspend fun queryDatabase(query: String): List<String> {
    delay(500)                                   // 模拟数据库查询耗时 0.5 秒
    return listOf("结果1", "结果2", "结果3")
}

suspend fun riskyApiCall(): String {
    delay(800)
    if (Math.random() < 0.3) {
        throw Exception("网络连接失败！")
    }
    return "API 返回的数据"
}

fun main() = runBlocking {
    // runBlocking 桥接普通函数和协程世界
    println("=== 协程基础示例 ===")

    // launch：启动协程，不返回结果
    val job: Job = launch {
        println("launch 协程开始")
        val data = fetchUserData(1)
        println("获取到数据: $data")
    }

    // async：启动协程，返回 Deferred 结果
    val deferred: Deferred<String> = async {
        println("async 协程开始")
        fetchUserData(2)
    }

    // 启动多个 async 实现并发请求
    val deferred1 = async { fetchUserData(3) }
    val deferred2 = async { fetchUserData(4) }
    val result = "${deferred1.await()} + ${deferred2.await()}"
    println("并发结果: $result")

    job.join()                                   // join() 等待协程执行完成
    val asyncResult = deferred.await()           // await() 获取 async 的结果
    println("async 结果: $asyncResult")

    println("\n=== 协程上下文与调度器 ===")

    launch(Dispatchers.Default) {                // CPU 密集型任务
        println("Default: ${Thread.currentThread().name}")
    }

    launch(Dispatchers.IO) {                     // IO 密集型任务
        println("IO: ${Thread.currentThread().name}")
    }

    // withContext：切换协程上下文执行代码块
    launch(Dispatchers.Main) {
        val dbResult = withContext(Dispatchers.IO) {
            queryDatabase("SELECT * FROM users")
        }
        println("数据库结果: $dbResult")
    }

    println("\n=== 协程异常处理 ===")

    launch {
        try {
            val apiResult = riskyApiCall()
            println("API 成功: $apiResult")
        } catch (e: Exception) {
            println("API 失败: ${e.message}")
        }
    }

    // supervisorScope：子协程异常不影响兄弟协程
    supervisorScope {
        launch {
            delay(100)
            println("兄弟协程1: 正常执行")
        }
        launch {
            throw RuntimeException("兄弟协程2: 出错啦")
        }
        launch {
            delay(200)
            println("兄弟协程3: 仍正常执行")
        }
    }

    println("所有协程执行完毕")
}
```

**💡 代码解释**：协程是 Kotlin 解决异步编程的核心方案。launch 用于发射后不管的场景，async 用于需要返回值的并发任务。Dispatchers 决定协程运行线程：Main 用于 UI，IO 用于网络/数据库，Default 用于 CPU 计算。supervisorScope 隔离异常不让兄弟协程受影响。

**🔑 关键要点**：
- launch 启动不返回结果的协程，返回 Job
- async 启动返回结果的协程，返回 Deferred，通过 await() 获取
- suspend 函数可挂起不阻塞线程，只能在协程或挂起函数中调用
- Dispatchers.Main/IO/Default 控制协程运行线程
- withContext 切换上下文执行代码块，try-catch 捕获协程异常

---

### 3. Flow 响应式数据流

> **级别**：中级 | **概念**：Flow 是 Kotlin 协程中的冷数据流，支持异步、按需发射数据。通过操作符（map/filter/collect）处理数据流，StateFlow 和 SharedFlow 用于状态管理和事件分发。

```kotlin
// ===== 中级：Kotlin Flow 响应式数据流 =====
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

// 创建 Flow：flow 构建器创建冷流
fun createNumberFlow(): Flow<Int> = flow {
    println("Flow 开始发射数据")
    for (i in 1..5) {
        delay(500)                               // 模拟异步操作
        emit(i)                                  // emit() 发射数据到流中
        println("已发射: $i")
    }
}

fun main() = runBlocking {
    println("=== Flow 基础操作 ===\n")

    // Flow 操作符链式处理
    createNumberFlow()
        .filter { it % 2 != 0 }                  // 过滤：只保留奇数
        .map { it * 10 }                         // 映射：每个值乘以 10
        .onEach { println("处理中: $it") }        // 副作用：每个元素执行操作
        .collect { value ->                      // collect 是终端操作符
            println("收集到: $value")
        }

    println("\n=== Flow 组合操作 ===\n")

    // zip：配对两个流的数据
    val flow1 = flowOf(1, 2, 3)
    val flow2 = flowOf("A", "B", "C")
    flow1.zip(flow2) { num, letter ->
        "$num$letter"
    }.collect { println("zip结果: $it") }        // 输出：1A, 2B, 3C

    // combine：组合两个流的最新值
    val priceFlow = flow {
        emit(10.0)
        delay(200)
        emit(12.0)
    }
    val quantityFlow = flow {
        emit(5)
        delay(100)
        emit(8)
    }
    priceFlow.combine(quantityFlow) { price, qty ->
        "总价: ${price * qty}元"
    }.collect { println(it) }

    println("\n=== StateFlow 状态管理 ===\n")

    val _uiState = MutableStateFlow("初始状态")   // 可变状态流
    val uiState: StateFlow<String> = _uiState    // 对外暴露不可变版本

    val collectorJob = launch {
        uiState.collect { state ->
            println("UI状态更新: $state")
        }
    }

    delay(100)
    _uiState.value = "加载中..."
    delay(100)
    _uiState.value = "加载完成"
    delay(100)
    println("当前状态: ${uiState.value}")
    collectorJob.cancel()

    println("\n=== SharedFlow 事件分发 ===\n")

    val _events = MutableSharedFlow<String>(
        replay = 1,
        extraBufferCapacity = 5
    )
    val events: SharedFlow<String> = _events

    launch {
        events.collect { event ->
            println("订阅者1 收到: $event")
        }
    }

    delay(50)
    _events.emit("用户登录事件")
    _events.emit("数据更新事件")

    delay(100)

    launch {
        events.collect { event ->
            println("订阅者2 收到: $event")
        }
    }

    delay(100)
    _events.emit("新消息事件")

    delay(200)
    println("\n所有 Flow 操作完成")
}
```

**💡 代码解释**：Flow 是冷流，只有调用 collect 终端操作符时才执行。操作符（filter/map/onEach）是中间操作，返回新 Flow。StateFlow 是有状态的热流，始终持有最新值，适合 UI 状态管理。SharedFlow 是无状态的热流，适合事件分发，支持 replay 缓存和 buffer 配置。

**🔑 关键要点**：
- Flow 是冷流，collect 触发执行，操作符链式处理
- filter/map/onEach 是中间操作符，collect 是终端操作符
- StateFlow 持有最新值，适合 UI 状态管理
- SharedFlow 是无状态热流，适合一次性事件分发
- zip 配对两个流，combine 组合最新值

---

### 4. 密封类与枚举

> **级别**：中级 | **概念**：密封类（sealed class）用于表示受限的类层次结构，子类在编译期已知。配合 when 表达式可实现穷尽匹配，编译器会检查是否覆盖所有分支。枚举（enum class）适合表示固定常量集合。

```kotlin
// ===== 中级：密封类与枚举 =====

// --- 枚举类：表示固定的常量集合 ---
enum class OrderStatus(val code: Int, val description: String) {
    PENDING(0, "待支付"),
    PAID(1, "已支付"),
    SHIPPING(2, "配送中"),
    DELIVERED(3, "已送达"),
    CANCELLED(4, "已取消");

    fun isFinal(): Boolean {
        return this == DELIVERED || this == CANCELLED
    }

    fun canTransitionTo(target: OrderStatus): Boolean {
        return when (this) {
            PENDING -> target == PAID || target == CANCELLED
            PAID -> target == SHIPPING || target == CANCELLED
            SHIPPING -> target == DELIVERED
            DELIVERED, CANCELLED -> false
        }
    }

    companion object {
        fun fromCode(code: Int): OrderStatus {
            return entries.first { it.code == code }
        }
    }
}

// --- 密封类：表示网络请求结果 ---
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val code: Int, val message: String) : NetworkResult<Nothing>()
    data object Loading : NetworkResult<Nothing>()

    fun <R> fold(
        onSuccess: (T) -> R,
        onError: (Int, String) -> R,
        onLoading: () -> R
    ): R {
        return when (this) {
            is Success -> onSuccess(data)
            is Error -> onError(code, message)
            is Loading -> onLoading()
            // 编译器会检查 when 是否覆盖所有分支，无需 else
        }
    }
}

// --- 密封类：表示 UI 状态（MVVM 中常用）---
sealed class UiState<out T> {
    data object Idle : UiState<Nothing>()
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

fun main() {
    println("=== 枚举类使用 ===\n")

    OrderStatus.entries.forEach { status ->
        println("${status.description} (code=${status.code}) 终态=${status.isFinal()}")
    }

    val current = OrderStatus.PAID
    println("\n当前状态: ${current.description}")
    println("可以配送到: ${current.canTransitionTo(OrderStatus.SHIPPING)}")  // true

    val status = OrderStatus.fromCode(3)
    println("code=3 对应: ${status.description}")  // 输出：已送达

    println("\n=== 密封类使用 ===\n")

    fun handleResult(result: NetworkResult<String>) {
        val message = result.fold(
            onSuccess = { data -> "✅ 成功: $data" },
            onError = { code, msg -> "❌ 错误[$code]: $msg" },
            onLoading = { "⏳ 加载中..." }
        )
        println(message)
    }

    handleResult(NetworkResult.Success("用户数据加载完成"))
    handleResult(NetworkResult.Error(404, "资源未找到"))
    handleResult(NetworkResult.Loading)

    println("\n=== UI 状态处理 ===\n")

    fun renderUi(state: UiState<String>) {
        when (state) {
            is UiState.Idle -> println("界面空闲，等待用户操作")
            is UiState.Loading -> println("显示加载动画...")
            is UiState.Success -> println("显示数据: ${state.data}")
            is UiState.Error -> println("显示错误提示: ${state.message}")
            // 编译期保证所有分支已覆盖
        }
    }

    renderUi(UiState.Idle)
    renderUi(UiState.Loading)
    renderUi(UiState.Success("Hello Kotlin!"))
    renderUi(UiState.Error("网络连接失败"))
}
```

**💡 代码解释**：密封类限制子类在编译期已知，配合 when 表达式实现穷尽匹配，编译器会检查是否遗漏分支。枚举适合表示固定数量的常量，每个常量可带属性和方法。密封类常用于表示网络请求结果（Success/Error/Loading）和 UI 状态管理，是 MVVM 架构中推荐的状态建模方式。

**🔑 关键要点**：
- sealed class 子类在编译期已知，when 穷尽匹配无需 else
- 枚举类 enum class 表示固定常量，可带属性和方法
- 密封类常用于 Result 模式、UI 状态管理
- data object 是单例数据对象（Kotlin 1.9+），替代 object 声明
- fold 方法封装 when 分支，提供函数式处理方式

---

### 5. 高阶函数与 Lambda

> **级别**：中级 | **概念**：高阶函数是接收函数作为参数或返回函数的函数。Kotlin 全面支持函数式编程，lambda 表达式、函数引用、内联函数等特性让代码更简洁、更灵活。

```kotlin
// ===== 中级：高阶函数与 Lambda =====

// 高阶函数：接收函数类型作为参数
fun performOperation(a: Int, b: Int, operation: (Int, Int) -> Int): Int {
    println("执行操作: $a 和 $b")
    return operation(a, b)                       // 调用传入的函数
}

// 高阶函数：接收条件函数过滤列表
fun <T> List<T>.customFilter(predicate: (T) -> Boolean): List<T> {
    val result = mutableListOf<T>()
    for (item in this) {
        if (predicate(item)) {
            result.add(item)
        }
    }
    return result
}

// 高阶函数：返回函数（创建乘法函数工厂）
fun createMultiplier(factor: Int): (Int) -> Int {
    return { number -> number * factor }         // 闭包捕获了 factor 变量
}

// 内联函数：减少 lambda 对象创建开销
inline fun measureTime(block: () -> Unit): Long {
    val start = System.currentTimeMillis()
    block()
    val end = System.currentTimeMillis()
    return end - start
}

// 内联函数配合 crossinline：禁止 lambda 中的非局部返回
inline fun runSafely(crossinline action: () -> Unit) {
    try {
        action()
    } catch (e: Exception) {
        println("捕获异常: ${e.message}")
    }
}

fun main() {
    println("=== 高阶函数基本使用 ===\n")

    // 方式1：传入 lambda 表达式
    val result1 = performOperation(10, 5) { x, y -> x + y }
    println("10 + 5 = $result1")                 // 输出：10 + 5 = 15

    // 方式2：传入函数引用（用 :: 获取函数引用）
    fun multiply(a: Int, b: Int): Int = a * b
    val result2 = performOperation(10, 5, ::multiply)
    println("10 * 5 = $result2")                 // 输出：10 * 5 = 50

    // 方式3：传入匿名函数
    val result3 = performOperation(10, 5, fun(a: Int, b: Int): Int {
        return a - b
    })
    println("10 - 5 = $result3")                 // 输出：10 - 5 = 5

    println("\n=== 自定义 filter 使用 ===\n")

    val numbers = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    val evenNumbers = numbers.customFilter { it % 2 == 0 }
    println("偶数: $evenNumbers")

    println("\n=== 返回函数的函数 ===\n")

    val double = createMultiplier(2)
    val triple = createMultiplier(3)
    println("5 × 2 = ${double(5)}")
    println("5 × 3 = ${triple(5)}")

    println("\n=== 内联函数使用 ===\n")

    val elapsed = measureTime {
        var sum = 0L
        for (i in 1..1_000_000) {
            sum += i
        }
        println("1到100万累加: $sum")
    }
    println("耗时: ${elapsed}ms")
}
```

**💡 代码解释**：高阶函数以函数类型 (A, B) -> C 作为参数，让代码更灵活。lambda 作为最后一个参数时可移到括号外。inline 将函数体直接插入调用处，避免 lambda 对象创建开销，适合高频调用的小函数。crossinline 禁止 lambda 中的非局部返回，防止意外跳出外层函数。

**🔑 关键要点**：
- 高阶函数接收或返回函数，(Int, Int) -> Int 是函数类型
- lambda 作最后一个参数可移到括号外，:: 获取函数引用
- inline 内联函数避免 lambda 对象创建，提升性能
- crossinline 禁止非局部返回，noinline 阻止内联
- 闭包能捕获外部变量，函数引用 :: 是轻量级函数指针

---

### 6. Jetpack Compose 基础

> **级别**：中级 | **概念**：Jetpack Compose 是 Android 现代声明式 UI 框架。用 @Composable 注解标记 UI 函数，通过 State 驱动界面更新。Column/Row/Box 是核心布局组件，Modifier 用于样式和交互配置。

```kotlin
// ===== 中级：Jetpack Compose 基础 =====
package com.example.myapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 数据类：表示待办事项
data class TodoItem(val id: Int, val title: String, val isCompleted: Boolean)

// @Composable 函数：声明式 UI 组件
@Composable
fun TodoApp() {
    // remember + mutableStateOf：Compose 的状态管理
    var todoList by remember {
        mutableStateOf(
            listOf(
                TodoItem(1, "学习 Kotlin 协程", false),
                TodoItem(2, "完成 Compose 布局", true),
                TodoItem(3, "编写单元测试", false)
            )
        )
    }

    var inputText by remember { mutableStateOf("") }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("我的待办事项") })
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // Row 水平布局容器
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("添加新任务...") },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                val newId = (todoList.maxOfOrNull { it.id } ?: 0) + 1
                                todoList = todoList + TodoItem(
                                    id = newId,
                                    title = inputText.trim(),
                                    isCompleted = false
                                )
                                inputText = ""
                            }
                        }
                    ) {
                        Text("添加")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // LazyColumn 懒加载列表（类似 RecyclerView）
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(todoList, key = { it.id }) { todo ->
                        TodoItemCard(
                            todo = todo,
                            onToggle = {
                                todoList = todoList.map { item ->
                                    if (item.id == todo.id) {
                                        item.copy(isCompleted = !item.isCompleted)
                                    } else item
                                }
                            }
                        )
                    }
                }

                val completedCount = todoList.count { it.isCompleted }
                Text(
                    text = "已完成: $completedCount / ${todoList.size}",
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}

// 单个待办事项卡片组件
@Composable
fun TodoItemCard(todo: TodoItem, onToggle: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (todo.isCompleted)
                Color(0xFFE8F5E9)
            else
                Color(0xFFFFFFFF)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = todo.isCompleted,
                onCheckedChange = { onToggle() }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = todo.title,
                fontSize = 16.sp,
                fontWeight = if (todo.isCompleted)
                    FontWeight.Normal
                else
                    FontWeight.Medium,
                color = if (todo.isCompleted) Color.Gray else Color.Black
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTodoApp() {
    TodoApp()
}
```

**💡 代码解释**：Compose 是声明式 UI 框架，@Composable 函数描述 UI，组件状态变化时自动重组。remember 保存重组间的状态，mutableStateOf 创建可观察状态。Column/Row 是线性布局，Box 是层叠布局，LazyColumn 是高性能列表。Modifier 链式调用来配置 padding、大小、点击等属性和行为。

**🔑 关键要点**：
- @Composable 声明 UI 函数，状态变化自动重组界面
- remember + mutableStateOf 管理 Compose 状态
- Column 垂直布局、Row 水平布局、Box 层叠布局
- LazyColumn 懒加载列表，性能优于传统 RecyclerView
- Modifier 链式配置样式和行为，如 padding、clickable、fillMaxWidth

---

### 7. ViewModel 与 LiveData/StateFlow

> **级别**：中级 | **概念**：ViewModel 是 MVVM 架构的核心，负责管理 UI 相关数据，生命周期感知（屏幕旋转时数据不丢失）。LiveData 和 StateFlow 用于在 ViewModel 和 View 之间传递数据，两者都是生命周期感知的。

```kotlin
// ===== 中级：ViewModel + StateFlow 示例 =====
package com.example.myapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// UI 状态密封类
sealed class UserListUiState {
    data object Loading : UserListUiState()
    data class Success(val users: List<User>) : UserListUiState()
    data class Error(val message: String) : UserListUiState()
}

// 用户数据类
data class User(val id: Int, val name: String, val email: String)

// 模拟网络数据源
object UserRepository {
    suspend fun fetchUsers(): List<User> {
        delay(2000)                              // 模拟网络延迟 2 秒
        if (Math.random() < 0.1) {
            throw Exception("网络连接失败")
        }
        return listOf(
            User(1, "张三", "zhangsan@example.com"),
            User(2, "李四", "lisi@example.com"),
            User(3, "王五", "wangwu@example.com"),
        )
    }
}

// ViewModel：管理 UI 数据，生命周期感知
class UserListViewModel : ViewModel() {

    // StateFlow 方式（推荐）
    private val _uiState = MutableStateFlow<UserListUiState>(UserListUiState.Loading)
    val uiState: StateFlow<UserListUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        // viewModelScope 是 ViewModel 的协程作用域
        viewModelScope.launch {
            _uiState.value = UserListUiState.Loading
            try {
                val users = UserRepository.fetchUsers()
                _uiState.value = UserListUiState.Success(users)
            } catch (e: Exception) {
                _uiState.value = UserListUiState.Error(e.message ?: "未知错误")
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val users = UserRepository.fetchUsers()
                _uiState.update { UserListUiState.Success(users) }
            } catch (e: Exception) {
                _uiState.update { UserListUiState.Error(e.message ?: "刷新失败") }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    // 删除用户（乐观更新）
    fun deleteUser(userId: Int) {
        val currentState = _uiState.value
        if (currentState is UserListUiState.Success) {
            _uiState.value = currentState.copy(
                users = currentState.users.filter { it.id != userId }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        println("ViewModel 已清除，协程已自动取消")
    }
}

// ===== 在 Compose 中使用 ViewModel =====
/*
@Composable
fun UserListScreen(viewModel: UserListViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is UserListUiState.Loading -> {
            CircularProgressIndicator()
        }
        is UserListUiState.Success -> {
            LazyColumn {
                items(state.users, key = { it.id }) { user ->
                    Text("${user.name} - ${user.email}")
                }
            }
        }
        is UserListUiState.Error -> {
            Column {
                Text("出错了: ${state.message}")
                Button(onClick = { viewModel.loadUsers() }) {
                    Text("重试")
                }
            }
        }
    }
}
*/

fun main() {
    println("ViewModel 示例代码")
    println("注意：ViewModel 需要在 Android 环境中运行")
    println("核心概念：")
    println("1. ViewModel 生命周期比 Activity 长，屏幕旋转不丢失数据")
    println("2. viewModelScope 自动管理协程生命周期")
    println("3. StateFlow 替代 LiveData，配合 collectAsState 使用")
    println("4. 密封类表示 UI 状态，when 穷尽匹配所有状态")
}
```

**💡 代码解释**：ViewModel 由 Android 系统管理，生命周期比 Activity/Fragment 长，屏幕旋转时数据不丢失。viewModelScope 是 ViewModel 内置的协程作用域，ViewModel 销毁时自动取消所有协程。StateFlow 比 LiveData 更灵活，配合 collectAsStateWithLifecycle 在 Compose 中安全收集数据流。

**🔑 关键要点**：
- ViewModel 生命周期感知，屏幕旋转不丢失数据
- viewModelScope 自动管理协程，销毁时取消所有协程
- StateFlow 替代 LiveData，配合 collectAsState 在 Compose 中使用
- 密封类建模 UI 状态（Loading/Success/Error），when 穷尽匹配
- 乐观更新先更新 UI 再同步服务端，提升用户体验

---

### 8. Retrofit 网络请求

> **级别**：中级 | **概念**：Retrofit 是 Square 公司开发的类型安全的 HTTP 客户端，通过接口定义 API 端点。配合协程和 Gson/Moshi 实现声明式网络请求，是 Android 网络层的标准方案。

```kotlin
// ===== 中级：Retrofit 网络请求 =====
package com.example.myapp.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

// --- 数据模型：对应 JSON 响应结构 ---
data class ApiResponse<T>(
    val code: Int,                               // 响应状态码
    val message: String,                         // 响应消息
    val data: T?                                 // 泛型数据字段，可为 null
)

data class UserDto(
    val id: Long,                                // 用户 ID
    val username: String,                        // 用户名
    val email: String,                           // 邮箱
    val avatar: String? = null,                  // 头像 URL，可为 null
    val createdAt: String                        // 创建时间
)

data class LoginRequest(
    val username: String,                        // 用户名
    val password: String                         // 密码
)

data class LoginResponse(
    val token: String,                           // JWT Token
    val user: UserDto                            // 用户信息
)

// --- Retrofit API 接口定义 ---
interface ApiService {

    // GET 请求：获取用户列表
    @GET("api/users")
    suspend fun getUsers(
        @Query("page") page: Int = 1,           // @Query 添加 URL 查询参数
        @Query("limit") limit: Int = 20
    ): ApiResponse<List<UserDto>>

    // GET 请求：获取单个用户详情
    @GET("api/users/{id}")                       // {id} 是路径参数占位符
    suspend fun getUserById(
        @Path("id") userId: Long                // @Path 替换路径中的 {id}
    ): ApiResponse<UserDto>

    // POST 请求：用户登录
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest              // @Body 将对象序列化为 JSON 请求体
    ): ApiResponse<LoginResponse>

    // PUT 请求：更新用户信息
    @PUT("api/users/{id}")
    suspend fun updateUser(
        @Path("id") userId: Long,
        @Body user: UserDto,
        @Header("Authorization") token: String   // @Header 添加自定义请求头
    ): ApiResponse<UserDto>

    // DELETE 请求：删除用户
    @DELETE("api/users/{id}")
    suspend fun deleteUser(
        @Path("id") userId: Long
    ): ApiResponse<Unit>                         // Unit 表示无返回数据
}

// --- Retrofit 客户端配置 ---
object RetrofitClient {

    private const val BASE_URL = "https://api.example.com/"

    // 认证拦截器：自动添加 Token
    private val authInterceptor = okhttp3.Interceptor { chain ->
        val originalRequest = chain.request()
        val token = "从本地存储获取的 Token"
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/json")
            .build()
        chain.proceed(newRequest)
    }

    private val okHttpClient = okhttp3.OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .build()

    // Retrofit 实例（单例）
    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

// --- Repository 层：封装网络请求逻辑 ---
class UserRepository {

    private val api = RetrofitClient.instance

    suspend fun getUsers(page: Int = 1): Result<List<UserDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getUsers(page)
                if (response.code == 200) {
                    Result.success(response.data ?: emptyList())
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Result.failure(Exception("网络请求失败: ${e.message}"))
            }
        }
    }

    suspend fun login(username: String, password: String): Result<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.login(LoginRequest(username, password))
                if (response.code == 200 && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Result.failure(Exception("登录失败: ${e.message}"))
            }
        }
    }
}

fun main() {
    println("Retrofit 网络请求示例")
    println("核心组件：")
    println("1. ApiService 接口：定义 API 端点（@GET/@POST/@PUT/@DELETE）")
    println("2. RetrofitClient：配置 OkHttp 和 Gson 转换器")
    println("3. Repository：封装网络请求和错误处理")
    println("4. 配合协程实现异步请求，withContext(Dispatchers.IO) 切换线程")
}
```

**💡 代码解释**：Retrofit 通过注解定义 API 接口，@GET/@POST 标注 HTTP 方法，@Path/@Query/@Body 标注参数来源。GsonConverterFactory 自动将 JSON 转换为 Kotlin 数据类。OkHttp 拦截器统一处理认证、日志、重试等横切关注点。Repository 层封装网络逻辑，用 Kotlin Result 类型返回成功或失败。

**🔑 关键要点**：
- @GET/@POST/@PUT/@DELETE 注解定义 HTTP 方法
- @Path 替换路径参数，@Query 添加查询参数，@Body 发送请求体
- GsonConverterFactory 自动 JSON ↔ Kotlin 对象转换
- OkHttp 拦截器统一处理认证、日志、超时
- Repository 层封装网络逻辑，Result 类型处理成功/失败

---

## 高级精通

### 1. 协程深入：上下文与异常处理

> **级别**：高级 | **概念**：深入理解协程上下文（CoroutineContext）、Job 层级结构、异常传播机制和 CoroutineExceptionHandler。掌握结构化并发、协程作用域的自定义和超时处理。

```kotlin
// ===== 高级：协程深入 =====
import kotlinx.coroutines.*

// 自定义协程上下文元素
class LoggingContext(val tag: String) : AbstractCoroutineContextElement(LoggingContext) {
    companion object Key : CoroutineContext.Key<LoggingContext>
    fun log(message: String) {
        println("[$tag] $message")
    }
}

// CoroutineExceptionHandler：全局异常处理器
val exceptionHandler = CoroutineExceptionHandler { _, exception ->
    println("【全局异常捕获】${exception.javaClass.simpleName}: ${exception.message}")
}

// 自定义协程作用域
class AppCoroutineScope(
    private val onError: (Throwable) -> Unit = {}
) : CoroutineScope {
    private val job = SupervisorJob()            // 子协程失败不影响其他子协程
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Default + exceptionHandler

    fun launchSafely(block: suspend CoroutineScope.() -> Unit): Job {
        return launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e                            // 取消异常需要重新抛出
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun destroy() {
        job.cancel()
    }
}

// 模拟多级数据源
suspend fun fetchFromCache(): String {
    delay(100)
    throw RuntimeException("缓存读取失败")
}

suspend fun fetchFromNetwork(): String {
    delay(500)
    return "从网络获取的数据"
}

suspend fun fetchFromDatabase(): String {
    delay(300)
    return "从数据库获取的数据"
}

fun main() = runBlocking {
    println("=== 自定义协程上下文 ===\n")

    val scope = AppCoroutineScope { error ->
        println("外部错误回调: ${error.message}")
    }

    scope.launchSafely {
        println("协程正在执行...")
        delay(200)
        println("协程执行完成")
    }

    delay(300)

    println("\n=== 异常传播与 SupervisorJob ===\n")

    supervisorScope {
        launch {
            repeat(3) { i ->
                delay(100)
                println("协程1: 正常工作 $i")
            }
        }

        launch {
            delay(50)
            throw RuntimeException("协程2 发生异常")
            // 这个异常不会影响协程1，因为 supervisorScope 隔离异常
        }

        launch {
            repeat(3) { i ->
                delay(150)
                println("协程3: 不受影响 $i")
            }
        }
    }

    println("\n=== 多层降级策略 ===\n")

    val result = withContext(Dispatchers.Default) {
        try {
            fetchFromCache()                     // 第1级：尝试缓存
        } catch (e: Exception) {
            println("缓存失败: ${e.message}，尝试数据库...")
            try {
                fetchFromDatabase()              // 第2级：尝试数据库
            } catch (e2: Exception) {
                println("数据库失败: ${e2.message}，尝试网络...")
                fetchFromNetwork()               // 第3级：最终通过网络获取
            }
        }
    }
    println("最终数据: $result")

    println("\n=== 超时处理 ===\n")

    try {
        withTimeout(300) {                       // 300 毫秒超时
            delay(500)
        }
    } catch (e: TimeoutCancellationException) {
        println("操作超时: ${e.message}")
    }

    val timeoutResult = withTimeoutOrNull(300) {
        delay(200)
        "操作成功"
    }
    println("超时结果: $timeoutResult")            // 输出：操作成功

    val timeoutNull = withTimeoutOrNull(100) {
        delay(500)
    }
    println("超时返回 null: $timeoutNull")        // 输出：null

    scope.destroy()
    println("\n所有协程操作完成")
}
```

**💡 代码解释**：CoroutineContext 是协程的核心配置集合，包含 Job、Dispatcher 和自定义元素。SupervisorJob 隔离子协程异常，一个子协程失败不影响其他子协程。CoroutineExceptionHandler 仅在 launch 中捕获未处理异常，async 需 try-catch 配合 await。withTimeout 实现超时控制，结构化并发保证子协程随父协程取消。

**🔑 关键要点**：
- CoroutineContext = Job + Dispatcher + 自定义元素
- SupervisorJob 隔离子协程异常，普通 Job 异常会传播并取消兄弟协程
- CoroutineExceptionHandler 只在 launch 中生效，async 需 try-catch
- withTimeout 超时抛异常，withTimeoutOrNull 超时返回 null
- 结构化并发：父协程取消自动取消所有子协程

---

### 2. Compose 进阶：状态与动画

> **级别**：高级 | **概念**：深入 Compose 的状态管理（remember/mutableStateOf/derivedStateOf）、副作用处理（LaunchedEffect/DisposableEffect）和动画系统（animate*AsState/Transition）。

```kotlin
// ===== 高级：Compose 进阶 =====
package com.example.myapp.ui.advanced

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// --- 高级状态管理 ---
@Composable
fun AdvancedStateExample() {
    var count by remember { mutableStateOf(0) }

    // derivedStateOf：派生状态，仅在依赖变化时重新计算
    val isEven by remember {
        derivedStateOf { count % 2 == 0 }
    }

    // rememberSaveable：配置更改（如屏幕旋转）时保留状态
    var savedText by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "高级状态管理",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("计数: $count", fontSize = 20.sp)
        Text(
            "是偶数: $isEven",
            color = if (isEven) Color(0xFF4CAF50) else Color(0xFFFF5722)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { count++ }) { Text("+1") }
            Button(onClick = { count-- }) { Text("-1") }
            Button(onClick = { count = 0 }) { Text("重置") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = savedText,
            onValueChange = { savedText = it },
            label = { Text("旋转屏幕后保留的文本") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// --- 副作用处理 ---
@Composable
fun SideEffectExample() {
    var data by remember { mutableStateOf("加载中...") }

    // LaunchedEffect：在 Composable 进入组合时启动协程
    LaunchedEffect(key1 = Unit) {
        delay(2000)                              // 模拟加载
        data = "数据加载完成"
    }

    // DisposableEffect：在 Composable 离开组合时执行清理
    DisposableEffect(key1 = Unit) {
        println("进入组合：注册监听器")
        onDispose {
            println("离开组合：注销监听器")
        }
    }

    Text(
        text = data,
        modifier = Modifier.padding(16.dp),
        fontSize = 18.sp
    )
}

// --- 动画系统 ---
@Composable
fun AnimationExample() {
    var isExpanded by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }

    // animateFloatAsState：平滑过渡动画
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(
            durationMillis = 600,
            easing = FastOutSlowInEasing
        ),
        label = "rotation"
    )

    // animateDpAsState：尺寸动画（弹簧效果）
    val boxSize by animateDpAsState(
        targetValue = if (isExpanded) 200.dp else 60.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "size"
    )

    // animateColorAsState：颜色动画
    val bgColor by animateColorAsState(
        targetValue = if (isExpanded) Color(0xFF2196F3) else Color(0xFF4CAF50),
        animationSpec = tween(800),
        label = "color"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Compose 动画系统",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 带动画的可点击 Box
        Box(
            modifier = Modifier
                .size(boxSize)                     // 尺寸动画
                .graphicsLayer {
                    rotationZ = rotationAngle      // 旋转动画
                }
                .background(bgColor, CircleShape)  // 颜色动画
                .clickable { isExpanded = !isExpanded },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isExpanded) "收起" else "展开",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // AnimatedVisibility：显示/隐藏动画
        Button(onClick = { showContent = !showContent }) {
            Text(if (showContent) "隐藏内容" else "显示内容")
        }

        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(animationSpec = tween(500)) +
                    slideInVertically(
                        animationSpec = tween(500),
                        initialOffsetY = { it / 2 }
                    ) +
                    scaleIn(
                        animationSpec = tween(500),
                        initialScale = 0.5f,
                        transformOrigin = TransformOrigin(0.5f, 0.5f)
                    ),
            exit = fadeOut(animationSpec = tween(300)) +
                   slideOutVertically(
                       animationSpec = tween(300),
                       targetOffsetY = { it / 2 }
                   )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "这是一个带动画的内容区域\n支持多种动画效果组合",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAdvancedCompose() {
    MaterialTheme {
        Surface {
            AnimationExample()
        }
    }
}
```

**💡 代码解释**：derivedStateOf 创建派生状态避免不必要的重组。rememberSaveable 在配置更改时保留状态。LaunchedEffect 在组合时启动协程，DisposableEffect 在离开组合时清理资源。Compose 动画通过 animate*AsState 实现属性平滑过渡，AnimatedVisibility 控制显示/隐藏动画，支持多种动画效果组合。

**🔑 关键要点**：
- derivedStateOf 创建派生状态，仅在依赖变化时重新计算
- rememberSaveable 在配置更改时保留状态
- LaunchedEffect 在组合时启动协程，DisposableEffect 清理资源
- animate*AsState 实现属性动画，tween 和 spring 控制动画曲线
- AnimatedVisibility 实现显示/隐藏动画，支持 fadeIn/slideIn/scaleIn 组合

---

### 3. Hilt 依赖注入

> **级别**：高级 | **概念**：Hilt 是基于 Dagger 的 Android 专用依赖注入框架。通过 @HiltAndroidApp、@Module、@Inject 等注解实现自动依赖注入，简化对象创建和管理，提高代码可测试性。

```kotlin
// ===== 高级：Hilt 依赖注入 =====
package com.example.myapp.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

// ===== 第1步：Application 类添加 @HiltAndroidApp =====
@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}

// ===== 第2步：定义依赖接口和实现 =====

interface UserDataSource {
    suspend fun getUsers(): List<UserDto>
    suspend fun getUserById(id: Long): UserDto?
}

// @Inject constructor 告诉 Hilt 如何创建这个类的实例
class LocalUserDataSource @Inject constructor(
    private val database: AppDatabase           // Hilt 自动注入 AppDatabase
) : UserDataSource {

    override suspend fun getUsers(): List<UserDto> {
        return database.userDao().getAllUsers()
    }

    override suspend fun getUserById(id: Long): UserDto? {
        return database.userDao().getUserById(id)
    }
}

class RemoteUserDataSource @Inject constructor(
    private val apiService: ApiService          // Hilt 自动注入 ApiService
) : UserDataSource {

    override suspend fun getUsers(): List<UserDto> {
        val response = apiService.getUsers()
        return response.data ?: emptyList()
    }

    override suspend fun getUserById(id: Long): UserDto? {
        val response = apiService.getUserById(id)
        return response.data
    }
}

// ===== 第3步：创建 Module 提供第三方库依赖 =====

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.example.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): okhttp3.OkHttpClient {
        return okhttp3.OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }
}

// 数据库模块
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return androidx.room.Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }
}

// ===== 第4步：使用 @Binds 绑定接口到实现 =====

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserDataSource(
        remoteUserDataSource: RemoteUserDataSource
    ): UserDataSource
}

// ===== 第5步：在 ViewModel 中使用 @HiltViewModel 注入 =====

@dagger.hilt.android.lifecycle.HiltViewModel
class UserViewModel @Inject constructor(
    private val userDataSource: UserDataSource,
    private val apiService: ApiService
) : androidx.lifecycle.ViewModel() {

    private val _uiState = kotlinx.coroutines.flow.MutableStateFlow<UserListUiState>(
        UserListUiState.Loading
    )
    val uiState: kotlinx.coroutines.flow.StateFlow<UserListUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = UserListUiState.Loading
            try {
                val users = userDataSource.getUsers()
                _uiState.value = UserListUiState.Success(users)
            } catch (e: Exception) {
                _uiState.value = UserListUiState.Error(e.message ?: "未知错误")
            }
        }
    }
}

// ===== 第6步：在 Activity/Fragment 中注入 =====

@dagger.hilt.android.AndroidEntryPoint
class MainActivity : androidx.fragment.app.FragmentActivity() {

    @Inject
    lateinit var apiService: ApiService          // Hilt 自动注入

    private val viewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        // Hilt 在 super.onCreate() 之前完成注入
        setContent {
            // Compose UI...
        }
    }
}

// ===== 依赖关系图 =====
// MyApplication (@HiltAndroidApp)
//   └── SingletonComponent
//       ├── NetworkModule → Retrofit → ApiService
//       ├── DatabaseModule → AppDatabase → UserDao
//       └── RepositoryModule → UserDataSource (RemoteUserDataSource)
//           └── UserViewModel (@HiltViewModel)
//               └── MainActivity (@AndroidEntryPoint)

fun main() {
    println("Hilt 依赖注入示例")
    println("核心注解：")
    println("@HiltAndroidApp  → Application 入口")
    println("@AndroidEntryPoint → Activity/Fragment 注入点")
    println("@HiltViewModel   → ViewModel 注入")
    println("@Module + @InstallIn → 依赖提供模块")
    println("@Provides / @Binds → 提供依赖实例")
    println("@Singleton → 单例作用域")
    println("@Inject constructor → 标记构造函数注入")
}
```

**💡 代码解释**：Hilt 通过注解处理器生成依赖注入代码，编译期完成依赖图构建。@HiltAndroidApp 标记 Application，@AndroidEntryPoint 标记 Android 组件。@Module 提供第三方库依赖，@Binds 绑定接口到实现。@Inject constructor 标记构造函数注入，Hilt 自动解析参数依赖。作用域注解（@Singleton）控制实例生命周期。

**🔑 关键要点**：
- @HiltAndroidApp 标记 Application，@AndroidEntryPoint 标记 Activity/Fragment
- @Module + @InstallIn 定义依赖提供模块，@Provides/@Binds 提供实例
- @Inject constructor 标记构造函数注入，Hilt 自动解析依赖
- @Singleton/@ViewModelScoped 等作用域注解控制实例生命周期
- Hilt 编译期生成代码，不影响运行时性能

---

### 4. Android 性能优化

> **级别**：高级 | **概念**：Android 性能优化涵盖布局优化（减少层级）、内存管理（避免泄漏）、启动优化（延迟初始化）和打包优化（ProGuard/R8 混淆）。通过 Systrace/Profiler 工具定位性能瓶颈。

```kotlin
// ===== 高级：Android 性能优化 =====
package com.example.myapp.optimization

import android.app.Application
import android.os.Bundle
import android.os.Trace
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.lang.ref.WeakReference

// ===== 1. RecyclerView 优化 =====

// DiffUtil 回调：高效计算列表差异
class UserDiffCallback : DiffUtil.ItemCallback<UserDto>() {

    override fun areItemsTheSame(oldItem: UserDto, newItem: UserDto): Boolean {
        return oldItem.id == newItem.id           // 通过 ID 判断
    }

    override fun areContentsTheSame(oldItem: UserDto, newItem: UserDto): Boolean {
        return oldItem == newItem                 // 利用 data class 的 equals
    }

    // 返回变化的具体内容（用于局部刷新）
    override fun getChangePayload(oldItem: UserDto, newItem: UserDto): Any? {
        val diff = Bundle()
        if (oldItem.username != newItem.username) {
            diff.putString("username", newItem.username)
        }
        if (oldItem.email != newItem.email) {
            diff.putString("email", newItem.email)
        }
        return if (diff.isEmpty) null else diff
    }
}

// ListAdapter：内置 DiffUtil 的 RecyclerView Adapter
class UserAdapter(
    private val onItemClick: (UserDto) -> Unit
) : ListAdapter<UserDto, UserAdapter.ViewHolder>(UserDiffCallback()) {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: android.widget.TextView = itemView.findViewById(android.R.id.text1)
        val emailText: android.widget.TextView = itemView.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = getItem(position)
        holder.nameText.text = user.username
        holder.emailText.text = user.email
        holder.itemView.setOnClickListener { onItemClick(user) }
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            val bundle = payloads[0] as Bundle
            bundle.getString("username")?.let { holder.nameText.text = it }
            bundle.getString("email")?.let { holder.emailText.text = it }
        }
    }

    companion object {
        fun setupRecyclerView(recyclerView: RecyclerView) {
            recyclerView.apply {
                setHasFixedSize(true)              // 固定尺寸提升性能
                (layoutManager as? LinearLayoutManager)?.initialPrefetchItemCount = 4
                setRecycledViewPool(RecyclerView.RecycledViewPool())
            }
        }
    }
}

// ===== 2. 内存泄漏防范 =====

// 使用 WeakReference 避免 Handler/回调 导致的内存泄漏
class MyHandler(
    activity: AppCompatActivity,
    private val onMessage: (String) -> Unit
) {
    private val activityRef = WeakReference(activity)

    fun handleMessage(message: String) {
        val activity = activityRef.get()
        if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
            onMessage(message)
        }
    }
}

// ✅ 正确做法：使用 AndroidViewModel 或 ApplicationContext
class OptimizedViewModel(
    application: Application
) : androidx.lifecycle.AndroidViewModel(application) {

    fun doSomething() {
        val context = getApplication<Application>()
        // 用 Application Context 而非 Activity Context
    }
}

// ===== 3. 启动优化 =====

class OptimizedApplication : Application() {

    val database: AppDatabase by lazy {
        androidx.room.Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }

    override fun onCreate() {
        Trace.beginSection("AppOnCreate")
        super.onCreate()

        // 将非关键初始化放到后台线程
        Thread {
            initializeThirdPartySdks()
        }.start()

        Trace.endSection()
    }

    private fun initializeThirdPartySdks() {
        Trace.beginSection("InitThirdParty")
        // 初始化代码...
        Trace.endSection()
    }
}

// 启动 Activity 优化
class OptimizedMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(android.R.layout.activity_list_item)

        // 延迟加载非关键内容
        window.decorView.post {
            loadHeavyContent()
            loadAdvertisements()
        }
    }

    private fun loadHeavyContent() {
        // 加载大图、复杂数据等
    }

    private fun loadAdvertisements() {
        // 加载广告，非关键路径
    }
}

// ===== 4. ProGuard / R8 混淆配置 =====
/*
// build.gradle.kts
android {
    buildTypes {
        release {
            isMinifyEnabled = true               // 启用代码压缩和混淆
            isShrinkResources = true             // 启用资源压缩
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

// proguard-rules.pro 关键配置
// -keep class com.example.myapp.model.** { *; }  // 保留数据模型类
// -keepattributes Signature                       // 保留泛型签名
// -keepattributes *Annotation*                    // 保留注解
*/

// ===== 5. 性能监控工具 =====
object PerformanceMonitor {

    inline fun <T> traceMethod(tag: String, block: () -> T): T {
        Trace.beginSection(tag)
        val startTime = System.nanoTime()
        try {
            return block()
        } finally {
            val duration = (System.nanoTime() - startTime) / 1_000_000.0
            Log.d("Performance", "$tag 耗时: ${duration}ms")
            Trace.endSection()
        }
    }

    fun logStartupTime(application: Application) {
        val elapsed = System.currentTimeMillis() - android.os.Process.getStartElapsedRealtime()
        Log.d("Performance", "应用冷启动耗时: ${elapsed}ms")
    }
}

fun main() {
    println("Android 性能优化要点：")
    println("1. RecyclerView：使用 ListAdapter + DiffUtil + setHasFixedSize")
    println("2. 内存泄漏：WeakReference 持有 Activity，避免静态持有 Context")
    println("3. 启动优化：延迟初始化、首帧后加载非关键内容")
    println("4. ProGuard/R8：isMinifyEnabled + isShrinkResources")
    println("5. 监控工具：Systrace/Trace、Profiler、LeakCanary")
}
```

**💡 代码解释**：RecyclerView 优化核心是 ListAdapter + DiffUtil 自动计算差异、setHasFixedSize 固定尺寸、ViewHolder 复用。内存泄漏防范重点是用 WeakReference 持有 Activity 引用、ViewModel 不持有 View/Context。启动优化通过延迟初始化、首帧后加载非关键内容。ProGuard/R8 通过代码压缩、混淆和资源优化减少 APK 体积。

**🔑 关键要点**：
- ListAdapter + DiffUtil 自动计算差异，只更新变化项
- WeakReference 避免 Activity 内存泄漏，ViewModel 不持有 View
- 启动优化：延迟初始化 + 首帧后加载非关键内容
- ProGuard/R8：isMinifyEnabled + isShrinkResources 减少 APK 体积
- 使用 Systrace/Profiler/LeakCanary 工具监控性能

---

### 5. Room 数据库

> **级别**：高级 | **概念**：Room 是 Android 官方 ORM 数据库框架，基于 SQLite 提供类型安全的 API。通过 @Entity、@Dao、@Database 注解定义数据库结构，支持 Flow 响应式查询、Migration 版本迁移和事务操作。

```kotlin
// ===== 高级：Room 数据库 =====
package com.example.myapp.data

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

// ===== 1. Entity：定义数据库表 =====

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true), // email 唯一索引
        Index(value = ["username"])              // username 普通索引
    ]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)             // 主键，自动生成
    val id: Long = 0,

    @ColumnInfo(name = "username")               // 自定义列名
    val username: String,

    val email: String,

    @ColumnInfo(name = "avatar_url")
    val avatarUrl: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true
)

// 关联表：用户与订单的多对多关系
@Entity(
    tableName = "user_order_cross_ref",
    primaryKeys = ["userId", "orderId"],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE        // 用户删除时级联删除关联
        )
    ],
    indices = [Index(value = ["orderId"])]
)
data class UserOrderCrossRef(
    val userId: Long,
    val orderId: Long
)

// 查询结果映射：自定义多表查询结果
data class UserWithOrders(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "userId"
    )
    val orders: List<OrderEntity>
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val amount: Double,
    val status: String
)

// ===== 2. DAO：数据访问对象 =====

@Dao
interface UserDao {

    // 返回 Flow 实现响应式查询，数据库变化时自动发射新数据
    @Query("SELECT * FROM users ORDER BY created_at DESC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    // 普通挂起查询（一次性）
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Long): UserEntity?

    // 条件查询
    @Query("SELECT * FROM users WHERE username LIKE :query OR email LIKE :query")
    suspend fun searchUsers(query: String): List<UserEntity>

    // 多表关联查询
    @Transaction
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserWithOrders(userId: Long): UserWithOrders?

    // 返回 LiveData（传统方式）
    @Query("SELECT * FROM users")
    fun getAllUsersLiveData(): androidx.lifecycle.LiveData<List<UserEntity>>

    // --- 插入操作 ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long  // 返回插入行的 ID

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUsers(users: List<UserEntity>): List<Long>

    // --- 更新操作 ---
    @Update
    suspend fun updateUser(user: UserEntity): Int

    @Query("UPDATE users SET avatar_url = :avatarUrl WHERE id = :userId")
    suspend fun updateAvatar(userId: Long, avatarUrl: String): Int

    // --- 删除操作 ---
    @Delete
    suspend fun deleteUser(user: UserEntity): Int

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: Long): Int

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers(): Int

    // --- 事务操作 ---
    @Transaction
    suspend fun insertOrUpdate(user: UserEntity): Long {
        val existing = getUserById(user.id)
        return if (existing != null) {
            updateUser(user)
            user.id
        } else {
            insertUser(user)
        }
    }

    @Transaction
    suspend fun batchInsertWithCleanup(users: List<UserEntity>) {
        deleteAllUsers()
        insertUsers(users)
        // 两个操作在同一事务中，要么全部成功，要么全部回滚
    }

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Query("SELECT * FROM users WHERE is_active = 1")
    suspend fun getActiveUsers(): List<UserEntity>
}

// ===== 3. Database：数据库定义 =====

@Database(
    entities = [UserEntity::class, OrderEntity::class, UserOrderCrossRef::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    companion object {
        // 数据库迁移：版本 1 → 版本 2
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE users ADD COLUMN avatar_url TEXT"
                )
                database.execSQL(
                    "ALTER TABLE users ADD COLUMN is_active INTEGER NOT NULL DEFAULT 1"
                )
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

// 类型转换器
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): java.util.Date? {
        return value?.let { java.util.Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: java.util.Date?): Long? {
        return date?.time
    }
}

fun main() {
    println("Room 数据库要点：")
    println("1. @Entity 定义表，支持索引、外键、联合主键")
    println("2. @Dao 定义数据访问操作，支持 Flow 响应式查询")
    println("3. @Database 定义数据库，@TypeConverters 处理类型转换")
    println("4. Migration 定义版本迁移，fallbackToDestructiveMigration 兜底")
    println("5. @Transaction 保证事务原子性，@Relation 定义关联查询")
}
```

**💡 代码解释**：Room 通过 @Entity 定义表结构，@Dao 定义增删改查操作，@Database 定义数据库实例。Flow 返回类型实现响应式查询，数据库变化自动更新 UI。Migration 类处理版本升级，防止用户数据丢失。@Transaction 保证多步操作原子性。@Relation 和 @Embedded 简化多表关联查询。

**🔑 关键要点**：
- @Entity 定义表，支持 indices、foreignKeys、primaryKeys
- @Dao 接口定义 CRUD，Flow 返回实现响应式查询
- @Database 定义数据库版本，@TypeConverters 处理类型转换
- Migration 安全升级数据库，fallbackToDestructiveMigration 兜底
- @Transaction 保证事务原子性，@Relation 定义关联查询

---

### 6. 多模块架构与组件化

> **级别**：高级 | **概念**：多模块架构将大型项目按功能拆分为独立 Gradle 模块，实现代码隔离、并行编译和团队协作。组件化通过路由实现模块间通信，Navigation 组件管理页面导航。

```kotlin
// ===== 高级：多模块架构与组件化 =====
// 注意：以下为项目架构设计说明和关键代码示例

// ===== 1. 模块化项目结构 =====
/*
项目根目录：
├── app/                          ← 主模块（壳工程），负责组装所有模块
│   └── build.gradle.kts
├── core/                         ← 核心模块层
│   ├── core-base/                ← 基础工具类、扩展函数
│   ├── core-network/             ← 网络层（Retrofit + OkHttp）
│   ├── core-database/            ← 数据库层（Room）
│   └── core-ui/                  ← 公共 UI 组件
├── feature/                      ← 业务模块层
│   ├── feature-login/            ← 登录模块
│   ├── feature-home/             ← 首页模块
│   ├── feature-profile/          ← 个人中心模块
│   └── feature-settings/         ← 设置模块
├── lib/                          ← 公共库层
│   └── lib-navigation/           ← 导航路由库
├── build.gradle.kts              ← 根构建文件
└── settings.gradle.kts           ← 模块声明文件
*/

// ===== 2. settings.gradle.kts：声明所有模块 =====
/*
rootProject.name = "MyApp"
include(":app")
include(":core:core-base")
include(":core:core-network")
include(":core:core-database")
include(":core:core-ui")
include(":feature:feature-login")
include(":feature:feature-home")
include(":feature:feature-profile")
include(":lib:lib-navigation")
*/

// ===== 3. 模块间依赖管理（build.gradle.kts）=====
/*
// core-base/build.gradle.kts：基础模块不依赖任何业务模块
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}
android { ... }
dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
}

// feature-login/build.gradle.kts：业务模块依赖核心模块
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}
android { ... }
dependencies {
    implementation(project(":core:core-base"))
    implementation(project(":core:core-network"))
    implementation(project(":lib:lib-navigation"))
    // 业务模块之间通过路由通信，不直接依赖
}

// app/build.gradle.kts：壳工程依赖所有模块
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}
android { ... }
dependencies {
    implementation(project(":core:core-base"))
    implementation(project(":feature:feature-login"))
    implementation(project(":feature:feature-home"))
    implementation(project(":feature:feature-profile"))
}
*/

// ===== 4. 导航路由：模块间通信 =====
package com.example.myapp.navigation

// 定义路由常量（放在 lib-navigation 模块中，所有模块可访问）
object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val PROFILE = "profile/{userId}"       // 带参数的路由
    const val SETTINGS = "settings"

    fun profile(userId: Long) = "profile/$userId"
}

// Navigation Compose 导航图（在 app 模块的 MainActivity 中配置）
/*
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyAppTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = Routes.HOME
                ) {
                    composable(Routes.HOME) {
                        HomeScreen(
                            onNavigateToProfile = { userId ->
                                navController.navigate(Routes.profile(userId))
                            }
                        )
                    }
                    composable(
                        route = Routes.PROFILE,
                        arguments = listOf(
                            navArgument("userId") { type = NavType.LongType }
                        )
                    ) { backStackEntry ->
                        val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
                        ProfileScreen(userId = userId)
                    }
                }
            }
        }
    }
}
*/

// ===== 5. 模块间通信：接口 + 实现解耦 =====
// 在 core-base 模块定义接口
interface LoginManager {
    fun isLoggedIn(): Boolean
    fun getCurrentUserId(): Long?
}

// 在 feature-login 模块实现接口
class LoginManagerImpl @Inject constructor(
    private val sharedPreferences: android.content.SharedPreferences
) : LoginManager {
    override fun isLoggedIn(): Boolean {
        return sharedPreferences.getString("token", null) != null
    }
    override fun getCurrentUserId(): Long? {
        return sharedPreferences.getLong("userId", -1).takeIf { it != -1L }
    }
}

// 在 feature-login 模块的 Hilt Module 中绑定
@Module
@InstallIn(SingletonComponent::class)
abstract class LoginModule {
    @Binds
    @Singleton
    abstract fun bindLoginManager(
        impl: LoginManagerImpl
    ): LoginManager
}

// 其他模块通过依赖注入使用接口
class HomeViewModel @Inject constructor(
    private val loginManager: LoginManager       // 注入接口，不依赖具体实现
) : ViewModel() {
    fun checkLogin() {
        if (!loginManager.isLoggedIn()) {
            // 未登录，触发登录流程
        }
    }
}

fun main() {
    println("多模块架构要点：")
    println("1. 分层设计：app(壳) → feature(业务) → core(核心) → lib(公共库)")
    println("2. 模块间依赖单向：上层依赖下层，同层通过路由通信")
    println("3. Navigation Compose 管理页面导航，路由常量统一管理")
    println("4. 接口 + Hilt 绑定实现模块解耦，依赖倒置原则")
    println("5. 并行编译提升构建速度，模块独立开发测试")
}
```

**💡 代码解释**：多模块架构按功能分层：app 壳工程负责组装，feature 业务模块通过路由通信，core 核心模块提供基础能力，lib 公共库提供通用功能。Hilt 通过接口绑定实现模块间解耦，Navigation Compose 管理页面导航。模块化带来并行编译、代码隔离和团队协作优势。

**🔑 关键要点**：
- 分层设计：app(壳) → feature(业务) → core(核心) → lib(公共库)
- 模块间依赖单向，同层模块通过路由通信而非直接依赖
- Navigation Compose 统一管理页面导航，路由常量集中定义
- 接口 + Hilt @Binds 实现模块解耦，遵循依赖倒置原则
- 并行编译提升构建速度，各模块可独立开发测试

---
