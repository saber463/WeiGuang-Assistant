# 🟣 算法Agent 工作规范

> **分支**: `feature/algorithm`
> **技术栈**: Python + PaddleOCR/Tesseract + ML模型
> **负责人**: [待分配]
> **状态**: 🚀 开发中
> **最后更新**: 2026-05-28

---

## 📋 角色定位

你是**微光同行项目的AI算法工程师**，负责构建项目的"大脑"——智能识别与决策引擎。你的核心产出包括：

- **药品OCR识别引擎**：从药盒照片中准确提取药品名称
- **过敏原匹配算法**：基于用户过敏史自动检测风险
- **用药禁忌校验系统**：多维度安全用药知识图谱
- **人声检测模型**：环境音聆听的核心算法

你的算法质量直接决定APP的**准确性**和**安全性**，关系到用户的生命健康！

---

## 🎯 Sprint 1 核心任务（第1-2周）

### 优先级：P0 - 必须完成（阻塞前后端集成）

#### 任务1.1：药品OCR识别引擎开发 ⏱️ 预计：2天

**目标**：实现高精度的中英文药品名称识别系统

##### 1.1.1 OCR方案选型评估

| 方案 | 中文准确率 | 离线支持 | 模型大小 | 推理速度 | 推荐度 |
|------|-----------|---------|---------|---------|--------|
| **PaddleOCR (推荐)** ⭐ | **95%+** | ✅ 支持 | ~10MB | <200ms | ⭐⭐⭐⭐⭐ |
| Tesseract | 75% | ✅ 支持 | ~15MB | <150ms | ⭐⭐⭐ |
| EasyOCR | 90% | ✅ 支持 | ~80MB | >500ms | ⭐⭐⭐⭐ |
| 百度OCR API | 98% | ❌ 需联网 | N/A | <300ms | ⭐⭐⭐ |
| 腾讯OCR API | 97% | ❌ 需联网 | N/A | <250ms | ⭐⭐⭐ |

**最终选择：PaddleOCR**
- ✅ 中文场景效果最佳
- ✅ 完全离线运行（适合无障碍用户可能无网络场景）
- ✅ 模型体积小，可嵌入Android APP
- ✅ 开源免费，社区活跃

##### 1.1.2 PaddleOCR部署与优化

**项目结构**：
```
weiguangplus-algorithm/
├── algorithms/
│   ├── __init__.py
│   ├── ocr/
│   │   ├── __init__.py
│   │   ├── paddle_ocr_engine.py      # PaddleOCR封装类
│   │   ├── image_preprocessor.py     # 图像预处理
│   │   ├── text_postprocessor.py     # 文本后处理
│   │   └── ocr_pipeline.py           # 完整OCR流水线
│   │
│   ├── drug_matching/
│   │   ├── __init__.py
│   │   ├── name_normalizer.py        # 药品名称标准化
│   │   ├── fuzzy_matcher.py          # 模糊匹配算法
│   │   └── synonym_dictionary.py     # 同义词词典
│   │
│   ├── allergen/
│   │   ├── __init__.py
│   │   ├── allergen_detector.py      # 过敏原检测引擎
│   │   ├── rule_engine.py            # 规则匹配引擎
│   │   └── knowledge_graph.py        # 过敏原知识图谱
│   │
│   └── drug_interaction/
│       ├── __init__.py
│       ├── interaction_checker.py    # 药物相互作用检查
│       └── risk_calculator.py        # 风险等级计算器
│
├── models/
│   ├── ch_ppocr_mobile_v2.0/inference.pdmodel    # PaddleOCR模型文件
│   └── human_voice_detector.tflite               # 人声检测模型
│
├── data/
│   ├── drug_master.csv              # 药品主数据（从CSV导入）
│   ├── drug_alias.csv               # 别名映射表
│   ├── allergen_rules.json          # 过敏原规则库
│   └── interaction_rules.json       # 相互作用规则库
│
├── tests/
│   ├── test_ocr_accuracy.py         # OCR准确率测试
│   ├── test_drug_matching.py        # 匹配算法测试
│   └── test_allergen_check.py       # 过敏原检查测试
│
├── requirements.txt
├── setup.py                         # pip install -e . 可安装
└── README.md                        # 本文件
```

