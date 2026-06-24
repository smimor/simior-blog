# simior-blog 部署文档

## 架构

```
                    ┌─────────────────┐
   IP:80 ─────────►│     Nginx       │
                    │  (反向代理)      │
                    └────────┬────────┘
                             │
            ┌────────────────┼────────────────┐
            │                │                │
   / ──────►│  blog-web     │  blog-admin    │  blog-server
            │  (Next.js)    │  (Vue)         │  (Spring Boot)
            │  :3000        │  :80           │  :8080
            └───────┬───────┘                └───────┬───────┘
                    │                                │
         ┌──────────┴──────────┐         ┌───────────┴───────────┐
         │                     │         │                       │
    ┌────▼────┐  ┌────▼────┐  ┌▼────────▼┐  ┌────▼────┐  ┌────▼────┐
    │  MySQL  │  │  Redis  │  │   MinIO  │  │  MySQL  │  │  Redis  │
    │  :3306  │  │  :6379  │  │  :9000   │  │  :3306  │  │  :6379  │
    └─────────┘  └─────────┘  └──────────┘  └─────────┘  └─────────┘
```

### 路由规则

| 路径 | 后端服务 | 说明 |
|------|----------|------|
| `http://IP/` | blog-web:3000 | Next.js 前台 |
| `http://IP/admin` | blog-admin:80 | Vue 管理后台 |
| `http://IP/api/*` | blog-server:8080 | Spring Boot API |

---

## 一、安装环境（CentOS）

```bash
# 安装 git
yum install -y git

# 安装 docker
yum install -y yum-utils
yum-config-manager --add-repo https://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
sed -i 's/download.docker.com/mirrors.aliyun.com/g' /etc/yum.repos.d/docker-ce.repo
yum install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
systemctl enable --now docker
```

## 一、安装环境（Ubuntu/Debian）

```bash
# 安装 git 和 docker
apt-get update && apt-get install -y ca-certificates curl git
curl -fsSL https://get.docker.com | sh
systemctl enable --now docker
apt-get install -y docker-compose-plugin
```

---

## 二、部署

```bash
cd /opt
git clone https://github.com/smimor/simior-blog.git
cd /opt/simior-blog && bash scripts/deploy.sh
```

部署完成后访问：
- 前台：`http://服务器IP/`
- 后台：`http://服务器IP/admin`
- API：`http://服务器IP/api/`

默认管理员：`admin / admin123456`

---

## 三、清理部署

```bash
bash /opt/simior-blog/scripts/reset-server.sh
```

仅清理部署文件（容器、数据卷、项目文件），**保留 git、docker**。

如需彻底卸载 docker：

```bash
# CentOS
yum remove -y docker-ce docker-ce-cli containerd.io
rm -rf /var/lib/docker

# Ubuntu/Debian
apt-get purge -y docker-ce docker-ce-cli containerd.io
rm -rf /var/lib/docker
```

---

## 四、运维

### 查看日志

```bash
docker compose -f /opt/simior-blog/docker-compose.yml logs -f
```

### 重启服务

```bash
docker compose -f /opt/simior-blog/docker-compose.yml restart
```

### 更新部署

```bash
cd /opt/simior-blog
git pull
docker compose up -d --build
```

### 数据库备份

```bash
bash /opt/simior-blog/scripts/backup.sh
```

自动备份：每天凌晨 3 点，保留 7 天。

### 恢复备份

```bash
gunzip < /opt/simior-blog/backups/simior-blog_20260101_030000.sql.gz | docker exec -i simior-mysql mysql -uroot -p'密码' `simior-blog`
```

---

## 五、常见问题

### Nginx 502 Bad Gateway

```bash
docker compose -f /opt/simior-blog/docker-compose.yml ps
docker compose -f /opt/simior-blog/docker-compose.yml logs blog-server --tail=50
```

### 数据库连接失败

```bash
docker exec simior-mysql mysql -uroot -p'密码' -e "SELECT 1"
```

### 中文乱码

确认 JDBC URL 包含 `characterEncoding=utf-8`（注意是 `utf-8` 不是 `utf8`）。

### 登录提示"未提供 token"

确认 nginx.conf 中 `/api/` 代理配置末尾有 `/`：

```nginx
location /api/ {
    proxy_pass http://blog-server:8080/;  # 末尾的 / 不能少
}
```
