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

ENV SERVER_PORT=8085 \
    ELASTICSEARCH_URIS=http://elasticsearch:9200 \
    REDIS_HOST=redis \
    REDIS_PORT=6379 \
    RABBITMQ_HOST=rabbitmq \
    RABBITMQ_PORT=5672 \
    RABBITMQ_USER=admin \
    RABBITMQ_PASS=secret \
    POST_SERVICE_URL=http://post-service:8083

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
