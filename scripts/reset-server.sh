#!/bin/bash
# ============================================
# simior-blog 重置服务器脚本
# 删除所有容器、卷、镜像、项目文件，恢复初始状态
# 用法：bash scripts/reset-server.sh
# ============================================

set -e

echo "=========================================="
echo "  警告：此操作将删除所有数据！"
echo "  包括：容器、数据卷、项目文件、备份"
echo "=========================================="
read -p "确认重置？(y/N): " confirm
if [ "$confirm" != "y" ] && [ "$confirm" != "Y" ]; then
    echo "已取消"
    exit 0
fi

echo "[1/4] 停止并删除所有容器..."
cd /opt/simior-blog 2>/dev/null || true
docker compose down -v --remove-orphans 2>/dev/null || true

echo "[2/4] 清理 Docker 资源..."
docker rm -f $(docker ps -aq) 2>/dev/null || true
docker volume prune -f 2>/dev/null || true
docker network prune -f 2>/dev/null || true
docker image prune -af 2>/dev/null || true

echo "[3/4] 删除项目文件..."
rm -rf /opt/simior-blog

echo "[4/4] 清理定时任务..."
crontab -l 2>/dev/null | grep -v "simior-blog" | crontab - 2>/dev/null || true

echo ""
echo "=========================================="
echo "  服务器已重置为初始状态"
echo "  可以重新执行部署脚本"
echo "=========================================="
