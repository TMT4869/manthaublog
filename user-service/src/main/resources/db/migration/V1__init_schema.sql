CREATE TABLE IF NOT EXISTS user_profiles (
    id UUID PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    name_tag VARCHAR(6),
    bio TEXT,
    avatar_url VARCHAR(500),
    website VARCHAR(255),
    location VARCHAR(100),
    followers_count INTEGER NOT NULL,
    following_count INTEGER NOT NULL,
    posts_count INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP(6),
    updated_at TIMESTAMP(6)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_user_profiles_username ON user_profiles (username);
CREATE UNIQUE INDEX IF NOT EXISTS uk_user_profiles_display_name_tag
    ON user_profiles (display_name, name_tag);

CREATE TABLE IF NOT EXISTS follows (
    follower_id UUID NOT NULL,
    following_id UUID NOT NULL,
    created_at TIMESTAMP(6),
    PRIMARY KEY (follower_id, following_id)
);

CREATE INDEX IF NOT EXISTS idx_follows_follower_id ON follows (follower_id);
CREATE INDEX IF NOT EXISTS idx_follows_following_id ON follows (following_id);
