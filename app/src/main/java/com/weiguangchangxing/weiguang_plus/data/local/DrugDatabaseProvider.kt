package com.weiguangchangxing.weiguang_plus.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import com.weiguangchangxing.weiguang_plus.data.local.entity.DrugAliasEntity
import com.weiguangchangxing.weiguang_plus.data.local.entity.DrugDetailEntity
import com.weiguangchangxing.weiguang_plus.data.local.entity.DrugMasterEntity
import com.weiguangchangxing.weiguang_plus.data.local.entity.DrugRuleEntity
import com.weiguangchangxing.weiguang_plus.data.local.entity.DrugSignMappingEntity
import com.weiguangchangxing.weiguang_plus.data.local.entity.UserProfileEntity
import com.weiguangchangxing.weiguang_plus.data.repository.DrugRepository
import com.weiguangchangxing.weiguang_plus.data.repository.LocalDrugRepository

// 本地药品数据库提供器：
// 负责三件事：
// 1. 创建并缓存 Room 数据库单例
// 2. 优先从 assets 离线库加载预制数据
// 3. 当离线库为空时，用代码种子数据兜底，保证演示环境可运行
object DrugDatabaseProvider {

    private const val DATABASE_NAME = "weiguang_drugs.db"
    private const val ASSET_DATABASE_PATH = "db/drugs.db"

    @Volatile
    private var database: AppDatabase? = null

    @Volatile
    private var repository: DrugRepository? = null

    @Volatile
    private var preferAssetDatabase: Boolean = true

    fun recoverDatabase(context: Context) {
        synchronized(this) {
            preferAssetDatabase = false
            resetDatabase(context.applicationContext)
        }
    }

