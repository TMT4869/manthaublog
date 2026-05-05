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

ENV SERVER_PORT=8082 \
    DB_URL=jdbc:postgresql://postgres:5432/user_db \
    DB_USERNAME=postgres \
    DB_PASSWORD=secret \
    JPA_DDL_AUTO=update \
    JPA_SHOW_SQL=false

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/app.jar"]