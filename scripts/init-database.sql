-- ============================================================================
-- 微光同行 (WeiGuangPlus) 数据库初始化脚本
-- 项目描述：面向视障人士的Android无障碍助残APP后端数据库
-- 技术栈：Python FastAPI + SQLAlchemy + PostgreSQL 15+
-- 功能模块：药品OCR识别、过敏原匹配、SOS紧急求助、用户管理
-- 创建时间：2026-05-28
-- 字符编码：UTF-8
-- 时区设置：Asia/Shanghai (UTC+8)
-- ============================================================================

-- ============================================================================
-- 第一部分：数据库创建与基础配置
-- 功能说明：
--   1. 创建专用数据库（如不存在）
--   2. 创建应用专用数据库用户并设置密码策略
--   3. 授予用户对数据库的完整权限
--   4. 配置数据库时区为北京时间
--   5. 启用必要的PostgreSQL扩展（UUID、JSONB增强等）
-- ============================================================================

-- [1.1] 创建微光同行专用数据库
-- 使用UTF-8编码确保中文数据存储无误
-- LC_COLLATE和LC_CTYPE设置为中文排序规则，支持中文排序和比较
CREATE DATABASE weiguangplus
    WITH ENCODING = 'UTF8'
         LC_COLLATE = 'zh_CN.UTF-8'
         LC_CTYPE = 'zh_CN.UTF-8'
         TEMPLATE = template0
         CONNECTION LIMIT = -1;  -- -1表示无连接数限制

-- [1.2] 连接到新创建的数据库（后续所有操作在此数据库中执行）
-- 注意：在psql中执行此脚本前，需要先 \c weiguangplus 或使用 -d 参数

-- [1.3] 创建应用专用数据库用户
-- 用户名：wg_admin（微光管理员）
-- 密码策略：建议生产环境使用强密码（至少12位，含大小写字母、数字、特殊字符）
-- 安全说明：此用户拥有该数据库的完全控制权限，仅用于应用层连接
CREATE USER wg_admin WITH
    PASSWORD 'Wg@2026Secure!Pwd'       -- 生产环境请修改为强密码
    NOSUPERUSER                         -- 非超级用户，遵循最小权限原则
    NOCREATEDB                          -- 不能创建数据库
    NOCREATEROLE;                       -- 不能创建角色

-- [1.4] 授予用户对weiguangplus数据库的所有权限
-- 包括：连接权限、schema权限、表权限、序列权限等
GRANT ALL PRIVILEGES ON DATABASE weiguangplus TO wg_admin;

-- [1.5] 授权默认public schema的完整访问权限
-- PostgreSQL的表默认创建在public schema中
GRANT ALL ON SCHEMA public TO wg_admin;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO wg_admin;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO wg_admin;

-- 设置默认权限：未来创建的表自动授权给wg_admin
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT ALL ON TABLES TO wg_admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT ALL ON SEQUENCES TO wg_admin;

-- [1.6] 设置数据库时区为Asia/Shanghai（北京时间）
-- 这会影响TIMESTAMPTZ类型的显示和存储
-- 所有时间戳将自动转换为UTC+8存储和显示
ALTER DATABASE weiguangplus SET timezone TO 'Asia/Shanghai';

-- [1.7] 启用PostgreSQL扩展
-- uuid-ossp：提供UUID生成函数（用于生成唯一标识符）
-- pg_trgm：提供三元组模糊匹配（用于药品名称模糊搜索）
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS pg_trgm;


-- ============================================================================
-- 第二部分：通用工具函数与触发器
-- 功能说明：
--   1. 创建updated_at自动更新触发器函数
--   2. 该函数会在记录更新时自动将updated_at字段设为当前时间
--   3. 所有包含updated_at字段的表都应使用此触发器
-- ============================================================================

-- [2.1] 创建自动更新时间戳的触发器函数
-- 函数逻辑：当UPDATE操作执行时，自动将updated_at列设置为当前时间戳
-- 返回类型：TRIGGER（触发器函数必须返回此类型）
-- 语言：PL/pgSQL（PostgreSQL内置过程语言）
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION update_updated_at_column() IS
    '通用触发器函数：自动更新表的updated_at字段为当前时间戳，用于追踪记录最后修改时间';


-- ============================================================================
-- 第三部分：users 用户表（核心业务表）
-- 表功能：
--   存储视障用户的基本信息、健康档案、过敏史、用药清单等
-- 设计理念：
--   - 使用TEXT[]数组类型存储过敏史和慢性病，便于快速查询和索引
--   - 使用JSONB类型存储当前用药清单，灵活应对复杂的用药结构
--   - 使用JSONB存储紧急联系人冗余数据，提高SOS响应速度
-- 业务规则：
--   - 手机号作为唯一登录凭证
--   - 密码使用bcrypt哈希存储（应用层处理）
--   - 支持多种残疾类型和等级分类
-- ============================================================================

