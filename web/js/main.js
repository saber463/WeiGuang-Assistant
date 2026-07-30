/**
 * ============================================================================
 *  微光畅行 - 主交互脚本 (main.js)
 * ============================================================================
 *  本文件包含微光畅行官网的所有前端交互逻辑，从 index.html 中提取。
 *  主要功能模块：
 *    1. Scroll Reveal（滚动显示动画）
 *    2. Stats Counter（数字计数器动画）
 *    3. Smooth Scroll Navigation（平滑滚动导航）
 *    4. Mouse Trail（鼠标拖尾效果）
 *    5. Tech Card Modal（技术卡片弹窗）
 *    6. Mascot Chat System（吉祥物聊天系统）
 *  依赖：Bootstrap 5.3.2 JS Bundle（通过 CDN 加载）
 * ============================================================================
 */


/* ═══════════════════════════════════════════════════════════════════════════════
   Section 1: Scroll & Navigation（滚动监听与导航栏交互）
   功能：监听页面滚动事件，控制导航栏样式、返回顶部按钮显示、当前导航项高亮
   ═══════════════════════════════════════════════════════════════════════════════ */

/**
 * 全局滚动事件监听器
 * 作用：在用户滚动页面时执行以下三项操作：
 *   1. 导航栏添加/移除 'scrolled' 类名（改变背景色和阴影）
 *   2. 返回顶部按钮在滚动超过300px时显示/隐藏
 *   3. 根据当前可视区域自动高亮对应的导航链接
 */
window.addEventListener('scroll', function() {
    // 获取导航栏元素
    var nav = document.getElementById('mainNav');
    // 当滚动距离 > 50px 时，给导航栏添加 'scrolled' 类名（CSS 控制背景变深/加阴影）
    nav.classList.toggle('scrolled', window.scrollY > 50);
    // 当滚动距离 > 300px 时，显示返回顶部按钮（CSS 控制 opacity 和 visibility）
    document.getElementById('backToTop').classList.toggle('show', window.scrollY > 300);

    // --- 当前活跃导航项检测 ---
    // 记录当前应该高亮的 section id
    var current = '';
    // 遍历所有带 id 的 section 元素
    document.querySelectorAll('section[id]').forEach(function(s) {
        // 如果页面滚动位置超过了该 section 顶部偏移150px 的位置，则认为当前可视区域在该 section 内
        if (window.scrollY >= s.offsetTop - 150) current = s.id;
    });
    // 遍历所有导航链接，将 href 与当前 section id 匹配的链接添加 'active' 类名
    document.querySelectorAll('.nav-link').forEach(function(a) {
        a.classList.toggle('active', a.getAttribute('href') === '#' + current);
    });
});


/* ═══════════════════════════════════════════════════════════════════════════════
   Section 2: Scroll Reveal（滚动显示动画）
   功能：使用 IntersectionObserver 监听元素进入视口，触发淡入动画
   原理：所有带 .reveal 类名的元素初始状态为透明+上移，进入视口后添加 .visible 类名触发 CSS 动画
   ═══════════════════════════════════════════════════════════════════════════════ */

/**
 * IntersectionObserver 实例 - 滚动显示动画
 * 配置：threshold: 0.08 表示元素有 8% 进入视口时触发回调
 * 行为：元素进入视口后添加 'visible' 类名，并取消观察（只播放一次动画）
 */
var observer = new IntersectionObserver(function(entries) {
    entries.forEach(function(e) {
        // 当元素与视口交叉时（进入可视区域）
        if (e.isIntersecting) {
            // 添加 'visible' 类名，触发 CSS transition/animation 淡入效果
            e.target.classList.add('visible');
            // 取消对该元素的观察，避免重复触发
            observer.unobserve(e.target);
        }
    });
}, { threshold: 0.08 });

// 为页面中所有带 .reveal 类名的元素注册观察器
document.querySelectorAll('.reveal').forEach(function(r) { observer.observe(r); });


/* ═══════════════════════════════════════════════════════════════════════════════
   Section 3: Stats Counter（数字计数器动画）
   功能：当统计数字区域进入视口时，数字从 0 快速递增到目标值，形成计数动画效果
   原理：使用 IntersectionObserver 检测 .counter 元素进入视口后启动 setInterval 定时器
   ═══════════════════════════════════════════════════════════════════════════════ */

/**
 * IntersectionObserver 实例 - 数字计数器动画
 * 配置：threshold: 0.4 表示元素有 40% 进入视口时触发
 * 行为：读取元素的 data-target 属性获取目标数字，启动定时器从 0 递增
 */
var counterObserver = new IntersectionObserver(function(entries) {
    entries.forEach(function(e) {
        if (e.isIntersecting) {
            var el = e.target; // 当前进入视口的计数器 DOM 元素
            var target = parseInt(el.dataset.target); // 读取 data-target 属性值作为目标数字
            var cur = 0; // 当前显示的数字，初始为 0
            // 计算每步递增的数值：总目标 / 45步，确保约 1.575 秒（45 * 35ms）内完成动画
            var step = Math.ceil(target / 45);
            // 启动定时器，每 35ms 执行一次递增
            var t = setInterval(function() {
                cur += step;
                // 当前值达到或超过目标值时，锁定为最终值并停止定时器
                if (cur >= target) { cur = target; clearInterval(t); }
                // 将当前数字更新到 DOM 元素的文本内容中
                el.textContent = cur;
            }, 35);
            // 取消对该计数器的观察，避免重复触发
            counterObserver.unobserve(el);
        }
    });
}, { threshold: 0.4 });

// 为页面中所有带 .counter 类名的元素注册计数器观察器
document.querySelectorAll('.counter').forEach(function(c) { counterObserver.observe(c); });


/* ═══════════════════════════════════════════════════════════════════════════════
   Section 4: Smooth Scroll Navigation（平滑滚动导航）
   功能：拦截所有锚点链接的点击事件，平滑滚动到目标 section，并自动关闭移动端导航菜单
   ═══════════════════════════════════════════════════════════════════════════════ */

