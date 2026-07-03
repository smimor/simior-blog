#!/bin/bash
# ============================================
# simior-blog 数据库备份脚本
# 用法：bash deploy/scripts/backup.sh
# 定时备份：0 3 * * * /opt/simior-blog/deploy/scripts/backup.sh
# ============================================

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR/.."

# 加载环境变量
source .env 2>/dev/null || true

BACKUP_DIR="/opt/simior-blog/backups"
MYSQL_PASSWORD="${MYSQL_ROOT_PASSWORD:-Sb7kL9xQ2wR}"
RETENTION_DAYS=7
DATE=$(date +%Y%m%d_%H%M%S)

mkdir -p "${BACKUP_DIR}"

echo "[$(date)] 开始备份数据库..."

docker exec simior-mysql mysqldump \
    -uroot -p"${MYSQL_PASSWORD}" \
    --single-transaction \
    --quick \
    --routines \
    --triggers \
    `simior-blog` | gzip > "${BACKUP_DIR}/simior-blog_${DATE}.sql.gz"

# 删除过期备份
find "${BACKUP_DIR}" -name "*.sql.gz" -mtime +${RETENTION_DAYS} -delete

BACKUP_SIZE=$(du -h "${BACKUP_DIR}/simior-blog_${DATE}.sql.gz" | cut -f1)
echo "[$(date)] 备份完成: simior-blog_${DATE}.sql.gz (${BACKUP_SIZE})"
