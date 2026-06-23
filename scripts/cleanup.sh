#!/bin/bash
# ============================================
# simior-blog 完整清理脚本
# 在 VPS 上运行：bash cleanup.sh
# ============================================

set -e

echo "=== 停止所有容器 ==="
cd /opt/simior-blog 2>/dev/null || cd ~/simior-blog 2>/dev/null || true
docker compose down -v --remove-orphans 2>/dev/null || true

echo "=== 删除残留容器 ==="
docker rm -f simior-mysql simior-redis simior-minio simior-server simior-admin 2>/dev/null || true

echo "=== 删除相关镜像 ==="
docker rmi $(docker images -q) 2>/dev/null || true

echo "=== 删除 Docker 卷 ==="
docker volume rm simior-blog_mysql-data simior-blog_redis-data simior-blog_minio-data 2>/dev/null || true
docker volume prune -f 2>/dev/null || true

echo "=== 删除项目文件 ==="
rm -rf /opt/simior-blog
rm -rf ~/simior-blog

echo "=== 清理完成 ==="
echo ""
echo "现在可以重新部署了，运行："
echo "  cd /opt"
echo "  git clone https://github.com/YOUR_USERNAME/simior-blog.git"
echo "  cd simior-blog"
echo "  cp .env.example .env"
echo "  vim .env"
