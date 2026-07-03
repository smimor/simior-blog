# blog-web 多阶段构建
FROM node:22-alpine AS builder
WORKDIR /app

COPY blog-web/package.json blog-web/
RUN cd blog-web && npm install --registry=https://registry.npmmirror.com

COPY blog-web/ blog-web/
RUN cd blog-web && npm run build

FROM node:22-alpine
LABEL maintainer="simior"
WORKDIR /app

ENV NODE_ENV=production
ENV PORT=3000
ENV HOSTNAME="0.0.0.0"

COPY --from=builder /app/blog-web/.next/standalone ./
COPY --from=builder /app/blog-web/.next/static ./.next/static
COPY --from=builder /app/blog-web/public ./public

EXPOSE 3000

CMD ["node", "server.js"]
