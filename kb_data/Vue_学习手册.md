# 💚 Vue 编程语言学习手册

> **分类**：前端  
> **描述**：Vue 3是渐进式JavaScript框架，基于组合式API和响应式系统，轻量高效地构建用户界面  
> **生成时间**：2026-08-07  

---

## 目录

- [初级入门](#primary)（8个知识点）
- [中级进阶](#intermediate)（8个知识点）
- [高级精通](#advanced)（6个知识点）

---

## 初级入门

### 1. 模板语法

> **级别**：初级 | **概念**：Vue模板语法基于HTML，通过双花括号{{}}插值表达式和v-指令绑定数据，实现声明式渲染。

```html
<!-- ====== Vue 3 模板语法 ====== -->
<template>
  <!-- 根元素：template中必须有唯一的根元素 -->
  <div>
    <!-- 1. 文本插值：{{ 表达式 }} -->
    <!-- 双花括号中写JavaScript表达式 -->
    <h1>{{ title }}</h1>
    <p>欢迎，{{ username }}！</p>
    <p>年龄：{{ age + 1 }}</p> <!-- 表达式计算 -->
    <p>{{ isActive ? '激活' : '禁用' }}</p> <!-- 三元表达式 -->

    <!-- 2. v-html：渲染HTML内容（注意XSS风险） -->
    <div v-html="htmlContent"></div>

    <!-- 3. v-bind：动态绑定属性 -->
    <!-- 完整写法 v-bind:属性名="表达式" -->
    <a v-bind:href="url">链接</a>
    <!-- 简写 :属性名="表达式" -->
    <img :src="imageSrc" :alt="imageAlt">
    <!-- 动态绑定多个属性 -->
    <div v-bind="attrsObject"></div>

    <!-- 4. v-on：事件绑定 -->
    <!-- 完整写法 v-on:事件名="处理函数" -->
    <button v-on:click="handleClick">点击</button>
    <!-- 简写 @事件名="处理函数" -->
    <button @click="count++">计数：{{ count }}</button>
    <!-- 传递参数 -->
    <button @click="sayHello('张三')">打招呼</button>
    <!-- 访问原生事件对象 -->
    <button @click="handleEvent($event)">事件对象</button>

    <!-- 5. 修饰符 -->
    <!-- .prevent阻止默认行为 -->
    <form @submit.prevent="onSubmit">
      <button type="submit">提交</button>
    </form>
    <!-- .stop阻止事件冒泡 -->
    <div @click="parentClick">
      <button @click.stop="childClick">不冒泡</button>
    </div>
    <!-- 键盘修饰符 -->
    <input @keyup.enter="handleEnter" placeholder="按回车触发">
  </div>
</template>

<script setup>
// 响应式数据
import { ref, reactive } from 'vue';

const title = ref('Vue 3 模板语法');
const username = ref('张三');
const age = ref(25);
const isActive = ref(true);
const htmlContent = ref('<strong>粗体文字</strong>');
const url = ref('https://vuejs.org');
const imageSrc = ref('/images/logo.png');
const imageAlt = ref('Logo');
const count = ref(0);

const attrsObject = reactive({
  id: 'my-div',
  class: 'container'
});

// 事件处理函数
const handleClick = () => {
  alert('按钮被点击');
};

const sayHello = (name) => {
  alert(`你好，${name}！`);
};

const handleEvent = (event) => {
  console.log('事件对象:', event);
};

const onSubmit = () => {
  console.log('表单提交');
};

const parentClick = () => console.log('父元素');
const childClick = () => console.log('子元素');
const handleEnter = () => console.log('回车键');
</script>
```

**💡 代码解释**：第3-38行：模板语法——{{}}插值、v-html、v-bind、v-on、事件修饰符。第42-56行：响应式数据定义。第59-75行：事件处理函数。

**🔑 关键要点**：
- {{}}插值表达式
- v-bind简写为:属性
- v-on简写为@事件
- .prevent/.stop等修饰符

---

### 2. 响应式基础：ref与reactive

> **级别**：初级 | **概念**：Vue 3的响应式系统通过ref（基本类型）和reactive（对象类型）创建响应式数据，数据变化时自动更新视图。

```html
<!-- ====== ref 和 reactive 基础 ====== -->
<template>
  <div>
    <h2>ref 响应式示例</h2>
    <!-- 模板中ref自动解包，不需要.value -->
    <p>计数：{{ count }}</p>
    <button @click="increment">+1</button>

    <p>姓名：{{ name }}</p>
    <input v-model="name" placeholder="输入姓名">
    <!-- v-model双向绑定：输入框变化时name自动更新 -->

    <h2>reactive 响应式示例</h2>
    <p>用户：{{ user.name }}，年龄：{{ user.age }}</p>
    <button @click="updateUser">更新用户</button>

    <h2>ref 包装对象</h2>
    <!-- ref也可以包装对象，内部自动转为reactive -->
    <p>产品：{{ product.name }}，价格：{{ product.price }}</p>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';

// ====== ref：用于基本类型和对象 ======
// ref创建响应式引用，内部值通过.value访问
const count = ref(0); // 数字类型
const name = ref('张三'); // 字符串类型

// 在JS中访问ref需要.value
const increment = () => {
  count.value++; // 修改需要.value
  console.log('当前计数:', count.value); // 读取也需要.value
};

// ref也可以包装对象
const product = ref({
  name: '笔记本电脑',
  price: 6999
});
// product.value.price = 7999; // 修改对象属性

// ====== reactive：用于对象类型 ======
// reactive创建深层响应式对象，不需要.value
const user = reactive({
  name: '李四',
  age: 30,
  address: {
    city: '北京',
    district: '朝阳区'
  }
});

const updateUser = () => {
  // 直接修改属性，不需要.value
  user.age++;
  user.address.city = '上海'; // 深层响应式
};

// ====== ref vs reactive 对比 ======
// ref：
// - 适合基本类型
// - .value访问和修改
// - 模板中自动解包
// - 可整体替换

// reactive：
// - 适合对象/数组
// - 直接访问属性
// - 不能整体替换（会丢失响应式）
// - 解构会丢失响应式

// 解决reactive解构问题：使用toRefs
import { toRefs } from 'vue';
// const { name: userName, age: userAge } = toRefs(user);
// 解构后仍保持响应式

// ====== shallowRef / shallowReactive ======
// 浅层响应式：只有顶层属性是响应式的
import { shallowRef, shallowReactive } from 'vue';
const shallowObj = shallowRef({ nested: { value: 1 } });
// shallowObj.value.nested.value = 2; // ❌ 不会触发更新
// shallowObj.value = { nested: { value: 2 } }; // ✅ 整体替换触发更新
</script>
```

**💡 代码解释**：第3-16行：模板中使用ref和reactive。第22-36行：ref定义和.value访问。第40-50行：reactive定义和直接修改。第54-80行：ref vs reactive对比和shallowRef。

**🔑 关键要点**：
- ref用于基本类型，需.value访问
- reactive用于对象，直接访问属性
- 模板中ref自动解包
- reactive解构会丢失响应式

---

### 3. 计算属性computed

> **级别**：初级 | **概念**：computed基于响应式数据派生新值，具有缓存机制，只有依赖变化时才重新计算，避免不必要的重复运算。

```html
<!-- ====== 计算属性 ====== -->
<template>
  <div>
    <h2>购物车</h2>
    <ul>
      <li v-for="item in cart" :key="item.id">
        {{ item.name }} - ¥{{ item.price }} × {{ item.quantity }}
        <button @click="item.quantity++">+</button>
        <button @click="item.quantity--" :disabled="item.quantity <= 1">-</button>
        <span>小计：¥{{ item.price * item.quantity }}</span>
      </li>
    </ul>

    <!-- 计算属性：自动更新 -->
    <p>总数量：{{ totalQuantity }}</p>
    <p>总价格：¥{{ totalPrice }}</p>
    <p>是否免运费：{{ freeShipping ? '是' : '否（满100免运费）' }}</p>

    <h2>搜索过滤</h2>
    <input v-model="searchQuery" placeholder="搜索商品">
    <ul>
      <li v-for="item in filteredItems" :key="item.id">
        {{ item.name }}
      </li>
    </ul>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue';

const cart = reactive([
  { id: 1, name: '商品A', price: 29.9, quantity: 1 },
  { id: 2, name: '商品B', price: 59.9, quantity: 2 },
  { id: 3, name: '商品C', price: 19.9, quantity: 1 }
]);

// ====== 计算属性：只读 ======
// computed返回一个ref，依赖变化时自动更新
const totalQuantity = computed(() => {
  console.log('计算总数量...'); // 只在依赖变化时执行
  return cart.reduce((sum, item) => sum + item.quantity, 0);
});

const totalPrice = computed(() => {
  return cart.reduce((sum, item) => sum + item.price * item.quantity, 0);
});

// 计算属性可以依赖其他计算属性
const freeShipping = computed(() => {
  return totalPrice.value >= 100; // 计算属性用.value访问
});

// ====== 搜索过滤 ======
const searchQuery = ref('');
const allItems = ref([
  { id: 1, name: '苹果' },
  { id: 2, name: '香蕉' },
  { id: 3, name: '橙子' },
  { id: 4, name: '西瓜' },
  { id: 5, name: '葡萄' }
]);

const filteredItems = computed(() => {
  // 搜索过滤：不区分大小写
  return allItems.value.filter(item =>
    item.name.toLowerCase().includes(searchQuery.value.toLowerCase())
  );
});

// ====== 可写计算属性 ======
// computed也可以提供get和set
const fullName = computed({
  get() {
    return `${firstName.value} ${lastName.value}`;
  },
  set(newValue) {
    // 设置时拆分名字
    const parts = newValue.split(' ');
    firstName.value = parts[0] || '';
    lastName.value = parts[1] || '';
  }
});

const firstName = ref('三');
const lastName = ref('张');
// fullName.value = '四 李'; // 触发set

// ====== computed vs methods ======
// computed：有缓存，依赖不变时不重新计算
// methods：每次调用都重新执行
// 使用computed的场景：
// 1. 基于现有数据的派生状态
// 2. 需要缓存的昂贵计算
// 3. 多个地方使用同一个计算结果
</script>
```

**💡 代码解释**：第3-23行：模板中使用computed。第29-52行：computed计算总价和数量。第56-68行：搜索过滤computed。第72-88行：可写computed的get/set。

**🔑 关键要点**：
- computed返回ref，有缓存
- 依赖变化时才重新计算
- 可写computed提供get/set
- computed优先于methods

---

### 4. 侦听器watch

> **级别**：初级 | **概念**：watch监听响应式数据的变化，在数据变化时执行副作用（异步请求、DOM操作等），适合处理需要响应数据变化的逻辑。

```html
<!-- ====== 侦听器 ====== -->
<template>
  <div>
    <h2>搜索（watch版）</h2>
    <input v-model="keyword" placeholder="输入搜索关键词">
    <p v-if="loading">搜索中...</p>
    <ul>
      <li v-for="item in results" :key="item">{{ item }}</li>
    </ul>

    <h2>表单监听</h2>
    <input v-model="formData.name" placeholder="姓名">
    <input v-model="formData.email" placeholder="邮箱">
    <p>表单变化次数：{{ changeCount }}</p>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue';

// ====== watch：监听单个ref ======
const keyword = ref('');
const results = ref([]);
const loading = ref(false);

// watch(监听源, 回调函数, 选项)
watch(keyword, async (newValue, oldValue) => {
  // newValue：新值，oldValue：旧值
  console.log(`关键词从 "${oldValue}" 变为 "${newValue}"`);

  if (!newValue.trim()) {
    results.value = [];
    return;
  }

  loading.value = true;
  // 模拟异步搜索
  await new Promise(resolve => setTimeout(resolve, 500));
  results.value = ['结果1', '结果2', '结果3'].filter(
    item => item.includes(newValue)
  );
  loading.value = false;
});

// ====== watch：监听reactive对象 ======
const formData = reactive({
  name: '',
  email: ''
});
const changeCount = ref(0);

// 监听reactive对象：默认深层监听
watch(formData, (newValue) => {
  changeCount.value++;
  console.log('表单变化:', newValue);
});

// ====== watch：监听对象特定属性 ======
// 使用getter函数监听reactive的特定属性
watch(
  () => formData.name, // getter函数
  (newName) => {
    console.log('姓名变化:', newName);
  }
);

// ====== watch：监听多个数据源 ======
const x = ref(0);
const y = ref(0);

watch([x, y], ([newX, newY], [oldX, oldY]) => {
  // 回调参数也是数组
  console.log(`坐标: (${oldX},${oldY}) → (${newX},${newY})`);
});

// ====== watch选项 ======
// watch(source, callback, { immediate, deep, flush })

// immediate: true → 立即执行一次回调
watch(keyword, (val) => {
  console.log('立即执行:', val);
}, { immediate: true });

// deep: true → 深层监听（reactive默认深层）
// 监听ref对象需要deep: true
const obj = ref({ nested: { value: 1 } });
watch(obj, (val) => {
  console.log('深层变化:', val);
}, { deep: true });

// ====== watchEffect：自动追踪依赖 ======
import { watchEffect } from 'vue';

// watchEffect不需要指定监听源，自动追踪回调中使用的响应式数据
watchEffect(() => {
  // 自动追踪keyword和formData.name
  console.log(`关键词: ${keyword.value}, 姓名: ${formData.name}`);
});

// watch vs watchEffect
// watch：明确指定监听源，可获取旧值
// watchEffect：自动追踪依赖，立即执行，无法获取旧值
</script>
```

**💡 代码解释**：第3-15行：模板中搜索和表单。第22-43行：watch监听ref。第47-53行：watch监听reactive。第57-64行：getter监听特定属性。第68-78行：监听多个数据源。第82-98行：watch选项和watchEffect。

**🔑 关键要点**：
- watch明确指定监听源
- watchEffect自动追踪依赖
- immediate立即执行
- deep深层监听

---

### 5. 条件与列表渲染

> **级别**：初级 | **概念**：Vue提供v-if/v-else-if/v-else条件渲染和v-for列表渲染指令，以及v-show控制显示隐藏，灵活控制DOM结构。

```html
<!-- ====== 条件渲染与列表渲染 ====== -->
<template>
  <div>
    <!-- ====== v-if / v-else-if / v-else ====== -->
    <!-- v-if：条件为真时渲染元素，为假时DOM完全移除 -->
    <h2>条件渲染</h2>
    <p v-if="score >= 90">优秀</p>
    <p v-else-if="score >= 60">及格</p>
    <p v-else>不及格</p>

    <!-- v-show：通过display:none控制显示隐藏，DOM始终存在 -->
    <div v-show="isVisible">
      这个div通过v-show控制显示
    </div>

    <!-- v-if vs v-show -->
    <!-- v-if：条件为假时DOM移除，切换开销大 -->
    <!-- v-show：始终渲染，切换开销小，适合频繁切换 -->

    <!-- ====== v-for 列表渲染 ====== -->
    <h2>列表渲染</h2>

    <!-- 遍历数组：v-for="(item, index) in items" :key="唯一标识" -->
    <ul>
      <li v-for="(fruit, index) in fruits" :key="fruit">
        <!-- key属性必须唯一且稳定，帮助Vue追踪元素 -->
        {{ index + 1 }}. {{ fruit }}
      </li>
    </ul>

    <!-- 遍历对象 -->
    <ul>
      <li v-for="(value, key, index) in userInfo" :key="key">
        {{ key }}: {{ value }} (第{{ index + 1 }}个属性)
      </li>
    </ul>

    <!-- 遍历数字范围 -->
    <span v-for="n in 5" :key="n">{{ n }} </span>

    <!-- ====== v-for 与 v-if 同时使用 ====== -->
    <!-- ⚠️ 不推荐在同一元素上使用v-for和v-if -->
    <!-- 推荐：使用computed过滤后再v-for -->
    <h2>待办事项</h2>
    <ul>
      <!-- 使用template包裹，v-for在template上 -->
      <template v-for="todo in todos" :key="todo.id">
        <li v-if="!todo.completed">
          {{ todo.text }}
        </li>
      </template>
    </ul>

    <!-- ====== 数组更新检测 ====== -->
    <h2>数组操作</h2>
    <button @click="addItem">添加</button>
    <button @click="removeLast">删除最后一个</button>
    <button @click="sortItems">排序</button>

    <!-- 过渡效果：配合Transition使用 -->
    <!-- <TransitionGroup name="list" tag="ul">
      <li v-for="item in items" :key="item.id">{{ item.text }}</li>
    </TransitionGroup> -->
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';

const score = ref(85);
const isVisible = ref(true);

const fruits = ref(['苹果', '香蕉', '橙子', '葡萄']);

const userInfo = reactive({
  name: '张三',
  age: 25,
  email: 'zhangsan@example.com'
});

const todos = ref([
  { id: 1, text: '学习Vue', completed: true },
  { id: 2, text: '写代码', completed: false },
  { id: 3, text: '运动', completed: false }
]);

// 数组操作
const items = ref([1, 2, 3, 4, 5]);

const addItem = () => {
  items.value.push(items.value.length + 1);
};

const removeLast = () => {
  items.value.pop();
};

const sortItems = () => {
  items.value.sort((a, b) => b - a); // 降序排列
};

// key的作用：
// 1. 帮助Vue识别每个节点，高效更新DOM
// 2. 没有key时使用"就地更新"策略
// 3. 必须使用唯一且稳定的标识符
// 4. 不要使用index作为key（列表会重排时）
</script>
```

**💡 代码解释**：第3-14行：v-if/v-else条件渲染。第19-47行：v-for遍历数组、对象、数字范围。第50-56行：v-for和v-if配合。第62-67行：数组更新操作。

**🔑 关键要点**：
- v-if移除DOM，v-show隐藏DOM
- v-for必须绑定:key
- v-for和v-if不推荐同元素
- 数组变更方法自动触发更新

---

### 6. 事件处理与表单绑定

> **级别**：初级 | **概念**：Vue通过v-on(@)绑定事件和v-model双向绑定表单数据，配合修饰符实现多种交互场景。

```html
<!-- ====== 事件处理与表单绑定 ====== -->
<template>
  <div>
    <h2>事件处理</h2>
    <!-- 内联事件处理 -->
    <button @click="count++">计数：{{ count }}</button>

    <!-- 方法事件处理 -->
    <button @click="greet('张三')">打招呼</button>

    <!-- 事件修饰符 -->
    <!-- .prevent：阻止默认事件 -->
    <a href="https://vuejs.org" @click.prevent="handlePrevent">
      阻止跳转
    </a>

    <!-- .stop：阻止事件冒泡 -->
    <div @click="parentClick">
      父元素
      <button @click.stop="childClick">阻止冒泡</button>
    </div>

    <!-- .once：只触发一次 -->
    <button @click.once="handleOnce">只触发一次</button>

    <!-- .self：只在自身触发 -->
    <div @click.self="handleSelf">
      点击我自身才触发
      <button>点击按钮不会触发父元素</button>
    </div>

    <!-- 按键修饰符 -->
    <input @keyup.enter="handleEnter" placeholder="按回车">
    <input @keyup.ctrl.s="handleSave" placeholder="Ctrl+S保存">

    <!-- 系统修饰符：.exact精确匹配 -->
    <button @click.ctrl.exact="handleCtrlClick">
      仅Ctrl+点击触发
    </button>

    <h2>表单绑定 v-model</h2>
    <!-- v-model：双向绑定，自动根据控件类型选择更新方式 -->

    <!-- 文本输入 -->
    <input v-model="text" placeholder="输入文本">
    <p>输入内容：{{ text }}</p>

    <!-- 多行文本 -->
    <textarea v-model="message" placeholder="多行文本"></textarea>
    <p>消息：{{ message }}</p>

    <!-- 复选框（单个） -->
    <input type="checkbox" v-model="checked">
    <span>{{ checked ? '已选中' : '未选中' }}</span>

    <!-- 复选框（多个，绑定到数组） -->
    <div>
      <input type="checkbox" v-model="selectedFruits" value="苹果"> 苹果
      <input type="checkbox" v-model="selectedFruits" value="香蕉"> 香蕉
      <input type="checkbox" v-model="selectedFruits" value="橙子"> 橙子
    </div>
    <p>选中：{{ selectedFruits }}</p>

    <!-- 单选按钮 -->
    <div>
      <input type="radio" v-model="gender" value="male"> 男
      <input type="radio" v-model="gender" value="female"> 女
    </div>
    <p>性别：{{ gender }}</p>

    <!-- 下拉选择 -->
    <select v-model="selectedCity">
      <option disabled value="">请选择城市</option>
      <option value="北京">北京</option>
      <option value="上海">上海</option>
      <option value="广州">广州</option>
    </select>
    <p>城市：{{ selectedCity }}</p>

    <!-- v-model 修饰符 -->
    <!-- .lazy：change事件时更新（而非input） -->
    <input v-model.lazy="lazyText" placeholder="失去焦点时更新">

    <!-- .number：自动转为数字 -->
    <input v-model.number="age" type="number" placeholder="年龄">

    <!-- .trim：自动去除首尾空格 -->
    <input v-model.trim="trimmedText" placeholder="自动去空格">
  </div>
</template>

<script setup>
import { ref } from 'vue';

const count = ref(0);

const greet = (name) => alert(`你好，${name}！`);
const handlePrevent = () => console.log('默认行为被阻止');
const parentClick = () => console.log('父元素');
const childClick = () => console.log('子元素');
const handleOnce = () => console.log('只触发一次');
const handleSelf = () => console.log('自身触发');
const handleEnter = () => console.log('回车键');
const handleSave = () => console.log('Ctrl+S 保存');
const handleCtrlClick = () => console.log('仅Ctrl+点击');

// 表单数据
const text = ref('');
const message = ref('');
const checked = ref(false);
const selectedFruits = ref([]);
const gender = ref('male');
const selectedCity = ref('');
const lazyText = ref('');
const age = ref(0);
const trimmedText = ref('');
</script>
```

**💡 代码解释**：第3-41行：事件绑定和修饰符。第45-85行：v-model各种表单控件绑定。第89-96行：v-model修饰符。第100-117行：响应式数据定义。

**🔑 关键要点**：
- @事件名绑定事件
- .prevent/.stop/.once修饰符
- v-model双向绑定
- .lazy/.number/.trim修饰符

---

### 7. 组件基础

> **级别**：初级 | **概念**：Vue组件是可复用的Vue实例，通过props接收数据、emits发送事件，实现组件树中的数据传递和通信。

```html
<!-- ====== 父组件 App.vue ====== -->
<template>
  <div>
    <h1>组件基础</h1>

    <!-- 使用子组件，传递props -->
    <UserCard
      name="张三"
      :age="25"
      role="开发者"
      @update="handleUpdate"
    />

    <!-- 动态props -->
    <UserCard
      v-for="user in users"
      :key="user.id"
      :name="user.name"
      :age="user.age"
      :role="user.role"
    />

    <p>收到更新事件：{{ lastUpdate }}</p>
  </div>
</template>

<script setup>
import { ref } from 'vue';
// 导入子组件
import UserCard from './UserCard.vue';

const users = ref([
  { id: 1, name: '张三', age: 25, role: '前端' },
  { id: 2, name: '李四', age: 30, role: '后端' },
  { id: 3, name: '王五', age: 28, role: '设计师' }
]);

const lastUpdate = ref('');

const handleUpdate = (info) => {
  lastUpdate.value = `更新了 ${info}`;
};
</script>

<!-- ====== 子组件 UserCard.vue ====== -->
<!--
<template>
  <div class="user-card">
    <h3>{{ name }}</h3>
    <p>年龄：{{ age }}</p>
    <p>角色：{{ role }}</p>
    <button @click="notifyParent">通知父组件</button>
  </div>
</template>

<script setup>
// defineProps：声明接收的props（无需导入）
const props = defineProps({
  name: {
    type: String,   // 类型校验
    required: true, // 必填
    default: '未知' // 默认值
  },
  age: {
    type: Number,
    default: 0,
    validator: (value) => value >= 0 && value <= 150 // 自定义验证
  },
  role: {
    type: String,
    default: '普通用户'
  }
});

// defineEmits：声明组件可以触发的事件
const emit = defineEmits(['update']);

const notifyParent = () => {
  // emit触发事件，传递数据给父组件
  emit('update', props.name);
};
</script>

<style scoped>
/* scoped：样式仅作用于当前组件 */
.user-card {
  border: 1px solid #ddd;
  padding: 16px;
  margin: 8px 0;
  border-radius: 8px;
}
</style>
-->

<!-- ====== 组件注册方式 ====== -->
<!-- 1. 局部注册：在script setup中import后直接使用 -->
<!-- 2. 全局注册：app.component('MyComponent', MyComponent) -->

<!-- ====== 组件命名规范 ====== -->
<!-- PascalCase：UserCard（模板中可用<UserCard>或<user-card>） -->
<!-- kebab-case：user-card（模板中必须用<user-card>） -->
```

**💡 代码解释**：第3-18行：父组件使用子组件传递props。第28-37行：父组件数据定义。第43-77行：子组件defineProps和defineEmits。第80-85行：scoped样式。

**🔑 关键要点**：
- defineProps声明接收的数据
- defineEmits声明触发的事件
- props支持类型校验和默认值
- scoped样式隔离

---

### 8. 生命周期钩子

> **级别**：初级 | **概念**：Vue 3生命周期钩子（onMounted、onUpdated、onUnmounted等）在组件的不同阶段执行，用于管理副作用、获取数据和清理资源。

```html
<!-- ====== Vue 3 生命周期钩子 ====== -->
<template>
  <div>
    <h2>生命周期演示</h2>
    <p>计数：{{ count }}</p>
    <button @click="count++">+1</button>
    <button @click="toggleChild">
      {{ showChild ? '隐藏' : '显示' }}子组件
    </button>

    <!-- 条件渲染触发生命周期 -->
    <ChildComponent v-if="showChild" :message="`计数: ${count}`" />
  </div>
</template>

<script setup>
import { ref, onMounted, onUpdated, onUnmounted, onBeforeMount, onBeforeUpdate, onBeforeUnmount } from 'vue';

const count = ref(0);
const showChild = ref(true);

// ====== 生命周期钩子（按执行顺序） ======

// setup()：组件初始化，最先执行
console.log('1. setup - 组件初始化');

// onBeforeMount：DOM挂载前，DOM还未生成
onBeforeMount(() => {
  console.log('2. onBeforeMount - DOM挂载前');
  // 此时无法访问DOM元素
});

// onMounted：DOM挂载完成后，可以访问DOM
onMounted(() => {
  console.log('3. onMounted - DOM挂载完成');
  // 适合：发起API请求、初始化第三方库、添加事件监听
  // fetchData();
  // initChart();
});

// onBeforeUpdate：数据变化后、DOM更新前
onBeforeUpdate(() => {
  console.log('4. onBeforeUpdate - 数据变化，DOM更新前');
  // 可以读取更新前的DOM状态
});

// onUpdated：DOM更新完成后
onUpdated(() => {
  console.log('5. onUpdated - DOM更新完成');
  // 注意：不要在updated中修改数据，可能导致无限循环
});

// onBeforeUnmount：组件卸载前
onBeforeUnmount(() => {
  console.log('6. onBeforeUnmount - 组件卸载前');
  // 清理工作：移除事件监听、取消定时器
});

// onUnmounted：组件卸载后
onUnmounted(() => {
  console.log('7. onUnmounted - 组件卸载完成');
  // 组件完全销毁后的清理
});

const toggleChild = () => {
  showChild.value = !showChild.value;
};
</script>

<!-- ====== 子组件：观察生命周期 ====== -->
<!-- ChildComponent.vue -->
<!--
<template>
  <div class="child">
    <p>子组件：{{ message }}</p>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue';

defineProps(['message']);

onMounted(() => {
  console.log('  子组件 onMounted');
});

onUnmounted(() => {
  console.log('  子组件 onUnmounted');
});
</script>
-->

<!-- ====== 生命周期钩子使用场景 ====== -->
<!-- onMounted： -->
<!-- - 发起API请求获取数据 -->
<!-- - 初始化第三方库（图表、地图等） -->
<!-- - 添加DOM事件监听 -->
<!-- - 获取DOM元素引用 -->

<!-- onUnmounted： -->
<!-- - 清除定时器/interval -->
<!-- - 移除事件监听 -->
<!-- - 取消API请求/订阅 -->
<!-- - 销毁第三方库实例 -->

<!-- onUpdated： -->
<!-- - 数据变化后操作DOM -->
<!-- - 同步第三方库状态 -->

<!-- ====== Vue 3 vs Vue 2 生命周期对比 ====== -->
<!-- Vue 2 Options API → Vue 3 Composition API -->
<!-- beforeCreate → setup() -->
<!-- created → setup() -->
<!-- beforeMount → onBeforeMount -->
<!-- mounted → onMounted -->
<!-- beforeUpdate → onBeforeUpdate -->
<!-- updated → onUpdated -->
<!-- beforeDestroy → onBeforeUnmount -->
<!-- destroyed → onUnmounted -->
```

**💡 代码解释**：第3-33行：模板演示生命周期。第37-76行：7个生命周期钩子函数按顺序排列。第80-100行：子组件生命周期观察。第104-122行：各钩子使用场景和Vue 2对比。

**🔑 关键要点**：
- onMounted操作DOM和发起请求
- onUnmounted清理副作用
- onBeforeUpdate读取更新前DOM
- setup替代beforeCreate/created

---

## 中级进阶

### 1. 组合式API深入

> **级别**：中级 | **概念**：组合式API（Composition API）通过setup()函数或<script setup>组织逻辑，支持将相关功能封装为可复用的组合函数（Composables）。

```html
<!-- ====== 组合式API：Composables ====== -->
<!-- 将逻辑封装为可复用的组合函数 -->

<!-- 使用组合函数的组件 -->
<template>
  <div>
    <h2>鼠标位置</h2>
    <p>X: {{ x }}, Y: {{ y }}</p>

    <h2>计数器</h2>
    <p>{{ count }}</p>
    <button @click="increment">+</button>
    <button @click="decrement">-</button>
    <button @click="reset">重置</button>

    <h2>网络状态</h2>
    <p>{{ isOnline ? '在线' : '离线' }}</p>
  </div>
</template>

<script setup>
import { useMouse } from './composables/useMouse';
import { useCounter } from './composables/useCounter';
import { useOnline } from './composables/useOnline';

// 使用组合函数，获取响应式数据和方法
const { x, y } = useMouse();
const { count, increment, decrement, reset } = useCounter(0);
const { isOnline } = useOnline();
</script>

<!-- ====== composables/useMouse.js ====== -->
<!--
import { ref, onMounted, onUnmounted } from 'vue';

// 组合函数命名以use开头
export function useMouse() {
  // 响应式状态
  const x = ref(0);
  const y = ref(0);

  // 事件处理函数
  function update(event) {
    x.value = event.pageX;
    y.value = event.pageY;
  }

  // 生命周期：挂载时添加监听
  onMounted(() => {
    window.addEventListener('mousemove', update);
  });

  // 生命周期：卸载时移除监听
  onUnmounted(() => {
    window.removeEventListener('mousemove', update);
  });

  // 返回响应式数据和方法
  return { x, y };
}
-->

<!-- ====== composables/useCounter.js ====== -->
<!--
import { ref, computed } from 'vue';

export function useCounter(initialValue = 0) {
  const count = ref(initialValue);

  // 计算属性
  const isEven = computed(() => count.value % 2 === 0);

  // 方法
  const increment = () => count.value++;
  const decrement = () => count.value--;
  const reset = () => count.value = initialValue;

  return { count, isEven, increment, decrement, reset };
}
-->

<!-- ====== composables/useOnline.js ====== -->
<!--
import { ref, onMounted, onUnmounted } from 'vue';

export function useOnline() {
  const isOnline = ref(navigator.onLine);

  const updateOnline = () => isOnline.value = true;
  const updateOffline = () => isOnline.value = false;

  onMounted(() => {
    window.addEventListener('online', updateOnline);
    window.addEventListener('offline', updateOffline);
  });

  onUnmounted(() => {
    window.removeEventListener('online', updateOnline);
    window.removeEventListener('offline', updateOffline);
  });

  return { isOnline };
}
-->

<!-- ====== Options API vs Composition API ====== -->
<!-- Options API：data、methods、computed等选项组织代码 -->
<!-- Composition API：setup()中自由组织逻辑，更灵活 -->

<!-- Options API 写法（对比） -->
<!--
export default {
  data() {
    return { count: 0 }
  },
  methods: {
    increment() { this.count++ }
  },
  computed: {
    double() { return this.count * 2 }
  }
}
-->

<!-- Composition API 写法 -->
<!--
<script setup>
const count = ref(0);
const increment = () => count.value++;
const double = computed(() => count.value * 2);
</script>
-->
```

**💡 代码解释**：第3-18行：使用组合函数的组件。第24-46行：useMouse组合函数。第50-64行：useCounter组合函数。第68-84行：useOnline组合函数。第90-102行：Options API vs Composition API对比。

**🔑 关键要点**：
- 组合函数以use开头命名
- 封装响应式状态和逻辑
- 返回响应式数据和方法
- 支持在组合函数中使用生命周期

---

### 2. 组件通信

> **级别**：中级 | **概念**：Vue组件通信方式包括props/emits（父子）、provide/inject（跨层级）、事件总线（不推荐）和状态管理（Pinia），选择合适的方式传递数据。

```html
<!-- ====== 1. Props + Emits：父子组件通信 ====== -->
<!-- 父组件 -->
<template>
  <div>
    <h2>父子通信</h2>
    <!-- 通过props向下传递数据 -->
    <ChildComponent
      :message="parentMessage"
      :count="count"
      @response="handleResponse"
      @update:count="count = $event"
    />
    <p>子组件回复：{{ childResponse }}</p>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const parentMessage = ref('来自父组件的消息');
const count = ref(0);
const childResponse = ref('');

const handleResponse = (msg) => {
  childResponse.value = msg;
};
</script>

<!-- 子组件 ChildComponent.vue -->
<!--
<template>
  <div>
    <p>父组件消息：{{ message }}</p>
    <p>计数：{{ count }}</p>
    <button @click="respond">回复父组件</button>
    <button @click="updateCount">更新计数</button>
  </div>
</template>

<script setup>
const props = defineProps({
  message: String,
  count: Number
});

const emit = defineEmits(['response', 'update:count']);

const respond = () => {
  emit('response', '收到消息！');
};

const updateCount = () => {
  // v-model语法糖：emit('update:count', value)
  emit('update:count', props.count + 1);
};
</script>
-->

<!-- ====== 2. Provide / Inject：跨层级通信 ====== -->
<!-- 祖先组件提供数据 -->
<template>
  <div>
    <h2>Provide / Inject</h2>
    <DeepChild />
  </div>
</template>

<script setup>
import { provide, ref, readonly } from 'vue';

// 提供数据
const theme = ref('light');
// provide(键名, 值)
provide('theme', readonly(theme)); // readonly防止子组件修改

// 提供方法
const toggleTheme = () => {
  theme.value = theme.value === 'light' ? 'dark' : 'light';
};
provide('toggleTheme', toggleTheme);

// 也可以提供响应式对象
provide('appConfig', {
  version: '1.0.0',
  apiUrl: 'https://api.example.com'
});
</script>

<!-- 深层子组件 DeepChild.vue -->
<!--
<template>
  <div>
    <p>当前主题：{{ theme }}</p>
    <button @click="toggleTheme">切换主题</button>
  </div>
</template>

<script setup>
import { inject } from 'vue';

// 注入数据，第二个参数是默认值
const theme = inject('theme', 'light');
const toggleTheme = inject('toggleTheme', () => {});
const appConfig = inject('appConfig', {});
</script>
-->

<!-- ====== 3. v-model 组件双向绑定 ====== -->
<!-- 父组件使用v-model -->
<template>
  <CustomInput v-model="inputValue" />
  <CustomInput v-model:title="title" v-model:content="content" />
</template>

<script setup>
import { ref } from 'vue';
const inputValue = ref('');
const title = ref('');
const content = ref('');
</script>

<!-- CustomInput.vue -->
<!--
<template>
  <input
    :value="modelValue"
    @input="$emit('update:modelValue', $event.target.value)"
  >
</template>

<script setup>
defineProps(['modelValue']);
defineEmits(['update:modelValue']);
</script>
-->

<!-- ====== 通信方式对比 ====== -->
<!-- Props/Emits：父子组件，最常用 -->
<!-- Provide/Inject：跨层级，适合主题/配置 -->
<!-- v-model：双向绑定，表单组件 -->
<!-- Pinia：全局状态，跨组件共享 -->
<!-- 事件总线：不推荐，Vue 3已移除$on/$off -->
```

**💡 代码解释**：第3-29行：Props+Emits父子通信。第35-60行：Provide/Inject跨层级通信。第65-85行：v-model组件双向绑定。第90-95行：通信方式对比。

**🔑 关键要点**：
- Props向下，Emits向上
- Provide/Inject跨层级
- v-model是update:xxx语法糖
- 大型应用使用Pinia

---

### 3. Vue Router路由

> **级别**：中级 | **概念**：Vue Router是Vue官方路由库，支持动态路由、嵌套路由、路由守卫和懒加载，实现SPA页面导航。

```html
// ====== 路由配置：router/index.js ======
// import { createRouter, createWebHistory } from 'vue-router';

// // 路由懒加载：使用动态import
// const Home = () => import('../views/Home.vue');
// const About = () => import('../views/About.vue');
// const UserProfile = () => import('../views/UserProfile.vue');
// const NotFound = () => import('../views/NotFound.vue');

// // 路由配置数组
// const routes = [
//   {
//     path: '/',
//     name: 'home',
//     component: Home,
//     meta: { title: '首页', requiresAuth: false } // 路由元信息
//   },
//   {
//     path: '/about',
//     name: 'about',
//     component: About
//   },
//   {
//     // 动态路由：:id是参数占位符
//     path: '/users/:id',
//     name: 'user',
//     component: UserProfile,
//     // props: true 将路由参数作为props传递给组件
//     props: true
//   },
//   {
//     // 嵌套路由
//     path: '/dashboard',
//     component: () => import('../views/Dashboard.vue'),
//     children: [
//       {
//         path: '', // 默认子路由
//         component: () => import('../views/DashboardHome.vue')
//       },
//       {
//         path: 'stats',
//         component: () => import('../views/DashboardStats.vue')
//       },
//       {
//         path: 'settings',
//         component: () => import('../views/DashboardSettings.vue')
//       }
//     ]
//   },
//   {
//     // 404页面：匹配所有未定义的路径
//     path: '/:pathMatch(.*)*',
//     name: 'not-found',
//     component: NotFound
//   }
// ];

// // 创建路由实例
// const router = createRouter({
//   history: createWebHistory(), // HTML5 History模式
//   // history: createWebHashHistory(), // Hash模式（#）
//   routes,
//   // 滚动行为
//   scrollBehavior(to, from, savedPosition) {
//     if (savedPosition) {
//       return savedPosition; // 返回保存的位置
//     }
//     return { top: 0 }; // 滚动到顶部
//   }
// });

// ====== 路由守卫 ======
// 全局前置守卫
// router.beforeEach((to, from, next) => {
//   // 设置页面标题
//   document.title = to.meta.title || '默认标题';

//   // 检查是否需要登录
//   if (to.meta.requiresAuth && !isAuthenticated()) {
//     next({ name: 'login', query: { redirect: to.fullPath } });
//   } else {
//     next(); // 放行
//   }
// });

// 全局后置钩子
// router.afterEach((to, from) => {
//   console.log(`从 ${from.path} 导航到 ${to.path}`);
// });

// export default router;

// ====== 在组件中使用路由 ======
<template>
  <div>
    <nav>
      <!-- router-link：声明式导航 -->
      <router-link to="/">首页</router-link>
      <router-link to="/about">关于</router-link>
      <!-- 动态路由 -->
      <router-link :to="{ name: 'user', params: { id: 123 } }">
        用户123
      </router-link>
    </nav>

    <!-- router-view：路由出口，匹配的组件在此渲染 -->
    <router-view />
  </div>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router';

// useRouter：获取路由实例（用于编程式导航）
const router = useRouter();

// useRoute：获取当前路由信息（只读）
const route = useRoute();

// 获取动态路由参数
const userId = route.params.id; // 对应 /users/:id
const searchQuery = route.query.q; // 对应 ?q=xxx

// 编程式导航
const navigateToUser = (id) => {
  // 字符串路径
  router.push(`/users/${id}`);

  // 对象方式
  // router.push({ name: 'user', params: { id } });

  // 带查询参数
  // router.push({ path: '/search', query: { q: 'vue' } });

  // 替换当前历史记录（不产生新记录）
  // router.replace('/home');

  // 前进/后退
  // router.go(1);  // 前进
  // router.go(-1); // 后退
};
</script>

<!-- ====== 路由懒加载 ====== -->
<!-- 动态import()实现代码分割，按需加载路由组件 -->
<!-- const UserProfile = () => import('../views/UserProfile.vue'); -->

<!-- 使用webpack的魔法注释命名chunk -->
<!-- const UserProfile = () => import(/* webpackChunkName: "user" */ '../views/UserProfile.vue'); -->
```

**💡 代码解释**：第3-67行：路由配置（懒加载、动态路由、嵌套路由）。第71-87行：路由守卫。第91-120行：router-link和router-view使用。第124-142行：useRouter编程式导航。

**🔑 关键要点**：
- 动态import()实现路由懒加载
- :id动态路由参数
- router.beforeEach全局守卫
- useRouter/useRoute获取路由

---

### 4. 状态管理Pinia

> **级别**：中级 | **概念**：Pinia是Vue 3官方推荐的状态管理库，相比Vuex更简洁灵活，支持TypeScript、组合式API和模块化Store。

```html
// ====== 定义Store：stores/counter.js ======
// import { defineStore } from 'pinia';

// // 方式1：Options API风格
// export const useCounterStore = defineStore('counter', {
//   // state：返回初始状态的函数
//   state: () => ({
//     count: 0,
//     name: '计数器'
//   }),

//   // getters：计算属性，接收state
//   getters: {
//     doubleCount: (state) => state.count * 2,
//     // 使用其他getter
//     doublePlusOne(): number {
//       return this.doubleCount + 1;
//     }
//   },

//   // actions：修改状态的方法，支持异步
//   actions: {
//     increment() {
//       this.count++;
//     },
//     decrement() {
//       this.count--;
//     },
//     async fetchAndSet() {
//       // 异步操作
//       const data = await fetch('/api/count');
//       this.count = await data.json();
//     }
//   }
// });

// // 方式2：Composition API风格（推荐）
// export const useCounterStore = defineStore('counter', () => {
//   // state → ref()
//   const count = ref(0);
//   const name = ref('计数器');

//   // getters → computed()
//   const doubleCount = computed(() => count.value * 2);

//   // actions → 普通函数
//   function increment() {
//     count.value++;
//   }

//   async function fetchAndSet() {
//     const data = await fetch('/api/count');
//     count.value = await data.json();
//   }

//   // 返回暴露的状态和方法
//   return { count, name, doubleCount, increment, fetchAndSet };
// });

// ====== 在组件中使用Store ======
<template>
  <div>
    <h2>{{ counterStore.name }}</h2>
    <p>计数：{{ counterStore.count }}</p>
    <p>双倍：{{ counterStore.doubleCount }}</p>

    <button @click="counterStore.increment">+1</button>
    <button @click="counterStore.decrement">-1</button>

    <h2>用户信息</h2>
    <p>{{ userStore.name }} - {{ userStore.email }}</p>
    <button @click="updateUser">更新用户</button>
  </div>
</template>

<script setup>
import { useCounterStore } from '@/stores/counter';
import { useUserStore } from '@/stores/user';

// 在setup中调用useStore函数
const counterStore = useCounterStore();
const userStore = useUserStore();

// 使用storeToRefs保持响应式解构
import { storeToRefs } from 'pinia';
const { count, doubleCount } = storeToRefs(counterStore);
// 方法可以直接解构
const { increment } = counterStore;

const updateUser = () => {
  // 使用$patch批量更新
  userStore.$patch({
    name: '新名字',
    email: 'new@email.com'
  });

  // 或直接修改
  // userStore.name = '新名字';
  // userStore.email = 'new@email.com';
};
</script>

<!-- ====== stores/user.js ====== -->
<!--
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

export const useUserStore = defineStore('user', () => {
  const name = ref('张三');
  const email = ref('zhangsan@example.com');
  const isLoggedIn = ref(false);

  // getter
  const displayName = computed(() =>
    isLoggedIn.value ? name.value : '未登录'
  );

  // actions
  function login(userName, userEmail) {
    name.value = userName;
    email.value = userEmail;
    isLoggedIn.value = true;
  }

  function logout() {
    name.value = '';
    email.value = '';
    isLoggedIn.value = false;
  }

  return { name, email, isLoggedIn, displayName, login, logout };
});
-->

<!-- ====== Pinia vs Vuex ====== -->
<!-- Pinia优势： -->
<!-- 1. 更简洁的API，无需mutations -->
<!-- 2. 完整的TypeScript支持 -->
<!-- 3. 无需嵌套模块 -->
<!-- 4. 支持组合式API风格 -->
<!-- 5. 更好的DevTools支持 -->
```

**💡 代码解释**：第3-50行：defineStore两种风格定义Store。第54-79行：组件中使用Store。第83-112行：userStore示例。第116-121行：Pinia对比Vuex优势。

**🔑 关键要点**：
- defineStore定义Store
- Options API和Composition API两种风格
- storeToRefs保持响应式解构
- $patch批量更新

---

### 5. 插槽Slots

> **级别**：中级 | **概念**：插槽（Slots）让组件可以接收外部传入的模板内容，支持默认插槽、具名插槽和作用域插槽，实现灵活的组件内容分发。

```html
<!-- ====== 插槽类型 ====== -->

<!-- 1. 默认插槽：组件标签内的内容 -->
<!-- 子组件 Card.vue -->
<!--
<template>
  <div class="card">
    <div class="card-header">
      <slot name="header">默认标题</slot>
    </div>
    <div class="card-body">
      <slot>默认内容</slot>  <-- 默认插槽，name="default" -->
    </div>
    <div class="card-footer">
      <slot name="footer">
        <button>默认按钮</button>
      </slot>
    </div>
  </div>
</template>
-->

<!-- 父组件使用 -->
<template>
  <Card>
    <!-- 具名插槽：v-slot:插槽名 或 #插槽名 -->
    <template #header>
      <h2>自定义标题</h2>
    </template>

    <!-- 默认插槽：直接写内容 -->
    <p>这是卡片的主要内容</p>
    <p>可以包含多个元素</p>

    <!-- 具名插槽简写 -->
    <template #footer>
      <button>确定</button>
      <button>取消</button>
    </template>
  </Card>
</template>

<!-- ====== 2. 作用域插槽：子组件向父组件传数据 ====== -->
<!-- 子组件 List.vue -->
<!--
<template>
  <ul>
    <li v-for="item in items" :key="item.id">
      <-- 通过slot props传递数据给父组件 -->
      <slot name="item" :item="item" :index="index">
        {{ item.name }}  <-- 默认渲染 -->
      </slot>
    </li>
  </ul>
</template>

<script setup>
const items = [
  { id: 1, name: '项目A', status: 'active' },
  { id: 2, name: '项目B', status: 'inactive' },
  { id: 3, name: '项目C', status: 'active' }
];
</script>
-->

<!-- 父组件使用作用域插槽 -->
<template>
  <List>
    <!-- 接收子组件传递的数据 -->
    <template #item="{ item, index }">
      <!-- 自定义渲染每个列表项 -->
      <div class="custom-item">
        <span>{{ index + 1 }}.</span>
        <strong>{{ item.name }}</strong>
        <span :class="item.status">{{ item.status }}</span>
      </div>
    </template>
  </List>
</template>

<!-- ====== 3. 动态插槽名 ====== -->
<!-- 使用动态变量作为插槽名 -->
<template>
  <DataTable>
    <template #[dynamicSlotName]="{ data }">
      <span>{{ data }}</span>
    </template>
  </DataTable>
</template>

<script setup>
const dynamicSlotName = ref('header');
</script>

<!-- ====== 插槽使用场景 ====== -->
<!-- 1. 布局组件：Header/Content/Footer -->
<!-- 2. 列表组件：自定义列表项渲染 -->
<!-- 3. 弹窗组件：自定义标题和内容 -->
<!-- 4. 表格组件：自定义列渲染 -->
<!-- 5. 可复用组件：提供默认内容+自定义能力 -->
```

**💡 代码解释**：第3-21行：默认插槽和具名插槽。第25-59行：作用域插槽传递数据。第63-72行：动态插槽名。第77-81行：插槽使用场景。

**🔑 关键要点**：
- 默认插槽：<slot />
- 具名插槽：<slot name="xxx">
- 作用域插槽：<slot :data="xxx">
- v-slot简写为#

---

### 6. 异步组件与Teleport

> **级别**：中级 | **概念**：异步组件通过defineAsyncComponent实现按需加载，Teleport将组件内容渲染到DOM的任意位置，解决弹窗/模态框的z-index问题。

```html
<!-- ====== 异步组件 defineAsyncComponent ====== -->
<script setup>
import { defineAsyncComponent } from 'vue';

// 基础用法：传入返回Promise的工厂函数
const AsyncModal = defineAsyncComponent(() =>
  import('./components/HeavyModal.vue')
);

// 高级用法：配置加载状态
const AsyncChart = defineAsyncComponent({
  // 加载函数
  loader: () => import('./components/Chart.vue'),

  // 加载中显示的组件
  loadingComponent: () => import('./components/LoadingSpinner.vue'),
  // 加载组件的延迟时间（ms），超时后才显示loading
  delay: 200,

  // 加载失败显示的组件
  errorComponent: () => import('./components/ErrorDisplay.vue'),
  // 超时时间（ms），超时后显示error组件
  timeout: 10000
});
</script>

<template>
  <div>
    <!-- 使用异步组件，与普通组件一样 -->
    <Suspense>
      <!-- Suspense处理异步依赖 -->
      <template #default>
        <AsyncChart />
      </template>
      <template #fallback>
        <div>图表加载中...</div>
      </template>
    </Suspense>

    <button @click="showModal = true">打开弹窗</button>
    <AsyncModal v-if="showModal" @close="showModal = false" />
  </div>
</template>

<!-- ====== Teleport：传送门 ====== -->
<!-- 将组件内容渲染到指定DOM节点，解决弹窗层级问题 -->

<!-- 子组件 Modal.vue -->
<template>
  <!-- Teleport将内容传送到body下，避免受父组件z-index影响 -->
  <Teleport to="body">
    <!-- 遮罩层 -->
    <div class="modal-overlay" @click.self="$emit('close')">
      <!-- 弹窗内容 -->
      <div class="modal-content">
        <h2>{{ title }}</h2>
        <slot>默认内容</slot>
        <div class="modal-actions">
          <button @click="$emit('close')">取消</button>
          <button @click="$emit('confirm')">确定</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
defineProps({
  title: { type: String, default: '提示' }
});
defineEmits(['close', 'confirm']);
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999; /* 确保在最上层 */
}

.modal-content {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
  min-width: 300px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
}
</style>

<!-- ====== 使用Modal组件 ====== -->
<template>
  <div>
    <button @click="showModal = true">显示弹窗</button>

    <!-- 弹窗内容通过Teleport渲染到body下 -->
    <Modal
      v-if="showModal"
      title="确认删除"
      @close="showModal = false"
      @confirm="handleConfirm"
    >
      <p>确定要删除这条记录吗？此操作不可撤销。</p>
    </Modal>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const showModal = ref(false);

const handleConfirm = () => {
  console.log('确认删除');
  showModal.value = false;
};
</script>

<!-- ====== Suspense：异步依赖处理 ====== -->
<!-- 处理组件中的异步setup() -->
<!--
<Suspense>
  <template #default>
    <AsyncSetupComponent />
  </template>
  <template #fallback>
    <LoadingSpinner />
  </template>
</Suspense>
-->

<!-- ====== Teleport注意事项 ====== -->
<!-- 1. to属性必须是有效的CSS选择器 -->
<!-- 2. 目标元素必须在Teleport之前存在于DOM中 -->
<!-- 3. 多个Teleport可以传送到同一目标 -->
<!-- 4. Teleport内容仍是父组件的逻辑子组件 -->
<!-- 5. disabled属性可动态禁用Teleport -->
```

**💡 代码解释**：第3-28行：defineAsyncComponent异步组件。第32-51行：Suspense处理异步依赖。第57-98行：Teleport模态框组件。第102-128行：使用Modal组件。

**🔑 关键要点**：
- defineAsyncComponent异步加载
- Teleport传送到任意DOM节点
- Suspense处理异步组件
- to属性指定目标CSS选择器

---

### 7. 过渡与动画

> **级别**：中级 | **概念**：Vue 3的Transition和TransitionGroup组件提供声明式的进入/离开动画，配合CSS动画或JS钩子，实现流畅的UI过渡效果。

```html
<!-- ====== Transition 组件：单元素过渡 ====== -->
<template>
  <div>
    <button @click="show = !show">切换显示</button>

    <!-- Transition包裹需要动画的元素 -->
    <!-- name属性对应CSS类名前缀 -->
    <Transition name="fade">
      <p v-if="show">淡入淡出效果</p>
    </Transition>

    <!-- 自定义过渡类名 -->
    <Transition
      name="slide"
      enter-active-class="animate__animated animate__bounceIn"
      leave-active-class="animate__animated animate__bounceOut"
    >
      <p v-if="show">自定义动画类</p>
    </Transition>

    <!-- 模式切换：out-in先离开再进入 -->
    <Transition name="switch" mode="out-in">
      <p :key="currentView">{{ currentView }}</p>
    </Transition>
    <button @click="currentView = currentView === 'A' ? 'B' : 'A'">
      切换视图
    </button>
  </div>
</template>

<script setup>
import { ref } from 'vue';
const show = ref(true);
const currentView = ref('A');
</script>

<!-- ====== Transition CSS类名 ====== -->
<!--
.v-enter-from { opacity: 0; transform: translateX(-20px); }
.v-enter-active { transition: all 0.5s ease; }
.v-enter-to { opacity: 1; transform: translateX(0); }
.v-leave-from { opacity: 1; }
.v-leave-active { transition: all 0.3s ease; }
.v-leave-to { opacity: 0; transform: translateX(20px); }

用name替换v前缀：
.fade-enter-from { ... }
.fade-enter-active { ... }
-->

<!-- ====== TransitionGroup：列表过渡 ====== -->
<template>
  <div>
    <button @click="addItem">添加</button>
    <button @click="removeItem">删除</button>
    <button @click="shuffle">随机排列</button>

    <!-- TransitionGroup：列表动画 -->
    <TransitionGroup name="list" tag="ul">
      <li
        v-for="item in items"
        :key="item.id"
        class="list-item"
      >
        {{ item.text }}
        <button @click="removeItemById(item.id)">×</button>
      </li>
    </TransitionGroup>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const items = ref([
  { id: 1, text: '项目A' },
  { id: 2, text: '项目B' },
  { id: 3, text: '项目C' }
]);

let nextId = 4;
const addItem = () => {
  items.value.push({ id: nextId++, text: `项目${String.fromCharCode(64 + nextId - 1)}` });
};

const removeItem = () => items.value.pop();
const removeItemById = (id) => {
  items.value = items.value.filter(item => item.id !== id);
};

const shuffle = () => {
  items.value = items.value.sort(() => Math.random() - 0.5);
};
</script>

<!-- ====== TransitionGroup CSS ====== -->
<!--
.list-enter-active,
.list-leave-active {
  transition: all 0.5s ease;
}
.list-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}
.list-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
/* 确保离开元素占据空间，实现平滑过渡 */
.list-leave-active {
  position: absolute;
}
/* 移动动画：其他元素平滑移动 */
.list-move {
  transition: transform 0.5s ease;
}
-->

<!-- ====== JavaScript动画钩子 ====== -->
<!-- 使用JS钩子实现复杂动画（如配合GSAP库） -->
<!--
<Transition
  @before-enter="onBeforeEnter"
  @enter="onEnter"
  @after-enter="onAfterEnter"
  @before-leave="onBeforeLeave"
  @leave="onLeave"
  @after-leave="onAfterLeave"
>
  <p v-if="show">JS动画</p>
</Transition>

<script setup>
// 使用GSAP（需要安装）
// import gsap from 'gsap';
// const onEnter = (el, done) => {
//   gsap.from(el, { opacity: 0, x: -50, duration: 0.5, onComplete: done });
// };
// const onLeave = (el, done) => {
//   gsap.to(el, { opacity: 0, x: 50, duration: 0.3, onComplete: done });
// };
</script>
-->

<!-- ====== 过渡模式总结 ====== -->
<!-- Transition：单元素/组件进入离开 -->
<!-- TransitionGroup：列表项增删移动 -->
<!-- mode="out-in"：先离开后进入 -->
<!-- mode="in-out"：先进入后离开 -->
<!-- appear：页面初次渲染时也触发过渡 -->
<!-- :duration：控制过渡持续时间 -->
```

**💡 代码解释**：第3-28行：Transition组件基础用法。第33-45行：CSS过渡类名。第49-78行：TransitionGroup列表过渡。第82-95行：TransitionGroup CSS。第99-115行：JavaScript动画钩子。第119-124行：过渡模式总结。

**🔑 关键要点**：
- Transition包裹单元素动画
- TransitionGroup处理列表动画
- 6个CSS类名控制过渡阶段
- JS钩子配合GSAP等动画库

---

### 8. 模板引用与组合式API进阶

> **级别**：中级 | **概念**：模板引用（ref）获取DOM元素或组件实例，配合组合式API的watchEffect、toRef、自定义ref等高级特性，实现更灵活的响应式逻辑。

```html
<!-- ====== 模板引用：获取DOM元素 ====== -->
<template>
  <div>
    <!-- ref绑定到DOM元素，变量名与ref名一致 -->
    <input ref="inputRef" placeholder="自动聚焦" />
    <button @click="focusInput">聚焦输入框</button>

    <!-- 动态ref：v-for中使用 -->
    <ul>
      <li
        v-for="item in items"
        :key="item.id"
        :ref="(el) => setItemRef(el, item.id)"
      >
        {{ item.text }}
      </li>
    </ul>

    <!-- 组件ref：获取子组件实例 -->
    <ChildComp ref="childRef" />
    <button @click="callChildMethod">调用子组件方法</button>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';

// 模板引用：变量名必须与ref属性一致
const inputRef = ref(null);

// 挂载后才能访问DOM
onMounted(() => {
  inputRef.value?.focus(); // 自动聚焦
});

const focusInput = () => {
  inputRef.value?.focus();
  console.log(inputRef.value?.value); // 获取输入值
};

// 动态ref：v-for中的元素引用
const itemRefs = ref({});
const setItemRef = (el, id) => {
  if (el) {
    itemRefs.value[id] = el;
  }
};

const items = ref([
  { id: 1, text: '项目A' },
  { id: 2, text: '项目B' },
  { id: 3, text: '项目C' }
]);

// 组件引用
const childRef = ref(null);
const callChildMethod = () => {
  // 调用子组件暴露的方法
  childRef.value?.exposedMethod();
};
</script>

<!-- ====== watchEffect：自动追踪依赖 ====== -->
<script setup>
import { ref, watchEffect } from 'vue';

const searchQuery = ref('');
const searchResults = ref([]);
const loading = ref(false);

// watchEffect自动追踪回调中使用的响应式数据
// 不需要手动指定依赖，立即执行一次
watchEffect(async () => {
  // 如果searchQuery为空，清空结果
  if (!searchQuery.value.trim()) {
    searchResults.value = [];
    return;
  }

  loading.value = true;
  // 模拟API搜索
  await new Promise(r => setTimeout(r, 300));
  searchResults.value = [`结果: ${searchQuery.value}`];
  loading.value = false;
});
</script>

<!-- ====== toRef / toRefs：解构响应式 ====== -->
<script setup>
import { reactive, toRef, toRefs, ref } from 'vue';

// reactive对象解构会丢失响应式
const state = reactive({
  name: '张三',
  age: 25,
  email: 'test@test.com'
});

// ❌ 直接解构丢失响应式
// const { name, age } = state;

// ✅ toRefs：转为ref对象，保持响应式
const { name, age, email } = toRefs(state);
// name.value、age.value、email.value都是响应式的

// ✅ toRef：单个属性转ref
const nameRef = toRef(state, 'name');
// nameRef.value与state.name保持同步

// ====== triggerRef：手动触发更新 ======
import { shallowRef, triggerRef } from 'vue';

const shallowData = shallowRef({ count: 0 });

// 修改内部属性不会触发更新
shallowData.value.count = 1;

// 手动触发更新
triggerRef(shallowData);

// ====== 自定义ref ======
// 创建带逻辑的ref（如防抖ref）
function useDebouncedRef(value, delay = 300) {
  let timeout;
  const debouncedValue = ref(value);

  return {
    get value() {
      return debouncedValue.value;
    },
    set value(newValue) {
      // 清除之前的定时器
      clearTimeout(timeout);
      // 延迟更新值
      timeout = setTimeout(() => {
        debouncedValue.value = newValue;
      }, delay);
    }
  };
}

// 使用自定义ref
const searchText = useDebouncedRef('', 500);
// searchText.value = 'hello'; // 500ms后才更新
</script>

<!-- ====== 组合式API进阶总结 ====== -->
<!-- 1. 模板引用：ref获取DOM/组件实例 -->
<!-- 2. watchEffect：自动追踪，立即执行 -->
<!-- 3. toRef/toRefs：解构保持响应式 -->
<!-- 4. triggerRef：手动触发shallowRef更新 -->
<!-- 5. 自定义ref：封装带逻辑的响应式 -->
<!-- 6. effectScope：管理多个effect的生命周期 -->
```

**💡 代码解释**：第3-30行：模板引用获取DOM元素。第33-53行：onMounted后访问ref。第57-82行：watchEffect自动追踪。第86-105行：toRef/toRefs解构保持响应式。第109-115行：triggerRef手动触发。第119-143行：自定义ref防抖示例。

**🔑 关键要点**：
- ref属性获取DOM/组件引用
- watchEffect自动追踪依赖
- toRefs解构保持响应式
- 自定义ref封装响应式逻辑

---

## 高级精通

### 1. 渲染函数与JSX

> **级别**：高级 | **概念**：渲染函数h()是Vue模板的底层实现，通过编程方式创建虚拟DOM，适合需要动态生成组件的复杂场景。

```html
<!-- ====== render函数：h() ====== -->
<script>
import { h } from 'vue';

// h()函数创建虚拟DOM节点
// h(标签名/组件, props/attrs, children)
export default {
  props: {
    level: { type: Number, default: 1 },
    text: { type: String, required: true }
  },
  render() {
    // 动态生成h1-h6标题
    return h(
      `h${this.level}`,  // 标签名：动态的h1/h2/...
      { class: 'dynamic-heading' }, // props
      this.text  // 子节点内容
    );
  }
};
</script>

<!-- ====== 使用JSX（需要配置@vitejs/plugin-vue-jsx） ====== -->
<script setup lang="jsx">
import { ref } from 'vue';

// JSX中渲染Vue组件
const items = ref([
  { id: 1, text: '项目A', done: false },
  { id: 2, text: '项目B', done: true }
]);

// 使用JSX定义渲染函数
const renderList = () => (
  <ul class="todo-list">
    {items.value.map(item => (
      <li
        key={item.id}
        class={{ done: item.done }}
        onClick={() => toggleItem(item.id)}
      >
        {item.text}
      </li>
    ))}
  </ul>
);

const toggleItem = (id) => {
  const item = items.value.find(i => i.id === id);
  if (item) item.done = !item.done;
};
</script>

<!-- ====== 渲染函数应用：动态表格组件 ====== -->
<script>
import { h } from 'vue';

export default {
  props: {
    columns: Array, // [{ key: 'name', title: '姓名' }]
    data: Array     // [{ name: '张三', age: 25 }]
  },
  render() {
    // 渲染表头
    const headers = this.columns.map(col =>
      h('th', { key: col.key }, col.title)
    );

    // 渲染数据行
    const rows = this.data.map(row =>
      h('tr', { key: row.id },
        this.columns.map(col =>
          h('td', { key: col.key }, row[col.key])
        )
      )
    );

    // 组装表格
    return h('table', { class: 'data-table' }, [
      h('thead', null, [h('tr', null, headers)]),
      h('tbody', null, rows)
    ]);
  }
};
</script>

<!-- ====== 渲染函数应用：递归组件 ====== -->
<script>
import { h } from 'vue';

// 递归渲染树形结构
export default {
  name: 'TreeNode',
  props: {
    node: Object // { label: '根节点', children: [...] }
  },
  render() {
    const children = this.node.children
      ? this.node.children.map(child =>
          h('TreeNode', { key: child.label, node: child })
        )
      : [];

    return h('div', { class: 'tree-node' }, [
      h('div', { class: 'node-label' }, this.node.label),
      children.length > 0
        ? h('div', { class: 'node-children' }, children)
        : null
    ]);
  }
};
</script>

<!-- ====== 模板 vs 渲染函数 ====== -->
<!-- 模板：声明式，易于阅读，适合大多数场景 -->
<!-- 渲染函数：编程式，更灵活，适合动态组件 -->
<!-- JSX：模板和渲染函数的折中方案 -->

<!-- 使用场景： -->
<!-- 1. 动态标签名/组件 -->
<!-- 2. 高度动态的内容 -->
<!-- 3. 递归组件 -->
<!-- 4. 需要完整的JavaScript能力 -->
```

**💡 代码解释**：第3-17行：h()渲染函数基础。第21-45行：JSX渲染。第49-72行：动态表格组件。第76-100行：递归树组件。第104-112行：使用场景总结。

**🔑 关键要点**：
- h()创建虚拟DOM节点
- h(标签, props, children)
- JSX需要额外插件
- 递归组件中自引用

---

### 2. 自定义指令

> **级别**：高级 | **概念**：自定义指令通过操作底层DOM实现可复用的行为，如自动聚焦、权限控制、懒加载等，是对Vue内置指令的扩展。

```html
<!-- ====== 自定义指令 ====== -->

<!-- 1. 局部注册：在组件中定义 -->
<script setup>
// 指令命名：v-focus，定义时用focus
// 模板中使用：<input v-focus>
const vFocus = {
  // mounted：元素挂载到DOM时调用
  mounted(el) {
    // el是绑定的DOM元素
    el.focus(); // 自动聚焦
  }
};
</script>

<!-- 2. 全局注册：在main.js中 -->
<!--
// main.js
import { createApp } from 'vue';
const app = createApp(App);

// 全局指令：v-highlight
app.directive('highlight', {
  mounted(el, binding) {
    // binding.value：指令的值
    // binding.arg：指令的参数（v-highlight:xxx）
    // binding.modifiers：修饰符对象
    el.style.backgroundColor = binding.value || 'yellow';
  },
  updated(el, binding) {
    // 值更新时也更新样式
    el.style.backgroundColor = binding.value || 'yellow';
  }
});
-->

<!-- 使用 -->
<template>
  <div>
    <!-- 自动聚焦 -->
    <input v-focus placeholder="自动聚焦">

    <!-- 高亮：传值 -->
    <p v-highlight="'#ffeb3b'">这段文字高亮显示</p>
    <p v-highlight="activeColor">动态颜色</p>

    <!-- 带参数 -->
    <p v-highlight:background="'#e3f2fd'">参数用法</p>

    <!-- 权限控制指令 -->
    <button v-permission="'admin'">管理员按钮</button>
    <button v-permission="'user'">普通用户按钮</button>

    <!-- 懒加载指令 -->
    <img v-lazy="imageUrl" alt="懒加载图片">
  </div>
</template>

<script setup>
import { ref } from 'vue';

const activeColor = ref('#c8e6c9');
const imageUrl = ref('https://example.com/image.jpg');
</script>

<!-- ====== 自定义指令示例：v-permission ====== -->
<!--
// directives/permission.js
export default {
  mounted(el, binding) {
    // 获取当前用户权限
    const userPermission = getUserPermission(); // 假设获取用户权限
    const requiredPermission = binding.value; // 指令值

    // 如果没有权限，移除元素
    if (!userPermission.includes(requiredPermission)) {
      el.parentNode?.removeChild(el);
    }
  }
};
-->

<!-- ====== 自定义指令示例：v-lazy ====== -->
<!--
// directives/lazy.js
export default {
  mounted(el, binding) {
    // 使用IntersectionObserver实现懒加载
    const observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          // 元素进入视口时加载图片
          el.src = binding.value;
          observer.unobserve(el); // 停止观察
        }
      });
    });

    // 设置占位图
    el.src = 'placeholder.jpg';
    observer.observe(el); // 开始观察
  }
};
-->

<!-- ====== 指令的简化写法 ====== -->
<!-- 如果只需要mounted和updated，可以简写为函数 -->
<!--
app.directive('color', (el, binding) => {
  el.style.color = binding.value;
});
-->

<!-- ====== 指令生命周期钩子 ====== -->
<!-- created：元素属性/事件监听器应用前 -->
<!-- beforeMount：元素挂载前 -->
<!-- mounted：元素挂载后 -->
<!-- beforeUpdate：元素更新前 -->
<!-- updated：元素更新后 -->
<!-- beforeUnmount：元素卸载前 -->
<!-- unmounted：元素卸载后 -->
```

**💡 代码解释**：第5-15行：局部注册v-focus。第19-37行：全局注册v-highlight。第42-56行：模板中使用指令。第60-72行：v-permission权限指令。第76-93行：v-lazy懒加载指令。

**🔑 关键要点**：
- 局部注册：vXxx对象
- 全局注册：app.directive()
- binding.value获取指令值
- 利用生命周期钩子操作DOM

---

### 3. 性能优化

> **级别**：高级 | **概念**：Vue 3性能优化包括虚拟列表、组件懒加载、v-memo缓存、shallowRef浅响应和合理的计算属性使用，全面提升应用性能。

```html
// ====== 1. v-memo：缓存子树 ======
// Vue 3.2+ 新增，类似React.memo，缓存模板子树
<template>
  <div>
    <!-- 仅当item.id或selected变化时才重新渲染 -->
    <div v-for="item in list" :key="item.id" v-memo="[item.id, selected === item.id]">
      <p>{{ item.name }}</p>
      <p>{{ item.description }}</p>
      <!-- 如果memo条件不变，即使父组件渲染，这里也不会更新 -->
    </div>
  </div>
</template>

// ====== 2. shallowRef：浅层响应式 ======
// 只有.value的变化触发更新，内部属性变化不触发
import { shallowRef } from 'vue';

const state = shallowRef({ count: 0, nested: { value: 1 } });

// ✅ 触发更新
state.value = { count: 1, nested: { value: 2 } };

// ❌ 不触发更新
state.value.count = 1;
state.value.nested.value = 2;

// 手动触发更新
state.value = { ...state.value, count: 1 };

// 适用场景：大型数据对象，只有顶层引用会变化

// ====== 3. 虚拟列表：处理大量数据 ======
// 使用vue-virtual-scroller等库
// npm install vue-virtual-scroller
/*
<template>
  <RecycleScroller
    :items="largeList"
    :item-size="50"
    key-field="id"
    v-slot="{ item }"
  >
    <div class="item">{{ item.name }}</div>
  </RecycleScroller>
</template>
*/

// ====== 4. 异步组件 + 代码分割 ======
import { defineAsyncComponent } from 'vue';

// 路由级别代码分割
const routes = [
  {
    path: '/dashboard',
    component: () => import('./views/Dashboard.vue')
  }
];

// 组件级别代码分割
const HeavyChart = defineAsyncComponent(() =>
  import('./components/HeavyChart.vue')
);

// ====== 5. KeepAlive：缓存组件状态 ======
<template>
  <!-- 切换tab时保留组件状态，避免重新渲染 -->
  <KeepAlive :max="10">
    <!-- max限制最大缓存数量 -->
    <component :is="activeComponent" />
  </KeepAlive>
</template>

// KeepAlive配合include/exclude
// <KeepAlive include="UserList,UserProfile">
//   <component :is="view" />
// </KeepAlive>

// ====== 6. 合理使用computed ======
// ✅ 推荐：computed缓存派生数据
const filteredList = computed(() => {
  return items.value.filter(item => item.active);
});

// ❌ 避免：methods中做过滤（每次渲染都执行）
// function getFilteredList() {
//   return items.value.filter(item => item.active);
// }

// ====== 7. 避免不必要的响应式 ======
// 对于不需要响应式的大型数据，使用shallowRef或markRaw
import { markRaw } from 'vue';

const externalLibrary = markRaw(new SomeExternalLib());
// markRaw标记对象，使其永远不会被转为响应式

// ====== 8. 事件监听优化 ======
// 使用事件委托，减少监听器数量
<template>
  <ul @click="handleClick">
    <!-- 一个监听器处理所有li的点击 -->
    <li v-for="item in items" :key="item.id" :data-id="item.id">
      {{ item.name }}
    </li>
  </ul>
</template>

<script setup>
const handleClick = (event) => {
  const li = event.target.closest('li');
  if (li) {
    const id = li.dataset.id;
    console.log('点击了:', id);
  }
};
</script>

// ====== 性能优化检查清单 ======
console.log('Vue 3 性能优化检查清单：');
console.log('1. 大列表 → 虚拟滚动');
console.log('2. 路由/组件 → 异步加载');
console.log('3. 频繁切换 → KeepAlive');
console.log('4. 静态子树 → v-memo');
console.log('5. 大数据 → shallowRef');
console.log('6. 派生数据 → computed');
console.log('7. 第三方库 → markRaw');
console.log('8. 大量事件 → 事件委托');
console.log('9. 使用Vue DevTools性能分析');
```

**💡 代码解释**：第3-14行：v-memo缓存子树。第18-30行：shallowRef浅响应式。第34-47行：虚拟列表。第51-63行：异步组件和代码分割。第67-76行：KeepAlive缓存。第80-96行：markRaw和事件委托。

**🔑 关键要点**：
- v-memo缓存子树
- shallowRef浅响应式
- KeepAlive缓存组件
- markRaw标记非响应式对象

---

### 4. Vue 3响应式原理

> **级别**：高级 | **概念**：Vue 3使用Proxy替代Vue 2的Object.defineProperty实现响应式，通过effect追踪依赖和trigger触发更新，性能更好且支持更多数据类型。

```html
// ====== Vue 3 响应式系统核心原理 ======
// 基于 Proxy 和 Reflect 的响应式系统

// 1. 依赖收集：track函数
// 存储依赖关系：targetMap → depsMap → dep(Set)
const targetMap = new WeakMap(); // 全局依赖映射表

let activeEffect = null; // 当前活跃的effect

// track：追踪依赖，将effect添加到依赖集合
function track(target, key) {
  if (!activeEffect) return; // 没有活跃effect则跳过

  // 获取或创建target的依赖映射
  let depsMap = targetMap.get(target);
  if (!depsMap) {
    targetMap.set(target, (depsMap = new Map()));
  }

  // 获取或创建key的依赖集合
  let dep = depsMap.get(key);
  if (!dep) {
    depsMap.set(key, (dep = new Set()));
  }

  // 将当前effect添加到依赖集合
  dep.add(activeEffect);
}

// 2. 触发更新：trigger函数
function trigger(target, key) {
  const depsMap = targetMap.get(target);
  if (!depsMap) return;

  const dep = depsMap.get(key);
  if (dep) {
    // 执行所有依赖该key的effect
    dep.forEach(effect => effect());
  }
}

// 3. reactive：使用Proxy创建响应式对象
function reactive(target) {
  // 只处理对象类型
  if (typeof target !== 'object' || target === null) {
    return target;
  }

  return new Proxy(target, {
    // get陷阱：读取属性时追踪依赖
    get(obj, key, receiver) {
      const result = Reflect.get(obj, key, receiver);
      // 追踪依赖：记录谁在使用这个属性
      track(obj, key);
      // 深层响应式：嵌套对象也转为reactive
      return typeof result === 'object' && result !== null
        ? reactive(result)
        : result;
    },

    // set陷阱：设置属性时触发更新
    set(obj, key, value, receiver) {
      const oldValue = obj[key];
      const result = Reflect.set(obj, key, value, receiver);
      // 值变化时触发更新
      if (oldValue !== value) {
        trigger(obj, key);
      }
      return result;
    },

    // deleteProperty陷阱：删除属性时触发更新
    deleteProperty(obj, key) {
      const hadKey = Object.prototype.hasOwnProperty.call(obj, key);
      const result = Reflect.deleteProperty(obj, key);
      if (hadKey) {
        trigger(obj, key);
      }
      return result;
    }
  });
}

// 4. ref：基本类型的响应式包装
function ref(value) {
  // 创建包含value属性的响应式对象
  const refObject = {
    get value() {
      // 读取时追踪依赖
      track(refObject, 'value');
      return value;
    },
    set value(newValue) {
      if (newValue !== value) {
        value = newValue;
        // 设置时触发更新
        trigger(refObject, 'value');
      }
    }
  };
  return refObject;
}

// 5. effect：创建副作用函数
function effect(fn) {
  // 包装函数，设置activeEffect
  const effectFn = () => {
    activeEffect = effectFn;
    fn(); // 执行副作用函数，触发track
    activeEffect = null;
  };
  effectFn(); // 立即执行一次
  return effectFn;
}

// ====== 使用示例 ======
// 创建响应式对象
const state = reactive({ count: 0, name: 'Vue' });

// 创建副作用：自动追踪依赖
// effect(() => {
//   console.log(`count: ${state.count}, name: ${state.name}`);
// });

// 修改数据，自动触发更新
// state.count++; // 输出：count: 1, name: Vue
// state.name = 'Vue 3'; // 输出：count: 1, name: Vue 3

// ====== Vue 3 vs Vue 2 响应式对比 ======
console.log('Vue 3 响应式优势：');
console.log('1. Proxy可以拦截更多操作（delete、has等）');
console.log('2. 支持数组索引和length变化检测');
console.log('3. 支持Map、Set、WeakMap、WeakSet');
console.log('4. 不需要递归遍历，性能更好');
console.log('5. 支持动态属性添加/删除');

// Vue 2 限制（Object.defineProperty）：
// 1. 无法检测属性添加/删除 → 需要Vue.set/Vue.delete
// 2. 无法检测数组索引变化 → 需要Vue.set或splice
// 3. 无法检测数组length变化
// 4. 需要递归遍历所有属性（初始化性能差）
```

**💡 代码解释**：第3-24行：track依赖收集。第27-36行：trigger触发更新。第40-76行：reactive基于Proxy实现。第80-96行：ref实现。第100-112行：effect副作用函数。第128-140行：Vue 3 vs Vue 2对比。

**🔑 关键要点**：
- Proxy拦截get/set/deleteProperty
- track收集依赖，trigger触发更新
- ref内部也是响应式对象
- effect自动追踪依赖

---

### 5. SSR服务端渲染

> **级别**：高级 | **概念**：Vue 3支持SSR（服务端渲染），通过Nuxt 3或手动配置实现SEO优化、首屏性能提升，支持静态生成和混合渲染模式。

```html
// ====== Vue 3 SSR 核心概念 ======
// SSR在服务端将Vue组件渲染为HTML字符串，客户端进行水合

// ====== 1. 基础SSR实现（手动） ======
// server.js
// import { createSSRApp } from 'vue';
// import { renderToString } from 'vue/server-renderer';
// import express from 'express';
//
// const app = express();
//
// app.get('*', async (req, res) => {
//   // 创建SSR应用实例
//   const vueApp = createSSRApp({
//     template: `<div>Hello SSR！当前路径：${req.url}</div>`
//   });
//
//   // 渲染为HTML字符串
//   const html = await renderToString(vueApp);
//
//   // 返回完整HTML
//   res.send(`
//     <!DOCTYPE html>
//     <html>
//       <head><title>SSR示例</title></head>
//       <body>
//         <div id="app">${html}</div>
//         <script src="/client.js"></script>
//       </body>
//     </html>
//   `);
// });

// ====== 2. Nuxt 3 SSR（推荐方案） ======
// Nuxt 3是基于Vue 3的全栈框架，内置SSR支持

// 页面组件：pages/index.vue
// <template>
//   <div>
//     <h1>{{ title }}</h1>
//     <ul>
//       <li v-for="post in posts" :key="post.id">
//         {{ post.title }}
//       </li>
//     </ul>
//   </div>
// </template>
//
// <script setup>
// // useAsyncData：在服务端获取数据
// // 数据在服务端预取，客户端水合时复用
// const { data: posts } = await useAsyncData('posts', () =>
//   $fetch('https://api.example.com/posts')
// );
//
// const title = ref('Nuxt 3 SSR 示例');
// </script>

// ====== 3. Nuxt 3 渲染模式 ======
// 三种渲染模式：
// - SSR（默认）：每次请求服务端渲染
// - SSG（静态生成）：构建时生成静态HTML
// - CSR（客户端渲染）：仅客户端渲染

// 指定渲染模式：nuxt.config.ts
// export default defineNuxtConfig({
//   // 全局SSR
//   ssr: true,

//   // 路由规则：混合渲染
//   routeRules: {
//     // 首页静态生成
//     '/': { prerender: true },
//     // 博客文章SSR（动态内容）
//     '/blog/**': { ssr: true },
//     // 管理后台CSR（交互密集）
//     '/admin/**': { ssr: false },
//     // ISR：增量静态再生（定时更新）
//     '/products/**': { isr: 3600 }
//   }
// });

// ====== 4. 同构代码注意事项 ======
// 服务端无法访问浏览器API
// 需要做平台判断
// if (import.meta.client) {
//   // 仅在客户端执行
//   window.addEventListener('scroll', handleScroll);
//   localStorage.setItem('key', 'value');
// }

// 使用ClientOnly组件包裹仅客户端渲染的内容
// <ClientOnly>
//   <UserProfile />
//   <template #fallback>
//     <div>加载中...</div>
//   </template>
// </ClientOnly>

// ====== 5. SEO优化 ======
// Nuxt 3 内置SEO功能
// useHead({
//   title: '页面标题',
//   meta: [
//     { name: 'description', content: '页面描述' },
//     { name: 'keywords', content: '关键词1,关键词2' },
//     { property: 'og:title', content: 'Open Graph标题' },
//     { property: 'og:image', content: 'https://example.com/image.jpg' }
//   ],
//   link: [
//     { rel: 'canonical', href: 'https://example.com' }
//   ]
// });

// 动态生成sitemap
// server/sitemap.xml.ts
// export default defineEventHandler(() => {
//   const posts = getPosts();
//   const urls = posts.map(p =>
//     `<url><loc>https://example.com/posts/${p.slug}</loc></url>`
//   );
//   return `<?xml version="1.0" encoding="UTF-8"?>
//     <urlset>${urls.join('')}</urlset>`;
// });

// ====== SSR vs SPA 对比 ======
console.log('SSR vs SPA 对比：');
console.log('SSR优势：');
console.log('1. 首屏加载快（服务端直出HTML）');
console.log('2. SEO友好（搜索引擎可爬取内容）');
console.log('3. 更好的社交分享体验（OG标签）');
console.log('SSR劣势：');
console.log('1. 服务器压力大（每次请求需渲染）');
console.log('2. 开发复杂度更高');
console.log('3. 需要注意同构兼容性');
console.log('推荐：内容型网站用SSR，后台管理用SPA');
```

**💡 代码解释**：第3-24行：手动SSR实现。第28-55行：Nuxt 3 SSR和useAsyncData。第59-80行：三种渲染模式和路由规则。第84-94行：同构代码注意事项。第98-120行：SEO优化和sitemap。第124-133行：SSR vs SPA对比。

**🔑 关键要点**：
- Nuxt 3是Vue SSR推荐方案
- useAsyncData服务端预取数据
- 混合渲染：SSR/SSG/CSR/ISR
- 同构代码注意平台判断

---

### 6. Vue 3新特性与测试

> **级别**：高级 | **概念**：Vue 3.3+引入泛型组件、defineOptions、defineSlots等新特性，搭配Vitest和Vue Test Utils实现完善的单元测试和组件测试。

```html
// ====== Vue 3.3+ 新特性 ======

// 1. 泛型组件（Generic Components）
// <script setup lang="ts" generic="T">
// // 泛型参数声明：generic="T"
// const props = defineProps<{
//   items: T[];            // 泛型列表
//   selected: T | null;    // 泛型选中项
// }>();
//
// const emit = defineEmits<{
//   select: [item: T];     // 泛型事件参数
// }>();
//
// // 在模板中使用
// // <template>
// //   <ul>
// //     <li v-for="item in items" :key="item.id" @click="emit('select', item)">
// //       {{ item }}
// //     </li>
// //   </ul>
// // </template>
// </script>

// 2. defineOptions：声明组件选项
// <script setup>
// defineOptions({
//   name: 'MyComponent',      // 组件名
//   inheritAttrs: false,      // 不继承attrs
//   // 其他Options API选项
// });
// </script>

// 3. defineSlots：声明插槽类型
// <script setup lang="ts">
// defineSlots<{
//   // 默认插槽
//   default: (props: { msg: string }) => any;
//   // 具名插槽
//   header: (props: { title: string }) => any;
//   footer: (props: {}) => any;
// }>();
// </script>

// 4. defineModel：简化v-model
// <script setup>
// // 替代传统的props + emit('update:xxx')
// const modelValue = defineModel();
// const count = defineModel('count', { type: Number, default: 0 });
// // 直接修改：count.value++
// </script>

// 5. v-bind同名的简写
// <template>
//   <!-- 等价于 :id="id" -->
//   <div :id></div>
//   <!-- 等价于 v-bind:id="id" -->
// </template>

// ====== Vue组件测试（Vitest + Vue Test Utils） ======
// 安装：npm install -D vitest @vue/test-utils jsdom

// 1. 基础组件测试
// Counter.spec.ts
// import { mount } from '@vue/test-utils';
// import { describe, it, expect } from 'vitest';
// import Counter from './Counter.vue';
//
// describe('Counter.vue', () => {
//   it('渲染初始计数', () => {
//     const wrapper = mount(Counter, {
//       props: { initialCount: 5 }
//     });
//     // 断言文本内容
//     expect(wrapper.text()).toContain('5');
//   });
//
//   it('点击按钮增加计数', async () => {
//     const wrapper = mount(Counter);
//     // 查找按钮并点击
//     await wrapper.find('button').trigger('click');
//     // 断言计数增加
//     expect(wrapper.text()).toContain('1');
//   });
//
//   it('触发emit事件', async () => {
//     const wrapper = mount(Counter);
//     await wrapper.find('button').trigger('click');
//     // 断言触发了事件
//     expect(wrapper.emitted()).toHaveProperty('update');
//     expect(wrapper.emitted('update')[0]).toEqual([1]);
//   });
// });

// 2. 测试composables
// useCounter.spec.ts
// import { describe, it, expect } from 'vitest';
// import { useCounter } from './useCounter';
//
// describe('useCounter', () => {
//   it('初始化和递增', () => {
//     const { count, increment } = useCounter();
//     expect(count.value).toBe(0);
//     increment();
//     expect(count.value).toBe(1);
//   });
// });

// 3. 测试异步组件
// AsyncComponent.spec.ts
// import { mount, flushPromises } from '@vue/test-utils';
//
// it('异步加载数据', async () => {
//   const wrapper = mount(AsyncComponent);
//   // 等待所有Promise完成
//   await flushPromises();
//   expect(wrapper.text()).toContain('加载完成');
// });

// 4. 测试Pinia Store
// import { setActivePinia, createPinia } from 'pinia';
//
// describe('Counter Store', () => {
//   beforeEach(() => {
//     setActivePinia(createPinia());
//   });
//
//   it('increment', () => {
//     const store = useCounterStore();
//     expect(store.count).toBe(0);
//     store.increment();
//     expect(store.count).toBe(1);
//   });
// });

// 5. 测试覆盖率
// vitest.config.ts
// export default {
//   test: {
//     coverage: {
//       provider: 'v8',
//       reporter: ['text', 'html'],
//       include: ['src/**/*.{ts,vue}']
//     }
//   }
// };

// ====== 测试最佳实践 ======
console.log('Vue测试最佳实践：');
console.log('1. 组件：测试渲染、交互、事件');
console.log('2. Composables：独立测试逻辑函数');
console.log('3. Store：测试状态变化和actions');
console.log('4. 路由：使用mock路由测试导航');
console.log('5. 快照：用于UI回归测试');
console.log('6. 覆盖率目标：80%以上');
console.log('测试工具链：Vitest + Vue Test Utils + jsdom');
```

**💡 代码解释**：第3-47行：Vue 3.3+新特性（泛型组件、defineOptions、defineSlots、defineModel）。第51-100行：组件测试基本模式。第104-116行：Composables和异步测试。第120-140行：Pinia Store测试和覆盖率。第144-150行：测试最佳实践。

**🔑 关键要点**：
- 泛型组件generic="T"声明
- defineModel简化v-model
- Vitest + Vue Test Utils测试组件
- 80%+测试覆盖率目标

---
