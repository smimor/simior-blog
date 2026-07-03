# simior-blog 部署文档

## 架构

```
                        ┌──────────────────┐
     IP:80 ────────────►│  Nginx (alpine)  │
                        │  反向代理 + 路由  │
                        └────────┬─────────┘
                                 │
            ┌────────────────────┼────────────────────┐
            │                    │                    │
   / ──────►│   blog-web        │  blog-admin        │  blog-server
            │   Next.js :3000   │  Vue :80           │  Spring Boot :8080
            └────────┬──────────┘                    └────────┬──────────┘
                     │                                       │
          ┌──────────┴──────────┐              ┌──────────────┴──────────────┐
          │                     │              │                             │
     ┌────▼────┐  ┌────▼────┐  │  ┌────▼────┐  ┌────▼────┐  ┌────▼────┐
     │  MySQL  │  │  Redis  │  │  │   MinIO  │  │  MySQL  │  │  Redis  │
     │  :3306  │  │  :6379  │  │  │  :9000   │  │  :3306  │  │  :6379  │
     └─────────┘  └─────────┘  │  └──────────┘  └─────────┘  └─────────┘
                               │
                        （共享同一套中间件）
```

### 路由规则

| 路径 | 目标 | 说明 |
|------|------|------|
| `http://IP/` | blog-web:3000 | Next.js 前台 |
| `http://IP/admin` | blog-admin:80 | Vue 管理后台 |
| `http://IP/api/*` | blog-server:8080 | Spring Boot API（去除 `/api` 前缀） |

---

## 项目结构

```
simior-blog/
├── blog-admin/              # Vue 管理后台源码
├── blog-server/             # Spring Boot 后端源码
├── blog-web/                # Next.js 前台源码
├── deploy/                  # 所有部署相关文件
│   ├── docker-compose.yml   # 服务编排
│   ├── .env.example         # 环境变量模板
│   ├── blog-admin/
│   │   ├── Dockerfile
│   │   └── nginx.conf       # SPA fallback 配置
│   ├── blog-server/
│   │   └── Dockerfile
│   ├── blog-web/
│   │   └── Dockerfile
│   ├── nginx/
│   │   ├── nginx.conf       # 主配置
│   │   └── conf.d/default.conf  # 反向代理路由
│   └── scripts/
│       ├── deploy.sh        # 一键部署
│       ├── backup.sh        # 数据库备份
│       └── reset-server.sh  # 清理部署
├── .dockerignore
├── .gitignore
├── README.md
└── DEPLOY.md
```

---

## 一、安装环境

### CentOS 7

```bash
# 安装 git
yum install -y git

# 安装 docker
yum install -y yum-utils
yum-config-manager --add-repo https://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
sed -i 's/download.docker.com/mirrors.aliyun.com/g' /etc/yum.repos.d/docker-ce.repo
yum install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
systemctl enable --now docker

# 关闭 SELinux（CentOS 7 默认开启，会导致 Docker 权限问题）
sed -i 's/SELINUX=enforcing/SELINUX=disabled/g' /etc/selinux/config
setenforce 0

# 修改 Docker 存储驱动为 vfs（解决 CentOS 7 XFS 的 EPerM 问题）
systemctl stop docker
mkdir -p /opt/docker
cat > /etc/docker/daemon.json << 'EOF'
{
  "data-root": "/opt/docker",
  "storage-driver": "vfs",
  "registry-mirrors": [
    "https://mirror.ccs.tencentyun.com",
    "https://hub-mirror.c.163.com",
    "https://docker.m.daocloud.io"
  ]
}
EOF
systemctl start docker
```

### Ubuntu / Debian

```bash
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
cd simior-blog/deploy
bash scripts/deploy.sh
```

部署脚本自动完成：
1. 生成 `deploy/.env` 配置文件
2. 启动 MySQL 并导入数据库
3. 构建并启动所有服务
4. 配置定时备份（每天凌晨 3 点）

部署完成后访问：
- 前台：`http://服务器IP/`
- 后台：`http://服务器IP/admin`
- API：`http://服务器IP/api/`

默认管理员：`admin / admin123456`

---

## 三、环境变量

