#!/bin/bash
# ============================================
# simior-blog VPS 初始化部署脚本
# 在全新 Ubuntu/Debian 服务器上运行
# ============================================

set -e

echo "=== simior-blog 部署脚本 ==="

# ---- 1. 安装 Docker ----
if ! command -v docker &> /dev/null; then
    echo "[1/5] 安装 Docker..."
    curl -fsSL https://get.docker.com | sh
    systemctl enable --now docker
else
    echo "[1/5] Docker 已安装"
fi

# ---- 2. 安装 Docker Compose 插件 ----
if ! docker compose version &> /dev/null; then
    echo "[2/5] 安装 Docker Compose 插件..."
    apt-get update && apt-get install -y docker-compose-plugin
else
    echo "[2/5] Docker Compose 已安装"
fi

# ---- 3. 创建部署目录 ----
DEPLOY_DIR="/opt/simior-blog"
echo "[3/5] 创建部署目录 ${DEPLOY_DIR}..."
mkdir -p "${DEPLOY_DIR}"
cd "${DEPLOY_DIR}"

# ---- 4. 拉取代码或更新 ----
if [ -d ".git" ]; then
    echo "[4/5] 更新代码..."
    git pull origin main
else
    echo "[4/5] 克隆代码..."
    git clone https://github.com/YOUR_USERNAME/simior-blog.git .
fi

# ---- 5. 配置环境变量 ----
if [ ! -f ".env" ]; then
    echo "[5/5] 创建 .env 配置文件..."
    cp .env.example .env
    echo ""
    echo "请编辑 .env 文件填写实际的配置值："
    echo "  vim ${DEPLOY_DIR}/.env"
    echo ""
    echo "配置完成后运行："
    echo "  cd ${DEPLOY_DIR} && docker compose up -d"
else
    echo "[5/5] .env 文件已存在"
fi

# ---- 6. 导入数据库（首次部署）----
echo ""
echo "=== 首次部署请手动导入数据库 ==="
echo "docker exec -i simior-mysql mysql -uroot -p\${MYSQL_ROOT_PASSWORD} simior-blog < blog-server/src/main/resources/simior-blog.sql"

echo ""
echo "=== 部署完成 ==="
echo "访问地址: http://$(curl -s ifconfig.me)"
echo "MinIO 控制台: http://$(curl -s ifconfig.me):9001"
