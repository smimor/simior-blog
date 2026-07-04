#!/bin/bash
# ============================================
# 重置脚本
#
# 用法：cd /opt/simior-blog && bash deploy/scripts/reset-server.sh
# ============================================

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
DEPLOY_DIR="$(dirname "$SCRIPT_DIR")"

echo "=========================================="
echo "  重置所有部署数据"
echo "  MySQL、Redis、MinIO 数据将全部删除！"
echo "=========================================="

read -p "确认重置？(y/N): " confirm
if [ "$confirm" != "y" ] && [ "$confirm" != "Y" ]; then
    echo "已取消"
    exit 0
fi

# ---- 停止并删除容器和数据卷 ----
cd "$DEPLOY_DIR/docker"
docker compose down -v --remove-orphans 2>/dev/null || true

echo ""
echo "=========================================="
echo "  重置完成！"
echo "  重新部署：bash deploy/scripts/deploy.sh"
echo "=========================================="
