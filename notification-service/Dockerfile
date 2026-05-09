# syntax=docker/dockerfile:1.7

FROM golang:1.25-alpine AS builder
WORKDIR /app

# Copy all sources — go mod tidy generates go.sum on first build
COPY . .
RUN go mod tidy && CGO_ENABLED=0 GOOS=linux go build -trimpath -o notification-service .

FROM alpine:3.21
WORKDIR /app
RUN apk --no-cache add ca-certificates tzdata

COPY --from=builder /app/notification-service .

ENV SERVER_PORT=8086 \
    DATABASE_URL=postgres://postgres:secret@postgres:5432/notification_db \
    REDIS_HOST=redis \
    REDIS_PORT=6379 \
    REDIS_PASSWORD= \
    RABBITMQ_HOST=rabbitmq \
    RABBITMQ_PORT=5672 \
    RABBITMQ_USERNAME=admin \
    RABBITMQ_PASSWORD=secret \
    AUTH_SERVICE_URL=http://auth-service:8081 \
    SMTP_HOST=smtp.gmail.com \
    SMTP_PORT=587 \
    SMTP_USER= \
    SMTP_PASSWORD=

EXPOSE 8086

ENTRYPOINT ["/app/notification-service"]