/**
 * 为所有以 '#' 开头的链接添加平滑滚动行为
 * 点击导航链接时：
 *   1. 阻止默认的跳转行为
 *   2. 使用 scrollIntoView 平滑滚动到目标 section
 *   3. 如果是移动端且导航菜单已展开，则自动收起菜单
 */
document.querySelectorAll('a[href^="#"]').forEach(function(a) {
    a.addEventListener('click', function(e) {
        // 获取链接 href 指向的目标 DOM 元素
        var t = document.querySelector(this.getAttribute('href'));
        if (t) {
            // 阻止浏览器默认的锚点跳转行为
            e.preventDefault();
            // 平滑滚动到目标元素，block: 'start' 表示滚动到元素顶部对齐视口顶部
            t.scrollIntoView({ behavior: 'smooth', block: 'start' });
            // 获取 Bootstrap 导航折叠菜单元素
            var nc = document.querySelector('.navbar-collapse');
            // 如果导航菜单处于展开状态（移动端），则调用 Bootstrap Collapse 方法收起
            if (nc.classList.contains('show')) {
                bootstrap.Collapse.getInstance(nc)?.hide();
            }
        }
    });
});


/* ═══════════════════════════════════════════════════════════════════════════════
   Section 5: Mouse Trail（鼠标拖尾效果）
   功能：鼠标移动时在光标位置随机生成 emoji（脚印/太阳/花朵等），1.6秒后自动消失
   频率控制：距离阈值 120px 且时间间隔 300ms 以上才生成，避免过于密集影响性能
   ═══════════════════════════════════════════════════════════════════════════════ */

/**
 * IIFE（立即执行函数表达式） - 鼠标拖尾效果
 * 内部维护上一次鼠标坐标和时间戳，用于频率控制
 */
(function() {
    // 可随机显示的 emoji 数组：脚印、太阳、向日葵、樱花、雏菊、树叶、木槿花
    var trailEmojis = ['👣', '☀️', '🌻', '🌸', '🌼', '🍃', '🌺'];
    // 上一次生成 emoji 时的鼠标坐标和时间戳
    var lastX = 0, lastY = 0, lastTime = Date.now();

    // 监听全局鼠标移动事件
    document.addEventListener('mousemove', function(e) {
        var now = Date.now(); // 当前时间戳
        // 计算当前鼠标位置与上一次位置的欧几里得距离
        var dist = Math.sqrt((e.clientX - lastX) * (e.clientX - lastX) + (e.clientY - lastY) * (e.clientY - lastY));
        // 频率控制：距离 < 120px 且时间间隔 < 300ms 时不生成，跳过本次事件
        if (dist < 120 && now - lastTime < 300) return;
        // 更新上一次的坐标和时间戳
        lastX = e.clientX; lastY = e.clientY; lastTime = now;

        // 创建一个 div 元素作为拖尾 emoji 容器
        var el = document.createElement('div');
        el.className = 'trail-emoji'; // CSS 控制定位、动画、透明度等
        // 从 emoji 数组中随机选取一个
        el.textContent = trailEmojis[Math.floor(Math.random() * trailEmojis.length)];
        // 将元素定位到鼠标光标位置（偏移 10px 让 emoji 居中于光标）
        el.style.left = (e.clientX - 10) + 'px';
        el.style.top = (e.clientY - 10) + 'px';
        // 将元素添加到 DOM 中
        document.body.appendChild(el);
        // 1600ms（1.6秒）后自动移除该 emoji 元素（配合 CSS 淡出动画）
        setTimeout(function() { el.remove(); }, 1600);
    });
})();


/* ═══════════════════════════════════════════════════════════════════════════════
   Section 6: Tech Card Modal（技术卡片弹窗）
   功能：点击技术栈卡片后弹出详细说明弹窗，展示该项技术的完整介绍
   数据：techData 数组存储 8 项核心技术的标题、图标、标签、详细内容
   ═══════════════════════════════════════════════════════════════════════════════ */

/**
 * 技术详情数据数组
 * 每项包含：
 *   - title: 技术名称（与 HTML 中 h6 标签文本匹配）
 *   - icon: Bootstrap Icons 图标 HTML
 *   - tag: 弹窗顶部的分类标签
 *   - body: 详细技术介绍 HTML 内容
 */
