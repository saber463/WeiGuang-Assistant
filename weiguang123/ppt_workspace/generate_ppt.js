// ============================================================
// 微光守护 - 项目路演PPT生成脚本
// 2026 AI助残创新创意大赛 · 创意赛道
// ============================================================

const PptxGenJS = require("pptxgenjs");
const pptx = new PptxGenJS();

// ── 全局设置 ──
pptx.defineLayout({ name: "WIDE", width: 13.33, height: 7.5 });
pptx.layout = "WIDE";
pptx.theme = {
  headFontFace: "Microsoft YaHei",
  bodyFontFace: "Microsoft YaHei",
};
pptx.author = "微光科技";
pptx.title = "微光守护 - 项目路演";

// ── 品牌色板 ──
const C = {
  primary: "FF6F00",       // 主色-橙
  primaryDark: "E65100",   // 深橙
  accent: "FF9800",        // 亮橙
  bg: "FFF8F0",            // 浅橙背景
  dark: "1a1a2e",          // 深色背景
  dark2: "16213e",         // 深蓝背景
  white: "FFFFFF",
  black: "1A1A1A",
  gray: "757575",
  lightGray: "F5F5F5",
  danger: "D32F2F",
  success: "2E7D32",
  warning: "F57C00",
  info: "1565C0",
  purple: "7B1FA2",
  cardBg: "FFFFFF",
};

// ── 通用辅助函数 ──
function addSlideNumber(slide, num, total) {
  slide.addText(`${num} / ${total}`, {
    x: 12.2, y: 7.1, w: 1, h: 0.3,
    fontSize: 9, color: C.gray, align: "right",
  });
}

function addFooter(slide, text) {
  slide.addText(text || "微光科技 · 2026 AI助残创新创意大赛", {
    x: 0.5, y: 7.1, w: 8, h: 0.3,
    fontSize: 8, color: C.gray,
  });
}

function addSectionTitle(slide, title, subtitle) {
  slide.addText(title, {
    x: 0.8, y: 0.3, w: 11, h: 0.6,
    fontSize: 28, fontFace: "Microsoft YaHei", bold: true, color: C.primaryDark,
  });
  if (subtitle) {
    slide.addText(subtitle, {
      x: 0.8, y: 0.9, w: 11, h: 0.4,
      fontSize: 13, color: C.gray,
    });
  }
  // 分隔线
  slide.addShape(pptx.ShapeType.rect, {
    x: 0.8, y: 1.35, w: 11.7, h: 0.03,
    fill: { color: C.primary },
  });
}

// ── 颜色主题覆盖函数 ──
function darkSlide(slide) {
  slide.background = { fill: C.dark };
}

function gradientSlide(slide, from, to) {
  slide.background = { fill: C.dark };
  // 添加渐变覆盖层
  slide.addShape(pptx.ShapeType.rect, {
    x: 0, y: 0, w: 13.33, h: 7.5,
    fill: { type: "solid", color: C.dark },
  });
}

const TOTAL_SLIDES = 13;
let slideNum = 0;

// ============================================================
// 幻灯片1: 封面
// ============================================================
slideNum++;
const s1 = pptx.addSlide();
s1.background = { fill: C.dark };
// 顶部装饰条
s1.addShape(pptx.ShapeType.rect, {
  x: 0, y: 0, w: 13.33, h: 0.08,
  fill: { color: C.primary },
});
// 大赛标签
s1.addShape(pptx.ShapeType.roundRect, {
  x: 4.5, y: 0.8, w: 4.33, h: 0.5,
  fill: { color: C.danger }, rectRadius: 0.25,
});
s1.addText("2026 AI助残创新创意大赛 · 创意赛道", {
  x: 4.5, y: 0.8, w: 4.33, h: 0.5,
  fontSize: 12, color: C.white, align: "center", bold: true,
});
// 项目名称
s1.addText("微光守护", {
  x: 1, y: 1.8, w: 11.33, h: 1.2,
  fontSize: 54, color: C.primary, bold: true, align: "center",
  fontFace: "Microsoft YaHei",
});
s1.addText("AI多模态无声安全预警系统", {
  x: 1, y: 3.0, w: 11.33, h: 0.6,
  fontSize: 22, color: C.white, align: "center",
});
// 口号
s1.addShape(pptx.ShapeType.rect, {
  x: 3.5, y: 3.9, w: 6.33, h: 0.04,
  fill: { color: C.primary },
});
s1.addText("以AI之手，搭建听障人士与世界沟通的桥梁", {
  x: 1, y: 4.1, w: 11.33, h: 0.6,
  fontSize: 16, color: C.accent, align: "center", italic: true,
});
// 团队信息
s1.addText("微光科技（WeiGuang Tech）", {
  x: 1, y: 5.3, w: 11.33, h: 0.5,
  fontSize: 18, color: C.white, align: "center", bold: true,
});
s1.addText("天府新区通用航空职业学院  |  2026年7月", {
  x: 1, y: 5.8, w: 11.33, h: 0.4,
  fontSize: 12, color: C.gray, align: "center",
});
// 底部装饰
s1.addShape(pptx.ShapeType.rect, {
  x: 0, y: 7.42, w: 13.33, h: 0.08,
  fill: { color: C.primary },
});

