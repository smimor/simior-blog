#!/bin/bash
set -e

echo "=========================================="
echo "  simior-blog CentOS 一键部署"
echo "=========================================="

# ---- 1. 清理旧环境 ----
echo "[1/7] 清理旧环境..."
docker compose down -v --remove-orphans 2>/dev/null || true
docker rm -f simior-mysql simior-redis simior-minio simior-server simior-admin 2>/dev/null || true
docker volume prune -f 2>/dev/null || true
rm -rf /opt/simior-blog

# ---- 2. 安装系统依赖 ----
echo "[2/7] 安装系统依赖..."
yum install -y yum-utils git

# ---- 3. 安装 Docker（阿里云镜像）----
echo "[3/7] 安装 Docker..."
yum-config-manager --add-repo https://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
sed -i 's/download.docker.com/mirrors.aliyun.com/g' /etc/yum.repos.d/docker-ce.repo
yum install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
systemctl enable --now docker

# ---- 4. 配置 Docker 镜像加速 ----
echo "[4/7] 配置 Docker 镜像加速..."
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

# ---- 5. 拉取项目代码 ----
echo "[5/7] 拉取项目代码..."
cd /opt
git clone https://github.com/smimor/simior-blog.git
cd simior-blog

# ---- 6. 生成配置文件 ----
echo "[6/7] 生成配置文件..."
cat > .env << 'EOF'
MYSQL_ROOT_PASSWORD=Sb7kL9xQ2wR
MYSQL_PORT=3306
REDIS_PORT=6379
MINIO_ACCESS_KEY=admin
MINIO_SECRET_KEY=mK3pL8vN5qT
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

# ---- 7. 启动服务 ----
echo "[7/7] 启动服务..."

# 先启动 MySQL
docker compose up -d mysql
echo "等待 MySQL 就绪..."
for i in $(seq 1 30); do
  if docker exec simior-mysql mysql -uroot -p'Sb7kL9xQ2wR' -e "SELECT 1" &>/dev/null; then
    echo "MySQL 已就绪"
    break
  fi
  if [ $i -eq 30 ]; then
    echo "MySQL 启动超时，请检查日志: docker compose logs mysql"
    exit 1
  fi
  sleep 2
done

# 导入数据库
echo "导入数据库..."
docker exec -i simior-mysql mysql -uroot -p'Sb7kL9xQ2wR' simior-blog < blog-server/src/main/resources/simior-blog.sql
echo "数据库导入完成"

# 启动所有服务（首次构建需要下载镜像，请耐心等待）
echo "启动所有服务（首次构建约需 5-10 分钟）..."
docker compose up -d

# 等待后端启动
echo "等待后端服务就绪..."
for i in $(seq 1 30); do
  if curl -s http://localhost:8080/api/ &>/dev/null; then
    echo "后端服务已就绪"
    break
  fi
  sleep 3
done

echo ""
echo "=========================================="
echo "  部署完成！"
echo "  访问地址: http://115.159.54.200"
echo "  管理员账号: admin"
echo "  管理员密码: admin123456"
echo "=========================================="
echo ""
docker compose ps
