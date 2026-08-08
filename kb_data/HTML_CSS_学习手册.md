# 🎨 HTML/CSS 编程语言学习手册

> **分类**：前端  
> **描述**：HTML定义网页结构，CSS负责样式美化，两者是前端开发的基石  
> **生成时间**：2026-08-07  

---

## 目录

- [初级入门](#primary)（9个知识点）
- [中级进阶](#intermediate)（8个知识点）
- [高级精通](#advanced)（6个知识点）

---

## 初级入门

### 1. HTML基础结构

> **级别**：初级 | **概念**：HTML文档由DOCTYPE声明、html根元素、head头部和body主体组成，是每个网页的骨架。

```htmlcss
<!DOCTYPE html>
<!-- 声明文档类型为HTML5，确保浏览器以标准模式渲染 -->
<html lang="zh-CN">
<!-- lang属性指定页面语言为中文，帮助搜索引擎和屏幕阅读器理解 -->
<head>
  <!-- head标签包含页面的元数据，不会显示在页面上 -->
  <meta charset="UTF-8">
  <!-- 设置字符编码为UTF-8，支持中文和全球多语言字符 -->
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <!-- viewport让页面在移动端自适应，initial-scale=1.0防止缩放 -->
  <title>我的第一个网页</title>
  <!-- title标签定义浏览器标签页上显示的标题 -->
</head>
<body>
  <!-- body标签包含所有用户可见的页面内容 -->
  <h1>欢迎来到前端世界！</h1>
  <!-- h1是最重要的标题标签，一个页面建议只用一个h1 -->
  <p>这是HTML文档的基本结构示例。</p>
  <!-- p标签表示段落，自动在前后添加换行间距 -->
</body>
</html>
```

**💡 代码解释**：第1行DOCTYPE声明告诉浏览器使用HTML5标准。第2行html元素包裹整个页面。第4-9行head包含字符编码、视口设置和标题。第11-15行body包含实际显示的内容，h1是主标题，p是段落。

**🔑 关键要点**：
- DOCTYPE声明必须放在第一行
- lang属性提升无障碍访问
- viewport是移动端适配的关键
- title影响SEO排名

---

### 2. 常用标签详解

> **级别**：初级 | **概念**：HTML提供了丰富的语义化标签：div用于布局容器、p用于段落、a用于超链接、img用于图片、span用于行内文本。

```htmlcss
<body>
  <!-- div：块级容器，常用于布局分组，默认占满整行 -->
  <div class="container">
    <!-- a：超链接，href属性指定跳转地址 -->
    <a href="https://example.com" target="_blank" title="访问示例网站">
      <!-- target="_blank"在新标签页打开，title是鼠标悬停提示 -->
      点击跳转
    </a>

    <!-- img：图片标签，src指定图片路径，alt是加载失败时的替代文本 -->
    <img src="photo.jpg" alt="一张风景照片" width="300" height="200">
    <!-- width/height设置宽高，避免加载时页面抖动 -->

    <!-- span：行内容器，不换行，用于包裹小段文本 -->
    <p>这是一个<span style="color: red;">重要</span>的提示。</p>
    <!-- span配合CSS可以实现局部样式高亮 -->
  </div>
</body>
```

**💡 代码解释**：第2行div是块级容器，用于页面布局。第4-7行a标签创建超链接，href是目标地址，target="_blank"在新窗口打开。第10行img嵌入图片，src是图片路径，alt提供替代文本。第14行span是行内元素，不打断文本流，常用于局部样式。

**🔑 关键要点**：
- div是块级元素独占一行
- span是行内元素不换行
- a标签的target控制打开方式
- img必须有alt属性提升可访问性

---

### 3. 表格与表单

> **级别**：初级 | **概念**：table用于展示结构化数据，form用于收集用户输入并提交到服务器。

```htmlcss
<!-- ====== 表格示例 ====== -->
<!-- table：表格容器，border属性添加边框 -->
<table border="1">
  <!-- thead：表格头部，包含列标题 -->
  <thead>
    <tr>
      <!-- tr：表格行(table row) -->
      <th>姓名</th>
      <!-- th：表头单元格，默认加粗居中 -->
      <th>年龄</th>
      <th>城市</th>
    </tr>
  </thead>
  <!-- tbody：表格主体，包含数据行 -->
  <tbody>
    <tr>
      <td>张三</td>
      <!-- td：普通数据单元格 -->
      <td>25</td>
      <td>北京</td>
    </tr>
  </tbody>
</table>

<!-- ====== 表单示例 ====== -->
<!-- form：表单容器，action是提交地址，method是请求方式 -->
<form action="/submit" method="POST">
  <!-- label：关联表单控件，for属性指向input的id -->
  <label for="username">用户名：</label>
  <!-- input：输入框，type="text"为文本输入 -->
  <input type="text" id="username" name="username" placeholder="请输入用户名" required>
  <!-- placeholder是占位提示文字，required表示必填 -->

  <label for="password">密码：</label>
  <!-- type="password"时输入内容会显示为圆点 -->
  <input type="password" id="password" name="password" required>

  <!-- button：提交按钮，type="submit"触发表单提交 -->
  <button type="submit">登录</button>
</form>
```

**💡 代码解释**：表格部分：第3行table定义表格，第5行thead包含标题行，th是表头单元格。第13行tbody包含数据行，td是普通单元格。表单部分：第24行form定义表单，第26行label关联input提升点击区域，第28行input的type决定控件类型，required表示必填。

**🔑 关键要点**：
- table用于数据展示不是布局
- form的action和method控制提交行为
- label的for属性提升可用性
- input的type决定输入类型

---

### 4. CSS选择器

> **级别**：初级 | **概念**：CSS选择器用于选中HTML元素并应用样式，包括标签选择器、类选择器、ID选择器和组合选择器。

```htmlcss
/* ====== 标签选择器：选中所有同名标签 ====== */
/* 选中所有p标签，设置字体大小为16px */
p {
  font-size: 16px;      /* 字体大小 */
  line-height: 1.6;     /* 行高为字号的1.6倍，提升可读性 */
  color: #333;          /* 文字颜色为深灰色 */
}

/* ====== 类选择器：以.开头，选中class属性匹配的元素 ====== */
/* 选中所有class="highlight"的元素 */
.highlight {
  background-color: yellow;  /* 背景色黄色 */
  padding: 4px 8px;         /* 上下4px，左右8px的内边距 */
  border-radius: 4px;       /* 圆角边框4px */
}

/* ====== ID选择器：以#开头，选中唯一ID的元素 ====== */
/* 选中id="main-title"的元素，ID在页面中必须唯一 */
#main-title {
  font-size: 32px;      /* 大号字体 */
  text-align: center;   /* 文字居中 */
}

/* ====== 组合选择器：后代选择器（空格分隔） ====== */
/* 选中class="article"内部的所有p标签 */
.article p {
  text-indent: 2em;     /* 首行缩进2个字符宽度 */
  margin-bottom: 10px;  /* 段落下边距 */
}

/* ====== 伪类选择器：选中特定状态的元素 ====== */
/* 鼠标悬停在链接上时改变颜色 */
a:hover {
  color: orange;        /* 悬停时变为橙色 */
  text-decoration: underline;  /* 添加下划线 */
}
```

**💡 代码解释**：第2-6行：标签选择器直接使用标签名，选中所有p标签。第9-13行：类选择器以.开头，同一个class可应用于多个元素。第16-19行：ID选择器以#开头，ID在页面中唯一。第22-25行：后代选择器用空格表示层级关系。第28-31行：伪类选择器用冒号表示状态，hover是鼠标悬停。

**🔑 关键要点**：
- 类选择器可复用，ID选择器唯一
- 后代选择器用空格连接
- 伪类选择器以冒号开头
- 选择器优先级：ID>类>标签

---

### 5. 盒模型

> **级别**：初级 | **概念**：CSS盒模型是页面布局的核心，每个元素都是一个矩形盒子，由内容(content)、内边距(padding)、边框(border)、外边距(margin)组成。

```htmlcss
/* ====== 盒模型四层结构 ====== */
/* 从内到外：content → padding → border → margin */
.box {
  /* 内容区域：文字和子元素显示的地方 */
  width: 200px;              /* 内容宽度200px */
  height: 100px;             /* 内容高度100px */

  /* 内边距：内容与边框之间的空白区域 */
  padding: 20px;             /* 四边统一20px内边距 */
  /* 等价于 padding: 20px 20px 20px 20px; 上右下左 */

  /* 边框：围绕内边距和内容的线条 */
  border: 2px solid #3498db; /* 2px宽、实线、蓝色边框 */

  /* 外边距：元素与相邻元素之间的间距 */
  margin: 15px;              /* 四边统一15px外边距 */

  /* 背景色填充content和padding区域 */
  background-color: #ecf0f1;
}

/* ====== box-sizing：改变盒模型计算方式 ====== */
/* 默认content-box：width只指内容宽度 */
/* 推荐border-box：width包含content+padding+border */
.box-border {
  box-sizing: border-box;    /* 更直观的盒模型 */
  width: 200px;              /* 此时200px是content+padding+border的总宽 */
  padding: 20px;
  border: 5px solid #e74c3c;
  /* 实际内容宽度 = 200 - 20*2 - 5*2 = 150px */
}
```

**💡 代码解释**：第3-14行：展示了盒模型四层结构，width设置内容宽度，padding是内边距，border是边框，margin是外边距。第19-24行：box-sizing: border-box让width包含padding和border，布局计算更直观，推荐全局使用。

**🔑 关键要点**：
- 盒模型四层：content→padding→border→margin
- 默认box-sizing是content-box
- 推荐全局设置border-box
- margin可以设置负值，padding不能

---

### 6. Flexbox布局基础

> **级别**：初级 | **概念**：Flexbox是一维布局模型，通过设置容器和项目的属性，可以轻松实现水平或垂直方向的对齐、分布和排序。

```htmlcss
/* ====== Flex容器属性 ====== */
.flex-container {
  display: flex;              /* 开启Flex布局，子元素变为flex项目 */
  flex-direction: row;        /* 主轴方向：row水平(默认) | column垂直 */
  justify-content: space-between; /* 主轴对齐：space-between两端对齐 */
  align-items: center;        /* 交叉轴对齐：center垂直居中 */
  flex-wrap: wrap;            /* 允许换行，项目超出容器宽度时自动折行 */
  gap: 16px;                  /* 项目之间的间距16px */
  height: 200px;              /* 容器高度 */
  background-color: #f0f0f0;
}

/* ====== Flex项目属性 ====== */
.flex-item {
  flex: 1;                    /* 简写：flex-grow:1 flex-shrink:1 flex-basis:0 */
  /* flex-grow: 1 表示有剩余空间时，该项目按比例放大 */
  /* flex-shrink: 1 表示空间不足时，该项目按比例缩小 */
  /* flex-basis: 0 表示基础大小为0，完全按grow分配 */
  padding: 20px;
  background-color: #3498db;
  color: #fff;
  text-align: center;
}

/* 让某个项目占据2倍空间 */
.flex-item.double {
  flex: 2;                    /* 占据2倍于其他项目的空间 */
}
```

**💡 代码解释**：第2-9行：容器设置display:flex开启布局，flex-direction控制主轴方向，justify-content控制主轴对齐，align-items控制交叉轴对齐，flex-wrap允许换行，gap设置间距。第13-20行：flex:1是简写属性，控制项目的伸缩比例。第23-25行：不同flex值让项目按比例分配空间。

**🔑 关键要点**：
- 主轴和交叉轴是Flexbox的核心概念
- justify-content控制主轴对齐
- align-items控制交叉轴对齐
- flex属性控制项目伸缩比例

---

### 7. Grid布局基础

> **级别**：初级 | **概念**：CSS Grid是二维布局系统，可以同时控制行和列，适合构建复杂的页面布局结构。

```htmlcss
/* ====== Grid容器：定义网格轨道 ====== */
.grid-container {
  display: grid;                          /* 开启Grid布局 */
  /* grid-template-columns：定义3列，每列等宽 */
  grid-template-columns: repeat(3, 1fr);  /* 1fr表示等分剩余空间 */
  /* grid-template-rows：定义行高 */
  grid-template-rows: 100px 150px;        /* 第1行100px，第2行150px */
  /* gap：行列间距 */
  gap: 16px;                              /* 行和列的间距都是16px */
  /* 也可以分别设置：row-gap: 16px; column-gap: 20px; */
  padding: 16px;
  background-color: #f5f5f5;
}

/* ====== Grid项目：放置到指定位置 ====== */
.grid-item {
  background-color: #2ecc71;
  color: #fff;
  padding: 20px;
  text-align: center;
  border-radius: 8px;
}

/* 让某个项目横跨多列 */
.grid-item.span-2 {
  /* grid-column：从第1条线开始，横跨2列 */
  grid-column: span 2;    /* 占2列宽度 */
}

/* 让某个项目横跨多行 */
.grid-item.span-row {
  /* grid-row：从第1条线开始，横跨2行 */
  grid-row: span 2;       /* 占2行高度 */
}
```

**💡 代码解释**：第2-10行：display:grid开启网格布局，grid-template-columns定义列轨道，repeat(3,1fr)创建3等分列，grid-template-rows定义行高，gap统一设置间距。第15-18行：基础项目样式。第21-23行：grid-column:span 2让项目占据2列。第26-28行：grid-row:span 2让项目占据2行。

**🔑 关键要点**：
- fr单位表示等分剩余空间
- repeat()简化重复定义
- grid-column/row实现跨列跨行
- Grid是二维布局，Flex是一维

---

### 8. 响应式设计与媒体查询

> **级别**：初级 | **概念**：响应式设计让网页在不同屏幕尺寸下都能良好显示，媒体查询@media是实现响应式的核心技术。

```htmlcss
/* ====== 移动优先：先写移动端样式 ====== */
/* 基础样式适用于手机屏幕（< 768px） */
.card {
  width: 100%;               /* 手机端占满整个宽度 */
  padding: 16px;
  margin-bottom: 12px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);  /* 轻微阴影 */
}

/* ====== 平板断点：768px以上 ====== */
@media (min-width: 768px) {
  /* 屏幕宽度>=768px时，以下样式生效 */
  .card {
    width: calc(50% - 24px); /* 两列布局，减去间距 */
    display: inline-block;   /* 行内块元素，可并排显示 */
    margin-right: 12px;
  }
}

/* ====== 桌面断点：1024px以上 ====== */
@media (min-width: 1024px) {
  /* 屏幕宽度>=1024px时，以下样式生效 */
  .card {
    width: calc(33.333% - 24px); /* 三列布局 */
  }

  .container {
    max-width: 1200px;       /* 限制最大宽度，超宽屏内容不散开 */
    margin: 0 auto;          /* 水平居中 */
  }
}

/* ====== 打印样式 ====== */
@media print {
  /* 打印时隐藏导航栏和广告 */
  .navbar, .advertisement {
    display: none;
  }
  body {
    font-size: 12pt;         /* 打印用的字号单位 */
  }
}
```

**💡 代码解释**：第2-9行：移动优先策略，基础样式先适配小屏。第12-18行：@media (min-width:768px)是平板断点，宽度达到768px时应用两列布局。第21-30行：@media (min-width:1024px)是桌面断点，三列布局并限制最大宽度。第33-40行：@media print设置打印样式。

**🔑 关键要点**：
- 移动优先：基础样式为小屏设计
- min-width断点从小到大覆盖
- 常见断点：768px平板、1024px桌面
- max-width容器防止内容过度拉伸

---

### 9. CSS变量（自定义属性）

> **级别**：初级 | **概念**：CSS变量（--*）允许在样式表中定义可复用的值，通过var()函数引用，方便统一管理和动态修改主题色。

```htmlcss
/* ====== 在:root中定义全局CSS变量 ====== */
/* :root选择器代表html元素，变量定义在此处全局可用 */
:root {
  /* 以--开头定义变量，命名建议用kebab-case */
  --primary-color: #3498db;       /* 主题色：蓝色 */
  --secondary-color: #2ecc71;     /* 辅助色：绿色 */
  --text-color: #333333;          /* 文字颜色 */
  --border-radius: 8px;           /* 圆角大小 */
  --spacing-unit: 8px;            /* 基础间距单位 */
  --font-size-base: 16px;         /* 基础字号 */
}

/* ====== 使用var()函数引用变量 ====== */
.button {
  /* var(变量名, 默认值)：第二个参数是变量不存在时的备用值 */
  background-color: var(--primary-color, #3498db);
  color: #fff;
  padding: var(--spacing-unit) calc(var(--spacing-unit) * 2);
  /* calc()函数可以结合变量进行计算 */
  border: none;
  border-radius: var(--border-radius);
  font-size: var(--font-size-base);
  cursor: pointer;
}

/* ====== 局部覆盖变量 ====== */
.dark-theme {
  /* 在特定作用域覆盖变量，只影响该元素及其子元素 */
  --primary-color: #8e44ad;       /* 暗色主题下改为紫色 */
  --text-color: #ecf0f1;          /* 浅色文字 */
  background-color: #2c3e50;
}
```

**💡 代码解释**：第3-10行：在:root中定义全局变量，--开头是语法要求，命名用连字符分隔。第13-22行：通过var()函数引用变量，第二个参数是备用值，calc()可结合变量计算。第26-30行：在特定选择器内覆盖变量值，实现局部主题切换。

**🔑 关键要点**：
- 变量以--开头，在:root中定义全局可用
- var()的第二个参数是默认值
- 变量可在任意作用域内覆盖
- 配合calc()可实现动态计算

---

## 中级进阶

### 1. HTML5语义化标签

> **级别**：中级 | **概念**：HTML5引入了header、nav、main、article、section、aside、footer等语义化标签，让页面结构更清晰，提升SEO和无障碍访问。

```htmlcss
<body>
  <!-- header：页头区域，通常包含logo、导航、搜索框 -->
  <header>
    <!-- nav：导航链接区域，包裹主要的导航菜单 -->
    <nav aria-label="主导航">
      <!-- aria-label为屏幕阅读器提供标签说明 -->
      <ul>
        <li><a href="/">首页</a></li>
        <li><a href="/about">关于</a></li>
        <li><a href="/contact">联系</a></li>
      </ul>
    </nav>
  </header>

  <!-- main：页面主体内容，每个页面只能用一次 -->
  <main>
    <!-- article：独立完整的内容块，可独立分发 -->
    <article>
      <h2>文章标题</h2>
      <!-- time：时间标签，datetime属性提供机器可读格式 -->
      <time datetime="2026-08-07">2026年8月7日</time>
      <!-- section：文档中的章节，通常带有标题 -->
      <section>
        <h3>第一节</h3>
        <p>这是章节内容...</p>
      </section>
    </article>

    <!-- aside：侧边栏，与主内容相关但非核心 -->
    <aside>
      <h3>相关文章</h3>
      <ul>
        <li><a href="#">推荐文章1</a></li>
      </ul>
    </aside>
  </main>

  <!-- footer：页脚，包含版权、联系方式等 -->
  <footer>
    <p>&copy; 2026 我的网站. 保留所有权利.</p>
  </footer>
</body>
```

**💡 代码解释**：第2-10行：header包裹页头，nav定义导航区域，aria-label提供无障碍标签。第13-27行：main是唯一主体容器，article表示独立内容，section划分章节，time提供语义化时间。第30-34行：aside是侧边栏。第37-39行：footer是页脚。

**🔑 关键要点**：
- main每页只能用一次
- article表示独立完整内容
- section通常带标题
- 语义化标签提升SEO和可访问性

---

### 2. CSS动画：Transition

> **级别**：中级 | **概念**：transition让CSS属性值的变化变得平滑，通过指定过渡属性、时长、缓动函数和延迟，实现流畅的动画效果。

```htmlcss
/* ====== Transition基础语法 ====== */
.btn {
  padding: 12px 24px;
  background-color: #3498db;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  /* transition: 属性名 时长 缓动函数 延迟时间; */
  /* 过渡属性为all表示所有可动画属性都过渡 */
  transition: all 0.3s ease-in-out;
  /* all：所有属性变化都过渡 */
  /* 0.3s：过渡持续300毫秒 */
  /* ease-in-out：先加速后减速的缓动函数 */
}

/* 鼠标悬停时触发过渡 */
.btn:hover {
  background-color: #2980b9;  /* 背景色变深 */
  transform: scale(1.05);     /* 放大到105% */
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);  /* 阴影加深 */
}

/* ====== 分别设置不同属性的过渡 ====== */
.card {
  width: 300px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  /* 不同属性使用不同的过渡参数 */
  transition: transform 0.4s cubic-bezier(0.68, -0.55, 0.27, 1.55),
              box-shadow 0.3s ease,
              opacity 0.2s linear;
  /* transform用弹性缓动，box-shadow用默认缓动，opacity用线性 */
}

.card:hover {
  transform: translateY(-8px);  /* 向上移动8px */
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  opacity: 0.9;
}
```

**💡 代码解释**：第2-13行：transition是简写属性，all表示所有属性都过渡，0.3s是时长，ease-in-out是缓动函数。第16-19行：hover时改变背景色、缩放和阴影，因为有transition所以会平滑过渡。第23-34行：可以为不同属性设置不同的过渡参数，cubic-bezier自定义缓动曲线。

**🔑 关键要点**：
- transition简写：属性 时长 缓动 延迟
- ease-in-out是最常用的缓动函数
- cubic-bezier可自定义缓动曲线
- transition需要触发条件（如hover）

---

### 3. CSS动画：Keyframes

> **级别**：中级 | **概念**：@keyframes定义动画的关键帧序列，animation属性将动画应用到元素上，支持循环、反向、暂停等控制。

```htmlcss
/* ====== 定义关键帧动画 ====== */
/* @keyframes后面跟动画名称 */
@keyframes slideIn {
  /* 0%是动画开始状态（也可用from） */
  0% {
    transform: translateX(-100%);  /* 从左侧屏幕外开始 */
    opacity: 0;                    /* 完全透明 */
  }
  /* 60%是动画中间状态 */
  60% {
    transform: translateX(20px);   /* 稍微超过目标位置 */
    opacity: 0.8;
  }
  /* 100%是动画结束状态（也可用to） */
  100% {
    transform: translateX(0);      /* 回到正常位置 */
    opacity: 1;                    /* 完全不透明 */
  }
}

/* ====== 应用动画到元素 ====== */
.animated-box {
  width: 200px;
  height: 100px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 8px;
  /* animation: 名称 时长 缓动 延迟 次数 方向 填充模式; */
  animation: slideIn 0.8s ease-out 0.2s 1 forwards;
  /* slideIn：动画名称 */
  /* 0.8s：单次动画持续时间 */
  /* ease-out：缓动函数（先快后慢） */
  /* 0.2s：延迟0.2秒后开始 */
  /* 1：播放1次（infinite表示无限循环） */
  /* forwards：动画结束后保持最终状态 */
}

/* ====== 无限循环的脉冲动画 ====== */
@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50%      { transform: scale(1.1); }
}

.loading-dot {
  width: 12px;
  height: 12px;
  background: #3498db;
  border-radius: 50%;
  animation: pulse 1.5s ease-in-out infinite;
  /* infinite：无限循环播放 */
}
```

**💡 代码解释**：第3-16行：@keyframes定义slideIn动画，用百分比指定关键帧，0%是起始状态，100%是结束状态。第19-31行：animation是简写属性，forwards让动画停在最后一帧。第34-41行：pulse动画利用0%/50%/100%三个关键帧实现缩放脉冲，infinite让动画无限循环。

**🔑 关键要点**：
- @keyframes用百分比定义关键帧
- animation-direction控制播放方向
- forwards保持动画结束状态
- infinite实现无限循环动画

---

### 4. BEM命名规范

> **级别**：中级 | **概念**：BEM（Block Element Modifier）是一种CSS命名方法论，通过 块__元素--修饰符 的命名模式，让CSS类名更具语义化和可维护性。

```htmlcss
/* ====== BEM命名规范示例 ====== */
/* Block（块）：独立的组件，如 .card */
/* Element（元素）：块的组成部分，用 __ 连接，如 .card__title */
/* Modifier（修饰符）：块或元素的状态变体，用 -- 连接，如 .card--featured */

/* ====== Block：卡片组件 ====== */
.card {
  display: flex;
  flex-direction: column;
  padding: 20px;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

/* ====== Element：卡片的子元素 ====== */
/* 卡片标题 */
.card__title {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

/* 卡片描述 */
.card__description {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 16px;
}

/* 卡片操作按钮区 */
.card__actions {
  display: flex;
  gap: 12px;
  margin-top: auto;  /* 自动推到卡片底部 */
}

/* 卡片按钮 */
.card__button {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  background: #3498db;
  color: #fff;
}

/* ====== Modifier：卡片的状态变体 ====== */
/* 精选卡片：特殊样式 */
.card--featured {
  border: 2px solid #f39c12;
  background: linear-gradient(135deg, #fff9e6, #fff);
}

/* 主要按钮：强调色 */
.card__button--primary {
  background: #e74c3c;
}

/* 小尺寸卡片 */
.card--small {
  padding: 12px;
  max-width: 250px;
}
```

**💡 代码解释**：第1-4行：BEM命名规则说明。第7-14行：.card是Block，表示独立组件。第18-42行：.card__title、.card__description等是Element，用__连接，表示块的子元素。第46-55行：.card--featured是Modifier，用--连接，表示块的状态变体。第58-62行：.card--small是尺寸修饰符。

**🔑 关键要点**：
- Block用单个类名表示独立组件
- Element用__连接表示子元素
- Modifier用--连接表示状态变体
- BEM避免深层嵌套，保持选择器扁平

---

### 5. CSS函数：calc()与clamp()

> **级别**：中级 | **概念**：calc()允许在CSS中进行数学计算，clamp()实现响应式数值约束，min()和max()则选取最值，这些函数让CSS更动态灵活。

```htmlcss
/* ====== calc()：数学计算函数 ====== */
/* 支持加减乘除运算，不同单位可以混合计算 */
.sidebar {
  /* 宽度 = 100%视口宽度 - 主内容区300px - 间距40px */
  width: calc(100% - 300px - 40px);
  /* 计算后的值作为最终宽度 */
  padding: 20px;
}

.hero-section {
  /* 高度 = 视口高度 - 导航栏60px */
  min-height: calc(100vh - 60px);
  /* vh是视口高度单位，100vh = 整个视口高度 */
  background: linear-gradient(135deg, #667eea, #764ba2);
}

/* ====== clamp()：响应式约束函数 ====== */
/* 语法：clamp(最小值, 首选值, 最大值) */
.responsive-text {
  /* 字体大小：最小16px，理想值4vw，最大24px */
  font-size: clamp(16px, 4vw, 24px);
  /* 在小屏幕上不会小于16px，在大屏幕上不会超过24px */
  /* 4vw = 视口宽度的4%，随屏幕大小动态变化 */
}

.responsive-container {
  /* 容器宽度：最小320px，理想值80%，最大1200px */
  width: clamp(320px, 80%, 1200px);
  /* 保证内容不会太窄或太宽 */
  margin: 0 auto;  /* 居中 */
  padding: clamp(16px, 3vw, 40px);
  /* 内边距也随视口动态调整 */
}

/* ====== min()与max()：选取最值 ====== */
.hero-image {
  /* 宽度取80%和600px中较小的那个 */
  width: min(80%, 600px);
  /* 大屏幕时不超过600px，小屏幕时自适应 */
}

.full-width {
  /* 宽度取100%和1200px中较大的那个 */
  width: max(100%, 1200px);
  /* 确保内容至少1200px宽 */
}
```

**💡 代码解释**：第3-7行：calc()支持加减乘除，不同单位混合计算。第10-13行：100vh是视口高度。第17-27行：clamp()设置响应式字体和容器，最小值保证可读性，最大值防止过度放大。第30-37行：min()取较小值限制最大尺寸，max()取较大值保证最小尺寸。

**🔑 关键要点**：
- calc()支持混合单位计算
- clamp()实现响应式约束
- min()和max()选取最值
- 这些函数在运行时动态计算

---

### 6. 伪类与伪元素深入

> **级别**：中级 | **概念**：伪类(:)表示元素的特殊状态，伪元素(::)创建文档中不存在的虚拟元素，两者结合可以实现丰富的交互和装饰效果。

```htmlcss
/* ====== 结构伪类：根据位置选择元素 ====== */
/* :nth-child(n)：选中父元素的第n个子元素 */
.list-item:nth-child(odd) {
  /* odd表示奇数项，even表示偶数项 */
  background-color: #f8f9fa;
}

.list-item:nth-child(3n) {
  /* 3n表示每3个选一个（3, 6, 9...） */
  font-weight: bold;
}

.list-item:first-child {
  /* 选中第一个子元素 */
  border-top: 2px solid #3498db;
}

.list-item:last-child {
  /* 选中最后一个子元素 */
  border-bottom: 2px solid #3498db;
}

/* ====== 状态伪类 ====== */
/* :focus-within：元素自身或其子元素获得焦点时 */
.search-form:focus-within {
  /* 搜索框内任意子元素获得焦点时，整个表单高亮 */
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.3);
  border-color: #3498db;
}

/* :empty：选中没有子元素（包括文本）的元素 */
.error-message:empty {
  display: none;  /* 没有错误信息时隐藏 */
}

/* ====== 伪元素 ====== */
/* ::before和::after：在元素前后插入虚拟内容 */
.quote::before {
  content: '“';  /* 左双引号的Unicode编码 */
  font-size: 3em;
  color: #3498db;
  line-height: 0;
  vertical-align: -0.3em;
}

.quote::after {
  content: '”';  /* 右双引号 */
  font-size: 3em;
  color: #3498db;
}

/* ::selection：用户选中文本时的样式 */
::selection {
  background-color: #3498db;
  color: #fff;
}
```

**💡 代码解释**：第3-17行：:nth-child根据位置选择元素，odd/even选奇偶项，3n选每3个。第20-27行：:focus-within检测焦点在子元素内，:empty匹配空元素。第31-42行：::before和::after通过content插入虚拟内容，创建装饰性引号。第46-49行：::selection自定义选中文本样式。

**🔑 关键要点**：
- :nth-child()支持odd/even和公式
- :focus-within检测后代焦点
- ::before/::after必须设置content
- ::selection自定义文本选中样式

---

### 7. 表单验证

> **级别**：中级 | **概念**：HTML5原生表单验证通过属性约束和CSS伪类提供即时反馈，结合JavaScript可实现更复杂的自定义验证逻辑。

```htmlcss
<!-- ====== HTML5原生验证属性 ====== -->
<form id="signup-form" novalidate>
  <!-- novalidate禁用浏览器默认验证弹窗，改用自定义样式 -->

  <div class="form-group">
    <label for="email">邮箱：</label>
    <!-- type="email"自动验证邮箱格式 -->
    <input type="email" id="email" name="email"
           required                    <!-- 必填 -->
           placeholder="请输入邮箱"
           pattern="[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,}$">
           <!-- pattern：正则表达式验证 -->
    <!-- :invalid伪类匹配验证失败的状态 -->
    <span class="error-message"></span>
  </div>

  <div class="form-group">
    <label for="password">密码：</label>
    <input type="password" id="password" name="password"
           required
           minlength="8"               <!-- 最小长度8 -->
           maxlength="20"              <!-- 最大长度20 -->
           placeholder="至少8位密码">
    <span class="error-message"></span>
  </div>

  <div class="form-group">
    <label for="age">年龄：</label>
    <input type="number" id="age" name="age"
           min="18"                    <!-- 最小值18 -->
           max="100"                   <!-- 最大值100 -->
           placeholder="18-100">
    <span class="error-message"></span>
  </div>

  <button type="submit">提交</button>
</form>

<script>
// 自定义验证：使用Constraint Validation API
const form = document.getElementById('signup-form');
form.addEventListener('submit', function(event) {
  // 阻止默认提交行为
  event.preventDefault();

  // checkValidity()检查所有表单控件是否满足约束
  if (form.checkValidity()) {
    alert('验证通过，提交表单！');
  } else {
    // reportValidity()触发浏览器显示验证错误
    form.reportValidity();
  }
});

// 实时验证单个字段
const emailInput = document.getElementById('email');
emailInput.addEventListener('input', function() {
  // validity对象包含详细的验证状态
  if (emailInput.validity.typeMismatch) {
    // 设置自定义错误消息
    emailInput.setCustomValidity('请输入有效的邮箱地址');
  } else {
    emailInput.setCustomValidity(''); // 清除自定义错误
  }
});
</script>
```

**💡 代码解释**：HTML部分：第2行novalidate禁用默认弹窗。第8行type="email"自动验证格式。第10行pattern自定义正则。第20行minlength限制最小长度。第29行min/max限制数值范围。JS部分：第42行checkValidity()检查所有字段。第47行reportValidity()显示错误。第53行validity对象提供详细验证状态。

**🔑 关键要点**：
- HTML5原生验证属性减少JS代码
- novalidate可禁用默认弹窗
- Constraint Validation API精细控制
- setCustomValidity()设置自定义错误

---

### 8. CSS打印样式

> **级别**：中级 | **概念**：通过@media print定义打印专用样式，隐藏不必要的元素、调整排版和颜色，确保网页内容在打印时清晰美观。

```htmlcss
/* ====== 打印样式：@media print ====== */
@media print {
  /* 隐藏不需要打印的元素 */
  .navbar,
  .sidebar,
  .advertisement,
  .no-print {
    display: none !important;  /* !important确保打印时一定隐藏 */
  }

  /* 调整全局排版 */
  body {
    font-size: 12pt;           /* 打印用pt单位更合适 */
    line-height: 1.5;
    color: #000;               /* 黑色文字，打印更清晰 */
    background: #fff !important;  /* 白色背景，节省墨水 */
  }

  /* 链接显示完整URL */
  a[href]::after {
    content: " (" attr(href) ")";  /* attr()获取href属性值 */
    font-size: 0.8em;
    color: #666;
  }

  /* 内部链接和空链接不显示URL */
  a[href^="#"]::after,
  a[href=""]::after {
    content: none;             /* 不显示链接 */
  }

  /* 避免内容在中间断页 */
  h2, h3 {
    page-break-after: avoid;   /* 标题后不要立即换页 */
  }

  article {
    page-break-inside: avoid;  /* 文章内部不要断页 */
  }

  /* 图片适应打印宽度 */
  img {
    max-width: 100% !important;  /* 图片不超过页面宽度 */
    page-break-inside: avoid;
  }

  /* 页面边距设置 */
  @page {
    margin: 2cm;               /* 打印页边距2厘米 */
    size: A4;                   /* 纸张大小A4 */
  }

  /* 首页特殊样式 */
  @page :first {
    margin-top: 3cm;           /* 首页顶部留更多空间 */
  }
}
```

**💡 代码解释**：第2-8行：隐藏导航、侧边栏、广告等打印无关元素。第11-16行：调整基础排版，pt单位更适合打印，黑色文字+白色背景。第19-23行：用::after伪元素在链接后显示URL。第26-30行：内部链接不显示URL。第33-39行：page-break-after/inside控制分页位置。第42-49行：@page规则设置纸张大小和边距。

**🔑 关键要点**：
- @media print针对打印设备
- page-break-*控制分页行为
- attr()可获取属性值显示
- @page规则设置纸张参数

---

## 高级精通

### 1. CSS性能优化：contain与will-change

> **级别**：高级 | **概念**：contain属性限制元素的重绘和重排范围，will-change提前告知浏览器将要变化的属性，两者都是提升渲染性能的关键手段。

```htmlcss
/* ====== contain：隔离渲染范围 ====== */
/* 告诉浏览器该元素的内部变化不会影响外部布局 */
.widget {
  /* contain: layout style paint size; */
  contain: content;  /* content = layout + style + paint */
  /* layout：内部布局变化不影响外部 */
  /* style：counter和quotes等不影响外部 */
  /* paint：内部绘制不超出边界 */
  /* content：layout + style + paint 的组合 */
  width: 300px;
  height: 200px;
  overflow: hidden;
}

/* 严格隔离：完全包含 */
.isolated-widget {
  contain: strict;  /* strict = layout + style + paint + size */
  /* size：元素的尺寸不依赖子元素内容 */
  /* 使用strict时必须显式设置宽高 */
  width: 300px;
  height: 200px;
}

/* ====== will-change：预告即将变化的属性 ====== */
/* 提前告知浏览器要变化的属性，浏览器提前优化 */
.slide-panel {
  transform: translateX(-100%);
  /* 告知浏览器transform即将变化 */
  will-change: transform;
  /* 浏览器会为transform创建独立的合成层，利用GPU加速 */
  transition: transform 0.3s ease;
}

/* ⚠️ 重要：动画结束后移除will-change，避免占用资源 */
.slide-panel.active {
  transform: translateX(0);
}

/* 使用JS在动画结束后移除will-change */
/*
const panel = document.querySelector('.slide-panel');
panel.addEventListener('transitionend', () => {
  panel.style.willChange = 'auto';  // 动画结束后移除
});
*/

/* 滚动容器的优化 */
.scroll-container {
  overflow-y: auto;
  will-change: scroll-position;  /* 预告即将滚动 */
}
```

**💡 代码解释**：第3-12行：contain:content隔离元素的布局、样式和绘制，让浏览器只重绘变化区域。第15-19行：contain:strict更进一步隔离尺寸计算。第24-30行：will-change:transform预告transform变化，浏览器预创建GPU合成层。第35-42行：注释展示了动画结束后移除will-change的最佳实践，避免持续占用GPU内存。

**🔑 关键要点**：
- contain限制重绘/重排范围
- will-change预创建GPU合成层
- 动画结束后必须移除will-change
- 合理使用可大幅提升渲染性能

---

### 2. Container Queries（容器查询）

> **级别**：高级 | **概念**：Container Queries根据父容器尺寸而非视口尺寸来应用样式，让组件能根据自身所在空间自适应，是响应式设计的重大升级。

```htmlcss
/* ====== 定义容器查询上下文 ====== */
/* 使用container-type声明元素为查询容器 */
.card-wrapper {
  container-type: inline-size;  /* 基于内联尺寸（宽度）查询 */
  container-name: card;         /* 命名容器，方便引用 */
  /* 简写：container: card / inline-size; */
}

/* ====== 使用容器查询 ====== */
/* @container规则：当容器满足条件时应用样式 */
@container card (min-width: 400px) {
  /* 当card容器宽度>=400px时 */
  .card {
    display: grid;
    grid-template-columns: 200px 1fr;  /* 图文并排 */
    gap: 16px;
  }

  .card__image {
    width: 200px;
    height: 150px;
  }

  .card__title {
    font-size: 1.5em;
  }
}

@container card (max-width: 399px) {
  /* 当card容器宽度<400px时（窄容器） */
  .card {
    display: flex;
    flex-direction: column;  /* 上下堆叠 */
  }

  .card__image {
    width: 100%;
    height: auto;
  }

  .card__title {
    font-size: 1.1em;
  }
}

/* ====== 容器查询长度单位 ====== */
/* cqw = 容器宽度的1%，cqh = 容器高度的1% */
.card__title {
  font-size: clamp(1em, 5cqw, 2em);
  /* 字体大小基于容器宽度动态调整 */
}

/* 不指定容器名，匹配最近的容器 */
@container (min-width: 300px) {
  .timeline-item {
    padding: 16px;
  }
}
```

**💡 代码解释**：第3-6行：container-type声明容器类型，inline-size基于宽度查询。第10-36行：@container规则根据容器尺寸应用不同样式，宽容器时图文并排，窄容器时上下堆叠。第40-43行：cqw/cqh是容器查询单位，基于容器尺寸计算。第47-51行：不指定容器名时匹配最近的查询容器。

**🔑 关键要点**：
- container-type声明查询容器
- @container替代@media做组件级响应
- cqw/cqh是容器相对单位
- 组件可在任何容器中自适应

---

### 3. CSS Grid高级布局

> **级别**：高级 | **概念**：CSS Grid的高级特性包括grid-template-areas命名区域、auto-fill/auto-fit自动填充、minmax()弹性轨道、以及子网格subgrid继承父网格。

```htmlcss
/* ====== grid-template-areas：命名区域布局 ====== */
.page-layout {
  display: grid;
  grid-template-columns: 250px 1fr 250px;
  grid-template-rows: auto 1fr auto;
  min-height: 100vh;
  /* 用命名区域描述布局，像画ASCII图一样直观 */
  grid-template-areas:
    "header  header  header"
    "sidebar main    aside"
    "footer  footer  footer";
  gap: 16px;
}

/* 将元素放置到命名区域 */
.header  { grid-area: header; }
.sidebar { grid-area: sidebar; }
.main    { grid-area: main; }
.aside   { grid-area: aside; }
.footer  { grid-area: footer; }

/* ====== auto-fill vs auto-fit ====== */
/* auto-fill：尽可能多地创建轨道，即使有些是空的 */
.auto-fill-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  /* minmax(250px, 1fr)：每列最小250px，最大等分剩余空间 */
  /* auto-fill：有多少空位就创建多少轨道 */
  gap: 16px;
}

/* auto-fit：拉伸现有项目填满空间 */
.auto-fit-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  /* auto-fit：现有项目拉伸填满，多余空轨道折叠为0 */
  gap: 16px;
}

/* ====== 响应式无需媒体查询 ====== */
/* minmax() + auto-fill 实现自适应列数 */
.gallery {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  /* 容器宽度>=200*n时自动增加列数 */
  /* 缩小窗口时自动减少列数 */
  /* 完全不需要写@media查询！ */
  gap: 12px;
}

/* ====== 密集填充模式 ====== */
.masonry-like {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  grid-auto-flow: dense;  /* 密集填充：小元素填充空隙 */
  gap: 8px;
}
```

**💡 代码解释**：第2-14行：grid-template-areas用命名区域直观描述布局，像一个ASCII图。第17-21行：grid-area将元素放入命名区域。第25-30行：auto-fill创建尽可能多的空白轨道。第32-37行：auto-fit拉伸现有项目填满空间。第41-48行：minmax()配合auto-fill实现无需媒体查询的响应式网格。第52-56行：grid-auto-flow:dense密集填充模式。

**🔑 关键要点**：
- grid-template-areas可视化布局
- auto-fill和auto-fit的区别
- minmax()实现弹性轨道
- dense模式自动填充空隙

---

### 4. 关键渲染路径优化

> **级别**：高级 | **概念**：关键渲染路径（CRP）是浏览器将HTML/CSS/JS转换为像素的过程，优化CRP可以显著提升首屏加载速度。

```htmlcss
<!-- ====== HTML优化：减少阻塞渲染的资源 ====== -->
<head>
  <meta charset="UTF-8">

  <!-- 1. 内联关键CSS（首屏必需的样式） -->
  <!-- 避免额外的网络请求，直接渲染首屏 -->
  <style>
    /* 仅包含首屏可见内容的样式 */
    .hero { min-height: 100vh; background: #1a1a2e; }
    .navbar { position: fixed; top: 0; width: 100%; }
  </style>

  <!-- 2. 非关键CSS异步加载 -->
  <!-- media="print"让浏览器低优先级加载，onload后切换为all -->
  <link rel="stylesheet" href="/styles/full.css"
        media="print" onload="this.media='all'">
  <!-- 备用方案：noscript标签在无JS时直接加载 -->
  <noscript>
    <link rel="stylesheet" href="/styles/full.css">
  </noscript>

  <!-- 3. 预加载关键资源 -->
  <!-- preload告诉浏览器优先下载该资源 -->
  <link rel="preload" href="/fonts/main.woff2" as="font" crossorigin>
  <link rel="preload" href="/images/hero.webp" as="image">

  <!-- 4. 脚本异步加载 -->
  <!-- async：下载不阻塞解析，下载完立即执行 -->
  <script src="/js/analytics.js" async></script>
  <!-- defer：下载不阻塞解析，DOM解析完后按顺序执行 -->
  <script src="/js/main.js" defer></script>
</head>

<!-- ====== CSS优化：减少重排重绘 ====== -->
<style>
/* 5. 使用transform和opacity做动画 */
/* 这些属性只触发composite，不触发layout和paint */
.animated {
  /* ✅ 推荐：只触发合成阶段 */
  transform: translateX(100px);
  opacity: 0.5;

  /* ❌ 避免：触发重排 */
  /* left: 100px; */
  /* width: 200px; */
  /* margin-left: 20px; */
}

/* 6. 提升到合成层 */
.smooth-scroll {
  transform: translateZ(0);  /* 创建独立合成层 */
  will-change: transform;    /* 预告变化 */
}

/* 7. content-visibility延迟渲染 */
.off-screen-section {
  content-visibility: auto;  /* 屏幕外的元素跳过渲染 */
  contain-intrinsic-size: 0 500px;  /* 预估高度，防止滚动条跳动 */
}
</style>
```

**💡 代码解释**：第5-9行：内联关键CSS避免额外请求。第12-18行：非关键CSS用media trick异步加载。第21-24行：preload预加载关键资源。第27-30行：async和defer避免脚本阻塞解析。第36-49行：CSS动画优化——使用transform/opacity只触发合成阶段，translateZ(0)创建合成层。第52-55行：content-visibility:auto跳过屏幕外渲染。

**🔑 关键要点**：
- 内联关键CSS减少请求
- async/defer异步加载JS
- transform/opacity只触发合成
- content-visibility跳过屏幕外渲染

---

### 5. CSS架构设计

> **级别**：高级 | **概念**：大型项目的CSS架构需要分层管理、命名规范、设计令牌和工具约束，确保样式可维护、可扩展、可复用。

```htmlcss
/* ====== CSS架构设计：分层结构 ====== */

/* 第一层：设计令牌（Design Tokens） */
/* 所有设计变量集中管理，是整个样式系统的基础 */
:root {
  /* 颜色系统 */
  --color-primary-100: #dbeafe;
  --color-primary-500: #3b82f6;
  --color-primary-900: #1e3a5f;

  /* 间距系统（基于4px的倍数） */
  --space-1: 4px;
  --space-2: 8px;
  --space-4: 16px;
  --space-6: 24px;
  --space-8: 32px;

  /* 字体系统 */
  --font-sans: 'Inter', system-ui, sans-serif;
  --font-mono: 'JetBrains Mono', monospace;

  /* 阴影层级 */
  --shadow-sm: 0 1px 2px rgba(0,0,0,0.05);
  --shadow-md: 0 4px 6px rgba(0,0,0,0.1);
  --shadow-lg: 0 10px 15px rgba(0,0,0,0.1);
}

/* 第二层：全局重置与基础样式 */
/* 统一浏览器默认样式，建立全局基线 */
*,
*::before,
*::after {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

html {
  font-size: 16px;
  -webkit-font-smoothing: antialiased;
}

body {
  font-family: var(--font-sans);
  line-height: 1.6;
  color: #1a1a2e;
}

/* 第三层：工具类（Utility Classes） */
/* 单一职责的原子化类，组合使用构建布局 */
.flex { display: flex; }
.flex-col { flex-direction: column; }
.items-center { align-items: center; }
.justify-between { justify-content: space-between; }
.gap-4 { gap: var(--space-4); }
.p-4 { padding: var(--space-4); }
.text-center { text-align: center; }

/* 第四层：组件样式 */
/* 遵循BEM规范，每个组件独立文件 */
/* @import './components/button.css'; */
/* @import './components/card.css'; */
/* @import './components/modal.css'; */

/* 第五层：页面布局 */
.page-layout {
  display: grid;
  grid-template-areas:
    "header"
    "main"
    "footer";
  min-height: 100vh;
}

/* 第六层：主题变体 */
[data-theme="dark"] {
  --color-primary-100: #1e293b;
  --color-primary-500: #60a5fa;
  background: #0f172a;
  color: #e2e8f0;
}
```

**💡 代码解释**：第4-24行：设计令牌层集中管理颜色、间距、字体、阴影等变量。第28-31行：全局重置统一浏览器默认样式。第33-40行：基础样式设置全局字体和行高。第44-51行：工具类提供原子化CSS。第57-60行：组件层按BEM规范组织。第63-69行：页面布局层定义整体结构。第72-77行：主题层通过变量覆盖实现主题切换。

**🔑 关键要点**：
- 设计令牌是样式系统的基石
- 分层架构：令牌→重置→工具→组件→布局→主题
- 原子化工具类提高复用性
- 变量覆盖实现主题切换

---

### 6. CSS Houdini入门

> **级别**：高级 | **概念**：CSS Houdini是一组底层API，允许开发者通过JavaScript扩展CSS引擎，实现自定义属性、布局、绘制等功能。

```htmlcss
/* ====== CSS Houdini Paint API ====== */
/* 通过JavaScript注册自定义绘制函数 */
<script>
// 注册一个自定义Paint Worklet
// paint()函数接收ctx（类似Canvas 2D上下文）、几何信息和属性
class CheckerboardPainter {
  // 声明CSS属性，这些属性变化时会触发重绘
  static get inputProperties() {
    return ['--checkerboard-size', '--checkerboard-color'];
  }

  // paint是核心绘制方法
  paint(ctx, geom, properties) {
    // 从CSS属性中读取值
    const size = parseInt(properties.get('--checkerboard-size').toString()) || 32;
    const color = properties.get('--checkerboard-color').toString() || '#3498db';

    ctx.fillStyle = color;
    // 绘制棋盘格图案
    for (let y = 0; y < geom.height; y += size) {
      for (let x = 0; x < geom.width; x += size) {
        // 交替绘制方格
        if ((Math.floor(x / size) + Math.floor(y / size)) % 2 === 0) {
          ctx.fillRect(x, y, size, size);
        }
      }
    }
  }
}

// 注册Paint Worklet
registerPaint('checkerboard', CheckerboardPainter);
</script>

<style>
  /* 使用自定义Paint */
  .checkerboard-bg {
    /* paint()函数调用自定义绘制 */
    background-image: paint(checkerboard);
    /* CSS变量控制绘制参数 */
    --checkerboard-size: 40px;
    --checkerboard-color: #3498db;
    width: 400px;
    height: 300px;
  }
</style>

<!-- ====== 使用方式 ====== -->
<!-- 在HTML中加载Paint Worklet -->
<script>
if ('paintWorklet' in CSS) {
  // 加载Paint Worklet模块
  CSS.paintWorklet.addModule('/worklets/checkerboard.js');
}
</script>
```

**💡 代码解释**：第4-28行：定义CheckerboardPainter类，inputProperties声明依赖的CSS变量，paint方法使用Canvas API绘制棋盘格图案。第33-41行：通过background-image:paint()调用自定义绘制，CSS变量控制参数。第47-51行：检测浏览器支持后通过CSS.paintWorklet.addModule加载Worklet。

**🔑 关键要点**：
- Houdini扩展CSS引擎底层能力
- Paint API用Canvas绘制背景
- inputProperties声明CSS变量依赖
- Paint Worklet在独立线程运行

---
