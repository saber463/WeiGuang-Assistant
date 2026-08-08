# 🔹 Go 编程语言学习手册

> **分类**：后端  
> **描述**：Go语言后端开发知识库，涵盖从入门到高级的完整学习路径  
> **生成时间**：2026-08-07  

---

## 目录

- [初级入门](#primary)（8个知识点）
- [中级进阶](#intermediate)（8个知识点）
- [高级精通](#advanced)（6个知识点）

---

## 初级入门

### 1. 环境搭建与Go Module

> **级别**：初级 | **概念**：安装Go SDK并配置GOPATH/GOROOT，使用go mod管理项目依赖，理解Go项目的标准目录结构。

```go
// ============================================
// 【初级】Go 环境搭建与 Go Module 包管理
// ============================================

// 1. 下载安装Go SDK
//    官网: https://go.dev/dl/
//    Windows: 下载.msi安装包，自动配置环境变量
//    Mac: brew install go
//    Linux: 下载tar.gz解压到 /usr/local/go

// 2. 验证安装
//    go version
//    输出：go version go1.21.0 windows/amd64

// 3. 环境变量说明
//    GOROOT: Go安装目录（如 C:\Program Files\Go）
//    GOPATH: 工作空间目录（Go 1.11+ 已弱化，但仍需设置）
//    GOBIN: 编译后的可执行文件输出目录

// 4. 创建Go Module项目
//    mkdir myproject && cd myproject
//    go mod init github.com/username/myproject
//    ↑ go mod init：初始化Go模块
//    ↑ 模块路径：通常为仓库地址，用于唯一标识

// 5. 添加依赖
//    go get github.com/gin-gonic/gin
//    ↑ go get：下载并添加依赖
//    ↑ 依赖记录在 go.mod 文件中

// 6. 整理依赖
//    go mod tidy
//    ↑ 自动添加缺失的依赖，移除未使用的依赖

package main  // package main：声明为可执行程序入口包

import (
    "fmt"  // 格式化输入输出
    "runtime"  // 运行时信息
)

func main() {  // main函数：程序入口
    // 打印Go版本信息
    fmt.Println("Go版本:", runtime.Version())
    // ↑ runtime.Version()：获取当前Go运行时版本

    fmt.Println("操作系统:", runtime.GOOS)
    // ↑ runtime.GOOS：当前操作系统（windows/linux/darwin）

    fmt.Println("CPU核心数:", runtime.NumCPU())
    // ↑ runtime.NumCPU()：可用的CPU逻辑核心数

    fmt.Println("\n项目结构说明:")
    fmt.Println("go.mod      - 模块定义文件（依赖管理）")
    fmt.Println("go.sum      - 依赖校验文件（自动生成）")
    fmt.Println("main.go     - 程序入口文件")
    fmt.Println("cmd/        - 多入口程序目录")
    fmt.Println("internal/   - 私有包（不可被外部导入）")
    fmt.Println("pkg/        - 可导出的公共库")
}

// 编译运行:
// go run main.go       → 编译并运行
// go build             → 编译为可执行文件
// go build -o app.exe  → 指定输出文件名
```

**💡 代码解释**：Go Module是Go 1.11引入的官方依赖管理方案，go.mod文件定义了模块路径和依赖版本。GOROOT指向Go安装目录，GOPATH是工作空间（Go Modules时代作用减弱）。go mod tidy是最常用的命令，自动维护依赖列表。Go项目推荐遵循标准目录结构。编译后生成单一可执行文件，无需运行时依赖。

**🔑 关键要点**：
- go mod init初始化模块，go.mod管理依赖
- go get下载依赖，go mod tidy整理依赖
- GOROOT是安装目录，GOPATH是工作空间
- go run编译运行，go build编译为可执行文件
- package main + func main() 是程序入口

---

### 2. 变量声明与基本类型

> **级别**：初级 | **概念**：Go支持var和:=两种变量声明方式，基本类型包括int/float/string/bool，类型推导简化代码。

```go
// ============================================
// 【初级】Go 变量声明与基本类型
// ============================================

package main

import "fmt"

func main() {
    // --- 变量声明的四种方式 ---

    // 方式1：var声明 + 显式类型
    var name string = "Go语言"  // var：变量声明关键字
    fmt.Println("name:", name)

    // 方式2：var声明 + 类型推导
    var age = 15  // 自动推断为int类型
    fmt.Println("age:", age)

    // 方式3：短变量声明 :=（最常用，仅函数内可用）
    version := 1.21  // := 自动推断类型为float64
    fmt.Println("version:", version)

    // 方式4：批量声明
    var (
        x int    = 10  // 显式指定类型
        y float64 = 3.14  // 64位浮点数
        z string  = "hello"
        isActive bool = true  // 布尔类型
    )
    fmt.Printf("x=%d, y=%.2f, z=%s, isActive=%t\n", x, y, z, isActive)
    // ↑ Printf：格式化输出
    // ↑ %d整数, %f浮点, %s字符串, %t布尔

    // --- 基本类型详解 ---
    // 整数类型（有符号）
    var i8 int8 = 127       // int8：-128~127
    var i16 int16 = 32767   // int16：-32768~32767
    var i32 int32 = 2147483647  // int32：约±21亿
    var i64 int64 = 9223372036854775807  // int64

    // int：根据平台自动选择32或64位
    var i int = 100
    fmt.Printf("int占用: %d字节\n", 8)  // 64位系统上int为8字节

    // 无符号整数
    var ui uint = 100  // uint：无符号整数（非负）
    fmt.Println("uint:", ui)

    // 浮点数
    var f32 float32 = 3.1415926  // float32：约6位精度
    var f64 float64 = 3.141592653589793  // float64：约15位精度（默认）
    fmt.Printf("float32: %.7f, float64: %.15f\n", f32, f64)

    // 字符串（UTF-8编码，原生支持中文）
    s1 := "Hello, 世界"  // 双引号：可含转义字符
    s2 := `多行
原始字符串
不转义\n`  // 反引号：原始字符串，不转义
    fmt.Println(s1)
    fmt.Print(s2)

    // 零值：未初始化的变量有默认零值
    var defaultInt int     // 0
    var defaultStr string  // ""（空字符串）
    var defaultBool bool   // false
    fmt.Printf("\n零值: int=%d, str='%s', bool=%t\n",
        defaultInt, defaultStr, defaultBool)
}
```

**💡 代码解释**：Go是静态类型语言，但支持类型推导简化代码。:=是Go特有的短变量声明语法，只能在函数内使用，是官方推荐的声明方式。Go的零值机制确保变量始终有确定值（数字0、字符串""、布尔false、指针nil），避免未初始化问题。Go的int大小随平台变化（32位系统4字节，64位系统8字节）。

**🔑 关键要点**：
- var声明变量，:=短声明（仅函数内可用）
- Go是静态类型，支持类型推导
- 零值机制：所有变量都有默认值
- int大小随平台（32位4字节，64位8字节）
- float64是默认浮点类型，string是UTF-8编码

---

### 3. 数组与切片

> **级别**：初级 | **概念**：数组是固定长度同类型序列，切片是动态数组的引用视图，是Go中最常用的数据结构。

```go
// ============================================
// 【初级】Go 数组与切片（Slice）
// ============================================

package main

import "fmt"

func main() {
    // --- 数组：固定长度，值类型 ---
    var arr1 [5]int  // 声明长度为5的int数组，零值填充
    fmt.Println("零值数组:", arr1)

    arr2 := [3]string{"Go", "Java", "Python"}  // 声明并初始化
    fmt.Println("初始化数组:", arr2)

    arr3 := [...]int{1, 2, 3, 4, 5}  // ... 自动推断长度
    fmt.Println("自动长度数组:", arr3)

    fmt.Println("arr2长度:", len(arr2))  // len()：获取数组长度
    fmt.Println("arr2[0]:", arr2[0])     // 索引访问

    // 数组是值类型：赋值会复制整个数组
    arr4 := arr3  // 完整复制，arr4和arr3独立
    arr4[0] = 100
    fmt.Printf("arr3[0]=%d, arr4[0]=%d (值拷贝，互不影响)\n", arr3[0], arr4[0])

    // --- 切片（Slice）：动态数组，引用类型 ---
    // 创建切片的三种方式

    // 方式1：从数组创建切片
    slice1 := arr3[1:4]  // [1:4]：从索引1到3（不含4）
    fmt.Println("\n从数组切片:", slice1)

    // 方式2：字面量创建
    slice2 := []int{10, 20, 30, 40, 50}
    fmt.Println("字面量切片:", slice2)

    // 方式3：make函数创建
    slice3 := make([]int, 3, 5)
    // ↑ make(类型, 长度, 容量)
    // ↑ 长度3：当前可访问的元素数
    // ↑ 容量5：底层数组的大小
    fmt.Printf("make切片: len=%d, cap=%d, %v\n",
        len(slice3), cap(slice3), slice3)

    // --- 切片动态操作 ---
    // append：追加元素
    slice2 = append(slice2, 60, 70)
    // ↑ append()：向切片追加元素，返回新切片
    fmt.Println("\nappend后:", slice2)

    // 合并切片
    slice2 = append(slice2, slice1...)  // ... 展开切片
    fmt.Println("合并后:", slice2)

    // copy：复制切片
    dest := make([]int, 3)
    copied := copy(dest, slice2)
    // ↑ copy(目标, 源)：返回复制的元素数
    fmt.Printf("复制了%d个元素: %v\n", copied, dest)

    // 切片遍历
    fmt.Println("\n遍历切片:")
    for i, v := range slice2 {  // range：遍历，i是索引，v是值
        fmt.Printf("  [%d] = %d\n", i, v)
    }
}
```

**💡 代码解释**：数组和切片是Go中最基本的数据结构。关键区别：(1)数组长度固定，是值类型，赋值会复制整个数组；(2)切片是动态的，是引用类型，底层指向数组。切片有三个属性：指针(指向底层数组)、长度(len)、容量(cap)。append自动扩容（容量不足时分配新数组）。make()预分配容量可减少扩容次数。...展开切片用于append合并。

**🔑 关键要点**：
- 数组长度固定，值类型，赋值会复制
- 切片动态可变，引用类型，底层指向数组
- 切片三属性：指针、长度(len)、容量(cap)
- append()追加元素，自动扩容
- make()预分配容量，copy()复制切片

---

### 4. 映射（Map）

> **级别**：初级 | **概念**：Map是Go的键值对集合，基于哈希表实现，支持快速查找、添加和删除操作。

```go
// ============================================
// 【初级】Go 映射（Map）操作
// ============================================

package main

import "fmt"

func main() {
    // --- 创建Map的三种方式 ---

    // 方式1：make函数创建
    scores := make(map[string]int)
    // ↑ map[键类型]值类型
    // ↑ make()：初始化map，分配内存

    // 方式2：字面量初始化
    colors := map[string]string{
        "red":   "#FF0000",
        "green": "#00FF00",
        "blue":  "#0000FF",
        // 注意：最后一个元素后的逗号不能省略
    }
    fmt.Println("颜色映射:", colors)

    // 方式3：声明后初始化（nil map不能直接赋值）
    var emptyMap map[string]int  // nil map
    // emptyMap["key"] = 1  // 报错！nil map不能赋值
    fmt.Println("nil map:", emptyMap)

    // --- Map基本操作 ---
    // 添加/更新元素
    scores["张三"] = 85  // 添加新键值对
    scores["李四"] = 92
    scores["王五"] = 78
    scores["张三"] = 90  // 更新已有键的值

    fmt.Println("\n成绩表:", scores)

    // 获取元素
    fmt.Println("张三的成绩:", scores["张三"])

    // 安全获取：检查键是否存在
    value, exists := scores["赵六"]
    // ↑ Go特色的"comma ok"模式
    // ↑ exists为true表示键存在，false表示不存在
    if exists {
        fmt.Println("赵六的成绩:", value)
    } else {
        fmt.Println("赵六不在成绩表中")
    }

    // 删除元素
    delete(scores, "王五")
    // ↑ delete(映射, 键)：删除键值对
    fmt.Println("删除王五后:", scores)

    // --- Map遍历 ---
    fmt.Println("\n遍历成绩表:")
    for name, score := range scores {
        // ↑ range遍历map：返回键和值
        // 注意：遍历顺序是随机的！
        fmt.Printf("  %s: %d分\n", name, score)
    }

    // 获取Map长度
    fmt.Println("\n元素数量:", len(scores))

    // --- Map作为集合使用 ---
    // Go没有内置Set，用map[T]bool模拟
    set := make(map[string]bool)
    set["Go"] = true
    set["Java"] = true
    set["Python"] = true
    set["Go"] = true  // 重复添加，不会报错
    fmt.Println("\n集合模拟:", set)

    // 检查元素是否存在
    if set["Go"] {
        fmt.Println("Go在集合中")
    }

    // 删除元素
    delete(set, "Java")
    fmt.Println("删除Java后:", set)
}
```

**💡 代码解释**：Go的Map基于哈希表，查找O(1)，是后端开发最常用的数据结构之一。重要特性：(1)nil map不能直接赋值，需用make初始化；(2)使用"comma ok"模式(value, ok := map[key])安全检查键是否存在；(3)遍历顺序随机，不保证有序；(4)Map不是线程安全的，并发读写需要加锁或使用sync.Map。Go没有内置Set，习惯用map[T]bool或map[T]struct{}模拟。

**🔑 关键要点**：
- make(map[K]V)初始化map，nil map不能赋值
- value, ok := map[key]安全检查键是否存在
- delete(map, key)删除键值对
- range遍历顺序随机，不保证有序
- Map不是线程安全的，并发需加锁

---

### 5. 控制流程（if/switch/for）

> **级别**：初级 | **概念**：Go的if支持初始化语句，switch默认不穿透，for是唯一的循环关键字，支持多种循环形式。

```go
// ============================================
// 【初级】Go 控制流程：if/switch/for
// ============================================

package main

import "fmt"

func main() {
    // --- if 条件判断（支持初始化语句） ---
    score := 85

    if score >= 90 {  // if：条件不需要括号
        fmt.Println("等级: 优秀")
    } else if score >= 80 {  // else if：必须紧接上一个}之后
        fmt.Println("等级: 良好")
    } else if score >= 60 {
        fmt.Println("等级: 及格")
    } else {
        fmt.Println("等级: 不及格")
    }

    // if支持初始化语句（Go特色）
    if grade := getGrade(score); grade == "良好" {
        // ↑ grade := getGrade(score)：初始化语句，仅在if作用域内可见
        fmt.Println("恭喜获得良好等级！")
    }
    // fmt.Println(grade)  // 编译错误：grade在if外部不可见

    // --- switch 多分支选择 ---
    day := 3
    switch day {  // switch：默认不穿透（无需break）
    case 1:
        fmt.Println("周一：开始新的一周")
    case 2:
        fmt.Println("周二：继续加油")
    case 3:
        fmt.Println("周三：一周过半")
    case 6, 7:  // 多个值用逗号分隔
        fmt.Println("周末：好好休息")
    default:
        fmt.Println("工作日")
    }

    // switch 无表达式（替代if-else链）
    switch {  // 无表达式：每个case是一个条件
    case score >= 90:
        fmt.Println("switch方式: 优秀")
    case score >= 60:
        fmt.Println("switch方式: 及格")
    default:
        fmt.Println("switch方式: 不及格")
    }

    // switch 类型断言
    var x interface{} = "hello"
    switch v := x.(type) {  // .(type) 只能在switch中使用
    case int:
        fmt.Println("整数:", v)
    case string:
        fmt.Println("字符串:", v)
    default:
        fmt.Println("未知类型")
    }

    // --- for 循环（Go唯一的循环关键字） ---
    // 形式1：传统for循环
    fmt.Println("\n传统for循环:")
    for i := 1; i <= 5; i++ {  // for 初始化; 条件; 迭代
        fmt.Print(i, " ")
    }
    fmt.Println()

    // 形式2：while风格（条件循环）
    fmt.Println("while风格:")
    count := 0
    for count < 3 {  // 只有条件，类似while
        fmt.Print(count, " ")
        count++
    }
    fmt.Println()

    // 形式3：无限循环
    // for { ... }  // 等价于 while(true)

    // 形式4：range遍历
    fruits := []string{"苹果", "香蕉", "橘子"}
    fmt.Println("range遍历:")
    for i, fruit := range fruits {
        fmt.Printf("  [%d] %s\n", i, fruit)
    }

    // break 与 continue
    fmt.Println("break: 找到第一个偶数")
    numbers := []int{1, 3, 5, 6, 7, 9}
    for _, n := range numbers {
        if n%2 == 0 {
            fmt.Println("  找到:", n)
            break  // 终止循环
        }
    }
}
```

**💡 代码解释**：Go的控制流程简洁而强大。(1)if的特殊之处在于支持初始化语句，变量作用域限定在if块内；(2)switch默认不穿透(no fallthrough)，无需break，case支持多个值；(3)for是唯一的循环关键字，通过不同形式实现while和无限循环。range遍历数组/切片/Map/字符串/通道。Go的switch还支持类型断言，用于接口类型判断。

**🔑 关键要点**：
- if支持初始化语句，变量作用域仅在if块内
- switch默认不穿透，无需break
- switch无表达式时case是条件判断
- for是唯一循环关键字，可替代while
- range遍历切片/Map/字符串/通道

---

### 6. 函数定义与多返回值

> **级别**：初级 | **概念**：Go函数用func关键字定义，支持多返回值和命名返回值，是Go语言的核心设计哲学。

```go
// ============================================
// 【初级】Go 函数定义与多返回值
// ============================================

package main

import (
    "errors"  // 错误处理
    "fmt"
)

// --- 基本函数定义 ---
func greet(name string) string {
    // ↑ func：函数声明关键字
    // ↑ (name string)：参数列表，参数名在前，类型在后
    // ↑ string：返回值类型
    return "你好，" + name + "！欢迎学习Go！"
}

// --- 多返回值（Go核心特性） ---
func divide(a, b float64) (float64, error) {
    // ↑ (float64, error)：返回两个值
    // ↑ 第二个返回值通常是error，用于错误处理
    if b == 0 {
        return 0, errors.New("除数不能为零")
        // ↑ errors.New()：创建错误信息
    }
    return a / b, nil  // nil：表示无错误
}

// --- 命名返回值 ---
func calculateStats(numbers []int) (sum int, avg float64) {
    // ↑ (sum int, avg float64)：命名返回值
    // ↑ 命名返回值自动初始化为零值，可直接使用
    for _, n := range numbers {
        sum += n  // sum已在返回值声明处定义
    }
    if len(numbers) > 0 {
        avg = float64(sum) / float64(len(numbers))
        // ↑ avg也是命名返回值
    }
    return  // 裸return：自动返回命名返回值
}

// --- 可变参数 ---
func sumAll(nums ...int) int {
    // ↑ ...int：可变参数，nums是[]int切片
    total := 0
    for _, n := range nums {
        total += n
    }
    return total
}

// --- 函数作为参数 ---
func applyOperation(a, b int, op func(int, int) int) int {
    // ↑ op func(int, int) int：函数类型参数
    return op(a, b)
}

func main() {
    // 调用基本函数
    fmt.Println(greet("小明"))

    // 调用多返回值函数
    result, err := divide(10, 3)
    // ↑ 接收两个返回值
    if err != nil {
        fmt.Println("错误:", err)
    } else {
        fmt.Printf("10 / 3 = %.2f\n", result)
    }

    // 忽略返回值（用_）
    result2, _ := divide(10, 2)
    // ↑ _：空白标识符，忽略不需要的返回值
    fmt.Println("10 / 2 =", result2)

    // 除零错误处理
    _, err = divide(10, 0)
    if err != nil {
        fmt.Println("除零错误:", err)
    }

    // 命名返回值
    sum, avg := calculateStats([]int{85, 92, 78, 95, 88})
    fmt.Printf("\n总和: %d, 平均: %.2f\n", sum, avg)

    // 可变参数
    fmt.Println("可变参数求和:", sumAll(1, 2, 3, 4, 5))

    // 函数作为参数
    multiply := func(a, b int) int { return a * b }
    // ↑ 匿名函数（闭包）
    fmt.Println("函数参数:", applyOperation(5, 3, multiply))

    // 匿名函数直接调用
    result3 := func(x, y int) int {
        return x + y
    }(10, 20)  // 定义后立即调用
    fmt.Println("匿名函数直接调用:", result3)
}
```

**💡 代码解释**：Go函数的核心特性：(1)多返回值——通常返回(result, error)，error是Go错误处理的标准模式；(2)命名返回值——在函数签名中命名返回值，裸return自动返回这些值；(3)函数是一等公民——可作为参数传递、赋值给变量、作为返回值。Go没有try-catch，错误通过返回值传递，这是Go的设计哲学：显式错误处理。

**🔑 关键要点**：
- func关键字定义函数，参数名在前类型在后
- 多返回值通常为(result, error)模式
- 命名返回值可用裸return自动返回
- 可变参数...int本质是切片
- 函数是一等公民，可作参数、返回值

---

### 7. 指针与结构体

> **级别**：初级 | **概念**：Go指针存储内存地址但不支持运算，结构体是字段集合，可关联方法实现面向对象风格编程。

```go
// ============================================
// 【初级】Go 指针与结构体
// ============================================

package main

import "fmt"

// --- 结构体定义 ---
type Student struct {
    // ↑ type：定义新类型
    // ↑ struct：结构体关键字
    Name   string  // 首字母大写：公开字段（可导出）
    Age    int
    Scores []int   // 切片字段
    email  string  // 首字母小写：私有字段（不可导出）
}

// --- 结构体方法（值接收者） ---
func (s Student) GetInfo() string {
    // ↑ (s Student)：值接收者，方法属于Student类型
    // ↑ 值接收者：方法内修改不会影响原结构体
    return fmt.Sprintf("%s, %d岁", s.Name, s.Age)
}

// --- 结构体方法（指针接收者） ---
func (s *Student) SetEmail(email string) {
    // ↑ (s *Student)：指针接收者，可修改原结构体
    s.email = email
}

func (s *Student) AddScore(score int) {
    s.Scores = append(s.Scores, score)
}

func (s *Student) AverageScore() float64 {
    if len(s.Scores) == 0 {
        return 0
    }
    sum := 0
    for _, sc := range s.Scores {
        sum += sc
    }
    return float64(sum) / float64(len(s.Scores))
}

func main() {
    // --- 指针基础 ---
    x := 42
    var ptr *int = &x
    // ↑ *int：指向int的指针类型
    // ↑ &x：取x的内存地址

    fmt.Printf("x的值: %d, x的地址: %p\n", x, &x)
    fmt.Printf("ptr的值(地址): %p, ptr指向的值: %d\n", ptr, *ptr)
    // ↑ *ptr：解引用，获取指针指向的值

    *ptr = 100  // 通过指针修改原值
    fmt.Println("修改后x:", x)  // 输出100

    // new函数：分配内存并返回指针
    p := new(int)  // new(int) 等价于 var i int; p := &i
    *p = 50
    fmt.Println("new分配的值:", *p)

    // --- 结构体创建 ---
    // 方式1：字面量
    stu1 := Student{
        Name:   "张三",
        Age:    20,
        Scores: []int{85, 90},
    }

    // 方式2：按顺序（不推荐，依赖字段顺序）
    stu2 := Student{"李四", 22, []int{78, 88}, ""}

    // 方式3：new返回指针
    stu3 := new(Student)  // *Student类型
    stu3.Name = "王五"
    stu3.Age = 19

    fmt.Println("\n=== 学生信息 ===")
    fmt.Println(stu1.GetInfo())
    fmt.Println(stu2.GetInfo())
    fmt.Println(stu3.GetInfo())

    // 调用指针接收者方法
    stu1.SetEmail("zhangsan@example.com")
    stu1.AddScore(95)
    fmt.Printf("\n%s 的平均分: %.2f\n",
        stu1.Name, stu1.AverageScore())

    // Go自动处理指针/值转换
    stu2Ptr := &stu2
    fmt.Println(stu2Ptr.GetInfo())
    // ↑ 即使GetInfo是值接收者，指针也能直接调用
    // ↑ Go自动解引用：(*stu2Ptr).GetInfo()
}
```

**💡 代码解释**：Go的指针和结构体是理解Go编程的关键。Go指针特点：(1)不支持算术运算，安全；(2)零值是nil。结构体是Go实现面向对象的核心，通过方法关联实现封装。值接收者vs指针接收者：(1)值接收者——方法内修改不影响原值；(2)指针接收者——可修改原值，避免大结构体拷贝。Go自动处理指针和值的转换，调用方法时无需显式解引用。

**🔑 关键要点**：
- &取地址，*解引用，Go指针不支持运算
- new()分配内存返回指针
- struct定义结构体，type定义新类型
- 值接收者方法不改原值，指针接收者可修改
- Go自动处理指针/值的方法调用转换

---

### 8. 包管理与导入

> **级别**：初级 | **概念**：Go以包(package)组织代码，首字母大小写决定可见性，import导入包，go mod管理外部依赖。

```go
// ============================================
// 【初级】Go 包管理与导入
// ============================================

package main  // main包：生成可执行文件

import (
    "fmt"
    "math"      // 标准库：数学函数
    "strings"   // 标准库：字符串操作
    "time"      // 标准库：时间操作

    // 自定义包导入示例
    // "github.com/user/project/internal/utils"
    // ↑ 导入项目内部的包
)

// --- 可见性规则：首字母大写=公开，小写=私有 ---

// 公开函数：可被外部包调用
func PublicFunction() string {
    return "我是公开函数（首字母大写）"
}

// 私有函数：仅本包内可用
func privateFunction() string {
    return "我是私有函数（首字母小写）"
}

// 公开常量
const MaxRetry = 3  // 首字母大写：外部可访问

// 私有常量
const defaultTimeout = 30  // 首字母小写：仅本包内可见

// --- 类型别名 ---
type UserID = int64  // =：类型别名，完全等价

// --- 自定义类型 ---
type StatusCode int  // 无=：定义新类型，需显式转换

const (
    StatusOK    StatusCode = 200
    StatusError StatusCode = 500
)

// --- init函数：包初始化时自动执行 ---
func init() {
    // ↑ init()：每个包可定义多个init函数
    // ↑ 在main函数之前自动执行
    fmt.Println("[init] 包初始化完成")
}

func main() {
    fmt.Println("=== 包管理与导入 ===")

    // 调用本包函数
    fmt.Println(PublicFunction())
    fmt.Println(privateFunction())

    // 使用导入的包
    fmt.Println("\n--- 标准库使用 ---")

    // math包：数学函数
    fmt.Printf("PI = %.10f\n", math.Pi)
    fmt.Printf("9的平方根 = %.2f\n", math.Sqrt(9))
    fmt.Printf("2的10次方 = %.0f\n", math.Pow(2, 10))

    // strings包：字符串操作
    text := "Hello, Go World"
    fmt.Println("原始:", text)
    fmt.Println("大写:", strings.ToUpper(text))
    fmt.Println("包含'Go'?", strings.Contains(text, "Go"))
    fmt.Println("替换:", strings.ReplaceAll(text, "World", "语言"))
    fmt.Println("分割:", strings.Split("a,b,c", ","))

    // time包：时间操作
    now := time.Now()
    fmt.Println("\n当前时间:", now.Format("2006-01-02 15:04:05"))
    // ↑ Go的格式化模板：2006-01-02 15:04:05（固定值）
    fmt.Println("Unix时间戳:", now.Unix())

    // 自定义类型
    fmt.Println("\n状态码:", StatusOK)
}

// 编译运行：
// go run main.go
// go build -o app.exe  # 编译为可执行文件
```

**💡 代码解释**：Go的包系统简洁而强大。可见性规则：首字母大写=公开(exported)，首字母小写=私有(unexported)。这个规则适用于变量、函数、常量、类型、结构体字段等所有命名元素。init()函数在包初始化时自动执行，常用于注册驱动、初始化配置等。Go的time格式化使用固定参考时间"2006-01-02 15:04:05"（即Mon Jan 2 15:04:05 MST 2006）。

**🔑 关键要点**：
- 首字母大写=公开（可导出），小写=私有（不可导出）
- 可见性规则适用于所有命名元素
- import导入包，go mod管理外部依赖
- init()函数在包初始化时自动执行
- time格式化使用固定参考时间

---

## 中级进阶

### 1. 接口（Interface）

> **级别**：中级 | **概念**：Go接口是方法集合的抽象，隐式实现（无需显式声明），空接口interface{}可表示任意类型。

```go
// ============================================
// 【中级】Go 接口（Interface）
// ============================================

package main

import (
    "fmt"
    "math"
)

// --- 接口定义 ---
type Shape interface {
    // ↑ interface：接口关键字
    // 接口定义了一组方法签名
    Area() float64      // 面积
    Perimeter() float64 // 周长
}

// --- 实现接口（隐式实现，无需implements关键字） ---

// 圆形
type Circle struct {
    Radius float64
}

// Circle实现Shape接口（隐式：只要实现所有方法即可）
func (c Circle) Area() float64 {
    return math.Pi * c.Radius * c.Radius
}

func (c Circle) Perimeter() float64 {
    return 2 * math.Pi * c.Radius
}

// 矩形
type Rectangle struct {
    Width, Height float64
}

func (r Rectangle) Area() float64 {
    return r.Width * r.Height
}

func (r Rectangle) Perimeter() float64 {
    return 2 * (r.Width + r.Height)
}

// --- 接口作为函数参数（多态） ---
func printShapeInfo(s Shape) {
    // ↑ 参数类型是接口，接受任何实现了Shape的类型
    fmt.Printf("面积: %.2f, 周长: %.2f\n", s.Area(), s.Perimeter())
}

// --- 空接口：可表示任意类型 ---
func describe(i interface{}) {
    // ↑ interface{}：空接口，任何类型都实现了它
    fmt.Printf("类型: %T, 值: %v\n", i, i)
    // ↑ %T：打印类型
    // ↑ %v：打印默认格式的值
}

// --- 类型断言 ---
func getTypeInfo(i interface{}) {
    // 方式1：comma ok模式
    if str, ok := i.(string); ok {
        // ↑ i.(string)：类型断言，判断i是否为string
        fmt.Println("是字符串:", str)
        return
    }

    // 方式2：switch类型断言
    switch v := i.(type) {  // .(type) 只能在switch中使用
    case int:
        fmt.Println("是整数:", v)
    case float64:
        fmt.Println("是浮点数:", v)
    case string:
        fmt.Println("是字符串:", v)
    case []int:
        fmt.Println("是int切片:", v)
    default:
        fmt.Printf("未知类型: %T\n", v)
    }
}

// --- 接口组合 ---
type Reader interface {
    Read(p []byte) (n int, err error)
}

type Writer interface {
    Write(p []byte) (n int, err error)
}

type ReadWriter interface {
    Reader  // 嵌入Reader接口
    Writer  // 嵌入Writer接口
}

func main() {
    // 创建实现类
    circle := Circle{Radius: 5}
    rect := Rectangle{Width: 4, Height: 6}

    // 多态：通过接口调用
    fmt.Println("=== 接口多态 ===")
    fmt.Print("圆形 - ")
    printShapeInfo(circle)
    fmt.Print("矩形 - ")
    printShapeInfo(rect)

    // 接口切片
    shapes := []Shape{circle, rect}
    fmt.Println("\n遍历所有形状:")
    for _, s := range shapes {
        fmt.Printf("  %T: 面积=%.2f\n", s, s.Area())
    }

    // 空接口
    fmt.Println("\n=== 空接口 ===")
    describe(42)
    describe("hello")
    describe(circle)

    // 类型断言
    fmt.Println("\n=== 类型断言 ===")
    getTypeInfo("Go语言")
    getTypeInfo(100)
    getTypeInfo(3.14)
}
```

**💡 代码解释**：Go接口最独特的设计是隐式实现：类型不需要显式声明实现了哪个接口，只要实现了接口的所有方法就自动满足。这种"鸭子类型"使代码解耦更加灵活。空接口interface{}可接受任何类型，类似Java的Object。类型断言(value, ok := x.(Type))安全地判断接口变量的实际类型。接口组合（嵌入）是Go实现大型接口的惯用方式。

**🔑 关键要点**：
- 接口隐式实现：无需implements关键字
- 只要实现了接口的所有方法就自动满足接口
- 空接口interface{}可接受任意类型
- 类型断言：value, ok := x.(Type)安全判断
- 接口组合：嵌入接口实现大型接口

---

### 2. 错误处理模式

> **级别**：中级 | **概念**：Go没有异常机制，错误通过返回值传递。error是内置接口，支持自定义错误、错误包装和 sentinel error。

```go
// ============================================
// 【中级】Go 错误处理模式
// ============================================

package main

import (
    "errors"   // 创建基本错误
    "fmt"
    "os"       // 系统错误
)

// --- 自定义错误类型 ---
type ValidationError struct {
    Field string  // 出错的字段名
    Value interface{}  // 出错的字段值
    Msg   string  // 错误描述
}

// 实现error接口（Error() string方法）
func (e *ValidationError) Error() string {
    // ↑ Error() string：error接口唯一的方法
    return fmt.Sprintf("验证失败 [%s=%v]: %s", e.Field, e.Value, e.Msg)
}

// --- 自定义错误类型的便捷构造函数 ---
func NewValidationError(field string, value interface{}, msg string) error {
    return &ValidationError{
        Field: field,
        Value: value,
        Msg:   msg,
    }
}

// --- 业务函数：演示错误处理 ---
func validateAge(age int) error {
    if age < 0 {
        return NewValidationError("age", age, "年龄不能为负数")
    }
    if age > 150 {
        return NewValidationError("age", age, "年龄超出合理范围")
    }
    return nil  // nil表示无错误
}

func validateName(name string) error {
    if len(name) == 0 {
        return NewValidationError("name", name, "名称不能为空")
    }
    if len(name) > 50 {
        return NewValidationError("name", name, "名称过长")
    }
    return nil
}

// --- 错误包装（Go 1.13+） ---
func readConfig(path string) error {
    _, err := os.ReadFile(path)
    if err != nil {
        // fmt.Errorf + %w：包装错误，保留原始错误链
        return fmt.Errorf("读取配置文件 %s 失败: %w", path, err)
        // ↑ %w：错误包装动词，保留原始错误
    }
    return nil
}

// --- Sentinel Error（哨兵错误） ---
var (
    ErrNotFound   = errors.New("资源不存在")  // 预定义错误
    ErrTimeout    = errors.New("操作超时")
    ErrPermission = errors.New("权限不足")
)

func findUser(id int) (string, error) {
    if id <= 0 {
        return "", ErrNotFound
    }
    if id == 999 {
        return "", ErrPermission
    }
    return "用户" + fmt.Sprint(id), nil
}

func main() {
    // --- 基本错误处理 ---
    fmt.Println("=== 基本错误处理 ===")
    if err := validateAge(-5); err != nil {
        fmt.Println("错误:", err)
    }

    if err := validateName(""); err != nil {
        fmt.Println("错误:", err)
    }

    // --- 类型断言：判断具体错误类型 ---
    fmt.Println("\n=== 错误类型判断 ===")
    err := validateAge(200)
    var valErr *ValidationError  // 声明目标错误类型
    if errors.As(err, &valErr) {
        // ↑ errors.As()：判断错误链中是否包含指定类型
        fmt.Printf("字段 '%s' 的值 %v 不合法\n", valErr.Field, valErr.Value)
    }

    // --- Sentinel Error 判断 ---
    fmt.Println("\n=== Sentinel Error ===")
    _, err = findUser(-1)
    if errors.Is(err, ErrNotFound) {
        // ↑ errors.Is()：判断错误链中是否包含指定错误
        fmt.Println("用户不存在，请检查ID")
    } else if errors.Is(err, ErrPermission) {
        fmt.Println("无权访问")
    }

    // --- 错误包装与解包 ---
    fmt.Println("\n=== 错误包装 ===")
    err = readConfig("nonexistent.json")
    if err != nil {
        fmt.Println("包装错误:", err)
        // errors.Unwrap()：获取被包装的原始错误
        if unwrapped := errors.Unwrap(err); unwrapped != nil {
            fmt.Println("原始错误:", unwrapped)
        }
    }

    // --- 错误处理最佳实践 ---
    fmt.Println("\n=== 最佳实践 ===")
    fmt.Println("1. 优先使用errors.New()或fmt.Errorf()")
    fmt.Println("2. 自定义错误类型实现Error()方法")
    fmt.Println("3. 用errors.Is()判断Sentinel Error")
    fmt.Println("4. 用errors.As()判断错误类型")
    fmt.Println("5. 用%w包装错误，保留错误链")
}
```

**💡 代码解释**：Go没有try-catch，错误处理通过返回值显式进行。error是内置接口，只有一个Error() string方法。Go 1.13引入了错误包装机制：fmt.Errorf的%w动词包装错误，errors.Is()判断错误链中是否包含特定错误，errors.As()判断错误链中是否包含特定类型。Sentinel Error是预定义的错误常量，用于比较判断。自定义错误类型可携带更多上下文信息。

**🔑 关键要点**：
- error接口只有Error() string方法
- errors.New()创建基本错误，fmt.Errorf()格式化
- errors.Is()判断Sentinel Error，errors.As()判断类型
- %w包装错误，errors.Unwrap()解包
- 函数通常返回(result, error)模式

---

### 3. Goroutine与Channel

> **级别**：中级 | **概念**：Goroutine是Go的轻量级协程，Channel是协程间通信的管道，两者结合实现CSP并发模型。

```go
// ============================================
// 【中级】Go Goroutine与Channel并发
// ============================================

package main

import (
    "fmt"
    "time"
)

// --- Goroutine：轻量级协程 ---
func worker(id int, jobs <-chan int, results chan<- int) {
    // ↑ jobs <-chan int：只读通道
    // ↑ results chan<- int：只写通道
    for job := range jobs {  // range遍历通道，通道关闭时自动退出
        fmt.Printf("  工作者%d 处理任务 %d\n", id, job)
        time.Sleep(500 * time.Millisecond)  // 模拟工作
        results <- job * 2  // 发送结果到通道
    }
}

func main() {
    // --- Goroutine基础 ---
    fmt.Println("=== Goroutine ===")

    // 使用go关键字启动协程
    go func() {
        // ↑ go：启动一个新的goroutine
        fmt.Println("  异步执行：在协程中运行")
    }()

    time.Sleep(100 * time.Millisecond)  // 等待协程执行

    // --- Channel：协程通信 ---
    fmt.Println("\n=== Channel ===")

    // 创建无缓冲通道
    ch := make(chan string)
    // ↑ make(chan string)：创建无缓冲通道
    // ↑ 无缓冲通道：发送和接收必须同时就绪

    // 在协程中发送
    go func() {
        ch <- "Hello from goroutine"  // <- 发送到通道
    }()

    // 在主协程中接收
    msg := <-ch  // <- 从通道接收
    fmt.Println("收到:", msg)

    // 创建有缓冲通道
    buffered := make(chan int, 3)  // 容量为3
    // ↑ 有缓冲通道：缓冲未满时发送不阻塞
    buffered <- 1
    buffered <- 2
    buffered <- 3
    fmt.Printf("缓冲通道长度: %d/%d\n", len(buffered), cap(buffered))

    fmt.Println("从缓冲通道接收:", <-buffered, <-buffered, <-buffered)

    // --- Worker Pool模式 ---
    fmt.Println("\n=== Worker Pool ===")

    const numJobs = 5
    const numWorkers = 3

    jobs := make(chan int, numJobs)
    results := make(chan int, numJobs)

    // 启动工作者
    for w := 1; w <= numWorkers; w++ {
        go worker(w, jobs, results)
    }

    // 发送任务
    for j := 1; j <= numJobs; j++ {
        jobs <- j
    }
    close(jobs)  // close()：关闭通道，通知接收方不再发送

    // 收集结果
    for r := 1; r <= numJobs; r++ {
        result := <-results
        fmt.Println("  结果:", result)
    }

    // --- select：多路复用 ---
    fmt.Println("\n=== select多路复用 ===")
    ch1 := make(chan string)
    ch2 := make(chan string)

    go func() {
        time.Sleep(200 * time.Millisecond)
        ch1 <- "来自通道1"
    }()

    go func() {
        time.Sleep(100 * time.Millisecond)
        ch2 <- "来自通道2"
    }()

    // select：等待多个通道操作
    for i := 0; i < 2; i++ {
        select {  // select：哪个通道先就绪就执行哪个case
        case msg1 := <-ch1:
            fmt.Println("收到:", msg1)
        case msg2 := <-ch2:
            fmt.Println("收到:", msg2)
        case <-time.After(1 * time.Second):
            // ↑ time.After()：超时控制
            fmt.Println("超时！")
        }
    }
}
```

**💡 代码解释**：Goroutine是Go并发的核心，比线程更轻量（几KB栈空间），可创建成千上万个。Channel是Go的CSP并发模型的核心："不要通过共享内存来通信，而要通过通信来共享内存"。无缓冲通道同步阻塞，有缓冲通道异步。select实现多路复用，配合time.After()实现超时控制。close()关闭通道后，range遍历自动退出。

**🔑 关键要点**：
- go关键字启动goroutine，轻量级协程
- Channel是协程通信的管道：<-发送，<-接收
- 无缓冲通道同步阻塞，有缓冲通道异步
- select多路复用，time.After()超时控制
- close()关闭通道，range自动退出

---

### 4. defer/panic/recover

> **级别**：中级 | **概念**：defer延迟执行用于资源清理，panic触发恐慌，recover捕获恐慌恢复程序，三者配合处理异常情况。

```go
// ============================================
// 【中级】Go defer/panic/recover 机制
// ============================================

package main

import "fmt"

// --- defer：延迟执行（LIFO栈顺序） ---
func deferDemo() {
    fmt.Println("=== defer 延迟执行 ===")

    defer fmt.Println("  第1个defer")
    // ↑ defer：函数返回前执行，类似finally
    defer fmt.Println("  第2个defer")
    defer fmt.Println("  第3个defer")
    // ↑ defer按LIFO顺序执行：3 → 2 → 1

    fmt.Println("  正常执行")
    // 输出顺序：正常执行 → 第3个defer → 第2个defer → 第1个defer
}

// --- defer 参数快照 ---
func deferSnapshot() {
    fmt.Println("\n=== defer参数快照 ===")
    x := 1
    defer fmt.Println("  defer时x =", x)  // 参数在defer时立即求值
    x = 100
    fmt.Println("  修改后x =", x)
    // 输出：修改后x=100 → defer时x=1
}

// --- defer 常用场景：资源清理 ---
func fileOperation() {
    fmt.Println("\n=== defer资源清理 ===")
    fmt.Println("  打开文件...")
    defer fmt.Println("  关闭文件（defer确保执行）")
    // ↑ 无论函数如何退出，defer都会执行
    fmt.Println("  处理文件...")
    fmt.Println("  函数即将返回")
}

// --- panic：触发恐慌 ---
func panicDemo() {
    fmt.Println("\n=== panic 触发恐慌 ===")
    defer fmt.Println("  defer在panic前注册的会执行")

    fmt.Println("  正常执行中...")
    // panic("发生严重错误！")
    // ↑ panic()：立即停止当前函数，执行defer，然后崩溃
    fmt.Println("  这行不会执行（如果panic被触发）")
}

// --- recover：捕获恐慌 ---
func safeDivide(a, b int) (result int) {
    defer func() {
        if r := recover(); r != nil {
            // ↑ recover()：捕获panic，返回panic的参数
            // ↑ 只能在defer函数中调用recover
            fmt.Printf("  捕获到panic: %v\n", r)
            result = 0  // 设置默认返回值
        }
    }()

    if b == 0 {
        panic("除数不能为零")  // 触发panic
    }
    return a / b
}

// --- 完整示例：安全的数据库操作模拟 ---
func safeOperation() {
    fmt.Println("\n=== 安全的数据库操作 ===")

    defer func() {
        if r := recover(); r != nil {
            fmt.Printf("  恢复: 数据库操作失败 - %v\n", r)
            fmt.Println("  回滚事务")
        }
        fmt.Println("  关闭数据库连接（defer确保）")
    }()

    fmt.Println("  开始事务")
    fmt.Println("  执行SQL...")
    // panic("数据库连接中断")  // 模拟故障
    fmt.Println("  提交事务")
}

func main() {
    // defer演示
    deferDemo()
    deferSnapshot()
    fileOperation()

    // panic/recover演示
    panicDemo()

    // 安全的除法
    fmt.Println("\n=== recover安全除法 ===")
    fmt.Println("10/2 =", safeDivide(10, 2))
    fmt.Println("10/0 =", safeDivide(10, 0))

    // 安全操作
    safeOperation()

    // 最佳实践
    fmt.Println("\n=== 最佳实践 ===")
    fmt.Println("1. defer用于资源清理（文件、锁、连接）")
    fmt.Println("2. panic用于不可恢复的严重错误")
    fmt.Println("3. recover只在defer函数中有效")
    fmt.Println("4. 业务错误用error返回值，不用panic")
    fmt.Println("5. defer参数在声明时立即求值")
}
```

**💡 代码解释**：defer/panic/recover是Go的异常处理机制。defer按LIFO顺序执行，参数在声明时立即求值（快照），常用于资源清理。panic用于不可恢复的错误，会沿调用栈向上传播并执行所有defer。recover只能在defer函数中调用，用于捕获panic并恢复程序正常执行。最佳实践：业务错误用error返回值，panic仅用于真正的异常情况（如除零、数组越界等）。

**🔑 关键要点**：
- defer按LIFO顺序执行，参数立即求值
- defer常用于资源清理（文件、锁、连接）
- panic触发恐慌，沿调用栈传播
- recover只能在defer函数中捕获panic
- 业务错误用error，panic仅用于不可恢复的异常

---

### 5. Context包

> **级别**：中级 | **概念**：Context用于在goroutine间传递取消信号、超时控制和请求范围的值，是Go并发编程的标准范式。

```go
// ============================================
// 【中级】Go Context 包
// ============================================

package main

import (
    "context"  // 上下文包
    "fmt"
    "time"
)

// --- Context传值 ---
type contextKey string  // 自定义key类型，避免冲突

const (
    keyUserID contextKey = "userID"
    keyTraceID contextKey = "traceID"
)

// --- 模拟数据库查询 ---
func queryDatabase(ctx context.Context, query string) (string, error) {
    // ↑ ctx context.Context：第一个参数约定为context

    // 模拟耗时操作
    select {
    case <-time.After(500 * time.Millisecond):
        // 正常完成
        return "查询结果: " + query, nil
    case <-ctx.Done():
        // ↑ ctx.Done()：返回一个通道，context取消时关闭
        return "", ctx.Err()
        // ↑ ctx.Err()：返回取消原因
    }
}

// --- 模拟HTTP请求处理 ---
func handleRequest(ctx context.Context) {
    // 从context中获取值
    userID, ok := ctx.Value(keyUserID).(string)
    // ↑ ctx.Value()：从context中获取值
    if ok {
        fmt.Printf("  处理用户 %s 的请求\n", userID)
    }

    // 创建子context（超时控制）
    queryCtx, cancel := context.WithTimeout(ctx, 2*time.Second)
    // ↑ WithTimeout()：创建带超时的子context
    // ↑ cancel()：用于提前取消（释放资源）
    defer cancel()  // 确保资源释放

    // 执行数据库查询
    result, err := queryDatabase(queryCtx, "SELECT * FROM users")
    if err != nil {
        fmt.Printf("  查询失败: %v\n", err)
        return
    }
    fmt.Println("  查询成功:", result)
}

func main() {
    // --- 创建Context的四种方式 ---
    fmt.Println("=== Context创建方式 ===")

    // 1. context.Background()：根context
    bg := context.Background()
    fmt.Println("Background:", bg)

    // 2. context.TODO()：不确定用哪个context时的占位
    todo := context.TODO()
    fmt.Println("TODO:", todo)

    // 3. WithCancel：手动取消
    fmt.Println("\n=== WithCancel：手动取消 ===")
    ctx, cancel := context.WithCancel(bg)

    go func() {
        time.Sleep(500 * time.Millisecond)
        cancel()  // 500ms后取消
        fmt.Println("  已发送取消信号")
    }()

    select {
    case <-ctx.Done():
        fmt.Println("  收到取消信号:", ctx.Err())
    case <-time.After(1 * time.Second):
        fmt.Println("  超时")
    }

    // 4. WithTimeout：超时自动取消
    fmt.Println("\n=== WithTimeout：超时控制 ===")
    timeoutCtx, timeoutCancel := context.WithTimeout(bg, 300*time.Millisecond)
    defer timeoutCancel()

    select {
    case <-timeoutCtx.Done():
        fmt.Println("  超时:", timeoutCtx.Err())
    case <-time.After(500 * time.Millisecond):
        fmt.Println("  操作完成")
    }

    // 5. WithDeadline：指定截止时间
    fmt.Println("\n=== WithDeadline：截止时间 ===")
    deadline := time.Now().Add(200 * time.Millisecond)
    deadlineCtx, deadlineCancel := context.WithDeadline(bg, deadline)
    defer deadlineCancel()

    select {
    case <-deadlineCtx.Done():
        fmt.Println("  截止时间到:", deadlineCtx.Err())
    case <-time.After(500 * time.Millisecond):
        fmt.Println("  操作完成")
    }

    // 6. WithValue：传递值
    fmt.Println("\n=== WithValue：传递请求范围的值 ===")
    valueCtx := context.WithValue(bg, keyUserID, "user123")
    valueCtx = context.WithValue(valueCtx, keyTraceID, "trace-abc")

    // 获取值
    if userID := valueCtx.Value(keyUserID); userID != nil {
        fmt.Println("  userID:", userID)
    }
    if traceID := valueCtx.Value(keyTraceID); traceID != nil {
        fmt.Println("  traceID:", traceID)
    }

    // --- 完整示例 ---
    fmt.Println("\n=== 完整请求处理 ===")
    requestCtx := context.WithValue(bg, keyUserID, "user456")
    handleRequest(requestCtx)

    // 最佳实践
    fmt.Println("\n=== Context最佳实践 ===")
    fmt.Println("1. Context作为函数第一个参数")
    fmt.Println("2. 不要将Context存储在结构体中")
    fmt.Println("3. Context是线程安全的")
    fmt.Println("4. 只传递请求范围的数据，不传业务参数")
    fmt.Println("5. 用完记得调用cancel()释放资源")
}
```

**💡 代码解释**：Context是Go并发编程的标准范式，解决了goroutine间的取消传播和超时控制问题。四种创建方式：Background()根节点、TODO()占位、WithCancel()手动取消、WithTimeout()/WithDeadline()超时取消。WithValue()传递请求范围的值（如traceID、userID），但不应传递业务参数。Context是线程安全的，应作为函数第一个参数传递。

**🔑 关键要点**：
- Context作为函数第一个参数传递
- WithCancel手动取消，WithTimeout超时取消
- ctx.Done()返回取消信号通道
- ctx.Value()获取传递的值
- 用完记得调用cancel()释放资源

---

### 6. 测试与基准测试

> **级别**：中级 | **概念**：Go内置testing包，支持单元测试、表驱动测试、基准测试和示例测试，go test命令运行测试。

```go
// ============================================
// 【中级】Go 测试与基准测试
// ============================================

package main

import (
    "fmt"
    "testing"  // 测试框架（仅在_test.go文件中使用）
)

// ★ 测试文件命名：xxx_test.go
// ★ 测试函数命名：TestXxx(t *testing.T)
// ★ 运行测试：go test -v
// ★ 覆盖率：go test -cover

// --- 被测试的函数 ---
func Add(a, b int) int {
    return a + b
}

func Divide(a, b float64) (float64, error) {
    if b == 0 {
        return 0, fmt.Errorf("除数不能为零")
    }
    return a / b, nil
}

func IsPalindrome(s string) bool {
    // 判断字符串是否为回文
    runes := []rune(s)  // 转为rune切片，支持中文
    for i, j := 0, len(runes)-1; i < j; i, j = i+1, j-1 {
        if runes[i] != runes[j] {
            return false
        }
    }
    return true
}

// --- 单元测试 ---
func TestAdd(t *testing.T) {
    // ↑ 测试函数必须以Test开头
    // ↑ t *testing.T：测试句柄

    result := Add(2, 3)
    expected := 5

    if result != expected {
        t.Errorf("Add(2, 3) = %d, 期望 %d", result, expected)
        // ↑ t.Errorf()：报告错误但不停止测试
    }
}

// --- 表驱动测试（Go惯用模式） ---
func TestDivide(t *testing.T) {
    // 定义测试用例表
    tests := []struct {
        name     string   // 测试用例名称
        a, b     float64  // 输入参数
        expected float64  // 期望结果
        hasError bool     // 是否期望错误
    }{
        {"正常除法", 10, 2, 5, false},
        {"除零", 10, 0, 0, true},
        {"负数除法", -10, 2, -5, false},
        {"小数除法", 7.5, 2.5, 3, false},
    }

    for _, tt := range tests {
        t.Run(tt.name, func(t *testing.T) {
            // ↑ t.Run()：子测试，可单独运行
            result, err := Divide(tt.a, tt.b)

            if tt.hasError {
                if err == nil {
                    t.Error("期望有错误，但没有发生")
                }
            } else {
                if err != nil {
                    t.Errorf("不期望的错误: %v", err)
                }
                if result != tt.expected {
                    t.Errorf("Divide(%v, %v) = %v, 期望 %v",
                        tt.a, tt.b, result, tt.expected)
                }
            }
        })
    }
}

// --- 表驱动测试：回文判断 ---
func TestIsPalindrome(t *testing.T) {
    tests := []struct {
        input    string
        expected bool
    }{
        {"level", true},
        {"hello", false},
        {"上海自来水来自海上", true},  // 中文回文
        {"", true},  // 空字符串是回文
        {"a", true}, // 单字符是回文
    }

    for _, tt := range tests {
        t.Run(tt.input, func(t *testing.T) {
            result := IsPalindrome(tt.input)
            if result != tt.expected {
                t.Errorf("IsPalindrome(%q) = %v, 期望 %v",
                    tt.input, result, tt.expected)
            }
        })
    }
}

// --- 基准测试 ---
func BenchmarkAdd(b *testing.B) {
    // ↑ 基准测试函数以Benchmark开头
    // ↑ b *testing.B：基准测试句柄
    // 运行：go test -bench=. -benchmem

    for i := 0; i < b.N; i++ {
        // ↑ b.N：框架自动调整的迭代次数
        Add(10, 20)
    }
}

// --- 示例测试（同时作为文档） ---
func ExampleAdd() {
    // ↑ 示例函数以Example开头
    // 运行：go test -v
    // godoc会将其显示为文档

    fmt.Println(Add(1, 2))
    // Output: 3
    // ↑ Output注释：期望的输出，go test会验证
}

// --- 测试辅助函数 ---
func assertEqual(t *testing.T, got, expected interface{}, msg string) {
    t.Helper()  // 标记为辅助函数，错误报告在调用处
    if got != expected {
        t.Errorf("%s: got %v, expected %v", msg, got, expected)
    }
}

// 运行测试的命令：
// go test                    # 运行当前包测试
// go test -v                 # 详细输出
// go test -cover             # 覆盖率
// go test -coverprofile=c.out  # 生成覆盖率文件
// go tool cover -html=c.out  # 浏览器查看覆盖率
// go test -bench=.           # 运行基准测试
// go test -bench=. -benchmem # 含内存分配统计
func main() {
    fmt.Println("=== Go测试指南 ===")
    fmt.Println("测试文件: xxx_test.go")
    fmt.Println("测试函数: TestXxx(t *testing.T)")
    fmt.Println("基准测试: BenchmarkXxx(b *testing.B)")
    fmt.Println("示例测试: ExampleXxx()")
    fmt.Println("\n运行命令:")
    fmt.Println("  go test -v")
    fmt.Println("  go test -cover")
    fmt.Println("  go test -bench=. -benchmem")
}
```

**💡 代码解释**：Go的测试工具链内置于语言中。表驱动测试是Go社区的惯用模式，用一个测试用例切片覆盖多种场景，代码简洁且易于扩展。基准测试通过b.N自动调整迭代次数，配合-benchmem分析内存分配。示例测试既是测试也是文档，Output注释会被go test验证。t.Helper()标记辅助函数，错误报告定位到调用处。

**🔑 关键要点**：
- 测试文件命名xxx_test.go，函数Test开头
- 表驱动测试：用测试用例切片覆盖多种场景
- t.Run()子测试可单独运行特定用例
- 基准测试Benchmark开头，b.N自动调整迭代次数
- 示例测试Example开头，Output注释验证输出

---

### 7. 标准库（net/http + encoding/json）

> **级别**：中级 | **概念**：Go标准库的net/http提供HTTP客户端和服务端，encoding/json处理JSON序列化，无需第三方框架即可构建API。

```go
// ============================================
// 【中级】Go 标准库：net/http + encoding/json
// ============================================

package main

import (
    "encoding/json"  // JSON序列化
    "fmt"
    "io"
    "net/http"       // HTTP客户端与服务端
    "strings"
    "time"
)

// --- 数据结构 ---
type User struct {
    ID        int       `json:"id"`
    // ↑ json tag：指定JSON字段名
    Name      string    `json:"name"`
    Email     string    `json:"email"`
    Age       int       `json:"age"`
    CreatedAt time.Time `json:"created_at"`
    isActive  bool      // 小写字段不会被JSON序列化
}

// --- JSON序列化 ---
func jsonDemo() {
    fmt.Println("=== JSON序列化 ===")

    // 结构体 → JSON（Marshal）
    user := User{
        ID:        1,
        Name:      "张三",
        Email:     "zhangsan@example.com",
        Age:       28,
        CreatedAt: time.Now(),
        isActive:  true,
    }

    jsonBytes, err := json.Marshal(user)
    // ↑ Marshal()：序列化为JSON字节切片
    if err != nil {
        fmt.Println("序列化失败:", err)
    } else {
        fmt.Println("序列化:", string(jsonBytes))
    }

    // 格式化输出
    jsonPretty, _ := json.MarshalIndent(user, "", "  ")
    // ↑ MarshalIndent()：带缩进的格式化输出
    fmt.Println("格式化:\n" + string(jsonPretty))

    // JSON → 结构体（Unmarshal）
    jsonStr := `{"id":2,"name":"李四","email":"lisi@example.com","age":25}`
    var newUser User
    err = json.Unmarshal([]byte(jsonStr), &newUser)
    // ↑ Unmarshal()：反序列化，传入指针
    if err != nil {
        fmt.Println("反序列化失败:", err)
    } else {
        fmt.Printf("反序列化: %+v\n", newUser)
    }

    // 解码为map
    var data map[string]interface{}
    json.Unmarshal([]byte(jsonStr), &data)
    fmt.Println("解码为map:", data)
}

// --- HTTP服务端 ---
func startServer() {
    // 注册路由处理函数
    http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
        // ↑ w http.ResponseWriter：写入响应
        // ↑ r *http.Request：请求信息
        fmt.Fprintf(w, "欢迎访问Go HTTP服务！")
    })

    http.HandleFunc("/api/users", func(w http.ResponseWriter, r *http.Request) {
        w.Header().Set("Content-Type", "application/json")
        // ↑ 设置响应头

        users := []User{
            {ID: 1, Name: "张三", Email: "zhangsan@example.com", Age: 28},
            {ID: 2, Name: "李四", Email: "lisi@example.com", Age: 25},
        }

        json.NewEncoder(w).Encode(users)
        // ↑ json.NewEncoder(w).Encode()：直接写入响应
    })

    fmt.Println("HTTP服务启动在 http://localhost:8080")
    // http.ListenAndServe(":8080", nil)  // 启动服务（阻塞）
}

// --- HTTP客户端 ---
func httpClientDemo() {
    fmt.Println("\n=== HTTP客户端 ===")

    // GET请求
    resp, err := http.Get("https://httpbin.org/get")
    // ↑ http.Get()：发送GET请求
    if err != nil {
        fmt.Println("GET请求失败:", err)
        return
    }
    defer resp.Body.Close()  // 关闭响应体

    fmt.Println("状态码:", resp.StatusCode)
    body, _ := io.ReadAll(resp.Body)
    fmt.Println("响应体(前100字符):", string(body)[:100])

    // POST请求
    jsonData := `{"name":"测试用户","age":30}`
    resp, err = http.Post(
        "https://httpbin.org/post",
        "application/json",  // Content-Type
        strings.NewReader(jsonData))  // 请求体

    if err != nil {
        fmt.Println("POST请求失败:", err)
        return
    }
    defer resp.Body.Close()

    fmt.Println("\nPOST状态码:", resp.StatusCode)
    body, _ = io.ReadAll(resp.Body)
    fmt.Println("POST响应(前100字符):", string(body)[:100])

    // 自定义请求
    req, _ := http.NewRequest("GET", "https://httpbin.org/headers", nil)
    req.Header.Set("X-Custom-Header", "go-client")
    // ↑ 设置自定义请求头

    client := &http.Client{Timeout: 10 * time.Second}
    // ↑ 设置超时时间
    resp, err = client.Do(req)
    if err == nil {
        fmt.Println("\n自定义请求状态码:", resp.StatusCode)
        resp.Body.Close()
    }
}

func main() {
    jsonDemo()
    httpClientDemo()

    fmt.Println("\n=== HTTP服务端示例 ===")
    fmt.Println("取消注释 http.ListenAndServe 启动服务")
    fmt.Println("访问 http://localhost:8080/api/users 获取JSON")
    // startServer()  // 取消注释启动服务
}
```

**💡 代码解释**：Go标准库的net/http功能强大，无需第三方框架即可构建生产级HTTP服务。json tag控制字段映射，小写字段不会被序列化。json.NewEncoder(w).Encode()直接写入HTTP响应，比Marshal+Write更高效。http.Client可设置超时、连接池等参数。json.RawMessage用于延迟解析JSON字段。Go 1.22+支持更简洁的路由模式。

**🔑 关键要点**：
- json.Marshal()序列化，json.Unmarshal()反序列化
- json tag控制字段名映射，小写字段不序列化
- http.HandleFunc注册路由，ListenAndServe启动服务
- json.NewEncoder(w).Encode()直接写入响应
- http.Client设置超时，http.NewRequest自定义请求

---

### 8. 反射（Reflect）

> **级别**：中级 | **概念**：Go的reflect包提供运行时类型检查和值操作，用于序列化、ORM、依赖注入等通用框架开发。

```go
// ============================================
// 【中级】Go 反射（Reflect）
// ============================================

package main

import (
    "fmt"
    "reflect"  // 反射包
)

// --- 结构体定义 ---
type Person struct {
    Name string `json:"name" validate:"required"`
    Age  int    `json:"age" validate:"min=0,max=150"`
    Email string `json:"email"`
}

// --- 泛型打印函数（使用反射） ---
func PrintTypeInfo(v interface{}) {
    // ↑ interface{}：接受任意类型
    t := reflect.TypeOf(v)  // TypeOf()：获取类型信息
    val := reflect.ValueOf(v)  // ValueOf()：获取值信息

    fmt.Printf("类型: %v, 种类: %v\n", t, val.Kind())
    // ↑ Kind()：获取底层种类（struct/ptr/slice等）
}

// --- 反射遍历结构体字段 ---
func InspectStruct(v interface{}) {
    val := reflect.ValueOf(v)

    // 如果是指针，获取指向的值
    if val.Kind() == reflect.Ptr {
        val = val.Elem()  // Elem()：解引用
    }

    if val.Kind() != reflect.Struct {
        fmt.Println("不是结构体类型")
        return
    }

    t := val.Type()
    fmt.Printf("\n结构体: %s (%d个字段)\n", t.Name(), t.NumField())
    // ↑ NumField()：字段数量

    for i := 0; i < t.NumField(); i++ {
        field := t.Field(i)  // 字段类型信息
        value := val.Field(i)  // 字段值信息

        // 获取struct tag
        jsonTag := field.Tag.Get("json")
        // ↑ Tag.Get()：获取指定tag的值

        fmt.Printf("  [%d] %s (%s) = %v, json:"%s"\n",
            i, field.Name, field.Type, value, jsonTag)
    }
}

// --- 反射动态调用方法 ---
func CallMethod(v interface{}, methodName string) {
    val := reflect.ValueOf(v)
    method := val.MethodByName(methodName)
    // ↑ MethodByName()：按名称获取方法

    if !method.IsValid() {
        fmt.Printf("方法 %s 不存在\n", methodName)
        return
    }

    // 调用方法
    results := method.Call(nil)  // Call()：调用方法，传入参数
    fmt.Printf("调用 %s() 返回: ", methodName)
    for _, r := range results {
        fmt.Print(r, " ")
    }
    fmt.Println()
}

// --- 反射动态设置字段值 ---
func SetField(v interface{}, fieldName string, newValue interface{}) error {
    val := reflect.ValueOf(v)

    // 必须是指针才能修改
    if val.Kind() != reflect.Ptr {
        return fmt.Errorf("必须传入指针")
    }

    val = val.Elem()  // 获取指针指向的值
    field := val.FieldByName(fieldName)
    // ↑ FieldByName()：按名称获取字段

    if !field.IsValid() {
        return fmt.Errorf("字段 %s 不存在", fieldName)
    }

    if !field.CanSet() {
        // ↑ CanSet()：检查字段是否可设置
        return fmt.Errorf("字段 %s 不可设置（可能是未导出字段）", fieldName)
    }

    field.Set(reflect.ValueOf(newValue))  // Set()：设置字段值
    return nil
}

func main() {
    // 类型信息
    fmt.Println("=== 类型信息 ===")
    PrintTypeInfo(42)
    PrintTypeInfo("hello")
    PrintTypeInfo([]int{1, 2, 3})

    // 结构体反射
    person := Person{Name: "张三", Age: 30, Email: "zhangsan@example.com"}
    InspectStruct(person)

    // 动态设置字段
    fmt.Println("\n=== 动态设置字段 ===")
    fmt.Println("修改前:", person.Name)
    err := SetField(&person, "Name", "李四")
    // ↑ 必须传入指针
    if err != nil {
        fmt.Println("设置失败:", err)
    } else {
        fmt.Println("修改后:", person.Name)
    }

    // 反射应用场景
    fmt.Println("\n=== 反射应用场景 ===")
    fmt.Println("1. JSON序列化/反序列化")
    fmt.Println("2. ORM框架（结构体→数据库表映射）")
    fmt.Println("3. 依赖注入容器")
    fmt.Println("4. 参数验证框架")
    fmt.Println("5. RPC框架（动态调用）")

    // 反射注意事项
    fmt.Println("\n=== 反射注意事项 ===")
    fmt.Println("1. 性能开销较大，慎用")
    fmt.Println("2. 编译期类型安全丧失")
    fmt.Println("3. 代码可读性降低")
    fmt.Println("4. 优先使用泛型（Go 1.18+）")
}
```

**💡 代码解释**：Go的reflect包提供了强大的运行时反射能力，但应谨慎使用。核心API：TypeOf()获取类型信息，ValueOf()获取值信息，FieldByName()按名称获取字段，MethodByName()按名称获取方法。反射常用于JSON序列化、ORM框架、依赖注入等通用框架开发。Go 1.18引入泛型后，许多反射场景可用泛型替代，性能更好且类型安全。

**🔑 关键要点**：
- TypeOf()获取类型，ValueOf()获取值
- FieldByName()按名称获取字段，Tag.Get()获取tag
- Elem()解引用指针，CanSet()检查可设置性
- Set()设置字段值，需传入指针
- 反射有性能开销，优先使用泛型替代

---

## 高级精通

### 1. 并发模式（Worker Pool / Pipeline）

> **级别**：高级 | **概念**：掌握Worker Pool、Pipeline、Fan-out/Fan-in等Go高级并发模式，构建高性能并发应用。

```go
// ============================================
// 【高级】Go 并发模式：Worker Pool / Pipeline
// ============================================

package main

import (
    "fmt"
    "sync"
    "time"
)

// --- 1. Worker Pool：限制并发数 ---
func workerPool() {
    fmt.Println("=== Worker Pool 模式 ===")

    const numJobs = 10
    const numWorkers = 3

    jobs := make(chan int, numJobs)
    results := make(chan int, numJobs)

    // 使用WaitGroup等待所有worker完成
    var wg sync.WaitGroup
    // ↑ WaitGroup：等待一组goroutine完成

    // 启动固定数量的worker
    for w := 1; w <= numWorkers; w++ {
        wg.Add(1)  // 计数器+1
        go func(workerID int) {
            defer wg.Done()  // 完成时计数器-1
            for job := range jobs {  // range通道，直到关闭
                fmt.Printf("  Worker %d 处理任务 %d\n", workerID, job)
                time.Sleep(200 * time.Millisecond)
                results <- job * 2
            }
        }(w)
    }

    // 发送任务
    for j := 1; j <= numJobs; j++ {
        jobs <- j
    }
    close(jobs)  // 关闭任务通道

    // 等待所有worker完成后再关闭results
    go func() {
        wg.Wait()
        close(results)
    }()

    // 收集结果
    for result := range results {
        fmt.Println("  结果:", result)
    }
}

// --- 2. Pipeline：数据流管道 ---
func pipeline() {
    fmt.Println("\n=== Pipeline 模式 ===")

    // 阶段1：生成数字
    generate := func(nums ...int) <-chan int {
        out := make(chan int)
        go func() {
            for _, n := range nums {
                out <- n
            }
            close(out)
        }()
        return out
    }

    // 阶段2：平方
    square := func(in <-chan int) <-chan int {
        out := make(chan int)
        go func() {
            for n := range in {
                out <- n * n
            }
            close(out)
        }()
        return out
    }

    // 阶段3：过滤奇数
    filterOdd := func(in <-chan int) <-chan int {
        out := make(chan int)
        go func() {
            for n := range in {
                if n%2 == 0 {
                    out <- n
                }
            }
            close(out)
        }()
        return out
    }

    // 构建管道：generate → square → filterOdd
    c := generate(1, 2, 3, 4, 5, 6, 7, 8)
    out := filterOdd(square(c))

    fmt.Print("管道结果: ")
    for n := range out {
        fmt.Print(n, " ")
    }
    fmt.Println()
}

// --- 3. Fan-out/Fan-in：扇出扇入 ---
func fanOutFanIn() {
    fmt.Println("\n=== Fan-out / Fan-in 模式 ===")

    // 生成器
    producer := func() <-chan int {
        out := make(chan int)
        go func() {
            for i := 1; i <= 6; i++ {
                out <- i
            }
            close(out)
        }()
        return out
    }

    // 工作函数（扇出：多个goroutine处理同一个通道）
    worker := func(id int, in <-chan int) <-chan int {
        out := make(chan int)
        go func() {
            for n := range in {
                fmt.Printf("  Worker %d 处理 %d\n", id, n)
                time.Sleep(100 * time.Millisecond)
                out <- n * 10
            }
            close(out)
        }()
        return out
    }

    // 扇入：合并多个通道
    merge := func(channels ...<-chan int) <-chan int {
        var wg sync.WaitGroup
        out := make(chan int)

        // 为每个输入通道启动一个goroutine
        output := func(c <-chan int) {
            defer wg.Done()
            for n := range c {
                out <- n
            }
        }

        wg.Add(len(channels))
        for _, c := range channels {
            go output(c)
        }

        // 等待所有输入处理完毕，关闭输出
        go func() {
            wg.Wait()
            close(out)
        }()

        return out
    }

    // 构建Fan-out/Fan-in
    in := producer()

    // Fan-out：分发到3个worker
    w1 := worker(1, in)
    w2 := worker(2, in)
    w3 := worker(3, in)

    // Fan-in：合并结果
    fmt.Print("扇入结果: ")
    for n := range merge(w1, w2, w3) {
        fmt.Print(n, " ")
    }
    fmt.Println()
}

func main() {
    workerPool()
    pipeline()
    fanOutFanIn()

    fmt.Println("\n=== 并发模式总结 ===")
    fmt.Println("Worker Pool: 限制并发goroutine数量")
    fmt.Println("Pipeline: 多阶段数据流处理")
    fmt.Println("Fan-out: 多个goroutine从同一通道读取")
    fmt.Println("Fan-in: 多个通道合并为一个通道")
}
```

**💡 代码解释**：Go的并发模式建立在goroutine和channel之上。Worker Pool通过固定数量的goroutine处理任务，避免goroutine无限增长。Pipeline模式通过管道串联多个处理阶段，每个阶段通过channel传递数据。Fan-out/Fan-in模式实现负载分发和结果聚合。sync.WaitGroup用于等待一组goroutine完成。这些模式是构建高性能Go服务的基础。

**🔑 关键要点**：
- Worker Pool限制并发数，避免goroutine爆炸
- Pipeline通过channel串联多阶段处理
- Fan-out分发到多个goroutine，Fan-in合并结果
- sync.WaitGroup等待一组goroutine完成
- close()关闭通道通知接收方

---

### 2. 内存管理与GC调优

> **级别**：高级 | **概念**：理解Go内存分配（栈/堆/逃逸分析），掌握GC原理和三色标记算法，使用GODEBUG和pprof调优。

```go
// ============================================
// 【高级】Go 内存管理与GC调优
// ============================================

package main

import (
    "fmt"
    "runtime"
    "runtime/debug"  // GC控制
    "time"
)

// --- 逃逸分析示例 ---

// 返回指针：逃逸到堆
func createOnHeap() *int {
    x := 42
    return &x  // x逃逸到堆（因为返回了指针）
}

// 返回值：留在栈上
func createOnStack() int {
    x := 42
    return x  // x在栈上分配
}

// --- 内存分配演示 ---
func memoryDemo() {
    fmt.Println("=== 内存分配演示 ===")

    var m runtime.MemStats
    // ↑ MemStats：内存统计信息

    runtime.ReadMemStats(&m)
    // ↑ ReadMemStats()：读取当前内存统计

    fmt.Printf("当前分配: %d KB\n", m.Alloc/1024)
    fmt.Printf("累计分配: %d KB\n", m.TotalAlloc/1024)
    fmt.Printf("堆对象数: %d\n", m.HeapObjects)

    // 分配一些内存
    fmt.Println("\n分配100000个对象...")
    var slice []*int
    for i := 0; i < 100000; i++ {
        val := i
        slice = append(slice, &val)
    }

    runtime.ReadMemStats(&m)
    fmt.Printf("分配后: %d KB\n", m.Alloc/1024)
    fmt.Printf("堆对象数: %d\n", m.HeapObjects)

    // 释放引用
    slice = nil
    fmt.Println("\n释放引用后，触发GC...")

    runtime.GC()  // 手动触发GC
    // ↑ GC()：强制执行垃圾回收
    time.Sleep(100 * time.Millisecond)

    runtime.ReadMemStats(&m)
    fmt.Printf("GC后: %d KB\n", m.Alloc/1024)
    fmt.Printf("堆对象数: %d\n", m.HeapObjects)
}

// --- GC调优参数 ---
func gcTuning() {
    fmt.Println("\n=== GC调优参数 ===")

    // GOGC：控制GC触发频率
    // 默认100：堆增长100%时触发GC
    // 设置方式：
    //   export GOGC=200  (环境变量)
    //   debug.SetGCPercent(200)  (代码设置)

    currentGC := debug.SetGCPercent(100)
    // ↑ SetGCPercent()：设置并返回之前的GOGC值
    fmt.Println("当前GOGC:", currentGC)
    fmt.Println("GOGC=100: 堆增长100%时触发GC（默认）")
    fmt.Println("GOGC=off: 关闭自动GC")
    fmt.Println("GOGC=200: 降低GC频率（内存换CPU）")
    fmt.Println("GOGC=50: 增加GC频率（CPU换内存）")

    // GOMEMLIMIT：内存软限制（Go 1.19+）
    // 设置最大内存使用量，接近限制时更频繁GC
    // debug.SetMemoryLimit(8 * 1024 * 1024 * 1024)  // 8GB
    fmt.Println("\nGOMEMLIMIT: 内存软限制（Go 1.19+）")
    fmt.Println("接近限制时自动增加GC频率")
}

// --- 逃逸分析 ---
func escapeAnalysis() {
    fmt.Println("\n=== 逃逸分析 ===")
    fmt.Println("逃逸分析决定变量分配在栈还是堆")
    fmt.Println("\n逃逸到堆的场景:")
    fmt.Println("1. 返回局部变量的指针")
    fmt.Println("2. 将指针存入interface{}")
    fmt.Println("3. 闭包引用外部变量")
    fmt.Println("4. 变量过大（超过栈帧大小）")
    fmt.Println("\n分析命令:")
    fmt.Println("  go build -gcflags='-m -m' main.go")
    fmt.Println("  → 输出详细的逃逸分析结果")

    // 演示逃逸
    heapVal := createOnHeap()
    stackVal := createOnStack()
    fmt.Printf("\n堆上: %d, 栈上: %d\n", *heapVal, stackVal)
}

// --- 性能分析工具 ---
func profilingTools() {
    fmt.Println("\n=== 性能分析工具 ===")
    fmt.Println("pprof: 内置性能分析")
    fmt.Println("  import _ \"net/http/pprof\"")
    fmt.Println("  访问 http://localhost:6060/debug/pprof/")
    fmt.Println("\n命令行分析:")
    fmt.Println("  go tool pprof http://localhost:6060/debug/pprof/heap")
    fmt.Println("  go tool pprof http://localhost:6060/debug/pprof/profile")
    fmt.Println("\ntrace: 执行追踪")
    fmt.Println("  curl http://localhost:6060/debug/pprof/trace?seconds=5 > trace.out")
    fmt.Println("  go tool trace trace.out")
    fmt.Println("\nGODEBUG环境变量:")
    fmt.Println("  GODEBUG=gctrace=1 ./app  → GC跟踪日志")
    fmt.Println("  GODEBUG=schedtrace=1000 ./app  → 调度器跟踪")
}

func main() {
    memoryDemo()
    gcTuning()
    escapeAnalysis()
    profilingTools()

    fmt.Println("\n=== GC三色标记算法 ===")
    fmt.Println("白色: 未访问对象（潜在垃圾）")
    fmt.Println("灰色: 已访问但子对象未扫描")
    fmt.Println("黑色: 已访问且子对象已扫描")
    fmt.Println("\nGC过程:")
    fmt.Println("1. Mark Setup: STW，启动写屏障")
    fmt.Println("2. Marking: 并发标记，三色标记")
    fmt.Println("3. Mark Termination: STW，结束标记")
    fmt.Println("4. Sweeping: 并发清除白色对象")
}
```

**💡 代码解释**：Go的GC采用并发三色标记-清除算法，目标是低延迟（<1ms STW）。GOGC（默认100）控制GC触发时机：堆增长100%时触发。GOMEMLIMIT（Go 1.19+）设置内存软限制。逃逸分析决定变量分配在栈还是堆：栈上分配更快且无需GC。pprof是Go内置的性能分析工具，支持CPU、内存、goroutine等分析。trace工具追踪goroutine调度和执行。

**🔑 关键要点**：
- GOGC控制GC触发频率（默认100）
- GOMEMLIMIT设置内存软限制（Go 1.19+）
- 逃逸分析决定栈/堆分配
- pprof分析CPU/内存/goroutine
- GC三色标记：白→灰→黑，并发标记清除

---

### 3. CGO：调用C语言代码

> **级别**：高级 | **概念**：CGO允许Go调用C代码和C库，适用于性能关键模块、复用C库和系统级编程。

```go
// ============================================
// 【高级】Go CGO：调用C语言代码
// 编译: CGO_ENABLED=1 go build
// ============================================

package main

/*
// #include <stdio.h>   // C标准输入输出
// #include <stdlib.h>  // C标准库（malloc/free）
// #include <math.h>    // C数学库
//
// // C函数：计算平方
// double c_square(double x) {
//     return x * x;
// }
//
// // C函数：快速斐波那契
// int c_fibonacci(int n) {
//     if (n <= 1) return n;
//     return c_fibonacci(n-1) + c_fibonacci(n-2);
// }
//
// // C函数：字符串拼接
// char* c_concat(const char* a, const char* b) {
//     char* result = malloc(strlen(a) + strlen(b) + 1);
//     strcpy(result, a);
//     strcat(result, b);
//     return result;
// }
*/
import "C"  // 导入C伪包（必须在C注释块之后）
// ↑ import "C"：启用CGO，必须单独一行

import (
    "fmt"
    "unsafe"
)

func main() {
    fmt.Println("=== CGO 基础 ===")
    fmt.Println("CGO允许Go调用C代码和C库")
    fmt.Println("\n使用场景:")
    fmt.Println("1. 复用现有C/C++库")
    fmt.Println("2. 性能关键模块（SIMD、加密）")
    fmt.Println("3. 系统调用和底层操作")
    fmt.Println("4. 与硬件驱动交互")

    fmt.Println("\n注意事项:")
    fmt.Println("1. CGO调用有开销（约100ns/次）")
    fmt.Println("2. 失去跨平台编译优势")
    fmt.Println("3. C内存需手动管理（C.free）")
    fmt.Println("4. C代码中的panic不会传播到Go")

    // --- CGO使用示例 ---
    fmt.Println("\n=== CGO调用示例 ===")

    // 调用C数学函数
    // result := C.sqrt(16.0)  // 调用C的sqrt
    // fmt.Printf("C.sqrt(16) = %.2f\n", float64(result))

    // 调用自定义C函数
    // square := C.c_square(5.0)
    // fmt.Printf("C.c_square(5) = %.2f\n", float64(square))

    // 调用C斐波那契
    // fib := C.c_fibonacci(10)
    // fmt.Printf("C.fibonacci(10) = %d\n", int(fib))

    // 字符串操作
    // a := C.CString("Hello, ")
    // b := C.CString("CGO!")
    // defer C.free(unsafe.Pointer(a))  // 释放C内存
    // defer C.free(unsafe.Pointer(b))

    // resultStr := C.c_concat(a, b)
    // defer C.free(unsafe.Pointer(resultStr))
    // fmt.Printf("C.c_concat: %s\n", C.GoString(resultStr))
    // ↑ C.GoString()：C字符串转Go字符串

    // --- 类型转换 ---
    fmt.Println("\n=== CGO类型转换 ===")
    fmt.Println("Go → C:")
    fmt.Println("  C.int(x), C.double(x), C.CString(s)")
    fmt.Println("C → Go:")
    fmt.Println("  int(c), float64(c), C.GoString(s)")
    fmt.Println("指针:")
    fmt.Println("  unsafe.Pointer  ↔  C指针")

    // --- 编译说明 ---
    fmt.Println("\n=== 编译命令 ===")
    fmt.Println("CGO_ENABLED=1 go build")
    fmt.Println("CGO_ENABLED=1 GOOS=linux GOARCH=amd64 go build")
    fmt.Println("# 交叉编译需要C交叉编译器")

    // 避免未使用导入的编译错误
    _ = unsafe.Sizeof(0)
}
```

**💡 代码解释**：CGO是Go与C语言互操作的桥梁，通过import "C"启用。C代码写在紧邻import "C"之前的注释块中，使用/**/包围。C.CString()将Go字符串转为C字符串（需手动C.free()释放），C.GoString()反之。CGO调用有性能开销，每次调用约100ns，不适合频繁调用的场景。CGO使Go失去跨平台编译优势，需安装C编译器。

**🔑 关键要点**：
- import "C"启用CGO，C代码写在注释块中
- C.CString()转C字符串，C.GoString()转Go字符串
- C内存需手动管理（C.free()）
- CGO调用有约100ns开销
- CGO使Go失去跨平台编译优势

---

### 4. pprof性能分析

> **级别**：高级 | **概念**：使用pprof进行CPU、内存、goroutine分析，结合go tool pprof可视化和火焰图定位性能瓶颈。

```go
// ============================================
// 【高级】Go pprof 性能分析
// ============================================

package main

import (
    "fmt"
    "os"
    "runtime"
    "runtime/pprof"  // 性能分析
    "time"
)

// --- 待分析的函数 ---
func slowFunction() []int {
    // 故意低效的实现
    var result []int
    for i := 0; i < 100000; i++ {
        result = append(result, i*i)
        // ↑ append可能触发多次扩容
    }
    return result
}

func fastFunction() []int {
    // 优化版本：预分配容量
    result := make([]int, 0, 100000)
    // ↑ make预分配容量，避免扩容
    for i := 0; i < 100000; i++ {
        result = append(result, i*i)
    }
    return result
}

func memoryIntensive() {
    // 模拟内存密集型操作
    var data [][]byte
    for i := 0; i < 100; i++ {
        chunk := make([]byte, 1024*1024)  // 每次分配1MB
        data = append(data, chunk)
    }
    fmt.Printf("分配了 %d 个1MB块\n", len(data))
}

// --- CPU性能分析 ---
func cpuProfile() {
    fmt.Println("=== CPU性能分析 ===")

    // 创建CPU profile文件
    f, err := os.Create("cpu.prof")
    if err != nil {
        fmt.Println("创建文件失败:", err)
        return
    }
    defer f.Close()

    // 开始CPU分析
    pprof.StartCPUProfile(f)
    // ↑ StartCPUProfile()：开始记录CPU使用情况

    // 运行待分析的代码
    slowFunction()
    fastFunction()

    // 停止CPU分析
    pprof.StopCPUProfile()
    // ↑ StopCPUProfile()：停止记录

    fmt.Println("CPU profile 已保存到 cpu.prof")
    fmt.Println("分析命令: go tool pprof cpu.prof")
    fmt.Println("交互命令: top, list 函数名, web")
}

// --- 内存性能分析 ---
func memoryProfile() {
    fmt.Println("\n=== 内存性能分析 ===")

    // 运行内存密集型代码
    memoryIntensive()

    // 创建内存profile文件
    f, err := os.Create("mem.prof")
    if err != nil {
        fmt.Println("创建文件失败:", err)
        return
    }
    defer f.Close()

    runtime.GC()  // 先GC，分析存活对象
    pprof.WriteHeapProfile(f)
    // ↑ WriteHeapProfile()：写入堆内存profile

    fmt.Println("内存 profile 已保存到 mem.prof")
    fmt.Println("分析命令: go tool pprof mem.prof")
}

// --- goroutine分析 ---
func goroutineProfile() {
    fmt.Println("\n=== Goroutine分析 ===")

    // 创建大量goroutine
    done := make(chan bool)
    for i := 0; i < 100; i++ {
        go func(id int) {
            time.Sleep(100 * time.Millisecond)
            done <- true
        }(i)
    }

    // 等待所有goroutine完成
    for i := 0; i < 100; i++ {
        <-done
    }

    fmt.Printf("当前goroutine数: %d\n", runtime.NumGoroutine())
    // ↑ NumGoroutine()：当前goroutine数量

    // 保存goroutine profile
    f, _ := os.Create("goroutine.prof")
    defer f.Close()
    pprof.Lookup("goroutine").WriteTo(f, 0)
    // ↑ Lookup()：获取指定类型的profile
    fmt.Println("Goroutine profile 已保存")
}

func main() {
    fmt.Println("=== pprof 性能分析工具 ===")
    fmt.Println("\npprof类型:")
    fmt.Println("  profile: CPU使用情况")
    fmt.Println("  heap: 内存分配情况")
    fmt.Println("  goroutine: goroutine堆栈")
    fmt.Println("  block: 阻塞分析")
    fmt.Println("  mutex: 锁竞争分析")
    fmt.Println("  allocs: 所有内存分配")

    cpuProfile()
    memoryProfile()
    goroutineProfile()

    fmt.Println("\n=== 分析命令 ===")
    fmt.Println("go tool pprof cpu.prof")
    fmt.Println("  → top: 查看CPU消耗最高的函数")
    fmt.Println("  → list 函数名: 查看函数源码级分析")
    fmt.Println("  → web: 生成调用图（需graphviz）")
    fmt.Println("\ngo tool pprof -http=:8080 cpu.prof")
    fmt.Println("  → 浏览器查看火焰图等可视化分析")

    fmt.Println("\n=== HTTP端点方式 ===")
    fmt.Println("import _ \"net/http/pprof\"")
    fmt.Println("go func() { http.ListenAndServe(\":6060\", nil) }()")
    fmt.Println("访问 http://localhost:6060/debug/pprof/")

    // 清理
    os.Remove("cpu.prof")
    os.Remove("mem.prof")
    os.Remove("goroutine.prof")
}
```

**💡 代码解释**：pprof是Go内置的性能分析工具，支持CPU、内存、goroutine、阻塞、锁竞争等分析。使用方式：(1)代码中生成profile文件；(2)HTTP端点方式（net/http/pprof）。go tool pprof分析profile文件，支持top（查看热点函数）、list（源码级分析）、web（调用图）等命令。火焰图（-http模式）是最直观的性能分析方式。生产环境建议使用HTTP端点方式，无需重启服务。

**🔑 关键要点**：
- pprof支持CPU、内存、goroutine、阻塞、锁竞争分析
- StartCPUProfile/StopCPUProfile采集CPU数据
- WriteHeapProfile采集内存数据
- go tool pprof分析profile文件
- HTTP端点方式无需重启服务

---

### 5. 泛型（Go 1.18+）

> **级别**：高级 | **概念**：Go 1.18引入泛型，支持类型参数约束，编写类型安全的通用数据结构和算法，减少代码重复。

```go
// ============================================
// 【高级】Go 泛型编程（Go 1.18+）
// ============================================

package main

import (
    "fmt"
    "golang.org/x/exp/constraints"
    // ↑ constraints包：常用类型约束
)

// --- 泛型函数：类型参数 ---
func Min[T constraints.Ordered](a, b T) T {
    // ↑ [T constraints.Ordered]：类型参数列表
    // ↑ T：类型参数名
    // ↑ constraints.Ordered：类型约束（可比较的有序类型）
    if a < b {
        return a
    }
    return b
}

// --- 泛型函数：多类型参数 ---
func MapKeys[K comparable, V any](m map[K]V) []K {
    // ↑ [K comparable, V any]：两个类型参数
    // ↑ comparable：可比较的类型约束
    // ↑ any：任意类型（interface{}的别名）
    keys := make([]K, 0, len(m))
    for k := range m {
        keys = append(keys, k)
    }
    return keys
}

// --- 泛型结构体 ---
type Stack[T any] struct {
    // ↑ [T any]：类型参数化结构体
    items []T
}

// 泛型方法
func (s *Stack[T]) Push(item T) {
    // ↑ (s *Stack[T])：接收者包含类型参数
    s.items = append(s.items, item)
}

func (s *Stack[T]) Pop() (T, bool) {
    var zero T  // 类型参数的零值
    if len(s.items) == 0 {
        return zero, false
    }
    last := s.items[len(s.items)-1]
    s.items = s.items[:len(s.items)-1]
    return last, true
}

func (s *Stack[T]) Peek() (T, bool) {
    var zero T
    if len(s.items) == 0 {
        return zero, false
    }
    return s.items[len(s.items)-1], true
}

func (s *Stack[T]) Size() int {
    return len(s.items)
}

// --- 泛型接口 ---
type Set[T comparable] interface {
    // ↑ [T comparable]：泛型接口
    Add(item T)
    Contains(item T) bool
    Remove(item T)
}

// 泛型Set实现
type HashSet[T comparable] struct {
    data map[T]struct{}
    // ↑ struct{}：零内存占用的空结构体
}

func NewHashSet[T comparable]() *HashSet[T] {
    // ↑ 泛型构造函数
    return &HashSet[T]{
        data: make(map[T]struct{}),
    }
}

func (s *HashSet[T]) Add(item T) {
    s.data[item] = struct{}{}
}

func (s *HashSet[T]) Contains(item T) bool {
    _, exists := s.data[item]
    return exists
}

func (s *HashSet[T]) Remove(item T) {
    delete(s.data, item)
}

func (s *HashSet[T]) Size() int {
    return len(s.data)
}

// --- 自定义类型约束 ---
type Number interface {
    // ↑ 自定义约束接口
    ~int | ~int64 | ~float64
    // ↑ ~int：包括int和基于int的自定义类型
    // ↑ |：类型并集
}

func Sum[T Number](values []T) T {
    var total T
    for _, v := range values {
        total += v
    }
    return total
}

func main() {
    fmt.Println("=== Go 泛型编程（Go 1.18+） ===")

    // 泛型函数
    fmt.Println("Min(10, 20):", Min(10, 20))
    fmt.Println("Min(3.14, 2.71):", Min(3.14, 2.71))
    fmt.Println("Min('a', 'z'):", string(Min('a', 'z')))

    // MapKeys
    m := map[string]int{"a": 1, "b": 2, "c": 3}
    fmt.Println("\nMapKeys:", MapKeys(m))

    // 泛型Stack
    fmt.Println("\n=== 泛型Stack ===")
    intStack := Stack[int]{}
    intStack.Push(10)
    intStack.Push(20)
    intStack.Push(30)
    fmt.Printf("Stack大小: %d\n", intStack.Size())

    if val, ok := intStack.Pop(); ok {
        fmt.Println("Pop:", val)
    }
    if val, ok := intStack.Peek(); ok {
        fmt.Println("Peek:", val)
    }

    // 泛型HashSet
    fmt.Println("\n=== 泛型HashSet ===")
    set := NewHashSet[string]()
    set.Add("Go")
    set.Add("Java")
    set.Add("Python")
    set.Add("Go")  // 重复，不会添加
    fmt.Printf("Set大小: %d\n", set.Size())
    fmt.Println("包含Go?", set.Contains("Go"))
    fmt.Println("包含Rust?", set.Contains("Rust"))

    // 自定义约束
    fmt.Println("\nSum:", Sum([]int{1, 2, 3, 4, 5}))
    fmt.Println("Sum:", Sum([]float64{1.5, 2.5, 3.5}))

    // 泛型优势
    fmt.Println("\n=== 泛型 vs 反射 ===")
    fmt.Println("泛型: 编译期类型安全，性能好")
    fmt.Println("反射: 运行时类型检查，有性能开销")
    fmt.Println("建议: 优先使用泛型，反射用于框架开发")
}
```

**💡 代码解释**：Go 1.18引入泛型，是Go语言最大的变化之一。核心概念：(1)类型参数——[T any]声明类型参数；(2)类型约束——constraints.Ordered、comparable、any等；(3)类型推断——调用时通常可省略类型参数。泛型vs反射：泛型编译期类型安全、性能好；反射运行时灵活但有性能开销。建议优先使用泛型，反射用于框架开发。当前Go泛型还不支持方法上的类型参数。

**🔑 关键要点**：
- [T any]声明类型参数，constraints包提供常用约束
- comparable约束可比较类型，any是interface{}别名
- ~int约束包括int及其派生类型
- |表示类型并集，自定义约束接口
- 泛型编译期类型安全，优先于反射使用

---

### 6. 微服务框架与Raft算法

> **级别**：高级 | **概念**：Go在微服务领域广泛应用，掌握gRPC、Go-Zero等框架，理解Raft共识算法在分布式系统中的应用。

```go
// ============================================
// 【高级】Go 微服务框架与Raft共识算法
// ============================================

package main

import (
    "fmt"
    "sync"
    "time"
)

// --- Raft算法核心概念演示 ---

// Raft节点状态
type RaftState int

const (
    Follower  RaftState = iota  // 跟随者（默认状态）
    Candidate                    // 候选者（选举中）
    Leader                       // 领导者（唯一）
)

func (s RaftState) String() string {
    switch s {
    case Follower:
        return "Follower"
    case Candidate:
        return "Candidate"
    case Leader:
        return "Leader"
    default:
        return "Unknown"
    }
}

// Raft日志条目
type LogEntry struct {
    Term    int         // 任期号
    Command interface{} // 状态机命令
}

// Raft节点（简化版）
type RaftNode struct {
    mu sync.Mutex

    // 持久状态
    CurrentTerm int        // 当前任期
    VotedFor    int        // 投票给谁
    Log         []LogEntry // 日志条目

    // 易失状态
    State       RaftState // 节点状态
    CommitIndex int       // 已提交的日志索引
    LastApplied int       // 已应用到状态机的日志索引

    // Leader特有
    NextIndex  map[int]int // 每个follower的下一条日志索引
    MatchIndex map[int]int // 每个follower已复制的日志索引
}

// 创建Raft节点
func NewRaftNode() *RaftNode {
    return &RaftNode{
        State:       Follower,
        CurrentTerm: 0,
        VotedFor:    -1,
        CommitIndex: -1,
        LastApplied: -1,
        NextIndex:   make(map[int]int),
        MatchIndex:  make(map[int]int),
    }
}

// 选举超时（随机150-300ms）
func (rn *RaftNode) electionTimeout() time.Duration {
    return time.Duration(150+time.Now().UnixNano()%150) * time.Millisecond
}

// 心跳间隔
func (rn *RaftNode) heartbeatInterval() time.Duration {
    return 50 * time.Millisecond
}

// 开始选举
func (rn *RaftNode) StartElection() {
    rn.mu.Lock()
    defer rn.mu.Unlock()

    rn.State = Candidate
    rn.CurrentTerm++
    rn.VotedFor = 0  // 投票给自己
    fmt.Printf("[Term %d] 成为 Candidate，开始选举\n", rn.CurrentTerm)
}

// 成为Leader
func (rn *RaftNode) BecomeLeader() {
    rn.mu.Lock()
    defer rn.mu.Unlock()

    rn.State = Leader
    fmt.Printf("[Term %d] 成为 Leader\n", rn.CurrentTerm)

    // 初始化NextIndex和MatchIndex
    for i := 0; i < 3; i++ {
        rn.NextIndex[i] = len(rn.Log)
        rn.MatchIndex[i] = -1
    }
}

// 追加日志条目
func (rn *RaftNode) AppendEntries(term int, entries []LogEntry) bool {
    rn.mu.Lock()
    defer rn.mu.Unlock()

    if term < rn.CurrentTerm {
        return false
    }

    rn.CurrentTerm = term
    rn.State = Follower
    rn.Log = append(rn.Log, entries...)
    return true
}

// --- Raft演示 ---
func raftDemo() {
    fmt.Println("=== Raft 共识算法 ===")

    node := NewRaftNode()
    fmt.Printf("初始状态: %s\n", node.State)

    // 选举过程
    fmt.Println("\n--- 选举过程 ---")
    node.StartElection()
    fmt.Printf("选举后状态: %s\n", node.State)

    // 获得多数票，成为Leader
    node.BecomeLeader()
    fmt.Printf("成为Leader后状态: %s\n", node.State)

    // 日志复制
    fmt.Println("\n--- 日志复制 ---")
    entry := LogEntry{Term: node.CurrentTerm, Command: "SET key=value"}
    node.Log = append(node.Log, entry)
    fmt.Printf("添加日志: Term=%d, Command=%v\n", entry.Term, entry.Command)
    fmt.Printf("日志数量: %d\n", len(node.Log))

    // 提交日志
    fmt.Println("\n--- 提交与应用 ---")
    node.CommitIndex = len(node.Log) - 1
    fmt.Printf("提交索引: %d\n", node.CommitIndex)

    // 状态机应用
    for node.LastApplied < node.CommitIndex {
        node.LastApplied++
        cmd := node.Log[node.LastApplied].Command
        fmt.Printf("应用命令: %v\n", cmd)
    }
}

// --- 微服务框架概览 ---
func microserviceOverview() {
    fmt.Println("\n=== Go 微服务框架 ===")

    fmt.Println("\n【HTTP框架】")
    fmt.Println("Gin: 高性能HTTP框架，最流行")
    fmt.Println("Echo: 简约高性能，类似Gin")
    fmt.Println("Fiber: 类Express，基于fasthttp")

    fmt.Println("\n【微服务框架】")
    fmt.Println("Go-Zero: 集成了Web和RPC框架")
    fmt.Println("Go-Micro: 插件化微服务框架")
    fmt.Println("Kratos: B站开源微服务框架")
    fmt.Println("Kitex: 字节跳动高性能RPC框架")

    fmt.Println("\n【RPC框架】")
    fmt.Println("gRPC: Google开源，基于HTTP/2和Protobuf")
    fmt.Println("tRPC-Go: 腾讯开源RPC框架")

    fmt.Println("\n【服务治理】")
    fmt.Println("服务注册发现: etcd/Consul/Nacos")
    fmt.Println("配置中心: Apollo/Nacos/etcd")
    fmt.Println("链路追踪: Jaeger/Zipkin")
    fmt.Println("监控告警: Prometheus + Grafana")
    fmt.Println("限流熔断: Sentinel/hystrix-go")
}

func main() {
    raftDemo()
    microserviceOverview()

    fmt.Println("\n=== Raft应用场景 ===")
    fmt.Println("etcd: 分布式键值存储")
    fmt.Println("TiKV: 分布式事务键值存储")
    fmt.Println("Consul: 服务发现与配置")
    fmt.Println("Nacos: 注册中心与配置中心")

    fmt.Println("\n=== Raft vs Paxos ===")
    fmt.Println("Raft: 易于理解，工程实现友好")
    fmt.Println("Paxos: 理论完备，实现复杂")
    fmt.Println("Go生态中Raft占主导地位")
}
```

**💡 代码解释**：Go是云原生和微服务领域的主流语言。Raft是分布式共识算法，通过Leader选举、日志复制和安全性保证实现强一致性。Raft节点有三种状态：Follower(默认)、Candidate(选举)、Leader(唯一)。etcd、TiKV等核心基础设施都基于Raft实现。Go微服务生态成熟：Gin(HTTP)、Go-Zero(微服务)、gRPC(RPC)、etcd(注册发现)、Prometheus(监控)等。

**🔑 关键要点**：
- Raft三种状态：Follower → Candidate → Leader
- Leader选举：随机超时，多数投票
- 日志复制：Leader→Follower，多数确认后提交
- Go微服务框架：Go-Zero、Kratos、Kitex
- etcd/TiKV基于Raft实现分布式共识

---
