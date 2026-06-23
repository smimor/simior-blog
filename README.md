# simior-blog

全栈博客系统，包含 Java 后端和 Vue 3 管理后台。

## 技术栈

| 组件 | 技术 |
|------|------|
| 后端 | Spring Boot 4.1 + MyBatis-Plus + Sa-Token |
| 前端 | Vue 3 + TypeScript + Vite + Element Plus |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis 7 |
| 存储 | MinIO |
| 部署 | Docker Compose |

## 快速部署

### 1. 克隆项目

```bash
git clone https://github.com/YOUR_USERNAME/simior-blog.git
cd simior-blog
```

### 2. 配置环境变量

```bash
cp .env.example .env
vim .env  # 修改密码等敏感配置
```

### 3. 导入数据库

```bash
docker compose up -d mysql
docker exec -i simior-mysql mysql -uroot -proot123456 simior-blog < blog-server/src/main/resources/simior-blog.sql
```

### 4. 启动服务

```bash
docker compose up -d
```

访问地址：`http://YOUR_SERVER_IP`

默认管理员账号：`admin` / `admin123456`

## 一键部署（全新服务器）

```bash
curl -fsSL https://raw.githubusercontent.com/YOUR_USERNAME/simior-blog/main/scripts/deploy.sh | bash
```

## 数据库备份

```bash
# 手动备份
bash scripts/backup.sh

# 定时备份（每天凌晨3点）
crontab -e
0 3 * * * /opt/simior-blog/scripts/backup.sh >> /var/log/simior-backup.log 2>&1
```

## CI/CD 配置

在 GitHub 仓库 Settings > Secrets 中添加：

| Secret | 说明 |
|--------|------|
| `DOCKER_REGISTRY` | 阿里云容器镜像仓库地址 |
| `DOCKER_USERNAME` | 阿里云容器镜像仓库用户名 |
| `DOCKER_PASSWORD` | 阿里云容器镜像仓库密码 |
| `SERVER_HOST` | VPS 服务器 IP |
| `SERVER_USER` | VPS SSH 用户名 |
| `SERVER_SSH_KEY` | VPS SSH 私钥 |

推送到 `main` 分支后自动构建并部署。

## 项目结构

```
simior-blog/
├── blog-server/          # Spring Boot 后端
├── blog-admin/           # Vue 3 管理后台
├── docker-compose.yml    # Docker 编排
├── .env.example          # 环境变量模板
├── scripts/
│   ├── deploy.sh         # 部署脚本
│   └── backup.sh         # 备份脚本
└── .github/workflows/
    └── deploy.yml        # CI/CD 流水线
```

## License

MIT
