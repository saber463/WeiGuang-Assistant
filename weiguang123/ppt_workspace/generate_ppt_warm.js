// ============================================================
// 微光守护 - 项目路演PPT · 暖色完整版
// 2026 AI助残创新创意大赛 · 创意赛道
// 标题：AI 之手 搭建听障人士与世界沟通的桥梁
// 15页完整结构：封面→目录→简介→痛点→产品方案→手势识别→
//               技术方案(截图)→技术架构→加密引擎→创新点→
//               商业模式→团队→社会效益→发展规划→致谢
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
pptx.title = "微光守护 - AI 之手 搭建听障人士与世界沟通的桥梁";

// ── 暖色系品牌色板 ──
const C = {
  primary:   "FF6B35",   // 火焰橙 - 主色
  primaryLt: "FF8C52",   // 浅橙
  primaryDk: "E55A2B",   // 深橙
  gold:      "FFC145",   // 暖金色
  goldLt:    "FFD97A",   // 浅金
  warmRed:   "EF476F",   // 暖红
  warmRedDk: "D93D5E",   // 深暖红
  bg:        "FFFBF5",   // 暖米白背景
  bgWarm:    "FFF3E8",   // 暖橙背景
  cardBg:    "FFFFFF",   // 白色卡片
  white:     "FFFDF9",   // 暖白
  dark:      "3D2C2C",   // 暖棕黑（正文）
  gray:      "8B7B7B",   // 暖灰
  grayLt:    "C4B5B5",   // 浅暖灰
  darkBg:    "2C1810",   // 深暖棕（深色背景）
  darkBg2:   "3D2418",   // 次深暖棕
  success:   "06A77D",   // 翠绿
  info:      "5B8DEF",   // 蓝
  purple:    "8B5CF6",   // 紫
  teal:      "14B8A6",   // 青
};

const TOTAL = 15;

// ── 辅助函数 ──
function addFooter(slide) {
  slide.addText("微光科技 · 2026 AI助残创新创意大赛 · 创意赛道", {
    x: 0.6, y: 7.15, w: 8, h: 0.25,
    fontSize: 7, color: C.gray,
  });
}
function addPageNum(slide, n) {
  slide.addText(`${n} / ${TOTAL}`, {
    x: 12.2, y: 7.15, w: 0.9, h: 0.25,
    fontSize: 7, color: C.gray, align: "right",
  });
}

function addSectionBar(slide, title, subtitle) {
  slide.addShape(pptx.ShapeType.rect, {
    x: 0, y: 0, w: 13.33, h: 0.06,
    fill: { color: C.primary },
  });
  slide.addText(title, {
    x: 0.7, y: 0.2, w: 10, h: 0.55,
    fontSize: 24, fontFace: "Microsoft YaHei", bold: true, color: C.dark,
  });
  if (subtitle) {
    slide.addText(subtitle, {
      x: 0.7, y: 0.72, w: 10, h: 0.3,
      fontSize: 11, color: C.gray,
    });
  }
  slide.addShape(pptx.ShapeType.rect, {
    x: 0.7, y: 1.08, w: 11.9, h: 0.02,
    fill: { color: C.grayLt },
  });
}

// 深色背景页面的标题栏
function addDarkTitle(slide, title, subtitle) {
  slide.addShape(pptx.ShapeType.rect, {
    x: 0, y: 0, w: 13.33, h: 0.06,
    fill: { color: C.primary },
  });
  slide.addText(title, {
    x: 0.5, y: 0.2, w: 12.33, h: 0.55,
    fontSize: 26, color: C.white, bold: true, align: "center",
  });
  if (subtitle) {
    slide.addText(subtitle, {
      x: 0.5, y: 0.72, w: 12.33, h: 0.3,
      fontSize: 12, color: C.gray, align: "center",
    });
  }
  slide.addShape(pptx.ShapeType.rect, {
    x: 5, y: 1.05, w: 3.33, h: 0.02,
    fill: { color: C.primary },
  });
}

let n = 0;

// ============================================================
// Slide 1: 封面
// ============================================================
n++;
const s1 = pptx.addSlide();
s1.background = { fill: C.darkBg };

// 装饰光晕：右上火焰橙
s1.addShape(pptx.ShapeType.ellipse, {
  x: 9.5, y: -1.5, w: 6, h: 6,
  fill: { color: C.primary },
  shadow: { type: "outer", blur: 50, offset: 0, color: C.primary, opacity: 0.4 },
});
// 左下暖金
s1.addShape(pptx.ShapeType.ellipse, {
  x: -1, y: 5.5, w: 3.5, h: 3.5,
  fill: { color: C.gold },
  shadow: { type: "outer", blur: 30, offset: 0, color: C.gold, opacity: 0.2 },
});
// 右上暖红光点
s1.addShape(pptx.ShapeType.ellipse, {
  x: 7, y: 0.5, w: 1.5, h: 1.5,
  fill: { color: C.warmRed },
  shadow: { type: "outer", blur: 25, offset: 0, color: C.warmRed, opacity: 0.15 },
});

// 大赛标签
s1.addShape(pptx.ShapeType.roundRect, {
  x: 4.5, y: 0.6, w: 4.33, h: 0.42,
  fill: { color: C.warmRed }, rectRadius: 0.2,
});
s1.addText("2026 AI助残创新创意大赛 · 创意赛道", {
  x: 4.5, y: 0.6, w: 4.33, h: 0.42,
  fontSize: 11, color: C.white, bold: true, align: "center",
});

// 主标题
s1.addText("AI 之手", {
  x: 1.5, y: 1.5, w: 10.33, h: 0.9,
  fontSize: 54, color: C.white, bold: true, align: "center",
  fontFace: "Microsoft YaHei",
});
s1.addText("搭建听障人士与世界沟通的桥梁", {
  x: 1.5, y: 2.4, w: 10.33, h: 0.7,
  fontSize: 28, color: C.primaryLt, align: "center",
  fontFace: "Microsoft YaHei",
});

// 装饰线
s1.addShape(pptx.ShapeType.rect, {
  x: 4.0, y: 3.3, w: 5.33, h: 0.03,
  fill: { color: C.gold },
});

// 项目名称
s1.addText("微光守护 — AI 多模态无声安全预警系统", {
  x: 1.5, y: 3.65, w: 10.33, h: 0.5,
  fontSize: 17, color: C.goldLt, align: "center",
});

// 核心数据
s1.addText("2700 万听障人士  ·  6 大安全预警模块  ·  11 种手势识别  ·  100% 端侧推理", {
  x: 1.5, y: 4.35, w: 10.33, h: 0.4,
  fontSize: 12, color: C.grayLt, align: "center",
});

// 团队信息
s1.addText("微光科技（WeiGuang Tech）", {
  x: 1.5, y: 5.3, w: 10.33, h: 0.45,
  fontSize: 18, color: C.white, bold: true, align: "center",
});
s1.addText("天府新区通用航空职业学院  |  2026 年 7 月", {
  x: 1.5, y: 5.75, w: 10.33, h: 0.35,
  fontSize: 11, color: C.gray, align: "center",
});

// 底部装饰条
s1.addShape(pptx.ShapeType.rect, {
  x: 0, y: 7.44, w: 13.33, h: 0.06,
  fill: { color: C.primary },
});

// ============================================================
// Slide 2: 目录
// ============================================================
n++;
const s2 = pptx.addSlide();
s2.background = { fill: C.bg };
addSectionBar(s2, "目  录", "CONTENTS");
addFooter(s2); addPageNum(s2, n);

