package com.weiguangchangxing.weiguang_plus.feature.signlanguage

import android.content.Context

data class SignPhrase(
    val id: Int,
    val text: String,
    val chineseSign: String,
    val lottieFile: String,
    val category: String,
    val frequency: Int
) {
    var gestureTemplates: List<HandGestureClassifier.GestureTemplate> = emptyList()
    var gestureHints: String = ""
}

class SignLanguageDatabase(private val context: Context) {

    private val phrases = mutableListOf(
        // ========== 原有30条短语（保持不变） ==========
        SignPhrase(1, "我要喝水", "我-喝水", "water.json", "日常需求", 95).apply {
            gestureTemplates = listOf(
                HandGestureClassifier.GestureTemplate(name = "六(喝水)", extendedFingers = listOf(true, false, false, false, true)),
                HandGestureClassifier.GestureTemplate(name = "握拳(拿杯子)", extendedFingers = listOf(false, false, false, false, false))
            )
            gestureHints = "可做握拳状或六手势"
        },
        SignPhrase(2, "我渴了", "我-渴", "thirsty.json", "日常需求", 90).apply {
            gestureTemplates = listOf(
                HandGestureClassifier.GestureTemplate(name = "六(渴)", extendedFingers = listOf(true, false, false, false, true))
            )
            gestureHints = "张开拇指和小指"
        },
        SignPhrase(3, "我要去厕所", "我-厕所", "toilet.json", "日常需求", 98),
        SignPhrase(4, "我不舒服", "我-不舒服", "unwell.json", "健康相关", 96).apply {
            gestureTemplates = listOf(
                HandGestureClassifier.GestureTemplate(name = "握拳(不舒服)", extendedFingers = listOf(false, false, false, false, false))
            )
            gestureHints = "可做握拳状表示"
        },
        SignPhrase(5, "请帮帮我", "请-帮助", "help.json", "紧急求助", 100).apply {
            gestureTemplates = listOf(
                HandGestureClassifier.GestureTemplate(name = "打开手掌(求助)", extendedFingers = listOf(true, true, true, true, true))
            )
            gestureHints = "张开手掌示意"
        },
        SignPhrase(6, "我要吃药", "我-吃药", "medicine.json", "健康相关", 92),
        SignPhrase(7, "请联系家人", "请-联系-家人", "family.json", "紧急求助", 94),
        SignPhrase(8, "我头疼", "我-头-疼", "headache.json", "健康相关", 88).apply {
            gestureTemplates = listOf(
                HandGestureClassifier.GestureTemplate(name = "食指指(头疼)", extendedFingers = listOf(false, true, false, false, false))
            )
            gestureHints = "伸出食指指向头部"
        },
        SignPhrase(9, "我肚子疼", "我-肚子-疼", "stomachache.json", "健康相关", 87),
        SignPhrase(10, "我发烧了", "我-发烧", "fever.json", "健康相关", 85),
        SignPhrase(11, "我咳嗽", "我-咳嗽", "cough.json", "健康相关", 80),
        SignPhrase(12, "我感冒了", "我-感冒", "cold.json", "健康相关", 83),
        SignPhrase(13, "我想睡觉", "我-睡觉", "sleepy.json", "日常需求", 78),
        SignPhrase(14, "我饿了", "我-饿", "hungry.json", "日常需求", 91).apply {
            gestureTemplates = listOf(
                HandGestureClassifier.GestureTemplate(name = "握拳(饿)", extendedFingers = listOf(false, false, false, false, false))
            )
            gestureHints = "轻握拳头示意饥饿"
        },
        SignPhrase(15, "谢谢", "谢谢", "thanks.json", "礼貌用语", 99).apply {
            gestureTemplates = listOf(
                HandGestureClassifier.GestureTemplate(name = "打开手掌(谢谢)", extendedFingers = listOf(true, true, true, true, true))
            )
            gestureHints = "张开手掌轻轻摆动画弧线"
        },
        SignPhrase(16, "你好", "你好", "hello.json", "礼貌用语", 97).apply {
            gestureTemplates = listOf(
                HandGestureClassifier.GestureTemplate(name = "打开手掌(你好)", extendedFingers = listOf(true, true, true, true, true))
            )
            gestureHints = "张开手掌轻轻摆动"
        },
        SignPhrase(17, "再见", "再见", "bye.json", "礼貌用语", 93).apply {
            gestureTemplates = listOf(
                HandGestureClassifier.GestureTemplate(name = "打开手掌(再见)", extendedFingers = listOf(true, true, true, true, true))
            )
            gestureHints = "张开手掌前后摆动"
        },
        SignPhrase(18, "对不起", "对不起", "sorry.json", "礼貌用语", 86).apply {
            gestureTemplates = listOf(
                HandGestureClassifier.GestureTemplate(name = "握拳(抱歉)", extendedFingers = listOf(false, false, false, false, false))
            )
            gestureHints = "握拳做出抱歉表情"
        },
        SignPhrase(19, "没关系", "没关系", "itsok.json", "礼貌用语", 84),
        SignPhrase(20, "请稍等", "请-稍等", "wait.json", "礼貌用语", 82).apply {
            gestureTemplates = listOf(
                HandGestureClassifier.GestureTemplate(name = "打开手掌(稍等)", extendedFingers = listOf(true, true, true, true, true))
            )
            gestureHints = "掌心朝外示意稍等"
        },
        SignPhrase(21, "我明白了", "我-明白", "understand.json", "日常表达", 79),
        SignPhrase(22, "我不明白", "我-不明白", "dont_understand.json", "日常表达", 81),
        SignPhrase(23, "请问", "请问", "excuse_me.json", "礼貌用语", 77),
        SignPhrase(24, "救命", "救命", "emergency.json", "紧急求助", 100).apply {
            gestureTemplates = listOf(
                HandGestureClassifier.GestureTemplate(name = "打开手掌(求救)", extendedFingers = listOf(true, true, true, true, true)),
                HandGestureClassifier.GestureTemplate(name = "六(求救)", extendedFingers = listOf(true, false, false, false, true))
            )
            gestureHints = "用力张开手掌或做六手势"
        },
        SignPhrase(25, "打119", "打-119", "call_119.json", "紧急求助", 98),
        SignPhrase(26, "打120", "打-120", "call_120.json", "紧急求助", 98),
        SignPhrase(27, "我叫", "我-叫", "my_name.json", "自我介绍", 89).apply {
            gestureTemplates = listOf(
                HandGestureClassifier.GestureTemplate(name = "食指指(指向自己)", extendedFingers = listOf(false, true, false, false, false))
            )
            gestureHints = "伸出食指指向自己"
        },
        SignPhrase(28, "我是聋人", "我-聋人", "deaf.json", "身份说明", 95).apply {
            gestureTemplates = listOf(
                HandGestureClassifier.GestureTemplate(name = "食指指(耳朵)", extendedFingers = listOf(false, true, false, false, false))
            )
            gestureHints = "伸出食指指向耳朵"
        },
        SignPhrase(29, "我是盲人", "我-盲人", "blind.json", "身份说明", 93).apply {
            gestureTemplates = listOf(
                HandGestureClassifier.GestureTemplate(name = "食指指(眼睛)", extendedFingers = listOf(false, true, false, false, false))
            )
            gestureHints = "伸出食指指向眼睛"
        },
        SignPhrase(30, "我需要手语翻译", "我-需要-翻译", "interpreter.json", "特殊需求", 91).apply {
            gestureTemplates = listOf(
                HandGestureClassifier.GestureTemplate(name = "V字(翻译)", extendedFingers = listOf(false, true, true, false, false))
            )
            gestureHints = "伸出食指和中指"
        },

        // ========== 医院场景（31~44）共14条 ==========
        SignPhrase(31, "我要挂号", "我-挂号", "register.json", "医院场景", 85).apply {
            gestureTemplates = listOf(
                HandGestureClassifier.GestureTemplate(name = "食指横划(挂号)", extendedFingers = listOf(false, true, false, false, false))
            )
            gestureHints = "食指横划模拟挂号登记"
        },
        SignPhrase(32, "看医生", "看-医生", "see_doctor.json", "医院场景", 90).apply {
            gestureTemplates = listOf(
                HandGestureClassifier.GestureTemplate(name = "打开手掌(看医生)", extendedFingers = listOf(true, true, true, true, true))
            )
            gestureHints = "张开手掌指向医生方向"
        },
        SignPhrase(33, "拍片子", "拍-片子", "xray.json", "医院场景", 78),
        SignPhrase(34, "抽血", "抽血", "blood_test.json", "医院场景", 80).apply {
            gestureTemplates = listOf(
                HandGestureClassifier.GestureTemplate(name = "食指中指并拢(抽血)", extendedFingers = listOf(false, true, true, false, false))
            )
            gestureHints = "食指中指并拢做抽血姿势"
        },
        SignPhrase(35, "化验", "化验", "lab_test.json", "医院场景", 76),
        SignPhrase(36, "取药", "取-药", "get_medicine.json", "医院场景", 82),
        SignPhrase(37, "住院", "住院", "hospitalize.json", "医院场景", 75),
        SignPhrase(38, "出院", "出院", "discharge.json", "医院场景", 74),
        SignPhrase(39, "打针", "打-针", "injection.json", "医院场景", 77),
        SignPhrase(40, "量体温", "量-体温", "temperature.json", "医院场景", 79).apply {
            gestureTemplates = listOf(
                HandGestureClassifier.GestureTemplate(name = "食指指(量体温)", extendedFingers = listOf(false, true, false, false, false))
            )
            gestureHints = "食指指向腋下示意量体温"
        },
        SignPhrase(41, "做手术", "做-手术", "surgery.json", "医院场景", 72),
        SignPhrase(42, "病历", "病历", "medical_record.json", "医院场景", 70),
        SignPhrase(43, "医保卡", "医保-卡", "insurance_card.json", "医院场景", 71),
        SignPhrase(44, "急诊", "急诊", "emergency_room.json", "医院场景", 88).apply {
            gestureTemplates = listOf(
                HandGestureClassifier.GestureTemplate(name = "打开手掌(急诊)", extendedFingers = listOf(true, true, true, true, true))
            )
            gestureHints = "张开手掌快速挥动表示紧急"
        },

        // ========== 药店场景（45~51）共7条 ==========
        SignPhrase(45, "买药", "买-药", "buy_medicine.json", "药店场景", 86),
        SignPhrase(46, "测血压", "测-血压", "blood_pressure.json", "药店场景", 75).apply {
            gestureTemplates = listOf(
                HandGestureClassifier.GestureTemplate(name = "握拳(测血压)", extendedFingers = listOf(false, false, false, false, false))
            )
            gestureHints = "握拳模仿测血压姿势"
        },
        SignPhrase(47, "测血糖", "测-血糖", "blood_sugar.json", "药店场景", 72),
        SignPhrase(48, "戴口罩", "戴-口罩", "wear_mask.json", "药店场景", 78).apply {
            gestureTemplates = listOf(
                HandGestureClassifier.GestureTemplate(name = "打开手掌(戴口罩)", extendedFingers = listOf(true, true, true, true, true))
            )
            gestureHints = "手掌在面前划过模拟戴口罩"
        },
        SignPhrase(49, "体温计", "体温计", "thermometer.json", "药店场景", 68),
        SignPhrase(50, "创可贴", "创可贴", "bandaid.json", "药店场景", 65),
        SignPhrase(51, "消毒液", "消毒-液", "disinfect.json", "药店场景", 66),

        // ========== 交通出行（52~63）共12条 ==========
        SignPhrase(52, "打车", "打车", "taxi.json", "交通出行", 88).apply {
            gestureTemplates = listOf(
                HandGestureClassifier.GestureTemplate(name = "打开手掌(打车)", extendedFingers = listOf(true, true, true, true, true))
            )
            gestureHints = "手掌朝外挥动示意打车"
        },
        SignPhrase(53, "坐公交", "坐-公交", "bus.json", "交通出行", 85),
        SignPhrase(54, "坐地铁", "坐-地铁", "subway.json", "交通出行", 84),
        SignPhrase(55, "坐飞机", "坐-飞机", "airplane.json", "交通出行", 77),
        SignPhrase(56, "坐火车", "坐-火车", "train.json", "交通出行", 80),
        SignPhrase(57, "去哪里", "去-哪里", "where_to.json", "交通出行", 82).apply {
            gestureTemplates = listOf(
                HandGestureClassifier.GestureTemplate(name = "食指指(去哪里)", extendedFingers = listOf(false, true, false, false, false))
            )
            gestureHints = "伸出食指指向远方表示去向"
        },
        SignPhrase(58, "买票", "买-票", "buy_ticket.json", "交通出行", 79),
        SignPhrase(59, "导航", "导航", "navigate.json", "交通出行", 76),
        SignPhrase(60, "下车", "下车", "get_off.json", "交通出行", 81),
        SignPhrase(61, "车站", "车站", "station.json", "交通出行", 73),
        SignPhrase(62, "路口", "路口", "intersection.json", "交通出行", 70),
        SignPhrase(63, "我迷路了", "我-迷路", "lost.json", "交通出行", 78),

        // ========== 购物消费（64~75）共12条 ==========
        SignPhrase(64, "多少钱", "多少-钱", "how_much.json", "购物消费", 92).apply {
            gestureTemplates = listOf(
                HandGestureClassifier.GestureTemplate(name = "拇指食指捏合(钱)", extendedFingers = listOf(false, true, true, false, false))
            )
            gestureHints = "拇指与食指捏合搓动表示钱"
        },
        SignPhrase(65, "太贵了", "太-贵", "too_expensive.json", "购物消费", 85).apply {
            gestureTemplates = listOf(
                HandGestureClassifier.GestureTemplate(name = "打开手掌(太贵)", extendedFingers = listOf(true, true, true, true, true))
            )
            gestureHints = "手掌摊开做出惊讶表情"
        },
        SignPhrase(66, "便宜点", "便宜-点", "cheaper.json", "购物消费", 80),
        SignPhrase(67, "扫码支付", "扫码-支付", "scan_pay.json", "购物消费", 88).apply {
            gestureTemplates = listOf(
                HandGestureClassifier.GestureTemplate(name = "食指指(扫码)", extendedFingers = listOf(false, true, false, false, false))
            )
            gestureHints = "食指指向手机模拟扫码"
        },
        SignPhrase(68, "微信支付", "微信-支付", "wechat_pay.json", "购物消费", 90),
        SignPhrase(69, "支付宝", "支付宝", "alipay.json", "购物消费", 89),
        SignPhrase(70, "银行卡", "银行-卡", "bank_card.json", "购物消费", 82),
        SignPhrase(71, "现金", "现金", "cash.json", "购物消费", 78),
        SignPhrase(72, "购物袋", "购物-袋", "shopping_bag.json", "购物消费", 72),
        SignPhrase(73, "打折", "打折", "discount.json", "购物消费", 76),
        SignPhrase(74, "退货", "退货", "refund.json", "购物消费", 71),
        SignPhrase(75, "试穿", "试穿", "try_on.json", "购物消费", 73),

        // ========== 学习教育（76~85）共10条 ==========
        SignPhrase(76, "上课", "上-课", "class.json", "学习教育", 84).apply {
            gestureTemplates = listOf(
                HandGestureClassifier.GestureTemplate(name = "打开手掌(上课)", extendedFingers = listOf(true, true, true, true, true))
            )
            gestureHints = "抬手示意上课"
        },
        SignPhrase(77, "考试", "考试", "exam.json", "学习教育", 82),
        SignPhrase(78, "做作业", "做-作业", "homework.json", "学习教育", 78),
        SignPhrase(79, "老师", "老师", "teacher.json", "学习教育", 86).apply {
            gestureTemplates = listOf(
                HandGestureClassifier.GestureTemplate(name = "食指指(老师)", extendedFingers = listOf(false, true, false, false, false))
            )
            gestureHints = "伸出食指朝上表示老师"
        },
        SignPhrase(80, "同学", "同学", "classmate.json", "学习教育", 83),
        SignPhrase(81, "图书馆", "图书馆", "library.json", "学习教育", 75),
        SignPhrase(82, "教室", "教室", "classroom.json", "学习教育", 74),
        SignPhrase(83, "毕业", "毕业", "graduate.json", "学习教育", 72),
        SignPhrase(84, "请假", "请假", "ask_leave.json", "学习教育", 77),
        SignPhrase(85, "奖学金", "奖学金", "scholarship.json", "学习教育", 68),

        // ========== 家庭生活（86~96）共11条 ==========
        SignPhrase(86, "做饭", "做饭", "cook.json", "家庭生活", 85).apply {
            gestureTemplates = listOf(
                HandGestureClassifier.GestureTemplate(name = "握拳(做饭)", extendedFingers = listOf(false, false, false, false, false))
            )
            gestureHints = "握拳模拟握锅铲炒菜"
        },
        SignPhrase(87, "打扫", "打扫", "clean.json", "家庭生活", 80),
        SignPhrase(88, "洗衣服", "洗-衣服", "laundry.json", "家庭生活", 82),
        SignPhrase(89, "起床", "起床", "wake_up.json", "家庭生活", 87).apply {
            gestureTemplates = listOf(
                HandGestureClassifier.GestureTemplate(name = "打开手掌(起床)", extendedFingers = listOf(true, true, true, true, true))
            )
            gestureHints = "双手上举伸懒腰动作"
        },
        SignPhrase(90, "刷牙", "刷牙", "brush_teeth.json", "家庭生活", 88).apply {
            gestureTemplates = listOf(
                HandGestureClassifier.GestureTemplate(name = "食指指(刷牙)", extendedFingers = listOf(false, true, false, false, false))
            )
            gestureHints = "食指在嘴前模拟刷牙"
        },
        SignPhrase(91, "洗脸", "洗脸", "wash_face.json", "家庭生活", 86),
        SignPhrase(92, "洗澡", "洗澡", "shower.json", "家庭生活", 84),
        SignPhrase(93, "洗碗", "洗碗", "wash_dishes.json", "家庭生活", 76),
        SignPhrase(94, "倒垃圾", "倒-垃圾", "trash.json", "家庭生活", 74),
        SignPhrase(95, "锁门", "锁-门", "lock_door.json", "家庭生活", 72),
        SignPhrase(96, "晾衣服", "晾-衣服", "hang_clothes.json", "家庭生活", 71),

        // ========== 情绪表达（97~108）共12条 ==========
        SignPhrase(97, "开心", "开心", "happy.json", "情绪表达", 90).apply {
            gestureTemplates = listOf(
                HandGestureClassifier.GestureTemplate(name = "打开手掌(开心)", extendedFingers = listOf(true, true, true, true, true))
            )
            gestureHints = "张开手掌配合笑脸"
        },
        SignPhrase(98, "难过", "难过", "sad.json", "情绪表达", 82),
        SignPhrase(99, "生气", "生气", "angry.json", "情绪表达", 80).apply {
            gestureTemplates = listOf(
                HandGestureClassifier.GestureTemplate(name = "握拳(生气)", extendedFingers = listOf(false, false, false, false, false))
            )
            gestureHints = "握拳表示生气"
        },
        SignPhrase(100, "害怕", "害怕", "scared.json", "情绪表达", 78),
        SignPhrase(101, "着急", "着急", "anxious.json", "情绪表达", 76).apply {
            gestureTemplates = listOf(
                HandGestureClassifier.GestureTemplate(name = "打开手掌(着急)", extendedFingers = listOf(true, true, true, true, true))
            )
            gestureHints = "双手快速上下摆动表示着急"
        },
        SignPhrase(102, "担心", "担心", "worried.json", "情绪表达", 77),
        SignPhrase(103, "感谢", "感谢", "grateful.json", "情绪表达", 88).apply {
            gestureTemplates = listOf(
                HandGestureClassifier.GestureTemplate(name = "打开手掌(感谢)", extendedFingers = listOf(true, true, true, true, true))
            )
            gestureHints = "张开手掌在胸前画弧线"
        },
        SignPhrase(104, "抱歉", "抱歉", "apologize.json", "情绪表达", 83),
        SignPhrase(105, "惊讶", "惊讶", "surprised.json", "情绪表达", 72),
        SignPhrase(106, "无聊", "无聊", "bored.json", "情绪表达", 70),
        SignPhrase(107, "满意", "满意", "satisfied.json", "情绪表达", 74),
        SignPhrase(108, "骄傲", "骄傲", "proud.json", "情绪表达", 69),

        // ========== 社交互动（109~115）共7条 ==========
        SignPhrase(109, "认识你很高兴", "认识-你-很高兴", "nice_to_meet.json", "社交互动", 85).apply {
            gestureTemplates = listOf(
                HandGestureClassifier.GestureTemplate(name = "打开手掌(认识)", extendedFingers = listOf(true, true, true, true, true))
            )
            gestureHints = "张开手掌在胸前画圈表示高兴认识"
        },
        SignPhrase(110, "你好吗", "你好-吗", "how_are_you.json", "社交互动", 88),
        SignPhrase(111, "明天见", "明天-见", "see_you_tomorrow.json", "社交互动", 82),
        SignPhrase(112, "周末愉快", "周末-愉快", "happy_weekend.json", "社交互动", 78),
        SignPhrase(113, "生日快乐", "生日-快乐", "happy_birthday.json", "社交互动", 80),
        SignPhrase(114, "恭喜", "恭喜", "congratulations.json", "社交互动", 76),
        SignPhrase(115, "加油", "加油", "come_on.json", "社交互动", 84).apply {
            gestureTemplates = listOf(
                HandGestureClassifier.GestureTemplate(name = "握拳(加油)", extendedFingers = listOf(false, false, false, false, false))
            )
            gestureHints = "握拳向下挥动表示加油"
        },

        // ========== 自我介绍扩展（116~117）共2条 ==========
        SignPhrase(116, "我来自", "我-来自", "from.json", "自我介绍", 82),
        SignPhrase(117, "我今年...岁", "我-今年-岁", "my_age.json", "自我介绍", 80)
    )

