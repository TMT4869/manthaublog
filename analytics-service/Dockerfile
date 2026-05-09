# syntax=docker/dockerfile:1.7

FROM golang:1.25-alpine AS builder
WORKDIR /app

COPY . .
RUN go mod tidy && CGO_ENABLED=0 GOOS=linux go build -trimpath -o analytics-service .

FROM alpine:3.21
WORKDIR /app
RUN apk --no-cache add ca-certificates tzdata

COPY --from=builder /app/analytics-service .

ENV SERVER_PORT=8087 \
    DATABASE_URL=postgres://postgres:secret@postgres:5432/analytics_db \
    REDIS_HOST=redis \
    REDIS_PORT=6379 \
    REDIS_PASSWORD= \
    RABBITMQ_HOST=rabbitmq \
    RABBITMQ_PORT=5672 \
    RABBITMQ_USERNAME=admin \
    RABBITMQ_PASSWORD=secret

EXPOSE 8087

ENTRYPOINT ["/app/analytics-service"]