// ============================================================
// 幻灯片2: 目录
// ============================================================
slideNum++;
const s2 = pptx.addSlide();
s2.background = { fill: C.bg };
addSectionTitle(s2, "目录", "CONTENTS");
addFooter(s2);
addSlideNumber(s2, slideNum, TOTAL_SLIDES);

const tocItems = [
  ["01", "项目背景与痛点", "2700万听障人士的安全困境"],
  ["02", "产品方案设计", "六大核心模块 + 手势识别 + 商品识别"],
  ["03", "技术方案", "端侧AI + 自研加密引擎"],
  ["04", "项目创新点", "五大自研核心技术"],
  ["05", "团队介绍", "微光科技核心成员"],
  ["06", "商业模式与社会效益", "B2B2C + 科技向善"],
];

tocItems.forEach((item, i) => {
  const y = 1.8 + i * 0.8;
  s2.addShape(pptx.ShapeType.roundRect, {
    x: 1.5, y: y, w: 0.6, h: 0.5,
    fill: { color: C.primary }, rectRadius: 0.1,
  });
  s2.addText(item[0], {
    x: 1.5, y: y, w: 0.6, h: 0.5,
    fontSize: 16, color: C.white, align: "center", bold: true,
  });
  s2.addText(item[1], {
    x: 2.3, y: y, w: 4, h: 0.3,
    fontSize: 16, color: C.primaryDark, bold: true,
  });
  s2.addText(item[2], {
    x: 2.3, y: y + 0.28, w: 8, h: 0.25,
    fontSize: 11, color: C.gray,
  });
});

// ============================================================
// 幻灯片3: 项目背景 - 数据冲击
// ============================================================
slideNum++;
const s3 = pptx.addSlide();
s3.background = { fill: C.dark };
addFooter(s3);
addSlideNumber(s3, slideNum, TOTAL_SLIDES);

s3.addText("中国有超过 2700万 听障人士", {
  x: 0.5, y: 0.5, w: 12.33, h: 0.8,
  fontSize: 32, color: C.primary, bold: true, align: "center",
});
s3.addText("他们在日常生活中，面临着常人难以想象的安全威胁", {
  x: 0.5, y: 1.3, w: 12.33, h: 0.5,
  fontSize: 16, color: C.white, align: "center",
});

// 四个数据卡片
const stats = [
  { num: "2700万+", label: "听障人士", sub: "占全国残疾人约30%" },
  { num: "2-3万", label: "每年新增听障儿童", sub: "数据来源：中国残联" },
  { num: "95%+", label: "手机覆盖率", sub: "Android 7.0+设备" },
  { num: "0", label: "全场景AI方案", sub: "目前市场空白" },
];

stats.forEach((s, i) => {
  const x = 0.8 + i * 3.1;
  s3.addShape(pptx.ShapeType.roundRect, {
    x: x, y: 2.2, w: 2.7, h: 2.0,
    fill: { color: "FFFFFF" }, rectRadius: 0.15,
    shadow: { type: "outer", blur: 8, offset: 3, color: "000000", opacity: 0.3 },
  });
  s3.addText(s.num, {
    x: x, y: 2.4, w: 2.7, h: 0.7,
    fontSize: 28, color: C.primary, bold: true, align: "center",
  });
  s3.addText(s.label, {
    x: x, y: 3.1, w: 2.7, h: 0.4,
    fontSize: 14, color: C.black, bold: true, align: "center",
  });
  s3.addText(s.sub, {
    x: x, y: 3.5, w: 2.7, h: 0.3,
    fontSize: 10, color: C.gray, align: "center",
  });
});

// 底部痛点提示
s3.addText("听不见闹钟 · 听不见火警 · 感知不到燃气泄漏 · 察觉不到异常敲门 · 跌倒无法呼救", {
  x: 0.5, y: 4.8, w: 12.33, h: 0.5,
  fontSize: 14, color: C.accent, align: "center",
});
s3.addText('这些"听不见"的困境，每时每刻都在威胁着2700万听障人士的生命安全', {
  x: 0.5, y: 5.4, w: 12.33, h: 0.5,
  fontSize: 16, color: C.white, align: "center", bold: true,
});

// ============================================================
// 幻灯片4: 六大痛点
// ============================================================
slideNum++;
const s4 = pptx.addSlide();
s4.background = { fill: C.white };
addSectionTitle(s4, "六大核心痛点", "覆盖听障人士日常安全的所有关键场景");
addFooter(s4);
addSlideNumber(s4, slideNum, TOTAL_SLIDES);

const pains = [
  { icon: "⏰", title: "听不见闹钟", desc: "传统声音闹钟完全无效", level: "中", color: C.warning },
  { icon: "🔥", title: "听不见火警", desc: "生命安全受严重威胁", level: "极高", color: C.danger },
  { icon: "💨", title: "感知不到燃气泄漏", desc: "中毒和爆炸风险", level: "极高", color: C.danger },
  { icon: "🚪", title: "察觉不到异常敲门", desc: "居家安全存隐患", level: "高", color: C.warning },
  { icon: "🩺", title: "健康监测缺失", desc: "病理性咳嗽难以评估", level: "中", color: C.warning },
  { icon: "🆘", title: "跌倒无法呼救", desc: "错过黄金救援时间", level: "极高", color: C.danger },
];

