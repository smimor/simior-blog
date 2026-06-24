# ============================================
# blog-web Next.js 前台 Dockerfile
# 多阶段构建：先用 Node 编译，再用 Node 运行（standalone 模式）
# ============================================

# --- 构建阶段 ---
FROM node:22-alpine AS builder
WORKDIR /app

COPY package.json package-lock.json* ./
RUN npm install --registry=https://registry.npmmirror.com

COPY . .
RUN chmod -R 777 /app
RUN npm run build

# --- 运行阶段 ---
FROM node:22-alpine
LABEL maintainer="simior"

WORKDIR /app

ENV NODE_ENV=production
ENV PORT=3000
ENV HOSTNAME="0.0.0.0"

COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static
COPY --from=builder /app/public ./public

EXPOSE 3000

CMD ["node", "server.js"]