-- [3.1] 创建users表
CREATE TABLE users (
    -- 主键：自增序列ID
    -- 类型选择理由：SERIAL是INT + SERIAL的组合，适用于中小规模用户量（约20亿上限）
    -- 如需更大容量可改用BIGSERIAL
    id SERIAL PRIMARY KEY,

    -- 手机号：用户唯一登录标识
    -- 类型选择：VARCHAR(20) - 中国手机号11位，预留国际号码空间
    -- 约束：UNIQUE NOT NULL - 确保唯一性且不允许为空
    phone VARCHAR(20) UNIQUE NOT NULL,

    -- 密码哈希值：存储bcrypt加密后的密码
    -- 类型选择：VARCHAR(255) - bcrypt哈希固定60字符，预留足够空间
    -- 安全说明：明文密码绝对不存入数据库，哈希运算在应用层完成
    password_hash VARCHAR(255) NOT NULL,

    -- 用户昵称：显示名称
    -- 类型选择：VARCHAR(50) - 足够容纳大多数中文昵称
    nickname VARCHAR(50),

    -- 头像URL：MinIO对象存储路径
    -- 类型选择：VARCHAR(500) - MinIO URL可能较长，包含bucket和object path
    -- 存储格式示例：http://minio:9000/weiguangplus/avatars/user_123.jpg
    avatar_url VARCHAR(500),

    -- 残疾类型：视觉障碍的具体分类
    -- 可选值：全盲/低视力/色盲/夜盲/其他
    -- 用途：个性化无障碍功能配置（如语音语速、对比度等）
    disability_type VARCHAR(50),

    -- 残疾等级：官方认定的残疾等级
    -- 可选值：一级/二级/三级/四级（一级最重）
    -- 数据来源：残疾人证信息
    disability_level VARCHAR(20),

    -- 过敏史：用户已知的过敏物质列表
    -- 类型选择：TEXT[] - PostgreSQL原生数组类型
    -- 优势：支持GIN索引加速查询，可直接用ANY/ALL语法查询
    -- 示例值：{'青霉素','磺胺类药物','花生'}
    allergy_history TEXT[],

    -- 慢性病史：用户长期患有的慢性疾病
    -- 类型选择：TEXT[] - 同上，便于多条件筛选
    -- 示例值：{'高血压','糖尿病','冠心病'}
    chronic_diseases TEXT[],

    -- 当前用药清单：用户正在服用的药品详细信息
    -- 类型选择：JSONB - 灵活的文档结构
    -- 选择理由：每种药品需要记录名称、剂量、频次、开始时间等复杂信息
    -- JSONB优势：支持内部字段查询和索引，比JSON性能更好
    -- 结构示例：
    -- [
    --   {"drug_name": "阿莫西林", "dosage": "500mg", "frequency": "每日3次", "started_at": "2026-01-15"},
    --   {"drug_name": "布洛芬", "dosage": "200mg", "frequency": "必要时", "started_at": "2026-02-01"}
    -- ]
    current_medications JSONB,

    -- 紧急联系人列表（冗余存储）：用于SOS事件快速获取
    -- 类型选择：JSONB - 冗余自emergency_contacts表，提高读取速度
    -- 为什么冗余？SOS场景要求毫秒级响应，避免JOIN操作
    -- 结构示例：
    -- [
    --   {"name": "张三", "relationship": "家人", "phone": "13800138000", "is_primary": true},
    --   {"name": "李医生", "relationship": "医生", "phone": "13900139000", "is_primary": false}
    -- ]
    emergency_contacts JSONB DEFAULT '[]',

    -- 账户状态：是否激活
    -- 默认值：TRUE - 新注册用户默认激活
    -- 用途：支持软删除/封禁账户，不直接删除数据保留历史记录
    is_active BOOLEAN DEFAULT TRUE,

    -- 记录创建时间：带时区的时间戳
    -- 类型选择：TIMESTAMPTZ - 自动转换为数据库时区(Asia/Shanghai)
    created_at TIMESTAMPTZ DEFAULT NOW(),

    -- 记录最后更新时间：由触发器自动维护
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- [3.2] 为users表添加详细注释
COMMENT ON TABLE users IS
    '用户主表：存储视障人士的基本信息、健康档案、过敏史、用药清单等核心数据。
     作为系统的核心实体，几乎所有业务表都通过user_id关联到此表。
     设计原则：平衡规范化与查询性能，对高频访问数据适当冗余。';

COMMENT ON COLUMN users.id IS
    '用户唯一标识：自增主键，用于与其他表建立外键关系';
COMMENT ON COLUMN users.phone IS
    '手机号：用户的唯一登录账号，格式为中国大陆手机号（11位）或国际号码';
COMMENT ON COLUMN users.password_hash IS
    '密码哈希：bcrypt算法加密后的密码字符串（60位固定长度），绝对不存储明文密码';
COMMENT ON COLUMN users.nickname IS
    '用户昵称：显示名称，可用于社区互动和个人展示';
COMMENT ON COLUMN users.avatar_url IS
    '头像地址：MinIO对象存储的完整URL路径，格式为http://host:port/bucket/object';
COMMENT ON COLUMN users.disability_type IS
    '残疾类型：视觉障碍的具体分类，可选值包括全盲/低视力/色盲/夜盲/其他';
COMMENT ON COLUMN users.disability_level IS
    '残疾等级：国家认定的等级标准，一级（最重）至四级（最轻）';
COMMENT ON COLUMN users.allergy_history IS
    '过敏史：已知过敏物质数组，使用PostgreSQL原生数组类型，支持高效查询和GIN索引。
     示例：{青霉素,磺胺类药物,花生,海鲜}';
COMMENT ON COLUMN users.chronic_diseases IS
    '慢性病史：长期患有的疾病列表，用于药品禁忌症匹配和健康风险评估';
COMMENT ON COLUMN users.current_medications IS
    '当前用药清单：JSONB格式存储正在使用的药品详细信息，
     包含药品名、剂量、服用频次、开始日期等。用于药物相互作用检测。';
COMMENT ON COLUMN users.emergency_contacts IS
    '紧急联系人缓存：从emergency_contacts表冗余存储，优化SOS事件的毫秒级响应需求。
     当联系人变更时需同步更新此字段。';
COMMENT ON COLUMN users.is_active IS
    '账户状态：TRUE表示正常激活，FALSE表示已禁用或注销（软删除）';
COMMENT ON COLUMN users.created_at IS
    '注册时间：账户创建的精确时间戳，带时区信息';
COMMENT ON COLUMN users.updated_at IS
    '更新时间：最后一次修改记录的时间，由触发器自动维护';

-- [3.3] 为users表创建索引
-- 索引1：phone唯一索引（UNIQUE约束已隐式创建，此处显式声明增强可读性）
-- 作用：加速手机号登录查询，确保O(1)复杂度
-- 使用场景：用户登录、手机号查找、账号唯一性校验
CREATE UNIQUE INDEX idx_users_phone_unique ON users(phone);

-- 索引2：created_at普通索引
-- 作用：按注册时间排序和范围查询
-- 使用场景：用户增长统计、按时间段筛选用户、分页查询
CREATE INDEX idx_users_created_at ON users(created_at);

-- 索引3：disability_type索引（可选，根据查询频率决定是否启用）
-- 作用：按残疾类型筛选用户
-- 使用场景：统计各类残疾用户数量、推送针对性功能
-- CREATE INDEX idx_users_disability_type ON users(disability_type);

-- 索引4：is_active索引（高优先级）
-- 作用：快速过滤活跃/非活跃用户
-- 使用场景：登录验证、用户列表展示、后台管理
CREATE INDEX idx_users_is_active ON users(is_active);

-- [3.4] 为users表创建updated_at自动更新触发器
-- 触发时机：每次UPDATE操作之前（BEFORE UPDATE）
-- 作用：自动将updated_at字段设置为当前时间戳
CREATE TRIGGER trigger_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();


-- ============================================================================
-- 第四部分：drugs 药品主表（知识库核心表）
-- 表功能：
--   存储药品的完整信息，包括成分、适应症、禁忌症、不良反应等
--   作为OCR识别结果匹配的目标库
--   提供过敏原检测和药物相互作用判断的数据基础
-- 设计特点：
--   - 使用TEXT[]数组存储标签化数据（适应症、禁忌症等），支持高效过滤
--   - 使用JSONB存储结构化数据（成分、相互作用规则），支持灵活查询
--   - 使用GIN索引加速全文检索和数组元素查询
-- 数据来源：
--   - manual：人工录入（权威药典数据）
--   - ocr：OCR识别后人工审核
--   - import：批量导入（第三方药品库）
-- ============================================================================

-- [4.1] 创建drugs表
CREATE TABLE drugs (
    -- 主键：自增序列ID
    id SERIAL PRIMARY KEY,

    -- 通用名：药品的标准化学名或法定名称
    -- 类型选择：VARCHAR(200) - 足够容纳长药品名（如"盐酸左氧氟沙星氯化钠注射液"）
    -- 约束：NOT NULL - 通用名为必填项
    drug_name VARCHAR(200) NOT NULL,

    -- 商品名/品牌名：药厂注册的商品名称
    -- 类型选择：VARCHAR(200) - 品牌名通常较短但可能有多个
    -- 示例：泰诺、芬必得、感康
    brand_name VARCHAR(200),

    -- 别名数组：药品的其他常见称呼
    -- 类型选择：TEXT[] - 一个药品可能有多个别名
    -- 示例：{'对乙酰氨基酚片','扑热息痛','APAP'}
    -- 用途：提高OCR识别匹配率，用户可能扫描到别名而非通用名
    alias_names TEXT[],

    -- 药品分类：按照治疗领域或药理作用分类
    -- 可选值：感冒/止痛/降压/降糖/抗生素/心血管/消化/外用/其他
    category VARCHAR(50),

    -- 剂型：药品的物理形态
    -- 可选值：片剂/胶囊/颗粒/注射液/口服液/膏剂/滴眼液/贴剂
    dosage_form VARCHAR(50),

    -- 规格：药品的含量规格
    -- 类型选择：VARCHAR(100) - 规格格式多样（如"0.5g*12片"、"10ml:0.3g"）
    specification VARCHAR(100),

    -- 生产厂家/制药企业
    -- 类型选择：VARCHAR(200) - 厂家全称
    manufacturer VARCHAR(200),

    -- 成分列表：药品的有效成分及其含量
    -- 类型选择：JSONB - 每种成分需要名称、含量、单位等信息
    -- 结构示例：
    -- [
    --   {"name": "对乙酰氨基酚", "content": "500mg", "role": "有效成分"},
    --   {"name": "淀粉", "content": "适量", "role": "辅料"}
    -- ]
    ingredients JSONB,

    -- 适应症：药品可以治疗的疾病或症状列表
    -- 类型选择：TEXT[] - 多个适应症，支持独立查询
    -- 示例：{'普通感冒','流行性感冒引起的发热头痛','关节痛','偏头痛'}
    indications TEXT[],

    -- 禁忌症：禁止使用该药品的情况列表
    -- 类型选择：TEXT[] - 用于与用户过敏史、慢性病进行交叉比对
    -- 示例：{'对本品过敏者禁用','严重肝肾功能不全者禁用','孕妇禁用'}
    contraindications TEXT[],

    -- 注意事项：使用药品时需要注意的事项
    -- 类型选择：TEXT[] - 多条注意事项
    precautions TEXT[],

    -- 不良反应：可能出现的副作用列表
    -- 类型选择：TEXT[] - 用于向用户提示风险
    adverse_reactions TEXT[],

    -- 孕妇用药风险分级：FDA妊娠期用药分级标准
    -- 可选值：A/B/C/D/X
    -- A级：对照研究显示无害（极少药品）
    -- B级：没有证据显示有害
    -- C级：不能排除危害性（大多数药品属于此类）
    -- D级：有明确证据显示有风险（但在某些情况下可能获益）
    -- X级：动物或人体研究明确显示畸形或胎儿异常（绝对禁用）
    pregnancy_risk VARCHAR(20),

    -- 儿童用药说明
    -- 类型选择：TEXT - 可能是大段文字说明
    pediatric_use TEXT,

    -- 老年人用药说明
    elderly_use TEXT,

    -- 过敏原标签：该药品包含的常见过敏原标记
    -- 类型选择：TEXT[] - 与用户allergy_history字段进行交集匹配
    -- 示例：{'青霉素类','磺胺类','酒精','鸡蛋蛋白'}
    -- 核心用途：这是系统最重要的安全检查字段！
    allergen_tags TEXT[],

    -- 药物相互作用规则：与其他药品同服时的注意事项
    -- 类型选择：JSONB - 结构化存储相互作用对象和后果
    -- 结构示例：
    -- [
    --   {
    --     "interact_with": "华法林",
    --     "effect": "增强抗凝作用，增加出血风险",
    --     "severity": "HIGH",
    --     "mechanism": "抑制CYP2C9酶"
    --   }
    -- ]
    interaction_rules JSONB,

    -- 是否处方药：区分处方药和非处方药（OTC）
    -- 默认值：FALSE（OTC）
    -- 用途：提醒用户部分药品需要医生处方
    is_prescription BOOLEAN DEFAULT FALSE,

    -- 数据来源标识
    -- 可选值：manual（人工录入）/ ocr（OCR识别审核）/ import（批量导入）/ api（第三方API）
    data_source VARCHAR(50) DEFAULT 'manual',

    -- 时间戳字段
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- [4.2] 为drugs表添加详细注释
COMMENT ON TABLE drugs IS
    '药品主表：存储药品知识库的核心数据，包括药品基本信息、成分、适应症、
     禁忌症、不良反应、过敏原标签等。作为OCR识别结果匹配的目标库，
     是药品安全和过敏原检测功能的数据基础。建议定期从权威药典同步更新。';

COMMENT ON COLUMN drugs.id IS
    '药品唯一标识：自增主键，被drug_recognition_records表的matched_drug_id引用';
COMMENT ON COLUMN drugs.drug_name IS
    '通用名：药品的标准化学名或法定名称（必填），是主要搜索和匹配依据';
COMMENT ON COLUMN drugs.brand_name IS
    '商品名：药厂注册的品牌名称（如泰诺、芬必得），辅助识别匹配';
COMMENT ON COLUMN drugs.alias_names IS
    '别名数组：药品的其他常见称呼（如扑热息痛=对乙酰氨基酚），
     显著提升OCR识别的匹配成功率';
COMMENT ON COLUMN drugs.category IS
    '药品分类：按治疗领域分类（感冒/止痛/降压/降糖/抗生素等），用于分类筛选和统计';
COMMENT ON COLUMN drugs.dosage_form IS
    '剂型：药品的物理形态（片剂/胶囊/注射液/口服液等）';
COMMENT ON COLUMN drugs.specification IS
    '规格：药品含量规格（如0.5g*12片、10ml:0.3g）';
COMMENT ON COLUMN drugs.manufacturer IS
    '生产厂家：制药企业的全称';
COMMENT ON COLUMN drugs.ingredients IS
    '成分详情：JSONB格式存储有效成分和辅料信息，用于深度成分分析';
COMMENT ON COLUMN drugs.indications IS
    '适应症数组：该药品可治疗的疾病或症状列表';
COMMENT ON COLUMN drugs.contraindications IS
    '禁忌症数组：禁止使用该药品的情况，与用户健康档案交叉比对的关键数据';
COMMENT ON COLUMN drugs.precautions IS
    '注意事项数组：使用时需特别留意的事项';
COMMENT ON COLUMN drugs.adverse_reactions IS
    '不良反应数组：可能出现的副作用，用于向用户预警';
COMMENT ON COLUMN drugs.pregnancy_risk IS
    '孕妇风险等级：FDA妊娠期用药分级（A/B/C/D/X），X级为绝对禁用';
COMMENT ON COLUMN drugs.pediatric_use IS
    '儿童用药：儿童使用该药品的特殊说明和要求';
COMMENT ON COLUMN drugs.elderly_use IS
    '老年用药：老年人使用该药品的剂量调整和特殊注意';
COMMENT ON COLUMN drugs.allergen_tags IS
    '过敏原标签：该药品包含的潜在过敏原标记数组，
     与用户allergy_history字段进行快速交集匹配，是系统核心安全检查字段！';
COMMENT ON COLUMN drugs.interaction_rules IS
    '相互作用规则：JSONB格式存储与其他药品的相互作用信息，
     包括相互作用对象、严重程度、作用机制等';
COMMENT ON COLUMN drugs.is_prescription IS
    '处方药标识：TRUE表示处方药，FALSE表示非处方药(OTC)';
COMMENT ON COLUMN drugs.data_source IS
    '数据来源：manual(人工录入)/ocr(OCR审核)/import(批量导入)/api(第三方API)';
COMMENT ON COLUMN drugs.created_at IS
    '录入时间：药品数据首次添加到知识库的时间';
COMMENT ON COLUMN drugs.updated_at IS
    '更新时间：药品信息最后修改的时间，由触发器自动维护';

-- [4.3] 为drugs表创建索引（重点优化）

-- 索引1：drug_name B-tree索引
-- 作用：精确匹配和前缀查询
-- 使用场景：OCR结果精确匹配、药品名称搜索
CREATE INDEX idx_drugs_drug_name ON drugs(drug_name);

-- 索引2：brand_name索引
-- 作用：按商品名查找药品
-- 使用场景：用户习惯用商品名搜索（如搜"泰诺"而不是"对乙酰氨基酚"）
CREATE INDEX idx_drugs_brand_name ON drugs(brand_name);

-- 索引3：category索引
-- 作用：按分类筛选药品
-- 使用场景：按类别浏览药品库、统计分析各类药品数量
CREATE INDEX idx_drugs_category ON drugs(category);

-- 索引4：allergen_tags GIN索引（关键索引！）
-- 索引类型：GIN（Generalized Inverted Index）- 专门用于数组/全文检索
-- 作用：极速查询包含特定过敏原的药品
-- 使用场景：用户过敏史匹配 - SELECT * FROM drugs WHERE allergen_tags && ARRAY['青霉素']
-- 性能：将O(n)全表扫描优化为O(log n)索引查找，大数据量下性能提升显著
CREATE INDEX idx_drugs_allergen_tags_gin ON drugs USING GIN(allergen_tags);

-- 索引5：indications GIN索引
-- 作用：按适应症快速筛选药品
-- 使用场景："我有头痛，能吃什么药？"这类查询
CREATE INDEX idx_drugs_indications_gin ON drugs USING GIN(indications);

-- 索引6：contraindications GIN索引
-- 作用：快速排除有特定禁忌症的药品
-- 使用场景：用户有某疾病时，排除相关禁忌药品
CREATE INDEX idx_drugs_contraindications_gin ON drugs USING GIN(contraindications);

-- 索引7：is_prescription索引
-- 作用：快速区分处方药和OTC
-- 使用场景：OTC药品推荐、处方药警示
CREATE INDEX idx_drugs_is_prescription ON drugs(is_prescription);

-- 索引8：drug_name三元组模糊匹配索引（高级特性）
-- 索引类型：GIN + pg_trgm扩展
-- 作用：支持模糊搜索（LIKE '%keyword%'、相似度查询）
-- 使用场景：OCR识别结果不完整或有错别字时的模糊匹配
-- 前提：需要pg_trgm扩展（已在第一部分启用）
CREATE INDEX idx_drugs_drug_name_trgm ON drugs USING GIN(drug_name gin_trgm_ops);

-- [4.4] 为drugs表创建updated_at触发器
CREATE TRIGGER trigger_drugs_updated_at
    BEFORE UPDATE ON drugs
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();


-- ============================================================================
-- 第五部分：drug_recognition_records 药品识别记录表
-- 表功能：
--   记录用户每次使用OCR识别药品的完整过程和结果
--   存储原始OCR文本、匹配结果、风险评估、过敏原匹配等
--   支持历史记录查询、收藏、笔记等功能
-- 业务价值：
--   - 用户可以回顾之前的识别记录
--   - 收藏常用药品便于快速查看
--   - 记录个人用药历史，辅助健康管理
-- 数据特点：
--   - 写入频率中等（用户主动触发OCR）
--   - 读取消耗较高（历史记录浏览）
--   - 数据量随用户量和使用频率线性增长
-- ============================================================================

-- [5.1] 创建drug_recognition_records表
CREATE TABLE drug_recognition_records (
    -- 主键
    id SERIAL PRIMARY KEY,

    -- 用户ID：外键关联到users表
    -- 级联策略：ON DELETE CASCADE - 用户删除时，其所有识别记录一并删除
    -- 理由：识别记录归属用户，用户不存在则记录无意义
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    -- 图片URL：用户上传的药品照片
    -- 存储位置：MinIO对象存储
    -- 格式示例：http://minio:9000/weiguangplus/ocr_images/record_001.jpg
    image_url VARCHAR(500) NOT NULL,

    -- OCR原始识别文本：OCR引擎返回的原始文字内容
    -- 类型选择：TEXT - OCR结果可能很长（整张说明书文字）
    -- 保留原因：用于调试、人工复核、改进OCR模型
    ocr_raw_text TEXT NOT NULL,

    -- 匹配到的药品ID：外键关联到drugs表
    -- 级联策略：ON DELETE SET NULL - 药品从知识库删除时，保留记录但清空匹配ID
    -- 理由：历史识别记录不应因药品数据清理而丢失
    matched_drug_id INTEGER REFERENCES drugs(id) ON DELETE SET NULL,

    -- 匹配置信度分数：OCR+匹配算法的综合置信度
    -- 取值范围：0.0 - 1.0（0% - 100%）
    -- 应用规则：
    --   > 0.9：高度可信，可直接采用
    --   0.7 - 0.9：较可信，建议用户确认
    --   < 0.7：低可信，强烈建议人工核实
    confidence_score FLOAT CHECK (confidence_score >= 0 AND confidence_score <= 1),

    -- 风险等级评估：基于用户健康档案综合评估的安全风险
    -- 可选值：LOW/MEDIUM/HIGH/CRITICAL
    -- LOW：无明显风险，可正常使用
    -- MEDIUM：存在轻微风险，需注意
    -- HIGH：存在较大风险，强烈警告
    -- CRITICAL：严重危险，禁止使用（如严重过敏史匹配）
    risk_level VARCHAR(10) CHECK (risk_level IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),

    -- 风险详情：结构化的风险评估报告
    -- 类型选择：JSONB - 包含多项风险因素和分析结果
    -- 结构示例：
    -- {
    --   "allergy_risk": "HIGH",
    --   "interaction_risk": "MEDIUM",
    --   "contraindication_risk": "LOW",
    --   "details": ["用户对青霉素类过敏，本品含相关成分"]
    -- }
    risk_details JSONB,

    -- 过敏原匹配结果：详细的过敏原交叉比对结果
    -- 类型选择：JSONB - 记录每个匹配到的过敏原及严重程度
    -- 结构示例：
    -- [
    --   {
    --     "allergen": "青霉素",
    --     "matched_in": "ingredients",
    --     "severity": "CRITICAL",
    --     "user_allergy_record": "2020年过敏性休克"
    --   }
    -- ]
    allergen_matches JSONB,

    -- 相互作用警告：与用户当前用药的潜在冲突
    -- 类型选择：TEXT[] - 多条警告信息
    -- 示例：{'与阿司匹林同服增加出血风险','与降压药同服可能影响疗效'}
    interaction_warnings TEXT[],

    -- 是否收藏：用户可将重要记录收藏以便快速访问
    is_bookmarked BOOLEAN DEFAULT FALSE,

    -- 用户备注：用户对该次识别的个人笔记
    -- 类型选择：TEXT - 可能是大段文字
    user_notes TEXT,

    -- 识别耗时：从上传图片到返回结果的耗时（秒）
    -- 类型选择：FLOAT - 精确到毫秒级
    -- 用途：性能监控、用户体验优化
    recognition_time FLOAT,

    -- 识别时间：记录创建时间（即识别发生时间）
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- [5.2] 为drug_recognition_records表添加注释
COMMENT ON TABLE drug_recognition_records IS
    '药品识别记录表：记录用户每次OCR识别药品的完整过程和结果。
     包含原始OCR文本、匹配药品、置信度、风险评估、过敏原匹配等核心数据。
     支持历史查询、收藏、笔记功能，构成用户的个人用药识别档案。';

COMMENT ON COLUMN drug_recognition_records.id IS
    '记录唯一标识：自增主键';
COMMENT ON COLUMN drug_recognition_records.user_id IS
    '用户ID：外键关联users表，标识本次识别的操作者';
COMMENT ON COLUMN drug_recognition_records.image_url IS
    '图片URL：用户上传的药品包装照片，存储于MinIO对象存储';
COMMENT ON COLUMN drug_recognition_records.ocr_raw_text IS
    'OCR原始文本：OCR引擎识别出的完整文字内容，保留用于调试和人工复核';
COMMENT ON COLUMN drug_recognition_records.matched_drug_id IS
    '匹配药品ID：外键关联drugs表，指向识别结果匹配到的药品（可能为NULL表示未匹配到）';
COMMENT ON COLUMN drug_recognition_records.confidence_score IS
    '置信度分数：0.0-1.0之间，表示匹配结果的可信程度';
COMMENT ON COLUMN drug_recognition_records.risk_level IS
    '风险等级：LOW/MEDIUM/HIGH/CRITICAL，基于用户健康档案综合评估';
COMMENT ON COLUMN drug_recognition_records.risk_details IS
    '风险详情：JSONB格式的结构化风险评估报告';
COMMENT ON COLUMN drug_recognition_records.allergen_matches IS
    '过敏原匹配：JSONB格式的详细过敏原交叉比对结果列表';
COMMENT ON COLUMN drug_recognition_records.interaction_warnings IS
    '相互作用警告：与用户当前用药的潜在冲突列表';
COMMENT ON COLUMN drug_recognition_records.is_bookmarked IS
    '收藏状态：用户是否收藏了这条识别记录';
COMMENT ON COLUMN drug_recognition_records.user_notes IS
    '用户备注：用户对该次识别添加的个人笔记或心得';
COMMENT ON COLUMN drug_recognition_records.recognition_time IS
    '识别耗时：OCR识别+匹配的总耗时（秒），用于性能监控';
COMMENT ON COLUMN drug_recognition_records.created_at IS
    '识别时间：记录创建时间，即OCR识别发生的时刻';

-- [5.3] 为drug_recognition_records表创建索引

-- 索引1：user_id + created_at 联合复合索引（核心查询索引）
-- 索引类型：B-tree复合索引
-- 列顺序：user_id在前，created_at在后
-- 作用：极速查询某个用户的历史识别记录并按时间倒序排列
-- 使用场景：用户进入"我的识别记录"页面时的分页查询
-- SQL示例：SELECT * FROM drug_recognition_records WHERE user_id = ? ORDER BY created_at DESC LIMIT 20
-- 性能优化：避免额外的排序操作（Sort操作），直接利用索引有序性
CREATE INDEX idx_recognition_user_time ON drug_recognition_records(user_id, created_at DESC);

-- 索引2：matched_drug_id索引
-- 作用：反向查询某药品被识别的次数和用户分布
-- 使用场景：热门药品统计、药品识别成功率分析
CREATE INDEX idx_recognition_matched_drug ON drug_recognition_records(matched_drug_id);

-- 索引3：user_id + is_bookmarked 复合索引
-- 作用：快速查询用户的收藏记录
-- 使用场景：用户查看"我的收藏"列表
CREATE INDEX idx_recognition_user_bookmark ON drug_recognition_records(user_id, is_bookmarked);

-- 索引4：risk_level索引
-- 作用：按风险等级筛选记录
-- 使用场景：高风险记录审计、安全事件回溯
CREATE INDEX idx_recognition_risk_level ON drug_recognition_records(risk_level);

-- 索引5：confidence_score索引
-- 作用：按置信度范围查询
-- 使用场景：低置信度记录的人工审核队列
CREATE INDEX idx_recognition_confidence ON drug_recognition_records(confidence_score);


-- ============================================================================
-- 第六部分：sos_events SOS紧急求助事件表
-- 表功能：
--   记录用户触发的每一次SOS紧急求助事件
--   存储事件类型、地理位置、通知联系人、事件状态等完整信息
--   支持事件生命周期管理（激活→解决/取消）
-- 业务重要性：
--   - 这是关乎生命安全的核心功能表
--   - 数据可靠性和查询速度至关重要
--   - 需要支持高并发写入（SOS事件可能突发）
-- 设计考虑：
--   - 地理位置精度：GPS坐标 + 地址描述 + 定位精度
--   - 通知追踪：记录已通知的联系人ID和短信发送次数
--   - 状态机：ACTIVE → RESOLVED/CANCELLED
-- ============================================================================

-- [6.1] 创建sos_events表
CREATE TABLE sos_events (
    -- 主键
    id SERIAL PRIMARY KEY,

    -- 用户ID：外键关联到users表
    -- 级联策略：ON DELETE CASCADE - 用户删除时清除SOS记录
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    -- 场景类型：SOS触发的具体情境
    -- 可选值：疾病突发/迷路走失/交通事故/意外跌倒/其他
    -- 用途：统计分析和针对性救援资源调度
    scenario_type VARCHAR(50) NOT NULL,

    -- 严重程度：事件的紧急级别
    -- 可选值：LOW/MEDIUM/HIGH/CRITICAL
    -- LOW：轻微情况，可等待常规响应
    -- MEDIUM：需要关注，优先处理
    -- HIGH：紧急情况，立即响应
    -- CRITICAL：危及生命，最高优先级
    severity VARCHAR(20) NOT NULL CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),

    -- 纬度：GPS定位纬度坐标
    -- 取值范围：-90 到 90
    -- 精度：浮点型，一般保留6位小数（精度约10厘米）
    latitude FLOAT,

    -- 经度：GPS定位经度坐标
    -- 取值范围：-180 到 180
    longitude FLOAT,

    -- 位置地址描述：地理编码后的可读地址
    -- 类型选择：TEXT - 地址可能较长
    -- 示例：北京市朝阳区建国路88号SOHO现代城A座
    location_address TEXT,

    -- 定位精度：GPS定位的误差范围（米）
    -- 数值越小越精确
    -- 典型值：室外5-15米，室内30-100米
    accuracy FLOAT,

    -- 已通知的联系人ID列表：记录已经发送过SOS通知的联系人
    -- 类型选择：INTEGER[] - 存储emergency_contacts表的id数组
    -- 初始值：空数组 '{}'
    -- 用途：避免重复发送通知，跟踪通知覆盖范围
    notified_contacts INTEGER[] DEFAULT '{}',

    -- SMS发送计数：累计发送的短信通知数量
    -- 用途：计费统计、防止短信轰炸（可设置上限阈值）
    sms_sent_count INTEGER DEFAULT 0,

    -- 事件状态：当前所处的生命周期阶段
    -- 可选值：
    --   ACTIVE：活动状态，正在处理中
    --   RESOLVED：已解决，用户安全获救
    --   CANCELLED：已取消，误报或用户自行解除
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'RESOLVED', 'CANCELLED')),

    -- 解决时间：事件转为RESOLVED状态的时间
    resolved_at TIMESTAMPTZ,

    -- 解决备注：事件解决的详细说明
    -- 示例：'已联系家属，用户已被接回'
    resolution_note TEXT,

    -- 事件创建时间：SOS触发的时刻
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- [6.2] 为sos_events表添加注释
COMMENT ON TABLE sos_events IS
    'SOS紧急求助事件表：记录用户触发的每一次紧急求助事件的完整信息。
     包含事件类型、严重程度、GPS定位、通知状态、事件生命周期等关键数据。
     这是关乎生命安全的核心业务表，对数据可靠性和查询性能有极高要求。';

COMMENT ON COLUMN sos_events.id IS
    '事件唯一标识：自增主键';
COMMENT ON COLUMN sos_events.user_id IS
    '用户ID：外键关联users表，SOS触发者';
COMMENT ON COLUMN sos_events.scenario_type IS
    '场景类型：疾病突发/迷路走失/交通事故/意外跌倒/其他';
COMMENT ON COLUMN sos_events.severity IS
    '严重程度：LOW/MEDIUM/HIGH/CRITICAL，决定响应优先级';
COMMENT ON COLUMN sos_events.latitude IS
    '纬度坐标：GPS定位纬度，范围-90到90';
COMMENT ON COLUMN sos_events.longitude IS
    '经度坐标：GPS定位经度，范围-180到180';
COMMENT ON COLUMN sos_events.location_address IS
    '位置地址：地理编码后的可读地址文本';
COMMENT ON COLUMN sos_events.accuracy IS
    '定位精度：GPS误差范围（米），数值越小越精确';
COMMENT ON COLUMN sos_events.notified_contacts IS
    '已通知联系人ID数组：记录已发送SOS通知的emergency_contacts.id列表，防止重复通知';
COMMENT ON COLUMN sos_events.sms_sent_count IS
    '短信发送计数：累计发送的通知短信数量，用于成本控制和防滥用';
COMMENT ON COLUMN sos_events.status IS
    '事件状态：ACTIVE(处理中)/RESOLVED(已解决)/CANCELLED(已取消)';
COMMENT ON COLUMN sos_events.resolved_at IS
    '解决时间：事件转为RESOLVED状态的时间戳';
COMMENT ON COLUMN sos_events.resolution_note IS
    '解决备注：事件解决过程的文字说明';
COMMENT ON COLUMN sos_events.created_at IS
    '创建时间：SOS事件触发的精确时刻';

-- [6.3] 为sos_events表创建索引（高性能优化）

-- 索引1：user_id + status + created_at 三列复合索引（超级重要！）
-- 这是SOS模块最核心的查询索引，覆盖以下高频查询场景：
--   a) 查询某用户的活跃SOS事件：WHERE user_id=? AND status='ACTIVE'
--   b) 查询某用户的所有SOS历史：WHERE user_id=? ORDER BY created_at DESC
--   c) 统计各状态的SOS数量：GROUP BY status
-- 列顺序原理：等值查询列(user_id, status)在前，范围查询列(created_at)在后
CREATE INDEX idx_sos_user_status_time ON sos_events(user_id, status, created_at DESC);