const toc = [
  ["01", "项目简介", "目标用户 · 核心定位 · 关键数据", C.primary],
  ["02", "重点分析", "六大日常安全核心痛点", C.warmRed],
  ["03", "产品方案设计", "AI 多模态安全预警六大模块", C.primaryDk],
  ["04", "手势识别与手语交互", "11 种手势实时识别 · 双向手语翻译", C.gold],
  ["05", "技术方案 · 真机展示", "五大模块在手机上的实际运行效果", C.primary],
  ["06", "技术架构", "四层架构 · 端侧推理 · 离线优先", C.info],
  ["07", "加密引擎", "WeiGuang-Secure 零知识加密", C.warmRed],
  ["08", "创新亮点", "五大核心技术 · 三大模式创新", C.purple],
  ["09", "商业模式", "B2B2C · 免费增值 · 开源生态", C.teal],
  ["10", "核心团队", "微光科技成员介绍", C.primaryDk],
  ["11", "社会效益", "科技向善 · 行业推动", C.success],
  ["12", "发展规划", "三步走战略 · 长期愿景", C.info],
  ["13", "致谢", "感谢聆听", C.primary],
];

const tocStartY = 1.5;
const tocH = 0.42;
toc.forEach((item, i) => {
  const y = tocStartY + i * tocH;
  const color = item[3];
  // 序号圆圈
  s2.addShape(pptx.ShapeType.ellipse, {
    x: 1.2, y: y + 0.04, w: 0.34, h: 0.34,
    fill: { color },
  });
  s2.addText(item[0], {
    x: 1.2, y: y + 0.04, w: 0.34, h: 0.34,
    fontSize: 12, color: C.white, bold: true, align: "center", valign: "middle",
  });
  // 标题
  s2.addText(item[1], {
    x: 1.8, y: y, w: 3.5, h: 0.22,
    fontSize: 14, bold: true, color: C.dark,
  });
  // 描述
  s2.addText(item[2], {
    x: 1.8, y: y + 0.22, w: 5, h: 0.18,
    fontSize: 9, color: C.gray,
  });
  // 连接线
  if (i < toc.length - 1) {
    s2.addShape(pptx.ShapeType.rect, {
      x: 1.36, y: y + 0.4, w: 0.02, h: 0.08,
      fill: { color: C.grayLt },
    });
  }
});

// 右侧装饰
s2.addShape(pptx.ShapeType.ellipse, {
  x: 9.5, y: 1.5, w: 4.5, h: 4.5,
  fill: { color: C.primary },
  shadow: { type: "outer", blur: 30, offset: 0, color: C.primary, opacity: 0.15 },
});
s2.addText("微光\n守护", {
  x: 9.5, y: 2.5, w: 4.5, h: 2.5,
  fontSize: 26, color: C.white, bold: true, align: "center", valign: "middle",
  fontFace: "Microsoft YaHei",
});

// ============================================================
// Slide 3: 项目简介
// ============================================================
n++;
const s3 = pptx.addSlide();
s3.background = { fill: C.bg };
addSectionBar(s3, "项目简介", "微光守护 — 为听障人士打造的全场景 AI 安全守护平台");
addFooter(s3); addPageNum(s3, n);

// 左侧：项目概述卡片
s3.addShape(pptx.ShapeType.roundRect, {
  x: 0.5, y: 1.35, w: 7.5, h: 5.5,
  fill: { color: C.white }, rectRadius: 0.12,
  shadow: { type: "outer", blur: 8, offset: 3, color: "000000", opacity: 0.06 },
  line: { color: C.primary, width: 2 },
});
s3.addShape(pptx.ShapeType.rect, {
  x: 0.5, y: 1.35, w: 7.5, h: 0.06,
  fill: { color: C.primary },
});

const introTexts = [
  { label: "目标用户", value: "全国 2700 万+ 听障人士，每年新增 2-3 万听障儿童" },
  { label: "核心问题", value: "听不见闹钟、火警、燃气泄漏、敲门声、咳嗽异常、跌倒呼救" },
  { label: "产品定位", value: '基于 AI 多模态感知的无声安全预警系统，变 "听不见" 为 "感知到"' },
  { label: "运行方式", value: "100% 端侧 AI 推理，无需网络，数据不出手机，隐私安全可控" },
  { label: "覆盖场景", value: "6 大日常安全场景：唤醒、火灾、燃气、健康、安防、跌倒" },
  { label: "核心优势", value: "零知识加密架构 + 离线可用 + 11 种手势识别 + 双向手语交互" },
];

introTexts.forEach((item, i) => {
  const y = 1.65 + i * 0.82;
  s3.addShape(pptx.ShapeType.roundRect, {
    x: 0.8, y: y + 0.04, w: 1.3, h: 0.32,
    fill: { color: C.primary }, rectRadius: 0.15,
  });
  s3.addText(item.label, {
    x: 0.8, y: y + 0.04, w: 1.3, h: 0.32,
    fontSize: 10, color: C.white, bold: true, align: "center", valign: "middle",
  });
  s3.addText(item.value, {
    x: 2.3, y: y, w: 5.5, h: 0.5,
    fontSize: 11, color: C.dark, valign: "middle",
  });
});

// 右侧：核心数据
s3.addShape(pptx.ShapeType.roundRect, {
  x: 8.4, y: 1.35, w: 4.5, h: 5.5,
  fill: { color: C.darkBg }, rectRadius: 0.12,
});

const bigNums = [
  { num: "2700万+", label: "听障人士", color: C.primaryLt },
  { num: "6", label: "大安全模块", color: C.gold },
  { num: "11", label: "种手势识别", color: C.warmRed },
  { num: "100%", label: "端侧推理", color: C.success },
];

bigNums.forEach((bn, i) => {
  const y = 1.65 + i * 1.25;
  s3.addText(bn.num, {
    x: 8.4, y: y, w: 4.5, h: 0.55,
    fontSize: 32, color: bn.color, bold: true, align: "center",
  });
  s3.addText(bn.label, {
    x: 8.4, y: y + 0.55, w: 4.5, h: 0.3,
    fontSize: 12, color: C.grayLt, align: "center",
  });
  if (i < bigNums.length - 1) {
    s3.addShape(pptx.ShapeType.rect, {
      x: 9.5, y: y + 1.05, w: 2.3, h: 0.01,
      fill: { color: C.darkBg2 },
    });
  }
});

s3.addShape(pptx.ShapeType.roundRect, {
  x: 0.5, y: 6.7, w: 12.4, h: 0.42,
  fill: { color: C.bgWarm }, rectRadius: 0.08,
  line: { color: C.primary, width: 1 },
});
s3.addText('"微光守护" 让听障人士也能 "感知" 到世界的声音 — 用 AI 的微光，守护无声世界的安全', {
  x: 0.7, y: 6.72, w: 12.0, h: 0.38,
  fontSize: 12, color: C.primaryDk, bold: true, align: "center", valign: "middle", italic: true,
});

// ============================================================
// Slide 4: 重点分析 — 六大痛点
// ============================================================
n++;
const s4 = pptx.addSlide();
s4.background = { fill: C.white };
addSectionBar(s4, "重点分析", "听障人士面临的六大核心安全痛点 — 覆盖日常生活的每个高危场景");
addFooter(s4); addPageNum(s4, n);

