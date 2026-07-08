# simior-blog 部署文档

## 架构

```
浏览器 → Nginx(:80) → blog-web(:3000)      [访问 /]
                     → blog-admin(:80)      [访问 /admin]
                     → blog-server(:8080)   [访问 /api/]
                          ↓
               MySQL(:3306) + Redis(:6379) + MinIO(:9000)
```

## 项目结构

```
simior-blog/
├── blog-web/                       # Next.js 前台（standalone 模式，自带服务器）
│   ├── .env                        # 通用环境变量
│   ├── .env.development            # 开发环境变量
│   └── .env.production             # 生产环境变量
├── blog-admin/                     # Vue 管理后台
├── blog-server/                    # Spring Boot 后端
├── deploy/
│   ├── .env.example                # 环境变量模板
│   ├── docker/
│   │   ├── docker-compose.yml
│   │   ├── Dockerfile              # blog-server
│   │   ├── Dockerfile.admin        # blog-admin（内含 Nginx）
│   │   └── Dockerfile.web          # blog-web（standalone，无 Nginx）
│   ├── nginx/
│   │   ├── nginx.conf              # 反向代理入口
│   │   └── nginx-admin.conf        # blog-admin 容器内部配置
│   └── scripts/
│       ├── deploy.sh               # 部署
│       └── reset-server.sh         # 重置数据
└── .github/workflows/deploy.yml    # GitHub Actions 自动部署
```

---

## 一、安装环境（CentOS 7）

以下命令逐条复制到服务器执行。

### 1. 安装 Docker

```bash
yum install -y yum-utils
yum-config-manager --add-repo https://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
sed -i 's/download.docker.com/mirrors.aliyun.com/g' /etc/yum.repos.d/docker-ce.repo
yum install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
systemctl enable --now docker
```

验证：

```bash
docker --version
docker compose version
```

### 2. 配置 Docker 存储驱动

CentOS 7 的 XFS 不支持 overlay2，必须改为 vfs：

```bash
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

验证：

```bash
docker info | grep "Storage Driver"
# 输出 Storage Driver: vfs
```

### 3. 关闭 SELinux

```bash
sed -i 's/SELINUX=enforcing/SELINUX=disabled/g' /etc/selinux/config
setenforce 0
```

### 4. 安装 Git

```bash
yum install -y git
```

---

## 二、部署项目

### 1. 克隆代码

```bash
cd /opt
git clone https://github.com/smimor/simior-blog.git
cd simior-blog
```

### 2. 执行部署

```bash
bash deploy/scripts/deploy.sh
```

### 3. 访问

| 地址 | 说明 |
|------|------|
| `http://<your-server-ip>/` | 博客前台 |
| `http://<your-server-ip>/admin` | 管理后台 |
| `http://<your-server-ip>/api/` | 后端 API |
| `http://<your-server-ip>:9001` | MinIO 控制台 |

默认管理员：`admin / 123456`（**首次登录后请立即修改密码**，该账号密码是种子数据里的公开默认值）

> 服务器 IP 等具体环境信息不建议写进仓库文档，请自行记录在本地或密码管理器中，
> 避免随代码一起提交、泄露到 Git 历史。

---

## 三、自动部署（GitHub Actions）

在 GitHub 仓库 **Settings > Secrets and variables > Actions** 添加：

| Secret | 值 |
|--------|-----|
| `SERVER_HOST` | 服务器 IP |
| `SERVER_USER` | `root` |
| `SERVER_PASSWORD` | SSH 密码 |

之后推送代码到 main 分支自动部署。

---

## 四、环境变量

编辑 `deploy/.env`：

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `DB_PASSWORD` | `CHANGE_ME_DB_PASSWORD` | MySQL 密码 |
| `DB_PORT_EXPOSE` | 空 | MySQL 端口映射（留空=不暴露） |
| `REDIS_PORT_EXPOSE` | 空 | Redis 端口映射 |
| `MINIO_ACCESS_KEY` | `admin` | MinIO 密钥 |
| `MINIO_SECRET_KEY` | `CHANGE_ME_MINIO_SECRET` | MinIO 密码 |
| `MINIO_API_PORT` | `9000` | MinIO API 端口 |
| `MINIO_CONSOLE_PORT` | `9001` | MinIO 控制台端口 |
| `BLOG_UPLOAD_MODE` | `minio` | 上传模式 |
| `CORS_ALLOWED_ORIGINS` | `*` | 跨域来源 |
| `NGINX_PORT` | `80` | Nginx 端口 |

修改后重新部署：

```bash
cd /opt/simior-blog/deploy/docker
docker compose up -d --build
```

---

## 五、重置数据

删除所有容器和数据卷（MySQL、Redis、MinIO 数据全部清空）：

```bash
cd /opt/simior-blog
bash deploy/scripts/reset-server.sh
```

重置后重新部署：

```bash
cd /opt/simior-blog
bash deploy/scripts/deploy.sh
```

---

## 六、常用命令

```bash
cd /opt/simior-blog/deploy/docker

# 查看状态
docker compose ps

# 查看日志
docker compose logs -f
docker compose logs -f blog-server

# 重启
docker compose restart
docker compose restart blog-server

# 停止
docker compose down
```

---

## 七、卸载 Docker

```bash
systemctl stop docker
yum remove -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
rm -rf /opt/docker
rm -rf /etc/docker
```

## 八、卸载 Git

```bash
yum remove -y git
```

---

## 九、常见问题

### Nginx 502 Bad Gateway

```bash
cd /opt/simior-blog/deploy/docker
docker compose ps
docker compose logs blog-server --tail=50
```

### Docker 构建 EPERM 错误

```bash
docker info | grep "Storage Driver"
# 必须是 vfs
```

### 镜像拉取超时

已配置国内镜源。如仍超时手动测试：

```bash
docker pull mysql:latest
docker pull redis:latest
docker pull minio/minio:latest
docker pull eclipse-temurin:latest
docker pull node:latest
docker pull nginx:latest
```

### 数据库连接失败

```bash
cd /opt/simior-blog/deploy/docker
docker compose ps
grep DB_PASSWORD ../.env
```

---

## 十、端口一览

| 服务 | 容器名 | 端口 | 说明 |
|------|--------|------|------|
| Nginx | simior-nginx | **80** | 入口 |
| MySQL | simior-mysql | 3306（可暴露） | 数据库 |
| Redis | simior-redis | 6379（可暴露） | 缓存 |
| MinIO | simior-minio | 9000/9001 | 文件存储 |
| blog-server | simior-server | 8080（内部） | 后端 |
| blog-admin | simior-admin | 80（内部） | 管理后台（内含 Nginx） |
| blog-web | simior-web | 3000（内部） | 前台（standalone，无 Nginx） |
