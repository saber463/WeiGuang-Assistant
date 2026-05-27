const pptxgen = require("pptxgenjs");

const pptx = new pptxgen();
pptx.layout = "LAYOUT_WIDE";
pptx.author = "微光畅行团队";
pptx.subject = "微光畅行 - 十五五科技助残创新应用路演";

// ── Tech Compassion Theme ──
const ELECTRIC_BLUE = "0066FF";
const NEON_CYAN     = "00D4FF";
const DEEP_NAVY     = "0B1628";
const DARK_NAVY     = "111D35";
const CHARCOAL      = "1A2A44";
const STEEL_BLUE    = "4A7BC7";
const ICE_BLUE      = "D6E8FF";
const TEAL          = "00BFA5";
const WARM_WHITE    = "F0F5FF";
const WHITE         = "FFFFFF";
const WARN_RED      = "FF4757";
const AMBER         = "FFB347";
const BG_CREAM      = "F5F7FA";

const helpers = require("./helpers/layout.js");
const FONT = "Microsoft YaHei";

function addGlowCircle(slide, x, y, size, color, opacity) {
  slide.addShape(pptx.ShapeType.ellipse, {
    x, y, w: size, h: size,
    fill: { color: color || ELECTRIC_BLUE, transparency: opacity || 85 }
  });
}

function addPageNum(slide, num, total) {
  slide.addText("第 " + num + " / " + total + " 页", {
    x: 11.5, y: 7.0, w: 1.5, h: 0.35,
    fontSize: 9, fontFace: FONT, color: "8899AA",
    align: "right"
  });
}

function addAccentBar(slide, y, w, color) {
  slide.addShape(pptx.ShapeType.rect, {
    x: 0.6, y: y, w: w || 3, h: 0.03, fill: { color: color || ELECTRIC_BLUE }
  });
}

const TOTAL = 9;

// ═══════════════════════════════════════════════════
// 第1页：封面（15秒）
// ═══════════════════════════════════════════════════
{
  const slide = pptx.addSlide();
  slide.background = { color: DEEP_NAVY };
  addGlowCircle(slide, 9.5, -0.8, 4.5, ELECTRIC_BLUE, 88);
  addGlowCircle(slide, 10.8, 2.0, 2.8, NEON_CYAN, 90);
  addGlowCircle(slide, -1.5, 4.0, 3.5, ELECTRIC_BLUE, 92);

  slide.addShape(pptx.ShapeType.rect, {
    x: 0.8, y: 1.2, w: 0.04, h: 5.0, fill: { color: ELECTRIC_BLUE }
  });

  slide.addText("微光畅行", {
    x: 1.2, y: 1.6, w: 10, h: 1.4,
    fontSize: 56, fontFace: FONT, color: WHITE, bold: true
  });

  slide.addShape(pptx.ShapeType.rect, {
    x: 1.2, y: 3.0, w: 4.5, h: 0.04, fill: { color: NEON_CYAN }
  });

  slide.addText("响应「十五五」科技助残号召\n全场景无障碍出行助手", {
    x: 1.2, y: 3.2, w: 10, h: 0.7,
    fontSize: 18, fontFace: FONT, color: ICE_BLUE
  });

  slide.addShape(pptx.ShapeType.roundRect, {
    x: 1.2, y: 4.2, w: 3.6, h: 0.45,
    fill: { color: ELECTRIC_BLUE, transparency: 70 },
    rectRadius: 0.1,
    line: { color: ELECTRIC_BLUE, width: 1 }
  });
  slide.addText("AI + 自主研发算法", {
    x: 1.2, y: 4.2, w: 3.6, h: 0.45,
    fontSize: 13, fontFace: FONT, color: NEON_CYAN,
    align: "center", valign: "middle"
  });

  slide.addText("5分钟路演 · 2026", {
    x: 1.2, y: 6.2, w: 5, h: 0.4,
    fontSize: 14, fontFace: FONT, color: STEEL_BLUE
  });
  slide.addText("政策来源：中国政府网 · 中国残联\n数据来源：央视网 · 中国残疾人网 · 搜狐旅游调研", {
    x: 1.2, y: 6.55, w: 11, h: 0.5,
    fontSize: 9, fontFace: FONT, color: "2A4A7A"
  });
  addPageNum(slide, 1, TOTAL);
  helpers.warnIfSlideHasOverlaps(slide, pptx);
  helpers.warnIfSlideElementsOutOfBounds(slide, pptx);
}