    private val categories = mapOf(
        "日常需求" to listOf(1, 2, 3, 13, 14),
        "健康相关" to listOf(4, 6, 8, 9, 10, 11, 12),
        "紧急求助" to listOf(5, 7, 24, 25, 26),
        "礼貌用语" to listOf(15, 16, 17, 18, 19, 20, 23),
        "日常表达" to listOf(21, 22),
        "自我介绍" to listOf(27, 116, 117),
        "身份说明" to listOf(28, 29),
        "特殊需求" to listOf(30),
        "医院场景" to listOf(31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44),
        "药店场景" to listOf(45, 46, 47, 48, 49, 50, 51),
        "交通出行" to listOf(52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63),
        "购物消费" to listOf(64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75),
        "学习教育" to listOf(76, 77, 78, 79, 80, 81, 82, 83, 84, 85),
        "家庭生活" to listOf(86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96),
        "情绪表达" to listOf(97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108),
        "社交互动" to listOf(109, 110, 111, 112, 113, 114, 115)
    )

    private val keywordMap = mapOf(
        // 原有关键字映射
        "水" to listOf(1, 2),
        "渴" to listOf(2),
        "厕所" to listOf(3),
        "不舒服" to listOf(4),
        "帮助" to listOf(5),
        "药" to listOf(6, 36, 45),
        "家人" to listOf(7),
        "头" to listOf(8),
        "疼" to listOf(8, 9),
        "肚子" to listOf(9),
        "发烧" to listOf(10),
        "咳嗽" to listOf(11),
        "感冒" to listOf(12),
        "睡觉" to listOf(13),
        "饿" to listOf(14),
        "谢谢" to listOf(15),
        "你好" to listOf(16, 110),
        "再见" to listOf(17),
        "对不起" to listOf(18),
        "没关系" to listOf(19),
        "等" to listOf(20),
        "明白" to listOf(21, 22),
        "不懂" to listOf(22),
        "请问" to listOf(23),
        "救命" to listOf(24),
        "119" to listOf(25),
        "120" to listOf(26),
        "名字" to listOf(27),
        "聋" to listOf(28),
        "盲" to listOf(29),
        "翻译" to listOf(30),
        // 医院场景关键字映射
        "挂号" to listOf(31),
        "医生" to listOf(32),
        "拍片" to listOf(33),
        "抽血" to listOf(34),
        "化验" to listOf(35),
        "住院" to listOf(37),
        "出院" to listOf(38),
        "打针" to listOf(39),
        "体温" to listOf(40),
        "手术" to listOf(41),
        "病历" to listOf(42),
        "医保" to listOf(43),
        "急诊" to listOf(44),
        // 药店场景关键字映射
        "血压" to listOf(46),
        "血糖" to listOf(47),
        "口罩" to listOf(48),
        "体温计" to listOf(49),
        "创可贴" to listOf(50),
        "消毒" to listOf(51),
        // 交通出行关键字映射
        "打车" to listOf(52),
        "公交" to listOf(53),
        "地铁" to listOf(54),
        "飞机" to listOf(55),
        "火车" to listOf(56),
        "哪里" to listOf(57),
        "迷路" to listOf(63),
        "买票" to listOf(58),
        "导航" to listOf(59),
        "下车" to listOf(60),
        "车站" to listOf(61),
        "路口" to listOf(62),
        // 购物消费关键字映射
        "多少钱" to listOf(64),
        "太贵" to listOf(65),
        "便宜" to listOf(66),
        "扫码" to listOf(67),
        "微信" to listOf(68),
        "支付宝" to listOf(69),
        "银行卡" to listOf(70),
        "现金" to listOf(71),
        "购物袋" to listOf(72),
        "打折" to listOf(73),
        "退货" to listOf(74),
        "试穿" to listOf(75),
        // 学习教育关键字映射
        "上课" to listOf(76),
        "考试" to listOf(77),
        "作业" to listOf(78),
        "老师" to listOf(79),
        "同学" to listOf(80),
        "图书馆" to listOf(81),
        "教室" to listOf(82),
        "毕业" to listOf(83),
        "请假" to listOf(84),
        "奖学金" to listOf(85),
        // 家庭生活关键字映射
        "做饭" to listOf(86),
        "打扫" to listOf(87),
        "洗衣服" to listOf(88),
        "起床" to listOf(89),
        "刷牙" to listOf(90),
        "洗脸" to listOf(91),
        "洗澡" to listOf(92),
        "洗碗" to listOf(93),
        "垃圾" to listOf(94),
        "锁门" to listOf(95),
        "晾衣服" to listOf(96),
        // 情绪表达关键字映射
        "开心" to listOf(97),
        "难过" to listOf(98),
        "生气" to listOf(99),
        "害怕" to listOf(100),
        "着急" to listOf(101),
        "担心" to listOf(102),
        "感谢" to listOf(103),
        "抱歉" to listOf(104),
        "惊讶" to listOf(105),
        "无聊" to listOf(106),
        "满意" to listOf(107),
        "骄傲" to listOf(108),
        // 社交互动关键字映射
        "认识" to listOf(109),
        "高兴" to listOf(109),
        "明天" to listOf(111),
        "周末" to listOf(112),
        "生日" to listOf(113),
        "恭喜" to listOf(114),
        "加油" to listOf(115),
        "来自" to listOf(116),
        "岁" to listOf(117)
    )

