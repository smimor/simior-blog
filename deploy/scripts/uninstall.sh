#!/bin/bash
# ============================================
# simior-blog 完整卸载脚本
#
# 用法：bash deploy/scripts/uninstall.sh
#
# 清理范围（除 Docker、Git 本身以外的全部内容）：
#   - 停止并删除所有容器、数据卷、网络
#   - 删除本项目构建/拉取的所有镜像
#   - 兜底清理以 simior- 为前缀的残留容器与本项目数据卷
#   - 删除克隆的项目文件夹（含本脚本）
#
# 保留：Docker、Git 本身（其卸载方式见 deploy/DEPLOY.md）
# ============================================

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
DEPLOY_DIR="$(dirname "$SCRIPT_DIR")"
PROJECT_DIR="$(cd "$DEPLOY_DIR/../.." && pwd)"

# ---- 输出着色 ----
info()  { printf "\033[36m[INFO]\033[0m  %s\n"  "$*"; }
warn()  { printf "\033[33m[WARN]\033[0m  %s\n"  "$*"; }

echo "=========================================="
echo "  simior-blog 完整卸载"
echo "  将删除：容器 / 数据卷 / 网络 / 镜像 / 项目文件夹"
echo "  将保留：Docker、Git 本身"
echo "=========================================="

read -p "确认要彻底卸载并删除所有数据与项目文件？此操作不可恢复 (y/N): " confirm
if [ "$confirm" != "y" ] && [ "$confirm" != "Y" ]; then
    echo "已取消"
    exit 0
fi

# ------------------------------------------
# 1. 通过 compose 一次性清理容器 / 数据卷 / 网络 / 镜像
#    -v           删除数据卷（MySQL、Redis、MinIO 数据）
#    --rmi all    删除本项目用到的所有镜像（构建产物 + 拉取的基础镜像）
#    --remove-orphans 清理孤儿容器
# ------------------------------------------
if command -v docker >/dev/null 2>&1 && docker info >/dev/null 2>&1; then
    info "停止并删除容器、数据卷、网络、镜像..."
    cd "$DEPLOY_DIR/docker"
    docker compose down -v --rmi all --remove-orphans 2>/dev/null || true
else
    warn "Docker 未安装或未运行，跳过容器/镜像清理。"
fi

# ------------------------------------------
# 2. 兜底清理：删除可能残留的容器与数据卷
#    依据 compose 项目标签精确匹配，避免误伤其他项目
# ------------------------------------------
if command -v docker >/dev/null 2>&1 && docker info >/dev/null 2>&1; then
    info "兜底清理残留容器与数据卷..."

    # 以容器名前缀删除残留容器
    for c in $(docker ps -a --filter "name=simior-" --format '{{.Names}}' 2>/dev/null); do
        docker rm -f "$c" >/dev/null 2>&1 || true
    done

    # 按 compose 项目标签删除本项目数据卷（项目名默认为 compose 文件所在目录名 docker）
    for v in $(docker volume ls --filter "label=com.docker.compose.project=docker" --format '{{.Name}}' 2>/dev/null); do
        docker volume rm "$v" >/dev/null 2>&1 || true
    done

    # 清理本项目产生的悬空镜像
    docker image prune -f >/dev/null 2>&1 || true
fi

# ------------------------------------------
# 3. 删除克隆的项目文件夹（含本脚本）
#    先离开目录再删除；脚本已读入内存，删除文件不影响后续执行
# ------------------------------------------
info "删除项目文件夹: $PROJECT_DIR"
cd /tmp
rm -rf "$PROJECT_DIR"

echo ""
echo "=========================================="
echo "  卸载完成！"
echo "  已删除：容器 / 数据卷 / 网络 / 镜像 / 项目文件夹"
echo "  已保留：Docker、Git（卸载方式见 deploy/DEPLOY.md）"
echo "=========================================="
