# blog-admin 多阶段构建
FROM node:22-alpine AS builder
WORKDIR /app

COPY blog-admin/package.json blog-admin/
RUN cd blog-admin && npm install --registry=https://registry.npmmirror.com

COPY blog-admin/ blog-admin/
RUN cd blog-admin && npm run build

FROM nginx:alpine
LABEL maintainer="simior"

COPY --from=builder /app/blog-admin/dist /usr/share/nginx/html
COPY deploy/blog-admin/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