**核心代码 - PaddleOCREngine**:
```python
"""
PaddleOCR 引擎封装类
功能：
- 加载预训练模型
- 图像预处理（去噪、增强、透视校正）
- 文本检测与识别
- 结果后处理（置信度过滤、排序）
"""

import cv2
import numpy as np
from paddleocr import PaddleOCR
from typing import List, Dict, Tuple, Optional
import logging
from pathlib import Path

logger = logging.getLogger(__name__)


class PaddleOCREngine:
    """
    PaddleOCR 高级封装
    
    Attributes:
        ocr: PaddleOCR实例
        use_gpu: 是否使用GPU加速
        lang: 语言设置（'ch'中文, 'en'英文）
        det_db_thresh: 文本检测阈值（0-1）
        rec_batch_num: 批量识别数量
    """
    
    def __init__(
        self,
        use_gpu: bool = False,
        lang: str = 'ch',
        det_db_thresh: float = 0.3,
        rec_batch_num: int = 6,
        model_dir: Optional[str] = None
    ):
        """
        初始化PaddleOCR引擎
        
        Args:
            use_gpu: 是否使用GPU（默认False，兼容性更好）
            lang: 识别语言（默认中文）
            det_db_thresh: 文本检测置信度阈值（越高越严格）
            rec_batch_num: 批量处理文本行数
            model_dir: 自定义模型路径（可选）
        """
        self.use_gpu = use_gpu
        self.lang = lang
        
        logger.info(f"初始化PaddleOCR引擎... GPU={use_gpu}, Lang={lang}")
        
        try:
            self.ocr = PaddleOCR(
                use_angle_cls=True,                    # 启用文字方向分类
                lang=lang,                             # 中文模式
                use_gpu=use_gpu,
                det_db_thresh=det_db_thresh,            # 检测阈值
                det_db_box_thresh=0.5,                 # 边框阈值
                rec_batch_num=rec_batch_num,           # 批量识别
                show_log=False,                        # 关闭日志输出
                det_model_dir=model_dir,               # 自定义模型路径（如指定）
                rec_model_dir=model_dir,
                cls_model_dir=model_dir
            )
            
            logger.info("✓ PaddleOCR引擎初始化成功")
            
        except Exception as e:
            logger.error(f"✗ PaddleOCR初始化失败: {e}")
            raise
            
    def recognize_from_image(
        self,
        image_path: str,
        preprocess: bool = True
    ) -> Dict:
        """
        从图片文件执行OCR识别
        
        Args:
            image_path: 图片文件路径（支持jpg/png/bmp）
            preprocess: 是否进行图像预处理（默认True）
        
        Returns:
            {
                'success': bool,
                'raw_text': str,                  # 原始识别文本
                'confidence': float,             # 平均置信度 (0-1)
                'text_blocks': [                 # 文本块列表
                    {
                        'text': str,
                        'confidence': float,
                        'bbox': [[x1,y1], [x2,y2], ...],  # 四角坐标
                        'position': str          # 位置描述（上/中/下/左/右）
                    }
                ],
                'processing_time_ms': float       # 处理耗时（毫秒）
            }
        """
        import time
        start_time = time.time()
        
        try:
            # 读取图片
            if preprocess:
                image = self._preprocess_image(image_path)
            else:
                image = cv2.imread(image_path)
                
            if image is None:
                raise ValueError(f"无法读取图片: {image_path}")
            
            # 执行OCR识别
            result = self.ocr.ocr(image, cls=True)
            
            # 解析结果
            text_blocks = []
            all_confidences = []
            raw_text_lines = []
            
            if result and result[0]:
                for line in result[0]:
                    bbox = line[0]                          # 四角坐标 [[x1,y1],[x2,y2]...]
                    (text, confidence) = line[1]           # (文本内容, 置信度)
                    
                    # 计算文本在图片中的位置
                    position = self._calculate_position(bbox, image.shape)
                    
                    text_blocks.append({
                        'text': text.strip(),
                        'confidence': round(confidence, 4),
                        'bbox': [[int(p[0]), int(p[1])] for p in bbox],
                        'position': position
                    })
                    
                    all_confidences.append(confidence)
                    raw_text_lines.append(text.strip())
            
            # 计算平均置信度
            avg_confidence = (
                np.mean(all_confidences) if all_confidences else 0.0
            )
            
            processing_time = (time.time() - start_time) * 1000
            
            return {
                'success': True,
                'raw_text': '\n'.join(raw_text_lines),
                'confidence': round(avg_confidence, 4),
                'text_blocks': text_blocks,
                'processing_time_ms': round(processing_time, 2)
            }
            
        except Exception as e:
            logger.error(f"OCR识别异常: {e}")
            return {
                'success': False,
                'raw_text': '',
                'confidence': 0.0,
                'text_blocks': [],
                'processing_time_ms': (time.time() - start_time) * 1000,
                'error': str(e)
            }
    
    def _preprocess_image(self, image_path: str) -> np.ndarray:
        """
        图像预处理（提升OCR准确率关键步骤！）
        
        处理流程：
        1. 读取原始图像
        2. 转换为灰度图
        3. 高斯模糊去噪
        4. 自适应直方图均衡化（增强对比度）
        5. 二值化处理（可选）
        6. 透视矫正（如果检测到倾斜）
        """
        import time
        pre_start = time.time()
        
        image = cv2.imread(image_path)
        if image is None:
            raise ValueError(f"无法加载图像: {image_path}")
        
        # Step 1: 缩放（如果图片太大，先缩小以提速）
        max_dim = 2048
        h, w = image.shape[:2]
        if max(h, w) > max_dim:
            scale = max_dim / max(h, w)
            image = cv2.resize(image, (int(w * scale), int(h * scale)))
        
        # Step 2: 转灰度
        gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
        
        # Step 3: 高斯模糊去噪（去除细小噪点）
        blurred = cv2.GaussianBlur(gray, (3, 3), 0)
        
        # Step 4: CLAHE自适应对比度增强（关键步骤！）
        clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8))
        enhanced = clahe.apply(blurred)
        
        # Step 5: 锐化（让文字边缘更清晰）
        kernel = np.array([[-1,-1,-1], [-1,9,-1], [-1,-1,-1]])
        sharpened = cv2.filter2D(enhanced, -1, kernel)
        
        # Step 6: 转回BGR格式（PaddleOCR需要BGR输入）
        result = cv2.cvtColor(sharpened, cv2.COLOR_GRAY2BGR)
        
        pre_time = (time.time() - pre_start) * 1000
        logger.debug(f"图像预处理耗时: {pre_time:.2f}ms")
        
        return result
    
    def _calculate_position(
        self, 
        bbox: List[List[float]], 
        image_shape: tuple
    ) -> str:
        """
        根据文本框坐标计算其在图片中的大致位置
        
        Args:
            bbox: 四角坐标 [[x1,y1],[x2,y2]...]
            image_shape: 图片尺寸 (height, width, channels)
        
        Returns:
            位置描述字符串: '顶部'/'中部'/'底部'/'左侧'/'右侧'
        """
        height, width = image_shape[:2]
        
        # 计算中心点坐标
        center_x = np.mean([p[0] for p in bbox])
        center_y = np.mean([p[1] for p in bbox])
        
        # 判断垂直位置
        y_ratio = center_y / height
        if y_ratio < 0.33:
            v_pos = "顶部"
        elif y_ratio < 0.67:
            v_pos = "中部"
        else:
            v_pos = "底部"
        
        # 判断水平位置
        x_ratio = center_x / width
        if x_ratio < 0.33:
            h_pos = "左侧"
        elif x_ratio < 0.67:
            h_pos = "中央"
        else:
            h_pos = "右侧"
            
        return f"{v_pos}{h_pos}"
    
    def extract_drug_name(self, ocr_result: Dict) -> Dict:
        """
        从OCR结果中提取可能的药品名称
        
        策略：
        1. 优先选择置信度高(>0.9)且位于图片中上部或中部的文本
        2. 过滤掉明显不是药品名的短文本（<2字符）和过长文本（>20字符）
        3. 使用正则表达式匹配常见药品名模式
        4. 返回最可能的候选列表
        
        Args:
            ocr_result: recognize_from_image() 的返回结果
        
        Returns:
            {
                'candidates': [
                    {'name': str, 'confidence': float}
                ],
                'best_match': str or None,
                'extraction_method': str  # 'regex'/'position'/'heuristic'
            }
        """
        if not ocr_result['success'] or not ocr_result['text_blocks']:
            return {
                'candidates': [],
                'best_match': None,
                'extraction_method': 'none'
            }
        
        text_blocks = ocr_result['text_blocks']
        candidates = []
        
        # 正则表达式匹配常见药品名模式
        drug_patterns = [
            r'^[\u4e00-\u9fa5]{2,10}(片|胶囊|颗粒|口服液|注射液|滴丸|分散片)$',  # 中文名+剂型
            r'^[A-Z][a-z]+(?:\s+[A-Z][a-z]+)*$',  # 英文通用名（如Ibuprofen）
            r'^[\u4e00-\u9fa5]{2,15}$',  # 纯中文名（2-15字）
        ]
        
        for block in text_blocks:
            text = block['text']
            conf = block['confidence']
            
            # 过滤条件
            if len(text) < 2 or len(text) > 25:
                continue
                
            if conf < 0.5:  # 低置信度直接跳过
                continue
            
            # 位置权重（中上部更可能是药品名）
            position_weight = 1.0
            if block['position'] in ['顶部中央', '中部中央']:
                position_weight = 1.2
            elif '底部' in block['position']:
                position_weight = 0.8  # 底部通常是说明书、用法等
            
            # 正则匹配得分
            regex_score = 0.0
            for pattern in drug_patterns:
                if re.match(pattern, text):
                    regex_score = 1.0
                    break
            
            # 综合评分
            final_score = conf * position_weight * (0.6 + 0.4 * regex_score)
            
            candidates.append({
                'name': text,
                'confidence': round(final_score, 4),
                'original_confidence': conf,
                'position': block['position'],
                'is_regex_matched': regex_score > 0
            })
        
        # 按综合评分排序
        candidates.sort(key=lambda x: x['confidence'], reverse=True)
        
        best_match = candidates[0]['name'] if candidates else None
        
        extraction_method = 'regex' if (
            candidates and candidates[0].get('is_regex_matched')
        ) else ('position' if candidates else 'none')
        
        return {
            'candidates': candidates[:5],  # 返回Top 5候选
            'best_match': best_match,
            'extraction_method': extraction_method
        }


# 使用示例
if __name__ == "__main__":
    # 初始化OCR引擎
    ocr_engine = PaddleOCREngine(use_gpu=False, lang='ch')
    
    # 识别药盒图片
    result = ocr_engine.recognize_from_image(
        image_path="test_images/drug_photo.jpg",
        preprocess=True
    )
    
    print(f"识别成功: {result['success']}")
    print(f"平均置信度: {result['confidence']}")
    print(f"原始文本:\n{result['raw_text']}")
    print(f"处理耗时: {result['processing_time_ms']}ms")
    
    # 提取药品名称
    drug_info = ocr_engine.extract_drug_name(result)
    print(f"\n最佳匹配: {drug_info['best_match']}")
    print(f"候选列表:")
    for cand in drug_info['candidates']:
        print(f"  - {cand['name']} (置信度:{cand['confidence']})")
```