// ═══════════════════════════════════════════════════
// 第2页：政策背景（25秒）
// ═══════════════════════════════════════════════════
{
  const slide = pptx.addSlide();
  slide.background = { color: BG_CREAM };

  slide.addShape(pptx.ShapeType.rect, {
    x: 0, y: 0, w: 0.06, h: 7.5, fill: { color: ELECTRIC_BLUE }
  });

  slide.addText("第2页 · 十五五定了什么新方向？", {
    x: 0.6, y: 0.3, w: 12, h: 0.6,
    fontSize: 22, fontFace: FONT, color: DEEP_NAVY, bold: true
  });
  slide.addText("两个关键政策，指明了方向", {
    x: 0.6, y: 0.85, w: 12, h: 0.4,
    fontSize: 14, fontFace: FONT, color: STEEL_BLUE
  });
  addAccentBar(slide, 1.2, 3.5, ELECTRIC_BLUE);

  slide.addShape(pptx.ShapeType.roundRect, {
    x: 0.6, y: 1.5, w: 12.1, h: 1.8,
    fill: { color: WHITE }, rectRadius: 0.12,
    shadow: { type: "outer", blur: 6, offset: 2, color: "000000", opacity: 0.08 }
  });
  slide.addShape(pptx.ShapeType.roundRect, {
    x: 0.6, y: 1.5, w: 0.08, h: 1.8, fill: { color: ELECTRIC_BLUE }
  });
  slide.addText([
    { text: "政策一：科技助残，写入了十五五规划\n", options: { fontSize: 15, fontFace: FONT, bold: true, color: DEEP_NAVY } },
    { text: "• 重点推进「科技助残创新应用」，人工智能上前沿技术要用起来\n", options: { fontSize: 13, fontFace: FONT, color: CHARCOAL } },
    { text: "• 深入实施《无障碍环境建设法》，物理和数字无障碍一起抓", options: { fontSize: 13, fontFace: FONT, color: CHARCOAL } },
    { text: "\n【此处插入：国新办发布会官方通知截图 | 可访问网址：https://www.gov.cn/lianbo/fabu/202507/content_7033401.htm】", options: { fontSize: 8, fontFace: FONT, color: WARN_RED } }
  ], { x: 0.9, y: 1.6, w: 11.5, h: 1.6, valign: "top", lineSpacingMultiple: 1.4 });

  slide.addShape(pptx.ShapeType.roundRect, {
    x: 0.6, y: 3.6, w: 12.1, h: 1.8,
    fill: { color: WHITE }, rectRadius: 0.12,
    shadow: { type: "outer", blur: 6, offset: 2, color: "000000", opacity: 0.08 }
  });
  slide.addShape(pptx.ShapeType.roundRect, {
    x: 0.6, y: 3.6, w: 0.08, h: 1.8, fill: { color: TEAL }
  });
  slide.addText([
    { text: "政策二：从项目化转向制度化保障\n", options: { fontSize: 15, fontFace: FONT, bold: true, color: DEEP_NAVY } },
    { text: "• 推动残疾人事业从「项目化推进」变成「制度化保障」\n", options: { fontSize: 13, fontFace: FONT, color: CHARCOAL } },
    { text: "• 重点解决数字无障碍短板，让残疾人平等享受互联网服务", options: { fontSize: 13, fontFace: FONT, color: CHARCOAL } },
    { text: "\n【此处插入：中国残联「十五五」目标研究截图 | 可访问网址：https://www.cdpf.org.cn/ywpd/llyj/llwz/dc107719d1ac47d89dbe2244a049723f_mobile.htm】", options: { fontSize: 8, fontFace: FONT, color: WARN_RED } }
  ], { x: 0.9, y: 3.7, w: 11.5, h: 1.6, valign: "top", lineSpacingMultiple: 1.4 });

  slide.addShape(pptx.ShapeType.roundRect, {
    x: 2.5, y: 5.8, w: 8.3, h: 0.65,
    fill: { color: DEEP_NAVY }, rectRadius: 0.12
  });
  slide.addText("那现实情况怎么样？差距有多大？", {
    x: 2.5, y: 5.8, w: 8.3, h: 0.65,
    fontSize: 16, fontFace: FONT, color: WHITE, bold: true,
    align: "center", valign: "middle"
  });
  addPageNum(slide, 2, TOTAL);
  helpers.warnIfSlideHasOverlaps(slide, pptx);
  helpers.warnIfSlideElementsOutOfBounds(slide, pptx);
}