const pains = [
  { icon: "⏰", title: "听不见闹钟", desc: "传统声音闹钟完全无效\n错过重要日程，影响正常工作生活", level: "中风险", lc: C.gold, bc: C.primary },
  { icon: "🔥", title: "听不见火警", desc: "生命安全受严重威胁\n火灾警报无法感知，逃生窗口期极短", level: "极高风险", lc: C.warmRed, bc: C.warmRed },
  { icon: "💨", title: "感知不到燃气泄漏", desc: "中毒和爆炸风险\n低频异响无法察觉，夜间睡眠时最危险", level: "极高风险", lc: C.warmRed, bc: C.warmRed },
  { icon: "🚪", title: "察觉不到异常敲门", desc: "居家安全存隐患\n无法分辨敲门类型，陌生人侵入风险高", level: "高风险", lc: C.warmRed, bc: C.warmRedDk },
  { icon: "🩺", title: "健康监测缺失", desc: "病理性咳嗽难以评估\n缺乏健康预警手段，延误就医时机", level: "中风险", lc: C.gold, bc: C.info },
  { icon: "🆘", title: "跌倒无法呼救", desc: "错过黄金救援时间\n无法语音呼叫求助，独居时最危险", level: "极高风险", lc: C.warmRed, bc: C.warmRed },
];

pains.forEach((p, i) => {
  const col = i % 3;
  const row = Math.floor(i / 3);
  const x = 0.6 + col * 4.15;
  const y = 1.35 + row * 2.85;

  s4.addShape(pptx.ShapeType.roundRect, {
    x: x, y: y, w: 3.85, h: 2.5,
    fill: { color: C.bg }, rectRadius: 0.12,
    shadow: { type: "outer", blur: 5, offset: 2, color: "000000", opacity: 0.05 },
    line: { color: p.bc, width: 1.5 },
  });
  s4.addShape(pptx.ShapeType.rect, {
    x: x, y: y, w: 3.85, h: 0.06,
    fill: { color: p.bc },
  });
  s4.addShape(pptx.ShapeType.rect, {
    x: x, y: y, w: 0.05, h: 2.5,
    fill: { color: p.bc },
  });
  s4.addText(p.icon, {
    x: x, y: y + 0.2, w: 3.85, h: 0.55,
    fontSize: 30, align: "center",
  });
  s4.addText(p.title, {
    x: x + 0.2, y: y + 0.8, w: 3.45, h: 0.35,
    fontSize: 15, bold: true, color: C.dark, align: "center",
  });
  s4.addText(p.desc, {
    x: x + 0.3, y: y + 1.2, w: 3.25, h: 0.7,
    fontSize: 10, color: C.gray, align: "center", lineSpacingMultiple: 1.4,
  });
  s4.addShape(pptx.ShapeType.roundRect, {
    x: x + 1.2, y: y + 2.0, w: 1.45, h: 0.32,
    fill: { color: p.lc }, rectRadius: 0.15,
  });
  s4.addText(p.level, {
    x: x + 1.2, y: y + 2.0, w: 1.45, h: 0.32,
    fontSize: 9, color: C.white, bold: true, align: "center",
  });
});

s4.addShape(pptx.ShapeType.roundRect, {
  x: 0.6, y: 6.7, w: 12.1, h: 0.45,
  fill: { color: C.warmRedDk }, rectRadius: 0.08,
});
s4.addText("六大痛点覆盖听障人士 24 小时生活场景，每一个都可能危及生命 — 无声世界的安全，刻不容缓", {
  x: 0.6, y: 6.7, w: 12.1, h: 0.45,
  fontSize: 12, color: C.white, bold: true, align: "center", valign: "middle",
});

// ============================================================
// Slide 5: 产品方案设计
// ============================================================
n++;
const s5 = pptx.addSlide();
s5.background = { fill: C.bg };
addSectionBar(s5, "产品方案设计", "AI 多模态安全预警系统 — 六大核心模块，全场景守护");
addFooter(s5); addPageNum(s5, n);

const modules = [
  { icon: "⏰", name: "触觉唤醒引擎", desc: "振动 + 闪光双通道唤醒\n三种振动模式 · 双重关闭验证\n确保重要日程不遗漏", c: C.primary },
  { icon: "🔥", name: "火灾声纹预警", desc: "CNN + Mel 频谱图识别\n准确率 >95% · 全屏红色预警\nTTS 语音 + 振动双重提醒", c: C.warmRed },
  { icon: "💨", name: "燃气泄漏预警", desc: "8kHz 低频 MFCC 检测\n轻量 CNN <500K · 四步处置指南\n联动物业紧急联系人", c: C.gold },
  { icon: "🩺", name: "咳嗽健康监测", desc: "15 秒滑窗 ≥5 次触发\n三级预警 · 健康闭环\n辅助就医判断", c: C.info },
  { icon: "🚪", name: "敲门安防预警", desc: "3 秒密度分析算法\n七级安全评估 · 手语沟通面板\n识别敲门人身份", c: C.teal },
  { icon: "🆘", name: "跌倒检测", desc: "传感器 + 摄像头双重确认\n30 秒自动派发应急\nTTS 自动呼救播报", c: C.purple },
];

modules.forEach((m, i) => {
  const col = i % 3;
  const row = Math.floor(i / 3);
  const x = 0.6 + col * 4.15;
  const y = 1.35 + row * 2.85;

  s5.addShape(pptx.ShapeType.roundRect, {
    x: x, y: y, w: 3.85, h: 2.5,
    fill: { color: C.white }, rectRadius: 0.12,
    shadow: { type: "outer", blur: 5, offset: 2, color: "000000", opacity: 0.05 },
    line: { color: m.c, width: 1.5 },
  });
  s5.addShape(pptx.ShapeType.rect, {
    x: x, y: y, w: 0.06, h: 2.5,
    fill: { color: m.c },
  });
  s5.addShape(pptx.ShapeType.rect, {
    x: x, y: y, w: 3.85, h: 0.04,
    fill: { color: m.c },
  });
  s5.addText(m.icon, {
    x: x + 0.15, y: y + 0.15, w: 3.7, h: 0.5,
    fontSize: 28, align: "center",
  });
  s5.addText(m.name, {
    x: x + 0.25, y: y + 0.7, w: 3.35, h: 0.35,
    fontSize: 15, bold: true, color: C.dark, align: "center",
  });
  s5.addText(m.desc, {
    x: x + 0.3, y: y + 1.15, w: 3.25, h: 0.85,
    fontSize: 10, color: C.gray, align: "center", lineSpacingMultiple: 1.5,
  });
  s5.addShape(pptx.ShapeType.rect, {
    x: x + 0.8, y: y + 2.2, w: 2.25, h: 0.03,
    fill: { color: m.c },
  });
});

s5.addShape(pptx.ShapeType.roundRect, {
  x: 0.6, y: 6.7, w: 12.1, h: 0.45,
  fill: { color: C.primaryDk }, rectRadius: 0.08,
});
s5.addText("六大模块  ·  100% 端侧推理  ·  零网络依赖  ·  离线可用  ·  数据不出手机", {
  x: 0.6, y: 6.7, w: 12.1, h: 0.45,
  fontSize: 12, color: C.white, bold: true, align: "center", valign: "middle",
});

// ============================================================
// Slide 6: 手势识别与双向手语交互
// ============================================================
n++;
const s6 = pptx.addSlide();
s6.background = { fill: C.white };
addSectionBar(s6, "手势识别与双向手语交互", "11 种手势实时识别 · 100% 端侧推理 · 识别速度 < 1ms");
addFooter(s6); addPageNum(s6, n);

// 手势表格
const gestures = [
  ["✊", "握拳", "SOS 紧急求助"],
  ["✋", "手掌张开", "停止 / 问候"],
  ["👍", "竖大拇指", "确认 / 好的"],
  ["👎", "拇指朝下", "不好 / 拒绝"],
  ["☝️", "食指指向", "指向方向"],
  ["✌️", "剪刀手", "胜利 / 和平"],
  ["👌", "OK 手势", "没问题"],
  ["👋", "摆手", "你好 / 再见"],
  ["🫶", "比心", "谢谢 / 爱你"],
  ["🤙", "打电话", "联系我"],
  ["😐", "无手势", "待机状态"],
];

