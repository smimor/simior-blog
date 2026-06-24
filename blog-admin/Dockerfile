# blog-admin: 在宿主机构建，Docker 只复制产物
FROM nginx:alpine
LABEL maintainer="simior"

COPY dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
