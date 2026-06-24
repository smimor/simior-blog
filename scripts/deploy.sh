#!/bin/bash
# ============================================
# simior-blog 一键部署脚本（支持 CentOS/Ubuntu/Debian）
# 用法：bash scripts/deploy.sh
# ============================================

set -e

echo "=========================================="
echo "  simior-blog 一键部署"
echo "=========================================="

# ---- 检测操作系统 ----
detect_os() {
    if [ -f /etc/centos-release ]; then
        echo "centos"
    elif [ -f /etc/debian_version ]; then
        echo "debian"
    elif [ -f /etc/lsb-release ]; then
        echo "ubuntu"
    else
        echo "unknown"
    fi
}

OS=$(detect_os)
echo "检测到操作系统: $OS"

# ---- 1. 安装 Docker ----
echo "[1/7] 安装 Docker..."
if ! command -v docker &> /dev/null; then
    case "$OS" in
        centos)
            yum install -y yum-utils git
            yum-config-manager --add-repo https://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
            sed -i 's/download.docker.com/mirrors.aliyun.com/g' /etc/yum.repos.d/docker-ce.repo
            yum install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
            systemctl enable --now docker
            mkdir -p /etc/docker
            cat > /etc/docker/daemon.json << 'EOF'
{
  "registry-mirrors": [
    "https://mirror.ccs.tencentyun.com",
    "https://hub-mirror.c.163.com"
  ]
}
EOF
            systemctl daemon-reload
            systemctl restart docker
            ;;
        ubuntu|debian)
            apt-get update && apt-get install -y ca-certificates curl git
            curl -fsSL https://get.docker.com | sh
            systemctl enable --now docker
            apt-get install -y docker-compose-plugin
            ;;
        *)
            echo "不支持的操作系统: $OS"
            echo "请手动安装 Docker: https://docs.docker.com/engine/install/"
            exit 1
            ;;
    esac
fi

# ---- 2. 拉取代码 ----
echo "[2/7] 拉取项目代码..."
cd /opt && rm -rf simior-blog
git clone https://github.com/smimor/simior-blog.git
cd simior-blog

# ---- 3. 生成配置文件 ----
echo "[3/7] 生成配置文件..."
cat > .env << 'EOF'
MYSQL_ROOT_PASSWORD=Sb7kL9xQ2wR
MINIO_ACCESS_KEY=admin
MINIO_SECRET_KEY=mK3pL8vN5qT
EOF

cat > blog-web/.env << 'EOF'
NEXT_PUBLIC_API_URL=/api
NEXT_PUBLIC_SITE_NAME=Simior Blog
EOF

# ---- 4. 导入数据库 ----
echo "[4/7] 导入数据库..."
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

# ---- 5. 启动服务 ----
echo "[5/7] 启动所有服务..."
docker compose up -d

# ---- 6. 配置定时备份 ----
echo "[6/7] 配置定时备份..."
chmod +x scripts/backup.sh
(crontab -l 2>/dev/null; echo "0 3 * * * /opt/simior-blog/scripts/backup.sh >> /var/log/simior-backup.log 2>&1") | crontab -

# ---- 7. 完成 ----
echo "[7/7] 部署完成"
echo ""
echo "=========================================="
echo "  部署完成！"
echo ""
echo "  访问地址（通过 IP 访问）："
echo "    前台: http://服务器IP/"
echo "    后台: http://服务器IP/admin"
echo "    API:  http://服务器IP/api/"
echo ""
echo "  默认管理员: admin / admin123456"
echo "=========================================="
