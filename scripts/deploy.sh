#!/bin/bash
# ============================================
# simior-blog 一键部署脚本（在 VPS 上运行）
# ============================================

set -e

echo "=== simior-blog 部署开始 ==="

# ---- 1. 安装 Docker ----
if ! command -v docker &> /dev/null; then
    echo "[1/6] 安装 Docker..."
    curl -fsSL https://get.docker.com | sh
    systemctl enable --now docker
else
    echo "[1/6] Docker 已安装"
fi

# ---- 2. 清理旧部署 ----
echo "[2/6] 清理旧部署..."
docker compose down -v --remove-orphans 2>/dev/null || true
docker rm -f simior-mysql simior-redis simior-minio simior-server simior-admin 2>/dev/null || true
docker volume rm simior-blog_mysql-data simior-blog_redis-data simior-blog_minio-data 2>/dev/null || true
docker volume prune -f 2>/dev/null || true

# ---- 3. 拉取代码 ----
echo "[3/6] 拉取代码..."
rm -rf /opt/simior-blog
cd /opt
git clone https://github.com/YOUR_USERNAME/simior-blog.git
cd simior-blog

# ---- 4. 生成配置文件 ----
echo "[4/6] 生成配置文件..."
cat > .env << 'EOF'
# ---- MySQL ----
MYSQL_ROOT_PASSWORD=Sb7#kL9xQ2wR
MYSQL_PORT=3306

# ---- Redis ----
REDIS_PORT=6379

# ---- MinIO ----
MINIO_ACCESS_KEY=admin
MINIO_SECRET_KEY=mK3@pL8vN5qT
MINIO_API_PORT=9000
MINIO_CONSOLE_PORT=9001

# ---- 后端 ----
SERVER_PORT=8080

# ---- 前端 ----
ADMIN_PORT=80

# ---- CORS ----
CORS_ALLOWED_ORIGINS=http://115.159.54.200

# ---- OSS（留空）----
OSS_ENDPOINT=
OSS_ACCESS_KEY=
OSS_SECRET_KEY=
OSS_BUCKET_NAME=
EOF

echo "配置文件已生成，内容："
cat .env

# ---- 5. 启动数据库并导入 ----
echo "[5/6] 启动 MySQL 并导入数据库..."
docker compose up -d mysql
echo "等待 MySQL 就绪..."
sleep 15

docker exec -i simior-mysql mysql -uroot -p'Sb7#kL9xQ2wR' simior-blog < blog-server/src/main/resources/simior-blog.sql
echo "数据库导入完成"

# ---- 6. 启动所有服务 ----
echo "[6/6] 启动所有服务..."
docker compose up -d

echo ""
echo "=== 部署完成 ==="
echo "管理后台: http://115.159.54.200"
echo "MinIO控制台: http://115.159.54.200:9001"
echo ""
echo "默认管理员: admin / admin123456"
echo ""
echo "查看日志: docker compose logs -f"
echo "重启服务: docker compose restart"
