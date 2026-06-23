FROM node:22-alpine AS builder
WORKDIR /app

RUN npm install -g pnpm@latest --registry=https://registry.npmmirror.com

COPY package.json pnpm-lock.yaml* ./
RUN pnpm install --frozen-lockfile --registry=https://registry.npmmirror.com

COPY . .
RUN pnpm build

FROM nginx:alpine
LABEL maintainer="simior"

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
