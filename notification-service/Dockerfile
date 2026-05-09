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

EXPOSE 8086

ENTRYPOINT ["/app/notification-service"]
