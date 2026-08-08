#!/bin/bash
# ============================================================
# BUG诊断机器人 - 阿里云一键部署脚本
# 功能：上传文件 → 安装依赖 → 配置Nginx → HTTPS证书 → 启动服务
# 使用：bash deploy.sh <服务器IP>
# 示例：bash deploy.sh 47.108.149.191
# ============================================================
set -e

SERVER_IP="${1:-47.108.149.191}"
DOMAIN="${SERVER_IP}.nip.io"
APP_DIR="/opt/bug-bot"

echo "=========================================="
echo "  BUG诊断机器人 - 阿里云部署"
echo "  服务器: $SERVER_IP"
echo "  域名: $DOMAIN"
echo "=========================================="

# 1. 上传文件
echo ""
echo "[1/6] 上传项目文件..."
ssh root@$SERVER_IP "mkdir -p $APP_DIR"
scp app.py wsgi.py requirements.txt knowledge_base.json root@$SERVER_IP:$APP_DIR/
scp bug-bot.service root@$SERVER_IP:/etc/systemd/system/
scp nginx-bug-bot.conf root@$SERVER_IP:/etc/nginx/sites-available/bug-bot
echo "  ✓ 文件上传完成"

# 2. 安装系统依赖
echo ""
echo "[2/6] 安装系统依赖..."
ssh root@$SERVER_IP "apt-get update -qq && apt-get install -y -qq python3-pip python3-venv nginx certbot python3-certbot-nginx"
echo "  ✓ 系统依赖安装完成"

# 3. 安装Python依赖
echo ""
echo "[3/6] 安装Python依赖..."
ssh root@$SERVER_IP "cd $APP_DIR && pip3 install -r requirements.txt gunicorn --break-system-packages -q"
echo "  ✓ Python依赖安装完成"

# 4. 配置Nginx + HTTPS
echo ""
echo "[4/6] 配置Nginx和HTTPS证书..."
ssh root@$SERVER_IP "ln -sf /etc/nginx/sites-available/bug-bot /etc/nginx/sites-enabled/ && rm -f /etc/nginx/sites-enabled/default"
ssh root@$SERVER_IP "mkdir -p /var/www/html"
# 先启动HTTP-only的Nginx用于证书验证
ssh root@$SERVER_IP "sed -i 's/listen 443 ssl http2;/#listen 443 ssl http2;/' /etc/nginx/sites-available/bug-bot"
ssh root@$SERVER_IP "nginx -t && systemctl reload nginx"
# 申请Let's Encrypt证书
echo "  正在申请SSL证书..."
ssh root@$SERVER_IP "certbot certonly --webroot -w /var/www/html -d $DOMAIN --non-interactive --agree-tos --email admin@$DOMAIN --force-renewal" || {
    echo "  ⚠ SSL证书申请失败，将使用自签名证书"
    ssh root@$SERVER_IP "mkdir -p /etc/letsencrypt/live/$DOMAIN && openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout /etc/letsencrypt/live/$DOMAIN/privkey.pem -out /etc/letsencrypt/live/$DOMAIN/fullchain.pem -subj '/CN=$DOMAIN'"
}
# 恢复完整Nginx配置
ssh root@$SERVER_IP "sed -i 's/#listen 443 ssl http2;/listen 443 ssl http2;/' /etc/nginx/sites-available/bug-bot"
ssh root@$SERVER_IP "nginx -t && systemctl reload nginx"
echo "  ✓ Nginx + HTTPS配置完成"

# 5. 启动服务
echo ""
echo "[5/6] 启动BUG诊断机器人服务..."
ssh root@$SERVER_IP "systemctl daemon-reload && systemctl enable bug-bot && systemctl restart bug-bot"
echo "  ✓ 服务启动完成"

# 6. 验证部署
echo ""
echo "[6/6] 验证部署..."
sleep 3
HEALTH=$(ssh root@$SERVER_IP "curl -s http://127.0.0.1:5000/health")
echo "  本地健康检查: $HEALTH"
echo ""

echo "=========================================="
echo "  部署完成！"
echo ""
echo "  Webhook地址: https://$DOMAIN/webhook"
echo "  健康检查: https://$DOMAIN/health"
echo ""
echo "  服务管理命令（在服务器上执行）："
echo "    systemctl status bug-bot    # 查看状态"
echo "    systemctl restart bug-bot   # 重启服务"
echo "    journalctl -u bug-bot -f    # 查看日志"
echo "=========================================="