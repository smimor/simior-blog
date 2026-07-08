# ============================================
# blog-server 后端 Dockerfile
#
# 多阶段构建（分两步）：
#   第 1 步（builder）：用 JDK 编译 Java 代码，生成 jar 包
#   第 2 步（运行）：只用 JRE 运行 jar 包，镜像更小更安全
#
# 构建命令：docker build -f deploy/docker/Dockerfile -t simior-server .
# ============================================

# ---- 第 1 步：编译构建 ----
# 基础镜像：包含完整 JDK（编译工具）
# 注意：必须与 pom.xml 中 <java.version> 保持一致的大版本号！
# eclipse-temurin:latest 目前指向的是最新 LTS（25），而不是 26，
# 用它编译 java.version=26 的项目会在 mvnw package 阶段直接报错。
FROM eclipse-temurin:26-jdk AS builder
WORKDIR /app

# 复制 Maven 配置文件（用于下载依赖）
COPY blog-server/.mvn/ blog-server/.mvn/
COPY blog-server/mvnw blog-server/pom.xml blog-server/

# 下载所有依赖（单独一层，利用 Docker 缓存）
# 如果 pom.xml 没变，下次构建会跳过这一步，加速构建
RUN chmod +x blog-server/mvnw && cd blog-server && ./mvnw dependency:go-offline -q -s .mvn/settings.xml

# 复制 Java 源码并编译打包
COPY blog-server/src/ blog-server/src/
RUN cd blog-server && ./mvnw package -DskipTests -q -s .mvn/settings.xml

# ---- 第 2 步：运行 ----
# 基础镜像：只有 JRE（运行环境），没有编译工具，体积更小
# 同样钉死版本号，且用 -jre 变体（比 builder 用的 -jdk 更小、攻击面更小）
FROM eclipse-temurin:26-jre
LABEL maintainer="simior"

# 创建非 root 用户运行应用
RUN addgroup --system app && adduser --system --ingroup app app

WORKDIR /app

# 从构建阶段复制编译好的 jar 包
COPY --from=builder --chown=app:app /app/blog-server/target/*.jar app.jar

# JVM 参数：
#   -Xms256m  = 初始堆内存 256MB
#   -Xmx512m  = 最大堆内存 512MB
#   -XX:+UseG1GC = 使用 G1 垃圾回收器（适合大内存）
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

EXPOSE 8080

USER app

# 启动命令：
#   $JAVA_OPTS    = 上面定义的 JVM 参数
#   --spring.profiles.active=prod = 使用生产环境配置
#   如果没设置 SPRING_PROFILES_ACTIVE 环境变量，默认用 prod
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod}"]