    fun getAllPhrases(): List<SignPhrase> {
        return phrases.sortedByDescending { it.frequency }
    }

    fun getPhrasesByCategory(category: String): List<SignPhrase> {
        val ids = categories[category] ?: return emptyList()
        return phrases.filter { it.id in ids }.sortedByDescending { it.frequency }
    }

    fun getHighFrequencyPhrases(): List<SignPhrase> {
        return phrases.filter { it.frequency >= 90 }.sortedByDescending { it.frequency }
    }

    fun getEmergencyPhrases(): List<SignPhrase> {
        return phrases.filter { it.category == "紧急求助" }.sortedByDescending { it.frequency }
    }

    fun getPhraseById(id: Int): SignPhrase? {
        return phrases.find { it.id == id }
    }

    fun searchPhrases(query: String): List<SignPhrase> {
        return phrases.filter { phrase ->
            phrase.text.contains(query, ignoreCase = true) ||
            phrase.chineseSign.contains(query, ignoreCase = true) ||
            phrase.category.contains(query, ignoreCase = true)
        }
    }

    fun getCategories(): List<String> {
        return categories.keys.toList()
    }

    fun getRelatedPhrases(phraseId: Int): List<SignPhrase> {
        val currentPhrase = getPhraseById(phraseId) ?: return emptyList()
        val relatedIds = mutableSetOf<Int>()

        for ((keyword, ids) in keywordMap) {
            if (currentPhrase.text.contains(keyword) && phraseId in ids) {
                relatedIds.addAll(ids)
            }
        }

        relatedIds.remove(phraseId)
        return phrases.filter { it.id in relatedIds }.take(5)
    }

    fun getPhraseCount(): Int {
        return phrases.size
    }

    fun getCategoryCount(): Int {
        return categories.size
    }

    fun getMostUsedPhrases(limit: Int = 10): List<SignPhrase> {
        return phrases.sortedByDescending { it.frequency }.take(limit)
    }

    fun updatePhraseFrequency(phraseId: Int, newFrequency: Int) {
        val index = phrases.indexOfFirst { it.id == phraseId }
        if (index != -1) {
            phrases[index] = phrases[index].copy(frequency = newFrequency.coerceIn(0, 100))
        }
    }

    fun incrementPhraseUsage(phraseId: Int) {
        val index = phrases.indexOfFirst { it.id == phraseId }
        if (index != -1) {
            val currentFreq = phrases[index].frequency
            phrases[index] = phrases[index].copy(frequency = (currentFreq + 1).coerceAtMost(100))
        }
    }

    companion object {
        fun createDefaultDatabase(context: Context): SignLanguageDatabase {
            return SignLanguageDatabase(context)
        }
    }
}