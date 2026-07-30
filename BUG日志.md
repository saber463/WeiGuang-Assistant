# 项目开发BUG排查日志

> 时区标准：UTC+8 北京时间
> 使用规范：出现同类BUG优先查阅历史记录，无匹配方案再新增记录
> 记录格式：【时间】【BUG分类】问题现象 | 根因分析 | 最终修复方案

---

## 历史BUG记录区

### 【2026-06-07 14:30】【加密解密】网页端加密后无法解密
**问题现象**：
用户在fensafe-demo.html网页上可以正常加密数据，但点击"解密"按钮后无反应或输出乱码，无法还原明文。

**报错/根源原因**：
`doEncrypt()`函数在返回结果时执行了`res.iv.concat(res.ciphertext)`，但`res.ciphertext`内部已经包含了IV前缀。导致IV被双重拼接，解密时提取的IV位置偏移，密文解析错误。

**修复解决办法**：
修改`doEncrypt()`的return语句，直接使用`res.ciphertext`作为最终输出，不再手动拼接IV：
```javascript
// 修改前
return { iv: iv, ciphertext: iv.concat(encrypted) };
// 解密时错误地再次拼接: res.iv.concat(res.ciphertext)

// 修改后
return { ciphertext: iv.concat(encrypted) };  // IV已包含在内
// 解密时直接使用: res.ciphertext
```

---

### 【2026-06-07 15:45】【前端渲染】Three.js 3D页面所有按钮无响应
**问题现象**：
新创建的3D算法演示页面(fensafe-3d-demo.html)加载后，OrbitControls报404错误，页面上的所有交互按钮(开始/暂停/重置/单步)均无法点击，控制台报错`Uncaught ReferenceError: OrbitControls is not defined`。

**报错/根源原因**：
使用了Three.js r152版本的CDN链接，但从r152开始，Three.js将`/examples/js/controls/OrbitControls.js`从非模块化构建中移除，仅保留ES Module版本。旧版script标签引入方式找不到该文件。

**修复解决办法**：
降级Three.js版本至r128（最后一个包含非模块化OrbitControls的稳定版本）：
```html
<!-- 修改前 (r152+) -->
<script src="https://cdnjs.cloudflare.com/ajax/libs/three.js/r152/three.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/three@0.152.0/examples/js/controls/OrbitControls.js"></script>

<!-- 修改后 (r128) -->
<script src="https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/examples/js/controls/OrbitControls.js"></script>
```

---

### 【2026-06-07 17:20】【UI交互】雪崩演示按钮点击无效 + 数据面板收起后无法展开
**问题现象**：
1. "运行雪崩测试"按钮点击后无任何响应
2. 中间状态面板折叠后再次点击"展开"按钮无法恢复显示

**报错/根源原因**：
1. 雪崩按钮的事件绑定代码位于IIFE内部，如果IIFE之前的任何JavaScript代码抛出异常，整个IIFE不会执行，导致事件绑定丢失。
2. 数据面板使用`max-height: 0 !important`配合`overflow: hidden`实现折叠，但CSS transition恢复时`max-height`值不确定，导致展开失败。

**修复解决办法**：
1. 为雪崩按钮添加全局IIFE后备绑定（独立于主IIFE，带try-catch保护）：
```javascript
// 全局后备绑定
(function() {
    try {
        var btn = document.getElementById('runAvalancheTest');
        if (btn) btn.addEventListener('click', function() { /* ... */ });
    } catch(e) { console.warn('Avalanche fallback bind failed:', e); }
})();
```
2. 将CSS折叠方案从`max-height`改为`display: none/block`：
```css
/* 修改前 */
.data-panel.collapsed { max-height: 0 !important; overflow: hidden; }

/* 修改后 */
.data-panel.collapsed { display: none !important; }
.data-panel { display: block !important; }
```

---

### 【2026-06-08 08:35】【Java编译】volatile修饰符不能用于局部变量
**问题现象**：
编译FENSBox.java时报错：`illegal start of expression`，指向`injectRandomDelay()`方法内的`volatile int sink = 0;`这一行。