pains.forEach((p, i) => {
  const col = i % 3;
  const row = Math.floor(i / 3);
  const x = 0.8 + col * 4.0;
  const y = 1.7 + row * 2.6;

  s4.addShape(pptx.ShapeType.roundRect, {
    x: x, y: y, w: 3.7, h: 2.2,
    fill: { color: C.lightGray }, rectRadius: 0.12,
    line: { color: p.color, width: 2 },
  });
  s4.addText(p.icon, {
    x: x, y: y + 0.2, w: 3.7, h: 0.5,
    fontSize: 28, align: "center",
  });
  s4.addText(p.title, {
    x: x, y: y + 0.75, w: 3.7, h: 0.35,
    fontSize: 15, bold: true, color: C.primaryDark, align: "center",
  });
  s4.addText(p.desc, {
    x: x, y: y + 1.15, w: 3.7, h: 0.3,
    fontSize: 11, color: C.gray, align: "center",
  });
  // 风险等级标签
  s4.addShape(pptx.ShapeType.roundRect, {
    x: x + 1.3, y: y + 1.55, w: 1.1, h: 0.35,
    fill: { color: p.color }, rectRadius: 0.15,
  });
  s4.addText(p.level, {
    x: x + 1.3, y: y + 1.55, w: 1.1, h: 0.35,
    fontSize: 10, color: C.white, align: "center", bold: true,
  });
});

// ============================================================
// 幻灯片5: 产品方案 - 六大模块
// ============================================================
slideNum++;
const s5 = pptx.addSlide();
s5.background = { fill: C.bg };
addSectionTitle(s5, "产品方案设计", "六大核心安全预警模块，覆盖听障人士全场景安全需求");
addFooter(s5);
addSlideNumber(s5, slideNum, TOTAL_SLIDES);

const modules = [
  { icon: "⏰", name: "触觉唤醒引擎", desc: "振动+闪光双通道唤醒\n三种振动模式·双重关闭验证", c: C.primary },
  { icon: "🔥", name: "火灾声纹预警", desc: "CNN+Mel频谱图\n准确率>95%·全屏红色预警", c: C.danger },
  { icon: "💨", name: "燃气泄漏预警", desc: "8kHz低频MFCC检测\n轻量CNN<500K·橙色预警", c: C.warning },
  { icon: "🩺", name: "咳嗽健康监测", desc: "15秒滑窗≥5次触发\n三级预警·健康闭环", c: "FFC107" },
  { icon: "🚪", name: "敲门安防预警", desc: "3秒密度分析算法\n七级安全评估·手语沟通", c: C.info },
  { icon: "🆘", name: "跌倒检测", desc: "传感器+摄像头双重确认\n30秒自动派发应急", c: C.purple },
];

modules.forEach((m, i) => {
  const col = i % 3;
  const row = Math.floor(i / 3);
  const x = 0.8 + col * 4.0;
  const y = 1.7 + row * 2.6;

  s5.addShape(pptx.ShapeType.roundRect, {
    x: x, y: y, w: 3.7, h: 2.2,
    fill: { color: C.white }, rectRadius: 0.12,
    shadow: { type: "outer", blur: 5, offset: 2, color: "000000", opacity: 0.08 },
    line: { color: m.c, width: 2 },
  });
  s5.addText(m.icon, {
    x: x, y: y + 0.15, w: 3.7, h: 0.45,
    fontSize: 26, align: "center",
  });
  s5.addText(m.name, {
    x: x + 0.2, y: y + 0.65, w: 3.3, h: 0.3,
    fontSize: 14, bold: true, color: C.primaryDark, align: "center",
  });
  s5.addText(m.desc, {
    x: x + 0.3, y: y + 1.05, w: 3.1, h: 0.85,
    fontSize: 10, color: C.gray, align: "center", lineSpacingMultiple: 1.4,
  });
});

// ============================================================
// 幻灯片6: 手势识别 + 商品识别
// ============================================================
slideNum++;
const s6 = pptx.addSlide();
s6.background = { fill: C.white };
addSectionTitle(s6, "手势识别与双向手语交互", "11种手势实时识别 · 100%端侧推理 · 识别速度<1ms");
addFooter(s6);
addSlideNumber(s6, slideNum, TOTAL_SLIDES);

// 手势列表
const gestures = [
  ["✊", "握拳", "SOS紧急求助"],
  ["✋", "手掌张开", "停止/问候"],
  ["👍", "竖大拇指", "确认/好的"],
  ["👎", "拇指朝下", "不好/拒绝"],
  ["☝️", "食指指向", "指向方向"],
  ["✌️", "剪刀手", "胜利/和平"],
  ["👌", "OK手势", "没问题"],
  ["👋", "摆手", "你好/再见"],
  ["🫶", "比心", "谢谢/爱你"],
  ["🤙", "打电话", "联系我"],
  ["😐", "无手势", "待机状态"],
];

