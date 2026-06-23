#!/bin/bash
# ============================================
# simior-blog 一键部署脚本
# 在 VPS 上执行：bash deploy.sh
# ============================================

set -e

echo "=== simior-blog 一键部署 ==="

# 安装 Docker
if ! command -v docker &> /dev/null; then
    echo "[1/4] 安装 Docker..."
    curl -fsSL https://get.docker.com | sh
    systemctl enable --now docker
else
    echo "[1/4] Docker 已安装"
fi

# 安装 Docker Compose 插件
if ! docker compose version &> /dev/null; then
    echo "[2/4] 安装 Docker Compose..."
    apt-get update && apt-get install -y docker-compose-plugin
else
    echo "[2/4] Docker Compose 已安装"
fi

# 拉取代码
echo "[3/4] 拉取代码..."
rm -rf /opt/simior-blog
cd /opt
git clone https://github.com/smimor/simior-blog.git
cd simior-blog

# 生成 .env
cat > .env << 'EOF'
MYSQL_ROOT_PASSWORD=Sb7#kL9xQ2wR
MYSQL_PORT=3306
REDIS_PORT=6379
MINIO_ACCESS_KEY=admin
MINIO_SECRET_KEY=mK3@pL8vN5qT
MINIO_API_PORT=9000
MINIO_CONSOLE_PORT=9001
SERVER_PORT=8080
ADMIN_PORT=80
CORS_ALLOWED_ORIGINS=http://115.159.54.200
OSS_ENDPOINT=
OSS_ACCESS_KEY=
OSS_SECRET_KEY=
OSS_BUCKET_NAME=
EOF

# 启动服务并导入数据库
echo "[4/4] 启动服务..."
docker compose up -d mysql
echo "等待 MySQL 就绪..."
sleep 15
docker exec -i simior-mysql mysql -uroot -p'Sb7#kL9xQ2wR' simior-blog < blog-server/src/main/resources/simior-blog.sql
docker compose up -d

echo ""
echo "=== 部署完成 ==="
echo "访问地址: http://115.159.54.200"
echo "管理员账号: admin / admin123456"
