CREATE TABLE users
(
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(120) NOT NULL,
    email        VARCHAR(180) NOT NULL UNIQUE,
    phone_number VARCHAR(30)  NOT NULL,
    created_at   TIMESTAMP    NOT NULL,
    updated_at   TIMESTAMP    NOT NULL
);

CREATE INDEX idx_users_phone_number ON users (phone_number);

CREATE TABLE categories
(
    id     SMALLSERIAL PRIMARY KEY,
    code   VARCHAR(40) NOT NULL UNIQUE,
    name   VARCHAR(80) NOT NULL,
    active BOOLEAN     NOT NULL
);

CREATE TABLE notification_channels
(
    id     SMALLSERIAL PRIMARY KEY,
    code   VARCHAR(40) NOT NULL UNIQUE,
    name   VARCHAR(80) NOT NULL,
    active BOOLEAN     NOT NULL
);

CREATE TABLE user_category_subscriptions
(
    user_id     BIGINT    NOT NULL,
    category_id SMALLINT  NOT NULL,
    created_at  TIMESTAMP NOT NULL,
    PRIMARY KEY (user_id, category_id),
    CONSTRAINT fk_user_category_subscriptions_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_category_subscriptions_category
        FOREIGN KEY (category_id) REFERENCES categories (id)
);

CREATE INDEX idx_user_category_subscriptions_category_id
    ON user_category_subscriptions (category_id);

CREATE INDEX idx_user_category_subscriptions_user_id
    ON user_category_subscriptions (user_id);

CREATE TABLE user_channel_preferences
(
    user_id    BIGINT    NOT NULL,
    channel_id SMALLINT  NOT NULL,
    created_at TIMESTAMP NOT NULL,
    PRIMARY KEY (user_id, channel_id),
    CONSTRAINT fk_user_channel_preferences_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_channel_preferences_channel
        FOREIGN KEY (channel_id) REFERENCES notification_channels (id)
);

CREATE INDEX idx_user_channel_preferences_user_id
    ON user_channel_preferences (user_id);

CREATE INDEX idx_user_channel_preferences_channel_id
    ON user_channel_preferences (channel_id);

CREATE TABLE messages
(
    id          BIGSERIAL PRIMARY KEY,
    category_id SMALLINT      NOT NULL,
    body        VARCHAR(1000) NOT NULL,
    created_at  TIMESTAMP     NOT NULL,
    CONSTRAINT fk_messages_category
        FOREIGN KEY (category_id) REFERENCES categories (id)
);

CREATE INDEX idx_messages_category_id ON messages (category_id);
CREATE INDEX idx_messages_created_at ON messages (created_at);

CREATE TABLE notification_jobs
(
    id            BIGSERIAL PRIMARY KEY,
    message_id    BIGINT      NOT NULL,
    category_id   SMALLINT    NOT NULL,
    status        VARCHAR(30) NOT NULL,
    attempt_count INTEGER     NOT NULL,
    locked_at     TIMESTAMP NULL,
    processed_at  TIMESTAMP NULL,
    last_error    VARCHAR(500) NULL,
    created_at    TIMESTAMP   NOT NULL,
    updated_at    TIMESTAMP   NOT NULL,
    CONSTRAINT fk_notification_jobs_message
        FOREIGN KEY (message_id) REFERENCES messages (id),
    CONSTRAINT fk_notification_jobs_category
        FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT chk_notification_jobs_status
        CHECK (status IN ('PENDING', 'PROCESSING', 'PROCESSED', 'FAILED')),
    CONSTRAINT chk_notification_jobs_attempt_count
        CHECK (attempt_count >= 0)
);

CREATE INDEX idx_notification_jobs_status ON notification_jobs (status);
CREATE INDEX idx_notification_jobs_created_at ON notification_jobs (created_at);
CREATE INDEX idx_notification_jobs_message_id ON notification_jobs (message_id);
CREATE INDEX idx_notification_jobs_category_id ON notification_jobs (category_id);

CREATE TABLE notification_logs
(
    id                     BIGSERIAL PRIMARY KEY,
    message_id             BIGINT       NOT NULL,
    user_id                BIGINT       NOT NULL,
    category_id            SMALLINT     NOT NULL,
    channel_id             SMALLINT     NOT NULL,
    recipient_name         VARCHAR(120) NOT NULL,
    recipient_email        VARCHAR(180) NULL,
    recipient_phone_number VARCHAR(30) NULL,
    status                 VARCHAR(30)  NOT NULL,
    error_message          VARCHAR(500) NULL,
    attempt_count          INTEGER      NOT NULL,
    sent_at                TIMESTAMP NULL,
    created_at             TIMESTAMP    NOT NULL,
    CONSTRAINT fk_notification_logs_message
        FOREIGN KEY (message_id) REFERENCES messages (id),
    CONSTRAINT fk_notification_logs_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_notification_logs_category
        FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT fk_notification_logs_channel
        FOREIGN KEY (channel_id) REFERENCES notification_channels (id),
    CONSTRAINT chk_notification_logs_status
        CHECK (status IN ('PENDING', 'SENT', 'FAILED')),
    CONSTRAINT chk_notification_logs_attempt_count
        CHECK (attempt_count >= 1)
);

CREATE INDEX idx_notification_logs_created_at ON notification_logs (created_at);
CREATE INDEX idx_notification_logs_user_id ON notification_logs (user_id);
CREATE INDEX idx_notification_logs_message_id ON notification_logs (message_id);
CREATE INDEX idx_notification_logs_status ON notification_logs (status);
CREATE INDEX idx_notification_logs_category_id ON notification_logs (category_id);
CREATE INDEX idx_notification_logs_channel_id ON notification_logs (channel_id);