部署脚本自动生成 `deploy/.env`，如需修改可手动编辑：

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `MYSQL_ROOT_PASSWORD` | `Sb7kL9xQ2wR` | MySQL root 密码 |
| `MINIO_ACCESS_KEY` | `admin` | MinIO 访问密钥 |
| `MINIO_SECRET_KEY` | `mK3pL8vN5qT` | MinIO 秘密密钥 |
| `OSS_ENDPOINT` | 空 | 阿里云 OSS 端点（可选） |
| `OSS_ACCESS_KEY` | 空 | 阿里云 OSS AccessKey（可选） |
| `OSS_SECRET_KEY` | 空 | 阿里云 OSS SecretKey（可选） |
| `OSS_BUCKET_NAME` | 空 | 阿里云 OSS Bucket（可选） |

修改后需重新部署：`docker compose up -d --build`

---

## 四、运维

### 查看服务状态

```bash
cd /opt/simior-blog/deploy
docker compose ps
```

### 查看日志

```bash
cd /opt/simior-blog/deploy
docker compose logs -f                    # 所有服务
docker compose logs -f blog-server        # 仅后端
docker compose logs -f nginx              # 仅 Nginx
```

### 重启服务

```bash
cd /opt/simior-blog/deploy
docker compose restart                    # 所有服务
docker compose restart blog-server        # 仅重启后端
```

### 停止服务

```bash
cd /opt/simior-blog/deploy
docker compose down                       # 停止并删除容器
docker compose down -v                    # 同时删除数据卷（数据会丢失）
```

### 更新部署

```bash
cd /opt/simior-blog
git pull
cd deploy
docker compose up -d --build
```

### 数据库备份

```bash
cd /opt/simior-blog/deploy
bash scripts/backup.sh
```

备份文件保存在 `/opt/simior-blog/backups/`，保留 7 天。

自动备份已通过 cron 配置：每天凌晨 3 点执行。

### 清理部署

```bash
cd /opt/simior-blog/deploy
bash scripts/reset-server.sh
```

清理内容：容器、数据卷、项目文件、cron 任务。保留：git、docker。

---

## 五、常见问题

### Nginx 502 Bad Gateway

后端服务未启动或构建失败：

```bash
cd /opt/simior-blog/deploy
docker compose ps
docker compose logs blog-server --tail=50
```

### EPerM 错误（CentOS 7）

Docker 构建时报 `permission denied` 或 `operation not permitted`。

原因：CentOS 7 的 XFS 文件系统不支持 Docker overlay2。

解决：确认 `/etc/docker/daemon.json` 包含 `vfs` 存储驱动：

```json
{
  "data-root": "/opt/docker",
  "storage-driver": "vfs"
}
```

修改后重启 Docker 并清理旧镜像：

```bash
systemctl restart docker
docker system prune -a -f
```

### SELinux 导致容器启动失败

CentOS 7 默认开启 SELinux，可能阻止 Docker 挂载卷。

解决：关闭 SELinux：

```bash
sed -i 's/SELINUX=enforcing/SELINUX=disabled/g' /etc/selinux/config
setenforce 0
```

### Docker 镜像拉取超时（国内网络）

`docker-compose.yml` 中已配置腾讯云、网易、DaoCloud 镜像源。如仍超时，可手动拉取测试：

```bash
docker pull mysql:8.0
docker pull redis:7-alpine
docker pull minio/minio:latest
docker pull eclipse-temurin:26-jdk-alpine
docker pull node:22-alpine
docker pull nginx:alpine
```

### 登录提示"未提供 token"

Nginx 反向代理 `/api/` 时末尾的 `/` 不能省略，否则请求路径不会被正确转发：

```nginx
location /api/ {
    proxy_pass http://blog-server:8080/;  # 末尾的 / 会去除 /api 前缀
}
```

### 数据库连接失败

检查 MySQL 是否健康：

```bash
docker compose ps
docker exec simior-mysql mysql -uroot -p'Sb7kL9xQ2wR' -e "SELECT 1"
```

---

## 六、服务端口一览

| 服务 | 容器名 | 端口 | 说明 |
|------|--------|------|------|
| Nginx | simior-nginx | 80 (宿主机) | 入口，反向代理 |
| MySQL | simior-mysql | 3306 (内部) | 数据库 |
| Redis | simior-redis | 6379 (内部) | 缓存 |
| MinIO | simior-minio | 9000/9001 (内部) | 对象存储 |
| blog-server | simior-server | 8080 (内部) | Spring Boot 后端 |
| blog-admin | simior-admin | 80 (内部) | Vue 管理后台 |
| blog-web | simior-web | 3000 (内部) | Next.js 前台 |
