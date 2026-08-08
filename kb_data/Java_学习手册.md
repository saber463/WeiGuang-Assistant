# ☕ Java 编程语言学习手册

> **分类**：后端  
> **描述**：Java后端开发知识库，涵盖从入门到高级的完整学习路径  
> **生成时间**：2026-08-07  

---

## 目录

- [初级入门](#primary)（8个知识点）
- [中级进阶](#intermediate)（8个知识点）
- [高级精通](#advanced)（6个知识点）

---

## 初级入门

### 1. JDK环境搭建与第一个程序

> **级别**：初级 | **概念**：安装JDK并配置JAVA_HOME环境变量，使用javac编译Java源文件，java命令运行字节码。

```java
// ============================================
// 【初级】Java JDK环境搭建与第一个程序
// ============================================

// 1. 下载安装JDK（推荐JDK 17 LTS版本）
//    官网: https://adoptium.net/ 或 https://www.oracle.com/java/

// 2. 配置环境变量
//    Windows: 设置 → 系统 → 高级系统设置 → 环境变量
//    新建 JAVA_HOME = C:\Program Files\Java\jdk-17
//    Path 中添加 %JAVA_HOME%\bin

// 3. 验证安装
//    打开终端执行: java -version
//    输出应显示: openjdk version "17.0.x" ...

// 4. 第一个Java程序
public class HelloWorld {  // 类名必须与文件名一致（HelloWorld.java）
    // ↑ public：访问修饰符，表示公开可见
    // ↑ class：关键字，声明一个类

    public static void main(String[] args) {
        // ↑ main方法是程序的入口点，JVM从这里开始执行
        // ↑ public：JVM需要从外部调用main方法
        // ↑ static：静态方法，无需创建对象即可调用
        // ↑ void：main方法不返回任何值
        // ↑ String[] args：命令行参数数组

        // 输出到控制台
        System.out.println("Hello, Java World!");
        // ↑ System：Java标准库中的系统类
        // ↑ out：System类的静态成员，表示标准输出流
        // ↑ println()：打印一行并换行

        // 打印当前Java版本
        System.out.println("Java版本: " + System.getProperty("java.version"));
        // ↑ getProperty()：获取系统属性
        // ↑ +：字符串拼接运算符

        // 打印当前工作目录
        System.out.println("工作目录: " + System.getProperty("user.dir"));
    }
}

// 编译运行:
// javac HelloWorld.java    → 生成 HelloWorld.class 字节码文件
// java HelloWorld          → JVM加载并执行字节码
```

**💡 代码解释**：Java程序的运行流程：编写.java源文件 → javac编译为.class字节码 → JVM加载执行字节码。main方法是Java程序的入口，签名必须是public static void main(String[] args)。System.out.println是最常用的输出方法。Java是编译型+解释型语言，字节码可在任何平台的JVM上运行，实现"一次编写，到处运行"。

**🔑 关键要点**：
- JDK包含JRE（运行环境）和开发工具（javac、java等）
- 类名必须与.java文件名完全一致（区分大小写）
- main方法是程序入口，签名固定不可更改
- JVM加载.class字节码文件执行
- Java是跨平台语言，字节码与平台无关

---

### 2. 基本数据类型与运算符

> **级别**：初级 | **概念**：Java有8种基本类型（byte/short/int/long/float/double/char/boolean），运算符包括算术、比较、逻辑和赋值。

```java
// ============================================
// 【初级】Java 基本数据类型与运算符
// ============================================

public class DataTypes {
    public static void main(String[] args) {
        // --- 整数类型（4种） ---
        byte b = 127;           // byte：1字节，范围 -128 ~ 127
        short s = 32767;        // short：2字节，范围 -32768 ~ 32767
        int i = 2147483647;     // int：4字节（默认整数类型），约±21亿
        long l = 9223372036854775807L;
        // ↑ long：8字节，数值后加L表示长整型字面量

        System.out.println("byte: " + b + ", short: " + s);
        System.out.println("int: " + i + ", long: " + l);

        // --- 浮点类型（2种） ---
        float f = 3.14f;        // float：4字节，数值后加f表示float字面量
        double d = 3.141592653589793;
        // ↑ double：8字节（默认浮点类型），精度更高

        System.out.println("float: " + f + ", double: " + d);

        // --- 字符类型 ---
        char c1 = 'A';          // char：2字节，存储Unicode字符
        char c2 = '中';          // 支持中文字符
        char c3 = '\u4e2d';     // Unicode转义序列，也表示'中'

        System.out.println("char: " + c1 + " " + c2 + " " + c3);

        // --- 布尔类型 ---
        boolean isJavaFun = true;   // boolean：只有true和false两个值
        boolean isHard = false;
        System.out.println("Java有趣吗? " + isJavaFun);

        // --- 算术运算符 ---
        int a = 10, b2 = 3;
        System.out.println("\n=== 算术运算符 ===");
        System.out.println("a + b = " + (a + b2));   // 加法
        System.out.println("a - b = " + (a - b2));   // 减法
        System.out.println("a * b = " + (a * b2));   // 乘法
        System.out.println("a / b = " + (a / b2));   // 整数除法：结果截断为3
        System.out.println("a % b = " + (a % b2));   // 取模（余数）

        // --- 比较运算符 ---
        System.out.println("\n=== 比较运算符 ===");
        System.out.println("a > b: " + (a > b2));    // 大于
        System.out.println("a == b: " + (a == b2));   // 等于（注意用==而非=）
        System.out.println("a != b: " + (a != b2));   // 不等于

        // --- 逻辑运算符 ---
        boolean x = true, y = false;
        System.out.println("\n=== 逻辑运算符 ===");
        System.out.println("x && y: " + (x && y));   // 逻辑与：全真才真
        System.out.println("x || y: " + (x || y));   // 逻辑或：有真即真
        System.out.println("!x: " + (!x));           // 逻辑非：取反

        // --- 类型转换 ---
        int num = 100;
        long bigNum = num;          // 自动类型提升（小→大，安全）
        double dNum = num;          // int自动转为double

        int narrowed = (int) 3.99;  // 强制类型转换（大→小，可能丢失精度）
        System.out.println("\n强制转换: (int)3.99 = " + narrowed);  // 输出3
    }
}
```

**💡 代码解释**：Java是强类型语言，每个变量必须声明类型。8种基本类型分为四类：整数(byte/short/int/long)、浮点(float/double)、字符(char)、布尔(boolean)。整数默认int，浮点默认double。自动类型提升从小类型到大类型安全，强制转换从大类型到小类型可能丢失数据。整数除法会截断，需注意。

**🔑 关键要点**：
- Java是强类型语言，变量必须先声明类型
- 8种基本类型：byte/short/int/long/float/double/char/boolean
- long字面量加L，float字面量加f
- 整数除法截断小数部分，不是四舍五入
- 自动类型提升安全，强制转换可能丢失精度

---

### 3. 控制流程（if-else/switch/循环）

> **级别**：初级 | **概念**：if-else实现条件分支，switch处理多值匹配，for/while/do-while实现循环，break/continue控制流程。

```java
// ============================================
// 【初级】Java 控制流程：条件判断与循环
// ============================================

public class ControlFlow {
    public static void main(String[] args) {
        // --- if-else 条件判断 ---
        int score = 85;

        if (score >= 90) {  // if：判断条件是否为true
            System.out.println("等级: 优秀");
        } else if (score >= 80) {  // else if：上一个条件不满足时再判断
            System.out.println("等级: 良好");
        } else if (score >= 60) {
            System.out.println("等级: 及格");
        } else {  // else：所有条件都不满足时执行
            System.out.println("等级: 不及格");
        }

        // --- switch 多分支选择 ---
        int dayOfWeek = 3;  // 1=周一 ... 7=周日
        switch (dayOfWeek) {  // switch：根据变量值跳转到对应case
            case 1:  // case：匹配值
                System.out.println("周一：开始新的一周");
                break;  // break：跳出switch，防止case穿透
            case 2:
                System.out.println("周二：继续加油");
                break;
            case 3:
                System.out.println("周三：一周过半");
                break;
            case 6:
            case 7:  // 多个case共享同一段代码
                System.out.println("周末：好好休息");
                break;
            default:  // default：所有case都不匹配时执行
                System.out.println("工作日");
                break;
        }

        // --- for 循环：已知循环次数 ---
        System.out.println("\nfor循环: 打印1到5");
        for (int i = 1; i <= 5; i++) {
            // ↑ for(初始化; 条件; 迭代) { 循环体 }
            // ↑ i++：每次循环后i自增1
            System.out.print(i + " ");
        }
        System.out.println();  // 换行

        // --- 增强for循环（for-each）：遍历数组/集合 ---
        String[] fruits = {"苹果", "香蕉", "橘子"};
        System.out.println("\n增强for循环: 遍历数组");
        for (String fruit : fruits) {
            // ↑ 增强for：依次取出数组中的每个元素
            System.out.println("  " + fruit);
        }

        // --- while 循环：条件为true时持续执行 ---
        int count = 0;
        System.out.println("\nwhile循环:");
        while (count < 3) {  // 先判断条件，再执行循环体
            System.out.println("  计数: " + count);
            count++;
        }

        // --- do-while 循环：至少执行一次 ---
        System.out.println("\ndo-while循环:");
        int num = 5;
        do {  // 先执行循环体，再判断条件
            System.out.println("  数字: " + num);
            num++;
        } while (num < 3);  // 条件不满足，但循环体已执行一次

        // --- break 和 continue ---
        System.out.println("\nbreak: 找到第一个偶数就退出");
        int[] numbers = {1, 3, 5, 6, 7, 9};
        for (int n : numbers) {
            if (n % 2 == 0) {
                System.out.println("  找到偶数: " + n);
                break;  // break：立即终止循环
            }
        }
    }
}
```

**💡 代码解释**：Java的控制流程语法与C/C++类似。switch支持int/String/enum类型（Java 7+支持String）。for循环的三要素（初始化、条件、迭代）缺一不可。增强for循环（for-each）简洁遍历数组和集合，但无法获取索引。while先判断后执行，do-while先执行后判断。break终止整个循环，continue跳过本次迭代。

**🔑 关键要点**：
- if-else if-else：从上到下依次判断，匹配即停止
- switch中的break防止case穿透（fall-through）
- for(初始化;条件;迭代) 三要素控制循环
- 增强for循环简洁遍历数组和集合
- break终止循环，continue跳过本次迭代

---

### 4. 数组的定义与操作

> **级别**：初级 | **概念**：数组是固定长度的同类型数据容器，通过索引访问，支持遍历、排序和多维数组。

```java
// ============================================
// 【初级】Java 数组的定义与操作
// ============================================

import java.util.Arrays;  // 导入Arrays工具类，提供排序、填充等方法

public class ArrayDemo {
    public static void main(String[] args) {
        // --- 数组的三种声明方式 ---
        int[] arr1 = new int[5];  // 方式1：声明并分配空间，默认值为0
        int[] arr2 = {10, 20, 30, 40, 50};  // 方式2：声明并初始化
        int[] arr3 = new int[]{1, 2, 3, 4, 5};  // 方式3：匿名数组初始化

        // --- 访问和修改数组元素 ---
        arr1[0] = 100;  // 通过索引赋值，索引从0开始
        arr1[1] = 200;
        System.out.println("arr1[0] = " + arr1[0]);  // 通过索引访问
        System.out.println("arr1长度: " + arr1.length);  // length属性：获取数组长度

        // --- 遍历数组 ---
        System.out.println("\n遍历arr2:");
        for (int i = 0; i < arr2.length; i++) {
            // ↑ 传统for循环：通过索引访问每个元素
            System.out.println("  arr2[" + i + "] = " + arr2[i]);
        }

        // --- 数组排序 ---
        int[] scores = {85, 92, 78, 95, 88};
        System.out.println("\n排序前: " + Arrays.toString(scores));
        // ↑ Arrays.toString()：将数组转为可读字符串

        Arrays.sort(scores);  // sort()：升序排序（修改原数组）
        System.out.println("排序后: " + Arrays.toString(scores));

        // --- 数组查找 ---
        int index = Arrays.binarySearch(scores, 88);
        // ↑ binarySearch()：二分查找（数组必须已排序）
        System.out.println("88的索引位置: " + index);

        // --- 数组复制 ---
        int[] copied = Arrays.copyOf(scores, 3);
        // ↑ copyOf(原数组, 新长度)：复制前N个元素
        System.out.println("\n复制前3个: " + Arrays.toString(copied));

        int[] range = Arrays.copyOfRange(scores, 1, 4);
        // ↑ copyOfRange(原数组, 起始索引, 结束索引)：复制指定范围
        System.out.println("复制索引1-3: " + Arrays.toString(range));

        // --- 二维数组 ---
        int[][] matrix = {  // 二维数组：数组的数组
            {1, 2, 3},
            {4, 5, 6},
            {7, 8, 9}
        };
        System.out.println("\n二维数组遍历:");
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();  // 每行结束后换行
        }
    }
}
```

**💡 代码解释**：数组是Java中最基本的数据结构，长度固定，元素类型一致。创建后长度不可变（需扩容可考虑ArrayList）。Arrays工具类提供排序(sort)、查找(binarySearch)、复制(copyOf)、填充(fill)、比较(equals)等实用方法。binarySearch要求数组已排序，否则结果不可靠。二维数组本质是"数组的数组"，每行长度可以不同。

**🔑 关键要点**：
- 数组长度固定，创建后不可改变
- 索引从0开始，length属性获取长度
- Arrays.sort()升序排序，修改原数组
- binarySearch()要求数组已排序
- 二维数组是数组的数组，每行长度可不同

---

### 5. 方法的定义与重载

> **级别**：初级 | **概念**：方法封装可复用的代码逻辑，支持参数传递和返回值。方法重载允许同名方法接受不同参数。

```java
// ============================================
// 【初级】Java 方法的定义与重载
// ============================================

public class MethodDemo {
    // ★ main方法是程序入口
    public static void main(String[] args) {
        // --- 调用无返回值方法 ---
        printWelcome();  // 直接调用静态方法

        // --- 调用有返回值方法 ---
        int sum = add(10, 20);  // 返回值赋给变量
        System.out.println("10 + 20 = " + sum);

        // --- 方法重载演示 ---
        System.out.println("\n=== 方法重载 ===");
        System.out.println("两个整数相加: " + add(10, 20));
        // ↑ 调用 add(int, int)
        System.out.println("三个整数相加: " + add(10, 20, 30));
        // ↑ 调用 add(int, int, int) —— 参数个数不同
        System.out.println("两个浮点数相加: " + add(3.5, 2.5));
        // ↑ 调用 add(double, double) —— 参数类型不同

        // --- 可变参数方法 ---
        System.out.println("\n可变参数求和: " + sumAll(1, 2, 3, 4, 5));
        System.out.println("可变参数求和: " + sumAll(10, 20));
    }

    // --- 无参数无返回值的方法 ---
    public static void printWelcome() {
        // ↑ public：公开访问
        // ↑ static：静态方法，属于类而非实例
        // ↑ void：无返回值
        System.out.println("=== 欢迎学习Java方法 ===");
    }

    // --- 有参数有返回值的方法 ---
    public static int add(int a, int b) {
        // ↑ int：返回值类型
        // ↑ (int a, int b)：形参列表
        int result = a + b;  // 计算和
        return result;  // return：返回结果并结束方法
    }

    // --- 方法重载：同名方法，参数不同 ---
    public static int add(int a, int b, int c) {
        // ↑ 重载：参数个数不同（2个 → 3个）
        return a + b + c;
    }

    public static double add(double a, double b) {
        // ↑ 重载：参数类型不同（int → double）
        return a + b;
    }

    // --- 可变参数方法（JDK 5+） ---
    public static int sumAll(int... numbers) {
        // ↑ int... numbers：可变参数，本质是数组
        // ↑ 调用时可传入任意个int参数
        int total = 0;
        for (int num : numbers) {  // 增强for循环遍历
            total += num;
        }
        return total;
    }
}
```

**💡 代码解释**：方法是Java代码复用的基本单元。方法签名由方法名和参数列表组成（不包括返回值类型）。方法重载是Java多态的一种体现：同名方法根据参数类型和数量进行区分，编译器在编译时决定调用哪个版本。可变参数(int... args)是JDK 5引入的语法糖，底层是数组，一个方法只能有一个可变参数且必须放在最后。

**🔑 关键要点**：
- 方法签名 = 方法名 + 参数列表（不含返回值类型）
- static方法属于类，无需创建对象即可调用
- 方法重载：同名但参数类型/数量/顺序不同
- 重载与返回值类型无关，只与参数列表有关
- 可变参数(int... args)底层是数组，必须放在参数列表最后

---

### 6. 类与对象基础

> **级别**：初级 | **概念**：类是对象的模板，对象是类的实例。掌握构造方法、成员变量、成员方法、this关键字和访问修饰符。

```java
// ============================================
// 【初级】Java 类与对象基础
// ============================================

// --- 定义一个学生类 ---
class Student {
    // 成员变量（属性/字段）
    private String name;  // private：私有，仅类内部可访问
    private int age;      // 封装：隐藏内部数据
    private String studentId;

    // 静态变量（类变量）：所有实例共享
    private static int totalStudents = 0;
    // ↑ static：属于类，不属某个实例

    // 构造方法：创建对象时调用
    public Student(String name, int age, String studentId) {
        // ↑ 构造方法名必须与类名相同，无返回值类型
        this.name = name;  // this：指向当前对象的引用
        this.age = age;
        this.studentId = studentId;
        totalStudents++;  // 每创建一个学生，总数+1
    }

    // 无参构造方法（默认构造方法）
    public Student() {
        this("未知", 0, "000");  // 调用另一个构造方法
        // ↑ this()：调用本类的其他构造方法，必须在第一行
    }

    // Getter方法：获取私有属性值
    public String getName() {
        return name;
    }

    // Setter方法：设置私有属性值（可添加验证逻辑）
    public void setAge(int age) {
        if (age >= 0 && age <= 150) {  // 数据验证
            this.age = age;
        } else {
            System.out.println("年龄不合法: " + age);
        }
    }

    public int getAge() {
        return age;
    }

    // 成员方法：描述对象行为
    public void study(String subject) {
        System.out.println(name + " 正在学习 " + subject);
    }

    // 静态方法：属于类，通过类名调用
    public static int getTotalStudents() {
        return totalStudents;
    }

    // 重写toString()：自定义对象的字符串表示
    @Override  // @Override：注解，表示重写父类方法
    public String toString() {
        return "Student{name='" + name + "', age=" + age +
               ", id='" + studentId + "'}";
    }
}

// --- 主类：演示类的使用 ---
public class ClassDemo {
    public static void main(String[] args) {
        // 创建对象：使用new关键字调用构造方法
        Student s1 = new Student("张三", 20, "S001");
        Student s2 = new Student("李四", 22, "S002");

        // 调用对象方法
        s1.study("Java编程");
        s2.study("数据结构");

        // 使用Getter/Setter访问属性
        System.out.println(s1.getName() + " 的年龄: " + s1.getAge());
        s1.setAge(21);  // 通过Setter修改年龄
        System.out.println("修改后年龄: " + s1.getAge());

        // 调用静态方法（通过类名调用）
        System.out.println("学生总数: " + Student.getTotalStudents());

        // 打印对象（自动调用toString()）
        System.out.println("\n" + s1);
        System.out.println(s2);
    }
}
```

**💡 代码解释**：Java是纯面向对象语言，一切代码都在类中。类由成员变量（属性）和成员方法（行为）组成。构造方法用于初始化对象，与类同名且无返回值。封装通过private修饰符隐藏内部数据，通过public的Getter/Setter提供受控访问。static修饰的成员属于类，所有实例共享。this指向当前对象，用于区分成员变量和局部变量。

**🔑 关键要点**：
- 类是对象的模板，对象是类的实例（new关键字创建）
- 构造方法与类同名，无返回值，用于初始化
- private封装数据，public的Getter/Setter提供访问
- static成员属于类，所有实例共享
- this指向当前对象，this()调用本类其他构造方法

---

### 7. String与StringBuilder

> **级别**：初级 | **概念**：String是不可变字符序列，适合少量字符串操作；StringBuilder是可变的，适合频繁拼接的高效场景。

```java
// ============================================
// 【初级】Java String与StringBuilder
// ============================================

public class StringDemo {
    public static void main(String[] args) {
        // --- String：不可变字符序列 ---
        String s1 = "Hello";  // 字符串字面量，存储在字符串常量池
        String s2 = new String("Hello");
        // ↑ new创建：在堆中新建对象（不推荐，浪费内存）

        // 字符串比较：必须用equals()而非==
        System.out.println("s1.equals(s2): " + s1.equals(s2));
        // ↑ equals()：比较字符串内容是否相同
        System.out.println("s1 == s2: " + (s1 == s2));
        // ↑ ==：比较引用地址是否相同（此处为false）

        // --- 常用String方法 ---
        String text = "  Hello Java World  ";
        System.out.println("\n原始: '" + text + "'");
        System.out.println("长度: " + text.length());
        // ↑ length()：返回字符数
        System.out.println("去空格: '" + text.trim() + "'");
        // ↑ trim()：去除首尾空白字符
        System.out.println("大写: " + text.toUpperCase());
        // ↑ toUpperCase()：转为全大写（返回新字符串）
        System.out.println("小写: " + text.toLowerCase());

        // 子串与查找
        System.out.println("\n子串(0,5): " + text.substring(0, 5));
        // ↑ substring(start, end)：截取[start, end)区间
        System.out.println("'Java'的位置: " + text.indexOf("Java"));
        // ↑ indexOf()：返回首次出现的位置，找不到返回-1
        System.out.println("包含'World'? " + text.contains("World"));
        // ↑ contains()：判断是否包含子串

        // 分割字符串
        String csv = "苹果,香蕉,橘子,葡萄";
        String[] fruits = csv.split(",");
        // ↑ split()：按分隔符拆分为字符串数组
        System.out.println("\n分割结果:");
        for (String fruit : fruits) {
            System.out.println("  " + fruit);
        }

        // --- StringBuilder：可变字符串（高效拼接） ---
        System.out.println("\n=== StringBuilder 高效拼接 ===");
        StringBuilder sb = new StringBuilder();
        // ↑ StringBuilder：线程不安全但性能高，适合单线程

        sb.append("Hello");     // append()：追加内容
        sb.append(" ");
        sb.append("Java");
        sb.append(" ");
        sb.append("World");
        System.out.println("拼接结果: " + sb.toString());
        // ↑ toString()：转为String对象

        sb.insert(6, "Beautiful ");
        // ↑ insert(索引, 内容)：在指定位置插入
        System.out.println("插入后: " + sb);

        sb.replace(6, 15, "Amazing");
        // ↑ replace(start, end, 新内容)：替换指定范围
        System.out.println("替换后: " + sb);

        sb.delete(6, 13);
        // ↑ delete(start, end)：删除指定范围
        System.out.println("删除后: " + sb);

        sb.reverse();
        // ↑ reverse()：反转字符串
        System.out.println("反转后: " + sb);
    }
}
```

**💡 代码解释**：String最核心的特性是不可变性：任何修改操作都会返回新String对象，原对象不变。这导致频繁拼接时产生大量临时对象，影响性能。StringBuilder是可变的，append/insert/delete等方法直接修改自身，不创建新对象，适合循环拼接场景。StringBuffer是线程安全版本（方法加synchronized），性能略低于StringBuilder。

**🔑 关键要点**：
- String不可变，修改操作返回新对象
- 字符串比较用equals()而非==
- StringBuilder可变，适合频繁拼接场景
- append()追加，insert()插入，delete()删除
- StringBuilder线程不安全但性能高，StringBuffer线程安全

---

### 8. 集合框架（List/Set/Map基础）

> **级别**：初级 | **概念**：ArrayList动态数组替代固定数组，HashSet去重集合，HashMap键值对映射，是Java最常用的三种集合。

```java
// ============================================
// 【初级】Java 集合框架：List/Set/Map基础
// ============================================

import java.util.ArrayList;  // 动态数组列表
import java.util.HashSet;    // 哈希集合（去重）
import java.util.HashMap;    // 哈希映射（键值对）
import java.util.List;       // List接口
import java.util.Set;        // Set接口
import java.util.Map;        // Map接口

public class CollectionDemo {
    public static void main(String[] args) {
        // --- ArrayList：动态数组 ---
        System.out.println("=== ArrayList ===");
        List<String> tasks = new ArrayList<>();
        // ↑ List<String>：接口类型声明
        // ↑ ArrayList<>()：菱形语法，自动推断泛型

        tasks.add("写代码");   // add()：添加元素到末尾
        tasks.add("测试");
        tasks.add("部署");
        tasks.add(1, "评审");  // add(索引, 元素)：在指定位置插入

        System.out.println("任务列表: " + tasks);
        System.out.println("第1个任务: " + tasks.get(0));
        // ↑ get(索引)：获取指定位置的元素

        tasks.remove("测试");  // remove()：按值删除
        System.out.println("删除后: " + tasks);
        System.out.println("包含'部署'? " + tasks.contains("部署"));
        // ↑ contains()：判断是否包含指定元素

        // --- HashSet：无序不重复集合 ---
        System.out.println("\n=== HashSet ===");
        Set<String> tags = new HashSet<>();
        tags.add("Java");
        tags.add("编程");
        tags.add("后端");
        tags.add("Java");  // 重复元素不会被添加
        System.out.println("标签集合: " + tags);
        System.out.println("集合大小: " + tags.size());
        // ↑ size()：返回元素数量

        // 集合去重应用
        List<Integer> numbers = new ArrayList<>();
        numbers.add(1); numbers.add(2); numbers.add(2);
        numbers.add(3); numbers.add(3); numbers.add(3);
        Set<Integer> unique = new HashSet<>(numbers);
        // ↑ 将List传入HashSet构造方法，自动去重
        System.out.println("\n原始列表: " + numbers);
        System.out.println("去重后: " + unique);

        // --- HashMap：键值对映射 ---
        System.out.println("\n=== HashMap ===");
        Map<String, Integer> scores = new HashMap<>();
        scores.put("张三", 85);  // put(键, 值)：添加/更新键值对
        scores.put("李四", 92);
        scores.put("王五", 78);

        System.out.println("张三的成绩: " + scores.get("张三"));
        // ↑ get(键)：获取键对应的值
        System.out.println("赵六的成绩: " + scores.getOrDefault("赵六", 0));
        // ↑ getOrDefault()：键不存在时返回默认值

        // 遍历Map
        System.out.println("\n遍历所有成绩:");
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            // ↑ entrySet()：返回所有键值对的Set视图
            System.out.println("  " + entry.getKey() + " -> " + entry.getValue());
        }

        // 检查键是否存在
        System.out.println("\n包含'李四'? " + scores.containsKey("李四"));
        System.out.println("包含成绩92? " + scores.containsValue(92));
    }
}
```

**💡 代码解释**：Java集合框架是后端开发的核心。ArrayList基于动态数组，查询快(O(1))，增删慢(O(n))。HashSet基于HashMap实现，元素唯一，适合去重。HashMap基于哈希表，键值对存储，查找O(1)。使用接口类型(List/Set/Map)声明变量是良好实践，方便后续切换实现类。泛型(<>)提供编译期类型检查，避免运行时ClassCastException。

**🔑 关键要点**：
- ArrayList动态数组，替代固定长度数组
- HashSet基于HashMap，元素唯一无序
- HashMap键值对映射，查找O(1)
- 用接口类型声明变量(List/Set/Map)，方便切换实现
- 泛型提供编译期类型安全

---

## 中级进阶

### 1. 面向对象深入（封装/继承/多态）

> **级别**：中级 | **概念**：封装隐藏内部实现，继承复用代码并建立类层次，多态让同一接口表现不同行为，是OOP三大支柱。

```java
// ============================================
// 【中级】Java 面向对象深入：封装、继承、多态
// ============================================

// --- 1. 封装：隐藏内部实现 ---
class BankAccount {
    private String accountNumber;  // 私有：外部不可直接访问
    private double balance;        // 私有：保护数据安全

    public BankAccount(String accountNumber, double initialBalance) {
        this.accountNumber = accountNumber;
        this.balance = initialBalance;
    }

    // 公开方法提供受控访问
    public double getBalance() {
        return balance;  // 只读访问
    }

    public void deposit(double amount) {
        if (amount > 0) {  // 数据验证：防止非法操作
            balance += amount;
            System.out.println("存入: ¥" + amount + ", 余额: ¥" + balance);
        }
    }

    public boolean withdraw(double amount) {
        if (amount > 0 && amount <= balance) {  // 验证余额充足
            balance -= amount;
            System.out.println("取出: ¥" + amount + ", 余额: ¥" + balance);
            return true;
        }
        System.out.println("余额不足！");
        return false;
    }
}

// --- 2. 继承：代码复用 ---
// 父类（基类）
class Employee {
    protected String name;   // protected：子类可访问
    protected double salary;

    public Employee(String name, double salary) {
        this.name = name;
        this.salary = salary;
    }

    public double getAnnualSalary() {
        return salary * 12;  // 基础年薪 = 月薪 × 12
    }

    public void work() {
        System.out.println(name + " 正在工作...");
    }
}

// 子类（派生类）
class Manager extends Employee {  // extends：继承关键字
    private double bonus;  // 子类特有属性

    public Manager(String name, double salary, double bonus) {
        super(name, salary);  // super()：调用父类构造方法，必须在第一行
        this.bonus = bonus;
    }

    // 方法重写（Override）：覆盖父类方法
    @Override
    public double getAnnualSalary() {
        return super.getAnnualSalary() + bonus;
        // ↑ super.getAnnualSalary()：调用父类方法
        // ↑ 经理年薪 = 基础年薪 + 奖金
    }

    @Override
    public void work() {
        System.out.println("经理 " + name + " 正在管理团队...");
    }

    // 子类特有方法
    public void holdMeeting() {
        System.out.println(name + " 正在召开会议");
    }
}

// --- 3. 多态：同一接口，不同行为 ---
public class OOPDemo {
    public static void main(String[] args) {
        // 封装演示
        System.out.println("=== 封装 ===");
        BankAccount account = new BankAccount("622202123456", 1000);
        account.deposit(500);
        account.withdraw(200);

        // 多态演示
        System.out.println("\n=== 多态 ===");
        Employee emp1 = new Employee("张三", 8000);
        Employee emp2 = new Manager("李四", 12000, 50000);
        // ↑ 父类引用指向子类对象（向上转型）

        Employee[] team = {emp1, emp2};
        for (Employee emp : team) {
            emp.work();  // 多态：实际调用子类重写的方法
            System.out.println("  年薪: ¥" + emp.getAnnualSalary());
        }

        // 向下转型：恢复子类特有方法
        if (emp2 instanceof Manager) {  // instanceof：类型检查
            Manager mgr = (Manager) emp2;  // 向下转型
            mgr.holdMeeting();  // 调用子类特有方法
        }
    }
}
```

**💡 代码解释**：Java OOP三大特性：(1)封装——private隐藏数据，public方法提供受控访问，保护数据完整性；(2)继承——extends关键字，子类复用父类代码，super()调用父类构造，@Override重写方法；(3)多态——父类引用指向子类对象，运行时动态绑定实际方法。向上转型自动安全，向下转型需instanceof检查。

**🔑 关键要点**：
- 封装：private隐藏数据，public方法提供受控访问
- 继承：extends关键字，super()调用父类构造
- @Override重写父类方法，运行时动态绑定
- 多态：父类引用指向子类对象，调用实际类型的方法
- instanceof检查类型，向下转型需谨慎

---

### 2. 接口与抽象类

> **级别**：中级 | **概念**：抽象类定义'是什么'并提供部分实现，接口定义'能做什么'的契约，Java 8+接口支持默认方法和静态方法。

```java
// ============================================
// 【中级】Java 接口与抽象类
// ============================================

// --- 抽象类：定义共性，包含部分实现 ---
abstract class Shape {  // abstract：抽象类关键字
    protected String color;

    public Shape(String color) {
        this.color = color;
    }

    // 抽象方法：无方法体，子类必须实现
    public abstract double getArea();
    // ↑ abstract：抽象方法，子类必须重写

    // 具体方法：有方法体，子类可直接使用
    public void display() {
        System.out.println("这是一个" + color + "色的形状，面积: " + getArea());
    }
}

// 子类实现抽象方法
class Circle extends Shape {
    private double radius;

    public Circle(String color, double radius) {
        super(color);  // 调用父类构造
        this.radius = radius;
    }

    @Override
    public double getArea() {  // 必须实现抽象方法
        return Math.PI * radius * radius;
    }
}

// --- 接口：定义行为契约 ---
interface Flyable {  // interface：接口关键字
    // 抽象方法（默认 public abstract）
    void fly();

    // 默认方法（Java 8+）：提供默认实现
    default void takeOff() {
        // ↑ default：接口默认方法，实现类可继承或重写
        System.out.println("准备起飞...");
        fly();  // 调用抽象方法
    }

    // 静态方法（Java 8+）：通过接口名调用
    static String getType() {
        return "飞行器";
    }
}

interface Swimmable {
    void swim();
}

// 一个类可以实现多个接口（解决单继承限制）
class Duck implements Flyable, Swimmable {
    // ↑ implements：实现接口
    // ↑ Java类只能单继承，但可实现多个接口

    @Override
    public void fly() {
        System.out.println("鸭子扑腾翅膀飞行");
    }

    @Override
    public void swim() {
        System.out.println("鸭子悠闲地游泳");
    }
}

// --- 接口继承接口 ---
interface Amphibious extends Flyable, Swimmable {
    // ↑ 接口可以多继承接口
    void dive();  // 新增方法
}

// --- 主类演示 ---
public class InterfaceDemo {
    public static void main(String[] args) {
        // 抽象类使用
        System.out.println("=== 抽象类 ===");
        Shape circle = new Circle("红", 5.0);
        circle.display();

        // 接口使用
        System.out.println("\n=== 接口 ===");
        Duck duck = new Duck();
        duck.takeOff();  // 调用接口默认方法
        duck.swim();     // 调用接口抽象方法（已实现）

        // 接口静态方法
        System.out.println("类型: " + Flyable.getType());

        // 多态：通过接口引用
        Flyable flyer = new Duck();
        flyer.fly();  // 只能调用Flyable接口定义的方法
    }
}
```

**💡 代码解释**：抽象类vs接口的选择：(1)抽象类用abstract修饰，可以有构造方法、成员变量、具体方法，体现"is-a"关系；(2)接口用interface修饰，默认public abstract方法，Java 8+支持default和static方法，体现"can-do"能力。关键区别：类只能继承一个抽象类，但可以实现多个接口。接口用于定义跨类层次的行为契约。

**🔑 关键要点**：
- 抽象类abstract，有构造方法，单继承
- 接口interface，无构造方法，可多实现
- 抽象类体现"是什么"，接口体现"能做什么"
- Java 8+接口支持default方法和static方法
- 接口可以多继承接口

---

### 3. 异常处理机制

> **级别**：中级 | **概念**：try-catch-finally处理异常，throw抛出异常，throws声明异常，自定义异常类。区分受检异常和运行时异常。

```java
// ============================================
// 【中级】Java 异常处理机制
// ============================================

import java.io.*;  // 导入IO相关类

// --- 自定义异常类 ---
class InsufficientFundsException extends Exception {
    // ↑ 继承Exception：自定义受检异常
    private double deficit;  // 差额

    public InsufficientFundsException(String message, double deficit) {
        super(message);  // 调用父类构造，设置异常消息
        this.deficit = deficit;
    }

    public double getDeficit() {
        return deficit;
    }
}

// --- 业务类 ---
class TransferService {
    public static void transfer(double amount, double balance)
            throws InsufficientFundsException {
        // ↑ throws：声明方法可能抛出的受检异常
        if (amount > balance) {
            double deficit = amount - balance;
            throw new InsufficientFundsException(
                "余额不足！需要¥" + amount + "，余额¥" + balance, deficit);
            // ↑ throw：抛出异常对象
        }
        System.out.println("转账成功: ¥" + amount);
    }
}

// --- 主类演示 ---
public class ExceptionDemo {
    public static void main(String[] args) {
        // --- 1. try-catch-finally ---
        System.out.println("=== try-catch-finally ===");
        try {
            // 可能抛出异常的代码
            int result = 10 / 0;  // 除零异常 ArithmeticException
        } catch (ArithmeticException e) {
            // catch：捕获指定类型的异常
            System.out.println("捕获异常: " + e.getMessage());
            // ↑ getMessage()：获取异常消息
        } finally {
            // finally：无论是否异常都会执行（清理资源）
            System.out.println("finally块：资源清理");
        }

        // --- 2. 多catch块 ---
        System.out.println("\n=== 多catch块 ===");
        try {
            String str = null;
            str.length();  // 空指针异常 NullPointerException
        } catch (NullPointerException e) {
            System.out.println("空指针异常: " + e);
        } catch (RuntimeException e) {
            // 父类异常放在后面（否则编译错误）
            System.out.println("运行时异常: " + e);
        }

        // --- 3. try-with-resources（自动关闭资源） ---
        System.out.println("\n=== try-with-resources ===");
        try (BufferedReader reader = new BufferedReader(
                new FileReader("test.txt"))) {
            // ↑ try()中声明资源，自动调用close()
            String line = reader.readLine();
            System.out.println("读取: " + line);
        } catch (FileNotFoundException e) {
            System.out.println("文件不存在: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO异常: " + e.getMessage());
        }

        // --- 4. 自定义异常 ---
        System.out.println("\n=== 自定义异常 ===");
        try {
            TransferService.transfer(1000, 500);
        } catch (InsufficientFundsException e) {
            System.out.println("转账失败: " + e.getMessage());
            System.out.println("差额: ¥" + e.getDeficit());
        }

        // --- 异常分类说明 ---
        System.out.println("\n=== Java异常分类 ===");
        System.out.println("受检异常(Checked)：必须处理，如IOException");
        System.out.println("运行时异常(Runtime)：可选处理，如NullPointerException");
        System.out.println("错误(Error)：无法处理，如OutOfMemoryError");
    }
}
```

**💡 代码解释**：Java异常分三类：(1)受检异常(Checked)——编译期强制处理(try-catch或throws)，如IOException；(2)运行时异常(RuntimeException)——编译期不检查，如NullPointerException；(3)错误(Error)——JVM级别问题，不应捕获。try-with-resources(JDK 7+)自动关闭实现AutoCloseable的资源。自定义异常继承Exception或RuntimeException。

**🔑 关键要点**：
- try-catch-finally处理异常，finally始终执行
- throw抛出异常，throws声明异常
- try-with-resources自动关闭资源（JDK 7+）
- 受检异常必须处理，运行时异常可选
- 多catch块时，子类异常在前，父类在后

---

### 4. 泛型编程

> **级别**：中级 | **概念**：泛型提供编译期类型安全，避免运行时ClassCastException。支持泛型类、泛型方法、通配符和类型边界。

```java
// ============================================
// 【中级】Java 泛型编程
// ============================================

import java.util.ArrayList;
import java.util.List;

// --- 泛型类：类型参数化 ---
class Box<T> {  // <T>：类型参数，T是占位符
    private T content;  // 使用类型参数声明成员变量

    public void set(T content) {  // 使用类型参数作为方法参数
        this.content = content;
    }

    public T get() {  // 使用类型参数作为返回值
        return content;
    }

    public boolean isEmpty() {
        return content == null;
    }
}

// --- 泛型方法 ---
class ArrayUtils {
    // 泛型方法：类型参数在返回值前声明
    public static <T> void swap(T[] array, int i, int j) {
        // ↑ <T>：声明这是一个泛型方法，T是类型参数
        T temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    // 泛型方法 + 类型边界
    public static <T extends Comparable<T>> T max(T a, T b) {
        // ↑ <T extends Comparable<T>>：T必须实现Comparable接口
        return a.compareTo(b) > 0 ? a : b;
        // ↑ compareTo()：Comparable接口的比较方法
    }
}

// --- 泛型通配符 ---
public class GenericsDemo {

    // 上界通配符：只读（生产者）
    public static double sumOfList(List<? extends Number> list) {
        // ↑ ? extends Number：接受Number及其子类型
        double sum = 0;
        for (Number n : list) {
            sum += n.doubleValue();
        }
        return sum;
    }

    // 下界通配符：只写（消费者）
    public static void addNumbers(List<? super Integer> list) {
        // ↑ ? super Integer：接受Integer及其父类型
        list.add(1);
        list.add(2);
        list.add(3);
    }

    public static void main(String[] args) {
        // --- 泛型类使用 ---
        Box<String> stringBox = new Box<>();
        // ↑ <String>：指定类型参数为String
        stringBox.set("Hello Generics");
        String content = stringBox.get();  // 无需强制转换
        System.out.println("Box内容: " + content);

        Box<Integer> intBox = new Box<>();
        intBox.set(100);
        System.out.println("整数Box: " + intBox.get());

        // --- 泛型方法使用 ---
        String[] names = {"张三", "李四", "王五"};
        System.out.println("\n交换前: " + java.util.Arrays.toString(names));
        ArrayUtils.swap(names, 0, 2);
        // ↑ 类型自动推断，无需显式指定<String>
        System.out.println("交换后: " + java.util.Arrays.toString(names));

        System.out.println("最大值: " + ArrayUtils.max(10, 20));

        // --- 通配符使用 ---
        List<Integer> intList = List.of(1, 2, 3, 4, 5);
        System.out.println("\n整数列表和: " + sumOfList(intList));

        List<Double> doubleList = List.of(1.5, 2.5, 3.5);
        System.out.println("浮点列表和: " + sumOfList(doubleList));

        // --- 泛型擦除说明 ---
        System.out.println("\n=== 泛型擦除 ===");
        System.out.println("编译后泛型信息被擦除，运行时不可用");
        System.out.println("Box<String>和Box<Integer>运行时是同一个类");
    }
}
```

**💡 代码解释**：Java泛型通过类型擦除实现：编译后泛型信息被移除，所有类型参数替换为Object或边界类型。PECS原则：Producer Extends（生产者用extends，只读），Consumer Super（消费者用super，只写）。泛型优势：(1)编译期类型检查；(2)消除强制类型转换；(3)实现通用算法。注意：泛型不支持基本类型，需用包装类。

**🔑 关键要点**：
- <T>定义类型参数，编译后类型擦除
- 泛型类：class Box<T>，泛型方法：<T>返回类型 方法名()
- PECS原则：? extends 只读，? super 只写
- 类型边界：<T extends Comparable<T>>
- 泛型不支持基本类型（int需用Integer）

---

### 5. IO流操作

> **级别**：中级 | **概念**：Java IO分为字节流(InputStream/OutputStream)和字符流(Reader/Writer)，Buffered类提供缓冲提升性能。

```java
// ============================================
// 【中级】Java IO流操作
// ============================================

import java.io.*;  // 导入IO相关类
import java.nio.file.*;  // NIO文件操作

public class IODemo {
    public static void main(String[] args) {
        String filePath = "test_io.txt";

        // --- 1. 字符流写入文件 ---
        System.out.println("=== 文件写入 ===");
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(filePath))) {
            // ↑ BufferedWriter：缓冲字符输出流，提高性能
            // ↑ FileWriter：文件字符输出流
            writer.write("第一行：Java IO操作\n");
            writer.write("第二行：字符流写入\n");
            writer.write("第三行：自动关闭资源\n");
            System.out.println("文件写入成功");
        } catch (IOException e) {
            System.out.println("写入失败: " + e.getMessage());
        }

        // --- 2. 字符流读取文件 ---
        System.out.println("\n=== 文件读取 ===");
        try (BufferedReader reader = new BufferedReader(
                new FileReader(filePath))) {
            // ↑ BufferedReader：缓冲字符输入流
            String line;
            while ((line = reader.readLine()) != null) {
                // ↑ readLine()：读取一行，返回null表示文件结束
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("读取失败: " + e.getMessage());
        }

        // --- 3. 字节流复制文件 ---
        System.out.println("\n=== 字节流复制 ===");
        String copyPath = "test_io_copy.txt";
        try (FileInputStream fis = new FileInputStream(filePath);
             FileOutputStream fos = new FileOutputStream(copyPath)) {
            // ↑ FileInputStream：文件字节输入流
            // ↑ FileOutputStream：文件字节输出流

            byte[] buffer = new byte[1024];  // 缓冲区：1KB
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                // ↑ read()：读取字节到缓冲区，返回读取的字节数
                // ↑ 返回-1表示文件结束
                fos.write(buffer, 0, bytesRead);
                // ↑ write(buf, offset, len)：写入指定范围的字节
            }
            System.out.println("文件复制成功: " + copyPath);
        } catch (IOException e) {
            System.out.println("复制失败: " + e.getMessage());
        }

        // --- 4. NIO Files工具类（JDK 7+） ---
        System.out.println("\n=== NIO Files工具类 ===");
        try {
            // 读取所有行
            List<String> lines = Files.readAllLines(Path.of(filePath));
            // ↑ Files.readAllLines()：一次性读取所有行
            System.out.println("文件共 " + lines.size() + " 行");

            // 写入字符串
            Files.writeString(Path.of("nio_test.txt"), "NIO写入的内容");
            // ↑ Files.writeString()：直接写入字符串

            // 读取全部内容
            String content = Files.readString(Path.of(filePath));
            System.out.println("\n文件内容:\n" + content);
        } catch (IOException e) {
            System.out.println("NIO操作失败: " + e.getMessage());
        }

        // --- 清理测试文件 ---
        try {
            Files.deleteIfExists(Path.of(filePath));
            Files.deleteIfExists(Path.of(copyPath));
            Files.deleteIfExists(Path.of("nio_test.txt"));
            System.out.println("\n测试文件已清理");
        } catch (IOException e) {
            System.out.println("清理失败: " + e.getMessage());
        }
    }
}
```

**💡 代码解释**：Java IO体系：(1)字节流——InputStream/OutputStream处理二进制数据（图片、视频）；(2)字符流——Reader/Writer处理文本数据，自动处理编码。Buffered类添加缓冲减少系统调用次数。try-with-resources确保流自动关闭。JDK 7+的NIO Files工具类提供了更简洁的API：readAllLines()、writeString()、readString()等。

**🔑 关键要点**：
- 字节流(InputStream/OutputStream)处理二进制
- 字符流(Reader/Writer)处理文本，自动编码
- Buffered缓冲类减少系统调用，提升性能
- try-with-resources自动关闭流资源
- JDK 7+ Files工具类提供简洁的文件操作API

---

### 6. 多线程基础

> **级别**：中级 | **概念**：Java通过Thread类和Runnable接口创建线程，synchronized和volatile保证线程安全，wait/notify实现线程通信。

```java
// ============================================
// 【中级】Java 多线程基础
// ============================================

// --- 方式一：继承Thread类 ---
class MyThread extends Thread {
    private String threadName;

    public MyThread(String name) {
        this.threadName = name;
    }

    @Override
    public void run() {  // run()：线程执行的代码
        for (int i = 1; i <= 3; i++) {
            System.out.println(threadName + " 执行第 " + i + " 次");
            try {
                Thread.sleep(500);  // sleep()：线程休眠500毫秒
            } catch (InterruptedException e) {
                System.out.println(threadName + " 被中断");
            }
        }
    }
}

// --- 方式二：实现Runnable接口（推荐） ---
class MyRunnable implements Runnable {
    private String name;

    public MyRunnable(String name) {
        this.name = name;
    }

    @Override
    public void run() {
        for (int i = 1; i <= 3; i++) {
            System.out.println("Runnable-" + name + " 执行第 " + i + " 次");
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

// --- 线程安全：synchronized ---
class Counter {
    private int count = 0;

    // synchronized方法：同一时刻只有一个线程能执行
    public synchronized void increment() {
        // ↑ synchronized：获取对象锁
        count++;
    }

    public synchronized int getCount() {
        return count;
    }
}

// --- 主类 ---
public class ThreadDemo {
    public static void main(String[] args) throws InterruptedException {
        // --- 创建并启动线程 ---
        System.out.println("=== 线程创建 ===");
        MyThread t1 = new MyThread("线程A");

        Thread t2 = new Thread(new MyRunnable("B"));
        // ↑ 将Runnable对象传给Thread构造方法

        t1.start();  // start()：启动线程（调用run()方法）
        t2.start();
        // 注意：直接调用run()不会启动新线程！

        // 等待线程执行完毕
        t1.join();  // join()：等待线程结束
        t2.join();
        System.out.println("所有线程执行完毕\n");

        // --- 线程安全演示 ---
        System.out.println("=== 线程安全 ===");
        Counter counter = new Counter();

        // 创建多个线程同时操作同一个计数器
        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    counter.increment();
                }
            });
            threads[i].start();
        }

        // 等待所有线程完成
        for (Thread t : threads) {
            t.join();
        }

        System.out.println("最终计数: " + counter.getCount());
        // ↑ 有synchronized保护，结果应为5000
        System.out.println("预期: 5000");
    }
}
```

**💡 代码解释**：Java多线程两种创建方式：(1)继承Thread类——简单但单继承限制；(2)实现Runnable接口——更灵活，推荐使用。synchronized是Java内置锁机制，保证同一时刻只有一个线程执行同步代码块。volatile保证变量可见性（不保证原子性）。线程状态：NEW→RUNNABLE→BLOCKED/WAITING/TIMED_WAITING→TERMINATED。start()启动线程，run()定义执行逻辑。

**🔑 关键要点**：
- 继承Thread或实现Runnable创建线程（推荐Runnable）
- start()启动线程，run()定义执行逻辑
- synchronized保证线程安全（互斥访问）
- join()等待线程结束，sleep()休眠
- volatile保证可见性，不保证原子性

---

### 7. Lambda表达式与Stream API

> **级别**：中级 | **概念**：Lambda简化匿名类写法，Stream API提供声明式集合操作，支持filter/map/reduce等函数式编程范式。

```java
// ============================================
// 【中级】Java Lambda表达式与Stream API
// ============================================

import java.util.*;
import java.util.stream.*;
// ↑ stream包：Stream API

public class LambdaStreamDemo {
    public static void main(String[] args) {
        // --- Lambda表达式基础 ---
        // 传统匿名类写法
        Runnable oldWay = new Runnable() {
            @Override
            public void run() {
                System.out.println("传统匿名类");
            }
        };

        // Lambda写法：() -> { 函数体 }
        Runnable lambdaWay = () -> System.out.println("Lambda表达式");
        // ↑ ()：参数列表（此处无参数）
        // ↑ ->：Lambda运算符
        // ↑ 表达式体：单行可省略{}和return

        oldWay.run();
        lambdaWay.run();

        // --- Stream API：过滤、映射、收集 ---
        List<String> names = Arrays.asList("张三", "李四", "王五", "赵六", "钱七");

        System.out.println("\n=== Stream过滤 ===");
        List<String> filtered = names.stream()
            // ↑ stream()：将集合转为流
            .filter(name -> name.length() == 2)
            // ↑ filter()：过滤，保留返回true的元素
            // ↑ name -> name.length() == 2：Lambda表达式
            .collect(Collectors.toList());
            // ↑ collect()：将流收集为集合
        System.out.println("两个字的名字: " + filtered);

        // --- Stream映射 ---
        System.out.println("\n=== Stream映射 ===");
        List<Integer> nameLengths = names.stream()
            .map(String::length)  // map()：转换每个元素
            // ↑ String::length：方法引用，等价于 s -> s.length()
            .collect(Collectors.toList());
        System.out.println("名字长度: " + nameLengths);

        // --- 排序与去重 ---
        List<Integer> numbers = Arrays.asList(3, 1, 4, 1, 5, 9, 2, 6);
        List<Integer> sorted = numbers.stream()
            .distinct()   // distinct()：去重
            .sorted()     // sorted()：自然排序
            .collect(Collectors.toList());
        System.out.println("\n排序去重: " + sorted);

        // --- 统计操作 ---
        System.out.println("\n=== 统计操作 ===");
        long count = numbers.stream()
            .filter(n -> n > 3)
            .count();  // count()：计数
        System.out.println("大于3的元素个数: " + count);

        int sum = numbers.stream()
            .mapToInt(Integer::intValue)  // mapToInt()：转为IntStream
            .sum();  // sum()：求和
        System.out.println("总和: " + sum);

        OptionalDouble avg = numbers.stream()
            .mapToInt(Integer::intValue)
            .average();  // average()：求平均值
        avg.ifPresent(a -> System.out.println("平均值: " + a));

        // --- 分组操作 ---
        System.out.println("\n=== 分组操作 ===");
        List<Student> students = Arrays.asList(
            new Student("张三", "一班", 85),
            new Student("李四", "一班", 92),
            new Student("王五", "二班", 78),
            new Student("赵六", "二班", 88)
        );

        Map<String, List<Student>> byClass = students.stream()
            .collect(Collectors.groupingBy(Student::getClassName));
            // ↑ groupingBy()：按指定字段分组
        System.out.println("按班级分组:");
        byClass.forEach((className, stuList) ->
            System.out.println("  " + className + ": " + stuList));
    }

    // 内部类：用于演示分组
    static class Student {
        String name, className;
        int score;
        Student(String n, String c, int s) { name = n; className = c; score = s; }
        String getClassName() { return className; }
        @Override public String toString() { return name + "(" + score + ")"; }
    }
}
```

**💡 代码解释**：Lambda表达式是JDK 8最重要的特性，语法：(参数) -> {函数体}。Stream API提供声明式数据处理：filter(过滤)、map(转换)、sorted(排序)、distinct(去重)、collect(收集)。Stream是惰性求值的，只有遇到终端操作(count/collect/forEach)时才执行。方法引用(String::length)是Lambda的简写形式。groupingBy实现SQL GROUP BY效果。

**🔑 关键要点**：
- Lambda语法：(参数) -> {函数体}，单行可省略{}
- Stream惰性求值，终端操作触发执行
- filter过滤、map转换、sorted排序、distinct去重
- collect(Collectors.toList())收集结果
- groupingBy()实现分组，方法引用简化Lambda

---

### 8. Optional避免空指针

> **级别**：中级 | **概念**：Optional是容器类，优雅处理可能为null的值，避免NullPointerException，提供链式安全操作。

```java
// ============================================
// 【中级】Java Optional 避免空指针
// ============================================

import java.util.Optional;

public class OptionalDemo {

    // 模拟数据库查询：可能返回null
    private static String findUserById(int id) {
        if (id == 1) return "张三";
        if (id == 2) return "李四";
        return null;  // 用户不存在
    }

    // 推荐：返回Optional而非null
    private static Optional<String> findUserSafe(int id) {
        // ↑ Optional<String>：明确表达"可能为空"的语义
        if (id == 1) return Optional.of("张三");
        // ↑ Optional.of()：包装非null值
        if (id == 2) return Optional.of("李四");
        return Optional.empty();  // empty()：返回空Optional
        // ↑ 明确表达"无值"，而非返回null
    }

    public static void main(String[] args) {
        // --- 传统方式 vs Optional ---
        System.out.println("=== 传统null检查 ===");
        String user = findUserById(3);
        if (user != null) {  // 传统防御式检查
            System.out.println("找到用户: " + user.toUpperCase());
        } else {
            System.out.println("用户不存在");
        }

        System.out.println("\n=== Optional方式 ===");

        // 创建Optional
        Optional<String> opt1 = Optional.of("Hello");
        // ↑ of()：值不能为null，否则抛NullPointerException
        Optional<String> opt2 = Optional.ofNullable(null);
        // ↑ ofNullable()：值可以为null，自动转为empty()
        Optional<String> opt3 = Optional.empty();
        // ↑ empty()：创建空Optional

        // --- 判断是否有值 ---
        System.out.println("opt1有值? " + opt1.isPresent());
        // ↑ isPresent()：是否有值
        System.out.println("opt3有值? " + opt3.isPresent());

        // --- 获取值 ---
        System.out.println("opt1的值: " + opt1.get());
        // ↑ get()：获取值（空Optional调用会抛异常，慎用）

        // --- 安全的默认值 ---
        String value1 = opt3.orElse("默认值");
        // ↑ orElse()：有值返回值，无值返回默认值
        System.out.println("opt3默认值: " + value1);

        String value2 = opt3.orElseGet(() -> "动态默认值");
        // ↑ orElseGet()：惰性求值，仅在无值时调用Supplier
        System.out.println("opt3动态默认: " + value2);

        // --- 条件执行 ---
        opt1.ifPresent(v -> System.out.println("opt1存在: " + v));
        // ↑ ifPresent()：有值时执行操作

        // --- 链式操作 ---
        System.out.println("\n=== 链式操作 ===");
        Optional<String> result = findUserSafe(1)
            .map(String::toUpperCase)  // map()：转换值
            // ↑ 有值时转换为大写，无值时返回empty()
            .filter(s -> s.length() > 2)  // filter()：条件过滤
            // ↑ 满足条件保留，不满足返回empty()
            .map(s -> "用户: " + s);

        System.out.println("链式结果: " + result.orElse("无结果"));

        // --- 抛出异常 ---
        try {
            findUserSafe(3).orElseThrow(
                () -> new RuntimeException("用户不存在"));
            // ↑ orElseThrow()：无值时抛出指定异常
        } catch (RuntimeException e) {
            System.out.println("捕获异常: " + e.getMessage());
        }

        // --- 最佳实践 ---
        System.out.println("\n=== Optional最佳实践 ===");
        System.out.println("1. 方法返回Optional而非null");
        System.out.println("2. 不要用Optional作为字段类型");
        System.out.println("3. 不要用Optional作为方法参数");
        System.out.println("4. 用orElseGet()替代orElse()（惰性求值）");
    }
}
```

**💡 代码解释**：Optional是JDK 8引入的容器类，解决NullPointerException问题。核心方法：(1)创建——of()/ofNullable()/empty()；(2)判断——isPresent()/ifPresent()；(3)获取——get()/orElse()/orElseGet()/orElseThrow()；(4)转换——map()/flatMap()/filter()。最佳实践：方法返回值用Optional，但不要用作字段或参数类型。orElseGet()惰性求值优于orElse()。

**🔑 关键要点**：
- Optional.of()包装非null值，ofNullable()处理可能为null
- orElse()/orElseGet()提供默认值
- map()/filter()实现链式安全操作
- orElseThrow()无值时抛出异常
- 方法返回值用Optional，字段和参数不用

---

## 高级精通

### 1. 并发编程（线程池与锁）

> **级别**：高级 | **概念**：ExecutorService管理线程池，ReentrantLock提供更灵活的锁，CountDownLatch/CyclicBarrier协调线程，原子类保证无锁安全。

```java
// ============================================
// 【高级】Java 并发编程：线程池与锁机制
// ============================================

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
// ↑ concurrent包：Java并发工具集

public class ConcurrencyDemo {
    public static void main(String[] args) throws Exception {
        // --- 1. 线程池（ExecutorService） ---
        System.out.println("=== 线程池 ===");

        // 创建固定大小线程池
        ExecutorService executor = Executors.newFixedThreadPool(3);
        // ↑ newFixedThreadPool(3)：创建3个线程的线程池
        // ↑ 线程池复用线程，避免频繁创建销毁开销

        // 提交任务
        Future<String> future = executor.submit(() -> {
            // ↑ submit()：提交Callable任务，返回Future
            Thread.sleep(500);
            return "任务完成";
        });

        // 提交多个任务
        for (int i = 1; i <= 5; i++) {
            final int taskId = i;
            executor.execute(() -> {
                // ↑ execute()：提交Runnable任务，无返回值
                System.out.println("  任务" + taskId + " 由线程 " +
                    Thread.currentThread().getName() + " 执行");
            });
        }

        System.out.println("Future结果: " + future.get());
        // ↑ get()：阻塞等待任务完成并获取结果

        executor.shutdown();  // shutdown()：优雅关闭线程池
        executor.awaitTermination(2, TimeUnit.SECONDS);
        // ↑ awaitTermination()：等待所有任务完成

        // --- 2. ReentrantLock：可重入锁 ---
        System.out.println("\n=== ReentrantLock ===");
        ReentrantLock lock = new ReentrantLock();
        // ↑ ReentrantLock：可重入互斥锁，比synchronized更灵活

        Runnable lockTask = () -> {
            lock.lock();  // lock()：获取锁
            try {
                System.out.println(Thread.currentThread().getName()
                    + " 获取锁，执行业务逻辑");
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();  // unlock()：释放锁（必须在finally中）
            }
        };

        new Thread(lockTask, "线程A").start();
        new Thread(lockTask, "线程B").start();
        Thread.sleep(1000);

        // --- 3. CountDownLatch：倒计时协调 ---
        System.out.println("\n=== CountDownLatch ===");
        int workerCount = 3;
        CountDownLatch latch = new CountDownLatch(workerCount);
        // ↑ CountDownLatch：计数器，等待所有子任务完成

        for (int i = 0; i < workerCount; i++) {
            final int id = i;
            new Thread(() -> {
                System.out.println("  工作者" + id + " 完成工作");
                latch.countDown();  // countDown()：计数器减1
            }).start();
        }

        latch.await();  // await()：阻塞直到计数器归零
        System.out.println("所有工作者完成，主线程继续执行");

        // --- 4. 原子类：无锁线程安全 ---
        System.out.println("\n=== AtomicInteger ===");
        AtomicInteger atomicInt = new AtomicInteger(0);
        // ↑ AtomicInteger：基于CAS的无锁原子操作

        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    atomicInt.incrementAndGet();
                    // ↑ incrementAndGet()：原子自增（无锁）
                }
            });
            threads[i].start();
        }

        for (Thread t : threads) t.join();
        System.out.println("原子计数结果: " + atomicInt.get());
        System.out.println("预期: 5000");
    }
}
```

**💡 代码解释**：Java的java.util.concurrent包(JUC)提供了丰富的并发工具。(1)线程池——Executors工厂类创建，避免了线程创建销毁开销，核心参数：核心线程数、最大线程数、队列类型、拒绝策略。(2)ReentrantLock——可重入锁，支持公平锁、可中断获取锁、tryLock超时获取。(3)CountDownLatch——一次性倒计时门闩，适合等待多个任务完成。(4)原子类——基于CAS，性能优于synchronized。

**🔑 关键要点**：
- Executors.newFixedThreadPool()创建固定大小线程池
- submit()返回Future，execute()无返回值
- ReentrantLock比synchronized更灵活，支持tryLock()
- CountDownLatch等待多个任务完成
- AtomicInteger基于CAS实现无锁原子操作

---

### 2. JVM原理（内存模型与GC）

> **级别**：高级 | **概念**：JVM内存分为堆、栈、方法区等区域，GC通过标记-清除、复制、标记-整理等算法回收内存，不同GC器适用不同场景。

```java
// ============================================
// 【高级】Java JVM原理：内存模型与GC
// ============================================

public class JVMDemo {

    // 静态变量：存储在方法区（JDK 8+元空间Metaspace）
    private static final String APP_NAME = "JVM-Demo";

    // 实例变量：存储在堆中
    private String instanceData;

    public static void main(String[] args) {
        // ★ JVM内存模型（JDK 8+） ★
        // ┌──────────────────────────────────┐
        // │         程序计数器(PC Register)    │  线程私有
        // │         虚拟机栈(VM Stack)         │  线程私有，栈帧存储局部变量
        // │         本地方法栈(Native Stack)   │  线程私有
        // ├──────────────────────────────────┤
        // │         堆(Heap)                  │  线程共享，存放对象实例
        // │    ├── 新生代(Young Gen)          │  Eden + S0 + S1
        // │    └── 老年代(Old Gen)            │  长期存活对象
        // ├──────────────────────────────────┤
        // │         元空间(Metaspace)         │  线程共享，存储类元数据
        // └──────────────────────────────────┘

        System.out.println("=== JVM内存模型 ===");
        System.out.println("堆(Heap): 存放对象和数组，GC主要区域");
        System.out.println("虚拟机栈(Stack): 存放局部变量、方法调用栈帧");
        System.out.println("元空间(Metaspace): 存放类元数据（替代永久代）");

        // --- 演示堆内存分配 ---
        System.out.println("\n=== 堆内存使用 ===");
        Runtime runtime = Runtime.getRuntime();
        // ↑ Runtime：JVM运行时环境

        long maxMemory = runtime.maxMemory();  // 最大可用内存
        long totalMemory = runtime.totalMemory();  // 已分配内存
        long freeMemory = runtime.freeMemory();  // 空闲内存

        System.out.println("最大内存: " + maxMemory / 1024 / 1024 + " MB");
        System.out.println("已分配: " + totalMemory / 1024 / 1024 + " MB");
        System.out.println("空闲: " + freeMemory / 1024 / 1024 + " MB");
        System.out.println("已使用: " +
            (totalMemory - freeMemory) / 1024 / 1024 + " MB");

        // --- GC算法说明 ---
        System.out.println("\n=== GC算法与收集器 ===");
        System.out.println("1. 标记-清除(Mark-Sweep): 标记存活对象，清除未标记");
        System.out.println("   → 缺点: 产生内存碎片");
        System.out.println("2. 复制(Copying): 将存活对象复制到新区域");
        System.out.println("   → 用于新生代(Eden→Survivor)，高效但浪费空间");
        System.out.println("3. 标记-整理(Mark-Compact): 标记后移动对象消除碎片");
        System.out.println("   → 用于老年代");

        System.out.println("\n=== 常用GC收集器 ===");
        System.out.println("Serial GC: 单线程，适合客户端应用");
        System.out.println("Parallel GC: 多线程，吞吐量优先（JDK 8默认）");
        System.out.println("CMS GC: 低延迟，并发标记清除");
        System.out.println("G1 GC: 区域化，平衡吞吐量和延迟（JDK 9+默认）");
        System.out.println("ZGC/Shenandoah: 超低延迟（<10ms），大堆内存");

        // --- 对象生命周期 ---
        System.out.println("\n=== 对象生命周期 ===");
        System.out.println("1. 创建：new关键字在Eden区分配内存");
        System.out.println("2. Minor GC：存活对象复制到Survivor区");
        System.out.println("3. 多次Minor GC后：晋升到老年代");
        System.out.println("4. Major GC/Full GC：清理老年代");
        System.out.println("5. 回收：无引用对象被GC回收");

        // --- JVM参数建议 ---
        System.out.println("\n=== 常用JVM参数 ===");
        System.out.println("-Xms512m  : 初始堆大小");
        System.out.println("-Xmx2g    : 最大堆大小");
        System.out.println("-Xss256k  : 线程栈大小");
        System.out.println("-XX:+UseG1GC : 使用G1垃圾收集器");
        System.out.println("-XX:MaxGCPauseMillis=200 : GC最大暂停时间");
    }
}
```

**💡 代码解释**：JVM内存模型是Java性能调优的基础。堆是最重要的区域，分为新生代(Eden+两个Survivor)和老年代。Minor GC发生在新生代，频率高但速度快；Full GC发生在整个堆，频率低但暂停时间长。GC选择：低延迟用G1/ZGC，高吞吐量用Parallel GC。监控工具：jstat(命令行)、jvisualvm(图形化)、Arthas(阿里开源)。

**🔑 关键要点**：
- 堆分新生代(Eden+Survivor)和老年代
- Minor GC频繁快速，Full GC影响大需避免
- G1 GC是JDK 9+默认，平衡吞吐量和延迟
- -Xms/-Xmx设置堆大小，生产环境建议相同值
- 用jstat、jvisualvm、Arthas监控GC情况

---

### 3. 反射与注解

> **级别**：高级 | **概念**：反射在运行时动态获取类信息和操作对象，注解提供元数据标记，两者结合是Spring等框架的核心基础。

```java
// ============================================
// 【高级】Java 反射与注解
// ============================================

import java.lang.annotation.*;
import java.lang.reflect.*;
// ↑ reflect包：反射API

// --- 自定义注解 ---
@Retention(RetentionPolicy.RUNTIME)
// ↑ @Retention：注解保留策略（RUNTIME=运行时可通过反射读取）
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
// ↑ @Target：注解可以应用的目标
@interface Info {
    // ↑ @interface：定义注解
    String value() default "";  // 注解属性，default设置默认值
    String author() default "unknown";
}

// 标记注解：用于标记方法需要权限检查
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface RequiresAuth {
    String role() default "admin";
}

// --- 使用注解的类 ---
@Info(value = "用户服务类", author = "张三")
class UserService {
    @Info("用户姓名")
    private String name;

    @Info("用户年龄")
    private int age;

    public UserService(String name, int age) {
        this.name = name;
        this.age = age;
    }

    @RequiresAuth(role = "admin")
    @Info("获取用户信息")
    public String getUserInfo() {
        return name + ", " + age + "岁";
    }

    @RequiresAuth(role = "user")
    public void updateName(String newName) {
        this.name = newName;
        System.out.println("名称已更新为: " + newName);
    }

    private void secretMethod() {
        // 私有方法：反射可以访问
        System.out.println("这是私有方法！");
    }
}

// --- 主类：反射演示 ---
public class ReflectionDemo {
    public static void main(String[] args) throws Exception {
        // --- 1. 获取Class对象（三种方式） ---
        Class<?> clazz = UserService.class;  // 方式1：类字面量
        // Class<?> clazz = Class.forName("UserService");  // 方式2：全限定名
        // Class<?> clazz = obj.getClass();  // 方式3：实例方法

        System.out.println("=== 类信息 ===");
        System.out.println("类名: " + clazz.getName());
        System.out.println("简单名: " + clazz.getSimpleName());

        // --- 2. 读取注解 ---
        if (clazz.isAnnotationPresent(Info.class)) {
            Info info = clazz.getAnnotation(Info.class);
            System.out.println("类注解: value=" + info.value() +
                ", author=" + info.author());
        }

        // --- 3. 反射创建对象 ---
        System.out.println("\n=== 反射创建对象 ===");
        Constructor<?> constructor = clazz.getConstructor(
            String.class, int.class);
        // ↑ getConstructor()：获取指定参数类型的公开构造方法
        Object userService = constructor.newInstance("李四", 25);
        // ↑ newInstance()：调用构造方法创建实例

        // --- 4. 反射操作字段 ---
        System.out.println("\n=== 反射操作字段 ===");
        Field[] fields = clazz.getDeclaredFields();
        // ↑ getDeclaredFields()：获取所有字段（包括私有）
        for (Field field : fields) {
            System.out.println("字段: " + field.getName() +
                ", 类型: " + field.getType().getSimpleName());
        }

        // 访问私有字段
        Field nameField = clazz.getDeclaredField("name");
        nameField.setAccessible(true);  // 绕过访问控制
        String nameValue = (String) nameField.get(userService);
        System.out.println("私有字段name的值: " + nameValue);

        // --- 5. 反射调用方法 ---
        System.out.println("\n=== 反射调用方法 ===");
        Method getUserInfo = clazz.getMethod("getUserInfo");
        // ↑ getMethod()：获取公开方法
        String result = (String) getUserInfo.invoke(userService);
        // ↑ invoke()：调用方法
        System.out.println("调用getUserInfo: " + result);

        // 调用带注解的方法
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(RequiresAuth.class)) {
                RequiresAuth auth = method.getAnnotation(RequiresAuth.class);
                System.out.println("方法 " + method.getName() +
                    " 需要权限: " + auth.role());
            }
        }

        // 调用私有方法
        Method secret = clazz.getDeclaredMethod("secretMethod");
        secret.setAccessible(true);  // 绕过访问控制
        secret.invoke(userService);
    }
}
```

**💡 代码解释**：反射和注解是Java框架的基石。反射允许运行时动态获取类信息、创建对象、调用方法、访问字段，即使它们是私有的。注解(.annotation)提供元数据标记，配合反射实现声明式编程。Spring的@Autowired、@Transactional等注解都是通过反射实现的。反射的代价：性能开销、类型安全丧失、封装性破坏，需谨慎使用。

**🔑 关键要点**：
- Class.forName()/类名.class/对象.getClass()获取Class对象
- getConstructor()获取构造方法，newInstance()创建实例
- getMethod()获取方法，invoke()调用
- getDeclaredField()获取字段，setAccessible()绕过权限
- 注解+反射是Spring等框架的核心机制

---

### 4. Spring Boot 实战

> **级别**：高级 | **概念**：Spring Boot简化Spring应用开发，自动配置、起步依赖、内嵌服务器，快速构建RESTful API和微服务。

```java
// ============================================
// 【高级】Java Spring Boot RESTful API 实战
// 依赖: spring-boot-starter-web, spring-boot-starter-data-jpa, h2
// ============================================

// --- 1. 主启动类 ---
// @SpringBootApplication  // 组合注解：@Configuration + @EnableAutoConfiguration + @ComponentScan
// public class Application {
//     public static void main(String[] args) {
//         SpringApplication.run(Application.class, args);
//         // ↑ run()：启动Spring Boot应用，自动配置并启动内嵌Tomcat
//     }
// }

// --- 2. 实体类（Entity） ---
// @Entity  // 标记为JPA实体，映射到数据库表
// @Table(name = "users")  // 指定表名
// public class User {
//     @Id  // 主键
//     @GeneratedValue(strategy = GenerationType.IDENTITY)  // 自增策略
//     private Long id;
//
//     @Column(nullable = false, length = 50)  // 列约束
//     private String name;
//
//     @Column(unique = true)  // 唯一约束
//     private String email;
//
//     private Integer age;
//
//     // 无参构造方法（JPA要求）
//     public User() {}
//
//     // 全参构造方法
//     public User(String name, String email, Integer age) {
//         this.name = name;
//         this.email = email;
//         this.age = age;
//     }
//
//     // Getter和Setter（省略）
// }

// --- 3. 数据访问层（Repository） ---
// @Repository  // 标记为数据访问组件
// public interface UserRepository extends JpaRepository<User, Long> {
//     // ↑ JpaRepository<实体类, 主键类型>：提供CRUD方法
//     // Spring Data JPA自动实现此接口
//
//     // 自定义查询方法：根据方法名自动生成SQL
//     List<User> findByAgeGreaterThan(Integer age);
//     // ↑ 自动生成：SELECT * FROM users WHERE age > ?
//
//     Optional<User> findByEmail(String email);
//     // ↑ 根据邮箱查找用户
//
//     @Query("SELECT u FROM User u WHERE u.name LIKE %:keyword%")
//     // ↑ @Query：自定义JPQL查询
//     List<User> searchByName(@Param("keyword") String keyword);
// }

// --- 4. 服务层（Service） ---
// @Service  // 标记为业务逻辑组件
// @Transactional  // 开启事务管理
// public class UserService {
//
//     private final UserRepository userRepository;
//
//     // 构造方法注入（推荐方式）
//     public UserService(UserRepository userRepository) {
//         this.userRepository = userRepository;
//     }
//
//     public List<User> getAllUsers() {
//         return userRepository.findAll();  // 查询所有用户
//     }
//
//     public User createUser(User user) {
//         return userRepository.save(user);  // 保存用户
//     }
//
//     public User getUserById(Long id) {
//         return userRepository.findById(id)
//             .orElseThrow(() -> new RuntimeException("用户不存在: " + id));
//     }
// }

// --- 5. 控制器层（Controller） ---
// @RestController  // @Controller + @ResponseBody：返回JSON
// @RequestMapping("/api/users")  // 路径前缀
// public class UserController {
//
//     private final UserService userService;
//
//     public UserController(UserService userService) {
//         this.userService = userService;
//     }
//
//     @GetMapping  // 处理GET请求
//     public ResponseEntity<List<User>> getAllUsers() {
//         // ↑ ResponseEntity：封装HTTP响应（状态码、头、体）
//         return ResponseEntity.ok(userService.getAllUsers());
//         // ↑ ok()：返回200状态码
//     }
//
//     @GetMapping("/{id}")  // 路径变量
//     public ResponseEntity<User> getUser(@PathVariable Long id) {
//         // ↑ @PathVariable：绑定URL路径变量
//         return ResponseEntity.ok(userService.getUserById(id));
//     }
//
//     @PostMapping  // 处理POST请求
//     public ResponseEntity<User> createUser(
//             @Valid @RequestBody User user) {
//         // ↑ @RequestBody：将请求体JSON绑定到对象
//         // ↑ @Valid：触发JSR-303数据验证
//         User saved = userService.createUser(user);
//         return ResponseEntity.status(HttpStatus.CREATED).body(saved);
//         // ↑ status(201)：返回201 Created
//     }
//
//     @DeleteMapping("/{id}")  // 处理DELETE请求
//     public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
//         userService.deleteUser(id);
//         return ResponseEntity.noContent().build();
//         // ↑ noContent()：返回204 No Content
//     }
//
//     // 统一异常处理
//     @ExceptionHandler(RuntimeException.class)
//     public ResponseEntity<String> handleException(RuntimeException e) {
//         return ResponseEntity.badRequest().body(e.getMessage());
//     }
// }

// --- 6. 配置文件 application.yml ---
// server:
//   port: 8080  # 服务端口
// spring:
//   datasource:
//     url: jdbc:h2:mem:testdb  # H2内存数据库
//   jpa:
//     hibernate:
//       ddl-auto: update  # 自动更新表结构
//     show-sql: true  # 显示SQL语句

// --- 可运行的简化版 ---
public class SpringBootDemo {
    public static void main(String[] args) {
        System.out.println("=== Spring Boot 分层架构 ===");
        System.out.println("Controller层(@RestController): 处理HTTP请求，参数校验");
        System.out.println("Service层(@Service): 业务逻辑，事务管理");
        System.out.println("Repository层(@Repository): 数据访问，CRUD操作");
        System.out.println("Entity层(@Entity): 数据模型，ORM映射");
        System.out.println("\n=== 核心注解 ===");
        System.out.println("@SpringBootApplication: 启动类注解");
        System.out.println("@RestController: REST控制器");
        System.out.println("@GetMapping/@PostMapping/@PutMapping/@DeleteMapping");
        System.out.println("@PathVariable: 路径参数");
        System.out.println("@RequestBody: 请求体参数");
        System.out.println("@Autowired: 依赖注入（推荐构造方法注入）");
        System.out.println("@Service/@Repository/@Component: 组件扫描");
        System.out.println("\n=== 运行命令 ===");
        System.out.println("mvn spring-boot:run");
        System.out.println("java -jar target/app.jar");
    }
}
```

**💡 代码解释**：Spring Boot是Java企业级开发的事实标准。分层架构：Controller(API层)→Service(业务层)→Repository(数据层)。核心特性：(1)自动配置——根据依赖自动配置Bean；(2)起步依赖——一站式引入相关依赖；(3)内嵌服务器——无需部署Tomcat；(4)Actuator——生产级监控端点。依赖注入推荐使用构造方法注入，配合Lombok的@RequiredArgsConstructor更简洁。

**🔑 关键要点**：
- 三层架构：Controller(API) → Service(业务) → Repository(数据)
- @SpringBootApplication启动自动配置
- @RestController返回JSON，@RequestMapping定义路径
- JpaRepository提供CRUD，方法名自动生成SQL
- 依赖注入推荐构造方法注入（@Autowired可省略）

---

### 5. 设计模式实战

> **级别**：高级 | **概念**：掌握单例、工厂、策略、观察者等经典设计模式在Java中的实现，结合Spring框架理解其实际应用。

```java
// ============================================
// 【高级】Java 设计模式实战
// ============================================

import java.util.*;

// --- 1. 单例模式（饿汉式 + 双重检查锁） ---
class Singleton {
    // volatile：禁止指令重排序，保证可见性
    private static volatile Singleton instance;

    // 私有构造方法：防止外部new创建
    private Singleton() {
        System.out.println("单例对象创建");
    }

    // 双重检查锁定（DCL）：线程安全且高效
    public static Singleton getInstance() {
        if (instance == null) {  // 第一次检查：避免不必要的同步
            synchronized (Singleton.class) {  // 同步块
                if (instance == null) {  // 第二次检查：确保只创建一次
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }

    public void doSomething() {
        System.out.println("单例方法执行");
    }
}

// --- 2. 工厂模式 ---
// 产品接口
interface PaymentProcessor {
    void pay(double amount);
}

// 具体产品
class AlipayProcessor implements PaymentProcessor {
    @Override
    public void pay(double amount) {
        System.out.println("支付宝支付: ¥" + amount);
    }
}

class WechatPayProcessor implements PaymentProcessor {
    @Override
    public void pay(double amount) {
        System.out.println("微信支付: ¥" + amount);
    }
}

// 工厂类
class PaymentFactory {
    private static final Map<String, PaymentProcessor> cache = new HashMap<>();

    static {
        // 初始化时注册所有处理器
        cache.put("alipay", new AlipayProcessor());
        cache.put("wechat", new WechatPayProcessor());
    }

    public static PaymentProcessor getProcessor(String type) {
        PaymentProcessor processor = cache.get(type);
        if (processor == null) {
            throw new IllegalArgumentException("不支持的支付方式: " + type);
        }
        return processor;
    }
}

// --- 3. 策略模式 ---
// 策略接口
interface DiscountStrategy {
    double calculate(double price);
}

// 具体策略
class NoDiscount implements DiscountStrategy {
    public double calculate(double price) { return price; }
}

class PercentageDiscount implements DiscountStrategy {
    private final double percent;
    public PercentageDiscount(double percent) { this.percent = percent; }
    public double calculate(double price) {
        return price * (1 - percent / 100);
    }
}

// 上下文类
class Order {
    private final double price;
    private DiscountStrategy strategy;  // 策略可动态切换

    public Order(double price, DiscountStrategy strategy) {
        this.price = price;
        this.strategy = strategy;
    }

    public void setStrategy(DiscountStrategy strategy) {
        this.strategy = strategy;
    }

    public double getFinalPrice() {
        return strategy.calculate(price);
    }
}

// --- 主类 ---
public class DesignPatternDemo {
    public static void main(String[] args) {
        // 单例模式
        System.out.println("=== 单例模式 ===");
        Singleton s1 = Singleton.getInstance();
        Singleton s2 = Singleton.getInstance();
        System.out.println("s1 == s2: " + (s1 == s2));

        // 工厂模式
        System.out.println("\n=== 工厂模式 ===");
        PaymentProcessor alipay = PaymentFactory.getProcessor("alipay");
        alipay.pay(99.99);
        PaymentProcessor wechat = PaymentFactory.getProcessor("wechat");
        wechat.pay(199.00);

        // 策略模式
        System.out.println("\n=== 策略模式 ===");
        Order order = new Order(100, new NoDiscount());
        System.out.println("原价: ¥" + order.getFinalPrice());
        order.setStrategy(new PercentageDiscount(20));
        System.out.println("8折后: ¥" + order.getFinalPrice());

        // 设计模式总结
        System.out.println("\n=== 设计模式在Spring中的应用 ===");
        System.out.println("单例模式：Spring Bean默认是单例");
        System.out.println("工厂模式：BeanFactory/ApplicationContext");
        System.out.println("策略模式：Resource接口的不同实现");
        System.out.println("代理模式：AOP（面向切面编程）");
        System.out.println("模板方法：JdbcTemplate/RestTemplate");
        System.out.println("观察者模式：ApplicationEvent事件机制");
    }
}
```

**💡 代码解释**：设计模式在Java中广泛使用，Spring框架更是大量运用了设计模式。单例模式——DCL（双重检查锁定）是线程安全的懒汉式实现，volatile防止指令重排序。工厂模式——将对象创建集中管理，便于扩展。策略模式——运行时切换算法，替代大量if-else。Spring中：Bean是单例，BeanFactory是工厂，AOP是代理模式，ApplicationEvent是观察者模式。

**🔑 关键要点**：
- 单例DCL：volatile + synchronized + 双重检查
- 工厂模式：集中管理对象创建，便于扩展
- 策略模式：运行时切换算法，替代if-else
- Spring Bean默认单例，BeanFactory是工厂
- AOP基于代理模式，ApplicationEvent是观察者模式

---

### 6. 性能调优（JVM参数与JIT）

> **级别**：高级 | **概念**：通过JVM参数调优内存和GC，理解JIT编译原理，使用jstack/jmap/Arthas等工具诊断性能问题。

```java
// ============================================
// 【高级】Java 性能调优：JVM参数与JIT编译
// ============================================

public class PerformanceTuning {
    public static void main(String[] args) {
        // ★ JVM参数调优 ★
        System.out.println("=== JVM参数调优 ===");

        System.out.println("\n【内存参数】");
        System.out.println("-Xms2g -Xmx2g");
        System.out.println("  → 初始堆和最大堆设为相同值，避免扩容开销");
        System.out.println("-Xmn1g");
        System.out.println("  → 新生代大小，一般为堆的1/4~1/2");
        System.out.println("-XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=256m");
        System.out.println("  → 元空间大小");
        System.out.println("-Xss256k");
        System.out.println("  → 线程栈大小");

        System.out.println("\n【GC参数】");
        System.out.println("-XX:+UseG1GC");
        System.out.println("  → 使用G1垃圾收集器（JDK 9+默认）");
        System.out.println("-XX:MaxGCPauseMillis=200");
        System.out.println("  → GC最大暂停时间目标（毫秒）");
        System.out.println("-XX:+PrintGCDetails -XX:+PrintGCDateStamps");
        System.out.println("  → 打印GC详细日志");
        System.out.println("-Xlog:gc*:file=gc.log");
        System.out.println("  → 输出GC日志到文件（JDK 11+）");

        System.out.println("\n【OOM处理参数】");
        System.out.println("-XX:+HeapDumpOnOutOfMemoryError");
        System.out.println("  → OOM时自动生成堆转储文件");
        System.out.println("-XX:HeapDumpPath=/tmp/heapdump.hprof");
        System.out.println("  → 堆转储文件路径");

        // ★ JIT编译 ★
        System.out.println("\n=== JIT编译原理 ===");
        System.out.println("JIT(Just-In-Time)：运行时将热点代码编译为本地机器码");
        System.out.println("\n编译层级:");
        System.out.println("L0: 解释执行");
        System.out.println("L1/L2: C1编译器（客户端编译器），快速编译");
        System.out.println("L3: C1编译器 + 性能分析");
        System.out.println("L4: C2编译器（服务端编译器），深度优化");

        System.out.println("\nJIT优化技术:");
        System.out.println("1. 方法内联：将小方法调用替换为方法体");
        System.out.println("2. 逃逸分析：对象未逃逸则栈上分配");
        System.out.println("3. 锁消除：检测到锁不可能竞争时移除");
        System.out.println("4. 标量替换：将对象拆分为基本类型");

        // ★ 诊断工具 ★
        System.out.println("\n=== 性能诊断工具 ===");
        System.out.println("jps: 查看Java进程");
        System.out.println("jstat -gc <pid> 1000: 每秒查看GC情况");
        System.out.println("jstack <pid>: 查看线程堆栈（死锁检测）");
        System.out.println("jmap -histo <pid>: 查看对象直方图");
        System.out.println("jmap -dump:format=b,file=heap.hprof <pid>: 导出堆");
        System.out.println("jvisualvm: 图形化监控工具");
        System.out.println("Arthas: 阿里开源的诊断利器");
        System.out.println("MAT: 堆转储分析工具");

        // ★ 代码层面优化 ★
        System.out.println("\n=== 代码层面优化 ===");
        System.out.println("1. StringBuilder替代String拼接");
        System.out.println("2. ArrayList指定初始容量（减少扩容次数）");
        System.out.println("3. HashMap指定初始容量和负载因子");
        System.out.println("4. 使用基本类型替代包装类（减少装箱开销）");
        System.out.println("5. 复用对象（对象池模式）");
        System.out.println("6. 使用Stream并行流（注意线程安全）");
        System.out.println("7. 使用LocalVariableTable进行调试");

        // 演示：小方法被JIT内联优化
        long start = System.nanoTime();
        int result = add(10, 20);  // 简单方法可能被JIT内联
        long end = System.nanoTime();
        System.out.println("\nadd(10,20) = " + result +
            ", 耗时: " + (end - start) + "ns");
    }

    // 简单方法：JIT可能将其内联到调用处
    private static int add(int a, int b) {
        return a + b;
    }
}
```

**💡 代码解释**：JVM性能调优是Java高级工程师的必备技能。关键参数：-Xms/-Xmx设置堆大小，-XX:+UseG1GC选择GC收集器，-XX:MaxGCPauseMillis设置GC暂停目标。JIT编译器将热点代码编译为本地机器码，C1快速编译，C2深度优化。诊断工具链：jstat(GC监控)、jstack(线程分析)、jmap(堆分析)、Arthas(在线诊断)。代码层面优化同样重要。

**🔑 关键要点**：
- -Xms/-Xmx设置堆大小，生产环境建议相同值
- G1 GC平衡吞吐量和延迟，MaxGCPauseMillis控制暂停
- JIT将热点代码编译为本地机器码，分层编译
- jstat监控GC、jstack分析线程、jmap分析堆
- Arthas是在线诊断利器，无需重启应用

---
