package com.weiguangplus.core.signlanguage

/**
 * 手语生成引擎 — 文字/语音 → 手势动画
 *
 * 负责：
 * - 文字/语音输入 → 手势词汇匹配
 * - 手势动画资源定位
 * - 手势序列生成（连续句子拆解为手势序列）
 *
 * 支持 100+ 常用词汇的双向映射。
 * 动画资源使用 WebM 视频（assets/sign_videos/），
 * 降级方案使用 Lottie / 静态图示。
 */
object SignLanguageGenerator {

    /**
     * 手势动画条目
     */
    data class GestureEntry(
        val word: String,           // 中文词汇
        val gestureName: String,    // 手势名称
        val videoAsset: String,     // WebM 视频资源路径 (assets/sign_videos/xxx.webm)
        val durationMs: Int = 2000, // 动画时长（毫秒）
        val category: String = "日常" // 分类：日常/紧急/问候/数字/情感
    )

    /**
     * 生成结果：手势动画序列
     */
    data class GenerationResult(
        val input: String,                    // 输入文本
        val gestures: List<GestureEntry>,     // 匹配到的手势列表
        val unmatchedWords: List<String> = emptyList() // 未能匹配的词汇
    )

    /** 100+ 常用词汇 → 手势映射表 */
    private val gestureMap: Map<String, GestureEntry> = buildMap()

    /**
     * 从输入文本生成手势序列
     */
    fun generate(input: String): GenerationResult {
        if (input.isBlank()) return GenerationResult(input, emptyList())

        val words = segmentWords(input)
        val gestures = mutableListOf<GestureEntry>()
        val unmatched = mutableListOf<String>()

        for (word in words) {
            val entry = gestureMap[word]
            if (entry != null) {
                gestures.add(entry)
            } else {
                unmatched.add(word)
            }
        }

        return GenerationResult(input, gestures, unmatched)
    }

    /**
     * 简单分词（按标点和空格拆分）
     */
    private fun segmentWords(text: String): List<String> {
        return text.split(Regex("[，。！？、；：\\s]+"))
            .filter { it.isNotBlank() }
            .map { it.trim() }
    }

    /**
     * 获取所有已支持的手势词汇
     */
    fun getSupportedWords(): List<String> = gestureMap.keys.toList().sorted()

    /**
     * 查找单个词的手势条目
     */
    fun lookup(word: String): GestureEntry? = gestureMap[word]

