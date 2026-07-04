# simior-blog

全栈博客系统，包含 Java 后端、Vue 管理后台和 Next.js 前台。

## 技术栈

| 组件 | 技术 |
|------|------|
| 后端 | Spring Boot 4.1 + MyBatis-Plus + Sa-Token |
| 管理后台 | Vue 3 + TypeScript + Vite + Element Plus |
| 前台 | Next.js + React |
| 数据库 | MySQL |
| 缓存 | Redis |
| 存储 | MinIO |
| 部署 | Docker Compose + Nginx |

## 快速部署

### 1. 克隆项目

```bash
git clone https://github.com/smimor/simior-blog.git
cd simior-blog
```

### 2. 配置环境变量

```bash
cd deploy
cp .env.example .env
nano .env  # 修改密码等敏感配置
```

### 3. 启动服务

```bash
cd deploy/docker
docker compose up -d
```

首次启动会自动导入数据库，无需手动操作。

### 4. 访问

- 前台：`http://YOUR_SERVER_IP/`
- 后台：`http://YOUR_SERVER_IP/admin`
- API：`http://YOUR_SERVER_IP/api/`
- MinIO 控制台：`http://YOUR_SERVER_IP:9001`

默认管理员：`admin / 123456`

## 项目结构

```
simior-blog/
├── blog-server/              # Spring Boot 后端
├── blog-admin/               # Vue 管理后台
├── blog-web/                 # Next.js 前台
├── deploy/                   # 部署配置
│   ├── .env.example          # 环境变量模板
│   ├── docker/               # Docker 相关
│   ├── nginx/                # Nginx 配置
│   └── scripts/              # 部署脚本
├── .dockerignore
└── .gitignore
```

详细部署文档请查看 [deploy/DEPLOY.md](deploy/DEPLOY.md)

## License

MIT
