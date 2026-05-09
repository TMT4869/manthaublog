# syntax=docker/dockerfile:1.7

FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -DskipTests dependency:go-offline

COPY src ./src
RUN ./mvnw -B -DskipTests clean package

FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=builder /app/target/*.jar /app/app.jar

ENV SERVER_PORT=8081 \
    DB_HOST=postgres \
    DB_PORT=5432 \
    DB_NAME=auth_db \
    DB_USERNAME=postgres \
    DB_PASSWORD=secret \
    REDIS_HOST=redis \
    REDIS_PORT=6379 \
    REDIS_PASSWORD= \
    RABBITMQ_HOST=rabbitmq \
    RABBITMQ_PORT=5672 \
    RABBITMQ_USERNAME=admin \
    RABBITMQ_PASSWORD=secret \
    MAIL_HOST=smtp.gmail.com \
    MAIL_PORT=587 \
    MAIL_USERNAME= \
    MAIL_PASSWORD= \
    JWT_SECRET=change-me-in-production-use-256-bit-key \
    OAUTH2_REDIRECT_URI=http://localhost:3000/oauth2/callback \
    APP_BASE_URL=http://localhost:8081 \
    GOOGLE_CLIENT_ID= \
    GOOGLE_CLIENT_SECRET= \
    GITHUB_CLIENT_ID= \
    GITHUB_CLIENT_SECRET=

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
