#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
微光同行 - 网站自动部署脚本
通过 SSH 上传 dist 文件到阿里云 ECS 并配置 Nginx
"""

import os
import sys
import time
from pathlib import Path

import paramiko
from scp import SCPClient

# ═══════════════════════════════════════════════════════════════════════════════
# 服务器配置
# ═══════════════════════════════════════════════════════════════════════════════
HOST = "47.108.149.191"
PORT = 22
USER = "root"
PASSWORD = "WgTx2026!@Alibaba"

DIST_DIR = os.path.join(os.path.dirname(__file__), "dist")
REMOTE_DIR = "/var/www/weiguang"
NGINX_CONF = "/etc/nginx/sites-available/weiguang"


def create_ssh_client():
    """创建 SSH 连接"""
    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    client.connect(HOST, PORT, USER, PASSWORD, timeout=30)
    print(f"SSH 连接成功: {USER}@{HOST}")
    return client


def run_command(client, cmd, desc=""):
    """执行远程命令并打印输出"""
    if desc:
        print(f"\n[{desc}]")
    print(f"  $ {cmd}")
    stdin, stdout, stderr = client.exec_command(cmd)
    exit_code = stdout.channel.recv_exit_status()
    out = stdout.read().decode('utf-8', errors='replace')
    err = stderr.read().decode('utf-8', errors='replace')
    if out.strip():
        print(out.strip())
    if err.strip():
        print(f"  [stderr] {err.strip()}")
    if exit_code != 0:
        print(f"  [警告] 退出码: {exit_code}")
    return exit_code, out, err


def upload_files(client):
    """上传 dist 文件到服务器"""
    print(f"\n[1/5] 上传网站文件到服务器...")
    print(f"  本地: {DIST_DIR}")
    print(f"  远程: {REMOTE_DIR}")

    # 确保远程目录存在
    run_command(client, f"mkdir -p {REMOTE_DIR}")

    # 上传文件
    with SCPClient(client.get_transport()) as scp:
        for root, dirs, files in os.walk(DIST_DIR):
            for file in files:
                local_path = os.path.join(root, file)
                # 计算远程相对路径
                rel_path = os.path.relpath(local_path, DIST_DIR)
                remote_path = os.path.join(REMOTE_DIR, rel_path).replace("\\", "/")
                # 确保远程子目录存在
                remote_subdir = os.path.dirname(remote_path)
                client.exec_command(f"mkdir -p {remote_subdir}")
                scp.put(local_path, remote_path)
    print(f"  上传完成！")


def setup_nginx(client):
    """安装并配置 Nginx"""
    print(f"\n[2/5] 安装 Nginx...")
    run_command(client, "apt update -y")
    run_command(client, "apt install -y nginx")

    print(f"\n[3/5] 配置 Nginx...")

    # Nginx 配置内容
    nginx_config = """server {
    listen 80;
    server_name _;

    root /var/www/weiguang;
    index index.html;

    # Gzip 压缩
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml text/xml+rss text/javascript;
    gzip_min_length 1000;
    gzip_comp_level 6;

    # 静态资源缓存
    location /assets/ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # SPA 路由支持
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 安全头
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;
}
"""
    # 写入配置文件
    cmd = f"""cat > {NGINX_CONF} << 'EOF'
{nginx_config}
EOF"""
    run_command(client, cmd)

    # 启用站点
    run_command(client, f"ln -sf {NGINX_CONF} /etc/nginx/sites-enabled/weiguang")
    run_command(client, "rm -f /etc/nginx/sites-enabled/default")

    # 设置权限
    run_command(client, f"chown -R www-data:www-data {REMOTE_DIR}")

    # 测试配置
    print(f"\n[4/5] 测试 Nginx 配置...")
    run_command(client, "nginx -t")


def start_nginx(client):
    """启动 Nginx"""
    print(f"\n[5/5] 启动 Nginx...")
    run_command(client, "systemctl restart nginx")
    run_command(client, "systemctl enable nginx")

    # 配置防火墙
    run_command(client, "ufw allow 80/tcp", "配置防火墙")
    run_command(client, "ufw allow 443/tcp")
    run_command(client, "ufw allow 22/tcp")
    run_command(client, "ufw --force enable")


def verify_deployment(client):
    """验证部署"""
    print(f"\n[验证] 检查部署状态...")
    run_command(client, "systemctl status nginx --no-pager -l | head -20")
    run_command(client, f"ls -la {REMOTE_DIR}/")
    run_command(client, "curl -s -o /dev/null -w '%{http_code}' http://localhost/")


def main():
    print("=" * 60)
    print("  微光同行 - 网站自动部署脚本")
    print(f"  目标服务器: {HOST}:{PORT}")
    print("=" * 60)

    if not os.path.exists(DIST_DIR):
        print(f"\n[错误] dist 目录不存在: {DIST_DIR}")
        print("请先运行: cd website && npm run build")
        sys.exit(1)

    try:
        client = create_ssh_client()
        upload_files(client)
        setup_nginx(client)
        start_nginx(client)
        verify_deployment(client)
        client.close()

        print("\n" + "=" * 60)
        print(f"  部署成功！")
        print(f"  访问地址: http://{HOST}")
        print("=" * 60)
        print("\n  下一步（可选）:")
        print(f"  1. 绑定域名后运行: certbot --nginx -d your-domain.com")
        print(f"  2. 本地验证: curl -I http://{HOST}")
        print("=" * 60)

    except Exception as e:
        print(f"\n[错误] 部署失败: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()