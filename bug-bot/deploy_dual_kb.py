"""
双知识库部署脚本
=================
功能：将编程知识库和更新后的 polling_handler.py 部署到服务器，
     重启 bug-bot 服务，验证双知识库检索功能。

部署步骤：
  1. 上传 programming_kb_flat.json 到 /opt/bug-bot/
  2. 上传 polling_handler.py 到 /opt/bug-bot/
  3. 验证服务器 Python 依赖（jieba, sklearn）
  4. 重载 systemd 并重启 bug-bot 服务
  5. 检查服务状态和启动日志
"""
import paramiko
import time
import sys

# ============================================================
# 服务器配置
# ============================================================
SERVER_HOST = "47.108.149.191"
SERVER_PORT = 22
SERVER_USER = "root"
SERVER_PASS = "WgTx2026!@Alibaba"
SERVER_DIR = "/opt/bug-bot"

# 本地文件路径
LOCAL_DIR = "f:/java/weiguangplus/bug-bot"
FILES_TO_UPLOAD = [
    "polling_handler.py",
    "programming_kb_flat.json",
]

# ============================================================
# 连接服务器
# ============================================================
def connect():
    """建立 SSH 连接"""
    print(f"连接服务器 {SERVER_HOST}:{SERVER_PORT} ...")
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(SERVER_HOST, SERVER_PORT, SERVER_USER, SERVER_PASS, timeout=30)
    print("SSH 连接成功")
    return ssh


def upload_file(ssh, local_path: str, remote_path: str):
    """
    上传单个文件到服务器

    参数:
        ssh: SSH 客户端
        local_path: 本地文件完整路径
        remote_path: 服务器目标路径
    """
    sftp = ssh.open_sftp()
    try:
        sftp.put(local_path, remote_path)
        file_size = sftp.stat(remote_path).st_size
        print(f"  已上传: {local_path} -> {remote_path} ({file_size / 1024:.1f} KB)")
    except Exception as e:
        print(f"  上传失败: {e}")
    finally:
        sftp.close()


def run_remote_cmd(ssh, cmd: str, timeout: int = 30) -> tuple:
    """
    在服务器上执行命令

    参数:
        ssh: SSH 客户端
        cmd: 要执行的命令
        timeout: 超时时间（秒）

    返回:
        (stdout, stderr)
    """
    stdin, stdout, stderr = ssh.exec_command(cmd, timeout=timeout)
    out = stdout.read().decode("utf-8", errors="replace")
    err = stderr.read().decode("utf-8", errors="replace")
    return out, err


def main():
    print("=" * 60)
    print("双知识库部署到服务器")
    print("=" * 60)

    ssh = connect()

    # ---- 步骤1：上传文件 ----
    print("\n[步骤1] 上传文件到服务器...")
    for filename in FILES_TO_UPLOAD:
        local_path = f"{LOCAL_DIR}/{filename}"
        remote_path = f"{SERVER_DIR}/{filename}"
        upload_file(ssh, local_path, remote_path)

    # ---- 步骤2：验证文件 ----
    print("\n[步骤2] 验证服务器文件...")
    out, err = run_remote_cmd(ssh, f"ls -lh {SERVER_DIR}/polling_handler.py {SERVER_DIR}/programming_kb_flat.json")
    print(out.strip())

    # ---- 步骤3：验证 Python 依赖 ----
    print("[步骤3] 验证 Python 依赖...")
    out, err = run_remote_cmd(ssh, "python3 -c 'import jieba; import sklearn; print(\"jieba:\", jieba.__version__); print(\"sklearn:\", sklearn.__version__)'")
    print(out.strip())
    if err:
        print(f"依赖警告: {err[:200]}")

    # ---- 步骤4：重启服务 ----
    print("\n[步骤4] 重启 bug-bot 服务...")
    out, err = run_remote_cmd(ssh, "systemctl daemon-reload && systemctl restart bug-bot")
    if err:
        print(f"重启输出: {err[:300]}")

    # 等待服务启动
    print("等待服务启动 (8秒)...")
    time.sleep(8)

    # ---- 步骤5：检查服务状态 ----
    print("\n[步骤5] 服务状态检查...")
    out, err = run_remote_cmd(ssh, "systemctl status bug-bot --no-pager -l 2>&1")
    print(out)

    # ---- 步骤6：查看启动日志 ----
    print("\n[步骤6] 最新启动日志...")
    out, err = run_remote_cmd(ssh, "journalctl -u bug-bot --no-pager -n 30 2>&1")
    print(out)

    # ---- 步骤7：检查进程 ----
    print("\n[步骤7] 进程检查...")
    out, err = run_remote_cmd(ssh, "ps aux | grep polling_handler | grep -v grep")
    print(out.strip() or "（未找到进程，可能正在启动中）")

    ssh.close()
    print("\n" + "=" * 60)
    print("部署完成！请在飞书中发送消息测试双知识库检索")
    print("=" * 60)


if __name__ == "__main__":
    main()