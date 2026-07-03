#!/bin/bash
# ============================================
# 清理部署文件（保留 git、docker）
# 用法：cd /opt/simior-blog/deploy && bash scripts/reset-server.sh
# ============================================

set -e

echo "=========================================="
echo "  清理部署文件"
echo "  保留：git、docker、docker-compose"
echo "=========================================="
read -p "确认清理？(y/N): " confirm
if [ "$confirm" != "y" ] && [ "$confirm" != "Y" ]; then
    echo "已取消"
    exit 0
fi

echo "[1/3] 停止并删除容器和数据卷..."
cd /opt/simior-blog/deploy 2>/dev/null || true
docker compose down -v --remove-orphans 2>/dev/null || true

echo "[2/3] 清理 Docker 资源..."
docker volume prune -f 2>/dev/null || true
docker network prune -f 2>/dev/null || true
docker image prune -af 2>/dev/null || true

echo "[3/3] 删除项目文件和定时任务..."
rm -rf /opt/simior-blog
crontab -l 2>/dev/null | grep -v "simior-blog" | crontab 2>/dev/null || true

echo ""
echo "=========================================="
echo "  清理完成"
echo "  git: $(git --version 2>/dev/null || echo '未安装')"
echo "  docker: $(docker --version 2>/dev/null || echo '未安装')"
echo ""
echo "  重新部署：cd deploy && bash scripts/deploy.sh"
echo "=========================================="
