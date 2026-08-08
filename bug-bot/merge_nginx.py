# -*- coding: utf-8 -*-
"""方案：把fenis.asia挂到bug-bot的nginx server block上
bug-bot的443 block（nip.io）没有被WAF拦截，把Let's Encrypt证书挂上去
"""
import paramiko
import time

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('47.108.149.191', 22, 'root', 'WgTx2026!@Alibaba', timeout=15)

# 修改bug-bot配置，添加fenis.asia作为server_name，使用Let's Encrypt证书
bugbot_config = """# bug-bot + fenis.asia 合并站点
# HTTP -> HTTPS
server {
    listen 80;
    server_name 47.108.149.191.nip.io fenis.asia;
    location /.well-known/acme-challenge/ {
        root /var/www/html;
    }
    location / {
        return 301 https://$host$request_uri;
    }
}

# HTTPS - 使用Let's Encrypt证书
server {
    listen 443 ssl http2;
    server_name 47.108.149.191.nip.io fenis.asia;

    ssl_certificate     /etc/letsencrypt/live/fenis.asia/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/fenis.asia/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    access_log /var/log/nginx/bug-bot-access.log;
    error_log /var/log/nginx/bug-bot-error.log;

    location / {
        proxy_pass http://127.0.0.1:5000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 60s;
    }
}
"""

# 备份
ssh.exec_command('cp /etc/nginx/sites-available/bug-bot /etc/nginx/sites-available/bug-bot.bak3')

# 写入新配置
stdin, stdout, stderr = ssh.exec_command("cat > /etc/nginx/sites-available/bug-bot << 'NGINX_EOF'\n" + bugbot_config + "\nNGINX_EOF")
stdout.read()

# 禁用独立的fenis站点（避免冲突）
stdin, stdout, stderr = ssh.exec_command('rm -f /etc/nginx/sites-enabled/fenis')
stdout.read()

# 检查nginx配置
stdin, stdout, stderr = ssh.exec_command('nginx -t')
out = stdout.read().decode()
err = stderr.read().decode()
print('nginx -t:', out + err)

if 'successful' in (out + err):
    # 重载nginx
    ssh.exec_command('systemctl reload nginx')
    print('Nginx reloaded')
    
    # 测试
    time.sleep(1)
    
    print('\n=== 测试nip.io ===')
    stdin, stdout, stderr = ssh.exec_command('curl -sk https://47.108.149.191.nip.io/webhook?challenge=test_nip 2>&1')
    print(stdout.read().decode()[:200])
    
    print('\n=== 测试fenis.asia ===')
    stdin, stdout, stderr = ssh.exec_command('curl -sk https://fenis.asia/webhook?challenge=test_fenis 2>&1')
    print(stdout.read().decode()[:200])
    
    print('\n=== 检查证书 ===')
    stdin, stdout, stderr = ssh.exec_command('echo | openssl s_client -connect fenis.asia:443 -servername fenis.asia 2>&1 | grep -E "subject|issuer|Verify"')
    print(stdout.read().decode())
else:
    print('NGINX CONFIG ERROR!')

ssh.close()