var techData = [
    {
        title: 'CameraX + ML Kit OCR',
        icon: '<i class="bi bi-camera-video"></i>',
        tag: '📸 相机与文字识别',
        body: '<p><strong>CameraX</strong> 是 Android 官方推出的相机库，最大的特点是"一次开发，全机型适配"。不管你用的是华为、小米还是 OPPO，CameraX 能自动兼容不同手机的摄像头硬件差异，提供一致的预览画面流。</p><p>当用户对准药盒拍照时，CameraX 把每一帧实时画面传给 <strong>ML Kit OCR</strong>（Google 的移动端文字识别引擎）。OCR 模型会先定位图像中的文字区域，然后逐字识别中文、数字和字母，最后提取出药品名称、规格、生产日期等关键信息。</p><p>识别结果会与本地 <strong>Room 数据库</strong>中预存的 56 种常用药品信息进行模糊匹配，自动给出药品用途、用法用量和安全风险等级。整个过程完全离线，不消耗流量，也不用担心隐私泄露。</p>'
    },
    {
        title: 'MediaPipe Hands',
        icon: '<i class="bi bi-hand-index-thumb"></i>',
        tag: '🤟 手语识别引擎',
        body: '<p><strong>MediaPipe Hands</strong> 是 Google 开源的一套手部关键点追踪方案，由轻量级神经网络驱动。它能在手机端以每秒 30 帧以上的速度，实时检测一只手上的 <strong>21 个 3D 关键点</strong>——包括每根手指的指尖、指节、指根，以及手腕的位置坐标。</p><p>我们在这 21 个点的基础上，进一步提取手语的<strong>时空特征</strong>：不仅看当前手势的空间形状，还追踪手指在时间轴上的运动轨迹（比如"你好"的手势是从合掌到张开的过程）。</p><p>然后将这些特征序列与预训练的 <strong>117 条高频手语短语库</strong>进行比对匹配，最终输出对应的文字或语音。识别精度达到 95% 以上，单次识别延迟低于 50 毫秒——你打一个手语，屏幕几乎同时就显示出文字。</p>'
    },
    {
        title: 'SpeechRecognizer + TTS',
        icon: '<i class="bi bi-mic"></i>',
        tag: '🎤 语音双向交互',
        body: '<p><strong>SpeechRecognizer</strong> 是 Android 系统内置的语音识别引擎，支持纯离线模式。它通过声学模型和语言模型将语音波形转化为文字序列。在我们的 App 里，它是听障用户的"耳朵"——对方说的话实时转为文字显示在屏幕上，延迟控制在 200ms 以内。</p><p><strong>TTS</strong>（Text-To-Speech，文字转语音）则反其道而行之。它把用户输入的文字转化为自然流畅的语音播报出来，支持多种语速和音调调节。对于言语障碍用户来说，这就是他们的"嗓子"——打字输入，App 帮你说出来。</p><p>这两个功能组合在一起，就形成了一座<strong>双向沟通的桥梁</strong>：听障用户看文字、言语障碍用户听语音，双方都能顺畅交流。</p>'
    },
    {
        title: 'Jetpack Compose + Material3',
        icon: '<i class="bi bi-phone"></i>',
        tag: '🎨 现代化界面框架',
        body: '<p><strong>Jetpack Compose</strong> 是 Android 全新的声明式 UI 框架。传统开发方式像"手写装修图纸"（XML 布局），而 Compose 像"直接跟工头说我要什么"——用 Kotlin 代码直接描述界面长什么样子，代码量减少约 50%，且界面和数据自动同步更新。</p><p><strong>Material3</strong> 是 Google 最新设计语言，它内置了<strong>动态主题色彩</strong>系统，能根据壁纸自动提取主色和辅色，让 App 界面和手机风格融为一体。</p><p>我们在无障碍方面做了深度适配：大字体模式下布局自动调整不溢出；屏幕阅读器（TalkBack）内容描述全覆盖；所有颜色对比度严格遵循 <strong>WCAG 标准</strong>（确保色弱用户也能看清）；触控热区放大到 48dp 以上，减少误触。</p>'
    },
    {
        title: 'GPS + Room 数据库',
        icon: '<i class="bi bi-geo-alt"></i>',
        tag: '📍 定位与本地数据',
        body: '<p><strong>GPS 定位模块</strong>通过手机内置的定位芯片实时获取经纬度坐标。我们预存了 10 条公交线路的完整站点数据（经纬度+站点名称），App 会持续计算用户当前位置与最近站点的距离。</p><p>当用户接近目标站点约 200 米时，自动触发<strong>到站提醒</strong>——以振动+文字弹窗的方式通知用户准备下车，避免坐过站。整个过程无需网络，在隧道或地库等信号弱的地方也能正常工作。</p><p><strong>Room</strong> 是 Google 官方提供的本地数据库框架，基于 SQLite 但更易用。我们在 Room 中存储了 56 种常用药品信息库、10 条公交线路数据、用户个性化设置等。所有数据离线存储，查询速度在毫秒级，不消耗任何流量。</p>'
    },
    {
        title: '振动引擎 + AlertSystem',
        icon: '<i class="bi bi-phone-vibrate"></i>',
        tag: '🔔 智能警报系统',
        body: '<p><strong>振动引擎</strong>利用了 Android 的 Vibrator API，但我们不是简单地"开/关"振动。我们支持<strong>自定义振动波形</strong>——可以精确控制每次振动的时长（毫秒级）、振动强度（弱/中/强）、以及间歇模式（连续/间歇/脉冲）。</p><p>在此基础上，我们设计了<strong>三级风险差异化提醒策略</strong>：</p><p>• <strong>低风险</strong>（如到站提醒）：轻柔短振 2 次，不打扰<br>• <strong>中风险</strong>（如用药提醒）：规律中振 5 次 + 屏幕闪烁<br>• <strong>高风险</strong>（如 SOS 求救）：急促强振 + 闪光灯高频闪烁 + 最大音量警报声三联动</p><p><strong>AlertSystem</strong> 是我们自行研发的警报调度中心，统一管理所有通知的触发逻辑、优先级排序、防打扰策略（夜间自动降低强度），以及不同模块之间的冲突处理（比如同时收到到站提醒和 SOS 求救，优先响应 SOS）。</p>'
    },
    {
        title: 'ML Kit 图像识别',
        icon: '<i class="bi bi-eyeglasses"></i>',
        tag: '👁️ 场景文字理解',
        body: '<p><strong>ML Kit</strong> 是 Google 提供的移动端机器学习工具包，把训练好的 AI 模型打包成简单易用的 API。其中的<strong>文字识别模型</strong>经过了中文、英文等语言的专项训练，能准确识别路牌、门牌号、菜单、告示牌等场景中的印刷文字。</p><p>在 App 中，当用户打开相机对准场景中的文字时，ML Kit 会自动完成三步：<strong>① 检测</strong>——找到画面中的文字区域；<strong>② 识别</strong>——将文字区域中的字符逐个识别出来；<strong>③ 输出</strong>——将识别结果以文字形式显示在屏幕上。</p><p>这对听障用户来说是一个强大的"环境信息获取器"：走到一个陌生的路口，打开相机扫一扫路牌，文字立刻显示在屏幕上；去餐馆吃饭，扫一下菜单就知道今天有什么菜。世界的信息，尽在眼前。</p>'
    },
    {
        title: 'Kotlin + Coroutines',
        icon: '<i class="bi bi-braces"></i>',
        tag: '⚡ 开发语言与并发',
        body: '<p><strong>Kotlin</strong> 是 Android 官方首推的开发语言，相比传统的 Java，它的语法更简洁、更安全。举个例子：同样的功能，Kotlin 的代码量可以减少约 40%，而且它从语言层面杜绝了空指针异常（NullPointerException）——这是 Java 项目中最常见的崩溃原因。</p><p><strong>Coroutines</strong>（协程）是 Kotlin 的异步并发方案。想象一下你在厨房同时煮汤、切菜、接电话——协程就是能帮你在代码里同时处理多个任务，且不会让 App "卡住"。在我们的 App 里，协程同时管理着相机画面流、手语识别计算、语音合成等多个耗时任务，UI 线程始终保持流畅。</p><p>另外，我们的 APK 打包后<strong>仅 8MB</strong>，兼容从 Android 5.0（2014 年）到最新 Android 15 的所有设备，覆盖了市面上 99% 以上的 Android 手机。</p>'
    }
];

