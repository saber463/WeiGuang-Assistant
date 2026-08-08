"""
更新知识库并重启服务
功能：上传新版 knowledge_base.json → 验证格式 → 重启 bug-bot 服务
使用：python update_kb.py
"""
import paramiko
import os
import json

# ============================================================
# 服务器配置
# ============================================================
SERVER_IP = "47.108.149.191"
SERVER_PORT = 22
SERVER_USER = "root"
SERVER_PASSWORD = "WgTx2026!@Alibaba"

LOCAL_DIR = os.path.dirname(os.path.abspath(__file__))
LOCAL_KB = os.path.join(LOCAL_DIR, "knowledge_base.json")
REMOTE_KB = "/opt/bug-bot/knowledge_base.json"


def main():
    print("=" * 60)
    print("  知识库更新部署")
    print("=" * 60)

    # 1. 验证本地知识库
    with open(LOCAL_KB, "r", encoding="utf-8") as f:
        kb = json.load(f)
    print(f"\n[1/4] 本地知识库验证: {len(kb)} 条BUG, 格式正确")

    # 统计
    cats = {}
    for b in kb:
        c = b.get("category", "未知")
        cats[c] = cats.get(c, 0) + 1
    print(f"  分类: {len(cats)} 个主分类")
    langs = {}
    for b in kb:
        l = b.get("language", "未知")
        langs[l] = langs.get(l, 0) + 1
    print(f"  语言: {langs}")

    # 2. SSH 连接
    print(f"\n[2/4] 连接服务器 {SERVER_IP}...")
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(SERVER_IP, SERVER_PORT, SERVER_USER, SERVER_PASSWORD, timeout=30)
    sftp = ssh.open_sftp()
    print("  连接成功")

    # 3. 上传知识库
    print(f"\n[3/4] 上传知识库...")
    # 先备份
    stdin, stdout, stderr = ssh.exec_command(
        "cp /opt/bug-bot/knowledge_base.json /opt/bug-bot/knowledge_base_v1_backup.json"
    )
    stdout.read()
    print("  旧知识库已备份")

    sftp.put(LOCAL_KB, REMOTE_KB)
    print("  新知识库上传完成")

    # 验证远程文件
    stdin, stdout, stderr = ssh.exec_command(
        f"python3 -c \"import json; kb=json.load(open('{REMOTE_KB}','r')); print(f'{len(kb)} 条BUG')\""
    )
    remote_result = stdout.read().decode("utf-8", errors="replace").strip()
    print(f"  远程验证: {remote_result}")

    # 4. 重启服务
    print(f"\n[4/4] 重启 bug-bot 服务...")
    stdin, stdout, stderr = ssh.exec_command("systemctl restart bug-bot")
    stdout.read()
    stderr.read()
    print("  服务重启指令已发送")

    # 等待启动
    import time
    time.sleep(3)

    # 检查状态
    stdin, stdout, stderr = ssh.exec_command("systemctl is-active bug-bot")
    status = stdout.read().decode("utf-8", errors="replace").strip()
    print(f"  服务状态: {status}")

    # 检查最近日志
    stdin, stdout, stderr = ssh.exec_command(
        "journalctl -u bug-bot --no-pager -n 5 -o cat 2>/dev/null || echo '无日志'"
    )
    logs = stdout.read().decode("utf-8", errors="replace").strip()
    print(f"\n  最近日志:\n{logs}")

    sftp.close()
    ssh.close()
    print(f"\n{'=' * 60}")
    print(f"  部署完成! 服务状态: {status}")
    print(f"{'=' * 60}")


if __name__ == "__main__":
    main()