CREATE TABLE IF NOT EXISTS users_auth (
    id UUID PRIMARY KEY,
    username VARCHAR(50),
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255),
    provider VARCHAR(20) NOT NULL,
    provider_id VARCHAR(255),
    is_verified BOOLEAN NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_users_auth_username ON users_auth (username);
CREATE UNIQUE INDEX IF NOT EXISTS uk_users_auth_email ON users_auth (email);
CREATE INDEX IF NOT EXISTS idx_users_auth_provider_provider_id ON users_auth (provider, provider_id);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    revoked BOOLEAN NOT NULL,
    created_at TIMESTAMP(6) NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_refresh_tokens_token_hash ON refresh_tokens (token_hash);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id_revoked ON refresh_tokens (user_id, revoked);

CREATE TABLE IF NOT EXISTS outbox_events (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,
    published_at TIMESTAMP(6),
    created_at TIMESTAMP(6) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_outbox_events_unpublished_created_at
    ON outbox_events (published_at, created_at);