// 手势表格
const headerRow = [
  { text: "手势", options: { bold: true, color: C.white, fill: { color: C.primary }, fontSize: 11, align: "center" } },
  { text: "名称", options: { bold: true, color: C.white, fill: { color: C.primary }, fontSize: 11, align: "center" } },
  { text: "含义", options: { bold: true, color: C.white, fill: { color: C.primary }, fontSize: 11, align: "center" } },
];

const gestureRows = gestures.map((g, i) => [
  { text: g[0], options: { fontSize: 16, align: "center", fill: { color: i === 0 ? "FFEBEE" : (i % 2 === 0 ? C.lightGray : C.white) } } },
  { text: g[1], options: { fontSize: 11, bold: i === 0, align: "center", fill: { color: i === 0 ? "FFEBEE" : (i % 2 === 0 ? C.lightGray : C.white) } } },
  { text: g[2], options: { fontSize: 11, align: "center", fill: { color: i === 0 ? "FFEBEE" : (i % 2 === 0 ? C.lightGray : C.white) } } },
]);

s6.addTable(
  [headerRow, ...gestureRows],
  {
    x: 0.6, y: 1.6, w: 5.5,
    colW: [1.2, 1.8, 2.5],
    rowH: [0.4, ...Array(11).fill(0.38)],
    border: { type: "solid", pt: 0.5, color: "E0E0E0" },
    autoPage: false,
  }
);

// 右侧：核心技术指标
s6.addShape(pptx.ShapeType.roundRect, {
  x: 6.5, y: 1.6, w: 6.2, h: 5.0,
  fill: { color: C.bg }, rectRadius: 0.12,
  line: { color: C.primary, width: 1.5 },
});

const techHighlights = [
  { title: "MediaPipe Hand Landmarker", desc: "21点手部关键点检测，Google开源模型" },
  { title: "GestureVectorDB", desc: "自研手势质心向量数据库，余弦相似度匹配" },
  { title: "识别速度 < 1ms", desc: "100个质心向量余弦相似度计算" },
  { title: "100% 端侧推理", desc: "无网络依赖，隐私数据不出设备" },
  { title: "SOS 握拳自动触发", desc: "TTS语音播报 + 紧急联系人通知 + 10秒防重复" },
  { title: "双向手语交互", desc: "手势→文字 识别 + 文字→手语 动画生成" },
  { title: "增量更新", desc: "支持100+手势扩展，无需重新安装APP" },
  { title: "80+ 商品词库", desc: "OCR商品识别，辅助购物独立生活" },
];

techHighlights.forEach((h, i) => {
  const y = 1.85 + i * 0.58;
  s6.addShape(pptx.ShapeType.ellipse, {
    x: 6.8, y: y + 0.05, w: 0.25, h: 0.25,
    fill: { color: C.primary },
  });
  s6.addText("✓", {
    x: 6.8, y: y + 0.05, w: 0.25, h: 0.25,
    fontSize: 10, color: C.white, align: "center",
  });
  s6.addText(h.title, {
    x: 7.15, y: y - 0.02, w: 5.3, h: 0.25,
    fontSize: 12, bold: true, color: C.primaryDark,
  });
  s6.addText(h.desc, {
    x: 7.15, y: y + 0.22, w: 5.3, h: 0.22,
    fontSize: 10, color: C.gray,
  });
});

// ============================================================
// 幻灯片7: 技术架构
// ============================================================
slideNum++;
const s7 = pptx.addSlide();
s7.background = { fill: C.bg };
addSectionTitle(s7, "技术架构", "端侧AI + 零知识加密 + 离线优先");
addFooter(s7);
addSlideNumber(s7, slideNum, TOTAL_SLIDES);

const layers = [
  { name: "应用层", tech: "Jetpack Compose + Material3", desc: "6大模块统一设计语言 · 适配Android 7.0+", color: C.primary },
  { name: "AI推理层", tech: "TensorFlow Lite + ONNX", desc: "CNN/RNN端侧推理 · INT8量化<2MB · 离线运行", color: C.info },
  { name: "数据层", tech: "Room + SQLite", desc: "本地持久化存储 · 数据不出设备 · WiFi仅用于密文同步", color: C.success },
  { name: "安全层", tech: "WeiGuang-Secure加密引擎", desc: "RSA-2048+AES-256-GCM · 零知识架构 · 服务端不可解密", color: C.danger },
];

layers.forEach((l, i) => {
  const y = 1.7 + i * 1.3;
  s7.addShape(pptx.ShapeType.roundRect, {
    x: 1.5, y: y, w: 10.3, h: 1.05,
    fill: { color: C.white }, rectRadius: 0.1,
    shadow: { type: "outer", blur: 4, offset: 2, color: "000000", opacity: 0.06 },
    line: { color: l.color, width: 3 },
  });
  // 层标签
  s7.addShape(pptx.ShapeType.roundRect, {
    x: 1.5, y: y, w: 1.3, h: 1.05,
    fill: { color: l.color }, rectRadius: 0.1,
  });
  s7.addText(l.name, {
    x: 1.5, y: y, w: 1.3, h: 1.05,
    fontSize: 13, color: C.white, bold: true, align: "center", valign: "middle",
  });
  s7.addText(l.tech, {
    x: 3.1, y: y + 0.1, w: 8.5, h: 0.35,
    fontSize: 15, bold: true, color: C.primaryDark,
  });
  s7.addText(l.desc, {
    x: 3.1, y: y + 0.5, w: 8.5, h: 0.35,
    fontSize: 11, color: C.gray,
  });
});

