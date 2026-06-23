# ============================================
# simior-blog 后端服务 Dockerfile
# 多阶段构建：先用 JDK 编译打包，再用 JRE 运行
# ============================================

# --- 构建阶段 ---
FROM eclipse-temurin:26-jdk-alpine AS builder
WORKDIR /app

# 先复制 Maven Wrapper 和 pom.xml，利用 Docker 缓存加速依赖下载
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

# 复制源码并打包
COPY src/ src/
RUN ./mvnw package -DskipTests -q

# --- 运行阶段 ---
FROM eclipse-temurin:26-jre-alpine
LABEL maintainer="simior"

WORKDIR /app

# 从构建阶段复制 JAR 包
COPY --from=builder /app/target/*.jar app.jar

# 设置 JVM 参数（可以通过环境变量覆盖）
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

EXPOSE 8080

# 默认使用 prod 配置文件
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod}"]
