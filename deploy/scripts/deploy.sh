#!/bin/bash
# ============================================
# 部署脚本
#
# 用法：cd /opt/simior-blog && bash deploy/scripts/deploy.sh
# ============================================

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
DEPLOY_DIR="$(dirname "$SCRIPT_DIR")"

echo "=========================================="
echo "  simior-blog 部署"
echo "=========================================="

# ---- 生成 .env ----
if [ ! -f "$DEPLOY_DIR/.env" ]; then
    cp "$DEPLOY_DIR/.env.example" "$DEPLOY_DIR/.env"
    echo "已生成 .env 配置文件"
    echo "如需修改，请编辑 $DEPLOY_DIR/.env"
fi

# ---- 安全检查：拒绝使用未修改的默认密码 ----
if grep -q "CHANGE_ME_" "$DEPLOY_DIR/.env"; then
    echo ""
    echo "=========================================="
    echo "  错误：检测到 .env 中仍有未修改的默认密码"
    echo "  请先编辑 $DEPLOY_DIR/.env，将 CHANGE_ME_ 开头的值"
    echo "  替换为你自己的强密码，再重新运行本脚本。"
    echo "=========================================="
    exit 1
fi

# ---- 构建并启动 ----
cd "$DEPLOY_DIR/docker"
docker compose up -d --build

# ---- 清理构建产生的悬空镜像 ----
# 反复执行 --build 会积累大量 <none> 中间镜像，长期不清理容易把服务器磁盘写满
# （尤其 CentOS 7 用 vfs 存储驱动，空间开销本来就比 overlay2 大）
echo "清理悬空镜像..."
docker image prune -f

echo ""
echo "=========================================="
echo "  部署完成！"
echo ""
echo "  访问地址："
echo "    前台: http://服务器IP/"
echo "    后台: http://服务器IP/admin"
echo "    API:  http://服务器IP/api/"
echo "=========================================="