// 端侧 vs 云端对比
s7.addText("端侧推理优势", {
  x: 1.5, y: 6.5, w: 3, h: 0.3,
  fontSize: 12, bold: true, color: C.primaryDark,
});
const compareRows = [
  [
    { text: "", options: { fill: { color: C.primary }, fontSize: 10, bold: true, color: C.white } },
    { text: "云端推理", options: { fill: { color: C.primary }, fontSize: 10, bold: true, color: C.white, align: "center" } },
    { text: "端侧推理（本项目）", options: { fill: { color: C.primary }, fontSize: 10, bold: true, color: C.white, align: "center" } },
  ],
  ["网络依赖", "必须联网", "零网络依赖"],
  ["延迟", "100-500ms", "<50ms"],
  ["隐私保护", "数据上传云端", "数据不出设备"],
  ["成本", "持续服务费", "零运营成本"],
];
s7.addTable(compareRows.map((r, ri) =>
  r.map((c, ci) => {
    if (typeof c === 'object' && c.text !== undefined) return c;
    return {
      text: String(c),
      options: {
        fontSize: 9, align: ci > 0 ? "center" : "left",
        bold: ri === 0 || ci === 2,
        color: ri === 0 ? C.white : (ci === 2 ? C.success : C.black),
        fill: { color: ri === 0 ? C.primary : (ri % 2 === 0 ? C.lightGray : C.white) },
      },
    };
  })
), {
  x: 5, y: 6.3, w: 6.8,
  colW: [1.5, 1.8, 3.5],
  rowH: [0.3, 0.25, 0.25, 0.25, 0.25],
  border: { type: "solid", pt: 0.5, color: "E0E0E0" },
  autoPage: false,
});

// ============================================================
// 幻灯片8: 加密引擎
// ============================================================
slideNum++;
const s8 = pptx.addSlide();
s8.background = { fill: C.dark };
addFooter(s8, "微光科技 · WeiGuang-Secure 自研端到端加密引擎");
addSlideNumber(s8, slideNum, TOTAL_SLIDES);

s8.addText("WeiGuang-Secure 自研端到端加密引擎", {
  x: 0.5, y: 0.3, w: 12.33, h: 0.6,
  fontSize: 26, color: C.primary, bold: true, align: "center",
});
s8.addText("零知识架构 · 服务端无法解密任何用户数据", {
  x: 0.5, y: 0.9, w: 12.33, h: 0.4,
  fontSize: 14, color: C.white, align: "center",
});

// 三层加密架构
const encLayers = [
  { name: "RSA-2048 密钥交换", sub: "非对称加密保护AES会话密钥传输", param: "2048 bits · OAEP-SHA256", c: "FF9800" },
  { name: "AES-256-GCM 数据加密", sub: "对称加密保护业务数据机密性与完整性", param: "256 bits · 12B IV · 128位认证标签", c: "42A5F5" },
  { name: "Android Keystore 硬件保护", sub: "主密钥存于TEE可信执行环境", param: "不可导出 · 硬件隔离 · root也无法获取", c: "AB47BC" },
];

encLayers.forEach((l, i) => {
  const y = 1.6 + i * 1.2;
  s8.addShape(pptx.ShapeType.roundRect, {
    x: 1.0, y: y, w: 11.33, h: 0.95,
    fill: { color: "FFFFFF" }, rectRadius: 0.1,
    line: { color: l.c, width: 2 },
  });
  s8.addShape(pptx.ShapeType.roundRect, {
    x: 1.0, y: y, w: 0.08, h: 0.95,
    fill: { color: l.c }, rectRadius: 0.05,
  });
  s8.addText(`第${i + 1}层`, {
    x: 1.3, y: y + 0.1, w: 1.0, h: 0.3,
    fontSize: 10, color: C.gray,
  });
  s8.addText(l.name, {
    x: 1.3, y: y + 0.15, w: 4.5, h: 0.4,
    fontSize: 16, bold: true, color: C.primaryDark,
  });
  s8.addText(l.sub, {
    x: 1.3, y: y + 0.55, w: 5.5, h: 0.3,
    fontSize: 11, color: C.gray,
  });
  s8.addText(l.param, {
    x: 7.0, y: y + 0.2, w: 5.0, h: 0.55,
    fontSize: 11, color: C.black, valign: "middle",
  });
});