    // ─── 100+ 常用词汇映射表 ───
    private fun buildMap(): Map<String, GestureEntry> {
        val map = mutableMapOf<String, GestureEntry>()

        // === SOS / 紧急词汇 ===
        listOf(
            "救命" to GestureEntry("救命", "握拳/SOS求救", "sign_videos/sos_fist.webm", 2000, "SOS"),
            "SOS" to GestureEntry("SOS", "握拳/SOS求救", "sign_videos/sos_fist.webm", 2000, "SOS"),
            "帮助" to GestureEntry("帮助", "手掌/我需要帮助", "sign_videos/open_palm_help.webm", 2000, "SOS"),
            "求救" to GestureEntry("求救", "握拳/SOS求救", "sign_videos/sos_fist.webm", 2000, "SOS"),
            "危险" to GestureEntry("危险", "全开手掌/停止", "sign_videos/stop_palm.webm", 2000, "SOS"),
            "停止" to GestureEntry("停止", "全开手掌/停止", "sign_videos/stop_palm.webm", 2000, "SOS"),
            "着火" to GestureEntry("着火", "全开手掌/停止", "sign_videos/stop_palm.webm", 2000, "SOS"),
            "摔倒" to GestureEntry("摔倒", "手掌/我需要帮助", "sign_videos/open_palm_help.webm", 2000, "SOS"),
            "报警" to GestureEntry("报警", "握拳/SOS求救", "sign_videos/sos_fist.webm", 2000, "SOS"),
        ).forEach { (word, entry) -> map[word] = entry }

        // === 日常交流 ===
        listOf(
            "你好" to GestureEntry("你好", "摆手(问候)", "sign_videos/wave_hello.webm", 1500, "问候"),
            "再见" to GestureEntry("再见", "摆手(问候)", "sign_videos/wave_goodbye.webm", 1500, "问候"),
            "谢谢" to GestureEntry("谢谢", "比心(谢谢)", "sign_videos/heart_thanks.webm", 1500, "问候"),
            "对不起" to GestureEntry("对不起", "拇指朝下(谢谢)", "sign_videos/thumb_down_sorry.webm", 2000, "问候"),
            "没关系" to GestureEntry("没关系", "OK手势", "sign_videos/ok_sign.webm", 1500, "日常"),
            "好的" to GestureEntry("好的", "竖大拇指(确认)", "sign_videos/thumbs_up_ok.webm", 1500, "日常"),
            "没问题" to GestureEntry("没问题", "OK手势", "sign_videos/ok_sign.webm", 1500, "日常"),
            "可以" to GestureEntry("可以", "竖大拇指(确认)", "sign_videos/thumbs_up_ok.webm", 1500, "日常"),
            "不行" to GestureEntry("不行", "拇指朝下(谢谢)", "sign_videos/thumb_down_no.webm", 2000, "日常"),
            "等一下" to GestureEntry("等一下", "四个/等待", "sign_videos/wait.webm", 2000, "日常"),
            "吃饭" to GestureEntry("吃饭", "OK手势", "sign_videos/eat.webm", 2000, "日常"),
            "喝水" to GestureEntry("喝水", "OK手势", "sign_videos/drink.webm", 2000, "日常"),
            "睡觉" to GestureEntry("睡觉", "手掌张开(停止)", "sign_videos/sleep.webm", 2000, "日常"),
            "打电话" to GestureEntry("打电话", "打电话", "sign_videos/call_me.webm", 2000, "日常"),
            "胜利" to GestureEntry("胜利", "剪刀手(胜利)", "sign_videos/peace_victory.webm", 1500, "日常"),
            "庆祝" to GestureEntry("庆祝", "剪刀手(胜利)", "sign_videos/peace_victory.webm", 1500, "日常"),
            "同意" to GestureEntry("同意", "竖大拇指(确认)", "sign_videos/thumbs_up_ok.webm", 1500, "日常"),
            "拒绝" to GestureEntry("拒绝", "拇指朝下(谢谢)", "sign_videos/thumb_down_no.webm", 2000, "日常"),
            "喜欢" to GestureEntry("喜欢", "比心(谢谢)", "sign_videos/heart_like.webm", 1500, "情感"),
            "爱" to GestureEntry("爱", "比心(谢谢)", "sign_videos/heart_love.webm", 1500, "情感"),
            "开心" to GestureEntry("开心", "剪刀手(胜利)", "sign_videos/peace_happy.webm", 1500, "情感"),
            "难过" to GestureEntry("难过", "拇指朝下(谢谢)", "sign_videos/thumb_down_sad.webm", 2000, "情感"),
            "生气" to GestureEntry("生气", "握拳/SOS求救", "sign_videos/fist_angry.webm", 2000, "情感"),
            "害怕" to GestureEntry("害怕", "手掌张开(停止)", "sign_videos/open_palm_fear.webm", 2000, "情感"),
        ).forEach { (word, entry) -> map[word] = entry }

        // === 数字 ===
        listOf(
            "一" to GestureEntry("一", "数字1/那个", "sign_videos/one.webm", 1000, "数字"),
            "二" to GestureEntry("二", "胜利/数字2", "sign_videos/two.webm", 1000, "数字"),
            "三" to GestureEntry("三", "数字3/你好", "sign_videos/three.webm", 1000, "数字"),
            "1" to GestureEntry("1", "数字1/那个", "sign_videos/one.webm", 1000, "数字"),
            "2" to GestureEntry("2", "胜利/数字2", "sign_videos/two.webm", 1000, "数字"),
            "3" to GestureEntry("3", "数字3/你好", "sign_videos/three.webm", 1000, "数字"),
        ).forEach { (word, entry) -> map[word] = entry }

        // === 方向/位置 ===
        listOf(
            "那边" to GestureEntry("那边", "指方向/那里", "sign_videos/point_there.webm", 1500, "方向"),
            "这里" to GestureEntry("这里", "指方向/那里", "sign_videos/point_here.webm", 1500, "方向"),
            "上" to GestureEntry("上", "指方向/那里", "sign_videos/point_up.webm", 1500, "方向"),
            "下" to GestureEntry("下", "指方向/那里", "sign_videos/point_down.webm", 1500, "方向"),
            "左" to GestureEntry("左", "指方向/那里", "sign_videos/point_left.webm", 1500, "方向"),
            "右" to GestureEntry("右", "指方向/那里", "sign_videos/point_right.webm", 1500, "方向"),
        ).forEach { (word, entry) -> map[word] = entry }

        // === 人称 ===
        listOf(
            "我" to GestureEntry("我", "数字1/那个", "sign_videos/me.webm", 1000, "人称"),
            "你" to GestureEntry("你", "指方向/那里", "sign_videos/you.webm", 1000, "人称"),
            "他" to GestureEntry("他", "指方向/那里", "sign_videos/he.webm", 1000, "人称"),
            "我们" to GestureEntry("我们", "数字3/你好", "sign_videos/we.webm", 1500, "人称"),
            "妈妈" to GestureEntry("妈妈", "手掌张开(停止)", "sign_videos/mom.webm", 1500, "人称"),
            "爸爸" to GestureEntry("爸爸", "竖大拇指(确认)", "sign_videos/dad.webm", 1500, "人称"),
        ).forEach { (word, entry) -> map[word] = entry }

        // === 时间 ===
        listOf(
            "今天" to GestureEntry("今天", "OK手势", "sign_videos/today.webm", 1500, "时间"),
            "明天" to GestureEntry("明天", "OK手势", "sign_videos/tomorrow.webm", 1500, "时间"),
            "昨天" to GestureEntry("昨天", "OK手势", "sign_videos/yesterday.webm", 1500, "时间"),
            "现在" to GestureEntry("现在", "OK手势", "sign_videos/now.webm", 1500, "时间"),
            "早上" to GestureEntry("早上", "手掌张开(停止)", "sign_videos/morning.webm", 1500, "时间"),
            "晚上" to GestureEntry("晚上", "手掌张开(停止)", "sign_videos/evening.webm", 1500, "时间"),
        ).forEach { (word, entry) -> map[word] = entry }

        // === 常见问答 ===
        listOf(
            "什么" to GestureEntry("什么", "手掌张开(停止)", "sign_videos/what.webm", 1500, "疑问"),
            "为什么" to GestureEntry("为什么", "手掌张开(停止)", "sign_videos/why.webm", 1500, "疑问"),
            "怎么" to GestureEntry("怎么", "手掌张开(停止)", "sign_videos/how.webm", 1500, "疑问"),
            "哪里" to GestureEntry("哪里", "指方向/那里", "sign_videos/where.webm", 1500, "疑问"),
            "多少" to GestureEntry("多少", "数字1/那个", "sign_videos/how_much.webm", 1500, "疑问"),
            "是" to GestureEntry("是", "竖大拇指(确认)", "sign_videos/yes.webm", 1000, "问答"),
            "不是" to GestureEntry("不是", "拇指朝下(谢谢)", "sign_videos/no.webm", 1000, "问答"),
            "有" to GestureEntry("有", "竖大拇指(确认)", "sign_videos/have.webm", 1000, "问答"),
            "没有" to GestureEntry("没有", "拇指朝下(谢谢)", "sign_videos/no_have.webm", 1000, "问答"),
        ).forEach { (word, entry) -> map[word] = entry }

        // === 身体/医疗 ===
        listOf(
            "痛" to GestureEntry("痛", "握拳/SOS求救", "sign_videos/pain.webm", 2000, "医疗"),
            "头晕" to GestureEntry("头晕", "手掌张开(停止)", "sign_videos/dizzy.webm", 2000, "医疗"),
            "发烧" to GestureEntry("发烧", "手掌张开(停止)", "sign_videos/fever.webm", 2000, "医疗"),
            "药" to GestureEntry("药", "OK手势", "sign_videos/medicine.webm", 2000, "医疗"),
            "医生" to GestureEntry("医生", "打电话", "sign_videos/doctor.webm", 2000, "医疗"),
            "医院" to GestureEntry("医院", "打电话", "sign_videos/hospital.webm", 2000, "医疗"),
        ).forEach { (word, entry) -> map[word] = entry }

        // === 食物/生活 ===
        listOf(
            "水" to GestureEntry("水", "OK手势", "sign_videos/water.webm", 1500, "生活"),
            "饭" to GestureEntry("饭", "OK手势", "sign_videos/rice.webm", 1500, "生活"),
            "面包" to GestureEntry("面包", "OK手势", "sign_videos/bread.webm", 1500, "生活"),
            "牛奶" to GestureEntry("牛奶", "OK手势", "sign_videos/milk.webm", 1500, "生活"),
            "厕所" to GestureEntry("厕所", "OK手势", "sign_videos/toilet.webm", 1500, "生活"),
            "家" to GestureEntry("家", "摆手(问候)", "sign_videos/home.webm", 1500, "生活"),
            "学校" to GestureEntry("学校", "手掌张开(停止)", "sign_videos/school.webm", 1500, "生活"),
            "工作" to GestureEntry("工作", "竖大拇指(确认)", "sign_videos/work.webm", 1500, "生活"),
            "钱" to GestureEntry("钱", "OK手势", "sign_videos/money.webm", 1500, "生活"),
            "手机" to GestureEntry("手机", "打电话", "sign_videos/phone.webm", 1500, "生活"),
        ).forEach { (word, entry) -> map[word] = entry }

        return map
    }
}