// --- 获取技术卡片弹窗相关的 DOM 元素 ---
// 弹窗遮罩层（全屏半透明背景）
var techModalOverlay = document.getElementById('techModalOverlay');
// 关闭按钮
var techModalClose = document.getElementById('techModalClose');
// 弹窗内的图标容器
var techModalIcon = document.getElementById('techModalIcon');
// 弹窗标题
var techModalTitle = document.getElementById('techModalTitle');
// 弹窗分类标签
var techModalTag = document.getElementById('techModalTag');
// 弹窗正文内容
var techModalBody = document.getElementById('techModalBody');

/**
 * openTechCard(el) - 打开技术卡片详情弹窗
 * @param {HTMLElement} el - 被点击的技术卡片 DOM 元素
 * 逻辑：
 *   1. 从卡片中提取 h6 标签的文本作为技术名称
 *   2. 在 techData 数组中查找匹配的技术条目
 *   3. 将数据填充到弹窗的各个部分
 *   4. 显示弹窗并锁定页面滚动
 */
function openTechCard(el) {
    // 获取卡片内的标题元素
    var h6 = el.querySelector('h6');
    if (!h6) return; // 如果没有找到标题则直接返回
    // 获取标题文本并去除首尾空白
    var title = h6.textContent.trim();
    // 遍历技术数据数组，查找标题匹配的条目
    for (var i = 0; i < techData.length; i++) {
        if (techData[i].title === title) {
            // 将匹配的数据填充到弹窗各元素中
            techModalIcon.innerHTML = techData[i].icon;   // 设置图标
            techModalTitle.textContent = techData[i].title; // 设置标题
            techModalTag.textContent = techData[i].tag;     // 设置分类标签
            techModalBody.innerHTML = techData[i].body;     // 设置详细内容（支持 HTML）
            // 显示弹窗遮罩层（CSS transition 控制淡入）
            techModalOverlay.classList.add('active');
            // 锁定页面滚动，防止弹窗打开时背景页面可滚动
            document.body.style.overflow = 'hidden';
            return; // 找到匹配项后立即退出循环
        }
    }
}

/**
 * 关闭技术卡片弹窗 - 点击关闭按钮
 * 移除 'active' 类名触发淡出动画，恢复页面滚动
 */
techModalClose.addEventListener('click', function() {
    techModalOverlay.classList.remove('active');
    document.body.style.overflow = '';
});

/**
 * 关闭技术卡片弹窗 - 点击遮罩层（弹窗外部区域）
 * 只有点击遮罩层本身（而非弹窗卡片内部）才关闭
 */
techModalOverlay.addEventListener('click', function(e) {
    if (e.target === techModalOverlay) {
        techModalOverlay.classList.remove('active');
        document.body.style.overflow = '';
    }
});

/**
 * 关闭技术卡片弹窗 - 按下 Escape 键
 * 监听全局键盘事件，当弹窗处于打开状态时按 Escape 关闭
 */
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape' && techModalOverlay.classList.contains('active')) {
        techModalOverlay.classList.remove('active');
        document.body.style.overflow = '';
    }
});


/* ═══════════════════════════════════════════════════════════════════════════════
   Section 7: Mascot Chat System（吉祥物聊天系统）
   功能：页面右下角的吉祥物"小微"交互式聊天系统
   特点：
     - 点击吉祥物头像打开/关闭聊天窗口
     - 首次打开显示欢迎消息和导航按钮
     - 支持功能卡片浏览和功能原理弹窗
     - 模拟打字动画效果
   ═══════════════════════════════════════════════════════════════════════════════ */

/**
 * IIFE（立即执行函数表达式） - 吉祥物聊天系统
 * 使用 IIFE 封装所有聊天系统相关的变量和函数，避免全局命名空间污染
 */
