# syntax=docker/dockerfile:1.7

FROM golang:1.25-alpine AS builder
WORKDIR /app

COPY . .
RUN go mod tidy && CGO_ENABLED=0 GOOS=linux go build -trimpath -o analytics-service .

FROM alpine:3.21
WORKDIR /app
RUN apk --no-cache add ca-certificates tzdata

COPY --from=builder /app/analytics-service .

EXPOSE 8087

ENTRYPOINT ["/app/analytics-service"]
