#!/bin/bash
# =============================================================================
# SSL 证书安装脚本（Let's Encrypt）
# 使用前请确保域名已解析到服务器IP
# 使用方式: sudo ./deploy_ssl.sh your-domain.com
# =============================================================================

set -e

DOMAIN="${1}"

if [ -z "$DOMAIN" ]; then
    echo "用法: sudo ./deploy_ssl.sh your-domain.com"
    exit 1
fi

echo "============================================"
echo "  安装 SSL 证书: $DOMAIN"
echo "============================================"

# 安装 Certbot
apt install -y certbot python3-certbot-nginx

# 获取证书
certbot --nginx -d "$DOMAIN" --non-interactive --agree-tos --email admin@example.com

# 配置自动续期
systemctl enable certbot.timer
systemctl start certbot.timer

# 验证自动续期
certbot renew --dry-run

echo ""
echo "============================================"
echo "  SSL 证书安装完成！"
echo "  访问地址: https://$DOMAIN"
echo "============================================"