// ═══════════════════════════════════════════════════
// 第3页：痛点+解决方案 同页对应展示（35秒） ★★★ 核心改造
// ═══════════════════════════════════════════════════
{
  const slide = pptx.addSlide();
  slide.background = { color: BG_CREAM };

  slide.addText("第3页 · 痛点在哪，解决方案就在哪", {
    x: 0.6, y: 0.2, w: 12, h: 0.55,
    fontSize: 22, fontFace: FONT, color: DEEP_NAVY, bold: true
  });
  slide.addText("每个痛点都有对应的功能和自主研发算法", {
    x: 0.6, y: 0.7, w: 12, h: 0.35,
    fontSize: 13, fontFace: FONT, color: STEEL_BLUE
  });

  const pairs = [
    {
      pain: { title: "痛点①：出行困难", data: "65%有外出意愿，仅12%成行", desc: "交通不便占74%，残疾人无法独立乘坐公交" },
      solution: { title: "解决办法", feature: "公交智能报站+GPS定位", algo: "自主研发GPS定位算法", tech: "GPS模块 + CameraX + TTS", tag: "核心技术", tagColor: ELECTRIC_BLUE }
    },
    {
      pain: { title: "痛点②：信息缺失", data: "信息服务不足占61%", desc: "听障听不到报站、火警、路人说话" },
      solution: { title: "解决办法", feature: "语音转文字+三重强提醒", algo: "自主研发多模态感知算法", tech: "SpeechRecognizer + VibrationEngine", tag: "核心技术", tagColor: TEAL }
    },
    {
      pain: { title: "痛点③：沟通障碍", data: "路人不懂手语", desc: "突发状况无法电话求助，沟通信息不足" },
      solution: { title: "解决办法", feature: "双向手语互通+一键SOS", algo: "自主研发手语识别算法", tech: "MediaPipe底座 + 自研117条短语库", tag: "核心技术", tagColor: AMBER }
    }
  ];

  pairs.forEach((pair, i) => {
    const yBase = 1.2 + i * 2.0;
    const cardH = 1.8;

    // 左侧：痛点卡片
    slide.addShape(pptx.ShapeType.roundRect, {
      x: 0.6, y: yBase, w: 5.5, h: cardH,
      fill: { color: WHITE }, rectRadius: 0.12,
      shadow: { type: "outer", blur: 4, offset: 2, color: "000000", opacity: 0.06 }
    });
    slide.addShape(pptx.ShapeType.rect, {
      x: 0.6, y: yBase, w: 5.5, h: 0.05, fill: { color: WARN_RED }
    });
    slide.addText(pair.pain.title, {
      x: 0.8, y: yBase + 0.1, w: 5.1, h: 0.35,
      fontSize: 14, fontFace: FONT, color: WARN_RED, bold: true
    });
    slide.addText(pair.pain.data, {
      x: 0.8, y: yBase + 0.45, w: 5.1, h: 0.45,
      fontSize: 18, fontFace: FONT, color: WARN_RED, bold: true
    });
    slide.addText(pair.pain.desc, {
      x: 0.8, y: yBase + 0.9, w: 5.1, h: 0.7,
      fontSize: 11, fontFace: FONT, color: CHARCOAL
    });

    // 中间箭头
    slide.addText("→", {
      x: 6.2, y: yBase + 0.5, w: 0.7, h: 0.6,
      fontSize: 28, fontFace: FONT, color: ELECTRIC_BLUE, bold: true,
      align: "center", valign: "middle"
    });

    // 右侧：解决方案卡片
    slide.addShape(pptx.ShapeType.roundRect, {
      x: 6.9, y: yBase, w: 6.4, h: cardH,
      fill: { color: WHITE }, rectRadius: 0.12,
      shadow: { type: "outer", blur: 4, offset: 2, color: "000000", opacity: 0.06 }
    });
    slide.addShape(pptx.ShapeType.rect, {
      x: 6.9, y: yBase, w: 6.4, h: 0.05, fill: { color: pair.solution.tagColor }
    });

    // 解决方案内容
    slide.addText(pair.solution.feature, {
      x: 7.1, y: yBase + 0.1, w: 6.0, h: 0.35,
      fontSize: 14, fontFace: FONT, color: DEEP_NAVY, bold: true
    });

    // 自主研发算法标签
    slide.addShape(pptx.ShapeType.roundRect, {
      x: 7.1, y: yBase + 0.5, w: 5.0, h: 0.3,
      fill: { color: pair.solution.tagColor, transparency: 85 }, rectRadius: 0.08,
      line: { color: pair.solution.tagColor, width: 0.5 }
    });
    slide.addText(pair.solution.algo, {
      x: 7.1, y: yBase + 0.5, w: 5.0, h: 0.3,
      fontSize: 10, fontFace: FONT, color: pair.solution.tagColor, bold: true,
      align: "center", valign: "middle"
    });

    // 技术栈
    slide.addText("技术栈：", {
      x: 7.1, y: yBase + 0.9, w: 1.2, h: 0.3,
      fontSize: 10, fontFace: FONT, color: STEEL_BLUE, bold: true
    });
    slide.addText(pair.solution.tech, {
      x: 8.2, y: yBase + 0.9, w: 4.9, h: 0.3,
      fontSize: 10, fontFace: FONT, color: CHARCOAL
    });
  });

  // 底部总结
  slide.addShape(pptx.ShapeType.roundRect, {
    x: 0.6, y: 6.4, w: 12.1, h: 0.5,
    fill: { color: DEEP_NAVY }, rectRadius: 0.12
  });
  slide.addText("三大痛点 → 三大自主研发算法 → 十二项核心功能", {
    x: 0.6, y: 6.4, w: 12.1, h: 0.5,
    fontSize: 14, fontFace: FONT, color: WHITE, bold: true,
    align: "center", valign: "middle"
  });
  addPageNum(slide, 3, TOTAL);
  helpers.warnIfSlideHasOverlaps(slide, pptx);
  helpers.warnIfSlideElementsOutOfBounds(slide, pptx);
}

