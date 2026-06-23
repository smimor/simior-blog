#!/bin/bash
# ============================================
# simior-blog 数据库备份脚本
# 建议通过 cron 定时执行：0 3 * * * /opt/simior-blog/scripts/backup.sh
# ============================================

set -e

# ---- 配置 ----
BACKUP_DIR="/opt/simior-blog/backups"
CONTAINER_NAME="simior-mysql"
DATABASE="simior-blog"
RETENTION_DAYS=7
DATE=$(date +%Y%m%d_%H%M%S)

# ---- 创建备份目录 ----
mkdir -p "${BACKUP_DIR}"

# ---- 执行备份 ----
echo "[${DATE}] 开始备份数据库 ${DATABASE}..."
docker exec "${CONTAINER_NAME}" mysqldump -uroot -p"${MYSQL_ROOT_PASSWORD}" \
    --single-transaction --quick --routines --triggers "${DATABASE}" \
    | gzip > "${BACKUP_DIR}/${DATABASE}_${DATE}.sql.gz"

# ---- 删除过期备份 ----
echo "清理 ${RETENTION_DAYS} 天前的备份..."
find "${BACKUP_DIR}" -name "*.sql.gz" -mtime +${RETENTION_DAYS} -delete

# ---- 显示备份结果 ----
BACKUP_FILE="${BACKUP_DIR}/${DATABASE}_${DATE}.sql.gz"
BACKUP_SIZE=$(du -h "${BACKUP_FILE}" | cut -f1)
echo "[${DATE}] 备份完成: ${BACKUP_FILE} (${BACKUP_SIZE})"
