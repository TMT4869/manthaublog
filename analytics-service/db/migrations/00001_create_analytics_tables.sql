-- +goose Up
CREATE TABLE IF NOT EXISTS post_views (
    id BIGSERIAL PRIMARY KEY,
    post_id UUID NOT NULL,
    viewer_id UUID,
    ip_hash VARCHAR(64),
    read_time_seconds INTEGER DEFAULT 0,
    referrer VARCHAR(255),
    viewed_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_post_views_post_id ON post_views (post_id);

CREATE TABLE IF NOT EXISTS post_stats_daily (
    post_id UUID NOT NULL,
    date DATE NOT NULL,
    views INTEGER DEFAULT 0,
    unique_views INTEGER DEFAULT 0,
    avg_read_time_seconds INTEGER DEFAULT 0,
    PRIMARY KEY (post_id, date)
);

-- +goose Down
DROP TABLE IF EXISTS post_stats_daily;
DROP INDEX IF EXISTS idx_post_views_post_id;
DROP TABLE IF EXISTS post_views;
