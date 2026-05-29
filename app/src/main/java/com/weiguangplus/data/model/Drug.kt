/**
 * 文件名：Drug.kt
 * 作者：微光同行前端团队
 * 功能描述：药品信息数据类，对应后端药品实体和本地药品数据库表结构
 * 创建日期：2026-05-29
 * 所属模块：data/model（数据模型层）
 *
 * 数据来源：
 * 1. 后端API响应（药品识别接口、搜索接口、详情接口返回）
 * 2. Room本地离线数据库（预置103种常见药品数据，支持无网络使用）
 * 3. OCR识别结果提取（从药盒照片中识别出的关键信息）
 *
 * 字段说明（共22个核心属性）：
 *
 * 【基础信息 - 5个】
 * - id, genericName, tradeName, categoryName, approvalNo
 *
 * 【规格参数 - 4个】
 * - specification, manufacturer, dosageForm, storageCondition
 *
 * 【药理信息 - 5个】
 * - composition, indication, usageAndDosage, taboo, adverseReaction
 *
 * 【安全警示 - 4个】
 * - riskLevel, riskPrompts, warningLabel, contraindications
 *
 * 【扩展信息 - 4个】
 * - signKeywords, ttsSummary, imageUrl, sourceTag
 *
 * 使用场景：
 * - 药品识别结果展示页（核心展示组件）
 * - 药品搜索结果列表（卡片式布局）
 * - 用药安全风险提醒（颜色编码 + 振动反馈）
 * - TTS语音播报内容生成（无障碍访问支持）
 */

package com.weiguangplus.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * 药品实体数据类
 *
 * 使用Room @Entity注解标记为数据库表，
 * 同时兼容Gson JSON序列化和Room数据库持久化。
 *
 * 表名：drugs（Room自动映射）
 * 主键：id（Long类型，自增或后端分配的唯一标识）
 *
 * 设计特点：
 * 1. 字段全面覆盖药品说明书的核心内容
 * 2. 支持多源数据合并（在线API + 离线本地库 + OCR识别）
 * 3. 内置风险分级系统（high/medium/low三级）
 * 4. 预置手语关键词用于手语翻译功能
 */