const headerRow = [
  { text: "手势", options: { bold: true, color: C.white, fill: { color: C.primaryDk }, fontSize: 10, align: "center", fontFace: "Microsoft YaHei" } },
  { text: "名称", options: { bold: true, color: C.white, fill: { color: C.primaryDk }, fontSize: 10, align: "center", fontFace: "Microsoft YaHei" } },
  { text: "含义", options: { bold: true, color: C.white, fill: { color: C.primaryDk }, fontSize: 10, align: "center", fontFace: "Microsoft YaHei" } },
];

const gRows = gestures.map((g, i) => [
  { text: g[0], options: { fontSize: 15, align: "center", fill: { color: i === 0 ? C.bgWarm : (i % 2 === 0 ? C.bg : C.white) }, fontFace: "Microsoft YaHei" } },
  { text: g[1], options: { fontSize: 10, bold: i === 0, align: "center", fill: { color: i === 0 ? C.bgWarm : (i % 2 === 0 ? C.bg : C.white) }, fontFace: "Microsoft YaHei" } },
  { text: g[2], options: { fontSize: 10, align: "center", fill: { color: i === 0 ? C.bgWarm : (i % 2 === 0 ? C.bg : C.white) }, fontFace: "Microsoft YaHei" } },
]);

s6.addTable([headerRow, ...gRows], {
  x: 0.5, y: 1.35, w: 5.5,
  colW: [1.1, 1.8, 2.6],
  rowH: [0.35, ...Array(11).fill(0.34)],
  border: { type: "solid", pt: 0.5, color: "E8D5C8" },
  autoPage: false,
});

// 右侧：核心技术指标
s6.addShape(pptx.ShapeType.roundRect, {
  x: 6.4, y: 1.35, w: 6.4, h: 5.5,
  fill: { color: C.bg }, rectRadius: 0.12,
  line: { color: C.grayLt, width: 1 },
});

s6.addText("核心技术指标", {
  x: 6.6, y: 1.5, w: 6, h: 0.35,
  fontSize: 14, bold: true, color: C.primaryDk,
});

const techItems = [
  { title: "MediaPipe Hand Landmarker", desc: "21 点手部关键点检测，Google 开源模型" },
  { title: "GestureVectorDB", desc: "自研手势质心向量数据库，余弦相似度匹配" },
  { title: "识别速度 < 1ms", desc: "100 个质心向量余弦相似度计算" },
  { title: "100% 端侧推理", desc: "无网络依赖，隐私数据不出设备" },
  { title: "SOS 握拳自动触发", desc: "TTS 语音播报 + 紧急联系人通知 + 10 秒防重复" },
  { title: "双向手语交互", desc: "手势 → 文字 识别 + 文字 → 手语 动画生成" },
  { title: "增量更新", desc: "支持 100+ 手势扩展，无需重新安装 APP" },
  { title: "80+ 商品词库", desc: "OCR 商品识别，辅助购物独立生活" },
];

techItems.forEach((h, i) => {
  const y = 2.05 + i * 0.58;
  s6.addShape(pptx.ShapeType.ellipse, {
    x: 6.75, y: y + 0.06, w: 0.2, h: 0.2,
    fill: { color: C.primary },
  });
  s6.addText(h.title, {
    x: 7.1, y: y - 0.02, w: 5.5, h: 0.23,
    fontSize: 11.5, bold: true, color: C.dark,
  });
  s6.addText(h.desc, {
    x: 7.1, y: y + 0.22, w: 5.5, h: 0.2,
    fontSize: 9.5, color: C.gray,
  });
});

// ============================================================
// Slide 7: 技术方案 — 手机截图展示
// ============================================================
n++;
const s7 = pptx.addSlide();
s7.background = { fill: C.white };
addSectionBar(s7, "技术方案 · 真机展示", "产品实际运行效果 — 五大核心模块在手机上的真实呈现");
addFooter(s7); addPageNum(s7, n);

const screenshots = [
  { file: "phone_01.png", label: "首页 / 主界面", desc: "六大模块入口\n状态栏实时监控" },
  { file: "phone_02.png", label: "火灾预警", desc: "CNN 声纹识别\n全屏红色预警" },
  { file: "phone_03.png", label: "燃气泄漏预警", desc: "MFCC 低频检测\n四步处置指南" },
  { file: "phone_04.png", label: "手势识别", desc: "11 种手势实时识别\nSOS 握拳自动触发" },
  { file: "phone_05.png", label: "敲门检测", desc: "密度分析算法\n七级安全评估" },
];

const imgBasePath = "f:/java/weiguangplus/weiguang123/ppt_workspace/screenshots/";

screenshots.forEach((sc, i) => {
  const x = 0.35 + i * 2.55;
  const imgW = 2.2;
  const imgH = 4.2;

  s7.addShape(pptx.ShapeType.roundRect, {
    x: x, y: 1.3, w: 2.4, h: 5.8,
    fill: { color: C.bg }, rectRadius: 0.12,
    shadow: { type: "outer", blur: 5, offset: 2, color: "000000", opacity: 0.05 },
    line: { color: C.primaryLt, width: 1 },
  });
  s7.addShape(pptx.ShapeType.rect, {
    x: x, y: 1.3, w: 2.4, h: 0.04,
    fill: { color: C.primary },
  });

  s7.addImage({
    path: imgBasePath + sc.file,
    x: x + 0.1, y: 1.5, w: imgW, h: imgH,
    sizing: { type: "contain", w: imgW, h: imgH },
  });

  s7.addText(sc.label, {
    x: x + 0.05, y: 5.85, w: 2.3, h: 0.35,
    fontSize: 11, bold: true, color: C.primaryDk, align: "center",
  });
  s7.addText(sc.desc, {
    x: x + 0.1, y: 6.2, w: 2.2, h: 0.6,
    fontSize: 8.5, color: C.gray, align: "center", lineSpacingMultiple: 1.4,
  });
});

s7.addShape(pptx.ShapeType.roundRect, {
  x: 0.5, y: 6.85, w: 12.33, h: 0.35,
  fill: { color: C.bgWarm }, rectRadius: 0.06,
  line: { color: C.primary, width: 1 },
});
s7.addText("技术栈：MediaPipe Hand Landmarker · TensorFlow Lite · CNN 声纹识别 · MFCC 音频分析 · WeiGuang-Secure 加密引擎", {
  x: 0.5, y: 6.85, w: 12.33, h: 0.35,
  fontSize: 8.5, color: C.primaryDk, bold: true, align: "center", valign: "middle",
});

// ============================================================
// Slide 8: 技术架构
// ============================================================
n++;
const s8 = pptx.addSlide();
s8.background = { fill: C.bg };
addSectionBar(s8, "技术架构", "端侧 AI + 零知识加密 + 离线优先 — 四层架构设计");
addFooter(s8); addPageNum(s8, n);

const layers = [
  { name: "应用层", tech: "Jetpack Compose + Material3", desc: "6 大模块统一设计语言 · 适配 Android 7.0+ · 无障碍交互优化", color: C.primary },
  { name: "AI 推理层", tech: "TensorFlow Lite + ONNX", desc: "CNN / RNN 端侧推理 · INT8 量化 <2MB · 离线运行 · 推理速度 <50ms", color: C.info },
  { name: "数据层", tech: "Room + SQLite", desc: "本地持久化存储 · 手势向量库 · 数据不出设备 · WiFi 仅用于密文同步", color: C.success },
  { name: "安全层", tech: "WeiGuang-Secure 加密引擎", desc: "ECDH X25519 + AES-256-GCM · 零知识架构 · 服务端不可解密 · 生物特征绑定", color: C.warmRed },
];