-- 索引2：status单列索引
-- 作用：全局查询所有活跃SOS事件（运维监控大屏使用）
-- 使用场景：后台管理员查看当前所有未解决的紧急事件
CREATE INDEX idx_sos_status ON sos_events(status);

-- 索引3：severity索引
-- 作用：按严重程度筛选
-- 使用场景：优先处理CRITICAL级别的事件
CREATE INDEX idx_sos_severity ON sos_events(severity);

-- 索引4：scenario_type索引
-- 作用：按场景类型统计分析
-- 使用场景：统计哪类SOS事件最多，指导产品优化
CREATE INDEX idx_sos_scenario_type ON sos_events(scenario_type);

-- 索引5：created_at索引
-- 作用：按时间范围查询
-- 使用场景：统计某段时间内的SOS事件趋势
CREATE INDEX idx_sos_created_at ON sos_events(created_at);


-- ============================================================================
-- 第七部分：emergency_contacts 紧急联系人表
-- 表功能：
--   存储用户设置的紧急联系人信息
--   SOS事件触发时，系统自动向这些联系人发送通知
--   支持主要联系人标记和排序权重
-- 业务规则：
--   - 每个用户最多设置10个紧急联系人（通过触发器强制约束）
--   - 可以标记一个主要联系人（优先通知）
--   - 支持排序权重自定义显示顺序
-- 数据关系：
--   - 与users表：多对一关系（多个联系人属于一个用户）
--   - 与sos_events表：被notified_contacts字段引用（冗余ID数组）
-- ============================================================================

