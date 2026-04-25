# syntax=docker/dockerfile:1.7

FROM maven:3.9.11-eclipse-temurin-25 AS builder
WORKDIR /app

COPY pom.xml ./
RUN mvn -B -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:25-jdk
WORKDIR /app

COPY --from=builder /app/target/*.jar /app/app.jar

ENV SERVER_PORT=8082 \
    DB_URL=jdbc:postgresql://postgres:5432/user_db \
    DB_USERNAME=postgres \
    DB_PASSWORD=secret \
    JPA_DDL_AUTO=update \
    JPA_SHOW_SQL=false

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/app.jar"]