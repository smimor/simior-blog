#!/bin/bash
# ============================================
# 部署脚本（需要先克隆代码并进入目录）
# 用法：cd /opt/simior-blog && bash scripts/deploy.sh
# ============================================

set -e

echo "=========================================="
echo "  simior-blog 部署"
echo "=========================================="

# ---- 前置检查 ----
if ! command -v docker &> /dev/null; then
    echo "错误：docker 未安装"
    exit 1
fi

if [ ! -f "docker-compose.yml" ]; then
    echo "错误：请在项目根目录下执行此脚本"
    exit 1
fi

# ---- 安装 Node.js（用于构建前端）----
echo "[1/6] 检查 Node.js..."
if ! command -v node &> /dev/null; then
    echo "安装 Node.js 22..."
    curl -fsSL https://rpm.nodesource.com/setup_22.x | bash -
    yum install -y nodejs
fi
echo "Node: $(node -v), npm: $(npm -v)"

# ---- 生成配置文件 ----
echo "[2/6] 生成配置文件..."
cat > .env << 'EOF'
MYSQL_ROOT_PASSWORD=Sb7kL9xQ2wR
MINIO_ACCESS_KEY=admin
MINIO_SECRET_KEY=mK3pL8vN5qT
EOF

cat > blog-web/.env << 'EOF'
NEXT_PUBLIC_API_URL=/api
NEXT_PUBLIC_SITE_NAME=Simior Blog
EOF

# ---- 构建前端（在宿主机上，避免 Docker EPerM）----
echo "[3/6] 构建 blog-admin..."
cd blog-admin
npm install --registry=https://registry.npmmirror.com
npm run build
cd ..

echo "[4/6] 构建 blog-web..."
cd blog-web
npm install --registry=https://registry.npmmirror.com
npm run build
cd ..

# ---- 导入数据库 ----
echo "[5/6] 导入数据库..."
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

docker exec -i simior-mysql mysql -uroot -p'Sb7kL9xQ2wR' `simior-blog` < blog-server/src/main/resources/simior-blog.sql
echo "数据库导入完成"

# ---- 启动服务 ----
echo "[6/6] 启动所有服务..."
docker compose up -d --build

# ---- 配置定时备份 ----
chmod +x scripts/backup.sh
(crontab -l 2>/dev/null; echo "0 3 * * * /opt/simior-blog/scripts/backup.sh >> /var/log/simior-backup.log 2>&1") | crontab -

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
