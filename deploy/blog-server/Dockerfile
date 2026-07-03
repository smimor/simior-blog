FROM eclipse-temurin:26-jdk-alpine AS builder
WORKDIR /app

COPY blog-server/.mvn/ blog-server/.mvn/
COPY blog-server/mvnw blog-server/pom.xml blog-server/
RUN chmod +x blog-server/mvnw && cd blog-server && ./mvnw dependency:go-offline -q -s .mvn/settings.xml

COPY blog-server/src/ blog-server/src/
RUN cd blog-server && ./mvnw package -DskipTests -q -s .mvn/settings.xml

FROM eclipse-temurin:26-jre-alpine
LABEL maintainer="simior"

WORKDIR /app

COPY --from=builder /app/blog-server/target/*.jar app.jar

ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod}"]