-- [7.1] 创建emergency_contacts表
CREATE TABLE emergency_contacts (
    -- 主键
    id SERIAL PRIMARY KEY,

    -- 用户ID：外键关联到users表
    -- 级联策略：ON DELETE CASCADE - 用户删除时清除所有联系人
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    -- 联系人姓名
    name VARCHAR(100) NOT NULL,

    -- 与用户的关系
    -- 可选值：家人/朋友/医生/护工/邻居/同事/其他
    relationship VARCHAR(50),

    -- 联系电话
    phone VARCHAR(20) NOT NULL,

    -- 是否主要联系人：SOS时优先通知的主要联系人
    -- 约束：每个用户只能有一个主要联系人（通过触发器保证）
    is_primary BOOLEAN DEFAULT FALSE,

    -- 排序权重：数值越小排越前面
    -- 默认值：0
    sort_order INTEGER DEFAULT 0,

    -- 时间戳
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- [7.2] 为emergency_contacts表添加注释
COMMENT ON TABLE emergency_contacts IS
    '紧急联系人表：存储用户预设的紧急联系人信息，用于SOS事件自动通知。
     每个用户最多可设置10个联系人，支持标记主要联系人和自定义排序。
     此表数据会冗余同步到users.emergency_contacts字段以优化SOS响应速度。';

COMMENT ON COLUMN emergency_contacts.id IS
    '联系人唯一标识：自增主键，被sos_events.notified_contacts数组引用';
COMMENT ON COLUMN emergency_contacts.user_id IS
    '用户ID：外键关联users表，联系人所属的用户';
COMMENT ON COLUMN emergency_contacts.name IS
    '联系人姓名：紧急联系人的真实姓名';
COMMENT ON COLUMN emergency_contacts.relationship IS
    '关系：与用户的关系（家人/朋友/医生/护工/邻居/同事/其他）';
COMMENT ON COLUMN emergency_contacts.phone IS
    '联系电话：手机号码，SOS通知的接收号码';
COMMENT ON COLUMN emergency_contacts.is_primary IS
    '主要联系人标记：TRUE表示该用户的主要联系人，SOS时优先通知；
     每个用户只能有一个主要联系人';
COMMENT ON COLUMN emergency_contacts.sort_order IS
    '排序权重：数值越小显示越靠前，用于自定义联系人列表顺序';
COMMENT ON COLUMN emergency_contacts.created_at IS
    '创建时间：联系人添加的时间';
COMMENT ON COLUMN emergency_contacts.updated_at IS
    '更新时间：联系人信息最后修改的时间';

-- [7.3] 为emergency_contacts表创建索引

-- 索引1：user_id索引（核心索引）
-- 作用：快速查询某用户的所有紧急联系人
-- 使用场景：SOS事件触发时获取通知列表、用户管理联系人页面
CREATE INDEX idx_emergency_user ON emergency_contacts(user_id);

-- 索引2：user_id + is_primary 复合索引
-- 作用：快速找到用户的主要联系人
-- 使用场景：SOS触发时首先通知主要联系人
CREATE INDEX idx_emergency_user_primary ON emergency_contacts(user_id, is_primary);

-- 索引3：user_id + sort_order 复合索引
-- 作用：按排序权重获取联系人列表（避免额外排序）
-- 使用场景：展示联系人列表时按sort_order升序排列
CREATE INDEX idx_emergency_user_sort ON emergency_contacts(user_id, sort_order);

-- [7.4] 为emergency_contacts表创建updated_at触发器
CREATE TRIGGER trigger_emergency_updated_at
    BEFORE UPDATE ON emergency_contacts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- [7.5] 创建限制每个用户最多10个联系人的触发器函数和触发器
-- 这是一个业务规则约束，通过触发器在数据库层面强制执行

-- [7.5.1] 创建检查联系人数量限制的函数
CREATE OR REPLACE FUNCTION check_emergency_contact_limit()
RETURNS TRIGGER AS $$
DECLARE
    contact_count INTEGER;
BEGIN
    -- 统计该用户已有的联系人数量（不包括正在插入的这一条）
    SELECT COUNT(*) INTO contact_count
    FROM emergency_contacts
    WHERE user_id = NEW.user_id;

    -- 如果数量已达上限（10个），拒绝插入
    IF contact_count >= 10 THEN
        RAISE EXCEPTION '每个用户最多只能设置10个紧急联系人，当前已有 % 个', contact_count;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION check_emergency_contact_limit() IS
    '业务规则触发器函数：限制每个用户最多创建10个紧急联系人，
     超出限制时抛出异常阻止插入操作';

-- [7.5.2] 创建触发器：在INSERT之前检查联系人数量
CREATE TRIGGER trigger_check_contact_limit
    BEFORE INSERT ON emergency_contacts
    FOR EACH ROW
    EXECUTE FUNCTION check_emergency_contact_limit();

-- [7.6] 创建确保每个用户只有一个主要联系人的触发器函数

CREATE OR REPLACE FUNCTION enforce_single_primary_contact()
RETURNS TRIGGER AS $$
BEGIN
    -- 如果正在插入或更新的记录标记为主要联系人
    IF NEW.is_primary = TRUE THEN
        -- 将该用户的其他联系人全部设为非主要
        UPDATE emergency_contacts
        SET is_primary = FALSE,
            updated_at = NOW()
        WHERE user_id = NEW.user_id
          AND id != NEW.id
          AND is_primary = TRUE;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION enforce_single_primary_contact() IS
    '业务规则触发器函数：确保每个用户只有一个主要联系人(is_primary=TRUE)，
     当设置新的主要联系人时，自动将该用户原有的主要联系人标记取消';

-- [7.7] 创建触发器：在INSERT和UPDATE时执行单一主联系人检查
CREATE TRIGGER trigger_enforce_single_primary
    BEFORE INSERT OR UPDATE ON emergency_contacts
    FOR EACH ROW
    EXECUTE FUNCTION enforce_single_primary_contact();


-- ============================================================================
-- 第八部分：测试数据插入
-- 功能说明：
--   插入一套完整的测试数据集，用于开发调试和功能验证
--   包含：3个测试用户、10种常见药品、5条识别记录、2条SOS事件、8个联系人
-- 数据设计思路：
--   - 用户：覆盖不同残疾类型和等级，具有不同的过敏史和慢性病
--   - 药品：涵盖常见药品类别，包含各种风险等级和过敏原标签
--   - 识别记录：模拟不同置信度和风险等级的场景
--   - SOS事件：演示不同场景和严重程度的案例
-- ============================================================================

-- [8.1] 插入测试用户数据（3个不同类型的视障用户）

-- 用户1：张明（全盲一级，有青霉素过敏史和高血压）
-- 特点：高风险用户，需要严格的药品安全检查
INSERT INTO users (phone, password_hash, nickname, avatar_url, disability_type, disability_level,
                   allergy_history, chronic_diseases, current_medications, emergency_contacts, is_active)
VALUES (
    '13800001001',
    '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4f0FuXOdQMFKBKMG',  -- bcrypt hash of "password123"
    '光明使者张明',
    'http://minio:9000/weiguangplus/avatars/user_001.jpg',
    '全盲',
    '一级',
    ARRAY['青霉素', '磺胺类药物', '花粉'],
    ARRAY['高血压', '冠心病'],
    '[
        {"drug_name": "硝苯地平控释片", "dosage": "30mg", "frequency": "每日1次早晨服用", "started_at": "2025-06-01"},
        {"drug_name": "阿司匹林肠溶片", "dosage": "100mg", "frequency": "每晚1次", "started_at": "2025-08-15"}
    ]'::jsonb,
    '[]'::jsonb,
    TRUE
);