**报错/根源原因**：
Java语言规范规定`volatile`关键字只能用于字段（类成员变量）级别，不能用于方法内部的局部变量。原设计意图是用volatile防止编译器优化掉空循环（用于抵抗时序侧信道攻击），但在局部变量上下文中语法不合法。

**修复解决办法**：
使用`final int[]`数组模拟volatile语义——数组元素通过堆内存访问，JVM难以将其优化消除：
```java
// 修改前 (编译错误)
private static void injectRandomDelay(...) {
    volatile int sink = 0;  // ❌ Java不允许
    for (int d = 0; d < delay; d++) {
        sink += d * 31 + 17;
    }
}

// 修改后 (编译通过)
private static void injectRandomDelay(...) {
    final int[] sink = {0};  // ✅ 数组访问防优化
    for (int d = 0; d < delay; d++) {
        sink[0] += d * 31 + 17;
    }
}
```

---

### 【2026-06-08 08:38】【加解密一致性】SECURE版ECB模式解密输出与原文不一致
**问题现象**：
LIGHT版(5轮)ECB加解密完全一致，但SECURE版(7轮)ECB模式下encryptBlockSecure生成的密文用decryptBlockSecure解密后得到的结果与原始明文不同。CBC和CTR模式不受影响。

**报错/根源原因**：
v1.2安全加固时，encryptBlockSecure已改为使用`bitFlipDynamic(state, roundKeys[round], round)`动态掩码，但decryptBlockSecure仍使用旧的`FENSBox.bitFlip(state)`静态掩码。加密和解密使用的BitFlip掩码不同步，导致每轮的状态变换不对称，最终解密结果错误。

根本原因：安全加固修改了加密端的BitFlip调用方式，遗漏了同步修改解密端。

**修复解决办法**：
将decryptBlockSecure中的两处`FENSBox.bitFlip(state)`全部替换为`FENSBox.bitFlipDynamic(state, roundKeys[round], round)`，确保加解密的掩码生成逻辑完全对称：
```java
// decryptBlockSecure 修改后
state = FENSBox.bitFlipDynamic(state, roundKeys[NR_SECURE], NR_SECURE);  // 第7轮逆向
// ... 循环内 ...
state = FENSBox.bitFlipDynamic(state, roundKeys[round], round);           // 第6-1轮逆向
```
验证：修复后1000组随机明文SECURE版ECB加解密100%一致。

---

### 【2026-06-08 08:39】【类型转换】int到byte赋值需显式强制转换
**问题现象**：
编译FENSBox.java和FENSafeV12TestSuite.java时报告5个`possible lossy conversion from int to byte`错误，涉及高阶掩码S盒方法的返回值赋值和字节数组初始化。

**报错/根源原因**：
Java中`int`类型(32位)向`byte`类型(8位)赋值时需要显式强制转换，因为int值的范围(-2^31~2^31-1)远超byte范围(-128~127)。虽然逻辑上这些值都在0~255范围内（经过& 0xFF掩码），但编译器无法在编译期确定这一点。

**修复解决办法**：
对所有int→byte赋值点添加`(byte)`强制转换：
```java
// FENSBox.java - 掩码S盒方法
outputShare2[i] = (byte)deriveOutputMask1st(mask1[i] & 0xFF, correction);
share2[i] = (byte)deriveOutputMask2nd_A(mask1[i] & 0xFF, correction);
share3[i] = (byte)deriveOutputMask2nd_B(mask2[i] & 0xFF, correction);

// 字节数组初始化 (0x80~0xFF范围的值)
new byte[]{ ..., (byte)0xAA, (byte)0xF0, ..., (byte)0x88 };
```

---

## 新增BUG填写模板

### 【YYYY-MM-DD HH:MM】【BUG分类】
**问题现象**：
描述运行报错、闪退、功能异常、编译失败、接口报错等完整表现

**报错/根源原因**：
源码问题、依赖版本冲突、权限缺失、配置错误、逻辑漏洞、环境变量、数据库字段异常等

**修复解决办法**：
具体代码修改、配置调整、依赖降级/升级、权限开启、逻辑改写、清理缓存等可直接复用步骤