// ═══════════════════════════════════════════════════
// 第4页：自主研发算法技术栈全貌（30秒） ★★★ 新增
// ═══════════════════════════════════════════════════
{
  const slide = pptx.addSlide();
  slide.background = { color: DEEP_NAVY };

  addGlowCircle(slide, -1, -1, 4, ELECTRIC_BLUE, 90);
  addGlowCircle(slide, 11, 5, 4, NEON_CYAN, 92);

  slide.addText("第4页 · 自主研发算法技术栈", {
    x: 0.6, y: 0.25, w: 12, h: 0.55,
    fontSize: 22, fontFace: FONT, color: WHITE, bold: true
  });
  slide.addText("五大核心算法，全部自主研发，适配全残障人群", {
    x: 0.6, y: 0.75, w: 12, h: 0.3,
    fontSize: 13, fontFace: FONT, color: ICE_BLUE
  });
  addAccentBar(slide, 1.0, 5, NEON_CYAN);

  const algos = [
    { name: "手语识别算法", icon: "✋", desc: "ML Kit手势检测 + 117条高频短语库\n语音↔手语实时互转，双屏对话", color: ELECTRIC_BLUE },
    { name: "药品OCR算法", icon: "💊", desc: "中文文字识别 + 103种药品库匹配\n风险分级预警，三色安全指示", color: TEAL },
    { name: "GPS公交算法", icon: "🚌", desc: "10条线路预置 + 到站距离计算\n到站震动+弹窗双重提醒", color: WARN_RED },
    { name: "多模态感知算法", icon: "📳", desc: "20种震动编码 + 场景感知融合\n四级告警机制，三重联动提醒", color: AMBER },
    { name: "物品识别算法", icon: "👁️", desc: "ML Kit图像标签 + 置信度过滤\n实时识别+语音播报联动", color: NEON_CYAN }
  ];

  algos.forEach((algo, i) => {
    const col = i % 3, row = Math.floor(i / 3);
    const xPos = 0.6 + col * 4.1, yPos = 1.2 + row * 2.8;
    const cw = 3.8, ch = 2.5;

    slide.addShape(pptx.ShapeType.roundRect, {
      x: xPos, y: yPos, w: cw, h: ch,
      fill: { color: DARK_NAVY }, rectRadius: 0.15,
      line: { color: algo.color, width: 1.2 },
      shadow: { type: "outer", blur: 8, offset: 3, color: "000000", opacity: 0.3 }
    });

    slide.addShape(pptx.ShapeType.ellipse, {
      x: xPos + 0.25, y: yPos + 0.2, w: 0.55, h: 0.55,
      fill: { color: algo.color, transparency: 70 }
    });
    slide.addText(algo.icon, {
      x: xPos + 0.25, y: yPos + 0.2, w: 0.55, h: 0.55,
      fontSize: 18, align: "center", valign: "middle"
    });

    slide.addText(algo.name, {
      x: xPos + 0.9, y: yPos + 0.2, w: cw - 1.15, h: 0.5,
      fontSize: 14, fontFace: FONT, color: WHITE, bold: true,
      valign: "middle"
    });

    slide.addText(algo.desc, {
      x: xPos + 0.25, y: yPos + 0.85, w: cw - 0.5, h: 1.4,
      fontSize: 11, fontFace: FONT, color: ICE_BLUE,
      lineSpacingMultiple: 1.4
    });

    // 自主研发标签
    slide.addShape(pptx.ShapeType.roundRect, {
      x: xPos + cw - 2.2, y: yPos + ch - 0.4, w: 2.0, h: 0.25,
      fill: { color: algo.color, transparency: 80 }, rectRadius: 0.06,
      line: { color: algo.color, width: 0.5 }
    });
    slide.addText("自主研发", {
      x: xPos + cw - 2.2, y: yPos + ch - 0.4, w: 2.0, h: 0.25,
      fontSize: 8, fontFace: FONT, color: algo.color, align: "center", valign: "middle"
    });
  });

  // 底部全景图引用
  slide.addShape(pptx.ShapeType.roundRect, {
    x: 0.6, y: 6.2, w: 12.1, h: 0.7,
    fill: { color: DARK_NAVY }, rectRadius: 0.12,
    line: { color: ELECTRIC_BLUE, width: 0.5 }
  });
  slide.addText("【算法架构总览图】", {
    x: 0.6, y: 6.2, w: 12.1, h: 0.7,
    fontSize: 11, fontFace: FONT, color: NEON_CYAN,
    align: "center", valign: "middle"
  });
  slide.addText("此处可插入：自主研发算法体系思维导图 | 算法流程图", {
    x: 0.6, y: 6.4, w: 12.1, h: 0.3,
    fontSize: 8, fontFace: FONT, color: STEEL_BLUE,
    align: "center"
  });

  addPageNum(slide, 4, TOTAL);
  helpers.warnIfSlideHasOverlaps(slide, pptx);
  helpers.warnIfSlideElementsOutOfBounds(slide, pptx);
}