-- 用户2：李芳（低视力二级，糖尿病患者，无药物过敏）
-- 特点：中度风险，需要关注降糖药的相互作用
INSERT INTO users (phone, password_hash, nickname, avatar_url, disability_type, disability_level,
                   allergy_history, chronic_diseases, current_medications, emergency_contacts, is_active)
VALUES (
    '13800001002',
    '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4f0FuXOdQMFKBKMG',
    '阳光女孩李芳',
    'http://minio:9000/weiguangplus/avatars/user_002.jpg',
    '低视力',
    '二级',
    ARRAY[],
    ARRAY['2型糖尿病', '高血脂'],
    '[
        {"drug_name": "二甲双胍缓释片", "dosage": "500mg", "frequency": "每日2次餐后服用", "started_at": "2026-01-10"},
        {"drug_name": "格列美脲片", "dosage": "2mg", "frequency": "每日1次早餐前", "started_at": "2026-02-20"}
    ]'::jsonb,
    '[]'::jsonb,
    TRUE
);

-- 用户3：王强（色盲三级，健康人群，海鲜过敏）
-- 特点：低风险用户，主要用于测试基本功能
INSERT INTO users (phone, password_hash, nickname, avatar_url, disability_type, disability_level,
                   allergy_history, chronic_diseases, current_medications, emergency_contacts, is_active)
VALUES (
    '13800001003',
    '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4f0FuXOdQMFKBKMG',
    '探索者王强',
    'http://minio:9000/weiguangplus/avatars/user_003.jpg',
    '色盲',
    '三级',
    ARRAY['海鲜', '芒果'],
    ARRAY[],
    '[]'::jsonb,
    '[]'::jsonb,
    TRUE
);

-- [8.2] 插入测试药品数据（10种常见药品，覆盖多个类别和风险等级）

-- 药品1：对乙酰氨基酚片（泰诺）- 常用解热镇痛药，OTC，低风险
INSERT INTO drugs (drug_name, brand_name, alias_names, category, dosage_form, specification,
                  manufacturer, ingredients, indications, contraindications, precautions,
                  adverse_reactions, pregnancy_risk, pediatric_use, elderly_use,
                  allergen_tags, interaction_rules, is_prescription, data_source)
VALUES (
    '对乙酰氨基酚片',
    '泰诺',
    ARRAY['扑热息痛', '醋氨酚', 'APAP', '必理通'],
    '止痛',
    '片剂',
    '0.5g×12片',
    '强生(上海)医疗器材有限公司',
    '[{"name": "对乙酰氨基酚", "content": "500mg", "role": "有效成分"}]'::jsonb,
    ARRAY['普通感冒引起的发热', '流行性感冒引起的发热', '头痛', '关节痛', '偏头痛', '肌肉痛', '神经痛', '痛经'],
    ARRAY['对本品过敏者禁用', '严重肝肾功能不全者禁用', '孕妇及哺乳期妇女慎用'],
    ARRAY['不得与其他含对乙酰氨基酚的药品同时服用', '服药期间不得饮酒或饮用含有酒精的饮料', '肝病患者慎用'],
    ARRAY['偶见皮疹', '荨麻疹', '药热', '粒细胞减少', '长期大量使用可致肝肾功能损害'],
    'B',
    '儿童用量需按体重计算，详见说明书或在医师指导下使用',
    '老年患者由于肝、肾功能发生减退，本品半衰期有所延长，易发生不良反应，应慎用或适当减量',
    ARRAY[],
    '[{"interact_with": "酒精", "effect": "增加肝毒性风险", "severity": "HIGH", "mechanism": "竞争性代谢抑制"}, {"interact_with": "华法林", "effect": "增强抗凝作用导致出血风险", "severity": "MEDIUM", "mechanism": "代谢酶竞争"}]'::jsonb,
    FALSE,
    'manual'
);

-- 药品2：阿莫西林胶囊 - 抗生素，处方药，含青霉素过敏原（高风险）
INSERT INTO drugs (drug_name, brand_name, alias_names, category, dosage_form, specification,
                  manufacturer, ingredients, indications, contraindications, precautions,
                  adverse_reactions, pregnancy_risk, pediatric_use, elderly_use,
                  allergen_tags, interaction_rules, is_prescription, data_source)
VALUES (
    '阿莫西林胶囊',
    '阿莫仙',
    ARRAY['阿莫西林', 'Amoxicillin', '羟氨苄青霉素'],
    '抗生素',
    '胶囊',
    '0.25g×24粒',
    '珠海联邦制药股份有限公司中山分公司',
    '[{"name": "阿莫西林", "content": "250mg", "role": "有效成分"}]'::jsonb,
    ARRAY['敏感菌所致的中耳炎', '鼻窦炎', '咽炎', '扁桃体炎等上呼吸道感染', '急性支气管炎', '肺炎等下呼吸道感染'],
    ARRAY['青霉素过敏及青霉素皮肤试验阳性患者禁用', '传染性单核细胞增多症患者禁用'],
    ARRAY['用前必须做青霉素皮肤试验', '阳性反应者禁用', '疗程较长患者应检查肝、肾功能和血常规'],
    ARRAY['恶心', '呕吐', '腹泻', '皮疹', '药物热', '哮喘', '过敏性休克（罕见但严重）'],
    'B',
    '儿童按体重一次6.7～13.3mg/kg，每8小时1次',
    '老年人由于肾功能可能减退，用药量应适当减少',
    ARRAY['青霉素类', 'β-内酰胺类'],
    '[{"interact_with": "丙磺舒", "effect": "延缓阿莫西林排泄，提高血药浓度", "severity": "LOW", "mechanism": "肾小管分泌竞争"}, {"interact_with": "避孕药", "effect": "可能降低避孕药效果", "severity": "MEDIUM", "mechanism": "肠道菌群改变"}]'::jsonb,
    TRUE,
    'manual'
);

-- 药品3：布洛芬缓释胶囊（芬必得）- 非甾体抗炎药，OTC，中等风险
INSERT INTO drugs (drug_name, brand_name, alias_names, category, dosage_form, specification,
                  manufacturer, ingredients, indications, contraindications, precautions,
                  adverse_reactions, pregnancy_risk, pediatric_use, elderly_use,
                  allergen_tags, interaction_rules, is_prescription, data_source)
VALUES (
    '布洛芬缓释胶囊',
    '芬必得',
    ARRAY['布洛芬', 'Ibuprofen', '美林'],
    '止痛',
    '胶囊（缓释）',
    '0.3g×20粒',
    '中美天津史克制药有限公司',
    '[{"name": "布洛芬", "content": "300mg", "role": "有效成分"}]'::jsonb,
    ARRAY['缓解轻至中度疼痛', '头痛', '关节痛', '偏头痛', '牙痛', '肌肉痛', '神经痛', '痛经'],
    ARRAY['对本品及其他非甾体抗炎药过敏者禁用', '孕妇及哺乳期妇女禁用', '活动性消化道溃疡患者禁用'],
    array['不得与其他非甾体抗炎药同时服用', '有消化道溃疡史者慎用', '高血压、心脏病患者慎用'],
    ARRAY['恶心', '呕吐', '胃烧灼感', '轻度消化不良', '头痛', '头晕', '耳鸣', '皮疹'],
    'B（孕晚期禁用，D级）',
    '儿童必须在成人监护下使用', '12岁以下儿童不宜使用缓释制剂',
    '老年患者使用非甾体抗炎药出现不良反应的频率增加，尤其是胃肠道出血和穿孔',
    ARRAY['阿司匹林过敏', '非甾体抗炎药过敏'],
    '[{"interact_with": "阿司匹林", "effect": "干扰阿司匹林抗血小板作用", "severity": "MEDIUM", "mechanism": "竞争COX-1结合位点"}, {"interact_with": "利尿剂", "effect": "降低利尿剂的利尿及抗高血压效果", "severity": "MEDIUM", "mechanism": "前列腺素合成抑制"}]'::jsonb,
    FALSE,
    'manual'
);

-- 药品4：硝苯地平控释片（拜新同）- 降压药，处方药，心血管类
INSERT INTO drugs (drug_name, brand_name, alias_names, category, dosage_form, specification,
                  manufacturer, ingredients, indications, contraindications, precautions,
                  adverse_reactions, pregnancy_risk, pediatric_use, elderly_use,
                  allergen_tags, interaction_rules, is_prescription, data_source)