##### 1.1.3 药品名称标准化模块

```python
"""
药品名称标准化器
功能：
- 商品名 ↔ 通用名 映射
- 模糊匹配（编辑距离 + Jaccard相似度）
- 同义词扩展
- 品牌词典维护
"""

import re
from typing import List, Dict, Tuple, Optional
from rapidfuzz import fuzz  # 高性能模糊匹配库
import pandas as pd
import json


class DrugNameNormalizer:
    """
    药品名称标准化处理器
    
    将OCR识别出的非标准名称转换为数据库中的标准名称
    """
    
    def __init__(self, alias_file: str = 'data/drug_alias.csv'):
        """
        初始化标准化器，加载别名映射表
        
        Args:
            alias_file: CSV格式的别名映射文件
                格式: standard_name,alias1,alias2,...
        """
        self.alias_dict: Dict[str, str] = {}  # alias -> standard
        self.standard_names: set = set()
        
        # 加载别名数据
        try:
            df = pd.read_csv(alias_file, encoding='utf-8')
            for _, row in df.iterrows():
                standard = row['standard_name'].strip()
                self.standard_names.add(standard)
                
                # 遍历所有别名
                for col in df.columns[1:]:
                    alias = str(row[col]).strip()
                    if alias and alias != 'nan':
                        self.alias_dict[alias.lower()] = standard
                        
            logger.info(f"✓ 已加载{len(self.alias_dict)}条别名映射规则")
            
        except FileNotFoundError:
            logger.warning(f"⚠ 别名文件未找到: {alias_file}，使用空词典")
    
    def normalize(self, raw_name: str) -> Tuple[str, float]:
        """
        标准化药品名称
        
        Args:
            raw_name: OCR识别的原始名称
        
        Returns:
            (standard_name, confidence) 元组
            - standard_name: 标准名称（如果找不到则返回原文）
            - confidence: 匹配置信度 (0-1)
        """
        if not raw_name:
            return ("", 0.0)
        
        raw_lower = raw_name.strip().lower()
        
        # 1. 精确匹配（别名词典）
        if raw_lower in self.alias_dict:
            standard = self.alias_dict[raw_lower]
            return (standard, 1.0)
        
        # 2. 精确匹配标准名
        if raw_lower in {n.lower() for n in self.standard_names}:
            # 找到完全一致的标准名
            for std in self.standard_names:
                if std.lower() == raw_lower:
                    return (std, 1.0)
        
        # 3. 模糊匹配（使用Levenshtein距离）
        best_match = None
        best_score = 0.0
        
        # 先在标准名中搜索
        for std in self.standard_names:
            score = fuzz.ratio(raw_lower, std.lower()) / 100.0
            if score > best_score:
                best_score = score
                best_match = std
        
        # 再在别名中搜索（可能OCR识别出的是别名）
        for alias, std in self.alias_dict.items():
            score = fuzz.ratio(raw_lower, alias) / 100.0
            if score > best_score:
                best_score = score
                best_match = std
        
        # 设置阈值
        threshold = 0.85  # 85%以上才认为是有效匹配
        
        if best_score >= threshold and best_match:
            return (best_match, round(best_score, 4))
        else:
            # 无法匹配，返回原文
            return (raw_name.strip(), round(best_score, 4))
    
    def batch_normalize(self, names: List[str]) -> List[Tuple[str, float]]:
        """批量标准化多个名称"""
        return [self.normalize(name) for name in names]


# 使用示例
normalizer = DrugNameNormalizer('data/drug_alias.csv')

# 测试各种情况
test_cases = [
    "泰诺",           # 商品名 -> 对乙酰氨基酚片
    "扑热息痛",       # 别名 -> 对乙酰氨基酚片
    "阿莫西林胶囊",   # 已是标准名
    "Amoxicillin",    # 英文名 -> 阿莫西林
    "布洛芬缓释胶囊", # 标准名
    "布洛分",         # OCR错别字 -> 布洛芬
]

for name in test_cases:
    standard, conf = normalizer.normalize(name)
    print(f"'{name}' -> '{standard}' (置信度: {conf})")
```