// ═══════════════════════════════════════════════════
// 第5页：功能→痛点→技术栈 对应表（30秒） ★★★ 改造
// ═══════════════════════════════════════════════════
{
  const slide = pptx.addSlide();
  slide.background = { color: BG_CREAM };

  slide.addShape(pptx.ShapeType.rect, {
    x: 0, y: 0, w: 0.06, h: 7.5, fill: { color: ELECTRIC_BLUE }
  });

  slide.addText("第5页 · 每个功能都对应一个痛点", {
    x: 0.6, y: 0.2, w: 12, h: 0.5,
    fontSize: 22, fontFace: FONT, color: DEEP_NAVY, bold: true
  });
  slide.addText("12项功能 · 对应痛点 · 自主研发算法 · 技术栈", {
    x: 0.6, y: 0.65, w: 12, h: 0.3,
    fontSize: 13, fontFace: FONT, color: STEEL_BLUE
  });
  addAccentBar(slide, 0.9, 4, ELECTRIC_BLUE);

  const hdr = { bold: true, color: WHITE, fontSize: 9, fontFace: FONT };
  const cel = { color: CHARCOAL, fontSize: 9, fontFace: FONT };
  const tagStyle = { color: ELECTRIC_BLUE, fontSize: 9, fontFace: FONT, bold: true };

  const trows = [
    [
      { text: "功能分类", options: { ...hdr, fill: { color: ELECTRIC_BLUE } } },
      { text: "功能名称", options: { ...hdr, fill: { color: ELECTRIC_BLUE } } },
      { text: "对应痛点", options: { ...hdr, fill: { color: ELECTRIC_BLUE } } },
      { text: "自主研发算法", options: { ...hdr, fill: { color: DEEP_NAVY } } },
      { text: "技术栈", options: { ...hdr, fill: { color: DEEP_NAVY } } }
    ],
    [
      { text: "沟通", options: { ...cel, bold: true } },
      { text: "小玉手语", options: cel },
      { text: "路人不懂手语(61%)", options: cel },
      { text: "手语识别算法", options: tagStyle },
      { text: "ML Kit+117条短语库", options: cel }
    ],
    [
      { text: "沟通", options: { ...cel, bold: true } },
      { text: "双向对话", options: cel },
      { text: "沟通信息不足(61%)", options: cel },
      { text: "手语识别算法", options: tagStyle },
      { text: "SpeechRecognizer+TTS", options: cel }
    ],
    [
      { text: "沟通", options: { ...cel, bold: true } },
      { text: "语音助手", options: cel },
      { text: "手机操作困难", options: cel },
      { text: "多模态感知算法", options: tagStyle },
      { text: "7大品牌AI唤醒", options: cel }
    ],
    [
      { text: "出行", options: { ...cel, bold: true } },
      { text: "公交报站", options: cel },
      { text: "交通不便占74%", options: cel },
      { text: "GPS公交定位算法", options: tagStyle },
      { text: "GPS+震动+弹窗", options: cel }
    ],
    [
      { text: "出行", options: { ...cel, bold: true } },
      { text: "网约车助手", options: cel },
      { text: "无法沟通司机", options: cel },
      { text: "多模态感知算法", options: tagStyle },
      { text: "SpeechRecognizer", options: cel }
    ],
    [
      { text: "出行", options: { ...cel, bold: true } },
      { text: "无障碍地图", options: cel },
      { text: "设施缺失(68%)", options: cel },
      { text: "GPS公交定位算法", options: tagStyle },
      { text: "地图SDK+上报", options: cel }
    ],
    [
      { text: "安全", options: { ...cel, bold: true } },
      { text: "一键应急", options: cel },
      { text: "突发无人能帮", options: cel },
      { text: "多模态感知算法", options: tagStyle },
      { text: "SMS+GPS+闪光灯", options: cel }
    ],
    [
      { text: "安全", options: { ...cel, bold: true } },
      { text: "三重强提醒", options: cel },
      { text: "听不到警报(84%)", options: cel },
      { text: "多模态感知算法", options: tagStyle },
      { text: "震动+声音+闪光", options: cel }
    ],
    [
      { text: "安全", options: { ...cel, bold: true } },
      { text: "服务中心", options: cel },
      { text: "缺乏线下服务", options: cel },
      { text: "多模态感知算法", options: tagStyle },
      { text: "预约+申领系统", options: cel }
    ],
    [
      { text: "生活", options: { ...cel, bold: true } },
      { text: "药品识别", options: cel },
      { text: "看不清药盒说明", options: cel },
      { text: "药品OCR算法", options: tagStyle },
      { text: "ML Kit OCR+103种库", options: cel }
    ],
    [
      { text: "生活", options: { ...cel, bold: true } },
      { text: "视觉辅助", options: cel },
      { text: "视障无法感知环境", options: cel },
      { text: "物品识别算法", options: tagStyle },
      { text: "ML Kit标签+语音", options: cel }
    ],
    [
      { text: "生活", options: { ...cel, bold: true } },
      { text: "学习中心", options: cel },
      { text: "安全知识缺失", options: cel },
      { text: "手语识别算法", options: tagStyle },
      { text: "图文+手语课程", options: cel }
    ]
  ];

  slide.addTable(trows, {
    x: 0.4, y: 1.1, w: 12.5,
    colW: [1.0, 1.8, 2.8, 2.8, 4.1],
    border: { type: "solid", pt: 0.5, color: "C0D0E0" },
    rowH: [0.45, 0.38, 0.38, 0.38, 0.38, 0.38, 0.38, 0.38, 0.38, 0.38, 0.38, 0.38, 0.38]
  });

  // 底部总结
  slide.addShape(pptx.ShapeType.roundRect, {
    x: 0.6, y: 6.3, w: 12.1, h: 0.6,
    fill: { color: DEEP_NAVY }, rectRadius: 0.12
  });
  slide.addText("所有功能均以自主研发算法为底座，痛点→算法→功能，闭环对应", {
    x: 0.6, y: 6.3, w: 12.1, h: 0.6,
    fontSize: 13, fontFace: FONT, color: WHITE,
    align: "center", valign: "middle"
  });

  addPageNum(slide, 5, TOTAL);
  helpers.warnIfSlideHasOverlaps(slide, pptx);
  helpers.warnIfSlideElementsOutOfBounds(slide, pptx);
}

