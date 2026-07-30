/**
 * ============================================================================
 *  微光畅行 - 宣传网页交互脚本 (promote.js)
 * ============================================================================
 *  本文件包含宣传页的所有前端交互逻辑：
 *    1. 导航栏滚动监听
 *    2. 滚动显示动画（IntersectionObserver）
 *    3. Hero区域光粒子效果
 *    4. 移动端菜单控制
 *  依赖：Bootstrap 5.3.2 JS Bundle（通过 CDN 加载）
 * ============================================================================
 */

/* ═══════════════════════════════════════════════════════════════════════════════
   Section 1: 导航栏滚动监听
   功能：页面滚动时给导航栏添加背景和阴影效果
   ═══════════════════════════════════════════════════════════════════════════════ */
window.addEventListener('scroll', function() {
    // 获取导航栏元素
    var nav = document.getElementById('promoNav');
    // 当滚动距离 > 50px 时添加 'scrolled' 类名（CSS控制背景变深+毛玻璃）
    if (nav) {
        nav.classList.toggle('scrolled', window.scrollY > 50);
    }
});

/* ═══════════════════════════════════════════════════════════════════════════════
   Section 2: 滚动显示动画
   功能：使用 IntersectionObserver 监听元素进入视口，添加 'visible' 类名触发动画
   原理：当目标元素与根元素（视口）交叉比例达到10%时，认为元素已进入视口
   ═══════════════════════════════════════════════════════════════════════════════ */
(function() {
    // 获取所有需要滚动动画的元素
    var reveals = document.querySelectorAll('.reveal-up, .reveal-left, .reveal-right');

    // 创建 IntersectionObserver 实例
    var observer = new IntersectionObserver(function(entries) {
        // 遍历所有被观察的元素
        entries.forEach(function(entry) {
            // 如果元素已进入视口（交叉比例 > 10%）
            if (entry.isIntersecting) {
                // 添加 'visible' 类名，触发 CSS 过渡动画
                entry.target.classList.add('visible');
                // 停止观察该元素（避免重复触发）
                observer.unobserve(entry.target);
            }
        });
    }, {
        threshold: 0.1  // 元素进入视口10%时触发
    });

    // 对所有目标元素开始观察
    reveals.forEach(function(el) {
        observer.observe(el);
    });
})();

/* ═══════════════════════════════════════════════════════════════════════════════
   Section 3: Hero区域光粒子效果
   功能：在Hero区域随机生成浮动的暖色光粒子，营造"微光"氛围
   原理：动态创建小圆形div元素，使用CSS动画实现浮动效果
   ═══════════════════════════════════════════════════════════════════════════════ */
(function() {
    // 获取粒子容器
    var container = document.getElementById('heroParticles');
    if (!container) return;

    // 粒子数量
    var particleCount = 20;

    // 循环创建粒子
    for (var i = 0; i < particleCount; i++) {
        // 创建粒子元素
        var particle = document.createElement('div');

        // 随机大小（3px - 8px）
        var size = Math.random() * 5 + 3;
        // 随机水平位置（0% - 100%）
        var left = Math.random() * 100;
        // 随机垂直位置（0% - 100%）
        var top = Math.random() * 100;
        // 随机动画延迟（0s - 8s）
        var delay = Math.random() * 8;
        // 随机动画时长（6s - 14s）
        var duration = Math.random() * 8 + 6;
        // 随机透明度（0.2 - 0.6）
        var opacity = Math.random() * 0.4 + 0.2;

        // 设置粒子样式
        particle.style.cssText = [
            'position: absolute',
            'width: ' + size + 'px',
            'height: ' + size + 'px',
            'left: ' + left + '%',
            'top: ' + top + '%',
            'background: rgba(212, 137, 60, ' + opacity + ')',
            'border-radius: 50%',
            'animation: particleFloat ' + duration + 's ' + delay + 's ease-in-out infinite',
            'pointer-events: none'
        ].join(';');

        // 添加到容器
        container.appendChild(particle);
    }

    // 动态注入粒子动画关键帧
    var style = document.createElement('style');
    style.textContent = '@keyframes particleFloat { 0%,100% { transform: translateY(0) scale(1); opacity: 0.3; } 50% { transform: translateY(-40px) scale(1.3); opacity: 0.8; } }';
    document.head.appendChild(style);
})();

/* ═══════════════════════════════════════════════════════════════════════════════
   Section 4: 移动端菜单控制
   功能：点击汉堡按钮打开/关闭全屏菜单
   ═══════════════════════════════════════════════════════════════════════════════ */
(function() {
    // 获取菜单按钮和菜单面板
    var menuBtn = document.getElementById('mobileMenuBtn');
    var mobileMenu = document.getElementById('mobileMenu');
    if (!menuBtn || !mobileMenu) return;

    // 点击按钮切换菜单显示/隐藏
    menuBtn.addEventListener('click', function() {
        mobileMenu.classList.toggle('active');
        // 切换图标（列表 ↔ 关闭）
        var icon = menuBtn.querySelector('i');
        if (mobileMenu.classList.contains('active')) {
            icon.className = 'bi bi-x-lg';
        } else {
            icon.className = 'bi bi-list';
        }
    });
})();

/**
 * 关闭移动端菜单
 * 用途：点击菜单链接后自动关闭菜单
 */
function closeMobileMenu() {
    var mobileMenu = document.getElementById('mobileMenu');
    var menuBtn = document.getElementById('mobileMenuBtn');
    if (mobileMenu) {
        mobileMenu.classList.remove('active');
    }
    if (menuBtn) {
        var icon = menuBtn.querySelector('i');
        if (icon) icon.className = 'bi bi-list';
    }
}