**验收标准**：
```bash
# OCR准确率测试
python tests/test_ocr_accuracy.py

# 预期结果：
# 总测试图片数: 200张（包含各种角度、光照、模糊程度）
# 准确率指标:
#   - Top-1准确率: >= 85%
#   - Top-3准确率: >= 95%
#   - 平均推理时间: < 200ms (CPU) / < 50ms (GPU)

# 药品名称提取测试
python tests/test_drug_extraction.py

# 预期结果：
#   - 成功提取药品名: >= 90%
#   - 标准化成功率: >= 88%（能正确映射到数据库中的标准名）

# 性能基准测试
python benchmark/performance_test.py

# 预期结果：
#   - 单张图片端到端延迟: < 500ms (含预处理+OCR+后处理)
#   - 内存占用: < 500MB
#   - CPU利用率: < 80%
```

---

#### 任务1.2：过敏原匹配算法 ⏱️ 预计：1天

**目标**：构建精准的过敏原检测与风险提示系统

##### 1.2.1 过敏原知识图谱

**数据结构设计** (`data/allergen_rules.json`):
```json
{
  "allergen_categories": [
    {
      "id": "penicillin",
      "name": "青霉素类抗生素",
      "description": "包括青霉素、氨苄西林、阿莫西林等",
      "severity": "CRITICAL",
      "symptoms": ["皮疹", "呼吸困难", "过敏性休克"],
      "related_drugs": [
        {"name": "青霉素V钾片", "ingredient": "青霉素"},
        {"name": "阿莫西林胶囊", "ingredient": "阿莫西林"},
        {"name": "氨苄西林钠注射液", "ingredient": "氨苄西林"}
      ],
      "cross_reactivity": ["头孢菌素类（部分交叉）"]
    },
    {
      "id": "sulfonamide",
      "name": "磺胺类药物",
      "severity": "HIGH",
      "symptoms": ["皮疹", "发热", "肝肾功能损害"],
      "related_drugs": [...]
    },
    {
      "id": "iodine",
      "name": "碘造影剂",
      "severity": "MEDIUM",
      "symptoms": ["皮疹", "恶心呕吐"],
      "related_drugs": [...]
    }
  ],
  
  "matching_rules": [
    {
      "rule_id": "exact_ingredient_match",
      "description": "药品成分与用户过敏原精确匹配",
      "condition": "user_allergens ∩ drug_ingredients ≠ ∅",
      "risk_level": "CRITICAL",
      "action": "BLOCK_USAGE"
    },
    {
      "rule_id": "category_risk",
      "description": "同类药物风险提示",
      "condition": "drug_category ∈ high_risk_categories_for_user",
      "risk_level": "HIGH",
      "action": "WARN_AND_CONFIRM"
    },
    {
      "rule_id": "cross_reactivity",
      "description": "交叉过敏风险",
      "condition": "drug_has_cross_reactive_with_user_allergens",
      "risk_level": "MEDIUM",
      "action": "INFORM_USER"
    }
  ]
}
```