// ═══════════════════════════════════════════════════
// 第6页：三大独家功能深度解析（40秒） ★★★ 改造：增加算法标签
// ═══════════════════════════════════════════════════
{
  const slide = pptx.addSlide();
  slide.background = { color: BG_CREAM };

  slide.addShape(pptx.ShapeType.rect, {
    x: 0, y: 0, w: 0.06, h: 7.5, fill: { color: ELECTRIC_BLUE }
  });

  slide.addText("第6页 · 三大独家功能深度解析", {
    x: 0.6, y: 0.25, w: 12, h: 0.55,
    fontSize: 22, fontFace: FONT, color: DEEP_NAVY, bold: true
  });
  addAccentBar(slide, 0.75, 5, ELECTRIC_BLUE);

  const specials = [
    {
      num: "②",
      title: "双向手语互通",
      algo: "自主研发手语识别算法",
      bullet: [
        "痛点：路人不懂手语，沟通信息不足(61%)",
        "功能：双屏实时对话，上半屏手语→语音，下半屏语音→文字",
        "技术栈：ML Kit手势检测底座 + 自主研发117条高频短语库",
        "效果：听障人和健听人用一台手机就能对话"
      ],
      color: ELECTRIC_BLUE
    },
    {
      num: "③",
      title: "公交智能报站",
      algo: "自主研发GPS定位算法",
      bullet: [
        "痛点：交通不便占74%，残疾人无法独立坐公交",
        "功能：GPS实时定位，10条线路预置，到站震动+弹窗提醒",
        "技术栈：GPS定位模块 + 自主研发到站距离计算引擎",
        "效果：视障人士也能独立乘坐公共交通"
      ],
      color: TEAL
    },
    {
      num: "④",
      title: "一键应急求助",
      algo: "自主研发多模态感知算法",
      bullet: [
        "痛点：突发状况无人能帮，电话求助听不到",
        "功能：6种预设场景，一键SOS短信+GPS位置+闪光灯",
        "技术栈：自主研发20种震动编码 + TripleAlert三重联动系统",
        "效果：紧急时刻自动通知紧急联系人，吸引周围人注意"
      ],
      color: WARN_RED
    }
  ];

  specials.forEach((sp, i) => {
    const yPos = 1.0 + i * 2.0;

    slide.addShape(pptx.ShapeType.roundRect, {
      x: 0.6, y: yPos, w: 12.1, h: 1.85,
      fill: { color: WHITE }, rectRadius: 0.15,
      shadow: { type: "outer", blur: 6, offset: 2, color: "000000", opacity: 0.08 }
    });

    slide.addShape(pptx.ShapeType.roundRect, {
      x: 0.6, y: yPos, w: 12.1, h: 0.04, fill: { color: sp.color }
    });

    // 编号
    slide.addShape(pptx.ShapeType.ellipse, {
      x: 0.85, y: yPos + 0.2, w: 0.45, h: 0.45,
      fill: { color: sp.color, transparency: 15 }
    });
    slide.addText(sp.num, {
      x: 0.85, y: yPos + 0.2, w: 0.45, h: 0.45,
      fontSize: 15, fontFace: FONT, color: sp.color, bold: true,
      align: "center", valign: "middle"
    });

    slide.addText(sp.title, {
      x: 1.4, y: yPos + 0.15, w: 5, h: 0.45,
      fontSize: 16, fontFace: FONT, color: DEEP_NAVY, bold: true,
      valign: "middle"
    });

    // 自主研发算法标签
    slide.addShape(pptx.ShapeType.roundRect, {
      x: 7.5, y: yPos + 0.2, w: 4.8, h: 0.3,
      fill: { color: sp.color, transparency: 85 }, rectRadius: 0.08,
      line: { color: sp.color, width: 0.5 }
    });
    slide.addText(sp.algo, {
      x: 7.5, y: yPos + 0.2, w: 4.8, h: 0.3,
      fontSize: 10, fontFace: FONT, color: sp.color, bold: true,
      align: "center", valign: "middle"
    });

    // 痛点和功能
    slide.addText(sp.bullet.join("\n"), {
      x: 1.4, y: yPos + 0.6, w: 10.8, h: 1.15,
      fontSize: 11, fontFace: FONT, color: CHARCOAL,
      lineSpacingMultiple: 1.35
    });
  });

  // 无障碍强调
  slide.addShape(pptx.ShapeType.roundRect, {
    x: 0.6, y: 6.3, w: 12.1, h: 0.5,
    fill: { color: DEEP_NAVY }, rectRadius: 0.12
  });
  slide.addText("三大功能均以自主研发算法为驱动，痛点→算法→效果 完整闭环", {
    x: 0.6, y: 6.3, w: 12.1, h: 0.5,
    fontSize: 13, fontFace: FONT, color: WHITE,
    align: "center", valign: "middle"
  });

  addPageNum(slide, 6, TOTAL);
  helpers.warnIfSlideHasOverlaps(slide, pptx);
  helpers.warnIfSlideElementsOutOfBounds(slide, pptx);
}

