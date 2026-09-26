CREATE TABLE users
(
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    name              VARCHAR(50)  NOT NULL,
    email             VARCHAR(254) NOT NULL,
    password_hash     VARCHAR(100) NOT NULL,
    bio               VARCHAR(160) NULL,
    avatar_path       VARCHAR(255) NULL,
    email_verified_at DATETIME(6)  NULL,
    created_at        DATETIME(6)  NOT NULL,
    updated_at        DATETIME(6)  NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE user_tokens
(
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    user_id    BIGINT      NOT NULL,
    purpose    VARCHAR(30) NOT NULL,
    token_hash CHAR(64)    NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_user_tokens PRIMARY KEY (id),
    CONSTRAINT uk_user_tokens_hash UNIQUE (token_hash),
    CONSTRAINT fk_user_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX ix_user_tokens_user_purpose ON user_tokens (user_id, purpose);

CREATE TABLE posts
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    user_id    BIGINT       NOT NULL,
    text       VARCHAR(300) NOT NULL,
    video_path VARCHAR(255) NOT NULL,
    created_at DATETIME(6)  NOT NULL,
    CONSTRAINT pk_posts PRIMARY KEY (id),
    CONSTRAINT fk_posts_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX ix_posts_created ON posts (created_at, id);
CREATE INDEX ix_posts_user_created ON posts (user_id, created_at);

CREATE TABLE comments
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    post_id    BIGINT       NOT NULL,
    user_id    BIGINT       NOT NULL,
    text       VARCHAR(500) NOT NULL,
    created_at DATETIME(6)  NOT NULL,
    CONSTRAINT pk_comments PRIMARY KEY (id),
    CONSTRAINT fk_comments_post FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX ix_comments_post_created ON comments (post_id, created_at);

CREATE TABLE likes
(
    post_id    BIGINT      NOT NULL,
    user_id    BIGINT      NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_likes PRIMARY KEY (post_id, user_id),
    CONSTRAINT fk_likes_post FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
    CONSTRAINT fk_likes_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX ix_likes_user ON likes (user_id);

CREATE TABLE follows
(
    follower_id BIGINT      NOT NULL,
    followed_id BIGINT      NOT NULL,
    created_at  DATETIME(6) NOT NULL,
    CONSTRAINT pk_follows PRIMARY KEY (follower_id, followed_id),
    CONSTRAINT ck_follows_not_self CHECK (follower_id <> followed_id),
    CONSTRAINT fk_follows_follower FOREIGN KEY (follower_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_follows_followed FOREIGN KEY (followed_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX ix_follows_followed ON follows (followed_id);