// 密钥轮换 + 雪崩效应
s8.addText("密钥轮换安全收益", {
  x: 1.0, y: 5.2, w: 5, h: 0.3,
  fontSize: 13, bold: true, color: C.primary,
});
const rotRows = [
  [
    { text: "场景", options: { fill: { color: C.primary }, fontSize: 9, bold: true, color: C.white } },
    { text: "无密钥轮换", options: { fill: { color: C.primary }, fontSize: 9, bold: true, color: C.white, align: "center" } },
    { text: "有密钥轮换（本项目）", options: { fill: { color: C.primary }, fontSize: 9, bold: true, color: C.white, align: "center" } },
  ],
  ["密钥泄露影响", "全部历史+未来数据", "仅最近5分钟窗口"],
  ["攻击时间窗口", "无限期", "最多5分钟"],
  ["暴力破解成本", "破解一次即可", "需每5分钟破解一次"],
];
s8.addTable(rotRows.map((r, ri) =>
  r.map((c, ci) => {
    if (typeof c === 'object' && c.text !== undefined) return c;
    return {
      text: String(c),
      options: {
        fontSize: 9, align: ci > 0 ? "center" : "left",
        bold: ri === 0 || ci === 2,
        color: ri === 0 ? C.white : (ci === 2 ? C.success : C.black),
        fill: { color: ri === 0 ? C.primary : (ri % 2 === 0 ? C.lightGray : C.white) },
      },
    };
  })
), {
  x: 1.0, y: 5.5, w: 5.5,
  colW: [1.5, 2.0, 2.0],
  rowH: [0.28, 0.25, 0.25, 0.25],
  border: { type: "solid", pt: 0.5, color: "E0E0E0" },
  autoPage: false,
});

// 雪崩效应测试结果
s8.addText("雪崩效应测试（AES-256-GCM）", {
  x: 7.0, y: 5.2, w: 5, h: 0.3,
  fontSize: 13, bold: true, color: C.primary,
});
const avRows = [
  [
    { text: "测试类型", options: { fill: { color: C.primary }, fontSize: 9, bold: true, color: C.white } },
    { text: "实测值", options: { fill: { color: C.primary }, fontSize: 9, bold: true, color: C.white, align: "center" } },
    { text: "结论", options: { fill: { color: C.primary }, fontSize: 9, bold: true, color: C.white, align: "center" } },
  ],
  ["密钥雪崩", "49.98%", "✓ 通过"],
  ["明文雪崩", "50.04%", "✓ 通过"],
  ["IV雪崩", "49.96%", "✓ 通过"],
  ["认证标签", "100%不匹配", "✓ 通过"],
];
s8.addTable(avRows.map((r, ri) =>
  r.map((c, ci) => {
    if (typeof c === 'object' && c.text !== undefined) return c;
    return {
      text: String(c),
      options: {
        fontSize: 9, align: ci > 0 ? "center" : "left",
        bold: ri === 0,
        color: ri === 0 ? C.white : (ci === 2 ? C.success : C.black),
        fill: { color: ri === 0 ? C.primary : (ri % 2 === 0 ? C.lightGray : C.white) },
      },
    };
  })
), {
  x: 7.0, y: 5.5, w: 5.5,
  colW: [1.5, 1.8, 2.2],
  rowH: [0.28, 0.25, 0.25, 0.25, 0.25],
  border: { type: "solid", pt: 0.5, color: "E0E0E0" },
  autoPage: false,
});

// ============================================================
// 幻灯片9: 创新点
// ============================================================
slideNum++;
const s9 = pptx.addSlide();
s9.background = { fill: C.white };
addSectionTitle(s9, "项目创新点", "五大自研核心技术 + 三大模式创新");
addFooter(s9);
addSlideNumber(s9, slideNum, TOTAL_SLIDES);

const innovations = [
  { num: "01", title: "WeiGuang-Secure加密引擎", desc: "RSA-2048 + AES-256-GCM + Android Keystore三层加密\n零知识架构 · 完美前向安全性 · 5分钟密钥轮换" },
  { num: "02", title: "GestureVectorDB手势向量数据库", desc: "11种手势质心向量存储与匹配\n余弦相似度<1ms · 支持100+手势增量更新" },
  { num: "03", title: "端侧AI全离线推理", desc: "CNN/RNN神经网络INT8量化\n模型<2MB · 推理<50ms · 100%本地运行" },
  { num: "04", title: "多模态融合检测", desc: "传感器+摄像头双重确认（跌倒检测）\n音频+振动+视觉三通道联合预警" },
  { num: "05", title: "双向手语交互系统", desc: "手势→文字：实时识别\n文字→手势：动画生成 · SOS自动TTS播报" },
];

innovations.forEach((inn, i) => {
  const col = i < 3 ? 0 : 1;
  const row = i < 3 ? i : i - 3;
  const x = 0.8 + col * 6.2;
  const y = 1.7 + row * 1.75;

  s9.addShape(pptx.ShapeType.roundRect, {
    x: x, y: y, w: 5.8, h: 1.5,
    fill: { color: C.bg }, rectRadius: 0.1,
    line: { color: C.primary, width: 1 },
  });
  s9.addShape(pptx.ShapeType.roundRect, {
    x: x + 0.15, y: y + 0.15, w: 0.55, h: 0.45,
    fill: { color: C.primary }, rectRadius: 0.08,
  });
  s9.addText(inn.num, {
    x: x + 0.15, y: y + 0.15, w: 0.55, h: 0.45,
    fontSize: 18, color: C.white, bold: true, align: "center",
  });
  s9.addText(inn.title, {
    x: x + 0.85, y: y + 0.12, w: 4.7, h: 0.35,
    fontSize: 14, bold: true, color: C.primaryDark,
  });
  s9.addText(inn.desc, {
    x: x + 0.85, y: y + 0.5, w: 4.7, h: 0.8,
    fontSize: 10, color: C.gray, lineSpacingMultiple: 1.4,
  });
});

