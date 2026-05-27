-- 微光畅行离线药品库正式建表脚本：
-- 这份脚本必须与 App 侧 Room Entity 保持一致，只负责把 CSV 内容按正式 schema 落到 SQLite。
-- 当前正式基线对应 AppDatabase version = 4。
PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS drug_master (
    drug_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    generic_name TEXT NOT NULL,
    trade_name TEXT,
    approval_no TEXT,
    manufacturer TEXT,
    dosage_form TEXT,
    specification TEXT,
    category_name TEXT,
    pinyin_key TEXT,
    initials_key TEXT,
    search_tokens TEXT,
    source_tag TEXT,
    license_note TEXT,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS drug_detail (
    drug_id INTEGER NOT NULL PRIMARY KEY,
    composition TEXT,
    indication TEXT,
    usage_and_dosage TEXT,
    taboo TEXT,
    attention TEXT,
    adverse_reaction TEXT,
    interaction_text TEXT,
    storage_method TEXT,
    valid_period TEXT,
    package_info TEXT,
    tts_summary TEXT,
    source_tag TEXT,
    updated_at INTEGER NOT NULL,
    FOREIGN KEY (drug_id) REFERENCES drug_master(drug_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS drug_alias (
    alias_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    drug_id INTEGER NOT NULL,
    alias_name TEXT NOT NULL,
    alias_type TEXT NOT NULL,
    normalized_alias TEXT,
    created_at INTEGER NOT NULL,
    FOREIGN KEY (drug_id) REFERENCES drug_master(drug_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS drug_sign_mapping (
    mapping_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    drug_id INTEGER NOT NULL,
    sign_keyword TEXT NOT NULL,
    sign_display_text TEXT,
    video_path TEXT,
    image_seq_path TEXT,
    spelling_mode TEXT NOT NULL,
    priority_no INTEGER NOT NULL,
    enabled INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    FOREIGN KEY (drug_id) REFERENCES drug_master(drug_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS drug_rule (
    rule_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    drug_id INTEGER,
    match_field TEXT NOT NULL,
    match_value TEXT NOT NULL,
    rule_type TEXT NOT NULL,
    risk_level TEXT NOT NULL,
    display_message TEXT NOT NULL,
    tts_message TEXT,
    enabled INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    FOREIGN KEY (drug_id) REFERENCES drug_master(drug_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS index_drug_master_generic_name
ON drug_master(generic_name);

CREATE INDEX IF NOT EXISTS index_drug_master_trade_name
ON drug_master(trade_name);

CREATE UNIQUE INDEX IF NOT EXISTS index_drug_master_approval_no
ON drug_master(approval_no);

CREATE INDEX IF NOT EXISTS index_drug_master_pinyin_key
ON drug_master(pinyin_key);

CREATE INDEX IF NOT EXISTS index_drug_master_initials_key
ON drug_master(initials_key);

CREATE INDEX IF NOT EXISTS index_drug_alias_drug_id
ON drug_alias(drug_id);

CREATE INDEX IF NOT EXISTS index_drug_alias_alias_name
ON drug_alias(alias_name);

CREATE INDEX IF NOT EXISTS index_drug_alias_normalized_alias
ON drug_alias(normalized_alias);

CREATE INDEX IF NOT EXISTS index_drug_sign_mapping_drug_id
ON drug_sign_mapping(drug_id);

CREATE INDEX IF NOT EXISTS index_drug_sign_mapping_sign_keyword
ON drug_sign_mapping(sign_keyword);

CREATE INDEX IF NOT EXISTS index_drug_rule_drug_id
ON drug_rule(drug_id);

CREATE INDEX IF NOT EXISTS index_drug_rule_match_field
ON drug_rule(match_field);

CREATE INDEX IF NOT EXISTS index_drug_rule_match_value
ON drug_rule(match_value);