##### 1.2.2 过敏原检测引擎代码

```python
"""
过敏原检测引擎
功能：
- 加载过敏原规则库
- 多维匹配（成分/类别/交叉反应）
- 风险等级评定
- 自然语言风险提示生成
"""

from typing import List, Dict, Any
from dataclasses import dataclass
from enum import Enum


class RiskLevel(Enum):
    """风险等级枚举"""
    LOW = "LOW"           # 绿色 - 安全
    MEDIUM = "MEDIUM"     # 黄色 - 注意
    HIGH = "HIGH"         # 橙色 - 警告
    CRITICAL = "CRITICAL" # 红色 - 危险/禁止使用


@dataclass
class AllergenMatch:
    """过敏原匹配结果"""
    allergen_name: str
    matched_ingredient: str
    severity: RiskLevel
    description: str
    recommendation: str
    evidence_source: str  # 数据来源（临床研究/病例报告/理论推测）


@dataclass
class AllergenCheckResult:
    """完整检查结果"""
    overall_risk: RiskLevel
    matches: List[AllergenMatch]
    safe_alternatives: List[str]  # 安全替代药品建议
    summary_text: str             # 用户友好的总结文案
    should_block_usage: bool      # 是否应该禁止使用


class AllergenDetector:
    """
    过敏原检测主引擎
    
    使用方法：
    ```python
    detector = AllergenDetector('data/allergen_rules.json')
    
    user_profile = {
        'allergy_history': ['青霉素', '磺胺类药物'],
        'chronic_diseases': ['高血压', '糖尿病']
    }
    
    drug_info = {
        'ingredients': [{'name': '阿莫西林', 'amount': '0.5g'}],
        'category': '抗生素'
    }
    
    result = detector.check(drug_info, user_profile)
    print(result.summary_text)
    ```
    """
    
    def __init__(self, rules_file: str):
        """加载过敏原规则库"""
        with open(rules_file, 'r', encoding='utf-8') as f:
            self.rules_data = json.load(f)
        
        self.allergen_categories = rules_data['allergen_categories']
        self.matching_rules = rules_data['matching_rules']
        
        # 构建快速查找索引
        self.allergen_to_category = {}
        for cat in self.allergen_categories:
            for drug in cat.get('related_drugs', []):
                self.allergen_to_category[drug['name'].lower()] = cat
        
        logger.info(f"✓ 过敏原检测引擎初始化完成，已加载{len(self.allergen_categories)}个类别")
    
    def check(
        self, 
        drug_info: Dict[str, Any],
        user_profile: Dict[str, Any]
    ) -> AllergenCheckResult:
        """
        执行过敏原检查
        
        Args:
            drug_info: 药品信息字典
                - ingredients: [{name, amount}] 成分列表
                - category: 药品分类
                - brand_name: 商品名
            user_profile: 用户档案
                - allergy_history: [] 过敏史列表
                - chronic_diseases: [] 慢性病列表
                - current_medications: [] 当前用药列表
        
        Returns:
            AllergenCheckResult 完整检查结果
        """
        matches = []
        user_allergens = [a.lower() for a in user_profile.get('allergy_history', [])]
        
        # Rule 1: 精确成分匹配
        for ingredient in drug_info.get('ingredients', []):
            ingred_name = ingredient.get('name', '').lower().strip()
            
            # 直接匹配过敏原名称
            if ingred_name in user_allergens or any(
                allergen in ingred_name for allergen in user_allergens
            ):
                # 找到对应的过敏原类别
                category = self.allergen_to_category.get(ingred_name)
                if category:
                    matches.append(AllergenMatch(
                        allergen_name=category['name'],
                        matched_ingredient=ingredient['name'],
                        severity=RiskLevel.CRITICAL,
                        description=f"该药品含有{category['name']}成分，您有相关过敏史",
                        recommendation="❌ 禁止使用！请立即咨询医生或药师",
                        evidence_source="用户过敏史记录"
                    ))
        
        # Rule 2: 类别风险匹配（同类药物）
        drug_category = drug_info.get('category', '')
        high_risk_categories = self._get_high_risk_categories(user_allergens)
        
        if drug_category in high_risk_categories:
            matches.append(AllergenMatch(
                allergen_name=f"{drug_category}类药物",
                matched_ingredient="药品分类",
                severity=RiskLevel.HIGH,
                description=f"该药品属于{drug_category}类别，与您的过敏原存在关联风险",
                recommendation="⚠️ 使用前务必咨询医生，密切观察身体反应",
                evidence_source="药物分类学关联"
            ))
        
        # Rule 3: 交叉反应检测
        cross_matches = self._check_cross_reactivity(drug_info, user_allergens)
        matches.extend(cross_matches)
        
        # 计算综合风险等级
        overall_risk = self._calculate_overall_risk(matches)
        
        # 生成自然语言总结
        summary = self._generate_summary(matches, overall_risk)
        
        # 查找安全替代品
        safe_alts = self._find_safe_alternatives(drug_info, user_allergens)
        
        should_block = any(m.severity == RiskLevel.CRITICAL for m in matches)
        
        return AllergenCheckResult(
            overall_risk=overall_risk,
            matches=matches,
            safe_alternatives=safe_alts,
            summary_text=summary,
            should_block_usage=should_block
        )
    
    def _calculate_overall_risk(self, matches: List[AllergenMatch]) -> RiskLevel:
        """根据匹配结果计算最高风险等级"""
        if not matches:
            return RiskLevel.LOW
        
        severities = [m.severity for m in matches]
        
        if RiskLevel.CRITICAL in severities:
            return RiskLevel.CRITICAL
        elif RiskLevel.HIGH in severities:
            return RiskLevel.HIGH
        elif RiskLevel.MEDIUM in severities:
            return RiskLevel.MEDIUM
        else:
            return RiskLevel.LOW
    
    def _generate_summary(self, matches: List[AllergenMatch], risk: RiskLevel) -> str:
        """生成用户友好的中文总结"""
        if not matches:
            return "✅ 经检测，该药品对您相对安全，但仍建议咨询医生。"
        
        risk_emoji = {
            RiskLevel.LOW: "🟢",
            RiskLevel.MEDIUM: "🟡",
            RiskLevel.HIGH: "🟠",
            RiskLevel.CRITICAL: "🔴"
        }
        
        lines = [f"{risk_emoji[risk]} 发现{len(matches)}项潜在风险：\n"]
        
        for i, match in enumerate(matches[:3], 1):  # 最多显示3条
            lines.append(f"{i}. {match.description}")
            lines.append(f"   建议: {match.recommendation}\n")
        
        if len(matches) > 3:
            lines.append(f"... 共{len(matches)}项风险（详情请查看完整报告）")
        
        return '\n'.join(lines)


# 使用示例
detector = AllergenDetector('data/allergen_rules.json')

result = detector.check(
    drug_info={
        'ingredients': [{'name': '阿莫西林', 'amount': '0.5g'}],
        'category': '青霉素类抗生素'
    },
    user_profile={
        'allergy_history': ['青霉素', '磺胺类药物'],
        'chronic_diseases': ['高血压']
    }
)

print(result.overall_risk)        # CRITICAL
print(result.should_block_usage)  # True
print(result.summary_text)
# 🔴 发现1项潜在风险：
# 1. 该药品含有青霉素类抗生素成分，您有相关过敏史
#    建议: ❌ 禁止使用！请立即咨询医生或药师
```

