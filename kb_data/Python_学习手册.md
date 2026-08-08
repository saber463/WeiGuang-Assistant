# 🐍 Python 编程语言学习手册

> **分类**：后端  
> **描述**：Python后端开发知识库，涵盖从入门到高级的完整学习路径  
> **生成时间**：2026-08-07  

---

## 目录

- [初级入门](#primary)（8个知识点）
- [中级进阶](#intermediate)（8个知识点）
- [高级精通](#advanced)（6个知识点）

---

## 初级入门

### 1. 环境搭建与虚拟环境

> **级别**：初级 | **概念**：使用venv创建隔离的Python虚拟环境，用pip管理第三方依赖包，确保项目环境独立可控。

```python
# ============================================
# 【初级】Python 虚拟环境搭建与 pip 包管理
# 在终端中执行以下命令（非 Python 脚本）
# ============================================

# 1. 创建虚拟环境（在项目根目录执行）
# python -m venv venv
#    ↑ -m venv：调用Python内置的venv模块
#    ↑ venv：虚拟环境文件夹名称，可自定义

# 2. 激活虚拟环境
# Windows PowerShell:
#   .\venv\Scripts\Activate.ps1
#    ↑ 执行激活脚本，终端前缀会显示 (venv)
# Mac/Linux:
#   source venv/bin/activate
#    ↑ source 命令在当前shell中执行脚本

# 3. 安装第三方包
# pip install requests
#    ↑ pip：Python包管理工具
#    ↑ install：安装命令
#    ↑ requests：常用的HTTP请求库

# 4. 导出依赖列表（生成 requirements.txt）
# pip freeze > requirements.txt
#    ↑ freeze：列出所有已安装的包及版本号
#    ↑ >：将输出重定向到文件

# 5. 从文件批量安装依赖
# pip install -r requirements.txt
#    ↑ -r：从文件读取包列表进行安装

# 6. 退出虚拟环境
# deactivate
#    ↑ 退出虚拟环境，恢复系统默认Python环境

# 验证环境是否激活成功（Python代码）
import sys  # 导入系统模块，用于获取Python解释器路径
print(f"当前Python路径: {sys.executable}")
# ↑ sys.executable：当前运行的Python解释器的完整路径
print(f"Python版本: {sys.version}")
# ↑ sys.version：当前Python解释器的版本信息
```

**💡 代码解释**：本示例展示了Python虚拟环境的完整生命周期：创建、激活、安装依赖、导出依赖列表、退出。虚拟环境的核心价值在于隔离——每个项目拥有独立的Python解释器和依赖包，避免不同项目之间的版本冲突。

**🔑 关键要点**：
- venv是Python内置模块，无需额外安装
- 每个项目应有独立的虚拟环境
- requirements.txt是项目依赖的标准记录方式
- 激活虚拟环境后，pip install安装的包仅在当前环境生效

---

### 2. 变量与基本数据类型

> **级别**：初级 | **概念**：Python是动态类型语言，变量无需声明类型。掌握整数、浮点数、字符串、布尔值和None五种基本类型。

```python
# ============================================
# 【初级】Python 变量与基本数据类型
# ============================================

# --- 整数 (int) ---
age = 25  # 直接赋值，Python自动推断为int类型
print(f"年龄: {age}, 类型: {type(age)}")
# ↑ type()：查看变量的数据类型
# ↑ f"..."：f-string格式化字符串，{}内可嵌入表达式

# --- 浮点数 (float) ---
price = 19.99  # 带小数点的数字自动为float类型
print(f"价格: {price}, 类型: {type(price)}")

# --- 字符串 (str) ---
name = "小明"  # 双引号定义字符串
city = '北京'  # 单引号也可以定义字符串，两者等价
print(f"姓名: {name}, 城市: {city}")

# --- 布尔值 (bool) ---
is_active = True   # 布尔值只有True和False两个值，注意首字母大写
is_admin = False
print(f"激活状态: {is_active}, 是否管理员: {is_admin}")

# --- 空值 (None) ---
result = None  # None表示"没有值"或"未知"，类似于其他语言的null
print(f"结果: {result}, 是None吗? {result is None}")
# ↑ is：身份运算符，判断两个对象是否为同一个对象

# --- 类型转换 ---
num_str = "100"
num_int = int(num_str)  # int()：将字符串转为整数
print(f"字符串'{num_str}' 转为整数: {num_int}")

pi = 3.14
pi_int = int(pi)  # float转int会截断小数部分（非四舍五入）
print(f"浮点数{pi} 转为整数: {pi_int}")

# --- 动态类型特性 ---
x = 10       # x是int
print(f"x = {x}, 类型: {type(x).__name__}")
x = "hello"  # 同一变量可以赋不同类型的值
print(f"x = '{x}', 类型: {type(x).__name__}")
# ↑ Python是动态类型语言，变量只是对象的引用
```

**💡 代码解释**：本示例涵盖Python五种基本数据类型及其核心操作。Python的变量是对象的引用（标签），而非固定类型的内存槽位，因此同一变量可以在不同时刻指向不同类型的对象。type()函数用于运行时类型检查，f-string是Python 3.6+推荐的字符串格式化方式。

**🔑 关键要点**：
- Python是动态类型语言，变量无需声明类型
- int/float/str/bool/None是五种基本类型
- type()函数可查看变量类型
- int()/float()/str()等函数用于类型转换
- float转int会截断而非四舍五入

---

### 3. 字符串操作

> **级别**：初级 | **概念**：Python字符串是不可变序列，支持索引访问、切片、拼接、分割和丰富的内置方法进行文本处理。

```python
# ============================================
# 【初级】Python 字符串操作详解
# ============================================

text = "  Hello Python World  "

# --- 基本操作 ---
print(f"原始字符串: '{text}'")
print(f"长度: {len(text)}")  # len()：返回字符串的字符数
print(f"去除两端空格: '{text.strip()}'")
# ↑ strip()：去除首尾空白字符（空格、换行、制表符等）

# --- 字符串切片 ---
word = "Python编程"
print(f"前3个字符: '{word[0:3]}'")
# ↑ [0:3]：从索引0开始到索引3之前（不含3），即取第0,1,2个字符
print(f"后3个字符: '{word[-3:]}'")
# ↑ [-3:]：从倒数第3个字符开始，一直到末尾
print(f"反转字符串: '{word[::-1]}'")
# ↑ [::-1]：步长为-1，从右向左逐个取字符

# --- 大小写转换 ---
title = "python programming"
print(f"首字母大写: '{title.title()}'")
# ↑ title()：每个单词首字母大写
print(f"全大写: '{title.upper()}'")
print(f"全小写: '{title.lower()}'")

# --- 查找与替换 ---
message = "Python is great, Python is powerful"
print(f"'Python'出现次数: {message.count('Python')}")
# ↑ count()：统计子字符串出现的次数
print(f"'great'的起始位置: {message.find('great')}")
# ↑ find()：返回子字符串首次出现的索引，找不到返回-1
print(f"替换后: '{message.replace('Python', 'Java')}'")
# ↑ replace()：替换所有匹配的子字符串

# --- 分割与连接 ---
csv_data = "苹果,香蕉,橘子,葡萄"
fruits = csv_data.split(",")  # split()：按指定分隔符拆分为列表
print(f"分割结果: {fruits}")
joined = " | ".join(fruits)  # join()：用分隔符连接列表元素
print(f"连接结果: '{joined}'")

# --- 判断方法 ---
num_str = "12345"
print(f"'{num_str}' 是纯数字吗? {num_str.isdigit()}")
# ↑ isdigit()：判断字符串是否全部由数字组成
print(f"'{num_str}' 是纯字母吗? {num_str.isalpha()}")
```

**💡 代码解释**：字符串是Python中最常用的数据类型之一。关键特性：(1)不可变性——任何字符串操作都会返回新字符串，原字符串不变；(2)支持切片操作，语法为[start:end:step]；(3)内置丰富的方法，覆盖了大多数文本处理需求。split()和join()是处理格式化文本的黄金搭档。

**🔑 关键要点**：
- 字符串是不可变序列，修改操作返回新字符串
- 切片语法 [start:end:step] 非常灵活
- split()分割字符串为列表，join()连接列表为字符串
- find()找不到返回-1而非报错
- strip()/upper()/lower()/replace()是高频使用的方法

---

### 4. 列表与元组

> **级别**：初级 | **概念**：列表(list)是可变有序集合，支持增删改查；元组(tuple)是不可变有序集合，适合存储不应修改的数据。

```python
# ============================================
# 【初级】Python 列表与元组操作
# ============================================

# --- 列表 (list)：可变序列 ---
tasks = ["写代码", "测试", "部署"]  # 方括号定义列表
print(f"原始列表: {tasks}")

# 增 - 添加元素
tasks.append("写文档")  # append()：在末尾追加一个元素
print(f"append后: {tasks}")
tasks.insert(1, "评审")  # insert(索引, 元素)：在指定位置插入
print(f"insert后: {tasks}")
tasks.extend(["监控", "优化"])
# ↑ extend()：将另一个列表的元素逐个追加到末尾
print(f"extend后: {tasks}")

# 删 - 删除元素
removed = tasks.pop()  # pop()：移除并返回最后一个元素
print(f"pop移除: {removed}, 剩余: {tasks}")
tasks.remove("评审")  # remove()：按值删除第一个匹配项
print(f"remove后: {tasks}")

# 改 - 修改元素
tasks[0] = "重构代码"  # 通过索引直接赋值修改
print(f"修改索引0后: {tasks}")

# 查 - 访问元素
print(f"第一个任务: {tasks[0]}")   # 索引从0开始
print(f"最后一个任务: {tasks[-1]}")  # -1表示最后一个
print(f"前两个任务: {tasks[:2]}")    # 切片操作

# --- 元组 (tuple)：不可变序列 ---
point = (10, 20)  # 圆括号定义元组
print(f"\n坐标点: {point}, 类型: {type(point)}")
print(f"x坐标: {point[0]}")
# point[0] = 30  # 报错！元组不可修改

# 元组解包（unpacking）
x, y = point  # 一次性将元组元素赋值给多个变量
print(f"解包: x={x}, y={y}")

# 元组常用于函数返回多个值
def get_user_info():
    """返回用户信息的函数，演示元组作为返回值"""
    return "张三", 28, "北京"  # 逗号分隔的值会自动打包为元组

name, age, city = get_user_info()  # 直接解包
print(f"用户: {name}, 年龄: {age}, 城市: {city}")
```

**💡 代码解释**：列表和元组是Python最常用的序列类型。列表适合需要动态增删改查的场景（如任务队列），元组适合固定的数据集合（如坐标、配置项）。元组的不可变性使其可以作为字典的键，而列表不行。元组解包是Python的语法糖，让代码更简洁优雅。

**🔑 关键要点**：
- 列表用[]，元组用()
- 列表可变，支持append/insert/remove/pop等操作
- 元组不可变，适合存储不应修改的数据
- 元组解包可一次性赋值多个变量
- 函数返回多个值时自动打包为元组

---

### 5. 字典与集合

> **级别**：初级 | **概念**：字典(dict)是键值对映射结构，通过键快速查找值；集合(set)是无序不重复元素集合，支持数学集合运算。

```python
# ============================================
# 【初级】Python 字典与集合操作
# ============================================

# --- 字典 (dict)：键值对映射 ---
student = {
    "name": "李华",   # 键是字符串，值是字符串
    "age": 20,         # 键是字符串，值是整数
    "scores": [85, 90, 78],  # 值可以是任意类型
    "is_graduated": False
}
print(f"学生信息: {student}")

# 访问元素
print(f"姓名: {student['name']}")  # 直接通过键访问
print(f"年龄: {student.get('age', '未知')}")
# ↑ get()：安全访问，键不存在时返回默认值而非报错

# 修改与新增
student["age"] = 21  # 修改已有键的值
student["email"] = "lihua@example.com"  # 新增键值对
print(f"更新后: {student}")

# 遍历字典
print("\n遍历所有键值对:")
for key, value in student.items():
    # ↑ items()：返回键值对序列，可同时获取键和值
    print(f"  {key} -> {value}")

# 删除元素
removed_value = student.pop("is_graduated")
# ↑ pop()：删除指定键并返回其值
print(f"\n删除'is_graduated'后: {student}")

# --- 集合 (set)：无序不重复元素 ---
tags = {"Python", "编程", "后端", "Python"}
# ↑ 重复的"Python"会被自动去重
print(f"\n标签集合: {tags} (重复项已自动去重)")

# 集合运算
set_a = {1, 2, 3, 4}
set_b = {3, 4, 5, 6}
print(f"交集: {set_a & set_b}")   # &：两个集合都有的元素
print(f"并集: {set_a | set_b}")   # |：两个集合所有元素
print(f"差集: {set_a - set_b}")   # -：在set_a但不在set_b的元素

# 集合去重应用
numbers = [1, 2, 2, 3, 3, 3, 4, 4, 4, 4]
unique = list(set(numbers))  # 列表→集合（去重）→列表
print(f"\n原始列表: {numbers}")
print(f"去重后: {unique}")
```

**💡 代码解释**：字典基于哈希表实现，查找时间复杂度为O(1)，是Python后端开发中最核心的数据结构之一。集合同样基于哈希表，适合去重和成员检查场景。get()方法比直接[]访问更安全，因为键不存在时不会抛出KeyError异常。items()方法让遍历字典变得简洁。

**🔑 关键要点**：
- 字典是键值对映射，键必须不可变（字符串/数字/元组）
- get()安全访问，避免KeyError
- 集合自动去重，支持交并差运算
- 字典和集合都基于哈希表，查找为O(1)
- items()遍历键值对，keys()遍历键，values()遍历值

---

### 6. 条件判断与循环

> **级别**：初级 | **概念**：if-elif-else实现条件分支，for循环遍历可迭代对象，while实现条件循环，break/continue控制循环流程。

```python
# ============================================
# 【初级】Python 条件判断与循环控制
# ============================================

# --- 条件判断 if-elif-else ---
score = 85

if score >= 90:  # if：判断条件是否为True
    grade = "优秀"
elif score >= 80:  # elif：上一个条件为False时再判断
    grade = "良好"
elif score >= 60:
    grade = "及格"
else:  # else：所有条件都不满足时执行
    grade = "不及格"

print(f"分数: {score}, 等级: {grade}")

# --- for 循环：遍历可迭代对象 ---
fruits = ["苹果", "香蕉", "橘子"]
print("\n遍历水果列表:")
for i, fruit in enumerate(fruits):
    # ↑ enumerate()：同时获取索引和元素，i从0开始
    print(f"  [{i}] {fruit}")

# range() 生成数字序列
print("\nrange(5) 生成 0-4:")
for i in range(5):  # range(5)：生成0,1,2,3,4
    print(f"  {i}", end=" ")  # end=" "：打印后不换行，用空格分隔
print()  # 换行

# --- while 循环：条件为True时持续执行 ---
count = 0
print("\nwhile 循环示例:")
while count < 5:  # 当count小于5时循环
    print(f"  计数: {count}", end=" ")
    count += 1  # 自增1，等价于 count = count + 1
print()

# --- break 与 continue ---
print("\n查找列表中第一个偶数:")
numbers = [1, 3, 5, 6, 7, 9]
for num in numbers:
    if num % 2 == 0:  # 判断是否为偶数
        print(f"  找到偶数: {num}")
        break  # break：立即终止整个循环

print("\n跳过奇数，只打印偶数:")
for num in numbers:
    if num % 2 != 0:  # 判断是否为奇数
        continue  # continue：跳过本次循环剩余代码，进入下一次
    print(f"  偶数: {num}")
```

**💡 代码解释**：条件判断和循环是程序控制流的核心。Python使用缩进而非大括号定义代码块，这一点必须牢记。for循环配合range()和enumerate()覆盖了大多数遍历场景。while循环适合循环次数不确定的场景。break用于提前退出循环，continue用于跳过当前迭代。

**🔑 关键要点**：
- Python使用缩进定义代码块，通常4个空格
- if-elif-else结构从上到下依次判断，匹配即停止
- for循环遍历可迭代对象，range()生成数字序列
- enumerate()同时获取索引和元素值
- break终止循环，continue跳过本次迭代

---

### 7. 函数定义与参数

> **级别**：初级 | **概念**：用def关键字定义函数，支持位置参数、默认参数、关键字参数和可变参数，return返回值。

```python
# ============================================
# 【初级】Python 函数定义与参数类型
# ============================================

# --- 基本函数定义 ---
def greet(name):
    """向指定的人打招呼的函数。

    Args:
        name: 要打招呼的人的名字

    Returns:
        格式化后的问候语字符串
    """
    # ↑ 三引号字符串是函数文档字符串（docstring），用于描述函数用途
    return f"你好，{name}！欢迎学习Python！"

# 调用函数
print(greet("小明"))

# --- 默认参数 ---
def calculate_price(price, discount=0.9, tax=0.06):
    """计算最终价格。

    Args:
        price: 原价
        discount: 折扣，默认0.9（9折）
        tax: 税率，默认0.06（6%）
    """
    discounted = price * discount  # 折后价
    final = discounted * (1 + tax)  # 加税后最终价
    return round(final, 2)  # round()：四舍五入保留2位小数

print(f"默认参数: {calculate_price(100)}")
print(f"自定义折扣: {calculate_price(100, discount=0.8)}")
print(f"全部指定: {calculate_price(100, discount=0.7, tax=0.05)}")

# --- 可变参数 *args ---
def sum_all(*args):
    """计算任意数量参数的总和。

    *args 将所有位置参数打包为一个元组
    """
    total = 0
    for num in args:  # args是元组，可以遍历
        total += num
    return total

print(f"\n求和: sum_all(1,2,3) = {sum_all(1, 2, 3)}")
print(f"求和: sum_all(1,2,3,4,5) = {sum_all(1, 2, 3, 4, 5)}")

# --- 关键字参数 **kwargs ---
def build_profile(**kwargs):
    """构建用户档案。

    **kwargs 将所有关键字参数打包为一个字典
    """
    profile = {}
    for key, value in kwargs.items():
        profile[key] = value
    return profile

user = build_profile(name="张三", age=30, city="上海")
print(f"\n用户档案: {user}")
```

**💡 代码解释**：函数是代码复用的基本单元。Python函数支持多种参数传递方式：(1)位置参数按顺序传递；(2)默认参数在定义时提供默认值；(3)*args接收任意数量的位置参数；(4)**kwargs接收任意数量的关键字参数。docstring是Python的文档约定，建议每个函数都编写。

**🔑 关键要点**：
- def关键字定义函数，return返回值
- 默认参数必须放在位置参数之后
- *args将多余位置参数打包为元组
- **kwargs将多余关键字参数打包为字典
- docstring（三引号注释）是函数文档的标准写法

---

### 8. 文件读写与异常处理

> **级别**：初级 | **概念**：使用open()函数读写文件，with语句自动管理资源；try-except捕获异常，确保程序健壮运行。

```python
# ============================================
# 【初级】Python 文件读写与异常处理
# ============================================

import os  # 导入os模块，用于文件和路径操作

# --- 文件写入 ---
file_path = "test_data.txt"

try:
    # with语句：自动关闭文件，即使发生异常也能保证资源释放
    with open(file_path, "w", encoding="utf-8") as f:
        # ↑ open(路径, 模式, 编码)
        # ↑ "w"：写入模式（会覆盖原有内容）
        # ↑ encoding="utf-8"：指定UTF-8编码，支持中文
        f.write("第一行：Python文件操作\n")
        f.write("第二行：自动关闭文件\n")
        f.write("第三行：异常处理保障\n")
    print(f"文件写入成功: {file_path}")

except PermissionError:
    # ↑ 捕获权限不足异常
    print("错误：没有写入权限！")
except Exception as e:
    # ↑ 捕获其他所有异常，e是异常对象
    print(f"写入失败: {type(e).__name__}: {e}")

# --- 文件读取 ---
try:
    with open(file_path, "r", encoding="utf-8") as f:
        # ↑ "r"：读取模式（默认模式）
        content = f.read()  # read()：一次性读取全部内容
        print(f"\n文件内容:\n{content}")

except FileNotFoundError:
    # ↑ 捕获文件不存在异常
    print(f"错误：文件 '{file_path}' 不存在！")

# --- 逐行读取 ---
try:
    with open(file_path, "r", encoding="utf-8") as f:
        print("逐行读取:")
        for line_num, line in enumerate(f, 1):
            # ↑ enumerate(f, 1)：从1开始计数行号
            # ↑ 直接遍历文件对象，逐行读取，内存友好
            print(f"  第{line_num}行: {line.strip()}")
            # ↑ strip()：去除行尾的换行符

except FileNotFoundError:
    print(f"错误：文件 '{file_path}' 不存在！")

# --- 清理测试文件 ---
if os.path.exists(file_path):
    # ↑ os.path.exists()：检查文件是否存在
    os.remove(file_path)  # 删除文件
    print(f"\n测试文件已清理: {file_path}")
```

**💡 代码解释**：文件操作和异常处理是后端开发的基本功。with语句是Python的上下文管理器，确保文件在使用后自动关闭。try-except-else-finally构成完整的异常处理链。逐行读取文件比一次性读取更节省内存，适合处理大文件。使用encoding='utf-8'可避免中文编码问题。

**🔑 关键要点**：
- with open()自动管理文件资源，无需手动close()
- 文件模式：'r'读、'w'写（覆盖）、'a'追加
- utf-8编码确保中文正常读写
- try-except捕获特定异常，避免程序崩溃
- 逐行遍历文件对象比read()更省内存

---

## 中级进阶

### 1. 列表推导式与生成器表达式

> **级别**：中级 | **概念**：列表推导式用简洁语法创建列表，可包含条件过滤；生成器表达式惰性求值，节省内存。

```python
# ============================================
# 【中级】Python 列表推导式与生成器表达式
# ============================================

import sys  # 导入sys模块，用于获取对象内存大小

# --- 列表推导式：一行代码生成列表 ---
# 传统写法：循环创建列表
squares_old = []
for i in range(10):
    squares_old.append(i ** 2)  # ** 是幂运算，i**2 即 i的平方

# 列表推导式写法：更简洁、更Pythonic
squares = [i ** 2 for i in range(10)]
# ↑ 语法：[表达式 for 变量 in 可迭代对象]
print(f"平方数列表: {squares}")

# --- 带条件过滤的列表推导式 ---
even_squares = [i ** 2 for i in range(10) if i % 2 == 0]
# ↑ 末尾的 if 是过滤条件，只保留满足条件的元素
print(f"偶数的平方: {even_squares}")

# --- 嵌套循环的列表推导式 ---
pairs = [(x, y) for x in range(3) for y in range(2)]
# ↑ 等价于双重for循环，x为外层，y为内层
print(f"坐标对: {pairs}")

# --- 生成器表达式：惰性求值，节省内存 ---
# 将方括号换成圆括号即变为生成器表达式
squares_gen = (i ** 2 for i in range(1000000))
# ↑ 生成器表达式不会立即计算所有值，而是按需生成

# 对比内存占用
squares_list = [i ** 2 for i in range(1000000)]
print(f"\n列表占用内存: {sys.getsizeof(squares_list) // 1024} KB")
print(f"生成器占用内存: {sys.getsizeof(squares_gen)} bytes")
# ↑ 生成器仅占用固定内存，不随数据量增长

# 使用生成器：逐个消费
print("\n生成器前5个值:")
for i, val in enumerate(squares_gen):
    if i >= 5:
        break  # 只取前5个，剩余值不会被生成
    print(f"  {val}", end=" ")
print()

# --- 字典推导式 ---
word = "hello world"
# 统计每个字符出现的次数（排除空格）
char_count = {ch: word.count(ch) for ch in set(word) if ch != " "}
# ↑ {键: 值 for 变量 in 可迭代对象 if 条件}
print(f"\n字符统计: {char_count}")
```

**💡 代码解释**：列表推导式是Python的标志性语法，它将循环和条件判断压缩到一行。生成器表达式是列表推导式的惰性版本，只在需要时才计算下一个值，大幅节省内存。字典推导式和集合推导式也遵循相同的语法模式。对于百万级数据，生成器表达式是更优选择。

**🔑 关键要点**：
- 列表推导式语法：[表达式 for 变量 in 可迭代对象 if 条件]
- 生成器表达式用()替代[]，惰性求值
- 生成器适合处理大数据集，内存占用极低
- 字典推导式：{键:值 for ... in ...}
- 生成器只能消费一次，用完后无法重新迭代

---

### 2. 装饰器原理与实践

> **级别**：中级 | **概念**：装饰器是接受函数作为参数并返回新函数的高阶函数，用于在不修改原函数的情况下添加额外功能。

```python
# ============================================
# 【中级】Python 装饰器原理与实践
# ============================================

import time  # 导入time模块，用于获取时间戳
import functools  # 导入functools模块，用于保留函数元信息

# --- 装饰器基础：函数即对象 ---
def make_bold(func):
    """将函数返回值包装为粗体标记的装饰器。"""
    @functools.wraps(func)
    # ↑ @wraps：保留被装饰函数的名称、文档字符串等元信息
    def wrapper(*args, **kwargs):
        # ↑ *args和**kwargs确保装饰器适用于任意参数的函数
        result = func(*args, **kwargs)  # 调用原函数
        return f"<b>{result}</b>"  # 在返回值前后添加标签
    return wrapper  # 返回包装后的函数

@make_bold  # @语法糖：等价于 greet = make_bold(greet)
def greet(name):
    """返回问候语。"""
    return f"你好，{name}"

print(greet("小明"))  # 输出: <b>你好，小明</b>
print(f"函数名: {greet.__name__}")
# ↑ 因为有@wraps，函数名仍是greet而非wrapper

# --- 计时装饰器：统计函数执行时间 ---
def timer(func):
    """统计函数执行时间的装饰器。"""
    @functools.wraps(func)
    def wrapper(*args, **kwargs):
        start = time.perf_counter()
        # ↑ perf_counter()：高精度计时器，适合测量短时间间隔
        result = func(*args, **kwargs)
        elapsed = time.perf_counter() - start
        print(f"[{func.__name__}] 执行耗时: {elapsed:.4f}秒")
        return result
    return wrapper

@timer
def slow_sum(n):
    """模拟耗时操作。"""
    total = sum(range(n))
    time.sleep(0.5)  # 模拟IO等待
    return total

print(f"\n计算结果: {slow_sum(100000)}")

# --- 带参数的装饰器：三层嵌套 ---
def repeat(n):
    """返回一个装饰器，使函数重复执行n次。

    这是"装饰器工厂"模式：外层函数接收参数，返回真正的装饰器。
    """
    def decorator(func):  # 真正的装饰器
        @functools.wraps(func)
        def wrapper(*args, **kwargs):
            results = []
            for _ in range(n):
                results.append(func(*args, **kwargs))
            return results
        return wrapper
    return decorator

@repeat(3)  # 调用repeat(3)返回装饰器，再装饰函数
# ↑ 等价于：say_hello = repeat(3)(say_hello)
def say_hello():
    return "Hello!"

print(f"\n重复执行结果: {say_hello()}")
```

**💡 代码解释**：装饰器是Python AOP（面向切面编程）的核心实现。理解装饰器需要先理解闭包：内部函数可以访问外部函数的变量。@functools.wraps是装饰器编写的最佳实践，它将被装饰函数的元信息复制到wrapper函数上。三层装饰器（带参数的装饰器）实际上是装饰器工厂模式。

**🔑 关键要点**：
- 装饰器本质是接受函数、返回函数的高阶函数
- @语法糖简化装饰器的使用
- @functools.wraps保留原函数的元信息
- 带参数的装饰器需要三层函数嵌套
- 装饰器常用于日志、计时、权限校验、缓存等场景

---

### 3. 生成器与yield关键字

> **级别**：中级 | **概念**：yield让函数变为生成器，可暂停执行并返回中间值，实现惰性迭代和流式数据处理。

```python
# ============================================
# 【中级】Python 生成器与yield关键字
# ============================================

# --- 基本生成器：yield暂停函数执行 ---
def count_up_to(n):
    """生成从1到n的数字序列。

    每次调用yield时，函数暂停并返回值；
    下次迭代时从暂停点继续执行。
    """
    i = 1
    while i <= n:
        yield i  # yield：暂停函数，返回当前值
        # ↑ 函数状态（局部变量、执行位置）被保存
        i += 1

counter = count_up_to(5)
print(f"生成器类型: {type(counter)}")
print("逐个取值:", end=" ")
for num in counter:
    print(num, end=" ")
print()

# --- 使用next()逐一手动获取 ---
counter2 = count_up_to(3)
print(f"\nnext()调用: {next(counter2)}")
# ↑ next()：手动获取生成器的下一个值
print(f"next()调用: {next(counter2)}")
print(f"next()调用: {next(counter2)}")
# 如果再调用next(counter2)会抛出StopIteration异常

# --- 生成器实现斐波那契数列（无限序列） ---
def fibonacci():
    """生成无限斐波那契数列。

    生成器可以表示无限序列，因为值是按需生成的。
    """
    a, b = 0, 1
    while True:  # 无限循环
        yield a
        a, b = b, a + b  # 同时更新a和b

fib = fibonacci()
print("\n斐波那契数列前10项:", end=" ")
for _ in range(10):
    print(next(fib), end=" ")
print()

# --- 生成器实现文件流式读取 ---
def read_large_file(file_path, chunk_size=1024):
    """流式读取大文件，避免一次性加载到内存。

    适用于处理GB级别的日志文件等场景。
    """
    with open(file_path, "r", encoding="utf-8") as f:
        while True:
            chunk = f.read(chunk_size)
            # ↑ 每次只读取chunk_size个字符
            if not chunk:
                break  # 读取完毕
            yield chunk  # 逐块产出数据

# --- yield from：委托子生成器 ---
def chain_generators():
    """使用yield from串联多个生成器。"""
    yield from range(3)  # 委托给range生成器
    yield from [10, 20, 30]  # 委托给列表迭代器
    yield from "abc"  # 委托给字符串迭代器

print(f"\nyield from串联: {list(chain_generators())}")
```

**💡 代码解释**：生成器是Python处理流式数据和大型数据集的利器。yield关键字将普通函数变为生成器函数，每次调用yield时函数暂停并保存状态。生成器实现了迭代器协议，支持for循环和next()调用。yield from是Python 3.3+的特性，可以方便地将迭代委托给子生成器，简化嵌套生成器的代码。

**🔑 关键要点**：
- yield让函数变为生成器，暂停执行并保存状态
- 生成器按需生成值，内存占用极小
- next()手动获取下一个值，耗尽时抛出StopIteration
- 生成器可以表示无限序列
- yield from简化子生成器的委托调用

---

### 4. 上下文管理器

> **级别**：中级 | **概念**：上下文管理器通过__enter__和__exit__方法实现资源自动管理，可用with语句或contextlib装饰器创建。

```python
# ============================================
# 【中级】Python 上下文管理器
# ============================================

from contextlib import contextmanager
# ↑ contextmanager：将生成器函数转为上下文管理器的装饰器
import time

# --- 方式一：类实现上下文管理器 ---
class DatabaseConnection:
    """模拟数据库连接，演示上下文管理器类实现。

    __enter__在进入with块时调用，__exit__在退出时调用。
    """

    def __init__(self, db_name):
        """构造函数：初始化连接参数。"""
        self.db_name = db_name
        self.connected = False

    def __enter__(self):
        """进入上下文：建立连接，返回资源对象。"""
        print(f"[连接] 正在连接数据库 '{self.db_name}'...")
        self.connected = True
        time.sleep(0.3)  # 模拟连接耗时
        print(f"[连接] 数据库 '{self.db_name}' 连接成功")
        return self  # 返回的对象赋值给 as 后的变量

    def __exit__(self, exc_type, exc_val, exc_tb):
        """退出上下文：关闭连接，处理异常。

        Args:
            exc_type: 异常类型（无异常时为None）
            exc_val: 异常值
            exc_tb: 异常回溯信息
        """
        print(f"[断开] 正在断开数据库 '{self.db_name}'...")
        self.connected = False
        if exc_type:
            print(f"[警告] 发生异常: {exc_type.__name__}: {exc_val}")
        # 返回 False 会重新抛出异常，返回 True 则抑制异常
        return False

    def query(self, sql):
        """模拟查询操作。"""
        if not self.connected:
            raise RuntimeError("数据库未连接！")
        print(f"[查询] 执行SQL: {sql}")

# 使用自定义上下文管理器
print("=== 正常使用 ===")
with DatabaseConnection("test_db") as db:
    # ↑ __enter__的返回值赋给db
    db.query("SELECT * FROM users")
print("with块已退出，连接已自动关闭\n")

# --- 方式二：contextmanager装饰器实现 ---
@contextmanager
def timed_operation(name):
    """计时上下文管理器：用生成器函数实现。

    yield之前的代码是__enter__逻辑，
    yield之后的代码是__exit__逻辑。
    """
    print(f"[{name}] 开始操作...")
    start = time.perf_counter()
    try:
        yield  # 在此处暂停，将控制权交给with块
    finally:
        # ↑ finally：无论是否发生异常都会执行
        elapsed = time.perf_counter() - start
        print(f"[{name}] 操作完成，耗时: {elapsed:.4f}秒")

print("=== 计时上下文管理器 ===")
with timed_operation("数据处理"):
    time.sleep(0.5)  # 模拟数据处理
    print("  处理中...")
print("计时上下文已退出")
```

**💡 代码解释**：上下文管理器确保资源（文件、数据库连接、锁等）在使用后正确释放，即使发生异常也能保证清理。类实现方式适合复杂场景，contextmanager装饰器适合简单场景。__exit__返回True会抑制异常，返回False（默认）会重新抛出异常。Python的with语句比try-finally更简洁优雅。

**🔑 关键要点**：
- __enter__在with块开始时调用，返回资源对象
- __exit__在with块结束时调用，负责清理资源
- contextmanager装饰器将生成器转为上下文管理器
- yield之前是进入逻辑，之后是退出逻辑
- 上下文管理器保证资源释放，即使发生异常

---

### 5. 面向对象编程（类、继承、多态）

> **级别**：中级 | **概念**：Python支持完整的面向对象编程：类封装数据和行为，继承实现代码复用，多态让不同类响应相同方法。

```python
# ============================================
# 【中级】Python 面向对象编程：类、继承、多态
# ============================================

from abc import ABC, abstractmethod
# ↑ ABC：抽象基类，abstractmethod：抽象方法装饰器

# --- 基类定义 ---
class Animal(ABC):
    """动物抽象基类。

    所有动物的通用属性和行为定义在此。
    """
    # 类变量：所有实例共享
    kingdom = "动物界"

    def __init__(self, name, age):
        """实例初始化方法（构造函数）。

        self指向当前实例对象。
        """
        self.name = name  # 实例变量：每个实例独立
        self.__age = age  # 双下划线开头：私有属性（名称改写）

    @abstractmethod
    def make_sound(self):
        """抽象方法：子类必须实现。"""
        pass

    def get_age(self):
        """公开方法：访问私有属性。"""
        return self.__age

    def __str__(self):
        """魔法方法：定义对象的字符串表示。"""
        return f"{self.name}({self.__age}岁)"

# --- 子类继承 ---
class Dog(Animal):
    """狗类：继承自Animal。"""

    species = "犬科"  # 子类自有属性

    def __init__(self, name, age, breed):
        """子类构造函数。

        super()调用父类构造函数。
        """
        super().__init__(name, age)  # 调用父类__init__
        self.breed = breed  # 子类特有属性

    def make_sound(self):
        """实现抽象方法（多态）。"""
        return "汪汪！"

    def fetch(self):
        """子类特有方法。"""
        return f"{self.name} 去捡球了"

class Cat(Animal):
    """猫类：继承自Animal。"""

    def __init__(self, name, age, color):
        super().__init__(name, age)
        self.color = color

    def make_sound(self):
        """实现抽象方法（多态）。"""
        return "喵喵～"

# --- 多态演示 ---
def animal_chorus(animals):
    """多态函数：接受任何Animal子类，调用各自的方法。

    同一个接口，不同对象有不同的行为表现。
    """
    for animal in animals:
        print(f"{animal}: {animal.make_sound()}")

# 创建实例
animals = [
    Dog("旺财", 3, "金毛"),
    Cat("咪咪", 2, "橘色"),
]

print("=== 动物合唱（多态演示） ===")
animal_chorus(animals)
print(f"\n旺财的行为: {animals[0].fetch()}")
print(f"所有动物都属于: {Animal.kingdom}")
```

**💡 代码解释**：Python的面向对象编程基于类（class）和实例（instance）。三大特性：(1)封装——用私有属性（__前缀）隐藏内部实现；(2)继承——子类复用父类代码，super()调用父类方法；(3)多态——不同子类实现相同的抽象方法，表现出不同行为。ABC模块用于定义接口规范，确保子类实现必需的方法。

**🔑 关键要点**：
- __init__是构造函数，self指向实例本身
- 双下划线前缀(__age)实现私有属性（名称改写）
- super()调用父类方法，支持多重继承的MRO
- 抽象基类(ABC)定义接口规范，子类必须实现
- 多态：同一接口，不同对象表现不同行为

---

### 6. lambda、map、filter

> **级别**：中级 | **概念**：lambda创建匿名函数，map对序列每个元素应用函数，filter按条件过滤序列，三者组合实现函数式编程。

```python
# ============================================
# 【中级】Python lambda、map、filter 函数式编程
# ============================================

# --- lambda 匿名函数 ---
# 传统函数定义
def square(x):
    return x ** 2

# lambda等价写法：lambda 参数: 返回值
square_lambda = lambda x: x ** 2
# ↑ lambda关键字：创建匿名函数
# ↑ x：参数（可以有多个，用逗号分隔）
# ↑ x ** 2：函数体（只能是一个表达式）

print(f"传统函数: {square(5)}")
print(f"lambda: {square_lambda(5)}")

# lambda作为排序键
data = [("张三", 85), ("李四", 92), ("王五", 78)]
sorted_data = sorted(data, key=lambda item: item[1], reverse=True)
# ↑ key=lambda item: item[1]：按元组第二个元素（成绩）排序
# ↑ reverse=True：降序排列
print(f"\n按成绩排名: {sorted_data}")

# --- map：对每个元素应用函数 ---
numbers = [1, 2, 3, 4, 5]

# map返回迭代器，需要用list()转换查看
squared = list(map(lambda x: x ** 2, numbers))
# ↑ map(函数, 可迭代对象)：对每个元素调用函数，返回结果迭代器
print(f"\nmap平方: {squared}")

# 多个可迭代对象同时传入map
list_a = [1, 2, 3]
list_b = [10, 20, 30]
summed = list(map(lambda x, y: x + y, list_a, list_b))
# ↑ map可以接受多个可迭代对象，分别传给函数的多个参数
print(f"map两两相加: {summed}")

# --- filter：按条件过滤 ---
# 过滤出偶数
even_nums = list(filter(lambda x: x % 2 == 0, numbers))
# ↑ filter(判断函数, 可迭代对象)：保留函数返回True的元素
print(f"\nfilter偶数: {even_nums}")

# 过滤空字符串
words = ["hello", "", "world", "", "python"]
non_empty = list(filter(lambda w: w.strip(), words))
# ↑ strip()后非空即为True，空字符串为False
print(f"filter非空: {non_empty}")

# --- 组合使用：map + filter ---
# 将偶数平方后转为字符串
result = list(
    map(
        lambda x: f"平方:{x}",
        filter(lambda x: x % 2 == 0, numbers)
    )
)
# ↑ 先filter过滤偶数，再map格式化字符串
print(f"\nmap+filter组合: {result}")

# --- 列表推导式 vs map/filter ---
# 列表推导式通常更可读
result_lc = [f"平方:{x}" for x in numbers if x % 2 == 0]
print(f"列表推导式等价: {result_lc}")
```

**💡 代码解释**：lambda、map、filter是Python函数式编程的三大支柱。lambda适合定义简单的临时函数，避免创建完整的def函数。map和filter返回迭代器（惰性求值），在Python 3中是内存友好的。但要注意：列表推导式和生成器表达式通常比map/filter更Pythonic、更可读，是Python社区推荐的首选方式。

**🔑 关键要点**：
- lambda创建匿名函数，只能包含一个表达式
- map(函数, 可迭代对象)对每个元素应用函数
- filter(判断函数, 可迭代对象)保留True的元素
- map和filter返回惰性迭代器，用list()转换
- 列表推导式通常比map/filter更Pythonic

---

### 7. 常用标准库（os/json/datetime/re）

> **级别**：中级 | **概念**：Python标准库提供文件操作、JSON序列化、日期时间和正则表达式等核心功能，无需额外安装即可使用。

```python
# ============================================
# 【中级】Python 常用标准库：os/json/datetime/re
# ============================================

import os       # 操作系统接口：文件和目录操作
import json     # JSON处理：序列化与反序列化
import datetime # 日期时间处理
import re       # 正则表达式：模式匹配与文本处理

# --- os模块：文件和目录操作 ---
print("=== os 模块 ===")
current_dir = os.getcwd()  # getcwd()：获取当前工作目录
print(f"当前目录: {current_dir}")

# 列出目录内容
items = os.listdir(current_dir)  # listdir()：列出目录下的文件和文件夹
print(f"目录内容（前5个）: {items[:5]}")

# 路径拼接（跨平台兼容）
config_path = os.path.join(current_dir, "config", "app.json")
# ↑ join()：自动使用系统正确的路径分隔符（Windows用\，Linux用/）
print(f"配置路径: {config_path}")

# 判断路径是否存在
print(f"当前目录存在吗? {os.path.exists(current_dir)}")

# --- json模块：数据序列化 ---
print("\n=== json 模块 ===")
user_data = {
    "name": "张三",
    "age": 28,
    "skills": ["Python", "Java", "Go"],
    "active": True
}

# Python对象 → JSON字符串
json_str = json.dumps(user_data, ensure_ascii=False, indent=2)
# ↑ dumps()：序列化为JSON字符串
# ↑ ensure_ascii=False：正确显示中文（不转义为\uXXXX）
# ↑ indent=2：缩进2空格，格式化输出
print(f"JSON序列化:\n{json_str}")

# JSON字符串 → Python对象
parsed = json.loads(json_str)  # loads()：反序列化JSON字符串
print(f"\n反序列化后类型: {type(parsed)}, 姓名: {parsed['name']}")

# --- datetime模块：日期时间处理 ---
print("\n=== datetime 模块 ===")
now = datetime.datetime.now()  # now()：获取当前日期时间
print(f"当前时间: {now}")
print(f"格式化: {now.strftime('%Y-%m-%d %H:%M:%S')}")
# ↑ strftime()：将时间对象格式化为字符串
# ↑ %Y：四位年份，%m：月份，%d：日期，%H：小时，%M：分钟，%S：秒

# 时间计算
future = now + datetime.timedelta(days=30)
# ↑ timedelta：时间差，days=30表示30天后
print(f"30天后: {future.strftime('%Y-%m-%d')}")

# --- re模块：正则表达式 ---
print("\n=== re 模块 ===")
text = "联系方式：邮箱 zhangsan@example.com，电话 138-1234-5678"

# 提取邮箱
email_pattern = r'[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}'
# ↑ 正则表达式：匹配标准邮箱格式
email_match = re.search(email_pattern, text)
# ↑ search()：在字符串中搜索第一个匹配
if email_match:
    print(f"邮箱: {email_match.group()}")

# 提取所有数字
numbers = re.findall(r'\d+', text)
# ↑ findall()：查找所有匹配，返回列表
# ↑ \d+：匹配一个或多个数字
print(f"所有数字: {numbers}")

# 替换敏感信息
masked = re.sub(r'\d{3}-\d{4}', '***-****', text)
# ↑ sub()：替换匹配的内容
# ↑ \d{3}：恰好3个数字
print(f"脱敏后: {masked}")
```

**💡 代码解释**：这四个模块是Python后端开发中最常用的标准库。os模块处理文件路径和系统操作，注意使用os.path.join()而非字符串拼接以确保跨平台兼容。json模块是API开发的核心，ensure_ascii=False确保中文正常显示。datetime处理时间计算时注意时区问题。re模块的正则表达式功能强大但需注意性能。

**🔑 关键要点**：
- os.path.join()跨平台路径拼接，避免手动拼接分隔符
- json.dumps()的ensure_ascii=False正确显示中文
- strftime()格式化日期，strptime()解析日期字符串
- timedelta用于时间加减计算
- re.search()返回第一个匹配，re.findall()返回所有匹配

---

### 8. 类型注解（Type Hints）

> **级别**：中级 | **概念**：Python 3.5+支持类型注解，配合mypy静态检查工具，可在不牺牲动态特性的前提下提升代码可读性和可靠性。

```python
# ============================================
# 【中级】Python 类型注解（Type Hints）
# ============================================

from typing import List, Dict, Optional, Union, Tuple, Callable
# ↑ typing模块：提供复合类型的注解

# --- 基本类型注解 ---
def greet(name: str) -> str:
    # ↑ name: str：参数name应为字符串类型
    # ↑ -> str：函数返回值为字符串类型
    """带类型注解的问候函数。"""
    return f"你好，{name}！"

print(greet("小明"))
# greet(123)  # mypy会报错，但运行时不会阻止

# --- 复合类型注解 ---
def process_scores(
    students: List[str],           # 字符串列表
    scores: Dict[str, float],      # 键为str，值为float的字典
    threshold: Optional[float] = None  # Optional[X] = Union[X, None]
) -> Tuple[int, float]:
    # ↑ -> Tuple[int, float]：返回包含两个元素的元组
    """处理学生成绩，返回及格人数和平均分。"""
    if threshold is None:
        threshold = 60.0

    passed = [s for name, s in scores.items() if s >= threshold]
    count = len(passed)
    avg = sum(passed) / count if count > 0 else 0.0
    return count, round(avg, 2)

students = ["张三", "李四", "王五"]
scores = {"张三": 85.0, "李四": 55.0, "王五": 92.0}
result = process_scores(students, scores, threshold=60.0)
print(f"及格人数: {result[0]}, 平均分: {result[1]}")

# --- 函数类型注解 ---
def apply_transform(
    data: List[int],
    transform: Callable[[int], int]
    # ↑ Callable[[参数类型], 返回值类型]
) -> List[int]:
    """对数据列表应用转换函数。"""
    return [transform(x) for x in data]

result = apply_transform([1, 2, 3], lambda x: x * 2)
print(f"\n转换结果: {result}")

# --- 自定义类型别名 ---
# 为复杂类型定义别名，提高可读性
UserId = int  # 类型别名
JsonDict = Dict[str, Union[str, int, float, bool, None]]
# ↑ Union[]：表示可以是多种类型之一

def get_user(user_id: UserId) -> JsonDict:
    """根据用户ID获取用户信息（Mock）。"""
    return {
        "id": user_id,
        "name": "张三",
        "age": 28,
        "active": True
    }

user = get_user(1001)
print(f"\n用户信息: {user}")

# --- 使用 __future__ 注解（Python 3.10+ 可用新语法） ---
# Python 3.10+ 可以用 list[str] 代替 List[str]
# Python 3.10+ 可以用 str | None 代替 Optional[str]
```

**💡 代码解释**：类型注解是Python迈向大型项目的重要特性。它不会在运行时强制类型检查（Python仍是动态语言），但配合mypy等静态检查工具，可以在开发阶段发现类型错误。typing模块提供了丰富的类型构造器：List、Dict、Optional、Union、Callable等。类型别名可简化复杂类型注解，提高代码可读性。

**🔑 关键要点**：
- 类型注解不强制运行时检查，需配合mypy使用
- -> 标注返回值类型，: 标注参数类型
- Optional[X] 等价于 Union[X, None]
- Callable[[参数类型], 返回值类型] 标注函数类型
- Python 3.10+支持更简洁的 list[str] 和 X | None 语法

---

## 高级精通

### 1. 异步编程（asyncio）

> **级别**：高级 | **概念**：asyncio是Python的异步I/O框架，使用async/await语法实现协程并发，适合IO密集型任务，大幅提升吞吐量。

```python
# ============================================
# 【高级】Python 异步编程（asyncio）
# ============================================

import asyncio  # 异步I/O框架
import time     # 用于对比同步和异步耗时

# --- 异步函数（协程）定义 ---
async def fetch_data(source: str, delay: float) -> str:
    """模拟异步获取数据。

    async def：定义协程函数。
    await：暂停当前协程，等待异步操作完成。
    """
    print(f"[{source}] 开始获取数据...")
    await asyncio.sleep(delay)  # 模拟IO等待
    # ↑ await：将控制权交还给事件循环，让其他协程运行
    # ↑ asyncio.sleep()：异步版本的sleep，不会阻塞整个线程
    print(f"[{source}] 数据获取完成！")
    return f"{source}的数据(耗时{delay}秒)"

# --- 并发执行多个协程 ---
async def main():
    """主协程：并发执行多个异步任务。"""
    start = time.perf_counter()

    # asyncio.gather()：并发执行多个协程
    results = await asyncio.gather(
        fetch_data("数据库", 2),
        fetch_data("API接口", 1),
        fetch_data("缓存", 0.5),
    )
    # ↑ gather()：同时启动所有协程，等待全部完成后返回结果列表
    # ↑ 总耗时 = max(2, 1, 0.5) = 2秒，而非 2+1+0.5=3.5秒

    elapsed = time.perf_counter() - start
    print(f"\n所有结果: {results}")
    print(f"总耗时: {elapsed:.2f}秒 (并发执行)")
    return results

# 运行异步主函数
print("=== 异步并发示例 ===")
asyncio.run(main())  # run()：运行顶级协程，创建事件循环

# --- 同步版本对比 ---
def sync_main():
    """同步版本：顺序执行，对比耗时差异。"""
    start = time.perf_counter()

    # 同步顺序执行，总耗时 = 各任务耗时之和
    time.sleep(2)   # 模拟数据库操作
    time.sleep(1)   # 模拟API调用
    time.sleep(0.5) # 模拟缓存读取

    elapsed = time.perf_counter() - start
    print(f"\n同步版本总耗时: {elapsed:.2f}秒 (顺序执行)")

print("\n=== 同步版本对比 ===")
sync_main()  # 总耗时约3.5秒

# --- 异步任务创建与取消 ---
async def cancel_demo():
    """演示异步任务的创建和取消。"""
    task = asyncio.create_task(fetch_data("可取消任务", 5))
    # ↑ create_task()：将协程包装为Task，立即调度执行
    await asyncio.sleep(1)  # 等1秒后取消
    task.cancel()  # 取消任务
    try:
        await task
    except asyncio.CancelledError:
        print("[可取消任务] 任务已被取消！")

print("\n=== 任务取消演示 ===")
asyncio.run(cancel_demo())
```

**💡 代码解释**：asyncio是Python处理高并发IO的核心框架。关键概念：(1)协程(coroutine)——用async def定义的函数；(2)事件循环(event loop)——调度和执行协程的引擎；(3)await——暂停当前协程，让出控制权。asyncio.gather()是最常用的并发原语，多个任务同时执行，总耗时等于最慢的那个任务。注意：asyncio适合IO密集型，CPU密集型应用应使用多进程。

**🔑 关键要点**：
- async def定义协程，await暂停等待异步操作
- asyncio.run()创建事件循环并运行协程
- asyncio.gather()并发执行多个协程
- asyncio.create_task()创建后台任务
- asyncio适合IO密集型，CPU密集型应用多进程

---

### 2. 多线程与多进程并发

> **级别**：高级 | **概念**：threading适合IO密集型，multiprocessing适合CPU密集型，concurrent.futures提供统一的高级接口。

```python
# ============================================
# 【高级】Python 多线程与多进程并发
# ============================================

import time
import threading    # 多线程模块：适合IO密集型
import multiprocessing  # 多进程模块：适合CPU密集型
from concurrent.futures import ThreadPoolExecutor, ProcessPoolExecutor
# ↑ concurrent.futures：高级并发接口，统一线程池和进程池

# --- 多线程：IO密集型任务 ---
def io_task(name: str, delay: float) -> str:
    """模拟IO密集型任务（如网络请求、文件读写）。"""
    print(f"[线程-{name}] 开始IO操作")
    time.sleep(delay)  # 模拟IO等待
    # ↑ GIL在IO操作时会释放，所以多线程适合IO密集型
    print(f"[线程-{name}] IO操作完成")
    return f"{name}完成"

print("=== 多线程（ThreadPoolExecutor） ===")
start = time.perf_counter()

# ThreadPoolExecutor：线程池管理器
with ThreadPoolExecutor(max_workers=3) as executor:
    # ↑ max_workers=3：最多同时运行3个线程
    # submit()：提交任务到线程池，返回Future对象
    futures = [
        executor.submit(io_task, f"任务{i}", delay)
        for i, delay in enumerate([1, 2, 0.5], 1)
    ]
    # as_completed()：按完成顺序（而非提交顺序）获取结果
    for future in futures:
        print(f"结果: {future.result()}")

print(f"线程池总耗时: {time.perf_counter() - start:.2f}秒\n")

# --- 多进程：CPU密集型任务 ---
def cpu_task(n: int) -> int:
    """模拟CPU密集型任务（如计算）。

    多进程绕过GIL限制，真正利用多核CPU。
    """
    print(f"[进程-{n}] 开始计算")
    total = sum(i * i for i in range(10_000_000))
    # ↑ 大量计算，CPU密集
    print(f"[进程-{n}] 计算完成")
    return total

print("=== 多进程（ProcessPoolExecutor） ===")
start = time.perf_counter()

with ProcessPoolExecutor(max_workers=3) as executor:
    # ↑ ProcessPoolExecutor：进程池，每个进程有独立Python解释器
    futures = [executor.submit(cpu_task, i) for i in range(3)]
    for future in futures:
        result = future.result()
        print(f"计算结果: {str(result)[:20]}...")

print(f"进程池总耗时: {time.perf_counter() - start:.2f}秒")

# --- 线程 vs 进程选择指南 ---
print("\n=== 场景选择指南 ===")
print("IO密集型（网络请求、文件读写）→ 多线程/异步")
print("CPU密集型（计算、图像处理）→ 多进程")
print("混合型 → 多进程 + 每个进程内多线程/异步")
```

**💡 代码解释**：Python并发编程的核心挑战是GIL（全局解释器锁）。GIL导致同一时刻只有一个线程执行Python字节码，因此多线程无法加速CPU密集型任务。但IO操作会释放GIL，所以多线程适合IO密集型场景。多进程创建独立的Python解释器，完全绕过GIL，适合CPU密集型任务。concurrent.futures提供统一的Future接口，简化了线程池和进程池的使用。

**🔑 关键要点**：
- GIL限制多线程，但IO操作会释放GIL
- 多线程适合IO密集型，多进程适合CPU密集型
- ThreadPoolExecutor/ProcessPoolExecutor提供统一接口
- submit()提交任务返回Future，result()获取结果
- as_completed()按完成顺序获取结果

---

### 3. 元类编程

> **级别**：高级 | **概念**：元类(metaclass)是类的类，控制类的创建过程。用于框架开发、ORM、API注册等高级场景。

```python
# ============================================
# 【高级】Python 元类编程
# ============================================

# --- 元类基础：type是默认元类 ---
# 所有类都是由type元类创建的
print(f"int的类型: {type(int)}")
print(f"str的类型: {type(str)}")
print(f"自定义类的类型: {type(type)}")
# ↑ 一切皆是对象，类也是对象，由元类创建

# --- 自定义元类 ---
class ModelMeta(type):
    """自定义元类：在类创建时自动注册模型。

    继承自type，重写__new__方法控制类的创建。
    """
    # 类级别的注册表
    registry = {}  # 用于存储所有注册的模型类

    def __new__(mcs, name, bases, namespace):
        """创建类时被调用。

        Args:
            mcs: 元类本身（类似self，但约定叫mcs）
            name: 要创建的类名
            bases: 父类元组
            namespace: 类的属性字典
        """
        # 在类创建之前，可以修改类的属性
        namespace["created_at"] = "2024-01-01"  # 添加默认属性

        # 调用type.__new__创建类
        cls = super().__new__(mcs, name, bases, namespace)

        # 自动注册（排除基类自身）
        if name != "BaseModel":
            ModelMeta.registry[name] = cls
            print(f"[元类] 注册模型: {name}")

        return cls

# --- 使用元类的基类 ---
class BaseModel(metaclass=ModelMeta):
    # ↑ metaclass=ModelMeta：指定使用自定义元类创建这个类
    """所有模型的基类。"""

    def __init__(self, **kwargs):
        """初始化：将关键字参数设为实例属性。"""
        for key, value in kwargs.items():
            setattr(self, key, value)

    def save(self):
        """模拟保存操作。"""
        print(f"[保存] {self.__class__.__name__}: {self.__dict__}")

# --- 业务模型类（自动注册） ---
class User(BaseModel):
    """用户模型。"""
    pass  # 属性由BaseModel.__init__动态设置

class Product(BaseModel):
    """产品模型。"""
    pass

# 验证自动注册
print(f"\n注册的模型: {list(ModelMeta.registry.keys())}")

# 使用模型
user = User(name="张三", email="zhangsan@example.com")
user.save()
print(f"创建时间: {user.created_at}")  # 元类添加的属性

product = Product(name="笔记本电脑", price=5999)
product.save()

# --- 元类的实际应用场景 ---
print("\n=== 元类应用场景 ===")
print("1. ORM框架：自动将类映射到数据库表")
print("2. API注册：自动收集路由和视图函数")
print("3. 单例模式：确保类只有一个实例")
print("4. 属性验证：在类创建时检查字段定义")
```

**💡 代码解释**：元类是Python最强大的特性之一，也是最容易被滥用的。理解元类的关键在于：类也是对象，由元类创建。type是Python默认的元类。自定义元类继承type并重写__new__方法，可以在类创建时介入，实现自动注册、属性注入、验证等功能。Django ORM和SQLAlchemy等框架底层都使用了元类。

**🔑 关键要点**：
- 元类是类的类，type是默认元类
- metaclass参数指定自定义元类
- __new__在类创建时调用，可修改类属性
- 元类适合框架开发，业务代码中谨慎使用
- Django ORM、SQLAlchemy等框架底层使用了元类

---

### 4. 性能优化（cProfile与内存分析）

> **级别**：高级 | **概念**：使用cProfile定位性能瓶颈，memory_profiler分析内存使用，line_profiler逐行分析，实现精准优化。

```python
# ============================================
# 【高级】Python 性能优化：cProfile与内存分析
# ============================================

import cProfile   # 内置性能分析器
import pstats     # 性能分析结果处理
import io         # 用于捕获输出
import time

# 保存到文件：python -m cProfile -o output.prof script.py
# 可视化：pip install snakeviz && snakeviz output.prof

# --- 待优化的示例代码 ---
def slow_function():
    """一个故意写得低效的函数，用于演示性能分析。"""
    result = []
    for i in range(10000):
        # 低效：循环内重复计算
        result.append(i ** 2)
    return result

def fast_function():
    """优化后的版本：使用列表推导式。"""
    return [i ** 2 for i in range(10000)]

def mixed_workload():
    """混合工作负载，模拟真实场景。"""
    data = slow_function()
    time.sleep(0.1)  # 模拟IO等待
    more_data = fast_function()
    return len(data) + len(more_data)

# --- 使用cProfile进行性能分析 ---
print("=== cProfile 性能分析 ===")

# 创建Profiler对象
profiler = cProfile.Profile()
profiler.enable()  # 开始记录

# 运行待分析的代码
mixed_workload()

profiler.disable()  # 停止记录

# 分析结果
stream = io.StringIO()
stats = pstats.Stats(profiler, stream=stream)
stats.sort_stats('cumulative')  # 按累计时间排序
# ↑ 'cumulative'：累计时间（包含子函数调用）
# ↑ 'tottime'：总时间（仅自身代码）
# ↑ 'ncalls'：调用次数
stats.print_stats(10)  # 打印前10条
print(stream.getvalue())

# --- 内存分析（memory_profiler） ---
# 安装：pip install memory-profiler
# 命令行：python -m memory_profiler script.py
# 或在函数上添加 @profile 装饰器

def memory_example():
    """演示内存分析（需要安装memory_profiler）。

    使用方式：
    1. pip install memory-profiler
    2. python -m memory_profiler this_script.py
    """
    print("\n=== 内存分析提示 ===")
    print("安装: pip install memory-profiler")
    print("使用: python -m memory_profiler script.py")
    print("或在函数上使用 @profile 装饰器")

    # 示例：创建大列表
    large_list = [0] * 1_000_000  # 100万个元素的列表
    print(f"创建了 {len(large_list)} 个元素的列表")
    return large_list

memory_example()

# --- 性能优化实战建议 ---
print("\n=== 性能优化清单 ===")
print("1. 先测量，再优化（不要凭感觉）")
print("2. 用列表推导式替代显式循环")
print("3. 用生成器处理大数据集（节省内存）")
print("4. 用set/dict替代list做成员检查（O(1) vs O(n)）")
print("5. 用functools.lru_cache缓存重复计算结果")
print("6. 用__slots__减少类实例内存占用")
print("7. 用PyPy解释器提升执行速度（兼容性需测试）")
```

**💡 代码解释**：性能优化的黄金法则是：先测量，再优化。cProfile是Python内置的性能分析器，可以精确统计每个函数的调用次数和执行时间。pstats模块用于分析cProfile的输出结果。memory_profiler是第三方工具，用于逐行分析内存使用。优化的核心策略：用合适的数据结构、避免不必要的计算、利用缓存、惰性求值。

**🔑 关键要点**：
- cProfile是内置性能分析器，精确到函数级别
- pstats.sort_stats()按累计时间/总时间/调用次数排序
- memory_profiler逐行分析内存使用
- 先用cProfile定位瓶颈，再针对性优化
- set/dict成员检查O(1)远快于list的O(n)

---

### 5. FastAPI 框架实战

> **级别**：高级 | **概念**：FastAPI是高性能异步Web框架，基于Starlette和Pydantic，自动生成OpenAPI文档，支持类型验证和依赖注入。

```python
# ============================================
# 【高级】Python FastAPI 框架实战
# 安装: pip install fastapi uvicorn pydantic
# 运行: uvicorn this_file:app --reload
# ============================================

from fastapi import FastAPI, HTTPException, Depends, Query
from pydantic import BaseModel, Field, validator
from typing import List, Optional
from datetime import datetime

# --- 创建FastAPI应用 ---
app = FastAPI(
    title="用户管理API",
    description="FastAPI实战：RESTful API的完整示例",
    version="1.0.0"
)

# --- Pydantic模型：请求/响应数据验证 ---
class UserCreate(BaseModel):
    """创建用户的请求体模型。

    Pydantic自动验证数据类型，生成JSON Schema。
    """
    name: str = Field(..., min_length=2, max_length=50, description="用户名")
    # ↑ Field()：添加验证规则和文档描述
    # ↑ ...：表示必填字段
    email: str = Field(..., description="邮箱地址")
    age: int = Field(ge=0, le=150, description="年龄")
    # ↑ ge=0：大于等于0，le=150：小于等于150

    @validator('email')
    def validate_email(cls, v):
        """自定义验证器：检查邮箱格式。"""
        if '@' not in v:
            raise ValueError('邮箱格式不正确')
        return v

class UserResponse(BaseModel):
    """用户响应的模型。"""
    id: int
    name: str
    email: str
    age: int
    created_at: datetime

    class Config:
        orm_mode = True  # 支持从ORM对象自动转换

# --- 模拟数据库 ---
fake_db: List[dict] = []
user_id_counter = 0

# --- 依赖注入：提取公共逻辑 ---
async def get_current_user_id():
    """模拟从请求中获取当前用户ID。

    依赖注入函数：可被路由自动调用。
    """
    return 1  # 实际项目中从token解析

# --- API路由 ---
@app.get("/", summary="根路径")
async def root():
    """API根路径，返回欢迎信息。"""
    return {"message": "欢迎使用FastAPI用户管理API", "docs": "/docs"}

@app.post("/users", response_model=UserResponse, status_code=201)
# ↑ response_model：自动将输出转换为指定模型
# ↑ status_code=201：HTTP 201 Created
async def create_user(
    user: UserCreate,
    current_user_id: int = Depends(get_current_user_id)
    # ↑ Depends()：依赖注入，自动调用get_current_user_id
):
    """创建新用户。"""
    global user_id_counter
    user_id_counter += 1

    new_user = {
        "id": user_id_counter,
        "name": user.name,
        "email": user.email,
        "age": user.age,
        "created_at": datetime.now()
    }
    fake_db.append(new_user)
    return new_user

@app.get("/users", response_model=List[UserResponse])
async def list_users(
    page: int = Query(1, ge=1, description="页码"),
    size: int = Query(10, ge=1, le=100, description="每页数量")
    # ↑ Query()：查询参数验证
):
    """分页获取用户列表。"""
    start = (page - 1) * size
    end = start + size
    return fake_db[start:end]

@app.get("/users/{user_id}", response_model=UserResponse)
async def get_user(user_id: int):
    """根据ID获取用户。"""
    for user in fake_db:
        if user["id"] == user_id:
            return user
    raise HTTPException(status_code=404, detail="用户不存在")
    # ↑ HTTPException：返回标准HTTP错误响应

# 运行方式：
# uvicorn filename:app --reload
# 访问 http://localhost:8000/docs 查看自动生成的API文档
```

**💡 代码解释**：FastAPI是Python当前最流行的异步Web框架，核心优势：(1)高性能——基于Starlette和uvicorn，性能接近Node.js；(2)自动文档——生成OpenAPI(Swagger)文档；(3)类型安全——Pydantic模型自动验证请求数据；(4)依赖注入——Depends解耦公共逻辑。路由通过装饰器定义，response_model自动序列化输出。

**🔑 关键要点**：
- FastAPI基于Starlette和Pydantic，性能极高
- Pydantic模型自动验证请求数据，Field()添加约束
- response_model自动序列化响应数据
- Depends()实现依赖注入，解耦公共逻辑
- 访问 /docs 查看自动生成的Swagger文档

---

### 6. 设计模式实战

> **级别**：高级 | **概念**：掌握单例、工厂、策略、观察者等经典设计模式，在Python中利用语言特性简化实现，提升代码可维护性。

```python
# ============================================
# 【高级】Python 设计模式实战
# ============================================

from abc import ABC, abstractmethod
from typing import Dict, List, Callable

# --- 1. 单例模式：确保类只有一个实例 ---
class SingletonMeta(type):
    """单例元类：控制实例创建。"""
    _instances: Dict[type, object] = {}

    def __call__(cls, *args, **kwargs):
        """拦截类的实例化调用。"""
        if cls not in cls._instances:
            # 首次创建：调用父类__call__创建实例
            cls._instances[cls] = super().__call__(*args, **kwargs)
        return cls._instances[cls]

class DatabasePool(metaclass=SingletonMeta):
    """数据库连接池（单例）。"""
    def __init__(self):
        self.connections = 0

    def connect(self):
        self.connections += 1
        return self.connections

# 验证单例
pool1 = DatabasePool()
pool2 = DatabasePool()
print(f"单例验证: pool1 is pool2 = {pool1 is pool2}")
print(f"连接数: {pool1.connect()}, {pool2.connect()}")

# --- 2. 工厂模式：封装对象创建逻辑 ---
class PaymentProcessor(ABC):
    """支付处理器抽象基类。"""
    @abstractmethod
    def pay(self, amount: float) -> str:
        pass

class AlipayProcessor(PaymentProcessor):
    def pay(self, amount: float) -> str:
        return f"支付宝支付: ¥{amount:.2f}"

class WechatPayProcessor(PaymentProcessor):
    def pay(self, amount: float) -> str:
        return f"微信支付: ¥{amount:.2f}"

class PaymentFactory:
    """支付工厂：根据类型创建对应的支付处理器。"""
    _processors: Dict[str, type] = {
        "alipay": AlipayProcessor,
        "wechat": WechatPayProcessor,
    }

    @classmethod
    def create(cls, method: str) -> PaymentProcessor:
        """工厂方法：创建支付处理器。"""
        processor_cls = cls._processors.get(method)
        if not processor_cls:
            raise ValueError(f"不支持的支付方式: {method}")
        return processor_cls()

# 使用工厂
print("\n=== 工厂模式 ===")
processor = PaymentFactory.create("alipay")
print(processor.pay(99.99))
processor = PaymentFactory.create("wechat")
print(processor.pay(199.00))

# --- 3. 策略模式：运行时选择算法 ---
class DiscountStrategy(ABC):
    """折扣策略抽象基类。"""
    @abstractmethod
    def calculate(self, price: float) -> float:
        pass

class NoDiscount(DiscountStrategy):
    def calculate(self, price: float) -> float:
        return price

class PercentageDiscount(DiscountStrategy):
    def __init__(self, percent: float):
        self.percent = percent

    def calculate(self, price: float) -> float:
        return price * (1 - self.percent / 100)

class FixedDiscount(DiscountStrategy):
    def __init__(self, amount: float):
        self.amount = amount

    def calculate(self, price: float) -> float:
        return max(0, price - self.amount)

class Order:
    """订单类：使用策略模式计算最终价格。"""
    def __init__(self, price: float, strategy: DiscountStrategy):
        self.price = price
        self.strategy = strategy  # 策略可在运行时动态切换

    def final_price(self) -> float:
        return round(self.strategy.calculate(self.price), 2)

print("\n=== 策略模式 ===")
order = Order(100, NoDiscount())
print(f"原价: ¥{order.final_price()}")
order.strategy = PercentageDiscount(20)
print(f"8折后: ¥{order.final_price()}")
order.strategy = FixedDiscount(15)
print(f"减15元: ¥{order.final_price()}")

# --- 设计模式总结 ---
print("\n=== 设计模式应用场景 ===")
print("单例模式：配置管理、数据库连接池、日志器")
print("工厂模式：支付渠道、消息推送、数据源切换")
print("策略模式：折扣计算、排序算法、压缩算法选择")
```

**💡 代码解释**：设计模式是解决常见软件设计问题的可复用方案。在Python中，由于语言的动态特性，许多设计模式可以实现得更简洁。单例模式用元类实现最为优雅；工厂模式将对象创建逻辑集中管理，方便扩展；策略模式让算法可以独立于使用它的客户端变化。元类实现单例利用了__call__拦截实例化过程。

**🔑 关键要点**：
- 单例模式：元类实现最优雅，确保全局唯一实例
- 工厂模式：封装对象创建，新增类型无需修改客户端代码
- 策略模式：运行时切换算法，避免大量if-else
- Python动态特性让设计模式实现更简洁
- 设计模式的核心是面向接口编程而非具体实现

---