// ============================================================
// 幻灯片10: 商业模式
// ============================================================
slideNum++;
const s10 = pptx.addSlide();
s10.background = { fill: C.bg };
addSectionTitle(s10, "商业模式与市场策略", "B2B2C + 免费增值，三线并行覆盖目标用户");
addFooter(s10);
addSlideNumber(s10, slideNum, TOTAL_SLIDES);

const bizModels = [
  { icon: "🏛️", title: "残联/社区合作", desc: "与各地残联、社区服务中心合作推广\n政府采购或补贴模式", c: C.primary },
  { icon: "📱", title: "免费增值模式", desc: "基础功能免费，高级功能付费\n唤醒+火灾+燃气免费，其他付费", c: C.info },
  { icon: "🏭", title: "硬件预装合作", desc: "与手机厂商/智能家居品牌合作\n系统级预装，触达更多用户", c: C.success },
  { icon: "🌍", title: "开源社区生态", desc: "核心算法Apache 2.0开源\n吸引开发者贡献，接受安全审计", c: C.purple },
];

bizModels.forEach((b, i) => {
  const x = 0.8 + i * 3.1;
  s10.addShape(pptx.ShapeType.roundRect, {
    x: x, y: 1.8, w: 2.8, h: 3.5,
    fill: { color: C.white }, rectRadius: 0.12,
    shadow: { type: "outer", blur: 5, offset: 2, color: "000000", opacity: 0.06 },
    line: { color: b.c, width: 2 },
  });
  s10.addText(b.icon, {
    x: x, y: 2.0, w: 2.8, h: 0.6,
    fontSize: 36, align: "center",
  });
  s10.addText(b.title, {
    x: x + 0.2, y: 2.7, w: 2.4, h: 0.4,
    fontSize: 14, bold: true, color: C.primaryDark, align: "center",
  });
  s10.addText(b.desc, {
    x: x + 0.2, y: 3.2, w: 2.4, h: 1.5,
    fontSize: 10, color: C.gray, align: "center", lineSpacingMultiple: 1.5,
  });
});

// 市场数据
s10.addText("目标市场：2700万+听障人士 × 零竞品蓝海市场 × 国家科技助残政策红利", {
  x: 0.8, y: 5.8, w: 11.7, h: 0.5,
  fontSize: 14, color: C.primaryDark, bold: true, align: "center",
});

// ============================================================
// 幻灯片11: 团队介绍
// ============================================================
slideNum++;
const s11 = pptx.addSlide();
s11.background = { fill: C.white };
addSectionTitle(s11, "团队介绍", "微光科技 · 用AI的微光，守护无声世界");
addFooter(s11);
addSlideNumber(s11, slideNum, TOTAL_SLIDES);

const members = [
  { name: "蒲洋", role: "项目负责人", avatar: "🧑‍💼", dept: "整体规划 · 技术架构 · 加密算法", color: C.primary },
  { name: "代江宁", role: "AI算法工程师", avatar: "🧑‍🔬", dept: "AI模型训练 · TFLite部署 · 手势识别", color: C.info },
  { name: "王慧平", role: "Android开发工程师", avatar: "👩‍💻", dept: "客户端开发 · UI设计 · 无障碍交互", color: C.success },
  { name: "黄浙洋", role: "产品/市场负责人", avatar: "🧑‍💼", dept: "产品规划 · 用户调研 · 市场推广", color: C.purple },
];

members.forEach((m, i) => {
  const x = 0.8 + i * 3.1;
  s11.addShape(pptx.ShapeType.roundRect, {
    x: x, y: 1.8, w: 2.8, h: 3.8,
    fill: { color: C.bg }, rectRadius: 0.12,
    shadow: { type: "outer", blur: 5, offset: 2, color: "000000", opacity: 0.06 },
    line: { color: m.color, width: 2 },
  });
  // 头像
  s11.addShape(pptx.ShapeType.ellipse, {
    x: x + 0.8, y: 2.0, w: 1.2, h: 1.2,
    fill: { color: m.color },
  });
  s11.addText(m.avatar, {
    x: x + 0.8, y: 2.0, w: 1.2, h: 1.2,
    fontSize: 36, align: "center", valign: "middle",
  });
  s11.addText(m.name, {
    x: x + 0.2, y: 3.4, w: 2.4, h: 0.35,
    fontSize: 16, bold: true, color: C.primaryDark, align: "center",
  });
  s11.addText(m.role, {
    x: x + 0.2, y: 3.75, w: 2.4, h: 0.3,
    fontSize: 12, color: m.color, bold: true, align: "center",
  });
  s11.addText(m.dept, {
    x: x + 0.2, y: 4.1, w: 2.4, h: 0.8,
    fontSize: 10, color: C.gray, align: "center", lineSpacingMultiple: 1.4,
  });
});