@Entity(tableName = "drugs")
data class Drug(
    /**
     * 药品唯一标识符（主键）
     *
     * 由后端数据库分配的全局唯一ID。
     * 用于关联识别记录、用户收藏、反馈数据等。
     *
     * JSON字段名：id / drug_id
     */
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    val id: Long = 0,

    /**
     * 通用名（药品标准名称）
     *
     * 采用《中国药品通用名称命名原则》规定的法定名称。
     * 同一成分的药品无论哪个厂家生产，通用名都相同。
     *
     * 示例：
     * - "布洛芬"（Ibuprofen）
     * - "阿莫西林"（Amoxicillin）
     * - "对乙酰氨基酚"（Paracetamol/Acetaminophen）
     *
     * 重要提示：
     * - 这是药品的"身份证"，用于唯一标识某种药物
     * - 搜索和识别的主要匹配依据
     * - 处方开具时使用的标准名称
     *
     * JSON字段名：generic_name / name
     */
    @SerializedName("generic_name")
    val genericName: String? = null,

    /**
     * 商品名（品牌名/商标名）
     *
     * 药品生产企业为自己的产品注册的品牌名称。
     * 不同厂家的同一通用名药品可能有不同的商品名。
     *
     * 示例（通用名 → 商品名）：
     * - 布洛芬 → 芬必得、布洛芬缓释胶囊、美林
     * - 阿莫西林 → 阿莫仙、阿莫西林胶囊
     * - 对乙酰氨基酚 → 泰诺林、必理通、扑热息痛
     *
     * UI展示建议：
     * - 主要显示通用名（更权威准确）
     * - 商品名作为副标题或括号补充显示
     *
     * JSON字段名：trade_name / brand_name
     */
    @SerializedName("trade_name")
    val tradeName: String? = null,

    /**
     * 药品分类名称
     *
     * 根据药理学作用机制进行的分类。
     * 用于药品筛选和分类导航。
     *
     * 分类体系（一级分类）：
     * - "解热镇痛抗炎药"：布洛芬、对乙酰氨基酚等
     * - "抗生素"：阿莫西林、头孢菌素等
     * - "心血管系统用药"：硝苯地平、氨氯地平等
     * - "消化系统用药"：奥美拉唑、多潘立酮等
     * - "抗过敏药"：氯雷他定、西替利嗪等
     * - "降血糖药"：二甲双胍、格列美脲等
     * - "中药"：速效救心丸、复方丹参滴丸等
     *
     * JSON字段名：category_name / category
     */
    @SerializedName("category_name")
    val categoryName: String? = null,

    /**
     * 批准文号（国药准字）
     *
     * 国家药品监督管理局颁发的药品批准证明文件编号。
     * 是药品合法生产销售的唯一凭证，具有法律效力。
     *
     * 格式规则：
     * - 国药准字H/Z/S/J + 8位数字
     * - H：化学药品
     * - Z：中药
     * - S：生物制品
     * - J：进口药品分包装
     *
     * 示例：
     * - "国药准字H10900089"（芬必得布洛芬缓释胶囊）
     * - "国药准字H44021351"（阿莫仙阿莫西林胶囊）
     * - "国药准字H20023370"（格华止盐酸二甲双胍片）
     *
     * 业务价值：
     * - OCR识别的关键特征（药盒上必有此字段）
     * - 可通过批准文号在药监局网站查询真伪
     * - 精确匹配药品的可靠依据
     *
     * JSON字段名：approval_no / license_no
     */
    @SerializedName("approval_no")
    val approvalNo: String? = null,

    /**
     * 药品规格
     *
     * 描述药品的含量、剂量单位等信息。
     * 用于指导患者正确选择和使用药品。
     *
     * 格式示例：
     * - "0.3g*20粒/盒"（每粒0.3克，每盒20粒）
     * - "5ml:0.1g*10支/盒"（每支5ml含0.1克）
     * - "100mg*12片/板*2板/盒"
     *
     * UI展示位置：
     * - 药品详情页的基本信息区域
     * - 搜索结果卡片的副标题行
     *
     * JSON字段名：specification / spec
     */
    @SerializedName("specification")
    val specification: String? = null,

    /**
     * 生产企业名称
     *
     * 获得该药品生产许可的制药公司全称。
     * 用于质量追溯和责任认定。
     *
     * 示例：
     * - "中美天津史克制药有限公司"（芬必得）
     * - "香港澳美制药厂"（阿莫仙）
     * - "施维雅（天津）制药有限公司"（格华止）
     *
     * JSON字段名：manufacturer / producer
     */
    @SerializedName("manufacturer")
    val manufacturer: String? = null,

    /**
     * 剂型（Dosage Form）
     *
     * 药物加工成的最终形态，决定给药途径和使用方法。
     *
     * 常见剂型：
     * - "片剂"（Tablets）：口服固体剂型
     * - "胶囊"（Capsules）：外壳包裹的粉末或颗粒
     * - "注射液"（Injection）：静脉/肌肉注射用液体
     * - "口服液"（Oral Liquid）：可直接饮用的液体药剂
     * - "膏剂"（Ointment）：外用半固体制剂
     * - "滴眼剂"（Eye Drops）：眼部使用的液体药剂
     * - "气雾剂"（Aerosol）：吸入用的喷雾制剂
     *
     * 无障碍适配：
     * - 视障用户可通过TTS播报剂型了解用法
     * - 影响包装开启方式的语音提示
     *
     * JSON字段名：dosage_form / form
     */
    @SerializedName("dosage_form")
    val dosageForm: String? = null,

    /**
     * 贮藏条件/保存要求
     *
     * 药品在贮存过程中需要满足的环境条件。
     * 不当保存可能导致药品失效或产生毒副作用。
     *
     * 常见贮藏条件：
     * - "密封，在干燥处保存"
     * - "遮光，密封，在阴凉处保存（不超过20℃）"
     * - "2-8℃冷藏保存"
     * - "防冻，避免高温"
     *
     * 安全提示：
     * - 应在TTS播报中强调特殊存储要求
     * - 对需要冷藏的药品给出醒目警告
     *
     * JSON字段名：storage_condition / storage
     */
    @SerializedName("storage_condition")
    val storageCondition: String? = null,

    /**
     * 成分/组成（Composition）
     *
     * 药品的活性成分及含量描述。
     * 用于判断是否含有过敏原或与其他药物的相互作用。
     *
     * 格式示例：
     * - "本品主要成分为布洛芬，每粒含布洛芬0.3克。"
     * - "活性成份：盐酸二甲双胍。"
     * - "每片含对乙酰氨基酚0.5克。"
     *
     * 过敏检测用途：
     * - 用户声明过敏史后可自动筛查成分
     * - 高亮显示含有过敏原的药品
     *
     * JSON字段名：composition / ingredients
     */
    @SerializedName("composition")
    val composition: String? = null,

    /**
     * 适应症（Indication）
     *
     * 药品批准用于治疗或预防的疾病或症状列表。
     * 帮助用户判断该药品是否适用于当前症状。
     *
     * 示例：
     * - "用于缓解轻至中度疼痛如头痛、关节痛、偏头痛、牙痛..."
     * - "用于敏感菌所致的呼吸道感染、泌尿道感染..."
     * - "用于2型糖尿病控制血糖..."
     *
     * UI展示方式：
     * - 折叠面板形式（默认收起，点击展开）
     * - 关键词高亮显示（如"头痛"、"发热"等）
     *
     * JSON字段名：indication / indications
     */
    @SerializedName("indication")
    val indication: String? = null,

    /**
     * 用法用量（Usage and Dosage）
     *
     * 详细的使用方法和每次服用剂量说明。
     * 包括给药途径、服用时间、频次、剂量调整等信息。
     *
     * 内容结构：
     * - 口服/注射/外用等给药途径
     * - 每次/每日服用量
     * - 服用时间（饭前/饭后/睡前）
     * - 特殊人群用量调整（儿童/老人/肝肾功能不全者）
     * - 疗程时长
     *
     * 无障碍重要性：
     * - 这是TTS语音播报的核心内容之一
     * - 视障用户完全依赖语音获取用药指导
     * - 必须清晰准确，避免歧义
     *
     * JSON字段名：usage_and_dosage / dosage
     */
    @SerializedName("usage_and_dosage")
    val usageAndDosage: String? = null,

    /**
     * 禁忌（Contraindication / Taboo）
     *
     * 明确禁止使用该药品的情况列表。
     * 违反禁忌可能导致严重不良反应甚至危及生命。
     *
     * 常见禁忌类型：
     * - 对本品过敏者禁用
     * - 孕妇及哺乳期妇女禁用
     * - 严重肝肾功能不全者禁用
     * - 特定疾病状态禁用（如活动性消化道溃疡）
     * - 与某些药物联用时禁用
     *
     * UI交互设计：
     * - 使用红色背景高亮显示
     * - 配合警告图标（⚠️）
     * - 触发强提醒三联动（振动+闪光灯+声音）
     *
     * JSON字段名：taboo / contraindications
     */
    @SerializedName("taboo")
    val taboo: String? = null,

    /**
     * 不良反应（Adverse Reaction）
     *
     * 在正常用法用量下出现的与用药目的无关的有害反应。
     * 帮助患者在出现异常症状时判断是否为药物反应。
     *
     * 分类：
     * - 常见反应（发生率≥1%）：恶心、头晕、皮疹等
     * - 少见反应（发生率0.1%-1%）：
     * - 罕见反应（发生率<0.1%）：
     * - 严重反应（需立即停药并就医）：
     *
     * 用户教育意义：
     * - 降低因轻微不良反应导致的恐慌性停药
     * - 及时发现严重反应并就医
     *
     * JSON字段名：adverse_reaction / side_effects
     */
    @SerializedName("adverse_reaction")
    val adverseReaction: String? = null,

    /**
     * 风险等级（Risk Level）
     *
     * 基于多维度评估的综合风险指标。
     * 用于快速视觉识别和安全提醒强度控制。
     *
     * 分级标准：
     * - "high"（高风险 - 红色标签）：
     *   · 处方药/管制类药物
     *   · 有严重禁忌或不良反应
     *   · 需要医生指导下使用
     *   · 触发：强提醒三联动 + 弹窗确认
     *
     * - "medium"（中风险 - 橙色标签）：
     *   · OTC但需注意用法用量
     *   · 有一定的不良反应概率
     *   · 不适合长期大量使用
     *   · 触发：中等强度振动 + Toast提示
     *
     * - "low"（低风险 - 绿色标签）：
     *   · 常见OTC药品
     *   · 安全性较高
     *   · 按说明使用即可
     *   · 触发：页面内文字提示
     *
     * 评估维度：
     * 1. 是否为处方药
     * 2. 禁忌症数量和严重程度
     * 3. 不良反应发生率和严重程度
     * 4. 药物相互作用可能性
     * 5. 特殊人群（孕妇/儿童/老人）安全性
     *
     * JSON字段名：risk_level / risk
     */
    @SerializedName("risk_level")
    val riskLevel: String = "low",

    /**
     * 风险提示列表（Risk Prompts）
     *
     * 结构化的风险提示文本数组。
     * 每条提示都是一个简短明确的警告语句。
     *
     * 示例：
     * ```kotlin
     * listOf(
     *     "不可与酒精同时服用",
     *     "服药期间禁止驾驶机动车",
     *     "请勿超过推荐剂量服用",
     *     "孕妇及哺乳期妇女慎用"
     * )
     * ```
     *
     * UI渲染方式：
     * - 列表形式逐条展示（带圆点或数字序号）
     * - 高风险提示使用红色加粗字体
     * - 支持TTS逐条播报
     *
     * JSON字段名：risk_prompts / warnings
     */
    @SerializedName("risk_prompts")
    val riskPrompts: List<String> = emptyList(),

    /**
     * 警告标签（Warning Label）
     *
     * 药品包装上的强制性警示语。
     * 通常由监管部门规定必须标注的内容。
     *
     * 常见标签：
     * - "凭医师处方销售、购买和使用！"（处方药标识）
     * - "请仔细阅读说明书并在医师指导下使用"
     * - "本品含麻黄碱，运动员慎用"
     * - "外用药，切勿口服"
     *
     * 展示策略：
     * - 在药品卡片顶部以醒目徽章形式显示
     * - 使用国际通用的警告图标（⚠️、🚫等）
     *
     * JSON字段名：warning_label / label
     */
    @SerializedName("warning_label")
    val warningLabel: String? = null,

    /**
     * 禁忌症详细列表（Contraindications List）
     *
     * 结构化的禁忌症数组，比taboo字段更详细和机器可读。
     * 每项包含禁忌类型和具体描述。
     *
     * 数据格式示例：
     * ```json
     * [
     *   {"type": "allergy", "description": "对阿司匹林过敏者禁用"},
     *   {"type": "pregnancy", "description": "孕妇禁用，可能致畸"},
     *   {"type": "disease", "description": "活动性消化道溃疡患者禁用"},
     *   {"type": "drug_interaction", "description": "不可与抗凝药联用"}
     * ]
     * ```
     *
     * 类型枚举：
     * - allergy：过敏相关
     * - pregnancy：妊娠期相关
     * - lactation：哺乳期相关
     * - pediatric：儿童相关
     * - geriatric：老年相关
     * - disease：疾病状态相关
     * - drug_interaction：药物相互作用
     * - other：其他
     *
     * JSON字段名：contraindications
     */
    @SerializedName("contraindications")
    val contraindications: List<Map<String, String>> = emptyList(),

    /**
     * 手语关键词列表（Sign Language Keywords）
     *
     * 用于手语翻译系统的核心词汇集合。
     * 当TTS播报到这些词语时，同步播放对应的手语动画。
     *
     * 选词原则：
     * - 药品核心名称（通用名的简称）
     * - 关键医学术语（如"止痛"、"消炎"、"降压"）
     * - 用法相关词汇（如"口服"、"一日三次"、"饭后"）
     * - 警示词汇（如"危险"、"禁止"、"立即停药"）
     *
     * 示例：
     * ```kotlin
     * listOf(
     *     "布洛芬", "止痛", "退烧",
     *     "口服", "一日三次",
     *     "危险", "过敏", "禁止"
     * )
     * ```
     *
     * 技术实现：
     * - 与SignLanguageManager配合工作
     * - 实时检测TTS文本流中的关键词
     * - 匹配到手语数据库中的动画资源并播放
     *
     * JSON字段名：sign_keywords / sign_language
     */
    @SerializedName("sign_keywords")
    val signKeywords: List<String> = emptyList(),

    /**
     * TTS语音摘要（Text-to-Speech Summary）
     *
     * 为视障用户优化的精炼版药品信息摘要。
     * 经过语言优化处理，适合语音合成引擎朗读。
     *
     * 设计原则：
     * - 句子简短（避免长难句导致理解困难）
     * - 口语化表达（符合日常说话习惯）
     * - 重点突出（优先传达最关键的安全信息）
     * - 控制长度（完整播报不超过60秒）
     *
     * 内容结构模板：
     * """
     * [药品名]，[商品名]。
     * 这是[分类]类药品，[风险等级]风险。
     * [核心适应症一句话]。
     * 注意事项：[最重要的1-2条禁忌或警告]。
     * 用法：[简化版的用法用量]。
     * 请仔细阅读说明书或在医生指导下使用。
     * """
     *
     * 示例：
     * """
     * 布洛芬，商品名芬必得。
     * 这是解热镇痛药，低风险。
     * 用于缓解头痛、关节痛、牙痛、肌肉痛。
     * 注意：不可空腹服用，饭后服用减少胃刺激。
     * 用法：成人一次1粒，一日2-3次，24小时不超过6粒。
     * """
     *
     * JSON字段名：tts_summary / voice_summary
     */
    @SerializedName("tts_summary")
    val ttsSummary: String? = null,

    /**
     * 药品图片URL地址
     *
     * 药品包装盒正面照片的网络地址。
     * 用于UI展示和辅助用户确认药品外观。
     *
     * 图片规格：
     * - 格式：JPG/PNG/WebP
     * - 尺寸：400x400像素（缩略图）、800x800（高清图）
     * - 大小：200KB以内（压缩优化）
     *
     * 使用Coil图片加载库异步加载和缓存。
     *
     * JSON字段名：image_url / image / photo
     */
    @SerializedName("image_url")
    val imageUrl: String? = null,

    /**
     * 数据来源标识（Source Tag）
     *
     * 标识本条药品数据的来源渠道。
     * 用于数据溯源和质量可信度评估。
     *
     * 来源类型：
     * - "online_api"：来自后端实时API查询
     * - "offline_db"：来自本地预置离线数据库
     * - "ocr_recognition"：来自OCR识别提取
     * - "user_feedback"：来自用户纠错提交
     * - "seed_demo"：演示用的种子数据
     *
     * 业务影响：
     * - 决定是否显示"联网验证"按钮
     * - 影响数据的可信度标识展示
     * - 统计分析各来源的数据准确率
     *
     * JSON字段名：source_tag / source
     */
    @SerializedName("source_tag")
    val sourceTag: String? = null
) {
    companion object {
        /** 风险等级常量 */
        const val RISK_HIGH = "high"
        const val RISK_MEDIUM = "medium"
        const val RISK_LOW = "low"

        /** 数据来源常量 */
        const val SOURCE_ONLINE_API = "online_api"
        const val SOURCE_OFFLINE_DB = "offline_db"
        const val SOURCE_OCR = "ocr_recognition"
        const val SOURCE_FEEDBACK = "user_feedback"
        const val SOURCE_SEED = "seed_demo"

        /**
         * 获取最高风险等级
         *
         * 从riskLevel和riskPrompts综合判断实际风险级别。
         * 如果riskPrompts非空且包含严重警告，提升至high级别。
         *
         * @return 最终的风险等级字符串（"high"/"medium"/"low"）
         */
        fun Drug.getHighestRiskLevel(): String {
            // 如果已有高风险标记，直接返回
            if (this.riskLevel == RISK_HIGH) return RISK_HIGH

            // 检查风险提示中是否包含严重关键词
            val severeKeywords = listOf(
                "致命", "危及生命", "立即停药",
                "住院", "禁用", "严禁"
            )

            val hasSevereWarning = this.riskPrompts.any { prompt ->
                severeKeywords.any { keyword -> prompt.contains(keyword) }
            }

            return if (hasSevereWarning) {
                RISK_HIGH
            } else {
                this.riskLevel
            }
        }

        /**
         * 获取风险等级对应的Material3颜色
         *
         * 将抽象的风险等级映射为具体的UI颜色值，
         * 用于卡片背景、文字颜色、图标着色等。
         *
         * 注意：此方法返回的是颜色资源名称字符串，
         * 实际使用时需通过ColorResource解析。
         *
         * @param riskLevel 风险等级字符串
         * @return Material3语义化颜色名称
         */
        fun getRiskColorName(riskLevel: String): String {
            return when (riskLevel) {
                RISK_HIGH -> "errorContainer"      // 红色系（危险）
                RISK_MEDIUM -> "tertiaryContainer" // 橙色系（警告）
                else -> "surface"                  // 默认色（安全）
            }
        }

        /**
         * 生成完整的TTS播报文本
         *
         * 如果已存在预设的ttsSummary则直接使用，
         * 否则根据关键字段动态生成摘要文本。
         *
         * @return 适合TTS引擎朗读的文本字符串
         */
        fun Drug.generateTTSText(): String {
            // 优先使用预设的TTS摘要
            if (!this.ttsSummary.isNullOrBlank()) {
                return this.ttsSummary!!
            }

            // 动态生成摘要文本
            return buildString {
                appendLine("${this.genericName ?: '未知药品'}")
                this.tradeName?.let { appendLine("商品名：$it") }
                appendLine("风险等级：${getRiskDisplayName(this.getHighestRiskLevel())}")
                this.indication?.let { appendLine("适应症：$it") }

                if (riskPrompts.isNotEmpty()) {
                    appendLine("注意事项：")
                    riskPrompts.forEach { prompt ->
                        appendLine(prompt)
                    }
                }

                appendLine("请仔细阅读说明书或在药师指导下使用。")
            }.trim()
        }

        /**
         * 获取风险等级的中文显示名称
         *
         * @param level 风险等级代码
         * @return 中文显示文本
         */
        private fun getRiskDisplayName(level: String): String {
            return when (level) {
                RISK_HIGH -> "高风险"
                RISK_MEDIUM -> "中风险"
                RISK_LOW -> "低风险"
                else -> "未知"
            }
        }
    }
}
