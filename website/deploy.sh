#!/bin/bash
# =============================================================================
# 微光同行 - 网站服务器部署脚本
# 适用于: Ubuntu 24.04 (阿里云ECS)
# 使用方式: chmod +x deploy.sh && sudo ./deploy.sh
# =============================================================================

set -e

# ── 配置变量（请修改为你的域名） ──
DOMAIN="${1:-47.108.149.191}"  # 默认使用IP，有域名时替换
EMAIL="admin@example.com"       # Let's Encrypt证书邮箱
APP_DIR="/var/www/weiguang"
NGINX_CONF="/etc/nginx/sites-available/weiguang"

echo "============================================"
echo "  微光同行 - 网站部署脚本"
echo "  域名/IP: $DOMAIN"
echo "============================================"

# ── 1. 更新系统 ──
echo "[1/6] 更新系统包..."
apt update -y && apt upgrade -y

# ── 2. 安装 Nginx ──
echo "[2/6] 安装 Nginx..."
apt install -y nginx

# ── 3. 创建网站目录并复制文件 ──
echo "[3/6] 创建网站目录..."
mkdir -p "$APP_DIR"
# 从当前目录复制 dist 文件到网站目录
cp -r ./dist/* "$APP_DIR/"
chown -R www-data:www-data "$APP_DIR"
echo "  网站文件已复制到: $APP_DIR"

# ── 4. 配置 Nginx ──
echo "[4/6] 配置 Nginx..."

# 创建 Nginx 配置
cat > "$NGINX_CONF" << 'NGINX_EOF'
server {
    listen 80;
    server_name _;  # 匹配所有域名/IP

    root /var/www/weiguang;
    index index.html;

    # Gzip 压缩
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;
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
NGINX_EOF

# 启用站点
ln -sf "$NGINX_CONF" /etc/nginx/sites-enabled/weiguang
rm -f /etc/nginx/sites-enabled/default

# 测试配置
nginx -t

# 重载 Nginx
echo "[5/6] 重载 Nginx..."
systemctl reload nginx
systemctl enable nginx

# ── 6. 配置防火墙 ──
echo "[6/6] 配置防火墙..."
ufw allow 80/tcp
ufw allow 443/tcp
ufw allow 22/tcp
ufw --force enable

echo ""
echo "============================================"
echo "  部署完成！"
echo "  访问地址: http://$DOMAIN"
echo "============================================"
echo ""
echo "  下一步（可选）："
echo "  1. 绑定域名: 将域名A记录指向 $DOMAIN"
echo "  2. 安装SSL证书: sudo apt install -y certbot python3-certbot-nginx"
echo "  3. 获取证书: sudo certbot --nginx -d your-domain.com"
echo "============================================"