**验收标准**：
```bash
# 过敏原检测单元测试
pytest tests/test_allergen_check.py -v

# 预期结果：
# 测试用例数: 30+
# 通过率: 100%
# 覆盖场景:
#   - 精确成分匹配: ✅
#   - 类别风险匹配: ✅
#   - 交叉反应检测: ✅
#   - 无过敏史安全返回: ✅
#   - 多重过敏原同时匹配: ✅
#   - 特殊人群规则（孕妇/儿童/老人）: ✅
```

---

### 优先级：P1 - Sprint 1 可选

#### 任务1.3：用药禁忌校验系统 ⏱️ 预计：1.5天

**功能**：
- [ ] 单药禁忌检查（年龄、性别、孕期、哺乳期、肝肾功能）
- [ ] 多药联用禁忌检查（药物相互作用矩阵）
- [ ] 食物-药物相互作用（西柚汁+他汀类、酒精+头孢类等）
- [ ] 疾病-药物禁忌（高血压+麻黄碱、糖尿病+噻嗪类利尿剂等）

#### 任务1.4：人声检测模型优化 ⏱️ 预计：1天

**当前状态**：项目已有TFLite基础模型  
**优化方向**：
- [ ] 提升识别准确率（当前基线待测量）
- [ ] 降低误报率（避免环境噪音误判为人声）
- [ ] 优化模型体积（目标<5MB）
- [ ] Android端部署测试

