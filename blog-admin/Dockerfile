# ============================================
# simior-blog 前端管理后台 Dockerfile
# 多阶段构建：先用 Node 编译，再用 Nginx 托管
# ============================================

# --- 构建阶段 ---
FROM node:22-alpine AS builder
WORKDIR /app

RUN corepack enable && corepack prepare pnpm@latest --activate

COPY package.json pnpm-lock.yaml* ./
RUN pnpm install --frozen-lockfile --registry=https://registry.npmmirror.com

COPY . .
RUN pnpm build

# --- 运行阶段 ---
FROM nginx:alpine
LABEL maintainer="simior"

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