layers.forEach((l, i) => {
  const y = 1.35 + i * 1.3;
  s8.addShape(pptx.ShapeType.roundRect, {
    x: 1.2, y: y, w: 10.9, h: 1.05,
    fill: { color: C.white }, rectRadius: 0.1,
    shadow: { type: "outer", blur: 4, offset: 2, color: "000000", opacity: 0.05 },
    line: { color: l.color, width: 2.5 },
  });
  s8.addShape(pptx.ShapeType.roundRect, {
    x: 1.2, y: y, w: 1.4, h: 1.05,
    fill: { color: l.color }, rectRadius: 0.1,
  });
  s8.addText(l.name, {
    x: 1.2, y: y, w: 1.4, h: 1.05,
    fontSize: 14, color: C.white, bold: true, align: "center", valign: "middle",
  });
  s8.addText(l.tech, {
    x: 2.9, y: y + 0.1, w: 9, h: 0.35,
    fontSize: 15, bold: true, color: C.dark,
  });
  s8.addText(l.desc, {
    x: 2.9, y: y + 0.52, w: 9, h: 0.3,
    fontSize: 10.5, color: C.gray,
  });
});

// 端侧 vs 云端对比
s8.addText("端侧推理优势对比", {
  x: 1.2, y: 6.3, w: 3, h: 0.3,
  fontSize: 12, bold: true, color: C.primaryDk,
});
const compareHeader = [
  { text: "", options: { fill: { color: C.primaryDk }, fontSize: 9, bold: true, color: C.white, fontFace: "Microsoft YaHei" } },
  { text: "云端推理", options: { fill: { color: C.primaryDk }, fontSize: 9, bold: true, color: C.white, align: "center", fontFace: "Microsoft YaHei" } },
  { text: "端侧推理（本项目）", options: { fill: { color: C.primaryDk }, fontSize: 9, bold: true, color: C.white, align: "center", fontFace: "Microsoft YaHei" } },
];
const compareData = [
  ["网络依赖", "必须联网", "零网络依赖"],
  ["延迟", "100-500ms", "< 50ms"],
  ["隐私保护", "数据上传云端", "数据不出设备"],
  ["成本", "持续服务费", "零运营成本"],
];
const compareRows = [compareHeader, ...compareData.map((r, ri) =>
  r.map((c, ci) => ({
    text: String(c),
    options: {
      fontSize: 9, align: ci > 0 ? "center" : "left",
      bold: ri === 0 || ci === 2,
      color: ri === 0 ? C.white : (ci === 2 ? C.success : C.dark),
      fill: { color: ri === 0 ? C.primaryDk : (ri % 2 === 0 ? C.bg : C.white) },
      fontFace: "Microsoft YaHei",
    },
  }))
)];
s8.addTable(compareRows, {
  x: 4.5, y: 6.1, w: 7.6,
  colW: [1.5, 2.5, 3.6],
  rowH: [0.25, 0.22, 0.22, 0.22, 0.22],
  border: { type: "solid", pt: 0.5, color: "E8D5C8" },
  autoPage: false,
});

// ============================================================
// Slide 9: 加密引擎 — WeiGuang-Secure
// ============================================================
n++;
const s9 = pptx.addSlide();
s9.background = { fill: C.darkBg };
addDarkTitle(s9, "WeiGuang-Secure 端到端加密引擎", "零知识架构 · NIST STS + Wycheproof 权威验证 · 服务端不可解密");
addFooter(s9); addPageNum(s9, n);

const encLayers = [
  { name: "ECDH + RSA 混合密钥交换", sub: "X25519 为主 · RSA-2048 降级 · 预留 Kyber 抗量子接口", param: "X25519 256 bits · ECDHE 前向安全", c: C.primary },
  { name: "AES-GCM + ChaCha20 双引擎加密", sub: "高端 AES-NI 加速 · 低端 ChaCha20 抗侧信道 · GCM-SIV 抗误用", param: "256 bits · 128-bit 标签 · 自适应引擎切换", c: C.info },
  { name: "Keystore TEE + 生物特征绑定", sub: "硬件隔离存储 · 指纹/人脸门控 · 手机丢失无法解密", param: "不可导出 · root 免疫 · 每次操作独立认证", c: C.success },
];

encLayers.forEach((l, i) => {
  const y = 1.3 + i * 0.82;
  s9.addShape(pptx.ShapeType.roundRect, {
    x: 0.5, y: y, w: 12.33, h: 0.72,
    fill: { color: C.darkBg2 }, rectRadius: 0.06,
    line: { color: l.c, width: 1.5 },
  });
  s9.addShape(pptx.ShapeType.rect, {
    x: 0.5, y: y, w: 0.05, h: 0.72,
    fill: { color: l.c },
  });
  s9.addShape(pptx.ShapeType.roundRect, {
    x: 0.7, y: y + 0.14, w: 0.42, h: 0.42,
    fill: { color: l.c }, rectRadius: 0.06,
  });
  s9.addText(`${i + 1}`, {
    x: 0.7, y: y + 0.14, w: 0.42, h: 0.42,
    fontSize: 14, color: C.white, bold: true, align: "center", valign: "middle",
  });
  s9.addText(l.name, {
    x: 1.3, y: y + 0.05, w: 5.5, h: 0.3,
    fontSize: 13, bold: true, color: C.white,
  });
  s9.addText(l.sub, {
    x: 1.3, y: y + 0.38, w: 6.5, h: 0.25,
    fontSize: 8.5, color: C.gray,
  });
  s9.addText(l.param, {
    x: 7.8, y: y + 0.12, w: 4.8, h: 0.5,
    fontSize: 8.5, color: C.grayLt, valign: "middle",
  });
});

// 五大升级亮点
s9.addText("五大核心升级", {
  x: 0.5, y: 3.9, w: 4, h: 0.25,
  fontSize: 11, bold: true, color: C.primaryLt,
});
const upgrades = [
  { icon: "🔑", text: "ECDH (X25519)", sub: "前向安全", c: C.primary },
  { icon: "📱", text: "ChaCha20 引擎", sub: "低端机 +50%", c: C.info },
  { icon: "🛡️", text: "GCM-SIV 抗误用", sub: "IV 重复不泄露", c: C.success },
  { icon: "🫵", text: "生物特征绑定", sub: "丢失无法解密", c: C.purple },
  { icon: "🔮", text: "Kyber 抗量子", sub: "量子时代预备", c: C.teal },
];
upgrades.forEach((u, i) => {
  const x = 0.5 + i * 2.5;
  s9.addShape(pptx.ShapeType.roundRect, {
    x: x, y: 4.25, w: 2.3, h: 0.95,
    fill: { color: C.darkBg2 }, rectRadius: 0.06,
    line: { color: u.c, width: 1 },
  });
  s9.addText(u.icon, {
    x: x, y: 4.28, w: 2.3, h: 0.28,
    fontSize: 14, align: "center",
  });
  s9.addText(u.text, {
    x: x + 0.1, y: 4.55, w: 2.1, h: 0.28,
    fontSize: 8.5, bold: true, color: C.white, align: "center",
  });
  s9.addText(u.sub, {
    x: x + 0.1, y: 4.8, w: 2.1, h: 0.22,
    fontSize: 7.5, color: C.gray, align: "center",
  });
});