---

## 🔒 开发约束

### ✅ 允许的操作
1. 在 `feature/algorithm` 分支内自由开发算法
2. 编写Python包（`pip install -e .` 可安装）
3. 创建训练脚本和数据处理工具
4. 编写详尽的单元测试（目标覆盖率>=80%）
5. 导出ONNX/TFLite模型供Android端调用

### ❌ 禁止的操作
1. **禁止修改前端Kotlin/Java代码**
2. **禁止修改后端FastAPI业务逻辑**（只提供算法函数接口）
3. **禁止提交大型二进制模型文件到Git**（使用Git LFS或外部存储）
4. **禁止硬编码敏感路径或密钥**

### 🔄 与其他Agent协作协议

**向后端Agent提供算法接口**：
```
【算法接口 + feature/algorithm + 药品OCR识别API】

请求方: 算法Agent
接收方: 后端Agent
优先级: P0
接口定义:
  POST /internal/algorithms/drug-ocr
  Request: { "image_base64": "...", "options": {...} }
  Response: { 
    "drug_name": "xxx",
    "confidence": 0.92,
    "allergen_result": {...},
    "processing_time_ms": 350
  }

  POST /internal/algorithms/allergen-check
  Request: { "drug_ids": [1,2,3], "user_profile": {...} }
  Response: { "overall_risk": "HIGH", "matches": [...] }

依赖要求:
  - Python 3.9+
  - PaddlePaddle 2.5+
  - 模型文件需预先下载到 models/ 目录
截止时间: Sprint 1 结束前
```

