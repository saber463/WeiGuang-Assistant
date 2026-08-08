# 🔷 TypeScript 编程语言学习手册

> **分类**：前端  
> **描述**：TypeScript是JavaScript的超集，添加了静态类型系统，大幅提升代码可维护性和开发体验  
> **生成时间**：2026-08-07  

---

## 目录

- [初级入门](#primary)（8个知识点）
- [中级进阶](#intermediate)（8个知识点）
- [高级精通](#advanced)（6个知识点）

---

## 初级入门

### 1. 类型注解

> **级别**：初级 | **概念**：TypeScript通过类型注解为变量、函数参数和返回值指定类型，在编译阶段发现类型错误，是TS最核心的特性。

```typescript
// ====== 基本类型注解 ======
// 语法：变量名: 类型 = 值
let username: string = '张三'; // 字符串类型
let age: number = 25; // 数字类型（整数和浮点数统一）
let isActive: boolean = true; // 布尔类型
let nothing: null = null; // null类型
let notDefined: undefined = undefined; // undefined类型

// 类型推断：TS会自动推断类型，可以不写注解
let autoString = '自动推断为string'; // 鼠标悬停可见类型
// autoString = 123; // ❌ 编译错误：不能将number赋值给string

// any类型：关闭类型检查，不推荐使用
let flexible: any = '可以是任何类型';
flexible = 123; // ✅ 不会报错，但失去了类型安全
flexible = true;

// ====== 数组类型注解 ======
// 两种写法：type[] 或 Array<type>
let fruits: string[] = ['苹果', '香蕉', '橙子']; // 字符串数组
let numbers: Array<number> = [1, 2, 3]; // 数字数组（泛型写法）

// 联合类型数组：元素可以是多种类型
let mixed: (string | number)[] = ['hello', 42, 'world', 100];

// ====== 函数类型注解 ======
// 参数和返回值都可以添加类型注解
function add(a: number, b: number): number {
  // a和b必须是number，返回值也必须是number
  return a + b;
}
// add('1', '2'); // ❌ 编译错误：参数类型不匹配

// 可选参数：在参数名后加?
function greet(name: string, greeting?: string): string {
  // greeting是可选参数，调用时可省略
  return `${greeting || '你好'}，${name}！`;
}
console.log(greet('张三')); // 输出 '你好，张三！'
console.log(greet('张三', '欢迎')); // 输出 '欢迎，张三！'

// 默认参数：与JS相同，类型可自动推断
function multiply(a: number, b: number = 1): number {
  return a * b;
}

// 剩余参数类型注解
function sumAll(...nums: number[]): number {
  return nums.reduce((total, n) => total + n, 0);
}
console.log(sumAll(1, 2, 3, 4, 5)); // 15

// void类型：函数没有返回值
function logMessage(message: string): void {
  console.log(message);
  // 没有return或return undefined
}
```

**💡 代码解释**：第3-7行：基本类型注解语法，为变量指定类型。第10-11行：TS自动推断类型，省略注解。第14-16行：any类型关闭检查，不推荐。第19-23行：数组两种写法。第27-31行：函数参数和返回值类型。第35-42行：可选参数和默认参数。第46-48行：剩余参数类型。第52-54行：void表示无返回值。

**🔑 关键要点**：
- 类型注解语法：变量: 类型
- TS会自动推断类型
- 避免使用any丧失类型安全
- 可选参数用?，默认参数自动推断

---

### 2. 接口Interface

> **级别**：初级 | **概念**：Interface定义对象的形状（结构），描述对象应包含哪些属性及其类型，是TypeScript类型系统的核心抽象工具。

```typescript
// ====== 基础接口定义 ======
// 接口描述对象的结构，规定必须有哪些属性
interface User {
  name: string; // 必需属性
  age: number; // 必需属性
  email?: string; // 可选属性：属性名后加?
  readonly id: number; // 只读属性：初始化后不可修改
}

// 使用接口作为类型注解
const user1: User = {
  name: '张三',
  age: 25,
  id: 1
  // email是可选的，可以不写
};

// user1.id = 2; // ❌ 编译错误：id是只读属性

// ====== 接口用于函数类型 ======
// 定义函数的参数和返回值类型
interface MathOperation {
  (a: number, b: number): number; // 调用签名
}

const add: MathOperation = (a, b) => a + b;
const multiply: MathOperation = (a, b) => a * b;

// ====== 接口继承 ======
// 接口可以通过extends继承其他接口
interface Animal {
  name: string;
  age: number;
}

// Dog继承Animal的所有属性，并添加自己的属性
interface Dog extends Animal {
  breed: string; // 新增属性：品种
  bark(): void; // 新增方法
}

const myDog: Dog = {
  name: '旺财',
  age: 3,
  breed: '金毛',
  bark() {
    console.log('汪汪！');
  }
};

// 接口可以多重继承
interface Flyable {
  fly(): void;
}

interface Bird extends Animal, Flyable {
  // 同时继承Animal和Flyable
  wingSpan: number;
}

// ====== 索引签名 ======
// 允许对象有任意数量的动态属性
interface StringDictionary {
  [key: string]: string; // 键为string，值为string
  // [key: string] 表示任意字符串键
}

const dict: StringDictionary = {
  hello: '你好',
  world: '世界',
  any: '任意' // 可以添加任意多个键值对
};

// ====== 接口 vs 类型别名 ======
// 接口可以声明合并，类型别名不行
interface Person {
  name: string;
}

interface Person {
  // 同名接口会自动合并
  age: number;
}

// 现在Person同时有name和age
const person: Person = { name: '李四', age: 30 };
```

**💡 代码解释**：第3-9行：接口定义对象结构，支持可选(?)、只读(readonly)属性。第12-17行：使用接口约束对象。第21-28行：接口定义函数类型。第32-48行：接口继承(extends)实现代码复用。第52-62行：索引签名支持动态属性。第66-78行：接口声明合并特性。

**🔑 关键要点**：
- interface定义对象形状
- ?表示可选属性，readonly表示只读
- extends实现接口继承
- 同名接口会自动合并

---

### 3. 泛型基础

> **级别**：初级 | **概念**：泛型（Generics）允许在定义函数、接口或类时不指定具体类型，而在使用时再确定，实现类型安全的代码复用。

```typescript
// ====== 泛型函数 ======
// <T>是类型参数，调用时确定具体类型
function identity<T>(value: T): T {
  // 输入什么类型，返回什么类型
  return value;
}

// 调用泛型函数
const str = identity<string>('hello'); // 显式指定类型
const num = identity(42); // TS自动推断类型为number
const arr = identity([1, 2, 3]); // 推断为number[]

// ====== 泛型约束 ======
// 使用extends约束类型参数必须有某些属性
function getLength<T extends { length: number }>(arg: T): number {
  // T必须包含length属性
  return arg.length;
}

console.log(getLength('hello')); // 5（字符串有length）
console.log(getLength([1, 2, 3])); // 3（数组有length）
// getLength(123); // ❌ 编译错误：number没有length属性

// ====== 泛型接口 ======
// 定义可复用的接口，类型由使用者决定
interface ApiResponse<T> {
  // T是数据类型参数
  code: number; // 状态码
  message: string; // 消息
  data: T; // 数据类型由调用者指定
}

// 使用泛型接口
const userResponse: ApiResponse<{ name: string; age: number }> = {
  code: 200,
  message: '成功',
  data: { name: '张三', age: 25 }
};

const listResponse: ApiResponse<string[]> = {
  code: 200,
  message: '成功',
  data: ['项目1', '项目2', '项目3']
};

// ====== 多个泛型参数 ======
// 可以定义多个类型参数
function pair<T, U>(first: T, second: U): [T, U] {
  return [first, second];
}

const result = pair<string, number>('age', 25); // [string, number]元组
console.log(result); // ['age', 25]

// ====== 泛型默认值 ======
// 为泛型参数设置默认类型
interface Container<T = string> {
  // 不指定类型时默认T为string
  value: T;
}

const stringContainer: Container = { value: 'hello' }; // T默认为string
const numContainer: Container<number> = { value: 42 }; // 显式指定number
```

**💡 代码解释**：第3-10行：泛型函数identity，<T>是类型参数。第13-22行：extends约束泛型参数必须有特定属性。第26-43行：泛型接口ApiResponse<T>让data类型灵活。第47-53行：多个泛型参数。第57-64行：泛型默认值简化使用。

**🔑 关键要点**：
- <T>定义类型参数
- extends约束泛型
- 泛型接口实现类型复用
- 泛型默认值简化调用

---

### 4. 枚举Enum

> **级别**：初级 | **概念**：枚举（Enum）定义一组命名常量，可以是数字或字符串值，提高代码可读性和类型安全性。

```typescript
// ====== 数字枚举（默认） ======
// 第一个值默认为0，后续自动递增
enum Direction {
  Up,     // 0
  Down,   // 1
  Left,   // 2
  Right   // 3
}

// 使用枚举
const move: Direction = Direction.Up;
console.log(move); // 输出 0
console.log(Direction[0]); // 反向映射：输出 'Up'

// 自定义起始值
enum StatusCode {
  OK = 200,       // 200
  Created = 201,  // 201
  NotFound = 404, // 404
  ServerError = 500 // 500
}

// ====== 字符串枚举 ======
// 每个成员必须有字符串值，不能自动递增
enum Color {
  Red = 'RED',
  Green = 'GREEN',
  Blue = 'BLUE'
}

// 字符串枚举没有反向映射
console.log(Color.Red); // 输出 'RED'
// console.log(Color['RED']); // ❌ 编译错误

// ====== 常量枚举（const enum） ======
// 编译时内联枚举值，不生成额外代码（性能更好）
const enum Size {
  Small = 1,
  Medium = 2,
  Large = 3
}

// 编译后直接替换为字面量：console.log(2)
console.log(Size.Medium);

// ====== 枚举作为类型 ======
// 使用枚举约束参数类型
enum LogLevel {
  Debug = 'DEBUG',
  Info = 'INFO',
  Warn = 'WARN',
  Error = 'ERROR'
}

// 参数只能接收LogLevel枚举值
function log(level: LogLevel, message: string): void {
  console.log(`[${level}] ${message}`);
}

log(LogLevel.Info, '应用启动'); // [INFO] 应用启动
log(LogLevel.Error, '发生错误'); // [ERROR] 发生错误
// log('DEBUG', 'test'); // ❌ 编译错误：不是枚举值

// ====== 异构枚举（不推荐） ======
// 混合字符串和数字值，可能导致混淆
enum Mixed {
  No = 0,
  Yes = 'YES'
}
```

**💡 代码解释**：第3-10行：数字枚举默认从0开始递增。第13-20行：自定义数值枚举。第24-33行：字符串枚举无反向映射。第37-43行：const enum编译时内联，无运行时代码。第47-56行：枚举作为函数参数类型约束。第60-63行：异构枚举不推荐。

**🔑 关键要点**：
- 数字枚举默认从0递增
- const enum编译时内联
- 字符串枚举无反向映射
- 枚举可作为类型约束

---

### 5. 联合类型与交叉类型

> **级别**：初级 | **概念**：联合类型（|）表示值可以是多种类型之一，交叉类型（&）表示同时具有多种类型的属性，两者是TypeScript类型组合的核心工具。

```typescript
// ====== 联合类型（Union Types）：| ======
// 值可以是多种类型中的一种
type ID = string | number; // ID可以是字符串或数字

function printID(id: ID): void {
  // 使用联合类型前需要类型收窄（Narrowing）
  if (typeof id === 'string') {
    // 在此分支中，TS知道id是string
    console.log('ID是字符串:', id.toUpperCase());
  } else {
    // 在此分支中，TS知道id是number
    console.log('ID是数字:', id.toFixed(1));
  }
}

printID('abc123'); // ID是字符串: ABC123
printID(456); // ID是数字: 456.0

// ====== 字面量联合类型 ======
// 限制值只能是特定几个字面量
type Direction = 'up' | 'down' | 'left' | 'right';
type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE';

function move(direction: Direction): void {
  console.log(`向${direction}移动`);
}

move('up'); // ✅
// move('forward'); // ❌ 编译错误：不在联合类型中

// ====== 交叉类型（Intersection Types）：& ======
// 合并多个类型的属性，必须同时满足所有类型
interface Nameable {
  name: string;
}

interface Ageable {
  age: number;
}

// Person同时拥有name和age
type Person = Nameable & Ageable;

const person: Person = {
  name: '张三',
  age: 25
  // 必须同时有name和age
};

// 交叉类型用于扩展对象
interface Car {
  brand: string;
  model: string;
}

interface Electric {
  battery: number;
  charge(): void;
}

// ElectricCar同时拥有Car和Electric的所有属性
type ElectricCar = Car & Electric;

const tesla: ElectricCar = {
  brand: 'Tesla',
  model: 'Model 3',
  battery: 75,
  charge() {
    console.log('充电中...');
  }
};

// ====== 联合类型与交叉类型结合 ======
// 实际场景：API响应类型
interface SuccessResponse {
  status: 'success';
  data: unknown;
}

interface ErrorResponse {
  status: 'error';
  message: string;
  code: number;
}

// API响应可能是成功或失败
type ApiResponse = SuccessResponse | ErrorResponse;

function handleResponse(response: ApiResponse): void {
  // 通过status字段收窄类型
  if (response.status === 'success') {
    // TS知道这里是SuccessResponse
    console.log('成功:', response.data);
  } else {
    // TS知道这里是ErrorResponse
    console.log('错误:', response.message, response.code);
  }
}
```

**💡 代码解释**：第3-16行：联合类型|和类型收窄(typeof)。第19-28行：字面量联合类型限制值范围。第32-46行：交叉类型&合并两个接口。第50-68行：交叉类型实际应用。第72-92行：联合类型和交叉类型结合的API响应模式。

**🔑 关键要点**：
- | 联合类型表示"或"
- & 交叉类型表示"且"
- 类型收窄(typeof/in)安全访问
- 可辨识联合(Discriminated Union)

---

### 6. 类型断言

> **级别**：初级 | **概念**：类型断言告诉编译器"我知道这个值的类型"，有两种语法：as语法和尖括号语法，用于在开发者比编译器更了解类型时使用。

```typescript
// ====== 类型断言：as 语法（推荐） ======
// 当你知道某个值的类型比TS推断的更具体时使用
let someValue: unknown = '这是一个字符串';

// 断言为string类型，才能使用string的方法
const strLength: number = (someValue as string).length;
console.log('字符串长度:', strLength); // 9

// ====== 断言应用场景 ======
// 1. DOM元素类型断言
// TS不知道具体的HTML元素类型，需要断言
const canvas = document.getElementById('myCanvas') as HTMLCanvasElement;
const ctx = canvas.getContext('2d'); // 现在可以调用canvas方法

// 非空断言：! 表示值一定不是null/undefined
const element = document.getElementById('app')!;
// !告诉TS：这个值一定存在，不用检查null
element.innerHTML = 'Hello'; // 不会报可能为null

// 2. 从联合类型断言为具体类型
interface Cat {
  type: 'cat';
  meow(): void;
}
interface Dog {
  type: 'dog';
  bark(): void;
}
type Pet = Cat | Dog;

function handlePet(pet: Pet): void {
  // 当确定pet是Cat时直接断言
  (pet as Cat).meow();
  // ⚠️ 注意：如果pet实际是Dog，运行时会出错
}

// 3. any作为中间桥梁（不推荐，但有时需要）
// 某些复杂类型转换
const complexObj = { name: 'test' };
// const asInterface = complexObj as unknown as MyInterface;

// ====== const 断言 ======
// as const 将值断言为不可变的字面量类型
const colors = ['red', 'green', 'blue'] as const;
// colors类型变为 readonly ['red', 'green', 'blue']
// 而不是 string[]

// colors[0] = 'yellow'; // ❌ 编译错误：只读

const config = {
  url: 'https://api.example.com',
  timeout: 5000
} as const;
// config类型变为 { readonly url: 'https://api.example.com'; readonly timeout: 5000 }

// ====== 双重断言（极度不推荐） ======
// 绕过类型检查，可能导致运行时错误
// const value = 'hello' as unknown as number; // 欺骗编译器

// ====== 类型断言 vs 类型注解 ======
// 类型断言：告诉编译器"相信我"
// 类型注解：告诉编译器"检查我"

// 断言：不检查值是否匹配
const asserted = {} as User;

// 注解：检查值是否匹配
// const annotated: User = {}; // ❌ 编译错误：缺少必需属性
```

**💡 代码解释**：第3-7行：as语法进行类型断言。第11-13行：DOM元素断言为具体类型。第16-18行：!非空断言。第21-32行：联合类型断言为具体类型。第37-48行：as const断言为只读字面量类型。第55-60行：类型断言vs类型注解区别。

**🔑 关键要点**：
- as语法进行类型断言
- !是非空断言
- as const创建只读字面量类型
- 断言是"相信我"，注解是"检查我"

---

### 7. 函数类型

> **级别**：初级 | **概念**：TypeScript为函数提供了完整的类型系统，包括参数类型、返回值类型、函数重载和this类型，让函数调用更安全。

```typescript
// ====== 函数类型表达式 ======
// 定义函数类型：箭头语法
// (参数类型) => 返回值类型
type MathFunc = (a: number, b: number) => number;

// 符合MathFunc类型的函数
const add: MathFunc = (x, y) => x + y;
const subtract: MathFunc = (x, y) => x - y;

// ====== 调用签名 ======
// 接口定义函数类型，可以同时包含属性
interface Calculator {
  // 调用签名：描述函数参数和返回值
  (a: number, b: number): number;
  // 函数也可以有属性
  description: string;
}

// 创建符合Calculator接口的函数
const calc: Calculator = (a, b) => a + b;
calc.description = '加法计算器';
console.log(calc(1, 2)); // 3
console.log(calc.description); // 加法计算器

// ====== 函数重载（Overload） ======
// 同一函数支持多种参数组合和返回值
// 重载签名（2个以上，不包含实现）
function makeDate(timestamp: number): Date;
function makeDate(year: number, month: number, day: number): Date;

// 实现签名（必须兼容所有重载签名）
function makeDate(param1: number, param2?: number, param3?: number): Date {
  if (param2 !== undefined && param3 !== undefined) {
    // 三个参数：年、月、日
    return new Date(param1, param2, param3);
  }
  // 一个参数：时间戳
  return new Date(param1);
}

const d1 = makeDate(1700000000000); // 时间戳方式
const d2 = makeDate(2026, 7, 7); // 年月日方式
// makeDate(2026, 7); // ❌ 编译错误：没有匹配的重载

// ====== this 类型声明 ======
// 显式声明函数的this类型（第一个参数）
interface Card {
  suit: string;
  value: number;
}

function getCardName(this: Card): string {
  // this参数不占实际参数位置，仅用于类型检查
  return `${this.suit}${this.value}`;
}

const card: Card = { suit: '♠', value: 10 };
// 使用call调用，确保this指向正确
console.log(getCardName.call(card)); // ♠10
// getCardName(); // ❌ 编译错误：this上下文不正确

// ====== 参数解构的类型注解 ======
// 为解构参数添加类型
function createUser({ name, age }: { name: string; age: number }): object {
  return { name, age, createdAt: new Date() };
}

// 或者先定义接口
interface UserParams {
  name: string;
  age: number;
}
function createUser2({ name, age }: UserParams): object {
  return { name, age, createdAt: new Date() };
}
```

**💡 代码解释**：第3-8行：函数类型表达式定义。第12-25行：调用签名让函数同时有属性和方法。第29-44行：函数重载支持多种参数组合。第49-61行：this参数声明确保调用上下文正确。第65-75行：参数解构的类型注解。

**🔑 关键要点**：
- 函数类型表达式：(参数) => 返回值
- 函数重载：多个签名+一个实现
- this参数声明调用上下文
- 解构参数的类型注解

---

### 8. 类与面向对象

> **级别**：初级 | **概念**：TypeScript在JavaScript类的基础上添加了访问修饰符（public/private/protected）、抽象类、接口实现等面向对象特性，让类更安全强大。

```typescript
// ====== 类的基本定义 ======
// 使用class关键字定义类
class Person {
  // 属性声明（必须声明类型）
  name: string;
  age: number;

  // 构造函数：初始化实例
  constructor(name: string, age: number) {
    this.name = name;
    this.age = age;
  }

  // 实例方法
  greet(): string {
    return `你好，我是${this.name}，今年${this.age}岁`;
  }
}

const person = new Person('张三', 25);
console.log(person.greet()); // 你好，我是张三，今年25岁

// ====== 访问修饰符 ======
// public：默认，任何地方可访问
// private：仅在类内部可访问
// protected：类内部和子类可访问
class Employee {
  // 参数属性：构造函数参数前加修饰符，自动声明并赋值
  constructor(
    public name: string,        // public：外部可访问
    private salary: number,     // private：仅类内部可访问
    protected department: string // protected：子类也可访问
  ) {}

  getInfo(): string {
    // 可以访问private和protected
    return `${this.name} - ${this.department} - ${this.salary}`;
  }
}

const emp = new Employee('李四', 10000, '技术部');
console.log(emp.name); // ✅ public可访问
// console.log(emp.salary); // ❌ private不可访问
// console.log(emp.department); // ❌ protected不可访问

// ====== 继承 extends ======
// 子类继承父类的属性和方法
class Manager extends Employee {
  constructor(name: string, salary: number, department: string, public teamSize: number) {
    // super调用父类构造函数
    super(name, salary, department);
  }

  // 重写父类方法
  getInfo(): string {
    // protected成员在子类中可访问
    return `${this.name} - ${this.department} - 团队${this.teamSize}人`;
  }
}

const manager = new Manager('王五', 20000, '技术部', 10);
console.log(manager.getInfo()); // 王五 - 技术部 - 团队10人

// ====== 抽象类 abstract ======
// 抽象类不能直接实例化，只能被继承
abstract class Animal {
  abstract makeSound(): void; // 抽象方法：子类必须实现

  // 普通方法可以有实现
  move(): void {
    console.log('移动中...');
  }
}

class Dog extends Animal {
  // 必须实现抽象方法
  makeSound(): void {
    console.log('汪汪！');
  }
}

class Cat extends Animal {
  makeSound(): void {
    console.log('喵喵！');
  }
}

const dog = new Dog();
dog.makeSound(); // 汪汪！
dog.move(); // 移动中...
// const animal = new Animal(); // ❌ 抽象类不能实例化

// ====== implements 实现接口 ======
// 类可以实现多个接口，确保类有特定结构
interface Printable {
  print(): void;
}

interface Serializable {
  serialize(): string;
}

// 实现多个接口
class Document implements Printable, Serializable {
  constructor(public title: string, public content: string) {}

  print(): void {
    console.log(`打印: ${this.title}`);
  }

  serialize(): string {
    return JSON.stringify({ title: this.title, content: this.content });
  }
}

// ====== static 静态成员 ======
// 静态成员属于类本身，不属于实例
class MathUtils {
  static PI: number = 3.14159;

  static add(a: number, b: number): number {
    return a + b;
  }

  static multiply(a: number, b: number): number {
    return a * b;
  }
}

// 通过类名访问静态成员，不需要new
console.log(MathUtils.PI); // 3.14159
console.log(MathUtils.add(5, 3)); // 8
console.log(MathUtils.multiply(4, 2)); // 8
```

**💡 代码解释**：第3-18行：基础类定义，属性和构造函数。第22-40行：public/private/protected访问修饰符和参数属性。第44-64行：extends继承和super调用。第68-85行：abstract抽象类强制子类实现。第89-105行：implements实现接口。第109-123行：static静态成员。

**🔑 关键要点**：
- public/private/protected访问修饰符
- extends继承，super调用父类
- abstract抽象类不能实例化
- implements实现接口

---

## 中级进阶

### 1. 高级泛型

> **级别**：中级 | **概念**：高级泛型包括泛型约束（extends约束类型参数）、条件泛型（根据条件选择类型）和泛型工具类型，让类型系统更加灵活强大。

```typescript
// ====== 泛型约束：extends ======
// 约束泛型参数必须具有某些属性
interface HasLength {
  length: number;
}

// T必须包含length属性
function logLength<T extends HasLength>(arg: T): T {
  console.log(`长度: ${arg.length}`);
  return arg; // 可以安全访问length
}

logLength('hello'); // 5（字符串有length）
logLength([1, 2, 3]); // 3（数组有length）
// logLength(123); // ❌ number没有length

// ====== 使用类型参数约束另一个类型参数 ======
// 确保一个属性是另一个对象的键
function getProperty<T, K extends keyof T>(obj: T, key: K): T[K] {
  // keyof T获取T的所有键的联合类型
  // K extends keyof T确保K是T的有效键
  return obj[key];
}

const person = { name: '张三', age: 25 };
getProperty(person, 'name'); // ✅ 返回string
// getProperty(person, 'email'); // ❌ 'email'不是person的键

// ====== 条件类型：extends的三元运算 ======
// 根据条件决定最终类型
// 语法：T extends U ? X : Y
type IsString<T> = T extends string ? '是字符串' : '不是字符串';

type A = IsString<string>; // '是字符串'
type B = IsString<number>; // '不是字符串'

// 实际应用：提取特定类型
type ExtractString<T> = T extends string ? T : never;

type C = ExtractString<'a' | 1 | 'b' | 2>; // 'a' | 'b'（never被排除）

// ====== infer 关键字：类型推断 ======
// 在条件类型中推断类型变量
type ReturnType2<T> = T extends (...args: any[]) => infer R ? R : never;
// infer R在条件类型中声明R，推断出返回值类型

function getData(): { id: number; name: string } {
  return { id: 1, name: 'test' };
}

type Data = ReturnType2<typeof getData>; // { id: number; name: string }

// 提取数组元素类型
type ArrayElementType<T> = T extends (infer U)[] ? U : never;

type NumType = ArrayElementType<number[]>; // number
type StrType = ArrayElementType<string[]>; // string

// ====== 泛型在类中的应用 ======
// 泛型类：类型参数在类中复用
class DataStore<T> {
  private data: T[] = [];

  add(item: T): void {
    this.data.push(item);
  }

  get(index: number): T | undefined {
    return this.data[index];
  }

  getAll(): T[] {
    return [...this.data]; // 返回副本，保护内部数据
  }
}

const stringStore = new DataStore<string>();
stringStore.add('hello');
stringStore.add('world');
// stringStore.add(123); // ❌ 类型不匹配

const numStore = new DataStore<number>();
numStore.add(1);
numStore.add(2);
```

**💡 代码解释**：第3-13行：extends约束泛型参数。第17-23行：keyof约束参数为对象的键。第28-37行：条件类型T extends U ? X : Y。第41-58行：infer关键字推断类型。第62-83行：泛型类DataStore<T>。

**🔑 关键要点**：
- extends约束泛型参数
- keyof获取对象键的联合类型
- 条件类型实现类型分支
- infer在条件类型中推断类型

---

### 2. 工具类型

> **级别**：中级 | **概念**：TypeScript内置了丰富的工具类型（Utility Types），如Partial、Pick、Omit、Record等，用于对已有类型进行转换和操作。

```typescript
// ====== 基础接口定义 ======
interface User {
  id: number;
  name: string;
  email: string;
  age: number;
  address: string;
}

// ====== Partial<T>：所有属性变为可选 ======
// 常用于更新操作，只传部分字段
function updateUser(id: number, updates: Partial<User>): void {
  // updates可以只包含User的部分属性
  console.log(`更新用户${id}:`, updates);
}

updateUser(1, { name: '新名字' }); // ✅ 只更新name
updateUser(1, { age: 26, email: 'new@email.com' }); // ✅ 更新多个字段

// ====== Required<T>：所有属性变为必选 ======
// 与Partial相反
interface OptionalUser {
  name?: string;
  age?: number;
  email?: string;
}

type RequiredUser = Required<OptionalUser>;
// { name: string; age: number; email: string }（所有属性必选）

// ====== Pick<T, K>：从T中选取部分属性 ======
// 提取需要的字段
type UserPreview = Pick<User, 'id' | 'name'>;
// { id: number; name: string }

const preview: UserPreview = { id: 1, name: '张三' };

// ====== Omit<T, K>：从T中排除部分属性 ======
// 去掉不需要的字段
type UserWithoutAddress = Omit<User, 'address'>;
// { id: number; name: string; email: string; age: number }

// ====== Record<K, V>：创建键值对类型 ======
// 键为K的联合类型，值为V类型
type PageNames = 'home' | 'about' | 'contact';
type PageInfo = { title: string; url: string };

type Pages = Record<PageNames, PageInfo>;
// { home: PageInfo; about: PageInfo; contact: PageInfo }

const pages: Pages = {
  home: { title: '首页', url: '/' },
  about: { title: '关于', url: '/about' },
  contact: { title: '联系', url: '/contact' }
};

// ====== Readonly<T>：所有属性变为只读 ======
type ReadonlyUser = Readonly<User>;
const user: ReadonlyUser = {
  id: 1, name: '张三', email: 'test@test.com', age: 25, address: '北京'
};
// user.name = '李四'; // ❌ 编译错误：只读

// ====== Exclude<T, U>：从T中排除可分配给U的类型 ======
type AllTypes = 'a' | 'b' | 'c' | 'd';
type Excluded = Exclude<AllTypes, 'a' | 'c'>; // 'b' | 'd'

// ====== Extract<T, U>：从T中提取可分配给U的类型 ======
type Extracted = Extract<AllTypes, 'a' | 'c' | 'e'>; // 'a' | 'c'（e不在T中）

// ====== NonNullable<T>：从T中排除null和undefined ======
type MaybeString = string | null | undefined;
type DefinitelyString = NonNullable<MaybeString>; // string

// ====== ReturnType<T>：获取函数返回值类型 ======
function createUser(): { id: number; name: string } {
  return { id: 1, name: '张三' };
}
type UserType = ReturnType<typeof createUser>; // { id: number; name: string }

// ====== Parameters<T>：获取函数参数类型 ======
function saveUser(name: string, age: number): void {}
type SaveParams = Parameters<typeof saveUser>; // [string, number]
```

**💡 代码解释**：第3-9行：基础User接口。第13-19行：Partial将属性变为可选。第23-30行：Required与Partial相反。第34-38行：Pick选取属性。第42-44行：Omit排除属性。第48-59行：Record创建键值对。第63-68行：Readonly只读。第72-88行：Exclude/Extract/NonNullable类型过滤。第91-97行：ReturnType和Parameters。

**🔑 关键要点**：
- Partial让属性可选
- Pick/Omit选取/排除属性
- Record创建键值对类型
- ReturnType/Parameters获取函数类型

---

### 3. 类型守卫

> **级别**：中级 | **概念**：类型守卫（Type Guards）在运行时检查类型，帮助TypeScript在条件分支中收窄联合类型，包括typeof、instanceof、自定义守卫和in操作符。

```typescript
// ====== typeof 类型守卫 ======
// 用于原始类型判断
function processValue(value: string | number): void {
  if (typeof value === 'string') {
    // 此分支中value被收窄为string
    console.log(value.toUpperCase());
  } else {
    // 此分支中value被收窄为number
    console.log(value.toFixed(2));
  }
}

// typeof 可识别的类型：string, number, boolean, symbol, undefined, object, function, bigint

// ====== instanceof 类型守卫 ======
// 用于判断是否是某个类的实例
class Dog {
  bark() { console.log('汪汪！'); }
}
class Cat {
  meow() { console.log('喵喵！'); }
}

function makeSound(animal: Dog | Cat): void {
  if (animal instanceof Dog) {
    // animal被收窄为Dog
    animal.bark();
  } else {
    // animal被收窄为Cat
    animal.meow();
  }
}

// ====== in 类型守卫 ======
// 检查对象是否包含某个属性
interface Bird {
  fly(): void;
  layEggs(): void;
}

interface Fish {
  swim(): void;
  layEggs(): void;
}

function getSmallPet(): Fish | Bird {
  // 返回Fish或Bird
  return { swim() {}, layEggs() {} };
}

const pet = getSmallPet();

if ('swim' in pet) {
  // pet被收窄为Fish
  pet.swim();
} else {
  // pet被收窄为Bird
  pet.fly();
}

// 两者都有layEggs，所以可以安全访问
pet.layEggs();

// ====== 自定义类型守卫：is 关键字 ======
// 定义函数返回类型为 参数名 is 类型
interface Square {
  kind: 'square';
  size: number;
}

interface Circle {
  kind: 'circle';
  radius: number;
}

type Shape = Square | Circle;

// 自定义类型守卫：返回类型为 value is Type
function isSquare(shape: Shape): shape is Square {
  // shape is Square 告诉TS：返回true时shape就是Square
  return shape.kind === 'square';
}

function getArea(shape: Shape): number {
  if (isSquare(shape)) {
    // shape被收窄为Square
    return shape.size * shape.size;
  } else {
    // shape被收窄为Circle
    return Math.PI * shape.radius ** 2;
  }
}

// ====== 可辨识联合（Discriminated Union） ======
// 使用共同的kind字段区分类型
function getAreaByKind(shape: Shape): number {
  switch (shape.kind) {
    case 'square':
      // shape是Square
      return shape.size ** 2;
    case 'circle':
      // shape是Circle
      return Math.PI * shape.radius ** 2;
    default:
      // 确保处理了所有情况
      const _exhaustive: never = shape;
      return _exhaustive;
  }
}
```

**💡 代码解释**：第3-12行：typeof守卫用于原始类型。第16-30行：instanceof守卫用于类实例。第34-57行：in守卫检查属性存在。第61-85行：自定义is守卫让函数返回类型断言。第89-100行：可辨识联合通过kind字段区分类型。

**🔑 关键要点**：
- typeof守卫原始类型
- instanceof守卫类实例
- in守卫检查属性
- is关键字自定义类型守卫

---

### 4. 装饰器

> **级别**：中级 | **概念**：装饰器（Decorator）是一种特殊声明，可附加到类、方法、属性上，修改其行为，是一种元编程模式，在Angular和NestJS中广泛使用。

```typescript
// ====== 装饰器配置 ======
// tsconfig.json 需要开启: "experimentalDecorators": true

// ====== 类装饰器 ======
// 接收类的构造函数，可以修改或替换类
function sealed(constructor: Function) {
  // 使用Object.seal防止添加新属性
  Object.seal(constructor);
  Object.seal(constructor.prototype);
  console.log(`${constructor.name} 已被密封`);
}

// 装饰器工厂：返回装饰器函数，支持传参
function version(version: string) {
  return function <T extends { new(...args: any[]): {} }>(constructor: T) {
    // 返回一个新类，继承原类并添加属性
    return class extends constructor {
      apiVersion = version; // 添加版本号属性
    };
  };
}

@sealed
@version('1.0.0')
class UserService {
  constructor(private name: string) {}

  getUser() {
    return this.name;
  }
}

// 通过装饰器添加的属性
const service = new UserService('张三') as any;
console.log(service.apiVersion); // 输出 '1.0.0'

// ====== 方法装饰器 ======
// 可以拦截、修改方法的行为
function log(target: any, propertyKey: string, descriptor: PropertyDescriptor) {
  // target: 类的原型对象
  // propertyKey: 方法名
  // descriptor: 属性描述符
  const originalMethod = descriptor.value; // 保存原方法

  descriptor.value = function (...args: any[]) {
    console.log(`调用 ${propertyKey}，参数:`, args);
    const result = originalMethod.apply(this, args); // 调用原方法
    console.log(`${propertyKey} 返回:`, result);
    return result;
  };

  return descriptor;
}

// 装饰器工厂：限制方法调用频率
function throttle(delay: number) {
  let lastCall = 0;
  return function (target: any, propertyKey: string, descriptor: PropertyDescriptor) {
    const original = descriptor.value;
    descriptor.value = function (...args: any[]) {
      const now = Date.now();
      if (now - lastCall >= delay) {
        lastCall = now;
        return original.apply(this, args);
      }
    };
  };
}

class Calculator {
  @log
  add(a: number, b: number): number {
    return a + b;
  }

  @throttle(1000)
  heavyOperation(): void {
    console.log('执行耗时操作...');
  }
}

const calc = new Calculator();
calc.add(5, 3); // 输出: 调用 add，参数: [5, 3]  →  add 返回: 8

// ====== 属性装饰器 ======
// 可以观察或修改属性
function defaultValue(value: any) {
  return function (target: any, propertyKey: string) {
    let val = value;
    Object.defineProperty(target, propertyKey, {
      get: () => val,
      set: (newVal) => { val = newVal; },
      enumerable: true,
      configurable: true
    });
  };
}

class Settings {
  @defaultValue('light')
  theme!: string;
}

const settings = new Settings();
console.log(settings.theme); // 输出 'light'（默认值）
```

**💡 代码解释**：第4-17行：类装饰器sealed和version工厂。第19-34行：装饰器应用和效果。第38-55行：方法装饰器log实现。第58-70行：throttle装饰器工厂限制频率。第73-96行：属性装饰器设置默认值。

**🔑 关键要点**：
- 装饰器需要开启experimentalDecorators
- 类装饰器修改构造函数
- 方法装饰器拦截方法调用
- 装饰器工厂支持传参

---

### 5. 声明文件(.d.ts)

> **级别**：中级 | **概念**：声明文件(.d.ts)为JavaScript库提供类型信息，让TS项目能安全使用纯JS库，通过declare关键字描述外部代码的类型。

```typescript
// ====== 声明文件基础 ======
// 文件名：global.d.ts
// 声明文件只包含类型声明，不包含实现代码

// 声明全局变量
// declare var process: {
//   env: {
//     NODE_ENV: string;
//     API_URL: string;
//   };
// };

// 声明全局函数
// declare function greet(name: string): string;

// ====== 声明模块 ======
// 为没有类型定义的JS库声明类型
// 文件名：my-library.d.ts
declare module 'my-library' {
  // 导出类型定义
  export interface Config {
    apiKey: string;
    timeout: number;
  }

  export function init(config: Config): void;
  export function fetchData<T>(url: string): Promise<T>;

  // 默认导出
  const myLib: {
    version: string;
    init: (config: Config) => void;
  };
  export default myLib;
}

// 使用模块
// import myLib, { init, fetchData } from 'my-library';

// ====== 声明全局扩展 ======
// 扩展Window对象
declare global {
  interface Window {
    // 添加自定义全局属性
    __APP_VERSION__: string;
    __DEV__: boolean;
  }
}

// 现在可以在代码中安全使用
// console.log(window.__APP_VERSION__);

// 扩展String原型
declare global {
  interface String {
    // 声明自定义方法
    toCamelCase(): string;
  }
}

// ====== 声明命名空间 ======
// 为复杂的全局对象声明类型
declare namespace MyApp {
  // 命名空间内部声明
  interface User {
    id: number;
    name: string;
  }

  function getCurrentUser(): User;
  function setUser(user: User): void;

  const version: string;
}

// 使用命名空间
// const user = MyApp.getCurrentUser();

// ====== .d.ts的实际使用场景 ======
// 1. 为npm包补充类型：@types/xxx
// 2. 声明全局变量：process.env、window等
// 3. 声明CSS/图片等非代码模块
// declare module '*.css' {
//   const content: Record<string, string>;
//   export default content;
// }
// declare module '*.png' {
//   const src: string;
//   export default src;
// }

// 4. 声明Vue/React组件的类型
// declare module '*.vue' {
//   import type { DefineComponent } from 'vue';
//   const component: DefineComponent<{}, {}, any>;
//   export default component;
// }

console.log('声明文件的作用：');
console.log('1. 为JS库提供类型信息');
console.log('2. 扩展全局对象类型');
console.log('3. 声明非代码模块（CSS/图片等）');
console.log('4. 声明自定义全局变量');
```

**💡 代码解释**：第3-10行：声明全局变量和函数。第16-34行：declare module为JS库声明类型。第38-55行：declare global扩展全局类型。第59-71行：declare namespace声明命名空间。第76-92行：实际使用场景总结。

**🔑 关键要点**：
- declare关键字声明类型
- declare module为JS库提供类型
- declare global扩展全局类型
- .d.ts文件只包含类型不包含实现

---

### 6. 模块解析策略

> **级别**：中级 | **概念**：TypeScript支持多种模块解析策略（Classic/Node），通过tsconfig.json中的moduleResolution配置，影响import语句如何查找模块文件。

```typescript
// ====== tsconfig.json 模块解析配置 ======
// {
//   "compilerOptions": {
//     "module": "ESNext",           // 模块系统
//     "moduleResolution": "node",   // 解析策略：node或classic
//     "baseUrl": "./",              // 基础路径（非相对路径的起点）
//     "paths": {                    // 路径别名映射
//       "@/*": ["src/*"],
//       "@components/*": ["src/components/*"],
//       "@utils/*": ["src/utils/*"]
//     },
//     "rootDir": "./src",          // 源码根目录
//     "outDir": "./dist",          // 输出目录
//     "types": ["node", "jest"],    // 包含的类型声明包
//     "typeRoots": ["./node_modules/@types"]  // 类型声明包查找路径
//   }
// }

// ====== Node解析策略 ======
// 1. 相对路径导入：import './utils'
//    查找顺序：./utils.ts → ./utils.tsx → ./utils.d.ts
//    → ./utils/package.json(types字段) → ./utils/index.ts

// 2. 非相对路径导入：import 'lodash'
//    从最近的node_modules逐级向上查找
//    → ./node_modules/lodash → ../node_modules/lodash → ...

// ====== 路径别名（paths） ======
// 使用路径别名代替复杂的相对路径
// 不使用别名
// import { Button } from '../../../components/Button';
// import { formatDate } from '../../../utils/date';

// 使用别名（配置后）
// import { Button } from '@components/Button';
// import { formatDate } from '@utils/date';

// ====== exports 字段（package.json） ======
// 现代模块解析支持exports字段
// {
//   "name": "my-lib",
//   "exports": {
//     ".": {
//       "import": "./dist/esm/index.js",
//       "require": "./dist/cjs/index.js",
//       "types": "./dist/types/index.d.ts"
//     },
//     "./utils": {
//       "import": "./dist/esm/utils.js",
//       "types": "./dist/types/utils.d.ts"
//     }
//   }
// }

// ====== 模块解析最佳实践 ======
console.log('模块解析最佳实践：');
console.log('1. 使用Node解析策略（默认）');
console.log('2. 配置paths简化导入路径');
console.log('3. 使用baseUrl设置基础路径');
console.log('4. 发布库时配置exports字段');
console.log('5. 区分ESM和CJS的模块解析');

// ====== ESM vs CJS 模块解析 ======
// ESM（ES Modules）：使用import/export
// import { add } from './math.js'; // 必须包含扩展名

// CJS（CommonJS）：使用require/module.exports
// const { add } = require('./math');

// TypeScript的module配置影响编译输出
// "module": "ESNext" → 保留import/export语句
// "module": "CommonJS" → 转换为require/module.exports
```

**💡 代码解释**：第3-17行：tsconfig.json模块解析配置项。第20-25行：Node解析策略的查找顺序。第29-35行：路径别名简化导入。第39-53行：exports字段示例。第57-63行：最佳实践总结。第67-75行：ESM和CJS的模块解析差异。

**🔑 关键要点**：
- moduleResolution设为node
- paths配置路径别名
- baseUrl设置基础路径
- exports字段控制子路径导出

---

### 7. keyof与typeof操作符

> **级别**：中级 | **概念**：keyof获取对象类型的所有键组成的联合类型，typeof获取变量的类型，两者结合可以在类型层面实现动态属性访问和类型推导。

```typescript
// ====== keyof 操作符 ======
// keyof T：获取类型T的所有键组成的联合类型
interface User {
  id: number;
  name: string;
  email: string;
  age: number;
}

// UserKeys = 'id' | 'name' | 'email' | 'age'
type UserKeys = keyof User;

// keyof的实际应用：类型安全的属性访问函数
function getProperty<T, K extends keyof T>(obj: T, key: K): T[K] {
  // K extends keyof T确保key是T的有效属性名
  // T[K]是索引访问类型，获取属性值的类型
  return obj[key];
}

const user: User = { id: 1, name: '张三', email: 'test@test.com', age: 25 };

const name = getProperty(user, 'name'); // ✅ 类型为string
const age = getProperty(user, 'age'); // ✅ 类型为number
// const invalid = getProperty(user, 'address'); // ❌ 编译错误

// keyof + 映射类型：创建只读版本
type Readonly<T> = {
  readonly [K in keyof T]: T[K];
  // K遍历所有键，readonly使属性只读
};

// keyof + 条件类型：提取特定类型的键
type StringKeys<T> = {
  [K in keyof T]: T[K] extends string ? K : never;
}[keyof T];
// 获取User中所有string类型属性的键名
type UserStringKeys = StringKeys<User>; // 'name' | 'email'

// ====== typeof 操作符 ======
// typeof在类型上下文中获取变量的类型
const config = {
  apiUrl: 'https://api.example.com',
  timeout: 5000,
  retries: 3
};

// ConfigType = { apiUrl: string; timeout: number; retries: number }
type ConfigType = typeof config;

// typeof的应用：从已有变量推导类型
const colors = {
  red: '#FF0000',
  green: '#00FF00',
  blue: '#0000FF'
} as const; // as const保留字面量类型

// 从colors对象推导出值类型
type ColorName = keyof typeof colors; // 'red' | 'green' | 'blue'
type ColorValue = typeof colors[ColorName]; // '#FF0000' | '#00FF00' | '#0000FF'

// ====== keyof + typeof 组合应用 ======
// 场景1：从枚举对象创建类型安全的映射
const STATUS_MAP = {
  pending: '待处理',
  processing: '处理中',
  completed: '已完成',
  failed: '失败'
} as const;

// 从常量对象推导状态类型
type Status = keyof typeof STATUS_MAP; // 'pending' | 'processing' | 'completed' | 'failed'
type StatusLabel = typeof STATUS_MAP[Status]; // '待处理' | '处理中' | '已完成' | '失败'

// 场景2：类型安全的表单验证
const validationRules = {
  username: { required: true, minLength: 3 },
  email: { required: true, pattern: /^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$/ },
  age: { required: false, min: 0, max: 150 }
};

// 从验证规则对象推导表单字段类型
type FormField = keyof typeof validationRules; // 'username' | 'email' | 'age'
type ValidationRule = typeof validationRules[FormField];

// 场景3：类型安全的 Redux Action
const ActionTypes = {
  ADD_TODO: 'ADD_TODO',
  REMOVE_TODO: 'REMOVE_TODO',
  TOGGLE_TODO: 'TOGGLE_TODO'
} as const;

type ActionType = typeof ActionTypes[keyof typeof ActionTypes];
// 'ADD_TODO' | 'REMOVE_TODO' | 'TOGGLE_TODO'

// 场景4：从类实例获取类型
class ApiService {
  baseUrl = 'https://api.example.com';
  getUsers() { return []; }
  getUser(id: number) { return { id, name: '' }; }
}

// 从实例获取类型
const api = new ApiService();
type ApiType = typeof api; // 获取实例的类型
// 获取方法的返回值类型
type GetUserReturn = ReturnType<ApiType['getUser']>; // { id: number; name: string }
```

**💡 代码解释**：第3-12行：keyof获取对象键的联合类型。第15-22行：keyof+泛型实现类型安全的属性访问。第25-34行：keyof结合映射类型。第37-43行：typeof获取变量类型。第47-54行：typeof结合as const保留字面量。第58-85行：keyof+typeof四种实际应用场景。

**🔑 关键要点**：
- keyof获取对象键的联合类型
- typeof在类型上下文获取变量类型
- keyof+typeof推导常量对象类型
- T[K]索引访问类型获取属性值类型

---

### 8. TS与前端框架结合

> **级别**：中级 | **概念**：TypeScript与React、Vue 3等前端框架深度集成，通过类型推导和泛型组件提供完整的类型安全保障，提升开发效率。

```typescript
// ====== TS + React ======
// 1. 函数组件Props类型
// interface ButtonProps {
//   label: string;           // 按钮文字
//   onClick: () => void;     // 点击回调
//   disabled?: boolean;      // 可选：禁用状态
//   variant?: 'primary' | 'secondary' | 'danger'; // 联合类型限制值
// }
//
// // React.FC已不推荐，直接使用函数+类型注解
// function Button({ label, onClick, disabled = false, variant = 'primary' }: ButtonProps) {
//   return (
//     <button
//       onClick={onClick}
//       disabled={disabled}
//       className={`btn btn-${variant}`}
//     >
//       {label}
//     </button>
//   );
// }

// 2. useState类型推导
// const [count, setCount] = useState<number>(0); // 显式指定类型
// const [user, setUser] = useState<User | null>(null); // 联合类型
// const [items, setItems] = useState<string[]>([]); // 数组类型

// 3. useRef类型
// const inputRef = useRef<HTMLInputElement>(null); // DOM元素引用
// const timerRef = useRef<number | null>(null); // 定时器引用

// 4. 事件处理类型
// const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
//   console.log(e.target.value); // 类型安全
// };
// const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
//   e.preventDefault();
// };

// 5. 泛型组件
// interface ListProps<T> {
//   items: T[];                    // 泛型数组
//   renderItem: (item: T) => React.ReactNode; // 泛型渲染函数
//   keyExtractor: (item: T) => string | number; // 泛型key函数
// }
//
// function List<T>({ items, renderItem, keyExtractor }: ListProps<T>) {
//   return (
//     <ul>
//       {items.map(item => (
//         <li key={keyExtractor(item)}>{renderItem(item)}</li>
//       ))}
//     </ul>
//   );
// }

// ====== TS + Vue 3 ======
// 1. defineProps类型声明
// <script setup lang="ts">
// // 泛型参数方式声明props类型
// const props = defineProps<{
//   title: string;
//   count: number;
//   items: string[];
//   onUpdate?: (value: number) => void; // 可选回调
// }>();

// 2. defineEmits类型声明
// const emit = defineEmits<{
//   // 具名元组语法：事件名: [参数类型]
//   update: [value: number];
//   delete: [id: string];
//   submit: [data: { name: string; email: string }];
// }>();
//
// emit('update', 42); // ✅ 类型正确
// // emit('update', 'hello'); // ❌ 类型错误

// 3. ref类型推导
// const count = ref<number>(0); // 显式类型
// const message = ref(''); // 自动推导为string
// const user = ref<User | null>(null); // 联合类型

// 4. reactive类型
// interface FormState {
//   name: string;
//   email: string;
//   age: number;
// }
// const form = reactive<FormState>({
//   name: '',
//   email: '',
//   age: 0
// });

// 5. 模板引用类型
// const inputRef = ref<HTMLInputElement | null>(null);

// 6. 组合函数类型
// function useApi<T>(url: string) {
//   const data = ref<T | null>(null);
//   const loading = ref(false);
//   const error = ref<string | null>(null);

//   async function fetch() {
//     loading.value = true;
//     try {
//       const response = await fetch(url);
//       data.value = await response.json();
//     } catch (e) {
//       error.value = e instanceof Error ? e.message : '未知错误';
//     } finally {
//       loading.value = false;
//     }
//   }

//   return { data, loading, error, fetch };
// }

// // 使用泛型组合函数
// const { data: userData } = useApi<User>('/api/user');
// // userData类型为Ref<User | null>

// ====== TS + 框架总结 ======
console.log('TS与框架结合要点：');
console.log('1. React：Props接口定义 + 事件类型 + 泛型组件');
console.log('2. Vue：defineProps泛型 + defineEmits具名元组');
console.log('3. 组合函数：泛型参数 + 类型推导');
console.log('4. 状态管理：Pinia/Vuex支持完整TS类型');
console.log('5. 路由：类型安全的路由参数和导航');
```

**💡 代码解释**：第3-27行：TS+React的Props、Hooks、事件类型。第29-48行：React泛型组件List<T>。第52-88行：TS+Vue 3的defineProps/defineEmits/ref/reactive。第92-101行：泛型组合函数useApi<T>。第105-109行：总结要点。

**🔑 关键要点**：
- React Props接口定义组件类型
- Vue defineProps/Emits泛型声明
- 泛型组件和组合函数
- 事件类型和DOM引用类型

---

## 高级精通

### 1. 模板字面量类型

> **级别**：高级 | **概念**：模板字面量类型（Template Literal Types）在类型层面拼接字符串，可以创建基于字符串模式的高级类型，如事件名、CSS属性等。

```typescript
// ====== 模板字面量类型基础 ======
// 语法：与JS模板字符串相同，但是在类型层面
type World = 'world';
type Greeting = `hello ${World}`; // 'hello world'

// 联合类型会自动展开（笛卡尔积）
type Email = 'gmail' | 'outlook' | 'qq';
type Domain = 'com' | 'cn' | 'org';
// 自动组合所有可能：gmail.com | gmail.cn | ... | qq.org
type EmailAddress = `${string}@${Email}.${Domain}`;

const email1: EmailAddress = 'test@gmail.com'; // ✅
const email2: EmailAddress = 'user@qq.cn'; // ✅
// const email3: EmailAddress = 'test@yahoo.com'; // ❌ yahoo不在联合类型中

// ====== 内置字符串操作类型 ======
// Uppercase：转大写
type Upper = Uppercase<'hello'>; // 'HELLO'

// Lowercase：转小写
type Lower = Lowercase<'HELLO'>; // 'hello'

// Capitalize：首字母大写
type Capitalized = Capitalize<'hello'>; // 'Hello'

// Uncapitalize：首字母小写
type Uncapitalized = Uncapitalize<'Hello'>; // 'hello'

// ====== 实际应用：类型安全的事件系统 ======
// 定义事件名模式
type EventName = 'click' | 'focus' | 'blur' | 'change';

// 生成事件处理器类型
type HandlerName = `on${Capitalize<EventName>}`;
// 'onClick' | 'onFocus' | 'onBlur' | 'onChange'

// 定义事件处理器对象
// 键为HandlerName，值为函数类型
type EventHandlers = {
  [K in HandlerName]: (event: Event) => void;
};
// { onClick: (event: Event) => void; onFocus: ...; ... }

// ====== 实际应用：CSS属性类型 ======
// 将CSS属性名转换为类型安全的键
type CSSProperty = 'margin' | 'padding' | 'border';
type CSSDirection = 'Top' | 'Right' | 'Bottom' | 'Left';

// 生成 marginTop, marginRight, paddingTop 等类型
type CSSPropertyFull = `${CSSProperty}${CSSDirection}`;
// 'marginTop' | 'marginRight' | ... | 'borderLeft'

// 结合工具类型
type CSSProperties = Record<CSSPropertyFull, string>;

const styles: CSSProperties = {
  marginTop: '10px',
  paddingLeft: '20px',
  borderRight: '1px solid red'
  // 只能使用生成的属性名
};

// ====== 实际应用：API路由类型 ======
// 定义路由参数
type Resource = 'users' | 'posts' | 'comments';
type Action = 'list' | 'detail' | 'create' | 'update' | 'delete';

// 生成API路径
type ApiRoute = `/api/${Resource}/${Action}`;
// '/api/users/list' | '/api/users/detail' | ... | '/api/comments/delete'

// 结合infer提取路由参数
type ExtractRouteParams<T extends string> =
  T extends `/api/${infer Resource}/${infer Action}`
    ? { resource: Resource; action: Action }
    : never;

type Params = ExtractRouteParams<'/api/users/list'>;
// { resource: 'users'; action: 'list' }
```

**💡 代码解释**：第3-12行：模板字面量类型基础，联合类型自动展开。第16-27行：内置字符串操作类型。第31-45行：事件系统应用——生成onClick等处理器类型。第49-63行：CSS属性类型应用。第67-83行：API路由类型应用，infer提取路由参数。

**🔑 关键要点**：
- 模板字面量类型在类型层面拼接字符串
- 联合类型自动展开为笛卡尔积
- Uppercase/Lowercase/Capitalize操作字符串
- infer在模板字面量中提取类型

---

### 2. 映射类型

> **级别**：高级 | **概念**：映射类型（Mapped Types）基于旧类型创建新类型，通过遍历属性键并应用变换，实现类型级别的批量操作。

```typescript
// ====== 映射类型基础 ======
// 语法：{ [K in 键的联合类型]: 值类型 }
interface User {
  name: string;
  age: number;
  email: string;
}

// 将每个属性变为可选（与Partial等价）
type PartialUser = {
  [K in keyof User]?: User[K];
  // K遍历 'name' | 'age' | 'email'
  // ?使属性可选
};

// 将每个属性变为只读
type ReadonlyUser = {
  readonly [K in keyof User]: User[K];
};

// ====== 映射类型修饰符 ======
// + 和 - 控制修饰符的添加和移除
// 移除只读
type Mutable<T> = {
  -readonly [K in keyof T]: T[K];
  // -readonly 移除readonly修饰符
};

// 移除可选
type Required2<T> = {
  [K in keyof T]-?: T[K];
  // -? 移除可选修饰符
};

// ====== 键重映射（Key Remapping）：as 子句 ======
// 使用as子句对键进行转换
interface Person {
  name: string;
  age: number;
  email: string;
}

// 给每个属性名添加get前缀，并修改值类型为函数
type Getters<T> = {
  [K in keyof T as `get${Capitalize<string & K>}`]: () => T[K];
};

type PersonGetters = Getters<Person>;
// { getName: () => string; getAge: () => number; getEmail: () => string }

// 过滤特定类型的属性
type PickByType<T, V> = {
  [K in keyof T as T[K] extends V ? K : never]: T[K];
  // 如果T[K]是V的子类型，保留K，否则排除（never）
};

type StringProps = PickByType<Person, string>;
// { name: string; email: string }（只有string类型的属性）

// ====== 实际应用：响应式表单类型 ======
// 为表单模型创建类型安全的验证规则
interface FormModel {
  username: string;
  age: number;
  email: string;
  agreeTerms: boolean;
}

// 为每个字段创建验证状态
type ValidationState<T> = {
  [K in keyof T]: {
    value: T[K];
    dirty: boolean; // 是否被修改过
    errors: string[]; // 错误信息列表
    valid: boolean; // 是否有效
  };
};

type FormValidation = ValidationState<FormModel>;
// 每个字段都包含value、dirty、errors、valid

// ====== 实际应用：API响应包装 ======
// 将接口的所有方法包装为返回Promise
interface ApiMethods {
  getUser(id: number): { name: string };
  getPosts(): { title: string }[];
  login(credentials: { username: string; password: string }): { token: string };
}

// 将方法包装为异步版本
type AsyncApi<T> = {
  [K in keyof T]: T[K] extends (...args: infer P) => infer R
    ? (...args: P) => Promise<R> // 包装为Promise
    : T[K];
};

type AsyncApiMethods = AsyncApi<ApiMethods>;
// 所有方法返回值变为Promise包装
```

**💡 代码解释**：第3-15行：映射类型基础语法，遍历keyof结果。第19-30行：+/-修饰符控制readonly和可选。第34-52行：as子句实现键重映射和类型过滤。第56-72行：表单验证类型应用。第76-91行：API方法异步包装应用。

**🔑 关键要点**：
- [K in keyof T]遍历属性
- -readonly/-?移除修饰符
- as子句重映射键名
- 映射类型实现批量类型转换

---

### 3. 条件类型推断

> **级别**：高级 | **概念**：条件类型结合infer关键字可以在类型分支中推断出子类型，实现复杂类型提取，如递归类型、深层属性访问等。

```typescript
// ====== 条件类型 + infer 深度应用 ======

// 1. 提取Promise的内部类型（递归版本）
// 递归unwrap嵌套的Promise
type Awaited<T> =
  T extends Promise<infer U>
    ? Awaited<U> // 递归调用，继续解包
    : T; // 直到不是Promise时返回

type A = Awaited<Promise<string>>; // string
type B = Awaited<Promise<Promise<number>>>; // number（递归解包）
type C = Awaited<string>; // string（不是Promise直接返回）

// 2. 提取数组元素类型
type ElementType<T> =
  T extends (infer U)[]
    ? U
    : T extends ReadonlyArray<infer U> // 也支持只读数组
      ? U
      : never;

type E1 = ElementType<number[]>; // number
type E2 = ElementType<readonly string[]>; // string
type E3 = ElementType<{ name: string }[]>; // { name: string }

// 3. 提取函数第一个参数类型
type FirstParameter<T> =
  T extends (first: infer F, ...rest: any[]) => any
    ? F
    : never;

function createUser(name: string, age: number, email: string): User {
  return { name, age, email };
}

type FirstParam = FirstParameter<typeof createUser>; // string

// 4. 深层属性访问（Deep Pick）
// 通过路径字符串获取嵌套对象的类型
type DeepGet<T, Path extends string> =
  Path extends `${infer Key}.${infer Rest}`
    ? Key extends keyof T
      ? DeepGet<T[Key], Rest> // 递归进入下一层
      : never
    : Path extends keyof T
      ? T[Path] // 最后一层，返回属性类型
      : never;

interface Company {
  name: string;
  address: {
    city: string;
    street: {
      name: string;
      number: number;
    };
  };
  employees: {
    name: string;
    role: string;
  }[];
}

// 通过路径字符串访问深层类型
type City = DeepGet<Company, 'address.city'>; // string
type StreetName = DeepGet<Company, 'address.street.name'>; // string
type Invalid = DeepGet<Company, 'address.zip'>; // never（路径不存在）

// ====== 条件类型的分发特性 ======
// 当T是联合类型时，条件类型会分发（distribute）
type ToArray<T> = T extends any ? T[] : never;

// 联合类型中的每个成员都会分别应用条件类型
type Distributed = ToArray<string | number>;
// 等价于 ToArray<string> | ToArray<number>
// 即 string[] | number[]

// 阻止分发：用方括号包裹T
type ToArrayNoDistribute<T> = [T] extends [any] ? T[] : never;

type NotDistributed = ToArrayNoDistribute<string | number>;
// (string | number)[]（整个联合类型作为数组元素）

// ====== 实际应用：类型安全的EventEmitter ======
// 根据事件名推断回调参数类型
interface EventMap {
  click: { x: number; y: number };
  keydown: { key: string; code: string };
  load: { timestamp: number };
}

// 根据事件名获取对应的数据类型
type EventData<T extends keyof EventMap> = EventMap[T];

type ClickData = EventData<'click'>; // { x: number; y: number }
type KeyData = EventData<'keydown'>; // { key: string; code: string }
```

**💡 代码解释**：第5-12行：Awaited递归解包Promise。第15-25行：ElementType提取数组元素。第28-36行：FirstParameter提取函数参数。第40-69行：DeepGet深层属性访问。第73-83行：条件类型的分发特性。第88-100行：EventEmitter类型安全应用。

**🔑 关键要点**：
- infer在条件类型中推断类型
- 递归条件类型处理嵌套结构
- 模板字面量+infer解析路径
- 条件类型对联合类型自动分发

---

### 4. TS配置优化

> **级别**：高级 | **概念**：合理的TypeScript配置能大幅提升项目质量和开发体验，包括严格模式、编译目标、路径映射和项目引用等。

```typescript
// ====== tsconfig.json 最佳实践配置 ======
// {
//   "compilerOptions": {
//     /* ====== 严格模式 ====== */
//     "strict": true,                    // 开启所有严格检查（推荐）
//     // 等价于以下全部开启：
//     // "noImplicitAny": true,          // 禁止隐式any
//     // "strictNullChecks": true,       // 严格null检查
//     // "strictFunctionTypes": true,    // 严格函数类型检查
//     // "strictBindCallApply": true,    // 严格bind/call/apply
//     // "strictPropertyInitialization": true, // 严格属性初始化
//     // "noImplicitThis": true,          // 禁止隐式this
//     // "alwaysStrict": true,            // 始终使用严格模式
//
//     /* ====== 编译目标 ====== */
//     "target": "ES2020",                // 编译目标JS版本
//     "module": "ESNext",                // 模块系统
//     "lib": ["ES2020", "DOM"],          // 包含的库类型
//     "moduleResolution": "node",        // 模块解析策略
//
//     /* ====== 输出配置 ====== */
//     "outDir": "./dist",                // 输出目录
//     "rootDir": "./src",                // 源码根目录
//     "declaration": true,               // 生成.d.ts声明文件
//     "declarationMap": true,            // 生成声明文件的sourcemap
//     "sourceMap": true,                 // 生成sourcemap
//
//     /* ====== 路径映射 ====== */
//     "baseUrl": "./",                   // 基础路径
//     "paths": {                         // 路径别名
//       "@/*": ["src/*"],
//       "@components/*": ["src/components/*"]
//     },
//
//     /* ====== 额外检查 ====== */
//     "noUnusedLocals": true,            // 检查未使用的局部变量
//     "noUnusedParameters": true,         // 检查未使用的参数
//     "noFallthroughCasesInSwitch": true, // 检查switch穿透
//     "noUncheckedIndexedAccess": true,   // 索引访问包含undefined
//
//     /* ====== 其他 ====== */
//     "esModuleInterop": true,           // CommonJS/ESM互操作
//     "allowSyntheticDefaultImports": true, // 允许默认导入CJS模块
//     "forceConsistentCasingInFileNames": true, // 强制文件名大小写一致
//     "skipLibCheck": true,              // 跳过库类型检查（加速编译）
//     "resolveJsonModule": true,         // 允许导入JSON文件
//     "isolatedModules": true            // 每个文件独立编译
//   },
//
//   /* ====== 包含/排除 ====== */
//   "include": ["src/**/*"],             // 包含的文件
//   "exclude": ["node_modules", "dist"]  // 排除的文件
// }

// ====== 项目引用（Project References） ======
// 大型项目拆分为多个子项目
// 根 tsconfig.json
// {
//   "files": [],
//   "references": [
//     { "path": "./packages/core" },
//     { "path": "./packages/ui" },
//     { "path": "./packages/utils" }
//   ]
// }

// 子项目 tsconfig.json
// {
//   "compilerOptions": {
//     "composite": true,  // 必须启用
//     "declaration": true,
//     "outDir": "./dist",
//     "rootDir": "./src"
//   },
//   "include": ["src"]
// }

// 使用 tsc --build 编译所有项目
// 支持增量编译，只编译变更的部分

console.log('TS配置优化要点：');
console.log('1. strict: true 开启全部严格检查');
console.log('2. paths配置路径别名，简化导入');
console.log('3. noUncheckedIndexedAccess 防止索引越界');
console.log('4. 大型项目使用Project References');
console.log('5. skipLibCheck加速编译');
```

**💡 代码解释**：第3-56行：tsconfig.json最佳实践配置，包含strict严格模式、编译目标、输出配置、路径映射、额外检查等。第60-80行：Project References项目引用配置，拆分大型项目。第83-87行：配置优化要点总结。

**🔑 关键要点**：
- strict: true开启所有严格检查
- paths配置路径别名
- Project References拆分大型项目
- noUncheckedIndexedAccess防止索引越界

---

### 5. monorepo类型管理

> **级别**：高级 | **概念**：在monorepo（单仓库多包）项目中，通过TypeScript项目引用、路径映射和类型包的统一管理，实现跨包的类型共享和一致性。

```typescript
// ====== Monorepo项目结构 ======
// my-monorepo/
// ├── tsconfig.json          # 根配置，引用所有子项目
// ├── packages/
// │   ├── shared/            # 共享类型包
// │   │   ├── src/
// │   │   │   └── index.ts   # 导出所有共享类型
// │   │   ├── package.json
// │   │   └── tsconfig.json
// │   ├── core/              # 核心功能包
// │   │   ├── src/
// │   │   ├── package.json
// │   │   └── tsconfig.json
// │   └── ui/                # UI组件包
// │       ├── src/
// │       ├── package.json
// │       └── tsconfig.json
// └── package.json

// ====== 1. 共享类型包（packages/shared/src/index.ts） ======
// 集中管理所有跨包共享的类型定义

// 通用API响应类型
// export interface ApiResponse<T> {
//   code: number;
//   message: string;
//   data: T;
// }

// 分页类型
// export interface PaginatedResponse<T> {
//   items: T[];
//   total: number;
//   page: number;
//   pageSize: number;
// }

// 用户类型
// export interface User {
//   id: string;
//   name: string;
//   email: string;
// }

// ====== 2. 共享类型包的package.json ======
// {
//   "name": "@my-org/shared",
//   "version": "1.0.0",
//   "main": "./dist/index.js",
//   "types": "./dist/index.d.ts",
//   "exports": {
//     ".": {
//       "types": "./dist/index.d.ts",
//       "import": "./dist/index.js",
//       "require": "./dist/index.cjs"
//     }
//   }
// }

// ====== 3. 根tsconfig.json（项目引用） ======
// {
//   "files": [],
//   "references": [
//     { "path": "./packages/shared" },
//     { "path": "./packages/core" },
//     { "path": "./packages/ui" }
//   ]
// }

// ====== 4. 子包tsconfig.json（packages/core/tsconfig.json） ======
// {
//   "compilerOptions": {
//     "composite": true,           // 支持项目引用
//     "declaration": true,         // 生成类型声明
//     "declarationMap": true,      // 类型声明sourcemap
//     "outDir": "./dist",
//     "rootDir": "./src"
//   },
//   "include": ["src"],
//   "references": [
//     { "path": "../shared" }     // 引用共享类型包
//   ]
// }

// ====== 5. 使用共享类型 ======
// 在core包中导入shared包的类型
// import type { User, ApiResponse } from '@my-org/shared';

// async function getUser(id: string): Promise<ApiResponse<User>> {
//   const response = await fetch(`/api/users/${id}`);
//   return response.json();
// }

// ====== 6. 类型版本管理 ======
// 使用changesets或类似工具管理类型变更
// 类型变更视为breaking change，需要更新主版本号

// ====== 7. 类型检查脚本 ======
// package.json scripts:
// "typecheck": "tsc --build --force"
// "typecheck:watch": "tsc --build --watch"

console.log('Monorepo类型管理要点：');
console.log('1. 创建shared包集中管理共享类型');
console.log('2. 使用Project References建立依赖关系');
console.log('3. 启用composite支持增量编译');
console.log('4. 类型变更视为breaking change');
console.log('5. 使用tsc --build编译所有子项目');
console.log('6. exports字段控制包的类型导出');
```

**💡 代码解释**：第3-18行：monorepo目录结构。第22-34行：共享类型包集中管理类型。第38-51行：package.json配置types字段。第55-62行：根tsconfig.json引用子项目。第66-84行：子包tsconfig.json配置composite和references。第88-93行：使用共享类型。第97-105行：类型版本管理和检查脚本。

**🔑 关键要点**：
- shared包集中管理类型
- Project References建立包依赖
- composite启用增量编译
- exports字段控制类型导出

---

### 6. 递归类型与类型体操实战

> **级别**：高级 | **概念**：递归类型通过类型自身引用实现复杂类型运算，结合条件类型和infer可以构建JSON解析器、URL路由器、SQL查询构建器等类型层面的工具。

```typescript
// ====== 递归类型基础 ======
// 递归类型：类型定义中引用自身

// 1. 将嵌套数组展平为联合类型
type Flatten<T> =
  T extends any[]
    ? Flatten<T[number]> // 递归调用，展平数组
    : T; // 非数组类型直接返回

type F1 = Flatten<string[][]>; // string
type F2 = Flatten<[number, [string, [boolean]]]>; // number | string | boolean

// 2. 深度Readonly：递归只读所有嵌套属性
type DeepReadonly<T> = {
  readonly [K in keyof T]: T[K] extends object
    ? T[K] extends Function
      ? T[K] // 函数类型保持不变
      : DeepReadonly<T[K]> // 递归处理嵌套对象
    : T[K]; // 基本类型直接返回
};

interface Config {
  server: {
    host: string;
    port: number;
    ssl: { enabled: boolean; cert: string };
  };
  database: {
    url: string;
    pool: { min: number; max: number };
  };
}

type ReadonlyConfig = DeepReadonly<Config>;
// 所有层级属性均为readonly

// 3. 深度Partial：递归所有属性变为可选
type DeepPartial<T> = {
  [K in keyof T]?: T[K] extends object
    ? DeepPartial<T[K]>
    : T[K];
};

// 4. 路径类型：将嵌套对象路径转为联合类型
// 如 'server.host' | 'server.port' | 'server.ssl.enabled' ...
type Paths<T, P extends string = ''> =
  T extends object
    ? {
        [K in keyof T]: K extends string
          ? Paths<
              T[K],
              P extends '' ? K : `${P}.${K}`
            >
          : never;
      }[keyof T]
    : P;

type ConfigPaths = Paths<Config>;
// 'server' | 'server.host' | 'server.port' | ... | 'database.pool.max'

// ====== 类型体操实战：JSON解析器 ======
// 将JSON字符串解析为类型
type ParseJSON<T extends string> =
  T extends `"${infer S}"` ? S : // 字符串
  T extends `${infer N extends number}` ? N : // 数字
  T extends 'true' ? true : // 布尔
  T extends 'false' ? false :
  T extends 'null' ? null :
  T extends `[${infer A}]` ? // 数组
    A extends '' ? [] :
    A extends `${infer H},${infer R}` ? [ParseJSON<H>, ...ParseJSON<R>] :
    [ParseJSON<A>] :
  T extends `{${infer O}}` ? // 对象
    ParseJSONObject<O> :
  never;

type ParseJSONObject<T extends string> =
  T extends '' ? {} :
  T extends `${infer K}:${infer V};${infer Rest}` ?
    { [P in K]: ParseJSON<V> } & ParseJSONObject<Rest> :
  T extends `${infer K}:${infer V}` ?
    { [P in K]: ParseJSON<V> } :
  {};

// 实际使用很复杂，仅展示类型体操能力
// type Result = ParseJSON<'{"name":"张三","age":25}'>;

// ====== 类型体操实战：URL路径解析 ======
// 将URL路径模板转为参数对象类型
type ExtractParams<T extends string> =
  T extends `${infer _}:${infer Param}/${infer Rest}`
    ? { [K in Param]: string } & ExtractParams<`/${Rest}`>
    : T extends `${infer _}:${infer Param}`
      ? { [K in Param]: string }
      : {};

type UserParams = ExtractParams<'/users/:id/posts/:postId'>;
// { id: string; postId: string }

// ====== 类型体操实战：SQL查询构建器 ======
// 根据表定义推导SELECT返回类型
type TableSchema = {
  users: { id: number; name: string; email: string };
  posts: { id: number; title: string; userId: number };
};

type Select<
  T extends keyof TableSchema,
  C extends keyof TableSchema[T] = keyof TableSchema[T]
> = Pick<TableSchema[T], C>;

type UserRow = Select<'users', 'id' | 'name'>;
// { id: number; name: string }
type PostRow = Select<'posts'>;
// { id: number; title: string; userId: number }

// ====== 类型体操实战：EventEmitter类型安全 ======
// 根据事件Map推导emit和on的类型
type EventMap = {
  userLogin: { userId: string; timestamp: number };
  userLogout: { userId: string };
  pageView: { page: string; duration: number };
  error: { message: string; code: number };
};

// 类型安全的事件发射器类型
type TypedEmitter<T extends Record<string, any>> = {
  on<K extends keyof T>(
    event: K,
    handler: (data: T[K]) => void
  ): void;
  emit<K extends keyof T>(
    event: K,
    data: T[K]
  ): void;
};

// 使用TypedEmitter
// declare const emitter: TypedEmitter<EventMap>;
// emitter.on('userLogin', (data) => {
//   // data类型自动推导为 { userId: string; timestamp: number }
//   console.log(data.userId, data.timestamp);
// });
// emitter.emit('error', { message: '404', code: 404 });

// ====== 类型体操学习建议 ======
console.log('类型体操学习路径：');
console.log('1. 掌握基础：泛型、条件类型、infer');
console.log('2. 练习工具类型：Partial、Pick、Omit等');
console.log('3. 模板字面量类型：字符串拼接和解析');
console.log('4. 递归类型：嵌套结构的类型操作');
console.log('5. 挑战type-challenges项目');
console.log('https://github.com/type-challenges/type-challenges');
```

**💡 代码解释**：第3-13行：Flatten递归展平数组。第17-33行：DeepReadonly递归只读。第37-42行：DeepPartial递归可选。第46-62行：Paths将嵌套路径转为联合类型。第66-90行：JSON解析器类型体操。第94-104行：URL路径解析。第108-124行：SQL查询构建器。第128-152行：EventEmitter类型安全。第156-162行：学习建议。

**🔑 关键要点**：
- 递归类型处理嵌套结构
- 模板字面量+infer解析字符串
- 类型体操实战：JSON/URL/SQL
- type-challenges练习类型编程

---