// ═══════════════════════════════════════════════════
// 第7页：竞品对比（1分钟）
// ═══════════════════════════════════════════════════
{
  const slide = pptx.addSlide();
  slide.background = { color: BG_CREAM };

  slide.addShape(pptx.ShapeType.rect, {
    x: 0, y: 0, w: 0.06, h: 7.5, fill: { color: ELECTRIC_BLUE }
  });

  slide.addText("第7页 · 竞品对比：微光畅行优势明显", {
    x: 0.6, y: 0.25, w: 12, h: 0.55,
    fontSize: 22, fontFace: FONT, color: DEEP_NAVY, bold: true
  });
  addAccentBar(slide, 0.75, 4, ELECTRIC_BLUE);

  const hdr = { bold: true, color: WHITE, fontSize: 10, fontFace: FONT };
  const cel = { color: CHARCOAL, fontSize: 10, fontFace: FONT };
  const bad = { color: "999999", fontSize: 10, fontFace: FONT };
  const good = { color: TEAL, fontSize: 10, fontFace: FONT, bold: true };

  const trows = [
    [
      { text: "对比维度 (7项)", options: { ...hdr, fill: { color: ELECTRIC_BLUE } } },
      { text: "竞品A", options: { ...hdr, fill: { color: CHARCOAL } } },
      { text: "竞品B", options: { ...hdr, fill: { color: CHARCOAL } } },
      { text: "竞品C", options: { ...hdr, fill: { color: CHARCOAL } } },
      { text: "微光畅行", options: { ...hdr, fill: { color: DEEP_NAVY } } }
    ],
    [
      { text: "功能全覆盖度", options: cel },
      { text: "仅覆盖单一人群", options: bad },
      { text: "仅覆盖单一人群", options: bad },
      { text: "仅覆盖单一人群", options: bad },
      { text: "覆盖全残障人群", options: good }
    ],
    [
      { text: "全残障人群适配", options: cel },
      { text: "仅听障", options: bad },
      { text: "仅视障", options: bad },
      { text: "仅视障", options: bad },
      { text: "听障+视障+肢残", options: good }
    ],
    [
      { text: "自主研发算法", options: cel },
      { text: "无", options: bad },
      { text: "部分", options: bad },
      { text: "无", options: bad },
      { text: "5大核心自主研发算法", options: good }
    ],
    [
      { text: "痛点→功能闭环", options: cel },
      { text: "功能与痛点脱节", options: bad },
      { text: "功能与痛点脱节", options: bad },
      { text: "部分对应", options: cel },
      { text: "每个功能对应具体痛点", options: good }
    ],
    [
      { text: "无障碍原生设计", options: cel },
      { text: "无", options: bad },
      { text: "部分", options: bad },
      { text: "无", options: bad },
      { text: "语音+大字+色盲+震动", options: good }
    ],
    [
      { text: "线下服务联动", options: cel },
      { text: "无", options: bad },
      { text: "有限", options: bad },
      { text: "有", options: cel },
      { text: "基地帮扶+设备申领", options: good }
    ],
    [
      { text: "响应十五五科技助残", options: cel },
      { text: "未响应", options: { color: WARN_RED, fontSize: 10, fontFace: FONT } },
      { text: "未响应", options: { color: WARN_RED, fontSize: 10, fontFace: FONT } },
      { text: "未响应", options: { color: WARN_RED, fontSize: 10, fontFace: FONT } },
      { text: "全面响应科技助残", options: { color: TEAL, fontSize: 11, fontFace: FONT, bold: true } }
    ]
  ];

  slide.addTable(trows, {
    x: 0.5, y: 1.1, w: 12.3,
    colW: [2.2, 1.8, 1.8, 1.8, 4.7],
    border: { type: "solid", pt: 0.5, color: "C0D0E0" },
    rowH: [0.5, 0.42, 0.42, 0.42, 0.42, 0.42, 0.42, 0.5]
  });

  slide.addShape(pptx.ShapeType.roundRect, {
    x: 0.6, y: 5.6, w: 12.1, h: 0.75,
    fill: { color: DEEP_NAVY }, rectRadius: 0.12,
    shadow: { type: "outer", blur: 8, offset: 2, color: "000000", opacity: 0.2 }
  });
  slide.addShape(pptx.ShapeType.roundRect, {
    x: 0.6, y: 5.6, w: 12.1, h: 0.04, fill: { color: NEON_CYAN }
  });
  slide.addText("结论：微光畅行是唯一全面响应十五五规划要求的助残APP\n竞品共性短板：无自主研发算法、功能与痛点脱节、仅覆盖单一人群", {
    x: 0.9, y: 5.65, w: 11.5, h: 0.65,
    fontSize: 13, fontFace: FONT, color: WHITE,
    align: "center", valign: "middle", lineSpacingMultiple: 1.3
  });

  addPageNum(slide, 7, TOTAL);
  helpers.warnIfSlideHasOverlaps(slide, pptx);
  helpers.warnIfSlideElementsOutOfBounds(slide, pptx);
}

