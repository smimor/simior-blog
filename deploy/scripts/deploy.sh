#!/bin/bash
# ============================================
# simior-blog 一键部署脚本
#
# 用法：bash deploy/scripts/deploy.sh
#
# 本脚本会自动完成（无需任何手动操作）：
#   1. 检查 docker / git 是否已安装（安装方式见 deploy/DEPLOY.md，脚本不代为安装）
#   2. 生成 .env 并自动写入强随机密码（无需手动 cp / 编辑）
#   3. 构建并启动所有服务
#   4. 清理构建产生的悬空镜像
# ============================================

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
DEPLOY_DIR="$(dirname "$SCRIPT_DIR")"
ENV_FILE="$DEPLOY_DIR/.env"

# ---- 输出着色 ----
info()  { printf "\033[36m[INFO]\033[0m  %s\n"  "$*"; }
warn()  { printf "\033[33m[WARN]\033[0m  %s\n"  "$*"; }
error() { printf "\033[31m[ERROR]\033[0m %s\n" "$*" >&2; }

echo "=========================================="
echo "  simior-blog 一键部署"
echo "=========================================="

# ------------------------------------------
# 1. 环境检查：docker / git 必须已安装
#    Docker、Git 的安装与卸载见 deploy/DEPLOY.md，本脚本只做检查、不代为安装
# ------------------------------------------
info "检查运行环境..."

if ! command -v git >/dev/null 2>&1; then
    error "未检测到 git。请先按 deploy/DEPLOY.md「安装 Git」一节安装，再运行本脚本。"
    exit 1
fi

if ! command -v docker >/dev/null 2>&1; then
    error "未检测到 docker。请先按 deploy/DEPLOY.md「安装 Docker」一节安装，再运行本脚本。"
    exit 1
fi

if ! docker compose version >/dev/null 2>&1; then
    error "未检测到 docker compose 插件。请按 deploy/DEPLOY.md 安装 docker-compose-plugin。"
    exit 1
fi

if ! docker info >/dev/null 2>&1; then
    error "Docker 守护进程未运行，请先执行：systemctl start docker"
    exit 1
fi

DOCKER_VER="$(docker --version | awk '{print $3}' | tr -d ',')"
GIT_VER="$(git --version | awk '{print $3}')"
info "环境检查通过：docker=${DOCKER_VER}, git=${GIT_VER}"

# ------------------------------------------
# 2. 生成 .env 并自动写入强随机密码
#    - 不存在：从模板生成，写入随机密码
#    - 存在但仍是默认占位密码：自动替换为随机强密码
#    - 存在且已是自定义密码：沿用，避免覆盖已部署配置
# ------------------------------------------
generate_password() {
    # 24 位字母数字强随机密码，兼容 MySQL / MinIO（不含 / + 等特殊字符）
    head -c 32 /dev/urandom | base64 | tr -dc 'A-Za-z0-9' | head -c 24
}

GENERATED=0
if [ ! -f "$ENV_FILE" ]; then
    info "未找到 .env，从模板生成并自动写入随机密码..."
    cp "$DEPLOY_DIR/.env.example" "$ENV_FILE"
    DB_PWD="$(generate_password)"
    MINIO_PWD="$(generate_password)"
    sed -i "s|^DB_PASSWORD=.*|DB_PASSWORD=${DB_PWD}|"        "$ENV_FILE"
    sed -i "s|^MINIO_SECRET_KEY=.*|MINIO_SECRET_KEY=${MINIO_PWD}|" "$ENV_FILE"
    GENERATED=1
elif grep -q "CHANGE_ME_" "$ENV_FILE"; then
    warn "检测到 .env 中仍存在默认占位密码，自动替换为随机强密码..."
    DB_PWD="$(generate_password)"
    MINIO_PWD="$(generate_password)"
    sed -i "s|^DB_PASSWORD=.*|DB_PASSWORD=${DB_PWD}|"        "$ENV_FILE"
    sed -i "s|^MINIO_SECRET_KEY=.*|MINIO_SECRET_KEY=${MINIO_PWD}|" "$ENV_FILE"
    GENERATED=1
else
    info "检测到已有 .env，沿用现有配置（如需重新生成请删除 $ENV_FILE 后重跑）。"
fi

# ------------------------------------------
# 3. 构建并启动
# ------------------------------------------
info "构建并启动服务（首次构建较慢，请耐心等待）..."
cd "$DEPLOY_DIR/docker"
docker compose up -d --build

# ------------------------------------------
# 4. 清理悬空镜像
#    反复 --build 会积累大量 <none> 中间镜像，长期不清理可能写满磁盘
#    （CentOS 7 用 vfs 存储驱动，空间开销本就比 overlay2 大）
# ------------------------------------------
info "清理构建产生的悬空镜像..."
docker image prune -f >/dev/null

# ------------------------------------------
# 5. 完成
# ------------------------------------------
echo ""
echo "=========================================="
echo "  部署完成！"
echo ""
echo "  访问地址："
echo "    前台:          http://服务器IP/"
echo "    后台:          http://服务器IP/admin"
echo "    API:           http://服务器IP/api/"
echo "    MinIO 控制台:  http://服务器IP:9001"
echo "    默认管理员:    admin / 123456（首次登录请立即修改）"
if [ "$GENERATED" -eq 1 ]; then
    echo ""
    echo "  已自动生成随机密码，请妥善保存（也可在 deploy/.env 中查看）："
    echo "    MySQL root 密码: ${DB_PWD}"
    echo "    MinIO 密码:      ${MINIO_PWD}（账号默认 admin）"
fi
echo "=========================================="
