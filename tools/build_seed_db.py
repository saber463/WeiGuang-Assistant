from __future__ import annotations

import argparse
import csv
import sqlite3
import time
from pathlib import Path
from typing import Iterable


PROJECT_ROOT = Path(__file__).resolve().parents[1]
SCHEMA_PATH = PROJECT_ROOT / "docs" / "sql" / "drug_offline_schema.sql"
SEED_DIR = PROJECT_ROOT / "data" / "seed"
DEFAULT_OUTPUT = PROJECT_ROOT / "app" / "src" / "main" / "assets" / "db" / "drugs.db"


TABLE_IMPORTS = [
    ("drug_master", SEED_DIR / "drug_master.csv"),
    ("drug_detail", SEED_DIR / "drug_detail.csv"),
    ("drug_alias", SEED_DIR / "drug_alias.csv"),
    ("drug_sign_mapping", SEED_DIR / "drug_sign_mapping.csv"),
    ("drug_rule", SEED_DIR / "drug_rule.csv"),
]

# 正式基线列定义：
# 这份映射是构建脚本的“闸门”，用来防止 CSV 表头、SQL 建表脚本和 Room 实体再次各自漂移。
# 若后续要改 schema，必须同步修改：
# 1. Room Entity / AppDatabase
# 2. docs/sql/drug_offline_schema.sql
# 3. data/seed/*.csv
# 4. 本脚本中的 EXPECTED_TABLE_COLUMNS
EXPECTED_CSV_COLUMNS = {
    "drug_master": [
        "drug_id",
        "generic_name",
        "trade_name",
        "approval_no",
        "manufacturer",
        "dosage_form",
        "specification",
        "category_name",
        "pinyin_key",
        "initials_key",
        "search_tokens",
        "source_tag",
        "license_note",
    ],
    "drug_detail": [
        "drug_id",
        "composition",
        "indication",
        "usage_and_dosage",
        "taboo",
        "attention",
        "adverse_reaction",
        "interaction_text",
        "storage_method",
        "valid_period",
        "package_info",
        "tts_summary",
        "source_tag",
        "updated_at",
    ],
    "drug_alias": [
        "alias_id",
        "drug_id",
        "alias_name",
        "alias_type",
        "normalized_alias",
        "created_at",
    ],
    "drug_sign_mapping": [
        "mapping_id",
        "drug_id",
        "sign_keyword",
        "sign_display_text",
        "video_path",
        "image_seq_path",
        "spelling_mode",
        "priority_no",
        "enabled",
        "updated_at",
    ],
    "drug_rule": [
        "rule_id",
        "drug_id",
        "match_field",
        "match_value",
        "rule_type",
        "risk_level",
        "display_message",
        "tts_message",
        "enabled",
        "updated_at",
    ],
}

EXPECTED_SCHEMA_COLUMNS = {
    "drug_master": EXPECTED_CSV_COLUMNS["drug_master"] + ["created_at", "updated_at"],
    "drug_detail": EXPECTED_CSV_COLUMNS["drug_detail"],
    "drug_alias": EXPECTED_CSV_COLUMNS["drug_alias"],
    "drug_sign_mapping": EXPECTED_CSV_COLUMNS["drug_sign_mapping"],
    "drug_rule": EXPECTED_CSV_COLUMNS["drug_rule"],
}


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="根据 CSV 种子数据构建微光畅行本地药品 SQLite 数据库。"
    )
    parser.add_argument(
        "--output",
        default=str(DEFAULT_OUTPUT),
        help="输出 SQLite 文件路径，默认生成到 app/src/main/assets/db/drugs.db",
    )
    return parser.parse_args()


def read_csv_rows(csv_path: Path) -> tuple[list[str], list[dict[str, object]]]:
    with csv_path.open("r", encoding="utf-8-sig", newline="") as file:
        reader = csv.DictReader(file)
        fieldnames = reader.fieldnames or []
        rows = [normalize_row(row) for row in reader]
    return fieldnames, rows


def validate_csv_header(table_name: str, fieldnames: list[str]) -> None:
    expected = EXPECTED_CSV_COLUMNS[table_name]
    if fieldnames != expected:
        raise ValueError(
            f"{table_name} CSV 表头不匹配。\n"
            f"期望: {expected}\n"
            f"实际: {fieldnames}"
        )


def normalize_row(row: dict[str, str]) -> dict[str, object]:
    normalized: dict[str, object] = {}
    for key, value in row.items():
        if value is None:
            normalized[key] = None
            continue

        text = value.strip()
        if text == "":
            normalized[key] = None
            continue

        if text.isdigit():
            normalized[key] = int(text)
            continue

        normalized[key] = text
    return normalized


def build_insert_sql(table_name: str, columns: Iterable[str]) -> str:
    column_list = list(columns)
    placeholders = ", ".join(["?"] * len(column_list))
    joined_columns = ", ".join(column_list)
    return f"INSERT INTO {table_name} ({joined_columns}) VALUES ({placeholders})"


def initialize_database(conn: sqlite3.Connection) -> None:
    schema_sql = SCHEMA_PATH.read_text(encoding="utf-8")
    conn.executescript(schema_sql)


def read_table_columns(conn: sqlite3.Connection, table_name: str) -> list[str]:
    rows = conn.execute(f"PRAGMA table_info({table_name})").fetchall()
    return [row[1] for row in rows]


def validate_schema_columns(conn: sqlite3.Connection) -> None:
    for table_name, expected in EXPECTED_SCHEMA_COLUMNS.items():
        actual = read_table_columns(conn, table_name)
        if actual != expected:
            raise ValueError(
                f"{table_name} SQL 建表列不匹配。\n"
                f"期望: {expected}\n"
                f"实际: {actual}"
            )


def import_table(conn: sqlite3.Connection, table_name: str, csv_path: Path) -> None:
    if not csv_path.exists():
        raise FileNotFoundError(f"缺少种子文件: {csv_path}")

    fieldnames, rows = read_csv_rows(csv_path)
    if not fieldnames:
        return

    validate_csv_header(table_name, fieldnames)
    insert_columns = list(fieldnames)
    if table_name == "drug_master":
        insert_columns += ["created_at", "updated_at"]
        now_ms = int(time.time() * 1000)
        values = [
            tuple(row.get(column) for column in fieldnames) + (now_ms, now_ms)
            for row in rows
        ]
    else:
        values = [tuple(row.get(column) for column in insert_columns) for row in rows]

    insert_sql = build_insert_sql(table_name, insert_columns)
    conn.executemany(insert_sql, values)


def validate_seed_row_counts(conn: sqlite3.Connection) -> None:
    for table_name, _ in TABLE_IMPORTS:
        row_count = conn.execute(f"SELECT COUNT(*) FROM {table_name}").fetchone()[0]
        if row_count <= 0:
            raise ValueError(f"{table_name} 导入后为空，请检查 CSV 内容。")


def main() -> None:
    args = parse_args()
    output_path = Path(args.output).resolve()
    output_path.parent.mkdir(parents=True, exist_ok=True)

    if output_path.exists():
        output_path.unlink()

    with sqlite3.connect(output_path) as conn:
        conn.execute("PRAGMA foreign_keys = ON;")
        initialize_database(conn)
        validate_schema_columns(conn)
        for table_name, csv_path in TABLE_IMPORTS:
            import_table(conn, table_name, csv_path)
        validate_seed_row_counts(conn)
        conn.commit()

    print(f"SQLite 数据库已生成: {output_path}")


if __name__ == "__main__":
    main()
