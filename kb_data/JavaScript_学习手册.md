# 🟨 JavaScript 编程语言学习手册

> **分类**：前端  
> **描述**：JavaScript是前端核心编程语言，负责网页交互逻辑、数据处理和异步通信  
> **生成时间**：2026-08-07  

---

## 目录

- [初级入门](#primary)（8个知识点）
- [中级进阶](#intermediate)（8个知识点）
- [高级精通](#advanced)（7个知识点）

---

## 初级入门

### 1. 变量声明：var/let/const

> **级别**：初级 | **概念**：JavaScript有三种变量声明方式：var（函数作用域，可重复声明）、let（块作用域，不可重复声明）、const（块作用域，声明常量不可重新赋值）。

```javascript
// ====== var：函数作用域，存在变量提升 ======
// var声明的变量会被提升到函数顶部，但赋值不会提升
console.log(x); // 输出 undefined（变量提升，声明被提升但未赋值）
var x = 10;
console.log(x); // 输出 10

var x = 20; // 可以重复声明（不推荐）
console.log(x); // 输出 20

// ====== let：块作用域，不可重复声明 ======
// let声明的变量只在{}块内有效，不存在变量提升（暂时性死区）
let y = 30;
// let y = 40; // ❌ 报错：不能重复声明同一变量

if (true) {
  let z = 50; // z只在if块内有效
  console.log(z); // 输出 50
}
// console.log(z); // ❌ 报错：z未定义（块外访问不到）

// ====== const：声明常量，必须初始化 ======
const PI = 3.14159; // 常量必须在声明时赋值
// PI = 3.14; // ❌ 报错：常量不能重新赋值

// const声明的对象/数组内容可以修改（引用不变）
const person = { name: '张三' };
person.name = '李四'; // ✅ 可以修改对象属性
// person = { name: '王五' }; // ❌ 报错：不能重新赋值引用
console.log(person.name); // 输出 李四
```

**💡 代码解释**：第2-6行：var存在变量提升，声明提升但值未提升，输出undefined。第7-8行：var可以重复声明。第11-16行：let是块作用域，在if块内声明的变量外部无法访问。第19-26行：const声明常量必须初始化且不可重新赋值，但对象属性可以修改。

**🔑 关键要点**：
- const优先，let次之，避免var
- let/const是块作用域
- const对象属性可修改
- 暂时性死区：let/const声明前不可用

---

### 2. 数据类型

> **级别**：初级 | **概念**：JavaScript有7种原始类型（string、number、boolean、null、undefined、symbol、bigint）和1种引用类型（object），typeof运算符用于检测类型。

```javascript
// ====== 原始类型（Primitive Types） ======
// string：字符串，用单引号、双引号或反引号包裹
const name = '张三'; // 单引号字符串
const greeting = "你好"; // 双引号字符串
const template = `欢迎 ${name}`; // 模板字符串（反引号），支持变量插值

// number：数字，整数和浮点数统一为64位浮点数
const age = 25; // 整数
const price = 99.99; // 浮点数
const notANumber = NaN; // 非数值，如 0/0 的结果
// typeof NaN 返回 "number"，这是一个历史遗留问题

// boolean：布尔值，只有true和false两个值
const isActive = true; // 真
const isDeleted = false; // 假

// null：表示空值或不存在的对象
const empty = null; // 故意设置为空
// typeof null 返回 "object"，这是JavaScript的经典bug

// undefined：变量已声明但未赋值
let notDefined; // 声明但未赋值，值为undefined
console.log(notDefined); // 输出 undefined

// bigint：大整数，用于表示超过Number.MAX_SAFE_INTEGER的整数
const bigNumber = 9007199254740991n; // 末尾加n表示BigInt类型

// symbol：唯一标识符，常用于对象属性名
const sym1 = Symbol('id'); // 每次创建都是唯一的
const sym2 = Symbol('id');
console.log(sym1 === sym2); // 输出 false（每次创建都是唯一值）

// ====== 引用类型：Object ======
const obj = { key: 'value' }; // 对象
const arr = [1, 2, 3]; // 数组也是对象
// typeof arr 返回 "object"
console.log(Array.isArray(arr)); // 输出 true（正确判断数组的方式）
```

**💡 代码解释**：第3-5行：字符串三种写法，模板字符串支持变量插值。第8-11行：数字类型包括整数、浮点数和NaN。第14-15行：布尔类型只有true/false。第18-19行：null表示空值，typeof null返回object是历史bug。第22-23行：undefined是未赋值的默认值。第26行：bigint末尾加n。第30-32行：symbol每次创建唯一值。第36-39行：数组也是对象，用Array.isArray()判断。

**🔑 关键要点**：
- 7种原始类型+1种引用类型
- typeof null返回'object'是bug
- 模板字符串用反引号支持插值
- Array.isArray()判断数组

---

### 3. 运算符

> **级别**：初级 | **概念**：JavaScript运算符包括算术运算符、比较运算符、逻辑运算符和赋值运算符，==和===的区别是常见的面试考点。

```javascript
// ====== 算术运算符 ======
let a = 10;
let b = 3;
console.log(a + b);  // 加法：13
console.log(a - b);  // 减法：7
console.log(a * b);  // 乘法：30
console.log(a / b);  // 除法：3.333...（JS没有整数除法）
console.log(a % b);  // 取余（模运算）：1
console.log(a ** b); // 幂运算：10^3 = 1000（ES6新增）

// 自增自减
let count = 0;
count++;  // 后置自增：先使用再+1
++count;  // 前置自增：先+1再使用
console.log(count); // 输出 2

// ====== 比较运算符（==  vs ===） ======
// == 宽松相等：会进行类型转换
console.log(5 == '5');   // true（字符串'5'被转为数字5）
console.log(0 == false); // true（false被转为0）
console.log(null == undefined); // true（特殊规则）

// === 严格相等：不进行类型转换（推荐使用）
console.log(5 === '5');  // false（类型不同直接返回false）
console.log(0 === false); // false
console.log(null === undefined); // false

// ====== 逻辑运算符（短路求值） ======
// && 逻辑与：遇到false就停止，返回第一个假值或最后一个真值
console.log(true && 'hello'); // 输出 'hello'（两个都真，返回最后一个）
console.log(false && 'hello'); // 输出 false（遇到假值停止）

// || 逻辑或：遇到true就停止，返回第一个真值或最后一个假值
const username = '' || '匿名用户'; // ''是假值，返回'匿名用户'
console.log(username); // 输出 '匿名用户'

// ?? 空值合并运算符（ES2020）：只在null/undefined时使用默认值
const value = null ?? '默认值'; // null触发默认值
console.log(value); // 输出 '默认值'
const value2 = 0 ?? '默认值'; // 0不会触发（??只认null/undefined）
console.log(value2); // 输出 0
```

**💡 代码解释**：第3-8行：算术运算符基础，注意JS没有整数除法。第11-13行：自增运算符前置和后置的区别。第17-20行：==会类型转换，容易产生意外结果。第22-25行：===严格相等，推荐使用。第29-31行：&&短路求值返回第一个假值或最后一个真值。第34-35行：||常用于设置默认值。第38-42行：??只处理null和undefined。

**🔑 关键要点**：
- ===严格相等，==会类型转换
- ||和&&支持短路求值
- ??只处理null/undefined
- 幂运算用**运算符

---

### 4. 条件语句

> **级别**：初级 | **概念**：条件语句根据表达式的真假来执行不同代码块，包括if-else、switch和三元运算符，是程序逻辑控制的基础。

```javascript
// ====== if-else：基本条件判断 ======
const score = 85;

// if语句：条件为真时执行代码块
if (score >= 90) {
  console.log('优秀');
} else if (score >= 80) {
  // else if：前面的条件不满足时，检查这个条件
  console.log('良好'); // 输出这个
} else if (score >= 60) {
  console.log('及格');
} else {
  // else：前面所有条件都不满足时执行
  console.log('不及格');
}

// ====== 三元运算符：条件 ? 真值 : 假值 ======
// 简洁的条件赋值，适合简单的二选一场景
const isLoggedIn = true;
const greeting = isLoggedIn ? '欢迎回来' : '请先登录';
console.log(greeting); // 输出 '欢迎回来'

// 三元运算符可以嵌套，但可读性差（不推荐）
const level = score >= 90 ? 'A' : score >= 80 ? 'B' : 'C';

// ====== switch：多分支选择 ======
const day = 3;
switch (day) {
  case 1:
    console.log('星期一');
    break; // break防止穿透到下一个case
  case 2:
    console.log('星期二');
    break;
  case 3:
    console.log('星期三'); // 输出这个
    break;
  default:
    // default：所有case都不匹配时执行
    console.log('未知日期');
    break;
}

// ====== 逻辑运算符的短路应用 ======
// 常用于条件渲染和默认值设置
const name = null;
// name为null时使用默认值
const displayName = name || '未知用户';
console.log(displayName); // 输出 '未知用户'
```

**💡 代码解释**：第3-13行：if-else链式判断，按顺序检查条件。第17-19行：三元运算符是if-else的简写形式。第23行：嵌套三元运算符可读性差。第27-38行：switch匹配值，break防止穿透，default兜底。第43-45行：||短路求值实现默认值。

**🔑 关键要点**：
- if-else是最基本的条件判断
- 三元运算符适合简单二选一
- switch要用break防止穿透
- 逻辑运算符可实现简洁条件

---

### 5. 循环

> **级别**：初级 | **概念**：循环用于重复执行代码块，包括for、while、do-while和for...of，选择合适的循环类型能提高代码可读性。

```javascript
// ====== for循环：最常用的循环，适合已知次数 ======
// 语法：for(初始化; 条件; 每次迭代后执行)
for (let i = 0; i < 5; i++) {
  // i从0开始，每次加1，直到i>=5时停止
  console.log(`第${i + 1}次循环`); // 输出1到5
}

// ====== while循环：条件为真时持续执行 ======
let count = 0;
while (count < 3) {
  // 先检查条件再执行，可能一次都不执行
  console.log(`count = ${count}`);
  count++; // 别忘了更新条件变量，否则会死循环
}

// ====== do-while循环：至少执行一次 ======
let num = 0;
do {
  // 先执行一次，再检查条件
  console.log(`num = ${num}`);
  num++;
} while (num < 0); // 条件为false，但已经执行了一次

// ====== for...of：遍历可迭代对象（数组、字符串等） ======
const fruits = ['苹果', '香蕉', '橙子'];
for (const fruit of fruits) {
  // 直接获取元素值，不需要索引
  console.log(fruit); // 依次输出 '苹果' '香蕉' '橙子'
}

// ====== 循环控制：break和continue ======
for (let i = 0; i < 10; i++) {
  if (i === 3) {
    continue; // 跳过当前迭代，直接进入下一次循环
  }
  if (i === 7) {
    break; // 立即终止整个循环
  }
  console.log(i); // 输出 0, 1, 2, 4, 5, 6（跳过了3，7时终止）
}
```

**💡 代码解释**：第3-6行：for循环三要素——初始化、条件、迭代。第9-13行：while先判断后执行，可能0次执行。第16-20行：do-while先执行后判断，至少执行1次。第24-28行：for...of直接获取元素值，比for...in更适合遍历数组。第32-39行：continue跳过当前迭代，break终止整个循环。

**🔑 关键要点**：
- for循环适合已知次数
- while先判断，do-while先执行
- for...of遍历数组值
- break终止循环，continue跳过当次

---

### 6. 函数基础

> **级别**：初级 | **概念**：函数是封装可复用代码的块，支持参数传递和返回值，是JavaScript最重要的抽象手段。

```javascript
// ====== 函数声明（Function Declaration） ======
// 使用function关键字声明，会被提升到作用域顶部
function add(a, b) {
  // a和b是形参（参数），接收调用时传入的值
  const result = a + b; // 执行加法运算
  return result; // return将结果返回给调用者
  // return后面的代码不会执行
}

// 调用函数，传入实参（实际参数）
const sum = add(5, 3);
console.log(sum); // 输出 8

// ====== 函数表达式（Function Expression） ======
// 将匿名函数赋值给变量，不会提升
const multiply = function(a, b) {
  return a * b;
};
console.log(multiply(4, 5)); // 输出 20

// ====== 箭头函数（Arrow Function）：ES6 ======
// 更简洁的函数写法，没有自己的this
const subtract = (a, b) => {
  return a - b;
};

// 函数体只有一行时，可以省略{}和return
const square = x => x * x; // 一个参数时可省略括号
console.log(square(5)); // 输出 25

// ====== 默认参数 ======
// 调用时未传参则使用默认值
function greet(name = '访客', greeting = '你好') {
  return `${greeting}，${name}！`;
}
console.log(greet()); // 输出 '你好，访客！'（使用默认值）
console.log(greet('张三', '欢迎')); // 输出 '欢迎，张三！'

// ====== 剩余参数（Rest Parameters） ======
// ...args将多个参数收集为数组
function sumAll(...numbers) {
  // numbers是一个数组，包含所有传入的参数
  return numbers.reduce((total, num) => total + num, 0);
}
console.log(sumAll(1, 2, 3, 4, 5)); // 输出 15
```

**💡 代码解释**：第3-8行：函数声明通过function关键字，有提升特性。第11-12行：调用函数传入实参。第16-19行：函数表达式赋值给变量。第23-25行：箭头函数标准写法。第28-29行：简化箭头函数，单行可省略{}和return。第33-36行：默认参数在未传值时启用。第40-44行：剩余参数...收集多个参数为数组。

**🔑 关键要点**：
- 函数声明会提升，表达式不会
- 箭头函数更简洁但无自己的this
- 默认参数在未传值时生效
- 剩余参数...收集为数组

---

### 7. 数组操作

> **级别**：初级 | **概念**：数组是JavaScript最常用的数据结构，提供了丰富的方法进行增删改查和遍历操作。

```javascript
// ====== 数组创建与访问 ======
// 字面量创建数组，元素可以是任意类型
const fruits = ['苹果', '香蕉', '橙子'];
console.log(fruits[0]); // 索引从0开始，输出 '苹果'
console.log(fruits.length); // length属性获取数组长度，输出 3
console.log(fruits[fruits.length - 1]); // 获取最后一个元素，输出 '橙子'

// ====== 添加和删除元素 ======
const numbers = [1, 2, 3];

// push：在末尾添加元素，返回新长度
numbers.push(4, 5); // numbers变为 [1, 2, 3, 4, 5]

// pop：删除并返回最后一个元素
const last = numbers.pop(); // last = 5, numbers变为 [1, 2, 3, 4]

// unshift：在开头添加元素
numbers.unshift(0); // numbers变为 [0, 1, 2, 3, 4]

// shift：删除并返回第一个元素
const first = numbers.shift(); // first = 0, numbers变为 [1, 2, 3, 4]

// ====== 遍历数组 ======
// forEach：对每个元素执行回调函数
fruits.forEach((fruit, index) => {
  // fruit是当前元素，index是当前索引
  console.log(`${index}: ${fruit}`);
});
// 输出：0: 苹果  1: 香蕉  2: 橙子

// ====== 数组转换方法 ======
// map：将每个元素映射为新值，返回新数组
const lengths = fruits.map(fruit => fruit.length);
console.log(lengths); // 输出 [2, 2, 2]（每个字符的length）

// filter：筛选符合条件的元素，返回新数组
const longNames = fruits.filter(fruit => fruit.length > 2);
console.log(longNames); // 输出 []（没有长度大于2的水果名）

// find：查找第一个符合条件的元素
const found = fruits.find(fruit => fruit === '香蕉');
console.log(found); // 输出 '香蕉'

// includes：判断是否包含某个元素
console.log(fruits.includes('苹果')); // 输出 true

// join：将数组元素连接为字符串
console.log(fruits.join('、')); // 输出 '苹果、香蕉、橙子'
```

**💡 代码解释**：第3-6行：数组索引从0开始，length属性获取长度。第10-20行：push/pop操作末尾，unshift/shift操作开头。第24-28行：forEach遍历每个元素。第32-33行：map返回新数组，每个元素被转换。第36-37行：filter返回符合条件的元素。第40-41行：find查找第一个匹配。第44-45行：includes判断存在性。第48行：join连接数组元素。

**🔑 关键要点**：
- push/pop操作末尾，unshift/shift开头
- map/filter返回新数组不修改原数组
- forEach没有返回值
- find返回第一个匹配元素

---

### 8. 对象基础

> **级别**：初级 | **概念**：对象是键值对的集合，是JavaScript最核心的数据结构，支持属性访问、修改和遍历。

```javascript
// ====== 对象创建与属性访问 ======
// 字面量创建对象，属性名和值用冒号分隔
const person = {
  name: '张三',       // 字符串属性
  age: 25,             // 数字属性
  isStudent: true,     // 布尔属性
  hobbies: ['读书', '跑步'], // 数组属性
  address: {           // 嵌套对象
    city: '北京',
    district: '朝阳区'
  }
};

// 点号访问：最常用的方式
console.log(person.name); // 输出 '张三'

// 方括号访问：属性名可以是变量或包含特殊字符
const key = 'age';
console.log(person[key]); // 输出 25（动态属性名）

// 嵌套属性访问
console.log(person.address.city); // 输出 '北京'

// ====== 属性操作 ======
// 添加新属性
person.email = 'zhangsan@example.com';

// 修改属性
person.age = 26;

// 删除属性
// delete person.isStudent; // 删除isStudent属性

// 检查属性是否存在
console.log('name' in person); // 输出 true（in运算符）
console.log(person.hasOwnProperty('name')); // 输出 true

// ====== 对象遍历 ======
// for...in：遍历对象的所有可枚举属性（包括继承的）
for (const key in person) {
  // 只处理自身属性，过滤掉原型链上的属性
  if (person.hasOwnProperty(key)) {
    console.log(`${key}: ${person[key]}`);
  }
}

// Object.keys()：获取所有自身属性的键名
const keys = Object.keys(person);
console.log(keys); // 输出 ['name', 'age', 'isStudent', 'hobbies', 'address', 'email']

// Object.values()：获取所有自身属性的值
const values = Object.values(person);
console.log(values);

// Object.entries()：获取键值对数组
const entries = Object.entries(person);
// 输出 [['name','张三'], ['age',26], ...]

// ====== 对象解构（ES6） ======
// 从对象中快速提取属性值
const { name, age, address: { city } } = person;
console.log(name, age, city); // 输出 '张三' 26 '北京'
// address: { city } 是嵌套解构，提取address.city
```

**💡 代码解释**：第3-11行：字面量创建对象，属性可以是任意类型。第14-18行：点号和方括号两种访问方式。第23-30行：添加/修改/删除属性，in和hasOwnProperty检查属性。第34-39行：for...in遍历所有属性，hasOwnProperty过滤继承属性。第43-50行：Object静态方法获取键、值、键值对。第54-57行：解构赋值快速提取属性。

**🔑 关键要点**：
- 对象是键值对集合
- 点号和方括号两种访问方式
- Object.keys/values/entries遍历
- 解构赋值快速提取属性

---

## 中级进阶

### 1. 闭包

> **级别**：中级 | **概念**：闭包是指函数能够访问其外部作用域变量的能力，即使外部函数已经执行完毕，它是JavaScript模块化和数据私有化的基础。

```javascript
// ====== 闭包基本概念 ======
// 内部函数可以访问外部函数的变量，即使外部函数已返回
function createCounter() {
  // count是createCounter的局部变量
  let count = 0;

  // 返回的函数形成了一个闭包，"记住"了count变量
  return function() {
    count++; // 访问外部函数的变量
    return count;
  };
}

// counter1和counter2各自拥有独立的闭包环境
const counter1 = createCounter();
const counter2 = createCounter();

console.log(counter1()); // 输出 1
console.log(counter1()); // 输出 2（count被保留在闭包中）
console.log(counter2()); // 输出 1（独立的闭包，不与counter1共享）

// ====== 闭包应用：数据私有化 ======
// 利用闭包创建私有变量，只能通过暴露的方法访问
function createPerson(name) {
  // 私有变量，外部无法直接访问
  let _name = name;

  return {
    // getName只读访问私有变量
    getName() {
      return _name;
    },
    // setName控制修改私有变量
    setName(newName) {
      if (typeof newName === 'string' && newName.length > 0) {
        _name = newName; // 可以加验证逻辑
      }
    }
  };
}

const person = createPerson('张三');
console.log(person.getName()); // 输出 '张三'
console.log(person._name); // 输出 undefined（私有变量外部不可访问）
person.setName('李四');
console.log(person.getName()); // 输出 '李四'

// ====== 闭包经典问题：循环中的闭包 ======
// 问题：setTimeout中使用var会共享同一个变量
for (var i = 0; i < 3; i++) {
  setTimeout(() => console.log(i), 100); // 输出 3, 3, 3（不是0,1,2）
}

// 解决方案1：使用let（块作用域）
for (let j = 0; j < 3; j++) {
  setTimeout(() => console.log(j), 100); // 输出 0, 1, 2
}

// 解决方案2：使用IIFE创建独立作用域
for (var k = 0; k < 3; k++) {
  ((index) => {
    setTimeout(() => console.log(index), 100); // 输出 0, 1, 2
  })(k); // 立即执行函数，将当前k值传入
}
```

**💡 代码解释**：第3-13行：createCounter演示闭包，返回的函数"记住"了外部count变量。第16-20行：每次调用createCounter创建独立闭包环境。第24-43行：利用闭包实现私有变量，_name外部无法访问，只能通过getName/setName操作。第47-49行：var在循环中共享同一变量，导致延迟输出全部为3。第52-54行：let解决——每个迭代有独立作用域。第57-61行：IIFE方案——立即执行函数传入当前值。

**🔑 关键要点**：
- 闭包：函数+其外部作用域变量
- 闭包实现数据私有化
- 每次调用创建独立闭包
- 循环中用let避免闭包陷阱

---

### 2. 原型链

> **级别**：中级 | **概念**：原型链是JavaScript实现继承的机制，每个对象都有一个内部[[Prototype]]链接指向其原型对象，通过__proto__或Object.getPrototypeOf访问。

```javascript
// ====== 原型链基础 ======
// 每个函数都有prototype属性，通过new创建的对象__proto__指向它
function Animal(name) {
  this.name = name; // 实例属性
}

// 在原型上添加方法，所有实例共享
Animal.prototype.speak = function() {
  console.log(`${this.name} 发出声音`);
};

const dog = new Animal('小狗');
const cat = new Animal('小猫');

// 实例通过原型链访问原型方法
dog.speak(); // 输出 '小狗 发出声音'
cat.speak(); // 输出 '小猫 发出声音'

// 验证原型链关系
console.log(dog.__proto__ === Animal.prototype); // true
console.log(Animal.prototype.__proto__ === Object.prototype); // true
console.log(Object.prototype.__proto__); // null（原型链终点）

// ====== 原型链继承 ======
// 子类继承父类的属性和方法
function Dog(name, breed) {
  // 调用父类构造函数，继承实例属性
  Animal.call(this, name); // call改变this指向，传入当前this
  this.breed = breed; // 子类特有属性
}

// 设置原型链：Dog.prototype.__proto__ = Animal.prototype
Dog.prototype = Object.create(Animal.prototype);
// 修复constructor指向
Dog.prototype.constructor = Dog;

// 子类方法
Dog.prototype.bark = function() {
  console.log(`${this.name} 汪汪叫！`);
};

const myDog = new Dog('旺财', '金毛');
myDog.speak(); // 继承自Animal的方法，输出 '旺财 发出声音'
myDog.bark();  // Dog自己的方法，输出 '旺财 汪汪叫！'

// ====== class语法糖：ES6 ======
// class本质上是原型链的语法糖，底层仍是原型继承
class AnimalClass {
  constructor(name) {
    this.name = name;
  }

  speak() {
    console.log(`${this.name} 发出声音`);
  }
}

// extends关键字实现继承
class DogClass extends AnimalClass {
  constructor(name, breed) {
    super(name); // super调用父类构造函数
    this.breed = breed;
  }

  bark() {
    console.log(`${this.name} 汪汪叫！`);
  }
}

const classDog = new DogClass('阿黄', '柯基');
classDog.speak(); // 输出 '阿黄 发出声音'
```

**💡 代码解释**：第3-13行：Animal.prototype上定义方法，所有实例共享。第16-21行：实例通过__proto__访问原型，原型链终点是null。第25-42行：通过call借用父类构造函数+Object.create设置原型链实现继承。第46-59行：class语法是原型链的语法糖，extends更直观。第62-67行：super调用父类方法。

**🔑 关键要点**：
- prototype是函数属性，__proto__是对象属性
- 原型链终点是null
- Object.create()设置原型链
- class是原型链的语法糖

---

### 3. Promise异步

> **级别**：中级 | **概念**：Promise是处理异步操作的对象，有三种状态（pending/fulfilled/rejected），通过.then()和.catch()链式处理结果，解决了回调地狱问题。

```javascript
// ====== Promise基本用法 ======
// 创建Promise：接收executor函数(resolve, reject)
const fetchData = new Promise((resolve, reject) => {
  // 模拟异步操作（如网络请求）
  setTimeout(() => {
    const success = true; // 模拟成功/失败
    if (success) {
      // resolve将Promise状态变为fulfilled，传递结果
      resolve({ id: 1, data: '用户数据' });
    } else {
      // reject将Promise状态变为rejected，传递错误
      reject(new Error('请求失败'));
    }
  }, 1000); // 1秒后执行
});

// 使用Promise
fetchData
  .then(result => {
    // then处理成功结果，可链式调用
    console.log('成功:', result); // 输出 成功: { id: 1, data: '用户数据' }
    return result.id; // 返回值会传递给下一个then
  })
  .then(id => {
    console.log('获取到ID:', id); // 输出 获取到ID: 1
  })
  .catch(error => {
    // catch捕获链中任何位置的错误
    console.error('错误:', error.message);
  })
  .finally(() => {
    // finally无论成功失败都会执行
    console.log('请求完成');
  });

// ====== Promise静态方法 ======
// Promise.all：等待所有Promise完成，全部成功才成功
const p1 = Promise.resolve(1); // 快速创建已解决的Promise
const p2 = Promise.resolve(2);
const p3 = Promise.resolve(3);

Promise.all([p1, p2, p3])
  .then(results => {
    console.log('全部完成:', results); // 输出 [1, 2, 3]
  })
  .catch(error => {
    // 任何一个失败，整个all就失败
    console.error('有请求失败:', error);
  });

// Promise.race：竞速，返回最先完成的Promise
const slow = new Promise(resolve => setTimeout(() => resolve('慢'), 2000));
const fast = new Promise(resolve => setTimeout(() => resolve('快'), 500));

Promise.race([slow, fast])
  .then(result => console.log('最快:', result)); // 输出 '最快: 快'

// Promise.allSettled：等待所有Promise完成（不论成败）
Promise.allSettled([p1, Promise.reject('失败'), p3])
  .then(results => {
    // results是数组，每个元素有status和value/reason
    results.forEach(r => console.log(r.status));
    // 输出 fulfilled, rejected, fulfilled
  });
```

**💡 代码解释**：第3-13行：new Promise创建异步任务，resolve成功，reject失败。第16-32行：.then链式处理成功结果，.catch捕获错误，.finally最终执行。第36-43行：Promise.all等待所有成功，一个失败则整体失败。第46-50行：Promise.race返回最快完成的。第53-58行：Promise.allSettled等待所有完成，不论成败。

**🔑 关键要点**：
- Promise三种状态：pending/fulfilled/rejected
- then链式调用，catch捕获错误
- Promise.all全部成功才成功
- Promise.race返回最快完成的

---

### 4. async/await

> **级别**：中级 | **概念**：async/await是Promise的语法糖，让异步代码看起来像同步代码，通过try-catch处理错误，大幅提升代码可读性。

```javascript
// ====== async函数基础 ======
// async关键字声明异步函数，函数返回Promise
async function fetchUser() {
  // await等待Promise完成，直接获取结果值
  // await只能在async函数内部使用
  const response = await fetch('https://api.example.com/user');
  // fetch返回Promise，await等待响应完成
  const data = await response.json(); // json()也返回Promise
  return data; // 返回的值自动包装为Promise
}

// ====== 错误处理：try-catch ======
async function getData() {
  try {
    // try块中放置可能出错的异步代码
    const user = await fetchUser();
    console.log('用户数据:', user);
    return user;
  } catch (error) {
    // catch块捕获异步操作中的错误
    console.error('获取数据失败:', error.message);
    // 返回默认值或重新抛出
    return null;
  }
}

// ====== 并行执行：Promise.all ======
async function loadAllData() {
  // await Promise.all让多个异步操作并行执行
  const [user, posts, settings] = await Promise.all([
    fetch('/api/user').then(r => r.json()),     // 获取用户
    fetch('/api/posts').then(r => r.json()),    // 获取文章
    fetch('/api/settings').then(r => r.json())  // 获取设置
  ]);
  // 三个请求同时发出，等待全部完成，比串行快三倍
  return { user, posts, settings };
}

// ====== 实际场景：模拟API调用 ======
// 模拟延迟函数
function delay(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

async function loginFlow() {
  console.log('开始登录...');
  await delay(500); // 模拟网络请求
  console.log('验证账号...');
  await delay(500);
  console.log('获取权限...');
  await delay(500);
  console.log('登录成功！');
  return { token: 'abc123', user: '张三' };

  // 上面的代码看起来像同步执行，但实际是异步的
  // 不会阻塞事件循环，其他任务可以继续执行
}

// 调用异步函数
loginFlow().then(result => {
  console.log('登录结果:', result);
});

// ====== async/await的陷阱 ======
// 注意：不要在循环中使用await导致串行执行
async function badExample() {
  const ids = [1, 2, 3, 4, 5];
  const results = [];
  // ❌ 错误：串行执行，每次等待前一个完成
  for (const id of ids) {
    results.push(await fetch(`/api/item/${id}`));
  }
}

// ✅ 正确：并行执行
async function goodExample() {
  const ids = [1, 2, 3, 4, 5];
  const results = await Promise.all(
    ids.map(id => fetch(`/api/item/${id}`))
  );
}
```

**💡 代码解释**：第3-8行：async函数返回Promise，await等待Promise完成。第12-21行：try-catch捕获异步错误，比Promise.catch更直观。第25-33行：Promise.all实现并行请求，比串行快得多。第37-58行：模拟登录流程展示了async/await的同步风格写法。第63-74行：对比串行（错误）和并行（正确）的区别。

**🔑 关键要点**：
- async函数返回Promise
- await只能在async函数内使用
- try-catch处理异步错误
- Promise.all实现并行请求

---

### 5. 事件循环（Event Loop）

> **级别**：中级 | **概念**：事件循环是JavaScript的运行机制，通过调用栈、任务队列（宏任务/微任务）管理异步代码的执行顺序，保证单线程下的非阻塞运行。

```javascript
// ====== 事件循环执行顺序演示 ======
// JavaScript是单线程，通过事件循环实现异步非阻塞

console.log('1: 同步代码开始'); // 同步代码，立即执行

// setTimeout：宏任务（MacroTask）
setTimeout(() => {
  // 宏任务在事件循环的下一轮执行
  console.log('2: setTimeout 宏任务');
}, 0); // 即使延迟为0，也是异步执行

// Promise.then：微任务（MicroTask）
Promise.resolve().then(() => {
  // 微任务在当前宏任务执行完后立即执行
  console.log('3: Promise.then 微任务');
});

// 另一个微任务
Promise.resolve().then(() => {
  console.log('4: 第二个微任务');
  // 微任务中嵌套微任务，会添加到当前微任务队列
  Promise.resolve().then(() => {
    console.log('5: 嵌套微任务');
  });
});

console.log('6: 同步代码结束');

// 实际输出顺序：
// 1: 同步代码开始
// 6: 同步代码结束
// 3: Promise.then 微任务
// 4: 第二个微任务
// 5: 嵌套微任务
// 2: setTimeout 宏任务

// ====== 执行顺序总结 ======
// 1. 执行同步代码（调用栈）
// 2. 清空微任务队列（Promise.then/catch/finally, MutationObserver）
// 3. 执行一个宏任务（setTimeout, setInterval, I/O）
// 4. 再次清空微任务队列
// 5. 重复步骤3-4

// ====== 实际应用：DOM更新时机 ======
// 微任务在DOM渲染前执行，宏任务在DOM渲染后执行
document.body.innerHTML = '<div id="box">Hello</div>';

// 微任务：此时DOM已更新但浏览器可能还没渲染
Promise.resolve().then(() => {
  const box = document.getElementById('box');
  console.log('微任务中获取DOM:', box.textContent); // 可以获取到
});

// 宏任务：此时浏览器已经渲染完毕
setTimeout(() => {
  const box = document.getElementById('box');
  console.log('宏任务中获取DOM:', box.textContent); // 可以获取到
}, 0);

// ====== async/await与微任务 ======
async function asyncFunc() {
  console.log('async函数内同步代码'); // 同步执行
  await Promise.resolve(); // await后面的代码相当于.then()，是微任务
  console.log('await后的代码，作为微任务执行');
}

asyncFunc();
console.log('async函数调用后');
// 输出顺序：async函数内同步代码 → async函数调用后 → await后的代码
```

**💡 代码解释**：第4-29行：演示了同步代码、宏任务、微任务的执行顺序，微任务优先于宏任务。第32-36行：事件循环的执行步骤总结。第40-50行：展示了微任务和宏任务与DOM渲染的关系。第54-62行：async/await本质是Promise的语法糖，await后的代码是微任务。

**🔑 关键要点**：
- 同步 > 微任务 > 宏任务
- 微任务：Promise.then、MutationObserver
- 宏任务：setTimeout、setInterval、I/O
- await后面的代码是微任务

---

### 6. 模块化：ESM与CommonJS

> **级别**：中级 | **概念**：JavaScript模块化经历了CommonJS（Node.js）和ES Modules（ES6标准）两个主要阶段，ESM支持静态分析和Tree Shaking。

```javascript
// ====== ESM（ES Modules）：现代标准 ======
// 使用export导出，import导入
// 文件：math.js
// 命名导出：可以导出多个
// export const PI = 3.14159;

// export function add(a, b) {
//   return a + b;
// }

// export function multiply(a, b) {
//   return a * b;
// }

// 默认导出：每个模块只能有一个
// export default function subtract(a, b) {
//   return a - b;
// }

// 文件：main.js - 导入方式
// 命名导入：必须使用花括号，名称要匹配
// import { add, multiply, PI } from './math.js';

// 默认导入：不需要花括号，可以任意命名
// import subtract from './math.js';

// 混合导入：同时导入默认和命名导出
// import subtract, { add, PI } from './math.js';

// 全部导入：使用 * 创建命名空间对象
// import * as math from './math.js';
// console.log(math.add(1, 2), math.PI);

// 动态导入：返回Promise，支持按需加载
// import('./math.js').then(module => {
//   console.log(module.add(1, 2));
// });

// ====== CommonJS：Node.js标准 ======
// 使用module.exports导出，require导入
// 文件：math.cjs
// const PI = 3.14159;

// function add(a, b) {
//   return a + b;
// }

// 导出方式1：分别导出
// module.exports = { PI, add };

// 导出方式2：单个导出
// exports.PI = PI;
// exports.add = add;

// 文件：main.cjs - 导入
// const math = require('./math.cjs');
// console.log(math.add(1, 2));

// 解构导入
// const { add, PI } = require('./math.cjs');

console.log('// ====== ESM vs CommonJS 关键区别 ======');
console.log('// ESM是静态的，import提升到顶部');
console.log('// CommonJS是动态的，require可以在条件中使用');
console.log('// ESM的this是undefined，CommonJS的this指向当前模块');
console.log('// ESM输出的是值的引用（动态绑定）');
console.log('// CommonJS输出的是值的拷贝');

// ====== ESM动态绑定演示 ======
// 文件：counter.js
// export let count = 0;
// export function increment() { count++; }

// 文件：main.js
// import { count, increment } from './counter.js';
// console.log(count); // 0
// increment();
// console.log(count); // 1（ESM获取的是最新值）

// CommonJS版本
// const counter = require('./counter.cjs');
// console.log(counter.count); // 0
// counter.increment();
// console.log(counter.count); // 0（CommonJS是值的拷贝）
```

**💡 代码解释**：第3-30行：ESM的export/import语法，包括命名导出、默认导出、混合导入、动态导入。第33-46行：CommonJS的module.exports和require语法。第49-54行：ESM和CJS的关键区别总结。第57-70行：ESM动态绑定vs CJS值拷贝的对比演示。

**🔑 关键要点**：
- ESM是静态编译，支持Tree Shaking
- CommonJS是运行时动态加载
- ESM输出引用，CJS输出拷贝
- import()动态导入实现按需加载

---

### 7. 错误处理

> **级别**：中级 | **概念**：JavaScript提供try-catch-finally、throw语句和Error对象来进行错误处理，结合Promise的.catch()和async/await的try-catch实现全面的错误管理。

```javascript
// ====== try-catch-finally ======
function parseJSON(jsonString) {
  try {
    // try块中放置可能出错的代码
    const result = JSON.parse(jsonString);
    // 如果JSON格式正确，正常返回结果
    return result;
  } catch (error) {
    // catch块捕获错误，error包含错误信息
    console.error('JSON解析失败:', error.message);
    // error.name：错误类型（如SyntaxError）
    // error.message：错误描述
    // error.stack：调用堆栈
    return null; // 返回默认值，防止程序崩溃
  } finally {
    // finally块无论成功失败都会执行
    console.log('解析完成（无论成功或失败）');
    // 常用于清理资源，如关闭文件、清除定时器等
  }
}

parseJSON('{ invalid json }'); // 触发catch

// ====== 自定义错误：throw ======
// 创建自定义错误类
class ValidationError extends Error {
  constructor(message, field) {
    super(message); // 调用父类构造函数
    this.name = 'ValidationError'; // 自定义错误名称
    this.field = field; // 额外信息：哪个字段验证失败
  }
}

function validateAge(age) {
  if (typeof age !== 'number') {
    // throw抛出自定义错误
    throw new ValidationError('年龄必须是数字', 'age');
  }
  if (age < 0 || age > 150) {
    throw new RangeError('年龄必须在0-150之间');
  }
  return true;
}

// 使用自定义错误
try {
  validateAge('abc');
} catch (error) {
  if (error instanceof ValidationError) {
    // 根据错误类型做不同处理
    console.error(`字段${error.field}验证失败: ${error.message}`);
  } else {
    console.error('未知错误:', error.message);
  }
}

// ====== 全局错误捕获 ======
// 捕获未处理的Promise rejection
// window.addEventListener('unhandledrejection', event => {
//   console.error('未处理的Promise拒绝:', event.reason);
//   event.preventDefault(); // 阻止默认控制台输出
// });

// 捕获全局未捕获的错误
// window.addEventListener('error', event => {
//   console.error('全局错误:', event.message, event.filename, event.lineno);
// });

console.log('错误处理最佳实践：');
console.log('1. 在Promise链末尾添加.catch()');
console.log('2. async/await使用try-catch包裹');
console.log('3. 创建自定义错误类细化错误类型');
console.log('4. 添加全局错误处理兜底');
```

**💡 代码解释**：第3-19行：try-catch-finally基本语法，finally用于清理。第23-32行：自定义错误类继承Error，添加额外字段。第35-46行：throw抛出自定义错误，catch中用instanceof判断类型。第50-59行：全局错误事件监听（注释形式）。第62-66行：错误处理最佳实践总结。

**🔑 关键要点**：
- try-catch-finally捕获同步错误
- throw可以抛出自定义错误
- 自定义错误类继承Error
- 全局unhandledrejection和error事件

---

### 8. DOM操作

> **级别**：中级 | **概念**：DOM（文档对象模型）是浏览器提供的API，用于操作HTML文档结构和内容，包括查询元素、修改属性、创建节点和事件处理。

```javascript
// ====== 查询DOM元素 ======
// 通过ID获取单个元素
const header = document.getElementById('header');

// 通过CSS选择器获取第一个匹配元素
const button = document.querySelector('.btn-primary');

// 通过CSS选择器获取所有匹配元素（返回NodeList）
const items = document.querySelectorAll('.list-item');
// NodeList是类数组，可以用forEach遍历
items.forEach(item => console.log(item.textContent));

// 通过类名获取（返回HTMLCollection，实时更新）
const cards = document.getElementsByClassName('card');

// ====== 修改DOM内容 ======
// 修改文本内容（HTML会被转义，安全）
header.textContent = '新标题';

// 修改HTML内容（会解析HTML标签，注意XSS风险）
// header.innerHTML = '<span>新标题</span>';

// 修改属性
button.setAttribute('disabled', 'true'); // 禁用按钮
button.removeAttribute('disabled'); // 移除禁用
button.classList.add('active'); // 添加类名
button.classList.remove('active'); // 移除类名
button.classList.toggle('dark'); // 切换类名（有则删，无则加）

// 修改样式
button.style.backgroundColor = '#3498db';
button.style.cssText = 'color: #fff; padding: 10px;';

// ====== 创建和插入DOM节点 ======
// 创建新元素
const newDiv = document.createElement('div');
newDiv.textContent = '我是新创建的div';
newDiv.className = 'new-item'; // 设置类名

// 插入到指定位置
const container = document.querySelector('.container');
container.appendChild(newDiv); // 追加到末尾

// insertBefore：插入到指定元素之前
// container.insertBefore(newDiv, container.firstChild);

// 使用innerHTML批量创建（更高效）
container.insertAdjacentHTML('beforeend', `
  <div class="card">
    <h3>卡片标题</h3>
    <p>卡片内容</p>
  </div>
`);
// beforeend在容器末尾插入，beforebegin在容器之前插入
// afterbegin在容器开头插入，afterend在容器之后插入

// ====== 事件处理 ======
// addEventListener：添加事件监听器
button.addEventListener('click', function(event) {
  // event是事件对象，包含事件相关信息
  console.log('按钮被点击了');
  console.log('点击坐标:', event.clientX, event.clientY);
  // event.target：触发事件的元素
  // event.preventDefault()：阻止默认行为
  // event.stopPropagation()：阻止事件冒泡
});

// 事件委托：将事件监听器绑定到父元素
// 利用事件冒泡，高效处理动态添加的子元素
document.querySelector('.list').addEventListener('click', function(event) {
  // 判断点击的是否是目标子元素
  if (event.target.matches('.list-item')) {
    console.log('点击了列表项:', event.target.textContent);
  }
});

// 移除事件监听器
// button.removeEventListener('click', handlerFunction);
```

**💡 代码解释**：第3-10行：querySelector/querySelectorAll使用CSS选择器查找元素。第14-26行：修改DOM内容的多种方式，textContent安全，innerHTML有XSS风险。第30-45行：createElement创建节点，appendChild/insertAdjacentHTML插入。第49-56行：addEventListener绑定事件。第60-66行：事件委托利用冒泡处理动态元素。

**🔑 关键要点**：
- querySelector支持CSS选择器
- textContent安全，innerHTML有XSS风险
- classList操作类名最方便
- 事件委托减少监听器数量

---

## 高级精通

### 1. Proxy与Reflect

> **级别**：高级 | **概念**：Proxy用于创建对象的代理，拦截并自定义基本操作（读取、赋值、遍历等），Reflect提供对应的默认行为方法，两者结合构建强大的元编程能力。

```javascript
// ====== Proxy基础：拦截对象操作 ======
// Proxy(target, handler)：target是目标对象，handler是拦截器
const target = { name: '张三', age: 25 };

const handler = {
  // get陷阱：拦截属性读取
  get(obj, prop, receiver) {
    console.log(`读取属性: ${String(prop)}`);
    if (prop in obj) {
      // Reflect.get()执行默认的读取操作，更安全
      return Reflect.get(obj, prop, receiver);
    }
    return `属性 ${String(prop)} 不存在`; // 不存在时返回默认值
  },

  // set陷阱：拦截属性赋值
  set(obj, prop, value, receiver) {
    // 添加验证逻辑：年龄必须是数字
    if (prop === 'age' && typeof value !== 'number') {
      throw new TypeError('年龄必须是数字类型');
    }
    console.log(`设置属性: ${String(prop)} = ${value}`);
    // Reflect.set()返回布尔值表示是否成功
    return Reflect.set(obj, prop, value, receiver);
  },

  // deleteProperty陷阱：拦截属性删除
  deleteProperty(obj, prop) {
    if (prop === 'name') {
      console.warn('不允许删除name属性');
      return false; // 阻止删除
    }
    return Reflect.deleteProperty(obj, prop);
  },

  // has陷阱：拦截 in 运算符
  has(obj, prop) {
    console.log(`检查属性是否存在: ${String(prop)}`);
    return Reflect.has(obj, prop);
  }
};

const proxy = new Proxy(target, handler);

console.log(proxy.name); // 触发get陷阱，输出 '张三'
console.log(proxy.nonexistent); // 输出 '属性 nonexistent 不存在'
proxy.age = 30; // 触发set陷阱
// proxy.age = 'abc'; // 抛出 TypeError
delete proxy.name; // 触发deleteProperty陷阱，删除被阻止

// ====== 实际应用：响应式数据 ======
// 利用Proxy实现简单的响应式系统
function createReactive(obj, onChange) {
  return new Proxy(obj, {
    set(target, prop, value, receiver) {
      const oldValue = target[prop];
      const result = Reflect.set(target, prop, value, receiver);
      // 值发生变化时触发回调
      if (oldValue !== value) {
        onChange(prop, value, oldValue);
      }
      return result;
    },
    deleteProperty(target, prop) {
      const hadKey = prop in target;
      const result = Reflect.deleteProperty(target, prop);
      if (hadKey) {
        onChange(prop, undefined, target[prop]);
      }
      return result;
    }
  });
}

const state = createReactive({ count: 0, text: '' }, (key, newVal, oldVal) => {
  console.log(`[响应式] ${key}: ${oldVal} -> ${newVal}`);
});

state.count++; // 输出 [响应式] count: 0 -> 1
state.text = 'hello'; // 输出 [响应式] text:  -> hello
```

**💡 代码解释**：第3-38行：proxy拦截get/set/deleteProperty/has操作，Reflect执行默认行为。第42-68行：利用Proxy实现响应式数据，set陷阱中比较新旧值，变化时触发onChange回调。这是Vue 3响应式系统的核心原理。

**🔑 关键要点**：
- Proxy拦截13种基本操作
- Reflect提供默认行为方法
- Proxy实现响应式数据
- Vue 3响应式基于Proxy

---

### 2. Generator与Iterator

> **级别**：高级 | **概念**：Generator函数（function*）支持暂停和恢复执行，通过yield产出值，搭配next()控制流程，是异步编程和惰性计算的强大工具。

```javascript
// ====== Generator基础 ======
// function* 定义Generator函数，yield暂停执行
function* numberGenerator() {
  console.log('开始生成');
  yield 1; // 第一次调用next()停在这里，返回{value:1, done:false}
  console.log('生成第一个后');
  yield 2; // 第二次调用next()停在这里
  console.log('生成第二个后');
  yield 3; // 第三次调用next()停在这里
  console.log('生成完毕');
  return '完成'; // 最后一次next()得到{value:'完成', done:true}
}

const gen = numberGenerator();
console.log(gen.next()); // { value: 1, done: false }
console.log(gen.next()); // { value: 2, done: false }
console.log(gen.next()); // { value: 3, done: false }
console.log(gen.next()); // { value: '完成', done: true }
// done: true表示Generator已执行完毕

// ====== 双向通信：next()传参 ======
function* interactiveGenerator() {
  // 第一次next()的参数会被忽略
  const name = yield '请问你的名字？';
  // 第二次next()传入的值会赋给name
  const age = yield `${name}，请问你的年龄？`;
  // 第三次next()传入的值
  yield `${name}，你${age}岁了。`;
  return '对话结束';
}

const chat = interactiveGenerator();
console.log(chat.next().value); // 输出 '请问你的名字？'
console.log(chat.next('小明').value); // 输出 '小明，请问你的年龄？'
console.log(chat.next('20').value); // 输出 '小明，你20岁了。'

// ====== Generator实现迭代器 ======
// 自定义可迭代对象
const range = {
  from: 1,
  to: 5,
  // [Symbol.iterator]让对象可被for...of遍历
  *[Symbol.iterator]() {
    for (let i = this.from; i <= this.to; i++) {
      yield i; // 每次yield产出序列中的一个值
    }
  }
};

// for...of自动调用[Symbol.iterator]()
for (const num of range) {
  console.log(num); // 依次输出 1 2 3 4 5
}
console.log([...range]); // 展开运算符也使用迭代器，输出 [1, 2, 3, 4, 5]

// ====== Generator用于异步流程控制 ======
// 模拟异步操作
function fetchData(url) {
  return new Promise(resolve => {
    setTimeout(() => resolve(`${url} 的数据`), 1000);
  });
}

// Generator + 自动执行器 = async/await 的底层原理
function* asyncFlow() {
  const data1 = yield fetchData('/api/user');
  console.log('第一步:', data1);
  const data2 = yield fetchData('/api/posts');
  console.log('第二步:', data2);
  return '全部完成';
}

// 自动执行器：递归调用next()处理Promise
function run(generator) {
  const gen = generator();

  function handle(result) {
    if (result.done) return Promise.resolve(result.value);
    return Promise.resolve(result.value).then(
      res => handle(gen.next(res)),  // 成功，继续执行
      err => handle(gen.throw(err))  // 失败，抛出错误
    );
  }

  return handle(gen.next());
}

// run(asyncFlow).then(result => console.log(result));
```

**💡 代码解释**：第3-19行：Generator基础，yield暂停，next()恢复。第22-35行：next()可以传参实现双向通信，第二次next传入的值会赋给第一个yield的左边。第39-52行：Generator实现[Symbol.iterator]让对象可迭代。第56-85行：Generator+自动执行器模拟async/await原理。

**🔑 关键要点**：
- function*声明Generator
- yield暂停，next()恢复
- next()可传参实现双向通信
- Generator是async/await的底层基础

---

### 3. Web Workers

> **级别**：高级 | **概念**：Web Workers在后台线程中运行JavaScript，不阻塞主线程UI渲染，适合处理CPU密集型计算任务，通过postMessage进行线程间通信。

```javascript
// ====== 主线程代码（main.js） ======
// 创建Worker实例，指定Worker脚本文件路径
const worker = new Worker('worker.js');

// 向Worker发送消息：postMessage()发送数据
worker.postMessage({ type: 'CALCULATE', data: { start: 1, end: 1000000 } });

// 接收Worker返回的消息：onmessage监听
worker.onmessage = function(event) {
  // event.data是Worker发送回来的数据
  console.log('Worker计算结果:', event.data);
  // 计算完成后可以终止Worker释放资源
  // worker.terminate();
};

// 监听Worker中的错误
worker.onerror = function(error) {
  console.error('Worker错误:', error.message, '行号:', error.lineno);
};

// 主动终止Worker
// worker.terminate();

// ====== Worker线程代码（worker.js） ======
// Worker内部是独立的全局作用域，self指向Worker自身
// Worker中不能访问DOM、window对象

// self.onmessage监听主线程发来的消息
// self.onmessage = function(event) {
//   const { type, data } = event.data;
//
//   if (type === 'CALCULATE') {
//     // 执行CPU密集型计算，不会阻塞主线程
//     let sum = 0;
//     for (let i = data.start; i <= data.end; i++) {
//       sum += i;
//     }
//
//     // 将结果发送回主线程
//     self.postMessage({ type: 'RESULT', sum });
//   }
// };

// Worker中也可以使用importScripts导入其他脚本
// importScripts('/js/helper.js');

console.log('主线程中Worker创建完毕，不阻塞UI');

// ====== SharedWorker：多个页面共享 ======
// 多个同源页面可以共享同一个Worker
// const sharedWorker = new SharedWorker('shared-worker.js');

// 通过port进行通信
// sharedWorker.port.onmessage = function(event) {
//   console.log('SharedWorker消息:', event.data);
// };
// sharedWorker.port.start(); // 启动端口
// sharedWorker.port.postMessage('Hello from page');

// ====== 实际应用：图片处理 ======
// 将大文件解析、图片滤镜等耗时操作放到Worker中
// const imageWorker = new Worker('image-worker.js');

// 使用Transferable对象传输（零拷贝，性能更好）
// const buffer = new ArrayBuffer(1024);
// worker.postMessage({ buffer }, [buffer]);
// 传输后主线程中buffer变为空，所有权转移到Worker
```

**💡 代码解释**：第3-16行：主线程创建Worker，通过postMessage发送消息，onmessage接收结果。第19-34行：Worker线程代码（注释形式），通过self.onmessage接收，self.postMessage发送。第39-48行：SharedWorker允许多页面共享。第53-58行：Transferable对象实现零拷贝传输。

**🔑 关键要点**：
- Worker在独立线程运行，不阻塞UI
- 主线程和Worker通过postMessage通信
- Worker不能访问DOM和window
- Transferable对象实现零拷贝

---

### 4. 内存管理与WeakMap

> **级别**：高级 | **概念**：JavaScript的垃圾回收基于引用计数和标记清除，WeakMap和WeakSet使用弱引用，不阻止垃圾回收，适合缓存和私有数据存储。

```javascript
// ====== JavaScript内存管理 ======
// JS引擎通过垃圾回收(GC)自动管理内存
// 主要策略：标记-清除(Mark-and-Sweep)

// 内存泄漏常见原因：
// 1. 全局变量：始终可达，不会被回收
// 2. 未清理的定时器：setInterval持续运行
// 3. 闭包引用：闭包保持外部变量引用
// 4. DOM引用：移除DOM节点后JS仍持有引用
// 5. 事件监听器未移除

// ====== 避免内存泄漏 ======
// 清理定时器
let timerId = setInterval(() => console.log('tick'), 1000);
// 不再需要时清理
// clearInterval(timerId);
// timerId = null; // 解除引用

// 移除事件监听器
function handleClick() { console.log('clicked'); }
document.addEventListener('click', handleClick);
// 组件销毁时移除
// document.removeEventListener('click', handleClick);

// ====== WeakMap：弱引用键 ======
// WeakMap的键是弱引用，当键所指对象被回收时，WeakMap中的条目自动删除
// 适用于：缓存、私有数据
const weakMap = new WeakMap();

// WeakMap只接受对象作为键
let user = { name: '张三' };
// 将user作为键，关联一些元数据
weakMap.set(user, { lastAccess: Date.now(), visits: 5 });

console.log(weakMap.get(user)); // 获取关联数据
console.log(weakMap.has(user)); // 检查是否存在

// 当user被设为null时，WeakMap中的条目会被自动垃圾回收
// user = null; // 没有其他引用时，WeakMap条目自动清除

// WeakMap无法被遍历（没有keys/values/entries方法）
// 这是为了保护弱引用特性

// ====== 实际应用：DOM节点元数据 ======
// 为DOM节点存储额外数据，节点移除时自动清理
const nodeDataMap = new WeakMap();

function attachData(node, data) {
  nodeDataMap.set(node, data);
}

function getData(node) {
  return nodeDataMap.get(node);
}

// 当DOM节点从文档中移除且没有其他引用时，
// WeakMap中的相关数据会自动被垃圾回收，无需手动清理

// ====== WeakSet：弱引用集合 ======
const weakSet = new WeakSet();

// 用于标记对象，不阻止垃圾回收
let obj = { id: 1 };
weakSet.add(obj);
console.log(weakSet.has(obj)); // true
// obj = null; // 自动回收

// 实际应用：标记已处理的对象
const processedNodes = new WeakSet();
function processNode(node) {
  if (processedNodes.has(node)) return; // 已处理过
  // 处理逻辑...
  processedNodes.add(node); // 标记为已处理
}
```

**💡 代码解释**：第3-10行：JS垃圾回收机制和内存泄漏常见原因。第13-21行：定时器和事件监听器的清理示例。第25-42行：WeakMap使用弱引用键，对象被回收时条目自动清除。第46-56行：WeakMap应用于DOM节点元数据存储。第60-72行：WeakSet类似用途，用于标记对象。

**🔑 关键要点**：
- 标记-清除是主要GC策略
- WeakMap键是弱引用，不阻止GC
- WeakMap不能遍历，无size属性
- WeakMap适合缓存和元数据存储

---

### 5. 性能优化：防抖与节流

> **级别**：高级 | **概念**：防抖（debounce）将多次高频调用合并为一次，在最后一次触发后延迟执行；节流（throttle）限制函数在固定时间间隔内只执行一次，是前端性能优化的核心手段。

```javascript
// ====== 防抖（Debounce） ======
// 场景：搜索框输入、窗口resize、表单验证
// 在事件触发n秒后才执行，如果n秒内再次触发则重新计时
function debounce(fn, delay = 300) {
  // 闭包保存定时器ID
  let timer = null;

  // 返回防抖后的函数
  return function(...args) {
    // 每次调用先清除之前的定时器
    if (timer) clearTimeout(timer);

    // 设置新的定时器，delay毫秒后执行
    timer = setTimeout(() => {
      // 使用apply确保this指向和参数传递正确
      fn.apply(this, args);
      timer = null; // 执行后清除引用
    }, delay);
  };
}

// 使用示例：搜索输入防抖
// const searchInput = document.getElementById('search');
// const handleSearch = debounce(function(event) {
//   console.log('搜索:', event.target.value);
//   // 发送API请求
// }, 500);
// searchInput.addEventListener('input', handleSearch);

// ====== 节流（Throttle） ======
// 场景：滚动事件、鼠标移动、游戏帧率控制
// 在固定时间间隔内只执行一次函数
function throttle(fn, interval = 300) {
  // 记录上次执行时间
  let lastTime = 0;

  return function(...args) {
    const now = Date.now();

    // 当前时间与上次执行时间差 >= interval时执行
    if (now - lastTime >= interval) {
      lastTime = now; // 更新上次执行时间
      fn.apply(this, args);
    }
  };
}

// 使用示例：滚动事件节流
// const handleScroll = throttle(function() {
//   console.log('滚动位置:', window.scrollY);
//   // 更新UI或懒加载逻辑
// }, 200);
// window.addEventListener('scroll', handleScroll);

// ====== 带立即执行选项的防抖 ======
// 支持首次立即执行，后续防抖
function debounceWithImmediate(fn, delay = 300, immediate = false) {
  let timer = null;

  return function(...args) {
    // 首次调用且immediate为true时立即执行
    const callNow = immediate && !timer;

    if (timer) clearTimeout(timer);

    timer = setTimeout(() => {
      timer = null;
      // 延迟执行模式下才在定时器中执行
      if (!immediate) fn.apply(this, args);
    }, delay);

    if (callNow) fn.apply(this, args);
  };
}

// 使用示例
// const button = document.getElementById('submit');
// button.addEventListener('click', debounceWithImmediate(function() {
//   console.log('提交表单');
// }, 1000, true)); // 首次点击立即执行，1秒内重复点击无效

// ====== 使用requestAnimationFrame做节流 ======
// 适合动画和视觉更新，与浏览器渲染帧同步
function rafThrottle(fn) {
  let rafId = null;

  return function(...args) {
    // 如果已有待处理的帧则跳过
    if (rafId) return;

    rafId = requestAnimationFrame(() => {
      fn.apply(this, args);
      rafId = null; // 执行后清除
    });
  };
}

// 使用示例
// const handleMouseMove = rafThrottle(function(event) {
//   console.log('鼠标位置:', event.clientX, event.clientY);
// });
// document.addEventListener('mousemove', handleMouseMove);

console.log('防抖vs节流：');
console.log('防抖：多次触发只执行最后一次（输入框搜索）');
console.log('节流：固定间隔执行一次（滚动事件）');
console.log('RAF节流：与浏览器渲染同步（动画）');
```

**💡 代码解释**：第4-20行：debounce实现，每次调用清除旧定时器，重新计时。第26-38行：throttle实现，记录上次执行时间，间隔不足时跳过。第43-62行：带immediate选项的防抖，首次立即执行。第67-79行：requestAnimationFrame节流，与浏览器渲染帧率同步。

**🔑 关键要点**：
- debounce：延迟执行，连续触发只执行最后一次
- throttle：固定间隔执行，限制执行频率
- RAF节流适合动画场景
- 选择合适的策略取决于业务场景

---

### 6. 函数式编程

> **级别**：高级 | **概念**：函数式编程强调纯函数、不可变数据和高阶函数，通过组合而非命令式操作来处理数据，提高代码的可预测性和可测试性。

```javascript
// ====== 纯函数（Pure Function） ======
// 纯函数：相同输入永远得到相同输出，无副作用
// 不修改外部状态，不依赖外部变量

// ✅ 纯函数：结果只依赖参数
function add(a, b) {
  return a + b;
}

// ❌ 非纯函数：依赖外部变量
let tax = 0.1;
function calculateTotal(price) {
  return price + price * tax; // 依赖外部tax变量
}

// ====== 不可变性（Immutability） ======
// 不修改原始数据，而是创建新数据
const originalArray = [1, 2, 3];

// ❌ 可变：修改原数组
// originalArray.push(4);

// ✅ 不可变：创建新数组
const newArray = [...originalArray, 4]; // 展开运算符
const newArray2 = originalArray.concat(4);
console.log(originalArray); // [1, 2, 3]（原数组不变）
console.log(newArray); // [1, 2, 3, 4]

// 对象不可变更新
const originalObj = { name: '张三', age: 25 };
const updatedObj = { ...originalObj, age: 26 }; // 创建新对象
console.log(originalObj.age); // 25（原对象不变）

// ====== 高阶函数（Higher-Order Function） ======
// 接收函数作为参数或返回函数的函数

// 函数组合：compose(f, g)(x) = f(g(x))
function compose(...fns) {
  return function(value) {
    // reduceRight从右到左执行函数
    return fns.reduceRight((acc, fn) => fn(acc), value);
  };
}

// 管道：pipe(f, g)(x) = g(f(x))，与compose方向相反
function pipe(...fns) {
  return function(value) {
    // reduce从左到右执行函数
    return fns.reduce((acc, fn) => fn(acc), value);
  };
}

// 使用示例
const double = x => x * 2;
const increment = x => x + 1;
const square = x => x * x;

// compose: 先执行increment，再double，最后square
const composed = compose(square, double, increment);
console.log(composed(3)); // square(double(increment(3))) = square(double(4)) = square(8) = 64

// pipe: 先执行increment，再double，最后square
// const piped = pipe(increment, double, square);
// console.log(piped(3)); // 结果相同但方向不同

// ====== 柯里化（Currying） ======
// 将多参数函数转换为一系列单参数函数
function curry(fn) {
  return function curried(...args) {
    // 如果参数数量足够，直接调用原函数
    if (args.length >= fn.length) {
      return fn.apply(this, args);
    }
    // 否则返回一个新函数，等待更多参数
    return function(...nextArgs) {
      return curried.apply(this, args.concat(nextArgs));
    };
  };
}

// 使用示例
const curriedAdd = curry((a, b, c) => a + b + c);
console.log(curriedAdd(1)(2)(3)); // 6
console.log(curriedAdd(1, 2)(3)); // 6
console.log(curriedAdd(1)(2, 3)); // 6

// ====== 实际应用：数据处理管道 ======
const users = [
  { name: '张三', age: 25, active: true },
  { name: '李四', age: 17, active: true },
  { name: '王五', age: 30, active: false },
  { name: '赵六', age: 22, active: true }
];

// 函数式数据处理管道
const processUsers = pipe(
  users => users.filter(u => u.active),           // 过滤活跃用户
  users => users.filter(u => u.age >= 18),        // 过滤成年人
  users => users.map(u => ({ ...u, name: u.name.toUpperCase() })), // 名字大写
  users => users.sort((a, b) => a.age - b.age)    // 按年龄排序
);

const result = processUsers(users);
console.log(result);
// [{ name: '赵六', age: 22, active: true }, { name: '张三', age: 25, active: true }]
```

**💡 代码解释**：第3-12行：纯函数vs非纯函数对比。第16-28行：不可变数据操作，展开运算符创建新对象/数组。第32-55行：compose和pipe实现函数组合。第59-74行：curry实现柯里化，将多参数函数转为单参数链。第77-101行：实际应用——数据处理管道，链式处理数组。

**🔑 关键要点**：
- 纯函数：相同输入→相同输出，无副作用
- 不可变数据：创建新数据而非修改旧数据
- compose/pipe实现函数组合
- 柯里化将多参数转为单参数函数

---

### 7. 设计模式

> **级别**：高级 | **概念**：设计模式是解决常见编程问题的可复用方案，JavaScript中常用的有观察者模式、单例模式、工厂模式和策略模式。

```javascript
// ====== 1. 观察者模式（Observer Pattern） ======
// 定义一对多依赖关系，当主题状态变化时通知所有观察者
// 典型应用：事件系统、Vue响应式、RxJS
class EventEmitter {
  constructor() {
    // 存储事件名和对应的回调函数列表
    this.events = {};
  }

  // 订阅事件：注册监听器
  on(eventName, callback) {
    if (!this.events[eventName]) {
      this.events[eventName] = []; // 初始化事件列表
    }
    this.events[eventName].push(callback);
    // 返回取消订阅的函数（方便清理）
    return () => this.off(eventName, callback);
  }

  // 发布事件：触发所有监听器
  emit(eventName, ...args) {
    const callbacks = this.events[eventName] || [];
    callbacks.forEach(cb => cb(...args)); // 依次调用回调
  }

  // 取消订阅
  off(eventName, callback) {
    if (!this.events[eventName]) return;
    this.events[eventName] = this.events[eventName].filter(cb => cb !== callback);
  }

  // 只订阅一次：触发后自动取消
  once(eventName, callback) {
    const wrapper = (...args) => {
      callback(...args);
      this.off(eventName, wrapper); // 执行后自动取消
    };
    this.on(eventName, wrapper);
  }
}

// 使用示例
const emitter = new EventEmitter();
const unsubscribe = emitter.on('login', user => {
  console.log('用户登录:', user.name);
});
emitter.emit('login', { name: '张三' }); // 输出 用户登录: 张三
// unsubscribe(); // 取消订阅

// ====== 2. 单例模式（Singleton Pattern） ======
// 确保一个类只有一个实例，并提供全局访问点
class Database {
  constructor() {
    if (Database.instance) {
      return Database.instance; // 已存在实例则返回
    }
    this.connection = '已连接'; // 初始化连接
    Database.instance = this; // 保存实例
  }

  query(sql) {
    console.log(`执行查询: ${sql}`);
  }
}

const db1 = new Database();
const db2 = new Database();
console.log(db1 === db2); // true（同一个实例）

// ====== 3. 工厂模式（Factory Pattern） ======
// 不直接new创建对象，通过工厂方法统一创建
class Button {
  constructor(type, text) {
    this.type = type;
    this.text = text;
  }

  render() {
    return `<button class="btn-${this.type}">${this.text}</button>`;
  }
}

// 工厂函数：根据类型创建不同按钮
function createButton(type, text) {
  switch (type) {
    case 'primary':
      return new Button('primary', text);
    case 'danger':
      return new Button('danger', text);
    default:
      return new Button('default', text);
  }
}

const btn = createButton('primary', '提交');
console.log(btn.render()); // <button class="btn-primary">提交</button>

// ====== 4. 策略模式（Strategy Pattern） ======
// 定义一系列算法，分别封装，使它们可以互相替换
// 典型应用：表单验证、支付方式选择
const discountStrategies = {
  // 普通用户：无折扣
  normal(price) {
    return price;
  },
  // VIP用户：8折
  vip(price) {
    return price * 0.8;
  },
  // 超级VIP：6折
  superVip(price) {
    return price * 0.6;
  }
};

function calculatePrice(userType, price) {
  // 根据用户类型选择对应的折扣策略
  const strategy = discountStrategies[userType] || discountStrategies.normal;
  return strategy(price);
}

console.log(calculatePrice('vip', 100)); // 80
console.log(calculatePrice('superVip', 100)); // 60
console.log(calculatePrice('guest', 100)); // 100（使用默认策略）

console.log('设计模式总结：');
console.log('观察者模式：一对多的事件通知系统');
console.log('单例模式：全局唯一实例');
console.log('工厂模式：统一创建对象的入口');
console.log('策略模式：可替换的算法族');
```

**💡 代码解释**：第5-40行：EventEmitter实现观察者模式，支持on/emit/off/once。第45-58行：单例模式通过静态属性保存实例。第63-88行：工厂模式根据类型创建不同对象。第93-112行：策略模式将算法封装为独立函数，运行时动态选择。

**🔑 关键要点**：
- 观察者模式：发布-订阅事件系统
- 单例模式：全局唯一实例
- 工厂模式：统一创建不同类型对象
- 策略模式：可替换的算法族

---