VALUES (
    '硝苯地平控释片',
    '拜新同',
    ARRAY['硝苯地平', 'Nifedipine', '心痛定'],
    '降压',
    '片剂（控释）',
    '30mg×7片',
    '拜耳医药保健有限公司',
    '[{"name": "硝苯地平", "content": "30mg", "role": "有效成分"}]'::jsonb,
    ARRAY['高血压', '冠心病', '慢性稳定型心绞痛', '血管痉挛性心绞痛'],
    ARRAY['对本品任何成分过敏者禁用', '心源性休克患者禁用', '急性心肌梗死（包括急性期后）禁用'],
    ARRAY['停药需逐渐减量', '避免突然停药引起反跳性高血压', '肝功能损害者慎用', '严重主动脉瓣狭窄者慎用'],
    ARRAY['头痛', '面部潮红', '心悸', '踝部水肿', '乏力', '恶心', '牙龈增生'],
    'C',
    '儿童的安全性和有效性尚未确立',
    '老年患者无需调整起始剂量，但应密切监测血压和不良反应',
    ARRAY['钙通道阻滞剂过敏'],
    '[{"interact_with": "β受体阻滞剂", "effect": "可能导致严重低血压和心力衰竭", "severity": "HIGH", "mechanism": "协同负性肌力作用"}, {"interact_with": "西咪替丁", "effect": "升高硝苯地平血药浓度", "severity": "MEDIUM", "mechanism": "抑制CYP3A4酶"}]'::jsonb,
    TRUE,
    'manual'
);

-- 药品5：二甲双胍缓释片 - 降糖药，处方药，糖尿病一线用药
INSERT INTO drugs (drug_name, brand_name, alias_names, category, dosage_form, specification,
                  manufacturer, ingredients, indications, contraindications, precautions,
                  adverse_reactions, pregnancy_risk, pediatric_use, elderly_use,
                  allergen_tags, interaction_rules, is_prescription, data_source)
VALUES (
    '二甲双胍缓释片',
    '格华止XR',
    ARRAY['二甲双胍', 'Metformin', '甲福明'],
    '降糖',
    '片剂（缓释）',
    '0.5g×30片',
    '中美施贵宝制药有限公司',
    '[{"name": "盐酸二甲双胍", "content": "500mg", "role": "有效成分"}]'::jsonb,
    ARRAY['2型糖尿病', '单纯饮食控制和运动治疗效果不佳者', '与胰岛素合用于1型糖尿病'],
    ARRAY['严重肾功能不全（肌酐清除率<45ml/min）禁用', '急性或慢性代谢性酸中毒禁用', '对本品过敏者禁用'],
    ARRAY['定期监测肾功能', '碘造影检查前后48小时暂停服用', '酗酒者禁用', '维生素B12缺乏者应监测血清维生素B12水平'],
    ARRAY['常见腹泻', '恶心', '呕吐', '胃胀', '消化不良', '腹部不适', '乏力', '头痛'],
    'B',
    '10-16岁2型糖尿病患儿可用普通片，缓释片不推荐儿童使用',
    '80岁以上老人应谨慎使用，定期监测肾功能',
    ARRAY[],
    '[{"interact_with": "碘造影剂", "effect": "增加乳酸酸中毒风险", "severity": "CRITICAL", "mechanism": "肾功能急性恶化"}, {"interact_with": "酒精", "effect": "增强乳酸酸中毒风险", "severity": "HIGH", "mechanism": "乳酸代谢紊乱"}]'::jsonb,
    TRUE,
    'manual'
);

-- 药品6：氯雷他定片（开瑞坦）- 抗过敏药，OTC，常用于过敏症状
INSERT INTO drugs (drug_name, brand_name, alias_names, category, dosage_form, specification,
                  manufacturer, ingredients, indications, contraindications, precautions,
                  adverse_reactions, pregnancy_risk, pediatric_use, elderly_use,
                  allergen_tags, interaction_rules, is_prescription, data_source)
VALUES (
    '氯雷他定片',
    '开瑞坦',
    ARRAY['氯雷他定', 'Loratadine', '百为哈'],
    '抗过敏',
    '片剂',
    '10mg×6片',
    '西安杨森制药有限公司',
    '[{"name": "氯雷他定", "content": "10mg", "role": "有效成分"}]'::jsonb,
    ARRAY['过敏性鼻炎', '荨麻疹', '过敏性结膜炎', '皮肤瘙痒'],
    ARRAY['对本品过敏者禁用', '严重肝功能损害者禁用'],
    ARRAY['严重肾功能不全者慎用', '孕妇及哺乳期妇女使用前咨询医师', '高空作业者慎用（可能嗜睡）'],
    ARRAY['乏力', '头痛', '嗜睡', '口干', '胃肠道不适（恶心、胃炎）', '皮疹'],
    'B',
    '2岁以上儿童可用，体重≤30kg者每日5mg，>30kg者每日10mg',
    '老年患者无需调整剂量',
    ARRAY[],
    '[{"interact_with": "酮康唑", "effect": "升高氯雷他定血药浓度", "severity": "LOW", "mechanism": "CYP3A4抑制"}, {"interact_with": "大环内酯类抗生素", "effect": "可能增加血药浓度", "severity": "LOW", "mechanism": "酶抑制作用"}]'::jsonb,
    FALSE,
    'manual'
);

-- 药品7：奥美拉唑肠溶胶囊 - 抑酸药，处方药，治疗胃酸过多
INSERT INTO drugs (drug_name, brand_name, alias_names, category, dosage_form, specification,
                  manufacturer, ingredients, indications, contraindications, precautions,
                  adverse_reactions, pregnancy_risk, pediatric_use, elderly_use,
                  allergen_tags, interaction_rules, is_prescription, data_source)
VALUES (
    '奥美拉唑肠溶胶囊',
    '洛赛克',
    ARRAY['奥美拉唑', 'Omeprazole', '奥克'],
    '消化',
    '胶囊（肠溶）',
    '20mg×14粒',
    '阿斯利康制药有限公司',
    '[{"name": "奥美拉唑", "content": "20mg", "role": "有效成分"}]'::jsonb,
    ARRAY['十二指肠溃疡', '胃溃疡', '反流性食管炎', '卓-艾综合征（胃泌素瘤）'],
    ARRAY['对本品过敏者禁用', '严重肾功能不全者禁用'],
    ARRAY['长期使用应定期监测镁水平', '可能掩盖胃癌症状', '骨质疏松患者长期使用应补充钙和维生素D'],
    ARRAY['头痛', '腹泻', '便秘', '腹胀', '恶心', '皮疹', '瘙痒', '头晕'],
    'C（避免使用，除非必要）',
    '儿童用药经验有限，应在医师严密监督下使用',
    '老年患者无需调整剂量',
    ARRAY['苯并咪唑类化合物过敏'],
    '[{"interact_with": "氯吡格雷", "effect": "降低氯吡格雷抗血小板活性", "severity": "HIGH", "mechanism": "CYP2C19抑制"}, {"interact_with": "地高辛", "effect": "可能升高地高辛血药浓度", "severity": "MEDIUM", "mechanism": "胃酸减少增加吸收"}]'::jsonb,
    TRUE,
    'manual'
);

-- 药品8：复方甘草片 - 镇咳祛痰药，含阿片类成分，特殊管控
INSERT INTO drugs (drug_name, brand_name, alias_names, category, dosage_form, specification,
                  manufacturer, ingredients, indications, contraindications, precautions,
                  adverse_reactions, pregnancy_risk, pediatric_use, elderly_use,
                  allergen_tags, interaction_rules, is_prescription, data_source)
VALUES (
    '复方甘草片',
    NULL,
    ARRAY['甘草片', '复方甘草合剂'],
    '止咳',
    '片剂',
    '100片/瓶',
    '甘肃陇神戎发药业股份有限公司',
    '[{"name": "甘草浸膏", "content": "112.5mg", "role": "有效成分"},
      {"name": "阿片粉", "content": "4mg", "role": "有效成分（中枢镇咳）"},
      {"name": "樟脑", "content": "2mg", "role": "辅料"},
      {"name": "八角茴香油", "content": "2mg", "role": "辅料"},
      {"name": "苯甲酸钠", "content": "2mg", "role": "防腐剂"}]'::jsonb,
    ARRAY['镇咳祛痰', '上呼吸道感染', '支气管炎引起的咳嗽'],
    ARRAY['对本品过敏者禁用', '孕妇及哺乳期妇女禁用', '婴幼儿禁用'],
    ARRAY['含阿片成分，有成瘾性，不宜长期服用', '驾驶员和高空作业者慎用', '胃炎及胃溃疡患者慎用'],
    ARRAY['轻微恶心', '呕吐', '便秘', '成瘾性（长期使用）', '镇静'],
    'D',
    '禁用于儿童和青少年',
    '老年患者慎用，可能引起便秘和尿潴留',
    ARRAY['阿片类过敏', '酒精'],
    '[{"interact_with": "酒精", "effect": "增强中枢抑制作用", "severity": "HIGH", "mechanism": "协同中枢抑制"}, {"interact_with": "MAOI抑制剂", "effect": "可能引发高血压危象", "severity": "CRITICAL", "mechanism": "儿茶酚胺蓄积"}]'::jsonb,
    TRUE,
    'manual'
);

-- 药品9：维生素C泡腾片 - 补充维生素，OTC，保健品级别
INSERT INTO drugs (drug_name, brand_name, alias_names, category, dosage_form, specification,
                  manufacturer, ingredients, indications, contraindications, precautions,
                  adverse_reactions, pregnancy_risk, pediatric_use, elderly_use,
                  allergen_tags, interaction_rules, is_prescription, data_source)
VALUES (
    '维生素C泡腾片',
    '力度伸',
    ARRAY['维C', '维生素C', 'Vitamin C', '抗坏血酸'],
    '维生素',
    '泡腾片',
    '1g×10片',
    '拜耳保健消费品公司',
    '[{"name": "抗坏血酸（维生素C）", "content": "1000mg", "role": "有效成分"}]'::jsonb,
    ARRAY['预防和治疗维生素C缺乏症', '增强机体免疫力', '辅助治疗感冒', '促进铁吸收'],
    ARRAY['对本品过敏者禁用'],
    ARRAY['不宜长期过量服用', '尿结石患者慎用（草酸盐结石）', '糖尿病患者慎用（含甜味剂）', '勿直接吞服泡腾片'],
    ARRAY['长期过量服用可引起腹泻', '皮肤红亮', '头痛', '尿频', '肾结石（极罕见）'],
    'A（孕期适量补充有益）',
    '儿童按年龄调整剂量，遵医嘱',
    '老年患者无需特殊调整',
    ARRAY[],
    '[{"interact_with": "铁剂", "effect": "促进铁的吸收", "severity": "POSITIVE", "mechanism": "还原Fe3+为Fe2+"}, {"interact_with": "华法林", "effect": "可能降低华法林抗凝效果", "severity": "LOW", "mechanism": "未知机制"}]'::jsonb,
    FALSE,
    'manual'
);