// 雪崩效应 + 密钥轮换
s9.addText("雪崩效应测试", {
  x: 0.5, y: 5.4, w: 5, h: 0.22,
  fontSize: 10, bold: true, color: C.primaryLt,
});
const avHeader = [
  { text: "测试类型", options: { fill: { color: C.primary }, fontSize: 7, bold: true, color: C.white, fontFace: "Microsoft YaHei" } },
  { text: "实测值", options: { fill: { color: C.primary }, fontSize: 7, bold: true, color: C.white, align: "center", fontFace: "Microsoft YaHei" } },
  { text: "结论", options: { fill: { color: C.primary }, fontSize: 7, bold: true, color: C.white, align: "center", fontFace: "Microsoft YaHei" } },
];
const avData = [
  ["密钥雪崩", "49.98%", "通过"],
  ["明文雪崩", "50.04%", "通过"],
  ["IV 雪崩", "49.96%", "通过"],
  ["认证标签", "100% 不匹配", "通过"],
];
const avRows = [avHeader, ...avData.map((r, ri) =>
  r.map((c, ci) => ({
    text: String(c),
    options: {
      fontSize: 7, align: ci > 0 ? "center" : "left",
      bold: ri === 0,
      color: ri === 0 ? C.white : (ci === 2 ? C.success : C.white),
      fill: { color: ri === 0 ? C.primary : (ri % 2 === 0 ? C.darkBg2 : C.darkBg) },
      fontFace: "Microsoft YaHei",
    },
  }))
)];
s9.addTable(avRows, {
  x: 0.5, y: 5.65, w: 5.0,
  colW: [1.3, 1.5, 2.2],
  rowH: [0.22, 0.2, 0.2, 0.2, 0.2],
  border: { type: "solid", pt: 0.5, color: C.darkBg2 },
  autoPage: false,
});

// 密钥轮换
s9.addText("密钥轮换安全收益", {
  x: 6.0, y: 5.4, w: 5, h: 0.22,
  fontSize: 10, bold: true, color: C.primaryLt,
});
const rotHeader = [
  { text: "场景", options: { fill: { color: C.primary }, fontSize: 7, bold: true, color: C.white, fontFace: "Microsoft YaHei" } },
  { text: "无轮换", options: { fill: { color: C.primary }, fontSize: 7, bold: true, color: C.white, align: "center", fontFace: "Microsoft YaHei" } },
  { text: "本项目", options: { fill: { color: C.primary }, fontSize: 7, bold: true, color: C.white, align: "center", fontFace: "Microsoft YaHei" } },
];
const rotData = [
  ["密钥泄露影响", "全部数据", "仅 5 分钟窗口"],
  ["攻击时间窗口", "无限期", "最多 5 分钟"],
  ["暴力破解成本", "破解一次", "每 5 分钟一次"],
];
const rotRows = [rotHeader, ...rotData.map((r, ri) =>
  r.map((c, ci) => ({
    text: String(c),
    options: {
      fontSize: 7, align: ci > 0 ? "center" : "left",
      bold: ri === 0 || ci === 2,
      color: ri === 0 ? C.white : (ci === 2 ? C.success : C.white),
      fill: { color: ri === 0 ? C.primary : (ri % 2 === 0 ? C.darkBg2 : C.darkBg) },
      fontFace: "Microsoft YaHei",
    },
  }))
)];
s9.addTable(rotRows, {
  x: 6.0, y: 5.65, w: 6.8,
  colW: [1.5, 2.0, 3.3],
  rowH: [0.22, 0.2, 0.2, 0.2],
  border: { type: "solid", pt: 0.5, color: C.darkBg2 },
  autoPage: false,
});

// 底部安全结论
s9.addShape(pptx.ShapeType.roundRect, {
  x: 0.5, y: 6.75, w: 12.33, h: 0.38,
  fill: { color: C.darkBg2 }, rectRadius: 0.06,
  line: { color: C.success, width: 1 },
});
s9.addText("安全结论：服务端看不到 · 黑客攻不破 · 丢失读不了 — 真正实现零知识安全目标", {
  x: 0.7, y: 6.78, w: 11.93, h: 0.32,
  fontSize: 9, color: C.grayLt, valign: "middle", align: "center",
});

// ============================================================
// Slide 10: 创新亮点
// ============================================================
n++;
const s10 = pptx.addSlide();
s10.background = { fill: C.white };
addSectionBar(s10, "项目创新亮点", "五大核心技术 + 三大模式创新");
addFooter(s10); addPageNum(s10, n);

const innovations = [
  { num: "01", title: "WeiGuang-Secure 加密引擎", desc: "ECDH + AES-GCM/ChaCha20 双引擎\nNIST STS + Wycheproof 权威验证 · 零知识架构", color: C.primary },
  { num: "02", title: "GestureVectorDB 手势向量库", desc: "11 种手势质心向量存储匹配\n余弦相似度 <1ms · 100+ 手势增量更新", color: C.info },
  { num: "03", title: "端侧 AI 全离线推理", desc: "CNN / RNN INT8 量化\n模型 <2MB · 推理 <50ms · 100% 本地", color: C.success },
  { num: "04", title: "多模态融合检测", desc: "传感器 + 摄像头双重确认\n音频 + 振动 + 视觉三通道联合预警", color: C.purple },
  { num: "05", title: "双向手语交互系统", desc: "手势 → 文字：实时识别\n文字 → 手势：动画生成 · SOS 自动 TTS", color: C.teal },
];

innovations.forEach((inn, i) => {
  const col = i < 3 ? 0 : 1;
  const row = i < 3 ? i : i - 3;
  const x = 0.6 + col * 6.3;
  const y = 1.35 + row * 1.75;

  s10.addShape(pptx.ShapeType.roundRect, {
    x: x, y: y, w: 5.9, h: 1.5,
    fill: { color: C.bg }, rectRadius: 0.1,
    shadow: { type: "outer", blur: 4, offset: 2, color: "000000", opacity: 0.04 },
    line: { color: inn.color, width: 1.5 },
  });
  s10.addShape(pptx.ShapeType.rect, {
    x: x, y: y, w: 0.06, h: 1.5,
    fill: { color: inn.color },
  });
  s10.addShape(pptx.ShapeType.roundRect, {
    x: x + 0.2, y: y + 0.12, w: 0.55, h: 0.45,
    fill: { color: inn.color }, rectRadius: 0.08,
  });
  s10.addText(inn.num, {
    x: x + 0.2, y: y + 0.12, w: 0.55, h: 0.45,
    fontSize: 18, color: C.white, bold: true, align: "center", valign: "middle",
  });
  s10.addText(inn.title, {
    x: x + 0.9, y: y + 0.1, w: 4.8, h: 0.35,
    fontSize: 14, bold: true, color: C.dark,
  });
  s10.addText(inn.desc, {
    x: x + 0.9, y: y + 0.48, w: 4.8, h: 0.8,
    fontSize: 10, color: C.gray, lineSpacingMultiple: 1.5,
  });
});

// 模式创新
s10.addText("模式创新", {
  x: 0.6, y: 5.25, w: 2, h: 0.3,
  fontSize: 13, bold: true, color: C.primaryDk,
});
const modeInnovations = [
  { label: "离线优先", desc: "核心功能 100% 离线运行", color: C.primary },
  { label: "隐私保护", desc: "零知识架构，数据由用户掌控", color: C.warmRed },
  { label: "开源策略", desc: "核心算法 Apache 2.0 开源", color: C.success },
];
modeInnovations.forEach((m, i) => {
  const x = 0.6 + i * 4.1;
  s10.addShape(pptx.ShapeType.roundRect, {
    x: x, y: 5.65, w: 3.8, h: 0.85,
    fill: { color: m.color }, rectRadius: 0.1,
  });
  s10.addText(m.label, {
    x: x, y: 5.7, w: 3.8, h: 0.38,
    fontSize: 13, color: C.white, bold: true, align: "center",
  });
  s10.addText(m.desc, {
    x: x, y: 6.1, w: 3.8, h: 0.28,
    fontSize: 10, color: C.white, align: "center",
  });
});

