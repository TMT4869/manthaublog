CREATE TABLE IF NOT EXISTS comments (
    id UUID PRIMARY KEY,
    post_id UUID NOT NULL,
    author_id UUID NOT NULL,
    parent_id UUID,
    content TEXT NOT NULL,
    is_deleted BOOLEAN NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_comments_post_id_created_at ON comments (post_id, created_at);
CREATE INDEX IF NOT EXISTS idx_comments_parent_id ON comments (parent_id);

CREATE TABLE IF NOT EXISTS reactions (
    user_id UUID NOT NULL,
    target_id UUID NOT NULL,
    target_type VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (user_id, target_id, target_type)
);

CREATE INDEX IF NOT EXISTS idx_reactions_target ON reactions (target_id, target_type);