-- 药品10：地塞米松片 - 强效糖皮质激素，处方药，严格管控
INSERT INTO drugs (drug_name, brand_name, alias_names, category, dosage_form, specification,
                  manufacturer, ingredients, indications, contraindications, precautions,
                  adverse_reactions, pregnancy_risk, pediatric_use, elderly_use,
                  allergen_tags, interaction_rules, is_prescription, data_source)
VALUES (
    '地塞米松片',
    NULL,
    ARRAY['地塞米松', 'Dexamethasone', 'DXM', '氟美松'],
    '激素',
    '片剂',
    '0.75mg×100片',
    '天津力生制药股份有限公司',
    '[{"name": "地塞米松", "content": "0.75mg", "role": "有效成分"}]'::jsonb,
    ARRAY['过敏性与自身免疫性炎症性疾病', '支气管哮喘', '皮炎等皮肤病', '严重急性细菌感染辅助治疗'],
    ARRAY['对本品及肾上腺皮质激素类药物有过敏史者禁用', '全身性真菌感染者禁用', '活动性肺结核禁用'],
    array['长期使用需逐渐减量停药（不可骤停）', '结核病、糖尿病患者慎用', '高血压、精神病史者慎用', '可能掩盖感染症状'],
    ARRAY['医源性库欣综合征面容', '体重增加', '下肢水肿', '紫纹', '易出血倾向', '创口愈合不良', '骨质疏松', '血糖升高'],
    'C',
    '儿童如确实需要使用，必须十分慎重，短程使用', '可抑制生长发育',
    '老年患者用糖皮质激素易引起高血压和糖尿病', '需更频繁监测',
    ARRAY[],
    '[{"interact_with": "利尿剂", "effect": "加重低钾血症", "severity": "HIGH", "mechanism": "钾排泄增加"}, {"interact_with": "非甾体抗炎药", "effect": "增加消化性溃疡风险", "severity": "HIGH", "mechanism": "黏膜保护减弱"}, {"interact_with": "疫苗", "effect": "降低疫苗免疫效果", "severity": "MEDIUM", "mechanism": "免疫抑制"}]'::jsonb,
    TRUE,
    'manual'
);

-- [8.3] 插入测试用的药品识别记录（5条不同场景）

-- 识别记录1：用户1识别泰诺（对乙酰氨基酚）- 高置信度，低风险
INSERT INTO drug_recognition_records (user_id, image_url, ocr_raw_text, matched_drug_id,
                                     confidence_score, risk_level, risk_details, allergen_matches,
                                     interaction_warnings, is_bookmarked, user_notes, recognition_time)
VALUES (
    1,
    'http://minio:9000/weiguangplus/ocr_images/record_20260528_001.jpg',
    '泰诺 对乙酰氨基酚片 0.5g×12片 强生(上海)医疗器材有限公司
     【适应症】普通感冒引起的发热、流行性感冒引起的发热、头痛...
     【用法用量】口服，成人一次1片，若症状不缓解间隔4-6小时重复...',
    1,
    0.95,
    'LOW',
    '{"allergy_risk": "LOW", "interaction_risk": "LOW", "contraindication_risk": "LOW", "overall_assessment": "该药品安全性较高，用户可放心使用", "checked_items": ["过敏史匹配", "禁忌症检查", "相互作用分析"]}'::jsonb,
    '[]'::jsonb,
    ARRAY[],
    TRUE,
    '家里常备药，确认一下成分安全',
    1.23
);

-- 识别记录2：用户1识别阿莫西林 - 中等置信度，CRITICAL风险（用户有青霉素过敏！）
INSERT INTO drug_recognition_records (user_id, image_url, ocr_raw_text, matched_drug_id,
                                     confidence_score, risk_level, risk_details, allergen_matches,
                                     interaction_warnings, is_bookmarked, user_notes, recognition_time)
VALUES (
    1,
    'http://minio:9000/weiguangplus/ocr_images/record_20260528_002.jpg',
    '阿莫西林胶囊 0.25g×24粒 阿莫仙
     【适应症】敏感菌所致的上呼吸道感染、下呼吸道感染...
     【禁忌】青霉素过敏及青霉素皮肤试验阳性患者禁用...',
    2,
    0.88,
    'CRITICAL',
    '{"allergy_risk": "CRITICAL", "interaction_risk": "MEDIUM", "contraindication_risk": "CRITICAL", "overall_assessment": "⚠️ 严重危险！用户对青霉素类过敏，本品含青霉素成分，严禁使用！", "details": ["用户过敏史包含：青霉素", "本品过敏原标签包含：青霉素类、β-内酰胺类", "历史上曾发生：需进一步了解"], "recommendation": "立即停止使用，尽快就医"]}'::jsonb,
    '[{"allergen": "青霉素", "matched_in": "allergen_tags", "severity": "CRITICAL", "user_allergy_record": "用户过敏史中有青霉素过敏记录"}]'::jsonb,
    ARRAY['⚠️ 危险：您对青霉素过敏，本品为青霉素类抗生素，严禁服用！', '如已误服请立即就医或拨打120'],
    TRUE,
    '⚠️ 系统检测到严重过敏风险！',
    1.56
);

-- 识别记录3：用户2识别二甲双胍 - 高置信度，低风险（用户正在服用此药）
INSERT INTO drug_recognition_records (user_id, image_url, ocr_raw_text, matched_drug_id,
                                     confidence_score, risk_level, risk_details, allergen_matches,
                                     interaction_warnings, is_bookmarked, user_notes, recognition_time)
VALUES (
    2,
    'http://minio:9000/weiguangplus/ocr_images/record_20260528_003.jpg',
    '格华止XR 二甲双胍缓释片 0.5g×30片 中美施贵宝
     【适应症】2型糖尿病...
     【用法】起始剂量为500mg，每日1次...',
    5,
    0.92,
    'LOW',
    '{"allergy_risk": "LOW", "interaction_risk": "LOW", "contraindication_risk": "LOW", "overall_assessment": "该药品为用户当前用药之一，符合治疗方案", "note": "用户current_medications中已包含此药"}'::jsonb,
    '[]'::jsonb,
    ARRAY[],
    FALSE,
    '',
    0.98
);

-- 识别记录4：用户3识别布洛芬 - 中等置信度，MEDIUM风险（有非甾体抗炎药过敏可能）
INSERT INTO drug_recognition_records (user_id, image_url, ocr_raw_text, matched_drug_id,
                                     confidence_score, risk_level, risk_details, allergen_matches,
                                     interaction_warnings, is_bookmarked, user_notes, recognition_time)
VALUES (
    3,
    'http://minio:9000/weiguangplus/ocr_images/record_20260528_004.jpg',
    '芬必得 布洛芬缓释胶囊 0.3g×20粒 中美天津史克
     【适应症】缓解轻至中度疼痛：头痛、关节痛、偏头痛...',
    3,
    0.78,
    'MEDIUM',
    '{"allergy_risk": "LOW", "interaction_risk": "LOW", "contraindication_risk": "MEDIUM", "overall_assessment": "中等风险，用户无明确药物过敏但需注意非甾体抗炎药的常见不良反应", "suggestions": ["建议饭后服用减少胃肠刺激", "如有胃部不适立即停药"]}'::jsonb,
    '[]'::jsonb,
    ARRAY['建议饭后服用以减轻胃肠道刺激', '连续使用不超过5天，如症状持续请就医'],
    FALSE,
    '',
    2.11
);

-- 识别记录5：用户2识别奥美拉唑 - 低置信度，MEDIUM风险（可能与氯吡格雷冲突）
INSERT INTO drug_recognition_records (user_id, image_url, ocr_raw_text, matched_drug_id,
                                     confidence_score, risk_level, risk_details, allergen_matches,
                                     interaction_warnings, is_bookmarked, user_notes, recognition_time)
VALUES (
    2,
    'http://minio:9000/weiguangplus/ocr_images/record_20260528_005.jpg',
    '洛赛克 奥美拉唑肠溶胶囊 20mg×14片 阿斯利康
     （OCR图像部分模糊，文字识别不完整）...',
    7,
    0.65,
    'MEDIUM',
    '{"allergy_risk": "LOW", "interaction_risk": "MEDIUM", "contraindication_risk": "LOW", "overall_assessment": "中等风险，图像质量一般，建议人工确认", "ocr_quality": "图像边缘模糊，部分文字无法识别"}'::jsonb,
    '[]'::jsonb,
    ARRAY['如正在服用氯吡格雷等抗血小板药物，请告知医生', '本品的OCR识别置信度较低，建议核对药品实物'],
    FALSE,
    '图片拍得不太清楚，下次注意光线',
    3.45
);

-- [8.4] 插入测试SOS事件数据（2条不同场景）

-- SOS事件1：用户1（张明）迷路走失事件 - HIGH严重度，已解决
INSERT INTO sos_events (user_id, scenario_type, severity, latitude, longitude,
                        location_address, accuracy, notified_contacts, sms_sent_count,
                        status, resolved_at, resolution_note)
VALUES (
    1,
    '迷路走失',
    'HIGH',
    39.904214,
    116.407413,
    '北京市东城区王府井大街138号附近',
    12.5,
    ARRAY[1, 2],
    3,
    'RESOLVED',
    NOW() - INTERVAL '2 hours',
    '已成功联系到家属张伟（父亲），用户已被安全接回家中。本次因独自外出购物时在商圈迷失方向。'
);

-- SOS事件2：用户2（李芳）疾病突发 - CRITICAL严重度，仍处于活动状态
INSERT INTO sos_events (user_id, scenario_type, severity, latitude, longitude,
                        location_address, accuracy, notified_contacts, sms_sent_count,
                        status, resolved_at, resolution_note)
VALUES (
    2,
    '疾病突发',
    'CRITICAL',
    31.230416,
    121.473701,
    '上海市浦东新区陆家嘴环路1000号恒生银行大厦',
    8.3,
    ARRAY[3],
    5,
    'ACTIVE',
    NULL,
    NULL
);

-- [8.5] 插入测试紧急联系人数据（为3个用户各分配2-3个联系人）

-- 用户1（张明）的紧急联系人
INSERT INTO emergency_contacts (user_id, name, relationship, phone, is_primary, sort_order) VALUES
    (1, '张伟', '家人', '13900001001', TRUE, 1),
    (1, '张丽', '家人', '13900001002', FALSE, 2),
    (1, '王医生', '医生', '13800009999', FALSE, 3);

-- 用户2（李芳）的紧急联系人
INSERT INTO emergency_contacts (user_id, name, relationship, phone, is_primary, sort_order) VALUES
    (2, '李国强', '家人', '13700001001', TRUE, 1),
    (2, '陈梅', '朋友', '13700001002', FALSE, 2),
    (2, '刘主任', '医生', '13800008888', FALSE, 3);

-- 用户3（王强）的紧急联系人
INSERT INTO emergency_contacts (user_id, name, relationship, phone, is_primary, sort_order) VALUES
    (3, '王建军', '家人', '13600001001', TRUE, 1),
    (3, '赵敏', '朋友', '13600001002', FALSE, 2);

-- [8.6] 更新用户的emergency_contacts冗余字段（保持数据一致性）
-- 将联系人数据同步到users表的JSONB字段中，优化SOS响应速度

UPDATE users SET emergency_contacts = (
    SELECT jsonb_agg(jsonb_build_object(
        'name', ec.name,
        'relationship', ec.relationship,
        'phone', ec.phone,
        'is_primary', ec.is_primary,
        'contact_id', ec.id
    ))
    FROM emergency_contacts ec
    WHERE ec.user_id = users.id
) WHERE id IN (1, 2, 3);


