#!/bin/bash
# ============================================
# 服务器初始化脚本
# 安装 git、docker、docker-compose-plugin
# 用法：bash scripts/setup-server.sh
# ============================================

set -e

echo "=========================================="
echo "  服务器初始化（安装 git、docker）"
echo "=========================================="

# ---- 检测操作系统 ----
if [ -f /etc/centos-release ]; then
    OS="centos"
elif [ -f /etc/debian_version ] || [ -f /etc/lsb-release ]; then
    OS="debian"
else
    echo "不支持的操作系统"
    exit 1
fi

echo "操作系统: $OS"

# ---- 安装 git ----
if ! command -v git &> /dev/null; then
    echo "[1/3] 安装 git..."
    case "$OS" in
        centos) yum install -y git ;;
        debian) apt-get update && apt-get install -y git ;;
    esac
else
    echo "[1/3] git 已安装: $(git --version)"
fi

# ---- 安装 docker ----
if ! command -v docker &> /dev/null; then
    echo "[2/3] 安装 docker..."
    case "$OS" in
        centos)
            yum install -y yum-utils
            yum-config-manager --add-repo https://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
            sed -i 's/download.docker.com/mirrors.aliyun.com/g' /etc/yum.repos.d/docker-ce.repo
            yum install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
            ;;
        debian)
            apt-get install -y ca-certificates curl
            curl -fsSL https://get.docker.com | sh
            apt-get install -y docker-compose-plugin
            ;;
    esac
    systemctl enable --now docker
else
    echo "[2/3] docker 已安装: $(docker --version)"
fi

# ---- 配置镜像加速 ----
echo "[3/3] 配置 Docker 镜像加速..."
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

echo ""
echo "=========================================="
echo "  初始化完成"
echo "  git: $(git --version)"
echo "  docker: $(docker --version)"
echo "  compose: $(docker compose version)"
echo ""
echo "  下一步执行：bash scripts/deploy.sh"
echo "=========================================="