// 团队单位
s11.addText("所属单位：天府新区通用航空职业学院", {
  x: 0.8, y: 6.1, w: 11.7, h: 0.4,
  fontSize: 13, color: C.gray, align: "center",
});

// ============================================================
// 幻灯片12: 社会效益
// ============================================================
slideNum++;
const s12 = pptx.addSlide();
s12.background = { fill: C.dark };
addFooter(s12, "微光科技 · 科技向善");
addSlideNumber(s12, slideNum, TOTAL_SLIDES);

s12.addText("社会效益与影响", {
  x: 0.5, y: 0.3, w: 12.33, h: 0.6,
  fontSize: 28, color: C.primary, bold: true, align: "center",
});
s12.addText("用科技创新消除听障人士的生活障碍", {
  x: 0.5, y: 0.9, w: 12.33, h: 0.4,
  fontSize: 14, color: C.white, align: "center",
});

const benefits = [
  { icon: "🛡️", title: "生命安全", desc: "火灾预警、燃气泄漏预警、跌倒检测\n直接保护听障人士生命安全" },
  { icon: "🌟", title: "生活质量", desc: "闹钟唤醒、敲门识别、健康监测\n提升日常生活便利性与独立性" },
  { icon: "🤝", title: "社会融入", desc: "手势识别、双向手语交互\n帮助听障人士更好地与外界沟通" },
  { icon: "🔐", title: "隐私保护", desc: "零知识加密架构\n确保用户数据安全，树立科技向善典范" },
  { icon: "📜", title: "行业推动", desc: "核心算法Apache 2.0开源\n推动科技助残行业技术进步" },
  { icon: "🎯", title: "大赛契合", desc: "\"科创无碍，智享生活\"\nAI创新消除听障生活障碍" },
];

benefits.forEach((b, i) => {
  const col = i % 3;
  const row = Math.floor(i / 3);
  const x = 0.8 + col * 4.0;
  const y = 1.6 + row * 2.6;

  s12.addShape(pptx.ShapeType.roundRect, {
    x: x, y: y, w: 3.7, h: 2.2,
    fill: { color: "FFFFFF" }, rectRadius: 0.12,
    shadow: { type: "outer", blur: 5, offset: 2, color: "000000", opacity: 0.2 },
  });
  s12.addText(b.icon, {
    x: x, y: y + 0.1, w: 3.7, h: 0.5,
    fontSize: 28, align: "center",
  });
  s12.addText(b.title, {
    x: x + 0.2, y: y + 0.65, w: 3.3, h: 0.3,
    fontSize: 15, bold: true, color: C.primaryDark, align: "center",
  });
  s12.addText(b.desc, {
    x: x + 0.2, y: y + 1.05, w: 3.3, h: 0.9,
    fontSize: 10, color: C.gray, align: "center", lineSpacingMultiple: 1.4,
  });
});

// ============================================================
// 幻灯片13: 致谢
// ============================================================
slideNum++;
const s13 = pptx.addSlide();
s13.background = { fill: C.dark };
// 顶部装饰
s13.addShape(pptx.ShapeType.rect, {
  x: 0, y: 0, w: 13.33, h: 0.08,
  fill: { color: C.primary },
});

s13.addText("感谢聆听", {
  x: 1, y: 1.5, w: 11.33, h: 1.0,
  fontSize: 48, color: C.primary, bold: true, align: "center",
});
s13.addText("微光守护 —— AI多模态无声安全预警系统", {
  x: 1, y: 2.6, w: 11.33, h: 0.5,
  fontSize: 20, color: C.white, align: "center",
});

s13.addShape(pptx.ShapeType.rect, {
  x: 4.0, y: 3.3, w: 5.33, h: 0.03,
  fill: { color: C.primary },
});

s13.addText("以AI之手，搭建听障人士与世界沟通的桥梁", {
  x: 1, y: 3.6, w: 11.33, h: 0.5,
  fontSize: 16, color: C.accent, align: "center", italic: true,
});

s13.addText("微光科技（WeiGuang Tech）", {
  x: 1, y: 4.6, w: 11.33, h: 0.5,
  fontSize: 18, color: C.white, align: "center", bold: true,
});
s13.addText("天府新区通用航空职业学院", {
  x: 1, y: 5.1, w: 11.33, h: 0.4,
  fontSize: 14, color: C.gray, align: "center",
});

// 联系方式
s13.addText("演示站：http://47.108.149.191/  |  核心算法开源：Apache 2.0  |  2026年7月", {
  x: 1, y: 5.9, w: 11.33, h: 0.4,
  fontSize: 11, color: C.gray, align: "center",
});

// 底部装饰
s13.addShape(pptx.ShapeType.rect, {
  x: 0, y: 7.42, w: 13.33, h: 0.08,
  fill: { color: C.primary },
});

// ============================================================
// 生成PPT文件
// ============================================================
const outputPath = "f:/java/weiguangplus/weiguang123/参赛材料/微光科技_微光守护_路演PPT.pptx";
pptx.writeFile({ fileName: outputPath }).then(() => {
  console.log("PPT已生成: " + outputPath);
  console.log("共 " + slideNum + " 页幻灯片");
}).catch((err) => {
  console.error("生成失败:", err);
});