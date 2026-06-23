FROM eclipse-temurin:26-jdk-alpine AS builder
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q -s .mvn/settings.xml

COPY src/ src/
RUN ./mvnw package -DskipTests -q -s .mvn/settings.xml

FROM eclipse-temurin:26-jre-alpine
LABEL maintainer="simior"

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod}"]