// ============================================================
// Slide 11: 商业模式
// ============================================================
n++;
const s11 = pptx.addSlide();
s11.background = { fill: C.bg };
addSectionBar(s11, "商业模式与市场策略", "B2B2C + 免费增值，三线并行覆盖目标用户");
addFooter(s11); addPageNum(s11, n);

const bizModels = [
  { icon: "残联", title: "残联 / 社区合作", desc: "与各地残联、社区服务中心合作推广\n政府采购或补贴模式", c: C.primary },
  { icon: "增值", title: "免费增值模式", desc: "基础功能免费，高级功能付费\n唤醒 + 火灾 + 燃气免费", c: C.info },
  { icon: "预装", title: "硬件预装合作", desc: "与手机厂商 / 智能家居品牌合作\n系统级预装，触达更多用户", c: C.success },
  { icon: "开源", title: "开源社区生态", desc: "核心算法 Apache 2.0 开源\n吸引开发者贡献，接受安全审计", c: C.purple },
];

bizModels.forEach((b, i) => {
  const x = 0.6 + i * 3.15;
  s11.addShape(pptx.ShapeType.roundRect, {
    x: x, y: 1.45, w: 2.9, h: 3.8,
    fill: { color: C.white }, rectRadius: 0.12,
    shadow: { type: "outer", blur: 4, offset: 2, color: "000000", opacity: 0.05 },
    line: { color: b.c, width: 1.5 },
  });
  s11.addShape(pptx.ShapeType.rect, {
    x: x, y: 1.45, w: 2.9, h: 0.06,
    fill: { color: b.c },
  });
  // 图标圆
  s11.addShape(pptx.ShapeType.ellipse, {
    x: x + 0.85, y: 1.75, w: 1.2, h: 1.2,
    fill: { color: b.c },
  });
  s11.addText(b.icon, {
    x: x + 0.85, y: 1.75, w: 1.2, h: 1.2,
    fontSize: 18, color: C.white, bold: true, align: "center", valign: "middle",
  });
  s11.addText(b.title, {
    x: x + 0.15, y: 3.15, w: 2.6, h: 0.4,
    fontSize: 13, bold: true, color: C.dark, align: "center",
  });
  s11.addText(b.desc, {
    x: x + 0.15, y: 3.65, w: 2.6, h: 1.0,
    fontSize: 10, color: C.gray, align: "center", lineSpacingMultiple: 1.5,
  });
});

// 市场数据
s11.addShape(pptx.ShapeType.roundRect, {
  x: 0.6, y: 5.7, w: 12.1, h: 0.7,
  fill: { color: C.primaryDk }, rectRadius: 0.1,
});
s11.addText("目标市场：2700 万+ 听障人士  ×  零竞品蓝海市场  ×  国家科技助残政策红利", {
  x: 0.6, y: 5.7, w: 12.1, h: 0.7,
  fontSize: 15, color: C.white, bold: true, align: "center", valign: "middle",
});

// 演示站
s11.addShape(pptx.ShapeType.roundRect, {
  x: 3.5, y: 6.65, w: 6.33, h: 0.4,
  fill: { color: C.bgWarm }, rectRadius: 0.2,
  line: { color: C.primary, width: 1 },
});
s11.addText("演示站：http://47.108.149.191/", {
  x: 3.5, y: 6.65, w: 6.33, h: 0.4,
  fontSize: 10, color: C.primaryDk, align: "center", valign: "middle",
});

// ============================================================
// Slide 12: 核心团队
// ============================================================
n++;
const s12 = pptx.addSlide();
s12.background = { fill: C.white };
addSectionBar(s12, "核心团队", "微光科技 · 用 AI 的微光，守护无声世界");
addFooter(s12); addPageNum(s12, n);

const members = [
  { name: "蒲  洋", role: "项目负责人", dept: "整体规划 · 技术架构 · 加密算法设计", color: C.primary },
  { name: "代江宁", role: "AI 算法工程师", dept: "AI 模型训练 · TFLite 部署 · 手势识别", color: C.info },
  { name: "王慧平", role: "Android 开发工程师", dept: "客户端开发 · UI 设计 · 无障碍交互", color: C.success },
  { name: "黄浙洋", role: "产品 / 市场负责人", dept: "产品规划 · 用户调研 · 市场推广", color: C.purple },
];

members.forEach((m, i) => {
  const x = 0.6 + i * 3.15;
  s12.addShape(pptx.ShapeType.roundRect, {
    x: x, y: 1.45, w: 2.9, h: 4.2,
    fill: { color: C.bg }, rectRadius: 0.12,
    shadow: { type: "outer", blur: 4, offset: 2, color: "000000", opacity: 0.05 },
    line: { color: m.color, width: 1.5 },
  });
  s12.addShape(pptx.ShapeType.rect, {
    x: x, y: 1.45, w: 2.9, h: 0.06,
    fill: { color: m.color },
  });
  // 头像圆圈
  s12.addShape(pptx.ShapeType.ellipse, {
    x: x + 0.75, y: 1.75, w: 1.4, h: 1.4,
    fill: { color: m.color },
  });
  s12.addText(m.name.charAt(0), {
    x: x + 0.75, y: 1.75, w: 1.4, h: 1.4,
    fontSize: 40, color: C.white, bold: true, align: "center", valign: "middle",
  });
  s12.addText(m.name, {
    x: x + 0.15, y: 3.4, w: 2.6, h: 0.4,
    fontSize: 18, bold: true, color: C.dark, align: "center",
  });
  s12.addShape(pptx.ShapeType.roundRect, {
    x: x + 0.5, y: 3.85, w: 1.9, h: 0.32,
    fill: { color: m.color }, rectRadius: 0.15,
  });
  s12.addText(m.role, {
    x: x + 0.5, y: 3.85, w: 1.9, h: 0.32,
    fontSize: 10, color: C.white, bold: true, align: "center",
  });
  s12.addText(m.dept, {
    x: x + 0.15, y: 4.35, w: 2.6, h: 0.7,
    fontSize: 10, color: C.gray, align: "center", lineSpacingMultiple: 1.5,
  });
});

s12.addText("所属单位：天府新区通用航空职业学院", {
  x: 0.6, y: 6.1, w: 12.1, h: 0.35,
  fontSize: 12, color: C.gray, align: "center",
});

// ============================================================
// Slide 13: 社会效益
// ============================================================
n++;
const s13 = pptx.addSlide();
s13.background = { fill: C.darkBg };
addDarkTitle(s13, "社会效益与影响", "用科技创新消除听障人士的生活障碍");
addFooter(s13); addPageNum(s13, n);

const benefits = [
  { icon: "🛡️", title: "生命安全", desc: "火灾预警、燃气泄漏预警、跌倒检测\n直接保护听障人士生命安全", color: C.warmRed },
  { icon: "🌟", title: "生活质量", desc: "闹钟唤醒、敲门识别、健康监测\n提升日常生活便利性与独立性", color: C.primary },
  { icon: "🤝", title: "社会融入", desc: "手势识别、双向手语交互\n帮助听障人士更好地与外界沟通", color: C.info },
  { icon: "🔐", title: "隐私保护", desc: "零知识加密架构\n确保用户数据安全，树立科技向善典范", color: C.success },
  { icon: "📜", title: "行业推动", desc: "核心算法 Apache 2.0 开源\n推动科技助残行业技术进步", color: C.purple },
  { icon: "🎯", title: "大赛契合", desc: '"科创无碍，智享生活"\nAI 创新消除听障生活障碍', color: C.teal },
];

