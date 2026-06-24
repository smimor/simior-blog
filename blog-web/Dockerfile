# blog-web: 在宿主机构建，Docker 只复制产物
FROM node:22-alpine
LABEL maintainer="simior"

WORKDIR /app

ENV NODE_ENV=production
ENV PORT=3000
ENV HOSTNAME="0.0.0.0"

COPY . .

EXPOSE 3000

CMD ["node", "server.js"]