-- ============================================================================
-- 第九部分：性能优化建议与维护策略
-- 说明：以下内容以SQL注释形式呈现，供DBA和开发团队参考实施
-- ============================================================================

/*
================================================================================
第九部分：性能优化建议与数据库维护策略
编写时间：2026-05-28
适用版本：PostgreSQL 15+
目标读者：DBA、后端开发工程师、DevOps工程师
================================================================================

【一、复合索引优化建议】

1. drug_recognition_records 表建议新增复合索引：
   -------------------------------------------------------
   场景：按用户+风险等级+时间范围查询高危记录
   SQL示例：
     SELECT * FROM drug_recognition_records
     WHERE user_id = ?
       AND risk_level IN ('HIGH', 'CRITICAL')
       AND created_at >= NOW() - INTERVAL '30 days'
     ORDER BY created_at DESC;
   
   建议索引：
     CREATE INDEX idx_recognition_user_risk_time
       ON drug_recognition_records(user_id, risk_level, created_at DESC);
   
   原因：现有索引分别覆盖user_id和risk_level，但联合查询时效率不如复合索引

2. drugs 表建议新增复合索引：
   -------------------------------------------------------
   场景：按分类+是否处方药筛选
   SQL示例：
     SELECT * FROM drugs WHERE category = '抗生素' AND is_prescription = TRUE;
   
   建议索引：
     CREATE INDEX idx_drugs_category_prescription
       ON drugs(category, is_prescription);

3. sos_events 表建议新增地理位置索引（GiST索引）：
   -------------------------------------------------------
   场景：查询某地理范围内的SOS事件（周边告警功能）
   前提：需要安装postgis扩展或使用cube/earthdistance扩展
   
   如果使用PostGIS：
     CREATE EXTENSION IF NOT EXISTS postgis;
     ALTER TABLE sos_events ADD COLUMN location GEOGRAPHY(POINT, 4326);
     CREATE INDEX idx_sos_location ON sos_events USING GIST (location);
   
   或者使用简单距离计算（无需PostGIS）：
     -- 基于经纬度的粗略范围查询可使用表达式索引


【二、分区表策略建议】

适用场景判断：
  - drug_recognition_records：数据量大（每用户平均每月10-50条）
  - sos_events：数据量中等但需要长期保存
  - 建议数据量超过1000万行时考虑分区

推荐的分区方案：

1. drug_recognition_records 按月分区（RANGE分区）：
   -------------------------------------------------------
   优点：
     a) 历史数据归档方便：可直接DROP旧分区
     b) 查询性能提升：分区裁剪（Partition Pruning）自动跳过无关分区
     c) 维护灵活：可为不同分区设置不同存储参数
   
   实施时机：单表数据量超过500万行或单月新增超过50万行
   
   示例DDL（PostgreSQL 10+原生分区）：
   
   -- 创建主表（分区表）
   CREATE TABLE drug_recognition_records_partitioned (
       -- 与原表相同的列定义
       id SERIAL,
       user_id INTEGER NOT NULL,
       ...（其他字段）
       created_at TIMESTAMPTZ DEFAULT NOW()
   ) PARTITION BY RANGE (created_at);
   
   -- 创建2026年1月的分区
   CREATE TABLE drug_recognition_records_202601
       PARTITION OF drug_recognition_records_partitioned
       FOR VALUES FROM ('2026-01-01') TO ('2026-02-01');
   
   -- 创建2026年2月的分区
   CREATE TABLE drug_recognition_records_202602
       PARTITION OF drug_recognition_records_partitioned
       FOR VALUES FROM ('2026-02-01') TO ('2026-03-01');
   
   -- 自动创建后续分区的脚本可通过pg_cron定时任务实现

2. sos_events 按季度分区（数据保留期更长）：
   -------------------------------------------------------
   SOS事件涉及法律和安全责任，建议保留至少3年
   按季度分区可平衡分区数量和单个分区大小


【三、VACUUM和ANALYZE维护策略】

1. autovacuum配置调优（postgresql.conf或ALTER TABLE）：
   -------------------------------------------------------
   对于高频更新的表（users, emergency_contacts）：
   
   -- 降低autovacuum触发阈值（默认20%，改为10%更快响应）
   ALTER TABLE users SET (autovacuum_vacuum_scale_factor = 0.1);
   ALTER TABLE emergency_contacts SET (autovacuum_vacuum_scale_factor = 0.1);
   
   -- 对于大批量插入后需要及时ANALYZE的表（drug_recognition_records）：
   ALTER TABLE drug_recognition_records SET (autovacuum_analyze_scale_factor = 0.05);
   
   -- 对于几乎只追加的表（sos_events）可放宽vacuum频率：
   ALTER TABLE sos_events SET (autovacuum_vacuum_scale_factor = 0.3);

2. 定期手动维护脚本建议（通过pg_cron或操作系统cron）：
   -------------------------------------------------------
   -- 每周日凌晨3点执行全库VACUUM ANALYZE
   VACUUM ANALYZE;
   
   -- 每月月初重建碎片严重的索引
   REINDEX TABLE CONCURRENTLY drug_recognition_records;
   REINDEX TABLE CONCURRENTLY drugs;  -- GIN索引尤其容易碎片化
   
   -- 每季度检查表膨胀率
   SELECT 
       relname AS table_name,
       pg_size_pretty(pg_total_relation_size(relid)) AS total_size,
       pg_size_pretty(pg_relation_size(relid)) AS table_size,
       n_dead_tup AS dead_rows
   FROM pg_stat_user_tables
   ORDER BY n_dead_tup DESC
   LIMIT 10;

3. 监控关键指标：
   -------------------------------------------------------
   a) 序列接近上限警告（SERIAL/BIGSERIAL）
   b) 表膨胀率超过30%报警
   c) 长事务阻塞VACUUM报警（>5分钟的事务）
   d) 索引命中率低于95%报警
   e) 慢查询日志分析（超过1秒的查询）


【四、连接池配置建议】

1. 推荐连接池方案：PgBouncer（轻量级、高性能）
   -------------------------------------------------------
   
   安装方式（Ubuntu/Debian）：
     sudo apt install pgbouncer
   
   关键配置参数（pgbouncer.ini）：
   
   [databases]
   weiguangplus = host=127.0.0.1 port=5432 dbname=weiguangplus
   
   [pgbouncer]
   listen_port = 6432
   auth_type = md5
   auth_file = /etc/pgbouncer/userlist.txt
   pool_mode = transaction  -- 事务级池化（推荐）
   
   # 连接池大小计算公式：
   # max_client_conn = (应用服务器数量 × 每服务器连接数) + 预留连接
   # default_pool_size = CPU核心数 × 2（保守估计）
   # pool_size = min(default_pool_size, max_db_connections / 应用服务器数量)
   
   max_client_conn = 200
   default_pool_size = 20
   reserve_pool_size = 5
   reserve_pool_timeout = 3

2. SQLAlchemy端连接池配置（Python FastAPI应用）：
   -------------------------------------------------------
   from sqlalchemy.pool import QueuePool
   
   engine = create_engine(
       DATABASE_URL,
       poolclass=QueuePool,
       pool_size=10,           # 保持的连接数
       max_overflow=20,        # 额外允许的最大连接数
       pool_recycle=3600,      # 连接回收时间（秒），防断连
       pool_pre_ping=True,     # 使用前自动检测连接有效性
       pool_timeout=30,        # 获取连接超时时间（秒）
       echo=False              # 生产环境关闭SQL日志
   )

3. 连接数监控SQL：
   -------------------------------------------------------
   -- 查看当前数据库连接数和状态
   SELECT 
       state,
       count(*) AS connection_count,
       count(*) FILTER (WHERE query LIKE '%weiguangplus%') AS app_connections
   FROM pg_stat_activity
   WHERE datname = 'weiguangplus'
   GROUP BY state;
   
   -- 查看最大连接数配置
   SHOW max_connections;


【五、备份恢复策略】

1. 日常备份计划：
   -------------------------------------------------------
   a) 全量备份：每天凌晨2点（pg_dump或pg_basebackup）
      pg_dump -F c -Z 9 -f /backup/weiguangplus_$(date +%Y%m%d).dump weiguangplus
   
   b) 增量备份（WAL归档）：实时归档WAL日志
      - 启用archive_mode = on
      - 归档命令：archive_command = 'cp %p /wal_archive/%f'
   
   c) 备份保留策略：
      - 保留最近7天的全量备份
      - 保留最近30天的WAL归档
      - 异地备份：每日同步到云存储（OSS/S3）

2. Point-in-Time Recovery (PITR) 能力：
   -------------------------------------------------------
   -- 可恢复到任意时间点（前提是有连续的WAL归档）
   -- 恢复命令示例（恢复到2026-05-28 15:30:00）：
   pg_basebackup -h localhost -D /var/lib/postgresql/recovery -X stream -P
   # 然后在recovery.conf中设置：
   # recovery_target_time = '2026-05-28 15:30:00'


【六、安全加固建议】

1. 用户权限最小化：
   -------------------------------------------------------
   -- 生产环境应用用户不应拥有DROP/ALTER权限
   -- 仅授予SELECT/INSERT/UPDATE/DELETE即可
   REVOKE ALL PRIVILEGES ON ALL TABLES IN SCHEMA public FROM wg_admin;
   GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO wg_admin;

2. SSL/TLS加密连接：
   -------------------------------------------------------
   -- postgresql.conf:
   ssl = on
   ssl_cert_file = '/etc/ssl/certs/server.crt'
   ssl_key_file = '/etc/ssl/private/server.key'
   
   -- pg_hba.conf:
   # hostssl weiguangplus wg_admin 0.0.0.0/0 scram-sha-256

3. 审计日志：
   -------------------------------------------------------
   -- 安装pg_audit扩展记录所有DDL/DML操作
   CREATE EXTENSION IF NOT EXISTS pgaudit;
   
   -- postgresql.conf:
   pgaudid.log = 'ddl, write'
   pgaudit.log_catalog = off


【七、监控告警指标】

关键监控项：
  1. 数据库连接数使用率（>80%告警）
  2. 查询响应时间P99（>500ms告警）
  3. 死锁发生率（每小时>5次告警）
  4. 磁盘使用率（>85%告警）
  5. 复制延迟（如果有主从复制，>10秒告警）
  6. SOS事件写入延迟（>100ms告警，这是生命攸关的功能！）


【八、SQL编写最佳实践】

1. 使用PREPARED STATEMENT（参数化查询）防止SQL注入
2. 批量操作使用COPY代替多条INSERT（提升10-100倍性能）
3. 大表查询务必加LIMIT，避免意外全表扫描
4. JSONB查询使用@>、?、?&等专用操作符，不要转成文本再查
5. 数组查询使用&&（重叠）、@>（包含）、<@（被包含）操作符
6. 全文搜索优先使用tsvector + tsquery，性能优于LIKE '%keyword%'
7. 避免在WHERE子句中对列使用函数（会导致索引失效）
8. 使用EXPLAIN ANALYZE分析慢查询的执行计划


================================================================================
文档结束
版本：v1.0.0
最后更新：2026-05-28
维护者：微光同行开发团队
================================================================================
*/
