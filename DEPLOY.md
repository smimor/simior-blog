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

## 一、服务器初始化

### 1.1 系统要求

- 操作系统：CentOS 7+ / Ubuntu 18+ / Debian 9+
- 内存：≥ 2GB
- 磁盘：≥ 10GB
- 开放端口：80

### 1.2 安装 Docker

**CentOS：**

```bash
yum install -y yum-utils git
yum-config-manager --add-repo https://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
sed -i 's/download.docker.com/mirrors.aliyun.com/g' /etc/yum.repos.d/docker-ce.repo
yum install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
systemctl enable --now docker
```

**Ubuntu/Debian：**

```bash
apt-get update && apt-get install -y ca-certificates curl git
curl -fsSL https://get.docker.com | sh
systemctl enable --now docker
apt-get install -y docker-compose-plugin
```

### 1.3 配置 Docker 镜像加速

```bash
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
```

---

## 二、文件清单

```
simior-blog/
├── docker-compose.yml          # Docker 编排（7 个服务）
├── .env.example                # 环境变量模板
├── .env                        # 实际环境变量（不提交 Git）
├── .gitignore
├── DEPLOY.md                   # 本文档
│
├── nginx/
│   ├── nginx.conf              # Nginx 主配置
│   └── conf.d/
│       └── default.conf        # 路由规则（/ → web, /admin → admin, /api/ → server）
│
├── blog-server/                # Spring Boot 后端
│   ├── Dockerfile
│   ├── .mvn/settings.xml       # 阿里云 Maven 镜像
│   └── src/main/resources/
│       ├── application.yml
│       ├── application-prod.yml
│       └── simior-blog.sql     # 数据库初始化脚本
│
├── blog-admin/                 # Vue 3 管理后台
│   ├── Dockerfile
│   ├── nginx.conf              # 容器内 Nginx（SPA fallback）
│   └── src/
│
├── blog-web/                   # Next.js 前台
│   ├── Dockerfile
│   ├── next.config.ts
│   └── src/app/
│
├── scripts/
│   ├── deploy.sh               # 一键部署
│   ├── backup.sh               # 数据库备份
│   └── reset-server.sh         # 重置服务器
│
└── .github/workflows/
    └── deploy.yml              # GitHub Actions CI/CD
```

---

## 三、一键部署

```bash
cd /opt
git clone https://github.com/smimor/simior-blog.git
cd simior-blog
bash scripts/deploy.sh
```

部署完成后访问：
- 前台：`http://服务器IP/`
- 后台：`http://服务器IP/admin`
- API：`http://服务器IP/api/`

默认管理员：`admin / admin123456`

---

## 四、手动部署

### 4.1 配置环境变量

```bash
cp .env.example .env
vim .env
```

### 4.2 导入数据库

```bash
docker compose up -d mysql
sleep 15
docker exec -i simior-mysql mysql -uroot -p'密码' `simior-blog` < blog-server/src/main/resources/simior-blog.sql
```

### 4.3 启动所有服务

```bash
docker compose up -d
```

---

## 五、CI/CD

### GitHub Secrets 配置

| Secret | 说明 |
|--------|------|
| `DOCKER_NAMESPACE` | 阿里云 ACR 命名空间 |
| `DOCKER_USERNAME` | 阿里云 ACR 用户名 |
| `DOCKER_PASSWORD` | 阿里云 ACR 密码 |
| `SERVER_HOST` | 服务器 IP |
| `SERVER_USER` | SSH 用户名 |
| `SERVER_SSH_KEY` | SSH 私钥 |

推送 `main` 分支后自动构建部署。

---

## 六、运维

### 查看日志

```bash
docker compose logs -f
docker compose logs -f blog-server
```

### 重启服务

```bash
docker compose restart
docker compose restart blog-server
```

### 更新部署

```bash
cd /opt/simior-blog
git pull
docker compose up -d --build
```

### 数据库备份

```bash
bash scripts/backup.sh
```

自动备份：每天凌晨 3 点，保留 7 天。

### 恢复备份

```bash
gunzip < backups/simior-blog_20260101_030000.sql.gz | docker exec -i simior-mysql mysql -uroot -p'密码' `simior-blog`
```

### 重置服务器

```bash
bash scripts/reset-server.sh
```

---

## 七、常见问题

### Nginx 502 Bad Gateway

```bash
docker compose ps
docker compose logs blog-server --tail=50
docker compose restart nginx
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