// ═══════════════════════════════════════════════════
// 第8页：无障碍原生设计（20秒）
// ═══════════════════════════════════════════════════
{
  const slide = pptx.addSlide();
  slide.background = { color: DEEP_NAVY };

  addGlowCircle(slide, 10, -1, 5, ELECTRIC_BLUE, 90);
  addGlowCircle(slide, -2, 5, 4, NEON_CYAN, 92);

  slide.addText("第8页 · 残疾人也能用的助残APP", {
    x: 0.6, y: 0.3, w: 12, h: 0.6,
    fontSize: 24, fontFace: FONT, color: WHITE, bold: true
  });
  slide.addText("响应十五五「数字无障碍」要求——自主研发算法保障无障碍体验", {
    x: 0.6, y: 0.85, w: 12, h: 0.35,
    fontSize: 13, fontFace: FONT, color: NEON_CYAN
  });
  addAccentBar(slide, 1.15, 12, NEON_CYAN);

  const designItems = [
    { title: "全局语音交互", desc: "进入即TTS播报，语速/音调可调，自主研发语音引擎", icon: "🎙️", ac: ELECTRIC_BLUE },
    { title: "大字高对比", desc: "16sp+大字，48dp+按钮，独立于颜色传递信息", icon: "🔍", ac: TEAL },
    { title: "色盲友好设计", desc: "高对比度色彩，关键信息不依赖颜色区分", icon: "🎨", ac: AMBER },
    { title: "精细震动反馈", desc: "自主研发20种震动编码，公交到站+应急SOS+触觉闭环", icon: "📳", ac: WARN_RED }
  ];

  designItems.forEach((d, i) => {
    const col = i % 2, row = Math.floor(i / 2);
    const xPos = 0.6 + col * 6.2, yPos = 1.4 + row * 2.6;
    const cw = 5.9, ch = 2.2;

    slide.addShape(pptx.ShapeType.roundRect, {
      x: xPos, y: yPos, w: cw, h: ch,
      fill: { color: DARK_NAVY }, rectRadius: 0.15,
      line: { color: d.ac, width: 1.2 },
      shadow: { type: "outer", blur: 8, offset: 2, color: "000000", opacity: 0.25 }
    });

    slide.addShape(pptx.ShapeType.ellipse, {
      x: xPos + 0.3, y: yPos + 0.3, w: 0.7, h: 0.7,
      fill: { color: d.ac, transparency: 75 }
    });
    slide.addText(d.icon, {
      x: xPos + 0.3, y: yPos + 0.3, w: 0.7, h: 0.7,
      fontSize: 22, align: "center", valign: "middle"
    });

    slide.addText(d.title, {
      x: xPos + 1.15, y: yPos + 0.3, w: cw - 1.45, h: 0.5,
      fontSize: 18, fontFace: FONT, color: WHITE, bold: true,
      valign: "middle"
    });
    slide.addText(d.desc, {
      x: xPos + 1.15, y: yPos + 0.9, w: cw - 1.45, h: 1.0,
      fontSize: 13, fontFace: FONT, color: ICE_BLUE,
      valign: "top"
    });
  });

  addPageNum(slide, 8, TOTAL);
  helpers.warnIfSlideHasOverlaps(slide, pptx);
  helpers.warnIfSlideElementsOutOfBounds(slide, pptx);
}

// ═══════════════════════════════════════════════════
// 第9页：核心价值+结尾（45秒）
// ═══════════════════════════════════════════════════
{
  const slide = pptx.addSlide();
  slide.background = { color: DEEP_NAVY };

  addGlowCircle(slide, -0.5, -0.5, 5, ELECTRIC_BLUE, 88);
  addGlowCircle(slide, 8, 4, 6, NEON_CYAN, 92);
  addGlowCircle(slide, 5, -2, 3.5, ELECTRIC_BLUE, 90);

  slide.addText("微光汇聚   畅行无碍", {
    x: 0.8, y: 1.5, w: 11.7, h: 1.2,
    fontSize: 44, fontFace: FONT, color: WHITE, bold: true,
    align: "center"
  });

  slide.addShape(pptx.ShapeType.rect, {
    x: 5.0, y: 2.8, w: 3.3, h: 0.04, fill: { color: NEON_CYAN }
  });

  slide.addShape(pptx.ShapeType.roundRect, {
    x: 1.5, y: 3.2, w: 10.3, h: 1.5,
    fill: { color: DARK_NAVY }, rectRadius: 0.15,
    line: { color: ELECTRIC_BLUE, width: 1.5 }
  });
  slide.addText([
    { text: "响应十五五「科技助残」号召\n", options: { fontSize: 18, fontFace: FONT, color: WHITE, bold: true } },
    { text: "用自主研发算法消除出行障碍\n让每一位残疾人都能独立、自信地走出家门", options: { fontSize: 15, fontFace: FONT, color: ICE_BLUE } }
  ], {
    x: 1.8, y: 3.3, w: 9.7, h: 1.3,
    align: "center", valign: "middle", lineSpacingMultiple: 1.4
  });

  slide.addText("微光畅行助力国家十五五残疾人事业目标实现：", {
    x: 1.5, y: 5.0, w: 10.3, h: 0.4,
    fontSize: 14, fontFace: FONT, color: "7BB3E0",
    align: "center"
  });
  slide.addText("从「项目化推进」迈向「制度化保障」——让每一位残疾人都能平等享受互联网服务", {
    x: 1.5, y: 5.4, w: 10.3, h: 0.4,
    fontSize: 13, fontFace: FONT, color: ICE_BLUE,
    align: "center"
  });

  slide.addShape(pptx.ShapeType.rect, {
    x: 4.8, y: 6.0, w: 3.7, h: 0.03, fill: { color: STEEL_BLUE }
  });

  slide.addText("谢谢 · 欢迎体验和投资", {
    x: 0.8, y: 6.2, w: 11.7, h: 0.7,
    fontSize: 22, fontFace: FONT, color: WHITE,
    align: "center", valign: "middle"
  });

  addPageNum(slide, 9, TOTAL);
  helpers.warnIfSlideHasOverlaps(slide, pptx);
  helpers.warnIfSlideElementsOutOfBounds(slide, pptx);
}

// ═══════════════════════════════════════════════════
// 保存
// ═══════════════════════════════════════════════════
pptx.writeFile({ fileName: "f:\\java\\weiguangplus\\ppt-workspace\\weiguangplus_roadshow.pptx" })
  .then(() => { console.log("PPT 生成成功！严格遵循终极提示词！"); })
  .catch((err) => { console.error("PPT 生成失败:", err); });