(function() {
    // --- 获取聊天系统相关的 DOM 元素 ---
    // 吉祥物头像容器（点击可打开/关闭聊天）
    var mascotBody = document.getElementById('mascotBody');
    // 聊天窗口主容器
    var mascotChat = document.getElementById('mascotChat');
    // 聊天消息区域（显示对话内容）
    var mascotChatBody = document.getElementById('mascotChatBody');
    // 聊天操作按钮区域（显示导航按钮）
    var mascotChatActions = document.getElementById('mascotChatActions');
    // 聊天窗口关闭按钮
    var mascotChatClose = document.getElementById('mascotChatClose');
    // 未读消息徽章（头像右上角的数字气泡）
    var mascotBadge = document.getElementById('mascotBadge');
    // 功能详情弹窗相关元素
    var featModalOverlay = document.getElementById('featModalOverlay'); // 弹窗遮罩层
    var featModalClose = document.getElementById('featModalClose');     // 弹窗关闭按钮
    var featModalEmoji = document.getElementById('featModalEmoji');     // 功能 emoji 图标
    var featModalTitle = document.getElementById('featModalTitle');     // 功能标题
    var featModalTag = document.getElementById('featModalTag');         // 功能分类标签
    var featModalBody = document.getElementById('featModalBody');       // 功能详细内容
    // 聊天窗口状态标记
    var chatOpen = false;     // 当前聊天窗口是否打开
    var welcomeSent = false;  // 是否已发送过欢迎消息


    /* ───────────────────────────────────────────────────────────────────────
       7.1 Feature Data（功能数据）
       存储 12 大功能模块的 emoji、名称、标签和详细介绍
       用于聊天窗口中的功能卡片渲染和功能原理弹窗
       ─────────────────────────────────────────────────────────────────────── */

    /**
     * featureData - 功能详情数据数组
     * 包含微光畅行全部 12 个核心功能的完整信息
     * 每个对象包含：
     *   - emoji: 功能对应的 emoji 图标
     *   - name: 功能名称
     *   - tag: 功能分类标签（显示在弹窗中）
     *   - body: 功能详细介绍 HTML 内容（含技术原理、使用方法等）
     */
    var featureData = [
        {
            // 功能1：一键应急 - SOS紧急求助系统
            emoji: '🆘', name: '一键应急',
            tag: '🚨 SOS紧急求助',
            body: '<p><strong>一键应急</strong>是微光畅行最核心的安全功能。用户只需按下SOS按钮，系统会同时执行三重联动：</p><p>• <strong>SOS短信</strong>：自动发送包含GPS坐标的求助短信给预设的5位紧急联系人<br>• <strong>位置共享</strong>：实时上传位置到云端，联系人可通过网页查看移动轨迹<br>• <strong>闪光灯警报</strong>：高频闪烁吸引周围人注意</p><p>支持<strong>6种预设场景</strong>：迷路求助、遭遇危险、身体不适、交通事故、自然灾害、其他紧急情况。</p>'
        },
        {
            // 功能2：公交报站 - GPS精准定位到站提醒
            emoji: '🚌', name: '公交报站',
            tag: '🗺️ GPS精准定位',
            body: '<p><strong>公交报站</strong>利用GPS实时定位技术，为听障用户提供精准的到站提醒。</p><p>工作原理：系统持续获取GPS坐标，与预存的<strong>10条公交线路</strong>站点数据进行匹配。当距离下一站小于<strong>200米</strong>时，自动触发振动提醒。</p><p>关键技术：<br>• <strong>GPS漂移过滤</strong>：卡尔曼滤波算法消除定位误差<br>• <strong>路线匹配</strong>：GPS点投影到最近的公交线路上<br>• <strong>到站预测</strong>：结合历史数据预估到达时间</p>'
        },
        {
            // 功能3：药品识别 - OCR智能识别药盒信息
            emoji: '💊', name: '药品识别',
            tag: '📸 OCR智能识别',
            body: '<p><strong>药品识别</strong>基于CameraX + ML Kit OCR技术，实现药盒信息的智能提取。</p><p>使用流程：<br>1. 打开相机对准药盒<br>2. OCR引擎实时识别中文文字<br>3. 提取药品名称、规格、生产日期<br>4. 与本地<strong>56种常用药品库</strong>进行模糊匹配<br>5. 自动显示药品用途、用法用量、风险等级</p><p>亮点：<strong>完全离线</strong>，无需联网即可使用，保护用户隐私。</p>'
        },
        {
            // 功能4：手语沟通 - MediaPipe手势识别与双向互转
            emoji: '🤟', name: '手语沟通',
            tag: '👋 MediaPipe手势识别',
            body: '<p><strong>手语沟通</strong>基于MediaPipe Hands实现手势识别。</p><p>技术原理：<br>• <strong>21个手部关键点</strong>实时追踪，每秒30帧<br>• 识别<strong>117条高频手语短语</strong><br>• 语音⇄手语双向互转</p><p>使用方式：<br>1. 开启摄像头，系统自动识别手语动作<br>2. 将手语转换为文字显示在屏幕上<br>3. 点击文字可转换为手语动画演示<br>4. 支持自定义手语短语库</p>'
        },
        {
            // 功能5：强提醒 - 三重警报系统（振动/闪光/声音）
            emoji: '🔔', name: '强提醒',
            tag: '⚡ 三重警报系统',
            body: '<p><strong>强提醒</strong>提供振动/闪光/声音三重警报，确保用户不会错过任何重要信息。</p><p>四级风险等级：<br>• <strong>低风险</strong>（绿色）：轻柔短振2次<br>• <strong>中风险</strong>（黄色）：规律中振5次 + 屏幕闪烁<br>• <strong>高风险</strong>（橙色）：急促强振 + 闪光灯闪烁<br>• <strong>紧急</strong>（红色）：三重警报全开 + 最大音量</p><p>支持<strong>自定义波形</strong>：可调节振动时长、间隔、强度。</p>'
        },
        {
            // 功能6：学习中心 - 手语教学与安全知识课程
            emoji: '📚', name: '学习中心',
            tag: '📖 手语教学平台',
            body: '<p><strong>学习中心</strong>提供交通规则和应急避险课程，采用图文+手语双语教学。</p><p>课程内容：<br>• <strong>交通规则</strong>：红绿灯识别、斑马线使用、公交乘车规范<br>• <strong>应急避险</strong>：火灾逃生、地震避险、溺水自救<br>• <strong>日常沟通</strong>：购物、就医、乘车等场景用语</p><p>每个知识点配有<strong>手语视频演示</strong> + 文字说明 + 图解示意。</p>'
        },
        {
            // 功能7：场景文字识别 - ML Kit图像识别提取环境文字
            emoji: '👁️', name: '场景文字识别',
            tag: '🔍 ML Kit图像识别',
            body: '<p><strong>场景文字识别</strong>基于ML Kit图像识别技术，自动提取环境中的文字信息。</p><p>识别范围：<br>• <strong>路牌</strong>：街道名称、方向指示<br>• <strong>告示</strong>：公告、通知、警示<br>• <strong>菜单</strong>：餐厅菜单、价目表<br>• <strong>标签</strong>：商品信息、说明书</p><p>开启摄像头后，系统自动扫描画面中的文字区域，实时转化为手语动画或文字提示。</p>'
        },
        {
            // 功能8：障碍物检测 - 距离感应与振动反馈
            emoji: '🚧', name: '障碍物检测',
            tag: '🛡️ 距离感应系统',
            body: '<p><strong>障碍物检测</strong>利用手机传感器实现前后方向的障碍物识别。</p><p>技术原理：<br>• <strong>超声波测距</strong>：利用手机扬声器和麦克风进行回波测距<br>• <strong>振动反馈</strong>：距离越近，振动频率越高<br>• <strong>多级警报</strong>：1米/0.5米/0.2米三级提醒</p><p>使用场景：行走时检测前方障碍物、上下楼梯时检测台阶、进出电梯时提示门的位置。</p>'
        },
        {
            // 功能9：语音转文字 - 全局实时语音转写服务
            emoji: '🎤', name: '语音转文字',
            tag: '🗣️ SpeechRecognizer',
            body: '<p><strong>语音转文字</strong>基于Android SpeechRecognizer API，实现全局实时语音转写。</p><p>技术特点：<br>• <strong>常驻服务</strong>：后台持续运行，随时可用<br>• <strong>离线识别</strong>：无需联网，支持中英文<br>• <strong>低延迟</strong>：识别延迟小于200ms<br>• <strong>高准确率</strong>：针对嘈杂环境优化</p><p>与他人对话时，系统自动将语音转换为文字显示在屏幕上。</p>'
        },
        {
            // 功能10：基地帮扶 - 线下无障碍服务平台
            emoji: '🏠', name: '基地帮扶',
            tag: '🤝 无障碍服务平台',
            body: '<p><strong>基地帮扶</strong>连接线下服务资源，为听障用户提供手语翻译和陪同出行服务。</p><p>服务内容：<br>• <strong>手语翻译预约</strong>：在线预约专业手语翻译员<br>• <strong>陪同出行</strong>：预约志愿者陪同就医、办事<br>• <strong>设备申领</strong>：申请无障碍辅助设备</p><p>预约流程：选择服务类型和时间 → 填写具体需求 → 系统匹配最近的服务人员 → 确认预约并等待服务。</p>'
        },
        {
            // 功能11：语音助手 - 7大品牌语音唤醒
            emoji: '🗣️', name: '语音助手',
            tag: '🔊 多品牌语音唤醒',
            body: '<p><strong>语音助手</strong>支持7大品牌手机的语音唤醒功能。</p><p>支持品牌：<br>• 小爱同学（小米）<br>• 小艺（华为）<br>• 小布（OPPO）<br>• YOYO（荣耀）<br>• Jovi（vivo）<br>• 三星Bixby<br>• 通用语音助手</p><p>说出唤醒词后，可以直接语音指令打开微光畅行的任意功能。</p>'
        },
        {
            // 功能12：TTS播报 - 全局离线语音合成播报
            emoji: '🔊', name: 'TTS播报',
            tag: '🔈 离线语音合成',
            body: '<p><strong>TTS播报</strong>基于Android TextToSpeech API，实现全局离线语音播报。</p><p>技术特点：<br>• <strong>离线合成</strong>：无需联网，响应速度小于200ms<br>• <strong>多语言</strong>：支持中文、英文、日文<br>• <strong>可调节</strong>：语速、音调、音量均可自定义<br>• <strong>低功耗</strong>：后台运行仅占用5% CPU</p><p>应用场景：收到文字消息时自动朗读、手语识别结果语音播报、导航提示语音播报。</p>'
        }
    ];


    /* ───────────────────────────────────────────────────────────────────────
       7.2 Chat Topics（对话知识库）
       定义聊天系统的预设对话内容和导航按钮
       用户点击按钮后，系统根据 buttonMap 映射到对应话题并回复
       ─────────────────────────────────────────────────────────────────────── */

    /**
     * chatTopics - 对话话题数据对象
     * 每个话题包含：
     *   - msg: 吉祥物回复的消息文本
     *   - buttons: 消息下方显示的导航按钮文字数组
     *   - showFeatureCards:（可选）是否显示功能卡片网格
     */
    var chatTopics = {
        // 欢迎消息 - 首次打开聊天窗口时显示
        welcome: {
            msg: '你好呀！👋 我是小微，微光畅行的智能助手。很高兴认识你！有什么想了解的，尽管问我~',
            buttons: ['关于微光', '核心功能', '技术亮点', '下载App']
        },
        // 关于微光 - 产品简介
        about: {
            msg: '微光畅行是一款专为听障与言语障碍人群打造的智能沟通助手 🌟 融合手语翻译、语音转文字、文字播报等前沿技术，让无声的世界也能畅快交流。完全免费，无需注册！',
            buttons: ['核心功能', '技术亮点', '下载App', '返回']
        },
        // 核心功能 - 展示 12 个功能卡片
        features: {
            msg: '我们有12大核心功能模块，点击下方卡片可以查看每个功能的详细原理 👇',
            buttons: ['技术亮点', '下载App', '返回'],
            showFeatureCards: true // 标记需要渲染功能卡片网格
        },
        // 技术亮点 - 技术栈概览
        tech: {
            msg: '技术栈超硬核！💪\n\n• CameraX + ML Kit OCR 实时识别\n• MediaPipe Hands 21个手部关键点\n• SpeechRecognizer 离线语音\n• Kotlin + Coroutines 全异步\n• APK仅8MB，兼容Android 5.0+',
            buttons: ['核心功能', '下载App', '返回']
        },
        // 下载引导
        download: {
            msg: '下载超简单！📱\n\n点击页面底部的「下载 APK」按钮即可获取，完全免费，无需注册。支持 Android 5.0 及以上所有设备。',
            buttons: ['核心功能', '技术亮点', '返回']
        },
        // 感谢回复
        thanks: {
            msg: '不客气！😊 如果有任何问题，随时点击我聊天哦。祝你使用愉快！',
            buttons: ['核心功能', '技术亮点', '下载App']
        }
    };

    /**
     * buttonMap - 按钮文字到话题键名的映射表
     * 用户点击按钮时，通过此映射表找到对应的话题键名
     */
    var buttonMap = {
        '关于微光': 'about',   // 跳转到"关于微光"话题
        '核心功能': 'features', // 跳转到"核心功能"话题
        '技术亮点': 'tech',     // 跳转到"技术亮点"话题
        '下载App': 'download',  // 跳转到"下载引导"话题
        '返回': 'welcome'       // 返回到欢迎话题
    };


    /* ───────────────────────────────────────────────────────────────────────
       7.3 Message Functions（消息函数）
       负责聊天消息的添加、打字动画的显示/隐藏
       ─────────────────────────────────────────────────────────────────────── */

    /**
     * addMessage(text, isUser) - 向聊天窗口添加一条消息
     * @param {string} text - 消息文本内容
     * @param {boolean} isUser - 是否为用户发送的消息（true=用户消息靠右，false=吉祥物消息靠左）
     * 逻辑：
     *   1. 创建消息 div 元素
     *   2. 根据 isUser 添加不同的 CSS 类名
     *   3. 将消息文本设置为元素内容（使用 textContent 防止 XSS）
     *   4. 追加到聊天消息区域并自动滚动到底部
     */
    function addMessage(text, isUser) {
        var div = document.createElement('div');
        // 用户消息添加 'user-msg' 类名（CSS 控制靠右显示和背景色）
        div.className = 'mascot-chat-msg' + (isUser ? ' user-msg' : '');
        div.textContent = text; // 使用 textContent 安全地设置文本（不解析 HTML）
        mascotChatBody.appendChild(div);
        // 自动滚动到消息区域底部，确保最新消息可见
        mascotChatBody.scrollTop = mascotChatBody.scrollHeight;
    }

    /**
     * showTyping() - 显示"正在输入"动画指示器
     * 在吉祥物回复消息前显示三个跳动的圆点，模拟打字效果
     */
    function showTyping() {
        var div = document.createElement('div');
        div.className = 'mascot-typing'; // CSS 控制三个圆点的跳动动画
        div.id = 'typingIndicator'; // 设置 id 以便后续移除
        // 三个 span 元素分别代表三个跳动的圆点
        div.innerHTML = '<span></span><span></span><span></span>';
        mascotChatBody.appendChild(div);
        mascotChatBody.scrollTop = mascotChatBody.scrollHeight;
    }

    /**
     * hideTyping() - 隐藏"正在输入"动画指示器
     * 从 DOM 中移除打字动画元素
     */
    function hideTyping() {
        var typing = document.getElementById('typingIndicator');
        if (typing) typing.remove();
    }


    /* ───────────────────────────────────────────────────────────────────────
       7.4 Feature Cards（功能卡片渲染）
       在聊天窗口中渲染 12 个功能卡片的网格布局
       用户点击卡片可打开对应的功能原理弹窗
       ─────────────────────────────────────────────────────────────────────── */

    /**
     * renderFeatureCards() - 渲染功能卡片网格
     * 在聊天消息区域创建一个网格容器，为 featureData 中的每个功能生成一个卡片
     * 每个卡片包含 emoji、功能名称和"查看原理"按钮
     * 点击卡片触发 openFeatModal() 打开对应的功能详情弹窗
     */
    function renderFeatureCards() {
        // 创建功能卡片网格容器
        var grid = document.createElement('div');
        grid.className = 'chat-feature-grid'; // CSS 控制网格布局（如 CSS Grid 或 Flexbox）
        // 遍历所有功能数据，为每个功能创建一张卡片
        featureData.forEach(function(feat, idx) {
            var card = document.createElement('div');
            card.className = 'chat-feature-card';
            // 卡片内容：emoji + 功能名称 + "查看原理"按钮
            card.innerHTML = '<span class="feat-emoji">' + feat.emoji + '</span>' +
                '<div class="feat-name">' + feat.name + '</div>' +
                '<span class="feat-btn">查看原理</span>';
            // 为卡片绑定点击事件，打开对应功能的详情弹窗
            card.addEventListener('click', function() {
                openFeatModal(idx); // idx 为当前功能在 featureData 中的索引
            });
            grid.appendChild(card);
        });
        // 将网格容器添加到聊天消息区域
        mascotChatBody.appendChild(grid);
        mascotChatBody.scrollTop = mascotChatBody.scrollHeight;
    }


    /* ───────────────────────────────────────────────────────────────────────
       7.5 Feature Modal（功能原理弹窗）
       点击功能卡片后弹出详细说明弹窗，展示功能的技术原理和使用方法
       ─────────────────────────────────────────────────────────────────────── */

    /**
     * openFeatModal(index) - 打开功能详情弹窗
     * @param {number} index - featureData 数组中的索引值
     * 逻辑：
     *   1. 根据索引从 featureData 获取功能数据
     *   2. 将数据填充到弹窗各元素
     *   3. 显示弹窗并锁定页面滚动
     */
    function openFeatModal(index) {
        var feat = featureData[index];
        featModalEmoji.textContent = feat.emoji;  // 设置功能 emoji
        featModalTitle.textContent = feat.name;    // 设置功能名称
        featModalTag.textContent = feat.tag;       // 设置分类标签
        featModalBody.innerHTML = feat.body;       // 设置详细内容（支持 HTML）
        // 显示弹窗遮罩层（CSS transition 控制淡入动画）
        featModalOverlay.classList.add('active');
        document.body.style.overflow = 'hidden';   // 锁定页面滚动
    }

    /**
     * closeFeatModal() - 关闭功能详情弹窗
     * 移除弹窗遮罩层的 'active' 类名，恢复页面滚动
     */
    function closeFeatModal() {
        featModalOverlay.classList.remove('active');
        document.body.style.overflow = ''; // 恢复页面滚动
    }


    /* ───────────────────────────────────────────────────────────────────────
       7.6 Chat Navigation（聊天导航按钮）
       渲染和处理聊天窗口底部的导航按钮
       用户点击按钮后触发话题切换和消息回复
       ─────────────────────────────────────────────────────────────────────── */

    /**
     * renderButtons(buttons) - 渲染导航按钮组
     * @param {string[]} buttons - 按钮文字数组
     * 逻辑：
     *   1. 清空按钮区域现有内容
     *   2. 遍历按钮数组，为每个按钮创建 button 元素
     *   3. 绑定点击事件到 handleUserChoice 函数
     */
    function renderButtons(buttons) {
        // 清空操作按钮区域
        mascotChatActions.innerHTML = '';
        buttons.forEach(function(text) {
            var btn = document.createElement('button');
            btn.textContent = text;
            // 点击按钮时调用 handleUserChoice 处理用户选择
            btn.addEventListener('click', function() {
                handleUserChoice(text);
            });
            mascotChatActions.appendChild(btn);
        });
    }

    /**
     * handleUserChoice(buttonText) - 处理用户点击导航按钮的选择
     * @param {string} buttonText - 用户点击的按钮文字
     * 逻辑：
     *   1. 通过 buttonMap 将按钮文字映射到话题键名
     *   2. 将用户的选择作为用户消息显示
     *   3. 显示"正在输入"动画
     *   4. 延迟 600-1000ms 后（模拟思考时间）显示吉祥物的回复
     *   5. 如果话题包含功能卡片，渲染功能卡片网格
     *   6. 渲染新的话题导航按钮
     */
    function handleUserChoice(buttonText) {
        // 通过映射表查找对应的话题键名
        var topicKey = buttonMap[buttonText];
        if (!topicKey) return; // 如果没有匹配的话题则不处理

        // 将用户的选择作为用户消息显示在聊天窗口中
        addMessage(buttonText, true);
        // 清空当前的导航按钮
        mascotChatActions.innerHTML = '';
        // 显示"正在输入"动画
        showTyping();

        // 延迟 600-1000ms（随机延迟模拟自然对话节奏）后回复
        setTimeout(function() {
            hideTyping(); // 移除"正在输入"动画
            // 获取对应话题的回复数据
            var topic = chatTopics[topicKey];
            // 显示吉祥物的回复消息
            addMessage(topic.msg, false);
            // 如果该话题需要显示功能卡片（如"核心功能"话题）
            if (topic.showFeatureCards) {
                renderFeatureCards();
            }
            // 渲染该话题的导航按钮
            renderButtons(topic.buttons);
        }, 600 + Math.random() * 400); // 随机延迟 600~1000ms
    }


    /* ───────────────────────────────────────────────────────────────────────
       7.7 Chat Window Open/Close（聊天窗口打开/关闭控制）
       控制聊天窗口的显示/隐藏，以及首次打开时的欢迎消息
       ─────────────────────────────────────────────────────────────────────── */

    /**
     * openChat() - 打开聊天窗口
     * 逻辑：
     *   1. 标记聊天窗口为打开状态
     *   2. 给聊天窗口添加 'active' 类名显示
     *   3. 隐藏未读消息徽章
     *   4. 如果是首次打开，显示"正在输入"动画后发送欢迎消息
     */
    function openChat() {
        chatOpen = true;
        mascotChat.classList.add('active'); // CSS 控制聊天窗口显示动画
        mascotBadge.style.display = 'none'; // 隐藏右上角的未读消息徽章

        // 仅在首次打开时发送欢迎消息
        if (!welcomeSent) {
            welcomeSent = true; // 标记已发送欢迎消息
            showTyping(); // 显示"正在输入"动画
            setTimeout(function() {
                hideTyping();
                var welcome = chatTopics.welcome;
                addMessage(welcome.msg, false); // 显示欢迎消息
                renderButtons(welcome.buttons); // 渲染导航按钮
            }, 800); // 延迟 800ms 模拟打字时间
        }
    }

    /**
     * closeChat() - 关闭聊天窗口
     * 标记聊天窗口为关闭状态，移除 'active' 类名隐藏窗口
     */
    function closeChat() {
        chatOpen = false;
        mascotChat.classList.remove('active'); // CSS 控制聊天窗口淡出动画
    }


    /* ───────────────────────────────────────────────────────────────────────
       7.8 Event Bindings（事件绑定）
       绑定吉祥物头像点击、聊天窗口关闭按钮、功能弹窗关闭等事件
       ─────────────────────────────────────────────────────────────────────── */

    /**
     * 吉祥物头像点击事件
     * 如果聊天窗口已打开则关闭，如果已关闭则打开
     */
    mascotBody.addEventListener('click', function() {
        if (chatOpen) { closeChat(); } else { openChat(); }
    });

    /**
     * 聊天窗口关闭按钮点击事件
     * 使用 stopPropagation 阻止事件冒泡到 mascotBody（避免触发头像点击事件）
     */
    mascotChatClose.addEventListener('click', function(e) {
        e.stopPropagation(); // 阻止事件冒泡
        closeChat();
    });

    /**
     * 功能弹窗关闭按钮点击事件
     */
    featModalClose.addEventListener('click', closeFeatModal);

    /**
     * 功能弹窗遮罩层点击事件
     * 只有点击遮罩层本身（而非弹窗内容）才关闭
     */
    featModalOverlay.addEventListener('click', function(e) {
        if (e.target === featModalOverlay) closeFeatModal();
    });

    /**
     * 功能弹窗 Escape 键关闭事件
     * 当功能弹窗处于打开状态时，按 Escape 键关闭
     */
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && featModalOverlay.classList.contains('active')) {
            closeFeatModal();
        }
    });

    /**
     * 延迟显示吉祥物未读消息徽章
     * 页面加载 3 秒后，如果聊天窗口未打开，则显示头像右上角的未读消息徽章
     * 作用：引导用户点击吉祥物开始聊天
     */
    setTimeout(function() {
        if (!chatOpen) { mascotBadge.style.display = 'flex'; }
    }, 3000);
})();
