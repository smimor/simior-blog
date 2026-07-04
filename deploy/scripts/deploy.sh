#!/bin/bash
# ============================================
# 部署脚本
#
# 用法：cd /opt/simior-blog && bash deploy/scripts/deploy.sh
# ============================================

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
DEPLOY_DIR="$(dirname "$SCRIPT_DIR")"

echo "=========================================="
echo "  simior-blog 部署"
echo "=========================================="

# ---- 生成 .env ----
if [ ! -f "$DEPLOY_DIR/.env" ]; then
    cp "$DEPLOY_DIR/.env.example" "$DEPLOY_DIR/.env"
    echo "已生成 .env 配置文件"
    echo "如需修改，请编辑 $DEPLOY_DIR/.env"
fi

# ---- 构建并启动 ----
cd "$DEPLOY_DIR/docker"
docker compose up -d --build

echo ""
echo "=========================================="
echo "  部署完成！"
echo ""
echo "  访问地址："
echo "    前台: http://服务器IP/"
echo "    后台: http://服务器IP/admin"
echo "    API:  http://服务器IP/api/"
echo "=========================================="
