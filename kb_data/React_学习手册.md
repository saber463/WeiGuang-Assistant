# ⚛️ React 编程语言学习手册

> **分类**：前端  
> **描述**：React是Facebook开源的声明式UI库，基于组件化和虚拟DOM，构建交互式用户界面  
> **生成时间**：2026-08-07  

---

## 目录

- [初级入门](#primary)（8个知识点）
- [中级进阶](#intermediate)（8个知识点）
- [高级精通](#advanced)（6个知识点）

---

## 初级入门

### 1. JSX语法

> **级别**：初级 | **概念**：JSX是JavaScript的语法扩展，看起来像HTML但本质是JavaScript，通过Babel编译为React.createElement()调用。

```jsx
// ====== JSX基础语法 ======
// JSX看起来像HTML，但运行在JavaScript中
// 每个JSX表达式必须有唯一的根元素

function App() {
  // 存储JSX到变量
  const element = (
    // 使用圆括号包裹多行JSX
    <div className="app">
      {/* JSX中的注释使用花括号+多行注释 */}
      {/* 注意：class在JSX中写作className */}
      <h1>Hello, React!</h1>
      <p>这是一个JSX示例</p>
    </div>
  );

  return element;
}

// 等价于纯JavaScript（编译后）：
// React.createElement('div', { className: 'app' },
//   React.createElement('h1', null, 'Hello, React!'),
//   React.createElement('p', null, '这是一个JSX示例')
// );

// ====== JSX嵌入JavaScript表达式 ======
function Greeting() {
  const name = '张三'; // 普通变量
  const isLoggedIn = true; // 布尔值
  const user = { firstName: '张', lastName: '三' }; // 对象

  return (
    <div>
      {/* 花括号{}中嵌入任何JavaScript表达式 */}
      <h2>欢迎，{name}！</h2>

      {/* 表达式计算 */}
      <p>年龄：{20 + 5}</p>

      {/* 三元表达式进行条件渲染 */}
      <p>{isLoggedIn ? '已登录' : '未登录'}</p>

      {/* 调用方法 */}
      <p>{user.firstName + user.lastName}</p>

      {/* 调用函数 */}
      <p>{new Date().toLocaleDateString()}</p>
    </div>
  );
}

// ====== JSX属性 ======
function StyledComponent() {
  // 样式对象：属性名用驼峰命名
  const style = {
    backgroundColor: '#3498db', // 注意：background-color → backgroundColor
    color: '#fff',
    padding: '16px',
    borderRadius: '8px',
    fontSize: '18px' // 注意：font-size → fontSize
  };

  return (
    <div
      className="container" // class → className
      style={style} // 内联样式用对象
      tabIndex={0} // tabindex → tabIndex
      htmlFor="input-id" // for → htmlFor
      data-testid="styled-div" // data-* 属性保持不变
    >
      样式化组件
    </div>
  );
}
```

**💡 代码解释**：第3-16行：JSX基础，className替代class，注释用花括号。第18-22行：JSX编译后的等价代码。第27-47行：花括号嵌入JavaScript表达式。第51-70行：JSX属性，驼峰命名，style用对象。

**🔑 关键要点**：
- JSX必须有唯一根元素
- class写作className
- 花括号嵌入JS表达式
- style属性接受对象，驼峰命名

---

### 2. 函数组件与Props

> **级别**：初级 | **概念**：React组件是返回JSX的JavaScript函数，Props是父组件传递给子组件的只读数据，实现组件间通信。

```jsx
// ====== 函数组件（Function Component） ======
// 最简单的组件：一个返回JSX的函数
function Welcome() {
  return <h1>欢迎来到React</h1>;
}

// 箭头函数写法（更简洁）
const Welcome2 = () => <h1>欢迎来到React</h1>;

// ====== Props：组件间传递数据 ======
// props是组件的参数，父组件通过属性传递，子组件接收
// 子组件：接收props参数
function UserCard(props) {
  // props是一个对象，包含所有传递的属性
  console.log(props); // { name: '张三', age: 25, role: '开发者' }

  return (
    <div className="user-card">
      <h3>{props.name}</h3> {/* 访问props.name */}
      <p>年龄：{props.age}</p>
      <p>角色：{props.role}</p>
    </div>
  );
}

// 父组件：传递props
function App() {
  return (
    <div>
      {/* 通过属性名=值的方式传递props */}
      <UserCard name="张三" age={25} role="开发者" />
      {/* 属性值用{}表示JavaScript表达式 */}
      <UserCard name="李四" age={30} role="设计师" />
    </div>
  );
}

// ====== Props解构（推荐写法） ======
// 直接在参数中解构props，更清晰
function UserCard2({ name, age, role }) {
  // 直接使用解构后的变量
  return (
    <div className="user-card">
      <h3>{name}</h3>
      <p>年龄：{age}</p>
      <p>角色：{role}</p>
    </div>
  );
}

// ====== children属性 ======
// 组件标签之间的内容会作为children传递
function Card({ title, children }) {
  // children是特殊的prop，包含标签内的内容
  return (
    <div className="card">
      <h3>{title}</h3>
      <div className="card-body">
        {children} {/* 渲染子内容 */}
      </div>
    </div>
  );
}

// 使用children
function App2() {
  return (
    <Card title="通知">
      {/* 标签内的内容作为children传递 */}
      <p>您有一条新消息</p>
      <button>查看</button>
    </Card>
  );
}

// ====== 默认Props ======
// 在参数中设置默认值
function Button({ text = '点击', disabled = false }) {
  return (
    <button disabled={disabled}>
      {text}
    </button>
  );
}
// <Button /> 使用默认值
// <Button text="提交" /> 覆盖默认值
```

**💡 代码解释**：第3-9行：函数组件两种写法。第14-28行：props接收和传递数据。第33-44行：参数解构推荐写法。第48-64行：children特殊prop。第68-78行：默认props设置。

**🔑 关键要点**：
- 组件是返回JSX的函数
- props是只读的，不可修改
- 参数解构让代码更清晰
- children是标签内的内容

---

### 3. useState状态管理

> **级别**：初级 | **概念**：useState是React最基础的Hook，为函数组件添加状态变量，调用setState函数触发重新渲染。

```jsx
import { useState } from 'react';

// ====== useState基础用法 ======
// useState(初始值) 返回 [当前值, 更新函数]
function Counter() {
  // count：当前状态值，初始为0
  // setCount：更新count的函数
  const [count, setCount] = useState(0);

  return (
    <div>
      <p>当前计数：{count}</p>
      {/* 点击按钮时更新状态 */}
      <button onClick={() => setCount(count + 1)}>
        增加
      </button>
      {/* 使用函数式更新：基于前一个状态 */}
      <button onClick={() => setCount(prev => prev - 1)}>
        减少
      </button>
      {/* 重置为初始值 */}
      <button onClick={() => setCount(0)}>
        重置
      </button>
    </div>
  );
}

// ====== 多个状态变量 ======
// 一个组件可以有多个useState
function UserForm() {
  const [username, setUsername] = useState(''); // 字符串状态
  const [age, setAge] = useState(18); // 数字状态
  const [isActive, setIsActive] = useState(true); // 布尔状态

  return (
    <form>
      <input
        type="text"
        value={username} // 受控组件：value绑定状态
        onChange={(e) => setUsername(e.target.value)} // 输入时更新状态
        placeholder="请输入用户名"
      />
      <input
        type="number"
        value={age}
        onChange={(e) => setAge(Number(e.target.value))}
        placeholder="请输入年龄"
      />
      <label>
        <input
          type="checkbox"
          checked={isActive} // checked绑定布尔状态
          onChange={(e) => setIsActive(e.target.checked)}
        />
        激活状态
      </label>
      <p>用户名：{username}，年龄：{age}，状态：{isActive ? '激活' : '禁用'}</p>
    </form>
  );
}

// ====== 对象状态 ======
// 更新对象状态时，需要展开旧对象
function TodoForm() {
  const [form, setForm] = useState({
    title: '',
    priority: 'normal',
    completed: false
  });

  const updateField = (field, value) => {
    // 使用展开运算符复制旧对象，然后更新指定字段
    setForm(prev => ({
      ...prev, // 保留其他字段
      [field]: value // 更新指定字段（计算属性名）
    }));
  };

  return (
    <div>
      <input
        value={form.title}
        onChange={(e) => updateField('title', e.target.value)}
      />
      <select
        value={form.priority}
        onChange={(e) => updateField('priority', e.target.value)}
      >
        <option value="low">低</option>
        <option value="normal">中</option>
        <option value="high">高</option>
      </select>
      <pre>{JSON.stringify(form, null, 2)}</pre>
    </div>
  );
}

// ====== 状态更新是异步的 ======
// 多次setState在事件处理中会批量处理
function AsyncExample() {
  const [count, setCount] = useState(0);

  const handleClick = () => {
    // 在同一个事件处理中，React会批量处理状态更新
    setCount(prev => prev + 1);
    setCount(prev => prev + 1);
    setCount(prev => prev + 1);
    // 使用函数式更新，每次基于最新值，最终count + 3
  };

  return <button onClick={handleClick}>+3: {count}</button>;
}
```

**💡 代码解释**：第5-23行：useState基础，count状态和setCount更新函数。第28-59行：多个状态变量，受控组件模式。第63-97行：对象状态更新需展开旧对象。第101-111行：状态更新是异步的，函数式更新基于最新值。

**🔑 关键要点**：
- useState返回[值, 更新函数]
- 状态更新是异步的
- 函数式更新基于前一个状态
- 对象状态需展开旧对象

---

### 4. useEffect副作用

> **级别**：初级 | **概念**：useEffect在组件渲染后执行副作用操作（数据获取、订阅、DOM操作），通过依赖数组控制执行时机。

```jsx
import { useState, useEffect } from 'react';

// ====== useEffect基础：每次渲染后执行 ======
function Timer() {
  const [seconds, setSeconds] = useState(0);

  useEffect(() => {
    // 副作用：设置定时器
    const interval = setInterval(() => {
      setSeconds(prev => prev + 1); // 每秒递增
    }, 1000);

    // 清理函数：组件卸载时清除定时器
    return () => {
      clearInterval(interval);
      console.log('定时器已清除');
    };
  }, []); // 空依赖数组：只在组件挂载时执行一次

  return <div>已运行 {seconds} 秒</div>;
}

// ====== 依赖数组的三种情况 ======
function DataFetcher({ userId }) {
  const [user, setUser] = useState(null);

  // 情况1：无依赖数组 → 每次渲染都执行
  // useEffect(() => {
  //   console.log('每次渲染都执行');
  // });

  // 情况2：空依赖数组 → 只在挂载时执行一次
  // useEffect(() => {
  //   console.log('只在挂载时执行');
  // }, []);

  // 情况3：有依赖 → 依赖变化时执行
  useEffect(() => {
    console.log('userId变化，重新获取数据');
    // 模拟API请求
    const fetchUser = async () => {
      const response = await fetch(`/api/users/${userId}`);
      const data = await response.json();
      setUser(data);
    };
    fetchUser();
  }, [userId]); // userId变化时重新执行

  return (
    <div>
      {user ? <p>{user.name}</p> : <p>加载中...</p>}
    </div>
  );
}

// ====== 清理函数的作用 ======
// 在effect重新执行前或组件卸载时运行
function SearchInput() {
  const [query, setQuery] = useState('');

  useEffect(() => {
    // 防抖：延迟发送请求
    const timer = setTimeout(() => {
      if (query) {
        console.log('搜索:', query);
      }
    }, 500);

    // 清理函数：在下一次effect执行前清除上一次的定时器
    return () => {
      clearTimeout(timer);
    };
  }, [query]); // query变化时重新执行

  return (
    <input
      value={query}
      onChange={(e) => setQuery(e.target.value)}
      placeholder="搜索..."
    />
  );
}

// ====== DOM操作在useEffect中进行 ======
function AutoFocus() {
  const inputRef = useRef(null);

  useEffect(() => {
    // DOM操作必须在useEffect中，因为此时DOM已经渲染
    inputRef.current?.focus(); // 自动聚焦
  }, []);

  return <input ref={inputRef} placeholder="自动聚焦" />;
}

// ====== useEffect注意事项 ======
console.log('useEffect要点：');
console.log('1. 副作用在渲染后执行');
console.log('2. 清理函数在组件卸载或下次effect前执行');
console.log('3. 依赖数组中的值必须完整');
console.log('4. 不要在effect中直接修改DOM（使用ref）');
```

**💡 代码解释**：第5-18行：useEffect基础，设置定时器，清理函数清除定时器。第23-50行：依赖数组三种情况。第54-74行：清理函数防止内存泄漏。第78-87行：DOM操作需在useEffect中。

**🔑 关键要点**：
- 副作用在渲染后执行
- 空依赖数组只在挂载时执行
- 清理函数防止内存泄漏
- 依赖数组要完整列出所有依赖

---

### 5. 事件处理

> **级别**：初级 | **概念**：React事件处理使用驼峰命名，通过onClick、onChange等属性绑定事件处理函数，事件对象是SyntheticEvent（合成事件）。

```jsx
import { useState } from 'react';

// ====== 事件处理基础 ======
function EventDemo() {
  // 定义事件处理函数
  const handleClick = () => {
    console.log('按钮被点击了');
  };

  const handleMouseEnter = () => {
    console.log('鼠标进入');
  };

  return (
    <div>
      {/* 事件名用驼峰命名：onClick而不是onclick */}
      <button onClick={handleClick}>
        点击我
      </button>

      {/* 鼠标事件 */}
      <div
        onMouseEnter={handleMouseEnter}
        onMouseLeave={() => console.log('鼠标离开')}
        style={{ width: 200, height: 100, background: '#eee' }}
      >
        鼠标放上来
      </div>
    </div>
  );
}

// ====== 事件对象（SyntheticEvent） ======
function FormEvents() {
  const [text, setText] = useState('');

  // 输入事件：获取输入值
  const handleChange = (event) => {
    // event.target是触发事件的DOM元素
    // event.target.value获取输入框的值
    setText(event.target.value);
  };

  // 表单提交事件
  const handleSubmit = (event) => {
    event.preventDefault(); // 阻止默认提交行为（页面刷新）
    console.log('提交:', text);
  };

  // 键盘事件
  const handleKeyDown = (event) => {
    if (event.key === 'Enter') {
      // 按回车键时触发
      console.log('回车键被按下');
    }
    if (event.ctrlKey && event.key === 's') {
      // Ctrl+S 保存
      event.preventDefault(); // 阻止浏览器默认保存行为
      console.log('Ctrl+S 保存');
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        value={text}
        onChange={handleChange} // 输入变化事件
        onKeyDown={handleKeyDown} // 键盘按下事件
        onFocus={() => console.log('获得焦点')} // 获得焦点
        onBlur={() => console.log('失去焦点')} // 失去焦点
        placeholder="输入内容"
      />
      <button type="submit">提交</button>
    </form>
  );
}

// ====== 传递参数给事件处理函数 ======
function ParameterExample() {
  const items = ['项目A', '项目B', '项目C'];

  const handleItemClick = (item, index) => {
    console.log(`点击了第${index}项: ${item}`);
  };

  const handleDelete = (item, event) => {
    event.stopPropagation(); // 阻止事件冒泡
    console.log('删除:', item);
  };

  return (
    <ul>
      {items.map((item, index) => (
        <li key={index}>
          {/* 使用箭头函数传递自定义参数 */}
          <span onClick={() => handleItemClick(item, index)}>
            {item}
          </span>
          {/* event作为最后一个参数传递 */}
          <button onClick={(e) => handleDelete(item, e)}>
            删除
          </button>
        </li>
      ))}
    </ul>
  );
}

// ====== 事件处理最佳实践 ======
// 1. 使用箭头函数避免this绑定问题
// 2. 避免在JSX中写复杂逻辑，提取为函数
// 3. 使用event.preventDefault()阻止默认行为
// 4. 使用event.stopPropagation()阻止冒泡
```

**💡 代码解释**：第5-28行：事件处理基础，驼峰命名。第32-72行：SyntheticEvent对象、preventDefault、键盘事件。第76-104行：箭头函数传递自定义参数。

**🔑 关键要点**：
- 事件名驼峰命名：onClick
- event.preventDefault()阻止默认行为
- 箭头函数传递自定义参数
- 合成事件自动处理跨浏览器兼容

---

### 6. 条件渲染

> **级别**：初级 | **概念**：React支持多种条件渲染方式：三元运算符、&&短路运算符、if-else语句和条件变量，灵活控制组件的显示与隐藏。

```jsx
import { useState } from 'react';

// ====== 方式1：三元运算符 ======
// 最常用的条件渲染方式
function StatusBadge({ status }) {
  return (
    <span>
      状态：
      {status === 'online'
        ? <span style={{ color: 'green' }}>在线</span>
        : <span style={{ color: 'red' }}>离线</span>
      }
    </span>
  );
}

// ====== 方式2：&&短路运算符 ======
// 当条件为true时渲染，false时不渲染
function Notification({ message }) {
  return (
    <div>
      {/* 有消息时才显示通知栏 */}
      {message && (
        <div className="notification">
          {message}
        </div>
      )}
      {/* 注意：message为0或''时也会被渲染 */}
    </div>
  );
}

// ====== 方式3：if-else提前返回 ======
// 适合组件级别的条件渲染
function UserProfile({ user, loading, error }) {
  // 加载中状态
  if (loading) {
    return <div className="loading">加载中...</div>;
  }

  // 错误状态
  if (error) {
    return <div className="error">出错了：{error}</div>;
  }

  // 空数据状态
  if (!user) {
    return <div className="empty">暂无用户数据</div>;
  }

  // 正常渲染
  return (
    <div className="profile">
      <h3>{user.name}</h3>
      <p>{user.email}</p>
    </div>
  );
}

// ====== 方式4：条件变量 ======
// 将条件渲染结果赋值给变量
function Dashboard({ role }) {
  // 根据角色确定要渲染的内容
  let adminPanel = null;
  if (role === 'admin') {
    adminPanel = (
      <div className="admin-panel">
        <h3>管理员面板</h3>
        <button>管理用户</button>
        <button>系统设置</button>
      </div>
    );
  }

  return (
    <div>
      <h2>控制台</h2>
      {adminPanel} {/* 渲染管理员面板（或null） */}
      <div>普通内容</div>
    </div>
  );
}

// ====== 方式5：条件渲染 + 组件切换 ======
function TabPanel({ activeTab }) {
  // 对象映射：根据key渲染不同组件
  const panels = {
    home: <div>首页内容</div>,
    profile: <div>个人资料</div>,
    settings: <div>设置页面</div>
  };

  return (
    <div>
      <nav>
        <button>首页</button>
        <button>资料</button>
        <button>设置</button>
      </nav>
      <div className="tab-content">
        {panels[activeTab] || panels.home} {/* 渲染对应面板 */}
      </div>
    </div>
  );
}
```

**💡 代码解释**：第5-16行：三元运算符条件渲染。第20-33行：&&短路运算符。第37-60行：if-else提前返回模式。第64-88行：条件变量赋值。第92-110行：对象映射切换组件。

**🔑 关键要点**：
- 三元运算符：条件 ? A : B
- &&短路：条件 && 组件
- if-else适合组件级条件
- 注意假值（0/''）不会渲染

---

### 7. 列表渲染

> **级别**：初级 | **概念**：React使用数组的map()方法渲染列表，每个列表项需要唯一的key属性帮助React识别变化，优化渲染性能。

```jsx
import { useState } from 'react';

// ====== 基础列表渲染 ======
// 使用map()将数组每个元素映射为JSX
function FruitList() {
  const fruits = ['苹果', '香蕉', '橙子', '葡萄', '西瓜'];

  return (
    <ul>
      {fruits.map((fruit, index) => (
        // key属性：每个列表项需要唯一标识
        // 使用index作为key仅在没有稳定ID时的备选方案
        <li key={fruit}>{fruit}</li>
      ))}
    </ul>
  );
}

// ====== 对象数组渲染 ======
// 渲染复杂数据结构
function UserList() {
  const users = [
    { id: 1, name: '张三', age: 25, active: true },
    { id: 2, name: '李四', age: 30, active: false },
    { id: 3, name: '王五', age: 28, active: true }
  ];

  return (
    <div>
      {users.map(user => (
        // 使用稳定的id作为key（推荐）
        <div key={user.id} className="user-item">
          <h3>{user.name}</h3>
          <p>年龄：{user.age}</p>
          <span style={{ color: user.active ? 'green' : 'gray' }}>
            {user.active ? '在线' : '离线'}
          </span>
        </div>
      ))}
    </div>
  );
}

// ====== key的重要性 ======
// key帮助React识别哪些元素改变了、添加了或删除了
// 没有key或key不稳定的后果：
// 1. 列表重排时性能下降
// 2. 组件状态可能错乱
// 3. 输入框内容可能错位

// ❌ 错误：使用index作为key（当列表会重排时）
// {items.map((item, index) => <li key={index}>{item}</li>)}

// ✅ 正确：使用稳定的唯一标识
// {items.map(item => <li key={item.id}>{item.name}</li>)}

// ====== 动态列表：添加和删除 ======
function TodoList() {
  const [todos, setTodos] = useState([
    { id: 1, text: '学习React', completed: false },
    { id: 2, text: '写代码', completed: false }
  ]);
  const [input, setInput] = useState('');

  // 添加新任务
  const addTodo = () => {
    if (input.trim()) {
      const newTodo = {
        id: Date.now(), // 使用时间戳作为唯一ID
        text: input,
        completed: false
      };
      setTodos(prev => [...prev, newTodo]); // 添加到数组末尾
      setInput(''); // 清空输入
    }
  };

  // 切换完成状态
  const toggleTodo = (id) => {
    setTodos(prev =>
      prev.map(todo =>
        todo.id === id
          ? { ...todo, completed: !todo.completed } // 切换状态
          : todo
      )
    );
  };

  // 删除任务
  const deleteTodo = (id) => {
    setTodos(prev => prev.filter(todo => todo.id !== id));
  };

  return (
    <div>
      <div>
        <input
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && addTodo()}
          placeholder="添加任务"
        />
        <button onClick={addTodo}>添加</button>
      </div>
      <ul>
        {todos.map(todo => (
          <li key={todo.id}>
            <span
              style={{
                textDecoration: todo.completed ? 'line-through' : 'none',
                cursor: 'pointer'
              }}
              onClick={() => toggleTodo(todo.id)}
            >
              {todo.text}
            </span>
            <button onClick={() => deleteTodo(todo.id)}>删除</button>
          </li>
        ))}
      </ul>
    </div>
  );
}
```

**💡 代码解释**：第5-15行：map()基础列表渲染。第19-39行：对象数组渲染，使用id作为key。第51-54行：key使用index vs 稳定id对比。第58-112行：动态列表增删改完整示例。

**🔑 关键要点**：
- map()遍历数组生成JSX
- key必须是唯一且稳定的
- 避免使用index作为key
- 列表更新用不可变方式

---

### 8. 受控组件与非受控组件

> **级别**：初级 | **概念**：受控组件通过React state管理表单数据，非受控组件通过DOM自身管理数据（使用ref获取），两者各有适用场景。

```jsx
import { useState, useRef } from 'react';

// ====== 受控组件（Controlled Component） ======
// 表单数据由React state控制，React是"唯一数据源"
function ControlledForm() {
  const [name, setName] = useState(''); // state管理输入值
  const [email, setEmail] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault(); // 阻止默认提交
    // 直接从state获取表单数据
    console.log('提交:', { name, email });
  };

  return (
    <form onSubmit={handleSubmit}>
      {/* value绑定state，onChange更新state */}
      <input
        value={name}
        onChange={(e) => setName(e.target.value)}
        placeholder="姓名"
      />
      <input
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        placeholder="邮箱"
      />
      {/* 可以实时验证 */}
      {name.length < 2 && <span>姓名至少2个字符</span>}
      <button type="submit">提交</button>
    </form>
  );
}

// ====== 非受控组件（Uncontrolled Component） ======
// 表单数据由DOM自身管理，通过ref获取值
function UncontrolledForm() {
  // 创建ref引用DOM元素
  const nameRef = useRef(null);
  const emailRef = useRef(null);

  const handleSubmit = (e) => {
    e.preventDefault();
    // 通过ref.current.value获取DOM值
    console.log('提交:', {
      name: nameRef.current.value,
      email: emailRef.current.value
    });
  };

  return (
    <form onSubmit={handleSubmit}>
      {/* defaultValue设置初始值，ref绑定DOM */}
      <input ref={nameRef} defaultValue="" placeholder="姓名" />
      <input ref={emailRef} defaultValue="" placeholder="邮箱" />
      <button type="submit">提交</button>
    </form>
  );
}

// ====== 受控 vs 非受控 对比 ======
// 受控组件：
// - 实时验证和格式化
// - 动态禁用提交按钮
// - 强制输入格式
// - 适合复杂表单

// 非受控组件：
// - 代码更简洁
// - 适合简单表单
// - 与第三方非React库集成
// - 文件上传必须用非受控（type="file"）
```

**💡 代码解释**：第5-33行：受控组件用useState管理表单值，value+onChange实现双向绑定。第37-57行：非受控组件用useRef获取DOM值，defaultValue设置初始值。第61-71行：两者对比，受控适合复杂表单，非受控适合简单场景。

**🔑 关键要点**：
- 受控：state管理表单数据
- 非受控：ref获取DOM值
- 文件上传必须用非受控
- 受控组件支持实时验证

---

## 中级进阶

### 1. useContext上下文

> **级别**：中级 | **概念**：useContext让组件树中任意层级的组件都能访问共享数据，无需逐层传递props，是解决"prop drilling"问题的方案。

```jsx
import { createContext, useContext, useState } from 'react';

// ====== 创建Context ======
// 1. 创建Context对象，可传入默认值
const ThemeContext = createContext('light');
// 默认值在组件没有匹配的Provider时使用

// 2. 创建Provider组件（封装Context.Provider）
function ThemeProvider({ children }) {
  const [theme, setTheme] = useState('light');

  // 切换主题的函数
  const toggleTheme = () => {
    setTheme(prev => prev === 'light' ? 'dark' : 'light');
  };

  // 通过value属性传递共享数据
  return (
    <ThemeContext.Provider value={{ theme, toggleTheme }}>
      {children}
    </ThemeContext.Provider>
  );
}

// ====== 消费Context ======
// 3. 在子组件中使用useContext获取数据
function ThemedButton() {
  // useContext接收Context对象，返回当前value
  const { theme, toggleTheme } = useContext(ThemeContext);

  return (
    <button
      onClick={toggleTheme}
      style={{
        backgroundColor: theme === 'light' ? '#fff' : '#333',
        color: theme === 'light' ? '#333' : '#fff',
        padding: '10px 20px',
        border: '1px solid #ccc',
        borderRadius: '4px'
      }}
    >
      当前主题: {theme}（点击切换）
    </button>
  );
}

// 深层嵌套的子组件也能访问Context
function DeepChild() {
  const { theme } = useContext(ThemeContext);
  return <p>深层组件知道主题是: {theme}</p>;
}

// ====== 完整应用 ======
function App() {
  return (
    // Provider包裹需要共享数据的组件树
    <ThemeProvider>
      <div>
        <h1>Context示例</h1>
        <ThemedButton />
        <div>
          <DeepChild />
        </div>
      </div>
    </ThemeProvider>
  );
}

// ====== 多个Context ======
// 可以同时使用多个Context，互不影响
const UserContext = createContext(null);
const LanguageContext = createContext('zh');

function MultiContextComponent() {
  // 同时消费多个Context
  const user = useContext(UserContext);
  const lang = useContext(LanguageContext);

  return (
    <div>
      <p>用户：{user?.name}</p>
      <p>语言：{lang}</p>
    </div>
  );
}

// ====== Context注意事项 ======
console.log('Context使用要点：');
console.log('1. Context值变化时，所有消费组件都会重新渲染');
console.log('2. 不要滥用Context，适合全局主题、用户信息等');
console.log('3. 频繁变化的数据不适合用Context');
console.log('4. 可以拆分多个Context减少不必要的渲染');
```

**💡 代码解释**：第5-7行：创建Context。第10-22行：ThemeProvider封装Provider逻辑。第26-45行：useContext消费数据。第52-64行：App使用Provider包裹。第69-81行：多个Context示例。

**🔑 关键要点**：
- createContext创建上下文
- Provider的value传递数据
- useContext消费数据
- Context值变化触发重渲染

---

### 2. useReducer状态管理

> **级别**：中级 | **概念**：useReducer是useState的替代方案，适合复杂状态逻辑，通过dispatch(action)触发状态变更，类似Redux的reducer模式。

```jsx
import { useReducer } from 'react';

// ====== Reducer函数 ======
// reducer接收当前状态和action，返回新状态
// 语法：(state, action) => newState
const initialState = { count: 0, step: 1 };

function counterReducer(state, action) {
  // 根据action.type决定如何更新状态
  switch (action.type) {
    case 'increment':
      // 返回新状态对象，不修改原状态
      return { ...state, count: state.count + state.step };
    case 'decrement':
      return { ...state, count: state.count - state.step };
    case 'reset':
      return initialState; // 重置为初始状态
    case 'setStep':
      // action.payload携带额外数据
      return { ...state, step: action.payload };
    default:
      // 未知action，返回原状态
      return state;
  }
}

// ====== 使用useReducer ======
function Counter() {
  // useReducer返回[state, dispatch]
  const [state, dispatch] = useReducer(counterReducer, initialState);

  return (
    <div>
      <h2>计数：{state.count}</h2>
      <p>步长：{state.step}</p>

      {/* dispatch发送action对象 */}
      <button onClick={() => dispatch({ type: 'increment' })}>
        +{state.step}
      </button>
      <button onClick={() => dispatch({ type: 'decrement' })}>
        -{state.step}
      </button>
      <button onClick={() => dispatch({ type: 'reset' })}>
        重置
      </button>

      {/* 带payload的action */}
      <select
        value={state.step}
        onChange={(e) =>
          dispatch({ type: 'setStep', payload: Number(e.target.value) })
        }
      >
        <option value={1}>步长 1</option>
        <option value={5}>步长 5</option>
        <option value={10}>步长 10</option>
      </select>
    </div>
  );
}

// ====== 复杂状态：TodoList ======
// useReducer适合管理复杂的状态逻辑
const todoReducer = (state, action) => {
  switch (action.type) {
    case 'add':
      return [
        ...state,
        {
          id: Date.now(),
          text: action.payload,
          completed: false
        }
      ];
    case 'toggle':
      return state.map(todo =>
        todo.id === action.payload
          ? { ...todo, completed: !todo.completed }
          : todo
      );
    case 'delete':
      return state.filter(todo => todo.id !== action.payload);
    case 'clearCompleted':
      return state.filter(todo => !todo.completed);
    default:
      return state;
  }
};

function TodoApp() {
  const [todos, dispatch] = useReducer(todoReducer, []);
  const [text, setText] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    if (text.trim()) {
      dispatch({ type: 'add', payload: text });
      setText('');
    }
  };

  return (
    <div>
      <form onSubmit={handleSubmit}>
        <input value={text} onChange={e => setText(e.target.value)} />
        <button type="submit">添加</button>
      </form>

      <ul>
        {todos.map(todo => (
          <li key={todo.id}>
            <span
              onClick={() => dispatch({ type: 'toggle', payload: todo.id })}
              style={{
                textDecoration: todo.completed ? 'line-through' : 'none',
                cursor: 'pointer'
              }}
            >
              {todo.text}
            </span>
            <button
              onClick={() => dispatch({ type: 'delete', payload: todo.id })}
            >
              删除
            </button>
          </li>
        ))}
      </ul>

      {todos.some(t => t.completed) && (
        <button onClick={() => dispatch({ type: 'clearCompleted' })}>
          清除已完成
        </button>
      )}
    </div>
  );
}
```

**💡 代码解释**：第5-22行：reducer函数定义状态转换逻辑。第27-61行：useReducer基本使用，dispatch发送action。第65-123行：TodoList完整示例，展示复杂状态管理。

**🔑 关键要点**：
- reducer: (state, action) => newState
- dispatch发送action对象
- action通常有type和payload
- 适合复杂状态逻辑

---

### 3. useRef与DOM引用

> **级别**：中级 | **概念**：useRef创建可变的引用对象，常用于访问DOM元素、存储不触发重渲染的变量、以及保存定时器ID等。

```jsx
import { useRef, useState, useEffect } from 'react';

// ====== 1. 访问DOM元素 ======
function AutoFocusInput() {
  // 创建ref对象，初始值为null
  const inputRef = useRef(null);

  useEffect(() => {
    // 组件挂载后，inputRef.current指向真实的DOM元素
    inputRef.current?.focus(); // 自动聚焦
  }, []);

  const handleClick = () => {
    // 通过ref操作DOM
    inputRef.current?.select(); // 选中文本
  };

  return (
    <div>
      <input ref={inputRef} placeholder="自动聚焦" />
      <button onClick={handleClick}>选中文本</button>
    </div>
  );
}

// ====== 2. 存储不触发重渲染的值 ======
// 与useState不同，修改ref.current不会触发重渲染
function RenderCounter() {
  const [count, setCount] = useState(0);
  // 用ref记录渲染次数
  const renderCount = useRef(0);

  // 每次渲染时递增
  renderCount.current += 1;

  return (
    <div>
      <p>计数：{count}</p>
      <p>渲染次数：{renderCount.current}</p>
      <button onClick={() => setCount(prev => prev + 1)}>
        增加
      </button>
    </div>
  );
}

// ====== 3. 保存定时器ID ======
function Timer() {
  const [seconds, setSeconds] = useState(0);
  // 用ref保存定时器ID，不触发重渲染
  const intervalRef = useRef(null);

  const start = () => {
    if (intervalRef.current) return; // 防止重复启动
    intervalRef.current = setInterval(() => {
      setSeconds(prev => prev + 1);
    }, 1000);
  };

  const stop = () => {
    clearInterval(intervalRef.current);
    intervalRef.current = null;
  };

  const reset = () => {
    stop();
    setSeconds(0);
  };

  // 组件卸载时清理
  useEffect(() => {
    return () => clearInterval(intervalRef.current);
  }, []);

  return (
    <div>
      <p>{seconds} 秒</p>
      <button onClick={start}>开始</button>
      <button onClick={stop}>停止</button>
      <button onClick={reset}>重置</button>
    </div>
  );
}

// ====== 4. 获取上一个状态值 ======
function usePrevious(value) {
  const ref = useRef();
  useEffect(() => {
    ref.current = value; // 在渲染后更新ref
  }, [value]);
  return ref.current; // 返回的是上一次的值
}

function PreviousValueDemo() {
  const [count, setCount] = useState(0);
  const prevCount = usePrevious(count);

  return (
    <div>
      <p>当前：{count}，上一次：{prevCount ?? '无'}</p>
      <button onClick={() => setCount(prev => prev + 1)}>增加</button>
    </div>
  );
}

// ====== ref vs state 对比 ======
console.log('ref vs state：');
console.log('ref：修改不触发重渲染，适合DOM引用和可变值');
console.log('state：修改触发重渲染，适合UI数据');
console.log('ref.current可以直接修改，state必须用setState');
```

**💡 代码解释**：第5-21行：ref访问DOM元素。第25-40行：ref存储不触发重渲染的值。第44-78行：ref保存定时器ID。第82-96行：usePrevious自定义Hook。

**🔑 关键要点**：
- ref.current指向DOM或任意值
- 修改ref不触发重渲染
- 适合保存DOM引用、定时器ID
- usePrevious获取上一次状态

---

### 4. 自定义Hook

> **级别**：中级 | **概念**：自定义Hook是复用状态逻辑的机制，将组件逻辑提取到可复用的函数中，命名以use开头，内部可以使用其他Hook。

```jsx
import { useState, useEffect, useCallback } from 'react';

// ====== 自定义Hook 1：useLocalStorage ======
// 将状态持久化到localStorage
function useLocalStorage(key, initialValue) {
  // 懒初始化：从localStorage读取或使用默认值
  const [storedValue, setStoredValue] = useState(() => {
    try {
      const item = window.localStorage.getItem(key);
      return item ? JSON.parse(item) : initialValue;
    } catch (error) {
      console.error('读取localStorage失败:', error);
      return initialValue;
    }
  });

  // 值变化时同步到localStorage
  useEffect(() => {
    try {
      window.localStorage.setItem(key, JSON.stringify(storedValue));
    } catch (error) {
      console.error('写入localStorage失败:', error);
    }
  }, [key, storedValue]);

  return [storedValue, setStoredValue];
}

// 使用示例
function Settings() {
  const [theme, setTheme] = useLocalStorage('app-theme', 'light');
  const [fontSize, setFontSize] = useLocalStorage('font-size', 16);

  return (
    <div>
      <p>主题：{theme}</p>
      <button onClick={() => setTheme(theme === 'light' ? 'dark' : 'light')}>
        切换主题
      </button>
      <p>字号：{fontSize}px</p>
      <button onClick={() => setFontSize(prev => prev + 1)}>增大字号</button>
    </div>
  );
}

// ====== 自定义Hook 2：useFetch ======
// 封装数据获取逻辑
function useFetch(url) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    // 标记是否取消请求
    let cancelled = false;

    const fetchData = async () => {
      setLoading(true);
      setError(null);
      try {
        const response = await fetch(url);
        if (!response.ok) {
          throw new Error(`HTTP ${response.status}`);
        }
        const result = await response.json();
        if (!cancelled) {
          setData(result);
        }
      } catch (err) {
        if (!cancelled) {
          setError(err.message);
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    fetchData();

    // 清理函数：组件卸载时取消
    return () => { cancelled = true; };
  }, [url]);

  return { data, loading, error };
}

// 使用示例
function UserDisplay({ userId }) {
  const { data: user, loading, error } = useFetch(`/api/users/${userId}`);

  if (loading) return <div>加载中...</div>;
  if (error) return <div>错误：{error}</div>;
  if (!user) return <div>无数据</div>;

  return <div>{user.name}</div>;
}

// ====== 自定义Hook 3：useDebounce ======
// 防抖Hook
function useDebounce(value, delay = 300) {
  const [debouncedValue, setDebouncedValue] = useState(value);

  useEffect(() => {
    // 设置定时器
    const timer = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    // 清理函数：清除上一次的定时器
    return () => clearTimeout(timer);
  }, [value, delay]);

  return debouncedValue;
}

// 使用示例
function SearchBox() {
  const [query, setQuery] = useState('');
  const debouncedQuery = useDebounce(query, 500);

  useEffect(() => {
    if (debouncedQuery) {
      console.log('搜索:', debouncedQuery);
      // 发送API请求
    }
  }, [debouncedQuery]);

  return (
    <input
      value={query}
      onChange={(e) => setQuery(e.target.value)}
      placeholder="搜索..."
    />
  );
}

// ====== 自定义Hook规则 ======
console.log('自定义Hook规则：');
console.log('1. 命名必须以use开头');
console.log('2. 内部可以使用其他Hook');
console.log('3. 每次调用都有独立的状态');
console.log('4. 提取可复用的逻辑，不包含UI');
```

**💡 代码解释**：第5-30行：useLocalStorage持久化状态。第34-102行：useFetch封装数据获取。第107-140行：useDebounce防抖Hook。

**🔑 关键要点**：
- 自定义Hook以use开头
- 复用状态逻辑，不共享状态
- 内部可使用其他Hook
- 每次调用有独立状态

---

### 5. React Router路由

> **级别**：中级 | **概念**：React Router是React的事实标准路由库，通过URL路径匹配渲染对应组件，支持嵌套路由、动态参数和导航守卫。

```jsx
// ====== React Router v6 基础配置 ======
// 需要安装：npm install react-router-dom
import { BrowserRouter, Routes, Route, Link, NavLink, useParams, useNavigate } from 'react-router-dom';

// 页面组件
function Home() { return <h2>首页</h2>; }
function About() { return <h2>关于我们</h2>; }

// 动态路由组件：通过useParams获取URL参数
function UserProfile() {
  // useParams返回URL中的动态参数
  const { userId } = useParams();
  return <h2>用户 {userId} 的个人资料</h2>;
}

// 404页面
function NotFound() { return <h2>404 - 页面未找到</h2>; }

// ====== 路由配置 ======
function App() {
  return (
    <BrowserRouter>
      <div>
        {/* 导航栏 */}
        <nav>
          {/* Link组件：声明式导航，不刷新页面 */}
          <Link to="/">首页</Link>
          {/* NavLink：激活时自动添加active类名 */}
          <NavLink
            to="/about"
            style={({ isActive }) => ({
              fontWeight: isActive ? 'bold' : 'normal',
              color: isActive ? '#3498db' : '#333'
            })}
          >
            关于
          </NavLink>
          <Link to="/users/123">用户123</Link>
        </nav>

        {/* 路由渲染区域 */}
        <Routes>
          {/* Route：path匹配URL，element是要渲染的组件 */}
          <Route path="/" element={<Home />} />
          <Route path="/about" element={<About />} />
          {/* 动态路由：:userId是参数占位符 */}
          <Route path="/users/:userId" element={<UserProfile />} />
          {/* * 匹配所有未定义的路径 */}
          <Route path="*" element={<NotFound />} />
        </Routes>
      </div>
    </BrowserRouter>
  );
}

// ====== 编程式导航：useNavigate ======
function LoginButton() {
  // useNavigate返回导航函数
  const navigate = useNavigate();

  const handleLogin = () => {
    // 模拟登录
    console.log('登录成功');
    // 跳转到首页
    navigate('/');
    // navigate(-1) 返回上一页
    // navigate('/dashboard', { replace: true }) 替换当前历史记录
  };

  return <button onClick={handleLogin}>登录并跳转</button>;
}

// ====== 嵌套路由 ======
function Dashboard() {
  return (
    <div>
      <h2>仪表盘</h2>
      {/* Outlet渲染子路由 */}
      <nav>
        <Link to="stats">统计</Link>
        <Link to="settings">设置</Link>
      </nav>
      <Outlet /> {/* 子路由内容在此渲染 */}
    </div>
  );
}

// 嵌套路由配置
// <Route path="/dashboard" element={<Dashboard />}>
//   <Route path="stats" element={<Stats />} />
//   <Route path="settings" element={<Settings />} />
// </Route>

// ====== 路由守卫（Protected Route） ======
function ProtectedRoute({ children }) {
  const isAuthenticated = true; // 从Context或状态管理获取
  const navigate = useNavigate();

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login', { replace: true });
    }
  }, [isAuthenticated, navigate]);

  return isAuthenticated ? children : null;
}

// 使用
// <Route path="/admin" element={
//   <ProtectedRoute>
//     <AdminPanel />
//   </ProtectedRoute>
// } />
```

**💡 代码解释**：第5-13行：页面组件。第17-50行：Routes和Route配置路由。第54-67行：useNavigate编程式导航。第71-86行：嵌套路由和Outlet。第90-108行：ProtectedRoute路由守卫。

**🔑 关键要点**：
- Routes包裹Route定义路由
- useParams获取动态参数
- useNavigate编程式导航
- Outlet渲染嵌套子路由

---

### 6. 性能优化：useMemo与useCallback

> **级别**：中级 | **概念**：useMemo缓存计算结果，useCallback缓存函数引用，两者配合React.memo避免不必要的子组件重渲染，提升应用性能。

```jsx
import { useState, useMemo, useCallback, memo } from 'react';

// ====== useMemo：缓存计算结果 ======
// 避免每次渲染都重新计算昂贵的操作
function ExpensiveCalculation({ items }) {
  const [filter, setFilter] = useState('');

  // 过滤后的列表：只有items或filter变化时才重新计算
  const filteredItems = useMemo(() => {
    console.log('正在过滤...（仅在依赖变化时执行）');
    return items.filter(item =>
      item.name.toLowerCase().includes(filter.toLowerCase())
    );
  }, [items, filter]); // 依赖数组

  // 统计数据：只有items变化时才重新计算
  const stats = useMemo(() => {
    console.log('计算统计数据...');
    return {
      total: items.length,
      activeCount: items.filter(i => i.active).length,
      averageScore: items.reduce((sum, i) => sum + i.score, 0) / items.length
    };
  }, [items]);

  return (
    <div>
      <input
        value={filter}
        onChange={e => setFilter(e.target.value)}
        placeholder="过滤..."
      />
      <p>总计：{stats.total}，活跃：{stats.activeCount}，平均分：{stats.averageScore.toFixed(1)}</p>
      <ul>
        {filteredItems.map(item => (
          <li key={item.id}>{item.name} - {item.score}分</li>
        ))}
      </ul>
    </div>
  );
}

// ====== useCallback：缓存函数引用 ======
// 配合React.memo避免子组件不必要的重渲染

// 使用memo包裹子组件：只有props变化时才重渲染
const Button = memo(function Button({ onClick, children }) {
  console.log('Button 渲染:', children);
  return <button onClick={onClick}>{children}</button>;
});

function Parent() {
  const [count, setCount] = useState(0);
  const [text, setText] = useState('');

  // ❌ 不用useCallback：每次渲染都创建新函数
  // const handleClick = () => setCount(c => c + 1);

  // ✅ 使用useCallback：函数引用稳定
  const handleClick = useCallback(() => {
    setCount(c => c + 1);
  }, []); // 空依赖，函数引用永不变化

  const handleReset = useCallback(() => {
    setCount(0);
  }, []);

  return (
    <div>
      <p>计数：{count}</p>
      {/* Button是memo组件，只有onClick变化时才重渲染 */}
      <Button onClick={handleClick}>增加</Button>
      <Button onClick={handleReset}>重置</Button>
      <input
        value={text}
        onChange={e => setText(e.target.value)}
        placeholder="输入文字（不会触发Button重渲染）"
      />
    </div>
  );
}

// ====== useMemo + useCallback + memo 组合使用 ======
// 完整的性能优化模式
const TodoItem = memo(function TodoItem({ todo, onToggle, onDelete }) {
  console.log('TodoItem 渲染:', todo.text);
  return (
    <li>
      <span
        onClick={() => onToggle(todo.id)}
        style={{ textDecoration: todo.completed ? 'line-through' : 'none' }}
      >
        {todo.text}
      </span>
      <button onClick={() => onDelete(todo.id)}>删除</button>
    </li>
  );
});

function TodoList({ todos }) {
  const [items, setItems] = useState(todos);

  // 使用useCallback稳定回调函数引用
  const handleToggle = useCallback((id) => {
    setItems(prev =>
      prev.map(item =>
        item.id === id ? { ...item, completed: !item.completed } : item
      )
    );
  }, []);

  const handleDelete = useCallback((id) => {
    setItems(prev => prev.filter(item => item.id !== id));
  }, []);

  return (
    <ul>
      {items.map(todo => (
        <TodoItem
          key={todo.id}
          todo={todo}
          onToggle={handleToggle}
          onDelete={handleDelete}
        />
      ))}
    </ul>
  );
}

// ====== 性能优化原则 ======
console.log('性能优化原则：');
console.log('1. 先测量，后优化（React DevTools Profiler）');
console.log('2. useMemo用于昂贵的计算');
console.log('3. useCallback配合memo使用');
console.log('4. 不要过度优化，只在必要时使用');
```

**💡 代码解释**：第5-33行：useMemo缓存过滤结果和统计数据。第39-79行：useCallback缓存函数引用，配合memo避免子组件重渲染。第83-122行：完整组合优化示例。

**🔑 关键要点**：
- useMemo缓存计算结果
- useCallback缓存函数引用
- memo阻止不必要的重渲染
- 先测量后优化，不过度使用

---

### 7. 状态管理方案对比

> **级别**：中级 | **概念**：React状态管理方案多种多样，包括Context API、Redux、Zustand、Jotai等，各有适用场景，选型需考虑项目规模和团队熟悉度。

```jsx
// ====== 方案1：Context + useReducer（轻量方案） ======
// 适合中小型应用，不需要额外依赖
import { createContext, useContext, useReducer } from 'react';

// 创建Store Context
const StoreContext = createContext(null);

// 定义reducer
function appReducer(state, action) {
  switch (action.type) {
    case 'SET_USER':
      return { ...state, user: action.payload };
    case 'ADD_TODO':
      return { ...state, todos: [...state.todos, action.payload] };
    case 'TOGGLE_TODO':
      return {
        ...state,
        todos: state.todos.map(t =>
          t.id === action.payload ? { ...t, done: !t.done } : t
        )
      };
    default:
      return state;
  }
}

// Provider组件
function StoreProvider({ children }) {
  const [state, dispatch] = useReducer(appReducer, {
    user: null,
    todos: []
  });

  return (
    <StoreContext.Provider value={{ state, dispatch }}>
      {children}
    </StoreContext.Provider>
  );
}

// 自定义Hook：简化使用
function useStore() {
  const context = useContext(StoreContext);
  if (!context) {
    throw new Error('useStore必须在StoreProvider内使用');
  }
  return context;
}

// 使用
// function MyComponent() {
//   const { state, dispatch } = useStore();
//   dispatch({ type: 'SET_USER', payload: { name: '张三' } });
// }

// ====== 方案2：Zustand（轻量推荐） ======
// 极简API，适合中小型项目
// import { create } from 'zustand';
//
// const useStore = create((set) => ({
//   count: 0,
//   increment: () => set((state) => ({ count: state.count + 1 })),
//   decrement: () => set((state) => ({ count: state.count - 1 })),
//   reset: () => set({ count: 0 })
// }));
//
// function Counter() {
//   const { count, increment, decrement } = useStore();
//   return (
//     <div>
//       <p>{count}</p>
//       <button onClick={increment}>+</button>
//       <button onClick={decrement}>-</button>
//     </div>
//   );
// }

// ====== 方案3：Redux Toolkit（大型项目推荐） ======
// 适合大型复杂应用
// import { configureStore, createSlice } from '@reduxjs/toolkit';
// import { Provider, useSelector, useDispatch } from 'react-redux';
//
// // 创建Slice
// const counterSlice = createSlice({
//   name: 'counter',
//   initialState: { value: 0 },
//   reducers: {
//     increment: (state) => { state.value += 1; },
//     decrement: (state) => { state.value -= 1; },
//     incrementByAmount: (state, action) => {
//       state.value += action.payload;
//     }
//   }
// });
//
// // 创建Store
// const store = configureStore({
//   reducer: { counter: counterSlice.reducer }
// });
//
// function Counter() {
//   const count = useSelector((state) => state.counter.value);
//   const dispatch = useDispatch();
//   return (
//     <div>
//       <p>{count}</p>
//       <button onClick={() => dispatch(counterSlice.actions.increment())}>+</button>
//     </div>
//   );
// }

// ====== 方案对比 ======
console.log('状态管理方案对比：');
console.log('Context+useReducer：零依赖，适合小型应用');
console.log('Zustand：极简API，适合中小型应用');
console.log('Redux Toolkit：功能全面，适合大型应用');
console.log('Jotai/Recoil：原子化状态，适合细粒度状态');
console.log('选型建议：简单→Context，中等→Zustand，复杂→Redux Toolkit');
```

**💡 代码解释**：第3-52行：Context+useReducer方案。第56-76行：Zustand极简方案。第80-112行：Redux Toolkit方案。第116-121行：各方案对比和选型建议。

**🔑 关键要点**：
- Context+useReducer适合小型应用
- Zustand API极简，学习成本低
- Redux Toolkit适合大型项目
- 根据项目规模和复杂度选型

---

### 8. 错误边界

> **级别**：中级 | **概念**：错误边界（Error Boundary）是React组件，捕获子组件树中的JavaScript错误，显示降级UI，防止整个应用崩溃。

```jsx
import { Component } from 'react';

// ====== 错误边界类组件 ======
// 错误边界必须使用类组件（目前不支持函数组件+Hook）
class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    // 状态：hasError标记是否捕获到错误
    this.state = { hasError: false, error: null };
  }

  // getDerivedStateFromError：渲染阶段捕获错误
  static getDerivedStateFromError(error) {
    // 更新state，下次渲染显示降级UI
    return { hasError: true, error };
  }

  // componentDidCatch：提交阶段记录错误日志
  componentDidCatch(error, errorInfo) {
    // 记录错误信息到日志服务
    console.error('错误边界捕获:', error, errorInfo);
    // 可以发送到错误追踪服务如Sentry
    // logErrorToService(error, errorInfo);
  }

  // 重置错误状态
  handleReset = () => {
    this.setState({ hasError: false, error: null });
  };

  render() {
    if (this.state.hasError) {
      // 降级UI：显示错误提示
      return (
        <div className="error-boundary">
          <h2>出错了</h2>
          <p>{this.state.error?.message}</p>
          <button onClick={this.handleReset}>重试</button>
        </div>
      );
    }

    // 正常渲染子组件
    return this.props.children;
  }
}

// ====== 使用错误边界 ======
// 包裹可能出错的组件
function App() {
  return (
    <div>
      <ErrorBoundary>
        <RiskyComponent />
      </ErrorBoundary>

      {/* 不同区域用不同的错误边界，互不影响 */}
      <ErrorBoundary>
        <AnotherComponent />
      </ErrorBoundary>
    </div>
  );
}

// 可能出错的组件
function RiskyComponent() {
  const [data, setData] = useState(null);

  useEffect(() => {
    fetch('/api/data')
      .then(res => res.json())
      .then(data => setData(data))
      .catch(() => {
        // 注意：异步错误不会被错误边界捕获
        // 需要转换为状态更新
        throw new Error('数据加载失败');
      });
  }, []);

  return <div>{data.name}</div>;
}

// ====== 错误边界注意事项 ======
// 1. 只能捕获子组件的渲染错误，不能捕获自身错误
// 2. 不能捕获异步错误（setTimeout/Promise）
// 3. 不能捕获事件处理中的错误
// 4. 不能捕获服务端渲染错误
// 5. 推荐使用react-error-boundary库（支持Hook）
```

**💡 代码解释**：第5-43行：ErrorBoundary类组件实现，getDerivedStateFromError捕获渲染错误，componentDidCatch记录日志。第47-58行：使用错误边界包裹组件。第62-80行：RiskyComponent演示可能出错的场景。第83-88行：错误边界局限性说明。

**🔑 关键要点**：
- 错误边界必须用类组件
- getDerivedStateFromError设置降级UI
- componentDidCatch记录错误日志
- 不能捕获异步和事件处理错误

---

## 高级精通

### 1. React 18并发特性

> **级别**：高级 | **概念**：React 18引入并发渲染，通过Suspense、startTransition和useDeferredValue实现非阻塞UI更新，提升用户体验流畅度。

```jsx
import { useState, useTransition, useDeferredValue, Suspense } from 'react';

// ====== startTransition：标记低优先级更新 ======
// 将耗时更新标记为可中断的过渡，保持UI响应
function SearchApp() {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);
  // useTransition返回[isPending, startTransition]
  const [isPending, startTransition] = useTransition();

  const handleChange = (e) => {
    const value = e.target.value;
    // 高优先级：立即更新输入框
    setQuery(value);

    // 低优先级：可中断的搜索更新
    startTransition(() => {
      // 模拟耗时搜索
      const filtered = largeList.filter(item =>
        item.name.toLowerCase().includes(value.toLowerCase())
      );
      setResults(filtered);
    });
  };

  return (
    <div>
      <input value={query} onChange={handleChange} placeholder="搜索..." />
      {/* isPending为true时显示加载状态 */}
      {isPending && <span>搜索中...</span>}
      <ul>
        {results.map(item => (
          <li key={item.id}>{item.name}</li>
        ))}
      </ul>
    </div>
  );
}

// ====== useDeferredValue：延迟值 ======
// 让某个值"滞后"于其他更新
function DeferredSearch() {
  const [query, setQuery] = useState('');
  // deferredQuery是query的"滞后"版本
  const deferredQuery = useDeferredValue(query);
  // query更新立即反映在输入框，deferredQuery滞后更新搜索结果
  const isStale = query !== deferredQuery;

  return (
    <div>
      <input
        value={query}
        onChange={e => setQuery(e.target.value)}
        placeholder="搜索..."
      />
      <div style={{ opacity: isStale ? 0.5 : 1 }}>
        <SearchResults query={deferredQuery} />
      </div>
    </div>
  );
}

// ====== Suspense：数据获取 ======
// 声明式处理加载状态
function App() {
  return (
    <div>
      <h1>我的应用</h1>
      {/* Suspense包裹异步组件 */}
      <Suspense fallback={<div>加载中...</div>}>
        <AsyncComponent />
      </Suspense>
      <Suspense fallback={<div>侧边栏加载中...</div>}>
        <Sidebar />
      </Suspense>
    </div>
  );
}

// ====== 并发特性总结 ======
console.log('React 18并发特性：');
console.log('1. startTransition：标记低优先级更新');
console.log('2. useDeferredValue：延迟值更新');
console.log('3. Suspense：声明式加载状态');
console.log('4. 并发渲染让UI保持响应');
console.log('5. 自动批处理：多个setState合并为一次渲染');
```

**💡 代码解释**：第5-38行：startTransition标记低优先级更新。第42-63行：useDeferredValue延迟值。第67-81行：Suspense声明式加载。

**🔑 关键要点**：
- startTransition标记低优先级更新
- useDeferredValue延迟值
- Suspense声明式加载状态
- 并发渲染保持UI响应

---

### 2. React性能分析

> **级别**：高级 | **概念**：React提供了Profiler API、DevTools Profiler和性能监控工具，帮助开发者识别性能瓶颈和优化渲染行为。

```jsx
import { Profiler } from 'react';

// ====== Profiler API：测量渲染性能 ======
// Profiler包裹需要测量的组件树
function onRenderCallback(
  id,          // Profiler的id属性
  phase,       // 'mount' 或 'update'
  actualDuration, // 本次渲染耗时（ms）
  baseDuration,   // 无优化时的预估耗时
  startTime,      // 渲染开始时间
  commitTime      // 渲染提交时间
) {
  console.log(`[${id}] ${phase} 耗时: ${actualDuration.toFixed(2)}ms`);
}

function App() {
  return (
    <Profiler id="Navigation" onRender={onRenderCallback}>
      <nav>
        <a href="/">首页</a>
        <a href="/about">关于</a>
      </nav>
    </Profiler>
  );
}

// ====== React DevTools Profiler 使用 ======
console.log('React DevTools Profiler 使用步骤：');
console.log('1. 安装React DevTools浏览器扩展');
console.log('2. 打开Profiler标签页');
console.log('3. 点击录制按钮，操作页面');
console.log('4. 停止录制，查看火焰图');
console.log('5. 分析每个组件的渲染时间和原因');

// ====== 常见性能问题与解决方案 ======
console.log('常见性能问题：');
console.log('1. 不必要的重渲染 → React.memo + useCallback');
console.log('2. 昂贵的计算 → useMemo');
console.log('3. 大列表渲染 → 虚拟列表（react-window）');
console.log('4. Context频繁更新 → 拆分Context或使用状态管理库');
console.log('5. 图片加载 → 懒加载（loading="lazy"）');

// ====== 代码分割：React.lazy + Suspense ======
// 按路由拆分代码，减少初始加载体积
import { lazy, Suspense } from 'react';

// 动态导入组件
const HeavyComponent = lazy(() => import('./HeavyComponent'));
const AdminPanel = lazy(() => import('./AdminPanel'));

function AppWithCodeSplitting() {
  return (
    <div>
      <Suspense fallback={<div>加载中...</div>}>
        <HeavyComponent />
      </Suspense>
    </div>
  );
}

// ====== 虚拟列表（Virtual List） ======
console.log('虚拟列表原理：');
console.log('1. 只渲染可视区域内的元素');
console.log('2. 滚动时动态计算可见元素');
console.log('3. 推荐库：react-window, react-virtuoso');
console.log('4. 适用于数千条以上的列表');

// 使用react-window示例
// import { FixedSizeList } from 'react-window';
//
// function VirtualList({ items }) {
//   const Row = ({ index, style }) => (
//     <div style={style}>第 {index} 项: {items[index]}</div>
//   );
//
//   return (
//     <FixedSizeList
//       height={400}
//       width="100%"
//       itemCount={items.length}
//       itemSize={50}
//     >
//       {Row}
//     </FixedSizeList>
//   );
// }

// ====== 性能监控工具 ======
console.log('性能监控工具：');
console.log('1. React DevTools Profiler');
console.log('2. Chrome Performance Tab');
console.log('3. Lighthouse');
console.log('4. Web Vitals（LCP, FID, CLS）');
console.log('5. why-did-you-render（检测不必要渲染）');
```

**💡 代码解释**：第5-22行：Profiler API测量渲染性能。第28-33行：DevTools Profiler使用。第37-47行：代码分割lazy+Suspense。第53-70行：虚拟列表原理。

**🔑 关键要点**：
- Profiler API测量渲染耗时
- React DevTools Profiler火焰图
- React.lazy实现代码分割
- 虚拟列表优化大列表渲染

---

### 3. Server Components（RSC）

> **级别**：高级 | **概念**：React Server Components在服务端渲染，零客户端JS体积，可直接访问数据库和文件系统，是Next.js 13+ App Router的基础。

```jsx
// ====== 服务端组件（Server Component） ======
// 在Next.js 13+ App Router中，默认所有组件都是服务端组件
// 文件名：app/page.tsx

// 服务端组件可以直接使用async/await获取数据
// 无需useEffect，无需useState管理加载状态
// async function BlogPage() {
//   // 直接在服务端查询数据库
//   const posts = await db.post.findMany({
//     where: { published: true },
//     orderBy: { createdAt: 'desc' }
//   });
//
//   return (
//     <div>
//       <h1>博客文章</h1>
//       {posts.map(post => (
//         <article key={post.id}>
//           <h2>{post.title}</h2>
//           <p>{post.content}</p>
//         </article>
//       ))}
//     </div>
//   );
// }

// ====== 客户端组件（Client Component） ======
// 需要交互的组件使用 'use client' 标记
// 'use client';
// 'use client'指令声明这是客户端组件
// import { useState } from 'react';
//
// function LikeButton({ postId }) {
//   const [likes, setLikes] = useState(0);
//
//   return (
//     <button onClick={() => setLikes(prev => prev + 1)}>
//       ❤️ {likes} 赞
//     </button>
//   );
// }

// ====== 服务端组件 vs 客户端组件 ======
console.log('RSC架构要点：');
console.log('1. 服务端组件：默认，无JS体积，可访问后端资源');
console.log('2. 客户端组件：需use client标记，支持交互');
console.log('3. 服务端组件可嵌套客户端组件');
console.log('4. 客户端组件不能导入服务端组件');
console.log('5. 减少客户端JS体积，提升首屏性能');
```

**💡 代码解释**：第5-22行：服务端组件示例，async直接获取数据。第26-37行：客户端组件需use client标记。第41-47行：RSC架构要点总结。

**🔑 关键要点**：
- 服务端组件默认，零JS体积
- 客户端组件需use client
- 服务端组件可访问数据库
- 减少客户端JS提升性能

---

### 4. Fiber架构与虚拟DOM

> **级别**：高级 | **概念**：React Fiber是React 16+核心重构的协调引擎，通过可中断的链表遍历和优先级调度，实现增量渲染和时间切片，让UI更流畅。

```jsx
// ====== 虚拟DOM (Virtual DOM) ======
// 虚拟DOM是真实DOM的JavaScript对象表示
// 对比新旧虚拟DOM树（Diff算法），最小化DOM操作

// 一个React元素对应的虚拟DOM结构
// {
//   type: 'div',
//   props: {
//     className: 'container',
//     children: [
//       { type: 'h1', props: { children: 'Hello' } },
//       { type: 'p', props: { children: 'World' } }
//     ]
//   }
// }

// ====== Fiber架构核心 ======
// React 16+ 用Fiber替代了原有的Stack reconciler
// 每个组件实例对应一个Fiber节点，形成链表结构

// Fiber节点结构（简化版）
// type Fiber = {
//   tag: WorkTag,          // 组件类型（函数组件/类组件/原生标签）
//   type: any,             // 组件函数或标签名
//   stateNode: any,        // 对应的DOM节点或组件实例
//   return: Fiber | null,  // 父Fiber节点
//   child: Fiber | null,   // 第一个子Fiber节点
//   sibling: Fiber | null, // 下一个兄弟Fiber节点
//   alternate: Fiber | null, // 上一次渲染的Fiber（双缓冲）
//   pendingProps: any,     // 新的props
//   memoizedProps: any,    // 上次渲染的props
//   memoizedState: any,    // 上次渲染的state
//   effectTag: number,     // 副作用标记（插入/更新/删除）
//   nextEffect: Fiber | null, // 副作用链表
//   lanes: number,         // 优先级
//   childLanes: number     // 子树优先级
// };

// ====== Fiber工作流程 ======
// 1. Render阶段（可中断）：
//    - beginWork：遍历Fiber树，对比新旧节点
//    - completeWork：收集副作用
//    - 此阶段可被更高优先级任务中断

// 2. Commit阶段（不可中断）：
//    - 一次性将副作用应用到DOM
//    - 调用生命周期/useEffect
//    - 此阶段必须同步完成

// ====== 时间切片（Time Slicing） ======
// requestIdleCallback或Scheduler包实现
// 每个时间切片约5ms，处理完暂停让出主线程
// 确保浏览器能在60fps下响应用户输入

// ====== Diff算法优化 ======
// 1. 同层比较：只比较同层级节点，O(n)复杂度
// 2. 类型不同：直接替换整个子树
// 3. key属性：识别列表中的移动/新增/删除
// 4. 先序遍历：深度优先遍历Fiber树

console.log('Fiber架构关键概念：');
console.log('1. 可中断的异步渲染');
console.log('2. 双缓冲（Current/WorkInProgress树）');
console.log('3. 优先级调度（Lane模型）');
console.log('4. 时间切片保持UI响应');
console.log('5. 副作用收集和批量提交');
```

**💡 代码解释**：第3-15行：虚拟DOM的JavaScript对象结构。第19-40行：Fiber节点数据结构详解。第43-52行：Fiber工作流程Render和Commit两阶段。第56-59行：时间切片机制。第63-68行：Diff算法优化策略。

**🔑 关键要点**：
- 虚拟DOM是最小化DOM操作的中间层
- Fiber链表结构支持可中断遍历
- Render阶段可中断，Commit不可中断
- 时间切片保持60fps流畅体验

---

### 5. 微前端与React

> **级别**：高级 | **概念**：微前端将大型前端应用拆分为多个独立子应用，各团队独立开发部署，通过qiankun、Module Federation或single-spa集成。

```jsx
// ====== 微前端架构方案 ======

// 方案1：Module Federation（Webpack 5）
// 在webpack.config.js中配置
// const { ModuleFederationPlugin } = require('webpack').container;
//
// module.exports = {
//   plugins: [
//     new ModuleFederationPlugin({
//       name: 'host',
//       remotes: {
//         // 远程加载子应用
//         app1: 'app1@http://localhost:3001/remoteEntry.js',
//         app2: 'app2@http://localhost:3002/remoteEntry.js'
//       },
//       shared: ['react', 'react-dom'] // 共享依赖
//     })
//   ]
// };

// 使用远程组件
// const RemoteButton = React.lazy(() => import('app1/Button'));
//
// function App() {
//   return (
//     <Suspense fallback="加载中...">
//       <RemoteButton />
//     </Suspense>
//   );
// }

// ====== 方案2：qiankun（基于single-spa） ======
// 主应用注册子应用
// import { registerMicroApps, start } from 'qiankun';
//
// registerMicroApps([
//   {
//     name: 'react-app',
//     entry: '//localhost:3001',
//     container: '#sub-app-container',
//     activeRule: '/app1'
//   }
// ]);
//
// start();

// ====== 微前端通信 ======
// 1. 自定义事件：window.dispatchEvent
// 主应用发送事件
// window.dispatchEvent(new CustomEvent('user-login', {
//   detail: { userId: 123, name: '张三' }
// }));

// 子应用监听事件
// window.addEventListener('user-login', (event) => {
//   console.log('用户登录:', event.detail);
// });

// 2. 共享状态：通过props或全局store
// 3. URL参数：通过URL传递数据

console.log('微前端核心要点：');
console.log('1. 独立开发部署，技术栈无关');
console.log('2. Module Federation实现运行时集成');
console.log('3. 样式隔离：CSS Modules/Shadow DOM');
console.log('4. JS沙箱：qiankun的Proxy沙箱');
console.log('5. 公共依赖共享避免重复加载');
```

**💡 代码解释**：第5-24行：Module Federation配置和远程组件使用。第28-38行：qiankun注册子应用。第42-54行：微前端通信方式。第58-63行：核心要点总结。

**🔑 关键要点**：
- Module Federation运行时集成
- qiankun基于single-spa
- 自定义事件跨应用通信
- 样式隔离和JS沙箱

---

### 6. React 19新特性与测试

> **级别**：高级 | **概念**：React 19引入Actions、use() Hook、Document Metadata和表单增强，React Testing Library是官方推荐的测试工具。

```jsx
// ====== React 19 新特性 ======

// 1. Actions：自动处理pending状态的异步函数
// import { useActionState } from 'react';
//
// async function updateName(name) {
//   await fetch('/api/user', { method: 'POST', body: JSON.stringify({ name }) });
//   return name;
// }
//
// function ChangeName() {
//   const [state, submitAction, isPending] = useActionState(updateName, '');
//   return (
//     <form action={submitAction}>
//       <input name="name" />
//       <button disabled={isPending}>更新</button>
//     </form>
//   );
// }

// 2. use() Hook：在渲染中读取Promise和Context
// import { use } from 'react';
//
// function UserProfile({ userPromise }) {
//   // use()直接读取Promise结果，无需useEffect
//   const user = use(userPromise);
//   return <div>{user.name}</div>;
// }

// ====== React Testing Library（RTL） ======
// 安装：npm install --save-dev @testing-library/react @testing-library/jest-dom
// 文件名：Counter.test.jsx
// import { render, screen, fireEvent } from '@testing-library/react';
// import Counter from './Counter';
//
// describe('Counter组件', () => {
//   test('渲染初始计数', () => {
//     render(<Counter />);
//     // 通过文本查找元素
//     expect(screen.getByText('当前计数：0')).toBeInTheDocument();
//   });
//
//   test('点击增加按钮后计数+1', () => {
//     render(<Counter />);
//     // 找到按钮并点击
//     const button = screen.getByText('增加');
//     fireEvent.click(button);
//     // 验证计数更新
//     expect(screen.getByText('当前计数：1')).toBeInTheDocument();
//   });
//
//   test('异步数据加载', async () => {
//     render(<UserList />);
//     // 等待异步元素出现
//     const user = await screen.findByText('张三');
//     expect(user).toBeInTheDocument();
//   });
// });

// ====== 测试最佳实践 ======
console.log('测试最佳实践：');
console.log('1. 测试用户行为，而非实现细节');
console.log('2. 使用getByRole优先，getByText次之');
console.log('3. 异步操作用findBy*或waitFor');
console.log('4. 使用userEvent替代fireEvent（更真实）');
console.log('5. 测试Happy Path + 边界情况 + 错误状态');
```

**💡 代码解释**：第5-19行：React 19的useActionState。第23-29行：use() Hook。第33-54行：React Testing Library测试示例。第58-63行：测试最佳实践。

**🔑 关键要点**：
- React 19 Actions自动处理pending
- use()在渲染中读取Promise
- RTL测试用户行为而非实现
- userEvent模拟真实用户操作

---