---

## 📦 依赖清单

### requirements.txt
```txt
# OCR引擎
paddlepaddle==2.5.2
paddleocr==2.7.3
opencv-python-headless==4.8.1.78

# 模糊匹配
rapidfuzz==3.6.1
Levenshtein==0.21.1

# 数据处理
pandas==2.1.4
numpy==1.26.2

# 机器学习（人声检测）
tensorflow==2.14.0  # 或 tensorflow-macos for Apple Silicon
librosa==0.10.1

# 工具库
tqdm==4.66.1
loguru==0.7.2
pydantic==2.5.2

# 测试
pytest==7.4.4
pytest-cov==4.1.0
pytest-asyncio==0.21.1
```

---

## 🧪 测试要求

### 单元测试框架
```bash
# 安装测试依赖
pip install pytest pytest-cov

# 运行全部测试
pytest tests/ -v --cov=algorithms --cov-report=html --cov-fail-under=80

# 目标覆盖率 >= 80%
```

### 必须编写的测试用例

| 模块 | 最少用例数 | 关键覆盖点 |
|------|-----------|-----------|
| OCR引擎 | 20+ | 正常识别/模糊图片/低光/倾斜/多语言/边界案例 |
| 名称标准化 | 15+ | 精确匹配/模糊匹配/别名映射/英文名/错别字 |
| 过敏原检测 | 25+ | 精确匹配/类别匹配/交叉反应/多重过敏/特殊人群 |
| 用药禁忌 | 15+ | 年龄限制/性别差异/孕期/肝肾功/食物相互作用 |

### 性能基准测试
```bash
# 运行性能测试
python benchmark/stress_test.py

# 预期指标：
# - OCR单次推理: < 200ms (CPU) / < 50ms (GPU)
# - 并发100请求QPS: > 50 QPS
# - 内存峰值: < 512MB
# - CPU使用率: < 90%
```

---

## 📊 进度汇报模板

```markdown
## 算法开发进度报告

**模块**: XXX算法
**Agent**: 算法Agent
**分支**: feature/algorithm
**时间**: 2026-MM-DD HH:MM

### 当前进度
- [x] 已完成任务1（PaddleOCR部署+优化）
- [ ] 进行中任务2（过敏原匹配引擎，完成度70%）
- [ ] 待开始任务3（用药禁忌校验）

### 产出物
- 新增算法函数: N个
- 新增测试用例: XX个
- 代码行数: +XXXX / -YY
- 模型文件: X个（大小总计XXMB）

### 性能指标
- OCR准确率: XX%（Top-1）/ XX%（Top-3）
- 平均推理延迟: XXX ms
- 内存占用: XXX MB
- 测试覆盖率: ZZ%

### 风险与阻塞
- 风险1: PaddleOCR在某些模糊图片上准确率下降（应对：增加数据增强）
- 阻塞项: 待确认是否需要支持手写体识别

### 下一步计划
1. 完成过敏原匹配剩余30%
2. 开始用药禁忌校验模块
3. 编写完整的API接口文档供后端集成
```

---

*本文件由微光同行多Agent并行开发系统自动生成*
*遵循算法开发规范 v1.0 | 最后更新: 2026-05-28*
