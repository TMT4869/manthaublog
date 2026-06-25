CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(120) NOT NULL,
    description TEXT,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_categories_name ON categories (name);
CREATE UNIQUE INDEX IF NOT EXISTS uk_categories_slug ON categories (slug);

CREATE TABLE IF NOT EXISTS tags (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(120) NOT NULL,
    description TEXT,
    posts_count INTEGER,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_tags_name ON tags (name);
CREATE UNIQUE INDEX IF NOT EXISTS uk_tags_slug ON tags (slug);
CREATE INDEX IF NOT EXISTS idx_tags_posts_count ON tags (posts_count DESC);

CREATE TABLE IF NOT EXISTS posts (
    id UUID PRIMARY KEY,
    author_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(300) NOT NULL,
    content TEXT NOT NULL,
    content_html TEXT,
    excerpt VARCHAR(500),
    cover_image_url VARCHAR(500),
    category_id UUID NOT NULL,
    language VARCHAR(10),
    toc JSONB,
    status VARCHAR(255) NOT NULL,
    reading_time_minutes SMALLINT,
    view_count BIGINT,
    submitted_at TIMESTAMP(6) WITH TIME ZONE,
    reviewed_at TIMESTAMP(6) WITH TIME ZONE,
    reviewed_by UUID,
    rejection_reason TEXT,
    published_at TIMESTAMP(6) WITH TIME ZONE,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_posts_category FOREIGN KEY (category_id) REFERENCES categories (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_posts_slug ON posts (slug);
CREATE INDEX IF NOT EXISTS idx_posts_status ON posts (status);
CREATE INDEX IF NOT EXISTS idx_posts_author_status_published_at ON posts (author_id, status, published_at DESC);
CREATE INDEX IF NOT EXISTS idx_posts_category_status ON posts (category_id, status);

CREATE TABLE IF NOT EXISTS post_tags (
    post_id UUID NOT NULL,
    tag_id UUID NOT NULL,
    PRIMARY KEY (post_id, tag_id),
    CONSTRAINT fk_post_tags_post FOREIGN KEY (post_id) REFERENCES posts (id),
    CONSTRAINT fk_post_tags_tag FOREIGN KEY (tag_id) REFERENCES tags (id)
);

CREATE INDEX IF NOT EXISTS idx_post_tags_tag_id ON post_tags (tag_id);

CREATE TABLE IF NOT EXISTS post_review_history (
    id UUID PRIMARY KEY,
    post_id UUID NOT NULL,
    admin_id UUID NOT NULL,
    action VARCHAR(255) NOT NULL,
    reason TEXT,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_post_review_history_post_created_at
    ON post_review_history (post_id, created_at DESC);
