# manthaublog

A microservices-based blog platform where users write posts, admins review and publish them, readers follow authors and receive real-time notifications.

## Features

- OAuth2 login (Google, GitHub) and JWT-based session management
- Blog post authoring with admin review/approval workflow
- Threaded comments with emoji reactions
- Author follow system with personalized feed
- Full-text search with autocomplete suggestions
- Real-time in-app notifications via WebSocket
- Media uploads with automatic image resizing (thumbnail / medium / large)
- Post view tracking and reading time analytics

## Tech Stack

| Layer | Technology |
|---|---|
| API Gateway | Go + Gin |
| Auth, User, Post, Comment, Search | Java 25 / Spring Boot 4 |
| Notification, Analytics, Media | Go + Gin |
| Message broker | RabbitMQ |
| Cache & Pub/Sub | Redis |
| Databases | PostgreSQL, MongoDB |
| Search | Elasticsearch |
| Object storage | MinIO (S3-compatible) |
| Real-time | WebSocket + Redis Pub/Sub |
| Containerization | Docker + Docker Compose |

## Prerequisites

- Docker & Docker Compose
- Git

## Getting Started

### 1. Clone the repository

```bash
git clone <repo-url>
cd manthaublog
```

### 2. Configure environment variables

Copy the root `.env.example` and each service's `.env.example`, then fill in the required secrets:

```bash
cp .env.example .env
cp api-gateway/.env.example          api-gateway/.env
cp auth-service/.env.example         auth-service/.env
cp user-service/.env.example         user-service/.env
cp post-service/.env.example         post-service/.env
cp comment-service/.env.example      comment-service/.env
cp search-service/.env.example       search-service/.env
cp notification-service/.env.example notification-service/.env
cp analytics-service/.env.example    analytics-service/.env
cp media-service/.env.example        media-service/.env
```

At minimum, set these values in the root `.env`:

```env
POSTGRES_PASSWORD=your_postgres_password
RABBITMQ_DEFAULT_PASS=your_rabbitmq_password
MINIO_ROOT_PASSWORD=your_minio_password
```

Then set service-specific secrets (JWT secret, OAuth2 client credentials, etc.) in each service's `.env` file.

### 3. Start all services

```bash
docker compose up --build
```

Docker Compose handles startup order automatically. Once all containers are healthy, the API Gateway is available at `http://localhost:8080`.

## Service Ports

| Service | Port |
|---|---|
| API Gateway | 8080 |
| Auth Service | 8081 |
| User Service | 8082 |
| Post Service | 8083 |
| Comment Service | 8084 |
| Search Service | 8085 |
| Notification Service | 8086 |
| Analytics Service | 8087 |
| Media Service | 8088 |
| RabbitMQ Management UI | 15672 |
| MinIO Console | 9001 |