    fun getDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            database ?: buildDatabase(context.applicationContext, preferAssetDatabase)
                .also { database = it }
        }
    }

    fun getRepository(context: Context): DrugRepository {
        return repository ?: synchronized(this) {
            repository ?: getDatabase(context).let { db ->
                LocalDrugRepository(
                    drugDao = db.drugDao(),
                    drugRuleDao = db.drugRuleDao(),
                    userProfileDao = db.userProfileDao()
                )
            }.also { repository = it }
        }
    }

    suspend fun ensureSeedData(context: Context) {
        val db = try {
            getDatabase(context)
        } catch (throwable: IllegalStateException) {
            if (throwable.message?.contains("Pre-packaged database has an invalid schema") == true) {
                synchronized(this) {
                    preferAssetDatabase = false
                    resetDatabase(context.applicationContext)
                }
                getDatabase(context)
            } else {
                throw throwable
            }
        }

        val masterCount = try {
            db.drugDao().countDrugMasters()
        } catch (throwable: IllegalStateException) {
            if (throwable.message?.contains("Pre-packaged database has an invalid schema") == true) {
                synchronized(this) {
                    preferAssetDatabase = false
                    resetDatabase(context.applicationContext)
                }
                getDatabase(context).drugDao().countDrugMasters()
            } else {
                throw throwable
            }
        }
        if (masterCount > 0) return

        db.withTransaction {
            if (db.drugDao().countDrugMasters() > 0) return@withTransaction

            db.drugDao().upsertDrugMasters(seedDrugMasters)
            db.drugDao().upsertDrugDetails(seedDrugDetails)
            db.drugDao().upsertDrugAliases(seedDrugAliases)
            db.drugDao().upsertDrugSignMappings(seedDrugSignMappings)
            db.drugRuleDao().upsertRules(seedDrugRules)
            db.userProfileDao().upsertProfile(seedUserProfile)
        }
    }

    private fun buildDatabase(context: Context, preferAsset: Boolean): AppDatabase {
        val builder = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
        if (preferAsset) {
            builder.createFromAsset(ASSET_DATABASE_PATH)
        }
        val db = builder.build()
        try {
            db.openHelper.writableDatabase
        } catch (throwable: IllegalStateException) {
            val message = throwable.message ?: ""
            if (preferAsset && message.contains("Pre-packaged database has an invalid schema")) {
                db.close()
                preferAssetDatabase = false
                context.deleteDatabase(DATABASE_NAME)
                val fallbackDb = buildDatabase(context, preferAsset = false)
                fallbackDb.openHelper.writableDatabase
                return fallbackDb
            }
            throw throwable
        }
        return db
    }

    private fun resetDatabase(context: Context) {
        database?.close()
        database = null
        repository = null
        context.deleteDatabase(DATABASE_NAME)
    }

    private val seedDrugMasters = listOf(
        // ============================================================
        // 1-3. 原有3种种子药品（保留）
        // ============================================================
        DrugMasterEntity(
            drugId = 1001L,
            genericName = "布洛芬缓释胶囊",
            tradeName = "芬必得",
            approvalNo = "国药准字H10900089",
            manufacturer = "中美天津史克制药有限公司",
            dosageForm = "胶囊",
            specification = "0.3g",
            categoryName = "解热镇痛药",
            pinyinKey = "buluofen huanshi jiaonang",
            initialsKey = "blfhsjn",
            searchTokens = "布洛芬|芬必得|止痛药|退烧药",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 1002L,
            genericName = "阿莫西林胶囊",
            tradeName = "阿莫仙",
            approvalNo = "国药准字H44021351",
            manufacturer = "珠海联邦制药股份有限公司中山分公司",
            dosageForm = "胶囊",
            specification = "0.25g",
            categoryName = "抗生素",
            pinyinKey = "amoxilin jiaonang",
            initialsKey = "amxljn",
            searchTokens = "阿莫西林|阿莫仙|消炎药",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 1003L,
            genericName = "盐酸二甲双胍片",
            tradeName = "格华止",
            approvalNo = "国药准字H20023370",
            manufacturer = "中美上海施贵宝制药有限公司",
            dosageForm = "片剂",
            specification = "0.5g",
            categoryName = "降糖药",
            pinyinKey = "yansuan erjia shuanggua pian",
            initialsKey = "ysejsgp",
            searchTokens = "二甲双胍|格华止|糖尿病|降糖药",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        // ============================================================
        // 解热镇痛药 & 感冒用药（1004-1006）
        // ============================================================
        DrugMasterEntity(
            drugId = 1004L,
            genericName = "对乙酰氨基酚片",
            tradeName = "泰诺林",
            approvalNo = "国药准字H31022832",
            manufacturer = "上海强生制药有限公司",
            dosageForm = "片剂",
            specification = "0.5g",
            categoryName = "解热镇痛药",
            pinyinKey = "duiyixian anjifen pian",
            initialsKey = "dyxajfp",
            searchTokens = "对乙酰氨基酚|泰诺林|扑热息痛|退烧药|止痛药",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 1005L,
            genericName = "双氯芬酸钠缓释片",
            tradeName = "扶他林",
            approvalNo = "国药准字H10980297",
            manufacturer = "北京诺华制药有限公司",
            dosageForm = "缓释片",
            specification = "75mg",
            categoryName = "解热镇痛药",
            pinyinKey = "shuanglv fensuan na huanshi pian",
            initialsKey = "slfsnhsp",
            searchTokens = "双氯芬酸钠|扶他林|止痛药|消炎镇痛",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 1006L,
            genericName = "感冒灵颗粒",
            tradeName = "感冒灵",
            approvalNo = "国药准字Z44021940",
            manufacturer = "华润三九医药股份有限公司",
            dosageForm = "颗粒",
            specification = "10g*9袋",
            categoryName = "感冒用药",
            pinyinKey = "ganmao ling keli",
            initialsKey = "gmlkl",
            searchTokens = "感冒灵|感冒灵颗粒|三九感冒灵|感冒药",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        // ============================================================
        // 抗生素 & 抗菌药（1007-1010）
        // ============================================================
        DrugMasterEntity(
            drugId = 1007L,
            genericName = "头孢克肟胶囊",
            tradeName = "世福素",
            approvalNo = "国药准字H20020542",
            manufacturer = "广州白云山制药股份有限公司",
            dosageForm = "胶囊",
            specification = "0.1g",
            categoryName = "抗生素",
            pinyinKey = "toubao kewo jiaonang",
            initialsKey = "tbkwjn",
            searchTokens = "头孢克肟|世福素|头孢|抗生素|消炎药",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 1008L,
            genericName = "阿奇霉素胶囊",
            tradeName = "希舒美",
            approvalNo = "国药准字H10960167",
            manufacturer = "辉瑞制药有限公司",
            dosageForm = "胶囊",
            specification = "0.25g",
            categoryName = "抗生素",
            pinyinKey = "aqimeisu jiaonang",
            initialsKey = "aqmsjn",
            searchTokens = "阿奇霉素|希舒美|抗生素|消炎药|大环内酯",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 1009L,
            genericName = "头孢拉定胶囊",
            tradeName = "泛捷复",
            approvalNo = "国药准字H20023306",
            manufacturer = "中美上海施贵宝制药有限公司",
            dosageForm = "胶囊",
            specification = "0.25g",
            categoryName = "抗生素",
            pinyinKey = "toubao lading jiaonang",
            initialsKey = "tbldjn",
            searchTokens = "头孢拉定|泛捷复|头孢|抗生素|消炎药",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 1010L,
            genericName = "左氧氟沙星片",
            tradeName = "可乐必妥",
            approvalNo = "国药准字H20010125",
            manufacturer = "第一三共制药(北京)有限公司",
            dosageForm = "片剂",
            specification = "0.1g",
            categoryName = "抗生素",
            pinyinKey = "zuoyang fushaxing pian",
            initialsKey = "zyfsxp",
            searchTokens = "左氧氟沙星|可乐必妥|喹诺酮|抗生素|消炎药",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        // ============================================================
        // 降压药 - ARB类（1011-1054 interval, 具体见下）
        // 氯沙坦钾（1011）
        // ============================================================
        DrugMasterEntity(
            drugId = 1011L,
            genericName = "氯沙坦钾片",
            tradeName = "科素亚",
            approvalNo = "国药准字H20000371",
            manufacturer = "杭州默沙东制药有限公司",
            dosageForm = "片剂",
            specification = "50mg",
            categoryName = "降压药",
            pinyinKey = "lvshatan jia pian",
            initialsKey = "lstjp",
            searchTokens = "氯沙坦|科素亚|氯沙坦钾|降压药|高血压",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 1012L,
            genericName = "硝苯地平控释片",
            tradeName = "拜新同",
            approvalNo = "国药准字J20180033",
            manufacturer = "Bayer Pharma AG",
            dosageForm = "控释片",
            specification = "30mg",
            categoryName = "降压药",
            pinyinKey = "xiaobendiping kongshi pian",
            initialsKey = "xbdpksp",
            searchTokens = "硝苯地平|拜新同|硝苯地平控释片|降压药|高血压|心痛定",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        // ============================================================
        // 降压药 - CCB类（1013-1018）
        // ============================================================
        DrugMasterEntity(
            drugId = 1013L,
            genericName = "苯磺酸氨氯地平片",
            tradeName = "络活喜",
            approvalNo = "国药准字H10950224",
            manufacturer = "辉瑞制药有限公司",
            dosageForm = "片剂",
            specification = "5mg",
            categoryName = "降压药",
            pinyinKey = "benhuangsuan anlvdiping pian",
            initialsKey = "bhsaldp",
            searchTokens = "氨氯地平|络活喜|苯磺酸氨氯地平|降压药|高血压",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 1014L,
            genericName = "厄贝沙坦片",
            tradeName = "安博维",
            approvalNo = "国药准字H20030016",
            manufacturer = "赛诺菲(杭州)制药有限公司",
            dosageForm = "片剂",
            specification = "150mg",
            categoryName = "降压药",
            pinyinKey = "ebeishatan pian",
            initialsKey = "ebstp",
            searchTokens = "厄贝沙坦|安博维|降压药|高血压|ARB",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 1015L,
            genericName = "琥珀酸美托洛尔缓释片",
            tradeName = "倍他乐克",
            approvalNo = "国药准字J20150044",
            manufacturer = "AstraZeneca AB",
            dosageForm = "缓释片",
            specification = "47.5mg",
            categoryName = "降压药",
            pinyinKey = "huposuan meituoluoer huanshi pian",
            initialsKey = "hpsmtlrhsp",
            searchTokens = "美托洛尔|倍他乐克|琥珀酸美托洛尔|降压药|心律失常",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 1016L,
            genericName = "卡托普利片",
            tradeName = "开博通",
            approvalNo = "国药准字H31021317",
            manufacturer = "中美上海施贵宝制药有限公司",
            dosageForm = "片剂",
            specification = "25mg",
            categoryName = "降压药",
            pinyinKey = "katuopuli pian",
            initialsKey = "ktplp",
            searchTokens = "卡托普利|开博通|降压药|高血压|ACEI",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 1017L,
            genericName = "缬沙坦胶囊",
            tradeName = "代文",
            approvalNo = "国药准字H20040217",
            manufacturer = "北京诺华制药有限公司",
            dosageForm = "胶囊",
            specification = "80mg",
            categoryName = "降压药",
            pinyinKey = "xieshatan jiaonang",
            initialsKey = "xstjn",
            searchTokens = "缬沙坦|代文|降压药|高血压|ARB",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 1018L,
            genericName = "非洛地平缓释片",
            tradeName = "波依定",
            approvalNo = "国药准字H20030415",
            manufacturer = "AstraZeneca AB",
            dosageForm = "缓释片",
            specification = "5mg",
            categoryName = "降压药",
            pinyinKey = "feiluodiping huanshi pian",
            initialsKey = "fldphsp",
            searchTokens = "非洛地平|波依定|非洛地平缓释片|降压药|高血压",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        // ============================================================
        // 降压药 - ARB类续（1019-1021）
        // ============================================================
        DrugMasterEntity(
            drugId = 1019L,
            genericName = "坎地沙坦酯片",
            tradeName = "必洛斯",
            approvalNo = "国药准字H20030319",
            manufacturer = "天津武田药品有限公司",
            dosageForm = "片剂",
            specification = "8mg",
            categoryName = "降压药",
            pinyinKey = "kandishatan zhi pian",
            initialsKey = "kdstzp",
            searchTokens = "坎地沙坦|必洛斯|坎地沙坦酯|降压药|高血压",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 1020L,
            genericName = "替米沙坦片",
            tradeName = "美卡素",
            approvalNo = "国药准字H20060488",
            manufacturer = "上海勃林格殷格翰药业有限公司",
            dosageForm = "片剂",
            specification = "80mg",
            categoryName = "降压药",
            pinyinKey = "timishatan pian",
            initialsKey = "tmstp",
            searchTokens = "替米沙坦|美卡素|降压药|高血压|ARB",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 1021L,
            genericName = "奥美沙坦酯片",
            tradeName = "傲坦",
            approvalNo = "国药准字H20060371",
            manufacturer = "北京第一三共制药有限公司",
            dosageForm = "片剂",
            specification = "20mg",
            categoryName = "降压药",
            pinyinKey = "aomeishatan zhi pian",
            initialsKey = "amstzp",
            searchTokens = "奥美沙坦|傲坦|奥美沙坦酯|降压药|高血压",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        // ============================================================
        // β受体阻滞剂（1022-1023）
        // ============================================================
        DrugMasterEntity(
            drugId = 1022L,
            genericName = "富马酸比索洛尔片",
            tradeName = "康忻",
            approvalNo = "国药准字J20170042",
            manufacturer = "Merck KGaA",
            dosageForm = "片剂",
            specification = "5mg",
            categoryName = "降压药",
            pinyinKey = "fumasuan bisuoluoer pian",
            initialsKey = "fmsbslep",
            searchTokens = "比索洛尔|康忻|富马酸比索洛尔|降压药|心律失常",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 1023L,
            genericName = "盐酸阿罗洛尔片",
            tradeName = "阿尔马尔",
            approvalNo = "国药准字H20010435",
            manufacturer = "住友制药(苏州)有限公司",
            dosageForm = "片剂",
            specification = "10mg",
            categoryName = "降压药",
            pinyinKey = "yansuan aluoluoer pian",
            initialsKey = "ysalep",
            searchTokens = "阿罗洛尔|阿尔马尔|降压药|心律失常|αβ阻滞剂",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        // ============================================================
        // 利尿药（1024-1026）
        // ============================================================
        DrugMasterEntity(
            drugId = 1024L,
            genericName = "氢氯噻嗪片",
            tradeName = "双克",
            approvalNo = "国药准字H31021235",
            manufacturer = "上海信谊药厂有限公司",
            dosageForm = "片剂",
            specification = "25mg",
            categoryName = "降压药",
            pinyinKey = "qinglu saiqin pian",
            initialsKey = "qlsqp",
            searchTokens = "氢氯噻嗪|双克|氢氯噻嗪片|降压药|利尿药",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 1025L,
            genericName = "呋塞米片",
            tradeName = "速尿",
            approvalNo = "国药准字H31021074",
            manufacturer = "上海朝晖药业有限公司",
            dosageForm = "片剂",
            specification = "20mg",
            categoryName = "利尿药",
            pinyinKey = "fusaimi pian",
            initialsKey = "fsmp",
            searchTokens = "呋塞米|速尿|利尿药|水肿",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 1026L,
            genericName = "螺内酯片",
            tradeName = "安体舒通",
            approvalNo = "国药准字H31021273",
            manufacturer = "上海信谊药厂有限公司",
            dosageForm = "片剂",
            specification = "20mg",
            categoryName = "利尿药",
            pinyinKey = "luoneizhi pian",
            initialsKey = "lnzp",
            searchTokens = "螺内酯|安体舒通|利尿药|保钾利尿|高血压",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        // ============================================================
        // 调脂药 / 降脂药（1027-1030）
        // ============================================================
        DrugMasterEntity(
            drugId = 1027L,
            genericName = "辛伐他汀片",
            tradeName = "舒降之",
            approvalNo = "国药准字H19990366",
            manufacturer = "杭州默沙东制药有限公司",
            dosageForm = "片剂",
            specification = "20mg",
            categoryName = "调脂药",
            pinyinKey = "xinfatating pian",
            initialsKey = "xfttp",
            searchTokens = "辛伐他汀|舒降之|降脂药|他汀|高血脂",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 1028L,
            genericName = "阿托伐他汀钙片",
            tradeName = "立普妥",
            approvalNo = "国药准字H20051408",
            manufacturer = "辉瑞制药有限公司",
            dosageForm = "片剂",
            specification = "20mg",
            categoryName = "调脂药",
            pinyinKey = "atuofatating gai pian",
            initialsKey = "atfttgp",
            searchTokens = "阿托伐他汀|立普妥|降脂药|他汀|高血脂|胆固醇",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 1029L,
            genericName = "瑞舒伐他汀钙片",
            tradeName = "可定",
            approvalNo = "国药准字J20160008",
            manufacturer = "AstraZeneca UK Limited",
            dosageForm = "片剂",
            specification = "10mg",
            categoryName = "调脂药",
            pinyinKey = "ruishufatating gai pian",
            initialsKey = "rsfttgp",
            searchTokens = "瑞舒伐他汀|可定|降脂药|他汀|高血脂",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 1030L,
            genericName = "非诺贝特胶囊",
            tradeName = "力平之",
            approvalNo = "国药准字H20031203",
            manufacturer = "法国利博福尼制药公司",
            dosageForm = "胶囊",
            specification = "200mg",
            categoryName = "调脂药",
            pinyinKey = "feinuobeite jiaonang",
            initialsKey = "fnbtjn",
            searchTokens = "非诺贝特|力平之|降脂药|贝特|高甘油三酯",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        // ============================================================
        // 消化系统用药（1031-1036）
        // ============================================================
        DrugMasterEntity(
            drugId = 1031L,
            genericName = "奥美拉唑肠溶胶囊",
            tradeName = "洛赛克",
            approvalNo = "国药准字H20030477",
            manufacturer = "AstraZeneca AB",
            dosageForm = "肠溶胶囊",
            specification = "20mg",
            categoryName = "消化系统药",
            pinyinKey = "aomeilazuo changrong jiaonang",
            initialsKey = "amlzcrjn",
            searchTokens = "奥美拉唑|洛赛克|胃药|抑酸药|PPI|胃溃疡",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 1032L,
            genericName = "雷贝拉唑钠肠溶片",
            tradeName = "济诺",
            approvalNo = "国药准字H20040715",
            manufacturer = "江苏豪森药业集团有限公司",
            dosageForm = "肠溶片",
            specification = "10mg",
            categoryName = "消化系统药",
            pinyinKey = "leibeilazuona changrong pian",
            initialsKey = "lblzncrp",
            searchTokens = "雷贝拉唑|济诺|雷贝拉唑钠|胃药|抑酸药|PPI",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 1033L,
            genericName = "泮托拉唑钠肠溶胶囊",
            tradeName = "泰美尼克",
            approvalNo = "国药准字H20010032",
            manufacturer = "沈阳东宇药业有限公司",
            dosageForm = "肠溶胶囊",
            specification = "40mg",
            categoryName = "消化系统药",
            pinyinKey = "pantuolazuona changrong jiaonang",
            initialsKey = "ptlzncrjn",
            searchTokens = "泮托拉唑|泰美尼克|泮托拉唑钠|胃药|抑酸药|PPI",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 1034L,
            genericName = "多潘立酮片",
            tradeName = "吗丁啉",
            approvalNo = "国药准字H20010245",
            manufacturer = "西安杨森制药有限公司",
            dosageForm = "片剂",
            specification = "10mg",
            categoryName = "消化系统药",
            pinyinKey = "duopanlitong pian",
            initialsKey = "dpltp",
            searchTokens = "多潘立酮|吗丁啉|胃动力|促胃动力|消化不良",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 1035L,
            genericName = "莫沙必利片",
            tradeName = "加斯清",
            approvalNo = "国药准字H20090156",
            manufacturer = "住友制药(苏州)有限公司",
            dosageForm = "片剂",
            specification = "5mg",
            categoryName = "消化系统药",
            pinyinKey = "moshabili pian",
            initialsKey = "msblp",
            searchTokens = "莫沙必利|加斯清|胃动力|促胃动力|消化不良",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 1036L,
            genericName = "铝碳酸镁咀嚼片",
            tradeName = "达喜",
            approvalNo = "国药准字H20013405",
            manufacturer = "拜耳医药保健有限公司",
            dosageForm = "咀嚼片",
            specification = "0.5g",
            categoryName = "消化系统药",
            pinyinKey = "lv tansuanmei jujue pian",
            initialsKey = "ltsmjjp",
            searchTokens = "铝碳酸镁|达喜|胃药|抗酸药|胃痛|胃酸",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        // ============================================================
        // 心脑血管用药 - 中成药（1037-1039）
        // ============================================================
        DrugMasterEntity(
            drugId = 1037L,
            genericName = "复方丹参滴丸",
            tradeName = "复方丹参滴丸",
            approvalNo = "国药准字Z10950111",
            manufacturer = "天津天士力医药集团股份有限公司",
            dosageForm = "滴丸",
            specification = "27mg*180丸",
            categoryName = "心脑血管药",
            pinyinKey = "fufang danshen diwan",
            initialsKey = "ffdsdw",
            searchTokens = "复方丹参滴丸|丹参滴丸|冠心病|心绞痛|心血管",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 1038L,
            genericName = "速效救心丸",
            tradeName = "速效救心丸",
            approvalNo = "国药准字Z12020025",
            manufacturer = "天津中新药业集团股份有限公司第六中药厂",
            dosageForm = "滴丸",
            specification = "40mg*60丸",
            categoryName = "心脑血管药",
            pinyinKey = "suxiao jiuxin wan",
            initialsKey = "sxjxw",
            searchTokens = "速效救心丸|救心丸|心绞痛|冠心病|胸闷",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 1039L,
            genericName = "银杏叶片",
            tradeName = "金纳多",
            approvalNo = "国药准字H20070226",
            manufacturer = "德国威玛舒培博士药厂",
            dosageForm = "片剂",
            specification = "40mg",
            categoryName = "心脑血管药",
            pinyinKey = "yinxingye pian",
            initialsKey = "yxyyp",
            searchTokens = "银杏叶|金纳多|银杏叶片|脑循环|记忆力|心血管",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        // ============================================================
        // 抗血小板 / 抗凝药（1040-1042）
        // ============================================================
        DrugMasterEntity(
            drugId = 1040L,
            genericName = "硫酸氢氯吡格雷片",
            tradeName = "波立维",
            approvalNo = "国药准字J20130083",
            manufacturer = "赛诺菲(杭州)制药有限公司",
            dosageForm = "片剂",
            specification = "75mg",
            categoryName = "抗血小板药",
            pinyinKey = "liusuan qing lvbigelaipian",
            initialsKey = "lsqlbg lp",
            searchTokens = "氯吡格雷|波立维|硫酸氢氯吡格雷|抗血小板|泰嘉|冠心病",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 1041L,
            genericName = "阿司匹林肠溶片",
            tradeName = "拜阿司匹灵",
            approvalNo = "国药准字J20130078",
            manufacturer = "Bayer S.p.A.",
            dosageForm = "肠溶片",
            specification = "100mg",
            categoryName = "抗血小板药",
            pinyinKey = "asipilin changrong pian",
            initialsKey = "asplcrp",
            searchTokens = "阿司匹林|拜阿司匹灵|阿司匹林肠溶片|抗血小板|心脑血管",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 1042L,
            genericName = "华法林钠片",
            tradeName = "华法林",
            approvalNo = "国药准字H20171099",
            manufacturer = "上海信谊药厂有限公司",
            dosageForm = "片剂",
            specification = "2.5mg",
            categoryName = "抗凝药",
            pinyinKey = "huafalin na pian",
            initialsKey = "hflnp",
            searchTokens = "华法林|华法林钠|抗凝药|房颤|血栓",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        // ============================================================
        // 强心苷（1043）
        // ============================================================
        DrugMasterEntity(
            drugId = 1043L,
            genericName = "地高辛片",
            tradeName = "地高辛",
            approvalNo = "国药准字H31020678",
            manufacturer = "上海信谊药厂有限公司",
            dosageForm = "片剂",
            specification = "0.25mg",
            categoryName = "强心药",
            pinyinKey = "digaoxin pian",
            initialsKey = "dgxp",
            searchTokens = "地高辛|强心药|心力衰竭|房颤",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        // ============================================================
        // 降糖药 - 口服（1044-1051）
        // ============================================================
        DrugMasterEntity(
            drugId = 1044L,
            genericName = "格列美脲片",
            tradeName = "亚莫利",
            approvalNo = "国药准字H20010565",
            manufacturer = "赛诺菲(北京)制药有限公司",
            dosageForm = "片剂",
            specification = "2mg",
            categoryName = "降糖药",
            pinyinKey = "geliemeiniao pian",
            initialsKey = "glmnp",
            searchTokens = "格列美脲|亚莫利|降糖药|糖尿病|磺脲类",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 1045L,
            genericName = "利拉鲁肽注射液",
            tradeName = "诺和力",
            approvalNo = "国药准字J20110026",
            manufacturer = "Novo Nordisk A/S",
            dosageForm = "注射液",
            specification = "18mg/3ml",
            categoryName = "降糖药",
            pinyinKey = "lilalu tai zhusheye",
            initialsKey = "llltzsy",
            searchTokens = "利拉鲁肽|诺和力|降糖药|GLP-1|糖尿病",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 1046L,
            genericName = "达格列净片",
            tradeName = "安达唐",
            approvalNo = "国药准字J20170040",
            manufacturer = "AstraZeneca AB",
            dosageForm = "片剂",
            specification = "10mg",
            categoryName = "降糖药",
            pinyinKey = "dageliejing pian",
            initialsKey = "dgljp",
            searchTokens = "达格列净|安达唐|安达康|降糖药|SGLT2|糖尿病",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 1047L,
            genericName = "恩格列净片",
            tradeName = "欧唐静",
            approvalNo = "国药准字J20171073",
            manufacturer = "Boehringer Ingelheim International GmbH",
            dosageForm = "片剂",
            specification = "10mg",
            categoryName = "降糖药",
            pinyinKey = "engeliejing pian",
            initialsKey = "egljp",
            searchTokens = "恩格列净|欧唐静|降糖药|SGLT2|糖尿病",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 1048L,
            genericName = "西格列汀片",
            tradeName = "捷诺维",
            approvalNo = "国药准字J20140095",
            manufacturer = "Merck Sharp & Dohme Ltd.",
            dosageForm = "片剂",
            specification = "100mg",
            categoryName = "降糖药",
            pinyinKey = "xigelieting pian",
            initialsKey = "xgltp",
            searchTokens = "西格列汀|捷诺维|降糖药|DPP-4|糖尿病",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 1049L,
            genericName = "沙格列汀片",
            tradeName = "安立泽",
            approvalNo = "国药准字J20110081",
            manufacturer = "AstraZeneca AB",
            dosageForm = "片剂",
            specification = "5mg",
            categoryName = "降糖药",
            pinyinKey = "shagelieting pian",
            initialsKey = "sgltp",
            searchTokens = "沙格列汀|安立泽|降糖药|DPP-4|糖尿病",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        // ============================================================
        // 神经营养药（1050-1051）
        // ============================================================
        DrugMasterEntity(
            drugId = 1050L,
            genericName = "甲钴胺片",
            tradeName = "弥可保",
            approvalNo = "国药准字H20030812",
            manufacturer = "卫材(中国)药业有限公司",
            dosageForm = "片剂",
            specification = "0.5mg",
            categoryName = "神经营养药",
            pinyinKey = "jiagu an pian",
            initialsKey = "jgap",
            searchTokens = "甲钴胺|弥可保|维生素B12|神经|周围神经病变",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 1051L,
            genericName = "α-硫辛酸胶囊",
            tradeName = "硫辛酸",
            approvalNo = "国药准字H20051992",
            manufacturer = "山东健康药业有限公司",
            dosageForm = "胶囊",
            specification = "0.2g",
            categoryName = "神经营养药",
            pinyinKey = "liuxinsuan jiaonang",
            initialsKey = "lxsjn",
            searchTokens = "硫辛酸|α-硫辛酸|抗氧化|糖尿病神经病变",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        // ============================================================
        // 营养补充剂（1052-1053）
        // ============================================================
        DrugMasterEntity(
            drugId = 1052L,
            genericName = "碳酸钙D3片",
            tradeName = "钙尔奇",
            approvalNo = "国药准字H10950029",
            manufacturer = "辉瑞制药有限公司",
            dosageForm = "片剂",
            specification = "600mg",
            categoryName = "营养补充剂",
            pinyinKey = "tansuangai D3 pian",
            initialsKey = "tsgd3p",
            searchTokens = "碳酸钙D3|钙尔奇|钙片|补钙|维生素D|骨质疏松",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 1053L,
            genericName = "维生素D滴剂",
            tradeName = "维生素D",
            approvalNo = "国药准字H20113012",
            manufacturer = "青岛双鲸药业股份有限公司",
            dosageForm = "滴剂",
            specification = "400IU",
            categoryName = "营养补充剂",
            pinyinKey = "weishengsu D diji",
            initialsKey = "wsddj",
            searchTokens = "维生素D|维生素D滴剂|补钙|佝偻病|骨质疏松",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        // ============================================================
        // 其他（1054-1056）
        // ============================================================
        DrugMasterEntity(
            drugId = 1054L,
            genericName = "吲达帕胺片",
            tradeName = "纳催离",
            approvalNo = "国药准字H20032181",
            manufacturer = "天津施维雅制药有限公司",
            dosageForm = "片剂",
            specification = "2.5mg",
            categoryName = "降压药",
            pinyinKey = "yindapaan pian",
            initialsKey = "ydpap",
            searchTokens = "吲达帕胺|纳催离|降压药|利尿降压",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 1055L,
            genericName = "尼莫地平片",
            tradeName = "尼莫同",
            approvalNo = "国药准字H20003032",
            manufacturer = "拜耳医药保健有限公司",
            dosageForm = "片剂",
            specification = "30mg",
            categoryName = "心脑血管药",
            pinyinKey = "nimodiping pian",
            initialsKey = "nmdpp",
            searchTokens = "尼莫地平|尼莫同|脑血管|蛛网膜下腔出血",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 1056L,
            genericName = "长春西汀片",
            tradeName = "长春西汀",
            approvalNo = "国药准字H20010467",
            manufacturer = "东北制药集团沈阳第一制药有限公司",
            dosageForm = "片剂",
            specification = "5mg",
            categoryName = "心脑血管药",
            pinyinKey = "changchun xiting pian",
            initialsKey = "ccxtp",
            searchTokens = "长春西汀|脑循环|脑血管|认知障碍",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        // ============================================================
        // 2001-2005. 儿科用药（新增扩充）
        // ============================================================
        DrugMasterEntity(
            drugId = 2001L,
            genericName = "小儿氨酚黄那敏颗粒",
            tradeName = "护彤",
            approvalNo = "国药准字H23022518",
            manufacturer = "哈药集团制药六厂",
            dosageForm = "颗粒",
            specification = "6g*10袋",
            categoryName = "儿科用药",
            pinyinKey = "xiaoer anfen huang namin keli",
            initialsKey = "xeafhnmkl",
            searchTokens = "小儿氨酚黄那敏|护彤|小儿感冒药|儿童感冒|退烧药",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 2002L,
            genericName = "小儿止咳糖浆",
            tradeName = "小儿止咳糖浆",
            approvalNo = "国药准字Z20033107",
            manufacturer = "华润三九医药股份有限公司",
            dosageForm = "糖浆剂",
            specification = "100ml",
            categoryName = "儿科用药",
            pinyinKey = "xiaoer zhike tangjiang",
            initialsKey = "xezktj",
            searchTokens = "小儿止咳糖浆|儿童止咳|小儿止咳|止咳药",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 2003L,
            genericName = "小儿蒙脱石散",
            tradeName = "思密达",
            approvalNo = "国药准字H20000690",
            manufacturer = "博福-益普生(天津)制药有限公司",
            dosageForm = "散剂",
            specification = "3g*10袋",
            categoryName = "儿科用药",
            pinyinKey = "xiaoer mengtuoshi san",
            initialsKey = "xemts",
            searchTokens = "蒙脱石散|思密达|小儿止泻|儿童腹泻|止泻药",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 2004L,
            genericName = "小儿布洛芬混悬液",
            tradeName = "美林",
            approvalNo = "国药准字H19991011",
            manufacturer = "上海强生制药有限公司",
            dosageForm = "混悬液",
            specification = "100ml:2g",
            categoryName = "儿科用药",
            pinyinKey = "xiaoer buluofen hunxuanye",
            initialsKey = "xeblfhxy",
            searchTokens = "布洛芬混悬液|美林|小儿退烧|儿童退热|婴幼儿退烧",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 2005L,
            genericName = "小儿阿莫西林颗粒",
            tradeName = "再林",
            approvalNo = "国药准字H46020605",
            manufacturer = "先声药业有限公司",
            dosageForm = "颗粒",
            specification = "0.125g*12袋",
            categoryName = "儿科用药",
            pinyinKey = "xiaoer amoxilin keli",
            initialsKey = "xeamxlkl",
            searchTokens = "阿莫西林颗粒|再林|小儿消炎|儿童抗生素|消炎药",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        // ============================================================
        // 2006-2010. 妇科用药（新增扩充）
        // ============================================================
        DrugMasterEntity(
            drugId = 2006L,
            genericName = "乌鸡白凤丸",
            tradeName = "乌鸡白凤丸",
            approvalNo = "国药准字Z33020050",
            manufacturer = "北京同仁堂股份有限公司同仁堂制药厂",
            dosageForm = "丸剂",
            specification = "9g*10丸",
            categoryName = "妇科用药",
            pinyinKey = "wuji baifeng wan",
            initialsKey = "wjbfw",
            searchTokens = "乌鸡白凤丸|妇科|调经|补气血|月经不调",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 2007L,
            genericName = "益母草颗粒",
            tradeName = "益母草颗粒",
            approvalNo = "国药准字Z20023358",
            manufacturer = "广州白云山和记黄埔中药有限公司",
            dosageForm = "颗粒",
            specification = "15g*10袋",
            categoryName = "妇科用药",
            pinyinKey = "yimucao keli",
            initialsKey = "ymckl",
            searchTokens = "益母草|益母草颗粒|妇科|调经|产后调理",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 2008L,
            genericName = "妇科千金片",
            tradeName = "千金片",
            approvalNo = "国药准字Z43020077",
            manufacturer = "株洲千金药业股份有限公司",
            dosageForm = "片剂",
            specification = "0.32g*72片",
            categoryName = "妇科用药",
            pinyinKey = "fuke qianjin pian",
            initialsKey = "fkqjp",
            searchTokens = "妇科千金片|千金片|妇科炎症|盆腔炎|宫颈炎",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 2009L,
            genericName = "甲硝唑栓",
            tradeName = "甲硝唑栓",
            approvalNo = "国药准字H20053921",
            manufacturer = "上海现代制药股份有限公司",
            dosageForm = "栓剂",
            specification = "0.5g*10枚",
            categoryName = "妇科用药",
            pinyinKey = "jiaxiao zuo shuan",
            initialsKey = "jxzs",
            searchTokens = "甲硝唑栓|妇科|阴道炎|滴虫性阴道炎|抗菌栓剂",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 2010L,
            genericName = "克霉唑栓",
            tradeName = "克霉唑栓",
            approvalNo = "国药准字H20065221",
            manufacturer = "上海中华药业有限公司",
            dosageForm = "栓剂",
            specification = "0.15g*7枚",
            categoryName = "妇科用药",
            pinyinKey = "kemeizuo shuan",
            initialsKey = "kmzs",
            searchTokens = "克霉唑栓|妇科|霉菌性阴道炎|念珠菌|抗真菌栓剂",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        // ============================================================
        // 2011-2015. 皮肤科用药（新增扩充）
        // ============================================================
        DrugMasterEntity(
            drugId = 2011L,
            genericName = "复方醋酸地塞米松乳膏",
            tradeName = "皮炎平",
            approvalNo = "国药准字H44024363",
            manufacturer = "广州白云山制药股份有限公司",
            dosageForm = "乳膏剂",
            specification = "20g:15mg",
            categoryName = "皮肤科用药",
            pinyinKey = "fufang cusuan disaimisong rugao",
            initialsKey = "ffcsdsmsrg",
            searchTokens = "皮炎平|复方醋酸地塞米松|皮炎|湿疹|皮肤瘙痒",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 2012L,
            genericName = "曲安奈德益康唑乳膏",
            tradeName = "派瑞松",
            approvalNo = "国药准字H20000425",
            manufacturer = "西安杨森制药有限公司",
            dosageForm = "乳膏剂",
            specification = "15g",
            categoryName = "皮肤科用药",
            pinyinKey = "quannai de yikangzuo rugao",
            initialsKey = "qndykzrg",
            searchTokens = "派瑞松|曲安奈德益康唑|皮炎|湿疹|真菌感染|脚气",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 2013L,
            genericName = "莫匹罗星软膏",
            tradeName = "百多邦",
            approvalNo = "国药准字H10930064",
            manufacturer = "中美天津史克制药有限公司",
            dosageForm = "软膏剂",
            specification = "5g:0.1g",
            categoryName = "皮肤科用药",
            pinyinKey = "mopiluoxing ruangao",
            initialsKey = "mplxrg",
            searchTokens = "百多邦|莫匹罗星|皮肤感染|外用抗生素|脓疱疮",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 2014L,
            genericName = "硝酸咪康唑乳膏",
            tradeName = "达克宁",
            approvalNo = "国药准字H61020013",
            manufacturer = "西安杨森制药有限公司",
            dosageForm = "乳膏剂",
            specification = "20g:0.4g",
            categoryName = "皮肤科用药",
            pinyinKey = "xiaosuan mikangzuo rugao",
            initialsKey = "xsmkzrg",
            searchTokens = "达克宁|硝酸咪康唑|脚气|真菌感染|皮肤癣|念珠菌",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 2015L,
            genericName = "复方酮康唑软膏",
            tradeName = "复方酮康唑",
            approvalNo = "国药准字H20067289",
            manufacturer = "上海通用药业股份有限公司",
            dosageForm = "软膏剂",
            specification = "10g",
            categoryName = "皮肤科用药",
            pinyinKey = "fufang tongkangzuo ruangao",
            initialsKey = "fftkzrg",
            searchTokens = "复方酮康唑|酮康唑|皮肤癣|真菌感染|皮炎",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        // ============================================================
        // 2016-2019. 眼科用药（新增扩充）
        // ============================================================
        DrugMasterEntity(
            drugId = 2016L,
            genericName = "左氧氟沙星滴眼液",
            tradeName = "可乐必妥滴眼液",
            approvalNo = "国药准字H20020205",
            manufacturer = "第一三共制药(北京)有限公司",
            dosageForm = "滴眼剂",
            specification = "5ml:24.4mg",
            categoryName = "眼科用药",
            pinyinKey = "zuoyang fushaxing diyanye",
            initialsKey = "zyfsxdyy",
            searchTokens = "左氧氟沙星滴眼液|可乐必妥|滴眼液|结膜炎|眼部感染|眼药水",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 2017L,
            genericName = "妥布霉素滴眼液",
            tradeName = "托百士",
            approvalNo = "国药准字J20100028",
            manufacturer = "Alcon Laboratories Inc.",
            dosageForm = "滴眼剂",
            specification = "5ml:15mg",
            categoryName = "眼科用药",
            pinyinKey = "tuobumeisu diyanye",
            initialsKey = "tbmsdyy",
            searchTokens = "妥布霉素滴眼液|托百士|眼部感染|结膜炎|眼药水",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 2018L,
            genericName = "玻璃酸钠滴眼液",
            tradeName = "爱丽",
            approvalNo = "国药准字J20120043",
            manufacturer = "参天制药株式会社",
            dosageForm = "滴眼剂",
            specification = "5ml:5mg",
            categoryName = "眼科用药",
            pinyinKey = "bolisuan na diyanye",
            initialsKey = "blsndyy",
            searchTokens = "玻璃酸钠滴眼液|爱丽|人工泪液|干眼症|眼干涩|眼药水",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 2019L,
            genericName = "聚乙烯醇滴眼液",
            tradeName = "瑞珠",
            approvalNo = "国药准字H20073112",
            manufacturer = "湖北远大天天明制药有限公司",
            dosageForm = "滴眼剂",
            specification = "0.8ml:11.2mg*10支",
            categoryName = "眼科用药",
            pinyinKey = "juyixichun diyanye",
            initialsKey = "jyxcdyy",
            searchTokens = "聚乙烯醇滴眼液|瑞珠|人工泪液|干眼症|眼干涩|眼药水",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        // ============================================================
        // 2020-2023. 耳鼻喉科用药（新增扩充）
        // ============================================================
        DrugMasterEntity(
            drugId = 2020L,
            genericName = "氯霉素滴耳液",
            tradeName = "氯霉素滴耳液",
            approvalNo = "国药准字H20063526",
            manufacturer = "上海现代制药股份有限公司",
            dosageForm = "滴耳剂",
            specification = "10ml:0.25g",
            categoryName = "耳鼻喉科用药",
            pinyinKey = "lvmeisu dierye",
            initialsKey = "lmsdey",
            searchTokens = "氯霉素滴耳液|中耳炎|耳部感染|滴耳液",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 2021L,
            genericName = "氧氟沙星滴耳液",
            tradeName = "氧氟沙星滴耳液",
            approvalNo = "国药准字H20063021",
            manufacturer = "上海信谊药厂有限公司",
            dosageForm = "滴耳剂",
            specification = "5ml:15mg",
            categoryName = "耳鼻喉科用药",
            pinyinKey = "yangfu shaxing dierye",
            initialsKey = "yfsxdey",
            searchTokens = "氧氟沙星滴耳液|中耳炎|耳部感染|外耳道炎",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 2022L,
            genericName = "通窍鼻炎片",
            tradeName = "通窍鼻炎片",
            approvalNo = "国药准字Z20064051",
            manufacturer = "吉林敖东药业集团股份有限公司",
            dosageForm = "片剂",
            specification = "0.3g*36片",
            categoryName = "耳鼻喉科用药",
            pinyinKey = "tongqiao biyan pian",
            initialsKey = "tqbyp",
            searchTokens = "通窍鼻炎片|鼻炎|鼻塞|过敏性鼻炎|鼻窦炎",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 2023L,
            genericName = "鼻炎康片",
            tradeName = "鼻炎康",
            approvalNo = "国药准字Z20023074",
            manufacturer = "佛山德众药业有限公司",
            dosageForm = "片剂",
            specification = "0.3g*60片",
            categoryName = "耳鼻喉科用药",
            pinyinKey = "biyankang pian",
            initialsKey = "bykp",
            searchTokens = "鼻炎康|鼻炎康片|鼻炎|过敏性鼻炎|鼻窦炎|鼻塞",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        // ============================================================
        // 2024-2028. 消化科用药（新增扩充）
        // ============================================================
        DrugMasterEntity(
            drugId = 2024L,
            genericName = "蒙脱石散",
            tradeName = "蒙脱石散",
            approvalNo = "国药准字H20000690",
            manufacturer = "博福-益普生(天津)制药有限公司",
            dosageForm = "散剂",
            specification = "3g*10袋",
            categoryName = "消化科",
            pinyinKey = "mengtuoshi san",
            initialsKey = "mtss",
            searchTokens = "蒙脱石散|思密达|止泻|腹泻|成人腹泻",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 2025L,
            genericName = "双歧杆菌四联活菌片",
            tradeName = "思连康",
            approvalNo = "国药准字S20060010",
            manufacturer = "杭州远大生物制药有限公司",
            dosageForm = "片剂",
            specification = "0.5g*24片",
            categoryName = "消化科",
            pinyinKey = "shuangqi ganjun silian huojun pian",
            initialsKey = "sqgjslhjp",
            searchTokens = "双歧杆菌|四联活菌|思连康|益生菌|肠道菌群|腹泻|便秘",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 2026L,
            genericName = "乳果糖口服液",
            tradeName = "杜密克",
            approvalNo = "国药准字H20066146",
            manufacturer = "荷兰苏威制药公司",
            dosageForm = "口服液",
            specification = "15ml*6袋",
            categoryName = "消化科",
            pinyinKey = "ruguotang koufuye",
            initialsKey = "rgtkfy",
            searchTokens = "乳果糖|杜密克|便秘|通便|泻药|肝性脑病",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 2027L,
            genericName = "开塞露",
            tradeName = "开塞露",
            approvalNo = "国药准字H20073798",
            manufacturer = "上海运佳黄浦制药有限公司",
            dosageForm = "外用溶液",
            specification = "20ml*2支",
            categoryName = "消化科",
            pinyinKey = "kaisailu",
            initialsKey = "ksl",
            searchTokens = "开塞露|便秘|通便|灌肠|外用通便",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 2028L,
            genericName = "多酶片",
            tradeName = "多酶片",
            approvalNo = "国药准字H35021098",
            manufacturer = "福建太平洋制药有限公司",
            dosageForm = "片剂",
            specification = "100片",
            categoryName = "消化科",
            pinyinKey = "duomei pian",
            initialsKey = "dmp",
            searchTokens = "多酶片|消化酶|消化不良|胃动力|腹胀|食欲不振",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        // ============================================================
        // 2029-2033. 呼吸科用药（新增扩充）
        // ============================================================
        DrugMasterEntity(
            drugId = 2029L,
            genericName = "氨溴索片",
            tradeName = "沐舒坦",
            approvalNo = "国药准字H20010361",
            manufacturer = "上海勃林格殷格翰药业有限公司",
            dosageForm = "片剂",
            specification = "30mg*20片",
            categoryName = "呼吸科",
            pinyinKey = "anxiusuo pian",
            initialsKey = "axsp",
            searchTokens = "氨溴索|沐舒坦|化痰|祛痰|止咳化痰|呼吸道",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 2030L,
            genericName = "氢溴酸右美沙芬片",
            tradeName = "右美沙芬",
            approvalNo = "国药准字H20066318",
            manufacturer = "北京天衡药物研究院有限公司",
            dosageForm = "片剂",
            specification = "15mg*12片",
            categoryName = "呼吸科",
            pinyinKey = "qingxiusuan youmeishafen pian",
            initialsKey = "qxsymsfp",
            searchTokens = "右美沙芬|氢溴酸右美沙芬|止咳|镇咳|干咳|感冒咳嗽",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 2031L,
            genericName = "复方甘草片",
            tradeName = "复方甘草片",
            approvalNo = "国药准字H11021682",
            manufacturer = "北京同仁堂科技发展股份有限公司制药厂",
            dosageForm = "片剂",
            specification = "100片",
            categoryName = "呼吸科",
            pinyinKey = "fufang gancao pian",
            initialsKey = "ffgcp",
            searchTokens = "复方甘草片|甘草片|止咳|化痰|镇咳|咳嗽",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 2032L,
            genericName = "孟鲁司特钠片",
            tradeName = "顺尔宁",
            approvalNo = "国药准字J20130047",
            manufacturer = "Merck Sharp & Dohme Ltd.",
            dosageForm = "片剂",
            specification = "10mg*5片",
            categoryName = "呼吸科",
            pinyinKey = "menglusite na pian",
            initialsKey = "mlstnp",
            searchTokens = "孟鲁司特钠|顺尔宁|哮喘|过敏性鼻炎|白三烯|呼吸道",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 2033L,
            genericName = "沙丁胺醇气雾剂",
            tradeName = "万托林",
            approvalNo = "国药准字J20160056",
            manufacturer = "Glaxo Wellcome UK Ltd.",
            dosageForm = "气雾剂",
            specification = "100μg/喷*200喷",
            categoryName = "呼吸科",
            pinyinKey = "shadinganchun qiwuji",
            initialsKey = "sdacqwj",
            searchTokens = "沙丁胺醇|万托林|哮喘|气喘|支气管扩张|气雾剂|急救",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        // ============================================================
        // 2034-2037. 骨科/止痛用药（新增扩充）
        // ============================================================
        DrugMasterEntity(
            drugId = 2034L,
            genericName = "布洛芬凝胶",
            tradeName = "芬必得凝胶",
            approvalNo = "国药准字H20010706",
            manufacturer = "中美天津史克制药有限公司",
            dosageForm = "凝胶剂",
            specification = "20g:1g",
            categoryName = "骨科/止痛",
            pinyinKey = "buluofen ningjiao",
            initialsKey = "blfnj",
            searchTokens = "布洛芬凝胶|芬必得凝胶|止痛|外用止痛|关节炎|肌肉痛",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 2035L,
            genericName = "双氯芬酸二乙胺乳胶剂",
            tradeName = "扶他林乳胶剂",
            approvalNo = "国药准字H19990275",
            manufacturer = "北京诺华制药有限公司",
            dosageForm = "乳胶剂",
            specification = "20g:0.2g",
            categoryName = "骨科/止痛",
            pinyinKey = "shuanglv fensuan eryi'an rujiaoji",
            initialsKey = "slfseyarjj",
            searchTokens = "双氯芬酸二乙胺|扶他林|扶他林乳胶剂|止痛|关节炎|肌肉痛",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 2036L,
            genericName = "云南白药胶囊",
            tradeName = "云南白药",
            approvalNo = "国药准字Z53020798",
            manufacturer = "云南白药集团股份有限公司",
            dosageForm = "胶囊",
            specification = "0.25g*16粒",
            categoryName = "骨科/止痛",
            pinyinKey = "yunnan baiyao jiaonang",
            initialsKey = "ynbyjn",
            searchTokens = "云南白药|跌打损伤|止血|化瘀|止痛|活血",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 2037L,
            genericName = "活血止痛膏",
            tradeName = "活血止痛膏",
            approvalNo = "国药准字Z20063420",
            manufacturer = "安徽安科余良卿药业有限公司",
            dosageForm = "贴膏剂",
            specification = "7cm*10cm*4贴",
            categoryName = "骨科/止痛",
            pinyinKey = "huoxue zhitong gao",
            initialsKey = "hxztg",
            searchTokens = "活血止痛膏|跌打损伤|止痛贴|活血化瘀|关节痛|风湿",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        // ============================================================
        // 2038-2042. 外用药（新增扩充）
        // ============================================================
        DrugMasterEntity(
            drugId = 2038L,
            genericName = "聚维酮碘溶液",
            tradeName = "碘伏",
            approvalNo = "国药准字H20064256",
            manufacturer = "上海华联制药有限公司",
            dosageForm = "外用溶液",
            specification = "100ml:5g",
            categoryName = "外用药",
            pinyinKey = "juweitongdian rongye",
            initialsKey = "jwtdry",
            searchTokens = "碘伏|聚维酮碘|消毒|皮肤消毒|伤口消毒|外用消毒",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 2039L,
            genericName = "乙醇消毒液",
            tradeName = "75%医用酒精",
            approvalNo = "国药准字H20023087",
            manufacturer = "山东利尔康医疗科技股份有限公司",
            dosageForm = "外用溶液",
            specification = "100ml:75ml",
            categoryName = "外用药",
            pinyinKey = "yichun xiaodu ye",
            initialsKey = "ycxdy",
            searchTokens = "酒精|75%酒精|医用酒精|消毒|皮肤消毒|乙醇消毒液",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 2040L,
            genericName = "创可贴",
            tradeName = "邦迪创可贴",
            approvalNo = "国药准字H20063701",
            manufacturer = "上海强生有限公司",
            dosageForm = "贴膏剂",
            specification = "70mm*18mm*100片",
            categoryName = "外用药",
            pinyinKey = "chuangke tie",
            initialsKey = "ckt",
            searchTokens = "创可贴|邦迪|止血贴|伤口护理|小伤口|外用",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 2041L,
            genericName = "云南白药气雾剂",
            tradeName = "云南白药气雾剂",
            approvalNo = "国药准字Z20064222",
            manufacturer = "云南白药集团股份有限公司",
            dosageForm = "气雾剂",
            specification = "85g",
            categoryName = "外用药",
            pinyinKey = "yunnan baiyao qiwuji",
            initialsKey = "ynbyqwj",
            searchTokens = "云南白药气雾剂|跌打损伤|扭伤|瘀肿|活血|外用喷雾",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 2042L,
            genericName = "红花油",
            tradeName = "红花油",
            approvalNo = "国药准字Z20043165",
            manufacturer = "福建太平洋制药有限公司",
            dosageForm = "外用溶液",
            specification = "20ml",
            categoryName = "外用药",
            pinyinKey = "honghua you",
            initialsKey = "hhy",
            searchTokens = "红花油|跌打损伤|风湿|止痛|活血化瘀|外用",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        // ============================================================
        // 2043-2047. 罕见病/痛风用药（新增扩充）
        // ============================================================
        DrugMasterEntity(
            drugId = 2043L,
            genericName = "秋水仙碱片",
            tradeName = "秋水仙碱",
            approvalNo = "国药准字H20057250",
            manufacturer = "昆明制药集团股份有限公司",
            dosageForm = "片剂",
            specification = "0.5mg*20片",
            categoryName = "抗痛风药",
            pinyinKey = "qiushuixianjian pian",
            initialsKey = "qsxjp",
            searchTokens = "秋水仙碱|痛风|急性痛风|关节炎|抗痛风",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 2044L,
            genericName = "别嘌醇片",
            tradeName = "别嘌醇",
            approvalNo = "国药准字H20020322",
            manufacturer = "上海信谊万象药业股份有限公司",
            dosageForm = "片剂",
            specification = "0.1g*100片",
            categoryName = "抗痛风药",
            pinyinKey = "biepiaochun pian",
            initialsKey = "bcp",
            searchTokens = "别嘌醇|别嘌呤醇|痛风|高尿酸血症|降尿酸",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 2045L,
            genericName = "苯溴马隆片",
            tradeName = "立加利仙",
            approvalNo = "国药准字H20010787",
            manufacturer = "昆山龙灯瑞迪制药有限公司",
            dosageForm = "片剂",
            specification = "50mg*10片",
            categoryName = "抗痛风药",
            pinyinKey = "benxiu malong pian",
            initialsKey = "bxmlp",
            searchTokens = "苯溴马隆|立加利仙|痛风|高尿酸血症|降尿酸",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 2046L,
            genericName = "甲氨蝶呤片",
            tradeName = "甲氨蝶呤",
            approvalNo = "国药准字H20020578",
            manufacturer = "上海信谊药厂有限公司",
            dosageForm = "片剂",
            specification = "2.5mg*100片",
            categoryName = "抗肿瘤/免疫抑制剂",
            pinyinKey = "jiaandieling pian",
            initialsKey = "jadlp",
            searchTokens = "甲氨蝶呤|MTX|类风湿关节炎|抗肿瘤|免疫抑制剂|银屑病",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        ),
        DrugMasterEntity(
            drugId = 2047L,
            genericName = "青霉胺片",
            tradeName = "青霉胺",
            approvalNo = "国药准字H31022086",
            manufacturer = "上海信谊药厂有限公司",
            dosageForm = "片剂",
            specification = "0.125g*100片",
            categoryName = "免疫抑制剂",
            pinyinKey = "qingmeian pian",
            initialsKey = "qmap",
            searchTokens = "青霉胺|肝豆状核变性|Wilson病|类风湿关节炎|重金属中毒",
            sourceTag = "seed_demo",
            licenseNote = "仅限比赛演示样本，不作为正式商用药品数据库授权证明。"
        )
    )

    private val seedDrugDetails = listOf(
        // ===== 1001-1003 原有药品 =====
        DrugDetailEntity(
            drugId = 1001L,
            composition = "本品主要成分为布洛芬。",
            indication = "用于缓解轻至中度疼痛及普通感冒或流行性感冒引起的发热。",
            usageAndDosage = "成人一次1粒，一日2次，建议饭后服用。",
            taboo = "对布洛芬或其他非甾体抗炎药过敏者禁用。",
            attention = "胃溃疡、消化道出血病史、严重肝肾功能不全者慎用。",
            adverseReaction = "可见恶心、胃部不适、皮疹等反应。",
            interactionText = "与其他解热镇痛药同用前需谨慎核对。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别布洛芬缓释胶囊，建议重点核对禁忌和胃部风险。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1002L,
            composition = "本品主要成分为阿莫西林。",
            indication = "适用于敏感菌所致的呼吸道、泌尿道等感染。",
            usageAndDosage = "应遵医嘱使用，常见成人一次0.5g，每6至8小时一次。",
            taboo = "青霉素过敏者禁用。",
            attention = "首次使用前需确认过敏史，不建议自行长期服用。",
            adverseReaction = "可能出现腹泻、恶心、皮疹等。",
            interactionText = "与其他抗菌药合用前需咨询医生或药师。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别阿莫西林胶囊，如有青霉素过敏史应避免使用。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1003L,
            composition = "本品主要成分为盐酸二甲双胍。",
            indication = "用于2型糖尿病患者的血糖控制。",
            usageAndDosage = "建议随餐服用，具体剂量应依据医生方案调整。",
            taboo = "严重肾功能不全、代谢性酸中毒患者禁用。",
            attention = "老年人、肾功能异常患者需重点监测。",
            adverseReaction = "常见胃肠道不适，如恶心、腹泻。",
            interactionText = "与其他降糖药联用时需防范低血糖风险。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别盐酸二甲双胍片，建议重点关注肾功能和用药方案。",
            sourceTag = "seed_demo"
        ),
        // ===== 解热镇痛药 & 感冒用药 =====
        DrugDetailEntity(
            drugId = 1004L,
            composition = "本品主要成分为对乙酰氨基酚。",
            indication = "用于普通感冒或流行性感冒引起的发热，也用于缓解轻至中度疼痛。",
            usageAndDosage = "成人一次0.5g，一日不超过2g，用药间隔4-6小时。",
            taboo = "严重肝肾功能不全者禁用。对本品过敏者禁用。",
            attention = "每日用量不得超过2g，过量使用可能导致严重肝损伤。服药期间不得饮酒。",
            adverseReaction = "偶见皮疹、荨麻疹等过敏反应。长期大量使用可能导致肝肾功能异常。",
            interactionText = "与华法林同用可增强抗凝作用；与酒精同用增加肝毒性风险。",
            storageMethod = "密封，阴凉干燥处保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别对乙酰氨基酚片，注意每日最大剂量和肝损伤风险。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1005L,
            composition = "本品主要成分为双氯芬酸钠。",
            indication = "用于缓解类风湿关节炎、骨关节炎等各种关节炎的疼痛和炎症。",
            usageAndDosage = "成人一次75mg，一日1次，饭后整片吞服，不可掰开。",
            taboo = "消化道溃疡活动期、严重心肾衰竭患者禁用。对阿司匹林过敏者禁用。",
            attention = "长期使用需监测肝肾功能和血象。老年人应使用最小有效剂量。",
            adverseReaction = "常见胃肠道反应如上腹痛、恶心、腹泻。偶见头痛、眩晕。",
            interactionText = "与抗凝药同用增加出血风险；与锂剂、地高辛同用可增加其血药浓度。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别双氯芬酸钠缓释片，建议核对胃肠道和心血管风险。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1006L,
            composition = "本品为中西药复方制剂，含三叉苦、金盏银盘、野菊花等中药及对乙酰氨基酚、马来酸氯苯那敏等。",
            indication = "用于感冒引起的头痛、发热、鼻塞、流涕、咽痛等症状。",
            usageAndDosage = "成人一次1袋，一日3次，开水冲服。",
            taboo = "严重肝肾功能不全者禁用。对本药任何成分过敏者禁用。",
            attention = "服药期间不宜驾驶车辆或操作精密仪器。不宜同时服用含相同成分的感冒药。",
            adverseReaction = "偶见嗜睡、口干、乏力等。",
            interactionText = "不宜与其他含对乙酰氨基酚的感冒药同用。与中枢镇静药同用可增强嗜睡作用。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "复合膜袋装。",
            ttsSummary = "已识别感冒灵颗粒，注意复方成分和嗜睡副作用。",
            sourceTag = "seed_demo"
        ),
        // ===== 抗生素 & 抗菌药 =====
        DrugDetailEntity(
            drugId = 1007L,
            composition = "本品主要成分为头孢克肟。",
            indication = "用于敏感菌所致的呼吸道感染、泌尿道感染、胆道感染等。",
            usageAndDosage = "成人一次0.1g，一日2次，严重感染可增至每次0.2g。",
            taboo = "对头孢菌素类抗生素过敏者禁用。",
            attention = "青霉素过敏者慎用。严重肾功能不全者需调整剂量。长期使用需注意菌群失调。",
            adverseReaction = "常见腹泻、恶心等消化道反应。偶见皮疹、药物热等过敏反应。",
            interactionText = "与强利尿药同用可能增加肾毒性。与华法林同用可能增强抗凝作用。",
            storageMethod = "密封，阴凉干燥处保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别头孢克肟胶囊，需确认是否有头孢类或青霉素过敏史。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1008L,
            composition = "本品主要成分为阿奇霉素。",
            indication = "用于敏感菌所致的鼻窦炎、咽炎、扁桃体炎、支气管炎、肺炎等呼吸道感染。",
            usageAndDosage = "成人一次0.25g，一日1次，连服3日。",
            taboo = "对大环内酯类抗生素过敏者禁用。严重肝功能不全者禁用。",
            attention = "肝功能异常患者慎用。用药期间如出现腹泻需考虑假膜性肠炎可能。",
            adverseReaction = "常见恶心、腹痛、腹泻等胃肠道反应。偶见皮疹。",
            interactionText = "与华法林同用可增强抗凝作用。与地高辛同用可增加地高辛血药浓度。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别阿奇霉素胶囊，需关注肝功能和大环内酯类过敏史。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1009L,
            composition = "本品主要成分为头孢拉定。",
            indication = "用于敏感菌所致的呼吸道、泌尿道、皮肤及软组织等感染。",
            usageAndDosage = "成人一次0.25-0.5g，每6小时一次，空腹服用效果更佳。",
            taboo = "对头孢菌素类过敏者禁用。",
            attention = "青霉素过敏者交叉过敏风险约5-10%。肾功能不全者需调整剂量。",
            adverseReaction = "常见胃肠道反应。偶见药疹、药物热等过敏反应。",
            interactionText = "与氨基糖苷类抗生素同用增加肾毒性。与华法林同用可增强抗凝作用。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别头孢拉定胶囊，需确认是否有头孢类过敏史。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1010L,
            composition = "本品主要成分为左氧氟沙星。",
            indication = "用于敏感菌所致的呼吸系统、泌尿系统、生殖系统等感染。",
            usageAndDosage = "成人一次0.1g，一日2-3次，或遵医嘱。",
            taboo = "对喹诺酮类药过敏者禁用。18岁以下未成年人禁用。癫痫患者禁用。",
            attention = "避免与含钙、铝、镁的药物同服。用药期间多饮水。避免过度阳光暴晒。",
            adverseReaction = "偶见胃肠道反应、头痛、失眠。罕见肌腱炎或肌腱断裂。",
            interactionText = "与含钙、镁、铝的抗酸药同服可降低吸收率。与华法林同用增强抗凝作用。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别左氧氟沙星片，需注意喹诺酮类过敏和肌腱风险。",
            sourceTag = "seed_demo"
        ),
        // ===== 降压药 - ARB / CCB / β阻滞剂等 =====
        DrugDetailEntity(
            drugId = 1011L,
            composition = "本品主要成分为氯沙坦钾。",
            indication = "用于原发性高血压的治疗。",
            usageAndDosage = "常用起始剂量为一日50mg，根据血压情况可调整至一日100mg。",
            taboo = "妊娠期及哺乳期妇女禁用。严重肝功能不全者禁用。",
            attention = "血容量不足患者（如服用大剂量利尿药者）需纠正后使用。定期监测血钾和肾功能。",
            adverseReaction = "偶见头晕、乏力。罕见高钾血症。",
            interactionText = "与保钾利尿药、补钾药同用增加高钾血症风险。与锂剂同用增加锂中毒风险。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别氯沙坦钾片，注意监测血钾和肾功能。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1012L,
            composition = "本品主要成分为硝苯地平。",
            indication = "用于原发性高血压和慢性稳定型心绞痛的治疗。",
            usageAndDosage = "成人一次30mg，一日1次，整片吞服不可掰开。",
            taboo = "心源性休克患者禁用。妊娠期妇女禁用。",
            attention = "严重低血压患者慎用。停药需缓慢减量。肝功能损害者需减量。",
            adverseReaction = "常见头痛、面部潮红、踝部水肿。偶见心悸、头晕。",
            interactionText = "与CYP3A4抑制剂（如克拉霉素、伊曲康唑）同用可增加硝苯地平血药浓度。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别硝苯地平控释片，注意头痛、水肿等常见副作用。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1013L,
            composition = "本品主要成分为苯磺酸氨氯地平。",
            indication = "用于原发性高血压和冠心病（慢性稳定型心绞痛）的治疗。",
            usageAndDosage = "成人一次5mg，一日1次，最大剂量可增至10mg。",
            taboo = "严重低血压患者禁用。对二氢吡啶类钙拮抗剂过敏者禁用。",
            attention = "肝功能不全患者需减量。老年患者应从低剂量起始。",
            adverseReaction = "常见头痛、水肿、疲劳。偶见心悸、面部潮红。",
            interactionText = "与CYP3A4抑制剂和诱导剂存在相互作用。与其他降压药联用有协同作用。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别苯磺酸氨氯地平片，常见副作用为脚踝水肿。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1014L,
            composition = "本品主要成分为厄贝沙坦。",
            indication = "用于原发性高血压的治疗，也可用于2型糖尿病合并高血压患者的肾脏保护。",
            usageAndDosage = "常用起始剂量为一日150mg，可增至一日300mg。",
            taboo = "妊娠期及哺乳期妇女禁用。严重肾功能不全者慎用。",
            attention = "血容量不足患者应先纠正后使用。需定期监测血钾和肾功能。",
            adverseReaction = "偶见头晕、疲劳。罕见咳嗽、高钾血症。",
            interactionText = "与保钾利尿药同用增加高钾风险。与锂剂同用增加锂中毒风险。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别厄贝沙坦片，注意监测血钾水平。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1015L,
            composition = "本品主要成分为琥珀酸美托洛尔。",
            indication = "用于高血压、心绞痛、慢性心力衰竭及心律失常的治疗。",
            usageAndDosage = "成人一次47.5mg，一日1次，可增至95mg。整片吞服不可嚼碎。",
            taboo = "二至三度房室传导阻滞、失代偿性心衰、严重窦性心动过缓患者禁用。",
            attention = "停药需缓慢减量（至少1-2周）。糖尿病患者用药可能掩盖低血糖症状。",
            adverseReaction = "常见疲劳、头晕、心动过缓。偶见气短、肢端发冷。",
            interactionText = "与维拉帕米、地尔硫䓬同用增加心动过缓风险。与降糖药同用需监测血糖。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别琥珀酸美托洛尔缓释片，注意心动过缓风险。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1016L,
            composition = "本品主要成分为卡托普利。",
            indication = "用于高血压和心力衰竭的治疗。",
            usageAndDosage = "成人一次25mg，一日2-3次，餐前1小时服用。",
            taboo = "妊娠期妇女禁用。有血管神经性水肿史者禁用。",
            attention = "肾功能不全者需调整剂量。双侧肾动脉狭窄者禁用。定期监测血钾和肾功能。",
            adverseReaction = "常见干咳。偶见皮疹、味觉异常。罕见血管神经性水肿。",
            interactionText = "与保钾利尿药同用增加高钾风险。与非甾体抗炎药同用可减弱降压效果。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别卡托普利片，注意干咳副作用和血管性水肿风险。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1017L,
            composition = "本品主要成分为缬沙坦。",
            indication = "用于原发性高血压和慢性心力衰竭（NYHA II-IV级）的治疗。",
            usageAndDosage = "成人一次80mg，一日1次，可增至160mg。",
            taboo = "妊娠期及哺乳期妇女禁用。严重肝功能不全者禁用。",
            attention = "血容量不足患者应先纠正后使用。需定期监测血钾、肌酐。",
            adverseReaction = "偶见头晕、头痛、疲劳。罕见高钾血症。",
            interactionText = "与保钾利尿药同用增加高钾血症风险。与锂剂同用增加锂中毒风险。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别缬沙坦胶囊，降压效果平稳，副作用较少。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1018L,
            composition = "本品主要成分为非洛地平。",
            indication = "用于原发性高血压的治疗。",
            usageAndDosage = "成人一次5mg，一日1次，可增至10mg。整片吞服不可掰开。",
            taboo = "不稳定性心绞痛患者慎用。对二氢吡啶类过敏者禁用。",
            attention = "肝功能不全患者需减量。老年患者应从低剂量起始。",
            adverseReaction = "常见头痛、面部潮红、踝部水肿。偶见牙龈增生。",
            interactionText = "与CYP3A4抑制剂同用可升高非洛地平血药浓度。与葡萄柚汁同用需谨慎。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别非洛地平缓释片，注意踝部水肿和牙龈增生。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1019L,
            composition = "本品主要成分为坎地沙坦酯。",
            indication = "用于原发性高血压的治疗。",
            usageAndDosage = "成人一次8mg，一日1次，可根据血压调整至12mg。",
            taboo = "妊娠期及哺乳期妇女禁用。",
            attention = "血容量不足患者应先纠正。严重肾功能不全者需调整剂量。",
            adverseReaction = "偶见头晕、头痛。罕见咳嗽、高钾血症。",
            interactionText = "与保钾利尿药同用增加高钾风险。与NSAIDs同用可减弱降压效果。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别坎地沙坦酯片，耐受性良好，副作用少。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1020L,
            composition = "本品主要成分为替米沙坦。",
            indication = "用于原发性高血压的治疗，降低心血管风险。",
            usageAndDosage = "成人一次80mg，一日1次，可减至40mg。",
            taboo = "妊娠期及哺乳期妇女禁用。严重肝功能不全者禁用。",
            attention = "血容量不足患者应先纠正。需监测血钾和肾功能。",
            adverseReaction = "偶见头晕、腹泻。罕见血管神经性水肿。",
            interactionText = "与保钾利尿药同用增加高钾风险。与地高辛同用可增加地高辛血药浓度。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别替米沙坦片，半衰期长，一日一次服药方便。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1021L,
            composition = "本品主要成分为奥美沙坦酯。",
            indication = "用于原发性高血压的治疗。",
            usageAndDosage = "成人一次20mg，一日1次，可增至40mg。",
            taboo = "妊娠期及哺乳期妇女禁用。",
            attention = "血容量不足患者应先纠正后使用。定期监测血钾和肾功能。",
            adverseReaction = "偶见头晕、头痛。罕见高钾血症、肾功能异常。",
            interactionText = "与保钾利尿药同用增加高钾风险。与锂剂同用增加锂中毒风险。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别奥美沙坦酯片，降压作用强而持久。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1022L,
            composition = "本品主要成分为富马酸比索洛尔。",
            indication = "用于高血压、冠心病（心绞痛）及慢性稳定型心力衰竭的治疗。",
            usageAndDosage = "成人一次5mg，一日1次，可增至10mg。",
            taboo = "急性心力衰竭、二至三度房室传导阻滞、严重窦性心动过缓患者禁用。",
            attention = "停药需缓慢减量。慢性阻塞性肺疾病患者慎用。可能掩盖甲状腺功能亢进症状。",
            adverseReaction = "常见心动过缓、疲劳、肢端发冷。偶见睡眠障碍。",
            interactionText = "与维拉帕米类钙拮抗剂同用增加心动过缓风险。与降糖药同用需监测血糖。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别富马酸比索洛尔片，注意心率监测。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1023L,
            composition = "本品主要成分为盐酸阿罗洛尔。",
            indication = "用于轻至中度原发性高血压、心绞痛及心律失常的治疗。",
            usageAndDosage = "成人一次10mg，一日2次，可根据病情调整。",
            taboo = "严重窦性心动过缓、二至三度房室传导阻滞、失代偿性心衰患者禁用。",
            attention = "停药需逐渐减量。肝功能不全者需减量。老年患者应从低剂量开始。",
            adverseReaction = "常见心动过缓、头晕、乏力。偶见气短、肢端发冷。",
            interactionText = "与钙拮抗剂同用增加心动过缓风险。与降糖药同用需监测血糖。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别盐酸阿罗洛尔片，兼具α和β受体阻滞作用。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1024L,
            composition = "本品主要成分为氢氯噻嗪。",
            indication = "用于原发性高血压和水肿性疾病（如心力衰竭性水肿）的治疗。",
            usageAndDosage = "成人一次25mg，一日1次，早晨服用。",
            taboo = "无尿症、严重肾功能不全患者禁用。磺胺类药物过敏者禁用。",
            attention = "需定期监测电解质（尤其是血钾）、血糖和尿酸。糖尿病患者使用需注意血糖变化。",
            adverseReaction = "常见低钾血症、高尿酸血症。偶见皮疹、光敏反应。",
            interactionText = "与洋地黄类同用增加心律失常风险（低钾所致）。与降糖药同用需调整降糖药剂量。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别氢氯噻嗪片，注意低钾血症和尿酸升高风险。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1025L,
            composition = "本品主要成分为呋塞米。",
            indication = "用于心力衰竭、肝硬化、肾病综合征等引起的水肿及高血压。",
            usageAndDosage = "成人一次20mg，一日1-2次，早晨服用最佳。",
            taboo = "无尿症、严重电解质紊乱患者禁用。磺胺类药物过敏者禁用。",
            attention = "需密切监测电解质和血容量。老年患者、前列腺增生患者慎用。",
            adverseReaction = "常见电解质紊乱（低钾、低钠）、脱水和血容量不足。",
            interactionText = "与氨基糖苷类抗生素同用增加耳肾毒性。与锂剂同用增加锂中毒风险。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别呋塞米片，强效利尿，注意电解质监测。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1026L,
            composition = "本品主要成分为螺内酯。",
            indication = "用于心力衰竭、肝硬化腹水等水肿性疾病及原发性醛固酮增多症。",
            usageAndDosage = "成人一次20mg，一日2-3次，或遵医嘱。",
            taboo = "高钾血症患者禁用。严重肾功能不全（eGFR<30ml/min）患者禁用。",
            attention = "需定期监测血钾和肾功能。可能引起男性乳房发育。",
            adverseReaction = "常见高钾血症、男性乳房发育、月经紊乱。",
            interactionText = "与ACEI/ARB同用增加高钾血症风险。与补钾制剂同用严禁。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别螺内酯片，保钾利尿药，注意高钾血症风险。",
            sourceTag = "seed_demo"
        ),
        // ===== 调脂药 =====
        DrugDetailEntity(
            drugId = 1027L,
            composition = "本品主要成分为辛伐他汀。",
            indication = "用于高胆固醇血症和冠心病的治疗，降低心血管事件风险。",
            usageAndDosage = "成人一次20mg，一日1次，晚上服用。",
            taboo = "活动性肝病患者禁用。妊娠期及哺乳期妇女禁用。",
            attention = "用药前和用药期间需定期监测肝功能。出现不明原因肌痛需监测肌酸激酶。",
            adverseReaction = "偶见肌痛、肝酶升高。罕见横纹肌溶解症。",
            interactionText = "与CYP3A4抑制剂（如伊曲康唑、克拉霉素）同用增加肌病风险。与环孢素同用需减量。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别辛伐他汀片，注意肝酶和肌肉症状监测。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1028L,
            composition = "本品主要成分为阿托伐他汀钙。",
            indication = "用于高胆固醇血症和冠心病的治疗，降低心肌梗死和卒中的风险。",
            usageAndDosage = "起始剂量一日10-20mg，最大可至80mg，一日1次任意时间服用。",
            taboo = "活动性肝病、不明原因肝酶持续升高者禁用。妊娠期及哺乳期妇女禁用。",
            attention = "需定期监测肝功能。出现肌痛、乏力需监测肌酸激酶。",
            adverseReaction = "偶见鼻咽炎、关节痛、腹泻。罕见肌病、肝酶升高。",
            interactionText = "与CYP3A4抑制剂相互作用。与华法林同用需监测INR。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别阿托伐他汀钙片，强效降脂，注意肝功能监测。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1029L,
            composition = "本品主要成分为瑞舒伐他汀钙。",
            indication = "用于原发性高胆固醇血症和混合型血脂异常的治疗。",
            usageAndDosage = "起始剂量一日10mg，可调整至20-40mg，一日1次。",
            taboo = "活动性肝病患者禁用。妊娠期及哺乳期妇女禁用。",
            attention = "亚洲人群起始剂量建议5mg。需定期监测肝功能和肌酸激酶。",
            adverseReaction = "偶见肌痛、腹痛、恶心。罕见蛋白尿、横纹肌溶解。",
            interactionText = "与环孢素同用显著增加瑞舒伐他汀血药浓度。与华法林同用需监测INR。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别瑞舒伐他汀钙片，注意亚洲人群剂量和肝功能监测。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1030L,
            composition = "本品主要成分为非诺贝特。",
            indication = "用于高甘油三酯血症和混合型高脂血症的治疗。",
            usageAndDosage = "成人一次200mg，一日1次，与餐同服。",
            taboo = "严重肝功能不全、严重肾功能不全患者禁用。胆囊疾病患者禁用。",
            attention = "需定期监测肝功能和肾功能。与他汀类联用需警惕肌病风险。",
            adverseReaction = "常见消化道反应。偶见肝酶升高、肌痛。",
            interactionText = "与华法林同用增强抗凝作用。与他汀类同用增加肌病风险。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别非诺贝特胶囊，主要用于降甘油三酯。",
            sourceTag = "seed_demo"
        ),
        // ===== 消化系统用药 =====
        DrugDetailEntity(
            drugId = 1031L,
            composition = "本品主要成分为奥美拉唑。",
            indication = "用于胃溃疡、十二指肠溃疡、反流性食管炎及卓-艾综合征的治疗。",
            usageAndDosage = "成人一次20mg，一日1次，晨起空腹服用最佳。",
            taboo = "对质子泵抑制剂过敏者禁用。与阿扎那韦、奈非那韦联用禁忌。",
            attention = "长期使用（>1年）需监测维生素B12水平和骨折风险。",
            adverseReaction = "常见头痛、腹胀、便秘。长期使用增加肠道感染和骨质疏松风险。",
            interactionText = "与氯吡格雷同用可能降低氯吡格雷疗效。与地高辛同用可增加地高辛吸收。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别奥美拉唑肠溶胶囊，长期使用需关注维生素B12和骨折风险。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1032L,
            composition = "本品主要成分为雷贝拉唑钠。",
            indication = "用于胃溃疡、十二指肠溃疡、反流性食管炎及胃食管反流病的治疗。",
            usageAndDosage = "成人一次10mg，一日1次，晨起空腹服用。",
            taboo = "对雷贝拉唑或其他苯并咪唑类药物过敏者禁用。",
            attention = "长期使用需监测镁水平和骨折风险。严重肝功能不全者需减量。",
            adverseReaction = "偶见头痛、腹泻、便秘。罕见间质性肾炎。",
            interactionText = "可能影响pH依赖吸收的药物（如酮康唑、铁剂）的吸收。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别雷贝拉唑钠肠溶片，起效快，药物相互作用较少。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1033L,
            composition = "本品主要成分为泮托拉唑钠。",
            indication = "用于消化性溃疡、反流性食管炎和卓-艾综合征的治疗。",
            usageAndDosage = "成人一次40mg，一日1次，晨起空腹服用。",
            taboo = "对本药过敏者禁用。",
            attention = "长期使用（>1年）需监测维生素B12水平。骨折风险增加。",
            adverseReaction = "偶见头痛、腹泻、腹胀。罕见肝酶升高。",
            interactionText = "与甲氨蝶呤同用可能增加甲氨蝶呤毒性。与华法林同用需监测INR。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别泮托拉唑钠肠溶胶囊，药物相互作用风险较低。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1034L,
            composition = "本品主要成分为多潘立酮。",
            indication = "用于消化不良、腹胀、嗳气、恶心、呕吐等症状的治疗。",
            usageAndDosage = "成人一次10mg，一日3次，餐前15-30分钟服用。",
            taboo = "胃肠道出血、穿孔或梗阻患者禁用。催乳素瘤患者禁用。",
            attention = "肝功能中度以上不全者需减量。心电图QT间期延长患者慎用。",
            adverseReaction = "偶见口干、头痛、腹泻。长期使用可能出现锥体外系反应。",
            interactionText = "与CYP3A4抑制剂同用可升高多潘立酮血药浓度。与抗胆碱药同用减弱促胃动力作用。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别多潘立酮片，注意QT间期延长风险。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1035L,
            composition = "本品主要成分为莫沙必利。",
            indication = "用于功能性消化不良引起的胃部不适、腹胀、早饱等症状。",
            usageAndDosage = "成人一次5mg，一日3次，餐前或餐后服用。",
            taboo = "胃肠道出血、穿孔患者禁用。对本药过敏者禁用。",
            attention = "肝功能不全者需谨慎使用。老年患者需减量。",
            adverseReaction = "偶见腹泻、腹痛、口干。罕见皮疹。",
            interactionText = "与抗胆碱药同用可能减弱促动力效果。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别莫沙必利片，促胃动力药，副作用较少。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1036L,
            composition = "本品主要成分为铝碳酸镁。",
            indication = "用于胃酸过多引起的胃痛、胃灼热、反酸等症状。",
            usageAndDosage = "成人一次0.5-1g，一日3-4次，餐后1-2小时或胃痛时嚼服。",
            taboo = "严重肾功能不全患者禁用。低磷血症患者禁用。",
            attention = "长期使用需监测血磷水平。服用后2小时内不宜服用其他药物。",
            adverseReaction = "偶见腹泻、便秘。长期使用可能导致低磷血症。",
            interactionText = "可影响四环素类、铁剂、地高辛等药物的吸收，需间隔2小时服用。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别铝碳酸镁咀嚼片，抗酸药，服药需与其他药物间隔2小时。",
            sourceTag = "seed_demo"
        ),
        // ===== 心脑血管中成药 =====
        DrugDetailEntity(
            drugId = 1037L,
            composition = "本品主要成分为丹参、三七、冰片。",
            indication = "用于冠心病引起的胸闷、心绞痛及心肌梗死的辅助治疗。",
            usageAndDosage = "成人一次10丸，一日3次，口服或舌下含服。",
            taboo = "出血性疾病患者禁用。对本药成分过敏者禁用。",
            attention = "孕妇慎用。手术前一周应停用。不宜与抗凝药同时大剂量使用。",
            adverseReaction = "偶见胃肠道不适。罕见过敏反应。",
            interactionText = "与华法林、阿司匹林同用增加出血风险。",
            storageMethod = "密封，阴凉干燥处保存。",
            validPeriod = "36个月。",
            packageInfo = "瓶装。",
            ttsSummary = "已识别复方丹参滴丸，注意与抗凝药同用的出血风险。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1038L,
            composition = "本品主要成分为川芎、冰片。",
            indication = "用于气滞血瘀型冠心病心绞痛的治疗，缓解胸闷、胸痛症状。",
            usageAndDosage = "成人一次4-6丸，一日3次，舌下含服。急性发作时一次10-15丸。",
            taboo = "对本药成分过敏者禁用。",
            attention = "孕妇慎用。不宜长期大剂量使用。如症状不缓解需及时就医。",
            adverseReaction = "偶见口唇麻木感、头晕。罕见过敏反应。",
            interactionText = "与其他心血管药物联用需在医生指导下进行。",
            storageMethod = "密封，阴凉干燥处保存。",
            validPeriod = "36个月。",
            packageInfo = "瓶装。",
            ttsSummary = "已识别速效救心丸，用于心绞痛急性发作的急救用药。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1039L,
            composition = "本品主要成分为银杏叶提取物。",
            indication = "用于脑部血液循环障碍及血流灌注不足所引起的记忆力减退、注意力不集中等症状。",
            usageAndDosage = "成人一次40mg，一日3次，餐后服用。",
            taboo = "对本药成分过敏者禁用。",
            attention = "出血性疾病患者慎用。手术前应停用。",
            adverseReaction = "偶见胃肠道不适、头痛、过敏反应。",
            interactionText = "与抗凝药（华法林、阿司匹林）同用增加出血风险。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别银杏叶片，改善脑循环，注意与抗凝药的相互作用。",
            sourceTag = "seed_demo"
        ),
        // ===== 抗血小板 / 抗凝药 =====
        DrugDetailEntity(
            drugId = 1040L,
            composition = "本品主要成分为硫酸氢氯吡格雷。",
            indication = "用于预防动脉粥样硬化血栓形成事件（心肌梗死、卒中、外周动脉疾病）。",
            usageAndDosage = "成人一次75mg，一日1次，与或不与食物同服。",
            taboo = "活动性病理性出血（如消化性溃疡、颅内出血）患者禁用。",
            attention = "手术前5-7天应停用。肾功能不全者经验有限。CYP2C19慢代谢者疗效可能降低。",
            adverseReaction = "常见出血事件（鼻出血、瘀斑）。偶见腹泻、腹痛。",
            interactionText = "与华法林、阿司匹林、NSAIDs同用增加出血风险。与PPI（奥美拉唑）同用可能降低疗效。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别硫酸氢氯吡格雷片，抗血小板药，注意出血风险。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1041L,
            composition = "本品主要成分为阿司匹林。",
            indication = "用于抑制血小板聚集，预防心脑血管事件；也用于解热镇痛。",
            usageAndDosage = "抗血小板：成人一次100mg，一日1次，整片吞服。解热镇痛：一次0.3-0.6g。",
            taboo = "消化性溃疡活动期、血友病患者禁用。妊娠期最后三个月禁用。",
            attention = "哮喘患者、胃溃疡史患者慎用。饮酒后不宜服用。",
            adverseReaction = "常见胃肠道不适。偶见恶心、呕吐。长期使用增加胃溃疡和出血风险。",
            interactionText = "与华法林、氯吡格雷同用增加出血风险。与布洛芬同用可能减弱抗血小板作用。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别阿司匹林肠溶片，注意胃肠道和出血风险。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1042L,
            composition = "本品主要成分为华法林钠。",
            indication = "用于预防和治疗血栓栓塞性疾病，如心房颤动、人工心脏瓣膜置换术后。",
            usageAndDosage = "剂量个体化，常规起始剂量一日2.5-5mg，根据INR调整。",
            taboo = "妊娠期妇女禁用（致畸风险）。严重出血倾向患者禁用。",
            attention = "需定期监测INR（目标2.0-3.0）。饮食变化（维生素K摄入）会影响疗效。",
            adverseReaction = "主要不良反应为出血（鼻出血、牙龈出血、皮肤瘀斑）。",
            interactionText = "与阿司匹林、NSAIDs、氯吡格雷同用显著增加出血风险。多种药物与之有相互作用。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别华法林钠片，需定期监测INR，注意出血风险。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1043L,
            composition = "本品主要成分为地高辛。",
            indication = "用于心力衰竭和某些类型的心律失常（如心房颤动）的治疗。",
            usageAndDosage = "成人一次0.125-0.25mg，一日1次。",
            taboo = "二至三度房室传导阻滞患者禁用。肥厚型梗阻性心肌病患者禁用。",
            attention = "肾功能不全者需减量。治疗窗窄，需监测血药浓度。用药期间监测心电图和血钾。",
            adverseReaction = "常见恶心、呕吐、视觉异常（黄视、绿视）。过量可致严重心律失常。",
            interactionText = "与胺碘酮、维拉帕米同用增加地高辛血药浓度。低钾血症增加地高辛毒性。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别地高辛片，治疗窗窄，需监测血药浓度和心电图。",
            sourceTag = "seed_demo"
        ),
        // ===== 降糖药 =====
        DrugDetailEntity(
            drugId = 1044L,
            composition = "本品主要成分为格列美脲。",
            indication = "用于饮食控制及运动疗法无效的2型糖尿病患者的血糖控制。",
            usageAndDosage = "起始剂量一日1-2mg，根据血糖调整，最大日剂量6mg，早餐前服用。",
            taboo = "1型糖尿病、糖尿病酮症酸中毒患者禁用。严重肝功能不全者禁用。",
            attention = "老年人应从低剂量起始。用药期间需规律进食，避免低血糖。",
            adverseReaction = "常见低血糖。偶见胃肠道不适、体重增加。",
            interactionText = "与胰岛素、二甲双胍联用增加低血糖风险。与β受体阻滞剂可掩盖低血糖症状。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别格列美脲片，磺脲类降糖药，注意低血糖风险。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1045L,
            composition = "本品主要成分为利拉鲁肽。",
            indication = "用于2型糖尿病患者的血糖控制。",
            usageAndDosage = "起始剂量一日0.6mg，一周后增至1.2mg，最大1.8mg，皮下注射每日一次。",
            taboo = "有甲状腺髓样癌个人或家族史者禁用。1型糖尿病患者禁用。",
            attention = "有胰腺炎病史者慎用。胃肠道严重疾病患者慎用。",
            adverseReaction = "常见胃肠道反应（恶心、腹泻、呕吐）。偶见低血糖（与磺脲类联用时）。",
            interactionText = "与磺脲类或胰岛素联用时需减少磺脲类或胰岛素剂量。",
            storageMethod = "冷藏保存（2-8℃），首次使用后可室温保存。",
            validPeriod = "24个月。",
            packageInfo = "预填充注射笔。",
            ttsSummary = "已识别利拉鲁肽注射液，GLP-1受体激动剂，注意胃肠道反应。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1046L,
            composition = "本品主要成分为达格列净。",
            indication = "用于2型糖尿病患者的血糖控制，也可用于心力衰竭和慢性肾脏病的治疗。",
            usageAndDosage = "起始剂量一日5mg，可增至10mg，晨服，不受进餐影响。",
            taboo = "1型糖尿病患者禁用。重度肾功能不全（eGFR<25ml/min）禁用。",
            attention = "注意生殖泌尿道感染风险。手术前需暂停用药。需监测血容量状况。",
            adverseReaction = "常见生殖器真菌感染、泌尿道感染。偶见容量不足相关症状。",
            interactionText = "与胰岛素或磺脲类联用增加低血糖风险。与利尿药同用增加脱水风险。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别达格列净片，SGLT2抑制剂，注意泌尿道感染风险。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1047L,
            composition = "本品主要成分为恩格列净。",
            indication = "用于2型糖尿病患者的血糖控制，降低心血管死亡风险。",
            usageAndDosage = "一日一次10mg，可增至25mg，晨服。",
            taboo = "1型糖尿病患者禁用。重度肾功能不全禁用。",
            attention = "监测生殖泌尿道感染。手术前暂停。老年患者注意容量不足。",
            adverseReaction = "常见泌尿道感染、生殖器真菌感染。偶见脱水、低血压。",
            interactionText = "与胰岛素和磺脲类同用需调整剂量。与利尿药同用增加脱水风险。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别恩格列净片，SGLT2抑制剂，有心血管获益。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1048L,
            composition = "本品主要成分为西格列汀。",
            indication = "用于2型糖尿病患者的血糖控制。",
            usageAndDosage = "成人一次100mg，一日1次，可与食物同服或分开服用。",
            taboo = "1型糖尿病患者禁用。对本药过敏者禁用。",
            attention = "肾功能不全者需调整剂量。有胰腺炎病史者慎用。",
            adverseReaction = "偶见头痛、鼻咽炎、上呼吸道感染。罕见急性胰腺炎。",
            interactionText = "与磺脲类或胰岛素同用增加低血糖风险。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别西格列汀片，DPP-4抑制剂，副作用少，耐受性好。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1049L,
            composition = "本品主要成分为沙格列汀。",
            indication = "用于2型糖尿病患者的血糖控制。",
            usageAndDosage = "成人一次5mg，一日1次，不受进餐影响。",
            taboo = "1型糖尿病患者禁用。重度肾功能不全者需调整剂量。",
            attention = "监测急性胰腺炎症状。肝功能不全者需谨慎。",
            adverseReaction = "偶见头痛、鼻咽炎。罕见关节痛、胰腺炎。",
            interactionText = "与CYP3A4强抑制剂同用需减量至2.5mg。与磺脲类同用增加低血糖风险。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别沙格列汀片，DPP-4抑制剂，注意CYP3A4药物相互作用。",
            sourceTag = "seed_demo"
        ),
        // ===== 神经营养药 =====
        DrugDetailEntity(
            drugId = 1050L,
            composition = "本品主要成分为甲钴胺（活性维生素B12）。",
            indication = "用于周围神经病变的治疗，如糖尿病性神经病变、多发性神经炎。",
            usageAndDosage = "成人一次0.5mg，一日3次，口服。",
            taboo = "对甲钴胺过敏者禁用。",
            attention = "长期使用效果不佳者需就医评估。",
            adverseReaction = "偶见食欲不振、恶心、腹泻。罕见皮疹。",
            interactionText = "与其他维生素B12制剂同用需谨慎。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别甲钴胺片，用于神经病变的辅助治疗。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1051L,
            composition = "本品主要成分为α-硫辛酸。",
            indication = "用于糖尿病周围神经病变的辅助治疗。",
            usageAndDosage = "成人一次0.2g，一日2-3次，口服。",
            taboo = "对本药过敏者禁用。",
            attention = "需在医生指导下使用。可能影响血糖水平，糖尿病患者需监测血糖。",
            adverseReaction = "偶见胃肠道不适、皮疹。罕见低血糖。",
            interactionText = "与降糖药同用需监测血糖。与顺铂同用可降低顺铂疗效。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别α-硫辛酸胶囊，抗氧化剂，用于糖尿病神经病变。",
            sourceTag = "seed_demo"
        ),
        // ===== 营养补充剂 =====
        DrugDetailEntity(
            drugId = 1052L,
            composition = "本品主要成分为碳酸钙和维生素D3。",
            indication = "用于钙缺乏症的预防和治疗，如骨质疏松的防治。",
            usageAndDosage = "成人一次1片，一日1-2次，咀嚼或含服。",
            taboo = "高钙血症、高钙尿症患者禁用。维生素D过量者禁用。",
            attention = "长期使用需监测血钙和尿钙水平。肾功能不全者需谨慎。",
            adverseReaction = "偶见便秘、腹胀。长期过量使用可致高钙血症。",
            interactionText = "与四环素类、铁剂同用间隔2小时以上。与噻嗪类利尿药同用增加高钙风险。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "瓶装。",
            ttsSummary = "已识别碳酸钙D3片，用于补钙，注意与其他药物的间隔服用。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1053L,
            composition = "本品主要成分为维生素D3。",
            indication = "用于维生素D缺乏症的预防和治疗，促进钙的吸收。",
            usageAndDosage = "成人一日400-800IU，或遵医嘱，口服。",
            taboo = "高钙血症患者禁用。维生素D过量者禁用。",
            attention = "长期大剂量使用需监测血钙水平。肾功能不全者需调整剂量。",
            adverseReaction = "正常剂量下无明显不良反应。过量可致高钙血症。",
            interactionText = "与噻嗪类利尿药同用增加高钙血症风险。与皮质类固醇同用可减弱维生素D作用。",
            storageMethod = "密封，阴凉干燥处保存。",
            validPeriod = "24个月。",
            packageInfo = "瓶装滴剂。",
            ttsSummary = "已识别维生素D滴剂，促进钙吸收，需注意不要过量。",
            sourceTag = "seed_demo"
        ),
        // ===== 其他 =====
        DrugDetailEntity(
            drugId = 1054L,
            composition = "本品主要成分为吲达帕胺。",
            indication = "用于原发性高血压的治疗。",
            usageAndDosage = "成人一次2.5mg，一日1次，早晨服用。",
            taboo = "严重肾功能不全、严重肝功能不全患者禁用。低钾血症患者禁用。",
            attention = "需监测血钾和尿酸水平。糖尿病患者需监测血糖。",
            adverseReaction = "常见低钾血症。偶见高尿酸血症。",
            interactionText = "与锂剂同用增加锂中毒风险。与其他降压药同用有协同降压作用。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别吲达帕胺片，利尿类降压药，注意血钾监测。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1055L,
            composition = "本品主要成分为尼莫地平。",
            indication = "用于预防和治疗蛛网膜下腔出血后脑血管痉挛及缺血性神经损伤。也用于轻中度高血压。",
            usageAndDosage = "脑血管疾病：一次30mg，一日3次。高血压：一次30mg，一日2-3次。",
            taboo = "严重肝功能不全者禁用。与利福平同用禁忌。",
            attention = "低血压患者慎用。肝功能不全者需减量。",
            adverseReaction = "偶见头痛、面部潮红、血压下降。",
            interactionText = "与CYP3A4抑制剂同用可升高尼莫地平血药浓度。与抗高血压药同用增强降压效果。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别尼莫地平片，用于脑血管痉挛的防治。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 1056L,
            composition = "本品主要成分为长春西汀。",
            indication = "用于改善脑部血液循环障碍引起的各种症状，如记忆力减退、眩晕等。",
            usageAndDosage = "成人一次5mg，一日3次，餐后服用。",
            taboo = "有颅内出血倾向者禁用。妊娠期及哺乳期妇女禁用。",
            attention = "心律失常患者慎用。长期使用需监测肝功能。",
            adverseReaction = "偶见胃肠道不适、口干。罕见血压波动。",
            interactionText = "与抗高血压药同用需谨慎。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别长春西汀片，改善脑循环用药。",
            sourceTag = "seed_demo"
        ),
        // ============================================================
        // 2001-2005. 儿科用药详情
        // ============================================================
        DrugDetailEntity(
            drugId = 2001L,
            composition = "本品为复方制剂，每袋含对乙酰氨基酚125mg、人工牛黄5mg、马来酸氯苯那敏0.5mg。",
            indication = "用于儿童普通感冒及流行性感冒引起的发热、头痛、鼻塞、流涕等症状。",
            usageAndDosage = "儿童一次1袋，一日3次，开水冲服。2岁以下需遵医嘱。",
            taboo = "严重肝肾功能不全者禁用。对本品成分过敏者禁用。",
            attention = "不宜同时服用含相同成分的感冒药。服药期间不宜驾驶或操作精密仪器。2岁以下婴幼儿应在医师指导下使用。",
            adverseReaction = "偶见嗜睡、口干、乏力。罕见皮疹等过敏反应。",
            interactionText = "与其他含对乙酰氨基酚的药物同用增加肝毒性风险。与中枢镇静药同用增强嗜睡作用。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "复合膜袋装。",
            ttsSummary = "已识别小儿氨酚黄那敏颗粒，儿童感冒用药，注意嗜睡副作用。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 2002L,
            composition = "本品主要成分为氯化铵、桔梗流浸膏、甘草流浸膏等。",
            indication = "用于小儿感冒引起的咳嗽、咳痰等症状。",
            usageAndDosage = "2-5岁一次5ml，6-12岁一次10ml，一日3次，口服。",
            taboo = "对本品成分过敏者禁用。严重肝肾功能不全者禁用。",
            attention = "糖尿病患者慎用。服药期间多饮水。不宜长期服用。",
            adverseReaction = "偶见恶心、呕吐等胃肠道反应。",
            interactionText = "与强力镇咳药同用可能影响排痰。",
            storageMethod = "密封，阴凉处保存。",
            validPeriod = "24个月。",
            packageInfo = "玻璃瓶装。",
            ttsSummary = "已识别小儿止咳糖浆，儿童祛痰止咳用药。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 2003L,
            composition = "本品主要成分为蒙脱石。",
            indication = "用于儿童及成人腹泻的辅助治疗，也可用于食管反流性胃炎。",
            usageAndDosage = "儿童1岁以下一日1袋，1-2岁一日1-2袋，2岁以上一日2-3袋，分3次服用。",
            taboo = "对蒙脱石过敏者禁用。肠梗阻患者禁用。",
            attention = "建议餐前服用。服用后2小时内不宜服用其他药物。如需服用抗生素需间隔2小时。",
            adverseReaction = "偶见便秘。长期过量使用可能导致便秘。",
            interactionText = "与其他药物同服需间隔2小时以上，以免影响药物吸收。",
            storageMethod = "密封保存。",
            validPeriod = "36个月。",
            packageInfo = "复合膜袋装。",
            ttsSummary = "已识别小儿蒙脱石散，用于儿童止泻，需注意与其他药物间隔服用。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 2004L,
            composition = "本品主要成分为布洛芬。",
            indication = "用于婴幼儿和儿童的发热及轻至中度疼痛。",
            usageAndDosage = "按体重5-10mg/kg，每6-8小时一次，24小时不超过4次。使用前摇匀。",
            taboo = "对布洛芬过敏者禁用。消化道溃疡活动期禁用。严重肝肾功能不全禁用。",
            attention = "6个月以下婴幼儿需在医师指导下使用。使用时注意补液。不能与含相同成分药物同用。",
            adverseReaction = "偶见胃肠道不适、皮疹。罕见肾功能损害。",
            interactionText = "与其他解热镇痛药同用增加肾毒性风险。与抗凝药同用增加出血风险。",
            storageMethod = "密封，避光保存。",
            validPeriod = "24个月。",
            packageInfo = "塑料瓶装，附量杯。",
            ttsSummary = "已识别小儿布洛芬混悬液，婴幼儿退热镇痛用药，需按体重计算剂量。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 2005L,
            composition = "本品主要成分为阿莫西林。",
            indication = "用于敏感菌所致的儿童呼吸道感染、泌尿道感染、皮肤软组织感染等。",
            usageAndDosage = "儿童一日20-40mg/kg，分3次服用，或遵医嘱。",
            taboo = "青霉素过敏者禁用。传染性单核细胞增多症患者禁用。",
            attention = "首次使用前需做青霉素皮试。用药期间如出现皮疹、发热需停药。",
            adverseReaction = "常见腹泻、恶心、皮疹。偶见过敏性休克。",
            interactionText = "与丙磺舒同用可延长阿莫西林作用时间。与华法林同用增强抗凝作用。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "复合膜袋装。",
            ttsSummary = "已识别小儿阿莫西林颗粒，儿童抗生素，需确认青霉素过敏史。",
            sourceTag = "seed_demo"
        ),
        // ============================================================
        // 2006-2010. 妇科用药详情
        // ============================================================
        DrugDetailEntity(
            drugId = 2006L,
            composition = "本品主要成分为乌鸡、人参、黄芪、当归、白芍、熟地黄、川芎、丹参、鹿角胶等。",
            indication = "用于气血两虚引起的月经不调、腰膝酸软、带下清稀等症。",
            usageAndDosage = "一次1丸，一日2次，温开水送服。",
            taboo = "妊娠期妇女禁用。对本药成分过敏者禁用。",
            attention = "感冒发热期间暂停服用。糖尿病患者慎用。月经量过多者建议在医师指导下使用。",
            adverseReaction = "偶见胃肠道不适。罕见过敏反应。",
            interactionText = "与其他中药同用前需咨询医师。",
            storageMethod = "密封，阴凉干燥处保存。",
            validPeriod = "36个月。",
            packageInfo = "塑料球壳包装。",
            ttsSummary = "已识别乌鸡白凤丸，用于气血两虚引起的妇科症状。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 2007L,
            composition = "本品主要成分为益母草。",
            indication = "用于血瘀所致的月经不调、产后恶露不绝、小腹疼痛等症。",
            usageAndDosage = "一次1袋，一日2次，开水冲服。",
            taboo = "妊娠期妇女禁用。对本品过敏者禁用。",
            attention = "月经过多者慎用。服药期间如月经量过多需及时就医。",
            adverseReaction = "偶见胃肠道不适。",
            interactionText = "与抗凝药同用需谨慎。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "复合膜袋装。",
            ttsSummary = "已识别益母草颗粒，用于月经不调和产后调理。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 2008L,
            composition = "本品主要成分为千斤拔、金樱根、鸡血藤、当归、党参等。",
            indication = "用于湿热瘀阻所致的带下量多、色黄质稠、小腹疼痛等妇科炎症症状。",
            usageAndDosage = "一次6片，一日3次，口服。",
            taboo = "对本药成分过敏者禁用。妊娠期妇女禁用。",
            attention = "寒湿带下者不宜使用。服药期间忌辛辣、生冷、油腻食物。",
            adverseReaction = "偶见胃肠道不适。罕见过敏反应。",
            interactionText = "与其他药物联用前需咨询医师。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别妇科千金片，用于妇科炎症的辅助治疗。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 2009L,
            composition = "本品主要成分为甲硝唑。",
            indication = "用于滴虫性阴道炎及细菌性阴道病的局部治疗。",
            usageAndDosage = "阴道给药，一次1枚，一日1次，睡前使用，连用7-10天。",
            taboo = "对甲硝唑过敏者禁用。妊娠期前三个月禁用。",
            attention = "用药期间避免性生活。月经期不停药。如有局部刺激症状需停药。",
            adverseReaction = "偶见局部刺激、烧灼感。罕见过敏反应。",
            interactionText = "与华法林同用增强抗凝作用。与酒精同用引起双硫仑样反应。",
            storageMethod = "密封，阴凉处保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别甲硝唑栓，用于滴虫性阴道炎局部治疗。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 2010L,
            composition = "本品主要成分为克霉唑。",
            indication = "用于念珠菌性阴道炎（霉菌性阴道炎）的局部治疗。",
            usageAndDosage = "阴道给药，一次1枚，一日1次，睡前使用，连用7天。",
            taboo = "对克霉唑过敏者禁用。妊娠期前三个月禁用。",
            attention = "用药期间避免性生活。如局部刺激症状持续需停药就医。",
            adverseReaction = "偶见局部烧灼感、瘙痒加重。罕见过敏反应。",
            interactionText = "与口服抗凝药同用需监测凝血功能。",
            storageMethod = "密封，阴凉处保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别克霉唑栓，用于霉菌性阴道炎局部治疗。",
            sourceTag = "seed_demo"
        ),
        // ============================================================
        // 2011-2015. 皮肤科用药详情
        // ============================================================
        DrugDetailEntity(
            drugId = 2011L,
            composition = "本品主要成分为醋酸地塞米松、樟脑、薄荷脑。",
            indication = "用于局限性瘙痒性皮肤病、神经性皮炎、接触性皮炎、慢性湿疹等。",
            usageAndDosage = "外用，涂患处，一日1-2次。",
            taboo = "对皮质类固醇过敏者禁用。病毒性皮肤病患者禁用（如疱疹、水痘）。",
            attention = "不宜长期大面积使用。面部、皮肤褶皱处慎用。如需连续使用超过2周需在医师指导下进行。",
            adverseReaction = "长期使用可引起局部皮肤萎缩、毛细血管扩张。偶见皮肤刺激。",
            interactionText = "与其他外用药物同用时需间隔使用。",
            storageMethod = "密封，阴凉处保存。",
            validPeriod = "24个月。",
            packageInfo = "铝管包装。",
            ttsSummary = "已识别皮炎平，外用皮质类固醇，不宜长期大面积使用。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 2012L,
            composition = "本品主要成分为曲安奈德和硝酸益康唑。",
            indication = "用于伴有真菌感染或真菌感染倾向的皮炎、湿疹、手足癣等。",
            usageAndDosage = "外用，涂患处，一日2次，连续使用不超过2周。",
            taboo = "对咪唑类抗真菌药或皮质类固醇过敏者禁用。病毒性皮肤病禁用。",
            attention = "面部、眼部、黏膜部位禁用。不可长期大面积使用。儿童需在医师指导下使用。",
            adverseReaction = "偶见皮肤刺激、烧灼感。长期使用可致皮肤萎缩。",
            interactionText = "与其他外用制剂同用需咨询医师。",
            storageMethod = "密封，阴凉处保存。",
            validPeriod = "24个月。",
            packageInfo = "铝管包装。",
            ttsSummary = "已识别派瑞松，含抗真菌和皮质类固醇成分，用于皮炎合并真菌感染。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 2013L,
            composition = "本品主要成分为莫匹罗星。",
            indication = "用于革兰阳性球菌引起的皮肤感染，如脓疱疮、毛囊炎、疖肿等。",
            usageAndDosage = "外用，涂患处，一日3次，5天一疗程。",
            taboo = "对莫匹罗星过敏者禁用。严重烧伤创面慎用。",
            attention = "仅限外用，避免接触眼睛。肾功能不全者大面积使用需谨慎。",
            adverseReaction = "偶见局部瘙痒、皮疹等过敏反应。",
            interactionText = "与其他外用药物同用需间隔使用。",
            storageMethod = "密封，阴凉处保存。",
            validPeriod = "24个月。",
            packageInfo = "铝管包装。",
            ttsSummary = "已识别百多邦，外用抗生素，用于皮肤细菌感染。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 2014L,
            composition = "本品主要成分为硝酸咪康唑。",
            indication = "用于体癣、股癣、手足癣、花斑癣及念珠菌性皮肤感染等。",
            usageAndDosage = "外用，涂患处，一日2次，连续使用2-4周。",
            taboo = "对咪康唑过敏者禁用。",
            attention = "仅限外用，避免接触眼睛和口腔黏膜。症状消失后继续用药1周以防复发。",
            adverseReaction = "偶见局部刺激、烧灼感。罕见过敏性皮炎。",
            interactionText = "与华法林同用需监测INR。与其他外用制剂联用需咨询医师。",
            storageMethod = "密封，阴凉处保存。",
            validPeriod = "36个月。",
            packageInfo = "铝管包装。",
            ttsSummary = "已识别达克宁，外用抗真菌药，用于皮肤真菌感染和脚气。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 2015L,
            composition = "本品主要成分为酮康唑和丙酸氯倍他索。",
            indication = "用于体癣、股癣、手足癣等真菌感染及湿疹样皮炎继发真菌感染。",
            usageAndDosage = "外用，涂患处，一日2次，连续使用不超过2周。",
            taboo = "对酮康唑或皮质类固醇过敏者禁用。病毒性皮肤病禁用。",
            attention = "面部、腋下等皮肤薄嫩部位慎用。不可长期大面积使用。避免接触眼睛。",
            adverseReaction = "偶见皮肤刺激、色素沉着。长期使用可致皮肤萎缩。",
            interactionText = "与其他外用制剂同用需咨询医师。",
            storageMethod = "密封，阴凉处保存。",
            validPeriod = "24个月。",
            packageInfo = "铝管包装。",
            ttsSummary = "已识别复方酮康唑软膏，含抗真菌和激素成分，用于真菌感染性皮肤病。",
            sourceTag = "seed_demo"
        ),
        // ============================================================
        // 2016-2019. 眼科用药详情
        // ============================================================
        DrugDetailEntity(
            drugId = 2016L,
            composition = "本品主要成分为左氧氟沙星。",
            indication = "用于敏感菌所致的眼睑炎、睑腺炎、结膜炎、角膜炎等眼部感染。",
            usageAndDosage = "滴眼，一次1-2滴，一日3-5次，严重时可增加频率。",
            taboo = "对喹诺酮类药物过敏者禁用。",
            attention = "滴眼时瓶口勿触及眼睛。隐形眼镜佩戴者需摘镜后使用。用药后视力模糊时避免驾驶。",
            adverseReaction = "偶见眼部刺激、眼痛、眼痒。罕见过敏反应。",
            interactionText = "与其他滴眼液同用需间隔5分钟以上。",
            storageMethod = "密封，避光保存。",
            validPeriod = "24个月。开封后4周内使用。",
            packageInfo = "塑料滴眼瓶。",
            ttsSummary = "已识别左氧氟沙星滴眼液，用于眼部细菌感染。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 2017L,
            composition = "本品主要成分为妥布霉素。",
            indication = "用于敏感菌所致的眼外部感染及眼内感染。",
            usageAndDosage = "滴眼，轻至中度感染一次1-2滴，每4小时1次；严重感染一次2滴，每小时1次。",
            taboo = "对氨基糖苷类药物过敏者禁用。",
            attention = "长期使用需监测肾功能和听力。瓶口勿接触眼部。",
            adverseReaction = "偶见眼睑过敏、眼痛。长期使用可能导致菌群失调。",
            interactionText = "与其他滴眼液同用需间隔5分钟。",
            storageMethod = "密封，避光保存。",
            validPeriod = "24个月。开封后4周内使用。",
            packageInfo = "塑料滴眼瓶。",
            ttsSummary = "已识别妥布霉素滴眼液，氨基糖苷类抗生素滴眼液。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 2018L,
            composition = "本品主要成分为玻璃酸钠。",
            indication = "用于干眼症、角结膜上皮损伤、角膜干燥综合征等。",
            usageAndDosage = "滴眼，一次1滴，一日5-6次，可根据症状适当增减。",
            taboo = "对玻璃酸钠过敏者禁用。",
            attention = "滴眼时瓶口勿触及眼睛。佩戴隐形眼镜者需摘镜后使用。开封后应尽快使用。",
            adverseReaction = "偶见眼部刺激、异物感、眼痒。",
            interactionText = "与其他滴眼液同用需间隔5分钟。",
            storageMethod = "密封，避光保存。",
            validPeriod = "24个月。开封后4周内使用。",
            packageInfo = "塑料滴眼瓶。",
            ttsSummary = "已识别玻璃酸钠滴眼液，人工泪液，用于干眼症。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 2019L,
            composition = "本品主要成分为聚乙烯醇。",
            indication = "用于干眼症和眼部干涩的辅助治疗，保护角膜。",
            usageAndDosage = "滴眼，一次1-2滴，一日3-4次，按需使用。",
            taboo = "对聚乙烯醇过敏者禁用。",
            attention = "滴眼时瓶口勿触及眼睛。佩戴隐形眼镜者需摘镜后使用。开封后一次性使用。",
            adverseReaction = "偶见眼部刺激、异物感。",
            interactionText = "与其他滴眼液同用需间隔5分钟。",
            storageMethod = "密封，避光保存。",
            validPeriod = "24个月。单支包装开封即用。",
            packageInfo = "单剂量塑料滴眼支。",
            ttsSummary = "已识别聚乙烯醇滴眼液，人工泪液，用于缓解眼干涩。",
            sourceTag = "seed_demo"
        ),
        // ============================================================
        // 2020-2023. 耳鼻喉科用药详情
        // ============================================================
        DrugDetailEntity(
            drugId = 2020L,
            composition = "本品主要成分为氯霉素。",
            indication = "用于敏感菌所致的外耳道炎、急慢性中耳炎的局部治疗。",
            usageAndDosage = "滴耳，一次2-3滴，一日3次，滴耳后保持侧卧位5-10分钟。",
            taboo = "对氯霉素过敏者禁用。妊娠期和哺乳期妇女禁用。",
            attention = "长期使用需监测血象，警惕骨髓抑制风险。儿童慎用。",
            adverseReaction = "局部刺激感。长期使用罕见骨髓抑制。",
            interactionText = "与其他耳用制剂同用需间隔使用。",
            storageMethod = "密封，避光保存。",
            validPeriod = "24个月。开封后4周内使用。",
            packageInfo = "塑料滴耳瓶。",
            ttsSummary = "已识别氯霉素滴耳液，用于中耳炎局部治疗，注意骨髓抑制风险。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 2021L,
            composition = "本品主要成分为氧氟沙星。",
            indication = "用于敏感菌所致的外耳道炎、化脓性中耳炎、鼓膜炎等。",
            usageAndDosage = "滴耳，成人一次6-10滴，儿童适当减量，一日2次，滴后侧卧5分钟。",
            taboo = "对喹诺酮类药物过敏者禁用。",
            attention = "滴耳液温度宜接近体温，避免过冷引起眩晕。长期使用需警惕菌群失调。",
            adverseReaction = "偶见局部刺激、耳痛、瘙痒。",
            interactionText = "与其他耳用制剂同用需咨询医师。",
            storageMethod = "密封，避光保存。",
            validPeriod = "24个月。开封后4周内使用。",
            packageInfo = "塑料滴耳瓶。",
            ttsSummary = "已识别氧氟沙星滴耳液，喹诺酮类抗生素滴耳液，用于中耳炎。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 2022L,
            composition = "本品主要成分为黄芪、白术、防风、白芷、辛夷、苍耳子等。",
            indication = "用于风热上攻、肺气不宣所致的过敏性鼻炎、慢性鼻炎、鼻窦炎等。",
            usageAndDosage = "一次5-7片，一日3次，口服。",
            taboo = "对本药成分过敏者禁用。妊娠期妇女慎用。",
            attention = "感冒发热期间暂停服用。服药期间忌辛辣、油腻食物。",
            adverseReaction = "偶见胃肠道不适。罕见过敏反应。",
            interactionText = "与其他抗过敏药物同用需咨询医师。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别通窍鼻炎片，用于鼻炎和过敏性鼻炎。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 2023L,
            composition = "本品主要成分为黄芩提取物、苍耳子提取物、鹅不食草提取物、马来酸氯苯那敏等。",
            indication = "用于急慢性鼻炎、过敏性鼻炎引起的鼻塞、流涕、打喷嚏等症状。",
            usageAndDosage = "一次4片，一日3次，口服。",
            taboo = "对本药成分过敏者禁用。妊娠期妇女慎用。",
            attention = "服药期间不宜驾驶车辆或操作精密仪器（含抗组胺成分）。",
            adverseReaction = "常见嗜睡、口干。偶见胃肠道不适。",
            interactionText = "与中枢镇静药同用增强嗜睡作用。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别鼻炎康片，含抗组胺成分，治疗鼻炎引起的不适。",
            sourceTag = "seed_demo"
        ),
        // ============================================================
        // 2024-2028. 消化科用药详情
        // ============================================================
        DrugDetailEntity(
            drugId = 2024L,
            composition = "本品主要成分为蒙脱石。",
            indication = "用于成人及儿童急慢性腹泻的辅助治疗。",
            usageAndDosage = "成人一次3g（1袋），一日3次，溶于50ml温水中口服。急性腹泻首剂可加倍。",
            taboo = "对蒙脱石过敏者禁用。肠梗阻患者禁用。",
            attention = "建议餐前服用。如需服用抗生素需间隔2小时。腹泻期间注意补液。",
            adverseReaction = "偶见便秘。长期过量使用可致便秘。",
            interactionText = "与其他药物同服需间隔2小时以上。",
            storageMethod = "密封保存。",
            validPeriod = "36个月。",
            packageInfo = "复合膜袋装。",
            ttsSummary = "已识别蒙脱石散，用于止泻，需与其它药物间隔服用。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 2025L,
            composition = "本品主要成分为双歧杆菌、乳杆菌、粪肠球菌、蜡样芽孢杆菌等四联活菌。",
            indication = "用于肠道菌群失调引起的腹泻、便秘、腹胀、消化不良等。",
            usageAndDosage = "成人一次1.5-2g，一日2次，温水送服，或遵医嘱。",
            taboo = "对本品成分过敏者禁用。",
            attention = "不宜与抗生素同时服用，需间隔2小时以上。水温不宜超过40℃。",
            adverseReaction = "偶见轻度腹胀。一般可自行缓解。",
            interactionText = "与抗生素同用需间隔2小时。与铋剂、鞣酸同用影响疗效。",
            storageMethod = "冷藏保存（2-8℃）。",
            validPeriod = "12个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别双歧杆菌四联活菌片，益生菌制剂，需冷藏保存。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 2026L,
            composition = "本品主要成分为乳果糖。",
            indication = "用于慢性功能性便秘的治疗，也可用于肝性脑病的辅助治疗。",
            usageAndDosage = "成人起始剂量一日15-30ml，维持剂量一日15ml，早餐时一次性服用。",
            taboo = "半乳糖血症患者禁用。肠梗阻患者禁用。对本品过敏者禁用。",
            attention = "糖尿病患者可按推荐剂量使用。服药后如出现剧烈腹痛需停药。",
            adverseReaction = "初期使用偶见腹胀、胃肠胀气。剂量过大可致腹泻。",
            interactionText = "与其他泻药同用需咨询医师。",
            storageMethod = "密封，阴凉处保存。",
            validPeriod = "36个月。",
            packageInfo = "铝塑袋装或塑料瓶装。",
            ttsSummary = "已识别乳果糖口服液，用于慢性便秘的辅助治疗。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 2027L,
            composition = "本品主要成分为甘油或山梨醇。",
            indication = "用于轻度便秘的临时缓解。",
            usageAndDosage = "外用，将本品注入肛门，成人一次20ml，儿童一次10ml。",
            taboo = "肛门破裂或直肠黏膜损伤者慎用。对本品成分过敏者禁用。",
            attention = "仅用于临时缓解便秘，不宜长期使用。如连续使用3天无效需就医。",
            adverseReaction = "偶见肛门刺激不适感。",
            interactionText = "与其他口服药物无明显相互作用。",
            storageMethod = "密封保存。",
            validPeriod = "36个月。",
            packageInfo = "塑料容器包装。",
            ttsSummary = "已识别开塞露，外用通便药，用于临时缓解便秘。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 2028L,
            composition = "本品主要成分为胃蛋白酶、胰蛋白酶、胰淀粉酶、胰脂肪酶等消化酶。",
            indication = "用于消化酶缺乏引起的消化不良、食欲不振、腹胀等症状。",
            usageAndDosage = "成人一次2-3片，一日3次，饭前或饭时服用。",
            taboo = "对本品成分过敏者禁用。急性胰腺炎早期禁用。",
            attention = "不宜嚼碎服用。胃酸过多者慎用。",
            adverseReaction = "偶见胃肠道不适。罕见过敏反应。",
            interactionText = "与抗酸药同用可降低疗效。与碱性药物同用影响药效。",
            storageMethod = "密封，阴凉干燥处保存。",
            validPeriod = "24个月。",
            packageInfo = "塑料瓶装。",
            ttsSummary = "已识别多酶片，含多种消化酶，用于消化不良。",
            sourceTag = "seed_demo"
        ),
        // ============================================================
        // 2029-2033. 呼吸科用药详情
        // ============================================================
        DrugDetailEntity(
            drugId = 2029L,
            composition = "本品主要成分为盐酸氨溴索。",
            indication = "用于急慢性呼吸道疾病引起的痰液黏稠、咳痰困难。",
            usageAndDosage = "成人一次30mg，一日3次，餐后服用。",
            taboo = "对氨溴索过敏者禁用。妊娠期前三个月慎用。",
            attention = "与镇咳药合用需谨慎。胃溃疡患者慎用。",
            adverseReaction = "偶见胃肠道不适、恶心。罕见过敏反应。",
            interactionText = "与抗生素同用可增加抗生素在肺部的浓度。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别氨溴索片，用于祛痰，缓解痰液黏稠。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 2030L,
            composition = "本品主要成分为氢溴酸右美沙芬。",
            indication = "用于干咳（无痰或少痰）的镇咳治疗，如感冒引起的刺激性干咳。",
            usageAndDosage = "成人一次15mg，一日3-4次，口服。",
            taboo = "妊娠期前三个月禁用。有精神病史者禁用。痰多者不宜使用。",
            attention = "不宜长期使用（不超过7天）。服药期间不宜驾驶或操作精密仪器。",
            adverseReaction = "偶见头晕、嗜睡、恶心。罕见呼吸抑制。",
            interactionText = "与MAO抑制剂同用禁忌。与中枢镇静药同用增强镇静作用。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别右美沙芬片，用于干咳的镇咳治疗。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 2031L,
            composition = "本品为复方制剂，含甘草浸膏粉、阿片粉、樟脑、八角茴香油等。",
            indication = "用于上呼吸道感染、支气管炎引起的咳嗽、咳痰症状。",
            usageAndDosage = "成人一次3-4片，一日3次，含服或口服。",
            taboo = "妊娠期及哺乳期妇女禁用。严重呼吸抑制患者禁用。对本药成分过敏者禁用。",
            attention = "含阿片粉，有成瘾性，不宜长期使用。服药期间不宜驾驶或操作精密仪器。",
            adverseReaction = "偶见头晕、嗜睡。长期使用可产生依赖性。",
            interactionText = "与中枢镇静药同用增强镇静作用。与MAO抑制剂同用需谨慎。",
            storageMethod = "密封保存。",
            validPeriod = "36个月。",
            packageInfo = "塑料瓶装。",
            ttsSummary = "已识别复方甘草片，镇咳祛痰药，含阿片成分不宜长期使用。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 2032L,
            composition = "本品主要成分为孟鲁司特钠。",
            indication = "用于预防和长期治疗支气管哮喘，缓解过敏性鼻炎症状。",
            usageAndDosage = "成人一次10mg，一日1次，睡前服用。",
            taboo = "对孟鲁司特钠过敏者禁用。",
            attention = "不用于哮喘急性发作的急救治疗。如需减量停药需逐渐进行。",
            adverseReaction = "偶见头痛、腹痛、口渴。罕见神经精神事件。",
            interactionText = "与苯巴比妥同用可降低孟鲁司特血药浓度。",
            storageMethod = "密封，避光保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别孟鲁司特钠片，用于哮喘和过敏性鼻炎的长期控制。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 2033L,
            composition = "本品主要成分为沙丁胺醇。",
            indication = "用于支气管哮喘和喘息性支气管炎的急性症状缓解。",
            usageAndDosage = "急性发作时吸入1-2喷，必要时每4-6小时重复。维持治疗一日3-4次。",
            taboo = "对沙丁胺醇过敏者禁用。严重心律失常患者慎用。",
            attention = "频繁使用提示哮喘控制不佳，需就医调整治疗方案。",
            adverseReaction = "常见心悸、震颤、头痛。偶见肌肉痉挛。",
            interactionText = "与β受体阻滞剂同用减弱支气管扩张作用。与利尿药同用需监测血钾。",
            storageMethod = "避光，阴凉处保存。",
            validPeriod = "24个月。",
            packageInfo = "铝罐气雾剂。",
            ttsSummary = "已识别沙丁胺醇气雾剂，哮喘急性发作的急救用药。",
            sourceTag = "seed_demo"
        ),
        // ============================================================
        // 2034-2037. 骨科/止痛用药详情
        // ============================================================
        DrugDetailEntity(
            drugId = 2034L,
            composition = "本品主要成分布洛芬。",
            indication = "用于局部缓解肌肉痛、关节痛、扭伤和劳损引起的疼痛。",
            usageAndDosage = "外用，涂患处轻轻按摩，一日3-4次。",
            taboo = "对布洛芬或NSAIDs过敏者禁用。破损皮肤禁用。",
            attention = "仅限外用，避免接触眼睛和黏膜。不宜大面积长期使用。",
            adverseReaction = "偶见局部皮肤瘙痒、发红。罕见过敏反应。",
            interactionText = "与其他外用制剂同用需间隔使用。",
            storageMethod = "密封，阴凉处保存。",
            validPeriod = "24个月。",
            packageInfo = "铝管包装。",
            ttsSummary = "已识别布洛芬凝胶，外用止痛药，用于局部肌肉关节痛。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 2035L,
            composition = "本品主要成分为双氯芬酸二乙胺。",
            indication = "用于局部缓解关节炎、肌肉劳损、扭伤和腱鞘炎引起的疼痛。",
            usageAndDosage = "外用，涂患处一日3-4次，轻轻按摩。",
            taboo = "对双氯芬酸或NSAIDs过敏者禁用。破损皮肤禁用。妊娠期后三个月禁用。",
            attention = "仅限外用，避免接触眼睛。不宜长期大面积使用。",
            adverseReaction = "偶见局部皮肤过敏。罕见面部水肿。",
            interactionText = "与其他外用制剂同用需咨询医师。",
            storageMethod = "密封，阴凉处保存。",
            validPeriod = "24个月。",
            packageInfo = "铝管包装。",
            ttsSummary = "已识别扶他林乳胶剂，外用消炎镇痛药。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 2036L,
            composition = "本品主要成分为三七、麝香等。",
            indication = "用于跌打损伤、瘀血肿痛、吐血、咳血及内出血的辅助治疗。",
            usageAndDosage = "一次1-2粒，一日4次，口服。或遵医嘱。",
            taboo = "妊娠期妇女禁用。对本药成分过敏者禁用。",
            attention = "儿童慎用。出血量多者需及时就医。不宜与其他活血化瘀药同时大量使用。",
            adverseReaction = "偶见胃肠道不适。罕见过敏反应。",
            interactionText = "与抗凝药（华法林等）同用增加出血风险。",
            storageMethod = "密封，阴凉干燥处保存。",
            validPeriod = "36个月。",
            packageInfo = "塑料瓶装。",
            ttsSummary = "已识别云南白药胶囊，用于跌打损伤和化瘀止血。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 2037L,
            composition = "本品主要成分为活血化瘀类中药提取物，含当归、川芎、红花、冰片等。",
            indication = "用于跌打损伤、瘀血肿痛、风湿关节痛等的辅助治疗。",
            usageAndDosage = "外用，贴患处，每次1贴，一日1次。",
            taboo = "破损皮肤禁用。对本品成分过敏者禁用。妊娠期妇女禁用。",
            attention = "皮肤过敏者慎用。使用中如出现皮疹需暂停使用。",
            adverseReaction = "偶见局部皮肤瘙痒、发红。",
            interactionText = "与其他外用制剂同用需咨询医师。",
            storageMethod = "密封，阴凉干燥处保存。",
            validPeriod = "36个月。",
            packageInfo = "复合膜袋装。",
            ttsSummary = "已识别活血止痛膏，外用贴膏，用于跌打损伤和关节痛。",
            sourceTag = "seed_demo"
        ),
        // ============================================================
        // 2038-2042. 外用药详情
        // ============================================================
        DrugDetailEntity(
            drugId = 2038L,
            composition = "本品主要成分为聚维酮碘。",
            indication = "用于皮肤黏膜消毒，如手术前消毒、伤口消毒、皮肤感染消毒等。",
            usageAndDosage = "外用，用棉签蘸取适量涂于消毒部位。",
            taboo = "对碘过敏者禁用。甲状腺疾病患者慎用。",
            attention = "仅限外用，不可口服。大面积使用需在医师指导下进行。",
            adverseReaction = "偶见局部皮肤过敏反应。长期使用可致皮肤色素沉着。",
            interactionText = "与碱性药物同用降低消毒效果。与汞制剂同用产生腐蚀作用。",
            storageMethod = "密封，避光保存。",
            validPeriod = "24个月。",
            packageInfo = "塑料瓶装。",
            ttsSummary = "已识别碘伏，外用消毒剂，用于皮肤黏膜消毒。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 2039L,
            composition = "本品主要成分为乙醇，浓度为75%。",
            indication = "用于皮肤消毒和小面积伤口消毒。",
            usageAndDosage = "外用，用棉签蘸取适量涂于消毒部位。",
            taboo = "对酒精过敏者禁用。破损黏膜及创面慎用。",
            attention = "易燃，远离火源。仅限外用，不可口服。避免接触眼睛和黏膜。",
            adverseReaction = "偶见局部皮肤刺激。长期使用可致皮肤干燥。",
            interactionText = "与碘伏等其他消毒剂同用无显著相互作用。",
            storageMethod = "密封，避火保存。",
            validPeriod = "24个月。",
            packageInfo = "塑料瓶装。",
            ttsSummary = "已识别医用酒精，外用消毒剂，注意易燃需远离火源。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 2040L,
            composition = "本品由弹性织物和含苯扎氯铵的黏合剂组成。",
            indication = "用于小面积表浅伤口、擦伤的临时止血和保护。",
            usageAndDosage = "外用，清洁伤口后撕去覆盖膜，将吸收垫贴于伤口处贴牢。",
            taboo = "对苯扎氯铵过敏者禁用。严重出血或感染伤口不宜使用。",
            attention = "仅供一次性使用。如伤口感染需及时就医。不适用于大伤口或深度伤口。",
            adverseReaction = "偶见局部胶布过敏反应。",
            interactionText = "无明显药物相互作用。",
            storageMethod = "密封，阴凉干燥处保存。",
            validPeriod = "36个月。",
            packageInfo = "纸盒包装。",
            ttsSummary = "已识别创可贴，用于小伤口的临时保护。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 2041L,
            composition = "本品主要成分为云南白药提取物、冰片、麝香等。",
            indication = "用于跌打损伤、瘀血肿痛、肌肉酸痛的外用治疗。",
            usageAndDosage = "外用，喷患处，一日3-5次。使用前摇匀。",
            taboo = "妊娠期妇女禁用。破损皮肤禁用。对本品成分过敏者禁用。",
            attention = "仅限外用，避免喷入眼睛和口腔。禁止口服。",
            adverseReaction = "偶见局部皮肤过敏反应。",
            interactionText = "与其他外用制剂同用需咨询医师。",
            storageMethod = "密封，阴凉干燥处保存。",
            validPeriod = "36个月。",
            packageInfo = "铝罐气雾剂。",
            ttsSummary = "已识别云南白药气雾剂，外用活血化瘀止痛。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 2042L,
            composition = "本品主要成分为红花油、冬青油、薄荷油等。",
            indication = "用于风湿骨痛、跌打损伤、扭伤、肌肉酸痛等。",
            usageAndDosage = "外用，涂患处，一日3-4次，轻柔按摩至吸收。",
            taboo = "破损皮肤禁用。对本药成分过敏者禁用。妊娠期妇女慎用。",
            attention = "仅限外用，避免接触眼睛和黏膜。不可口服。",
            adverseReaction = "偶见局部皮肤过敏、发红。",
            interactionText = "与其他外用制剂同用需咨询医师。",
            storageMethod = "密封，阴凉处保存。",
            validPeriod = "36个月。",
            packageInfo = "玻璃瓶装或塑料瓶装。",
            ttsSummary = "已识别红花油，外用活血祛风止痛药。",
            sourceTag = "seed_demo"
        ),
        // ============================================================
        // 2043-2047. 罕见病/痛风用药详情
        // ============================================================
        DrugDetailEntity(
            drugId = 2043L,
            composition = "本品主要成分为秋水仙碱。",
            indication = "用于急性痛风性关节炎发作的治疗。",
            usageAndDosage = "急性发作：首剂1mg，之后每2小时0.5mg，至症状缓解或出现胃肠道反应。24小时总量不超过6mg。",
            taboo = "严重肾功能不全患者禁用。妊娠期及哺乳期妇女禁用。",
            attention = "治疗窗窄，出现腹泻、恶心等胃肠道反应应立即停药。肝肾功能不全者需减量。",
            adverseReaction = "常见腹泻、恶心、呕吐、腹痛。长期使用可致骨髓抑制和周围神经炎。",
            interactionText = "与CYP3A4抑制剂（如克拉霉素）同用增加毒性。与环孢素同用增加肾毒性。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别秋水仙碱片，用于急性痛风发作，注意胃肠道毒性反应。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 2044L,
            composition = "本品主要成分别为别嘌醇。",
            indication = "用于高尿酸血症和反复发作的痛风石、痛风性关节炎的长期治疗。",
            usageAndDosage = "起始剂量一日100mg，逐渐增加至一日200-300mg，分2-3次服用。",
            taboo = "对别嘌醇过敏者禁用。严重肝功能不全者禁用。急性痛风发作期不宜使用。",
            attention = "用药前需检测HLA-B*5801基因（亚洲人群过敏风险高）。多饮水（每日2L以上）。",
            adverseReaction = "常见皮疹、胃肠道不适。罕见严重超敏反应综合征（DRESS）。",
            interactionText = "与硫唑嘌呤、6-巯基嘌呤同用禁忌。与华法林同用增强抗凝作用。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别别嘌醇片，用于降尿酸，注意亚洲人群超敏反应风险。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 2045L,
            composition = "本品主要成分为苯溴马隆。",
            indication = "用于高尿酸血症和痛风的长期降尿酸治疗。",
            usageAndDosage = "成人一次50mg，一日1次，早餐后服用。",
            taboo = "严重肾功能不全（eGFR<20ml/min）禁用。妊娠期及哺乳期妇女禁用。",
            attention = "多饮水（每日2L以上）以预防尿酸结石。治疗初期可能诱发痛风发作。",
            adverseReaction = "偶见胃肠道不适、肝功能异常。罕见肾结石。",
            interactionText = "与阿司匹林同用减弱降尿酸效果。与华法林同用需监测INR。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别苯溴马隆片，用于降尿酸，需多饮水预防结石。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 2046L,
            composition = "本品主要成分为甲氨蝶呤。",
            indication = "用于类风湿关节炎、银屑病关节炎等自身免疫性疾病，以及某些肿瘤治疗。",
            usageAndDosage = "类风湿关节炎：一周一次7.5-15mg，分1-2次服用。具体剂量遵医嘱。",
            taboo = "妊娠期及哺乳期妇女禁用。严重肝肾功能不全者禁用。严重贫血患者禁用。",
            attention = "需补充叶酸以减轻副作用。定期监测血常规、肝肾功能。用药后如出现不明原因咳嗽、呼吸困难需警惕肺毒性。",
            adverseReaction = "常见恶心、口腔溃疡、脱发。罕见肝毒性、肺间质纤维化、骨髓抑制。",
            interactionText = "与NSAIDs同用增加肾毒性。与丙磺舒同用增加甲氨蝶呤血药浓度。",
            storageMethod = "密封，避光保存。",
            validPeriod = "24个月。",
            packageInfo = "铝塑包装。",
            ttsSummary = "已识别甲氨蝶呤片，免疫抑制剂/抗肿瘤药，需定期监测血常规和肝肾功能。",
            sourceTag = "seed_demo"
        ),
        DrugDetailEntity(
            drugId = 2047L,
            composition = "本品主要成分为青霉胺。",
            indication = "用于肝豆状核变性（Wilson病）、类风湿关节炎、重金属中毒等。",
            usageAndDosage = "肝豆状核变性：一日0.75-1.5g，分2-4次空腹服用。具体遵医嘱。",
            taboo = "妊娠期妇女禁用。严重肾功能不全者禁用。对青霉胺过敏者禁用。",
            attention = "需定期监测血常规、尿常规和肝功能。如出现蛋白尿或血尿需停药。",
            adverseReaction = "常见皮疹、食欲不振、味觉减退。罕见骨髓抑制、肾病综合征、重症肌无力。",
            interactionText = "与抗疟药、免疫抑制剂同用增加毒性。与铁剂同用需间隔2小时。",
            storageMethod = "密封保存。",
            validPeriod = "24个月。",
            packageInfo = "塑料瓶装。",
            ttsSummary = "已识别青霉胺片，用于肝豆状核变性和类风湿关节炎。",
            sourceTag = "seed_demo"
        )
    )

    private val seedDrugAliases = listOf(
        // 1001 布洛芬
        DrugAliasEntity(drugId = 1001L, aliasName = "布洛芬", aliasType = "short_name", normalizedAlias = "布洛芬"),
        DrugAliasEntity(drugId = 1001L, aliasName = "退烧药", aliasType = "ocr_token", normalizedAlias = "退烧药"),
        DrugAliasEntity(drugId = 1001L, aliasName = "芬必得", aliasType = "trade_name", normalizedAlias = "芬必得"),
        // 1002 阿莫西林
        DrugAliasEntity(drugId = 1002L, aliasName = "阿莫西林", aliasType = "short_name", normalizedAlias = "阿莫西林"),
        DrugAliasEntity(drugId = 1002L, aliasName = "消炎药", aliasType = "ocr_token", normalizedAlias = "消炎药"),
        DrugAliasEntity(drugId = 1002L, aliasName = "阿莫仙", aliasType = "trade_name", normalizedAlias = "阿莫仙"),
        // 1003 二甲双胍
        DrugAliasEntity(drugId = 1003L, aliasName = "二甲双胍", aliasType = "short_name", normalizedAlias = "二甲双胍"),
        DrugAliasEntity(drugId = 1003L, aliasName = "降糖药", aliasType = "ocr_token", normalizedAlias = "降糖药"),
        DrugAliasEntity(drugId = 1003L, aliasName = "格华止", aliasType = "trade_name", normalizedAlias = "格华止"),
        // 1004 对乙酰氨基酚
        DrugAliasEntity(drugId = 1004L, aliasName = "对乙酰氨基酚", aliasType = "short_name", normalizedAlias = "对乙酰氨基酚"),
        DrugAliasEntity(drugId = 1004L, aliasName = "扑热息痛", aliasType = "alias", normalizedAlias = "扑热息痛"),
        DrugAliasEntity(drugId = 1004L, aliasName = "泰诺林", aliasType = "trade_name", normalizedAlias = "泰诺林"),
        // 1005 双氯芬酸钠
        DrugAliasEntity(drugId = 1005L, aliasName = "双氯芬酸钠", aliasType = "short_name", normalizedAlias = "双氯芬酸钠"),
        DrugAliasEntity(drugId = 1005L, aliasName = "扶他林", aliasType = "trade_name", normalizedAlias = "扶他林"),
        DrugAliasEntity(drugId = 1005L, aliasName = "止痛药", aliasType = "ocr_token", normalizedAlias = "止痛药"),
        // 1006 感冒灵
        DrugAliasEntity(drugId = 1006L, aliasName = "感冒灵", aliasType = "short_name", normalizedAlias = "感冒灵"),
        DrugAliasEntity(drugId = 1006L, aliasName = "三九感冒灵", aliasType = "alias", normalizedAlias = "三九感冒灵"),
        DrugAliasEntity(drugId = 1006L, aliasName = "感冒药", aliasType = "ocr_token", normalizedAlias = "感冒药"),
        // 1007 头孢克肟
        DrugAliasEntity(drugId = 1007L, aliasName = "头孢克肟", aliasType = "short_name", normalizedAlias = "头孢克肟"),
        DrugAliasEntity(drugId = 1007L, aliasName = "世福素", aliasType = "trade_name", normalizedAlias = "世福素"),
        DrugAliasEntity(drugId = 1007L, aliasName = "头孢", aliasType = "ocr_token", normalizedAlias = "头孢"),
        // 1008 阿奇霉素
        DrugAliasEntity(drugId = 1008L, aliasName = "阿奇霉素", aliasType = "short_name", normalizedAlias = "阿奇霉素"),
        DrugAliasEntity(drugId = 1008L, aliasName = "希舒美", aliasType = "trade_name", normalizedAlias = "希舒美"),
        DrugAliasEntity(drugId = 1008L, aliasName = "大环内酯", aliasType = "ocr_token", normalizedAlias = "大环内酯"),
        // 1009 头孢拉定
        DrugAliasEntity(drugId = 1009L, aliasName = "头孢拉定", aliasType = "short_name", normalizedAlias = "头孢拉定"),
        DrugAliasEntity(drugId = 1009L, aliasName = "泛捷复", aliasType = "trade_name", normalizedAlias = "泛捷复"),
        // 1010 左氧氟沙星
        DrugAliasEntity(drugId = 1010L, aliasName = "左氧氟沙星", aliasType = "short_name", normalizedAlias = "左氧氟沙星"),
        DrugAliasEntity(drugId = 1010L, aliasName = "可乐必妥", aliasType = "trade_name", normalizedAlias = "可乐必妥"),
        DrugAliasEntity(drugId = 1010L, aliasName = "喹诺酮", aliasType = "ocr_token", normalizedAlias = "喹诺酮"),
        // 1011 氯沙坦钾
        DrugAliasEntity(drugId = 1011L, aliasName = "氯沙坦", aliasType = "short_name", normalizedAlias = "氯沙坦"),
        DrugAliasEntity(drugId = 1011L, aliasName = "科素亚", aliasType = "trade_name", normalizedAlias = "科素亚"),
        DrugAliasEntity(drugId = 1011L, aliasName = "降压药", aliasType = "ocr_token", normalizedAlias = "降压药"),
        // 1012 硝苯地平控释片
        DrugAliasEntity(drugId = 1012L, aliasName = "硝苯地平", aliasType = "short_name", normalizedAlias = "硝苯地平"),
        DrugAliasEntity(drugId = 1012L, aliasName = "拜新同", aliasType = "trade_name", normalizedAlias = "拜新同"),
        DrugAliasEntity(drugId = 1012L, aliasName = "心痛定", aliasType = "alias", normalizedAlias = "心痛定"),
        // 1013 氨氯地平
        DrugAliasEntity(drugId = 1013L, aliasName = "氨氯地平", aliasType = "short_name", normalizedAlias = "氨氯地平"),
        DrugAliasEntity(drugId = 1013L, aliasName = "络活喜", aliasType = "trade_name", normalizedAlias = "络活喜"),
        DrugAliasEntity(drugId = 1013L, aliasName = "苯磺酸氨氯地平", aliasType = "generic_name", normalizedAlias = "苯磺酸氨氯地平"),
        // 1014 厄贝沙坦
        DrugAliasEntity(drugId = 1014L, aliasName = "厄贝沙坦", aliasType = "short_name", normalizedAlias = "厄贝沙坦"),
        DrugAliasEntity(drugId = 1014L, aliasName = "安博维", aliasType = "trade_name", normalizedAlias = "安博维"),
        // 1015 美托洛尔
        DrugAliasEntity(drugId = 1015L, aliasName = "美托洛尔", aliasType = "short_name", normalizedAlias = "美托洛尔"),
        DrugAliasEntity(drugId = 1015L, aliasName = "倍他乐克", aliasType = "trade_name", normalizedAlias = "倍他乐克"),
        DrugAliasEntity(drugId = 1015L, aliasName = "β受体阻滞剂", aliasType = "ocr_token", normalizedAlias = "β受体阻滞剂"),
        // 1016 卡托普利
        DrugAliasEntity(drugId = 1016L, aliasName = "卡托普利", aliasType = "short_name", normalizedAlias = "卡托普利"),
        DrugAliasEntity(drugId = 1016L, aliasName = "开博通", aliasType = "trade_name", normalizedAlias = "开博通"),
        DrugAliasEntity(drugId = 1016L, aliasName = "ACEI", aliasType = "ocr_token", normalizedAlias = "ACEI"),
        // 1017 缬沙坦
        DrugAliasEntity(drugId = 1017L, aliasName = "缬沙坦", aliasType = "short_name", normalizedAlias = "缬沙坦"),
        DrugAliasEntity(drugId = 1017L, aliasName = "代文", aliasType = "trade_name", normalizedAlias = "代文"),
        // 1018 非洛地平
        DrugAliasEntity(drugId = 1018L, aliasName = "非洛地平", aliasType = "short_name", normalizedAlias = "非洛地平"),
        DrugAliasEntity(drugId = 1018L, aliasName = "波依定", aliasType = "trade_name", normalizedAlias = "波依定"),
        DrugAliasEntity(drugId = 1018L, aliasName = "非洛地平缓释片", aliasType = "generic_name", normalizedAlias = "非洛地平缓释片"),
        // 1019 坎地沙坦
        DrugAliasEntity(drugId = 1019L, aliasName = "坎地沙坦", aliasType = "short_name", normalizedAlias = "坎地沙坦"),
        DrugAliasEntity(drugId = 1019L, aliasName = "必洛斯", aliasType = "trade_name", normalizedAlias = "必洛斯"),
        // 1020 替米沙坦
        DrugAliasEntity(drugId = 1020L, aliasName = "替米沙坦", aliasType = "short_name", normalizedAlias = "替米沙坦"),
        DrugAliasEntity(drugId = 1020L, aliasName = "美卡素", aliasType = "trade_name", normalizedAlias = "美卡素"),
        // 1021 奥美沙坦
        DrugAliasEntity(drugId = 1021L, aliasName = "奥美沙坦", aliasType = "short_name", normalizedAlias = "奥美沙坦"),
        DrugAliasEntity(drugId = 1021L, aliasName = "傲坦", aliasType = "trade_name", normalizedAlias = "傲坦"),
        // 1022 比索洛尔
        DrugAliasEntity(drugId = 1022L, aliasName = "比索洛尔", aliasType = "short_name", normalizedAlias = "比索洛尔"),
        DrugAliasEntity(drugId = 1022L, aliasName = "康忻", aliasType = "trade_name", normalizedAlias = "康忻"),
        DrugAliasEntity(drugId = 1022L, aliasName = "富马酸比索洛尔", aliasType = "generic_name", normalizedAlias = "富马酸比索洛尔"),
        // 1023 阿罗洛尔
        DrugAliasEntity(drugId = 1023L, aliasName = "阿罗洛尔", aliasType = "short_name", normalizedAlias = "阿罗洛尔"),
        DrugAliasEntity(drugId = 1023L, aliasName = "阿尔马尔", aliasType = "trade_name", normalizedAlias = "阿尔马尔"),
        // 1024 氢氯噻嗪
        DrugAliasEntity(drugId = 1024L, aliasName = "氢氯噻嗪", aliasType = "short_name", normalizedAlias = "氢氯噻嗪"),
        DrugAliasEntity(drugId = 1024L, aliasName = "双克", aliasType = "trade_name", normalizedAlias = "双克"),
        DrugAliasEntity(drugId = 1024L, aliasName = "利尿药", aliasType = "ocr_token", normalizedAlias = "利尿药"),
        // 1025 呋塞米
        DrugAliasEntity(drugId = 1025L, aliasName = "呋塞米", aliasType = "short_name", normalizedAlias = "呋塞米"),
        DrugAliasEntity(drugId = 1025L, aliasName = "速尿", aliasType = "trade_name", normalizedAlias = "速尿"),
        DrugAliasEntity(drugId = 1025L, aliasName = "利尿剂", aliasType = "ocr_token", normalizedAlias = "利尿剂"),
        // 1026 螺内酯
        DrugAliasEntity(drugId = 1026L, aliasName = "螺内酯", aliasType = "short_name", normalizedAlias = "螺内酯"),
        DrugAliasEntity(drugId = 1026L, aliasName = "安体舒通", aliasType = "trade_name", normalizedAlias = "安体舒通"),
        DrugAliasEntity(drugId = 1026L, aliasName = "保钾利尿", aliasType = "ocr_token", normalizedAlias = "保钾利尿"),
        // 1027 辛伐他汀
        DrugAliasEntity(drugId = 1027L, aliasName = "辛伐他汀", aliasType = "short_name", normalizedAlias = "辛伐他汀"),
        DrugAliasEntity(drugId = 1027L, aliasName = "舒降之", aliasType = "trade_name", normalizedAlias = "舒降之"),
        DrugAliasEntity(drugId = 1027L, aliasName = "他汀", aliasType = "ocr_token", normalizedAlias = "他汀"),
        // 1028 阿托伐他汀
        DrugAliasEntity(drugId = 1028L, aliasName = "阿托伐他汀", aliasType = "short_name", normalizedAlias = "阿托伐他汀"),
        DrugAliasEntity(drugId = 1028L, aliasName = "立普妥", aliasType = "trade_name", normalizedAlias = "立普妥"),
        DrugAliasEntity(drugId = 1028L, aliasName = "降脂药", aliasType = "ocr_token", normalizedAlias = "降脂药"),
        // 1029 瑞舒伐他汀
        DrugAliasEntity(drugId = 1029L, aliasName = "瑞舒伐他汀", aliasType = "short_name", normalizedAlias = "瑞舒伐他汀"),
        DrugAliasEntity(drugId = 1029L, aliasName = "可定", aliasType = "trade_name", normalizedAlias = "可定"),
        // 1030 非诺贝特
        DrugAliasEntity(drugId = 1030L, aliasName = "非诺贝特", aliasType = "short_name", normalizedAlias = "非诺贝特"),
        DrugAliasEntity(drugId = 1030L, aliasName = "力平之", aliasType = "trade_name", normalizedAlias = "力平之"),
        DrugAliasEntity(drugId = 1030L, aliasName = "贝特", aliasType = "ocr_token", normalizedAlias = "贝特"),
        // 1031 奥美拉唑
        DrugAliasEntity(drugId = 1031L, aliasName = "奥美拉唑", aliasType = "short_name", normalizedAlias = "奥美拉唑"),
        DrugAliasEntity(drugId = 1031L, aliasName = "洛赛克", aliasType = "trade_name", normalizedAlias = "洛赛克"),
        DrugAliasEntity(drugId = 1031L, aliasName = "PPI", aliasType = "ocr_token", normalizedAlias = "PPI"),
        // 1032 雷贝拉唑
        DrugAliasEntity(drugId = 1032L, aliasName = "雷贝拉唑", aliasType = "short_name", normalizedAlias = "雷贝拉唑"),
        DrugAliasEntity(drugId = 1032L, aliasName = "济诺", aliasType = "trade_name", normalizedAlias = "济诺"),
        DrugAliasEntity(drugId = 1032L, aliasName = "胃药", aliasType = "ocr_token", normalizedAlias = "胃药"),
        // 1033 泮托拉唑
        DrugAliasEntity(drugId = 1033L, aliasName = "泮托拉唑", aliasType = "short_name", normalizedAlias = "泮托拉唑"),
        DrugAliasEntity(drugId = 1033L, aliasName = "泰美尼克", aliasType = "trade_name", normalizedAlias = "泰美尼克"),
        // 1034 多潘立酮
        DrugAliasEntity(drugId = 1034L, aliasName = "多潘立酮", aliasType = "short_name", normalizedAlias = "多潘立酮"),
        DrugAliasEntity(drugId = 1034L, aliasName = "吗丁啉", aliasType = "trade_name", normalizedAlias = "吗丁啉"),
        DrugAliasEntity(drugId = 1034L, aliasName = "胃动力", aliasType = "ocr_token", normalizedAlias = "胃动力"),
        // 1035 莫沙必利
        DrugAliasEntity(drugId = 1035L, aliasName = "莫沙必利", aliasType = "short_name", normalizedAlias = "莫沙必利"),
        DrugAliasEntity(drugId = 1035L, aliasName = "加斯清", aliasType = "trade_name", normalizedAlias = "加斯清"),
        // 1036 铝碳酸镁
        DrugAliasEntity(drugId = 1036L, aliasName = "铝碳酸镁", aliasType = "short_name", normalizedAlias = "铝碳酸镁"),
        DrugAliasEntity(drugId = 1036L, aliasName = "达喜", aliasType = "trade_name", normalizedAlias = "达喜"),
        DrugAliasEntity(drugId = 1036L, aliasName = "抗酸药", aliasType = "ocr_token", normalizedAlias = "抗酸药"),
        // 1037 复方丹参滴丸
        DrugAliasEntity(drugId = 1037L, aliasName = "复方丹参滴丸", aliasType = "short_name", normalizedAlias = "复方丹参滴丸"),
        DrugAliasEntity(drugId = 1037L, aliasName = "丹参滴丸", aliasType = "alias", normalizedAlias = "丹参滴丸"),
        DrugAliasEntity(drugId = 1037L, aliasName = "心血管药", aliasType = "ocr_token", normalizedAlias = "心血管药"),
        // 1038 速效救心丸
        DrugAliasEntity(drugId = 1038L, aliasName = "速效救心丸", aliasType = "short_name", normalizedAlias = "速效救心丸"),
        DrugAliasEntity(drugId = 1038L, aliasName = "救心丸", aliasType = "alias", normalizedAlias = "救心丸"),
        DrugAliasEntity(drugId = 1038L, aliasName = "心绞痛急救", aliasType = "ocr_token", normalizedAlias = "心绞痛急救"),
        // 1039 银杏叶
        DrugAliasEntity(drugId = 1039L, aliasName = "银杏叶", aliasType = "short_name", normalizedAlias = "银杏叶"),
        DrugAliasEntity(drugId = 1039L, aliasName = "金纳多", aliasType = "trade_name", normalizedAlias = "金纳多"),
        DrugAliasEntity(drugId = 1039L, aliasName = "脑循环", aliasType = "ocr_token", normalizedAlias = "脑循环"),
        // 1040 氯吡格雷
        DrugAliasEntity(drugId = 1040L, aliasName = "氯吡格雷", aliasType = "short_name", normalizedAlias = "氯吡格雷"),
        DrugAliasEntity(drugId = 1040L, aliasName = "波立维", aliasType = "trade_name", normalizedAlias = "波立维"),
        DrugAliasEntity(drugId = 1040L, aliasName = "抗血小板", aliasType = "ocr_token", normalizedAlias = "抗血小板"),
        // 1041 阿司匹林
        DrugAliasEntity(drugId = 1041L, aliasName = "阿司匹林", aliasType = "short_name", normalizedAlias = "阿司匹林"),
        DrugAliasEntity(drugId = 1041L, aliasName = "拜阿司匹灵", aliasType = "trade_name", normalizedAlias = "拜阿司匹灵"),
        DrugAliasEntity(drugId = 1041L, aliasName = "阿司匹林肠溶片", aliasType = "generic_name", normalizedAlias = "阿司匹林肠溶片"),
        // 1042 华法林
        DrugAliasEntity(drugId = 1042L, aliasName = "华法林", aliasType = "short_name", normalizedAlias = "华法林"),
        DrugAliasEntity(drugId = 1042L, aliasName = "华法林钠", aliasType = "generic_name", normalizedAlias = "华法林钠"),
        DrugAliasEntity(drugId = 1042L, aliasName = "抗凝药", aliasType = "ocr_token", normalizedAlias = "抗凝药"),
        // 1043 地高辛
        DrugAliasEntity(drugId = 1043L, aliasName = "地高辛", aliasType = "short_name", normalizedAlias = "地高辛"),
        DrugAliasEntity(drugId = 1043L, aliasName = "强心药", aliasType = "ocr_token", normalizedAlias = "强心药"),
        // 1044 格列美脲
        DrugAliasEntity(drugId = 1044L, aliasName = "格列美脲", aliasType = "short_name", normalizedAlias = "格列美脲"),
        DrugAliasEntity(drugId = 1044L, aliasName = "亚莫利", aliasType = "trade_name", normalizedAlias = "亚莫利"),
        DrugAliasEntity(drugId = 1044L, aliasName = "磺脲类", aliasType = "ocr_token", normalizedAlias = "磺脲类"),
        // 1045 利拉鲁肽
        DrugAliasEntity(drugId = 1045L, aliasName = "利拉鲁肽", aliasType = "short_name", normalizedAlias = "利拉鲁肽"),
        DrugAliasEntity(drugId = 1045L, aliasName = "诺和力", aliasType = "trade_name", normalizedAlias = "诺和力"),
        DrugAliasEntity(drugId = 1045L, aliasName = "GLP-1", aliasType = "ocr_token", normalizedAlias = "GLP-1"),
        // 1046 达格列净
        DrugAliasEntity(drugId = 1046L, aliasName = "达格列净", aliasType = "short_name", normalizedAlias = "达格列净"),
        DrugAliasEntity(drugId = 1046L, aliasName = "安达唐", aliasType = "trade_name", normalizedAlias = "安达唐"),
        DrugAliasEntity(drugId = 1046L, aliasName = "SGLT2", aliasType = "ocr_token", normalizedAlias = "SGLT2"),
        // 1047 恩格列净
        DrugAliasEntity(drugId = 1047L, aliasName = "恩格列净", aliasType = "short_name", normalizedAlias = "恩格列净"),
        DrugAliasEntity(drugId = 1047L, aliasName = "欧唐静", aliasType = "trade_name", normalizedAlias = "欧唐静"),
        // 1048 西格列汀
        DrugAliasEntity(drugId = 1048L, aliasName = "西格列汀", aliasType = "short_name", normalizedAlias = "西格列汀"),
        DrugAliasEntity(drugId = 1048L, aliasName = "捷诺维", aliasType = "trade_name", normalizedAlias = "捷诺维"),
        DrugAliasEntity(drugId = 1048L, aliasName = "DPP-4", aliasType = "ocr_token", normalizedAlias = "DPP-4"),
        // 1049 沙格列汀
        DrugAliasEntity(drugId = 1049L, aliasName = "沙格列汀", aliasType = "short_name", normalizedAlias = "沙格列汀"),
        DrugAliasEntity(drugId = 1049L, aliasName = "安立泽", aliasType = "trade_name", normalizedAlias = "安立泽"),
        // 1050 甲钴胺
        DrugAliasEntity(drugId = 1050L, aliasName = "甲钴胺", aliasType = "short_name", normalizedAlias = "甲钴胺"),
        DrugAliasEntity(drugId = 1050L, aliasName = "弥可保", aliasType = "trade_name", normalizedAlias = "弥可保"),
        DrugAliasEntity(drugId = 1050L, aliasName = "维生素B12", aliasType = "ocr_token", normalizedAlias = "维生素B12"),
        // 1051 硫辛酸
        DrugAliasEntity(drugId = 1051L, aliasName = "硫辛酸", aliasType = "short_name", normalizedAlias = "硫辛酸"),
        DrugAliasEntity(drugId = 1051L, aliasName = "α-硫辛酸", aliasType = "generic_name", normalizedAlias = "α-硫辛酸"),
        DrugAliasEntity(drugId = 1051L, aliasName = "抗氧化", aliasType = "ocr_token", normalizedAlias = "抗氧化"),
        // 1052 碳酸钙D3
        DrugAliasEntity(drugId = 1052L, aliasName = "碳酸钙D3", aliasType = "short_name", normalizedAlias = "碳酸钙D3"),
        DrugAliasEntity(drugId = 1052L, aliasName = "钙尔奇", aliasType = "trade_name", normalizedAlias = "钙尔奇"),
        DrugAliasEntity(drugId = 1052L, aliasName = "钙片", aliasType = "ocr_token", normalizedAlias = "钙片"),
        // 1053 维生素D
        DrugAliasEntity(drugId = 1053L, aliasName = "维生素D", aliasType = "short_name", normalizedAlias = "维生素D"),
        DrugAliasEntity(drugId = 1053L, aliasName = "维生素D滴剂", aliasType = "generic_name", normalizedAlias = "维生素D滴剂"),
        DrugAliasEntity(drugId = 1053L, aliasName = "补钙", aliasType = "ocr_token", normalizedAlias = "补钙"),
        // 1054 吲达帕胺
        DrugAliasEntity(drugId = 1054L, aliasName = "吲达帕胺", aliasType = "short_name", normalizedAlias = "吲达帕胺"),
        DrugAliasEntity(drugId = 1054L, aliasName = "纳催离", aliasType = "trade_name", normalizedAlias = "纳催离"),
        DrugAliasEntity(drugId = 1054L, aliasName = "利尿降压", aliasType = "ocr_token", normalizedAlias = "利尿降压"),
        // 1055 尼莫地平
        DrugAliasEntity(drugId = 1055L, aliasName = "尼莫地平", aliasType = "short_name", normalizedAlias = "尼莫地平"),
        DrugAliasEntity(drugId = 1055L, aliasName = "尼莫同", aliasType = "trade_name", normalizedAlias = "尼莫同"),
        // 1056 长春西汀
        DrugAliasEntity(drugId = 1056L, aliasName = "长春西汀", aliasType = "short_name", normalizedAlias = "长春西汀"),
        DrugAliasEntity(drugId = 1056L, aliasName = "脑循环药", aliasType = "ocr_token", normalizedAlias = "脑循环药"),
        // ============================================================
        // 2001-2005. 儿科用药别名
        // ============================================================
        DrugAliasEntity(drugId = 2001L, aliasName = "小儿氨酚黄那敏", aliasType = "short_name", normalizedAlias = "小儿氨酚黄那敏"),
        DrugAliasEntity(drugId = 2001L, aliasName = "护彤", aliasType = "trade_name", normalizedAlias = "护彤"),
        DrugAliasEntity(drugId = 2001L, aliasName = "儿童感冒药", aliasType = "ocr_token", normalizedAlias = "儿童感冒药"),
        DrugAliasEntity(drugId = 2002L, aliasName = "小儿止咳糖浆", aliasType = "short_name", normalizedAlias = "小儿止咳糖浆"),
        DrugAliasEntity(drugId = 2002L, aliasName = "儿童止咳药", aliasType = "ocr_token", normalizedAlias = "儿童止咳药"),
        DrugAliasEntity(drugId = 2003L, aliasName = "小儿蒙脱石散", aliasType = "short_name", normalizedAlias = "小儿蒙脱石散"),
        DrugAliasEntity(drugId = 2003L, aliasName = "思密达", aliasType = "trade_name", normalizedAlias = "思密达"),
        DrugAliasEntity(drugId = 2003L, aliasName = "儿童止泻药", aliasType = "ocr_token", normalizedAlias = "儿童止泻药"),
        DrugAliasEntity(drugId = 2004L, aliasName = "美林", aliasType = "trade_name", normalizedAlias = "美林"),
        DrugAliasEntity(drugId = 2004L, aliasName = "小儿退烧药", aliasType = "ocr_token", normalizedAlias = "小儿退烧药"),
        DrugAliasEntity(drugId = 2004L, aliasName = "布洛芬混悬液", aliasType = "generic_name", normalizedAlias = "布洛芬混悬液"),
        DrugAliasEntity(drugId = 2005L, aliasName = "小儿阿莫西林", aliasType = "short_name", normalizedAlias = "小儿阿莫西林"),
        DrugAliasEntity(drugId = 2005L, aliasName = "再林", aliasType = "trade_name", normalizedAlias = "再林"),
        DrugAliasEntity(drugId = 2005L, aliasName = "儿童消炎药", aliasType = "ocr_token", normalizedAlias = "儿童消炎药"),
        // ============================================================
        // 2006-2010. 妇科用药别名
        // ============================================================
        DrugAliasEntity(drugId = 2006L, aliasName = "乌鸡白凤丸", aliasType = "short_name", normalizedAlias = "乌鸡白凤丸"),
        DrugAliasEntity(drugId = 2006L, aliasName = "妇科调经药", aliasType = "ocr_token", normalizedAlias = "妇科调经药"),
        DrugAliasEntity(drugId = 2007L, aliasName = "益母草", aliasType = "short_name", normalizedAlias = "益母草"),
        DrugAliasEntity(drugId = 2007L, aliasName = "产后调理药", aliasType = "ocr_token", normalizedAlias = "产后调理药"),
        DrugAliasEntity(drugId = 2008L, aliasName = "妇科千金片", aliasType = "short_name", normalizedAlias = "妇科千金片"),
        DrugAliasEntity(drugId = 2008L, aliasName = "千金片", aliasType = "alias", normalizedAlias = "千金片"),
        DrugAliasEntity(drugId = 2008L, aliasName = "妇科炎症药", aliasType = "ocr_token", normalizedAlias = "妇科炎症药"),
        DrugAliasEntity(drugId = 2009L, aliasName = "甲硝唑栓", aliasType = "short_name", normalizedAlias = "甲硝唑栓"),
        DrugAliasEntity(drugId = 2009L, aliasName = "妇科抗菌栓", aliasType = "ocr_token", normalizedAlias = "妇科抗菌栓"),
        DrugAliasEntity(drugId = 2010L, aliasName = "克霉唑栓", aliasType = "short_name", normalizedAlias = "克霉唑栓"),
        DrugAliasEntity(drugId = 2010L, aliasName = "抗真菌栓", aliasType = "ocr_token", normalizedAlias = "抗真菌栓"),
        // ============================================================
        // 2011-2015. 皮肤科用药别名
        // ============================================================
        DrugAliasEntity(drugId = 2011L, aliasName = "皮炎平", aliasType = "trade_name", normalizedAlias = "皮炎平"),
        DrugAliasEntity(drugId = 2011L, aliasName = "复方醋酸地塞米松", aliasType = "generic_name", normalizedAlias = "复方醋酸地塞米松"),
        DrugAliasEntity(drugId = 2011L, aliasName = "皮肤药膏", aliasType = "ocr_token", normalizedAlias = "皮肤药膏"),
        DrugAliasEntity(drugId = 2012L, aliasName = "派瑞松", aliasType = "trade_name", normalizedAlias = "派瑞松"),
        DrugAliasEntity(drugId = 2012L, aliasName = "曲安奈德益康唑", aliasType = "generic_name", normalizedAlias = "曲安奈德益康唑"),
        DrugAliasEntity(drugId = 2013L, aliasName = "百多邦", aliasType = "trade_name", normalizedAlias = "百多邦"),
        DrugAliasEntity(drugId = 2013L, aliasName = "莫匹罗星", aliasType = "generic_name", normalizedAlias = "莫匹罗星"),
        DrugAliasEntity(drugId = 2013L, aliasName = "外用抗生素", aliasType = "ocr_token", normalizedAlias = "外用抗生素"),
        DrugAliasEntity(drugId = 2014L, aliasName = "达克宁", aliasType = "trade_name", normalizedAlias = "达克宁"),
        DrugAliasEntity(drugId = 2014L, aliasName = "硝酸咪康唑", aliasType = "generic_name", normalizedAlias = "硝酸咪康唑"),
        DrugAliasEntity(drugId = 2014L, aliasName = "脚气膏", aliasType = "ocr_token", normalizedAlias = "脚气膏"),
        DrugAliasEntity(drugId = 2015L, aliasName = "复方酮康唑", aliasType = "short_name", normalizedAlias = "复方酮康唑"),
        DrugAliasEntity(drugId = 2015L, aliasName = "酮康唑软膏", aliasType = "alias", normalizedAlias = "酮康唑软膏"),
        // ============================================================
        // 2016-2019. 眼科用药别名
        // ============================================================
        DrugAliasEntity(drugId = 2016L, aliasName = "左氧氟沙星滴眼液", aliasType = "short_name", normalizedAlias = "左氧氟沙星滴眼液"),
        DrugAliasEntity(drugId = 2016L, aliasName = "可乐必妥滴眼液", aliasType = "trade_name", normalizedAlias = "可乐必妥滴眼液"),
        DrugAliasEntity(drugId = 2016L, aliasName = "眼药水", aliasType = "ocr_token", normalizedAlias = "眼药水"),
        DrugAliasEntity(drugId = 2017L, aliasName = "妥布霉素滴眼液", aliasType = "short_name", normalizedAlias = "妥布霉素滴眼液"),
        DrugAliasEntity(drugId = 2017L, aliasName = "托百士", aliasType = "trade_name", normalizedAlias = "托百士"),
        DrugAliasEntity(drugId = 2018L, aliasName = "玻璃酸钠滴眼液", aliasType = "short_name", normalizedAlias = "玻璃酸钠滴眼液"),
        DrugAliasEntity(drugId = 2018L, aliasName = "爱丽", aliasType = "trade_name", normalizedAlias = "爱丽"),
        DrugAliasEntity(drugId = 2018L, aliasName = "人工泪液", aliasType = "ocr_token", normalizedAlias = "人工泪液"),
        DrugAliasEntity(drugId = 2019L, aliasName = "聚乙烯醇滴眼液", aliasType = "short_name", normalizedAlias = "聚乙烯醇滴眼液"),
        DrugAliasEntity(drugId = 2019L, aliasName = "瑞珠", aliasType = "trade_name", normalizedAlias = "瑞珠"),
        DrugAliasEntity(drugId = 2019L, aliasName = "干眼症药", aliasType = "ocr_token", normalizedAlias = "干眼症药"),
        // ============================================================
        // 2020-2023. 耳鼻喉科用药别名
        // ============================================================
        DrugAliasEntity(drugId = 2020L, aliasName = "氯霉素滴耳液", aliasType = "short_name", normalizedAlias = "氯霉素滴耳液"),
        DrugAliasEntity(drugId = 2020L, aliasName = "滴耳液", aliasType = "ocr_token", normalizedAlias = "滴耳液"),
        DrugAliasEntity(drugId = 2021L, aliasName = "氧氟沙星滴耳液", aliasType = "short_name", normalizedAlias = "氧氟沙星滴耳液"),
        DrugAliasEntity(drugId = 2021L, aliasName = "中耳炎药", aliasType = "ocr_token", normalizedAlias = "中耳炎药"),
        DrugAliasEntity(drugId = 2022L, aliasName = "通窍鼻炎片", aliasType = "short_name", normalizedAlias = "通窍鼻炎片"),
        DrugAliasEntity(drugId = 2022L, aliasName = "鼻炎药", aliasType = "ocr_token", normalizedAlias = "鼻炎药"),
        DrugAliasEntity(drugId = 2023L, aliasName = "鼻炎康", aliasType = "trade_name", normalizedAlias = "鼻炎康"),
        DrugAliasEntity(drugId = 2023L, aliasName = "鼻炎康片", aliasType = "generic_name", normalizedAlias = "鼻炎康片"),
        DrugAliasEntity(drugId = 2023L, aliasName = "过敏性鼻炎药", aliasType = "ocr_token", normalizedAlias = "过敏性鼻炎药"),
        // ============================================================
        // 2024-2028. 消化科用药别名
        // ============================================================
        DrugAliasEntity(drugId = 2024L, aliasName = "蒙脱石散", aliasType = "short_name", normalizedAlias = "蒙脱石散"),
        DrugAliasEntity(drugId = 2024L, aliasName = "思密达", aliasType = "alias", normalizedAlias = "思密达"),
        DrugAliasEntity(drugId = 2024L, aliasName = "止泻药", aliasType = "ocr_token", normalizedAlias = "止泻药"),
        DrugAliasEntity(drugId = 2025L, aliasName = "双歧杆菌四联活菌", aliasType = "short_name", normalizedAlias = "双歧杆菌四联活菌"),
        DrugAliasEntity(drugId = 2025L, aliasName = "思连康", aliasType = "trade_name", normalizedAlias = "思连康"),
        DrugAliasEntity(drugId = 2025L, aliasName = "益生菌", aliasType = "ocr_token", normalizedAlias = "益生菌"),
        DrugAliasEntity(drugId = 2026L, aliasName = "乳果糖", aliasType = "short_name", normalizedAlias = "乳果糖"),
        DrugAliasEntity(drugId = 2026L, aliasName = "杜密克", aliasType = "trade_name", normalizedAlias = "杜密克"),
        DrugAliasEntity(drugId = 2026L, aliasName = "便秘药", aliasType = "ocr_token", normalizedAlias = "便秘药"),
        DrugAliasEntity(drugId = 2027L, aliasName = "开塞露", aliasType = "short_name", normalizedAlias = "开塞露"),
        DrugAliasEntity(drugId = 2027L, aliasName = "通便药", aliasType = "ocr_token", normalizedAlias = "通便药"),
        DrugAliasEntity(drugId = 2028L, aliasName = "多酶片", aliasType = "short_name", normalizedAlias = "多酶片"),
        DrugAliasEntity(drugId = 2028L, aliasName = "消化酶", aliasType = "ocr_token", normalizedAlias = "消化酶"),
        // ============================================================
        // 2029-2033. 呼吸科用药别名
        // ============================================================
        DrugAliasEntity(drugId = 2029L, aliasName = "氨溴索", aliasType = "short_name", normalizedAlias = "氨溴索"),
        DrugAliasEntity(drugId = 2029L, aliasName = "沐舒坦", aliasType = "trade_name", normalizedAlias = "沐舒坦"),
        DrugAliasEntity(drugId = 2029L, aliasName = "化痰药", aliasType = "ocr_token", normalizedAlias = "化痰药"),
        DrugAliasEntity(drugId = 2030L, aliasName = "右美沙芬", aliasType = "short_name", normalizedAlias = "右美沙芬"),
        DrugAliasEntity(drugId = 2030L, aliasName = "镇咳药", aliasType = "ocr_token", normalizedAlias = "镇咳药"),
        DrugAliasEntity(drugId = 2031L, aliasName = "复方甘草片", aliasType = "short_name", normalizedAlias = "复方甘草片"),
        DrugAliasEntity(drugId = 2031L, aliasName = "甘草片", aliasType = "alias", normalizedAlias = "甘草片"),
        DrugAliasEntity(drugId = 2031L, aliasName = "止咳药", aliasType = "ocr_token", normalizedAlias = "止咳药"),
        DrugAliasEntity(drugId = 2032L, aliasName = "孟鲁司特钠", aliasType = "short_name", normalizedAlias = "孟鲁司特钠"),
        DrugAliasEntity(drugId = 2032L, aliasName = "顺尔宁", aliasType = "trade_name", normalizedAlias = "顺尔宁"),
        DrugAliasEntity(drugId = 2032L, aliasName = "哮喘药", aliasType = "ocr_token", normalizedAlias = "哮喘药"),
        DrugAliasEntity(drugId = 2033L, aliasName = "沙丁胺醇", aliasType = "short_name", normalizedAlias = "沙丁胺醇"),
        DrugAliasEntity(drugId = 2033L, aliasName = "万托林", aliasType = "trade_name", normalizedAlias = "万托林"),
        DrugAliasEntity(drugId = 2033L, aliasName = "气喘喷雾", aliasType = "ocr_token", normalizedAlias = "气喘喷雾"),
        // ============================================================
        // 2034-2037. 骨科/止痛用药别名
        // ============================================================
        DrugAliasEntity(drugId = 2034L, aliasName = "布洛芬凝胶", aliasType = "short_name", normalizedAlias = "布洛芬凝胶"),
        DrugAliasEntity(drugId = 2034L, aliasName = "芬必得凝胶", aliasType = "trade_name", normalizedAlias = "芬必得凝胶"),
        DrugAliasEntity(drugId = 2034L, aliasName = "外用止痛药", aliasType = "ocr_token", normalizedAlias = "外用止痛药"),
        DrugAliasEntity(drugId = 2035L, aliasName = "扶他林", aliasType = "trade_name", normalizedAlias = "扶他林"),
        DrugAliasEntity(drugId = 2035L, aliasName = "双氯芬酸二乙胺", aliasType = "generic_name", normalizedAlias = "双氯芬酸二乙胺"),
        DrugAliasEntity(drugId = 2036L, aliasName = "云南白药", aliasType = "trade_name", normalizedAlias = "云南白药"),
        DrugAliasEntity(drugId = 2036L, aliasName = "跌打损伤药", aliasType = "ocr_token", normalizedAlias = "跌打损伤药"),
        DrugAliasEntity(drugId = 2036L, aliasName = "止血化瘀药", aliasType = "ocr_token", normalizedAlias = "止血化瘀药"),
        DrugAliasEntity(drugId = 2037L, aliasName = "活血止痛膏", aliasType = "short_name", normalizedAlias = "活血止痛膏"),
        DrugAliasEntity(drugId = 2037L, aliasName = "止痛贴膏", aliasType = "ocr_token", normalizedAlias = "止痛贴膏"),
        // ============================================================
        // 2038-2042. 外用药别名
        // ============================================================
        DrugAliasEntity(drugId = 2038L, aliasName = "碘伏", aliasType = "trade_name", normalizedAlias = "碘伏"),
        DrugAliasEntity(drugId = 2038L, aliasName = "聚维酮碘", aliasType = "generic_name", normalizedAlias = "聚维酮碘"),
        DrugAliasEntity(drugId = 2038L, aliasName = "消毒药", aliasType = "ocr_token", normalizedAlias = "消毒药"),
        DrugAliasEntity(drugId = 2039L, aliasName = "酒精", aliasType = "short_name", normalizedAlias = "酒精"),
        DrugAliasEntity(drugId = 2039L, aliasName = "医用酒精", aliasType = "alias", normalizedAlias = "医用酒精"),
        DrugAliasEntity(drugId = 2039L, aliasName = "消毒液", aliasType = "ocr_token", normalizedAlias = "消毒液"),
        DrugAliasEntity(drugId = 2040L, aliasName = "创可贴", aliasType = "short_name", normalizedAlias = "创可贴"),
        DrugAliasEntity(drugId = 2040L, aliasName = "邦迪", aliasType = "trade_name", normalizedAlias = "邦迪"),
        DrugAliasEntity(drugId = 2040L, aliasName = "止血贴", aliasType = "ocr_token", normalizedAlias = "止血贴"),
        DrugAliasEntity(drugId = 2041L, aliasName = "云南白药气雾剂", aliasType = "short_name", normalizedAlias = "云南白药气雾剂"),
        DrugAliasEntity(drugId = 2041L, aliasName = "外用喷雾", aliasType = "ocr_token", normalizedAlias = "外用喷雾"),
        DrugAliasEntity(drugId = 2042L, aliasName = "红花油", aliasType = "short_name", normalizedAlias = "红花油"),
        DrugAliasEntity(drugId = 2042L, aliasName = "跌打药油", aliasType = "ocr_token", normalizedAlias = "跌打药油"),
        // ============================================================
        // 2043-2047. 罕见病/痛风用药别名
        // ============================================================
        DrugAliasEntity(drugId = 2043L, aliasName = "秋水仙碱", aliasType = "short_name", normalizedAlias = "秋水仙碱"),
        DrugAliasEntity(drugId = 2043L, aliasName = "痛风药", aliasType = "ocr_token", normalizedAlias = "痛风药"),
        DrugAliasEntity(drugId = 2044L, aliasName = "别嘌醇", aliasType = "short_name", normalizedAlias = "别嘌醇"),
        DrugAliasEntity(drugId = 2044L, aliasName = "别嘌呤醇", aliasType = "alias", normalizedAlias = "别嘌呤醇"),
        DrugAliasEntity(drugId = 2044L, aliasName = "降尿酸药", aliasType = "ocr_token", normalizedAlias = "降尿酸药"),
        DrugAliasEntity(drugId = 2045L, aliasName = "苯溴马隆", aliasType = "short_name", normalizedAlias = "苯溴马隆"),
        DrugAliasEntity(drugId = 2045L, aliasName = "立加利仙", aliasType = "trade_name", normalizedAlias = "立加利仙"),
        DrugAliasEntity(drugId = 2046L, aliasName = "甲氨蝶呤", aliasType = "short_name", normalizedAlias = "甲氨蝶呤"),
        DrugAliasEntity(drugId = 2046L, aliasName = "MTX", aliasType = "alias", normalizedAlias = "MTX"),
        DrugAliasEntity(drugId = 2046L, aliasName = "免疫抑制剂", aliasType = "ocr_token", normalizedAlias = "免疫抑制剂"),
        DrugAliasEntity(drugId = 2047L, aliasName = "青霉胺", aliasType = "short_name", normalizedAlias = "青霉胺"),
        DrugAliasEntity(drugId = 2047L, aliasName = "肝豆状核变性药", aliasType = "ocr_token", normalizedAlias = "肝豆状核变性药")
    )

    private val seedDrugSignMappings = listOf(
        DrugSignMappingEntity(drugId = 1001L, signKeyword = "布洛芬", signDisplayText = "布洛芬 手语演示", videoPath = "assets://signs/buluofen.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1002L, signKeyword = "阿莫西林", signDisplayText = "阿莫西林 手语演示", videoPath = "assets://signs/amoxilin.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1003L, signKeyword = "二甲双胍", signDisplayText = "二甲双胍 手语演示", videoPath = "assets://signs/erjiashuanggua.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1004L, signKeyword = "对乙酰氨基酚", signDisplayText = "对乙酰氨基酚 手语演示", videoPath = "assets://signs/duiyixiananjifen.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1005L, signKeyword = "双氯芬酸钠", signDisplayText = "双氯芬酸钠 手语演示", videoPath = "assets://signs/shuanglvfensuanna.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1006L, signKeyword = "感冒灵", signDisplayText = "感冒灵 手语演示", videoPath = "assets://signs/ganmaoling.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1007L, signKeyword = "头孢克肟", signDisplayText = "头孢克肟 手语演示", videoPath = "assets://signs/toubaokewo.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1008L, signKeyword = "阿奇霉素", signDisplayText = "阿奇霉素 手语演示", videoPath = "assets://signs/aqimeisu.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1009L, signKeyword = "头孢拉定", signDisplayText = "头孢拉定 手语演示", videoPath = "assets://signs/toubaolading.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1010L, signKeyword = "左氧氟沙星", signDisplayText = "左氧氟沙星 手语演示", videoPath = "assets://signs/zuoyangfushaxing.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1011L, signKeyword = "氯沙坦", signDisplayText = "氯沙坦 手语演示", videoPath = "assets://signs/lvshatan.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1012L, signKeyword = "硝苯地平", signDisplayText = "硝苯地平 手语演示", videoPath = "assets://signs/xiaobendiping.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1013L, signKeyword = "氨氯地平", signDisplayText = "氨氯地平 手语演示", videoPath = "assets://signs/anlvdiping.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1014L, signKeyword = "厄贝沙坦", signDisplayText = "厄贝沙坦 手语演示", videoPath = "assets://signs/ebeishatan.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1015L, signKeyword = "美托洛尔", signDisplayText = "美托洛尔 手语演示", videoPath = "assets://signs/meituoluoer.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1016L, signKeyword = "卡托普利", signDisplayText = "卡托普利 手语演示", videoPath = "assets://signs/katuopuli.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1017L, signKeyword = "缬沙坦", signDisplayText = "缬沙坦 手语演示", videoPath = "assets://signs/xieshatan.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1018L, signKeyword = "非洛地平", signDisplayText = "非洛地平 手语演示", videoPath = "assets://signs/feiluodiping.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1019L, signKeyword = "坎地沙坦", signDisplayText = "坎地沙坦 手语演示", videoPath = "assets://signs/kandishatan.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1020L, signKeyword = "替米沙坦", signDisplayText = "替米沙坦 手语演示", videoPath = "assets://signs/timishatan.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1021L, signKeyword = "奥美沙坦", signDisplayText = "奥美沙坦 手语演示", videoPath = "assets://signs/aomeishatan.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1022L, signKeyword = "比索洛尔", signDisplayText = "比索洛尔 手语演示", videoPath = "assets://signs/bisuoluoer.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1023L, signKeyword = "阿罗洛尔", signDisplayText = "阿罗洛尔 手语演示", videoPath = "assets://signs/aluoluoer.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1024L, signKeyword = "氢氯噻嗪", signDisplayText = "氢氯噻嗪 手语演示", videoPath = "assets://signs/qinglvsaijiang.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1025L, signKeyword = "呋塞米", signDisplayText = "呋塞米 手语演示", videoPath = "assets://signs/fusaimi.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1026L, signKeyword = "螺内酯", signDisplayText = "螺内酯 手语演示", videoPath = "assets://signs/luoneizhi.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1027L, signKeyword = "辛伐他汀", signDisplayText = "辛伐他汀 手语演示", videoPath = "assets://signs/xinfatating.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1028L, signKeyword = "阿托伐他汀", signDisplayText = "阿托伐他汀 手语演示", videoPath = "assets://signs/atuofatating.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1029L, signKeyword = "瑞舒伐他汀", signDisplayText = "瑞舒伐他汀 手语演示", videoPath = "assets://signs/ruishufatating.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1030L, signKeyword = "非诺贝特", signDisplayText = "非诺贝特 手语演示", videoPath = "assets://signs/feinuobeite.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1031L, signKeyword = "奥美拉唑", signDisplayText = "奥美拉唑 手语演示", videoPath = "assets://signs/aomeilazuo.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1032L, signKeyword = "雷贝拉唑", signDisplayText = "雷贝拉唑 手语演示", videoPath = "assets://signs/leibeilazuo.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1033L, signKeyword = "泮托拉唑", signDisplayText = "泮托拉唑 手语演示", videoPath = "assets://signs/pantuolazuo.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1034L, signKeyword = "多潘立酮", signDisplayText = "多潘立酮 手语演示", videoPath = "assets://signs/duopanlitong.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1035L, signKeyword = "莫沙必利", signDisplayText = "莫沙必利 手语演示", videoPath = "assets://signs/moshabili.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1036L, signKeyword = "铝碳酸镁", signDisplayText = "铝碳酸镁 手语演示", videoPath = "assets://signs/lvtansuanmei.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1037L, signKeyword = "复方丹参滴丸", signDisplayText = "复方丹参滴丸 手语演示", videoPath = "assets://signs/fufangdanshendiwan.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1038L, signKeyword = "速效救心丸", signDisplayText = "速效救心丸 手语演示", videoPath = "assets://signs/suxiaojiuxinwan.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1039L, signKeyword = "银杏叶", signDisplayText = "银杏叶 手语演示", videoPath = "assets://signs/yinxingye.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1040L, signKeyword = "氯吡格雷", signDisplayText = "氯吡格雷 手语演示", videoPath = "assets://signs/lvbigelai.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1041L, signKeyword = "阿司匹林", signDisplayText = "阿司匹林 手语演示", videoPath = "assets://signs/asipilin.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1042L, signKeyword = "华法林", signDisplayText = "华法林 手语演示", videoPath = "assets://signs/huafalin.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1043L, signKeyword = "地高辛", signDisplayText = "地高辛 手语演示", videoPath = "assets://signs/digaoxin.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1044L, signKeyword = "格列美脲", signDisplayText = "格列美脲 手语演示", videoPath = "assets://signs/geliemeiniao.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1045L, signKeyword = "利拉鲁肽", signDisplayText = "利拉鲁肽 手语演示", videoPath = "assets://signs/lilalu.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1046L, signKeyword = "达格列净", signDisplayText = "达格列净 手语演示", videoPath = "assets://signs/dageliejing.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1047L, signKeyword = "恩格列净", signDisplayText = "恩格列净 手语演示", videoPath = "assets://signs/engeliejing.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1048L, signKeyword = "西格列汀", signDisplayText = "西格列汀 手语演示", videoPath = "assets://signs/xigelieting.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1049L, signKeyword = "沙格列汀", signDisplayText = "沙格列汀 手语演示", videoPath = "assets://signs/shagelieting.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1050L, signKeyword = "甲钴胺", signDisplayText = "甲钴胺 手语演示", videoPath = "assets://signs/jiagu.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1051L, signKeyword = "硫辛酸", signDisplayText = "硫辛酸 手语演示", videoPath = "assets://signs/liuxinsuan.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1052L, signKeyword = "碳酸钙D3", signDisplayText = "碳酸钙D3 手语演示", videoPath = "assets://signs/tansuangai.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1053L, signKeyword = "维生素D", signDisplayText = "维生素D 手语演示", videoPath = "assets://signs/weishengsuD.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1054L, signKeyword = "吲达帕胺", signDisplayText = "吲达帕胺 手语演示", videoPath = "assets://signs/yindapaan.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1055L, signKeyword = "尼莫地平", signDisplayText = "尼莫地平 手语演示", videoPath = "assets://signs/nimodiping.mp4", spellingMode = "word"),
        DrugSignMappingEntity(drugId = 1056L, signKeyword = "长春西汀", signDisplayText = "长春西汀 手语演示", videoPath = "assets://signs/changchunxiting.mp4", spellingMode = "word"),
        // 2001 小儿氨酚黄那敏
        DrugSignMappingEntity(drugId = 2001L, signKeyword = "小儿氨酚黄那敏", signDisplayText = "小儿氨酚黄那敏 手语演示", videoPath = "assets://signs/xiaoeranfen.mp4", spellingMode = "word"),
        // 2002 小儿止咳糖浆
        DrugSignMappingEntity(drugId = 2002L, signKeyword = "小儿止咳糖浆", signDisplayText = "小儿止咳糖浆 手语演示", videoPath = "assets://signs/xiaoerzhike.mp4", spellingMode = "word"),
        // 2003 小儿蒙脱石散
        DrugSignMappingEntity(drugId = 2003L, signKeyword = "蒙脱石散", signDisplayText = "蒙脱石散 手语演示", videoPath = "assets://signs/mengtuoshi.mp4", spellingMode = "word"),
        // 2004 美林
        DrugSignMappingEntity(drugId = 2004L, signKeyword = "布洛芬混悬液", signDisplayText = "布洛芬混悬液 手语演示", videoPath = "assets://signs/buluofenxunye.mp4", spellingMode = "word"),
        // 2005 再林
        DrugSignMappingEntity(drugId = 2005L, signKeyword = "阿莫西林颗粒", signDisplayText = "阿莫西林颗粒 手语演示", videoPath = "assets://signs/amoxilinkeli.mp4", spellingMode = "word"),
        // 2006 乌鸡白凤丸
        DrugSignMappingEntity(drugId = 2006L, signKeyword = "乌鸡白凤丸", signDisplayText = "乌鸡白凤丸 手语演示", videoPath = "assets://signs/wujibaifeng.mp4", spellingMode = "word"),
        // 2007 益母草颗粒
        DrugSignMappingEntity(drugId = 2007L, signKeyword = "益母草", signDisplayText = "益母草 手语演示", videoPath = "assets://signs/yimucao.mp4", spellingMode = "word"),
        // 2008 妇科千金片
        DrugSignMappingEntity(drugId = 2008L, signKeyword = "妇科千金片", signDisplayText = "妇科千金片 手语演示", videoPath = "assets://signs/fukeqianjin.mp4", spellingMode = "word"),
        // 2009 甲硝唑栓
        DrugSignMappingEntity(drugId = 2009L, signKeyword = "甲硝唑", signDisplayText = "甲硝唑 手语演示", videoPath = "assets://signs/jiaxiao.mp4", spellingMode = "word"),
        // 2010 克霉唑栓
        DrugSignMappingEntity(drugId = 2010L, signKeyword = "克霉唑", signDisplayText = "克霉唑 手语演示", videoPath = "assets://signs/kemeizuo.mp4", spellingMode = "word"),
        // 2011 皮炎平
        DrugSignMappingEntity(drugId = 2011L, signKeyword = "皮炎平", signDisplayText = "皮炎平 手语演示", videoPath = "assets://signs/piyanping.mp4", spellingMode = "word"),
        // 2012 派瑞松
        DrugSignMappingEntity(drugId = 2012L, signKeyword = "派瑞松", signDisplayText = "派瑞松 手语演示", videoPath = "assets://signs/pairuisong.mp4", spellingMode = "word"),
        // 2013 百多邦
        DrugSignMappingEntity(drugId = 2013L, signKeyword = "百多邦", signDisplayText = "百多邦 手语演示", videoPath = "assets://signs/baiduobang.mp4", spellingMode = "word"),
        // 2014 达克宁
        DrugSignMappingEntity(drugId = 2014L, signKeyword = "达克宁", signDisplayText = "达克宁 手语演示", videoPath = "assets://signs/dakening.mp4", spellingMode = "word"),
        // 2015 复方酮康唑
        DrugSignMappingEntity(drugId = 2015L, signKeyword = "酮康唑", signDisplayText = "酮康唑 手语演示", videoPath = "assets://signs/tongkangzuo.mp4", spellingMode = "word"),
        // 2016 左氧氟沙星滴眼液
        DrugSignMappingEntity(drugId = 2016L, signKeyword = "左氧氟沙星滴眼液", signDisplayText = "左氧氟沙星滴眼液 手语演示", videoPath = "assets://signs/zuoyangfushaxingyan.mp4", spellingMode = "word"),
        // 2017 妥布霉素滴眼液
        DrugSignMappingEntity(drugId = 2017L, signKeyword = "妥布霉素", signDisplayText = "妥布霉素 手语演示", videoPath = "assets://signs/tuobumeisu.mp4", spellingMode = "word"),
        // 2018 玻璃酸钠滴眼液
        DrugSignMappingEntity(drugId = 2018L, signKeyword = "玻璃酸钠", signDisplayText = "玻璃酸钠 手语演示", videoPath = "assets://signs/bolisuan.mp4", spellingMode = "word"),
        // 2019 聚乙烯醇滴眼液
        DrugSignMappingEntity(drugId = 2019L, signKeyword = "聚乙烯醇", signDisplayText = "聚乙烯醇 手语演示", videoPath = "assets://signs/juyixichun.mp4", spellingMode = "word"),
        // 2020 氯霉素滴耳液
        DrugSignMappingEntity(drugId = 2020L, signKeyword = "氯霉素", signDisplayText = "氯霉素 手语演示", videoPath = "assets://signs/lvmeisu.mp4", spellingMode = "word"),
        // 2021 氧氟沙星滴耳液
        DrugSignMappingEntity(drugId = 2021L, signKeyword = "氧氟沙星", signDisplayText = "氧氟沙星 手语演示", videoPath = "assets://signs/yangfushaxing.mp4", spellingMode = "word"),
        // 2022 通窍鼻炎片
        DrugSignMappingEntity(drugId = 2022L, signKeyword = "通窍鼻炎片", signDisplayText = "通窍鼻炎片 手语演示", videoPath = "assets://signs/tongqiaobiyan.mp4", spellingMode = "word"),
        // 2023 鼻炎康
        DrugSignMappingEntity(drugId = 2023L, signKeyword = "鼻炎康", signDisplayText = "鼻炎康 手语演示", videoPath = "assets://signs/biyankang.mp4", spellingMode = "word"),
        // 2024 蒙脱石散(成人)
        DrugSignMappingEntity(drugId = 2024L, signKeyword = "蒙脱石散", signDisplayText = "蒙脱石散 手语演示", videoPath = "assets://signs/mengtuoshi.mp4", spellingMode = "word"),
        // 2025 双歧杆菌
        DrugSignMappingEntity(drugId = 2025L, signKeyword = "双歧杆菌", signDisplayText = "双歧杆菌 手语演示", videoPath = "assets://signs/shuangqiganjun.mp4", spellingMode = "word"),
        // 2026 乳果糖
        DrugSignMappingEntity(drugId = 2026L, signKeyword = "乳果糖", signDisplayText = "乳果糖 手语演示", videoPath = "assets://signs/ruguotang.mp4", spellingMode = "word"),
        // 2027 开塞露
        DrugSignMappingEntity(drugId = 2027L, signKeyword = "开塞露", signDisplayText = "开塞露 手语演示", videoPath = "assets://signs/kaisailu.mp4", spellingMode = "word"),
        // 2028 多酶片
        DrugSignMappingEntity(drugId = 2028L, signKeyword = "多酶片", signDisplayText = "多酶片 手语演示", videoPath = "assets://signs/duomeipian.mp4", spellingMode = "word"),
        // 2029 氨溴索
        DrugSignMappingEntity(drugId = 2029L, signKeyword = "氨溴索", signDisplayText = "氨溴索 手语演示", videoPath = "assets://signs/anxiusuo.mp4", spellingMode = "word"),
        // 2030 右美沙芬
        DrugSignMappingEntity(drugId = 2030L, signKeyword = "右美沙芬", signDisplayText = "右美沙芬 手语演示", videoPath = "assets://signs/youmeishafen.mp4", spellingMode = "word"),
        // 2031 复方甘草片
        DrugSignMappingEntity(drugId = 2031L, signKeyword = "复方甘草片", signDisplayText = "复方甘草片 手语演示", videoPath = "assets://signs/fufanggancao.mp4", spellingMode = "word"),
        // 2032 孟鲁司特钠
        DrugSignMappingEntity(drugId = 2032L, signKeyword = "孟鲁司特钠", signDisplayText = "孟鲁司特钠 手语演示", videoPath = "assets://signs/menglusite.mp4", spellingMode = "word"),
        // 2033 沙丁胺醇
        DrugSignMappingEntity(drugId = 2033L, signKeyword = "沙丁胺醇", signDisplayText = "沙丁胺醇 手语演示", videoPath = "assets://signs/shadinganchun.mp4", spellingMode = "word"),
        // 2034 布洛芬凝胶
        DrugSignMappingEntity(drugId = 2034L, signKeyword = "布洛芬凝胶", signDisplayText = "布洛芬凝胶 手语演示", videoPath = "assets://signs/buluofennjiao.mp4", spellingMode = "word"),
        // 2035 扶他林乳胶剂
        DrugSignMappingEntity(drugId = 2035L, signKeyword = "扶他林", signDisplayText = "扶他林 手语演示", videoPath = "assets://signs/futalin.mp4", spellingMode = "word"),
        // 2036 云南白药
        DrugSignMappingEntity(drugId = 2036L, signKeyword = "云南白药", signDisplayText = "云南白药 手语演示", videoPath = "assets://signs/yunnanbaiyao.mp4", spellingMode = "word"),
        // 2037 活血止痛膏
        DrugSignMappingEntity(drugId = 2037L, signKeyword = "活血止痛膏", signDisplayText = "活血止痛膏 手语演示", videoPath = "assets://signs/huoxuezhitong.mp4", spellingMode = "word"),
        // 2038 碘伏
        DrugSignMappingEntity(drugId = 2038L, signKeyword = "碘伏", signDisplayText = "碘伏 手语演示", videoPath = "assets://signs/dianfu.mp4", spellingMode = "word"),
        // 2039 酒精
        DrugSignMappingEntity(drugId = 2039L, signKeyword = "酒精", signDisplayText = "酒精 手语演示", videoPath = "assets://signs/jiujing.mp4", spellingMode = "word"),
        // 2040 创可贴
        DrugSignMappingEntity(drugId = 2040L, signKeyword = "创可贴", signDisplayText = "创可贴 手语演示", videoPath = "assets://signs/chuangketie.mp4", spellingMode = "word"),
        // 2041 云南白药气雾剂
        DrugSignMappingEntity(drugId = 2041L, signKeyword = "云南白药气雾剂", signDisplayText = "云南白药气雾剂 手语演示", videoPath = "assets://signs/yunnanbaiyaoqi.mp4", spellingMode = "word"),
        // 2042 红花油
        DrugSignMappingEntity(drugId = 2042L, signKeyword = "红花油", signDisplayText = "红花油 手语演示", videoPath = "assets://signs/honghuayou.mp4", spellingMode = "word"),
        // 2043 秋水仙碱
        DrugSignMappingEntity(drugId = 2043L, signKeyword = "秋水仙碱", signDisplayText = "秋水仙碱 手语演示", videoPath = "assets://signs/qiushuixianjian.mp4", spellingMode = "word"),
        // 2044 别嘌醇
        DrugSignMappingEntity(drugId = 2044L, signKeyword = "别嘌醇", signDisplayText = "别嘌醇 手语演示", videoPath = "assets://signs/biepiaochun.mp4", spellingMode = "word"),
        // 2045 苯溴马隆
        DrugSignMappingEntity(drugId = 2045L, signKeyword = "苯溴马隆", signDisplayText = "苯溴马隆 手语演示", videoPath = "assets://signs/benxiumalong.mp4", spellingMode = "word"),
        // 2046 甲氨蝶呤
        DrugSignMappingEntity(drugId = 2046L, signKeyword = "甲氨蝶呤", signDisplayText = "甲氨蝶呤 手语演示", videoPath = "assets://signs/jiaandieling.mp4", spellingMode = "word"),
        // 2047 青霉胺
        DrugSignMappingEntity(drugId = 2047L, signKeyword = "青霉胺", signDisplayText = "青霉胺 手语演示", videoPath = "assets://signs/qingmeian.mp4", spellingMode = "word")
    )

    private val seedDrugRules = listOf(
        // 1001 布洛芬 - 胃溃疡风险
        DrugRuleEntity(drugId = 1001L, matchField = "disease_tag", matchValue = "胃溃疡", ruleType = "caution", riskLevel = "high",
            displayMessage = "如用户存在胃溃疡或消化道出血病史，请先核对禁忌后再使用布洛芬。",
            ttsMessage = "检测到胃部风险标签，请谨慎使用布洛芬。"),
        // 1001 布洛芬 - 阿司匹林过敏
        DrugRuleEntity(drugId = 1001L, matchField = "allergy_tag", matchValue = "阿司匹林过敏", ruleType = "contraindication", riskLevel = "high",
            displayMessage = "对阿司匹林过敏者可能存在交叉过敏，使用布洛芬需谨慎。",
            ttsMessage = "检测到阿司匹林过敏史，使用布洛芬需谨慎。"),
        // 1002 阿莫西林 - 青霉素过敏
        DrugRuleEntity(drugId = 1002L, matchField = "allergy_tag", matchValue = "青霉素过敏", ruleType = "contraindication", riskLevel = "high",
            displayMessage = "如用户存在青霉素过敏史，应避免使用阿莫西林。",
            ttsMessage = "检测到青霉素过敏史，请勿使用阿莫西林。"),
        // 1003 二甲双胍 - 肾功能异常
        DrugRuleEntity(drugId = 1003L, matchField = "disease_tag", matchValue = "肾功能异常", ruleType = "caution", riskLevel = "medium",
            displayMessage = "二甲双胍对肾功能异常用户需重点监测，请结合医生建议使用。",
            ttsMessage = "检测到肾功能异常标签，请重点监测二甲双胍使用情况。"),
        // 1004 对乙酰氨基酚 - 肝病
        DrugRuleEntity(drugId = 1004L, matchField = "disease_tag", matchValue = "肝病", ruleType = "caution", riskLevel = "high",
            displayMessage = "肝病患者使用对乙酰氨基酚需严格限制剂量，过量可致肝损伤。",
            ttsMessage = "检测到肝病风险，请严格限制对乙酰氨基酚剂量。"),
        // 1004 对乙酰氨基酚 - 酒精
        DrugRuleEntity(drugId = 1004L, matchField = "disease_tag", matchValue = "酒精", ruleType = "caution", riskLevel = "high",
            displayMessage = "服药期间不得饮酒，否则增加肝毒性风险。",
            ttsMessage = "服用对乙酰氨基酚期间禁止饮酒。"),
        // 1005 双氯芬酸钠 - 胃溃疡
        DrugRuleEntity(drugId = 1005L, matchField = "disease_tag", matchValue = "胃溃疡", ruleType = "contraindication", riskLevel = "high",
            displayMessage = "消化道溃疡活动期患者禁用双氯芬酸钠。",
            ttsMessage = "消化道溃疡活动期禁用双氯芬酸钠。"),
        // 1007 头孢克肟 - 头孢过敏
        DrugRuleEntity(drugId = 1007L, matchField = "allergy_tag", matchValue = "头孢类过敏", ruleType = "contraindication", riskLevel = "high",
            displayMessage = "头孢克肟对头孢菌素类过敏者禁用。",
            ttsMessage = "检测到头孢类过敏史，请勿使用头孢克肟。"),
        // 1008 阿奇霉素 - 大环内酯过敏
        DrugRuleEntity(drugId = 1008L, matchField = "allergy_tag", matchValue = "大环内酯类过敏", ruleType = "contraindication", riskLevel = "high",
            displayMessage = "阿奇霉素对大环内酯类过敏者禁用。",
            ttsMessage = "检测到大环内酯类过敏史，请勿使用阿奇霉素。"),
        // 1010 左氧氟沙星 - 喹诺酮过敏
        DrugRuleEntity(drugId = 1010L, matchField = "allergy_tag", matchValue = "喹诺酮类过敏", ruleType = "contraindication", riskLevel = "high",
            displayMessage = "左氧氟沙星对喹诺酮类过敏者禁用。",
            ttsMessage = "检测到喹诺酮类过敏史，请勿使用左氧氟沙星。"),
        // 1011 氯沙坦钾 - 高钾血症
        DrugRuleEntity(drugId = 1011L, matchField = "disease_tag", matchValue = "高钾血症", ruleType = "caution", riskLevel = "medium",
            displayMessage = "氯沙坦钾可能引起高钾血症，需监测血钾水平。",
            ttsMessage = "使用氯沙坦钾需注意监测血钾水平。"),
        // 1012 硝苯地平 - 低血压
        DrugRuleEntity(drugId = 1012L, matchField = "disease_tag", matchValue = "低血压", ruleType = "caution", riskLevel = "medium",
            displayMessage = "硝苯地平控释片可能加重低血压症状，需谨慎使用。",
            ttsMessage = "低血压患者使用硝苯地平需注意监测血压。"),
        // 1015 美托洛尔 - 心动过缓
        DrugRuleEntity(drugId = 1015L, matchField = "disease_tag", matchValue = "心动过缓", ruleType = "caution", riskLevel = "high",
            displayMessage = "美托洛尔可能加重心动过缓，心率<50次/分时应减量或停药。",
            ttsMessage = "使用美托洛尔需注意监测心率，心动过缓风险较高。"),
        // 1016 卡托普利 - 肾动脉狭窄
        DrugRuleEntity(drugId = 1016L, matchField = "disease_tag", matchValue = "肾动脉狭窄", ruleType = "contraindication", riskLevel = "high",
            displayMessage = "双侧肾动脉狭窄患者禁用卡托普利。",
            ttsMessage = "双侧肾动脉狭窄患者禁用卡托普利。"),
        // 1016 卡托普利 - 高钾血症
        DrugRuleEntity(drugId = 1016L, matchField = "disease_tag", matchValue = "高钾血症", ruleType = "caution", riskLevel = "medium",
            displayMessage = "卡托普利可能引起高钾血症，需定期监测血钾。",
            ttsMessage = "使用卡托普利需监测血钾水平。"),
        // 1022 比索洛尔 - 哮喘
        DrugRuleEntity(drugId = 1022L, matchField = "disease_tag", matchValue = "哮喘", ruleType = "caution", riskLevel = "medium",
            displayMessage = "比索洛尔可能诱发或加重哮喘症状，支气管哮喘患者需谨慎使用。",
            ttsMessage = "哮喘患者使用比索洛尔需谨慎。"),
        // 1024 氢氯噻嗪 - 低钾血症
        DrugRuleEntity(drugId = 1024L, matchField = "disease_tag", matchValue = "低钾血症", ruleType = "caution", riskLevel = "high",
            displayMessage = "氢氯噻嗪可导致低钾血症，需定期监测血钾水平。",
            ttsMessage = "使用氢氯噻嗪需定期监测血钾水平。"),
        // 1024 氢氯噻嗪 - 磺胺过敏
        DrugRuleEntity(drugId = 1024L, matchField = "allergy_tag", matchValue = "磺胺类药物过敏", ruleType = "contraindication", riskLevel = "high",
            displayMessage = "氢氯噻嗪对磺胺类药物过敏者禁用。",
            ttsMessage = "检测到磺胺类药物过敏史，请勿使用氢氯噻嗪。"),
        // 1026 螺内酯 - 高钾血症
        DrugRuleEntity(drugId = 1026L, matchField = "disease_tag", matchValue = "高钾血症", ruleType = "contraindication", riskLevel = "high",
            displayMessage = "螺内酯可引起高钾血症，高钾血症患者禁用。",
            ttsMessage = "高钾血症患者禁用螺内酯。"),
        // 1027 辛伐他汀 - 肝病
        DrugRuleEntity(drugId = 1027L, matchField = "disease_tag", matchValue = "肝病", ruleType = "caution", riskLevel = "high",
            displayMessage = "辛伐他汀需监测肝功能，肝病患者使用需谨慎。",
            ttsMessage = "肝病患者使用辛伐他汀需定期监测肝功能。"),
        // 1028 阿托伐他汀 - 肝病
        DrugRuleEntity(drugId = 1028L, matchField = "disease_tag", matchValue = "肝病", ruleType = "caution", riskLevel = "high",
            displayMessage = "阿托伐他汀需监测肝功能，活动性肝病患者禁用。",
            ttsMessage = "活动性肝病患者禁用阿托伐他汀。"),
        // 1031 奥美拉唑 - 骨质疏松
        DrugRuleEntity(drugId = 1031L, matchField = "disease_tag", matchValue = "骨质疏松", ruleType = "caution", riskLevel = "medium",
            displayMessage = "长期使用奥美拉唑可能增加骨质疏松和骨折风险。",
            ttsMessage = "长期使用奥美拉唑需注意骨折风险。"),
        // 1034 多潘立酮 - QT间期延长
        DrugRuleEntity(drugId = 1034L, matchField = "disease_tag", matchValue = "QT间期延长", ruleType = "caution", riskLevel = "high",
            displayMessage = "多潘立酮可能引起QT间期延长，有心脏疾病患者需谨慎使用。",
            ttsMessage = "QT间期延长患者需谨慎使用多潘立酮。"),
        // 1037 复方丹参滴丸 - 出血倾向
        DrugRuleEntity(drugId = 1037L, matchField = "disease_tag", matchValue = "出血倾向", ruleType = "caution", riskLevel = "high",
            displayMessage = "复方丹参滴丸与抗凝药同用增加出血风险，出血性疾病患者禁用。",
            ttsMessage = "出血倾向患者禁用复方丹参滴丸。"),
        // 1038 速效救心丸 - 低血压
        DrugRuleEntity(drugId = 1038L, matchField = "disease_tag", matchValue = "低血压", ruleType = "caution", riskLevel = "medium",
            displayMessage = "速效救心丸可能引起血压下降，低血压患者需谨慎。",
            ttsMessage = "低血压患者使用速效救心丸需谨慎。"),
        // 1040 氯吡格雷 - 出血
        DrugRuleEntity(drugId = 1040L, matchField = "disease_tag", matchValue = "消化性溃疡", ruleType = "caution", riskLevel = "high",
            displayMessage = "氯吡格雷增加出血风险，消化性溃疡活动期患者禁用。",
            ttsMessage = "活动性消化性溃疡患者禁用氯吡格雷。"),
        // 1041 阿司匹林 - 胃溃疡
        DrugRuleEntity(drugId = 1041L, matchField = "disease_tag", matchValue = "消化性溃疡", ruleType = "contraindication", riskLevel = "high",
            displayMessage = "阿司匹林肠溶片在消化性溃疡活动期禁用，增加胃出血风险。",
            ttsMessage = "活动性消化性溃疡患者禁用阿司匹林。"),
        // 1041 阿司匹林 - 阿司匹林过敏
        DrugRuleEntity(drugId = 1041L, matchField = "allergy_tag", matchValue = "阿司匹林过敏", ruleType = "contraindication", riskLevel = "high",
            displayMessage = "对阿司匹林过敏者禁用阿司匹林肠溶片。",
            ttsMessage = "检测到阿司匹林过敏史，请勿使用阿司匹林肠溶片。"),
        // 1042 华法林 - 出血
        DrugRuleEntity(drugId = 1042L, matchField = "disease_tag", matchValue = "出血倾向", ruleType = "contraindication", riskLevel = "high",
            displayMessage = "华法林增加出血风险，出血倾向患者禁用。",
            ttsMessage = "出血倾向患者禁用华法林。"),
        // 1043 地高辛 - 心律失常
        DrugRuleEntity(drugId = 1043L, matchField = "disease_tag", matchValue = "心律失常", ruleType = "caution", riskLevel = "high",
            displayMessage = "地高辛治疗窗窄，过量可致严重心律失常，需监测血药浓度和心电图。",
            ttsMessage = "使用地高辛需定期监测血药浓度和心电图。"),
        // 1043 地高辛 - 低钾血症
        DrugRuleEntity(drugId = 1043L, matchField = "disease_tag", matchValue = "低钾血症", ruleType = "caution", riskLevel = "high",
            displayMessage = "低钾血症可增加地高辛毒性，使用地高辛需维持正常血钾水平。",
            ttsMessage = "使用地高辛需保持正常血钾水平。"),
        // 1044 格列美脲 - 低血糖
        DrugRuleEntity(drugId = 1044L, matchField = "disease_tag", matchValue = "低血糖", ruleType = "caution", riskLevel = "high",
            displayMessage = "格列美脲可引起低血糖，需规律进食并监测血糖。",
            ttsMessage = "使用格列美脲需注意低血糖风险。"),
        // 1044 格列美脲 - 肾功能异常
        DrugRuleEntity(drugId = 1044L, matchField = "disease_tag", matchValue = "肾功能异常", ruleType = "caution", riskLevel = "medium",
            displayMessage = "肾功能异常患者使用格列美脲需调整剂量。",
            ttsMessage = "肾功能异常者使用格列美脲需调整剂量。"),
        // 1046 达格列净 - 泌尿道感染
        DrugRuleEntity(drugId = 1046L, matchField = "disease_tag", matchValue = "泌尿道感染", ruleType = "caution", riskLevel = "medium",
            displayMessage = "达格列净增加生殖泌尿道感染风险，反复感染者需谨慎。",
            ttsMessage = "使用达格列净需注意泌尿道感染风险。"),
        // 1048 西格列汀 - 胰腺炎
        DrugRuleEntity(drugId = 1048L, matchField = "disease_tag", matchValue = "胰腺炎", ruleType = "caution", riskLevel = "medium",
            displayMessage = "西格列汀有极罕见胰腺炎报道，有胰腺炎病史者需谨慎。",
            ttsMessage = "有胰腺炎病史者使用西格列汀需谨慎。"),
        // 1052 碳酸钙D3 - 肾脏疾病
        DrugRuleEntity(drugId = 1052L, matchField = "disease_tag", matchValue = "肾结石", ruleType = "caution", riskLevel = "medium",
            displayMessage = "碳酸钙D3片可能增加肾结石风险，有肾结石病史者需在医生指导下使用。",
            ttsMessage = "肾结石患者使用碳酸钙需在医生指导下进行。"),
        // ============================================================
        // 2004 小儿布洛芬混悬液 - 6个月以下婴幼儿
        // ============================================================
        DrugRuleEntity(drugId = 2004L, matchField = "disease_tag", matchValue = "婴幼儿", ruleType = "caution", riskLevel = "high",
            displayMessage = "6个月以下婴幼儿使用小儿布洛芬混悬液必须在医师指导下使用，按体重精确计算剂量。",
            ttsMessage = "6个月以下婴幼儿使用小儿布洛芬需遵医嘱。"),
        // 2005 小儿阿莫西林颗粒 - 青霉素过敏
        DrugRuleEntity(drugId = 2005L, matchField = "allergy_tag", matchValue = "青霉素过敏", ruleType = "contraindication", riskLevel = "high",
            displayMessage = "小儿阿莫西林颗粒对青霉素过敏者禁用，使用前必须确认过敏史。",
            ttsMessage = "青霉素过敏儿童禁用阿莫西林颗粒。"),
        // 2009 甲硝唑栓 - 妊娠期
        DrugRuleEntity(drugId = 2009L, matchField = "disease_tag", matchValue = "妊娠", ruleType = "contraindication", riskLevel = "high",
            displayMessage = "妊娠期前三个月禁用甲硝唑栓。妊娠中后期需在医师指导下使用。",
            ttsMessage = "妊娠期前三个月禁用甲硝唑栓。"),
        // 2011 皮炎平 - 皮肤感染
        DrugRuleEntity(drugId = 2011L, matchField = "disease_tag", matchValue = "皮肤感染", ruleType = "caution", riskLevel = "medium",
            displayMessage = "皮炎平含皮质类固醇，细菌或真菌感染性皮肤病需在医师指导下使用。",
            ttsMessage = "皮肤感染使用皮炎平需遵医嘱。"),
        // 2013 百多邦 - 肾功能异常
        DrugRuleEntity(drugId = 2013L, matchField = "disease_tag", matchValue = "肾功能异常", ruleType = "caution", riskLevel = "medium",
            displayMessage = "百多邦（莫匹罗星）在大面积使用时，肾功能不全者需谨慎。",
            ttsMessage = "肾功能不全者大面积使用百多邦需谨慎。"),
        // 2016 左氧氟沙星滴眼液 - 喹诺酮过敏
        DrugRuleEntity(drugId = 2016L, matchField = "allergy_tag", matchValue = "喹诺酮类过敏", ruleType = "contraindication", riskLevel = "high",
            displayMessage = "左氧氟沙星滴眼液对喹诺酮类药物过敏者禁用。",
            ttsMessage = "喹诺酮类过敏者禁用左氧氟沙星滴眼液。"),
        // 2029 氨溴索 - 胃溃疡
        DrugRuleEntity(drugId = 2029L, matchField = "disease_tag", matchValue = "胃溃疡", ruleType = "caution", riskLevel = "medium",
            displayMessage = "氨溴索可能引起胃肠道不适，胃溃疡患者需谨慎使用。",
            ttsMessage = "胃溃疡患者使用氨溴索需谨慎。"),
        // 2033 沙丁胺醇气雾剂 - 心律失常
        DrugRuleEntity(drugId = 2033L, matchField = "disease_tag", matchValue = "心律失常", ruleType = "caution", riskLevel = "high",
            displayMessage = "沙丁胺醇气雾剂可能引起心悸和心律失常，严重心律失常患者慎用。",
            ttsMessage = "心律失常患者慎用沙丁胺醇气雾剂。"),
        // 2036 云南白药 - 抗凝药同用
        DrugRuleEntity(drugId = 2036L, matchField = "disease_tag", matchValue = "出血倾向", ruleType = "caution", riskLevel = "high",
            displayMessage = "云南白药与抗凝药（华法林、阿司匹林）同用增加出血风险，需在医师指导下使用。",
            ttsMessage = "服用抗凝药期间使用云南白药需谨慎。"),
        // 2038 碘伏 - 碘过敏
        DrugRuleEntity(drugId = 2038L, matchField = "allergy_tag", matchValue = "碘过敏", ruleType = "contraindication", riskLevel = "high",
            displayMessage = "碘伏对碘过敏者禁用。甲状腺疾病患者长期大面积使用需谨慎。",
            ttsMessage = "碘过敏者禁用碘伏。"),
        // 2043 秋水仙碱 - 肾功能异常
        DrugRuleEntity(drugId = 2043L, matchField = "disease_tag", matchValue = "肾功能异常", ruleType = "caution", riskLevel = "high",
            displayMessage = "秋水仙碱治疗窗窄，肾功能不全者需减量使用，出现胃肠道反应立即停药。",
            ttsMessage = "肾功能不全者使用秋水仙碱需减量并密切监测。"),
        // 2044 别嘌醇 - 超敏反应
        DrugRuleEntity(drugId = 2044L, matchField = "allergy_tag", matchValue = "药物过敏", ruleType = "caution", riskLevel = "high",
            displayMessage = "别嘌醇在亚洲人群中可能出现严重超敏反应（DRESS综合征），用药前建议检测HLA-B*5801基因。",
            ttsMessage = "使用别嘌醇需警惕超敏反应风险。"),
        // 2046 甲氨蝶呤 - 肝病
        DrugRuleEntity(drugId = 2046L, matchField = "disease_tag", matchValue = "肝病", ruleType = "caution", riskLevel = "high",
            displayMessage = "甲氨蝶呤有肝毒性，肝病患者使用需定期监测肝功能，严重肝病禁用。",
            ttsMessage = "肝病患者使用甲氨蝶呤需监测肝功能。"),
        // 2046 甲氨蝶呤 - 妊娠
        DrugRuleEntity(drugId = 2046L, matchField = "disease_tag", matchValue = "妊娠", ruleType = "contraindication", riskLevel = "high",
            displayMessage = "甲氨蝶呤有致畸作用，妊娠期及哺乳期妇女禁用。",
            ttsMessage = "妊娠期妇女禁用甲氨蝶呤。")
    )

    private val seedUserProfile = UserProfileEntity(
        userId = 1L,
        nickname = "演示用户",
        ageGroup = "adult",
        diseaseTags = "[\"高血压\",\"糖尿病\"]",
        allergyTags = "[\"青霉素过敏\"]",
        currentDrugs = "[\"氯沙坦钾片\"]",
        notes = "用于比赛演示的默认健康档案。"
    )
}
