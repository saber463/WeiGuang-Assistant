"""
阿里云ECS自动部署脚本
功能：通过SSH密码认证连接服务器 → 上传项目文件 → 执行一键部署
使用：python auto_deploy.py
"""
import paramiko
import os
import sys
import time

# ============================================================
# 服务器配置
# ============================================================
SERVER_IP = "47.108.149.191"
SERVER_PORT = 22
SERVER_USER = "root"
SERVER_PASSWORD = "WgTx2026!@Alibaba"

# 本地项目路径
LOCAL_DIR = os.path.dirname(os.path.abspath(__file__))

def print_step(msg):
    """打印步骤标题"""
    print(f"\n{'='*60}")
    print(f"  {msg}")
    print(f"{'='*60}")

def main():
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())

    try:
        # ============================================================
        # 步骤1: 连接服务器
        # ============================================================
        print_step("步骤1/4: 连接服务器")
        print(f"  正在连接 {SERVER_USER}@{SERVER_IP}:{SERVER_PORT} ...")
        ssh.connect(
            hostname=SERVER_IP,
            port=SERVER_PORT,
            username=SERVER_USER,
            password=SERVER_PASSWORD,
            timeout=30
        )
        print("  SSH连接成功!")

        # ============================================================
        # 步骤2: 上传项目文件
        # ============================================================
        print_step("步骤2/4: 上传项目文件")

        sftp = ssh.open_sftp()

        # 要上传的文件列表
        files_to_upload = [
            "setup.py",
            "app.py",
            "wsgi.py",
            "requirements.txt",
            "knowledge_base.json",
            "bug-bot.service",
            "nginx-bug-bot.conf",
        ]

        for filename in files_to_upload:
            local_path = os.path.join(LOCAL_DIR, filename)
            remote_path = f"/tmp/{filename}"

            if not os.path.exists(local_path):
                print(f"  [跳过] {filename} (文件不存在)")
                continue

            print(f"  上传中: {filename} ({os.path.getsize(local_path)} bytes)")
            sftp.put(local_path, remote_path)
            print(f"  [完成] {filename} -> {remote_path}")

        sftp.close()
        print("  所有文件上传完成!")

        # ============================================================
        # 步骤3: 执行部署
        # ============================================================
        print_step("步骤3/4: 执行服务器部署")

        # 先检查服务器Python版本和系统信息
        print("  检查服务器环境...")
        stdin, stdout, stderr = ssh.exec_command("python3 --version 2>&1 && cat /etc/os-release 2>&1 | head -5")
        print(f"  {stdout.read().decode('utf-8').strip()}")

        # 如果setup.py已经上传，直接运行它
        # 但setup.py是自包含的（内部base64编码了知识库），可以直接运行
        print("  正在运行setup.py部署脚本...")
        print("  (这可能需要几分钟，请耐心等待...)")

        # 执行setup.py
        stdin, stdout, stderr = ssh.exec_command(
            "cd /tmp && python3 setup.py 2>&1",
            timeout=600  # 10分钟超时
        )
        stdin.close()

        # 实时输出部署进度
        while not stdout.channel.exit_status_ready:
            if stdout.channel.recv_ready():
                output = stdout.channel.recv(4096).decode('utf-8', errors='replace')
                print(output, end='', flush=True)
            time.sleep(0.5)

        # 读取剩余输出
        exit_code = stdout.channel.recv_exit_status()
        remaining = stdout.read().decode('utf-8', errors='replace')
        if remaining:
            print(remaining)

        # 检查错误输出
        err_output = stderr.read().decode('utf-8', errors='replace')
        if err_output:
            print(f"  [stderr] {err_output}")

        if exit_code == 0:
            print(f"\n  setup.py 执行成功! (exit_code={exit_code})")
        else:
            print(f"\n  [警告] setup.py 退出码: {exit_code}")
            # 即使有部分错误，也继续验证

        # ============================================================
        # 步骤4: 验证部署
        # ============================================================
        print_step("步骤4/4: 验证部署")

        # 检查服务状态
        print("  检查bug-bot服务状态...")
        stdin, stdout, stderr = ssh.exec_command("systemctl status bug-bot --no-pager -l 2>&1")
        status_output = stdout.read().decode('utf-8', errors='replace')
        print(status_output[:500])

        # 健康检查
        print("  执行健康检查...")
        stdin, stdout, stderr = ssh.exec_command("curl -s http://127.0.0.1:5000/health 2>&1")
        health_output = stdout.read().decode('utf-8', errors='replace')
        print(f"  健康检查结果: {health_output}")

        # 检查Nginx
        print("  检查Nginx状态...")
        stdin, stdout, stderr = ssh.exec_command("systemctl status nginx --no-pager -l 2>&1 | head -10")
        nginx_output = stdout.read().decode('utf-8', errors='replace')
        print(nginx_output[:300])

        print(f"\n{'='*60}")
        print(f"  部署完成!")
        print(f"")
        print(f"  飞书Webhook地址: https://47.108.149.191.nip.io/webhook")
        print(f"  健康检查地址: https://47.108.149.191.nip.io/health")
        print(f"")
        print(f"  服务管理命令 (SSH登录服务器后执行):")
        print(f"    systemctl status bug-bot    # 查看状态")
        print(f"    systemctl restart bug-bot   # 重启服务")
        print(f"    journalctl -u bug-bot -f    # 查看日志")
        print(f"{'='*60}")

    except paramiko.AuthenticationException:
        print(f"\n[错误] SSH认证失败! 请检查密码是否正确。")
        sys.exit(1)
    except paramiko.SSHException as e:
        print(f"\n[错误] SSH连接异常: {e}")
        sys.exit(1)
    except Exception as e:
        print(f"\n[错误] 部署失败: {e}")
        sys.exit(1)
    finally:
        ssh.close()
        print("\n  SSH连接已关闭。")


if __name__ == "__main__":
    main()