benefits.forEach((b, i) => {
  const col = i % 3;
  const row = Math.floor(i / 3);
  const x = 0.6 + col * 4.15;
  const y = 1.35 + row * 2.75;

  s13.addShape(pptx.ShapeType.roundRect, {
    x: x, y: y, w: 3.85, h: 2.4,
    fill: { color: C.darkBg2 }, rectRadius: 0.12,
    shadow: { type: "outer", blur: 5, offset: 2, color: "000000", opacity: 0.2 },
    line: { color: b.color, width: 1.5 },
  });
  s13.addShape(pptx.ShapeType.rect, {
    x: x, y: y, w: 3.85, h: 0.06,
    fill: { color: b.color },
  });
  s13.addText(b.icon, {
    x: x, y: y + 0.2, w: 3.85, h: 0.5,
    fontSize: 28, align: "center",
  });
  s13.addText(b.title, {
    x: x + 0.2, y: y + 0.75, w: 3.45, h: 0.3,
    fontSize: 15, bold: true, color: C.white, align: "center",
  });
  s13.addText(b.desc, {
    x: x + 0.2, y: y + 1.15, w: 3.45, h: 0.85,
    fontSize: 10, color: C.grayLt, align: "center", lineSpacingMultiple: 1.5,
  });
});

// ============================================================
// Slide 14: 发展规划
// ============================================================
n++;
const s14 = pptx.addSlide();
s14.background = { fill: C.white };
addSectionBar(s14, "发展规划", "三步走战略 — 从产品打磨到生态构建");
addFooter(s14); addPageNum(s14, n);

const roadmap = [
  { phase: "第一阶段", time: "2026 - 2027", title: "产品打磨", items: ["完成 6 大模块产品化开发", "通过残联渠道小范围试点", "收集用户反馈迭代优化", "完善无障碍交互体验"], color: C.primary },
  { phase: "第二阶段", time: "2027 - 2028", title: "规模推广", items: ["与各省残联建立合作", "上线应用商店免费下载", "开源核心算法社区", "建立用户社群运营体系"], color: C.info },
  { phase: "第三阶段", time: "2028 - 2029", title: "生态构建", items: ["拓展硬件预装合作", "智能家居 IoT 联动", "AI 手语翻译深度研发", "打造听障服务生态平台"], color: C.success },
];

roadmap.forEach((r, i) => {
  const x = 0.6 + i * 4.15;
  s14.addShape(pptx.ShapeType.roundRect, {
    x: x, y: 1.45, w: 3.85, h: 4.9,
    fill: { color: C.bg }, rectRadius: 0.12,
    shadow: { type: "outer", blur: 4, offset: 2, color: "000000", opacity: 0.05 },
    line: { color: r.color, width: 2 },
  });
  s14.addShape(pptx.ShapeType.rect, {
    x: x, y: 1.45, w: 3.85, h: 0.06,
    fill: { color: r.color },
  });
  s14.addShape(pptx.ShapeType.roundRect, {
    x: x + 0.6, y: 1.75, w: 2.65, h: 0.45,
    fill: { color: r.color }, rectRadius: 0.2,
  });
  s14.addText(r.phase, {
    x: x + 0.6, y: 1.75, w: 2.65, h: 0.45,
    fontSize: 14, color: C.white, bold: true, align: "center",
  });
  s14.addText(r.time, {
    x: x, y: 2.4, w: 3.85, h: 0.3,
    fontSize: 11, color: r.color, bold: true, align: "center",
  });
  s14.addText(r.title, {
    x: x, y: 2.8, w: 3.85, h: 0.35,
    fontSize: 16, bold: true, color: C.dark, align: "center",
  });
  s14.addShape(pptx.ShapeType.rect, {
    x: x + 0.8, y: 3.25, w: 2.25, h: 0.02,
    fill: { color: C.grayLt },
  });
  r.items.forEach((item, j) => {
    const iy = 3.5 + j * 0.42;
    s14.addShape(pptx.ShapeType.ellipse, {
      x: x + 0.7, y: iy + 0.06, w: 0.18, h: 0.18,
      fill: { color: r.color },
    });
    s14.addText(item, {
      x: x + 1.05, y: iy, w: 2.5, h: 0.3,
      fontSize: 10.5, color: C.dark,
    });
  });
});

// ============================================================
// Slide 15: 致谢
// ============================================================
n++;
const s15 = pptx.addSlide();
s15.background = { fill: C.darkBg };

// 装饰光晕
s15.addShape(pptx.ShapeType.ellipse, {
  x: 9.5, y: -1.5, w: 6, h: 6,
  fill: { color: C.primary },
  shadow: { type: "outer", blur: 50, offset: 0, color: C.primary, opacity: 0.4 },
});
s15.addShape(pptx.ShapeType.ellipse, {
  x: -1, y: 5.5, w: 3.5, h: 3.5,
  fill: { color: C.gold },
  shadow: { type: "outer", blur: 30, offset: 0, color: C.gold, opacity: 0.2 },
});

s15.addShape(pptx.ShapeType.rect, {
  x: 0, y: 0, w: 13.33, h: 0.06,
  fill: { color: C.primary },
});

s15.addText("感谢聆听", {
  x: 1, y: 1.5, w: 11.33, h: 1.0,
  fontSize: 52, color: C.white, bold: true, align: "center",
});
s15.addText("微光守护 — AI 多模态无声安全预警系统", {
  x: 1, y: 2.5, w: 11.33, h: 0.5,
  fontSize: 20, color: C.primaryLt, align: "center",
});

s15.addShape(pptx.ShapeType.rect, {
  x: 4.0, y: 3.2, w: 5.33, h: 0.03,
  fill: { color: C.gold },
});

s15.addText("以 AI 之手，搭建听障人士与世界沟通的桥梁", {
  x: 1, y: 3.5, w: 11.33, h: 0.45,
  fontSize: 15, color: C.grayLt, align: "center", italic: true,
});

s15.addText("微光科技（WeiGuang Tech）", {
  x: 1, y: 4.5, w: 11.33, h: 0.45,
  fontSize: 18, color: C.white, bold: true, align: "center",
});
s15.addText("天府新区通用航空职业学院", {
  x: 1, y: 4.95, w: 11.33, h: 0.35,
  fontSize: 13, color: C.gray, align: "center",
});

s15.addShape(pptx.ShapeType.roundRect, {
  x: 4.5, y: 5.7, w: 4.33, h: 0.45,
  fill: { color: C.darkBg2 }, rectRadius: 0.2,
  line: { color: C.primary, width: 1 },
});
s15.addText("演示站：http://47.108.149.191/", {
  x: 4.5, y: 5.7, w: 4.33, h: 0.45,
  fontSize: 10, color: C.primaryLt, align: "center", valign: "middle",
});

s15.addText("核心算法开源：Apache 2.0  |  2026 年 7 月", {
  x: 1, y: 6.4, w: 11.33, h: 0.35,
  fontSize: 10, color: C.gray, align: "center",
});

s15.addShape(pptx.ShapeType.rect, {
  x: 0, y: 7.44, w: 13.33, h: 0.06,
  fill: { color: C.primary },
});

// ============================================================
// 生成
// ============================================================
const outputPath = "f:/java/weiguangplus/weiguang123/参赛材料/微光科技_微光守护_路演PPT_暖色版.pptx";
pptx.writeFile({ fileName: outputPath }).then(() => {
  console.log("PPT 暖色完整版已生成: " + outputPath);
  console.log("共 " + n + " 页幻灯片");
  console.log("暖色系配色：火焰橙 + 暖金 + 暖红 + 米白背景");
}).catch((err) => {
  console.error("生成失败:", err);
});