#!/bin/bash
# ============================================
# 部署脚本（在 deploy/ 目录下执行）
# 用法：cd /opt/simior-blog/deploy && bash scripts/deploy.sh
# ============================================

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
DEPLOY_DIR="$(dirname "$SCRIPT_DIR")"
PROJECT_DIR="$(dirname "$DEPLOY_DIR")"

echo "=========================================="
echo "  simior-blog 部署"
echo "=========================================="

# ---- 前置检查 ----
if ! command -v docker &> /dev/null; then
    echo "错误：docker 未安装"
    exit 1
fi

if [ ! -f "$DEPLOY_DIR/docker-compose.yml" ]; then
    echo "错误：请在 deploy/ 目录下执行此脚本"
    exit 1
fi

cd "$DEPLOY_DIR"

# ---- 生成 .env ----
echo "[1/4] 生成配置文件..."
cat > .env << 'EOF'
MYSQL_ROOT_PASSWORD=Sb7kL9xQ2wR
MINIO_ACCESS_KEY=admin
MINIO_SECRET_KEY=mK3pL8vN5qT
EOF

# ---- 生成 blog-web/.env ----
cat > "$PROJECT_DIR/blog-web/.env" << 'EOF'
NEXT_PUBLIC_API_URL=/api
NEXT_PUBLIC_SITE_NAME=Simior Blog
EOF

# ---- 导入数据库 ----
echo "[2/4] 导入数据库..."
docker compose up -d mysql
echo "等待 MySQL 就绪..."
for i in $(seq 1 30); do
    if docker exec simior-mysql mysql -uroot -p'Sb7kL9xQ2wR' -e "SELECT 1" &>/dev/null; then
        echo "MySQL 已就绪"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "MySQL 启动超时"
        exit 1
    fi
    sleep 2
done

docker exec -i simior-mysql mysql -uroot -p'Sb7kL9xQ2wR' -D "simior-blog" < "$PROJECT_DIR/blog-server/src/main/resources/simior-blog.sql"
echo "数据库导入完成"

# ---- 构建并启动服务 ----
echo "[3/4] 构建并启动所有服务..."
docker compose up -d --build

# ---- 配置定时备份 ----
echo "[4/4] 配置定时备份..."
chmod +x "$SCRIPT_DIR/backup.sh"
(crontab -l 2>/dev/null | grep -v "simior-blog"; echo "0 3 * * * $SCRIPT_DIR/backup.sh >> /var/log/simior-backup.log 2>&1") | crontab -

echo ""
echo "=========================================="
echo "  部署完成！"
echo ""
echo "  访问地址："
echo "    前台: http://服务器IP/"
echo "    后台: http://服务器IP/admin"
echo "    API:  http://服务器IP/api/"
echo ""
echo "  默认管理员: admin / admin123456"
echo "=========================================="
